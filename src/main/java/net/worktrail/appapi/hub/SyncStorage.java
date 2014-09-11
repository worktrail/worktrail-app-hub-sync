package net.worktrail.appapi.hub;

/**
 * Interface which can be used to store configuration data and information about synced objects.
 */
public interface SyncStorage {
	/**
	 * Set a configuration option as a key/value pair.
	 * @param key config key under which to store the value.
	 * @param value the actual string value to store.
	 */
	public void setString(String key, String value);
	
	/**
	 * Returns a persisted key previously stored with {@link #setString(String, String)}.
	 * @param key key to retrieve.
	 * @return the value or null, if it was never set before.
	 */
	public String getString(String key);
	
	/**
	 * Persists that a object was synced to or from a remote system into/from WorkTrail.
	 * 
	 * @param identifier identifier of the object in the remote system.
	 * @param workTrailId the id of the object within WorkTrail.
	 */
	public void syncedObject(String identifier, long workTrailId);
	
	/**
	 * Checks whether the given identifier of a remote system was already synced into worktrail.
	 * 
	 * @param identifier identifier of an object of a remote system.
	 * @return the id of that object within WorkTrail.
	 */
	public Long wasObjectSynced(String identifier);
	
	/**
	 * Removes all synced objects from the storage.
	 */
	public void cleanSyncedObjects();

	/**
	 * Closes this storage - make sure this call this to give implementations a chance to persist
	 * themselves and close open resources.
	 */
	public void close();
}
