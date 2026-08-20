import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'

import { getCurrentUser, logout } from '../api/apiClient.js'
import bloodOpsLogo from '../assets/images/bloodops-logo.png'

const menuItems = [
  {
    title: '헌혈지식',
    items: [
      { label: '헌혈의 필요성', to: '/knowledge/need' },
      { label: '오늘의 혈액정보', to: '/blood-status' },
      { label: '헌혈과정', to: '/knowledge/process' },
      { label: '혈액 검사·제제·공급', to: '/knowledge/blood' },
      { label: '혈액관리시스템', to: '/knowledge/system' },
      { label: '혈액상식', to: '/knowledge/common' },
      { label: '헌혈증서', to: '/certificate' },
    ],
  },
  {
    title: '헌혈참여',
    items: [
      { label: '헌혈의 집 찾기', to: '/centers' },
      { label: '헌혈 예약', to: '/reservation' },
      { label: '전자문진', to: '/questionnaire' },
    ],
  },
  {
    title: '헌혈활동',
    items: [
      { label: '헌혈내역 조회', to: '/records' },
      { label: '검사결과 조회', to: '/records/results' },
      { label: '증명서 발급', to: '/certificate' },
      { label: '혈액 사용 보류신청', to: '/blood-hold' },
    ],
  },
  {
    title: '고객센터',
    items: [
      { label: '자주하는 질문', to: '/faq' },
      { label: 'Q&A', to: '/qna' },
      { label: '이용 안내', to: '/guide' },
    ],
  },
  {
    title: '소식&프로모션',
    items: [
      { label: '새소식', to: '/news' },
      { label: '프로모션', to: '/promotion' },
      { label: '캠페인', to: '/campaign' },
    ],
  },
  {
    title: '홍보관&혈액정보',
    items: [
      { label: '혈액 보유현황', to: '/blood-status' },
      { label: '헌혈 통계', to: '/statistics' },
    ],
  },
  {
    title: '기관소개',
    items: [
      { label: 'BloodOps 소개', to: '/about' },
      { label: '서비스 안내', to: '/about/service' },
    ],
  },
]

function Header() {
  const [currentUser, setCurrentUser] = useState(null)
  const [activeMenu, setActiveMenu] = useState(null)
  const [searchKeyword, setSearchKeyword] = useState('')
  const [theme, setTheme] = useState('light')

  useEffect(() => {
    getCurrentUser()
      .then((user) => {
        setCurrentUser(user)
      })
      .catch(() => {
        setCurrentUser(null)
      })
  }, [])

    // 초기 테마 설정
    useEffect(() => {
      const savedTheme =
        localStorage.getItem('bloodops-theme')

      const prefersDark =
        window.matchMedia(
          '(prefers-color-scheme: dark)'
        ).matches

      const initialTheme =
        savedTheme ??
        (prefersDark ? 'dark' : 'light')

      setTheme(initialTheme)

      document.documentElement.setAttribute(
        'data-theme',
        initialTheme
      )
    }, [])

  async function handleLogout() {
    try {
      await logout()
      setCurrentUser(null)
      window.location.href = '/'
    } catch (error) {
      console.error('로그아웃 실패:', error)
    }
  }

    function handleThemeToggle() {
      const nextTheme =
        theme === 'light'
          ? 'dark'
          : 'light'

      setTheme(nextTheme)

      document.documentElement.setAttribute(
        'data-theme',
        nextTheme
      )

      localStorage.setItem(
        'bloodops-theme',
        nextTheme
      )
    }

  function handleMenuEnter(index) {
    setActiveMenu(index)
  }

  function handleMenuLeave() {
    setActiveMenu(null)
  }

    function handleSearch(event) {
      event.preventDefault()

      const keyword = searchKeyword.trim()

      if (!keyword) {
        return
      }

      // 검색 페이지 구현 전까지 URL 구조만 확보합니다.
      window.location.href =
        `/search?keyword=${encodeURIComponent(keyword)}`
    }

  const selectedMenu =
    activeMenu !== null ? menuItems[activeMenu] : null

  return (
    <header
      className="site-header"
      onMouseLeave={handleMenuLeave}
    >
      <div className="header-top">
        <div className="header-top-inner">
          <Link className="brand" to="/">
            <img
              className="brand-logo"
              src={bloodOpsLogo}
              alt="BloodOps"
            />

            <span className="brand-name">BloodOps</span>
          </Link>

          <div className="header-actions">
            {currentUser ? (
              <>
                <span className="header-user-name">
                  {currentUser.name ?? currentUser.email}
                </span>

                {currentUser.roles?.includes('ROLE_ADMIN') && (
                  <Link
                    className="header-admin-link"
                    to="/admin"
                  >
                    ADMIN
                  </Link>
                )}

                <button
                  type="button"
                  className="header-text-button"
                  onClick={handleLogout}
                >
                  로그아웃
                </button>
              </>
            ) : (
              <>
                <Link
                  className="header-text-button"
                  to="/login"
                >
                  로그인
                </Link>

                <Link
                  className="header-text-button"
                  to="/signup"
                >
                  회원가입
                </Link>
              </>
            )}

            <button
              type="button"
              className="header-utility-button"
              onClick={handleThemeToggle}
              aria-label={
                theme === 'light'
                  ? '다크모드로 변경'
                  : '라이트모드로 변경'
              }
              title={
                theme === 'light'
                  ? '다크모드'
                  : '라이트모드'
              }
            >
              <span aria-hidden="true">
                {theme === 'light' ? '☾' : '☀'}
              </span>
            </button>

            <form
              className="header-search"
              role="search"
              onSubmit={handleSearch}
            >
              <input
                type="search"
                value={searchKeyword}
                onChange={(event) =>
                  setSearchKeyword(event.target.value)
                }
                placeholder="무엇을 찾으시나요?"
                aria-label="사이트 검색"
              />

              <button
                type="submit"
                className="header-search-submit"
                aria-label="검색"
              >
                <span aria-hidden="true">⌕</span>
              </button>
            </form>
          </div>
        </div>
      </div>

      <div className="header-navigation-area">
        <nav
          className="main-navigation"
          aria-label="주요 메뉴"
        >
          {menuItems.map((menu, index) => (
            <button
              key={menu.title}
              type="button"
              className={
                activeMenu === index
                  ? 'main-navigation-item active'
                  : 'main-navigation-item'
              }
              onMouseEnter={() => handleMenuEnter(index)}
              onFocus={() => handleMenuEnter(index)}
              aria-expanded={activeMenu === index}
            >
              {menu.title}
            </button>
          ))}
        </nav>
      </div>

      <div
        className={
          selectedMenu
            ? 'mega-menu is-open'
            : 'mega-menu'
        }
      >
        <div className="mega-menu-inner">
          {selectedMenu && (
            <>
              <div className="mega-menu-title">
                <span>BloodOps</span>
                <h2>{selectedMenu.title}</h2>
              </div>

              <div className="mega-menu-links">
                {selectedMenu.items.map((item, index) => (
                  <Link
                    key={item.label}
                    to={item.to}
                    className="mega-menu-link"
                    style={{
                      '--menu-index': index,
                    }}
                    onClick={() => setActiveMenu(null)}
                  >
                    <span>{item.label}</span>
                    <span
                      className="mega-menu-arrow"
                      aria-hidden="true"
                    >
                      →
                    </span>
                  </Link>
                ))}
              </div>
            </>
          )}
        </div>
      </div>
    </header>
  )
}

export default Header