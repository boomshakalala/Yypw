package cn.sinata.xldutils.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import cn.sinata.xldutils.fragment.ImageFragment

/**
 *
 */
class ImagePagerAdapter(fragmentManager: FragmentManager,urls:ArrayList<String>) : FragmentPagerAdapter(fragmentManager) {
    var mUrls = urls

    override fun getItem(position: Int): Fragment = ImageFragment.newInstance(mUrls[position])

    override fun getCount(): Int = mUrls.size
}