package com.juniperphoton.frescobug

import android.app.Application
import com.facebook.cache.disk.DiskCacheConfig
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory
import com.juniperphoton.frescobug.storage.SampleDiskStorageCacheFactory
import com.juniperphoton.frescobug.storage.SampleDynamicDiskStorageFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        val diskCacheConfig = DiskCacheConfig.newBuilder(this)
            .setBaseDirectoryPathSupplier { this.cacheDir }
            .setBaseDirectoryName("image_caches")
            .build()

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val pipelineConfig = OkHttpImagePipelineConfigFactory.newBuilder(this, client)
            .setMainDiskCacheConfig(diskCacheConfig)
            .setFileCacheFactory(SampleDiskStorageCacheFactory(SampleDynamicDiskStorageFactory()))
            .build()

        Fresco.initialize(this, pipelineConfig)
    }
}