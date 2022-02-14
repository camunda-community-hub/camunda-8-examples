package org.camunda.community.examples.twitter.business;

public class DuplicateTweetException extends Exception {
    public DuplicateTweetException(String message) {
        super(message);
    }

    public DuplicateTweetException(String message, Throwable cause) {
        super(message, cause);
    }
}
