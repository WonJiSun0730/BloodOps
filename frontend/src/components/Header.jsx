import { Link } from 'react-router-dom'
import bloodOpsLogo from '../assets/images/bloodops-logo.png'

function Header() {
  return (
    <header className="site-header">
      <div className="header-inner">
        <Link className="brand" to="/">
          <img
            className="brand-logo"
            src={bloodOpsLogo}
            alt="BloodOps"
          />

          <span className="brand-name">BloodOps</span>
        </Link>

        <nav className="main-navigation" aria-label="주요 메뉴">
          <Link to="/centers">헌혈의 집</Link>
          <Link to="/reservation">헌혈 예약</Link>
          <Link to="/records">개인 기록</Link>
          <Link to="/community">커뮤니티</Link>
        </nav>

        <Link className="login-link" to="/login">
          로그인
        </Link>
      </div>
    </header>
  )
}

export default Header