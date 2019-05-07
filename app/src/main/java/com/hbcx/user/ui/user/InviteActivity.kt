package com.hbcx.user.ui.user

import android.content.Intent
import android.view.View
import cn.sinata.xldutils.utils.SPUtils
import cn.sinata.xldutils.utils.SpanBuilder
import cn.sinata.xldutils.utils.optDouble
import cn.sinata.xldutils.utils.optInt
import com.hbcx.user.R
import com.hbcx.user.dialogs.InviteDialog
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.TranslateStatusBarActivity
import com.hbcx.user.utils.Const
import com.hbcx.user.utils.request
import com.umeng.socialize.UMShareAPI
import kotlinx.android.synthetic.main.activity_invite.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.startActivity

class InviteActivity:TranslateStatusBarActivity() {
    override fun setContentView() = R.layout.activity_invite

    override fun initClick() {
        btn_action.onClick {
            val inviteDialog = InviteDialog()
            inviteDialog.show(supportFragmentManager,"share")
        }
    }

    override fun initView() {
        title = "邀请好友"
        titleBar.addRightButton("邀请记录",onClickListener = View.OnClickListener {
            startActivity<InviteRecordActivity>()
        })
        getData()
    }
    private val userId by lazy {
        SPUtils.instance().getInt(Const.User.USER_ID,0)
    }
    private fun getData(){
        HttpManager.getInviteData(userId).request(this){_,data->
            data?.let {
                val s = String.format("邀请好友注册即可领 %.1f 元优惠券",it.optDouble("money"))
                tv_title.text = SpanBuilder(s).color(this,9,s.length-4,R.color.color_tv_orange).build()
                tv_detail.text = String.format("已成功邀请%d位好友注册，累计领取%.1f元优惠券，点击右上角邀请记录即可查看邀请明细！"
                        ,it.optInt("inviteNum"),it.optDouble("totalMoney"))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        UMShareAPI.get(this).onActivityResult(requestCode,resultCode,data)
    }
}