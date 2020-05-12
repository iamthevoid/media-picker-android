package iam.thevoid.mediapicker.chooser

import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import iam.thevoid.ae.inflate
import iam.thevoid.e.safe
import iam.thevoid.mediapicker.R
import iam.thevoid.mediapicker.chooser.IntentDataAdapter.CustomAppChooserVH
import iam.thevoid.util.weak

class IntentDataAdapter internal constructor(pm: PackageManager, intentDatas: List<IntentData>?) : RecyclerView.Adapter<CustomAppChooserVH>() {

    private var packageManager by weak<PackageManager>()

    private val resolveMap = hashMapOf<ResolveInfo, IntentData>()
    private val resolveInfos = getResolveInfo(pm, intentDatas, resolveMap)

    var callback: OnSelectAppCallback? = null

    init {
        packageManager = pm
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomAppChooserVH =
            CustomAppChooserVH(callback, parent.context.inflate(R.layout.app_select_item, parent))

    override fun onBindViewHolder(holder: CustomAppChooserVH, position: Int) =
            holder.onBind(packageManager, resolveInfos[position], resolveMap[resolveInfos[position]])

    override fun getItemCount(): Int = resolveInfos.size

    class CustomAppChooserVH constructor(private val callback: OnSelectAppCallback?, itemView: View)
        : RecyclerView.ViewHolder(itemView) {

        private var imageView: ImageView = itemView.findViewById(R.id.app_icon)

        private var textView: TextView = itemView.findViewById(R.id.app_text)

        fun onBind(pm: PackageManager?, resolveInfo: ResolveInfo, intentData: IntentData?) {
            imageView.setImageDrawable(resolveInfo.loadIcon(pm))
            textView.text = intentData?.title?.takeIf { it > 0 }?.let(textView.context::getString)
                    ?: resolveInfo.loadLabel(pm)
            itemView.setOnClickListener { callback?.onSelectApp(textView.context, intentData) }
        }
    }

    companion object {
        private fun getResolveInfo(
                pm: PackageManager?,
                intents: List<IntentData>?,
                resolveMap: MutableMap<ResolveInfo, IntentData>
        ) = intents.safe()
                .map {
                    pm?.queryIntentActivities(it.intent, 0).safe()
                            .onEach { resolveInfo -> resolveMap[resolveInfo] = it }
                }
                .flatten()
    }
}