package io.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import io.rest.services.QueryServiceImpl;


public class JettyServerRun
{
	
	private static String contextProp;
	private static String mainPort;

	
	static{
		InputStream resourceStream = QueryServiceImpl.class.getResourceAsStream("/properties/common.properties");
		Properties props = new Properties();
		try {
			props.load(resourceStream);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		contextProp = props.getProperty("server.contextPath","/api");
		mainPort = props.getProperty("server.port","8080");
		
	}
	
    public static void main(String args[]) throws Exception
    {

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath(contextProp);
     
        ServletHolder servlet = context.addServlet(com.sun.jersey.spi.container.servlet.ServletContainer.class, "/*");
        servlet.setInitOrder(1);
     
        servlet.setInitParameter( "com.sun.jersey.config.property.packages",SimpleRestService.class.getPackage().getName());
        
        Server server = new Server(8080);
        server.setHandler(context);
        
        
        server.start();
        server.join();
    }


}
