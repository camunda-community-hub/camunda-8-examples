package org.example.camunda.process.solution;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.apache.commons.lang3.builder.MultilineRecursiveToStringStyle;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@JsonInclude(Include.NON_NULL)
public class ProcessVariables {

  private String s3BucketName;

  public String getS3BucketName() {
    return s3BucketName;
  }

  public ProcessVariables setS3BucketName(String s3BucketName) {
    this.s3BucketName = s3BucketName;
    return this;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(
        this,
        new MultilineRecursiveToStringStyle() {
          public ToStringStyle withShortPrefixes() {
            this.setUseShortClassName(true);
            this.setUseIdentityHashCode(false);
            return this;
          }
        }.withShortPrefixes());
  }
}
