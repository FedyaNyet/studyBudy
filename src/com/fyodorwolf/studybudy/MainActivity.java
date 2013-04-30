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
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

public class MainActivity extends ExpandableListActivity{

	
	EditText searchBox;
	ExpandableListView listView;
	private DatabaseAdapter myDB;
	private static final String TAG = "MainActivity";
	private boolean editing = false;
	ExpandableListAdapter myListViewAdapter;
	
	ArrayList<Section> _myExpListData = new ArrayList<Section>();
	HashMap<Long,Section> _sectionIdMap = new HashMap<Long,Section>();
	HashMap<Long,Integer> _deckPosition = new HashMap<Long,Integer>();
	protected boolean searching = false;
	
      
    @Override protected void onCreate(Bundle savedInstanceState) {
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
				if(editing){
					int position = parent.getPositionForView(child);
					boolean curCheckState = ((CheckedTextView)child).isChecked();
					listView.setItemChecked(position, !curCheckState);
				}else{
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
				}
				return true;
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
            	listView.setChoiceMode(ExpandableListView.CHOICE_MODE_MULTIPLE);
                listView.setItemsCanFocus(false);
                listView.setAdapter(myListViewAdapter);
                expandListView();
                break;
            case R.id.main_menu_delete:	
            	editing = false;
            	//get checked items...
            	//run query to delete checked decks..
                this.listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                this.listView.setItemsCanFocus(true);
                listView.setAdapter(myListViewAdapter);
                expandListView();
                break; 	
            case R.id.main_menu_cancel_edit:
            	editing = false;
                this.listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                this.listView.setItemsCanFocus(true);
                listView.setAdapter(myListViewAdapter);
                expandListView();
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
					_sectionIdMap.put(sectionId, section);
					_myExpListData.add(section);
				}
				Deck myDeck = new Deck(deckId,deckName);
				section.addDeck(myDeck);//addDeck won't allow repeats..
				_deckPosition.put(myDeck.id, _deckPosition.size());
				//cursor row may have additional column when searching with matching card ids...
				if(result.getColumnCount() > 4){
					long cardId = result.getLong(4);
					Log.d(TAG,Long.toString(sectionId)+','+sectionName+','+Long.toString(deckId)+','+deckName+','+Long.toString(cardId));
					section.getDeckById(deckId).cards.add(new Card(cardId));
				}
			}
		}
		
		myListViewAdapter = new ExpandableListAdapter(){
			@Override public Object getGroup(int groupPosition) {return _myExpListData.get(groupPosition);	}
			@Override public Object getChild(int groupPosition, int childPosition) {return _myExpListData.get(groupPosition).decks.get(childPosition);}
			@Override public int getChildrenCount(int groupPosition) {	return _myExpListData.get(groupPosition).decks.size();}
			@Override public int getGroupCount() { return _myExpListData.size();}
			@Override public long getChildId(int groupPosition, int childPosition) {return _myExpListData.get(groupPosition).decks.get(childPosition).id;}
			@Override public long getGroupId(int groupPosition) { return _myExpListData.get(groupPosition).id;}
			@Override public long getCombinedChildId(long groupId, long childId) {return childId;}
			@Override public long getCombinedGroupId(long groupId) { return groupId;}
			@Override public boolean areAllItemsEnabled() { return true; }
			@Override public boolean hasStableIds() {return true;}
			@Override public boolean isChildSelectable(int groupPosition, int childPosition) { return true;}
			@Override public boolean isEmpty() {return _myExpListData.isEmpty();}
			
			@Override public void onGroupCollapsed(int groupPosition) {}
			@Override public void onGroupExpanded(int groupPosition) {}
			@Override public void registerDataSetObserver(DataSetObserver observer) {}
			@Override public void unregisterDataSetObserver(DataSetObserver observer) {}
			
			@Override
			public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
				LayoutInflater myInflator = LayoutInflater.from(getApplicationContext());
				TextView item;
				if(editing){
					item = (TextView) myInflator.inflate(android.R.layout.simple_list_item_multiple_choice,null);
				}else{
					item = (TextView) myInflator.inflate(android.R.layout.simple_list_item_activated_1, null);
				}
				item.setMinHeight(96);
				item.setText(((Deck)getChild(groupPosition,childPosition)).name);
				item.setTextColor(Color.BLACK);
				item.setBackgroundColor(Color.WHITE);
//				item.getBackground().setAlpha(95);
				return item;
			}
			
			@Override
			public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
				Section sec = (Section) getGroup(groupPosition);
				LayoutInflater myInflator = LayoutInflater.from(getApplicationContext());
				TextView item = (TextView)myInflator.inflate(android.R.layout.simple_expandable_list_item_1,null);
				item.setBackgroundColor(Color.GRAY);
				item.setText(sec.name);
				item.setTextColor(Color.WHITE);
				return item;
			}
		};
		expandListView();
	}
    
    public void expandListView(){
		listView.setAdapter(myListViewAdapter);
		int count = listView.getExpandableListAdapter().getGroupCount();
		for (int position = 0; position < count; position++)
		    listView.expandGroup(position);
    }
}
