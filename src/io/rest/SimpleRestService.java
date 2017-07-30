package io.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;

import io.rest.services.QueryService;
import io.rest.services.QueryServiceImpl;
 
@Path("/")
public class SimpleRestService {
	
	static QueryService query = null;
	static{
		query = new QueryServiceImpl();
	}
	
	@GET
	@Path("/friends")
	@Produces(MediaType.APPLICATION_JSON)
	public Response verifyRESTService(@QueryParam("id")String friendId)  {
		String result = null;
 
		if(StringUtils.isEmpty(friendId)){
			return Response.status(400).entity("'id' is a required parameter").build();
		}
		
		try {
			result = query.getFriends(friendId);
		} catch (Exception e) {
			return Response.status(400).entity(e.getMessage()).build();
		}

		return Response.status(200).entity(result).build();
	}
	


	
 
}