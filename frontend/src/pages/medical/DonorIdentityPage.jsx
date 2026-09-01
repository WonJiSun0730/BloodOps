import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getCurrentUser, verifyMedicalDonorIdentity } from '../../api/apiClient.js'
import './DonorCheckPage.css'

const onlyDigits = (value, length) => value.replace(/\D/g, '').slice(0, length)

function DonorIdentityPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ name: '', residentFront: '', residentBack: '' })
  const [fieldErrors, setFieldErrors] = useState({})
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  function validate() {
    const errors = {}
    if (!form.name.trim()) errors.name = '성명을 입력해 주세요.'
    if (form.residentFront.length !== 6) errors.residentFront = '앞 6자리를 입력해 주세요.'
    if (form.residentBack.length !== 7) errors.residentBack = '뒤 7자리를 입력해 주세요.'
    setFieldErrors(errors)
    return Object.keys(errors).length === 0
  }

  async function submit(event) {
    event.preventDefault()
    if (!validate()) return

    try {
      setLoading(true)
      setError('')
      const result = await verifyMedicalDonorIdentity({
        name: form.name.trim(),
        residentRegistrationNumber: `${form.residentFront}-${form.residentBack}`,
      })
      const currentUser = await getCurrentUser()
      sessionStorage.setItem('verifiedMedicalDonor', JSON.stringify({
        confirmationId: String(result.confirmationId),
        owner: currentUser.identifier,
      }))
      navigate(`/medical/donor_check/${result.confirmationId}`, {
        state: { identityVerified: true, existingDonor: result.existingDonor },
      })
    } catch (requestError) {
      setFieldErrors(requestError.fieldErrors ?? {})
      setError(requestError.message || '신분 확인에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="donor-check-page">
      <header>
        <p>MEDICAL · IDENTITY VERIFICATION</p>
        <h1>헌혈자 신분 확인</h1>
      </header>
      {error && <div className="donor-check-alert" role="alert">{error}</div>}
      <form onSubmit={submit} noValidate>
        <section>
          <h2>신분증 정보 입력</h2>
          <div className="donor-grid two">
            <label className={fieldErrors.name ? 'field-invalid' : ''}>
              성명
              <input
                value={form.name}
                onChange={(event) => setForm({ ...form, name: event.target.value })}
                aria-invalid={Boolean(fieldErrors.name)}
              />
              {fieldErrors.name && <span className="field-error">{fieldErrors.name}</span>}
            </label>
            <fieldset className="resident-number-field">
              <legend>주민등록번호</legend>
              <div className="resident-number-inputs">
                <input inputMode="numeric" autoComplete="off" aria-label="주민등록번호 앞 6자리" placeholder="앞 6자리" value={form.residentFront} onChange={(event) => setForm({ ...form, residentFront: onlyDigits(event.target.value, 6) })} aria-invalid={Boolean(fieldErrors.residentFront)} />
                <span aria-hidden="true">−</span>
                <input type="password" inputMode="numeric" autoComplete="off" aria-label="주민등록번호 뒤 7자리" placeholder="뒤 7자리" value={form.residentBack} onChange={(event) => setForm({ ...form, residentBack: onlyDigits(event.target.value, 7) })} aria-invalid={Boolean(fieldErrors.residentBack)} />
              </div>
              {(fieldErrors.residentFront || fieldErrors.residentBack) && <span className="field-error">{fieldErrors.residentFront || fieldErrors.residentBack}</span>}
            </fieldset>
          </div>
          <p className="signature-guide">주민등록번호 원문은 저장하지 않으며, 기존 헌혈자 식별용 해시와 마스킹 값만 저장합니다.</p>
        </section>
        <button className="save-confirmation" type="submit" disabled={loading}>{loading ? '확인 중...' : '신분 확인 후 정보 조회'}</button>
      </form>
    </main>
  )
}

export default DonorIdentityPage
