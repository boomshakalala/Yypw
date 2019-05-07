package com.hbcx.user.dialogs

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import cn.sinata.xldutils.gone
import com.hbcx.user.R
import com.hbcx.user.interfaces.OnFilterDialogListener
import kotlinx.android.synthetic.main.dialog_rent_filter.*
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.support.v4.dip
import org.jetbrains.anko.wrapContent

/**
 * 租车筛选
 */
class RentFilterDialog : DialogFragment() {
    private var labels :ArrayList<com.hbcx.user.beans.CarLabel>?=null

    private val brands by lazy {
        arguments!!.getSerializable("brands") as ArrayList<com.hbcx.user.beans.CarBrand>
    }
    private var labelIndex = -1
    private var priceIndex = -1
    private var brandIndex = -1
    private var distance = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(android.support.v4.app.DialogFragment.STYLE_NO_FRAME, R.style.Dialog)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog.window.setLayout(matchParent, wrapContent)
        dialog.window.setGravity(Gravity.BOTTOM)
        dialog.setCanceledOnTouchOutside(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.dialog_rent_filter, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        labelIndex = arguments!!.getInt("label",-1)
        priceIndex = arguments!!.getInt("price",-1)
        brandIndex = arguments!!.getInt("brand",-1)
        distance = arguments!!.getInt("distance",-1)
        val type = arguments!!.getInt("type",0)  //0:租车 1:包车
        if (type == 1){
            tv_distance_title.gone()
            ll_distance.gone()
            tv_label_title.gone()
            fl_label.gone()
            rb_price_1.text = "￥500/天以下"
            rb_price_2.text = "￥500-￥1000/天"
            rb_price_3.text = "￥1000/天以上"
        }else{
            labels = arguments!!.getSerializable("labels") as ArrayList<com.hbcx.user.beans.CarLabel>
            labels!!.map {
                val textView = CheckedTextView(context)
                textView.text = it.name
                textView.setBackgroundResource(R.drawable.bg_selector_blue_black)
                textView.setPadding(dip(10),dip(5),dip(10),dip(5))
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,12F)
                textView.setTextColor(if (labels!!.indexOf(it) == labelIndex) context?.resources!!.getColor(R.color.colorPrimary) else context?.resources!!.getColor(R.color.black_text))
                textView.isChecked = labels!!.indexOf(it) == labelIndex
                textView.setOnClickListener {
                    it as CheckedTextView
                    if (fl_label.indexOfChild(it) == labelIndex){
                        it.isChecked = false
                        it.setTextColor(context!!.resources.getColor(R.color.black_text))
                        labelIndex = -1
                    }else{
                        if (labelIndex!=-1){
                            val checkedTextView = fl_label.getChildAt(labelIndex) as CheckedTextView
                            checkedTextView.isChecked = false
                            checkedTextView.setTextColor(context!!.resources.getColor(R.color.black_text))
                        }
                        it.isChecked = true
                        it.setTextColor(context!!.resources.getColor(R.color.colorPrimary))
                        labelIndex = fl_label.indexOfChild(it)
                    }
                }
                fl_label.addView(textView)
            }
        }
        when(priceIndex){
            0->rb_price_1.isChecked = true
            1->rb_price_2.isChecked = true
            2->rb_price_3.isChecked = true
        }
        when(distance){
            1->rb_distance_1.isChecked = true
            3->rb_distance_2.isChecked = true
            5->rb_distance_3.isChecked = true
        }
        val adapter = com.hbcx.user.adapter.RentFilterAdapter(context!!, brands, brandIndex)
        gv_brand.adapter = adapter
        gv_brand.setOnItemClickListener { adapterView, view, i, l ->
            view as CheckedTextView
            brandIndex = if (i == brandIndex){
                adapter.changeIndex(-1)
                -1
            }else{
                adapter.changeIndex(i)
                i
            }
        }

