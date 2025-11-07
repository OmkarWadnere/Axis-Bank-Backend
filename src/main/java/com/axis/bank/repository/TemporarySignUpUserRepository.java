package com.axis.bank.repository;

import com.axis.bank.entity.TemporarySignUpUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TemporarySignUpUserRepository extends CrudRepository<TemporarySignUpUser, Long> {

    Optional<TemporarySignUpUser> findByEmailId(String emailId);
}
