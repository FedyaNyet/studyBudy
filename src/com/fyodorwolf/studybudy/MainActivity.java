package com.fyodorwolf.studybudy;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
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
	private DatabaseAdapter myDB;
	private static final String TAG = "MainActivity";
	
      
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // define main views.
		setContentView(R.layout.list_view);
		
        //define the local views to be used
    	searchBox = (EditText) this.findViewById(R.id.edit_text);
    	listView = (ListView)this.getListView();
        
    	/*
    	 *Add local view listeners 
    	****************************/
        searchBox.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub
				
			}
			@Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//				_myAdapter.getFilter().filter(s);
            	Log.d(TAG,"Search: "+s.toString());
            }
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
			}
        });

    	listView.requestFocus();
    	listView.setAdapter(new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1,null,null,null,CursorAdapter.NO_SELECTION));
        listView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position,long id) {
				Intent it = new Intent(MainActivity.this,SectionActivity.class);
				it.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP|
                            Intent.FLAG_ACTIVITY_NO_ANIMATION);
				it.putExtra("com.example.studyBudy.SectionId", id);
		        startActivity(it); 
			}
        });


    	myDB = DatabaseAdapter.getInstance(this);
    	
        //ask another thread to get the sections and display them. 
        SectionGetter sectionGetter = new SectionGetter();
        sectionGetter.execute(DatabaseAdapter.allSectionsQuery());
        super.onCreate(savedInstanceState);
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

		@Override
		protected void onPreExecute() {}

		@Override
		protected Cursor doInBackground(String... params) {
			return myDB.getCursor(params[0]);
		}
		
		@Override
		protected void onPostExecute(final Cursor result) {
	    	SimpleCursorAdapter adp = (SimpleCursorAdapter) listView.getAdapter();
	    	adp.changeCursorAndColumns(result, new String[]{"name"},new int[]{android.R.id.text1});
			this.dialog.hide();
		}
	}
	
	
}
