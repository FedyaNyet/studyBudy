package com.fyodorwolf.studybudy;
 
import java.util.ArrayList;
 
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
 
public class MultiPhotoSelectActivity extends Activity {
 
    private ArrayList<String> imageUrls;
    private ImageAdapter imageAdapter;
    protected ImageLoader imageLoader = ImageLoader.getInstance();

	private final String TAG = MultiPhotoSelectActivity.class.getSimpleName();
    
    public static final String RESULT_BUNDLE_INDENTIFIER = "com.fyodorwolf.studyBudy.imageStrings";
 
    @Override  public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        /*VIEW SETUP & INSTANTIATION*/
        setContentView(R.layout.ac_image_grid);
        setTitle("Select Card Images");
 
        /*FETCH MEDIA*/
        final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
        Cursor imagecursor = this.getContentResolver().query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, MediaStore.Images.Media.DATE_TAKEN+" DESC"
        );
 
        /*SET GRIDVIEW ADAPTER*/
        this.imageUrls = new ArrayList<String>();
        for (int i = 0; i < imagecursor.getCount(); i++) {
            imagecursor.moveToPosition(i);
            int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);
            imageUrls.add(imagecursor.getString(dataColumnIndex));
        }
        imageAdapter = new ImageAdapter(this, imageUrls);
        ((GridView)findViewById(R.id.gridview)).setAdapter(imageAdapter);
        
        /*ACTIONS*/
        ((Button)findViewById(R.id.choose_photos)).setOnClickListener(new OnClickListener(){
			@Override public void onClick(View v) {          
				ArrayList<String> selectedItems = imageAdapter.getCheckedItems();
				String[] files = new String[selectedItems.size()];
	          	selectedItems.toArray(files);
	          	Intent resultIntent = new Intent();
	          	resultIntent.putExtra(RESULT_BUNDLE_INDENTIFIER, files);
	          	setResult(Activity.RESULT_OK, resultIntent);
	          	finish();
			}
        });
        
    }
 
    @Override protected void onStop() {
        imageLoader.stop();
        super.onStop();
    }
 
    public class ImageAdapter extends BaseAdapter {
 
        ArrayList<String> mList;
        LayoutInflater mInflater;
        Context mContext;
        SparseBooleanArray mSparseBooleanArray;
        OnCheckedChangeListener mCheckedChangeListener = new OnCheckedChangeListener() {
        	@Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSparseBooleanArray.put((Integer) buttonView.getTag(), isChecked);
            }
        };
        
        public ImageAdapter(Context context, ArrayList<String> imageList) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
            mSparseBooleanArray = new SparseBooleanArray();
            mList = new ArrayList<String>();
            this.mList = imageList;
        }
 
		public ArrayList<String> getCheckedItems() {
            ArrayList<String> mTempArry = new ArrayList<String>();
            for(int i=0;i<mList.size();i++) {
                if(mSparseBooleanArray.get(i)) {
                    mTempArry.add(mList.get(i));
                }
            }
            return mTempArry;
        }
        @Override public int getCount() {  return imageUrls.size();}
        @Override public Object getItem(int position) { return null;}
        @Override public long getItemId(int position) {return position;}
 
        @Override public View getView(final int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = mInflater.inflate(R.layout.row_multiphoto_item, null);
            }
 
            final CheckBox mCheckBox = (CheckBox) convertView.findViewById(R.id.checkBox1);
            final ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView1);
 
            imageLoader.displayImage("file://"+imageUrls.get(position), imageView, null, new SimpleImageLoadingListener() {
            	@Override public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage){
                    Animation anim = AnimationUtils.loadAnimation(MultiPhotoSelectActivity.this, android.R.anim.fade_in);
                    imageView.setAnimation(anim);
                    anim.start();
            	}
            });
            
            imageView.setOnClickListener(new OnClickListener(){
        		@Override public void onClick(View v) {
                    mCheckBox.setChecked(!mSparseBooleanArray.get(position));
				}
    		});
 
            mCheckBox.setTag(position);
            mCheckBox.setOnCheckedChangeListener(mCheckedChangeListener);
            mCheckBox.setChecked(mSparseBooleanArray.get(position));
            return convertView;
        }

    }
 
}
