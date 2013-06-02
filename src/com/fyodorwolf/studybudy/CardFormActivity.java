package com.fyodorwolf.studybudy;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import com.fyodorwolf.studybudy.db.DatabaseAdapter;
import com.fyodorwolf.studybudy.db.QueryRunner;
import com.fyodorwolf.studybudy.db.QueryString;
import com.fyodorwolf.studybudy.db.QueryRunner.QueryRunnerListener;
import com.fyodorwolf.studybudy.models.Photo;
import com.fyodorwolf.studybudy.ui.HorizontalListView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class CardFormActivity extends Activity {

	
	public static final int ACTIVITY_SELECT_IMAGE = 7777;
	public static final String TAG = "CardFormActivity";
	public static final String EXTRAS_CARD_ID = "com.fyodorwolf.studyBudy.cardId";

	private static final int IMAGE_REQUEST_CODE = 1;
	
	private ArrayList<File> imageFiles = new ArrayList<File>();
	private String[] imagePaths = new String[0];
	
	private long cardId;
	private long deckId;
	private String deckName;
	private DatabaseAdapter myDb;

	@Override protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
		myDb = DatabaseAdapter.getInstance();
		setContentView(R.layout.create_card);
	    getActionBar().setDisplayHomeAsUpEnabled(true);
	    
		deckId =  getIntent().getExtras().getLong("com.fyodorwolf.studyBudy.deckId");
		deckName =  getIntent().getExtras().getString("com.fyodorwolf.studyBudy.deckName");
		cardId = getIntent().getLongExtra(EXTRAS_CARD_ID, 0);
		
		setTitle("Add New Card to "+deckName);
		if(imagePaths.length<1){
			hideImageGallery();
		}

		final TextView question = (TextView) this.findViewById(R.id.question_input);
		final TextView answer = (TextView) this.findViewById(R.id.answer_input);
		hideImageGallery();
		if(cardId > 0){
			setTitle("Edit Card in "+deckName);
			new QueryRunner(myDb, new QueryRunnerListener(){
				@Override public void onPostExcecute(Cursor cursor) {
					cursor.moveToFirst();
					String questionText = cursor.getString(0);
					String answerText = cursor.getString(1);
					question.setText(questionText);
					answer.setText(answerText);
					cursor.moveToPosition(-1);
					while(cursor.moveToNext()){
						String filename = cursor.getString(3);
						if(filename != null){
							imageFiles.add(new File(filename));
						}
					}
					if(imageFiles.size()>0){
						showImageGallery();
					}
				}
			}).execute(QueryString.getCardWithPhotosQuery(cardId));
		}
		findViewById(R.id.create_card).setOnClickListener(new OnClickListener(){
			@Override public void onClick(View v){
				String question_text = question.getText().toString();
				String answer_text = answer.getText().toString();
				if(question_text.length()>0 && answer_text.length()>0){
					if(cardId == 0){
						//ITS A NEW CARD
						new QueryRunner(myDb,new QueryRunnerListener(){
							@Override public void onPostExcecute(Cursor cards){
								String[] absPaths = copyImageFiles();
								if(absPaths.length>0){
									new QueryRunner(myDb).execute(QueryString.getCreatePhotoForLatestCardQuery(absPaths));
								}
								backToParentActivity();
							}
						}).execute(QueryString.getCreateCardQuery(question_text,answer_text,deckId));					
					}else{
						//UPDATE EXISTING CARD
						new QueryRunner(myDb).execute(QueryString.getUpdateCardQuery(cardId, question_text, answer_text));
						//UPDATE CARD'S PHOTOS
						new QueryRunner(myDb, new QueryRunnerListener(){
							@Override public void onPostExcecute(Cursor cursor) {
								final String[] existingPhotoFiles = new String[cursor.getCount()];
								if(cursor.getCount() > 0){
									//remove all traces of existing card photos
									cursor.moveToPosition(-1);
									while(cursor.moveToNext()){
										String filename = cursor.getString(3);
										existingPhotoFiles[cursor.getPosition()] = filename;
										imageFiles.add(new File(filename));
									}
									new QueryRunner(myDb)
										.execute(QueryString.getDeletePhotosWithFilenamesQuery(existingPhotoFiles));
								}
								String[] absPaths = copyImageFiles();
								HashSet<String> filePaths = new HashSet<String>(Arrays.asList(existingPhotoFiles));
								SBApplication.removeFiles(filePaths);
								if(absPaths.length>0){
									new QueryRunner(myDb).execute(QueryString.getCreatePhotoForLatestCardQuery(absPaths));
								}
								backToParentActivity();
							}
						}).execute(QueryString.getCardWithPhotosQuery(cardId));
					}
	
				}
			}

			/*MOVE ALL THE PHOTO FILES TO APP DIRECTORY
			 ******************************************/
			private String[] copyImageFiles() {
				String[] absPaths = new String[imageFiles.size()];
				if(imageFiles.size()>0){
					int absPathIdx = 0; 
					String newFilePath = getApplicationContext().getFilesDir()+"/";
					for(File imageFile: imageFiles){
						String imageFileName = imageFile.getName();
						String imageFileExt = imageFileName.substring(imageFileName.lastIndexOf("."));
						String newFileName = Long.toString(System.currentTimeMillis())+imageFileExt;
						File newImageFile = new File(newFilePath+newFileName);
						Log.d(TAG, newImageFile.getAbsolutePath());
						try{
							SBApplication.copy(imageFile,newImageFile);
							absPaths[absPathIdx++] = newImageFile.getAbsolutePath();
						}catch(Exception e){
							Log.e(TAG, "unable to copy files");
						}
					}
				}
				return absPaths;
			}
		});
		findViewById(R.id.add_images).setOnClickListener(new OnClickListener(){
			@Override public void onClick(View v) {
				Intent multiSelect = new Intent(CardFormActivity.this, MultiPhotoSelectActivity.class);
				startActivityForResult(multiSelect,IMAGE_REQUEST_CODE);
//				Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//				startActivityForResult(i, IMAGE_REQUEST_CODE); 
			}
		});
	}
	
	@Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(data != null && resultCode == Activity.RESULT_OK){
			switch(requestCode) { 
			  	case (IMAGE_REQUEST_CODE) :
					imagePaths = data.getStringArrayExtra("com.fyodorwolf.studyBudy.imageStrings");
					imageFiles.clear();
					hideImageGallery();
					if(imagePaths.length>0){
						for(String imagePath : imagePaths){
							imageFiles.add(new File(imagePath));
						}
						showImageGallery();
					}
////SELECT SINGLE PHOTO
//		            Uri selectedImage = data.getData();
//		            String[] filePathColumn = {MediaStore.Images.Media.DATA};
//		            Cursor cursor = getContentResolver().query(
//		                               selectedImage, filePathColumn, null, null, null);
//		            cursor.moveToFirst();
//		            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//		            String filePath = cursor.getString(columnIndex);
//		            cursor.close();
//		            Bitmap yourSelectedImage = BitmapFactory.decodeFile(filePath);
//		            Log.d(TAG,filePath);
					break; 
		  	} 
		}
		super.onActivityResult(requestCode, resultCode, data); 
	}
	
    @Override public void onConfigurationChanged(Configuration newConfig){
    	Log.d(TAG,imagePaths.toString());
    	if(imagePaths.length>0){
			showImageGallery();
		}
    }
    

	@Override  public boolean onCreateOptionsMenu(Menu menu) {
    	if(cardId>0){
	        getMenuInflater().inflate(R.menu.card_form, menu);
	        return true;
    	}return false;
    }
	
	@Override public boolean onPrepareOptionsMenu(Menu menu){
    	menu.findItem(R.id.card_menu_remove_current_card).setVisible(false);
    	if(cardId>0){
        	menu.findItem(R.id.card_menu_remove_current_card).setVisible(true);
    	}
		return true;
	}

	@Override public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
            	backToParentActivity();
            	break;
            case R.id.card_menu_remove_current_card:
            	deleteCard().show();
            	break;
        }
		return true;
    }
    
    private void backToParentActivity() {
        Intent parentActivityIntent = new Intent(this, CardsActivity.class);
        parentActivityIntent.putExtra("com.fyodorwolf.studyBudy.deckId", deckId);
        parentActivityIntent.putExtra("com.fyodorwolf.studyBudy.deckName",  deckName);
        parentActivityIntent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(parentActivityIntent);
        overridePendingTransition(0,0);
        finish();
	}
	

    /**
     * This method deletes the current deck's card that is being viewed.
     * @return AlertDialog confirming the deletion of a specific card
     */
	private AlertDialog deleteCard() {
    	AlertDialog myDeleteConfirmationBox = new AlertDialog.Builder(this) 
           //set message, title, and icon
           .setTitle("Delete Card") 
           .setMessage("Are you sure you want to delete this card?") 
           .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int whichButton) { 
                   	dialog.dismiss();
		           	QueryRunner deleteCardQuery = new QueryRunner(DatabaseAdapter.getInstance());
		           	deleteCardQuery.setQueryRunnerListener(new QueryRunnerListener(){
		       			@Override public void onPostExcecute(Cursor cards) {
		       				backToParentActivity();
		       			}
		           	});
		           	String queryString = QueryString.getDeleteCardQuery(cardId);
		           	deleteCardQuery.execute(queryString);
               } 
           })
           .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int which) {
                   dialog.dismiss();
               }
           })
           .create();
       return myDeleteConfirmationBox;
   }

	private void showImageGallery() {
		((Button)this.findViewById(R.id.add_images)).setText("Change Images");
		ViewGroup tableRow = (ViewGroup) this.findViewById(R.id.create_card_gallary_row);
		tableRow.setVisibility(View.VISIBLE);
		final ArrayList<Photo> galleryItems = new ArrayList<Photo>();
		for(File img: imageFiles){
			galleryItems.add(new Photo(galleryItems.size(), img.getAbsolutePath(), 0));
		}
		final HorizontalListView gallery = (HorizontalListView) findViewById(R.id.photo_list_view);
		gallery.setOnItemClickListener(new OnItemClickListener(){
			@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Photo myPhoto = galleryItems.get(position);
				Intent intent = new Intent();
				intent.setAction(android.content.Intent.ACTION_VIEW); 
				intent.setDataAndType(Uri.parse("file://"+myPhoto.filename),"image/*");
				startActivity(intent);
			}
		});
		gallery.setAdapter(new BaseAdapter(){
			@Override public int getCount() { return galleryItems.size();}
			@Override public Object getItem(int position) {return galleryItems.get(position);}
			@Override public long getItemId(int position) {return galleryItems.get(position).id;}
			@Override public View getView(int position, View convertView, ViewGroup parent) {
				final Photo myPhoto = galleryItems.get(position);
				View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.galley_photo_item,null);
				ImageView myImage = (ImageView) layout.findViewById(R.id.galley_photo_item);
				ImageLoader.getInstance().displayImage("file://"+myPhoto.filename, myImage,new ImageLoadingListener(){
					@Override public void onLoadingStarted(String imageUri, View view) {}
					@Override public void onLoadingCancelled(String imageUri, View view) {}
					@Override public void onLoadingFailed(String imageUri, View view, FailReason failReason) {}
					@Override public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
						((ImageView)view).setImageBitmap(loadedImage);
					}
				});
				return myImage;
			}
		});
	}

	private void hideImageGallery() {
		this.findViewById(R.id.create_card_gallary_row).setVisibility(View.GONE);
		((Button)this.findViewById(R.id.add_images)).setText("Add Images");
	}
}
