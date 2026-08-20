import { useMemo, useState } from 'react'

import './AdminReservationsPage.css'


const reservationMockData = [
  {
    id: 1,
    memberName: '김회원',
    centerName: '강남 헌혈의 집',
    date: '2026.08.20',
    time: '10:30',
    type: '전혈',
    status: '예약완료',
  },
  {
    id: 2,
    memberName: '이회원',
    centerName: '홍대 헌혈의 집',
    date: '2026.08.20',
    time: '13:00',
    type: '혈장',
    status: '예약완료',
  },
  {
    id: 3,
    memberName: '박회원',
    centerName: '부평 헌혈의 집',
    date: '2026.08.21',
    time: '15:30',
    type: '전혈',
    status: '취소',
  },
]


const reservationStatuses = [
  '전체',
  '예약완료',
  '취소',
]


function AdminReservationsPage() {
  const [keyword, setKeyword] = useState('')
  const [statusFilter, setStatusFilter] =
    useState('전체')


  const filteredReservations = useMemo(() => {
    const normalizedKeyword =
      keyword.trim().toLowerCase()

    return reservationMockData.filter(
      (reservation) => {
        const matchesKeyword =
          !normalizedKeyword
          ||
          reservation.memberName
            .toLowerCase()
            .includes(normalizedKeyword)
          ||
          reservation.centerName
            .toLowerCase()
            .includes(normalizedKeyword)

        const matchesStatus =
          statusFilter === '전체'
          ||
          reservation.status === statusFilter

        return matchesKeyword && matchesStatus
      }
    )
  }, [keyword, statusFilter])


  return (
    <main className="admin-reservations-page">

      <section className="admin-reservations-header">

        <div>
          <span className="admin-page-eyebrow">
            ADMIN / RESERVATION
          </span>

          <h1>
            예약 관리
          </h1>

          <p>
            헌혈 예약 현황을 조회하고
            예약 상태를 관리합니다.
          </p>
        </div>

      </section>


      <section className="admin-reservation-toolbar">

        <div className="admin-reservation-search">
          <input
            type="search"
            value={keyword}
            onChange={(event) =>
              setKeyword(event.target.value)
            }
            placeholder="회원명 또는 센터명 검색"
            aria-label="예약 검색"
          />
        </div>


        <div
          className="admin-reservation-filter"
          aria-label="예약 상태 필터"
        >
          {reservationStatuses.map((status) => (
            <button
              key={status}
              type="button"
              className={
                statusFilter === status
                  ? 'active'
                  : ''
              }
              onClick={() =>
                setStatusFilter(status)
              }
            >
              {status}
            </button>
          ))}
        </div>


        <span className="admin-reservation-count">
          총 {filteredReservations.length}건
        </span>

      </section>


      <section className="admin-reservation-list">

        <div className="admin-reservation-list-header">
          <span>회원</span>
          <span>헌혈센터</span>
          <span>예약일</span>
          <span>시간</span>
          <span>헌혈종류</span>
          <span>상태</span>
          <span>관리</span>
        </div>


        {filteredReservations.map(
          (reservation) => (
            <article
              key={reservation.id}
              className="admin-reservation-row"
            >
              <strong>
                {reservation.memberName}
              </strong>

              <span>
                {reservation.centerName}
              </span>

              <span>
                {reservation.date}
              </span>

              <span>
                {reservation.time}
              </span>

              <span>
                {reservation.type}
              </span>

              <span
                className={
                  reservation.status === '예약완료'
                    ? 'admin-reservation-status confirmed'
                    : 'admin-reservation-status cancelled'
                }
              >
                {reservation.status}
              </span>

              <button
                type="button"
                className="admin-row-action"
              >
                상세
              </button>
            </article>
          )
        )}

      </section>

    </main>
  )
}


export default AdminReservationsPage