/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fbconnector;

import com.restfb.DefaultFacebookClient;

/**
 *
 * @author Petr
 */
public class Main {

	
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		Settings s = new Settings();
		//přihlášení pomocí Access Token z Graph exploreru
		//DefaultFacebookClient client = new FacebookClient(accessToken);
		//přihlášení pomocí aplikace na FB a její appId a app secret
		DefaultFacebookClient client = new FacebookClient(s.getAppToken());
		FBDownloader fbDwnldr = new FBDownloader(client);		
		
		String indexName = "banky4";
		//připojení do ES pomocí Java API
		ESConnect escon = new ESConnect(indexName);
		//příprava indexu 
		if (!escon.isIndex()){
			escon.createIndex();
		}	
		//escon.setIndexSettings(fbDwnldr.getAnalyzer(indexName));
		escon.createMapping("post", fbDwnldr.prepareMapping("post"));
		escon.createMapping("comment", fbDwnldr.prepareMapping("comment"));
		escon.setIndexSettings(fbDwnldr.getAnalyzer(indexName));
					
		//načítání dat z FB a odesílání do ES	
		fbDwnldr.setESConnect(escon);
		fbDwnldr.setFBPages(s.getFBPages());
		
		fbDwnldr.startDownload();
		
		escon.endSession();
	}

}
