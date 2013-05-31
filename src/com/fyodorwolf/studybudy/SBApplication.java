package com.fyodorwolf.studybudy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.fyodorwolf.studybudy.db.DatabaseAdapter;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class SBApplication extends Application {
 
    private static Context context;
	
    @Override public void onCreate() {
        super.onCreate();
        SBApplication.context = getApplicationContext();
 
        //  ImageLoaderConfiguration.createDefault(this);
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
            .threadPoolSize(3)
            .threadPriority(Thread.NORM_PRIORITY - 2)
            .memoryCacheSize(1500000) // 1.5 Mb
            .denyCacheImageMultipleSizesInMemory()
            .discCacheFileNameGenerator(new Md5FileNameGenerator())
//          .enableLogging() // Not necessary in common
            .defaultDisplayImageOptions(
        		new DisplayImageOptions.Builder()
		            .showStubImage(R.drawable.gallary_default)
		            .showImageForEmptyUri(R.drawable.gallary_error)
		            .cacheInMemory()
		            .cacheOnDisc()
		            .build()
            )
            .build();
        ImageLoader.getInstance().init(config);
        DatabaseAdapter.getInstance().init(this);
    }

	public static void copy(File src, File dst) throws IOException {
		if (!dst.getParentFile().exists() && !dst.getParentFile().mkdirs()){
			Log.d(CardFormActivity.TAG,"Unable to create:" + dst.getParentFile());
		}
	    InputStream mInput = new FileInputStream(src);
	    OutputStream mOutput = new FileOutputStream(dst);
	    byte[] mBuffer = new byte[1024];
	    int mLength;
	    while ((mLength = mInput.read(mBuffer))>0)
	    {
	        mOutput.write(mBuffer, 0, mLength);
	    }
	    mOutput.flush();
	    mOutput.close();
	    mInput.close();
	}

	public static void removeFiles(HashSet<String> filePaths){
		String appDir = getAppContext().getFilesDir()+"/";
		File[] files =  new File(appDir).listFiles();
		for(File file : files){
			String existingFilePath = file.getAbsolutePath();
			if(!filePaths.contains(existingFilePath)){
				file.delete();
			}
		}
	}

    public static Context getAppContext() {
        return SBApplication.context;
    }
}
