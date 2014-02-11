package net.worktrail.appapi.hub.git;

public interface SyncStorage {
	public void setString(String key, String value);
	public String getString(String key);
	
	public void syncedObject(String identifier, long workTrailId);
	public Long wasObjectSynced(String identifier);
	public void cleanSyncedObjects();

	public void close();
}
