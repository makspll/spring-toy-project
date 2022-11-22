package com.tasks.tasks.dao.account;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tasks.tasks.entities.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {

    public Optional<Account> findByUsername(String username);

}
