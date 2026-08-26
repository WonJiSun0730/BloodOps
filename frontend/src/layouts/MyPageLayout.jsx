import {
  NavLink,
  Outlet,
  useLocation,
} from 'react-router-dom'

import './MyPageLayout.css'


const myPageMenuGroups = [
  {
    title: '마이페이지',
    items: [
      {
        label: '마이페이지 홈',
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
      },
      {
        label: '검사결과',
        to: '/mypage/results',
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


function getCurrentPage(
  pathname
) {
  if (
    pathname === '/mypage/records'
  ) {
    return {
      group: '헌혈내역',
      title: '헌혈기록',
    }
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
    title: '마이페이지',
  }
}


function MyPageLayout() {
  const location = useLocation()

  const currentPage =
    getCurrentPage(
      location.pathname
    )


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
          !== '마이페이지' && (
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
            {myPageMenuGroups.map(
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
                      (item) => (
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
                    )}
                  </div>
                </section>
              )
            )}
          </nav>
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