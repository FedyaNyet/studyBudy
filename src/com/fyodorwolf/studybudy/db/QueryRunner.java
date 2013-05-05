package com.fyodorwolf.studybudy.db;


import android.database.Cursor;
import android.os.AsyncTask;

/**
 * This class is designed as a means for running queries on an
 * alternate thread in order to not block any running processes.
 * 
 *** EXAMPLE USAGE ****
 * QueryRunner myGetCardIdQuery = new QueryRunner(DatabaseAdapter.getInstance());
 * myGetCardIdQuery.setQueryRunnerListener(new QueryRunnerListener(){
 * 		@Override onPostExcecute(Cursor id){
 * 			cardId = id.getLong(0);
 * 			...
 * 		}
 * });
 * myGetCardIdQuery.execute(DatabaseAdapter.getLastCardIdQuery());
 * 
 * @author fwolf
 *
 */
	public class QueryRunner extends AsyncTask<String,Integer,Cursor>{

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
		public void onPostExcecute(Cursor cursor);
	}
	
}//E:DeckGetter