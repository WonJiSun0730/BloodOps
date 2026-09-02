import {
  useEffect,
  useState,
} from 'react'

import {
  getBloodHoldRoute,
  getMyBloodHoldGuide,
} from '../../api/apiClient.js'

import {
  getCurrentPosition,
} from '../../utils/geolocation.js'

import BloodHoldRouteMap
  from '../../components/bloodhold/BloodHoldRouteMap.jsx'

import './BloodHoldGuidePage.css'


const donationTypeLabels = {
  WHOLE_BLOOD: '전혈',
  PLATELET: '혈소판',
  PLASMA: '혈장',
  PLATELET_PLASMA: '혈소판혈장',
}


function getDialablePhone(phone) {
  if (!phone) {
    return ''
  }

  const firstNumber =
    phone.split('~')[0]

  return firstNumber.replace(
    /[^0-9+]/g,
    ''
  )
}


function formatRouteDistance(distanceMeters) {
  if (
    distanceMeters === null
    || distanceMeters === undefined
  ) {
    return ''
  }

  if (distanceMeters < 1000) {
    return `${distanceMeters}m`
  }

  return `${(
    distanceMeters / 1000
  ).toFixed(1)}km`
}


function formatRouteDuration(durationMillis) {
  if (
    durationMillis === null
    || durationMillis === undefined
  ) {
    return ''
  }

  const totalMinutes =
    Math.max(
      1,
      Math.round(
        durationMillis / 60000
      )
    )

  if (totalMinutes < 60) {
    return `약 ${totalMinutes}분`
  }

  const hours =
    Math.floor(
      totalMinutes / 60
    )

  const minutes =
    totalMinutes % 60

  if (minutes === 0) {
    return `약 ${hours}시간`
  }

  return `약 ${hours}시간 ${minutes}분`
}

function openNaverTransitRoute(
  userPosition,
  target
) {
  if (
    !userPosition?.latitude
    || !userPosition?.longitude
    || target?.latitude === null
    || target?.latitude === undefined
    || target?.longitude === null
    || target?.longitude === undefined
  ) {
    return
  }


  const isMobile =
    /Android|iPhone|iPad|iPod/i.test(
      navigator.userAgent
    )


  /*
   * 모바일:
   * NAVER Maps 공식 URL Scheme 사용
   */
  if (isMobile) {
    const params =
      new URLSearchParams({
        slat: String(
          userPosition.latitude
        ),

        slng: String(
          userPosition.longitude
        ),

        sname:
          '현재 위치',

        dlat: String(
          target.latitude
        ),

        dlng: String(
          target.longitude
        ),

        dname:
          target.name || '목적지',

        appname:
          window.location.href,
      })


    window.location.href =
      `nmap://route/public?${params.toString()}`

    return
  }


  /*
   * PC:
   * NAVER Maps 웹 내부 경로를 이용한 임시 구현.
   *
   * 공식 공개 API가 아니므로
   * NAVER Maps 웹 URL 구조 변경 시
   * 동작하지 않을 수 있음.
   */
  const start =
    [
      userPosition.longitude,
      userPosition.latitude,
      '현재 위치',
      '',
      'ADDRESS_POI',
    ]
      .map((value) =>
        encodeURIComponent(
          String(value)
        )
      )
      .join('%2C')


  const destination =
    [
      target.longitude,
      target.latitude,
      target.name || '목적지',
      '',
      'ADDRESS_POI',
    ]
      .map((value) =>
        encodeURIComponent(
          String(value)
        )
      )
      .join('%2C')


  const transitUrl =
    'https://qp.map.naver.com'
    + '/end-quick-path'
    + `/${start}`
    + `/${destination}`
    + '/-/transit/0'
    + '?nscs=0'


  window.open(
    transitUrl,
    '_blank',
    'noopener,noreferrer'
  )
}

