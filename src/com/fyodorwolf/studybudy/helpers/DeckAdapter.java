package com.fyodorwolf.studybudy.helpers;

import java.util.Collections;
import java.util.HashMap;

import com.fyodorwolf.studybudy.models.Card;
import com.fyodorwolf.studybudy.models.Deck;

/**
 * This class extends the basic functionality the deck model to 
 * allow keep track of the status of the cards it holds, in addition
 * to which cards are currently being displayed and practiced. 
 * 
 * @author fwolf
 *
 */
public class DeckAdapter{
	
	public static final int STACK_NOT_ANSWERED = Card.STATUS_NONE;//0
	public static final int STACK_CORRECT = Card.STATUS_CORRECT;//1
	public static final int STACK_WRONG = Card.STATUS_WRONG;//3
	public static final int STACK_ALL = 3;
	
	public int currentStack;
	private HashMap<Long,Integer> workingStackCardIdToIndexMap;
	private Deck workingStack;
	
	private HashMap<Long,Integer> allStackCardIdToIndexMap;
	private Deck allStack;
	
	public int[] stackCounts = {0,0,0,0};
	public int stackIndex;
	
	
	public DeckAdapter(Deck myDeck) {
		currentStack = STACK_ALL;
		workingStackCardIdToIndexMap = new HashMap<Long,Integer>();
		workingStack = new Deck(myDeck.id, myDeck.name);
		stackIndex = 0;
		allStackCardIdToIndexMap = new HashMap<Long,Integer>();
		allStack = myDeck;
	}
	
	public int getDeckCount() {
		return workingStack.cards.size();
	}

	public int getTotalCardCount(){
		return allStack.cards.size();
	}
	
	public void nowShowingAll() {
    	currentStack = STACK_ALL;
	}

	public void nowShowingNotAnswered() {
    	currentStack = STACK_NOT_ANSWERED;
	}

	public void nowShowingWrong() {
    	currentStack = STACK_WRONG;
	}

	public void nowShowingCorrect() {
    	currentStack = STACK_CORRECT;
	}

	public int decrementIndex() {
		return stackIndex = ((stackIndex + getWorkingStackSize()) - 1) % getWorkingStackSize();
	}
	
	public int incrementIndex(){
		return stackIndex =  (stackIndex+1) % getWorkingStackSize();
	}

	public long getDeckId(){
		return workingStack.id;
	}

	public boolean setCardStatus(long cardId, int status){
		Card card = getCardWithId(cardId);
		stackCounts[card.status]--;
		stackCounts[status]++;
		card.status = status;
		if(currentStack != card.status && currentStack != STACK_ALL){
			workingStack.cards.remove(stackIndex);
		}
		return true;
	}
	
	public void clear(){
		allStack.cards.clear();
		allStackCardIdToIndexMap.clear();
		workingStack.cards.clear();
		workingStackCardIdToIndexMap.clear();
		stackIndex = 0;
		for(int idx=0; idx<stackCounts.length;idx++){
			stackCounts[idx] = 0;
		}
	}
	
	public void addCard(Card card){
		if(allStackCardIdToIndexMap.get(card.id) == null){
			//add new card
			allStackCardIdToIndexMap.put(card.id,allStack.cards.size());
			allStack.cards.add(card);
			workingStackCardIdToIndexMap.put(card.id,workingStack.cards.size());
			workingStack.cards.add(card);
			stackCounts[card.status]++;
			stackCounts[STACK_ALL]++;
		}
	}
	
	public int getWorkingStackSize(){
		return workingStack.cards.size();
	}
	
	public Card getCurrentCard(){
		return workingStack.cards.get(stackIndex);
	}
	
	public Card getCardAtIndex(int index){
		return workingStack.cards.get(index);
	}
	
	public long getIdOfCardAtIndex(int index){
		return workingStack.getCards().get(index).id;
	}
	
	public void shuffleDeck(){
		Collections.shuffle(workingStack.cards);
	}
	
	public Card getCardWithId(long _id){
		Integer idx = allStackCardIdToIndexMap.get(_id);
		if(idx != null){
			return allStack.cards.get(idx);
		}
		return null;
	}
	
	public Deck getAllDeck(){
		return allStack;
	}

	public String getCardPositionString(){
		return Integer.toString(stackIndex+1)+"/"+Integer.toString(getWorkingStackSize());
	}

	public String getDeckName() {
		return this.workingStack.name;
	}
	
}