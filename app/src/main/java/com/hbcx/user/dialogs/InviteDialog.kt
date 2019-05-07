package com.hbcx.user.dialogs

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.sinata.xldutils.utils.SPUtils
import com.hbcx.user.R
import com.hbcx.user.network.Api
import com.hbcx.user.utils.Const
import com.share.utils.ShareUtils
import com.umeng.socialize.UMShareListener
import com.umeng.socialize.bean.SHARE_MEDIA
import com.umeng.socialize.media.UMImage
import kotlinx.android.synthetic.main.dialog_invite.*
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.wrapContent

class InviteDialog : DialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(android.support.v4.app.DialogFragment.STYLE_NO_FRAME, R.style.Dialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
            = inflater.inflate(R.layout.dialog_invite, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog.window.setLayout(matchParent, wrapContent)
        dialog.window.setGravity(Gravity.BOTTOM)
        dialog.setCanceledOnTouchOutside(true)
    }

    private val userId by lazy {
        SPUtils.instance().getInt(Const.User.USER_ID,0)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listener = object : UMShareListener {
            override fun onResult(p0: SHARE_MEDIA?) {
//                toast("分享成功") 微信不再提供回调状态
                dismiss()
            }

            override fun onCancel(p0: SHARE_MEDIA?) {
                toast("分享取消")
            }

            override fun onError(p0: SHARE_MEDIA?, p1: Throwable?) {
                toast("分享失败")
            }

            override fun onStart(p0: SHARE_MEDIA?) {
            }
        }
        tv_cancel.onClick {
            dismiss()
        }
        val umImage = UMImage(activity, R.drawable.share_icon)
        tv_qq.onClick {
            ShareUtils.share(activity, SHARE_MEDIA.QQ,"迎使用“伙伴出行”！伙伴出行是传统客运与互联网技术相融合的出行新产品，带给您线上与线下的新体验。伙伴出行，伴您全程！","伙伴出行", Api.SHARE_URL+userId,umImage,listener)
        }
        tv_qq_zone.onClick {
            ShareUtils.share(activity, SHARE_MEDIA.QZONE,"迎使用“伙伴出行”！伙伴出行是传统客运与互联网技术相融合的出行新产品，带给您线上与线下的新体验。伙伴出行，伴您全程！","伙伴出行", Api.SHARE_URL+userId,umImage,listener)
        }
        tv_wechat.onClick {
            ShareUtils.share(activity, SHARE_MEDIA.WEIXIN,"迎使用“伙伴出行”！伙伴出行是传统客运与互联网技术相融合的出行新产品，带给您线上与线下的新体验。伙伴出行，伴您全程！","伙伴出行", Api.SHARE_URL+userId,umImage,listener)
        }
        tv_moment.onClick {
            ShareUtils.share(activity, SHARE_MEDIA.WEIXIN_CIRCLE,"迎使用“伙伴出行”！伙伴出行是传统客运与互联网技术相融合的出行新产品，带给您线上与线下的新体验。伙伴出行，伴您全程！","伙伴出行", Api.SHARE_URL+userId,umImage,listener)
        }
        tv_weibo.onClick {
            ShareUtils.share(activity, SHARE_MEDIA.SINA,"迎使用“伙伴出行”！伙伴出行是传统客运与互联网技术相融合的出行新产品，带给您线上与线下的新体验。伙伴出行，伴您全程！","伙伴出行", Api.SHARE_URL+userId,umImage,listener)
        }
    }
}