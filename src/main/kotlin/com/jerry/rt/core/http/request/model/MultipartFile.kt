package com.jerry.rt.core.http.request.model

import com.jerry.rt.bean.RtFileConfig
import com.jerry.rt.jva.utils.FileUtil
import com.jerry.rt.jva.utils.IoUtil
import com.jerry.rt.jva.utils.MultipartRequestInputStream
import java.io.*

/**
 * @className: MultipartFile
 * @author: Jerry
 * @date: 2023/2/13:21:47
 **/
class MultipartFile(
    private val rtFileConfig: RtFileConfig,
     private val header: MultipartFileHeader
) {
    companion object {
        private const val TMP_FILE_PREFIX = "rt-"
        private const val TMP_FILE_SUFFIX = ".upload.tmp"
    }

    private var size: Long = -1

    // 文件流（小文件位于内存中）
    private var data: ByteArray?=null

    // 临时文件（大文件位于临时文件夹中）
    private var tempFile: File? = null



    fun delete(){
        tempFile?.delete()
        if (data != null) {
            data = null
        }
    }

    fun getHeader() = header


    fun getSize() = size

    /**
     * @return 是否上传成功
     */
    fun isUploaded(): Boolean {
        return size > 0
    }

    /**
     * @return 文件是否在内存中
     */
    fun isInMemory(): Boolean {
        return data != null
    }

    // ---------------------------------------------------------------- process
    @Throws(IOException::class)
    public fun processStream(input: MultipartRequestInputStream): Boolean {
        size = 0

        // 处理内存文件
        val memoryThreshold: Int = rtFileConfig.memoryThreshold
        if (memoryThreshold > 0) {
            val baos = ByteArrayOutputStream(memoryThreshold)
            val written = input.copy(baos, memoryThreshold.toLong())
            data = baos.toByteArray()
            if (written <= memoryThreshold) {
                // 文件存放于内存
                size = data!!.size.toLong()
                return true
            }
        }
        val tmpUploadPath = File(rtFileConfig.tempFileDir)
        if (!tmpUploadPath.exists()) {
            tmpUploadPath.mkdir()
        }

        // 处理硬盘文件
        tempFile = FileUtil.createTempFile(TMP_FILE_PREFIX, TMP_FILE_SUFFIX, tmpUploadPath, false)
        val out = FileUtil.getOutputStream(this.tempFile);
        if (data != null) {
            size = data!!.size.toLong()
            out.write(data!!)
            data = null // not needed anymore
        }
        val maxFileSize: Long = rtFileConfig.uploadMaxSize
        try {
            if (maxFileSize == -1L) {
                size += input.copy(out)
                return true
            }
            size += input.copy(out, maxFileSize - size + 1) // one more byte to detect larger files
            if (size > maxFileSize) {
                // 超出上传大小限制
                tempFile?.delete()
                tempFile = null
                input.skipToBoundary()
                return false
            }
        } finally {
            IoUtil.close(out);
        }
        return true
    }
}