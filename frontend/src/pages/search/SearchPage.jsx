import {
  Link,
  useSearchParams,
} from 'react-router-dom'

import './SearchPage.css'


const searchItems = [
  {
    title: '헌혈센터 찾기',
    description:
      '가까운 헌혈센터를 검색하고 위치 정보를 확인합니다.',
    path: '/centers',
    keywords: [
      '센터',
      '헌혈센터',
      '헌혈의집',
      '위치',
      '지도',
      '찾기',
    ],
  },
  {
    title: '헌혈 예약',
    description:
      '헌혈센터와 날짜, 시간을 선택해 예약합니다.',
    path: '/reservations/new',
    keywords: [
      '예약',
      '헌혈예약',
      '예약하기',
      '시간',
      '날짜',
    ],
  },
  {
    title: '예약 내역',
    description:
      '내 헌혈 예약을 조회하고 변경 또는 취소합니다.',
    path: '/reservations',
    keywords: [
      '예약내역',
      '예약조회',
      '예약변경',
      '예약취소',
    ],
  },
  {
    title: '헌혈 기록',
    description:
      '완료된 헌혈 기록을 확인합니다.',
    path: '/mypage/records',
    keywords: [
      '헌혈기록',
      '기록',
      '헌혈내역',
      '내역',
    ],
  },
  {
    title: '검사 결과',
    description:
      '헌혈 후 검사 결과를 확인합니다.',
    path: '/mypage/results',
    keywords: [
      '검사',
      '검사결과',
      '혈액검사',
      '결과',
    ],
  },
  {
    title: '혈액사용 보류',
    description:
      '최근 헌혈 후 혈액사용 보류가 필요한 경우 연락처를 안내합니다.',
    path: '/mypage/blood-hold',
    keywords: [
      '혈액사용보류',
      '보류',
      '혈액원',
      '질병',
      '헌혈후',
    ],
  },
  {
    title: '헌혈 팁',
    description:
      '헌혈 전·후 알아두면 좋은 주의사항과 정보를 확인합니다.',
    path: '/donation-tips',
    keywords: [
      '헌혈팁',
      '주의사항',
      '헌혈전',
      '헌혈후',
      '팁',
    ],
  },
  {
    title: '혈액 보유 현황',
    description:
      '혈액형별 혈액 보유 현황을 확인합니다.',
    path: '/blood-status',
    keywords: [
      '혈액',
      '혈액보유',
      '보유현황',
      '재고',
      '혈액형',
    ],
  },
  {
    title: '소식 & 프로모션',
    description:
      'BloodOps의 소식과 프로모션을 확인합니다.',
    path: '/promotion',
    keywords: [
      '소식',
      '프로모션',
      '이벤트',
      '공지',
    ],
  },
  {
    title: '마이페이지',
    description:
      '내 헌혈 활동과 개인 정보를 확인합니다.',
    path: '/mypage',
    keywords: [
      '마이페이지',
      '내정보',
      '회원정보',
      '활동',
    ],
  },
  {
    title: '포인트',
    description:
      '보유 포인트와 포인트 이용 내역을 확인합니다.',
    path: '/mypage/points',
    keywords: [
      '포인트',
      '점수',
      '리워드',
      '보상',
    ],
  },
]


function normalizeKeyword(value) {
  return String(value || '')
    .trim()
    .toLowerCase()
    .replace(/\s+/g, '')
}


function SearchPage() {
  const [searchParams] =
    useSearchParams()

  const keyword =
    searchParams.get('keyword')?.trim()
    || ''

  const normalizedKeyword =
    normalizeKeyword(keyword)

  const results =
    normalizedKeyword
      ? searchItems.filter((item) => {
          const searchableText =
            [
              item.title,
              item.description,
              ...item.keywords,
            ]
              .map(normalizeKeyword)
              .join(' ')

          return searchableText.includes(
            normalizedKeyword
          )
        })
      : []


  return (
    <main className="search-page">
      <section className="search-page-header">
        <span>
          SEARCH
        </span>

        <h1>
          사이트 검색
        </h1>

        {keyword ? (
          <p>
            <strong>
              “{keyword}”
            </strong>
            {' '}
            검색 결과입니다.
          </p>
        ) : (
          <p>
            찾고 싶은 기능이나
            메뉴를 검색해 주세요.
          </p>
        )}
      </section>


      <section
        className="search-result-section"
        aria-live="polite"
      >
        <header className="search-result-header">
          <h2>
            검색 결과
          </h2>

          <strong>
            {results.length}건
          </strong>
        </header>


        {!keyword ? (
          <div className="search-empty-state">
            검색어를 입력해 주세요.
          </div>
        ) : results.length === 0 ? (
          <div className="search-empty-state">
            <strong>
              검색 결과가 없습니다.
            </strong>

            <p>
              다른 검색어를 입력하거나
              상단 메뉴를 이용해 주세요.
            </p>
          </div>
        ) : (
          <div className="search-result-list">
            {results.map((item) => (
              <Link
                key={item.path}
                to={item.path}
                className="search-result-card"
              >
                <div>
                  <span>
                    BloodOps
                  </span>

                  <h3>
                    {item.title}
                  </h3>

                  <p>
                    {item.description}
                  </p>
                </div>

                <strong
                  className="search-result-arrow"
                  aria-hidden="true"
                >
                  →
                </strong>
              </Link>
            ))}
          </div>
        )}
      </section>
    </main>
  )
}


export default SearchPage