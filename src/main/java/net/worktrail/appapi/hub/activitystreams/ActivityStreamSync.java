package net.worktrail.appapi.hub.activitystreams;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.DatatypeConverter;

import net.worktrail.appapi.WorkTrailAppApi;
import net.worktrail.appapi.hub.SyncStorage;
import net.worktrail.appapi.hub.WorkTrailSync;
import net.worktrail.appapi.model.Employee;
import net.worktrail.appapi.model.HubEntry;
import net.worktrail.appapi.model.Privacy;
import net.worktrail.appapi.model.SrcType;
import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;

import org.eclipse.jgit.util.Base64;
import org.jsoup.Jsoup;

/**
 * Synchronize an activity stream (e.g. from Jira activities) into WorkTrail Hub.
 */
public class ActivityStreamSync extends WorkTrailSync {

	private SyncStorage storage;
	private Properties props;
	private String url;
	private String username;
	private String password;
	private Privacy defaultPrivacy;

	public ActivityStreamSync(SyncStorage storage, WorkTrailAppApi auth,
			String[] args) {
		super(auth, storage);
		this.storage = storage;
		try {
			File file = new File("activitystream.properties");
			if (!file.exists()) {
				throw new RuntimeException("Please create a file called activitystream.properties which contains activitystream.url, activitystream.username, activitystream.password");
			}
			FileInputStream fis = new FileInputStream(file);
			props = new Properties();
			props.load(fis);
			fis.close();
			url = props.getProperty("activitystream.url");
			username = props.getProperty("activitystream.username");
			password = props.getProperty("activitystream.password");
			String privacy = props.getProperty("activitystream.privacy");
			if (privacy != null) {
				defaultPrivacy = Privacy.getPrivacyByStringIdentifier(privacy);
			}
			if (url == null) {
				throw new RuntimeException("Please create property: activitystream.url in properties file.");
			}
		} catch (Exception e) {
			throw new RuntimeException("Error while reading properties.", e);
		}
	}

	@Override
	public List<HubEntry> startHubSync() {
		try {
			List<HubEntry> toCreate = new ArrayList<>();
			
			HttpURLConnection urlConneciton = (HttpURLConnection) new URL(url).openConnection();
			String auth = Base64.encodeBytes((username + ":" + password).getBytes("UTF-8"));
			urlConneciton.setRequestProperty("Authorization", "Basic " + auth);
			InputStream in = urlConneciton.getInputStream();
			
			Builder builder = new Builder();
			Document doc = builder.build(in);

			String activityNamespace = "http://activitystrea.ms/spec/1.0/";
			String atomNamespace = "http://www.w3.org/2005/Atom";
			
			Element rootElement = doc.getRootElement();
			Elements entryElements = rootElement.getChildElements("entry", atomNamespace);
			
			for (int i = 0 ; i < entryElements.size() ; i++) {
				Element element = entryElements.get(i);//element.getFirst
//				Element activityElement = element.getFirstChildElement("activity:object");
				Element objectElement = element.getFirstChildElement("object", activityNamespace);
				if (objectElement != null) {
					String entryTitle = getValueOfChild(element, "title", atomNamespace);
					String title = getValueOfChild(objectElement, "title", atomNamespace);
					String updatedStr = getValueOfChild(element, "updated", atomNamespace);
					String summary = getValueOfChild(objectElement, "summary", atomNamespace);
					String identifier = getValueOfChild(element, "id", atomNamespace);
					// TODO should we focus on rel="alternate" ??
					Element linkElement = element.getFirstChildElement("link", atomNamespace);
					String link = null;
					if (linkElement != null) {
						Attribute linkAttribute = linkElement.getAttribute("href");
						if (linkAttribute != null) {
							link = linkAttribute.getValue();
						}
					}
					Calendar updated = DatatypeConverter.parseDateTime(updatedStr);
					
					Element authorElement = element.getFirstChildElement("author", atomNamespace);
					String authorEmail = getValueOfChild(authorElement, "email", atomNamespace);
					
					Employee employee = getEmployeeByEmail(authorEmail);
					
					String entryTitleText = Jsoup.parse(entryTitle).text();
					
					//System.out.println("Got activity object. entryTitle: " + entryTitle + "  /// " + title + ": " + summary + " / " + updated.getTime() + " / id: " + identifier + " link: " + link);

					if (storage.wasObjectSynced(identifier) != null) {
						continue;
					}
					
					if (employee == null) {
						// we can only sync items with employee.
						continue;
					}
					
					toCreate.add(new HubEntry(identifier, employee, updated.getTime(), null, SrcType.ISSUES, entryTitleText, link, defaultPrivacy));
				} else {
					Element itemElement = element.getFirstChildElement("title", atomNamespace);
					if (itemElement != null) {
						System.out.println("did not find activity:object? " + itemElement.getValue());
					} else {
						System.out.println("Unable to find title of entry. " + element.getLocalName());
					}
				}
			}
			
			return toCreate;
		} catch (IOException | ParsingException e) {
			e.printStackTrace();
			return null;
		}
	}

	private String getValueOfChild(Element element, String localName,
			String namespace) {
		Element el = element.getFirstChildElement(localName, namespace);
		if (el != null) {
			return el.getValue();
		}
		return null;
	}

}
