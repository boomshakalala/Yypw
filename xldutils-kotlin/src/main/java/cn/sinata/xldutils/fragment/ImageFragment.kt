package cn.sinata.xldutils.fragment

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import cn.sinata.xldutils.R
import cn.sinata.xldutils.view.ZoomableDraweeView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.drawable.ProgressBarDrawable
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder
import org.jetbrains.anko.support.v4.find

/**
 *
 */
class ImageFragment:BaseFragment() {

    companion object {
        fun newInstance(url:String):Fragment{
            val fragment = ImageFragment()
            val bundle = Bundle()
            bundle.putString("url",url)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun contentViewId(): Int = R.layout.fragment_image

    override fun onFirstVisibleToUser() {
        var url:String
        if (arguments == null) {
            url = ""
        } else {
            url = arguments?.getString("url")?:""
        }

        val zoomDraweeView = find<ZoomableDraweeView>(R.id.zoomDrawee)
        val hierarchy = GenericDraweeHierarchyBuilder(resources)
                .setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP)
                .setProgressBarImage(ProgressBarDrawable())
                .build()
        println(url)
        val uri = Uri.parse(url)

        val controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setTapToRetryEnabled(true)
                .build()

        zoomDraweeView.hierarchy= hierarchy
        zoomDraweeView.controller = controller

        zoomDraweeView.setOnClickListener{
//            if (activity != null) {
//                activity?.onBackPressed()
//            }
        }
    }
}