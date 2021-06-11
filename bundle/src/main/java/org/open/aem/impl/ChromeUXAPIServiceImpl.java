package org.open.aem.impl;


import java.io.IOException;
import java.util.Dictionary;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.Consts;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.commons.json.JSONArray;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import org.apache.sling.commons.osgi.OsgiUtil;

import org.open.aem.ChromeUXAPIService;

/**
 * 
 * This service provides methods to call crux REST API.
 * 
 * @author shassanchi
 */
@Service
@Component(name = "org.open.aem.impl.ChromeUXAPIServiceImpl", metatype = true, immediate = true, specVersion = "1.1", label = "Web Vital API Service", description = "Service that provides methods to get performance stat.")
@Properties({
		@Property(name = Constants.SERVICE_DESCRIPTION, value = "Service that provides methods to call page insight API."),
		@Property(label = "CrUX Rest API Endpoint", name = ChromeUXAPIService.CRUX_REST_API_ENDPOINT, value = "https://chromeuxreport.googleapis.com/v1/records:queryRecord?key="),
		@Property(label = "CrUX Rest API Key", name = ChromeUXAPIService.CRUX_REST_API_KEY, value = "BIzaSyBtaObXNvbKHI751eb7SRLOsK2UazFrk8U"),
		@Property(label = "HTTP Connection Request Timeout", name = ChromeUXAPIService.HTTP_CONNECTION_REQUEST_TIMEOUT, longValue = 5000),
		@Property(label = "HTTP Connection Timeout", name = ChromeUXAPIService.HTTP_CONNECTION_TIMEOUT, longValue = 5000),
		@Property(label = "HTTP Socket Timeout", name = ChromeUXAPIService.HTTP_SOCKET_TIMEOUT, longValue = 5000),
		@Property(label = "List of Origin domain to compare", name = ChromeUXAPIService.CRUX_ORIGIN_DOMAIN,   cardinality = 100,   description = "List of domain to compare.",  value = { "https://www.paloaltonetworks.com/","https://www.apple.com/","https://www.google.com/"}) 
 	})
public class ChromeUXAPIServiceImpl implements ChromeUXAPIService {

	private static final Logger log = LoggerFactory.getLogger(ChromeUXAPIServiceImpl.class);

	private ComponentContext componentContext;
	private String cruxRestApiEndPoint;
	private String cruxRestApiKey;
	private long cacheExpirationTime;
	private int connectionRequestTimeout;
	private int connectionTimeout;
	private int socketTimeout;
	private static Map map ;
	private static String[] originDomainsArray;
	private static String allOriginStore;


	public void clearCache() {
		if(null != map){
			map.clear();
		}
	}


	/**
	 * This method retrieves the access token to access the Search REST API
	 * 
	 * @param refreshToken the parameter if true refreshes the token
	 * @return the JSON String response with the token
	 * @throws IOException
	 */
	public String getVitalScore(String pageurl) throws IOException {
		log.debug("Entering getVitalScore() method");
		long startTime = System.currentTimeMillis();
		log.debug("startTime: {}", startTime);
		String views = "";
		if(null != map){
			if(map.get(pageurl)!=null){
				views = map.get(pageurl).toString() ;
			}else{
				views = "{"+fetchView(pageurl,"DESKTOP","all")+","+fetchView(pageurl,"PHONE","all")+","+fetchView(pageurl,"TABLET","all")+","+fetchView(pageurl,"PHONE","3G")+","+fetchView(pageurl,"PHONE","4G")+fetchAllOrigin()+"}";
				map.put(pageurl, views);
			}
		}else{
			map  = new HashMap<String, String>();
			//views =  "{"+fetchView(pageurl,"DESKTOP")+","+fetchView(pageurl,"PHONE")+"}";
			views = "{"+fetchView(pageurl,"DESKTOP","all")+","+fetchView(pageurl,"PHONE","all")+","+fetchView(pageurl,"TABLET","all")+","+fetchView(pageurl,"PHONE","3G")+","+fetchView(pageurl,"PHONE","4G")+fetchAllOrigin()+"}";
			map.put(pageurl, views);
		}
		log.debug("Time taken by getYoutubeView() method: {} ms", System.currentTimeMillis() - startTime);
		log.debug("Exiting getVitalScore() method");

		return views;
	}

