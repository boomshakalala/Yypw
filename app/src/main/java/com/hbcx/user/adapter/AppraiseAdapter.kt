package com.hbcx.user.adapter

import android.widget.RatingBar
import cn.sinata.xldutils.adapter.HFRecyclerAdapter
import cn.sinata.xldutils.adapter.util.ViewHolder
import cn.sinata.xldutils.utils.toTime
import com.hbcx.user.R

/**
 * 我的评价adapter
 */
class AppraiseAdapter(mData:ArrayList<com.hbcx.user.beans.Appraise>):HFRecyclerAdapter<com.hbcx.user.beans.Appraise>(mData, R.layout.item_appraise_layout) {
    override fun onBind(holder: ViewHolder, position: Int, data: com.hbcx.user.beans.Appraise) {
        holder.setText(R.id.tv_time, data.createTime.toTime("yyyy-MM-dd"))
        holder.setText(R.id.tv_content,data.remark)
        val ratingBar = holder.bind<RatingBar>(R.id.rb_score)
        ratingBar.rating = data.score.toFloat()
    }
}