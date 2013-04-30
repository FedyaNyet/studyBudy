package com.fyodorwolf.studybudy;

import android.app.Application;
import com.fyodorwolf.studybudy.helpers.DatabaseAdapter;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class UILApplication extends Application {
 
    @Override
    public void onCreate() {
        super.onCreate();
 
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
            .threadPoolSize(3)
            .threadPriority(Thread.NORM_PRIORITY - 2)
            .memoryCacheSize(1500000) // 1.5 Mb
            .denyCacheImageMultipleSizesInMemory()
            .discCacheFileNameGenerator(new Md5FileNameGenerator())
            .defaultDisplayImageOptions(
        		new DisplayImageOptions.Builder()
		            .showStubImage(R.drawable.gallary_default)
		            .showImageForEmptyUri(R.drawable.gallary_error)
		            .cacheInMemory()
		            .cacheOnDisc()
		            .build()
            )
//            .enableLogging() // Not necessary in common
            .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
        
        DatabaseAdapter.getInstance().init(this);
    }
}
