import {
  Link,
} from 'react-router-dom'

import './MyPagePages.css'


const myPageServices = [
  {
    title: '헌혈기록',
    description:
      '완료된 헌혈 참여 기록을 확인합니다.',
    to: '/mypage/records',
  },
  {
    title: '검사결과',
    description:
      '헌혈 후 등록된 혈액검사 결과를 확인합니다.',
    to: '/mypage/results',
  },
  {
    title: '헌혈예약 내역',
    description:
      '예약 일정의 조회·변경·취소를 관리합니다.',
    to: '/reservations',
  },
]


function MyPageHomePage() {
  return (
    <div className="mypage-page">

      <section className="mypage-intro-card">
        <div>
          <span>
            MY ACTIVITY
          </span>

          <h3>
            나의 헌혈 활동
          </h3>

          <p>
            헌혈기록과 검사결과,
            예약 내역을 한곳에서 확인할 수 있습니다.
          </p>
        </div>
      </section>


      <section
        className="mypage-service-grid"
        aria-label="마이페이지 주요 서비스"
      >
        {myPageServices.map(
          (service) => (
            <Link
              key={service.to}
              to={service.to}
              className="mypage-service-card"
            >
              <div>
                <strong>
                  {service.title}
                </strong>

                <p>
                  {service.description}
                </p>
              </div>

              <span aria-hidden="true">
                →
              </span>
            </Link>
          )
        )}
      </section>

    </div>
  )
}


export default MyPageHomePage