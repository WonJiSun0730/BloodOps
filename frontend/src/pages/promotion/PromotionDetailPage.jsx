import { useCallback, useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import {
  apiRequest,
  getCsrfToken,
  getCurrentUser,
} from '../../api/apiClient.js'
import PromotionEditor from './PromotionEditor.jsx'
import './PromotionsPage.css'

export default function PromotionDetailPage() {
  const { promotionId } = useParams()
  const navigate = useNavigate()
  const [item, setItem] = useState(null)
  const [isAdmin, setIsAdmin] = useState(false)
  const [editing, setEditing] = useState(false)
  const [optionId, setOptionId] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [sending, setSending] = useState(false)

  const loadDetail = useCallback(async () => {
    try {
      let admin = false
      try {
        const user = await getCurrentUser()
        admin = user.roles?.includes('ROLE_ADMIN') || false
      } catch {
        admin = false
      }
      setIsAdmin(admin)
      const promotion = await apiRequest(
        admin
          ? `/api/admin/promotions/${promotionId}`
          : `/api/promotions/${promotionId}`
      )
      setItem(promotion)
    } catch (requestError) {
      setError(requestError.message)
    }
  }, [promotionId])

  useEffect(() => {
    loadDetail()
  }, [loadDetail])

  const goBack = () => {
    if (window.history.length > 1) {
      navigate(-1)
      return
    }
    navigate('/promotion')
  }

  const enter = async (event) => {
    event.preventDefault()
    if (!optionId) {
      setError('응모할 항목을 선택해주세요.')
      return
    }
    setSending(true)
    setError('')
    try {
      const csrf = await getCsrfToken()
      await apiRequest(`/api/promotions/${promotionId}/entries`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          [csrf.headerName]: csrf.token,
        },
        body: JSON.stringify({ optionId: Number(optionId) }),
      })
      setMessage('응모가 완료되었습니다.')
      setItem({ ...item, entered: true })
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setSending(false)
    }
  }

  const remove = async () => {
    if (!window.confirm(`"${item.title}" 프로모션을 삭제하시겠습니까?`)) {
      return
    }
    try {
      const csrf = await getCsrfToken()
      await apiRequest(`/api/admin/promotions/${item.id}`, {
        method: 'DELETE',
        headers: { [csrf.headerName]: csrf.token },
      })
      navigate('/promotion', { replace: true })
    } catch (requestError) {
      setError(requestError.message)
    }
  }

  if (error && !item) {
    return (
      <main className="promotion-detail">
        <button type="button" className="promotion-back-button" onClick={goBack}>
          <span aria-hidden="true">←</span> 목록으로 돌아가기
        </button>
        <p className="promotion-status error">⚠ {error}</p>
      </main>
    )
  }

  if (!item) {
    return (
      <main className="promotion-detail">
        <p className="promotion-status">프로모션을 불러오는 중입니다.</p>
      </main>
    )
  }

  return (
    <main className="promotion-detail">
      <div className="promotion-detail-toolbar">
        <button type="button" className="promotion-back-button" onClick={goBack}>
          <span aria-hidden="true">←</span> 목록으로 돌아가기
        </button>
        {isAdmin && (
          <div className="promotion-admin-actions">
            <button
              type="button"
              className="promotion-button secondary"
              onClick={() => setEditing(true)}
            >
              수정
            </button>
            <button
              type="button"
              className="promotion-button danger"
              onClick={remove}
            >
              삭제
            </button>
          </div>
        )}
      </div>

      {isAdmin && editing && (
        <PromotionEditor
          promotion={item}
          onCancel={() => setEditing(false)}
          onSaved={(saved) => {
            setItem(saved)
            setEditing(false)
            setMessage('프로모션이 수정되었습니다.')
          }}
        />
      )}

      <article>
        <div className="promotion-detail-heading">
          <span>
            {item.category === 'NATIONAL'
              ? '전국 프로모션'
              : `${item.regionDisplayName || item.regionName} 프로모션`}
          </span>
          <h1>{item.title}</h1>
          <p>{item.startDate} ~ {item.endDate}</p>
        </div>

        {item.imageUrl && (
          <img
            className="promotion-detail-image"
            src={item.imageUrl}
            alt={`${item.title} 프로모션 이미지`}
          />
        )}

        {item.content && (
          <div className="promotion-content">
            {item.content.split('\n').map((line, index) => (
              <p key={index}>{line}</p>
            ))}
          </div>
        )}

        {item.type === 'LOTTERY' && (
          <form className="promotion-entry" onSubmit={enter}>
            <h2>응모 항목 선택</h2>
            <small>
              헌혈 실적 인정 기간: {item.donationEligibilityStartDate || item.startDate}
              {' '}~ {item.endDate}
            </small>
            <p>
              관리자가 설정한 헌혈 실적 인정 기간에 완료된 헌혈 기록이
              있는 회원만 응모할 수 있습니다.
            </p>
            {item.eligibilityMessage && (
              <p
                className={
                  item.eligibleToEnter
                    ? 'promotion-eligibility eligible'
                    : 'promotion-eligibility'
                }
              >
                {item.eligibilityMessage}
              </p>
            )}
            <fieldset disabled={item.entered || !item.eligibleToEnter || sending}>
              {item.options.map((option) => (
                <label
                  key={option.id}
                  className={String(option.id) === optionId ? 'selected' : ''}
                >
                  <input
                    type="radio"
                    name="promotion-option"
                    value={option.id}
                    checked={String(option.id) === optionId}
                    onChange={(event) => setOptionId(event.target.value)}
                  />
                  <span>{option.label}</span>
                </label>
              ))}
            </fieldset>
            <button disabled={item.entered || !item.eligibleToEnter || sending}>
              {item.entered
                ? '응모 완료'
                : sending
                  ? '응모 중…'
                  : '응모하기'}
            </button>
          </form>
        )}

        {message && <p className="promotion-status success">✓ {message}</p>}
        {error && <p className="promotion-status error">⚠ {error}</p>}
      </article>
    </main>
  )
}
