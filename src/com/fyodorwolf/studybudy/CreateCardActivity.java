package com.fyodorwolf.studybudy;

import com.fyodorwolf.studybudy.helpers.DatabaseAdapter;
import com.fyodorwolf.studybudy.helpers.QueryRunner;
import com.fyodorwolf.studybudy.helpers.QueryRunner.QueryRunnerListener;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class CreateCardActivity extends Activity {

	long deckId;
	String deckName;
	private static final String TAG = "CreateCardActivity";
	private CreateCardActivity thisActivity;
	
	private static final int IMAGE_REQUEST_CODE = 1;

	@Override protected void onCreate(Bundle savedInstanceState){
		setContentView(R.layout.create_card);
	    getActionBar().setDisplayHomeAsUpEnabled(true);
		deckId =  getIntent().getExtras().getLong("com.fyodorwolf.studyBudy.deckId");
		deckName =  getIntent().getExtras().getString("com.fyodorwolf.studyBudy.deckName");
		setTitle("Add New Card to "+deckName);
		final TextView question = (TextView) this.findViewById(R.id.question_input);
		final TextView answer = (TextView) this.findViewById(R.id.answer_input);
		
		this.findViewById(R.id.create_card).setOnClickListener(new OnClickListener(){
			@Override public void onClick(View v){
				String question_text = question.getText().toString();
				String answer_text = answer.getText().toString();
				Log.d(TAG,"q:"+question_text+" a:"+answer_text);
				if(question_text.length()>0 && answer_text.length()>0){
					QueryRunner createCard = new QueryRunner(DatabaseAdapter.getInstance());
					createCard.setQueryRunnerListener(new QueryRunnerListener(){
						@Override public void onPostExcecute(Cursor cards) {
							backToParentActivity();
						}
					});
					createCard.execute(DatabaseAdapter.getCreateNewCardQuery(question_text,answer_text,deckId));
				}
			}
		});
		this.findViewById(R.id.add_images).setOnClickListener(new OnClickListener(){
			@Override public void onClick(View v) {
				Intent multiSelect = new Intent(thisActivity, MultiPhotoSelectActivity.class);
				startActivityForResult(multiSelect,IMAGE_REQUEST_CODE);
			}
		});
		thisActivity = this;
        super.onCreate(savedInstanceState);
	}
	
	@Override public void onActivityResult(int requestCode, int resultCode, Intent data) {     
		super.onActivityResult(requestCode, resultCode, data); 
		switch(requestCode) { 
		  	case (IMAGE_REQUEST_CODE) :
				if (resultCode == Activity.RESULT_OK) { 
					String[] newText = data.getStringArrayExtra("com.fyodorwolf.studyBudy.imageStrings");
					Log.d(TAG,"received: "+newText);
				} 
				break; 
	  	} 
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
        Intent parentActivityIntent = new Intent(this, DeckActivity.class);
        parentActivityIntent.putExtra("com.fyodorwolf.studyBudy.deckId", deckId);
        parentActivityIntent.putExtra("com.fyodorwolf.studyBudy.deckName",  deckName);
        parentActivityIntent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(parentActivityIntent);
        overridePendingTransition(0,0);
        finish();
	}
}
