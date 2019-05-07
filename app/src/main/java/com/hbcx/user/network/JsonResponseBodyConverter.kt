package com.hbcx.user.network

import cn.sinata.xldutils.sysErr
import com.google.gson.Gson
import com.google.gson.TypeAdapter
import okhttp3.ResponseBody
import retrofit2.Converter
import java.io.IOException

/**
 *
 */
class JsonResponseBodyConverter<T>(private val mGson: Gson, private val adapter: TypeAdapter<T>) : Converter<ResponseBody, T> {

    /**
     * 转换
     *
     * @param responseBody
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    override fun convert(responseBody: ResponseBody): T {
        val s = responseBody.string()
        sysErr(s)
//        //如果返还值需解密
//        if (HttpManager.decodeDes) {
//            val type = object : TypeToken<ResultData<String>>() {
//
//            }.type
//            val data = mGson.fromJson<ResultData<String>>(s, type)
//            val newData = data.getDecodeDesString()
//            return adapter.fromJson(newData)
//        }
        //        JsonReader jsonReader = mGson.newJsonReader(responseBody.charStream());
        responseBody.use { _ ->
            return adapter.fromJson(s)
            //            return adapter.read(jsonReader);
        }
    }
}