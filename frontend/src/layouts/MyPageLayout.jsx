import {
  useEffect,
  useState,
} from 'react'

import {
  NavLink,
  Outlet,
  useLocation,
} from 'react-router-dom'

import {
  getCurrentUser,
} from '../api/apiClient.js'

import './MyPageLayout.css'


const myPageMenuGroups = [
  {
    title: '마이페이지',
    items: [
      {
        label: 'HOME',
        to: '/mypage',
        end: true,
      },
    ],
  },
  {
    title: '헌혈내역',
    items: [
      {
        label: '헌혈기록',
        to: '/mypage/records',
      }
    ],
  },
  {
    title: '포인트',
    items: [
      {
        label: '내 포인트',
        to: '/mypage/points',
      },
      {
        label: '상품권·교환 내역',
        to: '/mypage/rewards',
      },
    ],
  },
  {
    title: '예약관리',
    items: [
      {
        label: '헌혈예약 내역',
        to: '/reservations',
      },
      {
        label: '새 헌혈 예약',
        to: '/reservations/new',
      },
    ],
  },
]

const medicalMenuGroup = {
  title: '의료진 업무',
  items: [
    { label: '의료진 HOME', to: '/mypage/medical', end: true },
    { label: '당일 예약자', to: '/medical/today-reservations' },
    { label: '전자문진 목록', to: '/medical/questionnaires' },
    { label: '헌혈자 신분 확인', to: '/medical/donor_identity' },
    { label: '전체 헌혈 기록', to: '/medical/donation_history' },
    { label: '혈액 재고 관리', to: '/medical/blood_units_management' },
  ],
}


function getCurrentPage(
  pathname
) {
  const medicalPages = {
    '/mypage/medical': '의료진 HOME',
    '/medical/today-reservations': '당일 예약자',
    '/medical/questionnaires': '전자문진 목록',
    '/medical/donor_identity': '헌혈자 신분 확인',
    '/medical/donation_history': '전체 헌혈 기록',
    '/medical/blood_units_management': '혈액 재고 관리',
  }
  if (pathname.startsWith('/medical/donor_check/')) return { group: '의료진 업무', title: '헌혈자 정보 확인' }
  if (medicalPages[pathname]) return { group: '의료진 업무', title: medicalPages[pathname] }
  if (
    pathname === '/mypage/records'
  ) {
    return {
      group: '헌혈내역',
      title: '헌혈기록',
    }
  }

  if (
    pathname === '/mypage/points'
  ) {
    return {
      group: '포인트',
      title: '내 포인트',
    }
  }

  if (pathname === '/mypage/rewards') {
    return { group: '포인트', title: '상품권·교환 내역' }
  }

  if (
    pathname === '/mypage/results'
  ) {
    return {
      group: '헌혈내역',
      title: '검사결과',
    }
  }

  if (
    pathname === '/reservations'
  ) {
    return {
      group: '예약관리',
      title: '헌혈예약 내역',
    }
  }

  return {
    group: '',
    title: 'HOME',
  }
}


function MyPageLayout() {
  const location = useLocation()

  const [
    canAccessMedical,
    setCanAccessMedical,
  ] = useState(false)

  const currentPage =
    getCurrentPage(
      location.pathname
    )


  useEffect(() => {
    let active = true

    getCurrentUser()
      .then((user) => {
        if (!active) {
          return
        }

        const canAccess =
          user.roles?.some(
            (role) =>
              role === 'ROLE_MEDICAL'
              || role === 'ROLE_ADMIN'
          ) ?? false

        setCanAccessMedical(
          canAccess
        )
      })
      .catch(() => {
        if (active) {
          setCanAccessMedical(false)
        }
      })

    return () => {
      active = false
    }
  }, [])


  return (
    <main className="mypage-shell">

      <nav
        className="mypage-breadcrumb"
        aria-label="현재 위치"
      >
        <NavLink to="/">
          홈
        </NavLink>

        <span aria-hidden="true">
          ›
        </span>

        <NavLink to="/mypage">
          마이페이지
        </NavLink>

        {currentPage.group && (
          <>
            <span aria-hidden="true">
              ›
            </span>

            <span>
              {currentPage.group}
            </span>
          </>
        )}

        {currentPage.title
          !== 'HOME' && (
          <>
            <span aria-hidden="true">
              ›
            </span>

            <strong>
              {currentPage.title}
            </strong>
          </>
        )}
      </nav>


      <div className="mypage-layout">

        <aside className="mypage-sidebar">
          <div className="mypage-sidebar-heading">
            <span>
              MEMBER
            </span>

            <h1>
              마이페이지
            </h1>
          </div>


          <nav
            className="mypage-sidebar-navigation"
            aria-label="마이페이지 메뉴"
          >
            {(canAccessMedical ? [...myPageMenuGroups, medicalMenuGroup] : myPageMenuGroups).map(
              (group) => (
                <section
                  key={group.title}
                  className="mypage-menu-group"
                >
                  <h2>
                    {group.title}
                  </h2>

                  <div>
                    {group.items.map(
                      (item) => {
                        if (item.disabled) {
                          return (
                            <span
                              key={item.to}
                              className="mypage-menu-link disabled"
                              aria-disabled="true"
                              title="현재 개발 예정인 기능입니다."
                            >
                              <span>
                                {item.label}
                              </span>

                              <span>
                                준비 중
                              </span>
                            </span>
                          )
                        }

                        return (
                          <NavLink
                            key={item.to}
                            to={item.to}
                            end={item.end}
                            className={({
                              isActive,
                            }) =>
                              isActive
                                ? 'mypage-menu-link active'
                                : 'mypage-menu-link'
                            }
                          >
                            <span>
                              {item.label}
                            </span>

                            <span
                              aria-hidden="true"
                            >
                              →
                            </span>
                          </NavLink>
                        )
                      }
                    )}
                  </div>
                </section>
              )
            )}
            </nav>

            {canAccessMedical && (
              <NavLink
                to="/mypage/medical"
                className="mypage-medical-link"
              >
                <span>
                  의료인
                </span>

                <span aria-hidden="true">
                  →
                </span>
              </NavLink>
            )}
          </aside>


        <section className="mypage-content">
          <header className="mypage-content-header">
            <span>
              MY BLOODOPS
            </span>

            <h2>
              {currentPage.title}
            </h2>
          </header>

          <Outlet />
        </section>

      </div>
    </main>
  )
}


export default MyPageLayout
