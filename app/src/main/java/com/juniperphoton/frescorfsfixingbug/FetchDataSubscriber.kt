package com.juniperphoton.frescorfsfixingbug

import android.net.Uri
import com.facebook.binaryresource.FileBinaryResource
import com.facebook.cache.common.CacheKey
import com.facebook.common.memory.PooledByteBuffer
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.BaseDataSubscriber
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory
import com.facebook.imagepipeline.image.EncodedImage
import com.facebook.imagepipeline.request.ImageRequest
import java.io.*

abstract class FetchDataSubscriber(
    private val url: String
) : BaseDataSubscriber<CloseableReference<PooledByteBuffer>>() {
    override fun onNewResultImpl(dataSource: DataSource<CloseableReference<PooledByteBuffer>>) {
        var buffer: CloseableReference<PooledByteBuffer>? = null
        var file: File? = null

        try {
            buffer = dataSource.result
            if (buffer != null) {
                file = insertToMainFileCache(Uri.parse(url), buffer)
            }
        } finally {
            closeSafely(buffer)
        }

        if (file == null) {
            onFailureImpl(null)
        } else {
            onFileCacheInserted(file)
        }
    }

    abstract fun onFileCacheInserted(file: File)
}

fun insertToMainFileCache(uri: Uri?, buffer: CloseableReference<PooledByteBuffer>): File? {
    if (uri == null) {
        return null
    }
    val cacheKey = DefaultCacheKeyFactory.getInstance()
        .getEncodedCacheKey(ImageRequest.fromUri(uri), null)
    return insertToMainFileCache(cacheKey, buffer)
}

fun insertToMainFileCache(
    cacheKey: CacheKey,
    buffer: CloseableReference<PooledByteBuffer>
): File? {
    val encodedImage = EncodedImage(buffer)
    val `is` = encodedImage.inputStream ?: return null

    try {
        val res = Fresco.getImagePipelineFactory().mainFileCache
            .insert(cacheKey) { os -> copyStream(`is`, os) }
        if (res is FileBinaryResource) {
            return res.file
        }
    } catch (e: IOException) {
        // ignore
    } finally {
        closeSafely(`is`)
    }

    return null
}

fun copyStream(`is`: InputStream, os: OutputStream) {
    val bufferSize = 1024 * 8
    try {
        val bytes = ByteArray(bufferSize)
        while (true) {
            val count = `is`.read(bytes, 0, bufferSize)
            if (count == -1)
                break
            os.write(bytes, 0, count)
        }
    } catch (ex: Exception) {
        // ignored
    }
}

fun closeSafely(closeable: Closeable?) {
    closeable ?: return

    try {
        closeable.close()
    } catch (e: Exception) {
        // ignored
    }
}