	private String fetchView(String pageurl, String formfactor,String effectiveConnectionType) throws IOException{
		//LCP:2.929 FID:0.135 CLS:0.06
		String output = "";
		CloseableHttpClient client = null;
		CloseableHttpResponse response = null;
		String jsonResponse = null;
		try {
				log.debug("Fetching webvital score for "+pageurl);
				client = HttpClientBuilder.create().build();
				String ep = this.cruxRestApiEndPoint + this.cruxRestApiKey;
				HttpPost httpPost = new HttpPost( ep);
				log.debug("End point url is "+ep);				
				RequestConfig requestConfig = RequestConfig.custom()
						.setConnectionRequestTimeout(connectionRequestTimeout).setConnectTimeout(connectionTimeout)
						.setSocketTimeout(socketTimeout).build();
				httpPost.setConfig(requestConfig);
				//httpPost.addHeader("Content-Type", "application/json");
				//httpPost.addHeader("Accept", "application/json");
				String reqBody = "";
				if(effectiveConnectionType.equals("all")){
					 output = "'"+ formfactor +"':{";
					reqBody = "{'url': '"+pageurl+"', 'formFactor':'"+formfactor+"','metrics': [ 'first_input_delay', 'largest_contentful_paint',  'cumulative_layout_shift']}";
				}else{
					 output = "'"+ formfactor +effectiveConnectionType+"':{";
					reqBody = "{'url': '"+pageurl+"', 'formFactor':'"+formfactor+"','effectiveConnectionType':'"+effectiveConnectionType+"','metrics': [ 'first_input_delay', 'largest_contentful_paint',  'cumulative_layout_shift']}";				
				}
				StringEntity postingString = new StringEntity(reqBody,ContentType.APPLICATION_FORM_URLENCODED);
				httpPost.setEntity(postingString);
				httpPost.setHeader("Accept", "application/json");
    			httpPost.setHeader("Content-type", "application/json");
				response = client.execute(httpPost);				
				int statusCode = response.getStatusLine().getStatusCode();
				log.debug("Status Code: {}", statusCode);
				if (statusCode == HttpStatus.SC_OK) {
					HttpEntity entity = response.getEntity();
					if (null != entity) {
						jsonResponse = EntityUtils.toString(entity, Consts.UTF_8);
					}
				}
				log.debug("JSON Response: {}", jsonResponse);
				if(jsonResponse!=null){
					JSONObject jsonResponseObj = new JSONObject(jsonResponse);
					final JSONObject jRecord = jsonResponseObj.getJSONObject("record");
				
     			 	if (jRecord != null){
     			 	final JSONObject jMetrics = jRecord.getJSONObject("metrics");
     			 	if (jMetrics != null){
     			 		
     			 		if ( jMetrics.has("cumulative_layout_shift")){
     			 			final JSONObject jCLS = jMetrics.getJSONObject("cumulative_layout_shift");
     			 			final JSONObject jCLSpercent = jCLS.getJSONObject("percentiles");
     			 			if(jCLSpercent!=null){
     			 			    double pCls = jCLSpercent.getDouble("p75");
     			 				String clss = "";
     			 				if(pCls<0.1){
     			 					clss = "pass";
     			 				}else if(pCls<0.25){
     			 					clss = "average";
     			 				}else if(pCls>=0.25){
     			 					clss = "fail";
     			 				}
     			 				output = output + "'CLS':'"+pCls+"',";
     			 				output = output + "'CLSSTYLE':'"+clss+"',";
     			 			}else{
     			 				output = output + "'CLS':'N/A',";
     			 				output = output + "'CLSSTYLE':'black',";
     			 			}
     			 		}else{
     			 			//"CLS": "",
     			 			output = output + "'CLS':'N/A',";
     			 			output = output + "'CLSSTYLE':'black',";
     			 		}
     			 		
     			 		if (jMetrics.has("first_input_delay")){
     			 			final JSONObject jFID = jMetrics.getJSONObject("first_input_delay");
     			 			final JSONObject jFIDpercent = jFID.getJSONObject("percentiles");
     			 			if(jFIDpercent!=null){
     			 				double pFid = jFIDpercent.getDouble("p75")/1000;
     			 				String fids = "";
     			 				if(pFid<100){
     			 					fids = "pass";
     			 				}else if(pFid<300){
     			 					fids = "average";
     			 				}else if(pFid>=300){
     			 					fids = "fail";
     			 				}
     			 				output = output + "'FID':'"+pFid+"',";
     			 				output = output + "'FIDSTYLE':'"+fids+"',";
     			 				
     			 			}else{
     			 				output = output + "'FID':'N/A',";
     			 				output = output + "'FIDSTYLE':'black',";
     			 			}
     			 		}else{
     			 			//"CLS": "",
     			 			output = output + "'FID':'N/A',";
     			 			output = output + "'FIDTYLE':'black',";
     			 		}
     			 		
     			 		if ( jMetrics.has("largest_contentful_paint")){
     			 			final JSONObject jLCP = jMetrics.getJSONObject("largest_contentful_paint");
     			 			final JSONObject jLCPpercent = jLCP.getJSONObject("percentiles");
     			 			if(jLCPpercent!=null){
	     			 			double pLcp = jLCPpercent.getDouble("p75")/1000;
     			 				 String lcps = "";
     			 				if(pLcp<2.5){
     			 					lcps = "pass";
     			 				}else if(pLcp<4){
     			 					lcps = "average";
     			 				}else if(pLcp>=4){
     			 					lcps = "fail";
     			 				}
     			 				output = output + "'LCP':'"+pLcp+"',";
     			 				output = output + "'LCPTYLE':'"+lcps+"',";
     			 			}else{
	     			 			output = output + "'LCP':'N/A',";
    	 			 			output = output + "'LCPTYLE':'black',";
     				 		}
     			 		}else{
     			 			//"CLS": "",
     			 			output = output + "'LCP':'N/A',";
     			 			output = output + "'LCPSTYLE':'black',";
     			 		}
     			 	}
     			 	if(output.equals(formfactor +"==>")){
						output = output + " NO DATA FOUND" ;		 	
     			 	}
					log.debug("Output is : {}", output);
				}
				}
		 } catch (JSONException e) {
			log.error("JSONException occured", e);
			throw new IOException(e);
		 }
		 if(output.endsWith(",")){
		 	output = output.substring(0,output.length()-1);
		 }
		 output =  output +"}";

		 return output ;
	}


