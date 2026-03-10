package in.parambir.android;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Log;

/**
 * This class provides methods for requesting the URLs 
 * of images on Flickr. It must be instantiated using a valid Flickr
 * API key (obtained from: http://www.flickr.com/services/api/).
 *
 */
public class FlickrImageLoader {

	//Local Variables
	private String m_flickrAPIKey;
	
	/**
	 * 
	 * @param flickrAPIKey A valid Flickr API key (obtained from: http://www.flickr.com/services/api/).
	 */
	FlickrImageLoader(String flickrAPIKey){
		this.m_flickrAPIKey = flickrAPIKey;
	}
    
    /**
     * Retrieves URLs for the most "interesting" images on Flickr.
     * @param thumbsPerPage The number of image urls to fetch.
     * @param pageNumber Which page of images to load. (e.g. if <code>thumbsPerPage</code>=10, 
     *   page 1 loads images 1-10 and page 2 loads images 11-20, etc.)
     * @return An array containing URLs for the requested images.
     * @throws IOException
     */
    public String[] getImagesByInterestingness(Integer thumbsPerPage, Integer pageNumber) throws IOException{
    	StringBuffer url = new StringBuffer("http://www.flickr.com/services/rest/?method=flickr.interestingness.getList");
    	url.append("&api_key=").append(m_flickrAPIKey);
    	if(thumbsPerPage != null && thumbsPerPage > 0) url.append("&per_page=").append(thumbsPerPage);
    	if(pageNumber != null && pageNumber > 0) url.append("&page=").append(pageNumber);
    	return getImageURLs(url);
    }
    
    /**
     * Sends a REST query using the Flickr API and Retrieves URLS for the images returned
     */
    private String[] getImageURLs(StringBuffer restUrl)throws IOException {

		Log.i("FlickrImageLoader", "Request to Flickr \n" + restUrl.toString());
		HttpURL httpURL = new HttpURL(restUrl.toString());
		HostConfiguration host = new HostConfiguration();
		host.setHost(httpURL.getHost(), httpURL.getPort());
		HttpConnectionManager connectionManager = new SimpleHttpConnectionManager();
		HttpConnection connection = connectionManager.getConnection(host);
		connection.open();
		GetMethod get = new GetMethod(restUrl.toString());
		get.execute(new HttpState(), connection);
		String response = get.getResponseBodyAsString();
		Log.i("FlickrImageLoader", "Request to Flickr \n" + response);
		connection.close();
		
		//Parse the returned XML string
		FlickrXMLHandler xmlHandler = new FlickrXMLHandler(response);
		String[] returnedPhotos = xmlHandler.getPhotos(); 
		
		return returnedPhotos;
	}
	
    
    /**
     * Helper class which parses XML responses from Flickr
     *
     */
    private class FlickrXMLHandler {
    	
    	Document doc = null;
    	
        FlickrXMLHandler(String xml){
        	try {
        		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        		DocumentBuilder db = dbf.newDocumentBuilder();
        		doc = db.parse(new InputSource(new StringReader(xml)));
        	} catch (IOException ioe) {
        		Log.e("FlickrXMLHandler", ioe.getMessage(), ioe);
        	} catch (ParserConfigurationException pce) {
        		Log.e("FlickrXMLHandler", pce.getMessage(), pce);
        	} catch (SAXException se) {
        		Log.e("FlickrXMLHandler", se.getMessage(), se);
        	}
        }

    	public String[] getPhotos() {
    		NodeList nl =  doc.getElementsByTagName("photo");
        	String[] mPhotos = new String[nl.getLength()];
        	for(int i=0; i < nl.getLength() ; i++){
        		Element e = (Element) nl.item(i);
        		String farm = e.getAttribute("farm");
        		String server = e.getAttribute("server");
        		String id = e.getAttribute("id");
        		String secret = e.getAttribute("secret");
        		String photoURL = "http://farm"+farm+".static.flickr.com/"+server+"/"+id+"_"+secret+"_m.jpg";
        		Log.i("FlickrXML", "Retrieved Photo URL: " + photoURL);
        		mPhotos[i]= photoURL;
        	}	
    		return mPhotos;
    	}
    }
	
	
}
