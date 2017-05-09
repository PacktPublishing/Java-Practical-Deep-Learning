package MLJavaBook.chap6;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.AbstractIDMigrator;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.model.PlusAnonymousConcurrentUserDataModel;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.GenericItemSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.GenericItemSimilarity.ItemItemSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
//import org.apache.mahout.cf.taste.impl.recommender.slopeone.SlopeOneRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.cf.taste.impl.model.jdbc.MySQLJDBCDataModel;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
/**
 * Hello world!
 *
 */
public class App {

	static HashMap books;

	public static void main(String[] args) throws Exception {
//		
//		StringItemIdFileDataModel model = new StringItemIdFileDataModel(
//				new File("/Users/bostjan/Dropbox/ML Java Book/book/datasets/chap6/BX-Book-Ratings.csv"), ";");
//		System.out.println("Total items: " + model.getNumItems() + "\nTotal users: " +model.getNumUsers());

		books = loadBooks("/Users/bostjan/Dropbox/ML Java Book/book/datasets/chap6/BX-Books.csv");
//		// System.out.println(books.keySet());
		itemBased();
//		userBased();
		// DataModel model = new FileDataModel(new
		// File("/Users/bostjan/Dropbox/ML Java Book/book/datasets/chap6/dataset.csv"));

		// Collection<GenericItemSimilarity.ItemItemSimilarity> correlations =
		// ...;
		// ItemSimilarity itemSimilarity = new
		// GenericItemSimilarity(correlations);

		// for (RecommendedItem recommendation : recommendations) {
		// System.out.println(recommendation);
		// }

		// DataModel model;
		// model = new FileDataModel(new
		// File("/home/padjiman/data/movieLens/mahout/ratingsForMahout.dat"));
		// CachingRecommender cachingRecommender = new CachingRecommender(new
		// SlopeOneRecommender(model));
		//
		// List recommendations = cachingRecommender.recommend(1, 10);
		// for (RecommendedItem recommendedItem : recommendations) {
		// System.out.println(recommendedItem);
		// }

	}

	public static HashMap loadBooks(String filename) throws Exception {
		HashMap<String, String> map = new HashMap<String, String>();
		BufferedReader in = new BufferedReader(new FileReader(filename));
		String line = "";
		while ((line = in.readLine()) != null) {
			String parts[] = line.replace("\"", "").split(";");
			map.put(parts[0], parts[1]);
		}
		in.close();
		// System.out.println(map.toString());
		System.out.println("Total items: " + map.size());
		return map;
	}

	public static void itemBased() throws IOException, TasteException {
//		 DataModel model = new FileDataModel(new File("/Users/bostjan/Dropbox/ML Java Book/book/datasets/chap6/dataset.csv"));
		
		StringItemIdFileDataModel model = new StringItemIdFileDataModel(
				new File("/Users/bostjan/Dropbox/ML Java Book/book/datasets/chap6/BX-Book-Ratings.csv"), ";");

		//Collection<GenericItemSimilarity.ItemItemSimilarity> correlations = null;
		//ItemItemSimilarity iis = new ItemItemSimilarity(0, 0, 0);
		//ItemSimilarity itemSimilarity = new GenericItemSimilarity(correlations);
		ItemSimilarity itemSimilarity = new PearsonCorrelationSimilarity(model);
		
		
		ItemBasedRecommender recommender = new GenericItemBasedRecommender(model, itemSimilarity);

		IDRescorer rescorer = new MyRescorer();

		// List recommendations = recommender.recommend(2, 3, rescorer);
		String itemISBN = "042513976X";
		long itemID = model.readItemIDFromString(itemISBN);
		int noItems = 10;

		System.out.println("Recommendations for item: "+books.get(itemISBN));


		System.out.println("\nMost similar items:");
		List<RecommendedItem> recommendations = recommender.mostSimilarItems(itemID, noItems);
		for (RecommendedItem item : recommendations) {
			itemISBN = model.getItemIDAsString(item.getItemID());
			System.out.println("Item: " + books.get(itemISBN) + " | Item id: " + itemISBN + " | Value: " + item.getValue());
		}
	}
	
