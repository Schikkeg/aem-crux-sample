package org.open.aem;

import java.io.IOException;

public interface ChromeUXAPIService {

	public static final String HTTP_CONNECTION_REQUEST_TIMEOUT = "http.connection.request.timeout";
	public static final String HTTP_CONNECTION_TIMEOUT = "http.connection.timeout";
	public static final String HTTP_SOCKET_TIMEOUT = "http.socket.timeout";
	public static final String CRUX_REST_API_ENDPOINT = "CrUX.restapi.endpoint";
	public static final String CRUX_REST_API_KEY = "CrUX.restapi.key";
	public static final String APPLICATION_JSON = "application/json";
	public static final String CRUX_ORIGIN_DOMAIN = "CRUX.ORIGIN.DOMAIN";

	/**
	 * This method retrieves the access token to access the API
	 * 
	 * @param refreshToken the parameter if true refreshes the token
	 * @return the JSON String response with the token
	 * @throws IOException
	 */
	public String getVitalScore(String pageurl) throws IOException;
	public void clearCache();

}
