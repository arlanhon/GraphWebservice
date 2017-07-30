package io.rest.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;


public class RESTClient {
	
	private static final long serialVersionUID = 1L;
	
	public enum METHOD{POST,PUT,GET};
	
	private String uri = null;
	private Map<String,String>header = null;
	private static RESTClient singleton = null;
	
	
	public RESTClient(String _uri){
		uri= _uri;
	}
		
	private WebResource webResource = null;

	public WebResource getWebResource(){
		if(webResource==null){
			ClientConfig config = new DefaultClientConfig();
			Client client = Client.create(config);
			webResource = client.resource(UriBuilder.fromUri(uri).build());
		}
		return webResource;
	}
	
	public InputStream send(METHOD method, String path) throws JsonGenerationException, JsonMappingException, IOException {
		
		webResource = getWebResource();
		webResource = StringUtils.isEmpty(path)?webResource: webResource.path(path);
		//webResource = queryParams==null?webResource: webResource.queryParams(queryParams);
		//Builder builder = webResource.accept(MediaType.APPLICATION_JSON);
		//builder.type(MediaType.APPLICATION_JSON );
			
		Builder builder = webResource.accept(MediaType.APPLICATION_JSON,MediaType.TEXT_PLAIN,MediaType.APPLICATION_OCTET_STREAM);
				
		if(getHeader()!=null){

			Set<String> headerkeys = getHeader().keySet();
								
			for(String headerkey : headerkeys){
				builder.header(headerkey, getHeader().get(headerkey));
			}
		}
		
		ClientResponse cliResponse = send(method, builder, null);
		//return cliResponse.getEntity(String.class);
		return cliResponse.getEntityInputStream();
	}
		
	public String send(METHOD method, String path, Map<String, ?> map) throws JsonGenerationException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(map);
		
		webResource = getWebResource();
		webResource = webResource.path(path);
		Builder builder = webResource.accept(MediaType.APPLICATION_JSON);
		builder.type(MediaType.APPLICATION_JSON );
			
		if(getHeader()!=null){

			Set<String> headerkeys = getHeader().keySet();
								
			for(String headerkey : headerkeys){
				builder.header(headerkey, getHeader().get(headerkey));
			}
		}
		
		ClientResponse cliResponse = send(method,builder, json);
		return cliResponse.getEntity(String.class);
	}
	
	private ClientResponse send(METHOD method, Builder builder, String payload){
		
		ClientResponse cliResponse = null;
		if(method.equals(METHOD.POST))
			cliResponse = builder.post(ClientResponse.class, payload);
		else if(method.equals(METHOD.PUT))
			cliResponse = builder.put(ClientResponse.class, payload); 
		else if(method.equals(METHOD.GET))
			cliResponse = builder.get(ClientResponse.class);
		
		return cliResponse;
	}
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * @return the header
	 */
	public Map<String,String> getHeader() {
		return header;
	}

	/**
	 * @param header the header to set
	 */
	public void setHeader(Map<String,String> header) {
		this.header = header;
	}


}

