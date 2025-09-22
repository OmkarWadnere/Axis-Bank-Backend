package com.axis.bank.repository;

import com.axis.bank.entity.User;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByEmailId(String emailId);

    Optional<User> findByMobileNumber(String mobileNumber);

    Optional<User> findByMobileNumberOrEmailId(String mobileNumber, String emailId);

    @Cacheable(value = "userExists", key = "#emailId", unless = "#result == false")
    boolean existsByEmailId(String emailId);

    @Cacheable(value = "userExists", key = "#mobileNumber", unless = "#result == false")
    boolean existsByMobileNumber(String mobileNumber);

    @Cacheable(value = "userExists", key = "#emailId + '_' + #mobileNumber", unless = "#result == false")
    boolean existsByEmailIdOrMobileNumber(String emailId, String mobileNumber);
}
