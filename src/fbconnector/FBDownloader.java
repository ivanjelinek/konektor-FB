package fbconnector;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.DefaultJsonMapper;
import com.restfb.JsonMapper;
import com.restfb.types.Comment;
import com.restfb.types.Post;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Trida pro zpracovani dat - index CSFD a MALL
 * Urceno pro studenty VŠE v Praze - Kompetencniho centra pro nestrukturovana data
 * 
 * @author Ivan Jelinek
 */
class FBDownloader {

  private final com.restfb.FacebookClient facebookClient;
	private ESConnect ESconn;
	private String[] FBPages;
	
  /**
	 *
	 * @param accessToken
	 */
	public FBDownloader(DefaultFacebookClient facebookClient) {
		this.facebookClient = facebookClient;
  }
	
	public void setESConnect(ESConnect ESconn){
		this.ESconn = ESconn;
	}
					
	public void setFBPages(String[] FBPages){
		this.FBPages = FBPages;
	}	
	
	public void startDownload (){
		for (String page : FBPages) {
			indexPosts(page, ESconn);
		}
	}
	
	private void indexPosts(String fbUser, ESConnect ESconn){
  	Connection<Post> myFeed = facebookClient.fetchConnection(fbUser + "/feed", Post.class);
		JsonMapper jsonMapper = new DefaultJsonMapper();
		List<JSONObject> jsonList;
		
		for (List<Post> feedItem : myFeed){
			for (Post post : feedItem){
				jsonList = null;
				List<Comment> comments = getCommentFromPost(facebookClient, post.getId());
				jsonList = prepareJsonForIndex(post, comments, fbUser);
				try {
					ESconn.postElasticSearch(jsonList);
				} catch (Exception ex) {
					Logger.getLogger(FBDownloader.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
	}	
	
	private List<Comment> getCommentFromPost(com.restfb.FacebookClient facebookClient, String postId){
		List<Comment> comments = new ArrayList();

		Connection<Comment> allComments = facebookClient.fetchConnection(postId + "/comments", Comment.class);
		for(List<Comment> postComments : allComments){
			for (Comment comment : postComments){
				comments.add(comment);
			}
		}
		return comments;
	}		
		
	private List<JSONObject> prepareJsonForIndex(Post post, List<Comment> comments, String page) {
		List<JSONObject> jsonList = new ArrayList<JSONObject>();

		Format f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		JSONObject lineJS = new JSONObject();
		lineJS.put("message", post.getMessage());
		lineJS.put("userId", post.getFrom().getId());
		lineJS.put("userName", post.getFrom().getName());
		lineJS.put("created", f.format(post.getCreatedTime()));
		//lineJS.put("caption", post.getCaption());
		//lineJS.put("posttype", post.getType());
		lineJS.put("type", "post");
		lineJS.put("likes", post.getLikesCount());
		lineJS.put("page", page);
		lineJS.put("id",  post.getId());

		jsonList.add(lineJS);

		if (comments.size()>0){
			for (Comment cmnt : comments){
				JSONObject lnJS = new JSONObject();
				lnJS.put("message", cmnt.getMessage());
				lnJS.put("userId",cmnt.getFrom().getId());
				lnJS.put("userName",cmnt.getFrom().getName());
				lnJS.put("created", f.format(cmnt.getCreatedTime()));			
				lnJS.put("type", "comment");
				lnJS.put("likes", cmnt.getLikeCount());
				lnJS.put("PostID", post.getId());
				lnJS.put("page", page);
				lnJS.put("id", cmnt.getId());

				jsonList.add(lnJS);
			}
		}
		return jsonList;
	}

	/**
	 * Metoda vytvori index v ES
	 * 
	 * @param index nazev indexu, ktery ma byt vytvoren
	 */
	public JSONObject getAnalyzer(String index) {
		String analyzer = "{\"settings\": {\"analysis\": {\"filter\": {\"czech_stop\": {\"type\": \"stop\",\"stopwords\":  \"_czech_\"},\"czech_keywords\": {\"type\":       \"keyword_marker\",\"keywords\":   [\"x\"]}, \"czech_stemmer\": { \"type\":       \"stemmer\", \"language\":   \"czech\"}},\"analyzer\": {\"czech\": {\"tokenizer\":  \"standard\",\"filter\": [ \"lowercase\",\"czech_stop\", \"czech_keywords\", \"czech_stemmer\"]}}}}}";
		//String analyzer = "{\"analysis\": {\"filter\": {\"czech_stop\": {\"type\": \"stop\",\"stopwords\":  \"_czech_\"},\"czech_keywords\": {\"type\":       \"keyword_marker\",\"keywords\":   [\"x\"]}, \"czech_stemmer\": { \"type\":       \"stemmer\", \"language\":   \"czech\"}},\"analyzer\": {\"czech\": {\"tokenizer\":  \"standard\",\"filter\": [ \"lowercase\",\"czech_stop\", \"czech_keywords\", \"czech_stemmer\"]}}}}";
		JSONParser parser = new JSONParser();		
	  Object obj = null;
		try {
			obj = parser.parse(analyzer);
		} catch (ParseException ex) {
			Logger.getLogger(FBDownloader.class.getName()).log(Level.SEVERE, null, ex);
		}
		JSONObject jsonObject = (JSONObject) obj;
		return jsonObject;
	}

	/**
	 * Metoda vytvori mapovani pro analyzer k indexu a typu, ktery je specifikovan
	 * 
	 * @param index mapovani, ktere ma byt vytvoren
	 * @param typ k namapovani
	 */
	public JSONObject prepareMapping(String typ) {
		//String mappingBody = "{\"properties\": {\"czech\": {\"type\":\"string\",\"analyzer\": \"czech\"}}}";

		JSONObject types = new JSONObject();

			JSONObject tChild = new JSONObject();
			tChild.put("type", "string");
		types.put("type", tChild);

			JSONObject child = new JSONObject();	
			child.put("type", "string");
			child.put("analyzer", "czech");
		types.put("message", child);

			JSONObject userId = new JSONObject();
			userId.put("type", "string");
		types.put("userId", userId);	

			JSONObject userName = new JSONObject();
			userName.put("type", "string");
		types.put("userName", userName);	

			JSONObject created = new JSONObject();
			created.put("format", "yyyy-MM-dd HH:mm:ss");
			created.put("type", "date");
		types.put("created", created);	

			JSONObject postId = new JSONObject();
			postId.put("type", "string");
		types.put("postId", postId);	

			JSONObject likes = new JSONObject();
			likes.put("type", "string");
		types.put("likes", likes);	

			JSONObject page = new JSONObject();
			page.put("type", "string");
		types.put("page", page);	
		
			JSONObject id = new JSONObject();
			id.put("type", "string");
		types.put("id", id);	

		JSONObject mappingBody = new JSONObject();
		mappingBody.put("properties", types);
		
		return mappingBody;
	}
}

