import { useMemo, useState } from 'react'

import './AdminCentersPage.css'


const centerMockData = [
  {
    id: 1,
    name: '강남 헌혈의 집',
    region: '서울 강남구',
    status: '운영중',
    phone: '02-0000-0001',
  },
  {
    id: 2,
    name: '홍대 헌혈의 집',
    region: '서울 마포구',
    status: '운영중',
    phone: '02-0000-0002',
  },
  {
    id: 3,
    name: '부평 헌혈의 집',
    region: '인천 부평구',
    status: '점검중',
    phone: '032-000-0003',
  },
]


function AdminCentersPage() {
  const [keyword, setKeyword] = useState('')


  const filteredCenters = useMemo(() => {
    const normalizedKeyword =
      keyword.trim().toLowerCase()

    if (!normalizedKeyword) {
      return centerMockData
    }

    return centerMockData.filter((center) => {
      return (
        center.name
          .toLowerCase()
          .includes(normalizedKeyword)
        ||
        center.region
          .toLowerCase()
          .includes(normalizedKeyword)
      )
    })
  }, [keyword])


  return (
    <main className="admin-centers-page">

      <section className="admin-centers-header">

        <div>
          <span className="admin-page-eyebrow">
            ADMIN / CENTER
          </span>

          <h1>
            헌혈센터 관리
          </h1>

          <p>
            헌혈센터의 기본 정보와 운영 상태를
            조회하고 관리합니다.
          </p>
        </div>

        <button
          type="button"
          className="admin-primary-button"
        >
          + 센터 등록
        </button>

      </section>


      <section className="admin-centers-toolbar">

        <div className="admin-center-search">
          <input
            type="search"
            value={keyword}
            onChange={(event) =>
              setKeyword(event.target.value)
            }
            placeholder="센터명 또는 지역 검색"
            aria-label="헌혈센터 검색"
          />
        </div>

        <span className="admin-center-count">
          총 {filteredCenters.length}개
        </span>

      </section>


      <section className="admin-center-list">

        <div className="admin-center-list-header">
          <span>센터명</span>
          <span>지역</span>
          <span>연락처</span>
          <span>상태</span>
          <span>관리</span>
        </div>


        {filteredCenters.map((center) => (
          <article
            key={center.id}
            className="admin-center-row"
          >
            <strong>
              {center.name}
            </strong>

            <span>
              {center.region}
            </span>

            <span>
              {center.phone}
            </span>

            <span
              className={
                center.status === '운영중'
                  ? 'admin-center-status active'
                  : 'admin-center-status inspection'
              }
            >
              {center.status}
            </span>

            <button
              type="button"
              className="admin-row-action"
            >
              수정
            </button>
          </article>
        ))}

      </section>

    </main>
  )
}


export default AdminCentersPage