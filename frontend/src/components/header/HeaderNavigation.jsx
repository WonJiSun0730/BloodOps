import { Link } from 'react-router-dom'

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
      { label: '헌혈내역 조회', to: '/mypage/records' },
      { label: '검사결과 조회', to: '/mypage/results' },
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

function HeaderNavigation({
  activeMenu,
  onMenuEnter,
  onMenuLeave,
}) {
  const selectedMenu =
    activeMenu !== null
      ? menuItems[activeMenu]
      : null

  return (
    <div
      className="header-menu-area"
      onMouseLeave={onMenuLeave}
    >
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
              onMouseEnter={() => onMenuEnter(index)}
              onFocus={() => onMenuEnter(index)}
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
                <span>
                  BloodOps
                </span>

                <h2>
                  {selectedMenu.title}
                </h2>
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
                    onClick={onMenuLeave}
                  >
                    <span>
                      {item.label}
                    </span>

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
    </div>
  )
}

export default HeaderNavigation