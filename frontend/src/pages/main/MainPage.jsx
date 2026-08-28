import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'

import {
  getCenterMapCenters,
} from '../../api/apiClient.js'

import {
  findNearestCenter,
  getCurrentPosition,
} from '../../utils/geolocation.js'

import {
  CenterIcon,
  ReservationIcon,
  QuestionnaireIcon,
  PromotionIcon,
  RecordsIcon,
  ResultsIcon,
  CertificateIcon,
  BloodHoldIcon,
  FaqIcon,
} from '../../components/icons/ServiceIcons.jsx'

import './MainPage.css'

import BackgroundOrbit
  from '../../components/decorations/BackgroundOrbit.jsx'


/*
 * 메인 이벤트 배너
 *
 * 현재는 디자인 확인용 임시 데이터입니다.
 * 추후 프로모션/공지 API가 구현되면
 * 이 배열 대신 서버 응답 데이터를 사용합니다.
 */
const heroBanners = [
  {
    id: 1,
    eyebrow: 'BLOODOPS',
    title: '오늘의 헌혈이\n누군가의 내일이 됩니다.',
    description:
      '가까운 헌혈의 집을 찾고 원하는 날짜에 헌혈을 예약해보세요.',
    actionLabel: '헌혈의 집 찾기',
    actionTo: '/centers',
    theme: 'primary',
  },
  {
    id: 2,
    eyebrow: 'DONATION',
    title: '기다리지 말고\n미리 예약하세요.',
    description:
      '원하는 헌혈센터와 시간대를 선택해 편리하게 예약할 수 있습니다.',
    actionLabel: '헌혈 예약하기',
    actionTo: '/reservations/new',
    theme: 'soft',
  },
  {
    id: 3,
    eyebrow: 'CAMPAIGN',
    title: '함께 만드는\n더 안전한 혈액 수급',
    description:
      'BloodOps에서 진행 중인 헌혈 캠페인과 프로모션을 확인해보세요.',
    actionLabel: '프로모션 보기',
    actionTo: '/promotion',
    theme: 'dark',
  },
]


/*
 * 혈액 보유량 임시 데이터
 *
 * stockDays 부분이 나중에
 * 크롤링/API 응답으로 교체될 부분입니다.
 */
const bloodStatusData = {
  redBloodCell: {
    label: '적혈구',
    totalDays: 4.6,
    updatedAt: '2026.08.19 기준',
    stockDays: [
      { type: 'A', days: 3.0 },
      { type: 'B', days: 7.0 },
      { type: 'O', days: 4.5 },
      { type: 'AB', days: 4.6 },
    ],
  },

  platelet: {
    label: '혈소판',
    totalDays: 3.8,
    updatedAt: '2026.08.19 기준',
    stockDays: [
      { type: 'A', days: 3.5 },
      { type: 'B', days: 4.2 },
      { type: 'O', days: 3.1 },
      { type: 'AB', days: 4.4 },
    ],
  },
}

const mainServices = [
  {
    id: 'center',
    title: '헌혈의 집 찾기',
    description:
      '가까운 헌혈센터의 위치와 운영 정보를 확인하세요.',
    to: '/centers',
    icon: CenterIcon,
    priority: true,
  },
  {
    id: 'reservation',
    title: '헌혈예약',
    description:
      '원하는 날짜와 시간대를 선택해 미리 예약하세요.',
    to: '/reservations/new',
    icon: ReservationIcon,
    priority: true,
  },
  {
    id: 'questionnaire',
    title: '전자문진',
    description:
      '방문 전에 사전 전자문진을 작성할 수 있습니다.',
    to: '/questionnaire',
    icon: QuestionnaireIcon,
  },
  {
    id: 'promotion',
    title: '프로모션',
    description:
      '진행 중인 캠페인과 헌혈 프로모션을 확인하세요.',
    to: '/promotion',
    icon: PromotionIcon,
  },
  {
    id: 'records',
    title: '헌혈내역 조회',
    description:
      '나의 헌혈 이력과 참여 기록을 확인하세요.',
    to: '/mypage/records',
    icon: RecordsIcon,
  },
  {
    id: 'results',
    title: '검사결과 조회',
    description:
      '헌혈 후 검사결과를 조회할 수 있습니다.',
    to: '/mypage/results',
    icon: ResultsIcon,
  },
  {
    id: 'certificate',
    title: '증명서 발급',
    description:
      '헌혈 관련 증명서를 확인하고 발급하세요.',
    to: '/certificate',
    icon: CertificateIcon,
  },
  {
    id: 'bloodHold',
    title: '혈액사용 보류신청',
    description:
      '필요한 경우 혈액 사용 보류를 신청할 수 있습니다.',
    to: '/blood-hold',
    icon: BloodHoldIcon,
  },
  {
    id: 'faq',
    title: '자주하는 질문',
    description:
      '헌혈과 BloodOps 이용에 관한 답변을 찾아보세요.',
    to: '/faq',
    icon: FaqIcon,
  },
]

