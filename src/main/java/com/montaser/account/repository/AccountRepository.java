package com.montaser.account.repository;

import com.montaser.account.model.Accounts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Accounts, Long> {
    Accounts findByCustomerId(long customerId);
}
