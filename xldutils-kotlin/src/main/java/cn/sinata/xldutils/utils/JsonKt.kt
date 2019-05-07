package cn.sinata.xldutils.utils

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.lang.Exception

/**
 *
 */
fun JsonObject.optString(key:String,defValue:String = ""):String {
    if (this.has(key)) {
        return try {
            this.get(key).asString
        } catch (e: Exception) {
            e.printStackTrace()
            defValue
        }
    }
    return defValue
}

fun JsonObject.optInt(key:String,defValue:Int = 0):Int {
    if (this.has(key)) {
        return try {
            this.get(key).asInt
        } catch (e: Exception) {
            e.printStackTrace()
            defValue
        }

    }
    return defValue
}

fun JsonObject.optFloat(key:String,defValue:Float = 0.0F):Float {
    if (this.has(key)) {
        return try {
            this.get(key).asFloat
        } catch (e: Exception) {
            e.printStackTrace()
            defValue
        }
    }
    return defValue
}

fun JsonObject.optLong(key:String,defValue:Long = 0):Long {
    if (this.has(key)) {
        return try {
            this.get(key).asLong
        } catch (e: Exception) {
            e.printStackTrace()
            defValue
        }
    }
    return defValue
}

fun JsonObject.optBoolean(key:String,defValue:Boolean = false):Boolean {
    if (this.has(key)) {
        return try {
            this.get(key).asBoolean
        } catch (e: Exception) {
            e.printStackTrace()
            defValue
        }
    }
    return defValue
}

fun JsonObject.optDouble(key:String,defValue:Double = 0.0):Double {
    if (this.has(key)) {
        return try {
            this.get(key).asDouble
        } catch (e: Exception) {
            e.printStackTrace()
            defValue
        }
    }
    return defValue
}

fun JsonObject.optJsonObj(key:String,defValue:JsonObject = JsonObject()):JsonObject {
    if (this.has(key)) {
        return try {
            this.get(key).asJsonObject
        } catch (e: Exception) {
            e.printStackTrace()
            defValue
        }
    }
    return defValue
}

fun JsonObject.optJsonArray(key:String,defValue:JsonArray = JsonArray()):JsonArray {
    if (this.has(key)) {
        return try {
            this.get(key).asJsonArray
        } catch (e: Exception) {
            e.printStackTrace()
            defValue
        }
    }
    return defValue
}