const newsTabs = [
  '새소식',
  '일반공지',
  '언론보도',
  '보도자료',
  '채용공고',
]

const newsItems = [
  {
    id: 1,
    category: '새소식',
    title: 'BloodOps 헌혈 예약 시스템 이용 안내',
    date: '2026.08.19',
  },
  {
    id: 2,
    category: '일반공지',
    title: '헌혈센터 운영시간 변경 안내',
    date: '2026.08.18',
  },
  {
    id: 3,
    category: '일반공지',
    title: '전자문진 사전 작성 기능 안내',
    date: '2026.08.18',
  },
  {
    id: 4,
    category: '보도자료',
    title: '안정적인 혈액 수급을 위한 캠페인 안내',
    date: '2026.08.13',
  },
]

const noticeSlides = [
  {
    id: 1,
    eyebrow: 'NOTICE',
    title: '헌혈 전 꼭 확인해주세요',
    description:
      '예약 시간과 신분증 지참 여부를 미리 확인하면 더 빠르게 헌혈에 참여할 수 있습니다.',
    actionLabel: '이용 안내 보기',
    actionTo: '/guide',
  },
  {
    id: 2,
    eyebrow: 'CHECK',
    title: '전자문진은 방문 전에',
    description:
      '사전 전자문진을 작성하면 헌혈센터에서 보다 빠르게 절차를 진행할 수 있습니다.',
    actionLabel: '전자문진 작성',
    actionTo: '/questionnaire',
  },
]

const hallOfFame = [
  {
    id: 1,
    rank: 1,
    name: '김헌혈',
    count: 128,
    region: '서울중앙혈액원',
  },
  {
    id: 2,
    rank: 2,
    name: '이생명',
    count: 112,
    region: '인천혈액원',
  },
  {
    id: 3,
    rank: 3,
    name: '박나눔',
    count: 104,
    region: '경기혈액원',
  },
]


const donationTips = [
  {
    id: 1,
    category: 'BEFORE',
    title: '헌혈 전에는 충분한 수분을 섭취하세요',
    description:
      '헌혈 전날과 당일에는 물을 충분히 마시고 과도한 음주는 피하는 것이 좋습니다.',
  },
  {
    id: 2,
    category: 'CHECK',
    title: '신분증을 꼭 준비해주세요',
    description:
      '본인 확인이 가능한 신분증을 준비하면 헌혈 절차를 빠르게 진행할 수 있습니다.',
  },
  {
    id: 3,
    category: 'AFTER',
    title: '헌혈 후에는 충분히 쉬어주세요',
    description:
      '헌혈 직후에는 잠시 휴식하고 무리한 운동이나 음주는 피해주세요.',
  },
]

const promotions = [
  {
    id: 1,
    category: 'CAMPAIGN',
    title: '이번 달 헌혈 캠페인',
    description:
      'BloodOps와 함께하는 헌혈 참여 캠페인을 확인해보세요.',
    to: '/campaign',
  },
  {
    id: 2,
    category: 'PROMOTION',
    title: '헌혈 참여 프로모션',
    description:
      '헌혈 참여자를 위한 프로모션과 혜택을 확인할 수 있습니다.',
    to: '/promotion',
  },
  {
    id: 3,
    category: 'EVENT',
    title: '지역별 헌혈 이벤트',
    description:
      '가까운 헌혈센터에서 진행 중인 이벤트를 확인해보세요.',
    to: '/promotion',
  },
]


