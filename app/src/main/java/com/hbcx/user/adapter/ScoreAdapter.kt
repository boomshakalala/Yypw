package com.hbcx.user.adapter

import android.widget.TextView
import cn.sinata.xldutils.adapter.HFRecyclerAdapter
import cn.sinata.xldutils.adapter.util.ViewHolder
import cn.sinata.xldutils.utils.toTime
import com.hbcx.user.R
import org.jetbrains.anko.textColorResource

class ScoreAdapter(data:ArrayList<com.hbcx.user.beans.ScoreRecored>):HFRecyclerAdapter<com.hbcx.user.beans.ScoreRecored>(data, R.layout.item_score_recored) {
    override fun onBind(holder: ViewHolder, position: Int, data: com.hbcx.user.beans.ScoreRecored) {
        holder.setText(R.id.tv_name,data.title)
        holder.setText(R.id.tv_time,data.createTime.toTime("yyyy-MM-dd HH:mm"))
        if (data.state == 1){
            holder.setText(R.id.tv_score,"+${data.integral}")
            holder.bind<TextView>(R.id.tv_score).textColorResource = R.color.black_text
        }else{
            holder.setText(R.id.tv_score,"-${data.integral}")
            holder.bind<TextView>(R.id.tv_score).textColorResource = R.color.color_money_text
        }
    }
}