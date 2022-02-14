package org.camunda.community.examples.twitter.business;


/**
 * Publish content on Twitter.
 */
public interface TwitterService {

  void tweet(String content) throws DuplicateTweetException;

}
