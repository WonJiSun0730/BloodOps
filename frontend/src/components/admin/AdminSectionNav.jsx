import { NavLink } from 'react-router-dom'

import './AdminSectionNav.css'


const adminSections = [
    {
        to: '/admin/centers',
        label: '헌혈센터 관리',
    },
    {
        to: '/admin/members',
        label: '회원 및 권한 관리',
    },
    {
        to: '/admin/reservations',
        label: '예약 관리',
    },
    {
        to: '/admin/reward-products',
        label: '상품·재고 관리',
    },
    {
        to: '/admin/reward-fulfillment',
        label: '상품권·수령 관리',
    },
    {
        to: '/admin/kakao-message-template',
        label: '카카오 예약 알림',
    },
]


function AdminSectionNav() {
    return (
        <nav
            className="admin-section-nav"
            aria-label="관리자 기능 이동"
        >
            {adminSections.map((section) => (
                <NavLink
                  key={section.to}
                  to={section.to}
                  className={({ isActive }) =>
                    isActive
                      ? 'admin-section-nav__link active'
                      : 'admin-section-nav__link'
                  }
                >
                  <span>
                    {section.label}
                  </span>

                  <span
                    className="admin-section-nav__arrow"
                    aria-hidden="true"
                  >
                    ›
                  </span>
                </NavLink>
            ))}
        </nav>
    )
}


export default AdminSectionNav
