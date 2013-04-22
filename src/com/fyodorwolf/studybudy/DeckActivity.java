package com.fyodorwolf.studybudy;

import java.util.Collections;

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
import android.view.SoundEffectConstants;
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
	private RelativeLayout actionsView;
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
		actionsView = (RelativeLayout)findViewById(R.id.card_actions);
		
		cardFront.setVisibility(View.GONE);
		cardBack.setVisibility(View.GONE);
		animatedCardFront.setVisibility(View.GONE);
		actionsView.setVisibility(View.GONE);
		
	    getActionBar().setDisplayHomeAsUpEnabled(true);

		long deckId =  getIntent().getExtras().getLong("com.example.studyBudy.deckId");
		String deckName =  getIntent().getExtras().getString("com.example.studyBudy.deckName");
		myDeck = new Deck(deckId,deckName);
		
        setTitle(myDeck.name);

        findViewById(R.id.button_correct).setOnClickListener(new OnClickListener(){
			@Override public void onClick(View v) {
				//setCard Correct
				StatusSetter setStatus = new StatusSetter();
				long card_id = myDeck.cards.get(myDeckCardIndex).id;
				myDeck.getCardWithId(card_id).status = Card.STATUS_CORRECT;
		        setStatus.execute(DatabaseAdapter.setStatusForCard(card_id,Card.STATUS_CORRECT));
			}
		});
        findViewById(R.id.button_wrong).setOnClickListener(new OnClickListener(){
			@Override public void onClick(View v) {
				//setCard incorrect
				StatusSetter setStatus = new StatusSetter();
				long card_id = myDeck.cards.get(myDeckCardIndex).id;
				myDeck.getCardWithId(card_id).status = Card.STATUS_WRONG;
		        setStatus.execute(DatabaseAdapter.setStatusForCard(card_id,Card.STATUS_WRONG));
			}
		});
        
        gestureDetector = new GestureDetector(this, new SimpleOnGestureListener(){
			@Override public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
	            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
	           	 	if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
			            // down swipe

	        			findViewById(R.id.skip_button).setSoundEffectsEnabled(false);
		            	findViewById(R.id.skip_button).performClick();
		            } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
			    	    // up swipe
		            	previousCard();
		            }
	            }else{
	           	 	//left or right swipe
	    			findViewById(R.id.flip_button).setSoundEffectsEnabled(false);
					findViewById(R.id.flip_button).performClick();
	            }
				return super.onFling(e1, e2, velocityX, velocityY);
			}
			@Override public boolean onDoubleTap(MotionEvent e) {
				return super.onDoubleTap(e);
			}
			@Override public void onLongPress(MotionEvent e) {
				super.onLongPress(e);
			}
			@Override public boolean onSingleTapConfirmed(MotionEvent e) {
				return super.onSingleTapConfirmed(e);	
			}
        });

    	myDB = DatabaseAdapter.getInstance(this);
        DeckGetter getCards = new DeckGetter();
        getCards.execute(DatabaseAdapter.cardsWithDeckIdQuery(deckId));
        
        super.onCreate(savedInstanceState);
	}

	@Override public void transformPage(View arg0, float arg1) {}
	
    @Override  public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.card, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
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
            case R.id.card_menu_shuffle:
            	Collections.shuffle(myDeck.cards);
            	findViewById(R.id.skip_button).setSoundEffectsEnabled(false);
            	findViewById(R.id.skip_button).performClick();
            	return true;
            case R.id.card_menu_show_previous:
            	this.previousCard();
            	return false;
            	
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void previousCard(){
    	if(!animating){
	    	animating = true;
			showingCardFront = true;
	    	myDeckCardIndex = ((myDeckCardIndex + myDeck.cards.size()) - 1) % myDeck.cards.size();
	    	final CharSequence prevQuestion = myDeck.cards.get(myDeckCardIndex).question;
	    	final CharSequence prevAnswer = myDeck.cards.get(myDeckCardIndex).answer;
	    	final String prevStatus = Integer.toString(myDeck.cards.get(myDeckCardIndex).status);
	    	final String prevCardId = Long.toString(myDeck.cards.get(myDeckCardIndex).id);
	    	
			/*set up the visibility properly*/
			animatedCardFront.setVisibility(View.VISIBLE);
			
			/*make sure the order is correct to produce the stack effect...*/
			cardFront.bringToFront();
			cardBack.bringToFront();
			animatedCardFront.bringToFront();

			((TextView) animatedCardFront.findViewById(R.id.question_text)).setText(prevQuestion);
			((TextView) animatedCardFront.findViewById(R.id.status_text)).setText(prevStatus);
			((TextView) animatedCardFront.findViewById(R.id.card_id)).setText(prevCardId);
	
			Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.animator.slide_in_down);
			anim.setAnimationListener(new AnimationListener(){
				@Override public void onAnimationEnd(Animation animation) {
					((TextView) cardBack.findViewById(R.id.answer_text)).setText(prevAnswer);
					((TextView) cardFront.findViewById(R.id.question_text)).setText(prevQuestion);
					((TextView) cardFront.findViewById(R.id.status_text)).setText(prevStatus);
					((TextView) cardFront.findViewById(R.id.card_id)).setText(prevCardId);
					cardFront.setVisibility(View.VISIBLE);
					cardBack.setVisibility(View.GONE);
					animatedCardFront.setVisibility(View.GONE);
			    	animating = false;
				}
				@Override public void onAnimationRepeat(Animation animation) {}
				@Override public void onAnimationStart(Animation animation) {}
			});
			animatedCardFront.startAnimation(anim);
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
			if(result.moveToFirst()){
				addCardAtCursorHeadToMyDeck(result);
				while(result.moveToNext()){
					addCardAtCursorHeadToMyDeck(result);
				}
				cardFront.setVisibility(View.VISIBLE);
				actionsView.setVisibility(View.VISIBLE);
				Card myCard = myDeck.cards.get(myDeckCardIndex);
				Log.d(TAG,Integer.toString(R.id.status_text));
				Log.d(TAG,Integer.toString(myCard.status));
				
				((TextView) cardFront.findViewById(R.id.status_text)).setText(Integer.toString(myCard.status));
				((TextView) cardFront.findViewById(R.id.card_id)).setText(Float.toString(myCard.id));
				((TextView) cardFront.findViewById(R.id.question_text)).setText(myCard.question);
				((TextView) cardBack.findViewById(R.id.answer_text)).setText(myCard.answer);
			}
			findViewById(R.id.skip_button).setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
	            	findViewById(R.id.skip_button).setSoundEffectsEnabled(true);
					if(!animating){
						animating = true;
						View animatedCard;
						cardFront.setVisibility(View.VISIBLE);
						if(showingCardFront){
							
							CharSequence oldQuestion = ((TextView) cardFront.findViewById(R.id.question_text)).getText();
							CharSequence cardStatus = ((TextView) cardFront.findViewById(R.id.status_text)).getText();
							CharSequence oldCardId = ((TextView) cardFront.findViewById(R.id.card_id)).getText();

							((TextView)animatedCardFront.findViewById(R.id.question_text)).setText(oldQuestion);
							((TextView)animatedCardFront.findViewById(R.id.status_text)).setText(cardStatus);
							((TextView)animatedCardFront.findViewById(R.id.card_id)).setText(oldCardId);
							
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
						
						CharSequence newQuestion = myDeck.cards.get(myDeckCardIndex).question;
						String newStatus = Integer.toString(myDeck.cards.get(myDeckCardIndex).status);
						String newCardId = Long.toString(myDeck.cards.get(myDeckCardIndex).id);
						
						((TextView) cardFront.findViewById(R.id.question_text)).setText(newQuestion);
						((TextView) cardFront.findViewById(R.id.status_text)).setText(newStatus);
						((TextView) cardFront.findViewById(R.id.card_id)).setText(newCardId);
	
						Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.animator.slide_out_up);
						anim.setAnimationListener(new AnimationListener(){
							@Override public void onAnimationEnd(Animation animation) {
								CharSequence newAnswer = myDeck.cards.get(myDeckCardIndex).answer;
								((TextView) cardBack.findViewById(R.id.answer_text)).setText(newAnswer);
								cardBack.setVisibility(View.GONE);
								animatedCardFront.setVisibility(View.GONE);
								showingCardFront = true;
								animating=false;
							}
							@Override public void onAnimationRepeat(Animation animation) {}
							@Override public void onAnimationStart(Animation animation) {}
						});
						animatedCard.startAnimation(anim);
					}
				}
			});
			findViewById(R.id.flip_button).setOnClickListener(new OnClickListener() {
				@Override public void onClick(View view) {
					findViewById(R.id.flip_button).setSoundEffectsEnabled(true);
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
	}//E:DeckGetter

	private class StatusSetter extends AsyncTask<String,Integer,Cursor>{

		@Override
		protected Cursor doInBackground(String... params) {
			return myDB.getCursor(params[0]);
		}

		@Override
		protected void onPostExecute(final Cursor result) {
			//skip to next
			findViewById(R.id.skip_button).setSoundEffectsEnabled(false);
			findViewById(R.id.skip_button).performClick();
			
		}
	}//E:StatusSetter
	
}