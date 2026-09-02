package com.shop.changhyeon.certificate.repository;
import com.shop.changhyeon.certificate.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface CertificateRepository extends JpaRepository<Certificate,Long>{
 Optional<Certificate> findByCertificateTypeAndIssueNumber(String certificateType,String issueNumber);
 boolean existsByIssueNumber(String issueNumber);
}
