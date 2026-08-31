import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { apiRequest, getCsrfToken } from '../../api/apiClient.js'
import './PromotionsPage.css'

export default function PromotionDetailPage() {
  const { promotionId } = useParams(); const [item, setItem] = useState(null); const [optionId, setOptionId] = useState(''); const [message, setMessage] = useState(''); const [error, setError] = useState(''); const [sending, setSending] = useState(false)
  useEffect(() => { apiRequest(`/api/promotions/${promotionId}`).then(setItem).catch(e => setError(e.message)) }, [promotionId])
  const enter = async e => { e.preventDefault(); if (!optionId) { setError('응모할 항목을 선택해주세요.'); return } setSending(true); setError(''); try { const csrf = await getCsrfToken(); await apiRequest(`/api/promotions/${promotionId}/entries`, { method: 'POST', headers: { 'Content-Type': 'application/json', [csrf.headerName]: csrf.token }, body: JSON.stringify({ optionId: Number(optionId) }) }); setMessage('응모가 완료되었습니다.'); setItem({ ...item, entered: true }) } catch (requestError) { setError(requestError.message) } finally { setSending(false) } }
  if (error && !item) return <main className="promotion-detail"><p className="promotion-status error">{error}</p><Link to="/promotion">목록으로</Link></main>
  if (!item) return <main className="promotion-detail"><p className="promotion-status">프로모션을 불러오는 중입니다.</p></main>
  return <main className="promotion-detail"><Link className="promotion-back" to="/promotions">← 프로모션 목록</Link><article>
    {item.imageUrl && <img className="promotion-detail-image" src={item.imageUrl} alt={`${item.title} 프로모션 이미지`} />}
    <div className="promotion-detail-heading"><span>{item.category === 'NATIONAL' ? '전국 프로모션' : `${item.regionName} 프로모션`}</span><h1>{item.title}</h1><p>{item.startDate} ~ {item.endDate}</p></div>
    <div className="promotion-content">{item.content.split('\n').map((line, i) => <p key={i}>{line}</p>)}</div>
    {item.type === 'LOTTERY' && <form className="promotion-entry" onSubmit={enter}><h2>응모 항목 선택</h2><p>프로모션 기간 내 헌혈 완료 실적이 있는 회원만 응모할 수 있습니다. 원하는 항목 하나를 선택해 주세요.</p>{item.eligibilityMessage && <p className={item.eligibleToEnter ? 'promotion-eligibility eligible' : 'promotion-eligibility'}>{item.eligibilityMessage}</p>}<fieldset disabled={item.entered || !item.eligibleToEnter || sending}>{item.options.map(option => <label key={option.id} className={String(option.id) === optionId ? 'selected' : ''}><input type="radio" name="promotion-option" value={option.id} checked={String(option.id) === optionId} onChange={e => setOptionId(e.target.value)} /><span>{option.label}</span></label>)}</fieldset><button disabled={item.entered || !item.eligibleToEnter || sending}>{item.entered ? '응모 완료' : sending ? '응모 중…' : '응모하기'}</button></form>}
    {message && <p className="promotion-status success">{message}</p>}{error && <p className="promotion-status error">{error}</p>}
  </article></main>
}
