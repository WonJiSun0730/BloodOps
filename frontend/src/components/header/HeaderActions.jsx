import { useState } from 'react'
import { Link } from 'react-router-dom'

import {
  ChatIcon,
  MoonIcon,
  SearchIcon,
  SunIcon,
} from '../icons/HeaderIcons.jsx'

function HeaderActions({
  currentUser,
  theme,
  searchKeyword,
  onLogout,
  onThemeToggle,
  onSearchKeywordChange,
  onSearch,
}) {
  const [accountMenuOpen, setAccountMenuOpen] =
    useState(false)

  const isAdmin =
    currentUser?.roles?.includes('ROLE_ADMIN')
    ?? false

  function handleAccountLogout() {
    setAccountMenuOpen(false)
    onLogout()
  }

  return (
    <div className="header-actions">
      {currentUser ? (
        <>
          <span className="header-user-name">
            {currentUser.name ?? currentUser.email}
          </span>

          <div
            className={
              isAdmin
                ? 'header-account-links header-account-links--admin'
                : 'header-account-links'
            }
          >
            <Link
              className="header-text-button"
              to="/mypage"
            >
              마이페이지
            </Link>

            <span
              className="header-account-separator"
              aria-hidden="true"
            >
              ·
            </span>

            {isAdmin && (
              <>
                <Link
                  className="header-admin-link"
                  to="/admin"
                >
                  ADMIN
                </Link>

                <span
                  className="header-account-separator"
                  aria-hidden="true"
                >
                  ·
                </span>
              </>
            )}

            <button
              type="button"
              className="header-text-button"
              onClick={onLogout}
            >
              로그아웃
            </button>
          </div>

          {isAdmin && (
            <div className="header-account-menu">
              <button
                type="button"
                className="header-account-menu-button"
                aria-haspopup="menu"
                aria-expanded={accountMenuOpen}
                aria-label="계정 메뉴 열기"
                onClick={() =>
                  setAccountMenuOpen((current) => !current)
                }
              >
                <span>내 계정</span>

                <span
                  className="header-account-menu-chevron"
                  aria-hidden="true"
                />
              </button>

              {accountMenuOpen && (
                <div
                  className="header-account-dropdown"
                  role="menu"
                >
                  <div className="header-account-dropdown-profile">
                    <strong>
                      {currentUser.name ?? 'BloodOps 회원'}
                    </strong>

                    <span>
                      {currentUser.email
                        ?? currentUser.identifier
                        ?? ''}
                    </span>
                  </div>
                  <Link
                    to="/mypage"
                    role="menuitem"
                    className="header-account-dropdown-link"
                    onClick={() =>
                      setAccountMenuOpen(false)
                    }
                  >
                    마이페이지
                  </Link>

                  <Link
                    to="/admin"
                    role="menuitem"
                    className="header-account-dropdown-link admin"
                    onClick={() =>
                      setAccountMenuOpen(false)
                    }
                  >
                    관리자 페이지
                  </Link>

                  <button
                    type="button"
                    role="menuitem"
                    className="header-account-dropdown-link logout"
                    onClick={handleAccountLogout}
                  >
                    로그아웃
                  </button>
                </div>
              )}
            </div>
          )}
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
        onClick={onThemeToggle}
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
        <span
          className="header-utility-icon"
          aria-hidden="true"
        >
          {theme === 'light'
            ? <MoonIcon />
            : <SunIcon />}
        </span>
      </button>

      <div className="header-mobile-utilities">
        <button
          type="button"
          className="header-mobile-utility-button"
          aria-label="AI 채팅"
          title="AI 채팅 - 준비 중"
          aria-disabled="true"
        >
          <span
            className="header-utility-icon"
            aria-hidden="true"
          >
            <ChatIcon />
          </span>
        </button>

        <span
          className="header-mobile-utility-divider"
          aria-hidden="true"
        />

        <button
          type="button"
          className="header-mobile-utility-button"
          onClick={onThemeToggle}
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
          <span
            className="header-utility-icon"
            aria-hidden="true"
          >
            {theme === 'light'
              ? <MoonIcon />
              : <SunIcon />}
          </span>
        </button>
      </div>

      <form
        className="header-search"
        role="search"
        onSubmit={onSearch}
      >
        <input
          type="search"
          value={searchKeyword}
          onChange={(event) =>
            onSearchKeywordChange(event.target.value)
          }
          placeholder="무엇을 찾으시나요?"
          aria-label="사이트 검색"
        />

        <button
          type="submit"
          className="header-search-submit"
          aria-label="검색"
        >
          <span
            className="header-utility-icon"
            aria-hidden="true"
          >
            <SearchIcon />
          </span>
        </button>
      </form>
    </div>
  )
}

export default HeaderActions