function BloodHoldGuidePage() {
  const storedCheckResult = (() => {
    try {
      const stored =
        sessionStorage.getItem(
          'bloodHoldCheckResult'
        )

      return stored
        ? JSON.parse(stored)
        : null

    } catch {
      return null
    }
  })()

  const directEntry =
    storedCheckResult === null

  const visitRestricted =
    directEntry
    || storedCheckResult
      ?.visitRestricted === true

  const [
    guide,
    setGuide,
  ] = useState(null)

  const [
    loading,
    setLoading,
  ] = useState(true)

  const [
    error,
    setError,
  ] = useState('')

  const [
    selectedTarget,
    setSelectedTarget,
  ] = useState(null)

  const [
    userPosition,
    setUserPosition,
  ] = useState(null)

  const [
    route,
    setRoute,
  ] = useState(null)

  const [
    locationLoading,
    setLocationLoading,
  ] = useState(false)

  const [
    locationError,
    setLocationError,
  ] = useState('')

  const [
    activePanel,
    setActivePanel,
  ] = useState(null)

  function isPanelOpen(
    type,
    mode
  ) {
    return (
      activePanel?.type === type
      && activePanel?.mode === mode
    )
  }

  function closeActivePanel() {
    setActivePanel(null)
    setLocationError('')
  }

  function toggleWarningPanel(type) {
    if (
      isPanelOpen(
        type,
        'warning'
      )
    ) {
      closeActivePanel()
      return
    }

    setSelectedTarget(null)
    setRoute(null)
    setLocationError('')

    setActivePanel({
      type,
      mode: 'warning',
    })
  }

  useEffect(() => {
    let active = true

    async function loadGuide() {
      try {
        setLoading(true)
        setError('')

        const data =
          await getMyBloodHoldGuide()

        if (active) {
          setGuide(data)
        }
      } catch (error) {
        if (active) {
          setGuide(null)

          setError(
            error.message
            || '혈액사용 보류 안내 정보를 불러오지 못했습니다.'
          )
        }
      } finally {
        if (active) {
          setLoading(false)
        }
      }
    }

    loadGuide()

    return () => {
      active = false
    }
  }, [])


  async function handleLocationClick(
    target,
    type
  ) {
    if (
      visitRestricted
    ) {
      toggleWarningPanel(type)
      return
    }

    if (
      isPanelOpen(
        type,
        'route'
      )
    ) {
      closeActivePanel()
      return
    }

    if (
      target?.latitude === null
      || target?.latitude === undefined
      || target?.longitude === null
      || target?.longitude === undefined
    ) {
      setActivePanel({
        type,
        mode: 'error',
      })

      setLocationError(
        '해당 기관의 지도 위치 정보가 등록되지 않았습니다.'
      )

      return
    }

    try {
      setLocationLoading(true)
      setLocationError('')

      const changingRoute =
        activePanel?.mode === 'route'
        && activePanel?.type !== type

      /*
       * 다른 카드의 지도가 열려 있으면
       * 먼저 닫힘 애니메이션을 끝낸 뒤
       * 새 경로를 불러옵니다.
       */
      if (changingRoute) {
        setActivePanel(null)

        await new Promise(
          (resolve) =>
            setTimeout(resolve, 320)
        )
      }

      setRoute(null)

      const position =
        await getCurrentPosition()

      const routeData =
        await getBloodHoldRoute({
          startLatitude:
            position.latitude,

          startLongitude:
            position.longitude,

          goalLatitude:
            Number(
              target.latitude
            ),

          goalLongitude:
            Number(
              target.longitude
            ),
        })

      setUserPosition(
        position
      )

      setSelectedTarget(
        target
      )

      setRoute(
        routeData
      )

      setActivePanel({
        type,
        mode: 'route',
      })

    } catch (error) {
      setSelectedTarget(null)
      setRoute(null)

      setActivePanel({
        type,
        mode: 'error',
      })

      setLocationError(
        error.message
        || '자동차 경로를 확인하지 못했습니다.'
      )

    } finally {
      setLocationLoading(false)
    }
  }


  function renderContactCard(
    target,
    type
  ) {
    if (!target) {
      return null
    }

    const primary =
      type === 'primary'

    const dialablePhone =
      getDialablePhone(
        target.phone
      )

    const routeOpen =
      isPanelOpen(
        type,
        'route'
      )

    const warningOpen =
      isPanelOpen(
        type,
        'warning'
      )

    const errorOpen =
      isPanelOpen(
        type,
        'error'
      )

    async function handleTransitClick() {
      /*
       * 전염 가능성 또는 불확실 판정이면
       * 위치 권한을 요청하지 않고
       * 카드 하단 안내만 표시합니다.
       */
      if (visitRestricted) {
        toggleWarningPanel(type)
        return
      }

      try {
        setLocationError('')

        let position =
          userPosition

        if (!position) {
          position =
            await getCurrentPosition()

          setUserPosition(
            position
          )
        }

        openNaverTransitRoute(
          position,
          target
        )

      } catch (error) {
        setSelectedTarget(null)
        setRoute(null)

        setActivePanel({
          type,
          mode: 'error',
        })

        setLocationError(
          error.message
          || '현재 위치를 확인하지 못했습니다.'
        )
      }
    }

    return (
      <div
        className="blood-hold-contact-block"
      >
        <article
          className={
            primary
              ? 'blood-hold-contact-card primary'
              : 'blood-hold-contact-card secondary'
          }
        >
          <div
            className="blood-hold-contact-heading"
          >
            <div>
              <span>
                {primary
                  ? '1순위 안내'
                  : '2순위 안내'}
              </span>

              <h3>
                {target.name}
              </h3>
            </div>

            <strong>
              {primary
                ? '관할 혈액원'
                : '헌혈한 센터'}
            </strong>
          </div>

          <dl
            className="blood-hold-contact-info"
          >
            <div>
              <dt>
                주소
              </dt>

              <dd>
                {target.address
                  || '정보 없음'}
              </dd>
            </div>

            <div>
              <dt>
                전화번호
              </dt>

              <dd>
                {target.phone
                  || '정보 없음'}
              </dd>
            </div>
          </dl>

          <div
            className="blood-hold-contact-actions"
          >
            {dialablePhone ? (
              <a
                href={`tel:${dialablePhone}`}
                className="blood-hold-action-button primary"
              >
                전화 문의
              </a>
            ) : (
              <button
                type="button"
                className="blood-hold-action-button primary"
                disabled
              >
                전화번호 없음
              </button>
            )}

            <button
              type="button"
              className={
                routeOpen
                  || warningOpen
                  ? 'blood-hold-action-button secondary active'
                  : 'blood-hold-action-button secondary'
              }
              onClick={() =>
                handleLocationClick(
                  target,
                  type
                )
              }
              disabled={
                locationLoading
              }
            >
              {locationLoading
                ? '자동차 경로 확인 중...'
                : routeOpen
                  ? '자동차 경로 닫기'
                  : '자동차 경로 확인'}
            </button>

            <button
              type="button"
              className="blood-hold-action-button secondary transit"
              onClick={
                handleTransitClick
              }
            >
              {warningOpen
                ? '안내 닫기'
                : '대중교통 길찾기'}
            </button>
          </div>
        </article>


        <div
          className={
            warningOpen
              ? 'blood-hold-expand-panel open'
              : 'blood-hold-expand-panel'
          }
          aria-hidden={!warningOpen}
        >
          <div className="blood-hold-expand-panel-inner">
            <section
              className="blood-hold-inline-warning"
              role="alert"
            >
            <div
              className="blood-hold-inline-warning-icon"
              aria-hidden="true"
            >
              !
            </div>

            <div
              className="blood-hold-inline-warning-content"
            >
              <h3>
                직접 방문은 권장하지 않습니다.
              </h3>

              <p>
                {directEntry
                  ? (
                    <>
                      현재 질환 상태를 확인할 수 없어
                      직접 방문이 가능한지 판단하기 어렵습니다.
                      방문하기 전에 혈액원에 먼저
                      전화로 문의해 주세요.
                    </>
                  )
                  : (
                    <>
                      현재 입력하신 질환은
                      전염 가능성이 있거나
                      안전하게 판단하기 어려워
                      다른 방문자와 직원의 안전을 위해
                      혈액원에 먼저 전화로 문의해 주세요.
                    </>
                  )}
              </p>

              {storedCheckResult
                ?.disease
                ?.reason && (
                <p
                  className="blood-hold-inline-warning-reason"
                >
                  {
                    storedCheckResult
                      .disease
                      .reason
                  }
                </p>
              )}

              <div
                className="blood-hold-inline-warning-actions"
              >
                {dialablePhone && (
                  <a
                    href={`tel:${dialablePhone}`}
                    className="blood-hold-action-button primary"
                  >
                    {primary
                      ? '혈액원 전화하기'
                      : '센터 전화하기'}
                  </a>
                )}

                <button
                  type="button"
                  className="blood-hold-action-button secondary"
                  onClick={
                    closeActivePanel
                  }
                >
                  안내 닫기
                </button>
              </div>
            </div>
          </section>
        </div>
        </div>


        <div
          className={
            routeOpen
              ? 'blood-hold-expand-panel open'
              : 'blood-hold-expand-panel'
          }
          aria-hidden={!routeOpen}
        >
          <div className="blood-hold-expand-panel-inner">
            {selectedTarget
              && userPosition
              && route && (
              <section
                className="blood-hold-map-section"
              >
            <header>
              <div>
                <span>
                  LOCATION
                </span>

                <h3>
                  자동차 최적 경로
                </h3>

                <p>
                  현재 위치에서
                  {` ${selectedTarget.name}`}까지의
                  실제 도로 경로입니다.
                </p>
              </div>

              <strong>
                {
                  formatRouteDistance(
                    route.distanceMeters
                  )
                }
                {' · '}
                {
                  formatRouteDuration(
                    route.durationMillis
                  )
                }
              </strong>
            </header>

            <BloodHoldRouteMap
              userPosition={
                userPosition
              }
              target={
                selectedTarget
              }
              routePath={
                route?.path ?? []
              }
            />

            <p
              className="blood-hold-distance-notice"
            >
              NAVER Directions의
              실시간 교통정보를 반영한
              자동차 최적 경로입니다.
              실제 도로 상황에 따라
              이동시간은 달라질 수 있습니다.
            </p>
          </section>
        )}
        </div>
        </div>


        {errorOpen
          && locationError && (
          <div
            className="mypage-record-error blood-hold-inline-error"
            role="alert"
          >
            {locationError}

            <button
              type="button"
              onClick={
                closeActivePanel
              }
            >
              닫기
            </button>
          </div>
        )}
      </div>
    )
  }


  if (loading) {
    return (
      <div className="mypage-page">
        <div className="mypage-empty-state standalone">
          <strong>
            혈액사용 보류 안내를 불러오는 중입니다.
          </strong>

          <p>
            잠시만 기다려주세요.
          </p>
        </div>
      </div>
    )
  }


  if (error) {
    return (
      <div className="mypage-page">
        <div className="mypage-empty-state standalone">
          <strong>
            안내 정보를 확인할 수 없습니다.
          </strong>

          <p>
            {error}
          </p>
        </div>
      </div>
    )
  }


  if (!guide) {
    return (
      <div className="mypage-page">
        <div className="mypage-empty-state standalone">
          <strong>
            혈액사용 보류 안내 정보가 없습니다.
          </strong>
        </div>
      </div>
    )
  }


  return (
    <div className="mypage-page blood-hold-page">

      <section className="blood-hold-intro">
        <span>
          BLOOD USE HOLD
        </span>

        <h2>
          혈액사용 보류 안내
        </h2>

        <p>
          최근 헌혈 기록을 기준으로
          우선 연락해야 할 혈액원과
          헌혈 장소를 안내합니다.
        </p>
      </section>


      <section className="blood-hold-latest">
        <div>
          <span>
            최근 헌혈
          </span>

          <strong>
            {guide.donationDate}
          </strong>
        </div>

        <div>
          <span>
            헌혈 종류
          </span>

          <strong>
            {
              donationTypeLabels[
                guide.donationType
              ]
              || guide.donationType
            }
          </strong>
        </div>

        <div>
          <span>
            헌혈 장소
          </span>

          <strong>
            {guide.secondaryContact?.name
              || '정보 없음'}
          </strong>
        </div>
      </section>


      {renderContactCard(
        guide.primaryContact,
        'primary'
      )}


      <section
        className="blood-hold-warning"
        role="alert"
      >
        <div
          className="blood-hold-warning-icon"
          aria-hidden="true"
        >
          !
        </div>

        <div>
          <span>
            잠깐!
          </span>

          <h3>
            전염성이 심한 질병 발병 상태이신가요?
          </h3>

          <p>
            직접 방문하기 전에
            우선 혈액원으로 전화하여
            상황을 알려주세요.
          </p>
        </div>
      </section>


      {renderContactCard(
        guide.secondaryContact,
        'secondary'
      )}

    </div>
  )
}


export default BloodHoldGuidePage