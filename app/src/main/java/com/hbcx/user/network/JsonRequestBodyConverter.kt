package com.hbcx.user.network

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import java.io.IOException
import java.util.HashMap

import okhttp3.FormBody
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Converter

/**
 *
 */
class JsonRequestBodyConverter<T>(private val gson: Gson, private val adapter: TypeAdapter<T>) : Converter<T, RequestBody> {


    @Throws(IOException::class)
    override fun convert(value: T): RequestBody {
        val postBody = value.toString()
        return RequestBody.create(MEDIA_TYPE, postBody)
    }


    private fun buildPostRequest(mParams: Map<String, String>?): RequestBody {
        var params = mParams
        if (params == null) {
            params = HashMap()
        }
        val builder = FormBody.Builder()
        val entries = params.entries

        for ((key, value) in entries) {
            builder.add(key, value)
        }
        return builder.build()
    }

    companion object {
        private val MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8")
    }

}
