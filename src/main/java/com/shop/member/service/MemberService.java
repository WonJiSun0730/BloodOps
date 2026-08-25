package com.shop.member.service;

import com.shop.member.constant.Role;
import com.shop.member.dto.AdminMemberResponseDto;
import com.shop.member.dto.MemberAdditionalInfoDto;
import com.shop.member.entity.Member;
import com.shop.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service    // 서비스
@Transactional  // 트랜잭션
@RequiredArgsConstructor    // @Autowired -> 싱글톤 스프링 컨테이너에서 받아서 쓸거임
//  @RequiredArgsConstructor -> 변수 final 객체 붙혀줌
public class MemberService implements UserDetailsService { //유저디테일서비스 = 스프링 프레임워크 시큐리티

    private final MemberRepository memberRepository;
    // db에 일반 회원 저장
    public Member saveMember(Member member) {
        validateDuplicateMember(member);
        return memberRepository.save(member);   // 데이터베이스에 저장을 하라는 명령
    }

    //멤버 중복 요소 체크
    private void validateDuplicateMember(Member member) {
        if (member.getLoginId() != null //로그인 아이디가 있을 때만 아이디 중복 검사 -> 소셜 회원 고려
                && memberRepository.existsByLoginId(member.getLoginId())) { //중복이면
            throw new IllegalStateException("이미 사용 중인 아이디입니다.");
        }
        if (memberRepository.existsByEmail(member.getEmail())) { //중복 이메일 체크
            throw new IllegalStateException("이미 가입된 이메일입니다.");
        }
    }

    //일반 로그인 요청 시 실행
    @Override //규칙에 따라 오버라이딩 한다.
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        Member member = memberRepository.findByLoginId(loginId);

        if (member == null){
            throw new UsernameNotFoundException("아이디에 해당하는 회원을 찾을 수 없습니다:" +loginId);
        }
        return User.builder().username(member.getEmail())
                .password(member.getPassword())
                .roles(member.getRole().toString())
                .build();
    }

    /*
     * 입력한 아이디를 사용할 수 있는지 확인합니다.
     * true  = 사용 가능
     * false = 이미 사용 중
     */
    @Transactional(readOnly = true)
    public boolean isLoginIdAvailable(String loginId) {
        return !memberRepository.existsByLoginId(loginId);
    }

    /*
     * 입력한 이메일을 사용할 수 있는지 확인합니다.
     * true  = 사용 가능
     * false = 이미 가입된 이메일
     */
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return !memberRepository.existsByEmail(email);
    }

    /*
     * ADMIN 회원 관리 화면에서 사용할 전체 회원 목록을 조회합니다.
     *
     * Member 엔티티를 그대로 반환하지 않고
     * 관리자 화면에 필요한 정보만 DTO로 변환합니다.
     */
    @Transactional(readOnly = true)
    public Page<AdminMemberResponseDto> getAdminMemberList(
            String keyword,
            Role role,
            Pageable pageable
    ) {

        String normalizedKeyword =
                keyword == null
                        ? ""
                        : keyword.trim();

        return memberRepository
                .findAdminMembers(
                        normalizedKeyword,
                        role,
                        pageable
                )
                .map(AdminMemberResponseDto::new);
    }

    /*
     * ADMIN 회원 관리 화면에서 회원 권한을 변경합니다.
     *
     * 마지막 남은 ADMIN은 USER 또는 MEDICAL로 변경할 수 없습니다.
     */
    //MemberService 자체에 이미 @Transactional 이 붙어 있으므로
    // 조회한 Member의 role을 변경하면 JPA 변경 감지로 트랜잭션 종료 시 UPDATE가 나갑니다.
    public AdminMemberResponseDto updateMemberRole(
            Long memberId,
            Role newRole
    ) {
        if (newRole == null) {
            throw new IllegalArgumentException(
                    "변경할 권한을 선택해주세요."
            );
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "회원을 찾을 수 없습니다."
                        )
                );

        Role currentRole = member.getRole();

        // 이미 같은 권한이면 별도 변경 없이 현재 정보를 반환합니다.
        if (currentRole == newRole) {
            return new AdminMemberResponseDto(member);
        }

        /*
         * ADMIN → USER/MEDICAL 변경 시
         * 현재 ADMIN이 한 명뿐이라면 변경을 막습니다.
         */
        if (
                currentRole == Role.ADMIN
                        && newRole != Role.ADMIN
                        && memberRepository.countByRole(Role.ADMIN) <= 1
        ) {
            throw new IllegalStateException(
                    "최소 1명의 관리자가 존재해야 합니다."
            );
        }

        member.setRole(newRole);

        return new AdminMemberResponseDto(member);
    }

    @Transactional
    public void updateAdditionalInfo(
            String email,
            MemberAdditionalInfoDto dto
    ) {
        Member member =
                memberRepository.findByEmail(email);

        if (member == null) {
            throw new IllegalArgumentException(
                    "회원 정보를 찾을 수 없습니다."
            );
        }

        /*
         * 추가정보 입력 화면에서 직접 입력한
         * 휴대전화 번호가 다른 회원에게 이미 등록되어 있다면 차단합니다.
         */
        if (
                dto.getPhone() != null
                        && !dto.getPhone().isBlank()
                        && memberRepository.existsByPhone(
                        dto.getPhone()
                )
                        && !dto.getPhone().equals(
                        member.getPhone()
                )
        ) {
            throw new IllegalArgumentException(
                    "이미 가입된 휴대전화 번호입니다. "
                            + "아이디 찾기 또는 비밀번호 변경을 이용해주세요."
            );
        }

        member.updateAdditionalInfo(
                dto.getBirthDate(),
                dto.getPhone(),
                dto.isSmsAgreed(),
                dto.isEmailAgreed()
        );
    }
}
