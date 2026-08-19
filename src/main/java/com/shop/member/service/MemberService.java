package com.shop.member.service;

import com.shop.member.entity.Member;
import com.shop.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
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
}
