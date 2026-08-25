import {
  useEffect,
  useRef,
  useState,
} from 'react'

import {
  Link,
  useSearchParams,
} from 'react-router-dom'

import {
  getCurrentUser,
  login,
} from '../../api/apiClient.js'

import EyeIcon
  from '../../components/icons/EyeIcon.jsx'

import {
  LoginLockIcon,
} from '../../components/icons/ServiceIcons.jsx'

import './LoginPage.css'

import BackgroundOrbit
  from '../../components/decorations/BackgroundOrbit.jsx'

function LoginPage() {
  const loginAlertShown =
    useRef(false)

  const [
      searchParams,
    ] = useSearchParams()


  const [
    checkingSession,
    setCheckingSession,
  ] = useState(true)

  const [loginId, setLoginId] =
    useState('')

  const [password, setPassword] =
    useState('')

  const [
    passwordVisible,
    setPasswordVisible,
  ] = useState(false)

  const [
    errorMessage,
    setErrorMessage,
  ] = useState('')

  const [
    fieldErrors,
    setFieldErrors,
  ] = useState({
    loginId: '',
    password: '',
  })

  const [
    isSubmitting,
    setIsSubmitting,
  ] = useState(false)

  const oauthError =
    searchParams.get('oauthError')

  let oauthErrorMessage = ''

  if (oauthError === 'duplicate_email') {
    oauthErrorMessage =
      '이미 가입된 이메일입니다. 아이디 찾기 또는 비밀번호 변경을 이용해주세요.'
  } else if (oauthError === 'duplicate_phone') {
    oauthErrorMessage =
      '이미 가입된 휴대전화 번호입니다. 아이디 찾기 또는 비밀번호 변경을 이용해주세요.'
  } else if (oauthError) {
    oauthErrorMessage =
      '소셜 로그인 중 오류가 발생했습니다. 다시 시도해주세요.'
  }

  useEffect(() => {
    async function checkSession() {
      try {
        const currentUser =
          await getCurrentUser()

        if (currentUser?.identifier) {
          if (!loginAlertShown.current) {
            loginAlertShown.current = true

            window.alert(
              '이미 로그인 중입니다.'
            )
          }

          window.location.replace('/')
          return
        }
      } catch {
        /*
         * 비로그인 상태라면
         * 로그인 화면을 그대로 표시합니다.
         */
      } finally {
        setCheckingSession(false)
      }
    }

    checkSession()
  }, [])

  function validateLoginForm() {
    const nextErrors = {
      loginId: '',
      password: '',
    }

    if (!loginId.trim()) {
      nextErrors.loginId =
        '아이디를 입력해주세요.'
    }

    if (!password) {
      nextErrors.password =
        '비밀번호를 입력해주세요.'
    }

    setFieldErrors(
      nextErrors
    )

    return (
      !nextErrors.loginId
      && !nextErrors.password
    )
  }

  async function handleSubmit(
    event
  ) {
    event.preventDefault()

    setErrorMessage('')

    if (!validateLoginForm()) {
      return
    }

    setIsSubmitting(true)

    try {
      await login(
        loginId,
        password
      )

      window.location.href = '/'
    } catch (error) {
      setErrorMessage(
        error.message
      )

      setIsSubmitting(false)
    }
  }

  if (checkingSession) {
    return null
  }

  return (
    <main className="login-page">

      <BackgroundOrbit />

      <section className="login-card">

        <div className="login-heading">

          <div className="login-heading-icon">
            <LoginLockIcon />
          </div>

          <div className="login-heading-copy">
            <span className="login-eyebrow">
              BLOODOPS
            </span>

            <h1>
              로그인
            </h1>

            <p>
              헌혈 예약과 개인 기록을 편리하게 관리해보세요.
            </p>
          </div>

        </div>

        <form
          className="login-form"
          onSubmit={handleSubmit}
        >
          <label
            className="login-field"
            htmlFor="loginId"
          >
            <span>
              아이디
            </span>

            <input
              id="loginId"
              type="text"
              name="loginId"
              className={
                fieldErrors.loginId
                  ? 'login-input-error'
                  : ''
              }
              placeholder="아이디를 입력해주세요."
              value={loginId}
              onChange={(event) => {
                setLoginId(
                  event.target.value
                )

                setFieldErrors(
                  (previous) => ({
                    ...previous,
                    loginId: '',
                  })
                )
              }}
              autoComplete="username"
              aria-invalid={
                Boolean(fieldErrors.loginId)
              }
            />

            {fieldErrors.loginId && (
              <small className="login-field-error">
                {fieldErrors.loginId}
              </small>
            )}
          </label>

          <label
            className="login-field"
            htmlFor="password"
          >
            <span>
              비밀번호
            </span>

            <div className="login-password-field">
              <input
                id="password"
                type={
                  passwordVisible
                    ? 'text'
                    : 'password'
                }
                name="password"
                className={
                  fieldErrors.password
                    ? 'login-input-error'
                    : ''
                }
                placeholder="비밀번호를 입력해주세요."
                value={password}
                onChange={(event) => {
                  setPassword(
                    event.target.value
                  )

                  setFieldErrors(
                    (previous) => ({
                      ...previous,
                      password: '',
                    })
                  )
                }}
                autoComplete="current-password"
                aria-invalid={
                  Boolean(fieldErrors.password)
                }
              />

              <button
                type="button"
                className="login-password-toggle"
                onClick={() =>
                  setPasswordVisible(
                    (visible) => !visible
                  )
                }
                aria-label={
                  passwordVisible
                    ? '비밀번호 숨기기'
                    : '비밀번호 보기'
                }
                aria-pressed={passwordVisible}
              >
                <EyeIcon
                  visible={passwordVisible}
                />
              </button>
            </div>

            {fieldErrors.password && (
              <small className="login-field-error">
                {fieldErrors.password}
              </small>
            )}

          </label>

          {oauthErrorMessage && (
            <p className="login-error">
              {oauthErrorMessage}
            </p>
          )}

          <button
            type="submit"
            className="login-submit"
            disabled={isSubmitting}
          >
            {isSubmitting
              ? '로그인 중...'
              : '로그인'}
          </button>

          <nav className="login-account-links">
            <Link to="/signup">
              회원가입
            </Link>

            <span aria-hidden="true">
              ·
            </span>

            <Link to="/find-id">
              아이디 찾기
            </Link>

            <span aria-hidden="true">
              ·
            </span>

            <Link to="/change-password">
              비밀번호 변경
            </Link>
          </nav>
        </form>

        <div className="login-social">

          <div className="login-social-divider">
            <span>
              또는
            </span>
          </div>

          <div className="login-social-list">

            <a
              className="login-social-button kakao"
              href="/oauth2/authorization/kakao"
            >
              <span className="login-social-mark">
                K
              </span>

              <span>
                카카오로 계속하기
              </span>
            </a>

            <a
              className="login-social-button naver"
              href="/oauth2/authorization/naver"
            >
              <span className="login-social-mark">
                N
              </span>

              <span>
                네이버로 계속하기
              </span>
            </a>

            <a
              className="login-social-button google"
              href="/oauth2/authorization/google"
            >
              <span className="login-social-mark">
                G
              </span>

              <span>
                Google로 계속하기
              </span>
            </a>

          </div>

        </div>

      </section>

    </main>
  )
}

export default LoginPage