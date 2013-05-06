package com.fyodorwolf.studybudy;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.fyodorwolf.studybudy.db.DatabaseAdapter;
import com.fyodorwolf.studybudy.db.QueryRunner;
import com.fyodorwolf.studybudy.db.QueryString;
import com.fyodorwolf.studybudy.db.QueryRunner.QueryRunnerListener;
import com.fyodorwolf.studybudy.models.*;

import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends ExpandableListActivity{

	public static final String TAG = "MainActivity";

	private boolean editing = false;
	private boolean deleting = false;
	private boolean searching = false;
	private EditText searchBox;
	private DatabaseAdapter myDB;
	private ExpandableListView listView;
	
	private ArrayList<Section> _sections = new ArrayList<Section>();
	private HashMap<Long,Section> _sectionIdMap = new HashMap<Long,Section>();
	private ExpandableListAdapter _listViewAdapter;
    		
    @Override protected void onCreate(Bundle savedInstanceState) {
        // define main views.
    	Log.d(TAG,"started");
		setContentView(R.layout.list_view);
	    getActionBar().setDisplayHomeAsUpEnabled(false);
	    
        //define the local views to be used
    	searchBox = (EditText) this.findViewById(R.id.edit_text);
	    searchBox.getBackground().setAlpha(95);
    	listView = this.getExpandableListView();
		myDB = DatabaseAdapter.getInstance();

		/* SHOW ALL SECTIONS AND DECKS */
    	preformNormalSearch();
    	
    	/* SET ACTION LISTENERS */
    	searchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
    	    @Override
    	    public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
    	        if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        event.getAction() == KeyEvent.ACTION_DOWN &&
                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
    	        	String searchString = textView.getText().toString();
    	        	if(searchString.isEmpty()){
    	        		searching = false;
    	            	preformNormalSearch();
    	        	}else{
    	        		searching = true;
                    	QueryRunner sectionsQuery = new QueryRunner(myDB);
                        sectionsQuery.setQueryRunnerListener(new QueryRunnerListener(){
                			@Override public void onPostExcecute(Cursor cards) {
                				gotSections(cards);
                            	listView.requestFocus();
                            	InputMethodManager in = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            	in.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
                			}
                		});
                        sectionsQuery.execute(QueryString.getSearchTermQuery(searchString));
    	        	}
                    return true;
    	        }
    	        return false;
    	    }
    	});
    	
		listView.setOnChildClickListener(new OnChildClickListener(){
			@Override public boolean onChildClick(ExpandableListView parent, View child, int groupIdx, int childIdx, long deckId) {
				if(editing){
	            	runCreateDeckActivity(deckId);
				}
				else if(deleting){
					int position = parent.getPositionForView(child);
					boolean curCheckState = ((CheckedTextView)child).isChecked();
					listView.setItemChecked(position, !curCheckState);
				}else{
					Intent deckIntent = new Intent(MainActivity.this,DeckActivity.class);
					deckIntent.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP|
	                            		Intent.FLAG_ACTIVITY_NO_ANIMATION);
					deckIntent.putExtra(DeckActivity.EXTRAS_DECK_ID, deckId);
					deckIntent.putExtra(DeckActivity.EXTRAS_DECK_NAME, ((TextView)child.findViewById(android.R.id.text1)).getText());
					Deck clickedDeck = _sections.get(groupIdx).getDeckById(deckId);
					if(searching && clickedDeck.cards.size()>0){
						//build an array of card id's to show in the next activity...
						long[] cardIds = new long[clickedDeck.cards.size()];
						int cardIdIdx = 0;
						for(Card searchCard : clickedDeck.cards){
							cardIds[cardIdIdx] = searchCard.id;
							cardIdIdx++;
						}
						deckIntent.putExtra(DeckActivity.EXTRAS_CARD_IDS, cardIds);
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
        sectionsQuery.execute(QueryString.getGroupedDeckQuery());
	}

	@Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.section, menu);
        return true;
    }

    @Override public boolean onPrepareOptionsMenu(Menu menu){
    	if(editing){
    		menu.findItem(R.id.main_menu_edit_deck).setVisible(false);
    		menu.findItem(R.id.main_menu_edit_list).setVisible(false);
    		menu.findItem(R.id.main_menu_delete).setVisible(false);
    		menu.findItem(R.id.main_menu_cancel_edit).setVisible(true);
    	}
    	else if(deleting){
    		menu.findItem(R.id.main_menu_edit_deck).setVisible(false);
    		menu.findItem(R.id.main_menu_edit_list).setVisible(false);
    		menu.findItem(R.id.main_menu_delete).setVisible(true);
    		menu.findItem(R.id.main_menu_cancel_edit).setVisible(true);
    	}else{
    		menu.findItem(R.id.main_menu_edit_deck).setVisible(true);
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
            	runCreateDeckActivity(0);
            	break;
            case R.id.main_menu_edit_deck:
            	editing = true;
            	break;
            case R.id.main_menu_edit_list:
            	deleting = true;
            	listView.setChoiceMode(ExpandableListView.CHOICE_MODE_MULTIPLE);
                listView.setItemsCanFocus(false);
                listView.setAdapter(_listViewAdapter);
                expandListView();
                break;
            case R.id.main_menu_delete:
            	if(listView.getCheckedItemCount()>0){
            		deleteDecks().show();
            	}else{
                	doneEditing();
            	}
                break; 	
            case R.id.main_menu_cancel_edit:
            	doneEditing();
                break;
        }
    	return true;
    }
    
    private void runCreateDeckActivity(long deckId) {
    	Intent createDeckIntent = new Intent(MainActivity.this,DeckFormActivity.class);
    	createDeckIntent.setFlags(
			Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP|
            Intent.FLAG_ACTIVITY_NO_ANIMATION
        );
    	String[] sectionNames = new String[_sections.size()];
    	long[] sectionIds = new long[_sections.size()];
    	for(int idx = 0; idx < _sections.size(); idx++){
    		sectionNames[idx] = _sections.get(idx).name;
    		sectionIds[idx] = _sections.get(idx).id;
    	}
    	createDeckIntent.putExtra(DeckFormActivity.EXTRAS_SECTION_IDS, sectionIds);
    	createDeckIntent.putExtra(DeckFormActivity.EXTRAS_SECTION_NAMES, sectionNames);
    	if(deckId > 0){
    		createDeckIntent.putExtra(DeckFormActivity.EXTRAS_EDITING_DECK_ID, deckId);
    	}
    	startActivity(createDeckIntent);
	}

	@Override public void onStart(){
    	listView.requestFocus();
        super.onStart();
    }
    
    private void doneEditing(){
    	deleting = false;
    	editing = false;
        this.listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        this.listView.setItemsCanFocus(true);
        listView.setAdapter(_listViewAdapter);
        expandListView();
    }

    protected void gotSections(Cursor result) {
    	_sections = new ArrayList<Section>();
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
					_sections.add(section);
				}
				Deck myDeck = new Deck(deckId,deckName);
				section.addDeck(myDeck);//addDeck won't allow repeats..
				//cursor row may have additional column when searching with matching card ids...
				if(result.getColumnCount() > 4){
					long cardId = result.getLong(4);
					Log.d(TAG,Long.toString(sectionId)+','+sectionName+','+Long.toString(deckId)+','+deckName+','+Long.toString(cardId));
					section.getDeckById(deckId).cards.add(new Card(cardId));
				}
			}
		}
		
		_listViewAdapter = new ExpandableListAdapter(){
			@Override public Object getGroup(int groupPosition) {return _sections.get(groupPosition);	}
			@Override public Object getChild(int groupPosition, int childPosition) {return _sections.get(groupPosition).decks.get(childPosition);}
			@Override public int getChildrenCount(int groupPosition) {	return _sections.get(groupPosition).decks.size();}
			@Override public int getGroupCount() { return _sections.size();}
			@Override public long getChildId(int groupPosition, int childPosition) {return _sections.get(groupPosition).decks.get(childPosition).id;}
			@Override public long getGroupId(int groupPosition) { return _sections.get(groupPosition).id;}
			@Override public long getCombinedChildId(long groupId, long childId) {return childId;}
			@Override public long getCombinedGroupId(long groupId) { return groupId;}
			@Override public boolean areAllItemsEnabled() { return true; }
			@Override public boolean hasStableIds() {return true;}
			@Override public boolean isChildSelectable(int groupPosition, int childPosition) { return true;}
			@Override public boolean isEmpty() {return _sections.isEmpty();}
			
			@Override public void onGroupCollapsed(int groupPosition) {}
			@Override public void onGroupExpanded(int groupPosition) {}
			@Override public void registerDataSetObserver(DataSetObserver observer) {
				
			}
			@Override public void unregisterDataSetObserver(DataSetObserver observer) {}
			
			@Override
			public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
				LayoutInflater myInflator = LayoutInflater.from(getApplicationContext());
				TextView item;
				if(deleting){
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
		listView.setAdapter(_listViewAdapter);
		int count = listView.getExpandableListAdapter().getGroupCount();
		for (int position = 0; position < count; position++)
		    listView.expandGroup(position);
    }
    

    
    private AlertDialog deleteDecks() {
    	AlertDialog myDeleteConfirmationBox = new AlertDialog.Builder(this) 
    	//set message, title, and icon
    		.setTitle("Delete Card") 
    		.setMessage("Are you sure you want to delete these decks?") 
    		.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int whichButton) { 
    				long[] deckIds = listView.getCheckedItemIds();
    				
    				/*DELETE THE PHOTOS, CARDS AND FILES OF THIS DECK*/
    				MainActivity.deleteAssociationsForDeckIds(MainActivity.this,deckIds);
    				
    				/*DELETE ACTUAL DECK*/
    				new QueryRunner(myDB, new QueryRunnerListener(){
    					@Override public void onPostExcecute(Cursor cursor) {
    						QueryRunner deleteEmptySections = new QueryRunner(myDB);
    						deleteEmptySections.setQueryRunnerListener(new QueryRunnerListener(){
    							@Override public void onPostExcecute(Cursor cursor) {
    								listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    								listView.setItemsCanFocus(true);
									doneEditing();
									preformNormalSearch();
    							}
    						});
    						deleteEmptySections.execute(QueryString.getRemoveEmptySectionsQuery());
						}
   					}).execute(QueryString.getRemoveDecksWithIdsQuery(deckIds));
    			} 
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					doneEditing();
				}
			})
			.create();
    	return myDeleteConfirmationBox;
   }

	protected static void deleteAssociationsForDeckIds(Context context, long[] deckIds) {
		final DatabaseAdapter myDB = DatabaseAdapter.getInstance();
		final Context myContext = context;
		//get all deck photo files.
	   	new QueryRunner(myDB, new QueryRunnerListener(){
			@Override public void onPostExcecute(Cursor cursor) {
				if(cursor.getCount()>0){
					int size = cursor.getCount();
					long[] cardIds = new long[size];
					String[] photoFilenames = new String[size];
					HashSet<String> photoPathsInDB = new HashSet<String>();
					cursor.moveToPosition(-1);
					while(cursor.moveToNext()){
						String filename = cursor.getString(1);
						long cardId = cursor.getLong(2);
						cardIds[cursor.getPosition()] = cardId;
						photoFilenames[cursor.getPosition()] = filename;
						photoPathsInDB.add(filename);
					}
					
					/*DELETE PHOTO IN PHOTOIDS[]*/
					new QueryRunner(myDB)
						.execute(QueryString.getDeletePhotosWithFilenamesQuery(photoFilenames));
					
					/*DELETE CARD IN CARDIDS[]*/
					new QueryRunner(myDB).execute(QueryString.getDeleteCardsQuery(cardIds));

					/*DELETE FILES THAT DON'T HAVE DB ENTRIES*/
					unlinkedFiles(myContext, photoPathsInDB);
				}
				
			}
	   	}).execute(QueryString.getCardsWithPhotosForDecksQuery(deckIds));
	}
    
	public static void unlinkedFiles(Context context, HashSet<String> filePaths){
		String appDir = context.getFilesDir()+"/";
		File[] files =  new File(appDir).listFiles();
		for(File file : files){
			String existingFilePath = file.getAbsolutePath();
			if(!filePaths.contains(existingFilePath)){
				file.delete();
			}
		}
	}
}
