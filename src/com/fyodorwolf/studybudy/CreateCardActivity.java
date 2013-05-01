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
import com.nostra13.universalimageloader.core.ImageLoader;

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
				if(question_text.length()>0 && answer_text.length()>0){
					final String[] absPaths = new String[imageFiles.size()];
					int absPathIdx = 0; 
					if(imageFiles.size()>0){
						//MOVE ALL THE PHOTO FILES TO APP DIRECTORY
						for(File imageFile: imageFiles){
							String newImagePath = getApplicationContext().getFilesDir()+imageFile.getName();
							File newImageFile = new File(newImagePath);
							Log.d(TAG, "filename"+newImagePath);
							try{
								copy(imageFile,newImageFile);
								absPaths[absPathIdx++] = newImagePath;
								Log.d(TAG,"absPath:"+absPaths[absPathIdx-1]);
							}catch(Exception e){
								Log.e(TAG, "unable to copy files");
							}
						}
					}
					QueryRunner createCard = new QueryRunner(DatabaseAdapter.getInstance());
					createCard.setQueryRunnerListener(new QueryRunnerListener(){
						@Override public void onPostExcecute(Cursor cards) {
							if(absPaths.length>0){
								//ADD PHOTOS TO DATABASE
								QueryRunner getCardId = new QueryRunner(DatabaseAdapter.getInstance());
								getCardId.setQueryRunnerListener(new QueryRunnerListener(){
									@Override public void onPostExcecute(Cursor cursor) {
										long cardId = cursor.getLong(0);
										QueryRunner addImagesToCard = new QueryRunner(DatabaseAdapter.getInstance());
										addImagesToCard.setQueryRunnerListener(new QueryRunnerListener(){
											@Override public void onPostExcecute(Cursor cursor) {
												backToParentActivity();
											}
										});
										addImagesToCard.execute(DatabaseAdapter.getCreatePhotoQuery(absPaths,cardId));
									}
									
								});
								getCardId.execute(DatabaseAdapter.getLastCardIdQuery());
							}else{
								backToParentActivity();
							}
						}
					});
					createCard.execute(DatabaseAdapter.getCreateCardQuery(question_text,answer_text,deckId));
				}
			}
		});
		findViewById(R.id.add_images).setOnClickListener(new OnClickListener(){
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
		if (!dst.getParentFile().exists() && !dst.getParentFile().mkdirs()){
			Log.d(TAG,"Unable to create:" + dst.getParentFile());
		}
        InputStream mInput = new FileInputStream(src);
        OutputStream mOutput = new FileOutputStream(dst);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer))>0)
        {
            mOutput.write(mBuffer, 0, mLength);
        }
        mOutput.flush();
        mOutput.close();
        mInput.close();
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
