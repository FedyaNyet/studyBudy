package com.fyodorwolf.studybudy.db;

public class QueryString {

	private QueryString(){};
	
	public static String join(long[] array){
		return join(array,",");
	}
	
	public static String join(long[] array,String delimiter){
		String ids = "";
		for(long deckId : array){
			ids += deckId+delimiter;
		}
		return ids.substring(0,ids.length()-1);
	}
	
	public static String getSearchTermQuery(String term){
		return 
			"SELECT " +
				"sec._id," +
				"sec.name," +
				"deck._id," +
				"deck.name, " +
				"card._id " +
			"FROM " +
			"	Card card "+
			"JOIN Deck deck ON card.deckId = deck._id " +
			"JOIN Section sec ON deck.sectionId = sec._id " +
			"WHERE " +
				"sec.name LIKE '%"+term+"%'" +
				"or " +
				"deck.name LIKE '%"+term+"%'" +
				"or " +
				"card.question LIKE '%"+term+"%' " +
				"or " +
				"card.answer LIKE '%"+term+"%' " +
			"ORDER BY sec.name ASC";
	}
	
	public static String getCardsWithIdsQuery(long[] cardIds) {
		return "SELECT c._id, c.question, c.answer, c.status, c.numberInDeck, p._id, p.filename, p.orderNum FROM Card c LEFT OUTER JOIN Photo p on p.cardId = c._id WHERE c._id IN ("+join(cardIds)+")";
	}
	
	public static String getCardsWithDeckIdQuery(long DeckId){
		return "SELECT c._id, c.question, c.answer, c.status, c.numberInDeck, p._id, p.filename, p.orderNum FROM Card c LEFT OUTER JOIN Photo p on p.cardId = c._id WHERE deckId = "+DeckId;
	}
	
	public static String getCardsWithDeckIdAndStatusQuery(long DeckId, int status){
		return getCardsWithDeckIdQuery(DeckId)+" and status = "+status;
	}
	
	public static String getCardUpdateStatusQuery(float cardId, int status){
		return "UPDATE Card SET status = "+status+" WHERE  _id = "+cardId;
	}
	
	public static String getCreateCardQuery(String question, String answer,long deckId) {
		return "INSERT INTO Card (question,answer,deckId) VALUES (\""+question+"\",\""+answer+"\","+deckId+")";
	}
	
	public static String getLastCardIdQuery() {
		return "SELECT MAX(_id) from Card";
	}

	public static String getRemoveCardQuery(long cardId){
		return "DELETE FROM Card WHERE _id = "+cardId;
	}
	
	public static String getCreateSectionQuery(String sectionName) {
		return "INSERT INTO Section (name) values (\""+sectionName+"\")";
	}
	
	public static String getLastSectionIdQuery() {
		return "SELECT MAX(_id) FROM Section";
	}
	
	public static String getResetAllCardsInDeckStatusQuery(long deckId){
		return "UPDATE Card set status = 0 WHERE deckId = "+deckId;
	}
	
	public static String getCreateDeckQuery(String deckName, long sectionId) {
		return "INSERT INTO Deck (name,sectionId) values (\""+deckName+"\","+sectionId+")";
	}
	
	public static String getCardWithPhotosQuery(long cardId){
		return "SELECT c.question, c.answer, p._id, p.filename FROM Card c LEFT OUTER JOIN Photo p on c._id =  p.cardId WHERE c._id ="+cardId;
	}
	
	public static String getCreatePhotoForLatestCardQuery(String[] imagePaths) {
		String values= "";
		int orderNum = 0;
		for(String path: imagePaths){
			values += "(\""+path+"\",(SELECT MAX(_id) from Card),"+orderNum+"),";
			orderNum++;
		}
		values = values.substring(0,values.length()-1);
		return "INSERT INTO Photo (filename,cardId,orderNum) values "+values;
	}
	public static String getPhotosWithCardIdQuery(long cardId){
		return "SELECT _id, filename, orderNum FROM Photo where cardId = "+cardId;
	}

	public static String getDeckQuery(long deckId) {
		return "SELECT _id, name, sectionId FROM Deck WHERE _id = "+deckId;
	}
	
	public static String getUpdateDeckQuery(long deckId, String deckName, long sectionId) {
		return "UPDATE Deck SET name=\""+deckName+"\", sectionId="+sectionId+" WHERE _id="+deckId;
	}

	public static String getRemoveDecksWithIdsQuery(long[] deckIds){
		return "DELETE FROM Deck WHERE _id IN ("+join(deckIds)+")";
	}
	
	public static String getRemoveEmptySectionsQuery(){
		return "DELETE FROM Section WHERE _id NOT IN (SELECT DISTINCT(sectionId) from Deck)";
	}
	
	public static String getGroupedDeckQuery(){
		return "SELECT " +
					"sec._id," +
					"sec.name," +
					"deck._id," +
					"deck.name " +
				"FROM " +
					"Section sec " +
				"JOIN Deck deck ON deck.sectionId = sec._id " +
				"ORDER BY sec.name ASC";
	}

	public static String getPhotosForDeck(long[] deckIds) {
		return "SELECT p._id, p.filename, p.cardId FROM Deck d, Card c, Photo p WHERE d._id  = c.deckId and c._id = p.cardId AND d._id IN("+join(deckIds)+")";
	}

	public static String getDeletePhotosQuery(long[] photoIds) {
		return "DELETE FROM Photo WHERE _id IN ("+join(photoIds)+")";
	}

	public static String getDeleteCardsQuery(long[] cardIds) {
		// TODO Auto-generated method stub
		return "DELETE FORM Card WHERE _id IN ("+join(cardIds)+")";
	}
	
	
}
