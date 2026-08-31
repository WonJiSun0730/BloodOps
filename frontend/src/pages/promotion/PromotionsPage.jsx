import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { apiRequest } from '../../api/apiClient.js'
import './PromotionsPage.css'

const tabs = [{ value: 'NATIONAL', label: '전국 프로모션' }, { value: 'REGIONAL', label: '지역별 프로모션' }]
export default function PromotionsPage() {
  const [category, setCategory] = useState('NATIONAL')
  const [promotions, setPromotions] = useState([])
  const [error, setError] = useState('')
  useEffect(() => { setError(''); apiRequest(`/api/promotions?category=${category}`).then(setPromotions).catch(e => setError(e.message)) }, [category])
  return <main className="promotion-page">
    <header className="promotion-hero"><span>PROMOTION</span><h1>헌혈 프로모션</h1><p>헌혈 참여로 누릴 수 있는 전국·지역별 혜택을 확인하세요.</p></header>
    <div className="promotion-tabs" role="tablist" aria-label="프로모션 구분">{tabs.map(tab => <button key={tab.value} role="tab" aria-selected={category === tab.value} className={category === tab.value ? 'active' : ''} onClick={() => setCategory(tab.value)}>{tab.label}</button>)}</div>
    {error && <p className="promotion-status error">{error}</p>}
    {!error && promotions.length === 0 && <p className="promotion-status">진행 중인 프로모션이 없습니다.</p>}
    <section className="promotion-grid" aria-live="polite">{promotions.map(item => <Link className="promotion-card" to={`/promotions/${item.id}`} key={item.id}>
      <div className="promotion-card-image">{item.imageUrl ? <img src={item.imageUrl} alt="" /> : <span>BloodOps Promotion</span>}<span className="promotion-type">{item.type === 'LOTTERY' ? '추첨' : '혜택'}</span></div>
      <div className="promotion-card-body"><div className="promotion-meta">{item.category === 'NATIONAL' ? '전국' : item.regionName}<span>{item.startDate} ~ {item.endDate}</span></div><h2>{item.title}</h2><p>{item.summary}</p><strong>자세히 보기 →</strong></div>
    </Link>)}</section>
  </main>
}
