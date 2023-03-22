package com.jerry.rt.core.http.interfaces

interface IDataConverter {
    fun handleFormat():String

    fun converter(data:ByteArray):ConverterDataResult



    data class ConverterDataResult(
        val encoding:String,
        val data:ByteArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ConverterDataResult

            if (encoding != other.encoding) return false
            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = encoding.hashCode()
            result = 31 * result + data.contentHashCode()
            return result
        }
    }
}