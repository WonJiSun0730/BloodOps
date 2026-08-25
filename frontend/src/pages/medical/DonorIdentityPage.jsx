import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { verifyMedicalDonorIdentity } from '../../api/apiClient.js'
import './DonorCheckPage.css'

function DonorIdentityPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ name: '', residentRegistrationNumber: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function submit(event) {
    event.preventDefault()
    try {
      setLoading(true); setError('')
      const result = await verifyMedicalDonorIdentity(form)
      sessionStorage.setItem('verifiedDonorConfirmationId', String(result.confirmationId))
      navigate(`/medical/donor_check/${result.confirmationId}`, {
        state: { identityVerified: true, existingDonor: result.existingDonor },
      })
    } catch (requestError) {
      setError(requestError.message || '신분 확인에 실패했습니다.')
    } finally { setLoading(false) }
  }

  return <main className="donor-check-page">
    <header><p>MEDICAL · IDENTITY VERIFICATION</p><h1>헌혈자 신분 확인</h1></header>
    {error && <div className="donor-check-alert" role="alert">{error}</div>}
    <form onSubmit={submit}>
      <section><h2>신분증 정보 입력</h2>
        <div className="donor-grid two">
          <label>성명<input value={form.name} onChange={(e) => setForm({...form,name:e.target.value})} required /></label>
          <label>주민등록번호<input inputMode="numeric" placeholder="000000-0000000" value={form.residentRegistrationNumber} onChange={(e) => setForm({...form,residentRegistrationNumber:e.target.value})} pattern="[0-9]{6}-?[0-9]{7}" required /></label>
        </div>
        <p className="signature-guide">주민등록번호 원문은 저장하지 않으며, 기존 헌혈자 식별용 해시와 마스킹 값만 저장합니다.</p>
      </section>
      <button className="save-confirmation" type="submit" disabled={loading}>{loading ? '확인 중...' : '신분 확인 후 정보 조회'}</button>
    </form>
  </main>
}

export default DonorIdentityPage
