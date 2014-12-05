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
		//přihlášení pomocí Access Token z Graph exploreru
		//DefaultFacebookClient client = new FacebookClient(accessToken);
		//přihlášení pomocí aplikace na FB a její appId a app secret
		DefaultFacebookClient client = new FacebookClient("1456410961253561", "7f0dff89761284729d0117995e63e04f");
		FBDownloader fbDwnldr = new FBDownloader(client);		
		
		
		String indexName = "facebook2";
		//připojení do ES pomocí Java API
		ESConnect escon = new ESConnect(indexName);
		//příprava indexu 
		if (!escon.isIndex()){
			escon.createIndex();
			escon.setIndexSettings(fbDwnldr.getAnalyzer(indexName));
			escon.createMapping("post", fbDwnldr.prepareMapping("post"));
			escon.createMapping("comment", fbDwnldr.prepareMapping("comment"));
		}
		

		//načítání dat z FB a odesílání do ES
		String[] pages;
		pages = new String[2];
		pages[0] = "ceskasporitelna";
		pages[1] = "komercni.banka";
		
		fbDwnldr.setESConnect(escon);
		fbDwnldr.setFBPages(pages);
		
		fbDwnldr.startDownload();
		
		escon.endSession();
	}
}
