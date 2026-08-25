import {
  Link,
  useLocation,
} from 'react-router-dom'


function AccountRecoveryPage() {
  const location = useLocation()

  const isPasswordPage =
    location.pathname === '/change-password'

  return (
    <main className="login-page">
      <section className="login-card">

        <div className="login-heading">
          <div className="login-heading-copy">

            <span className="login-eyebrow">
              BLOODOPS
            </span>

            <h1>
              {isPasswordPage
                ? '비밀번호 변경'
                : '아이디 찾기'}
            </h1>

            <p>
              해당 기능은 현재 준비 중입니다.
            </p>

          </div>
        </div>

        <nav className="login-account-links">
          <Link to="/login">
            로그인으로 돌아가기
          </Link>
        </nav>

      </section>
    </main>
  )
}


export default AccountRecoveryPage