import { useState } from 'react'
// apiClient.js에서 만든 로그인 요청 함수를 가져옵니다.
import { login } from '../../api/apiClient.js'

function LoginPage() {

  //email은 현재 입력값, setEmail은 입력값을 변경하는 함수
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [errorMessage, setErrorMessage] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)


  // 로그인 버튼을 눌렀을 때 실행됩니다.
  // 서버의 응답을 기다려야 하므로 async를 사용합니다.
  async function handleSubmit(event) {
    // form 제출로 인한 페이지 새로고침을 막습니다.
    event.preventDefault()

    // 이전 로그인 실패 메시지를 지웁니다.
    setErrorMessage('')

    // 로그인 요청이 시작되었음을 표시합니다.
    setIsSubmitting(true)

    try {
      // Spring Security에 이메일과 비밀번호를 전송하고
      // 로그인 결과가 올 때까지 기다립니다.
      await login(email, password)

      // 여기까지 실행됐다면 로그인이 성공한 것입니다.
      // 페이지를 새로 열어서 로그인 상태가 화면 전체에 반영되게 합니다.
      window.location.href = '/'
    } catch (error) {
      // 이메일이나 비밀번호가 틀리는 등 요청이 실패하면 실행됩니다.
      setErrorMessage(error.message)

      // 다시 로그인 버튼을 누를 수 있도록 요청 상태를 해제합니다.
      setIsSubmitting(false)
    }
  }

  return (
    <main>
      <h1>로그인</h1>

      <form onSubmit={handleSubmit}>
        <div>
          <label htmlFor="email">이메일 주소</label>
          <input
            id="email"
            type="email"
            name="email"
            placeholder="이메일을 입력해주세요."

            //리액트가 기억하는 이메일 값 출력
            value={email}

            //사용자가 입력할 때마다 새로운 값 이메일에 저장
            onChange={(event) => setEmail(event.target.value)}
          />
        </div>

        <div>
          <label htmlFor="password">비밀번호</label>
          <input
            id="password"
            type="password"
            name="password"
            placeholder="비밀번호를 입력해주세요."

             // React가 기억하고 있는 비밀번호 값을 input에 표시합니다.
          value={password}

              // 사용자가 입력할 때마다 새로운 값을 password에 저장합니다.
          onChange={(event) => setPassword(event.target.value)}
          />
        </div>

        {/* 로그인에 실패했을 때만 오류 메시지를 표시합니다. */}
        {errorMessage && (
          <p className="error-message">
            {errorMessage}
          </p>
        )}

        {/* 로그인 요청 중에는 버튼을 누를 수 없게 합니다. */}
        <button
            type="submit"
            disabled={isSubmitting}
        >{/* 요청 중에는 버튼 문구를 변경합니다. */}
           {isSubmitting ? '로그인 중...' : '로그인'}
        </button>

        <button type="button">회원가입</button>
      </form>

        <div>
          <p>소셜 계정으로 로그인</p>

          {/*
            소셜 로그인은 React가 이메일과 비밀번호를 직접 보내지 않습니다.
            Spring Security의 OAuth2 로그인 시작 주소로 브라우저를 이동시킵니다.
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
{/*         a → Spring Security 등 외부 인증 흐름으로 이동 */}
    </main>
  )
}

export default LoginPage