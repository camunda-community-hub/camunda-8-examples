package org.camunda.community.examples.dmn.process;

public class OnboardingProcessVariables {

  private String paymentType;
  private long customerRegionScore;
  private long monthlyPayment;

  public String getPaymentType() {
    return paymentType;
  }

  public OnboardingProcessVariables setPaymentType(String paymentType) {
    this.paymentType = paymentType;
    return this;
  }

  public long getCustomerRegionScore() {
    return customerRegionScore;
  }

  public OnboardingProcessVariables setCustomerRegionScore(long customerRegionScore) {
    this.customerRegionScore = customerRegionScore;
    return this;
  }

  public long getMonthlyPayment() {
    return monthlyPayment;
  }

  public OnboardingProcessVariables setMonthlyPayment(long monthlyPayment) {
    this.monthlyPayment = monthlyPayment;
    return this;
  }
}
