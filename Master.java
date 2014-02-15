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

		if (!unvisitedHostNames.contains(host)) {
			unvisitedHostNames.add(host);
			urlsRepository.add(uri);
		}
	}

	private void initializeCrawlers() {
		crawlers = new Crawler[this.numOfCrawlers];
		
		for (int i = 0; i < crawlers.length; i++) {
			crawlers[i] = new Crawler();
		}
	}
	
	public String[] startCrawl() throws UnknownHostException, IOException {
		// TODO: Change to all crawlers.
		String[] results = null;
		Crawler c = crawlers[0];
		if (!c.hasJobs) {
			results = c.assignJobs(urlsRepository.toArray(new URI[0]));
		}
		return results;
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