	private String fetchAllOrigin(){
		log.debug("fetchAllOrigin");
		String output = ",'ORIGINS':[";
		if(allOriginStore!=null ){
			output =  allOriginStore;
		
		}else{
		
		try {
			String originValues = "";
			for (int i = 0; i < originDomainsArray.length; i++) {
				log.debug("fetchAllOrigin array element {}", originDomainsArray[i]);
				if(i>0){
					originValues = originValues + ",";
				}
				originValues = originValues + "{ 'name':'"+originDomainsArray[i].substring(originDomainsArray[i].indexOf("/")+2)+"'";
				originValues = originValues + ","+ fetchOriginView(originDomainsArray[i],"DESKTOP","all")+ ","+ fetchOriginView(originDomainsArray[i],"PHONE","all")+ ","+ fetchOriginView(originDomainsArray[i],"TABLET","all")+ ","+ fetchOriginView(originDomainsArray[i],"PHONE","3G")+ ","+ fetchOriginView(originDomainsArray[i],"PHONE","4G");
				//originValues = originValues + ","+ fetchOriginView(originDomainsArray[i],"PHONE")+ ","+ fetchOriginView(originDomainsArray[i],"DESKTOP");
				originValues = originValues + "}";
				log.debug("fetchAllOrigin originValues is {}", originValues);
			}
			output = output + originValues;
		}catch(Exception e){
			log.error("JSONException fetchAllOrigin occured", e);
		}
		output = output + "]";
		allOriginStore = output;
		}
		return output;
	}

