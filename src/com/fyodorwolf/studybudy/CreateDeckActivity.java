package com.fyodorwolf.studybudy;

import java.util.ArrayList;

import com.fyodorwolf.studybudy.helpers.DatabaseAdapter;
import com.fyodorwolf.studybudy.helpers.QueryRunner;
import com.fyodorwolf.studybudy.helpers.QueryRunner.QueryRunnerListener;
import com.fyodorwolf.studybudy.models.Section;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TableRow;
import android.widget.TextView;

public class CreateDeckActivity extends Activity{

	private static final int NEW_SECTION_ID = -1;
	private static final String TAG = "CreateDeckActivity";
	private EditText deckInput;
	private EditText groupInput; 
	private Spinner groupSelect;
	ArrayList<Section> sections = new ArrayList<Section>();
	DatabaseAdapter myDb;
	TableRow groupNameRow;
	
	@Override protected void onCreate(Bundle savedInstanceState){
		setContentView(R.layout.create_deck);
	    getActionBar().setDisplayHomeAsUpEnabled(true);
		setTitle("Create New Deck");
		
		myDb = DatabaseAdapter.getInstance(getApplicationContext());
		deckInput = (EditText) this.findViewById(R.id.deck_name_input);
		groupSelect = (Spinner) this.findViewById(R.id.deck_group_select);
		groupInput = (EditText) this.findViewById(R.id.deck_group_input);
		groupNameRow = (TableRow) this.findViewById(R.id.group_name_row);
		
		groupNameRow.setVisibility(View.GONE);
		
		long[] sectionIds = getIntent().getExtras().getLongArray("com.fyodorwolf.studyBudy.sectionIds");
		CharSequence[] sectionNames = getIntent().getExtras().getCharSequenceArray("com.fyodorwolf.studyBudy.sectionNames");
		for(int idx = 0; idx<sectionIds.length; idx++){
			long sectionId = sectionIds[idx];
			String sectionName = (String) sectionNames[idx];
			Section section = new Section(sectionId, sectionName);
			sections.add(section);
		}
		sections.add(new Section(NEW_SECTION_ID, "Create New Seciton"));
		
		groupSelect.setAdapter(new SpinnerAdapter(){
			@Override public int getCount() {return sections.size();}
			@Override public Object getItem(int position) {return sections.get(position);}
			@Override public long getItemId(int position) {return sections.get(position).id;}
			@Override public int getItemViewType(int position) {return 0;}
			@Override public View getView(int position, View convertView, ViewGroup parent) {
				TextView text = new TextView(getApplicationContext());
		        text.setTextColor(Color.BLACK);
		        text.setText(sections.get(position).name);
		        return text;
			}
			@Override public int getViewTypeCount() {return 0;}
			@Override public boolean hasStableIds() {return true;}
			@Override public boolean isEmpty() {return sections.isEmpty();}
			@Override public void registerDataSetObserver(DataSetObserver observer) {}
			@Override public void unregisterDataSetObserver(DataSetObserver observer) {}
			@Override public View getDropDownView(int position, View convertView,ViewGroup parent) {
				return this.getView(position, convertView, parent);
			}
		});
		groupSelect.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override public void onItemSelected(AdapterView<?> parent, View view, int index, long id) {
				if(id == NEW_SECTION_ID){
					groupNameRow.setVisibility(View.VISIBLE);
				}else{
					groupNameRow.setVisibility(View.GONE);
				}
			}
			@Override public void onNothingSelected(AdapterView<?> parent) {}
		});

		this.findViewById(R.id.create_deck).setOnClickListener(new OnClickListener(){
			@Override public void onClick(View v){
				final String deckText = deckInput.getText().toString();
				boolean lastSectionIsSelected = (groupSelect.getSelectedItemId() == NEW_SECTION_ID);
				if(deckText.length()>0){
					if(lastSectionIsSelected){
						final String groupName = groupInput.getText().toString();
						if(groupName.length()>0){
							QueryRunner createSection = new QueryRunner(myDb);
							createSection.setQueryRunnerListener(new QueryRunnerListener(){
								@Override public void onPostExcecute(Cursor cur) {
									QueryRunner getSectionId = new QueryRunner(myDb);
									getSectionId.setQueryRunnerListener(new QueryRunnerListener(){
										@Override public void onPostExcecute(Cursor cur) {
											cur.moveToFirst();
											long sectionId = cur.getLong(0);
											createGroupWithNameAndSectionId(deckText, sectionId);
										}
									});
									getSectionId.execute(DatabaseAdapter.getSectionByNameQuery(groupName));
									
								}
							});
							createSection.execute(DatabaseAdapter.getCreateSectionQuery(groupName));
						}
					}else{
						int selectedIndex = groupSelect.getSelectedItemPosition();
						long sectionId = ((SpinnerAdapter)groupSelect.getAdapter()).getItemId(selectedIndex);
						Log.d(TAG, deckText+","+sectionId);
						createGroupWithNameAndSectionId(deckText, sectionId);
					}
				}
			}
		});
        super.onCreate(savedInstanceState);
	}

	public void createGroupWithNameAndSectionId(String name, long sectionId){
		QueryRunner createGroup = new QueryRunner(myDb);
		createGroup.setQueryRunnerListener(new QueryRunnerListener(){
			@Override public void onPostExcecute(Cursor cards) {
				backToParentActivity();
			}
		});
		createGroup.execute(DatabaseAdapter.getCreateDeckQuery(name,sectionId));
	
	}
	
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
            	backToParentActivity();
            	break;
        }
		return true;
    }
	private void backToParentActivity() {
        Intent parentActivityIntent = new Intent(this, MainActivity.class);
        parentActivityIntent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(parentActivityIntent);
        overridePendingTransition(0,0);
        finish();
	}
	
}
