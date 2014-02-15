import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;


public class Master {

	private HashSet<String> visitedHostNames = new HashSet<String>();
	private HashMap<String, String> urlsRepository = 
			new HashMap<String, String>();
	private Crawler[] crawlers = null;
	private String[] seedUrls = null;
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

		this.seedUrls = seedUrls;
		this.numOfCrawlers = numOfCrawlers;
		initializeCrawlers();
		addSeedUrlsToRepository();
	}
	
	private void addSeedUrlsToRepository() throws URISyntaxException {
		for (String url : seedUrls) {
			addUrlToRepository(url);
		}
	}

	private void addUrlToRepository(String url) throws URISyntaxException {
		URI uri = new URI(url);
		String host = uri.getHost();
		String requestPath = uri.getRawPath();

		if (!urlsRepository.containsKey(host)) {
			urlsRepository.put(host, requestPath);
		}
	}

	private void initializeCrawlers() {
		crawlers = new Crawler[this.numOfCrawlers];
		
		for (int i = 0; i < crawlers.length; i++) {
			crawlers[i] = new Crawler();
		}
	}
	
	public String[] StartCrawl() {
		// TODO: Change to all crawlers.
		Crawler c = crawlers[0];
		return null;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String[] seedUrls = {"http://www.google.com"};
		
	}

}
