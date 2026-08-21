import {
  useEffect,
  useState,
} from 'react'

import {
  getCenter,
  getCenters,
} from '../../api/apiClient.js'

import CenterMapArea
  from '../../components/center/CenterMapArea.jsx'

import './CenterSearchPage.css'

function CenterSearchPage() {
  const [keyword, setKeyword] =
    useState('')

  const [centers, setCenters] =
    useState([])

  const [loading, setLoading] =
    useState(true)

  const [error, setError] =
    useState('')

  const [
    selectedCenter,
    setSelectedCenter,
  ] = useState(null)

  const [
    detailLoading,
    setDetailLoading,
  ] = useState(false)

  const [
    detailError,
    setDetailError,
  ] = useState('')

  async function loadCenters(
    searchKeyword = ''
  ) {
    try {
      setLoading(true)
      setError('')

      const centerData =
        await getCenters(
          searchKeyword
        )

      setCenters(centerData)
    } catch (error) {
      setCenters([])

      setError(
        error.message
        || '헌혈센터 목록을 불러오지 못했습니다.'
      )
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadCenters()
  }, [])

  function handleSearch(
    event
  ) {
    event.preventDefault()

    loadCenters(
      keyword
    )
  }

  async function handleCenterSelect(
    centerId
  ) {
    try {
      setDetailLoading(true)
      setDetailError('')

      const center =
        await getCenter(
          centerId
        )

      setSelectedCenter(
        center
      )
    } catch (error) {
      setSelectedCenter(null)

      setDetailError(
        error.message
        || '헌혈센터 상세 정보를 불러오지 못했습니다.'
      )
    } finally {
      setDetailLoading(false)
    }
  }

  return (
    <main className="center-search-page">
      <section className="center-search-header">
        <span className="center-search-eyebrow">
          BLOOD DONATION CENTER
        </span>

        <h1>
          헌혈의 집 찾기
        </h1>

        <p>
          가까운 헌혈센터를 검색하고
          운영 정보를 확인할 수 있습니다.
        </p>
      </section>

      <section className="center-search-layout">

        <div className="center-search-list-area">

          <form
            onSubmit={handleSearch}
          >
            <input
              type="search"
              value={keyword}
              onChange={(event) =>
                setKeyword(
                  event.target.value
                )
              }
              placeholder="센터명 또는 주소 검색"
            />

            <button
              type="submit"
              disabled={loading}
            >
              {loading
                ? '조회 중...'
                : '검색'}
            </button>
          </form>

          {error && (
            <p>
              {error}
            </p>
          )}

          {!loading
            && !error
            && centers.length === 0 && (
            <p>
              검색된 헌혈센터가 없습니다.
            </p>
          )}

          {!loading
            && !error
            && centers.length > 0 && (
            <div>
              <p>
                총 {centers.length}개 센터
              </p>

              {centers.map(
                (center) => (
                <article
                  key={center.id}
                  className={
                    selectedCenter?.id === center.id
                      ? 'selected'
                      : ''
                  }
                >
                  <button
                    type="button"
                    onClick={() =>
                      handleCenterSelect(
                        center.id
                      )
                    }
                  >
                    <strong>
                      {center.centerName}
                    </strong>

                    <p>
                      {center.address}
                    </p>

                    <span>
                      {center.phone
                        || '전화번호 정보 없음'}
                    </span>
                  </button>
                </article>
              ))}
            </div>
          )}

        </div>

        <aside className="center-search-detail-area">

          {detailLoading && (
            <p>
              센터 정보를 불러오는 중입니다.
            </p>
          )}

          {detailError && (
            <p>
              {detailError}
            </p>
          )}

          {!detailLoading
            && !detailError
            && !selectedCenter && (
            <div className="center-search-placeholder">
              센터를 선택하면 상세 정보가 표시됩니다.
            </div>
          )}

          {!detailLoading
            && !detailError
            && selectedCenter && (
            <div className="center-detail-content">

              <h2>
                {selectedCenter.centerName}
              </h2>

              <p>
                {selectedCenter.address}
              </p>

              <dl>
                <div>
                  <dt>전화번호</dt>
                  <dd>
                    {selectedCenter.phone
                      || '정보 없음'}
                  </dd>
                </div>

                <div>
                  <dt>평일 운영시간</dt>
                  <dd>
                    {selectedCenter.weekdayHours
                      || '정보 없음'}
                  </dd>
                </div>

                <div>
                  <dt>토요일 운영시간</dt>
                  <dd>
                    {selectedCenter.saturdayHours
                      || '정보 없음'}
                  </dd>
                </div>

                <div>
                  <dt>일요일 운영시간</dt>
                  <dd>
                    {selectedCenter.sundayHours
                      || '정보 없음'}
                  </dd>
                </div>

                <div>
                  <dt>공휴일 운영시간</dt>
                  <dd>
                    {selectedCenter.holidayHours
                      || '정보 없음'}
                  </dd>
                </div>

                <div>
                  <dt>헌혈 종류</dt>
                  <dd>
                    {selectedCenter.donationTypes
                      || '정보 없음'}
                  </dd>
                </div>

                <div>
                  <dt>편의정보</dt>
                  <dd>
                    {selectedCenter.amenities
                      || '정보 없음'}
                  </dd>
                </div>

                <div>
                  <dt>찾아오는 길</dt>
                  <dd>
                    {selectedCenter.directions
                      || '정보 없음'}
                  </dd>
                </div>

                <div>
                  <dt>주차정보</dt>
                  <dd>
                    {selectedCenter.parkingInfo
                      || '정보 없음'}
                  </dd>
                </div>
              </dl>

            </div>
          )}

          <CenterMapArea
            center={selectedCenter}
          />

        </aside>

      </section>
    </main>
  )
}

export default CenterSearchPage