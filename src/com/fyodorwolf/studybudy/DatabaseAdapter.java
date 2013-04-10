package com.fyodorwolf.studybudy;

import java.io.IOException;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DatabaseAdapter 
{
    protected static final String TAG = "DataAdapter";

    private final Context mContext;
    private SQLiteDatabase mDb;
    private SQLiteHelper mDbHelper;

    public DatabaseAdapter(Context context) 
    {
        this.mContext = context;
        mDbHelper = new SQLiteHelper(mContext);
    }

    public DatabaseAdapter createDatabase() throws SQLException 
    {
        try 
        {
            mDbHelper.createDataBase();
        } 
        catch (IOException mIOException) 
        {
            Log.e(TAG, mIOException.toString() + "  UnableToCreateDatabase");
            throw new Error("UnableToCreateDatabase");
        }
        return this;
    }

    public DatabaseAdapter open() throws SQLException 
    {
        try 
        {
            mDbHelper.openDataBase();
            mDbHelper.close();
            mDb = mDbHelper.getReadableDatabase();
        } 
        catch (SQLException mSQLException) 
        {
            Log.e(TAG, "open >>"+ mSQLException.toString());
            throw mSQLException;
        }
        return this;
    }

    public void close() 
    {
        mDbHelper.close();
    }
    
    private Cursor getCursor(String query){
    	try
		{
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
    
	public Cursor filterForTerm(String term){
		return getCursor("SELECT " +
				"	card._id 'Card._id', dec._id 'Deck._id', sec._id 'Section._id'"+
				"FROM " +
				"	Card card , Deck dec, Section sec"+
				"WHERE " +
				"	(	card.deckId = dec._id " +
				"		and " +
				"		dec.sectionId = sec._id" +
				"	)"+
				"	and " +
				"	(	card.question LIKE '%"+term+"%' " +
				"       or " +
				"       card.answer LIKE '%"+term+"%' " +
				"       or " +
				"       dec.title LIKE '%"+term+"%'" +
				"       or " +
				"       sec.name LIKE '%"+term+"%'" +
				"	)"
			);
	}
	
	public Cursor getCardsWithDeckId(String DeckId){
		return getCursor("SELECT * FROM Card where deckId = "+DeckId);
	}
	
	public Cursor getCardsWithIds(String cardIds){
		return getCursor("SELECT * FROM Card WHERE _id in ("+cardIds+")");
	}
	
	public Cursor getDecksWithSectionId(String sectionId){
		return getCursor("SELECT * FROM Deck where sectionId = "+sectionId);
	}

	public Cursor getDecksWithIds(String deckIds){
		return getCursor("SELECT * FROM Deck WHERE _id in ("+deckIds+")");
	}
	
	public Cursor getSectionsWithSectionIds(String sectionIds){
		return getCursor("SELECT * FROM Section WHERE _id in ("+sectionIds+")");
	}
	    
	public Cursor getSections(){
		return getCursor("SELECT * FROM Section");
	}
}