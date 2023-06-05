package org.camunda.community.examples.twitter.process;

public class TwitterProcessVariables {

  private String tweet;
  private String author;
  private String boss;
  private boolean approved;

  public String getTweet() {
    return tweet;
  }

  public TwitterProcessVariables setTweet(String tweet) {
    this.tweet = tweet;
    return this;
  }

  public String getAuthor() {
    return author;
  }

  public TwitterProcessVariables setAuthor(String author) {
    this.author = author;
    return this;
  }

  public String getBoss() {
    return boss;
  }

  public TwitterProcessVariables setBoss(String boss) {
    this.boss = boss;
    return this;
  }

  public boolean isApproved() {
    return approved;
  }

  public TwitterProcessVariables setApproved(boolean approved) {
    this.approved = approved;
    return this;
  }
}