        rb_distance_1.setOnClickListener {
            when(distance){
                1->{
                    rb_distance_1.isChecked = !rb_distance_1.isChecked
                }
                3->{
                    rb_distance_2.isChecked = false
                    rb_distance_1.isChecked = true
                }
                5->{
                    rb_distance_3.isChecked = false
                    rb_distance_1.isChecked = true
                }
                else->{
                    rb_distance_1.isChecked = true
                }
            }
            distance = if (rb_distance_1.isChecked) 1 else 0
        }

        rb_distance_2.setOnClickListener {
            when(distance){
                3->{
                    rb_distance_2.isChecked = !rb_distance_2.isChecked
                }
                1->{
                    rb_distance_1.isChecked = false
                    rb_distance_2.isChecked = true
                }
                5->{
                    rb_distance_3.isChecked = false
                    rb_distance_2.isChecked = true
                }
                else -> rb_distance_2.isChecked = true
            }
            distance = if (rb_distance_2.isChecked) 3 else 0
        }

        rb_distance_3.setOnClickListener {
            when(distance){
                5->{
                    rb_distance_3.isChecked = !rb_distance_3.isChecked
                }
                1->{
                    rb_distance_1.isChecked = false
                    rb_distance_3.isChecked = true
                }
                3->{
                    rb_distance_2.isChecked = false
                    rb_distance_3.isChecked = true
                }
                else->rb_distance_3.isChecked = true
            }
            distance = if (rb_distance_3.isChecked) 5 else 0
        }

        rb_price_1.setOnClickListener {
            rb_price_1.isChecked = !rb_price_1.isChecked
            if (priceIndex == 1)
                rb_price_2.isChecked = false
            if (priceIndex == 2)
                rb_price_3.isChecked = false
            priceIndex = if (rb_price_1.isChecked) 0 else -1
        }
        rb_price_2.setOnClickListener {
            rb_price_2.isChecked = !rb_price_2.isChecked
            if (priceIndex == 0)
                rb_price_1.isChecked = false
            if (priceIndex == 2)
                rb_price_3.isChecked = false
            priceIndex = if (rb_price_2.isChecked) 1 else -1
        }
        rb_price_3.setOnClickListener {
            rb_price_3.isChecked = !rb_price_3.isChecked
            if (priceIndex == 1)
                rb_price_2.isChecked = false
            if (priceIndex == 0)
                rb_price_1.isChecked = false
            priceIndex = if (rb_price_3.isChecked) 2 else -1
        }

        tv_clear.setOnClickListener {
            if (labelIndex!=-1){
                val checkedTextView = fl_label.getChildAt(labelIndex) as CheckedTextView
                checkedTextView.isChecked = false
                checkedTextView.setTextColor(context!!.resources.getColor(R.color.black_text))
                labelIndex = -1
            }
            when(priceIndex){
                0-> rb_price_1.isChecked = false
                1-> rb_price_2.isChecked = false
                2-> rb_price_3.isChecked = false
            }
            priceIndex = -1
            when(distance){
                1-> rb_distance_1.isChecked = false
                3-> rb_distance_2.isChecked = false
                5-> rb_distance_3.isChecked = false
            }
            distance = 0
            brandIndex = -1
            adapter.changeIndex(brandIndex)
        }

        tv_ok.setOnClickListener {
            dialogListener?.onClick(labelIndex,priceIndex,distance,brandIndex)
            dismissAllowingStateLoss()
        }
    }

    private var isDestroy = false
    override fun onDestroyView() {
        isDestroy = true
        super.onDestroyView()
    }

    private var dialogListener: OnFilterDialogListener? = null

    fun setDialogListener(l: (label: Int, price: Int, distance: Int, brand: Int) -> Unit) {
        dialogListener = object : OnFilterDialogListener {
            override fun onClick(label: Int, price: Int, distance: Int, brand: Int) {
                l(label,price,distance,brand)
            }

        }
    }
}