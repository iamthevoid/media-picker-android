package iam.thevoid.mediapicker.chooser

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import iam.thevoid.ae.asFragmentActivity
import iam.thevoid.mediapicker.R
import iam.thevoid.mediapicker.bus.MediaPickerBus
import iam.thevoid.mediapicker.bus.SelectAppBus

class PickerSelectAppDialog : BottomSheetDialogFragment(), OnSelectAppCallback {

    private var intentData: ArrayList<IntentData>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intentData = getIntentDatas(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.about_dialog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.title)?.apply {
            text = arguments?.getString(EXTRA_CHOOSER_TITLE)
        }
        view.findViewById<RecyclerView>(R.id.recycler).apply {
            layoutManager = LinearLayoutManager(activity)
            isVerticalScrollBarEnabled = true
            adapter = activity?.let {
                IntentDataAdapter(it.packageManager, intentData)
                        .also { adapter -> adapter.callback = this@PickerSelectAppDialog }
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        MediaPickerBus.onDismissSelectApp()
        super.onDismiss(dialog)
    }

    private fun getIntentDatas(savedInstanceState: Bundle?): ArrayList<IntentData>? =
            savedInstanceState?.takeIf { it.containsKey(EXTRA_RESOLVE) }?.getParcelableArrayList(EXTRA_RESOLVE)
                    ?: arguments?.takeIf { it.containsKey(EXTRA_RESOLVE) }?.getParcelableArrayList(EXTRA_RESOLVE)
                    ?: ArrayList()

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList(EXTRA_RESOLVE, intentData)
        super.onSaveInstanceState(outState)
    }

    override fun onSelectApp(context: Context, intentData: IntentData?) {
        SelectAppBus.onSelectApp(context, intentData)
        dismiss()
    }

    fun show(fragmentManager: FragmentManager) =
            show(fragmentManager, this.javaClass.canonicalName)

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            super.show(manager, tag)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    companion object {

        const val EXTRA_RESOLVE = "EXTRA_RESOLVE"
        const val EXTRA_CHOOSER_TITLE = "EXTRA_CHOOSER_TITLE"

        fun showForResult(context: Context?, intents: List<IntentData>, title: String) {
            context?.asFragmentActivity()?.supportFragmentManager?.also {
                show(it, intents, title)
            }
        }

        private fun <T : IntentData?> show(fm: FragmentManager, data: List<T>, title: String) =
                PickerSelectAppDialog().apply {
                    arguments = Bundle().apply {
                        putParcelableArrayList(EXTRA_RESOLVE, ArrayList(data))
                        putString(EXTRA_CHOOSER_TITLE, title)
                    }
                }.show(fm)
    }
}