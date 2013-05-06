package com.fyodorwolf.studybudy;

import java.util.ArrayList;

import com.fyodorwolf.studybudy.db.DatabaseAdapter;
import com.fyodorwolf.studybudy.db.QueryRunner;
import com.fyodorwolf.studybudy.db.QueryString;
import com.fyodorwolf.studybudy.db.QueryRunner.QueryRunnerListener;
import com.fyodorwolf.studybudy.helpers.DeckAdapter;
import com.fyodorwolf.studybudy.helpers.ViewFlipper;
import com.fyodorwolf.studybudy.helpers.ViewFlipper.ViewSwapperListener;
import com.fyodorwolf.studybudy.models.*;
import com.fyodorwolf.studybudy.ui.HorizontalListView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.view.ViewPager;

public class DeckActivity extends Activity implements ViewPager.PageTransformer {

	public static final String TAG = "ListActivity";
	public static final String EXTRAS_DECK_ID = "com.fyodorwolf.studyBudy.deckId";
	public static final String EXTRAS_DECK_NAME = "com.fyodorwolf.studyBudy.deckName";
	public static final String EXTRAS_CARD_IDS = "com.fyodorwolf.studyBudy.cardIds";
	
	public static final int SWIPE_MIN_DISTANCE = 120;
	public static final int SWIPE_MAX_OFF_PATH = 250;
	public static final int SWIPE_THRESHOLD_VELOCITY = 2000;
	public static final long ANIMATION_DURATION = 300;
	
	/*Adapters*/
	private DatabaseAdapter myDB;
	private DeckAdapter myDeckAdapter;
	
	/*UX*/
    private GestureDetector gestureDetector;
	
    /*UI*/
	private RelativeLayout cardFront;
	private RelativeLayout cardBack;
	private RelativeLayout animatedCardFront;
	private RelativeLayout actionsView;
	
	/*BL*/
	private boolean showingCardFront = true;
	private boolean animating = false;
    

