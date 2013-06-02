package com.fyodorwolf.studybudy;

import java.util.ArrayList;
import java.util.HashMap;

import com.fyodorwolf.studybudy.db.DatabaseAdapter;
import com.fyodorwolf.studybudy.db.QueryRunner;
import com.fyodorwolf.studybudy.db.QueryString;
import com.fyodorwolf.studybudy.db.QueryRunner.QueryRunnerListener;
import com.fyodorwolf.studybudy.models.Section;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TableRow;
import android.widget.TextView;

public class DeckFormActivity extends Activity{

	public static final String TAG = "CreateDeckActivity";
	public static final String EXTRAS_EDITING_DECK_ID = "com.fyodorwolf.studybudy.deckId";
    public static final String EXTRAS_SECTION_IDS = "com.fyodorwolf.studyBudy.sectionIds"; 
    public static final String EXTRAS_SECTION_NAMES = "com.fyodorwolf.studyBudy.sectionNames";

	private static final int NEW_SECTION_ID = -1;
	
	private Spinner groupSelect;
	private EditText groupInput; 
	private EditText deckInput;
	ArrayList<Section> sections = new ArrayList<Section>();
	HashMap<Long,Integer> sectionIdToIndex = new HashMap<Long,Integer>();
	DatabaseAdapter myDb;
	TableRow groupNameRow;
	
	@Override protected void onCreate(Bundle savedInstanceState){
		setContentView(R.layout.deck_form);
	    getActionBar().setDisplayHomeAsUpEnabled(true);
		setTitle("Create New Deck");
		
		myDb = DatabaseAdapter.getInstance();
		deckInput = (EditText) this.findViewById(R.id.deck_name_input);
		groupSelect = (Spinner) this.findViewById(R.id.deck_group_select);
		groupInput = (EditText) this.findViewById(R.id.deck_group_input);
		groupNameRow = (TableRow) this.findViewById(R.id.group_name_row);
		
		groupNameRow.setVisibility(View.GONE);
		
		long[] sectionIds = getIntent().getExtras().getLongArray(EXTRAS_SECTION_IDS);
		CharSequence[] sectionNames = getIntent().getExtras().getCharSequenceArray(EXTRAS_SECTION_NAMES);
		for(int idx = 0; idx<sectionIds.length; idx++){
			long sectionId = sectionIds[idx];
			String sectionName = (String) sectionNames[idx];
			Section section = new Section(sectionId, sectionName);
			sectionIdToIndex.put(section.id, sections.size());
			sections.add(section);
		}
		sections.add(new Section(NEW_SECTION_ID, "Create New Section"));
		
		groupSelect.setAdapter(new SpinnerAdapter(){
			@Override public int getCount() {return sections.size();}
			@Override public Object getItem(int position) {return sections.get(position);}
			@Override public long getItemId(int position) {return sections.get(position).id;}
			@Override public int getItemViewType(int position) {return 0;}
			@Override public View getView(int position, View convertView, ViewGroup parent) {
				TextView text = new TextView(getApplicationContext());
		        text.setTextColor(Color.BLACK);
		        text.setPadding(7, 7, 7, 7);
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
		
		final long deckId = getIntent().getLongExtra(EXTRAS_EDITING_DECK_ID, 0);

		if(deckId > 0){
			setTitle("Edit Deck");
			((Button)this.findViewById(R.id.create_deck)).setText("Edit Deck");
			QueryRunner myQuery = new QueryRunner(myDb);
			myQuery.setQueryRunnerListener(new QueryRunnerListener(){
				@Override public void onPostExcecute(Cursor cursor) {
					String deckName = cursor.getString(1);
					long sectionId = cursor.getLong(2);
					int position  = sectionIdToIndex.get(sectionId);
					deckInput.setText(deckName);
					groupSelect.setSelection(position);
				}
			});
			myQuery.execute(QueryString.getDeckQuery(deckId));
			
		}
		this.findViewById(R.id.create_deck).setOnClickListener(new OnClickListener(){
			@Override public void onClick(View v){
				final String deckName = deckInput.getText().toString();
				if(deckName.length()>0){
					if(groupSelect.getSelectedItemId() == NEW_SECTION_ID){
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
											if(deckId > 0){
												String deckName = deckInput.getText().toString();
												updateDeck(deckId,deckName,sectionId);
											}else{
												createGroupWithNameAndSectionId(deckName, sectionId);
											}
										}
									});
									getSectionId.execute(QueryString.getLastSectionIdQuery());
									
								}
							});
							createSection.execute(QueryString.getCreateSectionQuery(groupName));
						}
					}else if(deckId > 0){
						long sectionId = groupSelect.getSelectedItemId();
						updateDeck(deckId,deckName,sectionId);
					}else{	
						int selectedIndex = groupSelect.getSelectedItemPosition();
						long sectionId = ((SpinnerAdapter)groupSelect.getAdapter()).getItemId(selectedIndex);
						createGroupWithNameAndSectionId(deckName, sectionId);
					}
				}
			}
		});
        super.onCreate(savedInstanceState);
	}

	public void updateDeck(long deckId, String deckName, long sectionId){
		QueryRunner createGroup = new QueryRunner(myDb);
		createGroup.setQueryRunnerListener(new QueryRunnerListener(){
			@Override public void onPostExcecute(Cursor cards) {
				backToParentActivity();
			}
		});
		createGroup.execute(QueryString.getUpdateDeckQuery(deckId,deckName,sectionId));
	}
	
	public void createGroupWithNameAndSectionId(String name, long sectionId){
		QueryRunner createGroup = new QueryRunner(myDb);
		createGroup.setQueryRunnerListener(new QueryRunnerListener(){
			@Override public void onPostExcecute(Cursor cards) {
				backToParentActivity();
			}
		});
		createGroup.execute(QueryString.getCreateDeckQuery(name,sectionId));
	
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
        Intent parentActivityIntent = new Intent(this, DecksActivity.class);
        parentActivityIntent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(parentActivityIntent);
        overridePendingTransition(0,0);
        finish();
	}
	
}
