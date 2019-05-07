package com.hbcx.user.db

import android.content.ContentValues
import android.content.Context
import cn.sinata.xldutils.sysErr
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.help.Tip
import org.jetbrains.anko.db.*

/**
 * 历史记录数据库操作
 */
class HistoryDBManager {

    private fun hasSameAddress(context: Context, name: String, address: String, table: String): Long {
        return context.database.use {
            return@use select(table)
                    .columns("id")
                    .whereSimple("name=? and address=?", name, address)
                    .exec {
                        if (this.moveToFirst()) {
                            this.getLong(0)
                        } else {
                            -1
                        }
                    }
        }
    }

    private fun hasSameRecord(context: Context, start: String, end: String, table: String): Long {
        return context.database.use {
            return@use select(table)
                    .columns("id")
                    .whereSimple("start=? and end=?", start, end)
                    .exec {
                        if (this.moveToFirst()) {
                            this.getLong(0)
                        } else {
                            -1
                        }
                    }
        }
    }

    fun getAddressList(context: Context, limit: Int = 10, table: String): List<Tip> {
        return context.database.use {
            return@use select(table)
                    .columns("name", "lat", "lng", "district", "address", "time")
                    .limit(limit)
                    .orderBy("time", SqlOrderDirection.DESC)
                    .parseList(object : RowParser<Tip> {
                        override fun parseRow(columns: Array<Any?>): Tip {
                            val tip = Tip()
                            tip.name = columns[0].toString()
                            val lat = columns[1].toString().toDoubleOrNull()
                            val lng = columns[2].toString().toDoubleOrNull()
                            sysErr("----->$lat-------->$lng")
                            if (lat != null && lng != null)
                                tip.setPostion(LatLonPoint(lat, lng))
                            tip.district = columns[3].toString()
                            tip.address = columns[4].toString()
                            return tip
                        }
                    })
        }
    }

    /**
     * 票务搜索历史记录
     */
    fun getHistoryList(context: Context, limit: Int = 10, table: String): List<com.hbcx.user.beans.TicketHistory> {
        return context.database.use {
            return@use select(table)
                    .columns("start", "end","start_code","end_code","start_id", "time","start_station_id","end_station_id","line_type")
                    .limit(limit)
                    .orderBy("time", SqlOrderDirection.DESC)
                    .parseList(object : RowParser<com.hbcx.user.beans.TicketHistory> {
                        override fun parseRow(columns: Array<Any?>): com.hbcx.user.beans.TicketHistory {
                            return com.hbcx.user.beans.TicketHistory(columns[0].toString(), columns[1].toString(), columns[2].toString(), columns[3].toString(), columns[4] as Long,columns[6] as Long,columns[7] as Long,columns[8] as Long)
                        }
                    })
        }
    }

    fun saveHistory(context: Context, tip: Tip, table: String): Long {
        return context.database.use {
            //最多存储10条
            getAllCount(context, table)
            val values = ContentValues()
            val id = hasSameAddress(context, tip.name, tip.address, table)
            //有
            if (id != -1L) {
                values.put("time", System.currentTimeMillis())
                return@use update(table, values, "id=?", arrayOf(id.toString())).toLong()
            } else {
                values.put("name", tip.name)
                values.put("lat", tip.point.latitude)
                values.put("lng", tip.point.longitude)
                values.put("district", tip.district)
                values.put("address", tip.address)
                values.put("time", System.currentTimeMillis())
                return@use insert(table, null, values)
            }
        }
    }

    fun saveHistory(context: Context, start: String, end: String,startCode:String,endCode:String,startId:Long,startStationId:Int,endStationId:Int,lineType:Int, table: String): Long {
        return context.database.use {
            //最多存储10条
            getAllCount(context, table)
            val values = ContentValues()
            val id = hasSameRecord(context, start, end, table)
            //有
            values.put("time", System.currentTimeMillis())
            if (id != -1L)
                return@use update(table, values, "id=?", arrayOf(id.toString())).toLong()
            else {
                values.put("start", start)
                values.put("end", end)
                values.put("start_code", startCode)
                values.put("end_code", endCode)
                values.put("start_id", startId)
                values.put("start_station_id", startStationId)
                values.put("end_station_id",endStationId)
                values.put("line_type",lineType)
                return@use insert(table, null, values)
            }
        }
    }

    fun clearHistory(context: Context, table: String) {
        context.database.use {
            delete(table)
        }
    }

    private fun getAllCount(context: Context, table: String) {
        context.database.use {
            val list = select(table)
                    .columns("time")
                    .orderBy("time", SqlOrderDirection.DESC)
                    .parseList(object : RowParser<Long> {
                        override fun parseRow(columns: Array<Any?>): Long {
                            return columns[0].toString().toLong()
                        }
                    })
            if (list.size >= 10) {
                val last = list[8]
                delete(table, "time<?", arrayOf(last.toString()))
            }
        }
    }

}