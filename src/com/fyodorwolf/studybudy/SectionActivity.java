package com.fyodorwolf.studybudy;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class SectionActivity extends ListActivity {
	
	private static final String TAG = "ListActivity";
	DatabaseAdapter mDbHelper;
	SimpleCursorAdapter myAdapter;
    
	
	@Override
	protected void onCreate(Bundle savedInstanceState){

	    getActionBar().setDisplayHomeAsUpEnabled(true);
	    
		long sectionId =  getIntent().getExtras().getLong("com.example.studyBudy.SectionId");
		Log.d(TAG,"SectionID: "+String.valueOf(sectionId));
        setTitle("Some Section");
        
//        String query = mDbHelper.decksWithSectionIdQuery(sectionId);	
        
//        ListView myList = this.getListView();
//        myList.setAdapter(new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1,null,null,null,CursorAdapter.NO_SELECTION));

        super.onCreate(savedInstanceState);
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
}
