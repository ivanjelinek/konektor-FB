package fbconnector;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.types.Comment;
import com.restfb.types.Post;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
//import org.elasticsearch.common.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Trida pro zpracovani dat - index CSFD a MALL Urceno pro studenty VŠE v Praze
 * - Kompetencniho centra pro nestrukturovana data
 *
 * @author Ivan Jelinek
 */
class FBDownloader {

    private final com.restfb.FacebookClient facebookClient;
    private ESConnect ESconn;
    private String[] FBPages;
    private int limitPages = 15;
    private Settings s;

    /**
     *
     * @param accessToken
     */
    public FBDownloader(DefaultFacebookClient facebookClient, int limitPages, Settings s) {
        this.facebookClient = facebookClient;
        this.limitPages = limitPages;
        this.s = s;
    }

    public FBDownloader(DefaultFacebookClient facebookClient, Settings s) {
        this.facebookClient = facebookClient;
    }

    public void setESConnect(ESConnect ESconn) {
        this.ESconn = ESconn;
    }

    public void setFBPages(String[] FBPages) {
        this.FBPages = FBPages;
    }

    public void startDownload(Settings settings) {
        for (String page : FBPages) {
            System.out.println("Začínám stahovat: " + page);
            indexPosts(page, ESconn, settings.getSegmentForPage(page));
        }
    }

    private void indexPosts(String fbUser, ESConnect ESconn, String segmentPage) {
        Connection<Post> myFeed = facebookClient.fetchConnection(fbUser + "/feed", Post.class);
        List<JSONObject> jsonList = new ArrayList<>();
        SimpleDateFormat f = new SimpleDateFormat("d.M.yyyy HH:mm:ss");
        long i = 1;
        for (List<Post> feedItem : myFeed) {
            //if (i % 20 == 0 || i == 1) 
            //DateTime start = new DateTime();
            //}
            int pN = 0;
            int pC = 0;
            for (Post post : feedItem) {
                pN++;
                List<Comment> comments = getCommentFromPost(facebookClient, post.getId());
                pC = pC + comments.size();

                jsonList.addAll(prepareJsonForIndex(post, fbUser, segmentPage));
                jsonList.addAll(prepareJsonForIndex(comments, post.getId(), null, fbUser));

                List<Comment> subComments;
                for (Comment cmnt : comments) {
                    subComments = getCommentFromPost(facebookClient, cmnt.getId());
                    pC = pC + subComments.size();
                    if (!subComments.isEmpty()) {
                        jsonList.addAll(prepareJsonForIndex(subComments, post.getId(), cmnt.getId(), fbUser));
                    }
                }
                try {
                    ESconn.postElasticSearch(jsonList);
                } catch (Exception ex) {
                    Logger.getLogger(FBDownloader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            //System.out.println(f.format(new Date()) + ": Zpracována feed page " + i + " (počet postů: " + pN + ", počet komentářů: " + pC + ") za " + (new DateTime().getMillis() - start.getMillis()) / 1000 + " vteřin");
            if (i > limitPages) {
                break;
            }
            i++;

        }
    }

    private List<Comment> getCommentFromPost(com.restfb.FacebookClient facebookClient, String elementId) {
        List<Comment> comments = new ArrayList();

        Connection<Comment> allComments = facebookClient.fetchConnection(elementId + "/comments", Comment.class);
        for (List<Comment> postComments : allComments) {
            for (Comment comment : postComments) {
                comments.add(comment);
            }
        }
        return comments;
    }

    private List<JSONObject> prepareJsonForIndex(Post post, String page, String segment) {
        
        List<JSONObject> jsonList = new ArrayList<JSONObject>();
        Format f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        JSONObject lineJS = new JSONObject();
        lineJS.put("message", post.getMessage() == null ? post.getStory() : post.getMessage());
        lineJS.put("sentiment", getSentimentForText(post.getMessage() == null ? post.getStory() : post.getMessage()));
        //lineJS.put("userId", post.getFrom().getId());
        //lineJS.put("userName", post.getFrom().getName());
        lineJS.put("created", f.format(post.getCreatedTime()));
		//lineJS.put("caption", post.getCaption());
        //lineJS.put("posttype", post.getType());
        lineJS.put("type", "post");
        lineJS.put("likes", post.getLikes() == null ? 0 : post.getLikes().getData().size());
        lineJS.put("page", page);
        lineJS.put("level", -1);
        lineJS.put("id", post.getId());
        lineJS.put("segment", segment);

        jsonList.add(lineJS);
        return jsonList;
    }

    private List<JSONObject> prepareJsonForIndex(List<Comment> comments, String postId, String parentCommentID, String page) {
        List<JSONObject> jsonList = new ArrayList<JSONObject>();
        Format f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (comments.size() > 0) {
            for (Comment cmnt : comments) {
                JSONObject lnJS = new JSONObject();
                lnJS.put("message", cmnt.getMessage());
                //lnJS.put("userId", cmnt.getFrom().getId());
                //lnJS.put("userName", cmnt.getFrom().getName());
                lnJS.put("sentiment", getSentimentForText(cmnt.getMessage()));
                lnJS.put("created", f.format(cmnt.getCreatedTime()));
                lnJS.put("type", "comment");
                lnJS.put("likes", cmnt.getLikeCount());
                lnJS.put("PostID", postId);
                lnJS.put("page", page);
                lnJS.put("id", cmnt.getId());
                lnJS.put("level", parentCommentID == null ? 0 : 1);
                lnJS.put("parentCommentId", parentCommentID == null ? null : parentCommentID);

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
     * Metoda vytvori mapovani pro analyzer k indexu a typu, ktery je
     * specifikovan
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
        //userName.put("index" , "not_analyzed");
        types.put("userName", userName);

        JSONObject created = new JSONObject();
        created.put("format", "yyyy-MM-dd HH:mm:ss");
        created.put("type", "date");
        types.put("created", created);

        JSONObject postId = new JSONObject();
        postId.put("type", "string");
        //postId.put("index" , "not_analyzed");
        types.put("postId", postId);

        JSONObject likes = new JSONObject();
        likes.put("type", "integer");
        types.put("likes", likes);

        JSONObject page = new JSONObject();
        page.put("type", "string");
        types.put("page", page);

        JSONObject id = new JSONObject();
        id.put("type", "string");
        //id.put("index" , "not_analyzed");
        types.put("id", id);

        JSONObject level = new JSONObject();
        level.put("type", "long");
        types.put("level", level);

        if (typ == "comment") {
            JSONObject parentCommentId = new JSONObject();
            parentCommentId.put("type", "string");
            types.put("parentCommentId", parentCommentId);
        }

        JSONObject mappingBody = new JSONObject();
        mappingBody.put("properties", types);

        return mappingBody;
    }
    
    private int getSentimentForText(String obsahPrispevku){
        try {
            InputStream is = new FileInputStream(this.s.getSentimentModel());
            DoccatModel m = new DoccatModel(is);
            String inputText = obsahPrispevku;
            DocumentCategorizerME myCategorizer = new DocumentCategorizerME(m);
            double[] outcomes = myCategorizer.categorize(inputText);
            String category = myCategorizer.getBestCategory(outcomes);
            if (category.equals("positive")){
                return 1;
            } else if (category.equals("negative")){
                return -1;
            }
            
        } catch (IOException ex) {
            Logger.getLogger(FBDownloader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }
}
