package com.fyodorwolf.studybudy;

import java.util.ArrayList;
import java.util.HashMap;

import com.fyodorwolf.studybudy.helpers.DatabaseAdapter;
import com.fyodorwolf.studybudy.helpers.QueryRunner;
import com.fyodorwolf.studybudy.helpers.QueryRunner.QueryRunnerListener;
import com.fyodorwolf.studybudy.models.*;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends ExpandableListActivity{

	EditText searchBox;
	ExpandableListView listView;
	private DatabaseAdapter myDB;
	private static final String TAG = "MainActivity";
	private boolean editing = false;
	
	ArrayList<Section> _myExpListData = new ArrayList<Section>();
	HashMap<Long,Section> _sectionIdMap = new HashMap<Long,Section>();
	protected boolean searching = false;
	
      
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // define main views.
    	Log.d(TAG,"started");
		setContentView(R.layout.list_view);
	    getActionBar().setDisplayHomeAsUpEnabled(false);
	    
        //define the local views to be used
    	searchBox = (EditText) this.findViewById(R.id.edit_text);
	    searchBox.getBackground().setAlpha(95);
    	listView = this.getExpandableListView();
		myDB = DatabaseAdapter.getInstance(this);

		/* SHOW ALL SECTIONS AND DECKS */
    	preformNormalSearch();
    	
    	/* SET ACTION LISTENERS */
    	searchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
    	    @Override
    	    public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
    	        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
    	        	String searchString = textView.getText().toString();
    	        	if(searchString.isEmpty()){
    	        		searching = false;
    	            	preformNormalSearch();
    	        	}else{
    	        		searching = true;
    	        		Log.d(TAG,"Search: "+searchString);
                    	QueryRunner sectionsQuery = new QueryRunner(myDB);
                        sectionsQuery.setQueryRunnerListener(new QueryRunnerListener(){
                			@Override public void onPostExcecute(Cursor cards) {
                				gotSections(cards);
                            	listView.requestFocus();
                            	InputMethodManager in = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            	in.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
                			}
                		});
                        sectionsQuery.execute(DatabaseAdapter.getSearchTermQuery(searchString));
    	        	}
                    return true;
    	        }
    	        return false;
    	    }
    	});
    	
		listView.setOnChildClickListener(new OnChildClickListener(){

			@Override
			public boolean onChildClick(ExpandableListView parent, View child, int groupIdx, int childIdx, long deckId) {
				Intent deckIntent = new Intent(MainActivity.this,DeckActivity.class);
				deckIntent.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP|
                            Intent.FLAG_ACTIVITY_NO_ANIMATION);
				deckIntent.putExtra("com.fyodorwolf.studyBudy.deckId", deckId);
				deckIntent.putExtra("com.fyodorwolf.studyBudy.deckName", ((TextView)child.findViewById(android.R.id.text1)).getText());
				Deck clickedDeck = _myExpListData.get(groupIdx).getDeckById(deckId);
				if(searching && clickedDeck.cards.size()>0){
					//build an array of card id's to show in the next activity...
					long[] cardIds = new long[clickedDeck.cards.size()];
					int cardIdIdx = 0;
					for(Card searchCard : clickedDeck.cards){
						cardIds[cardIdIdx] = searchCard.id;
						cardIdIdx++;
					}
					deckIntent.putExtra("com.fyodorwolf.studyBudy.cardIds", cardIds);
				}
				startActivity(deckIntent);
				return false;
			}
		});
        
        super.onCreate(savedInstanceState);
    }
    
    private void preformNormalSearch() {  
       	/* RUN QUERY ON ANOTHER THREAD */
    	QueryRunner sectionsQuery = new QueryRunner(myDB);
        sectionsQuery.setQueryRunnerListener(new QueryRunnerListener(){
			@Override public void onPostExcecute(Cursor cards) {
				gotSections(cards);
			}
		});
        sectionsQuery.execute(DatabaseAdapter.getGroupedDeckQuery());
		
	}

	@Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.section, menu);
        return true;
    }

    @Override public boolean onPrepareOptionsMenu(Menu menu){
    	if(editing){
    		menu.findItem(R.id.main_menu_edit_list).setVisible(false);
    		menu.findItem(R.id.main_menu_delete).setVisible(true);
    		menu.findItem(R.id.main_menu_cancel_edit).setVisible(true);
    	}else{
    		menu.findItem(R.id.main_menu_edit_list).setVisible(true);
    		menu.findItem(R.id.main_menu_delete).setVisible(false);
    		menu.findItem(R.id.main_menu_cancel_edit).setVisible(false);
    	}
    	return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.main_menu_new_deck:
            	Intent createDeckIntent = new Intent(MainActivity.this,CreateDeckActivity.class);
            	createDeckIntent.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP|
                        Intent.FLAG_ACTIVITY_NO_ANIMATION);
            	String[] sectionNames = new String[_myExpListData.size()];
            	long[] sectionIds = new long[_myExpListData.size()];
            	for(int idx = 0; idx < _myExpListData.size(); idx++){
            		sectionNames[idx] = _myExpListData.get(idx).name;
            		sectionIds[idx] = _myExpListData.get(idx).id;
            	}
            	createDeckIntent.putExtra("com.fyodorwolf.studyBudy.sectionNames", sectionNames);
            	createDeckIntent.putExtra("com.fyodorwolf.studyBudy.sectionIds", sectionIds);
            	startActivity(createDeckIntent);
            	break;
            case R.id.main_menu_edit_list:
            	editing = true;
                this.listView.setItemsCanFocus(false);
                this.listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                listView.invalidate();
                break;
            case R.id.main_menu_delete:
            	editing = false;
                this.listView.setItemsCanFocus(true);
                this.listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                ExpandableListAdapter myAdapter = (ExpandableListAdapter)listView.getAdapter();
                myAdapter.getChildView(0, 0, false, convertView, listView);
                listView.invalidate();
                break;
            case R.id.main_menu_cancel_edit:
            	editing = false;
                this.listView.setItemsCanFocus(true);
                this.listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                listView.invalidate();
                break;
        }
    	return true;
    }
    @Override public void onStart(){
    	listView.requestFocus();
        super.onStart();
    }

    protected void gotSections(Cursor result) {
    	_myExpListData = new ArrayList<Section>();
    	_sectionIdMap = new HashMap<Long,Section>();
    	
		if(result.getCount()>0){
	    	result.moveToPosition(-1);
			while(result.moveToNext()){
				Long sectionId = result.getLong(0);
				String sectionName = result.getString(1);
				Long deckId = result.getLong(2);
				String deckName = result.getString(3);
				//build the dataStores
				Section section = _sectionIdMap.get(sectionId);
				if(section == null){
					section = new Section(sectionId, sectionName);
					_myExpListData.add(section);
					_sectionIdMap.put(sectionId, section);
				}
				section.addDeck(new Deck(deckId,deckName));//won't allow repeats..
				//cursor row may have additional column when searching with matching card ids...
				if(result.getColumnCount() > 4){
					long cardId = result.getLong(4);
					Log.d(TAG,Long.toString(sectionId)+','+sectionName+','+Long.toString(deckId)+','+deckName+','+Long.toString(cardId));
					section.getDeckById(deckId).cards.add(new Card(cardId));
				}
			}
		}
		
		listView.setAdapter(new ExpandableListAdapter(){
			@Override
			public Object getChild(int groupPosition, int childPosition) {
				return _myExpListData.get(groupPosition).decks.get(childPosition);
			}

			@Override
			public long getChildId(int groupPosition, int childPosition) {
				return _myExpListData.get(groupPosition).decks.get(childPosition).id;
			}

			@Override
			public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
				View item = LayoutInflater.from(getApplicationContext()).inflate(android.R.layout.simple_list_item_activated_1, null);
				TextView tv = (TextView) item.findViewById(android.R.id.text1);
				Deck deck = (Deck) getChild(groupPosition,childPosition);
				tv.setText(deck.name);
				tv.setTextColor(Color.BLACK);
				tv.setBackgroundColor(Color.WHITE);
				tv.getBackground().setAlpha(95);
				return item;
			}

			@Override
			public int getChildrenCount(int groupPosition) {
				return _myExpListData.get(groupPosition).decks.size();
			}

			@Override
			public Object getGroup(int groupPosition) {
				return _myExpListData.get(groupPosition);
			}

			@Override
			public int getGroupCount() {
				return _myExpListData.size();
			}

			@Override
			public long getGroupId(int groupPosition) {
				return _myExpListData.get(groupPosition).id;
			}

			@Override
			public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
				View item = LayoutInflater.from(getApplicationContext()).inflate(android.R.layout.simple_expandable_list_item_1,null);
				item.setBackgroundColor(Color.GRAY);
				TextView tv = (TextView) item.findViewById(android.R.id.text1);
				Section sec = (Section) getGroup(groupPosition);
				tv.setText(sec.name);
				tv.setTextColor(Color.WHITE);
				return item;
			}

			@Override public long getCombinedChildId(long groupId, long childId) {return childId;}
			@Override public long getCombinedGroupId(long groupId) { return groupId;}
			@Override public boolean areAllItemsEnabled() { return true; }
			@Override public boolean hasStableIds() {return true;}
			@Override public boolean isChildSelectable(int groupPosition, int childPosition) { return true;}
			@Override public boolean isEmpty() {return false;}

			@Override public void onGroupCollapsed(int groupPosition) {}
			@Override public void onGroupExpanded(int groupPosition) {}
			@Override public void registerDataSetObserver(DataSetObserver observer) {}
			@Override public void unregisterDataSetObserver(DataSetObserver observer) {}
		});
		int count = listView.getExpandableListAdapter().getGroupCount();
		for (int position = 0; position < count; position++)
		    listView.expandGroup(position);
	}
}