	private String fetchOriginView(String origin, String formfactor,String effectiveConnectionType) throws IOException{
		//LCP:2.929 FID:0.135 CLS:0.06
		String output = "";
		CloseableHttpClient client = null;
		CloseableHttpResponse response = null;
		String jsonResponse = null;
		try {
				log.debug("Fetching webvital origin score for "+origin);
				client = HttpClientBuilder.create().build();
				String ep = this.cruxRestApiEndPoint + this.cruxRestApiKey;
				HttpPost httpPost = new HttpPost( ep);
				log.debug("End point url is "+ep);				
				RequestConfig requestConfig = RequestConfig.custom()
						.setConnectionRequestTimeout(connectionRequestTimeout).setConnectTimeout(connectionTimeout)
						.setSocketTimeout(socketTimeout).build();
				httpPost.setConfig(requestConfig);
				//httpPost.addHeader("Content-Type", "application/json");
				//httpPost.addHeader("Accept", "application/json");
				String reqBody = "";

				if(effectiveConnectionType.equals("all")){
					 output = "'"+ formfactor +"':{";
					reqBody = "{'origin': '"+origin+"', 'formFactor':'"+formfactor+"','metrics': [ 'first_input_delay', 'largest_contentful_paint',  'cumulative_layout_shift']}";
				}else{
					 output = "'"+ formfactor +effectiveConnectionType+"':{";
					reqBody = "{'origin': '"+origin+"', 'formFactor':'"+formfactor+"','effectiveConnectionType':'"+effectiveConnectionType+"','metrics': [ 'first_input_delay', 'largest_contentful_paint',  'cumulative_layout_shift']}";				
				}

				//output = "'"+ formfactor +"':{";
				//reqBody = "{'origin': '"+origin+"', 'formFactor':'"+formfactor+"','metrics': [ 'first_input_delay', 'largest_contentful_paint',  'cumulative_layout_shift']}";
				StringEntity postingString = new StringEntity(reqBody,ContentType.APPLICATION_FORM_URLENCODED);
				httpPost.setEntity(postingString);
				httpPost.setHeader("Accept", "application/json");
    			httpPost.setHeader("Content-type", "application/json");
				response = client.execute(httpPost);				
				int statusCode = response.getStatusLine().getStatusCode();
				log.debug("Status Code: {}", statusCode);
				if (statusCode == HttpStatus.SC_OK) {
					HttpEntity entity = response.getEntity();
					if (null != entity) {
						jsonResponse = EntityUtils.toString(entity, Consts.UTF_8);
					}
				}
				log.debug("JSON Response: {}", jsonResponse);
				if(jsonResponse!=null){
					JSONObject jsonResponseObj = new JSONObject(jsonResponse);
					final JSONObject jRecord = jsonResponseObj.getJSONObject("record");
				
     			 	if (jRecord != null){
     			 	final JSONObject jMetrics = jRecord.getJSONObject("metrics");
     			 	if (jMetrics != null){
     			 		
     			 		if ( jMetrics.has("cumulative_layout_shift")){
     			 			final JSONObject jCLS = jMetrics.getJSONObject("cumulative_layout_shift");
     			 			final JSONObject jCLSpercent = jCLS.getJSONObject("percentiles");
     			 			if(jCLSpercent!=null){
     			 			    double pCls = jCLSpercent.getDouble("p75");
     			 				String clss = "";
     			 				if(pCls<0.1){
     			 					clss = "pass";
     			 				}else if(pCls<0.25){
     			 					clss = "average";
     			 				}else if(pCls>=0.25){
     			 					clss = "fail";
     			 				}
     			 				output = output + "'CLS':'"+pCls+"',";
     			 				output = output + "'CLSSTYLE':'"+clss+"',";
     			 			}else{
     			 				output = output + "'CLS':'N/A',";
     			 				output = output + "'CLSSTYLE':'black',";
     			 			}
     			 		}else{
     			 			//"CLS": "",
     			 			output = output + "'CLS':'N/A',";
     			 			output = output + "'CLSSTYLE':'black',";
     			 		}
     			 		
     			 		if (jMetrics.has("first_input_delay")){
     			 			final JSONObject jFID = jMetrics.getJSONObject("first_input_delay");
     			 			final JSONObject jFIDpercent = jFID.getJSONObject("percentiles");
     			 			if(jFIDpercent!=null){
     			 				double pFid = jFIDpercent.getDouble("p75")/1000;
     			 				String fids = "";
     			 				if(pFid<100){
     			 					fids = "pass";
     			 				}else if(pFid<300){
     			 					fids = "average";
     			 				}else if(pFid>=300){
     			 					fids = "fail";
     			 				}
     			 				output = output + "'FID':'"+pFid+"',";
     			 				output = output + "'FIDSTYLE':'"+fids+"',";
     			 				
     			 			}else{
     			 				output = output + "'FID':'N/A',";
     			 				output = output + "'FIDSTYLE':'black',";
     			 			}
     			 		}else{
     			 			//"CLS": "",
     			 			output = output + "'FID':'N/A',";
     			 			output = output + "'FIDTYLE':'black',";
     			 		}
     			 		
     			 		if ( jMetrics.has("largest_contentful_paint")){
     			 			final JSONObject jLCP = jMetrics.getJSONObject("largest_contentful_paint");
     			 			final JSONObject jLCPpercent = jLCP.getJSONObject("percentiles");
     			 			if(jLCPpercent!=null){
	     			 			double pLcp = jLCPpercent.getDouble("p75")/1000;
     			 				 String lcps = "";
     			 				if(pLcp<2.5){
     			 					lcps = "pass";
     			 				}else if(pLcp<4){
     			 					lcps = "average";
     			 				}else if(pLcp>=4){
     			 					lcps = "fail";
     			 				}
     			 				output = output + "'LCP':'"+pLcp+"',";
     			 				output = output + "'LCPTYLE':'"+lcps+"',";
     			 			}else{
	     			 			output = output + "'LCP':'N/A',";
    	 			 			output = output + "'LCPTYLE':'black',";
     				 		}
     			 		}else{
     			 			//"CLS": "",
     			 			output = output + "'LCP':'N/A',";
     			 			output = output + "'LCPSTYLE':'black',";
     			 		}
     			 	}
     			 	if(output.equals(formfactor +"==>")){
						output = output + " NO DATA FOUND" ;		 	
     			 	}
					log.debug("Output is : {}", output);
				}
				}
		 } catch (JSONException e) {
			log.error("JSONException occured", e);
			throw new IOException(e);
		 }
		 if(output.endsWith(",")){
		 	output = output.substring(0,output.length()-1);
		 }
		 output =  output +"}";

		 return output ;
	}

