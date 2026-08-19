import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'

// 현재 사용자 조회 함수와 로그아웃 함수를 가져옵니다.
import { getCurrentUser, logout } from '../api/apiClient.js'

import bloodOpsLogo from '../assets/images/bloodops-logo.png'

function Header() {
    // 현재 로그인한 사용자 정보를 저장합니다.
    // 로그인하지 않았다면 null 상태를 유지합니다.
    const [currentUser, setCurrentUser] = useState(null)


    // Header가 처음 화면에 나타날 때 한 번 로그인 상태를 확인합니다.
    useEffect(() => {
      getCurrentUser()
        // 로그인 상태라면 받은 사용자 정보를 저장합니다.
        .then((user) => {
          setCurrentUser(user)
        })

        // 로그인하지 않은 경우 /api/auth/me가 401을 반환합니다.
        // 이 경우에는 사용자 정보를 null로 둡니다.
        .catch(() => {
          setCurrentUser(null)
        })
    }, [])

    // 로그아웃 버튼을 눌렀을 때 실행됩니다.
    async function handleLogout() {
      try {
        // Spring에 로그아웃 요청을 보내고 완료될 때까지 기다립니다.
        await logout()

        // 화면에 저장된 사용자 정보도 제거합니다.
        setCurrentUser(null)

        // 로그아웃 후 메인 화면으로 이동합니다.
        window.location.href = '/'
      } catch (error) {
        // 로그아웃 요청에 실패하면 콘솔에서 원인을 확인합니다.
        console.error('로그아웃 실패:', error)
      }
    }

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

        {/* 로그인 사용자 정보가 있으면 로그아웃 버튼을 표시합니다. */}
        {currentUser ? (
          <button
            type="button"
            className="login-link"
            onClick={handleLogout}
          >
            로그아웃
          </button>
        ) : (
          // 로그인하지 않았다면 기존 로그인 링크를 표시합니다.
          <Link className="login-link" to="/login">
            로그인
          </Link>
        )}
      </div>
    </header>
  )
}

export default Header