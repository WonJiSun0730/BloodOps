import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { apiRequest, getCurrentUser } from '../../api/apiClient.js'
import PromotionEditor from './PromotionEditor.jsx'
import './PromotionsPage.css'

const tabs = [
  { value: 'NATIONAL', label: '전국 프로모션' },
  { value: 'REGIONAL', label: '지역별 프로모션' },
]

export default function PromotionsPage() {
  const [category, setCategory] = useState('NATIONAL')
  const [promotions, setPromotions] = useState([])
  const [isAdmin, setIsAdmin] = useState(false)
  const [authReady, setAuthReady] = useState(false)
  const [editorOpen, setEditorOpen] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    getCurrentUser()
      .then((user) => setIsAdmin(user.roles?.includes('ROLE_ADMIN') || false))
      .catch(() => setIsAdmin(false))
      .finally(() => setAuthReady(true))
  }, [])

  const loadPromotions = useCallback(() => {
    if (!authReady) return
    const request = isAdmin
      ? apiRequest('/api/admin/promotions')
      : apiRequest(`/api/promotions?category=${category}`)
    request
      .then((items) =>
        setPromotions(
          isAdmin
            ? items.filter((item) => item.category === category)
            : items
        )
      )
      .catch((requestError) => setError(requestError.message))
  }, [authReady, category, isAdmin])

  useEffect(() => {
    loadPromotions()
  }, [loadPromotions])

  const selectCategory = (value) => {
    setError('')
    setCategory(value)
  }

  return (
    <main className="promotion-page">
      <header className="promotion-hero">
        <div>
          <span>PROMOTION</span>
          <h1>헌혈 프로모션</h1>
          <p>헌혈 참여로 누릴 수 있는 전국·지역별 혜택을 확인하세요.</p>
        </div>
        {isAdmin && (
          <button
            type="button"
            className="promotion-button primary"
            onClick={() => setEditorOpen(true)}
          >
            프로모션 작성
          </button>
        )}
      </header>

      {isAdmin && editorOpen && (
        <PromotionEditor
          onCancel={() => setEditorOpen(false)}
          onSaved={() => {
            setEditorOpen(false)
            loadPromotions()
          }}
        />
      )}

      <div className="promotion-tabs" role="tablist" aria-label="프로모션 구분">
        {tabs.map((tab) => (
          <button
            type="button"
            key={tab.value}
            role="tab"
            aria-selected={category === tab.value}
            className={category === tab.value ? 'active' : ''}
            onClick={() => selectCategory(tab.value)}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {error && <p className="promotion-status error">⚠ {error}</p>}
      {!error && promotions.length === 0 && (
        <p className="promotion-status">진행 중인 프로모션이 없습니다.</p>
      )}

      <section className="promotion-grid" aria-live="polite">
        {promotions.map((item) => (
          <Link
            className="promotion-card"
            to={`/promotion/${item.id}`}
            key={item.id}
          >
            <div className="promotion-card-image">
              {item.imageUrl ? (
                <img src={item.imageUrl} alt="" />
              ) : (
                <span>BloodOps Promotion</span>
              )}
              <span className="promotion-type">
                {item.type === 'LOTTERY' ? '추첨' : '혜택'}
              </span>
              {isAdmin && !item.active && (
                <span className="promotion-hidden-badge">숨김</span>
              )}
            </div>
            <div className="promotion-card-body">
              <div className="promotion-meta">
                {item.category === 'NATIONAL'
                  ? '전국'
                  : item.regionDisplayName || item.regionName}
                <span>{item.startDate} ~ {item.endDate}</span>
              </div>
              <h2>{item.title}</h2>
              {item.summary && <p>{item.summary}</p>}
              <strong>자세히 보기 →</strong>
            </div>
          </Link>
        ))}
      </section>
    </main>
  )
}
