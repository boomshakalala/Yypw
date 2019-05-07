package com.hbcx.user.ui.grouprent

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hbcx.user.R
import kotlinx.android.synthetic.main.fragment_group_rent_type.*
import org.jetbrains.anko.bundleOf

class TypeViewPagerFragment:Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_group_rent_type,null)
    }

    companion object {
        fun getInstance(data: com.hbcx.user.beans.GroupRentType):TypeViewPagerFragment{
            val fragment = TypeViewPagerFragment()
            fragment.arguments = bundleOf("data" to data)
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val data = arguments?.getSerializable("data") as com.hbcx.user.beans.GroupRentType
        iv_type.setImageURI(data.imgUrl)
        tv_name.text = data.name
        tv_person_count.text = data.remark
    }
}