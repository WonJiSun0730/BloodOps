import {
  useEffect,
  useState,
} from 'react'

import {
  Link,
} from 'react-router-dom'

import {
  getFavoriteCenters,
} from '../../api/apiClient.js'

import './MyPagePages.css'


const myPageServices = [
  {
    title: '헌혈기록',
    description:
      '완료된 헌혈 참여 기록을 확인합니다.',
    to: '/mypage/records',
  },
  {
    title: '헌혈예약 내역',
    description:
      '예약 일정의 조회·변경·취소를 관리합니다.',
    to: '/reservations',
  },
]


function MyPageHomePage() {
  const [
    favoriteCenters,
    setFavoriteCenters,
  ] = useState([])

  const [
    favoriteLoading,
    setFavoriteLoading,
  ] = useState(true)

  const [
    favoriteError,
    setFavoriteError,
  ] = useState('')


  useEffect(() => {
    let cancelled = false

    async function loadFavorites() {
      try {
        setFavoriteLoading(true)
        setFavoriteError('')

        const data =
          await getFavoriteCenters()

        if (!cancelled) {
          setFavoriteCenters(
            Array.isArray(data)
              ? data
              : []
          )
        }
      } catch (error) {
        if (!cancelled) {
          setFavoriteCenters([])

          setFavoriteError(
            error.message
            || '즐겨찾기 센터를 불러오지 못했습니다.'
          )
        }
      } finally {
        if (!cancelled) {
          setFavoriteLoading(false)
        }
      }
    }

    loadFavorites()

    return () => {
      cancelled = true
    }
  }, [])


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
            헌혈기록과 예약 내역을 한곳에서 확인할 수 있습니다.
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

            </Link>
          )
        )}
      </section>

            <section className="mypage-favorite-section">
              <div className="mypage-favorite-header">
                <div>
                  <span>FAVORITE CENTERS</span>

                  <h3>
                    즐겨찾기 센터
                  </h3>

                  <p>
                    자주 이용하는 헌혈센터에서
                    바로 예약할 수 있습니다.
                  </p>
                </div>

                {!favoriteLoading
                  && !favoriteError
                  && (
                    <span className="mypage-favorite-count">
                      {favoriteCenters.length}곳
                    </span>
                  )}
              </div>


              {favoriteLoading && (
                <div className="mypage-favorite-empty">
                  즐겨찾기 센터를 불러오는 중입니다.
                </div>
              )}


              {!favoriteLoading
                && favoriteError && (
                  <div className="mypage-favorite-empty">
                    {favoriteError}
                  </div>
                )}


      {!favoriteLoading
        && !favoriteError
        && favoriteCenters.length === 0 && (
          <div className="mypage-favorite-empty">
            <strong>
              아직 즐겨찾기한 센터가 없습니다.
            </strong>

            <p>
              헌혈기록에서 자주 이용하는
              센터를 ★로 등록해보세요.
            </p>

            <Link
              to="/mypage/records"
              className="mypage-favorite-empty-link"
            >
              헌혈기록 보기
            </Link>
          </div>
        )}


      {!favoriteLoading
        && !favoriteError
        && favoriteCenters.length > 0 && (
          <div className="mypage-favorite-list">
            {favoriteCenters.map(
              (center) => (
                <Link
                  key={center.id}
                  to={`/reservations/new?centerId=${center.centerId}`}
                  className="mypage-favorite-center-card"
                >
                  <div className="mypage-favorite-center-icon">
                    ★
                  </div>

                  <div className="mypage-favorite-center-info">
                    <strong>
                      {center.centerName}
                    </strong>

                    <span>
                      {center.address
                        || '주소 정보 없음'}
                    </span>
                  </div>

                  <div className="mypage-favorite-center-action">
                    예약하기
                  </div>
                </Link>
              )
            )}
          </div>
        )}
    </section>

    </div>
  )
}


export default MyPageHomePage