	public static void userBased() throws IOException, TasteException {
//		 DataModel model = new FileDataModel(new File("/Users/bostjan/Dropbox/ML Java Book/book/datasets/chap6/dataset.csv"));
		StringItemIdFileDataModel model = new StringItemIdFileDataModel(
				new File("/Users/bostjan/Dropbox/ML Java Book/book/datasets/chap6/BX-Book-Ratings.csv"), ";");
		UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
		UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
		UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

		IDRescorer rescorer = new MyRescorer();

		// List recommendations = recommender.recommend(2, 3, rescorer);
		long userID = 276704;// 276704;//212124;//277157;
		int noItems = 10;

		System.out.println("Rated items:");
		for (Preference preference : model.getPreferencesFromUser(userID)) {
			String itemISBN = model.getItemIDAsString(preference.getItemID());
			System.out.println("Item: " + books.get(itemISBN) + " | Item id: "
					+ itemISBN + " | Value: " + preference.getValue());
		}

		System.out.println("\nRecommended items:");
		List<RecommendedItem> recommendations = recommender.recommend(userID,noItems);
		for (RecommendedItem item : recommendations) {
			String itemISBN = model.getItemIDAsString(item.getItemID());
			System.out.println("Item: " + books.get(itemISBN) + " | Item id: "
					+ itemISBN + " | Value: " + item.getValue());
		}
	}
	
	public static void loadFromDB() throws Exception{
		// In-memory DataModel - GenericDataModels
FastByIDMap <PreferenceArray> preferences = new FastByIDMap <PreferenceArray> (); 

  PreferenceArray prefsForUser1 = new GenericUserPreferenceArray (10);  
  prefsForUser1.setUserID (0, 1L); 
  prefsForUser1.setItemID (0, 101L); 
  prefsForUser1.setValue (0, 3.0f);  
  prefsForUser1.setItemID (1, 102L); 
  prefsForUser1.setValue (1, 4.5F); 
  preferences.put (1L, prefsForUser1); // use userID as the key 
 

  DataModel model = new GenericDataModel(preferences); 

		  // File-based DataModel - FileDataModel 
		  DataModel dataModel = new FileDataModel (new File ("preferences.csv")); 

		  // Database-based DataModel - MySQLJDBCDataModel 
		  /*
   * A JDBCDataModel backed by a PostgreSQL database and accessed via JDBC. It may work with other JDBC databases. 
   * By default, this class assumes that there is a DataSource available under the JNDI name "jdbc/taste", which gives 
   * access to a database with a "taste_preferences" table with the following schema:
   * 	CREATE TABLE taste_preferences (
   * 		user_id BIGINT NOT NULL,
   * 		item_id BIGINT NOT NULL,
   * 		preference REAL NOT NULL,
   * 		PRIMARY KEY (user_id, item_id)
   * 	)
   * 	CREATE INDEX taste_preferences_user_id_index ON taste_preferences (user_id);
   * 	CREATE INDEX taste_preferences_item_id_index ON taste_preferences (item_id);
   * */
  MysqlDataSource dbsource = new MysqlDataSource();
  dbsource.setUser("user");
  dbsource.setPassword("pass");
  dbsource.setServerName("hostname.com");
  dbsource.setDatabaseName("db");
	
  DataModel dataModelDB = new MySQLJDBCDataModel(dbsource, "taste_preferences", 
	  "user_id", "item_id", "preference", "timestamp");
		  
//		  DataModel dataModel = new MySQLJDBCDataModel (dbsource, "my_prefs_table", "My_user_column", "my_item_column", "my_pref_value_column"); 
	}
	
//	public static void loadDBInMemory(){
//		  // In-memory DataModel - GenericDataModel 
//		  FastByIDMap <PreferenceArray> preferences = new FastByIDMap <PreferenceArray> (); 
//
//		  PreferenceArray prefsForUser1 = new GenericUserPreferenceArray (10);  
//		  prefsForUser1.setUserID (1L); 
//		  prefsForUser1.setItemID (101L); 
//		  prefsForUser1.setValue (0, 3.0f);  
//		  prefsForUser1.setItemID (1 102L); 
//		  prefsForUser1.setValue (1, 4.5F); 
//		  preferences.put (1L, prefsForUser1); / / use userID as the key 
//	}


}

class MyRescorer implements IDRescorer {

	public boolean isFiltered(long arg0) {
		return false;
	}

	public double rescore(long itemId, double originalScore) {
		if(bookIsNew(itemId)){
			originalScore *= 1.3;
		}
		return Math.random();
	}

	private boolean bookIsNew(long itemId) {
		// TODO Auto-generated method stub
		return false;
	}

}

