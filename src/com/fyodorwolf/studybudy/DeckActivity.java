package com.fyodorwolf.studybudy;

import com.fyodorwolf.studybudy.ViewSwapper.ViewSwapperListener;
import com.fyodorwolf.studybudy.models.*;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.support.v4.view.ViewPager;

public class DeckActivity extends Activity implements ViewPager.PageTransformer {

	private static final String TAG = "ListActivity";
	private DatabaseAdapter myDB;
	
	private RelativeLayout cardFront;
	private RelativeLayout cardBack;
	private RelativeLayout animatedCardFront;
	private boolean showingCardFront = true;
	boolean animating = false;
    public Deck myDeck;
	int myDeckCardIndex = 0;
    
	private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 2000;

    GestureDetector gestureDetector;
    
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return gestureDetector.onTouchEvent(event);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState){

		setContentView(R.layout.card_view);
		cardFront = (RelativeLayout)findViewById(R.id.card_front);
		cardBack  = (RelativeLayout)findViewById(R.id.card_back);
		animatedCardFront  = (RelativeLayout)findViewById(R.id.animated_card_front);
		cardFront.setVisibility(View.GONE);
		cardBack.setVisibility(View.GONE);
		animatedCardFront.setVisibility(View.GONE);
		
	    getActionBar().setDisplayHomeAsUpEnabled(true);

		long deckId =  getIntent().getExtras().getLong("com.example.studyBudy.deckId");
		String deckName =  getIntent().getExtras().getString("com.example.studyBudy.deckName");
		myDeck = new Deck(deckId,deckName);
		
        setTitle(myDeck.name);

        gestureDetector = new GestureDetector(this, new SimpleOnGestureListener(){
			@Override
			public boolean onDoubleTap(MotionEvent e) {
				// TODO Auto-generated method stub
				Log.d(TAG,"onDoubleTap");
				return super.onDoubleTap(e);
			}
			
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				// TODO Auto-generated method stub
	            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
	           	 	//vertical swipe
		            if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
			            // bottom to top swipe
		            	findViewById(R.id.skip_button).performClick();
		            } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
			    	    // top to bottom swipe
			    		Log.d(TAG,"top-bottom");
		            }
	            }else{
	           	 	//horizontal swipe
					findViewById(R.id.flip_button).performClick();
//		            if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//			            // left to right swipe
//						if (!showingCardFront)
//							findViewById(R.id.flip_button).performClick();
//		            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//			    	    // right to left swipe
//						if (showingCardFront)
//							findViewById(R.id.flip_button).performClick();
//		            }
	            }
				return super.onFling(e1, e2, velocityX, velocityY);
			}
			
			@Override
			public void onLongPress(MotionEvent e) {
				Log.d(TAG,"onLongPress");
				super.onLongPress(e);
			}
			
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				// TODO Auto-generated method stub
				Log.d(TAG,"onSingleTapConfirmed: \n" + e.toString());
				return super.onSingleTapConfirmed(e);	
			}
        });

    	myDB = DatabaseAdapter.getInstance(this);
        DeckGetter getCards = new DeckGetter();
        getCards.execute(DatabaseAdapter.cardsWithDeckIdQuery(deckId));
        
        super.onCreate(savedInstanceState);
	}

	
	@Override
	public void transformPage(View arg0, float arg1) {
		Log.d(TAG,Float.toString(arg1));
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
				((TextView) cardFront.findViewById(R.id.question_text)).setText(myDeck.cards.get(myDeckCardIndex).question);
				((TextView) cardBack.findViewById(R.id.answer_text)).setText(myDeck.cards.get(myDeckCardIndex).answer);
			}
			findViewById(R.id.skip_button).setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					View animatedCard;
					cardFront.setVisibility(View.VISIBLE);
					if(showingCardFront){
						CharSequence oldQuestion = ((TextView) cardFront.findViewById(R.id.question_text)).getText();
						((TextView)animatedCardFront.findViewById(R.id.question_text)).setText(oldQuestion);
						cardBack.setVisibility(View.GONE);
						animatedCardFront.setVisibility(View.VISIBLE);
						animatedCard = animatedCardFront;
					}else{
						cardBack.setVisibility(View.VISIBLE);
						animatedCardFront.setVisibility(View.GONE);
						animatedCard = cardBack;
					}
					/*make sure the order is correct to produce the stack effect...*/
					cardFront.bringToFront();
					cardBack.bringToFront();
					animatedCardFront.bringToFront();
					
					myDeckCardIndex = (myDeckCardIndex+1) % myDeck.cards.size();
					CharSequence newQuestion = myDeck.cards.get(myDeckCardIndex).question+myDeckCardIndex;
					((TextView) cardFront.findViewById(R.id.question_text)).setText(newQuestion);

					Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.animator.slide_out_up);
					anim.setAnimationListener(new AnimationListener(){
						@Override public void onAnimationEnd(Animation animation) {
							CharSequence newAnswer = myDeck.cards.get(myDeckCardIndex).answer+myDeckCardIndex;
							((TextView) cardBack.findViewById(R.id.answer_text)).setText(newAnswer);
							cardBack.setVisibility(View.GONE);
							animatedCardFront.setVisibility(View.GONE);
							showingCardFront = true;
						}
						@Override public void onAnimationRepeat(Animation animation) {}
						@Override public void onAnimationStart(Animation animation) {}
					});
					animatedCard.startAnimation(anim);
				}
			});
			findViewById(R.id.flip_button).setOnClickListener(new OnClickListener() {
				@Override public void onClick(View view) {
					if(!animating){
						animating = true;
						ViewSwapper rotation;
						if (showingCardFront){
							rotation = new ViewSwapper(cardFront, cardBack);
							rotation.setDirection(ViewSwapper.ROTATE_LEFT);
						} else {
							rotation = new ViewSwapper(cardBack, cardFront);
							rotation.setDirection(ViewSwapper.ROTATE_RIGHT);
						}
						rotation.setDuration(400);
						rotation.addViewSwapperListener(new ViewSwapperListener(){
							@Override public void onViewSwapperStart() {}
							@Override public void onViewSwapperHalfComplete() {}
							@Override public void onViewSwapperComplete() {
						    	animating = false;
							}
						});
						rotation.run();
						showingCardFront = !showingCardFront;
					}
				}
			}); 
		}
		
	}

	
}