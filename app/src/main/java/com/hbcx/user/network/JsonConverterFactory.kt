package com.hbcx.user.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * 自定义gson解析converter
 * Created by liaoxiang on 16/6/21.
 */
class JsonConverterFactory private constructor(private val gson: Gson?) : Converter.Factory() {

    init {
        if (gson == null) throw NullPointerException("gson == null")
    }


    override fun responseBodyConverter(type: Type?, annotations: Array<Annotation>?,
                                       retrofit: Retrofit?): Converter<ResponseBody, *>? {

        val adapter = gson!!.getAdapter(TypeToken.get(type!!))
        return JsonResponseBodyConverter(gson, adapter) //响应
    }

    override fun requestBodyConverter(type: Type?, parameterAnnotations: Array<Annotation>?, methodAnnotations: Array<Annotation>?, retrofit: Retrofit?): Converter<*, RequestBody>? {
        val adapter = gson!!.getAdapter(TypeToken.get(type!!))
        return JsonRequestBodyConverter(gson, adapter) //请求
    }

    companion object {

        fun create(): JsonConverterFactory {
            val builder = GsonBuilder().serializeNulls()
            return create(builder.create())
        }
        fun create(gson: Gson): JsonConverterFactory = JsonConverterFactory(gson)
    }
}
