package com.fyodorwolf.studybudy;

import java.util.List;

import android.app.ActionBar;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class MainActivity extends ListActivity{

	EditText searchBox;
	ListView listView;
	
	private static final String TAG = "MainActivity";
	
      
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	
    	/************************************************************
    	 * The following is simply to migrate the database 
    	 * file from assets to the application database director.
    	 ************************************************************/

        // define main views.
		setContentView(R.layout.list_view);
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.DISPLAY_HOME_AS_UP);
		
        //define the local views to be used
    	searchBox = (EditText) this.findViewById(R.id.edit_text);
    	listView = (ListView)this.getListView();

    	/*
    	 *Add local view listeners 
    	****************************/
        searchBox.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
			}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//				_myAdapter.getFilter().filter(s);
            	Log.d(TAG,"Search: "+s.toString());
            }
        });
        
    	listView.setAdapter(new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1,null,null,null,CursorAdapter.NO_SELECTION));
        listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position,long id) {
				Log.d(TAG,"Clicked "+id);
			}
        });
        SelectDataTask sectionGetter = new SelectDataTask();
        //ask another thread to get the data and 
        sectionGetter.execute(DatabaseAdapter.allSectionsQuery());
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    /*****************************
     * 		Private Classes		 *
     *****************************/
	private class SelectDataTask extends AsyncTask<String, Integer, Cursor> {
		
		private final String TAG = "SelectDataTask";
		private final DatabaseAdapter myDB = DatabaseAdapter.getInstance(MainActivity.this);
		private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);

		// can use UI thread here
		protected void onPreExecute() {
			this.dialog.setMessage("Selecting data...");
			this.dialog.show();
		}

		@Override
		protected Cursor doInBackground(String... params) {
			return myDB.getCursor(params[0]);
		}
		// can use UI thread here
		protected void onPostExecute(final Cursor result) {
			Log.d(TAG,result.toString());
			result.moveToFirst();
			while(result.getPosition() != result.getCount()){
				Log.d(TAG,"name"+result.getPosition()+": "+result.getString(1));
				result.moveToNext();
			}
			result.moveToFirst();
	    	SimpleCursorAdapter adp = (SimpleCursorAdapter) listView.getAdapter();
	    	adp.changeCursorAndColumns(result, new String[]{"name"},new int[]{android.R.id.text1});
			this.dialog.hide();
		}
	}
	
	
}
