package net.worktrail.appapi.hub.git;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import net.worktrail.appapi.WorkTrailAppApi;
import net.worktrail.appapi.hub.WorkTrailSync;
import net.worktrail.appapi.model.Employee;
import net.worktrail.appapi.model.HubEntry;
import net.worktrail.appapi.model.SrcType;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class GitSync extends WorkTrailSync {

	private Git git;
	private SyncStorage storage;
	private Properties props;
	private String urlPrefix;
	private String projectName;

	public GitSync(WorkTrailAppApi auth, SyncStorage storage, File gitRepository) {
		super(auth, storage);
		this.storage = storage;
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		try {
			readProperties(gitRepository);
			Repository repository = builder.setGitDir(new File(gitRepository, ".git"))
				.readEnvironment()
				.findGitDir()
				.build();
			git = new Git(repository);
		} catch (IOException e) {
			throw new RuntimeException("Error while creating file repository.", e);
		}
	}
	
	private void readProperties(File gitRepository) throws FileNotFoundException, IOException {
		File workTrailProperties = new File(gitRepository, ".worktrail.properties");
		if (!workTrailProperties.exists()) {
			throw new RuntimeException("No .worktrail.properties file found. Please create one!");
		}
		props = new Properties();
		props.load(new FileInputStream(workTrailProperties));
		
		urlPrefix = props.getProperty("urlprefix");
		projectName = props.getProperty("projectName");
		if (projectName == null) {
			projectName = gitRepository.getName();
		}
	}

	@Override
	public List<HubEntry> startHubSync() {
		// First fetch all employees
		try {

			
			Iterable<RevCommit> logs;
			logs = git.log().all().call();
			List<HubEntry> toCreate = new ArrayList<>();
//			List<String> identifier = new ArrayList<>();
			Set<String> missingUsers = new HashSet<>();
			for (RevCommit rev : logs) {
				PersonIdent author = rev.getAuthorIdent();
				String emailAddress = author.getEmailAddress();
				Employee employee = getEmployeeByEmail(emailAddress);
				if (employee != null) {
					String identifier = rev.getId().getName();
					if (storage.wasObjectSynced(identifier) != null) {
						// object was already synced.. nothing to do.
						continue;
					}
//					System.out.println("Found user with " + emailAddress);
					toCreate.add(
							new HubEntry(
									identifier,
									employee,
									new Date(rev.getCommitTime() * 1000L),
									null,
									SrcType.SCM,
									"Git Commit ("+projectName+"): " + rev.getShortMessage(),
									urlPrefix == null ? null : urlPrefix + rev.getId().getName()));
//					identifier.add(rev.getId().getName());
				} else {
//					System.out.println("No such user: " + emailAddress);
					missingUsers.add(emailAddress);
				}
//				System.out.println("rev: " + rev + " - " + rev.getShortMessage() + " - " + new Date(rev.getCommitTime() * 1000L));
			}
			
			return toCreate;
		} catch (GitAPIException | IOException e) {
			throw new RuntimeException("Error while syncing logs.", e);
		}
	}

}
