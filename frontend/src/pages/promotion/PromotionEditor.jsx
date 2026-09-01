import { useState } from 'react'
import { apiRequest, getCsrfToken } from '../../api/apiClient.js'

const regions = [
  ['SEOUL', '서울'],
  ['GYEONGGI', '경기'],
  ['INCHEON', '인천'],
  ['GANGWON', '강원'],
  ['CHUNGBUK', '충북'],
  ['DAEJEON_SEJONG_CHUNGNAM', '대전·세종·충남'],
  ['DAEGU_GYEONGBUK', '대구·경북'],
  ['GYEONGNAM', '경남'],
  ['ULSAN', '울산'],
  ['BUSAN', '부산'],
  ['JEONBUK', '전북'],
  ['JEONNAM_GWANGJU', '전남·광주'],
  ['JEJU', '제주'],
]

const emptyForm = {
  category: 'NATIONAL',
  type: 'INFORMATION',
  regionName: '',
  title: '',
  summary: '',
  content: '',
  startDate: '',
  endDate: '',
  donationEligibilityStartDate: '',
  active: true,
  options: ['', ''],
}

function formFromPromotion(promotion) {
  if (!promotion) return emptyForm
  return {
    category: promotion.category,
    type: promotion.type,
    regionName: promotion.regionName || '',
    title: promotion.title,
    summary: promotion.summary,
    content: promotion.content,
    startDate: promotion.startDate,
    endDate: promotion.endDate,
    donationEligibilityStartDate:
      promotion.donationEligibilityStartDate || promotion.startDate,
    active: promotion.active,
    options: promotion.options.length
      ? promotion.options.map((option) => option.label)
      : ['', ''],
  }
}

