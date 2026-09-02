import { useEffect, useState } from 'react'

import { getBloodStockStatus }
  from '../../api/apiClient.js'

import './BloodStatusPage.css'

const bloodTypes = ['A', 'B', 'O', 'AB']

const crisisLevels = [
  {
    level: '관심',
    english: 'Blue',
    criterion: '적혈구제제 5일분 미만',
    note: '징후감시 활동',
  },
  {
    level: '주의',
    english: 'Yellow',
    criterion: '적혈구제제 3일분 미만',
    note: '협조체제 가동',
  },
  {
    level: '경계',
    english: 'Orange',
    criterion: '적혈구제제 2일분 미만',
    note: '대비계획 점검',
  },
  {
    level: '심각',
    english: 'Red',
    criterion: '적혈구제제 1일분 미만',
    note: '즉각 대응태세 돌입',
  },
]

function formatNumber(value) {
  return new Intl.NumberFormat('ko-KR').format(value)
}

function ProductTable({
  title,
  product,
}) {
  if (!product) {
    return null
  }

  const columns = [
    ['합계', product.total],
    ...bloodTypes.map((type) => [
      `${type}형`,
      product.bloodTypes[type],
    ]),
  ]

  return (
    <section className="blood-status-panel">
      <header>
        <div>
          <span className="blood-status-page-eyebrow">
            BLOOD STOCK
          </span>
          <h2>{title}</h2>
        </div>
      </header>

      <div className="blood-status-table-wrap">
        <table className="blood-status-table">
          <thead>
            <tr>
              <th scope="col">구분</th>
              {columns.map(([label]) => (
                <th scope="col" key={label}>
                  {label}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            <tr>
              <th scope="row">1일 소요량</th>
              {columns.map(([label, metric]) => (
                <td data-label={label} key={label}>
                  {formatNumber(metric.dailyUsage)}
                </td>
              ))}
            </tr>
            <tr>
              <th scope="row">현재 혈액보유량</th>
              {columns.map(([label, metric]) => (
                <td data-label={label} key={label}>
                  {formatNumber(metric.currentStock)}
                </td>
              ))}
            </tr>
            <tr className="blood-status-highlight-row">
              <th scope="row">
                {product.metricUnit === '일'
                  ? '보유상태'
                  : '보유율'}
              </th>
              {columns.map(([label, metric]) => (
                <td data-label={label} key={label}>
                  <strong>
                    {metric.value}
                    {product.metricUnit}
                  </strong>
                </td>
              ))}
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  )
}

function BloodStatusPage() {
  const [status, setStatus] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true

    getBloodStockStatus()
      .then((data) => {
        if (active) {
          setStatus(data)
        }
      })
      .catch(() => {
        if (active) {
          setError(
            '혈액 보유현황을 불러오지 못했습니다.'
          )
        }
      })

    return () => {
      active = false
    }
  }, [])

  const updatedAt = status?.updatedAt
    ? new Intl.DateTimeFormat('ko-KR', {
        dateStyle: 'long',
      }).format(new Date(status.updatedAt))
    : ''

  return (
    <main className="blood-status-page">
      <header className="blood-status-page-header">
        <span className="blood-status-page-eyebrow">
          TODAY&apos;S BLOOD STOCK
        </span>
        <h1>혈액 보유현황</h1>
        <p>
          대한적십자사 혈액관리본부의
          혈액 보유현황을 기준으로 안내합니다.
        </p>
        {updatedAt && (
          <time dateTime={status.updatedAt}>
            {updatedAt} 기준
          </time>
        )}
      </header>

      {error && (
        <div className="blood-status-page-error" role="alert">
          {error}
        </div>
      )}

      {!status && !error && (
        <div className="blood-status-page-loading">
          혈액 보유현황을 불러오는 중입니다.
        </div>
      )}

      {status && (
        <>
          <section
            className={`blood-crisis-card blood-crisis-card--${status.crisis.level}`}
            aria-labelledby="blood-crisis-title"
          >
            <span>혈액 수급 위기 단계 기준</span>
            <h2 id="blood-crisis-title">
              [{status.crisis.criterion}]{' '}
              <strong>{status.crisis.level}</strong>{' '}
              단계 입니다!
            </h2>
            <p>
              {status.crisis.message}
              {' '}(적정 혈액 보유량 5일분 이상)
            </p>
          </section>

          <section
            className="blood-crisis-criteria"
            aria-labelledby="blood-crisis-criteria-title"
          >
            <header>
              <span className="blood-status-page-eyebrow">
                CRISIS LEVEL
              </span>
              <h2 id="blood-crisis-criteria-title">
                혈액수급위기단계
              </h2>
            </header>

            <table className="blood-crisis-table">
              <thead>
                <tr>
                  <th scope="col">구분</th>
                  <th scope="col">판단 기준</th>
                  <th scope="col">비고</th>
                </tr>
              </thead>
              <tbody>
                {crisisLevels.map((item) => (
                  <tr key={item.level}>
                    <th
                      scope="row"
                      className={
                        `crisis-level crisis-level--${item.level}`
                      }
                    >
                      {item.level}
                      <span>({item.english})</span>
                    </th>
                    <td data-label="판단 기준">
                      {item.criterion}
                    </td>
                    <td data-label="비고">
                      {item.note}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </section>

          <ProductTable
            title="적혈구제제 보유 현황"
            product={status.redBloodCell}
          />

          <ProductTable
            title="농축혈소판 보유 현황"
            product={status.concentratedPlatelet}
          />

          <ProductTable
            title="성분채혈혈소판 보유 현황"
            product={status.apheresisPlatelet}
          />

          <p className="blood-status-source">
            출처: 대한적십자사 혈액관리본부
          </p>
        </>
      )}
    </main>
  )
}

export default BloodStatusPage
