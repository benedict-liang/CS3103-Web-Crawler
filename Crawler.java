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
	
	/**
	 * 
	 */
	public Crawler() {

	}

	
	public String[] GetSiteLinks(String host, String requestPath) 
			throws UnknownHostException, IOException {
//		URI uri = new URI(url);
//		System.out.println(uri.getHost());
		
		Socket socket = new Socket(host, 80);
		
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
	
	public boolean isAvailable() {
		return hasJobs;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Crawler crawler = new Crawler();

		try {
			String[] links = crawler.GetSiteLinks("en.wikipedia.org", "/wiki/United_States");
			System.out.println(links);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
