package com.shop.changhyeon.certificate.controller;

import com.shop.changhyeon.certificate.service.CertificateService;
import com.shop.changhyeon.certificate.service.CertificateService.CertificateNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.*;

@RestController @RequestMapping("/api/certificates") @RequiredArgsConstructor
public class CertificateController {
 private final CertificateService certificateService;

 @PostMapping
 public ResponseEntity<CertificateResponse> issue(Authentication auth,@Valid @RequestBody CertificateIssueRequest request){
  if(auth==null||!auth.isAuthenticated()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  return ResponseEntity.status(HttpStatus.CREATED).body(certificateService.issue(auth.getName(),request));
 }
 @PostMapping("/verify")
 public CertificateResponse verify(@Valid @RequestBody CertificateVerifyRequest request){return certificateService.verify(request);}
 @ExceptionHandler(CertificateNotFoundException.class)
 public ResponseEntity<Map<String,String>> notFound(CertificateNotFoundException e){return ResponseEntity.status(404).body(Map.of("message",e.getMessage()));}
 @ExceptionHandler(IllegalArgumentException.class)
 public ResponseEntity<Map<String,String>> badRequest(IllegalArgumentException e){return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()));}

 public record CertificateIssueRequest(
  @NotBlank String certificateType,
  @NotBlank(message="발급 용도를 입력해주세요.") @Size(max=100) String purpose,
  @NotNull LocalDate periodStart,@NotNull LocalDate periodEnd){}
 public record CertificateVerifyRequest(
  @NotBlank String certificateType,
  @NotBlank(message="발급번호를 입력해주세요.") @Pattern(regexp="^[A-Za-z0-9-]{8,32}$") String issueNumber){}
 public record DonationItem(Long id,String donationType,LocalDate donationDate,String donationPlaceName){}
 public record CertificateResponse(String issueNumber,String certificateType,String certificateTypeName,String holderName,
  LocalDate birthDate,String purpose,LocalDate periodStart,LocalDate periodEnd,int donationCount,LocalDateTime issuedAt,List<DonationItem> donations){}
}
