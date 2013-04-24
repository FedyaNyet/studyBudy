package com.fyodorwolf.studybudy;

import java.util.Collections;

import com.fyodorwolf.studybudy.helpers.DatabaseAdapter;
import com.fyodorwolf.studybudy.helpers.QueryRunner;
import com.fyodorwolf.studybudy.helpers.ViewFlipper;
import com.fyodorwolf.studybudy.helpers.QueryRunner.QueryRunnerListener;
import com.fyodorwolf.studybudy.helpers.ViewFlipper.ViewSwapperListener;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.support.v4.view.ViewPager;

public class DeckActivity extends Activity implements ViewPager.PageTransformer {

	private static final String TAG = "ListActivity";
	DatabaseAdapter myDB;
	
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

		/* SET VIEWS AND COMPONENT VISIBILITY */
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
		long[] cardIds =  getIntent().getExtras().getLongArray("com.example.studyBudy.cardIds");
		myDeck = new Deck(deckId,deckName);
		
        setTitle(myDeck.name);
        
        
        /* DEFINE GESTURES*/
        gestureDetector = new GestureDetector(this, new SimpleOnGestureListener(){
			@Override public boolean onDoubleTap(MotionEvent e) { return super.onDoubleTap(e);}
			@Override public void onLongPress(MotionEvent e) { super.onLongPress(e);}
			@Override public boolean onSingleTapConfirmed(MotionEvent e) { return super.onSingleTapConfirmed(e);}
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
        });

        /* DEFINE BUTTON PRESSES */
        final QueryRunnerListener setStatusQueryListener = new QueryRunnerListener(){
			@Override public void beforeDoInBackground() {}
			@Override public void onPostExcecute(Cursor cards) {
				findViewById(R.id.skip_button).setSoundEffectsEnabled(false);
				findViewById(R.id.skip_button).performClick();
			}
		};
        findViewById(R.id.button_correct).setOnClickListener(new OnClickListener(){
			@Override public void onClick(View v) {
				//setCard Correct
				QueryRunner setStatusQuery = new QueryRunner(myDB);
				setStatusQuery.setQueryRunnerListener(setStatusQueryListener);
				long card_id = myDeck.cards.get(myDeckCardIndex).id;
				myDeck.getCardWithId(card_id).status = Card.STATUS_CORRECT;
				setStatusQuery.execute(DatabaseAdapter.setStatusForCard(card_id,Card.STATUS_CORRECT));
			}
		});
        findViewById(R.id.button_wrong).setOnClickListener(new OnClickListener(){
			@Override public void onClick(View v) {
				//setCard incorrect
				QueryRunner setStatusQuery = new QueryRunner(myDB);
				setStatusQuery.setQueryRunnerListener(setStatusQueryListener);
				long card_id = myDeck.cards.get(myDeckCardIndex).id;
				myDeck.getCardWithId(card_id).status = Card.STATUS_WRONG;
				setStatusQuery.execute(DatabaseAdapter.setStatusForCard(card_id,Card.STATUS_WRONG));
			}
		});
        
        /* SET CARD DATA */
    	myDB = DatabaseAdapter.getInstance(this);
    	QueryRunner query = new QueryRunner(myDB);
    	query.setQueryRunnerListener(new QueryRunnerListener(){
			@Override public void beforeDoInBackground() {}
			@Override public void onPostExcecute(Cursor cards) {
				gotCards(cards);
			}
        });
    	String queryString = DatabaseAdapter.cardsWithDeckIdQuery(deckId);
    	if(cardIds != null)
    		queryString = DatabaseAdapter.cardsWithIds(cardIds);
    	query.execute(queryString);
    	
        super.onCreate(savedInstanceState);
	}// E:onCreate

	@Override public void transformPage(View arg0, float arg1) {}
	
    @Override  public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.card, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Pressed home (Up) 
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
	    	
	    	Card myCard = myDeck.cards.get(myDeckCardIndex);
	    	final CharSequence prevQuestion = myCard.question;
	    	final CharSequence prevAnswer = myCard.answer;
	    	final int prevStatus = myCard.getStatusImageResource();
	    	final String prevCardId = Long.toString(myCard.id);
	    	
			/*set up the visibility properly*/
			animatedCardFront.setVisibility(View.VISIBLE);
			
			/*make sure the order is correct to produce the stack effect...*/
			cardFront.bringToFront();
			cardBack.bringToFront();
			animatedCardFront.bringToFront();

			((TextView) animatedCardFront.findViewById(R.id.question_text)).setText(prevQuestion);
			((ImageView) animatedCardFront.findViewById(R.id.card_status)).setImageResource(prevStatus);
			((TextView) animatedCardFront.findViewById(R.id.card_id)).setText(prevCardId);
	
			Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.animator.slide_in_down);
			anim.setAnimationListener(new AnimationListener(){
				@Override public void onAnimationEnd(Animation animation) {
					((TextView) cardBack.findViewById(R.id.answer_text)).setText(prevAnswer);
					((TextView) cardFront.findViewById(R.id.question_text)).setText(prevQuestion);
					((ImageView) cardFront.findViewById(R.id.card_status)).setImageResource(prevStatus);
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
	
	private void addCardAtCursorHeadToMyDeck(Cursor result){
		long id = result.getLong(0);
		String question = result.getString(1);
		String answer = result.getString(2);
		Integer status = result.getInt(3);
		Integer position = result.getInt(4);
		myDeck.cards.add(new Card(id,question,answer,status,position));
	}
    
    public void gotCards(Cursor cards){
    	Cursor result = cards;
		if(result.moveToFirst()){
			addCardAtCursorHeadToMyDeck(result);
			while(result.moveToNext()){
				addCardAtCursorHeadToMyDeck(result);
			}
			cardFront.setVisibility(View.VISIBLE);
			actionsView.setVisibility(View.VISIBLE);
			Card myCard = myDeck.cards.get(myDeckCardIndex);
			
			((ImageView) cardFront.findViewById(R.id.card_status)).setImageResource(myCard.getStatusImageResource());
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
						
						Card myCard = myDeck.cards.get(myDeckCardIndex);
						
						CharSequence oldQuestion = myCard.question;
						int cardStatus = myCard.getStatusImageResource();
						CharSequence oldCardId = Long.toString(myCard.id);

						((TextView)animatedCardFront.findViewById(R.id.question_text)).setText(oldQuestion);
						((ImageView)animatedCardFront.findViewById(R.id.card_status)).setImageResource(cardStatus);
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
					int newStatus = myDeck.cards.get(myDeckCardIndex).getStatusImageResource();
					String newCardId = Long.toString(myDeck.cards.get(myDeckCardIndex).id);
					
					((TextView) cardFront.findViewById(R.id.question_text)).setText(newQuestion);
					((ImageView) cardFront.findViewById(R.id.card_status)).setImageResource(newStatus);
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
					ViewFlipper rotation;
					if (showingCardFront){
						rotation = new ViewFlipper(cardFront, cardBack);
						rotation.setDirection(ViewFlipper.ROTATE_LEFT);
					} else {
						rotation = new ViewFlipper(cardBack, cardFront);
						rotation.setDirection(ViewFlipper.ROTATE_RIGHT);
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
    }//E: gotCards
	
}