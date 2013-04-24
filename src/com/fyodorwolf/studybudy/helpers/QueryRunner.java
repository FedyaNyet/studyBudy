package com.fyodorwolf.studybudy.helpers;

import android.database.Cursor;
import android.os.AsyncTask;

/********************************************************************************************************************************************
 * 							Private Classes		 																							*
 ********************************************************************************************************************************************/
	public class QueryRunner extends AsyncTask<String,Integer,Cursor>{

		/**
	 * 
	 */
	private DatabaseAdapter _myAdapter;
	private QueryRunnerListener _myListener;
	/**
	 * @param deckActivity
	 */
	public QueryRunner(DatabaseAdapter dbAdapter) {
		this._myAdapter = dbAdapter;
		this._myListener = new QueryRunnerListener(){ 
			@Override public void onPostExcecute(Cursor cards){}
		};
	}

	private static final String TAG = "DeckGetter";
	
	@Override
	protected Cursor doInBackground(String... params) {
		return _myAdapter.getCursor(params[0]);
	}
	
	@Override
	protected void onPostExecute(final Cursor result) {
		_myListener.onPostExcecute(result);
	}

	
	public void setQueryRunnerListener(QueryRunnerListener listener){
		_myListener = listener;
	}
	
	public interface QueryRunnerListener{
		public void onPostExcecute(Cursor cards);
	}
	
}//E:DeckGetter