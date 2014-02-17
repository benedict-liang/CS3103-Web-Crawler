import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This is the crawler handler for the concurrent web crawler. The following
 * are a few features of this handler:
 * - Users can set the maximum number of crawlers (threads). This provides
 * 		the user with control over how much resources to use for the crawler.
 * - Each thread request has a delay to prevent an "accidental" DOS attack.
 * - Threads are created and controlled by the ThreadPoolExecutor class. This
 * 		class acts as a pool and thread scheduler. The size of the pool is
 * 		bounded by the maximum number of threads set by the user.
 * - The crawlers terminate when encountering either conditions:
 * 		a) The crawler has reached a dead end, i.e. no links left to visit.
 * 		b) The crawler has reached the maximum number of links requested.
 * - Results are obtained and written to "results.txt" at the end of the crawl.
 * 
 * The following are some of the performance optimization techniques used. Some
 * optimizations are found in Crawler.java.
 * - Only HTML pages (links without extensions or ending with ".html"|".htm".
 * 		Reasons:		
 * 		1) As compared to other file types (.jpg|.pdf|.txt|etc), the html page
 * 		type will more likely result in more links. This is especially
 * 		important as each domain can only be visited once. (You will want to
 * 		pick the page in the domain that can yield links.)
 * 		2) Html pages will generally be smaller in size than the other mentioned
 * 		file types. This greatly reduces crawling time and does not compromise
 * 		the number of links found.
 * - Jsoup was used as opposed to pure regex for HTML parsing. Jsoup is a more
 * 		optimized parser with better results on obtaining links.
 * - The StringBuffer was used to obtain the HTML response page instead of
 * 		naive string concatenation. This cut down processing time by ~20% on
 * 		large webpages.
 * @author benedict
 *
 */
public class Master {

	private static final int REQUEST_DELAY = 2000;
	private int m_maxPagesToCrawl;
	private HashSet<String> m_seenHostNames = new HashSet<String>();
	private ArrayList<URI> m_urisRepository = new ArrayList<URI>();
	private String[] m_seedUrls = null;
	private ThreadPoolExecutor m_executorPool;
	private int m_linkCounts = 0;
	private ArrayList<String> m_results = new ArrayList<String>();

	
	/**
	 * Constructor for Master.
	 * @param seedUrls The URLs to start crawling with.
	 * @param maxPagesToCrawl The maximum number of pages to crawl.
	 * @param numOfCrawlers The maximum number of crawlers to use.
	 * @throws URISyntaxException
	 */
	public Master(String[] seedUrls, int maxPagesToCrawl, int numOfCrawlers) 
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
		this.m_maxPagesToCrawl = maxPagesToCrawl;
		this.m_executorPool = new ThreadPoolExecutor(
				numOfCrawlers,
				numOfCrawlers, 
				Long.MAX_VALUE,
				TimeUnit.SECONDS,
				new ArrayBlockingQueue<Runnable>(numOfCrawlers, true));
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

		if (!m_seenHostNames.contains(host) && isHTMLPageType(pageType)) {
			m_seenHostNames.add(host);
			m_urisRepository.add(uri);
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
		while ((m_linkCounts < m_maxPagesToCrawl) &&
				!(m_urisRepository.isEmpty() &&
						(m_executorPool.getActiveCount() == 0))) {
			if (m_urisRepository.isEmpty()) {
				continue;
			}

			try {
				Thread.sleep(REQUEST_DELAY);
			} catch (InterruptedException e) {
				System.err.println("Crawling delay interupted.");
			}
			
			URI uri = m_urisRepository.get(0);
			m_urisRepository.remove(0);
			
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
		
        System.out.println("Stopped all crawlers.");
        
        System.out.println("Writing results to file.");
        writeResultsToFile();
        System.out.println("Finished writing results to file.");
        
		return m_results.toArray(new String[0]);
	}
	
	
	private void writeResultsToFile() {
		try {
			File resultsFile = new File("results.txt");
			if(!resultsFile.exists()) {
				resultsFile.createNewFile();
			} 
			
			PrintWriter writer = new PrintWriter("results.txt");
			for (String r : m_results) {
				writer.println(r);
			}

			writer.close();
		} catch (FileNotFoundException e) {
			System.err.println("FileNotFoundException when writing results" +
					" to file.");
			return;
		} catch (IOException e) {
			System.err.println("IOException when writing results to file.");
			return;
		}
	}


	public synchronized void addCrawledLinks(String[] links, 
			String crawledHost, long RTT) {
		if (m_linkCounts >= m_maxPagesToCrawl) {
			return;
		}
		
//		addUrlListToRepository(links);
		m_results.add(crawledHost + "        " + RTT + " milliseconds");
		m_linkCounts += 1;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String[] seedUrls = {"http://en.wikipedia.org/wiki/United_States"};
		
		try {
			Master master = new Master(seedUrls, 20, 100);
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
