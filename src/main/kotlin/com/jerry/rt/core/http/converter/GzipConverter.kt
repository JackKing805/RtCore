package com.jerry.rt.core.http.converter

import com.jerry.rt.core.http.interfaces.IDataConverter
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

class GzipConverter: IDataConverter {
    private val byteArrayOutputStream = ByteArrayOutputStream()
    private val gzipOutputStream = GZIPOutputStream(byteArrayOutputStream)

    override fun converter(data: ByteArray): IDataConverter.ConverterDataResult {
        gzipOutputStream.write(data)
        return IDataConverter.ConverterDataResult("gzip",byteArrayOutputStream.toByteArray())
    }

    override fun handleFormat(): String {
        return "gzip"
    }
}