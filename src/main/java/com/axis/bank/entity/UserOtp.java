package com.axis.bank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_otp")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class UserOtp {

    @Id
    @Column(name = "otp_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long otpId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "otp", nullable = false)
    private String otp;

    @Column(name = "is_otp_used", nullable = false)
    private Boolean isOtpUsed = false;

    @Column(name = "otp_attempts", nullable = false)
    private Long otpAttempts;

    @Column(name = "opt_creation_time", nullable = false)
    private Long createdAt;
}
