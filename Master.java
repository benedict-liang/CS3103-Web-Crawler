import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.*;


public class Master {

	private final int LINK_COUNT_THRESHOLD = 20;
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
	
	
	private void addUrlListToRepository(String[] urlList) {
		for (String url : urlList) {
			addUrlToRepository(url);
		}
	}

	
	private void addUrlToRepository(String url) {
		URI uri = null;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			System.err.println("URISyntaxException when adding link: " + url);
			return;
		}

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
		while ((m_linkCounts < LINK_COUNT_THRESHOLD) || 
				(urlsRepository.isEmpty() && m_executorPool.isTerminated())) {
			if (urlsRepository.isEmpty()) {
				continue;
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.err.println("Crawling delay interupted.");
			}
			
			URI uri = urlsRepository.get(0);
			urlsRepository.remove(0);
			
			if (uri != null) {
				m_executorPool.execute(new Crawler(uri, this));
			}
		}
		
		System.out.println("Found " + m_linkCounts + " links.");
		
		if (!m_executorPool.isTerminated()) {
			m_executorPool.shutdownNow(); 
        }
		
		while (!m_executorPool.isTerminated()) {
			// Wait till threads in the executor pool are stopped.
		}
		
        System.out.println("Finished all threads.");

		return m_results.toArray(new String[0]);
	}
	
	
	public synchronized void addCrawledLinks(String[] links, 
			String crawledHost) {
		if (m_linkCounts >= LINK_COUNT_THRESHOLD) {
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