	/**
	 * This method retrieves the configuration properties for this Service.
	 * 
	 * @param componentContext
	 */
	public void activate(ComponentContext componentContext) {
		log.info("Entering activate method.");
		this.componentContext = componentContext;
		Dictionary properties = this.componentContext.getProperties();
		this.cruxRestApiEndPoint = (String) properties.get(ChromeUXAPIService.CRUX_REST_API_ENDPOINT);
		this.cruxRestApiKey = (String) properties.get(ChromeUXAPIService.CRUX_REST_API_KEY);
		this.connectionRequestTimeout = Integer.valueOf(properties.get(HTTP_CONNECTION_REQUEST_TIMEOUT).toString());
		this.connectionTimeout = Integer.valueOf(properties.get(HTTP_CONNECTION_TIMEOUT).toString());
		this.socketTimeout = Integer.valueOf(properties.get(HTTP_SOCKET_TIMEOUT).toString());
		this.originDomainsArray = OsgiUtil.toStringArray(properties.get(ChromeUXAPIService.CRUX_ORIGIN_DOMAIN)); 
		log.debug("cruxRestApiEndPoint = {}", this.cruxRestApiEndPoint);
		log.debug("cruxRestApiKey = {}", this.cruxRestApiKey);
		log.debug("connectionRequestTimeout = {}", this.connectionRequestTimeout);
		log.debug("connectionTimeout = {}", this.connectionTimeout);
		log.debug("socketTimeout = {}", this.socketTimeout);
		log.debug("originDomainsArray = {}", this.originDomainsArray);
		log.info("Exiting activate method.");
	}

}