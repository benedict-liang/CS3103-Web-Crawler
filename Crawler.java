import java.util.*;
import java.util.concurrent.ExecutorService;
import java.io.*;
import java.net.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

public class Crawler implements Runnable {
	
	private final String GET_REQUEST_STRING = "GET %s HTTP/1.1\r\nHost: %s" +
			"\r\nConnection: close\r\n\r\n";
	private URI m_uri;
	private Master m_master;


	public Crawler(URI uri, Master master) {
		this.m_uri = uri;
		this.m_master = master;
	}

	
	private String[] getSiteLinks(String host, String requestPath, int port) 
			throws UnknownHostException, IOException {
		
		if (port == -1) {
			throw new PortUnreachableException("Invalid port.");
		}
		
		Socket socket = new Socket(host, port);
		performGETRequest(host, requestPath, socket);

		ArrayList<String> absLinks = getLinksFromHTMLPage(socket);

		return absLinks.toArray(new String[0]);
	}


	private ArrayList<String> getLinksFromHTMLPage(Socket socket)
			throws IOException {
		String html = readDocumentStringFromSocketBuffer(socket);
		
		Document doc = Jsoup.parse(html);
		Elements links = doc.select("a[href]");
		
		return getLinksFromAnchorTags(links);
	}


	private ArrayList<String> getLinksFromAnchorTags(Elements links) {
		ArrayList<String> absLinks = new ArrayList<String>();
		
		for (Element l : links) {
			String link = l.absUrl("href");
			if (link != "") {
				absLinks.add(link);
			}
		}
		return absLinks;
	}


	private String readDocumentStringFromSocketBuffer(Socket socket)
			throws IOException {
		InputStream inputStream = socket.getInputStream();
		BufferedReader rd = new BufferedReader(
		        new InputStreamReader(inputStream));
		String line, html = "";
		while ((line = rd.readLine()) != null) {
		    html += "\n" + line;
		}
		return html;
	}


	private void performGETRequest(String host, String requestPath,
			Socket socket) throws IOException {
		PrintWriter request = new PrintWriter(socket.getOutputStream());
		String formattedGetRequestString = String.format(GET_REQUEST_STRING,
				requestPath, host);

		request.print(formattedGetRequestString);
		
		request.flush();
	}

	
	private int getPort(URI uri) {
		int port = uri.getPort( );
		String protocol = uri.getScheme( ); 
		if (port == -1) {
		    if (protocol.equals("http")) { 
		        return 80;
		    }
		    else if (protocol.equals("https")) {
		        return 443;
		    }
		}
		
		return -1;
	}
	
	@Override
	public void run() {
		if (m_uri == null) {
			return;
		}
		System.out.println("running");
		String[] links = null;

		try {
			links = getSiteLinks(m_uri.getHost(), m_uri.getRawPath(),
					getPort(m_uri));
		} catch (UnknownHostException e) {
			System.err.println("UnknownHostException during GET request: " + 
					m_uri.toString());
			return;
		} catch (PortUnreachableException e) {
			System.err.println("PortUnreachableException during GET request: " +
					m_uri.toString());
			return;
		} catch (IOException e) {
			System.err.println("IOException during GET request: " +
					m_uri.toString());
			return;
		}
		
		if (links != null) {
			m_master.addCrawledLinks(links, m_uri.getHost());
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
