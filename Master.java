import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Master {

	private final int LINK_COUNT_THRESHOLD = 20;
	private final int REQUEST_DELAY = 2000;
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
				Thread.sleep(REQUEST_DELAY);
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
        
        writeResultsToFile();
        
		return m_results.toArray(new String[0]);
	}
	
	
	private void writeResultsToFile() {
//		PrintWriter writer;
//		try {
//			writer = new PrintWriter("the-file-name.txt", "UTF-8");
//			writer.println("The first line");
//			writer.println("The second line");
//			writer.close();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

	}


	public synchronized void addCrawledLinks(String[] links, 
			String crawledHost, long RTT) {
		if (m_linkCounts >= LINK_COUNT_THRESHOLD) {
			return;
		}
		
		addUrlListToRepository(links);
		m_results.add(crawledHost + "        " + RTT + " milliseconds");
		m_linkCounts += 1;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String[] seedUrls = {"http://en.wikipedia.org/wiki/United_States"};
		
		try {
			Master master = new Master(seedUrls, 100);
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
