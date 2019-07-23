package com.juniperphoton.frescorfsfixingbug.storage;

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.cache.disk.DiskStorage;
import com.facebook.imagepipeline.core.DiskStorageFactory;

public class SampleDynamicDiskStorageFactory implements DiskStorageFactory {
    @Override
    public DiskStorage get(DiskCacheConfig diskCacheConfig) {
        return new SampleDynamicDiskStorage(
                diskCacheConfig.getVersion(),
                diskCacheConfig.getBaseDirectoryPathSupplier(),
                diskCacheConfig.getBaseDirectoryName(),
                diskCacheConfig.getCacheErrorLogger());
    }
}
