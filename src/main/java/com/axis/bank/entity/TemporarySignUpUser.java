package com.axis.bank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "temp_users_record")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class TemporarySignUpUser {

    @Id
    @Column(name = "temp_user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "temp_user_email_id", nullable = false)
    private String emailId;

    @Column(name = "temp_user_otp", nullable = false)
    private String otp;

    @Column(name = "temp_user_last_otp_generation_time", nullable = false)
    private Long lastOtpGenerationTime;

    @Column(name = "temp_user_otp_used", nullable = false)
    private Boolean isOtpUsed;

    @Column(name = "temp_user_otp_attempts", nullable = false)
    private Long otpAttempts;

    @Column(name = "temp_user_is_locked")
    private Boolean isUserLocked = false;

    @Column(name = "temp_user_locked_time")
    private LocalDateTime userLockedTime;

}
