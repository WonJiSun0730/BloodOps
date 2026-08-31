import {
  useEffect,
  useRef,
  useState,
} from 'react'

import {
  useNavigate,
} from 'react-router-dom'

import {
  getCenter,
  getCenterMapCenters,
  getCenterPage,
  getCurrentUser,
} from '../../api/apiClient.js'

import {
  findNearestCenter,
  getCurrentPosition,
} from '../../utils/geolocation.js'

import CenterMapArea
  from '../../components/center/CenterMapArea.jsx'

import ConfirmModal
  from '../../components/modal/ConfirmModal.jsx'

import './CenterSearchPage.css'

function CenterSearchPage() {
  const navigate = useNavigate()

  const [keyword, setKeyword] =
    useState('')

  const [
    hasSearched,
    setHasSearched,
  ] = useState(false)

  const [centers, setCenters] =
    useState([])

  const [
    mapCenters,
    setMapCenters,
  ] = useState([])

  const [
    totalElements,
    setTotalElements,
  ] = useState(0)

  const [page, setPage] =
    useState(0)

  const [
    hasNextPage,
    setHasNextPage,
  ] = useState(false)

  const [
    loadingMore,
    setLoadingMore,
  ] = useState(false)

  const [
    loadMoreError,
    setLoadMoreError,
  ] = useState('')

  const [
    activeKeyword,
    setActiveKeyword,
  ] = useState('')

  const requestIdRef =
    useRef(0)

  const detailRequestIdRef =
    useRef(0)

  const loadingMoreRef =
    useRef(false)

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

  const [
    loginModalOpen,
    setLoginModalOpen,
  ] = useState(false)

  const [
    reservationTarget,
    setReservationTarget,
  ] = useState(null)

   const [
     nearestCenter,
     setNearestCenter,
   ] = useState(null)

   const [
     locationLoading,
     setLocationLoading,
   ] = useState(false)

   const [
     locationError,
     setLocationError,
   ] = useState('')

  async function loadCenters(
    searchKeyword = '',
    selectFirst = false
  ) {
    const trimmedKeyword =
      searchKeyword.trim()

    const requestId =
      requestIdRef.current + 1

    requestIdRef.current =
      requestId

    /*
     * 이전 센터 상세 요청이 늦게 도착해
     * 새 검색 결과를 덮어쓰지 않도록 무효화합니다.
     */
    detailRequestIdRef.current += 1

    try {
      setLoading(true)
      setError('')
      setLoadMoreError('')
      setDetailError('')
      setSelectedCenter(null)

      setCenters([])
      setMapCenters([])
      setTotalElements(0)
      setPage(0)
      setHasNextPage(false)
      setActiveKeyword(
        trimmedKeyword
      )

      const [
        pageData,
        mapData,
      ] = await Promise.all([
        getCenterPage(
          trimmedKeyword,
          0
        ),
        getCenterMapCenters(
          trimmedKeyword
        ),
      ])

      if (
        requestId
        !== requestIdRef.current
      ) {
        return
      }

      const centerData =
        pageData.content || []

      setCenters(
        centerData
      )

      setMapCenters(
        mapData
      )

      setTotalElements(
        pageData.totalElements || 0
      )

      setPage(
        pageData.number || 0
      )

      setHasNextPage(
        pageData.last === false
      )

      if (
        selectFirst
        && centerData.length > 0
      ) {
        await handleCenterSelect(
          centerData[0].id
        )
      }
    } catch (error) {
      if (
        requestId
        !== requestIdRef.current
      ) {
        return
      }

      setCenters([])
      setMapCenters([])
      setSelectedCenter(null)

      setError(
        error.message
        || '헌혈센터 목록을 불러오지 못했습니다.'
      )
    } finally {
      if (
        requestId
        === requestIdRef.current
      ) {
        setLoading(false)
      }
    }
  }

  useEffect(() => {
    loadCenters()
  }, [])

  function handleSearch(
    event
  ) {
    event.preventDefault()

    const trimmedKeyword =
      keyword.trim()

    setHasSearched(
      Boolean(trimmedKeyword)
    )

    loadCenters(
      trimmedKeyword,
      Boolean(trimmedKeyword)
    )
  }

  async function handleCenterSelect(
    centerId
  ) {
    const detailRequestId =
      detailRequestIdRef.current + 1

    detailRequestIdRef.current =
      detailRequestId

    try {
      setDetailLoading(true)
      setDetailError('')

      const center =
        await getCenter(
          centerId
        )

      if (
        detailRequestId
        !== detailRequestIdRef.current
      ) {
        return
      }

      setSelectedCenter(
        center
      )
    } catch (error) {
      if (
        detailRequestId
        !== detailRequestIdRef.current
      ) {
        return
      }

      setSelectedCenter(null)

      setDetailError(
        error.message
        || '헌혈센터 상세 정보를 불러오지 못했습니다.'
      )
    } finally {
      if (
        detailRequestId
        === detailRequestIdRef.current
      ) {
        setDetailLoading(false)
      }
    }
  }

  async function loadMoreCenters() {
    if (
      loading
      || loadingMoreRef.current
      || !hasNextPage
    ) {
      return
    }

    /*
     * 스크롤 이벤트가 짧은 시간에 여러 번 발생해도
     * 같은 페이지를 중복 요청하지 않도록 차단합니다.
     */
    loadingMoreRef.current =
      true

    setLoadingMore(true)
    setLoadMoreError('')

    const requestId =
      requestIdRef.current

    const nextPage =
      page + 1

    try {
      const pageData =
        await getCenterPage(
          activeKeyword,
          nextPage
        )

      /*
       * 추가 조회 도중 새 검색을 시작했다면
       * 이전 검색 결과를 현재 목록에 추가하지 않습니다.
       */
      if (
        requestId
        !== requestIdRef.current
      ) {
        return
      }

      setCenters(
        (previous) => {
          const knownIds =
            new Set(
              previous.map(
                (center) =>
                  center.id
              )
            )

          const additionalCenters =
            (pageData.content || [])
              .filter(
                (center) =>
                  !knownIds.has(
                    center.id
                  )
              )

          return [
            ...previous,
            ...additionalCenters,
          ]
        }
      )

      setPage(
        pageData.number
      )

      setHasNextPage(
        pageData.last === false
      )
    } catch (error) {
      if (
        requestId
        === requestIdRef.current
      ) {
        setLoadMoreError(
          error.message
          || '센터 목록을 추가로 불러오지 못했습니다.'
        )
      }
    } finally {
      loadingMoreRef.current =
        false

      if (
        requestId
        === requestIdRef.current
      ) {
        setLoadingMore(false)
      }
    }
  }


  function handleCenterListScroll(
    event
  ) {
    const list =
      event.currentTarget

    const remainingDistance =
      list.scrollHeight
      - list.scrollTop
      - list.clientHeight

    if (remainingDistance <= 120) {
      loadMoreCenters()
    }
  }



  async function handleReservationClick() {
    if (
      !selectedCenter
      || selectedCenter.active === false
    ) {
      return
    }

    const reservationPath =
      `/reservations/new?centerId=${selectedCenter.id}`

    try {
      await getCurrentUser()

      navigate(
        reservationPath
      )
    } catch {
      setReservationTarget({
        pathname:
          '/reservations/new',
        search:
          `?centerId=${selectedCenter.id}`,
      })

      setLoginModalOpen(true)
    }
  }

  function handleConfirmLogin() {
    if (!reservationTarget) {
      return
    }

    setLoginModalOpen(false)

    navigate(
      '/login',
      {
        state: {
          from:
            reservationTarget,
        },
      }
    )
  }

  function handleCloseLoginModal() {
    setLoginModalOpen(false)
    setReservationTarget(null)
  }

  async function handleFindNearestCenter() {
    try {
      setLocationLoading(true)
      setLocationError('')

      const position =
        await getCurrentPosition()

      const centerData =
        await getCenterMapCenters()

      const nearest =
        findNearestCenter(
          centerData,
          position.latitude,
          position.longitude
        )

      if (!nearest) {
        setNearestCenter(null)

        setLocationError(
          '위치 정보가 등록된 운영 센터를 찾지 못했습니다.'
        )

        return
      }

      setNearestCenter(nearest)

      setKeyword('')
      setHasSearched(false)

      await loadCenters('')

      await handleCenterSelect(
        nearest.id
      )
    } catch (error) {
      setNearestCenter(null)

      setLocationError(
        error.message
        || '가까운 헌혈의 집을 찾지 못했습니다.'
      )
    } finally {
      setLocationLoading(false)
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
                      && !error && (
                      <div
                        className="center-search-results"
                        onScroll={
                          handleCenterListScroll
                        }
                      >
                        <p className="center-search-count">
                          총 {totalElements}개 센터
                        </p>

                        {centers.length === 0 && (
                          <p className="center-search-empty">
                            검색된 헌혈센터가 없습니다.
                          </p>
                        )}

                        {centers.map(
                          (center) => (
                            <article
                              key={center.id}
                              className={
                                selectedCenter?.id
                                  === center.id
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
                                <div className="center-list-title">
                                  <strong>
                                    {center.centerName}
                                  </strong>

                                  <span
                                    className={
                                      center.active === false
                                        ? 'center-status-badge inactive'
                                        : 'center-status-badge active'
                                    }
                                  >
                                    {center.active === false
                                      ? '운영 중단'
                                      : '운영 중'}
                                  </span>
                                </div>

                                <p>
                                  {center.address}
                                </p>

                                <span className="center-list-phone">
                                  {center.phone
                                    || '전화번호 정보 없음'}
                                </span>
                              </button>
                            </article>
                          )
                        )}

                        {loadingMore && (
                          <p className="center-list-loading">
                            센터를 더 불러오는 중입니다.
                          </p>
                        )}

                        {loadMoreError && (
                          <button
                            type="button"
                            className="center-list-retry"
                            onClick={
                              loadMoreCenters
                            }
                          >
                            추가 목록 다시 불러오기
                          </button>
                        )}

                        {!hasNextPage
                          && centers.length > 0 && (
                          <p className="center-list-end">
                            모든 센터를 불러왔습니다.
                          </p>
                        )}

                      </div>
                    )}

        </div>

        <aside className="center-search-detail-area">

          <div className="center-detail-panel">

          {detailLoading && (
            <div className="center-detail-status">
              <strong>
                센터 정보를 불러오는 중입니다.
              </strong>

              <p>
                잠시만 기다려주세요.
              </p>
            </div>
          )}

          {detailError && (
            <div
              className="center-detail-status error"
              role="alert"
            >
              <strong>
                센터 정보를 불러오지 못했습니다.
              </strong>

              <p>
                {detailError}
              </p>
            </div>
          )}

            {!detailLoading
              && !detailError
              && !selectedCenter && (
              <div className="center-search-placeholder">
                <span aria-hidden="true">
                  +
                </span>

                <strong>
                  {hasSearched
                    && totalElements === 0
                    ? '검색 결과가 없습니다.'
                    : '가장 가까운 곳을 찾으시나요?'}
                </strong>

                <p>
                  {hasSearched
                    && totalElements === 0
                    ? '다른 센터명이나 주소로 다시 검색하거나 현재 위치를 기준으로 가까운 센터를 찾아보세요.'
                    : '센터명이나 주소를 검색하거나 현재 위치를 기준으로 가까운 헌혈의 집을 확인할 수 있습니다.'}
                </p>

                <button
                  type="button"
                  className="center-location-button"
                  onClick={
                    handleFindNearestCenter
                  }
                  disabled={locationLoading}
                >
                  {locationLoading
                    ? '현재 위치 확인 중...'
                    : '현재 위치로 확인하기'}
                </button>

                {locationError && (
                  <p
                    className="center-location-error"
                    role="alert"
                  >
                    {locationError}
                  </p>
                )}
              </div>
            )}

        {!detailLoading
          && !detailError
          && selectedCenter && (
          <div className="center-detail-content">

            <div className="center-detail-title-row">
              <h2>
                {selectedCenter.centerName}
              </h2>

              {nearestCenter?.id === selectedCenter.id && (
                <span className="center-nearest-distance">
                  약 {
                    nearestCenter.distanceKm < 1
                      ? `${Math.round(
                          nearestCenter.distanceKm * 1000
                        )}m`
                      : `${nearestCenter.distanceKm.toFixed(1)}km`
                  }
                </span>
              )}
            </div>

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

            <button
              type="button"
              disabled={
                selectedCenter.active === false
              }
              onClick={
                handleReservationClick
              }
            >
              {selectedCenter.active === false
                ? '현재 예약할 수 없는 센터입니다'
                : '이 센터에서 예약하기'}
            </button>

          </div>
        )}

          </div>

          <CenterMapArea
            centers={mapCenters}
            selectedCenter={selectedCenter}
            onCenterSelect={handleCenterSelect}
            defaultViewport={
              !hasSearched
              || mapCenters.length === 0
            }
          />

        </aside>

      </section>

      <ConfirmModal
              open={loginModalOpen}
              title="로그인이 필요합니다"
              message="로그인 후 예약 가능합니다. 진행하시겠습니까?"
              confirmText="예"
              cancelText="아니오"
              onConfirm={
                handleConfirmLogin
              }
              onCancel={
                handleCloseLoginModal
              }
            />

    </main>
  )
}

export default CenterSearchPage