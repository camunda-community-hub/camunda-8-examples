package com.camunda.consulting.eventprocessing;

import com.camunda.consulting.eventprocessing.EventRepository.EventEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.OffsetDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, String> {

  @Entity(name = "EVENT")
  class EventEntity {
    @Id private String id;
    private String name;
    private String content;
    private State state;
    private OffsetDateTime createdAt;
    private OffsetDateTime publishingAt;
    private OffsetDateTime publishedAt;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getContent() {
      return content;
    }

    public void setContent(String content) {
      this.content = content;
    }

    public State getState() {
      return state;
    }

    public void setState(State state) {
      this.state = state;
    }

    public OffsetDateTime getCreatedAt() {
      return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
      this.createdAt = createdAt;
    }

    public OffsetDateTime getPublishingAt() {
      return publishingAt;
    }

    public void setPublishingAt(OffsetDateTime publishingAt) {
      this.publishingAt = publishingAt;
    }

    public OffsetDateTime getPublishedAt() {
      return publishedAt;
    }

    public void setPublishedAt(OffsetDateTime publishedAt) {
      this.publishedAt = publishedAt;
    }

    public enum State {
      CREATED,
      PUBLISHING,
      PUBLISHED
    }
  }
}
