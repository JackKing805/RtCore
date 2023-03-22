package com.jerry.rt.core.http.converter

import com.jerry.rt.core.RtContext
import com.jerry.rt.core.http.interfaces.IDataConverter

object DataConverter {
    fun converterToAcceptEncoding(rtContext: RtContext,encoding:List<String>,source:ByteArray): IDataConverter.ConverterDataResult {
        val rtDataConverter = rtContext.getRtConfig().rtDataConverter
        if (rtDataConverter.enabled){
            val transform = encoding.map { it.trim() }
            rtContext.getRtConfig().rtDataConverter.listAll().forEach {
                val newIns = it.newInstance()
                transform.forEach {format->
                    if (newIns.handleFormat()==format) {
                        return newIns.converter(source)
                    }
                }
            }
        }
        return IDataConverter.ConverterDataResult("identity", data = source)
    }
}

