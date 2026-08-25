import { useCallback, useEffect, useState } from 'react'
import { getMedicalBloodUnits } from '../../api/apiClient.js'
import './BloodUnitsManagementPage.css'

const PAGE_SIZE = 20

function formatDateTime(value) {
  if (!value) return '-'
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(new Date(value))
}

function statusLabel(status) {
  if (['보유중', 'Available'].includes(status)) return '사용 가능'
  if (['예약', 'Reserved'].includes(status)) return '예약됨'
  if (['폐기', 'Disposed'].includes(status)) return '폐기'
  if (['보류', 'Hold'].includes(status)) return '보류'
  return status || '-'
}

function statusClass(status) {
  if (['보유중', 'Available'].includes(status)) return 'available'
  if (['예약', 'Reserved'].includes(status)) return 'reserved'
  return 'unavailable'
}

function BloodUnitsManagementPage() {
  const [data, setData] = useState(null)
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [lastUpdated, setLastUpdated] = useState('')

  const loadBloodUnits = useCallback(async () => {
    try {
      setLoading(true)
      setError('')
      const response = await getMedicalBloodUnits(page, PAGE_SIZE)
      setData(response)
      setLastUpdated(new Date().toLocaleTimeString('ko-KR'))
    } catch (requestError) {
      setData(null)
      setError(requestError.message || '혈액 재고를 불러오지 못했습니다.')
    } finally {
      setLoading(false)
    }
  }, [page])

  useEffect(() => {
    loadBloodUnits()
  }, [loadBloodUnits])

  const units = data?.content ?? []

  return (
    <main className="blood-units-page">
      <header className="blood-units-header">
        <div>
          <p className="blood-units-eyebrow">MEDICAL · BLOOD UNITS</p>
          <h1>혈액 재고 관리 대시보드</h1>
          <p>Spring Boot API를 통해 blood_units 테이블의 실제 데이터를 조회합니다.</p>
        </div>
        <div className="blood-units-meta">
          <span>DB · blood_units</span>
          <small>{lastUpdated ? `최신화: ${lastUpdated}` : '불러오는 중...'}</small>
        </div>
      </header>

      {error && (
        <div className="blood-units-alert" role="alert">
          {error}
          <button type="button" onClick={loadBloodUnits}>다시 시도</button>
        </div>
      )}

      <section className="blood-units-summary" aria-label="혈액 재고 요약">
        <article><span>전체 혈액 팩 수</span><strong>{data?.totalElements ?? 0} 팩</strong></article>
        <article><span>사용 가능</span><strong className="success">{data?.available ?? 0} 팩</strong></article>
        <article><span>유효기간 7일 이내</span><strong className="warning">{data?.expiringSoon ?? 0} 팩</strong></article>
        <article><span>폐기/보류</span><strong className="danger">{data?.disposedOrHeld ?? 0} 팩</strong></article>
      </section>

      <section className="blood-units-table-card">
        <div className="blood-units-table-title">
          <h2>혈액 팩 목록</h2>
          <button type="button" onClick={loadBloodUnits} disabled={loading}>
            {loading ? '조회 중...' : '새로고침'}
          </button>
        </div>

        <div className="blood-units-table-wrap">
          <table>
            <thead>
              <tr>
                <th>Unit ID</th>
                <th>혈액형</th>
                <th>제제 종류</th>
                <th>채혈일</th>
                <th>유효기간</th>
                <th>상태</th>
              </tr>
            </thead>
            <tbody>
              {!loading && !error && units.length === 0 && (
                <tr><td colSpan="6" className="empty">등록된 혈액 데이터가 없습니다.</td></tr>
              )}
              {loading && (
                <tr><td colSpan="6" className="empty">DB 데이터를 불러오는 중입니다...</td></tr>
              )}
              {!loading && units.map((unit) => (
                <tr key={unit.id ?? unit.unitId}>
                  <td className="unit-id">{unit.unitId}</td>
                  <td className="blood-type">{unit.bloodType}{unit.rhType ?? ''}</td>
                  <td>{unit.productType || '-'}</td>
                  <td>{formatDateTime(unit.collectionDate)}</td>
                  <td>{formatDateTime(unit.expirationDate)}</td>
                  <td>
                    <span className={`status-badge ${statusClass(unit.status)}`}>
                      {statusLabel(unit.status)}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {data && data.totalPages > 1 && (
          <nav className="blood-units-pagination" aria-label="혈액 재고 페이지 이동">
            <button type="button" disabled={page === 0 || loading} onClick={() => setPage((value) => value - 1)}>
              이전
            </button>
            <span>{page + 1} / {data.totalPages}</span>
            <button type="button" disabled={page + 1 >= data.totalPages || loading} onClick={() => setPage((value) => value + 1)}>
              다음
            </button>
          </nav>
        )}
      </section>
    </main>
  )
}

export default BloodUnitsManagementPage
