package com.fyodorwolf.studybudy;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class SBApplication extends Application {
	
    private final static String TAG = "SBApplication";
    private static Context context;
	
    @Override public void onCreate() {
        super.onCreate();
        SBApplication.context = getApplicationContext();
    }

	public static void copy(File src, File dst) throws IOException {
		if (!dst.getParentFile().exists() && !dst.getParentFile().mkdirs()){
			Log.d(CardFormActivity.TAG,"Unable to create:" + dst.getParentFile());
		}
		FileUtils.copyFile(src, dst);
	}
	
	
	public static void removeFiles(File directory){
		try {
			Log.d(TAG, "deleteing files from: "+directory.getAbsolutePath());
			FileUtils.deleteDirectory(directory);
		} catch (IOException e) {
			Log.e(TAG,e.getMessage());
		}
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
