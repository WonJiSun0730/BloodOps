package com.shop.member.repository;

import com.shop.member.constant.LoginProvider;
import com.shop.member.constant.Role;
import com.shop.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {
    /**
     * 일반 로그인 시 아이디로 회원을 조회합니다.
     */
    Member findByLoginId(String loginId);

    /**
     * 회원가입 화면에서 아이디 중복 여부를 확인합니다.
     * true면 이미 사용 중인 아이디입니다.
     */
    boolean existsByLoginId(String loginId);

    /**
     * 일반 회원가입과 소셜 로그인에서 이메일로 회원을 조회합니다.
     */
    // select * from member where email = email;
    Member findByEmail(String email);

    /**
     * 회원가입 시 이메일 중복 여부를 확인합니다.
     */
    boolean existsByEmail(String email);

    /**
     * 소셜 로그인 신규 가입 시
     * 동일한 휴대전화 번호가 이미 사용 중인지 확인합니다.
     */
    boolean existsByPhone(String phone);

    /**
     * 소셜 로그인 제공자와 제공자 회원 ID로 회원을 조회합니다.
     *
     * 예:
     * provider = GOOGLE
     * providerId = Google이 제공한 고유 회원 ID
     */
    Optional<Member> findByProviderAndProviderId(
            LoginProvider provider,
            String providerId
    );

    //특정 권한을 가진 회원 수 조회, 마지막 관리자 권한 보호에 사용
    long countByRole(Role role);

    @Query("""
        select m
        from Member m
        where (
            :keyword is null
            or :keyword = ''
            or lower(m.name)
                like lower(concat('%', :keyword, '%'))
            or lower(m.email)
                like lower(concat('%', :keyword, '%'))
        )
        and (
            :role is null
            or m.role = :role
        )
        order by m.id desc
        """)
    Page<Member> findAdminMembers(
            @Param("keyword") String keyword,
            @Param("role") Role role,
            Pageable pageable
    );
}
