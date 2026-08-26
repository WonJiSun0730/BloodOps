import {
  useEffect,
  useMemo,
  useState,
} from 'react'

import CenterMapArea
  from '../../components/center/CenterMapArea.jsx'

function CenterSearchPanel({
  centers,
  selectedCenterId,
  initialKeyword,
  onSelectCenter,
}) {
  const [keyword, setKeyword] =
    useState('')

  useEffect(() => {
    if (!initialKeyword) {
      return
    }

    setKeyword(
      initialKeyword
    )
  }, [initialKeyword])


  const [mapOpen, setMapOpen] =
    useState(() => {
      if (
        typeof window !== 'undefined'
        && window.matchMedia(
          '(max-width: 480px)'
        ).matches
      ) {
        return false
      }

      return true
    })


  const filteredCenters =
    useMemo(() => {
      const normalizedKeyword =
        keyword
          .trim()
          .toLowerCase()

      if (!normalizedKeyword) {
        return centers
      }

      return centers.filter(
        (center) => {
          const searchTarget = [
            center.centerName,
            center.bloodCenterName,
            center.address,
          ]
            .filter(Boolean)
            .join(' ')
            .toLowerCase()

          return searchTarget.includes(
            normalizedKeyword
          )
        }
      )
    }, [
      centers,
      keyword,
    ])


  const selectedCenter =
    centers.find(
      (center) =>
        String(center.id)
          === String(selectedCenterId)
    )

  function handleMapCenterSelect(
    centerId
  ) {
    const center =
      centers.find(
        (item) =>
          String(item.id)
            === String(centerId)
      )

    if (
      !center
      || center.active === false
    ) {
      return
    }

    onSelectCenter(
      String(center.id)
    )
  }

  function handleLocationView(
    center
  ) {
    setMapOpen(true)

    onSelectCenter(
      String(center.id)
    )
  }


  return (
    <section className="reservation-center-section">

      <div className="reservation-center-section-header">
        <div>
          <span>
            STEP 01
          </span>

          <h2>
            헌혈센터 선택
          </h2>

          <p>
            센터명이나 지역을 검색하고
            예약할 센터를 선택해주세요.
          </p>
        </div>

        <button
          type="button"
          className="reservation-map-toggle"
          onClick={() =>
            setMapOpen(
              (current) => !current
            )
          }
          aria-expanded={mapOpen}
        >
          {mapOpen
            ? '지도 접기'
            : '지도 펼치기'}
        </button>
      </div>


      <div className="reservation-center-search">
        <span
          className="reservation-center-search-icon"
          aria-hidden="true"
        >
          ⌕
        </span>

        <input
          type="search"
          value={keyword}
          onChange={(event) =>
            setKeyword(
              event.target.value
            )
          }
          placeholder="센터명 또는 지역 검색"
          aria-label="헌혈센터 검색"
        />

        {keyword && (
          <button
            type="button"
            onClick={() =>
              setKeyword('')
            }
          >
            초기화
          </button>
        )}
      </div>


      {mapOpen && (
        <div className="reservation-map-shell">
          <CenterMapArea
            centers={filteredCenters}
            selectedCenter={
              selectedCenter
            }
            onCenterSelect={
              handleMapCenterSelect
            }
          />
        </div>
      )}


      <div className="reservation-center-result-header">
        <strong>
          검색 결과
        </strong>

        <span>
          {filteredCenters.length}곳
        </span>
      </div>


      <div className="reservation-center-list">

        {filteredCenters.length === 0 ? (
          <p className="reservation-center-empty">
            검색 결과가 없습니다.
          </p>
        ) : (
          filteredCenters.map(
            (center) => {

              const selected =
                String(center.id)
                  === String(
                    selectedCenterId
                  )

              const inactive =
                center.active === false

              return (
                <article
                  key={center.id}
                  className={
                    `reservation-center-card${
                      selected
                        ? ' selected'
                        : ''
                    }${
                      inactive
                        ? ' inactive'
                        : ''
                    }`
                  }
                >

                  <div className="reservation-center-card-content">

                    <div className="reservation-center-card-title">
                      <strong>
                        {center.centerName}
                      </strong>

                      {inactive && (
                        <span>
                          운영 중단
                        </span>
                      )}
                    </div>

                    <p>
                      {center.address
                        || '주소 정보 없음'}
                    </p>

                    <div className="reservation-center-meta">
                      {center.weekdayHours && (
                        <span>
                          평일 {center.weekdayHours}
                        </span>
                      )}

                      {center.phone && (
                        <span>
                          {center.phone}
                        </span>
                      )}
                    </div>

                  </div>


                  <div className="reservation-center-card-actions">

                    <button
                      type="button"
                      className="reservation-location-button"
                      disabled={
                        inactive
                        || center.latitude == null
                        || center.longitude == null
                      }
                      title={
                        center.latitude == null
                        || center.longitude == null
                          ? '좌표 정보가 없는 센터입니다.'
                          : `${center.centerName} 위치 보기`
                      }
                      onClick={() =>
                        handleLocationView(
                          center
                        )
                      }
                    >
                      위치 보기
                    </button>

                    <button
                      type="button"
                      className="reservation-center-select-button"
                      disabled={inactive}
                      onClick={() =>
                        onSelectCenter(
                          String(center.id)
                        )
                      }
                    >
                      {selected
                        ? '선택됨'
                        : inactive
                          ? '선택 불가'
                          : '이 센터 선택'}
                    </button>

                  </div>

                </article>
              )
            }
          )
        )}

      </div>

    </section>
  )
}


export default CenterSearchPanel