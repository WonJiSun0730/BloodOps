package com.shop.changhyeon.certificate.entity;

import com.shop.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.*;

@Entity
@Table(name="certificate", indexes=@Index(name="idx_certificate_member_issued", columnList="member_id, issued_at"))
@Getter @NoArgsConstructor(access=AccessLevel.PROTECTED)
public class Certificate {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="issue_number",unique=true,updatable=false,length=32) private String issueNumber;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="member_id",nullable=false,updatable=false) private Member member;
 @Column(name="certificate_type",nullable=false,updatable=false,length=40) private String certificateType;
 @Column(nullable=false,updatable=false,length=100) private String purpose;
 @Column(name="period_start",nullable=false,updatable=false) private LocalDate periodStart;
 @Column(name="period_end",nullable=false,updatable=false) private LocalDate periodEnd;
 @Column(name="donation_count",nullable=false,updatable=false) private int donationCount;
 @CreationTimestamp @Column(name="issued_at",nullable=false,updatable=false) private LocalDateTime issuedAt;
 public Certificate(String issueNumber,Member member,String purpose,LocalDate start,LocalDate end,int count){
  this.issueNumber=issueNumber;this.member=member;this.certificateType="DONATION_CONFIRMATION";
  this.purpose=purpose;this.periodStart=start;this.periodEnd=end;this.donationCount=count;
 }
}
