package iam.thevoid.mediapicker.picker

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import iam.thevoid.mediapicker.bus.MediaPickerBus
import iam.thevoid.mediapicker.util.FileUtil
import iam.thevoid.noxml.core.mvvm.activityViewModel
import java.io.File

class HiddenPickerFragment : Fragment(), ImageReceiver {

    private val vm by activityViewModel<HiddenPickerViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startPick(arguments)
    }

    override fun onResume() {
        super.onResume()
        vm.imageReceiver = this
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_CANCELED) {
            onDismiss()
        } else {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == Purpose.REQUEST_TAKE_PHOTO && FileUtil.getPhotoPath(activity).isNotEmpty()) {
                    (FileUtil.generatePathForPhotoIntent(activity)?.let(::File)?.let(Uri::fromFile)
                            ?: Uri.parse("")).also(vm::onImagePicked)
                } else if (data != null) {
                    if (data.data != null) {
                        vm.onImagePicked(data.data)
                    } else if (requestCode == Purpose.REQUEST_TAKE_PHOTO) {
                        vm.fetchPhotoUriFromIntent(data)
                    }
                } else {
                    onImagePickFinish(null)
                }
            } else {
                onImagePickFinish(null)
            }
        }
    }

    private fun startPick(bundle: Bundle?) =
            startActivityForResult(bundle?.getParcelable(Picker.EXTRA_INTENT),
                    bundle?.getInt(Picker.EXTRA_REQUEST_CODE, 0) ?: 0)

    override fun onDismiss() {
        MediaPickerBus.onDismiss()
        popBackStack()
    }

    override fun onImagePickFinish(uri: Uri?) {
        MediaPickerBus.onImagePicked(uri)
        popBackStack()
    }

    private fun popBackStack() {
        activity?.supportFragmentManager?.apply {
            val fragment = findFragmentByTag(HiddenPickerFragment::class.java.canonicalName)
            if (fragment != null) {
                beginTransaction()
                        .remove(fragment)
                        .commitAllowingStateLoss()
            }
        }
    }

    companion object {

        @JvmStatic
        fun getFragment(intent: Intent?, requestCode: Int): Fragment  = HiddenPickerFragment().apply{
            arguments = Bundle().apply {
                putParcelable(Picker.EXTRA_INTENT, intent)
                putInt(Picker.EXTRA_REQUEST_CODE, requestCode)
            }
        }
    }
}