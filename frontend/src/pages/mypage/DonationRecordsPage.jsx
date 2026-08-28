import {
  useEffect,
  useState,
} from 'react'

import {
  addFavoriteCenter,
  getFavoriteCenters,
  getMyDonationEligibility,
  getMyDonationHistory,
  removeFavoriteCenter,
} from '../../api/apiClient.js'


const PERIOD_OPTIONS = [
  { value: 'WEEK', label: '1주일' },
  { value: 'MONTH', label: '1개월' },
  { value: 'THREE_MONTHS', label: '3개월' },
  { value: 'SIX_MONTHS', label: '6개월' },
  { value: 'YEAR', label: '1년' },
  { value: 'ALL', label: '전체' },
]

const donationTypeLabels = {
  WHOLE_BLOOD: '전혈',
  PLATELET: '혈소판',
  PLASMA: '혈장',
  PLATELET_PLASMA: '혈소판혈장',
}


function DonationRecordsPage() {
  const [
    records,
    setRecords,
  ] = useState([])

  const [
    eligibility,
    setEligibility,
  ] = useState(null)

  const [
    loading,
    setLoading,
  ] = useState(true)

  const [
    error,
    setError,
  ] = useState('')

  const [periodFilter, setPeriodFilter] =
    useState('ALL')

  const [sortConfig, setSortConfig] =
    useState({
      key: 'donationDate',
      direction: 'desc',
    })

  const [
    favoriteCenterIds,
    setFavoriteCenterIds,
  ] = useState([])

  const [
    favoriteLoadingCenterId,
    setFavoriteLoadingCenterId,
  ] = useState(null)


  useEffect(() => {
    let active = true

    async function loadRecords() {
      try {
        setLoading(true)
        setError('')

        const [
          historyData,
          eligibilityData,
          favoriteData,
        ] = await Promise.all([
          getMyDonationHistory(),
          getMyDonationEligibility(),
          getFavoriteCenters(),
        ])

        if (active) {
          setRecords(
            Array.isArray(historyData)
              ? historyData
              : []
          )

          setEligibility(
            eligibilityData
              || null
          )

          setFavoriteCenterIds(
            Array.isArray(favoriteData)
              ? favoriteData.map(
                  (favorite) =>
                    favorite.centerId
                )
              : []
          )
        }
      } catch (error) {
        if (active) {
          setRecords([])
          setEligibility(null)

          setError(
            error.message
            || '헌혈기록을 불러오지 못했습니다.'
          )
        }
      } finally {
        if (active) {
          setLoading(false)
        }
      }
    }

    loadRecords()

    return () => {
      active = false
    }
  }, [])

  const getPeriodStartDate = () => {
    const today = new Date()

    switch (periodFilter) {
      case 'WEEK':
        today.setDate(today.getDate() - 7)
        return today

      case 'MONTH':
        today.setMonth(today.getMonth() - 1)
        return today

      case 'THREE_MONTHS':
        today.setMonth(today.getMonth() - 3)
        return today

      case 'SIX_MONTHS':
        today.setMonth(today.getMonth() - 6)
        return today

      case 'YEAR':
        today.setFullYear(today.getFullYear() - 1)
        return today

      default:
        return null
    }
  }


  const filteredRecords =
    records.filter((record) => {
      const startDate =
        getPeriodStartDate()

      if (!startDate) {
        return true
      }

      const donationDate =
        new Date(
          `${record.donationDate}T00:00:00`
        )

      return donationDate >= startDate
    })


  const sortedRecords = [
    ...filteredRecords,
  ].sort((a, b) => {
    const aValue =
      a[sortConfig.key] ?? ''

    const bValue =
      b[sortConfig.key] ?? ''

    const result =
      String(aValue).localeCompare(
        String(bValue),
        'ko',
        {
          numeric: true,
        }
      )

    return (
      sortConfig.direction === 'asc'
        ? result
        : -result
    )
  })


  const handleSort = (key) => {
    setSortConfig((current) => ({
      key,
      direction:
        current.key === key
        && current.direction === 'asc'
          ? 'desc'
          : 'asc',
    }))
  }


  const getSortMark = (key) => {
    if (sortConfig.key !== key) {
      return '↕'
    }

    const handleFavoriteToggle =
      async (centerId) => {
        if (!centerId) {
          return
        }

        if (
          favoriteLoadingCenterId
          === centerId
        ) {
          return
        }

        const isFavorite =
          favoriteCenterIds.includes(
            centerId
          )

        try {
          setFavoriteLoadingCenterId(
            centerId
          )

          if (isFavorite) {
            await removeFavoriteCenter(
              centerId
            )

            setFavoriteCenterIds(
              (current) =>
                current.filter(
                  (id) =>
                    id !== centerId
                )
            )
          } else {
            await addFavoriteCenter(
              centerId
            )

            setFavoriteCenterIds(
              (current) => [
                ...current,
                centerId,
              ]
            )
          }
        } catch (error) {
          window.alert(
            error.message
            || '즐겨찾기 변경에 실패했습니다.'
          )
        } finally {
          setFavoriteLoadingCenterId(
            null
          )
        }
      }

    return (
      sortConfig.direction === 'asc'
        ? '↑'
        : '↓'
    )
  }

  const handleFavoriteToggle = async (centerId) => {
    if (!centerId) {
      return
    }

    if (favoriteLoadingCenterId === centerId) {
      return
    }

    const isFavorite =
      favoriteCenterIds.includes(centerId)

    try {
      setFavoriteLoadingCenterId(centerId)

      if (isFavorite) {
        await removeFavoriteCenter(centerId)

        setFavoriteCenterIds((current) =>
          current.filter(
            (id) => id !== centerId
          )
        )
      } else {
        await addFavoriteCenter(centerId)

        setFavoriteCenterIds((current) => [
          ...current,
          centerId,
        ])
      }
    } catch (error) {
      window.alert(
        error.message
        || '즐겨찾기 변경에 실패했습니다.'
      )
    } finally {
      setFavoriteLoadingCenterId(null)
    }
  }

  return (
    <div className="mypage-page">
      {eligibility && (
        <section className="donation-eligibility-section">

          <div className="donation-eligibility-heading">
            <div>
              <span>
                NEXT DONATION
              </span>

              <h3>
                다음 헌혈 가능일
              </h3>

              <p>
                최근 헌혈 기록과 연간 횟수 제한을
                기준으로 계산됩니다.
              </p>
            </div>
          </div>


          <div className="donation-eligibility-grid">

            <article className="donation-eligibility-card">
              <span className="donation-eligibility-type">
                전혈
              </span>

              <strong>
                {
                  eligibility
                    .wholeBloodNextAvailableDate
                }
              </strong>

              <div className="donation-eligibility-meta">
                <span>
                  최근 1년
                </span>

                <b>
                  {
                    eligibility
                      .wholeBloodCountLastYear
                  }
                  {' / '}
                  {
                    eligibility
                      .wholeBloodAnnualLimit
                  }
                  회
                </b>
              </div>

              <div className="donation-eligibility-progress">
                <span
                  style={{
                    width: `${
                      Math.min(
                        100,
                        (
                          eligibility
                            .wholeBloodCountLastYear
                          / eligibility
                            .wholeBloodAnnualLimit
                        ) * 100
                      )
                    }%`,
                  }}
                />
              </div>
            </article>


            <article className="donation-eligibility-card">
              <span className="donation-eligibility-type">
                혈소판
              </span>

              <strong>
                {
                  eligibility
                    .plateletNextAvailableDate
                }
              </strong>

              <div className="donation-eligibility-meta">
                <span>
                  최근 1년
                </span>

                <b>
                  {
                    eligibility
                      .plateletCountLastYear
                  }
                  {' / '}
                  {
                    eligibility
                      .plateletAnnualLimit
                  }
                  회
                </b>
              </div>

              <div className="donation-eligibility-progress">
                <span
                  style={{
                    width: `${
                      Math.min(
                        100,
                        (
                          eligibility
                            .plateletCountLastYear
                          / eligibility
                            .plateletAnnualLimit
                        ) * 100
                      )
                    }%`,
                  }}
                />
              </div>
            </article>


            <article className="donation-eligibility-card">
              <span className="donation-eligibility-type">
                혈장
              </span>

              <strong>
                {
                  eligibility
                    .plasmaNextAvailableDate
                }
              </strong>

              <div className="donation-eligibility-meta">
                <span>
                  간격 제한
                </span>

                <b>
                  2주
                </b>
              </div>

              <div className="donation-eligibility-note">
                연간 횟수 제한 없음
              </div>
            </article>


            <article className="donation-eligibility-card">
              <span className="donation-eligibility-type">
                혈소판혈장
              </span>

              <strong>
                {
                  eligibility
                    .plateletPlasmaNextAvailableDate
                }
              </strong>

              <div className="donation-eligibility-meta">
                <span>
                  최근 1년
                </span>

                <b>
                  {
                    eligibility
                      .plateletCountLastYear
                  }
                  {' / '}
                  {
                    eligibility
                      .plateletAnnualLimit
                  }
                  회
                </b>
              </div>

              <div className="donation-eligibility-progress">
                <span
                  style={{
                    width: `${
                      Math.min(
                        100,
                        (
                          eligibility
                            .plateletCountLastYear
                          / eligibility
                            .plateletAnnualLimit
                        ) * 100
                      )
                    }%`,
                  }}
                />
              </div>
            </article>

          </div>

        </section>
      )}

      <section className="mypage-data-section">
        <header className="mypage-data-header">
          <div>
            <h3>
              헌혈기록
            </h3>

            <p>
              의료진의 헌혈 완료 처리 후
              헌혈기록이 표시됩니다.
            </p>
          </div>

          <div className="mypage-record-toolbar">
            <div
              className="mypage-period-filter"
              aria-label="헌혈기록 조회 기간"
            >
              {PERIOD_OPTIONS.map(
                (option) => (
                  <button
                    key={option.value}
                    type="button"
                    className={
                      periodFilter
                        === option.value
                        ? 'active'
                        : ''
                    }
                    onClick={() =>
                      setPeriodFilter(
                        option.value
                      )
                    }
                  >
                    {option.label}
                  </button>
                )
              )}
            </div>

            <span>
              총 {sortedRecords.length}건
            </span>
          </div>
        </header>


        {loading ? (
          <div className="mypage-empty-state standalone">
            <strong>
              헌혈기록을 불러오는 중입니다.
            </strong>

            <p>
              잠시만 기다려주세요.
            </p>
          </div>
        ) : error ? (
          <div className="mypage-record-error">
            {error}
          </div>
        ) : sortedRecords.length === 0 ? (
          <div className="mypage-empty-state standalone">
            <strong>
              조회된 헌혈기록이 없습니다.
            </strong>

            <p>
              헌혈이 완료되면
              이곳에서 기록을 확인할 수 있습니다.
            </p>
          </div>
        ) : (
          <div className="mypage-table-shell">
            <div
              className="mypage-table-header"
            >
              <span>
                구분
              </span>

              <button
                type="button"
                onClick={() =>
                  handleSort(
                    'donationDate'
                  )
                }
              >
                헌혈일자
                <b>
                  {getSortMark(
                    'donationDate'
                  )}
                </b>
              </button>

              <button
                type="button"
                onClick={() =>
                  handleSort(
                    'donationType'
                  )
                }
              >
                헌혈종류
                <b>
                  {getSortMark(
                    'donationType'
                  )}
                </b>
              </button>

              <button
                type="button"
                onClick={() =>
                  handleSort(
                    'bloodCenterName'
                  )
                }
              >
                혈액원
                <b>
                  {getSortMark(
                    'bloodCenterName'
                  )}
                </b>
              </button>

              <button
                type="button"
                onClick={() =>
                  handleSort(
                    'donationPlaceName'
                  )
                }
              >
                헌혈장소
                <b>
                  {getSortMark(
                    'donationPlaceName'
                  )}
                </b>
              </button>

              <span>
                즐겨찾기
              </span>
            </div>

            <div className="mypage-table-body">
              {sortedRecords.map(
                (record, index) => (
                  <article
                    key={record.id}
                    className="mypage-table-row"
                  >
                    <span data-label="구분">
                      {index + 1}
                    </span>

                    <span data-label="헌혈일자">
                      {record.donationDate}
                    </span>

                    <span data-label="헌혈종류">
                      {
                        donationTypeLabels[
                          record.donationType
                        ]
                        || record.donationType
                      }
                    </span>

                    <span data-label="혈액원">
                      {record.bloodCenterName}
                    </span>

                    <span data-label="헌혈장소">
                      {record.donationPlaceName}
                    </span>

                    <span data-label="즐겨찾기">
                      <button
                        type="button"
                        className={
                          favoriteCenterIds.includes(
                            record.centerId
                          )
                            ? 'mypage-favorite-button active'
                            : 'mypage-favorite-button'
                        }
                        aria-label={
                          favoriteCenterIds.includes(
                            record.centerId
                          )
                            ? `${record.donationPlaceName} 즐겨찾기 해제`
                            : `${record.donationPlaceName} 즐겨찾기 추가`
                        }
                        disabled={
                          !record.centerId
                          || favoriteLoadingCenterId
                            === record.centerId
                        }
                        onClick={() =>
                          handleFavoriteToggle(
                            record.centerId
                          )
                        }
                      >
                        {favoriteCenterIds.includes(
                          record.centerId
                        )
                          ? '★'
                          : '☆'}
                      </button>
                    </span>
                  </article>
                )
              )}
            </div>
          </div>
        )}
      </section>

    </div>
  )
}


export default DonationRecordsPage