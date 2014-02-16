import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Master {

	private HashSet<String> visitedHostNames = new HashSet<String>();
	private HashSet<String> unvisitedHostNames = new HashSet<String>();
	private ArrayList<URI> urlsRepository = new ArrayList<URI>();
	private String[] m_seedUrls = null;
	private ExecutorService m_executorPool;
	private int m_linkCounts = 0;
	private ArrayList<String> m_results = new ArrayList<String>();

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
		this.m_executorPool = Executors.newFixedThreadPool(numOfCrawlers);
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
	
	public String[] startCrawl() throws UnknownHostException, IOException,
			URISyntaxException {
		// TODO: Change to all crawlers.
//		ArrayList<String> results = new ArrayList<String>();
//		Crawler c = crawlers[0];
//		
//		while (!urlsRepository.isEmpty()) {
//			if (c.isAvailable()) {
//				// TODO: Change to a block of URLs
//				String[] newURls = c.assignJobs(urlsRepository.toArray(new URI[0]));
//				
//				for (URI n : urlsRepository) {
//					results.add(n.getHost());
//				}
//				
//				// TODO: clear urlsRepository for now.
//				urlsRepository.clear();
//				addUrlListToRepository(newURls);
//			}
//		}
//		return results.toArray(new String[0]);
		
//		ArrayList<String> results = new ArrayList<String>();
		
		while (m_linkCounts < 150) {
			System.out.println("Current linkcounts: " + m_linkCounts);
			if (urlsRepository.isEmpty()) {
				continue;
			}
			URI uri = urlsRepository.get(0);
			urlsRepository.remove(0);
			m_executorPool.execute(new Crawler(uri, this));
//			results.add(uri.getHost());
		}
		
		if (!m_executorPool.isTerminated()) {
			m_executorPool.shutdownNow(); 
        }
		
		while (!m_executorPool.isTerminated()) {
			
		}
		
        System.out.println("\nFinished all threads");
		
//		while ((!m_executorPool.isTerminated()) && (!urlsRepository.isEmpty() || m_linkCounts < 1500)) {
//			URI uri = urlsRepository.get(0);
//			urlsRepository.remove(0);
//			m_executorPool.execute(new Crawler(uri, this));
//			results.add(uri.getHost());
//		}
		
		
		return m_results.toArray(new String[0]);
	}
	
	
	public synchronized void addCrawledLinks(String[] links, String crawledHost)
			throws URISyntaxException {
		if (m_linkCounts >= 150) {
			return;
		}
		
		addUrlListToRepository(links);
		m_results.add(crawledHost);
		m_linkCounts += 1;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String[] seedUrls = {"http://en.wikipedia.org/wiki/United_States"};
		
		try {
			Master master = new Master(seedUrls, 64);
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
