package cn.sinata.xldutils.activity

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.widget.TextView
import cn.sinata.xldutils.R
import cn.sinata.xldutils.adapter.ImagePagerAdapter
import org.jetbrains.anko.find

class ImagePagerActivity : BaseActivity(),ViewPager.OnPageChangeListener {

    var urls : ArrayList<String> = ArrayList()

    companion object {
        val POSITION = "position"
        val URLS = "url"
    }

    val imagePager by lazy {
        find<ViewPager>(R.id.mViewPager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_pager)

        imagePager.addOnPageChangeListener(this)

        val position = intent.getIntExtra(POSITION,0)
        val urls = intent.getStringArrayListExtra(URLS)
        if (urls != null) {
            this.urls.addAll(urls)
        }
        find<TextView>(R.id.tv_pages).text = getString(R.string.pageAndSizes,position+1,urls.size)
        imagePager.adapter = ImagePagerAdapter(supportFragmentManager,urls)
        imagePager.currentItem = position
    }

    override fun onPageScrollStateChanged(state: Int) {

    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {
        find<TextView>(R.id.tv_pages).text = getString(R.string.pageAndSizes,position+1,urls.size)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (imagePager != null) {
            imagePager.removeOnPageChangeListener(this)
        }
    }
}
