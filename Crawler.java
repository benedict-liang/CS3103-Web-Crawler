import java.util.*;
import java.io.*;
import java.net.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

public class Crawler {
	
	private String getRequestString = "GET %s HTTP/1.1\r\nHost: %s\r\n" +
			"Connection: close\r\n\r\n";
	private String[] urls = null;
	boolean hasJobs = false;
	

	public Crawler() {

	}

	
	public String[] getSiteLinks(String host, String requestPath, int port) 
			throws UnknownHostException, IOException {
		
		Socket socket = new Socket(host, port);
		
		PrintWriter request = new PrintWriter(socket.getOutputStream());
		String formattedGetRequestString = String.format(this.getRequestString,
				requestPath, host);

		request.print(formattedGetRequestString);
		
		request.flush();
		
		InputStream inStream = socket.getInputStream();
		BufferedReader rd = new BufferedReader(
		        new InputStreamReader(inStream));
		String line, html = "";
		while ((line = rd.readLine()) != null) {
		    html += "\n" + line;
		}
		
		Document doc = Jsoup.parse(html);
		Elements links = doc.select("a[href]");
		
		ArrayList<String> absLinks = new ArrayList<String>();
		
		for (Element l : links) {
			String link = l.absUrl("href");
			if (link != "") {
				absLinks.add(link);
			}
		}

		return absLinks.toArray(new String[0]);
	}
	
	public String[] assignJobs(URI[] uris) throws UnknownHostException,
			IOException {
		HashSet<String> resultURLs = new HashSet<String>();

		for (URI uri : uris) {
			String[] links = getSiteLinks(uri.getHost(), uri.getRawPath(),
					getPort(uri));
			
			for (String link : links) {
				if (!resultURLs.contains(link)) {
					resultURLs.add(link);
				}
			}
		}
		
		return resultURLs.toArray(new String[0]);
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
	
	public boolean isAvailable() {
		return hasJobs;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Crawler crawler = new Crawler();
	}

}
