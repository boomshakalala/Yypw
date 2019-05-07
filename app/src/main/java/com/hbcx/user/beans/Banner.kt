package com.hbcx.user.beans

import android.os.Parcel
import android.os.Parcelable

/**
 *
 */
data class Banner(val id: String? = ""):Parcelable {
    var title: String? = ""
    var imgUrl: String? = ""
    var jumpUrl: String? = ""
    var jumpType: Int? = 0

    constructor(parcel: Parcel) : this(parcel.readString()) {
        title = parcel.readString()
        imgUrl = parcel.readString()
        jumpUrl = parcel.readString()
        jumpType = parcel.readValue(Int::class.java.classLoader) as? Int
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(imgUrl)
        parcel.writeString(jumpUrl)
        parcel.writeValue(jumpType)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Banner> {
        override fun createFromParcel(parcel: Parcel): Banner {
            return Banner(parcel)
        }

        override fun newArray(size: Int): Array<Banner?> {
            return arrayOfNulls(size)
        }
    }
}