	@Override protected void onCreate(Bundle savedInstanceState){

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

		long deckId =  getIntent().getExtras().getLong(EXTRAS_DECK_ID);
		String deckName =  getIntent().getExtras().getString(EXTRAS_DECK_NAME);
		long[] cardIds =  getIntent().getExtras().getLongArray(EXTRAS_CARD_IDS);
		myDeckAdapter = new DeckAdapter(new Deck(deckId,deckName));
		
        setTitle(deckName);
        
        /* DEFINE BUTTON PRESSES */
        final QueryRunnerListener setStatusQueryListener = new QueryRunnerListener(){
			@Override public void onPostExcecute(Cursor cards) {
				nextCard();
			}
		};
        findViewById(R.id.button_correct).setOnClickListener(new OnClickListener(){
			@Override public void onClick(View v) {
				//setCard Correct
				QueryRunner setStatusQuery = new QueryRunner(myDB);
				setStatusQuery.setQueryRunnerListener(setStatusQueryListener);
				long card_id = myDeckAdapter.getCurrentCard().id;
				myDeckAdapter.setCardStatus(card_id, Card.STATUS_CORRECT);
				setStatusQuery.execute(QueryString.getCardUpdateStatusQuery(card_id,Card.STATUS_CORRECT));
			}
		});
        findViewById(R.id.button_wrong).setOnClickListener(new OnClickListener(){
			@Override public void onClick(View v) {
				//setCard incorrect
				QueryRunner setStatusQuery = new QueryRunner(myDB);
				setStatusQuery.setQueryRunnerListener(setStatusQueryListener);
				long card_id = myDeckAdapter.getIdOfCardAtIndex(myDeckAdapter.stackIndex);
				myDeckAdapter.setCardStatus(card_id, Card.STATUS_WRONG);
				setStatusQuery.execute(QueryString.getCardUpdateStatusQuery(card_id,Card.STATUS_WRONG));
			}
		});
        
        /* SET CARD DATA */
    	myDB = DatabaseAdapter.getInstance();
    	QueryRunner query = new QueryRunner(myDB);
    	query.setQueryRunnerListener(new QueryRunnerListener(){
			@Override public void onPostExcecute(Cursor cards) {
				buildDeck(cards);
			}
        });
    	String getCardQuery = QueryString.getCardsWithDeckIdQuery(deckId);
    	if(cardIds != null){
    		getCardQuery = QueryString.getCardsWithIdsQuery(cardIds);
    	}
    	query.execute(getCardQuery);

		/* DEFINE GESTURES */
        gestureDetector = new GestureDetector(this, new SimpleOnGestureListener(){
			@Override public boolean onDoubleTap(MotionEvent e) { return super.onDoubleTap(e);}
			@Override public void onLongPress(MotionEvent e) { super.onLongPress(e);}
			@Override public boolean onSingleTapConfirmed(MotionEvent e) {
    			flipCard(); //left or right swipe
				return super.onSingleTapConfirmed(e);
			}
			@Override public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				if(e1 != null && e2 != null){
		            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
		           	 	if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
		           	 		nextCard(); // down swipe
			            } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
			            	previousCard();// up swipe
			            }
		            }else{
		    			flipCard(); //left or right swipe
		            }
				}
				return super.onFling(e1, e2, velocityX, velocityY);
			}
        });
        
        /* FLIP ACTION*/
		findViewById(R.id.flip_button).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View view) {
				flipCard();
			}
		});
		
		/* SKIP ACTION */
		findViewById(R.id.skip_button).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				nextCard();
			}
		});
    	
        super.onCreate(savedInstanceState);
	}// E:onCreate

	@Override public void transformPage(View arg0, float arg1) {}
	
    @Override  public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.card, menu);
        return true;
    }
    
    @Override public boolean onPrepareOptionsMenu(Menu menu){
    	menu.findItem(R.id.card_menu_edit_current_card).setVisible(false);
    	menu.findItem(R.id.card_menu_remove_current_card).setVisible(false);
    	menu.findItem(R.id.card_menu_show_previous).setVisible(false);
    	menu.findItem(R.id.card_menu_shuffle).setVisible(false);
    	menu.findItem(R.id.card_menu_reset).setVisible(false);
    	if(this.myDeckAdapter.getDeckCount()>0){
        	menu.findItem(R.id.card_menu_edit_current_card).setVisible(true);
        	menu.findItem(R.id.card_menu_remove_current_card).setVisible(true);
        	menu.findItem(R.id.card_menu_reset).setVisible(true);
        	if(this.myDeckAdapter.getDeckCount()>1){
            	menu.findItem(R.id.card_menu_show_previous).setVisible(true);
            	menu.findItem(R.id.card_menu_shuffle).setVisible(true);
        	}
    	}
		menu.findItem(R.id.card_menu_view_not_answered).setVisible(false);
		menu.findItem(R.id.card_menu_view_wrong).setVisible(false);
		menu.findItem(R.id.card_menu_view_correct).setVisible(false);
		menu.findItem(R.id.card_menu_view_all).setVisible(false);
		
    	switch(myDeckAdapter.currentStack){
	    	case DeckAdapter.STACK_NOT_ANSWERED:
				if(myDeckAdapter.stackCounts[DeckAdapter.STACK_CORRECT]>0){
					menu.findItem(R.id.card_menu_view_correct).setVisible(true);
				}
				if(myDeckAdapter.stackCounts[DeckAdapter.STACK_WRONG] > 0){
					menu.findItem(R.id.card_menu_view_wrong).setVisible(true);
				}
				if(myDeckAdapter.getTotalCardCount() > myDeckAdapter.stackCounts[DeckAdapter.STACK_NOT_ANSWERED]){
					menu.findItem(R.id.card_menu_view_all).setVisible(true);
				}
				break;
			case DeckAdapter.STACK_WRONG:
				if(myDeckAdapter.stackCounts[DeckAdapter.STACK_CORRECT]>0){
					menu.findItem(R.id.card_menu_view_correct).setVisible(true);
				}
				if(myDeckAdapter.stackCounts[DeckAdapter.STACK_NOT_ANSWERED] > 0){
					menu.findItem(R.id.card_menu_view_not_answered).setVisible(true);
				}
				if(myDeckAdapter.getTotalCardCount() > myDeckAdapter.stackCounts[DeckAdapter.STACK_WRONG]){
					menu.findItem(R.id.card_menu_view_all).setVisible(true);
				}
				break;
    		case DeckAdapter.STACK_CORRECT:
    			if(myDeckAdapter.stackCounts[DeckAdapter.STACK_WRONG]>0){
    				menu.findItem(R.id.card_menu_view_wrong).setVisible(true);
    			}
    			if(myDeckAdapter.stackCounts[DeckAdapter.STACK_NOT_ANSWERED] > 0){
    				menu.findItem(R.id.card_menu_view_not_answered).setVisible(true);
    			}
    			if(myDeckAdapter.getTotalCardCount() > myDeckAdapter.stackCounts[DeckAdapter.STACK_CORRECT]){
    				menu.findItem(R.id.card_menu_view_all).setVisible(true);
    			}
				break;
    		case DeckAdapter.STACK_ALL:
    			if(myDeckAdapter.stackCounts[DeckAdapter.STACK_WRONG]>0){
    				menu.findItem(R.id.card_menu_view_wrong).setVisible(true);
    			}
    			if(myDeckAdapter.stackCounts[DeckAdapter.STACK_CORRECT]>0){
    				menu.findItem(R.id.card_menu_view_correct).setVisible(true);
    			}
    			if(myDeckAdapter.stackCounts[DeckAdapter.STACK_NOT_ANSWERED]>0){
    				menu.findItem(R.id.card_menu_view_not_answered).setVisible(true);
    			}
				break;
    	}
//		Log.d(TAG,"nowShowing:"+myDeckAdapter.currentStack);
//    	Log.d(TAG,"C/W/N: "+myDeckAdapter.stackCounts[DeckAdapter.STACK_CORRECT]+", "+myDeckAdapter.stackCounts[DeckAdapter.STACK_WRONG]+", "+myDeckAdapter.stackCounts[DeckAdapter.STACK_NOT_ANSWERED]);
		return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case android.R.id.home:
                Intent parentActivityIntent = new Intent(this, MainActivity.class);
                parentActivityIntent.addFlags(
                    Intent.FLAG_ACTIVITY_NO_ANIMATION|
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK
                );
                startActivity(parentActivityIntent);
                overridePendingTransition(0,0);
                finish();
            	break;
            case R.id.card_menu_add_new_card:
            	Intent createCardIntent = new Intent(DeckActivity.this,CardFormActivity.class);
            	createCardIntent.setFlags(
					Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP|
                    Intent.FLAG_ACTIVITY_NO_ANIMATION
                );
            	createCardIntent.putExtra(EXTRAS_DECK_ID, myDeckAdapter.getDeckId());
				createCardIntent.putExtra(EXTRAS_DECK_NAME,  myDeckAdapter.getDeckName());
				startActivity(createCardIntent);
            	break;
            case R.id.card_menu_edit_current_card:
            	Intent editCardIntent = new Intent(DeckActivity.this,CardFormActivity.class);
            	editCardIntent.setFlags(
					Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP|
                    Intent.FLAG_ACTIVITY_NO_ANIMATION
                );
            	editCardIntent.putExtra(EXTRAS_DECK_ID, myDeckAdapter.getDeckId());
            	editCardIntent.putExtra(EXTRAS_DECK_NAME,  myDeckAdapter.getDeckName());
            	editCardIntent.putExtra(CardFormActivity.EXTRAS_CARD_ID,  myDeckAdapter.getCurrentCard().id);
				startActivity(editCardIntent);
            	break;
            case R.id.card_menu_remove_current_card:
            	deleteCard().show();
            	break;
            case R.id.card_menu_shuffle:
            	myDeckAdapter.shuffleDeck();
            	this.nextCard();
            	break;
            case R.id.card_menu_show_previous:
            	this.previousCard();
            	break;
            case R.id.card_menu_view_not_answered:
            	myDeckAdapter.nowShowingNotAnswered();
            	QueryRunner getNotAnsweredCards = new QueryRunner(myDB);
            	getNotAnsweredCards.setQueryRunnerListener(new QueryRunnerListener(){
    				@Override public void onPostExcecute(Cursor cards) {
    					buildDeck(cards);
    				}
            	});
            	getNotAnsweredCards.execute(QueryString.getCardsWithDeckIdAndStatusQuery(myDeckAdapter.getDeckId(), Card.STATUS_NONE));
            	break;
            case R.id.card_menu_view_correct:
            	myDeckAdapter.nowShowingCorrect();
            	QueryRunner getCorrectCards = new QueryRunner(myDB);
            	getCorrectCards.setQueryRunnerListener(new QueryRunnerListener(){
    				@Override public void onPostExcecute(Cursor cards) {
    					buildDeck(cards);
    				}
            	});
            	getCorrectCards.execute(QueryString.getCardsWithDeckIdAndStatusQuery(myDeckAdapter.getDeckId(), Card.STATUS_CORRECT));
            	break;
            case R.id.card_menu_view_wrong:
            	myDeckAdapter.nowShowingWrong();
            	QueryRunner getWrongCards = new QueryRunner(myDB);
            	getWrongCards.setQueryRunnerListener(new QueryRunnerListener(){
    				@Override public void onPostExcecute(Cursor cards) {
    					buildDeck(cards);
    				}
            	});
            	getWrongCards.execute(QueryString.getCardsWithDeckIdAndStatusQuery(myDeckAdapter.getDeckId(), Card.STATUS_WRONG));
            	break;
            case R.id.card_menu_view_all:
            	changeToAllStack();
            	break;
            case R.id.card_menu_reset:
            	resetDeck().show();
            	break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }

	@Override public boolean onTouchEvent(MotionEvent event) {
		return gestureDetector.onTouchEvent(event);
	}
	
    private void changeToAllStack(){
    	myDeckAdapter.nowShowingAll();
    	QueryRunner getAllCards = new QueryRunner(myDB);
    	getAllCards.setQueryRunnerListener(new QueryRunnerListener(){
			@Override public void onPostExcecute(Cursor cards) {
				buildDeck(cards);
			}
    	});
    	getAllCards.execute(QueryString.getCardsWithDeckIdQuery(myDeckAdapter.getDeckId()));
    }
    
    private void nextCard(){
    	if(myDeckAdapter.getDeckCount()==0){
    		changeToAllStack();
    		if(myDeckAdapter.currentStack != DeckAdapter.STACK_ALL){
    			Toast.makeText(getApplicationContext(), "Current stack is empty. Now viewing all Cards", Toast.LENGTH_LONG).show();
    		}
		}else if(!animating){
			animating = true;
			View animatedCard;
			cardFront.setVisibility(View.VISIBLE);
			if(showingCardFront){
				Card myCard = myDeckAdapter.getCurrentCard();
				setViewForCard(myCard);
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
			
			myDeckAdapter.incrementIndex();
			Card myCard = myDeckAdapter.getCurrentCard();
			Log.d(TAG,"currentCard: "+myCard.toString());
			
			CharSequence newQuestion = myCard.question;
			int newStatus = myCard.getResourceStatusImage();
			
			((TextView) cardFront.findViewById(R.id.question_text)).setText(newQuestion);
			((ImageView) cardFront.findViewById(R.id.card_status)).setImageResource(newStatus);
			((TextView) cardFront.findViewById(R.id.card_id)).setText(myDeckAdapter.getCardPositionString());
			setGalleryForTableRow(cardFront.findViewById(R.id.card_front_gallery_row),myCard);
			

			Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.animator.slide_out_up);
			anim.setDuration(ANIMATION_DURATION);
			anim.setAnimationListener(new AnimationListener(){
				@Override public void onAnimationEnd(Animation animation) {
					CharSequence newAnswer = myDeckAdapter.getCurrentCard().answer;
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
    
    private void previousCard(){
    	if(myDeckAdapter.getDeckCount()==0){
    		changeToAllStack();
    		if(myDeckAdapter.currentStack != DeckAdapter.STACK_ALL){
    			Toast.makeText(getApplicationContext(), "Current stack is empty. Now viewing all Cards", Toast.LENGTH_LONG).show();
    		}
		}else if(!animating){
	    	animating = true;
			showingCardFront = true;
			myDeckAdapter.decrementIndex();
	    	
	    	final Card myCard = myDeckAdapter.getCardAtIndex(myDeckAdapter.stackIndex);
	    	final CharSequence prevQuestion = myCard.question;
	    	final int prevStatus = myCard.getResourceStatusImage();
	    	
			/*set up the visibility properly*/
			animatedCardFront.setVisibility(View.VISIBLE);
			
			/*make sure the order is correct to produce the stack effect...*/
			cardFront.bringToFront();
			cardBack.bringToFront();
			animatedCardFront.bringToFront();

			((TextView) animatedCardFront.findViewById(R.id.question_text)).setText(prevQuestion);
			((ImageView) animatedCardFront.findViewById(R.id.card_status)).setImageResource(prevStatus);
			((TextView) animatedCardFront.findViewById(R.id.card_id)).setText(myDeckAdapter.getCardPositionString());
	
			Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.animator.slide_in_down);
			anim.setDuration(ANIMATION_DURATION);
			anim.setAnimationListener(new AnimationListener(){
				@Override public void onAnimationEnd(Animation animation) {
					setViewForCard(myCard);
			    	animating = false;
				}
				@Override public void onAnimationRepeat(Animation animation) {}
				@Override public void onAnimationStart(Animation animation) {}
			});
			animatedCardFront.startAnimation(anim);
    	}
    }

    private void flipCard(){
		if(!animating && myDeckAdapter.getDeckCount()>0){
			animating = true;
			ViewFlipper rotation;
			animatedCardFront.setVisibility(View.GONE);
			if (showingCardFront){
				rotation = new ViewFlipper(cardFront, cardBack);
				rotation.setDirection(ViewFlipper.ROTATE_LEFT);
			} else {
				rotation = new ViewFlipper(cardBack, cardFront);
				rotation.setDirection(ViewFlipper.ROTATE_RIGHT);
			}
			rotation.setDuration(ANIMATION_DURATION);
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
    
    /**
     * @param cards
     * This method is used to set up the deck of cards from a cursor it receives;
     */
    private void buildDeck(Cursor cards){
    	Cursor result = cards;
    	myDeckAdapter.clear();
		if(result.getCount()>0){
	    	result.moveToPosition(-1);
			Log.d(TAG,"cardId	   photoId		photoFileName");
			while(result.moveToNext()){
				long cardId = result.getLong(0);
				String cardQuestion = result.getString(1);
				String cardAnswer = result.getString(2);
				Integer cardStatus = result.getInt(3);
				Integer cardPosition = result.getInt(4);
				
				Card myCard = myDeckAdapter.getCardWithId(cardId);
				if(myCard == null){
					myCard = new Card(cardId,cardQuestion,cardAnswer,cardStatus,cardPosition);
					myDeckAdapter.addCard(myCard);
				}
				if(result.getInt(5) != 0){
					Integer photoId = result.getInt(5);
					String photoFileName = result.getString(6);
					Integer photoOrderNum = result.getInt(7);
					Photo newPhoto = new Photo(photoId,photoFileName,photoOrderNum);
					myCard.photos.add(newPhoto);
					Log.d(TAG, Long.toString(cardId)+"	"+Long.toString(photoId)+"	"+photoFileName);
				}
			}
			setViewForCard(myDeckAdapter.getCurrentCard());
	    	
			if (!showingCardFront){
				flipCard();
			}
		}else{
			//No Cards to show :(
			cardFront.setVisibility(View.GONE);
			cardBack.setVisibility(View.GONE);
			animatedCardFront.setVisibility(View.GONE);
			actionsView.setVisibility(View.GONE);
		}
		
    }//E: gotCards
	
    private void setViewForCard(Card card){

		/*make sure the order is correct to produce the stack effect...*/
		cardFront.bringToFront();
		cardBack.bringToFront();
		animatedCardFront.bringToFront();
		
    	((ImageView) animatedCardFront.findViewById(R.id.card_status)).setImageResource(card.getResourceStatusImage());
    	((TextView) animatedCardFront.findViewById(R.id.card_id)).setText(myDeckAdapter.getCardPositionString());
		((TextView) animatedCardFront.findViewById(R.id.question_text)).setText(card.question);
		
		((ImageView) cardFront.findViewById(R.id.card_status)).setImageResource(card.getResourceStatusImage());
		((TextView) cardFront.findViewById(R.id.card_id)).setText(myDeckAdapter.getCardPositionString());
		((TextView) cardFront.findViewById(R.id.question_text)).setText(card.question);
		
		((TextView) cardBack.findViewById(R.id.answer_text)).setText(card.answer);

		
		//common callback
		cardFront.setVisibility(View.VISIBLE);
		cardBack.setVisibility(View.GONE);
		actionsView.setVisibility(View.VISIBLE);
		
		//Handle photo gallery 
		View tableRow = cardFront.findViewById(R.id.card_front_gallery_row);
		setGalleryForTableRow(tableRow, card);
		
    }
    
    private void setGalleryForTableRow(View tableRow,Card card) {
    	tableRow.setVisibility(View.GONE);
    	if(card.photos.size()>0){

    		tableRow.setVisibility(View.VISIBLE);
    		final ArrayList<Photo> galleryItems = card.photos;
    		final HorizontalListView gallery = (HorizontalListView) findViewById(R.id.photo_list_view);
    		gallery.setAdapter(new BaseAdapter(){
    			@Override public int getCount() { return galleryItems.size();}
    			@Override public Object getItem(int position) {return galleryItems.get(position);}
    			@Override public long getItemId(int position) {return galleryItems.get(position).id;}
    			@Override public View getView(int position, View convertView, ViewGroup parent) {
    				final Photo myPhoto = galleryItems.get(position);
    				View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.galley_photo_item,null);
    				ImageView myImage = (ImageView) layout.findViewById(R.id.galley_photo_item);
    				Log.d(TAG,"---"+Integer.toString(position)+"	"+Long.toString(myPhoto.id)+"	"+myPhoto.filename);
    				ImageLoader.getInstance().displayImage("file://"+myPhoto.filename, myImage,new ImageLoadingListener(){
    					@Override public void onLoadingStarted(String imageUri, View view) {}
    					@Override public void onLoadingFailed(String imageUri, View view, FailReason failReason) {}
    					@Override public void onLoadingCancelled(String imageUri, View view) {}
    					@Override public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
    						((ImageView)view).setImageBitmap(loadedImage);
    					}
    				});
    				return myImage;
    			}
    		});
    	}
	}

    /**
     * 
     * @return AlertDialog confirming the reset status of the entire deck of cards
     */
	private AlertDialog resetDeck() {
    	AlertDialog myDeleteConfirmationBox = new AlertDialog.Builder(this) 
           //set message, title, and icon
           .setTitle("Delete Card") 
           .setMessage("Are you sure you want to reset the answer status of all the cards in this deck?") 
           .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int whichButton) { 
                   	dialog.dismiss();
                   	QueryRunner resetAllCards = new QueryRunner(myDB);
                	resetAllCards.setQueryRunnerListener(new QueryRunnerListener(){
                		@Override public void onPostExcecute(Cursor cursor) {
                			changeToAllStack();
                		}
                	});
                	resetAllCards.execute(QueryString.getResetAllCardsInDeckStatusQuery(myDeckAdapter.getDeckId()));
               } 
           })
           .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int which) {
                   dialog.dismiss();
               }
           })
           .create();
       return myDeleteConfirmationBox;
	}


    /**
     * This method deletes the current deck's card that is being viewed.
     * @return AlertDialog confirming the deletion of a specific card
     */
	private AlertDialog deleteCard() {
    	AlertDialog myDeleteConfirmationBox = new AlertDialog.Builder(this) 
           //set message, title, and icon
           .setTitle("Delete Card") 
           .setMessage("Are you sure you want to delete this card?") 
           .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int whichButton) { 
                   	dialog.dismiss();
		           	QueryRunner deleteCardQuery = new QueryRunner(myDB);
		           	deleteCardQuery.setQueryRunnerListener(new QueryRunnerListener(){
		       			@Override public void onPostExcecute(Cursor cards) {
		       				buildDeck(cards);
		       				nextCard();
		       			}
		           	});
		           	String queryString = QueryString.getDeleteCardQuery(myDeckAdapter.getCurrentCard().id);
		           	deleteCardQuery.execute(queryString);
               } 
           })
           .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int which) {
                   dialog.dismiss();
               }
           })
           .create();
       return myDeleteConfirmationBox;
   }
}