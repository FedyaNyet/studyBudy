package com.fyodorwolf.studybudy;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;

public class DeckActivity extends Activity {

		
	
	private ImageView image1;
	private ImageView image2;
	
	private boolean isFirstImage = true;

	private void applyRotation(float start, float end) {
		// Find the center of image
		final float centerX = image1.getWidth() / 2.0f;
		final float centerY = image1.getHeight() / 2.0f;
		
		// Create a new 3D rotation with the supplied parameter
		// The animation listener is used to trigger the next animation
		final Flip3dAnimation rotation = new Flip3dAnimation(start, end, centerX, centerY);
		rotation.setDuration(500);
		rotation.setFillAfter(true);
		rotation.setInterpolator(new AccelerateInterpolator());
		rotation.setAnimationListener(new DisplayNextView(isFirstImage, image1, image2));
		
		if (isFirstImage)
		{
			image1.startAnimation(rotation);
		} else {
			image2.startAnimation(rotation);
		}
	
	}
	
	private static final String TAG = "ListActivity";
	private DatabaseAdapter myDB;
    

	@Override
	protected void onCreate(Bundle savedInstanceState){

	    setContentView(R.layout.card_view);
	    getActionBar().setDisplayHomeAsUpEnabled(true);

		long sectionId =  getIntent().getExtras().getLong("com.example.studyBudy.deckId");
		Log.d(TAG,"SectionID: "+String.valueOf(sectionId));
        setTitle("DeckName");

		super.onCreate(savedInstanceState);
		
		image1 = (ImageView) findViewById(R.id.ImageView01);
		image2 = (ImageView) findViewById(R.id.ImageView02);
		image2.setVisibility(View.GONE);
		
		image1.setOnClickListener(new View.OnClickListener() {
		   public void onClick(View view) {
		    if (isFirstImage) {       
		     applyRotation(0, 90);
		     isFirstImage = !isFirstImage;
		
		    } else {    
		     applyRotation(0, -90);
		     isFirstImage = !isFirstImage;
		    }
		   }
		});  
        super.onCreate(savedInstanceState);
	}

    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This is called when the Home (Up) button is pressed
                // in the Action Bar.
                Intent parentActivityIntent = new Intent(this, MainActivity.class);
                parentActivityIntent.addFlags(
                        Intent.FLAG_ACTIVITY_NO_ANIMATION|
                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(parentActivityIntent);
                overridePendingTransition(0,0);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    
/********************************************************************************************************************************************
 * 							Private Classes		 																							*
 ********************************************************************************************************************************************/
	private class DeckGetter extends AsyncTask<String,Integer,Cursor>{

		@Override
		protected Cursor doInBackground(String... params) {
			return myDB.getCursor(params[0]);
		}

		@Override
		protected void onPostExecute(final Cursor result) {
			
		}
		
	}
	
	
	
}