package com.fyodorwolf.studybudy;

import android.app.ListActivity;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

public class SectionActivity extends ListActivity {
	

	DatabaseAdapter mDbHelper;
	SimpleCursorAdapter myAdapter;
    
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		
        Bundle bundle = getIntent().getExtras();
        long sectionId =  bundle.getParcelable("com.example.studyBudy.SectionId");

//        Cursor c = mDbHelper.getDecksWithSectionId(sectionId);	
        
        ListView myList = this.getListView();
        myList.setAdapter(new ListAdapter(){

			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public Object getItem(int position) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public long getItemId(int position) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int getItemViewType(int position) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public int getViewTypeCount() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public boolean hasStableIds() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isEmpty() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void registerDataSetObserver(DataSetObserver observer) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void unregisterDataSetObserver(DataSetObserver observer) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public boolean areAllItemsEnabled() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isEnabled(int position) {
				// TODO Auto-generated method stub
				return false;
			}});
        
	}
	
}
