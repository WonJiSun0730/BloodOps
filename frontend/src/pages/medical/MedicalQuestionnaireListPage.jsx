import { useCallback, useEffect, useState } from 'react'
import {
  getMedicalBloodUnitCenters,
  getMedicalQuestionnaires,
} from '../../api/apiClient.js'
import './MedicalWorkspaceListPage.css'

const PAGE_SIZE = 10

const reservationStatusLabel = {
  RESERVED: '예약 대기',
  COMPLETED: '헌혈 완료',
  CANCELED: '예약 취소',
}

function getReservationLabel(item) {
  if (item.reservationId && item.reservationWithinValidity === false) {
    return '문진 유효시간 외 예약'
  }
  if (item.reservationStatus) {
    return reservationStatusLabel[item.reservationStatus] || item.reservationStatus
  }
  if (item.visitType === 'WALK_IN') return '현장 방문'
  return item.visitType === 'UNDECIDED' ? '방문 방식 미정' : '예약 정보 없음'
}

function getReservationDateTime(item) {
  if (!item.reservationDate) return ''
  const startTime = item.reservationStartTime?.slice(0, 5) || ''
  const endTime = item.reservationEndTime?.slice(0, 5) || ''
  return `${item.reservationDate} ${startTime}${endTime ? `~${endTime}` : ''}`.trim()
}

function getConnectionLabel(type) {
  if (type === 'SELECTED') return '문진 작성 시 직접 연결'
  if (type === 'AUTO_IDENTITY') return '신분정보로 회원 예약 자동 연결'
  if (type === 'AUTO_CURRENT') return '문진 제출 후 회원 예약 자동 반영'
  return '예약 연결 없음'
}

function MedicalQuestionnaireListPage() {
  const [centers, setCenters] = useState([])
  const [centerId, setCenterId] = useState('')
  const [items, setItems] = useState([])
  const [selected, setSelected] = useState(null)
  const [page, setPage] = useState(0)
  const [pageInfo, setPageInfo] = useState({ totalElements: 0, totalPages: 0 })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    getMedicalBloodUnitCenters()
      .then((data) => setCenters(Array.isArray(data) ? data : []))
      .catch((requestError) => setError(requestError.message || '센터 목록을 불러오지 못했습니다.'))
  }, [])

  const load = useCallback(async () => {
    try {
      setLoading(true)
      setError('')
      const data = await getMedicalQuestionnaires(centerId, page, PAGE_SIZE)
      setItems(Array.isArray(data.content) ? data.content : [])
      setPageInfo({
        totalElements: Number(data.totalElements) || 0,
        totalPages: Number(data.totalPages) || 0,
      })
      setSelected(null)
    } catch (requestError) {
      setError(requestError.message || '전자문진 목록을 불러오지 못했습니다.')
    } finally {
      setLoading(false)
    }
  }, [centerId, page])

  useEffect(() => {
    load()
  }, [load])

  return (
    <main className="medical-workspace-list">
      <header>
        <p>MEDICAL · QUESTIONNAIRES</p>
        <h1>전자문진 작성자 목록</h1>
        <span>유효한 문진을 최신순으로 조회하고 내용을 확인합니다.</span>
      </header>

      <section className="workspace-center-filter" aria-labelledby="questionnaire-center-filter-title">
        <div>
          <h2 id="questionnaire-center-filter-title">센터 선택</h2>
          <p>전체 센터 또는 예약 센터별 전자문진 작성자를 조회합니다.</p>
        </div>
        <label>
          <span>조회 센터</span>
          <select value={centerId} onChange={(event) => { setCenterId(event.target.value); setPage(0) }}>
            <option value="">전체 센터</option>
            {centers.map((center) => (
              <option key={center.id} value={center.id}>{center.centerName} · {center.address}</option>
            ))}
          </select>
        </label>
      </section>

      {error && <div className="workspace-state is-error" role="alert">{error}</div>}

      <section className="workspace-panel">
        {loading ? (
          <div className="workspace-state">전자문진 목록을 불러오는 중입니다.</div>
        ) : items.length === 0 ? (
          <div className="workspace-state">선택한 조건에 해당하는 유효한 전자문진이 없습니다.</div>
        ) : (
          <div className="workspace-card-list">
            {items.map((item) => (
              <button type="button" className="workspace-data-card questionnaire-data-card" key={item.id} onClick={() => setSelected(selected?.id === item.id ? null : item)} aria-expanded={selected?.id === item.id}>
                <strong>{item.donorName}</strong>
                <span>{item.receiptCode}</span>
                <span className="workspace-reservation">
                  <strong>{item.donationCenterName || '방문 장소 미지정'}</strong>
                  {getReservationDateTime(item) && <small>{getReservationDateTime(item)}</small>}
                  <small>{getReservationLabel(item)}</small>
                </span>
                <span>{String(item.submittedAt).replace('T', ' ').slice(0, 16)}</span>
                <em>{item.status === 'REVIEW_REQUIRED' ? '현장 확인 필요' : '제출 완료'}</em>
              </button>
            ))}
          </div>
        )}

        {selected && (
          <article className="workspace-detail">
            <h2>{selected.donorName} 문진 상세</h2>
            <dl>
              <div><dt>체중</dt><dd>{selected.weightKg} kg</dd></div>
              <div><dt>연락처</dt><dd>{selected.contactPhone || '-'}</dd></div>
              <div><dt>이메일</dt><dd>{selected.contactEmail || '-'}</dd></div>
              <div><dt>방문 방식</dt><dd>{selected.visitType === 'RESERVATION' ? '예약 방문' : selected.visitType === 'WALK_IN' ? '현장 방문' : '미정'}</dd></div>
              <div><dt>방문 예정 장소</dt><dd>{selected.donationCenterName ? `${selected.donationCenterName} · ${selected.donationCenterAddress || ''}` : '방문 장소 미지정'}</dd></div>
              <div><dt>예약 일시</dt><dd>{getReservationDateTime(selected) || '-'}</dd></div>
              <div><dt>예약 상태</dt><dd>{getReservationLabel(selected)}</dd></div>
              <div><dt>연결 방식</dt><dd>{getConnectionLabel(selected.reservationConnectionType)}</dd></div>
            </dl>
            {selected.reviewReasons?.length > 0 && (
              <><h3>현장 확인 항목</h3><ul>{selected.reviewReasons.map((reason) => <li key={reason}>{reason}</li>)}</ul></>
            )}
          </article>
        )}

        {pageInfo.totalPages > 1 && (
          <nav className="workspace-pagination" aria-label="전자문진 목록 페이지">
            <button type="button" disabled={page === 0} onClick={() => setPage((current) => current - 1)}>이전</button>
            <span>{page + 1} / {pageInfo.totalPages} · 총 {pageInfo.totalElements}건</span>
            <button type="button" disabled={page + 1 >= pageInfo.totalPages} onClick={() => setPage((current) => current + 1)}>다음</button>
          </nav>
        )}
      </section>
    </main>
  )
}

export default MedicalQuestionnaireListPage