export default function PromotionEditor({
  promotion = null,
  onSaved,
  onCancel,
}) {
  const [form, setForm] = useState(() => formFromPromotion(promotion))
  const [imageFile, setImageFile] = useState(null)
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  const changeOption = (index, value) => {
    setForm((current) => ({
      ...current,
      options: current.options.map((option, optionIndex) =>
        optionIndex === index ? value : option
      ),
    }))
  }

  const submit = async (event) => {
    event.preventDefault()
    setError('')
    setSaving(true)
    try {
      const csrf = await getCsrfToken()
      const payload = {
        ...form,
        regionName: form.category === 'REGIONAL' ? form.regionName : null,
        donationEligibilityStartDate:
          form.type === 'LOTTERY'
            ? form.donationEligibilityStartDate || form.startDate
            : null,
        options:
          form.type === 'LOTTERY'
            ? form.options.map((option) => option.trim()).filter(Boolean)
            : [],
      }
      let saved = await apiRequest(
        promotion
          ? `/api/admin/promotions/${promotion.id}`
          : '/api/admin/promotions',
        {
          method: promotion ? 'PUT' : 'POST',
          headers: {
            'Content-Type': 'application/json',
            [csrf.headerName]: csrf.token,
          },
          body: JSON.stringify(payload),
        }
      )

      if (imageFile) {
        const imageData = new FormData()
        imageData.append('image', imageFile)
        saved = await apiRequest(
          `/api/admin/promotions/${saved.id}/image`,
          {
            method: 'PUT',
            headers: { [csrf.headerName]: csrf.token },
            body: imageData,
          }
        )
      }
      onSaved(saved)
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <form className="promotion-editor" onSubmit={submit}>
      <header>
        <div>
          <span>ADMIN ONLY</span>
          <h2>{promotion ? '프로모션 수정' : '프로모션 작성'}</h2>
        </div>
        <button type="button" className="promotion-button secondary" onClick={onCancel}>
          닫기
        </button>
      </header>

      <div className="promotion-editor-grid">
        <label>
          프로모션 구분
          <select
            value={form.category}
            onChange={(event) =>
              setForm({ ...form, category: event.target.value })
            }
          >
            <option value="NATIONAL">전국 프로모션</option>
            <option value="REGIONAL">지역별 프로모션</option>
          </select>
        </label>

        <label>
          게시물 유형
          <select
            value={form.type}
            onChange={(event) =>
              setForm({ ...form, type: event.target.value })
            }
          >
            <option value="INFORMATION">안내형</option>
            <option value="LOTTERY">추첨형</option>
          </select>
        </label>

        {form.category === 'REGIONAL' && (
          <label>
            지역
            <select
              required
              value={form.regionName}
              onChange={(event) =>
                setForm({ ...form, regionName: event.target.value })
              }
            >
              <option value="">지역을 선택하세요</option>
              {regions.map(([value, label]) => (
                <option value={value} key={value}>{label}</option>
              ))}
            </select>
          </label>
        )}

        <label className="wide">
          제목
          <input
            required
            maxLength="150"
            value={form.title}
            onChange={(event) =>
              setForm({ ...form, title: event.target.value })
            }
          />
        </label>

        <label className="wide">
          요약
          <input
            required
            maxLength="300"
            value={form.summary}
            onChange={(event) =>
              setForm({ ...form, summary: event.target.value })
            }
          />
        </label>

        <label>
          게시 시작일
          <input
            required
            type="date"
            value={form.startDate}
            onChange={(event) =>
              setForm({ ...form, startDate: event.target.value })
            }
          />
        </label>

        <label>
          게시 종료일
          <input
            required
            type="date"
            value={form.endDate}
            onChange={(event) =>
              setForm({ ...form, endDate: event.target.value })
            }
          />
        </label>

        {form.type === 'LOTTERY' && (
          <label className="wide">
            헌혈 실적 인정 시작일
            <input
              required
              type="date"
              max={form.endDate || undefined}
              value={form.donationEligibilityStartDate}
              onChange={(event) =>
                setForm({
                  ...form,
                  donationEligibilityStartDate: event.target.value,
                })
              }
            />
            <small>
              이 날짜부터 게시 종료일까지 완료된 헌혈 기록을 인정합니다.
            </small>
          </label>
        )}

        <label className="wide">
          게시물 이미지
          <input
            type="file"
            accept="image/jpeg,image/png,image/webp,image/gif"
            onChange={(event) =>
              setImageFile(event.target.files?.[0] || null)
            }
          />
          <small>JPG, PNG, WebP, GIF · 최대 5MB</small>
        </label>

        {imageFile && (
          <small className="wide">선택된 파일: {imageFile.name}</small>
        )}

        {!imageFile && promotion?.imageUrl && (
          <img
            className="promotion-editor-preview"
            src={promotion.imageUrl}
            alt="현재 프로모션 이미지"
          />
        )}

        <label className="wide">
          본문
          <textarea
            required
            value={form.content}
            onChange={(event) =>
              setForm({ ...form, content: event.target.value })
            }
          />
        </label>

        {form.type === 'LOTTERY' && (
          <fieldset className="wide promotion-option-editor">
            <legend>추첨 선택 항목</legend>
            {form.options.map((option, index) => (
              <div key={index}>
                <label>
                  항목 {index + 1}
                  <input
                    required
                    maxLength="120"
                    value={option}
                    onChange={(event) =>
                      changeOption(index, event.target.value)
                    }
                  />
                </label>
                {form.options.length > 2 && (
                  <button
                    type="button"
                    className="promotion-button secondary"
                    onClick={() =>
                      setForm({
                        ...form,
                        options: form.options.filter(
                          (_, optionIndex) => optionIndex !== index
                        ),
                      })
                    }
                  >
                    삭제
                  </button>
                )}
              </div>
            ))}
            <button
              type="button"
              className="promotion-button secondary"
              onClick={() =>
                setForm({ ...form, options: [...form.options, ''] })
              }
            >
              선택 항목 추가
            </button>
          </fieldset>
        )}

        <label className="promotion-editor-check">
          <input
            type="checkbox"
            checked={form.active}
            onChange={(event) =>
              setForm({ ...form, active: event.target.checked })
            }
          />
          사용자에게 게시물 노출
        </label>
      </div>

      {error && <p className="promotion-status error">⚠ {error}</p>}

      <div className="promotion-editor-actions">
        <button type="button" className="promotion-button secondary" onClick={onCancel}>
          취소
        </button>
        <button type="submit" className="promotion-button primary" disabled={saving}>
          {saving ? '저장 중…' : promotion ? '수정 저장' : '게시물 등록'}
        </button>
      </div>
    </form>
  )
}
