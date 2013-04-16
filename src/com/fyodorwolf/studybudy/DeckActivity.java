package com.fyodorwolf.studybudy;

import java.util.ArrayList;
import java.util.Iterator;

import com.fyodorwolf.studybudy.models.*;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DeckActivity extends Activity {

	private static final String TAG = "ListActivity";
	private DatabaseAdapter myDB;
	
	private RelativeLayout cardFront;
	private RelativeLayout cardBack;
	private boolean isFirstImage = true;
	boolean animating = false;
	long cardRotationSpeed = 200;
    public Deck myDeck;

	@Override
	protected void onCreate(Bundle savedInstanceState){

		setContentView(R.layout.card_view);
		cardFront = (RelativeLayout)findViewById(R.id.card_front);
		cardBack  = (RelativeLayout)findViewById(R.id.card_back);
		cardFront.setVisibility(View.GONE);
		cardBack.setVisibility(View.GONE);
	    getActionBar().setDisplayHomeAsUpEnabled(true);

		long deckId =  getIntent().getExtras().getLong("com.example.studyBudy.deckId");
		String deckName =  getIntent().getExtras().getString("com.example.studyBudy.deckName");
		myDeck = new Deck(deckId,deckName);
        setTitle(deckName);
		
        DeckGetter getCards = new DeckGetter();
        getCards.execute(DatabaseAdapter.cardsWithDeckIdQuery(deckId));

    	myDB = DatabaseAdapter.getInstance(this);
        super.onCreate(savedInstanceState);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.card, menu);
        return true;
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
    
    public void cardFlipComplete(){
    	this.animating = false;
    }


	private void applyRotation(float start, float end) {
		// Find the center of image
		final float centerX = cardFront.getWidth() / 2.0f;
		final float centerY = cardFront.getHeight() / 2.0f;
		
		// Create a new 3D rotation with the supplied parameter
		// The animation listener is used to trigger the next animation
		final Flip3dAnimation rotation = new Flip3dAnimation(start, end, centerX, centerY);
		rotation.setDuration(this.cardRotationSpeed);
		rotation.setFillAfter(true);
		rotation.setInterpolator(new AccelerateInterpolator());
		rotation.setAnimationListener(new DisplayNextView(isFirstImage, cardFront, cardBack, this));
		if (isFirstImage){
			cardFront.startAnimation(rotation);
		} else {
			cardBack.startAnimation(rotation);
		}
	
	}
    
/********************************************************************************************************************************************
 * 							Private Classes		 																							*
 ********************************************************************************************************************************************/
	private class DeckGetter extends AsyncTask<String,Integer,Cursor>{

		private static final String TAG = "DeckGetter";
		
		@Override
		protected Cursor doInBackground(String... params) {
			return myDB.getCursor(params[0]);
		}
		
		private void addCardAtCursorHeadToMyDeck(Cursor result){
			long id = result.getLong(0);
			String question = result.getString(1);
			String answer = result.getString(2);
			Integer status = result.getInt(3);
			Integer position = result.getInt(4);
			myDeck.cards.add(new Card(id,question,answer,status,position));
		}
		
		@Override
		protected void onPostExecute(final Cursor result) {

			Log.d(TAG,"id | questiong | answer | status | position");

			if(result.moveToFirst()){
				addCardAtCursorHeadToMyDeck(result);
				while(result.moveToNext()){
					addCardAtCursorHeadToMyDeck(result);
				}
				cardFront.setVisibility(View.VISIBLE);
				((TextView) cardFront.findViewById(R.id.question_text)).setText(myDeck.cards.get(0).question);
				((TextView)cardBack.findViewById(R.id.answer_text)).setText(myDeck.cards.get(0).answer);
			}
//			for (Iterator<Card> itr = myDeck.cards.iterator(); itr.hasNext();) {
//				if (itr.next() == null) { itr.remove(); }
//			}
			
			findViewById(R.id.flip_button).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View view) {
					if(!animating){
						animating = true;
						if (isFirstImage) {      
							applyRotation(0, 90);
						} else { 
							applyRotation(0, -90);
						}
						isFirstImage = !isFirstImage;
					}
				}
			}); 
		}
		
	}
	
	
	
}