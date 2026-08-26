import { useCallback, useEffect, useState } from 'react'
import {
  createMedicalBloodUnit,
  getMedicalBloodUnits,
  updateMedicalBloodUnit,
} from '../../api/apiClient.js'
import './BloodUnitsManagementPage.css'

const PAGE_SIZE = 20
const BLOOD_TYPES = ['A', 'B', 'O', 'AB']
const PRODUCT_TYPES = ['전혈', '혈장', '혈소판단종', '혈소판다종']
const STATUSES = ['보유중', '예약', '보류', '폐기']
const PRODUCT_EXPIRATION_DAYS = {
  전혈: 35,
  혈장: 365,
  혈소판단종: 5,
  혈소판다종: 5,
}
const emptyForm = {
  donorId: '', bloodType: 'A', rhType: '+', gender: '', birthDate: '',
  unitId: '', productType: '전혈', collectionDate: '', expirationDate: '', status: '보유중',
}

function formatDate(value, includeTime = false) {
  if (!value) return '-'
  const options = includeTime
    ? { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }
    : { year: 'numeric', month: '2-digit', day: '2-digit' }
  return new Intl.DateTimeFormat('ko-KR', options).format(new Date(value))
}

function localDateTimeMax() {
  const now = new Date()
  now.setMinutes(now.getMinutes() - now.getTimezoneOffset())
  return now.toISOString().slice(0, 16)
}

function currentLocalTime() {
  return localDateTimeMax().slice(11, 16)
}

function toLocalDateTimeValue(date) {
  const localDate = new Date(date)
  localDate.setMinutes(localDate.getMinutes() - localDate.getTimezoneOffset())
  return localDate.toISOString().slice(0, 16)
}

function expirationFrom(collectionDate, productType) {
  const days = PRODUCT_EXPIRATION_DAYS[productType]
  if (!collectionDate || !days) return ''
  const expirationDate = new Date(collectionDate)
  expirationDate.setDate(expirationDate.getDate() + days)
  return toLocalDateTimeValue(expirationDate)
}

function statusClass(status) {
  if (['보유중', 'Available'].includes(status)) return 'available'
  if (['예약', 'Reserved'].includes(status)) return 'reserved'
  return 'unavailable'
}

