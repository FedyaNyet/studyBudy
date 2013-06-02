package com.fyodorwolf.studybudy;

import com.fyodorwolf.studybudy.db.DatabaseAdapter;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

public class SplashScreenActivity extends Activity {

	@Override public void onCreate(Bundle savedInstanceState){
	      super.onCreate(savedInstanceState);//Remove title bar
	      
	      // set the content view for your splash screen defined in an xml file
	      setContentView(R.layout.splashscreen);

	      // execute your background stuff
	      new AsyncInitApp().execute();

	   }
	

    
    private class AsyncInitApp extends AsyncTask<Void, Void, Void>{

		@Override protected Void doInBackground(Void... arg0) {
	 
	        //  ImageLoaderConfiguration.createDefault(this);
			Context appContext = SBApplication.getAppContext();
	        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(appContext)
	            .threadPoolSize(3)
	            .threadPriority(Thread.NORM_PRIORITY - 2)
	            .memoryCacheSize(1500000) // 1.5 Mb
	            .denyCacheImageMultipleSizesInMemory()
	            .discCacheFileNameGenerator(new Md5FileNameGenerator())
//	          .enableLogging() // Not necessary in common
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
	        DatabaseAdapter.getInstance().init(appContext);
			return null;
		}

		@Override protected void onPostExecute(Void params){
			Intent intent = new Intent(SplashScreenActivity.this, DecksActivity.class);
			startActivity(intent);
			// close this activity
			finish();
		}
    }
}
