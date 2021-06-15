package org.open.aem.servlets;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.Map;

import javax.jcr.Node;
import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.open.aem.ChromeUXAPIService;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.io.JSONWriter;
import java.io.IOException;

/**
 * Scheduler &  servlet to get web vital score.
 * 
 *
 */
@Component(name = "org.open.aem.servlets.WebVitalServletScheduler", immediate = true, metatype = true, label = "Web Vital Score for live pages", description = "Get Web Vital Score from Chrome User Experience Report", policy = ConfigurationPolicy.REQUIRE)
@Service
@Properties({ @Property(name = "service.description", value = "service for geting Web Vital Score for live pages"),
		@Property(name = "sling.servlet.paths", value = "/apps/public/webvital", propertyPrivate = true),
		@Property(name = "sling.auth.requirements", value = "-/apps/public/webvital", propertyPrivate = true),
		@Property(name = "sling.servlet.methods", value = "GET", propertyPrivate = true),
		@Property(name = "scheduler.expression", value = "0 0 10 ? * *", label = "Scheduler Expression"),
		@Property(name = "append.html",  label = "Append HTML extension?", boolValue = false),
		@Property(name = "service.vendor", value = "Palo Alto Networks", propertyPrivate = true),
		@Property(name = "path.domain", label = "List of  path domain Mapping",  cardinality = 100,   description = "list of path domain Mapping to rewrite URL.",  value = { "/content/sample/en_US=https://www.sample.com" }) 
		})

public class WebVitalServletScheduler extends SlingAllMethodsServlet implements Runnable {

	private static String schedulerexpression;
	// Fire at 10:00am on every day
	private static final String DEFAULT_SCHEDULER_EXPRESSION = "0 0 10 ? * *"; 

	public static final String SUCCESS = "OK";
	public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
	
	public static final String JSON_CONTENTTYPE = "application/json";
	public static final String UTF_ENCODING = "UTF-8";
	
	private static final Logger logger = LoggerFactory.getLogger(WebVitalServletScheduler.class);

    private Map<String, String> mapPathDomain = new HashMap<String, String>();
    
    private boolean appendHTML = Boolean.FALSE;

	/* OSGi Service References */
	@Reference
	private ChromeUXAPIService chromeUXAPIService;

	public WebVitalServletScheduler() {
	}

	@Activate
	public final void activate(ComponentContext context) {
		schedulerexpression = PropertiesUtil.toString(context.getProperties().get("scheduler.expression"),
				DEFAULT_SCHEDULER_EXPRESSION);
        String[] pathDomainsArray = OsgiUtil.toStringArray(context.getProperties().get("path.domain")); 
        if (null != pathDomainsArray) {
				for (String path : pathDomainsArray) {
					String[] kv = path.trim().split("=");
    				if (kv.length != 2) {
    					continue;
					}
    				this.mapPathDomain.put(kv[0], kv[1]);				
					logger.debug("mapPathDomain : {}", path);
				}
		}
		appendHTML = PropertiesUtil.toBoolean(context.getProperties().get("append.html"),false);
	}

	@Override
	public void run() {
		long stTime = System.currentTimeMillis();
		logger.debug("run method started");
		chromeUXAPIService.clearCache();
		logger.debug("time taken to complete run method : {} ms", (System.currentTimeMillis() - stTime));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.sling.api.servlets.SlingSafeMethodsServlet#doGet(org.apache.
	 * sling.api.SlingHttpServletRequest,
	 * org.apache.sling.api.SlingHttpServletResponse)
	 */
	protected final void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServletException, IOException {
		logger.info("doGet called");

		// request.getUserPrincipal() != null means authenticated
		if (request.getRequestParameter("refresh") != null && request.getRequestParameter("refresh").toString().equals("true") && request.getUserPrincipal() != null) {
			run();
		} 
		
		if (request.getRequestParameter("path") != null && request.getUserPrincipal() != null) {
			logger.info("external execution by {}", request.getUserPrincipal());
			String rp = request.getRequestParameter("path").toString();
			String resourcePath = getDomainURL(rp);
			sendStatusResponse( response, "200", chromeUXAPIService.getVitalScore(resourcePath));
		} 
		logger.info("doGet completed");
	}

    private String getDomainURL(String input) {
    	logger.debug("getDomainURL input ===>"+input);
		String path = input ;

        if(input!=null && input.startsWith("/content")){
        	for (Map.Entry<String,String> entry : mapPathDomain.entrySet())  {
        		String key = entry.getKey();
        		String value = entry.getValue();        		
            	logger.debug ("Key = " + key+ ", Value = " +value ); 
            	if(input.startsWith(key)){
            		path = input.replace(key,value);
            		break;
            	}	
        	}
	    }
	    
	    if(appendHTML){
	    	if(path!=null && !path.endsWith(".html")){
	    		path = path + ".html";
	    	}
	    }
	    
	    logger.debug("getDomainURL output ===>"+path);
        return path;
    } 

	private void sendStatusResponse(SlingHttpServletResponse response, String errorCode, String message)
			throws ServletException, IOException {
		try {
			response.setContentType(JSON_CONTENTTYPE);
			response.setCharacterEncoding(UTF_ENCODING);
			response.setHeader("Dispatcher", "no-cache");
			response.setStatus(SlingHttpServletResponse.SC_OK);
			JSONWriter writer = new JSONWriter(response.getWriter());
			writer.object();
			writer.key("statuscode").value(errorCode);
			if (!SUCCESS.equals(errorCode)) {
				writer.key("success").value(false);
			} else {
				writer.key("success").value(true);
			}
			writer.key("statusmessage").value(message);
			writer.endObject();
		} catch (JSONException e) {
			logger.error("Exception in sendStatusResponse method", e);
			ServletException ex = new ServletException("Error while streaming the JSON.");
			ex.initCause(e);
			throw ex;
		}
	}
}
