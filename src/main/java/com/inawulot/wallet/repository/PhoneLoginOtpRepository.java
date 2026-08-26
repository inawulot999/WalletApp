package com.inawulot.wallet.repository;

import com.inawulot.wallet.domain.PhoneLoginOtp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhoneLoginOtpRepository extends JpaRepository<PhoneLoginOtp, String> {
}
