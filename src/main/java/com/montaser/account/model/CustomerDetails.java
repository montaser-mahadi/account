package com.montaser.account.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
public class CustomerDetails {

    private Accounts accounts;
    private List<Loans> loans;
    private List<Cards> cards;

}