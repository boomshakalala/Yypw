package com.hbcx.user.wxapi

import android.content.Intent
import android.os.Bundle
import cn.sinata.xldutils.activity.BaseActivity
import com.hbcx.user.utils.Const
import com.hbcx.user.utils.PayUtil
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import org.jetbrains.anko.toast

/**
 * 微信支付
 */
class WXPayEntryActivity : BaseActivity(), IWXAPIEventHandler {

    private var api: IWXAPI? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        api = WXAPIFactory.createWXAPI(this, Const.WX_APP_ID)
        api!!.handleIntent(intent, this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        api!!.handleIntent(intent, this)
    }

    override fun onReq(req: BaseReq) {

    }

    override fun onResp(resp: BaseResp) {

        if (resp.type == ConstantsAPI.COMMAND_PAY_BY_WX) {

            if (resp.errCode == 0) {

                toast("支付成功")
                //使用广播模式通知支付页面
//                val intent = Intent(Const.PAY_ACTION)
//                sendBroadcast(intent)
                PayUtil.payListeners.forEach {
                    it?.onPaySuccess()
                }
            } else {

                if (resp.errCode == -2) {
                    toast("取消支付")
                } else if (resp.errCode == -1) {
                    toast("支付出现错误！")
                }
            }
        }
        finish()
    }
}