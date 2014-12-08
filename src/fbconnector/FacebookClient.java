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
public class FacebookClient extends DefaultFacebookClient{
	
		public FacebookClient(String accessToken) {
			super(accessToken);
    }
	
    public FacebookClient(String appId, String appSecret) {
        com.restfb.FacebookClient.AccessToken accessToken = this.obtainAppAccessToken(appId, appSecret);
        this.accessToken = accessToken.getAccessToken();
    }	
}
