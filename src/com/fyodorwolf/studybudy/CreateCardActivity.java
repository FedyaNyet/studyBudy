package com.fyodorwolf.studybudy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.fyodorwolf.studybudy.helpers.DatabaseAdapter;
import com.fyodorwolf.studybudy.helpers.QueryRunner;
import com.fyodorwolf.studybudy.helpers.QueryRunner.QueryRunnerListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CreateCardActivity extends Activity {

	long deckId;
	String deckName;
	private static final String TAG = "CreateCardActivity";
	private CreateCardActivity thisActivity;
	
	ArrayList<File> imageFiles = new ArrayList<File>();
	String[] imagePaths;
	
	private static final int IMAGE_REQUEST_CODE = 1;

	@Override protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
		setContentView(R.layout.create_card);
	    getActionBar().setDisplayHomeAsUpEnabled(true);
		thisActivity = this;
		deckId =  getIntent().getExtras().getLong("com.fyodorwolf.studyBudy.deckId");
		deckName =  getIntent().getExtras().getString("com.fyodorwolf.studyBudy.deckName");
		setTitle("Add New Card to "+deckName);
		final TextView question = (TextView) this.findViewById(R.id.question_input);
		final TextView answer = (TextView) this.findViewById(R.id.answer_input);
		hideImageGallary();
		
		findViewById(R.id.create_card).setOnClickListener(new OnClickListener(){
			@Override public void onClick(View v){
				String question_text = question.getText().toString();
				String answer_text = answer.getText().toString();
				Log.d(TAG,"q:"+question_text+" a:"+answer_text);
				if(question_text.length()>0 && answer_text.length()>0){
					if(imageFiles.size()>0){
						for(File imageFile: imageFiles){
							String newImagePath = getApplicationContext().getFilesDir().getPath()+"/images/"+imageFile.getName();
							File newImageFile = new File(newImagePath);
							try{
								copy(imageFile,newImageFile);
							}catch(Exception e){
								Log.d(TAG, "unable to copy files");
							}
						}
					}
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
	}
	
	@Override public void onActivityResult(int requestCode, int resultCode, Intent data) {   
		if(data != null && resultCode == Activity.RESULT_OK){
			switch(requestCode) { 
			  	case (IMAGE_REQUEST_CODE) :
					imagePaths = data.getStringArrayExtra("com.fyodorwolf.studyBudy.imageStrings");
					imageFiles.clear();
					hideImageGallary();
					if(imagePaths.length>0){
						for(String imagePath : imagePaths){
							imageFiles.add(new File(imagePath));
						}
						showImageGallary();
					}
					break; 
		  	} 
		}
		super.onActivityResult(requestCode, resultCode, data); 
	}
	
    private void hideImageGallary() {
    	this.findViewById(R.id.create_card_gallary_row).setVisibility(View.GONE);
		((Button)this.findViewById(R.id.add_images)).setText("Add Images");
    }

	private void showImageGallary() {
		View tableRow = this.findViewById(R.id.create_card_gallary_row);
		tableRow.setVisibility(View.VISIBLE);
		LinearLayout gallary =  (LinearLayout) tableRow.findViewById(R.id.create_card_gallary);
		((Button)this.findViewById(R.id.add_images)).setText("Change Images");
		gallary.removeAllViews();
		for(final File imageFile : imageFiles){
			View imageLayout =  getLayoutInflater().inflate(R.layout.row_multiphoto_item, null);
			imageLayout.findViewById(R.id.checkBox1).setVisibility(View.GONE);
			ImageView myImage;
			myImage = (ImageView)imageLayout.findViewById(R.id.imageView1);
			myImage.setScaleType(ScaleType.FIT_END);
			myImage.setVisibility(View.VISIBLE);
			myImage.setOnClickListener(new OnClickListener(){
				@Override public void onClick(View imageView) {
					Intent intent = new Intent();
					intent.setAction(android.content.Intent.ACTION_VIEW); 
					intent.setDataAndType(Uri.fromFile(imageFile),"image/*");
					startActivity(intent);
				}
			});
			String path = Uri.fromFile(imageFile).toString();
			ImageLoader.getInstance().displayImage(path, myImage);
			gallary.addView(imageLayout);
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
    
    public void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
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