const socialChannels = [
  {
    id: 'instagram',
    label: 'Instagram',
    description: '캠페인과 헌혈 소식',
    symbol: '◎',
  },
  {
    id: 'youtube',
    label: 'YouTube',
    description: '헌혈 안내 영상',
    symbol: '▶',
  },
  {
    id: 'blog',
    label: 'Blog',
    description: 'BloodOps 이야기',
    symbol: 'B',
  },
]


function MainPage() {
  const [activeBanner, setActiveBanner] = useState(0)

  const [bloodType, setBloodType] =
    useState('redBloodCell')

  const [activeNewsTab, setActiveNewsTab] =
    useState('새소식')

  const [activeNotice, setActiveNotice] =
    useState(0)

  const [activeTip, setActiveTip] =
    useState(0)

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

  /*
   * 이벤트 배너 자동 전환
   *
   * 5초마다 다음 배너로 넘어갑니다.
   */
  useEffect(() => {
    const timer = setInterval(() => {
      setActiveBanner((current) =>
        current === heroBanners.length - 1
          ? 0
          : current + 1
      )
    }, 5000)

    return () => {
      clearInterval(timer)
    }
  }, [])


  const currentBanner =
    heroBanners[activeBanner]

  const currentBloodStatus =
    bloodStatusData[bloodType]


  function showPreviousBanner() {
    setActiveBanner((current) =>
      current === 0
        ? heroBanners.length - 1
        : current - 1
    )
  }


  function showNextBanner() {
    setActiveBanner((current) =>
      current === heroBanners.length - 1
        ? 0
        : current + 1
    )
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
    <main className="main-page">

      <BackgroundOrbit />
      {/* =================================================
          Hero
          ================================================= */}

      <section
        className="main-hero"
        aria-label="BloodOps 주요 안내"
      >

        {/* -----------------------------------------------
            이벤트 / 행사 배너
            ----------------------------------------------- */}

        <article
          className={
            `hero-banner hero-banner--${currentBanner.theme}`
          }
        >
          <div
            className="hero-banner-content"
            key={currentBanner.id}
          >
            <span className="hero-eyebrow">
              {currentBanner.eyebrow}
            </span>

            <h1>
              {currentBanner.title
                .split('\n')
                .map((line, index) => (
                  <span key={index}>
                    {line}

                    {index <
                      currentBanner.title
                        .split('\n').length - 1 && (
                      <br />
                    )}
                  </span>
                ))}
            </h1>

            <p>
              {currentBanner.description}
            </p>

            <Link
              className="hero-action"
              to={currentBanner.actionTo}
            >
              {currentBanner.actionLabel}

              <span aria-hidden="true">
                →
              </span>
            </Link>
          </div>


          {/* 배너 이동 버튼 */}

          <div className="hero-controller">
            <button
              type="button"
              onClick={showPreviousBanner}
              aria-label="이전 배너"
            >
              ←
            </button>


            <div
              className="hero-pagination"
              aria-label="배너 위치"
            >
              {heroBanners.map((banner, index) => (
                <button
                  key={banner.id}
                  type="button"
                  className={
                    activeBanner === index
                      ? 'hero-pagination-dot active'
                      : 'hero-pagination-dot'
                  }
                  onClick={() =>
                    setActiveBanner(index)
                  }
                  aria-label={
                    `${index + 1}번째 배너 보기`
                  }
                />
              ))}
            </div>


            <button
              type="button"
              onClick={showNextBanner}
              aria-label="다음 배너"
            >
              →
            </button>
          </div>
        </article>


        {/* -----------------------------------------------
            혈액 보유량
            ----------------------------------------------- */}

        <aside className="blood-status-card">

          <div className="blood-status-header">
            <div>
              <span className="section-eyebrow">
                BLOOD STATUS
              </span>

              <h2>
                오늘의 혈액보유량
              </h2>
            </div>

            <span className="blood-status-date">
              {currentBloodStatus.updatedAt}
            </span>
          </div>


          {/* 적혈구 / 혈소판 전환 */}

          <div
            className="blood-status-tabs"
            role="tablist"
            aria-label="혈액 종류"
          >
            <button
              type="button"
              role="tab"
              className={
                bloodType === 'redBloodCell'
                  ? 'active'
                  : ''
              }
              aria-selected={
                bloodType === 'redBloodCell'
              }
              onClick={() =>
                setBloodType('redBloodCell')
              }
            >
              적혈구
            </button>

            <button
              type="button"
              role="tab"
              className={
                bloodType === 'platelet'
                  ? 'active'
                  : ''
              }
              aria-selected={
                bloodType === 'platelet'
              }
              onClick={() =>
                setBloodType('platelet')
              }
            >
              혈소판
            </button>
          </div>


          {/* 전체 보유일수 */}

          <div className="blood-total">
            <div>
              <span>
                전체
              </span>

              <strong>
                {currentBloodStatus.totalDays}
              </strong>

              <span>
                일
              </span>
            </div>

            <p>
              안정적인 혈액 수급을 위해
              여러분의 참여가 필요합니다.
            </p>
          </div>


          {/* 혈액형별 보유량 */}

          <div className="blood-type-grid">
            {currentBloodStatus.stockDays.map(
              (blood) => (
                <div
                  className="blood-type-item"
                  key={blood.type}
                >
                  <span className="blood-type-name">
                    {blood.type}
                  </span>

                  <div>
                    <strong>
                      {blood.days}
                    </strong>

                    <span>
                      DAY
                    </span>
                  </div>
                </div>
              )
            )}
          </div>


          <Link
            className="blood-status-detail"
            to="/blood-status"
          >
            혈액 보유현황 자세히 보기

            <span aria-hidden="true">
              →
            </span>
          </Link>

        </aside>

      </section>


      {/* =================================================
          주요 서비스
          ================================================= */}

            <section
              className="nearest-center-section"
              aria-labelledby="nearest-center-title"
            >
              <div className="nearest-center-copy">
                <span className="section-eyebrow">
                  NEARBY DONATION CENTER
                </span>

                {!nearestCenter ? (
                  <>
                    <h2 id="nearest-center-title">
                      내 주변 헌혈의 집을 찾아보세요.
                    </h2>

                    <p>
                      현재 위치를 기준으로 가장 가까운
                      운영 중인 헌혈의 집을 안내해드립니다.
                    </p>
                  </>
                ) : (
                  <>
                    <h2 id="nearest-center-title">
                      가장 가까운 헌혈의 집은{' '}
                      <strong>
                        {nearestCenter.centerName}
                      </strong>
                      입니다!
                    </h2>

                    <p>
                      가까운 곳에서 시작하는 작은 나눔이
                      누군가에게는 소중한 내일이 됩니다.
                    </p>

                    <span className="nearest-center-distance">
                      현재 위치에서 약{' '}
                      {nearestCenter.distanceKm < 1
                        ? `${Math.round(
                            nearestCenter.distanceKm
                            * 1000
                          )}m`
                        : `${nearestCenter.distanceKm.toFixed(
                            1
                          )}km`}
                    </span>
                  </>
                )}

                {locationError && (
                  <p
                    className="nearest-center-error"
                    role="alert"
                  >
                    {locationError}
                  </p>
                )}
              </div>

              <div className="nearest-center-actions">
                <button
                  type="button"
                  onClick={
                    handleFindNearestCenter
                  }
                  disabled={locationLoading}
                >
                  {locationLoading
                    ? '현재 위치 확인 중...'
                    : nearestCenter
                      ? '다시 찾기'
                      : '가까운 헌혈의 집 찾기'}
                </button>

                {nearestCenter && (
                  <Link
                    to={
                      `/reservations/new?centerId=${nearestCenter.id}`
                    }
                  >
                    이 센터 예약하기
                    <span aria-hidden="true">
                      →
                    </span>
                  </Link>
                )}
              </div>
            </section>

      <section
        className="main-service-section"
        aria-labelledby="main-service-title"
      >
        <div className="main-section-header">
          <div>
            <span className="section-eyebrow">
              QUICK SERVICE
            </span>

            <h2 id="main-service-title">
              주요 서비스
            </h2>

            <p>
              헌혈 참여부터 기록 조회까지
              자주 사용하는 기능을 빠르게 이용하세요.
            </p>
          </div>
        </div>


        <div className="main-service-grid">
          {mainServices.map((service, index) => {
            const ServiceIcon = service.icon

            return (
              <Link
                key={service.id}
                to={service.to}
                className={
                  service.priority
                    ? 'main-service-card priority'
                    : 'main-service-card'
                }
                style={{
                  '--service-index': index,
                }}
              >
                <div className="main-service-icon">
                  <ServiceIcon />
                </div>

                <div className="main-service-content">
                  <h3>
                    {service.title}
                  </h3>

                  <p>
                    {service.description}
                  </p>
                </div>

                <span
                  className="main-service-arrow"
                  aria-hidden="true"
                >
                  →
                </span>
              </Link>
            )
          })}
        </div>
      </section>

          {/* =================================================
              새소식 + 알림판
              ================================================= */}

          <section className="main-information-section">

            {/* -----------------------------------------------
                새소식
                ----------------------------------------------- */}

            <article className="news-panel">

              <div className="information-panel-header">
                <div>
                  <span className="section-eyebrow">
                    NEWS
                  </span>

                  <h2>
                    새소식
                  </h2>
                </div>

                <Link
                  className="information-more"
                  to="/news"
                >
                  전체보기
                  <span aria-hidden="true">
                    →
                  </span>
                </Link>
              </div>


              <div
                className="news-tabs"
                role="tablist"
                aria-label="새소식 분류"
              >
                {newsTabs.map((tab) => (
                  <button
                    key={tab}
                    type="button"
                    role="tab"
                    className={
                      activeNewsTab === tab
                        ? 'active'
                        : ''
                    }
                    aria-selected={
                      activeNewsTab === tab
                    }
                    onClick={() =>
                      setActiveNewsTab(tab)
                    }
                  >
                    {tab}
                  </button>
                ))}
              </div>


              <div className="news-list">
                {newsItems
                  .filter((item) =>
                    activeNewsTab === '새소식'
                      ? true
                      : item.category === activeNewsTab
                  )
                  .slice(0, 4)
                  .map((item) => (
                    <Link
                      key={item.id}
                      to="/news"
                      className="news-item"
                    >
                      <span className="news-category">
                        {item.category}
                      </span>

                      <strong>
                        {item.title}
                      </strong>

                      <time>
                        {item.date}
                      </time>
                    </Link>
                  ))}
              </div>

            </article>


            {/* -----------------------------------------------
                알림판
                ----------------------------------------------- */}

            <aside className="notice-panel">

              <div className="information-panel-header">
                <div>
                  <span className="section-eyebrow">
                    NOTICE
                  </span>

                  <h2>
                    알림판
                  </h2>
                </div>

                <div className="notice-count">
                  <strong>
                    {String(activeNotice + 1).padStart(2, '0')}
                  </strong>

                  <span>
                    /
                  </span>

                  <span>
                    {String(noticeSlides.length).padStart(2, '0')}
                  </span>
                </div>
              </div>


              <div
                className="notice-card"
                key={noticeSlides[activeNotice].id}
              >
                <span className="notice-eyebrow">
                  {noticeSlides[activeNotice].eyebrow}
                </span>

                <h3>
                  {noticeSlides[activeNotice].title}
                </h3>

                <p>
                  {noticeSlides[activeNotice].description}
                </p>

                <Link
                  className="notice-action"
                  to={noticeSlides[activeNotice].actionTo}
                >
                  {noticeSlides[activeNotice].actionLabel}

                  <span aria-hidden="true">
                    →
                  </span>
                </Link>
              </div>


              <div className="notice-controller">
                <button
                  type="button"
                  onClick={() =>
                    setActiveNotice((current) =>
                      current === 0
                        ? noticeSlides.length - 1
                        : current - 1
                    )
                  }
                  aria-label="이전 알림"
                >
                  ←
                </button>

                <button
                  type="button"
                  onClick={() =>
                    setActiveNotice((current) =>
                      current === noticeSlides.length - 1
                        ? 0
                        : current + 1
                    )
                  }
                  aria-label="다음 알림"
                >
                  →
                </button>
              </div>

            </aside>

          </section>

          {/* =================================================
              명예의 전당 + 헌혈팁
              ================================================= */}

          <section className="main-highlight-section">

            {/* -----------------------------------------------
                명예의 전당
                ----------------------------------------------- */}

            <article className="hall-of-fame-panel">

              <div className="information-panel-header">
                <div>
                  <span className="section-eyebrow">
                    HALL OF FAME
                  </span>

                  <h2>
                    명예의 전당
                  </h2>
                </div>

                <Link
                  className="information-more"
                  to="/hall-of-fame"
                >
                  전체보기
                  <span aria-hidden="true">
                    →
                  </span>
                </Link>
              </div>


              <div className="hall-of-fame-list">
                {hallOfFame.map((member) => (
                  <div
                    key={member.id}
                    className="hall-of-fame-item"
                  >
                    <div className="hall-rank">
                      <span>
                        {member.rank}
                      </span>
                    </div>

                    <div className="hall-member">
                      <strong>
                        {member.name}
                      </strong>

                      <span>
                        {member.region}
                      </span>
                    </div>

                    <div className="hall-count">
                      <strong>
                        {member.count}
                      </strong>

                      <span>
                        회
                      </span>
                    </div>
                  </div>
                ))}
              </div>

            </article>


            {/* -----------------------------------------------
                헌혈팁
                ----------------------------------------------- */}

            <aside className="donation-tip-panel">

              <div className="information-panel-header">
                <div>
                  <span className="section-eyebrow">
                    DONATION TIP
                  </span>

                  <h2>
                    헌혈팁
                  </h2>
                </div>

                <div className="notice-count">
                  <strong>
                    {String(activeTip + 1).padStart(2, '0')}
                  </strong>

                  <span>
                    /
                  </span>

                  <span>
                    {String(donationTips.length).padStart(2, '0')}
                  </span>
                </div>
              </div>


              <div
                className="donation-tip-card"
                key={donationTips[activeTip].id}
              >
                <span className="donation-tip-category">
                  {donationTips[activeTip].category}
                </span>

                <h3>
                  {donationTips[activeTip].title}
                </h3>

                <p>
                  {donationTips[activeTip].description}
                </p>
              </div>


              <div className="notice-controller">
                <button
                  type="button"
                  onClick={() =>
                    setActiveTip((current) =>
                      current === 0
                        ? donationTips.length - 1
                        : current - 1
                    )
                  }
                  aria-label="이전 헌혈팁"
                >
                  ←
                </button>

                <button
                  type="button"
                  onClick={() =>
                    setActiveTip((current) =>
                      current === donationTips.length - 1
                        ? 0
                        : current + 1
                    )
                  }
                  aria-label="다음 헌혈팁"
                >
                  →
                </button>
              </div>

            </aside>

          </section>

          {/* =================================================
              프로모션 / 캠페인 + SNS
              ================================================= */}

          <section className="main-promotion-section">

            {/* -----------------------------------------------
                프로모션 / 캠페인
                ----------------------------------------------- */}

            <article className="promotion-panel">

              <div className="information-panel-header">
                <div>
                  <span className="section-eyebrow">
                    PROMOTION
                  </span>

                  <h2>
                    프로모션 & 캠페인
                  </h2>
                </div>

                <Link
                  className="information-more"
                  to="/promotion"
                >
                  전체보기
                  <span aria-hidden="true">
                    →
                  </span>
                </Link>
              </div>


              <div className="promotion-grid">
                {promotions.map((promotion) => (
                  <Link
                    key={promotion.id}
                    to={promotion.to}
                    className="promotion-card"
                  >
                    <span className="promotion-category">
                      {promotion.category}
                    </span>

                    <h3>
                      {promotion.title}
                    </h3>

                    <p>
                      {promotion.description}
                    </p>

                    <span
                      className="promotion-arrow"
                      aria-hidden="true"
                    >
                      →
                    </span>
                  </Link>
                ))}
              </div>

            </article>


        {/* -----------------------------------------------
            SNS
            ----------------------------------------------- */}

        <aside className="social-panel">

          <div className="information-panel-header">
            <div>
              <span className="section-eyebrow">
                SOCIAL
              </span>

              <h2>
                BloodOps SNS
              </h2>
            </div>
          </div>


          <p className="social-intro">
            BloodOps의 캠페인과 헌혈 관련 소식을
            다양한 채널에서 만나보세요.
          </p>


          <div className="social-channel-list">
            {socialChannels.map((channel) => (
              <button
                key={channel.id}
                type="button"
                className="social-channel"
              >
                <span
                  className="social-channel-icon"
                  aria-hidden="true"
                >
                  {channel.symbol}
                </span>

                <span className="social-channel-text">
                  <strong>
                    {channel.label}
                  </strong>

                  <span>
                    {channel.description}
                  </span>
                </span>

                <span
                  className="social-channel-arrow"
                  aria-hidden="true"
                >
                  →
                </span>
              </button>
            ))}
          </div>

        </aside>

      </section>

    </main>
  )
}

export default MainPage