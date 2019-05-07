package cn.sinata.xldutils.activity

import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import cn.sinata.xldutils.R
import org.jetbrains.anko.find

open class WebViewActivity : TitleActivity() {

    private val webView by lazy {
        find<WebView>(R.id.webView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        val t = intent.getStringExtra("title")
        title = t
        initView()
    }

    private fun initView() {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.defaultTextEncodingName = "utf-8"
        settings.domStorageEnabled = true
        webView.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView, title: String?) {
                super.onReceivedTitle(view, title)
                if (resetTitle()) {
                    setTitle(title)
                }
            }
        }
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String?) {
                super.onPageFinished(view, url)
                dismissDialog()
            }

            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                showDialog()
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
                if (!TextUtils.isEmpty(url)) {
                    webView.loadUrl(url)
                }
                return true
            }
        }
        val type = intent.getIntExtra("type", 0)
        val url = intent.getStringExtra("url")
        if (type == 1) {
            webView.loadData(url, "text/html; charset=utf-8", "utf-8")
        } else {
            webView.loadUrl(url)
        }
    }

    protected open fun resetTitle() = false
    override fun finishWhenCancelDialog(): Boolean = false
}
