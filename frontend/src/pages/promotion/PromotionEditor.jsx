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
  options: [''],
}

function formFromPromotion(promotion) {
  if (!promotion) return emptyForm
  return {
    category: promotion.category,
    type: promotion.type,
    regionName: promotion.regionName || '',
    title: promotion.title || '',
    summary: promotion.summary || '',
    content: promotion.content || '',
    startDate: promotion.startDate,
    endDate: promotion.endDate,
    donationEligibilityStartDate:
      promotion.donationEligibilityStartDate || promotion.startDate,
    active: promotion.active,
    options: promotion.options.length
      ? promotion.options.map((option) => option.label)
      : [''],
  }
}

function FieldError({ id, message }) {
  if (!message) return null
  return (
    <small id={id} className="promotion-field-error" role="alert">
      <span aria-hidden="true">⚠</span>
      {message}
    </small>
  )
}

export default function PromotionEditor({
  promotion = null,
  onSaved,
  onCancel,
}) {
  const [form, setForm] = useState(() => formFromPromotion(promotion))
  const [imageFile, setImageFile] = useState(null)
  const [fieldErrors, setFieldErrors] = useState({})
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  const updateField = (field, value) => {
    setForm((current) => ({ ...current, [field]: value }))
    setFieldErrors((current) => {
      if (!current[field]) return current
      const next = { ...current }
      delete next[field]
      return next
    })
  }

  const changeOption = (index, value) => {
    setForm((current) => ({
      ...current,
      options: current.options.map((option, optionIndex) =>
        optionIndex === index ? value : option
      ),
    }))
    setFieldErrors((current) => {
      if (!current.options) return current
      const next = { ...current }
      delete next.options
      return next
    })
  }

  const validate = () => {
    const errors = {}

    if (!form.title.trim()) errors.title = '제목을 입력해주세요.'
    if (form.category === 'REGIONAL' && !form.regionName) {
      errors.regionName = '지역을 선택해주세요.'
    }
    if (!form.startDate) errors.startDate = '게시 시작일을 선택해주세요.'
    if (!form.endDate) errors.endDate = '게시 종료일을 선택해주세요.'
    if (form.startDate && form.endDate && form.endDate < form.startDate) {
      errors.endDate = '게시 종료일은 시작일과 같거나 이후여야 합니다.'
    }

    if (form.type === 'LOTTERY') {
      if (!form.donationEligibilityStartDate) {
        errors.donationEligibilityStartDate =
          '헌혈 실적 인정 시작일을 선택해주세요.'
      } else if (
        form.endDate &&
        form.donationEligibilityStartDate > form.endDate
      ) {
        errors.donationEligibilityStartDate =
          '헌혈 실적 인정 시작일은 게시 종료일과 같거나 이전이어야 합니다.'
      }

      if (!form.options.some((option) => option.trim())) {
        errors.options = '추첨 선택 항목을 1개 이상 입력해주세요.'
      }
    }

    if (imageFile) {
      const allowedTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/gif']
      if (!allowedTypes.includes(imageFile.type)) {
        errors.image = 'JPG, PNG, WebP, GIF 이미지 파일만 첨부할 수 있습니다.'
      } else if (imageFile.size > 5 * 1024 * 1024) {
        errors.image = '이미지 파일은 5MB 이하로 첨부해주세요.'
      }
    }

    setFieldErrors(errors)
    return Object.keys(errors).length === 0
  }

  const submit = async (event) => {
    event.preventDefault()
    setError('')
    if (!validate()) return

    setSaving(true)
    try {
      const csrf = await getCsrfToken()
      const payload = {
        ...form,
        summary: form.summary.trim() || null,
        content: form.content.trim() || null,
        regionName: form.category === 'REGIONAL' ? form.regionName : null,
        donationEligibilityStartDate:
          form.type === 'LOTTERY'
            ? form.donationEligibilityStartDate
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
      const serverFieldErrors = requestError.fieldErrors || {}
      setFieldErrors(serverFieldErrors)
      if (Object.keys(serverFieldErrors).length === 0) {
        setError(requestError.message)
      }
    } finally {
      setSaving(false)
    }
  }

  return (
    <form className="promotion-editor" onSubmit={submit} noValidate>
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
            onChange={(event) => updateField('category', event.target.value)}
          >
            <option value="NATIONAL">전국 프로모션</option>
            <option value="REGIONAL">지역별 프로모션</option>
          </select>
        </label>

        <label>
          게시물 유형
          <select
            value={form.type}
            onChange={(event) => updateField('type', event.target.value)}
          >
            <option value="INFORMATION">안내형</option>
            <option value="LOTTERY">추첨형</option>
          </select>
        </label>

        {form.category === 'REGIONAL' && (
          <label>
            지역
            <select
              value={form.regionName}
              aria-invalid={Boolean(fieldErrors.regionName)}
              aria-describedby={fieldErrors.regionName ? 'region-error' : undefined}
              onChange={(event) => updateField('regionName', event.target.value)}
            >
              <option value="">지역을 선택하세요</option>
              {regions.map(([value, label]) => (
                <option value={value} key={value}>{label}</option>
              ))}
            </select>
            <FieldError id="region-error" message={fieldErrors.regionName} />
          </label>
        )}

        <label className="wide">
          제목
          <input
            maxLength="150"
            value={form.title}
            aria-invalid={Boolean(fieldErrors.title)}
            aria-describedby={fieldErrors.title ? 'title-error' : undefined}
            onChange={(event) => updateField('title', event.target.value)}
          />
          <FieldError id="title-error" message={fieldErrors.title} />
        </label>

        <label className="wide">
          요약 (선택)
          <input
            maxLength="300"
            value={form.summary}
            aria-invalid={Boolean(fieldErrors.summary)}
            aria-describedby={fieldErrors.summary ? 'summary-error' : undefined}
            onChange={(event) => updateField('summary', event.target.value)}
          />
          <FieldError id="summary-error" message={fieldErrors.summary} />
        </label>

        <label>
          게시 시작일
          <input
            type="date"
            value={form.startDate}
            aria-invalid={Boolean(fieldErrors.startDate)}
            aria-describedby={fieldErrors.startDate ? 'start-date-error' : undefined}
            onChange={(event) => updateField('startDate', event.target.value)}
          />
          <FieldError id="start-date-error" message={fieldErrors.startDate} />
        </label>

        <label>
          게시 종료일
          <input
            type="date"
            value={form.endDate}
            aria-invalid={Boolean(fieldErrors.endDate)}
            aria-describedby={fieldErrors.endDate ? 'end-date-error' : undefined}
            onChange={(event) => updateField('endDate', event.target.value)}
          />
          <FieldError id="end-date-error" message={fieldErrors.endDate} />
        </label>

        {form.type === 'LOTTERY' && (
          <label className="wide">
            헌혈 실적 인정 시작일
            <input
              type="date"
              max={form.endDate || undefined}
              value={form.donationEligibilityStartDate}
              aria-invalid={Boolean(fieldErrors.donationEligibilityStartDate)}
              aria-describedby={
                fieldErrors.donationEligibilityStartDate
                  ? 'eligibility-date-error'
                  : 'eligibility-date-help'
              }
              onChange={(event) =>
                updateField('donationEligibilityStartDate', event.target.value)
              }
            />
            <small id="eligibility-date-help">
              이 날짜부터 게시 종료일까지 완료된 헌혈 기록을 인정합니다.
            </small>
            <FieldError
              id="eligibility-date-error"
              message={fieldErrors.donationEligibilityStartDate}
            />
          </label>
        )}

        <label className="wide">
          게시물 이미지
          <input
            type="file"
            accept="image/jpeg,image/png,image/webp,image/gif"
            aria-invalid={Boolean(fieldErrors.image)}
            aria-describedby={fieldErrors.image ? 'image-error' : 'image-help'}
            onChange={(event) => {
              setImageFile(event.target.files?.[0] || null)
              setFieldErrors((current) => {
                const next = { ...current }
                delete next.image
                return next
              })
            }}
          />
          <small id="image-help">JPG, PNG, WebP, GIF · 최대 5MB</small>
          <FieldError id="image-error" message={fieldErrors.image} />
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
          본문 (선택)
          <textarea
            value={form.content}
            onChange={(event) => updateField('content', event.target.value)}
          />
        </label>

        {form.type === 'LOTTERY' && (
          <fieldset
            className="wide promotion-option-editor"
            aria-invalid={Boolean(fieldErrors.options)}
            aria-describedby={fieldErrors.options ? 'options-error' : undefined}
          >
            <legend>추첨 선택 항목 (1개 이상)</legend>
            {form.options.map((option, index) => (
              <div key={index}>
                <label>
                  항목 {index + 1}
                  <input
                    maxLength="120"
                    value={option}
                    onChange={(event) => changeOption(index, event.target.value)}
                  />
                </label>
                {form.options.length > 1 && (
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
            <FieldError id="options-error" message={fieldErrors.options} />
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
            onChange={(event) => updateField('active', event.target.checked)}
          />
          사용자에게 게시물 노출
        </label>
      </div>

      {error && <p className="promotion-status error" role="alert">⚠ {error}</p>}

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
