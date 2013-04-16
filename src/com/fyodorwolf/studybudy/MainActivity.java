package com.fyodorwolf.studybudy;

import java.util.ArrayList;
import java.util.HashMap;

import com.fyodorwolf.studybudy.models.*;

import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;

public class MainActivity extends ExpandableListActivity{

	EditText searchBox;
	ExpandableListView listView;
	private DatabaseAdapter myDB;
	private static final String TAG = "MainActivity";
	
      
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // define main views.
		setContentView(R.layout.list_view);
	    getActionBar().setDisplayHomeAsUpEnabled(false);
	    
        //define the local views to be used
    	searchBox = (EditText) this.findViewById(R.id.edit_text);
	    searchBox.getBackground().setAlpha(95);
    	listView = this.getExpandableListView();
        
    	/*
    	 *Add local view listeners 
    	****************************/
    	searchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
    	    @Override
    	    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
    	        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                	Log.d(TAG,"Search: "+v.getText().toString());
                	listView.requestFocus();
                	InputMethodManager in = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                	in.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
    	            return true;
    	        }
    	        return false;
    	    }
    	});

    	listView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position,long id) {
				Log.d(TAG,"selected Item"); 
			}
        });
		listView.setOnChildClickListener(new OnChildClickListener(){

			@Override
			public boolean onChildClick(ExpandableListView parent, View child, int groupIdx, int childIdx, long deckId) {
				Intent deckIntent = new Intent(MainActivity.this,DeckActivity.class);
				deckIntent.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP|
                            Intent.FLAG_ACTIVITY_NO_ANIMATION);
				deckIntent.putExtra("com.example.studyBudy.deckId", deckId);
				deckIntent.putExtra("com.example.studyBudy.deckName", ((TextView)child.findViewById(android.R.id.text1)).getText());
		        startActivity(deckIntent);
				return false;
			}
		});
    	myDB = DatabaseAdapter.getInstance(this);
    	
        //ask another thread to get the sections and display them. 
        SectionGetter sectionGetter = new SectionGetter();
        sectionGetter.execute(DatabaseAdapter.getGroupedDeckQuery());
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public void onStart(){
    	listView.requestFocus();
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    
/********************************************************************************************************************************************
 * 							Private Classes		 																							*
 ********************************************************************************************************************************************/
	private class SectionGetter extends AsyncTask<String, Integer, Cursor> {

		private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
		final ArrayList<Section> expListData = new ArrayList<Section>();
		HashMap<Long,Section> sections = new HashMap<Long,Section>();
		
		@Override
		protected void onPreExecute() {}

		@Override
		protected Cursor doInBackground(String... params) {
			return myDB.getCursor(params[0]);
		}
		
		private void handleCursor(Cursor result){
			Long sectionId = result.getLong(0);
			String sectionName = result.getString(1);
			Long deckId = result.getLong(2);
			String deckName = result.getString(3);
			//build the dataStores
			Section section = sections.get(sectionId);
			if(section == null){
				section = new Section(sectionId, sectionName);
				expListData.add(section);
				sections.put(sectionId, section);
			}
			section.decks.add(new Deck(deckId,deckName));
		}
		
		@Override
		protected void onPostExecute(final Cursor result) {
			
			if(result.moveToFirst()){
				handleCursor(result);
				while(result.moveToNext()){
					handleCursor(result);
				}
			}
			
			listView.setAdapter(new ExpandableListAdapter(){

				@Override
				public boolean areAllItemsEnabled() {
					return true;
				}

				@Override
				public Object getChild(int groupPosition, int childPosition) {
					return expListData.get(groupPosition).decks.get(childPosition);
				}

				@Override
				public long getChildId(int groupPosition, int childPosition) {
					return expListData.get(groupPosition).decks.get(childPosition).id;
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
					return expListData.get(groupPosition).decks.size();
				}

				@Override
				public long getCombinedChildId(long groupId, long childId) {
					return childId;
				}

				@Override
				public long getCombinedGroupId(long groupId) {
					return groupId;
				}

				@Override
				public Object getGroup(int groupPosition) {
					return expListData.get(groupPosition);
				}

				@Override
				public int getGroupCount() {
					return expListData.size();
				}

				@Override
				public long getGroupId(int groupPosition) {
					return expListData.get(groupPosition).id;
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

				@Override
				public boolean hasStableIds() {
					return true;
				}

				@Override
				public boolean isChildSelectable(int groupPosition, int childPosition) {
					return true;
				}

				@Override
				public boolean isEmpty() {
					return false;
				}

				@Override
				public void onGroupCollapsed(int groupPosition) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onGroupExpanded(int groupPosition) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void registerDataSetObserver(DataSetObserver observer) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void unregisterDataSetObserver(DataSetObserver observer) {
					// TODO Auto-generated method stub
					
				}
			});
			this.dialog.hide();
		}
	}
}
