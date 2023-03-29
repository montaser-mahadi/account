package com.montaser.account.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.montaser.account.config.AccountsServiceConfig;
import com.montaser.account.model.*;
import com.montaser.account.repository.AccountRepository;
import com.montaser.account.service.client.CardFeignsClient;
import com.montaser.account.service.client.LoansFeignClient;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.micrometer.core.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AccountsController {

    @Autowired
    private AccountRepository accountsRepository;
    @Autowired
    AccountsServiceConfig accountsConfig;
    @Autowired
    LoansFeignClient loansFeignClient;
    @Autowired
    CardFeignsClient cardsFeignClient;

    @PostMapping("/myAccount")
    @Timed(value = "getAccountDetails.time", description = "Time taken to return Account Details")
    public Accounts getAccountDetails(@RequestBody Customers customer) {

        Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId());
        if (accounts != null) {
            return accounts;
        } else {
            return null;
        }
    }

    @GetMapping("/account/properties")
    public String getPropertyDetails() throws JsonProcessingException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        Properties properties = new Properties(accountsConfig.getMsg(), accountsConfig.getBuildVersion(),
                accountsConfig.getMailDetails(), accountsConfig.getActiveBranches());
        String jsonStr = ow.writeValueAsString(properties);
        return jsonStr;
    }


    @PostMapping("/myCustomerDetails")
    @CircuitBreaker(name = "detailsForCustomerSupportApp", fallbackMethod = "myCustomerDetailsFallBack")
    //@Retry(name = "retryForCustomerDetails")
    public CustomerDetails myCustomerDetails(@RequestBody Customers customer) {
        Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId());
        List<Loans> loans = loansFeignClient.getLoansDetails(customer);
        List<Cards> cards = cardsFeignClient.getCardDetails(customer);

        CustomerDetails customerDetails = new CustomerDetails();
        customerDetails.setAccounts(accounts);
        customerDetails.setLoans(loans);
        customerDetails.setCards(cards);
        return customerDetails;
    }

    @GetMapping("/sayHello")
    @RateLimiter(name = "sayHello", fallbackMethod = "sayHelloFallBack")
    private String sayHello() {
        return "Welcome From Account Microservice and Hello.";
    }

    private String sayHelloFallBack(Throwable t) {
        return "Hi, Welcome to Bank application, account microservice";
    }

    private CustomerDetails myCustomerDetailsFallBack(@RequestBody Customers customers, Throwable t) {
        Accounts accounts = accountsRepository.findByCustomerId(customers.getCustomerId());
        List<Loans> loansList = loansFeignClient.getLoansDetails(customers);

        CustomerDetails customerDetails = new CustomerDetails();
        customerDetails.setAccounts(accounts);
        customerDetails.setLoans(loansList);
        return customerDetails;
    }

}