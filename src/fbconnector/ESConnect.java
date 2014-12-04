/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fbconnector;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;

import static org.elasticsearch.node.NodeBuilder.*;
import org.json.simple.JSONObject;

/**
 *
 * @author Petr
 */
public class ESConnect {
	private Client client;
		
	public ESConnect (){
			Client client = new TransportClient().addTransportAddress(new InetSocketTransportAddress("localhost",9300));
	}


	public static Map<String, Object> putJsonDocument(String title, String content, Date postDate, String author){

		Map<String, Object> jsonDocument = new HashMap<String, Object>();

		jsonDocument.put("title", title);
		jsonDocument.put("conten", content);
		jsonDocument.put("postDate", postDate);
		jsonDocument.put("author", author);

		return jsonDocument;
	}

	public void postElasticSearch(String indexName,JSONObject jsn){
		client.prepareIndex(indexName, "facebook").setSource(jsn).execute().actionGet();
	}
	
	public void endSession(){
		client.close();
	}
}

