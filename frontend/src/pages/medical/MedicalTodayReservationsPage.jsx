import { useCallback, useEffect, useState } from 'react'
import {
  completeMedicalReservation,
  getMedicalBloodUnitCenters,
  getMedicalTodayReservations,
} from '../../api/apiClient.js'
import './MedicalWorkspaceListPage.css'

const typeLabel = {
  WHOLE_BLOOD: '전혈',
  PLATELET: '혈소판',
  PLASMA: '혈장',
  PLATELET_PLASMA: '혈소판혈장',
}

function MedicalTodayReservationsPage() {
  const [centers, setCenters] = useState([])
  const [centerId, setCenterId] = useState('')
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [processingId, setProcessingId] = useState(null)

  useEffect(() => {
    let active = true
    getMedicalBloodUnitCenters()
      .then((data) => {
        if (active) setCenters(Array.isArray(data) ? data : [])
      })
      .catch((requestError) => {
        if (active) {
          setError(requestError.message || '센터 목록을 불러오지 못했습니다.')
        }
      })
    return () => {
      active = false
    }
  }, [])

  const load = useCallback(async () => {
    try {
      setLoading(true)
      setError('')
      const data = await getMedicalTodayReservations(centerId)
      setItems(Array.isArray(data) ? data : [])
    } catch (requestError) {
      setError(requestError.message || '당일 예약자를 불러오지 못했습니다.')
    } finally {
      setLoading(false)
    }
  }, [centerId])

  useEffect(() => {
    load()
  }, [load])

  async function complete(item) {
    if (!window.confirm(`${item.memberName}님의 예약을 헌혈 완료 처리할까요?`)) return
    try {
      setProcessingId(item.id)
      setError('')
      await completeMedicalReservation(item.id)
      await load()
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setProcessingId(null)
    }
  }

  return (
    <main className="medical-workspace-list">
      <header>
        <p>MEDICAL · TODAY</p>
        <h1>당일 헌혈 예약자</h1>
        <span>오늘 방문 예정자를 시간순으로 확인하고 헌혈 완료 처리합니다.</span>
      </header>

      <section className="workspace-center-filter" aria-labelledby="today-center-filter-title">
        <div>
          <h2 id="today-center-filter-title">센터 선택</h2>
          <p>전체 센터 또는 선택한 센터의 당일 예약자를 조회합니다.</p>
        </div>
        <label>
          <span>조회 센터</span>
          <select value={centerId} onChange={(event) => setCenterId(event.target.value)}>
            <option value="">전체 센터</option>
            {centers.map((center) => (
              <option key={center.id} value={center.id}>
                {center.centerName} · {center.address}
              </option>
            ))}
          </select>
        </label>
      </section>

      {error && <div className="workspace-state is-error" role="alert">{error}</div>}

      <section className="workspace-panel">
        {loading ? (
          <div className="workspace-state">당일 예약자를 불러오는 중입니다.</div>
        ) : items.length === 0 ? (
          <div className="workspace-state">선택한 조건에 해당하는 당일 예약자가 없습니다.</div>
        ) : (
          <div className="workspace-card-list">
            {items.map((item) => (
              <article className="workspace-data-card is-static" key={item.id}>
                <strong>{item.startTime?.slice(0, 5)} · {item.memberName}</strong>
                <span>{item.centerName}</span>
                <span>{typeLabel[item.donationType] || item.donationType}</span>
                <em>{item.status === 'COMPLETED' ? '헌혈 완료' : item.status === 'CANCELED' ? '예약 취소' : '예약 대기'}</em>
                <button type="button" disabled={item.status !== 'RESERVED' || processingId === item.id} onClick={() => complete(item)}>
                  {processingId === item.id ? '처리 중…' : '헌혈 완료 처리'}
                </button>
              </article>
            ))}
          </div>
        )}
      </section>
    </main>
  )
}

export default MedicalTodayReservationsPage
