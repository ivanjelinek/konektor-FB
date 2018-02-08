/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fbconnector;

import com.restfb.DefaultFacebookClient;
import com.restfb.Version;

/**
 *
 * @author Petr
 */
public class Main {

	
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
            Settings s;
            if (args.length == 0) {
                s = new Settings();
            } else {
                s = new Settings(args[0]);
            }
		//přihlášení pomocí Access Token z Graph exploreru
		//DefaultFacebookClient client = new FacebookClient(accessToken);
		//přihlášení pomocí aplikace na FB a její appId a app secret
		//DefaultFacebookClient client = new FacebookClient(s.getAppToken());
                System.out.println("Access token: " + s.getAccessToken());
                System.out.println("App Secret: " + s.getAppSecret());
                System.out.println("APPID: " + s.getAppID());
                DefaultFacebookClient client = new DefaultFacebookClient(s.getAccessToken(), s.getAppSecret(), Version.VERSION_2_5);
		FBDownloader fbDwnldr = new FBDownloader(client, s.getLimitPages(), s);		
		
		//String indexName = "banky";
		//připojení do ES pomocí Java API
		ESConnect escon = new ESConnect(s.getIndexES());
		//příprava indexu 
		if (!escon.isIndex()){
			escon.createIndex();
		}	
		//escon.setIndexSettings(fbDwnldr.getAnalyzer(indexName));
		escon.createMapping("post", fbDwnldr.prepareMapping("post"));
		escon.createMapping("comment", fbDwnldr.prepareMapping("comment"));
		escon.setIndexSettings(fbDwnldr.getAnalyzer(s.getIndexES()));
					
		//načítání dat z FB a odesílání do ES	
		fbDwnldr.setESConnect(escon);
		fbDwnldr.setFBPages(s.getFBPages());
		
		fbDwnldr.startDownload(s);
		
		escon.endSession();
	}

}