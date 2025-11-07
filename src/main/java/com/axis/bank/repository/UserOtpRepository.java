package com.axis.bank.repository;

import com.axis.bank.entity.UserOtp;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserOtpRepository extends CrudRepository<UserOtp, Long> {

    Optional<UserOtp> findByUserUserId(Long userId);
}
