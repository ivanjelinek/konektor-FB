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
		//připojení do ES
		ESConnect escon = new ESConnect();
		//přihlášení pomocí Access Token z Graph exploreru
		//DefaultFacebookClient client = new FacebookClient(accessToken);
		//přhlášení pomocí aplikace na FB a její appId a app secret
		DefaultFacebookClient client = new FacebookClient("1456410961253561", "7f0dff89761284729d0117995e63e04f");
		GraphReader graph = new GraphReader(client);
		escon.endSession();
	}
}
