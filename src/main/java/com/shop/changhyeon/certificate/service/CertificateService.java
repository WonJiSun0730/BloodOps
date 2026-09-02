package com.shop.changhyeon.certificate.service;

import com.shop.changhyeon.certificate.controller.CertificateController.*;
import com.shop.changhyeon.certificate.entity.Certificate;
import com.shop.changhyeon.certificate.repository.CertificateRepository;
import com.shop.changhyeon.donation.entity.DonationHistory;
import com.shop.changhyeon.donation.repository.DonationHistoryRepository;
import com.shop.member.entity.Member;
import com.shop.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service @RequiredArgsConstructor @Transactional
public class CertificateService {
 public static final String TYPE="DONATION_CONFIRMATION";
 public static final String NOT_FOUND="조회된 증명서 정보가 없습니다.\n입력하신 증명서 종류와 발급번호를 확인해주세요.";
 private static final SecureRandom RANDOM=new SecureRandom();
 private final CertificateRepository certificateRepository;
 private final DonationHistoryRepository donationHistoryRepository;
 private final MemberService memberService;

 public CertificateResponse issue(String identifier,CertificateIssueRequest request){
  if(!TYPE.equals(request.certificateType())) throw new IllegalArgumentException("발급 가능한 증명서 종류가 아닙니다.");
  LocalDate today=LocalDate.now(ZoneId.of("Asia/Seoul"));
  if(request.periodStart().isAfter(request.periodEnd())) throw new IllegalArgumentException("조회 시작일은 종료일보다 늦을 수 없습니다.");
  if(request.periodEnd().isAfter(today)) throw new IllegalArgumentException("조회 종료일은 오늘 이후로 입력할 수 없습니다.");
  Member member=memberService.findByAuthenticationIdentifier(identifier);
  List<DonationHistory> all=donationHistoryRepository.findByMember_IdOrderByDonationDateDescIdDesc(member.getId());
  List<DonationItem> items=all.stream()
   .filter(h->!h.getDonationDate().isBefore(request.periodStart())&&!h.getDonationDate().isAfter(request.periodEnd()))
   .map(h->new DonationItem(h.getId(),h.getDonationType().name(),h.getDonationDate(),h.getDonationPlaceName())).toList();
  String purpose=request.purpose().trim();
  if(!purpose.equals("조회용")&&!purpose.equals("제출용")) throw new IllegalArgumentException("사용 용도는 조회용 또는 제출용만 선택할 수 있습니다.");
  // 기존 DB의 issue_number NOT NULL 제약과 호환되도록 내부 번호는 항상 저장합니다.
  // 조회용 번호는 응답과 증명서 화면에서 노출하지 않습니다.
  String issueNumber=number(today);
  Certificate saved=certificateRepository.saveAndFlush(new Certificate(issueNumber,member,purpose,request.periodStart(),request.periodEnd(),items.size()));
  return response(saved,items);
 }

 @Transactional(readOnly=true)
 public CertificateResponse verify(CertificateVerifyRequest request){
  Certificate c=certificateRepository.findByCertificateTypeAndIssueNumber(request.certificateType(),request.issueNumber().trim().toUpperCase())
   .filter(certificate->"제출용".equals(certificate.getPurpose()))
   .orElseThrow(()->new CertificateNotFoundException(NOT_FOUND));
  return response(c,List.of());
 }

 private CertificateResponse response(Certificate c,List<DonationItem> items){
  String visibleIssueNumber="제출용".equals(c.getPurpose())?c.getIssueNumber():null;
  return new CertificateResponse(visibleIssueNumber,c.getCertificateType(),"헌혈확인증명서",c.getMember().getName(),
   c.getMember().getBirthDate(),c.getPurpose(),c.getPeriodStart(),c.getPeriodEnd(),c.getDonationCount(),c.getIssuedAt(),items);
 }
 private String number(LocalDate today){
  String value; do{value="BO-"+today.format(DateTimeFormatter.BASIC_ISO_DATE)+"-"+String.format("%06d",RANDOM.nextInt(1_000_000));}
  while(certificateRepository.existsByIssueNumber(value)); return value;
 }
 public static class CertificateNotFoundException extends RuntimeException{public CertificateNotFoundException(String message){super(message);}}
}
