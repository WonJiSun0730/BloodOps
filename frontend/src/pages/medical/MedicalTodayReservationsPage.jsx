import { useCallback, useEffect, useState } from 'react'
import { completeMedicalReservation, getMedicalTodayReservations } from '../../api/apiClient.js'
import './MedicalWorkspaceListPage.css'

const typeLabel = { WHOLE_BLOOD: '전혈', PLATELET: '혈소판', PLASMA: '혈장', PLATELET_PLASMA: '혈소판혈장' }

function MedicalTodayReservationsPage() {
  const [items, setItems] = useState([])
  const [error, setError] = useState('')
  const [processingId, setProcessingId] = useState(null)
  const load = useCallback(() => getMedicalTodayReservations().then(setItems).catch((e) => setError(e.message)), [])
  useEffect(() => { load() }, [load])

  async function complete(item) {
    if (!window.confirm(`${item.memberName}님의 예약을 헌혈 완료 처리할까요?`)) return
    try {
      setProcessingId(item.id); setError('')
      await completeMedicalReservation(item.id)
      await load()
    } catch (e) { setError(e.message) } finally { setProcessingId(null) }
  }

  return <main className="medical-workspace-list">
    <header><p>MEDICAL · TODAY</p><h1>당일 헌혈 예약자</h1><span>오늘 방문 예정자를 시간순으로 확인하고 헌혈 완료 처리합니다.</span></header>
    {error && <div className="workspace-state is-error" role="alert">{error}</div>}
    <section className="workspace-panel">
      {items.length === 0 ? <div className="workspace-state">오늘 예약된 헌혈자가 없습니다.</div> : <div className="workspace-card-list">
        {items.map((item) => <article className="workspace-data-card is-static" key={item.id}>
          <strong>{item.startTime?.slice(0, 5)} · {item.memberName}</strong><span>{item.centerName}</span><span>{typeLabel[item.donationType] || item.donationType}</span><em>{item.status === 'COMPLETED' ? '헌혈 완료' : item.status === 'CANCELED' ? '예약 취소' : '예약 대기'}</em>
          <button type="button" disabled={item.status !== 'RESERVED' || processingId === item.id} onClick={() => complete(item)}>{processingId === item.id ? '처리 중…' : '헌혈 완료 처리'}</button>
        </article>)}
      </div>}
    </section>
  </main>
}

export default MedicalTodayReservationsPage
