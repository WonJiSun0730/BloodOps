import { Link } from 'react-router-dom'

import './AdminPage.css'


const adminMenus = [
  {
    id: 'centers',
    eyebrow: 'CENTER',
    title: '헌혈센터 관리',
    description:
      '헌혈센터 정보를 조회하고 운영 정보를 관리합니다.',
    to: '/admin/centers',
  },
  {
    id: 'reservations',
    eyebrow: 'RESERVATION',
    title: '예약 관리',
    description:
      '헌혈 예약 현황을 조회하고 예약 상태를 관리합니다.',
    to: '/admin/reservations',
  },
  {
    id: 'permissions',
    eyebrow: 'ACCESS',
    title: '회원 및 권한 관리',
    description:
      '회원 정보를 조회하고 서비스 접근 권한을 관리합니다.',
    to: '/admin/members',
  },
  {
    id: 'kakao-message',
    eyebrow: 'KAKAO MESSAGE',
    title: '카카오 예약 알림 관리',
    description:
      '예약 완료 시 나에게 보내는 카카오 메시지 내용과 발송 여부를 관리합니다.',
    to: '/admin/kakao-message-template',
  },
]


function AdminPage() {
  return (
    <main className="admin-page">

      <section className="admin-page-header">

        <div>
          <span className="admin-page-eyebrow">
            ADMIN
          </span>

          <h1>
            관리자 대시보드
          </h1>

          <p>
            BloodOps 서비스 운영에 필요한
            관리 기능을 확인할 수 있습니다.
          </p>
        </div>

      </section>

      <section className="admin-summary">

        <div className="admin-summary-card">
          <span>등록 센터</span>

          <strong>—</strong>

          <small>
            센터 관리 데이터 연결 예정
          </small>
        </div>


        <div className="admin-summary-card">
          <span>오늘 예약</span>

          <strong>—</strong>

          <small>
            예약 데이터 연결 예정
          </small>
        </div>


        <div className="admin-summary-card">
          <span>전체 회원</span>

          <strong>—</strong>

          <small>
            회원 데이터 연결 예정
          </small>
        </div>

      </section>

      <section className="admin-menu-grid">

        {adminMenus.map((menu) => (
          <Link
            key={menu.id}
            to={menu.to}
            className="admin-menu-card"
          >
            <span className="admin-menu-eyebrow">
              {menu.eyebrow}
            </span>

            <h2>
              {menu.title}
            </h2>

            <p>
              {menu.description}
            </p>

            <span
              className="admin-menu-arrow"
              aria-hidden="true"
            >
              →
            </span>
          </Link>
        ))}

      </section>

    </main>
  )
}


export default AdminPage
