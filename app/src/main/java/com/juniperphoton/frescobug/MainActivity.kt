package com.juniperphoton.frescobug

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.common.executors.CallerThreadExecutor
import com.facebook.common.memory.PooledByteBuffer
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.request.ImageRequest
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileInputStream
import java.io.IOException

const val DEBUG_TAG = "cachetest"

class MainActivity : AppCompatActivity() {
    companion object {
        private const val DELAY_MS = 50L
    }

    private var index = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        goButton.setOnClickListener {
            postToLoad()
        }

        clearButton.setOnClickListener {
            Fresco.getImagePipeline().clearCaches()
            Toast.makeText(this, "Cache cleared", Toast.LENGTH_SHORT).show()
        }

        clearButton.performClick()
    }

    private fun postToLoad() {
        if (index >= urls.size) {
            return
        }
        window.decorView.postDelayed({
            load()
            postToLoad()
        }, DELAY_MS)
    }

    private fun load() {
        val currentIndex = index
        val url = urls[index++]
        fetchAndCache(url) { f ->
            if (f == null) {
                Log.e(DEBUG_TAG, "file is null: $url")
            } else {
                tryDecodeBitmapBounds(url, currentIndex, f)
            }
        }
    }

    private fun tryDecodeBitmapBounds(originalUrl: String, index: Int, file: File) {
        var fis: FileInputStream? = null
        try {
            fis = FileInputStream(file)
            Log.i(DEBUG_TAG, "no exception $index, $originalUrl, $file")
        } catch (e: IOException) {
            Log.e(DEBUG_TAG, "io exception: $index, $originalUrl, $file, $e")
        } catch (e: Exception) {
            Log.e(DEBUG_TAG, "unexpected exception: $index, $originalUrl, $file, $e")
        } finally {
            closeSafely(fis)
        }
    }

    private fun fetchAndCache(url: String, callback: ((File?) -> Unit)) {
        val ds = Fresco.getImagePipeline()
            .fetchEncodedImage(ImageRequest.fromUri(url), null)

        val s = object : FetchDataSubscriber(url) {
            override fun onFileCacheInserted(file: File) {
                callback.invoke(file)
            }

            override fun onFailureImpl(dataSource: DataSource<CloseableReference<PooledByteBuffer>>) {
                val error = dataSource.failureCause

                // The image service of https://picsum.photos can NOT guarantee the existence of photos with some ids.
                // We just ignore the 404 situation
                if (error?.message?.contains("code=404") == true) {
                    return
                }
                Log.i(DEBUG_TAG, "onFailureImpl: ${dataSource.failureCause}")
                callback.invoke(null)
            }
        }
        ds.subscribe(s, CallerThreadExecutor.getInstance())
    }
}