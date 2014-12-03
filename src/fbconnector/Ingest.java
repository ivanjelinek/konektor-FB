package fbconnector;

import com.restfb.types.Comment;
import com.restfb.types.Post;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.apache.commons.lang3.*;


/**
 * Trida pro zpracovani dat - index CSFD a MALL
 * Urceno pro studenty VŠE v Praze - Kompetencniho centra pro nestrukturovana data
 * 
 * @author Ivan Jelinek
 */
class Ingest {

    //kde mate spusten ES
    //private String hostES = "192.168.56.102";
    private String hostES = "localhost";
    private String portES = "9200";
    // upravte si cestu k datovym souborum
    //private String pathToData = "C:\\ES";
    // private String pathToData = "/mnt/";
		
		public Ingest(){
			createIndex("facebook");
			//vytvori mapovani pro defaultni analyzery
			prepareMapping("post");
			prepareMapping("comment");
		}

    public void sendIndex(Post post, List<Comment> comments, String page) {
			Format f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			JSONObject lineJS = new JSONObject();
			lineJS.put("message", post.getMessage());
			lineJS.put("userId", post.getFrom().getId());
			lineJS.put("userName", post.getFrom().getName());
			lineJS.put("created", f.format(post.getCreatedTime()));
			//lineJS.put("caption", post.getCaption());
			//lineJS.put("posttype", post.getType());
			lineJS.put("likes", post.getLikesCount());
			lineJS.put("page", page);
			
			URL url = null;
			try {
				url = new URL("http://" + hostES + ":" + portES + "/facebook/post/" + post.getId());
			} catch (MalformedURLException ex) {
				Logger.getLogger(Ingest.class.getName()).log(Level.SEVERE, null, ex);
			}
			System.out.println(sendRQ(url, "PUT", lineJS.toString()));
			
			if (comments.size()>0){
				for (Comment cmnt : comments){
					JSONObject lnJS = new JSONObject();
					lnJS.put("message", cmnt.getMessage());
					lnJS.put("userId",cmnt.getFrom().getId());
					lnJS.put("userName",cmnt.getFrom().getName());
					lnJS.put("created", f.format(cmnt.getCreatedTime()));			
					//lnJS.put("type", cmnt.getType());
					lnJS.put("likes", cmnt.getLikeCount());
					lnJS.put("PostID", post.getId());
					lnJS.put("page", page);
					
					URL urlC = null;
					try {
						urlC = new URL("http://" + hostES + ":" + portES + "/facebook/comment/" + cmnt.getId());
					} catch (MalformedURLException ex) {
						Logger.getLogger(Ingest.class.getName()).log(Level.SEVERE, null, ex);
					}
					System.out.println(sendRQ(urlC, "PUT", lnJS.toString()));
				}
			}
    }

    /**
     * Metoda vytvori index v ES
     * 
     * @param index nazev indexu, ktery ma byt vytvoren
     */
    private void createIndex(String index) {
        try {
            String analyzer = "{\"settings\": {\"analysis\": {\"filter\": {\"czech_stop\": {\"type\":       \"stop\",\"stopwords\":  \"_czech_\"},\"czech_keywords\": {\"type\":       \"keyword_marker\",\"keywords\":   [\"x\"]}, \"czech_stemmer\": { \"type\":       \"stemmer\", \"language\":   \"czech\"}},\"analyzer\": {\"czech\": {\"tokenizer\":  \"standard\",\"filter\": [ \"lowercase\",\"czech_stop\", \"czech_keywords\", \"czech_stemmer\"]}}}}}";
            URL url = new URL("http://" + hostES + ":" + portES + "/" + index + "/");
            System.out.println(sendRQ(url, "PUT", analyzer));
        } catch (MalformedURLException ex) {
            Logger.getLogger(Ingest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Metoda vytvori mapovani pro analyzer k indexu a typu, ktery je specifikovan
     * 
     * @param index mapovani, ktere ma byt vytvoren
     * @param typ k namapovani
     */
    private void prepareMapping(String typ) {
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
					likes.put("type", "string");
				types.put("page", page);	
								
				JSONObject mappingBody = new JSONObject();
        mappingBody.put("properties", types);
        try {
            System.out.println(sendRQ(new URL("http://" + hostES + ":" + portES+"/facebook/_mapping/" + typ), "POST", mappingBody.toString()));
        } catch (MalformedURLException ex) {
            Logger.getLogger(Ingest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Metoda odesle RQ skrze REST API
     * 
     * @param hostES IP adresa ES
     * @param portES port ES
     * @param index index, kam se ma zprava poslat
     * @param typ typ kam se zprava posle
     * @param method GET, POST, PUT, DELETE
     * @param message JSON zprava
     * @return String odpoved ES nebo null pri chybe
     */
    private String sendRQ(String hostES, String portES, String index, String typ, String method, String message) {

        String urlString = "";
        try {
            if (typ == null) {
                urlString = "http://" + hostES + ":" + portES + "/" + index + "/";
            } else {
                urlString = "http://" + hostES + ":" + portES + "/" + index + "/" + typ;
            }
            URL url = new URL(urlString);
            return sendRQ(url, method, message);

        } catch (MalformedURLException ex) {
            Logger.getLogger(Ingest.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return null;
    }

    /**
     * Pretizena verze metody pro odelsani pozadavku
     * 
     * @param url URL ES - cely string
     * @param method GET, POST, PUT, DELETE
     * @return String ES odpoved
     */
    private String sendRQ(URL url, String method) {
        return sendRQ(url, method, "");
    }

    /**
     * Pretizena verze metody, ktera skutecne odesle REST RQ
     * 
     * @param url URL string pro pripojeni k ES
     * @param method GET POST PUT DELETE
     * @param message JSON zprava
     * @return String odpoved ES null pri chybe
     */
    private String sendRQ(URL url, String method, String message) {
        try {
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setDoOutput(true);
            httpCon.setRequestMethod(method);

            if (!method.equals("GET")) {
                OutputStreamWriter out = new OutputStreamWriter(
                        httpCon.getOutputStream());
                out.write(message);
                out.close();
            }

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(httpCon.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            return response.toString();
        } catch (MalformedURLException ex) {
            Logger.getLogger(Ingest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Ingest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }
}

