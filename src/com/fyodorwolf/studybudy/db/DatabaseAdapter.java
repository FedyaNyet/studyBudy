package com.fyodorwolf.studybudy.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * This class is designed to provide a singleton class
 * to serve up as the connection to the database for the
 * application. it also houses static methods that return
 * the string that can then be run by the QueryRunner class.
 * 
 *** EXAMPLE USAGE ****
 * QueryRunner myQuery = new QueryRunner(DatabaseAdapter.getInstance());
 * myQuery.execute(DatabaseAdapter.getLastCardIdQuery());
 * 
 * @author fwolf
 *
 */
public class DatabaseAdapter
{
	public final static boolean WIPE_DATABASE = false;
	
	protected static final String TAG = "DataAdapter";
    
	//this is a singleton class
	private static volatile DatabaseAdapter instance;
    private SQLiteDatabase mDb;
    private SQLiteHelper mDbHelper;

    private DatabaseAdapter(){}
    
    public void init(Context context){
        mDbHelper = new SQLiteHelper(context);
        this.createDatabase();
        this.open();
    }

	public static DatabaseAdapter getInstance() {
		if(instance == null){
			synchronized(DatabaseAdapter.class){
				if(instance == null){
					instance = new DatabaseAdapter();
				}
			}
		}
		return instance;
	}
	
    private DatabaseAdapter createDatabase(){
        try{
            mDbHelper.createDataBase();
        } 
        catch (IOException mIOException){
            Log.e(TAG, mIOException.toString() + "  UnableToCreateDatabase");
        }
        return this;
    }
	
    private DatabaseAdapter open(){
        try{
            mDbHelper.openDataBase();
            mDbHelper.close();
            mDb = mDbHelper.getReadableDatabase();
        } 
        catch (SQLException mSQLException){
            Log.e(TAG, "open >>"+ mSQLException.toString());
        }
        return this;
    }
    
    public void close(){
    	if(mDb != null){
    		mDb.close();
    	}
    }

    public Cursor getCursor(String query){
    	try{
		    Cursor mCur = mDb.rawQuery(query, null);
		    if (mCur!=null){
		       mCur.moveToNext();
		    }
		    return mCur;
		}
		catch (SQLException mSQLException){
		     Log.e(TAG, "getCursor >>"+ mSQLException.toString());
	         throw mSQLException;
		}
    }
    
    
/********************************************************************************************************************************************
 * 							Private Classes		 																							*
 ********************************************************************************************************************************************/
	
	private static class SQLiteHelper extends SQLiteOpenHelper{
		
		private static String TAG = "DataBaseHelper"; // Tag just for the LogCat window
		//destination path (location) of our database on device
		private static String DB_PATH = ""; 
		private static String DB_NAME ="studyBudy.sqlite";// Database name
		private SQLiteDatabase mDataBase; 
		private final Context mContext;
		
		public SQLiteHelper(Context context) 
		{
		    super(context, DB_NAME, null, 1);// 1? its Database Version
		    DB_PATH = context.getFilesDir() + "/../databases/";
		    Log.d(TAG,DB_PATH);
		    this.mContext = context;
		}  

		public void createDataBase() throws IOException
		{
		    //If database not exists copy it from the assets
			boolean mDataBaseExist = checkDataBase();
		    if(!mDataBaseExist)
		    {
		        this.getReadableDatabase();
		        this.close();
		        try 
		        {
		            //Copy the database from assests
		            copyDataBase();
		            Log.e(TAG, "createDatabase database created");
		        } 
		        catch (IOException mIOException) 
		        {
		            throw new Error("ErrorCopyingDataBase");
		        }
		    }
		}
		
	    //Check that the database exists here: /data/data/your package/databases/Da Name
	    private boolean checkDataBase()
	    {
	        File dbFile = new File(DB_PATH + DB_NAME);
	        //Log.v("dbFile", dbFile + "   "+ dbFile.exists());
	        if(WIPE_DATABASE){
	        	dbFile.delete();
	        }
	        return dbFile.exists();
	    }
	
	    //Copy the database from assets
	    private void copyDataBase() throws IOException
	    {
	        InputStream mInput = mContext.getAssets().open(DB_NAME);
	        OutputStream mOutput = new FileOutputStream(DB_PATH + DB_NAME);
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
	
	    //Open the database, so we can query it
	    public boolean openDataBase() throws SQLException
	    {
	        String mPath = DB_PATH + DB_NAME;
	        //Log.v("mPath", mPath);
	        mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.CREATE_IF_NECESSARY);
	        return mDataBase != null;
	    }
	
		@Override
		public void onCreate(SQLiteDatabase db) {}
	
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
	}//E: SQLiteHelper
	
}