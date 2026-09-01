import { useEffect, useState } from 'react'
import { getMedicalQuestionnaires } from '../../api/apiClient.js'
import './MedicalWorkspaceListPage.css'

function MedicalQuestionnaireListPage() {
  const [items, setItems] = useState([])
  const [selected, setSelected] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    getMedicalQuestionnaires().then(setItems).catch((e) => setError(e.message))
  }, [])

  return <main className="medical-workspace-list">
    <header><p>MEDICAL · QUESTIONNAIRES</p><h1>전자문진 작성자 목록</h1><span>제출된 문진을 최신순으로 조회하고 내용을 확인합니다.</span></header>
    {error && <div className="workspace-state is-error" role="alert">{error}</div>}
    <section className="workspace-panel">
      {items.length === 0 ? <div className="workspace-state">제출된 전자문진이 없습니다.</div> : <div className="workspace-card-list">
        {items.map((item) => <button type="button" className="workspace-data-card" key={item.id} onClick={() => setSelected(selected?.id === item.id ? null : item)} aria-expanded={selected?.id === item.id}>
          <strong>{item.donorName}</strong><span>{item.receiptCode}</span><span>{String(item.submittedAt).replace('T', ' ').slice(0, 16)}</span><em>{item.status === 'REVIEW_REQUIRED' ? '현장 확인 필요' : '제출 완료'}</em>
        </button>)}
      </div>}
      {selected && <article className="workspace-detail">
        <h2>{selected.donorName} 문진 상세</h2>
        <dl><div><dt>체중</dt><dd>{selected.weightKg} kg</dd></div><div><dt>연락처</dt><dd>{selected.contactPhone || '-'}</dd></div><div><dt>이메일</dt><dd>{selected.contactEmail || '-'}</dd></div></dl>
        {selected.reviewReasons?.length > 0 && <><h3>현장 확인 항목</h3><ul>{selected.reviewReasons.map((reason) => <li key={reason}>{reason}</li>)}</ul></>}
      </article>}
    </section>
  </main>
}

export default MedicalQuestionnaireListPage
