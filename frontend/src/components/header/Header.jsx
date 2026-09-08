import {
  useEffect,
  useState,
} from 'react'

import {
  getCurrentUser,
  logout,
} from '../../api/apiClient.js'

import HeaderActions
  from './HeaderActions.jsx'

import HeaderBrand
  from './HeaderBrand.jsx'

import HeaderNavigation
  from './HeaderNavigation.jsx'

import './Header.css'

function Header({
  onChatbotOpen,
}) {
  const [
    currentUser,
    setCurrentUser,
  ] = useState(null)

  const [
    activeMenu,
    setActiveMenu,
  ] = useState(null)

  const [
    searchKeyword,
    setSearchKeyword,
  ] = useState('')

  const [
    theme,
    setTheme,
  ] = useState('light')

  useEffect(() => {
    getCurrentUser()
      .then((user) => {
        setCurrentUser(user)
      })
      .catch(() => {
        setCurrentUser(null)
      })
  }, [])

  useEffect(() => {
    const savedTheme =
      localStorage.getItem(
        'bloodops-theme'
      )

    const prefersDark =
      window.matchMedia(
        '(prefers-color-scheme: dark)'
      ).matches

    const initialTheme =
      savedTheme
      ?? (
        prefersDark
          ? 'dark'
          : 'light'
      )

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
      console.error(
        '로그아웃 실패:',
        error
      )
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

    const keyword =
      searchKeyword.trim()

    if (!keyword) {
      return
    }

    window.location.href =
      `/search?keyword=${encodeURIComponent(keyword)}`
  }

  return (
    <header className="site-header">
      <div className="header-top">
        <div className="header-top-inner">
          <HeaderBrand />

          <HeaderActions
            currentUser={currentUser}
            theme={theme}
            searchKeyword={searchKeyword}
            onLogout={handleLogout}
            onThemeToggle={handleThemeToggle}
            onSearchKeywordChange={setSearchKeyword}
            onSearch={handleSearch}
            onChatbotOpen={onChatbotOpen}
          />
        </div>
      </div>

      <HeaderNavigation
        activeMenu={activeMenu}
        onMenuEnter={handleMenuEnter}
        onMenuLeave={handleMenuLeave}
      />
    </header>
  )
}

export default Header