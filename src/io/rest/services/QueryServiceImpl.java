package io.rest.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.json.JSONException;
import org.json.JSONObject;


public class QueryServiceImpl implements QueryService{

	private TinkerGraph graph = null;
	private Map<String,Integer> keys = null;
	private String dataservice = null;
	
	{
		
		InputStream resourceStream = QueryServiceImpl.class.getResourceAsStream("/properties/common.properties");
		Properties props = new Properties();
		try {
			props.load(resourceStream);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		 dataservice = props.getProperty("backend.data.service");		
		 graph = TinkerGraph.open(); 
		 keys = new HashMap<String,Integer>();
		 ExecutorService service =  Executors.newFixedThreadPool(1);
		 service.submit(new DataLoader());
		 
	}
	
	@Override
	public String getFriends(String s_id) throws Exception{
		StringBuilder names = new StringBuilder();
		Integer key = getKey(s_id);
		GraphTraversal<Vertex, Vertex> trav= graph.traversal().V(key).bothE("areFriends").has("weight", 1.0).outV();//bothV();//.hasLabel("areFriends");//.has("weight", 1.0).bothV();//V(s_id);//.out("areFriends");//.filter("weight", 1.0);
		Element next = null;
		while(trav.hasNext() && (next = trav.next())!=null && next instanceof Vertex){
			Vertex v = (Vertex) next;
			if(v!=null){
				names.append("name :").append(v.value("name").toString()).append(" ,");
			}
		}
		
		return names.toString();
	}

	public InputStream getDataSinceTimestamp(Long timestamp) throws  Exception {
		RESTClient client = new RESTClient(dataservice+"?since="+timestamp);
		InputStream res = client.send(RESTClient.METHOD.GET, "/");
		return res;
		
	}
	
	private void loadGremlin(String line) throws JSONException{

		JSONObject json = new JSONObject(line);
		JSONObject p1 = json.getJSONObject("from");
		JSONObject p2 = json.getJSONObject("to");
		boolean friends = json.getBoolean("areFriends");
		String timestamp = json.getString("timestamp");
		
		Vertex person1 = queryGraph(getKey(p1.getString("id")));
		
		Vertex person2 = queryGraph(getKey(p2.getString("id")));
		
		if(person1!=null && person2!=null){
			Edge e = null;
			while(( e = person1.edges(Direction.BOTH, "areFriends").next()) != null){
				if(e.outVertex().equals(person2)){
					if(friends && e.value("timestamp")!=null && Long.parseLong(e.value("timestamp").toString()) < Long.parseLong(timestamp) ){
						e.property("weight",1.0);
					}else{
						e.property("weight",0.0);
					}
					e.property("timestamp", timestamp);
				}
			}
			
		}else{
		
			person1=person1!=null?person1:graph.addVertex(T.label, "person", T.id, getKey(p1.getString("id")), "name", p1.getString("name"));
			person2 = person2!=null?person2:graph.addVertex(T.label, "person", T.id, getKey(p2.getString("id")), "name", p2.getString("name"));

			if(friends){
				person1.addEdge("areFriends", person2, "weight",1.0,"timestamp", timestamp); 
			}else{
				person1.addEdge("areFriends", person2, "weight",0.0, "timestamp", timestamp); 
			}
		}
		
	}
	
	private synchronized int getKey(String keyStr){

		Integer ret = keys.size();
		if(keys.get(keyStr)==null) keys.put(keyStr, ret);
		
		return keys.get(keyStr);
	}
	
	
	private synchronized Vertex queryGraph(Integer id){
		Vertex p = null;
		try{
			p = graph.traversal().V(id).next();
		}catch(Exception e){
			//nothing
		}
		return p;
	}
	
	class DataLoader implements Runnable{

		@Override
		public void run() {

			int batch = 10;
			
			try {
				InputStream res = getDataSinceTimestamp(System.currentTimeMillis());
				
				InputStreamReader reader =  new InputStreamReader(res);
				BufferedReader br = new BufferedReader(reader);
				String sCurrentLine;
				
				
				while ((sCurrentLine = br.readLine()) != null) {
					
					System.out.println(sCurrentLine);
					
					loadGremlin(sCurrentLine);
					
					batch--;
					if(batch==0){
						//graph.tx().commit();
						batch=10;
					}
					
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
}
