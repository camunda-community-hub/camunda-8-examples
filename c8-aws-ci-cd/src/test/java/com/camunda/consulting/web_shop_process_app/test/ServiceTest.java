package com.camunda.consulting.web_shop_process_app.test;

import static org.assertj.core.api.Assertions.*;

import com.camunda.consulting.web_shop_process_app.service.CustomerService;
import org.junit.jupiter.api.Test;

public class ServiceTest {

  @Test
  public void test_Customer_With_No_Credit_Number_Throws_Exception() {
    CustomerService customerService = new CustomerService();

    try {
      customerService.getCustomerCredit("hallo");
      fail("Exception expected");
    } catch (Exception e) {
      assertThat(e).hasMessage("The customer ID doesn't end with a number");
    }
  }
}
