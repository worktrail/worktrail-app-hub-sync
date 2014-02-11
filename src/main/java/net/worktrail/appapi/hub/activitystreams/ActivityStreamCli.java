package net.worktrail.appapi.hub.activitystreams;

import net.worktrail.appapi.WorkTrailAppApi;
import net.worktrail.appapi.hub.WorkTrailCliFramework;
import net.worktrail.appapi.hub.WorkTrailSync;
import net.worktrail.appapi.hub.git.SyncStorage;

public class ActivityStreamCli extends WorkTrailCliFramework {

	@Override
	protected WorkTrailSync createSyncObject(SyncStorage storage,
			WorkTrailAppApi auth, String[] args) {
		return new ActivityStreamSync(storage, auth, args);
	}

	public static void main(String[] args) {
		new ActivityStreamCli().executeFromCommandline(args);
	}

	@Override
	protected String getSyncUnixName() {
		return "activitystreamsync";
	}
}
