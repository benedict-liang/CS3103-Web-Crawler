import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.*;


public class Master {

	private HashSet<String> visitedHostNames = new HashSet<String>();
	private HashSet<String> unvisitedHostNames = new HashSet<String>();
	private ArrayList<URI> urlsRepository = new ArrayList<URI>();
	private Crawler[] crawlers = null;
	private String[] m_seedUrls = null;
	private int numOfCrawlers = 0;

	public Master(String[] seedUrls, int numOfCrawlers) 
			throws URISyntaxException {
		if (seedUrls.length < 1) {
			throw new IllegalArgumentException(
					"There must be at least one seed url.");
		}
		
		if (numOfCrawlers < 1) {
			throw new IllegalArgumentException(
					"Number of crawlers must be more than 0.");
		}

		this.m_seedUrls = seedUrls;
		this.numOfCrawlers = numOfCrawlers;
		initializeCrawlers();
		addUrlListToRepository(this.m_seedUrls);
	}
	
	private void addUrlListToRepository(String[] urlList) 
			throws URISyntaxException {
		for (String url : urlList) {
			addUrlToRepository(url);
		}
	}

	private void addUrlToRepository(String url) throws URISyntaxException {
		URI uri = new URI(url);
		String host = uri.getHost();
		String pageType = getPageType(uri.getRawPath());

		if (!unvisitedHostNames.contains(host) && isHTMLPageType(pageType)) {
			unvisitedHostNames.add(host);
			urlsRepository.add(uri);
		}
	}

	private boolean isHTMLPageType(String pageType) {
		return pageType == "html" || pageType == "htm" || pageType == "";
	}

	private String getPageType(String rawPath) {
		if (rawPath == null) {
			return "";
		}

		int dotIndex = rawPath.lastIndexOf('.');
		if (dotIndex != -1) {
			return rawPath.substring(dotIndex + 1);
		}
		return "";
	}

	private void initializeCrawlers() {
		crawlers = new Crawler[this.numOfCrawlers];
		
		for (int i = 0; i < crawlers.length; i++) {
			crawlers[i] = new Crawler();
		}
	}
	
	public String[] startCrawl() throws UnknownHostException, IOException,
			URISyntaxException {
		// TODO: Change to all crawlers.
		ArrayList<String> results = new ArrayList<String>();
		Crawler c = crawlers[0];
		
		while (!urlsRepository.isEmpty()) {
			if (c.isAvailable()) {
				// TODO: Change to a block of URLs
				String[] newURls = c.assignJobs(urlsRepository.toArray(new URI[0]));
				
				for (URI n : urlsRepository) {
					results.add(n.getHost());
				}
				
				// TODO: clear urlsRepository for now.
				urlsRepository.clear();
				addUrlListToRepository(newURls);
			}
		}
		return results.toArray(new String[0]);
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String[] seedUrls = {"http://en.wikipedia.org/wiki/United_States"};
		
		try {
			Master master = new Master(seedUrls, 1);
			String[] res = master.startCrawl();
			
			System.out.println(Arrays.toString(res));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
