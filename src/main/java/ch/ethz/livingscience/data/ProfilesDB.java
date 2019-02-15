package ch.ethz.livingscience.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import utils.Log;

public class ProfilesDB 
{
	static final String dbName = "livingscience";
	static final String pubsCollection = "arxivcoll";
	static final String profilesAutomaticCollection = "profilesauto";
	static final String profilesManualCollection = "profilesmanual";
	static final String listsCollection = "publists";
	static final String acmCollection = "acmwords";

	MongoClient mongo;
	DB db;
	
	public DBCollection collPubs;
	public DBCollection collProfilesAuto;
	public DBCollection collAcm;
	DBCollection collProfilesManual;
	DBCollection collPubLists;
	
	public ProfilesDB(int mongoPort) throws Exception
	{
		mongo = new MongoClient("127.0.0.1", mongoPort);
		db = mongo.getDB(dbName);
		
		collPubs = db.getCollection(pubsCollection);
		collProfilesAuto = db.getCollection(profilesAutomaticCollection);
		collProfilesManual = db.getCollection(profilesManualCollection);
		collPubLists = db.getCollection(listsCollection);
		collAcm = db.getCollection(acmCollection);
		System.out.println("Connected to mongo.");
	}
	
	public Publication getPub(String id)
	{
		BasicDBObject query = new BasicDBObject("_id", new ObjectId(id));
		DBObject result = collPubs.findOne(query);
		Publication pub = toPub(result);
		pub.id = id;
		return pub;
	}
	
	public Publication toPub(DBObject result)
	{
		Publication pub = new Publication();
		
		if (result != null)
		{
		  pub.authors = getList(result.get("authors"));
		  pub.journal = getStringOrNull(result.get("journal"));
		  pub.title = getStringOrNull(result.get("title"));
		
		  String y = getStringOrNull(result.get("year"));
		  pub.year = y == null ? 0 : new Integer(y);
		  pub.summary = getStringOrNull(result.get("summary"));
		  pub.url = getStringOrNull(result.get("url"));
		
		  pub.affiliations = getList(result.get("affiliations"));
		
		  result.put("id", result.get("_id").toString());

		  pub.json = result.toString();
		}
		
		return pub;
	}
	
	static String getStringOrNull(Object o)
	{
		return o == null ? null : o.toString();
	}
	
	public static List<String> getList(Object result)
	{
		List<String> list = new ArrayList<>();
		if (result == null) return list;
		
		BasicDBList e = (BasicDBList) result;
		for (Object o : e)
		{
			list.add(o.toString());
		}
		
		return list;
	}
	
	public Map<String, String> labelIDtoInternalID = new HashMap<>();
	
	public boolean updatePub(String pubID, String json)
	{
		BasicDBObject query = new BasicDBObject("_id", new ObjectId(pubID));
		DBObject result = collPubs.findOne(query);
		if (result == null) return false;
		
		DBObject newData = (DBObject) com.mongodb.util.JSON.parse(json);
		collPubs.update(query, newData);
		Log.log("UPDATE pubs/" + pubID + " was " + result.toString());
		return true;
	}
	
	public boolean removePubFromProfile(String profileID, String pubID)
	{
		BasicDBObject query = getProfileQuery(profileID);
		
		DBCollection col = collProfilesManual;
		DBObject result = col.findOne(query);
		if (result == null) 
		{
			col = collProfilesAuto;
			result = col.findOne(query);
		}
		if (result == null) return false;
		
		List<String> pubIDs = getList(result.get("pubs"));
		if (!pubIDs.remove(pubID)) return false;
		
		result.put("pubs", pubIDs);
		
		col.update(query, result);
		return true;
	}
	
	private BasicDBObject getProfileQuery(String id) throws RuntimeException
	{
		String internalID = labelIDtoInternalID.get(id);
		BasicDBObject query = null;
		try {
			if (internalID == null)
			{
				query = new BasicDBObject("_id", new ObjectId(id));
			}
			else
			{
				query = new BasicDBObject("_id", new ObjectId(internalID));
			}
		}
		catch(IllegalArgumentException i){
			System.out.println("RuntimeException");
		}
		return query;
	}
	
	public Profile getProfile(String id)
	{
		
		BasicDBObject query = getProfileQuery(id);
		DBObject result = collProfilesManual.findOne(query);
		if (result == null) result = collProfilesAuto.findOne(query);
		if (result == null) return null;
		
		Profile profile = new Profile();
		
		profile.id = id;
		
		profile.lsid = id;
		result.put("lsid", profile.lsid);
		result.put("isProfile", true);
		
		profile.name = getStringOrNull(result.get("name"));
		profile.affiliation = getStringOrNull(result.get("affiliation"));
		profile.pubIDs = getList(result.get("pubs"));
		
		profile.json = result.toString();
		
		return profile;
	}
}