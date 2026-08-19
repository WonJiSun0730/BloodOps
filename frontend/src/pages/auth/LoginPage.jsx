import { useState } from 'react'
import { Link } from 'react-router-dom'

// apiClient.js에서 만든 로그인 요청 함수를 가져옵니다.
import { login } from '../../api/apiClient.js'

function LoginPage() {

  // 일반 로그인은 이메일이 아니라 아이디를 사용합니다.
  const [loginId, setLoginId] = useState('')
  const [password, setPassword] = useState('')
  const [errorMessage, setErrorMessage] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  // 로그인 버튼을 눌렀을 때 실행됩니다.
  async function handleSubmit(event) {
    // form 제출로 인한 페이지 새로고침을 막습니다.
    event.preventDefault()

    // 이전 로그인 실패 메시지를 지웁니다.
    setErrorMessage('')
    setIsSubmitting(true)

    try {
      // Spring Security에 아이디와 비밀번호를 전송합니다.
      await login(loginId, password)

      // 로그인 성공 후 메인 페이지로 이동합니다.
      window.location.href = '/'
    } catch (error) {
      setErrorMessage(error.message)
      setIsSubmitting(false)
    }
  }

  return (
    <main>
      <h1>로그인</h1>

      <form onSubmit={handleSubmit}>
        <div>
          <label htmlFor="loginId">아이디</label>
          <input
            id="loginId"
            type="text"
            name="loginId"
            placeholder="아이디를 입력해주세요."
            value={loginId}
            onChange={(event) => setLoginId(event.target.value)}
            autoComplete="username"
          />
        </div>

        <div>
          <label htmlFor="password">비밀번호</label>
          <input
            id="password"
            type="password"
            name="password"
            placeholder="비밀번호를 입력해주세요."
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            autoComplete="current-password"
          />
        </div>

        {/* 로그인에 실패했을 때만 오류 메시지를 표시합니다. */}
        {errorMessage && (
          <p className="error-message">
            {errorMessage}
          </p>
        )}

        <button
          type="submit"
          disabled={isSubmitting}
        >
          {isSubmitting ? '로그인 중...' : '로그인'}
        </button>

        <Link to="/signup">
          회원가입
        </Link>
      </form>

      <div>
        <p>소셜 계정으로 로그인</p>

        {/*
          소셜 로그인은 React가 아이디와 비밀번호를 보내지 않고
          Spring Security의 OAuth2 시작 주소로 이동합니다.
        */}
        <a href="/oauth2/authorization/kakao">
          카카오 로그인
        </a>

        <a href="/oauth2/authorization/naver">
          네이버 로그인
        </a>

        <a href="/oauth2/authorization/google">
          구글 로그인
        </a>
      </div>
    </main>
  )
}

export default LoginPage