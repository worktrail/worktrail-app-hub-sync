package net.worktrail.appapi.hub.git;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertySyncStorage implements SyncStorage {
	
	private Properties props;
	private Properties syncProps;
	private boolean dirty;
	private File file;
	private boolean dirtySyncProps;
	private File syncStorageFile;
	
	public PropertySyncStorage(File file, File syncStorageFile) {
		this.file = file;
		this.syncStorageFile = syncStorageFile;
		props = new Properties();
		syncProps = new Properties();
		try {
			if (file.exists()) {
				FileInputStream is = new FileInputStream(file);
				props.load(is);
				is.close();
			}
			if (syncStorageFile.exists()) {
				FileInputStream is = new FileInputStream(syncStorageFile);
				syncProps.load(is);
				is.close();
			}
		} catch (IOException e) {
			throw new RuntimeException("Error while loading properties.", e);
		}
		dirty = false;
		dirtySyncProps = false;
	}
	
	public void save() {
		if (dirty) {
			try {
				FileOutputStream out = new FileOutputStream(file);
				props.store(out, "");
				out.close();
				dirty = false;
			} catch (IOException e) {
				throw new RuntimeException("Error while storing output stream.", e);
			}
		}
		if (dirtySyncProps) {
			try {
				FileOutputStream out = new FileOutputStream(syncStorageFile);
				syncProps.store(out, "");
				out.close();
				dirtySyncProps = false;
			} catch (IOException e) {
				throw new RuntimeException("Error while storing output stream.", e);
			}
		}
	}
	
	@Override
	public void close() {
		save();
	}

	@Override
	public void setString(String key, String value) {
		props.setProperty(key, value);
		dirty = true;
		save();
	}

	@Override
	public String getString(String key) {
		return props.getProperty(key);
	}

	@Override
	public void syncedObject(String identifier, long workTrailId) {
		syncProps.setProperty("sync."+identifier, Long.toString(workTrailId));
		dirtySyncProps = true;
	}

	@Override
	public Long wasObjectSynced(String identifier) {
		String id = syncProps.getProperty("sync."+identifier);
		if (id == null) {
			return null;
		}
		return new Long(id);
	}

	@Override
	public void cleanSyncedObjects() {
		syncProps.clear();
		dirtySyncProps = true;
	}

}
