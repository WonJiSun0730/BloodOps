package com.shop.member.repository;

import com.shop.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member,Long> {
        // select * from member where email = email;
        Member findByEmail(String email);

}
