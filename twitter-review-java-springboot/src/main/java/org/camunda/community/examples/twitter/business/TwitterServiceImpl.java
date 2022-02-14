package org.camunda.community.examples.twitter.business;

import org.springframework.stereotype.Component;

/**
 * Publish content on Twitter. It really goes live! 
 * Watch out http://twitter.com/#!/camunda_demo for your postings.
 */
@Component
public class TwitterServiceImpl implements TwitterService {

  //private Twitter twitter;

  public TwitterServiceImpl() {
    //AccessToken accessToken = new AccessToken("220324559-jet1dkzhSOeDWdaclI48z5txJRFLCnLOK45qStvo", "B28Ze8VDucBdiE38aVQqTxOyPc7eHunxBVv7XgGim4say");
    //twitter = new TwitterFactory().getInstance();
    //twitter.setOAuthConsumer("lRhS80iIXXQtm6LM03awjvrvk", "gabtxwW8lnSL9yQUNdzAfgBOgIMSRqh7MegQs79GlKVWF36qLS");
    //twitter.setOAuthAccessToken(accessToken);
  }

  @Override
  public void tweet(String content) throws DuplicateTweetException {
    System.out.println(" PRODUCTION TWEET: " + content);
    /*try {
      twitter.updateStatus(content);
    } catch (TwitterException e) {
  		if (e.getErrorCode() == 187) {
        throw new DuplicateTweetException();
      } else {
        throw new RuntimeException(e);
      }
    }*/
  }

}
