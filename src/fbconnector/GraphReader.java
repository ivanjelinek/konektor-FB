/*
 * Copyright (c) 2010-2014 Mark Allen.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package fbconnector;


import java.util.ArrayList;
import java.util.List;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.DefaultJsonMapper;
import com.restfb.FacebookClient;
import com.restfb.JsonMapper;
import com.restfb.types.Comment;
import com.restfb.types.Post;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Examples of RestFB's Graph API functionality.
 * 
 * @author <a href="http://restfb.com">Mark Allen</a>
 */
//@SuppressWarnings("deprecation")
public class GraphReader {//extends Example {
  /**
   * RestFB Graph API client.
   */
  private final FacebookClient facebookClient;
	
	private String[] pages;

  /**
	 *
	 * @param accessToken
	 */
	public GraphReader(DefaultFacebookClient facebookClient) {
		this.facebookClient = facebookClient;
		pages = new String[2];
		pages[0] = "ceskasporitelna";
		pages[1] = "komercni.banka";
		for (String page : pages) {
			getPosts(page);
		}
  }
	
	private Writer prepareOutputWriter(String name){
		File file = new File(name);
		try {
			file.createNewFile();
		} catch (IOException ex) {
			Logger.getLogger(GraphReader.class.getName()).log(Level.SEVERE, null, ex);
		}
		Writer writer = null;
		try {
			writer = new FileWriter(file);
		} catch (IOException ex) {
			Logger.getLogger(GraphReader.class.getName()).log(Level.SEVERE, null, ex);
		}
		return writer;
	}
  
	private void getPosts(String fbUser){
  	Connection<Post> myFeed = facebookClient.fetchConnection(fbUser + "/feed", Post.class);
		JsonMapper jsonMapper = new DefaultJsonMapper();
		Ingest ingestor = new Ingest();

		for (List<Post> feedItem : myFeed){
			for (Post post : feedItem){
				List<Comment> comments = getCommentFromPost(facebookClient, post.getId());
				ingestor.sendIndex(post, comments, fbUser);
			}
		}
	}	
	
	private static List<Comment> getCommentFromPost(FacebookClient facebookClient, String postId){
		List<Comment> comments = new ArrayList();

		Connection<Comment> allComments = facebookClient.fetchConnection(postId + "/comments", Comment.class);
		for(List<Comment> postComments : allComments){
			for (Comment comment : postComments){
				comments.add(comment);
			}
		}
		return comments;
	}
}