class OnlineRecommendation{
	
	Recommender recommender;
	int concurrentUsers = 100;
	int noItems = 10;
	
	public OnlineRecommendation() throws IOException {
		
		 
		DataModel model = new StringItemIdFileDataModel(
				new File("/Users/bostjan/Dropbox/ML Java Book/book/datasets/chap6/BX-Book-Ratings.csv"), ";");
		PlusAnonymousConcurrentUserDataModel plusModel = new PlusAnonymousConcurrentUserDataModel(model, concurrentUsers);
		//recommender = ...;
		
	}
	
	public List<RecommendedItem> recommend(long userId, PreferenceArray preferences) throws TasteException{
		
		if(userExistsInDataModel(userId)){
			return recommender.recommend(userId, noItems);
		}
		else{
			PlusAnonymousConcurrentUserDataModel plusModel =
					   (PlusAnonymousConcurrentUserDataModel) recommender.getDataModel();
			
			// Take an available anonymous user form the poll
			Long anonymousUserID = plusModel.takeAvailableUser();
			
			// Set temporary preferences
			PreferenceArray tempPrefs = preferences;
			 tempPrefs.setUserID(0, anonymousUserID);
			 //tempPrefs.setItemID(0, itemID);
			 plusModel.setTempPrefs(tempPrefs, anonymousUserID);
			 
			List<RecommendedItem> results = recommender.recommend(anonymousUserID, noItems);
			
			// Release the user back to the poll
			plusModel.releaseUser(anonymousUserID);
			
			return results;

		}
		
	}

	private boolean userExistsInDataModel(long userId) {
		// TODO Auto-generated method stub
		return false;
	}
		// val dataModel = new PlusAnonymousConcurrentUserDataModel(
		// new FileDataModel(file),
		// 100)
		//
		// val recommender: org.apache.mahout.cf.taste.recommender.Recommender =
		// ...
		//
		// // we are assuming a unary model: we only know which items a user
		// likes
		// def recommendFor(userId: Long, userPreferences: List[Long]) = {
		// if (userExistsInDataModel(userId)) {
		// recommendForExistingUser(userId)
		// } else {
		// recommendForNewUser(userPreferences)
		// }
		// }
		//
		// def recommendForNewUser(userPreferences: List[Long]) = {
		// val tempUserId = dataModel.takeAvailableUser()
		//
		// try {
		// // filling in a Mahout data structure with the user's preferences
		// val tempPrefs = new BooleanUserPreferenceArray(userPreferences.size)
		// tempPrefs.setUserID(0, tempUserId)
		// userPreferences.zipWithIndex.foreach { case (preference, idx) =&gt;
		// tempPrefs.setItemID(idx, preference)
		// }
		// dataModel.setTempPrefs(tempPrefs, tempUserId)
		//
		// recommendForExistingUser(tempUserId)
		// } finally {
		// dataModel.releaseUser(tempUserId)
		// }
		// }
		//
		// def recommendForExistingUser(userId: Long) = {
		// recommender.recommend(userId, 10)
		// }
	//}
}

class StringItemIdFileDataModel extends FileDataModel {

	public ItemMemIDMigrator memIdMigtr;


	public StringItemIdFileDataModel(File dataFile, String regex) throws IOException {
		super(dataFile, regex);
	}

	@Override
	protected long readItemIDFromString(String value) {

		if (memIdMigtr == null) {
			memIdMigtr = new ItemMemIDMigrator();
		}

		long retValue = memIdMigtr.toLongID(value);
		if (null == memIdMigtr.toStringID(retValue)) {
			try {
				memIdMigtr.singleInit(value);
			} catch (TasteException e) {
				e.printStackTrace();
			}
		}
		return retValue;
	}

	String getItemIDAsString(long itemId) {
		return memIdMigtr.toStringID(itemId);
	}
}

class ItemMemIDMigrator extends AbstractIDMigrator {

	private FastByIDMap<String> longToString;

	public ItemMemIDMigrator() {
		this.longToString = new FastByIDMap<String>(10000);
	}

	public void storeMapping(long longID, String stringID) {
		longToString.put(longID, stringID);
	}

	public void singleInit(String stringID) throws TasteException {
		storeMapping(toLongID(stringID), stringID);
	}

	public String toStringID(long longID) {
		return longToString.get(longID);
	}

}