function BloodUnitsManagementPage() {
  const [data, setData] = useState(null)
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [filters, setFilters] = useState({ keyword: '', bloodType: '', productType: '', status: '', collectionFrom: '', collectionTo: '', expirationFrom: '', expirationTo: '' })
  const [form, setForm] = useState(emptyForm)
  const [fieldErrors, setFieldErrors] = useState({})
  const [editingId, setEditingId] = useState(null)
  const [showForm, setShowForm] = useState(false)
  const [saving, setSaving] = useState(false)
  const load = useCallback(async () => {
    try {
      setLoading(true)
      setError('')
      setData(await getMedicalBloodUnits(filters, page, PAGE_SIZE))
    } catch (requestError) {
      setError(requestError.message || '혈액 재고를 불러오지 못했습니다.')
    } finally {
      setLoading(false)
    }
  }, [filters, page])

  useEffect(() => {
    const requestTimer = window.setTimeout(load, 0)
    return () => window.clearTimeout(requestTimer)
  }, [load])

  function changeFilter(event) {
    setFilters((current) => ({ ...current, [event.target.name]: event.target.value }))
    setPage(0)
  }

  function changeForm(event) {
    const { name, value } = event.target
    setForm((current) => {
      const next = { ...current, [name]: value }
      if (name === 'productType' && !editingId) {
        next.expirationDate = expirationFrom(current.collectionDate, value)
      }
      return next
    })
    setFieldErrors((current) => ({ ...current, [name]: '' }))
  }

  function changeCollectionTime(event) {
    const collectionDate = form.collectionDate.slice(0, 10)
    const collectionDateTime = `${collectionDate}T${event.target.value}`
    setForm((current) => ({
      ...current,
      collectionDate: collectionDateTime,
      expirationDate: expirationFrom(collectionDateTime, current.productType),
    }))
    setFieldErrors((current) => ({ ...current, collectionDate: '' }))
  }

  function openCreate() {
    const collectionDate = toLocalDateTimeValue(new Date())
    const productType = emptyForm.productType
    setEditingId(null)
    setForm({
      ...emptyForm,
      productType,
      collectionDate,
      expirationDate: expirationFrom(collectionDate, productType),
    })
    setFieldErrors({})
    setError('')
    setShowForm(true)
  }

  function openEdit(unit) {
    setEditingId(unit.id)
    setForm({ ...emptyForm, ...unit, birthDate: unit.birthDate || '', collectionDate: unit.collectionDate?.slice(0, 16) || '', expirationDate: unit.expirationDate?.slice(0, 16) || '' })
    setFieldErrors({})
    setError('')
    setShowForm(true)
  }

  function validateForm() {
    const errors = {}
    const identifierPattern = /^[A-Za-z0-9-]+$/
    if (!form.donorId.trim()) errors.donorId = '헌혈자 ID를 입력해 주세요.'
    else if (!identifierPattern.test(form.donorId.trim())) errors.donorId = '영문, 숫자, 하이픈(-)만 입력할 수 있습니다.'
    if (!form.unitId.trim()) errors.unitId = 'Unit ID를 입력해 주세요.'
    else if (!identifierPattern.test(form.unitId.trim())) errors.unitId = '영문, 숫자, 하이픈(-)만 입력할 수 있습니다.'
    if (!form.bloodType) errors.bloodType = '혈액형을 선택해 주세요.'
    if (!form.rhType) errors.rhType = 'Rh 유형을 선택해 주세요.'
    if (!form.gender) errors.gender = '성별을 선택해 주세요.'
    if (!form.birthDate) errors.birthDate = '생년월일을 입력해 주세요.'
    else if (new Date(`${form.birthDate}T00:00:00`) >= new Date()) errors.birthDate = '생년월일은 오늘보다 이전 날짜여야 합니다.'
    if (!form.productType) errors.productType = '제제 종류를 선택해 주세요.'
    if (!form.collectionDate) errors.collectionDate = '채혈일을 입력해 주세요.'
    if (!form.expirationDate) errors.expirationDate = '유효기간을 입력해 주세요.'
    if (!form.status) errors.status = '상태를 선택해 주세요.'
    if (form.collectionDate && new Date(form.collectionDate) > new Date()) errors.collectionDate = '채혈일은 현재 시각 이후로 설정할 수 없습니다.'
    if (form.collectionDate && form.expirationDate && new Date(form.expirationDate) <= new Date(form.collectionDate)) errors.expirationDate = '유효기간은 채혈일 이후여야 합니다.'
    setFieldErrors(errors)
    return Object.keys(errors).length === 0
  }

  async function save(event) {
    event.preventDefault()
    if (!validateForm()) return
    try {
      setSaving(true)
      setError('')
      const body = { ...form, donorId: form.donorId.trim(), unitId: form.unitId.trim() }
      if (editingId) await updateMedicalBloodUnit(editingId, { status: form.status })
      else await createMedicalBloodUnit(body)
      setShowForm(false)
      await load()
    } catch (requestError) {
      setFieldErrors(requestError.fieldErrors ?? {})
      setError(requestError.message || '저장에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  const units = data?.content ?? []

  return (
    <main className="blood-units-page">
      <header className="blood-units-header">
        <div><p className="blood-units-eyebrow">MEDICAL · BLOOD UNITS</p><h1>혈액 재고 관리 대시보드</h1></div>
        <button className="blood-unit-button primary" type="button" onClick={openCreate}>혈액 팩 추가</button>
      </header>
      {error && <div className="blood-units-alert" role="alert"><span>{error}</span><button className="blood-unit-button secondary" type="button" onClick={load}>다시 시도</button></div>}
      <section className="blood-units-summary" aria-label="혈액 재고 요약">
        <article><span>검색 결과</span><strong>{data?.totalElements ?? 0} 팩</strong></article>
        <article><span>사용 가능</span><strong className="success">{data?.available ?? 0} 팩</strong></article>
        <article><span>유효기간 7일 이내</span><strong className="warning">{data?.expiringSoon ?? 0} 팩</strong></article>
        <article><span>폐기/보류</span><strong className="danger">{data?.disposedOrHeld ?? 0} 팩</strong></article>
      </section>
      <section className="blood-units-filters" aria-label="혈액 재고 검색 조건">
        <label>검색어<input name="keyword" value={filters.keyword} onChange={changeFilter} placeholder="Unit ID 또는 헌혈자 ID" /></label>
        <label>혈액형<select name="bloodType" value={filters.bloodType} onChange={changeFilter}><option value="">전체</option>{BLOOD_TYPES.map((value) => <option key={value}>{value}</option>)}</select></label>
        <label>제제 종류<select name="productType" value={filters.productType} onChange={changeFilter}><option value="">전체</option>{PRODUCT_TYPES.map((value) => <option key={value}>{value}</option>)}</select></label>
        <label>상태<select name="status" value={filters.status} onChange={changeFilter}><option value="">전체</option>{STATUSES.map((value) => <option key={value}>{value}</option>)}</select></label>
        <label>채혈 시작<input name="collectionFrom" type="date" value={filters.collectionFrom} onChange={changeFilter} /></label>
        <label>채혈 종료<input name="collectionTo" type="date" value={filters.collectionTo} onChange={changeFilter} /></label>
        <label>유효 시작<input name="expirationFrom" type="date" value={filters.expirationFrom} onChange={changeFilter} /></label>
        <label>유효 종료<input name="expirationTo" type="date" value={filters.expirationTo} onChange={changeFilter} /></label>
      </section>
      <section className="blood-units-table-card">
        <div className="blood-units-table-wrap"><table><thead><tr><th>DB ID</th><th>헌혈자 ID</th><th>혈액형</th><th>Rh</th><th>성별</th><th>생년월일</th><th>Unit ID</th><th>제제 종류</th><th>채혈일</th><th>유효기간</th><th>상태</th><th>관리</th></tr></thead>
          <tbody>{loading ? <tr><td colSpan="12" className="empty">조회 중...</td></tr> : units.map((unit) => <tr key={unit.id}><td data-label="DB ID">{unit.id}</td><td data-label="헌혈자 ID">{unit.donorId || '-'}</td><td data-label="혈액형" className="blood-type">{unit.bloodType}</td><td data-label="Rh">{unit.rhType || '-'}</td><td data-label="성별">{unit.gender || '-'}</td><td data-label="생년월일">{formatDate(unit.birthDate)}</td><td data-label="Unit ID" className="unit-id">{unit.unitId}</td><td data-label="제제 종류">{unit.productType || '-'}</td><td data-label="채혈일">{formatDate(unit.collectionDate, true)}</td><td data-label="유효기간">{formatDate(unit.expirationDate, true)}</td><td data-label="상태"><span className={`status-badge ${statusClass(unit.status)}`}>{unit.status || '-'}</span></td><td data-label="관리"><button className="blood-unit-button edit" type="button" onClick={() => openEdit(unit)}>수정</button></td></tr>)}</tbody>
        </table></div>
        {data?.totalPages > 1 && <nav className="blood-units-pagination" aria-label="혈액 재고 페이지 이동"><button className="blood-unit-button secondary" disabled={!page} onClick={() => setPage(page - 1)}>이전</button><span>{page + 1} / {data.totalPages}</span><button className="blood-unit-button secondary" disabled={page + 1 >= data.totalPages} onClick={() => setPage(page + 1)}>다음</button></nav>}
      </section>
      {showForm && <div className="blood-unit-modal" role="dialog" aria-modal="true" aria-labelledby="blood-unit-form-title"><form onSubmit={save} noValidate>
        <h2 id="blood-unit-form-title">{editingId ? '혈액 팩 수정' : '혈액 팩 추가'}</h2>
        {editingId ? (
          <>
            <div className="blood-unit-readonly-details">
              <span><b>Unit ID</b>{form.unitId}</span>
              <span><b>헌혈자 ID</b>{form.donorId || '-'}</span>
              <span><b>혈액형</b>{form.bloodType}{form.rhType}</span>
              <span><b>제제 종류</b>{form.productType}</span>
              <span><b>채혈일</b>{formatDate(form.collectionDate, true)}</span>
              <span><b>유효기간</b>{formatDate(form.expirationDate, true)}</span>
            </div>
            <SelectField label="상태" name="status" value={form.status} onChange={changeForm} options={STATUSES} error={fieldErrors.status} required />
          </>
        ) : (
          <>
            <FormField label="헌혈자 ID" name="donorId" value={form.donorId} onChange={changeForm} error={fieldErrors.donorId} placeholder="예: DNR-2026-00001" pattern="[A-Za-z0-9-]+" required />
            <FormField label="Unit ID" name="unitId" value={form.unitId} onChange={changeForm} error={fieldErrors.unitId} required />
            <SelectField label="혈액형" name="bloodType" value={form.bloodType} onChange={changeForm} options={BLOOD_TYPES} error={fieldErrors.bloodType} required />
            <SelectField label="Rh 유형" name="rhType" value={form.rhType} onChange={changeForm} options={['+', '-']} error={fieldErrors.rhType} required />
            <SelectField label="성별" name="gender" value={form.gender} onChange={changeForm} options={['Male', 'Female']} emptyLabel="성별 선택" error={fieldErrors.gender} required />
            <FormField label="생년월일" name="birthDate" type="date" value={form.birthDate} max={toLocalDateTimeValue(new Date(Date.now() - 86400000)).slice(0, 10)} onChange={changeForm} error={fieldErrors.birthDate} required />
            <SelectField label="제제 종류" name="productType" value={form.productType} onChange={changeForm} options={PRODUCT_TYPES} error={fieldErrors.productType} required />
            <SelectField label="상태" name="status" value={form.status} onChange={changeForm} options={STATUSES} error={fieldErrors.status} required />
            <div className="collection-date-fields">
              <FormField label="채혈 날짜 (오늘 자동 설정)" type="date" value={form.collectionDate.slice(0, 10)} readOnly required />
              <FormField label="채혈 시간" type="time" value={form.collectionDate.slice(11, 16)} max={currentLocalTime()} onChange={changeCollectionTime} error={fieldErrors.collectionDate} required />
            </div>
            <FormField label="유효기간 (제제별 자동 설정)" name="expirationDate" type="datetime-local" value={form.expirationDate} error={fieldErrors.expirationDate} readOnly required />
          </>
        )}
        <div className="blood-unit-modal-actions"><button className="blood-unit-button secondary" type="button" onClick={() => setShowForm(false)} disabled={saving}>취소</button><button className="blood-unit-button primary" type="submit" disabled={saving}>{saving ? '저장 중...' : '저장'}</button></div>
      </form></div>}
    </main>
  )
}

function FormField({ label, error, required, ...inputProps }) {
  return <label className={error ? 'field-invalid' : ''}><span>{label}{required && <b aria-hidden="true"> *</b>}</span><input {...inputProps} aria-invalid={Boolean(error)} />{error && <small className="field-error">{error}</small>}</label>
}

function SelectField({ label, options, emptyLabel, error, required, ...selectProps }) {
  return <label className={error ? 'field-invalid' : ''}><span>{label}{required && <b aria-hidden="true"> *</b>}</span><select {...selectProps} aria-invalid={Boolean(error)}>{emptyLabel && <option value="">{emptyLabel}</option>}{options.map((value) => <option key={value} value={value}>{value}</option>)}</select>{error && <small className="field-error">{error}</small>}</label>
}

export default BloodUnitsManagementPage