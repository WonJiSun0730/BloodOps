import { useEffect, useRef, useState } from 'react'
import { Link, Navigate, useParams } from 'react-router-dom'
import {
  getMedicalDonorConfirmation,
  getMedicalDonorQuestionnaire,
  updateMedicalDonorConfirmation,
} from '../../api/apiClient.js'
import './DonorCheckPage.css'

const initialForm = {
  donorName: '',
  donorBirth: '',
  height: '',
  weight: '',
  bloodType: 'A',
  phone: '',
  email: '',
  address: '',
  signatureData: '',
}

const questionnaireLabels = {
  feeling_well: '오늘 몸 상태가 양호합니까?',
  recent_donation: '최근 헌혈 후 필요한 헌혈 간격이 지났습니까?',
  recent_treatment: '최근 수술·입원·수혈 또는 치료를 받은 적이 없습니까?',
  medication: '현재 복용 중이거나 최근 복용한 약이 없습니까?',
  vaccination: '최근 예방접종을 받은 적이 없습니까?',
  infection: '최근 감염병 진단 또는 감염 의심 증상이 없습니까?',
  dental_treatment: '최근 치과 치료를 받은 적이 없습니까?',
  pregnancy: '현재 임신 중이거나 출산·유산 후 6개월 이내가 아닙니까?',
  overseas_travel: '최근 4주 이내 해외여행을 하지 않았습니까?',
  malaria_area: '최근 말라리아 관련 제한 지역에 거주하거나 숙박한 적이 없습니까?',
  test_result: '과거 헌혈검사에서 비정상 결과를 통보받은 적이 없습니까?',
  risk_exposure: '최근 혈액으로 전파될 수 있는 감염 위험에 노출된 적이 없습니까?',
}

function DonorCheckPage() {
  const { confirmationId } = useParams()
  const identityVerified = sessionStorage.getItem('verifiedDonorConfirmationId') === String(confirmationId)
  const canvasRef = useRef(null)
  const drawingRef = useRef(false)
  const [form, setForm] = useState(initialForm)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [questionnaire, setQuestionnaire] = useState(null)

  useEffect(() => {
    let mounted = true

    async function loadConfirmation() {
      if (!identityVerified) { setLoading(false); return }
      try {
        setLoading(true)
        setError('')
        const [data, questionnaireData] = await Promise.all([
          getMedicalDonorConfirmation(confirmationId),
          getMedicalDonorQuestionnaire(confirmationId),
        ])

        if (!mounted) return

        setForm({
          donorName: data.name ?? '',
          donorBirth: data.birthDate ?? '',
          height: data.height ?? '',
          weight: data.weight ?? '',
          bloodType: data.bloodType ?? 'A',
          phone: data.phone ?? '',
          email: data.email ?? '',
          address: data.address ?? '',
          signatureData: data.signatureData ?? '',
        })
        setQuestionnaire(questionnaireData)
      } catch (requestError) {
        if (mounted) {
          setError(requestError.message || '헌혈자 정보를 불러오지 못했습니다.')
        }
      } finally {
        if (mounted) setLoading(false)
      }
    }

    loadConfirmation()
    return () => { mounted = false }
  }, [confirmationId, identityVerified])

  useEffect(() => {
    const canvas = canvasRef.current
    if (!canvas || loading) return undefined

    const context = canvas.getContext('2d')
    const ratio = Math.max(window.devicePixelRatio || 1, 1)
    const width = canvas.clientWidth
    const height = 160

    canvas.width = width * ratio
    canvas.height = height * ratio
    context.setTransform(ratio, 0, 0, ratio, 0, 0)
    context.strokeStyle = '#111827'
    context.lineWidth = 2
    context.lineCap = 'round'

    if (form.signatureData) {
      const image = new Image()
      image.onload = () => context.drawImage(image, 0, 0, width, height)
      image.src = form.signatureData
    }

    function position(event) {
      const rect = canvas.getBoundingClientRect()
      const point = event.touches?.[0] ?? event
      return {
        x: point.clientX - rect.left,
        y: point.clientY - rect.top,
      }
    }

    function start(event) {
      event.preventDefault()
      drawingRef.current = true
      const point = position(event)
      context.beginPath()
      context.moveTo(point.x, point.y)
    }

    function move(event) {
      if (!drawingRef.current) return
      event.preventDefault()
      const point = position(event)
      context.lineTo(point.x, point.y)
      context.stroke()
    }

    function stop() {
      if (!drawingRef.current) return
      drawingRef.current = false
      setForm((current) => ({
        ...current,
        signatureData: canvas.toDataURL('image/png'),
      }))
    }

    canvas.addEventListener('pointerdown', start)
    canvas.addEventListener('pointermove', move)
    window.addEventListener('pointerup', stop)

    return () => {
      canvas.removeEventListener('pointerdown', start)
      canvas.removeEventListener('pointermove', move)
      window.removeEventListener('pointerup', stop)
    }
  }, [loading, form.signatureData])

  function changeField(event) {
    const { name, value } = event.target
    setForm((current) => ({ ...current, [name]: value }))
  }

  function clearSignature() {
    const canvas = canvasRef.current
    const context = canvas?.getContext('2d')
    if (canvas && context) {
      context.clearRect(0, 0, canvas.width, canvas.height)
    }
    setForm((current) => ({ ...current, signatureData: '' }))
  }

  async function submit(event) {
    event.preventDefault()

    if (!form.signatureData) {
      setError('서명을 완료해 주세요.')
      return
    }

    try {
      setSaving(true)
      setError('')
      const response = await updateMedicalDonorConfirmation(
        confirmationId,
        {
          ...form,
          height: form.height === '' ? null : Number(form.height),
          weight: form.weight === '' ? null : Number(form.weight),
        }
      )
      window.alert(response.message || '저장이 완료되었습니다.')
    } catch (requestError) {
      setError(requestError.message || '저장 중 오류가 발생했습니다.')
    } finally {
      setSaving(false)
    }
  }

  if (!identityVerified) return <Navigate to="/medical/donor_identity" replace />

  if (loading) {
    return <main className="donor-check-state">헌혈자 정보를 불러오는 중입니다...</main>
  }

  return (
    <main className="donor-check-page">
      <header>
        <Link className="donor-check-back-link" to="/medical/donor_identity">
          <span aria-hidden="true">←</span>
          헌혈자 신분 확인으로 돌아가기
        </Link>
        <p>MEDICAL · DONOR CONFIRMATION</p>
        <h1>헌혈자 정보 및 전자문진 확인</h1>
      </header>

      {error && <div className="donor-check-alert" role="alert">{error}</div>}

      <form onSubmit={submit}>
        <section>
          <h2>1. 헌혈자 정보 확인 및 변경</h2>
          <div className="donor-grid two">
            <label>성명<input name="donorName" value={form.donorName} onChange={changeField} required /></label>
            <label>생년월일<input name="donorBirth" type="date" value={form.donorBirth} onChange={changeField} required /></label>
          </div>
          <div className="donor-grid three">
            <label>신장 (cm)<input name="height" type="number" step="0.1" value={form.height} onChange={changeField} /></label>
            <label>체중 (kg)<input name="weight" type="number" step="0.1" value={form.weight} onChange={changeField} /></label>
            <label>혈액형
              <select name="bloodType" value={form.bloodType} onChange={changeField}>
                <option value="A">A형</option>
                <option value="B">B형</option>
                <option value="O">O형</option>
                <option value="AB">AB형</option>
              </select>
            </label>
          </div>
          <div className="donor-grid two">
            <label>연락처<input name="phone" type="tel" value={form.phone} onChange={changeField} /></label>
            <label>이메일<input name="email" type="email" value={form.email} onChange={changeField} /></label>
          </div>
          <label>주소<input name="address" value={form.address} onChange={changeField} /></label>
        </section>

        <section>
          <h2>2. 작성한 전자 문진 사항</h2>
          {questionnaire ? <div className="questionnaire-wrap">
            <p className="signature-guide">
              접수번호 {questionnaire.receiptCode} · {questionnaire.donorName} ·
              {questionnaire.identityVerified ? ' 신분 확인 완료' : ' 신분 확인 전'}
            </p>
            <table>
              <thead><tr><th>문진 항목</th><th>응답</th></tr></thead>
              <tbody>
                {Object.entries(questionnaire.answers).map(([questionId, answer]) => (
                  <tr key={questionId}><td>{questionnaireLabels[questionId] ?? questionId}</td><td className="answer">{answer === 'YES' ? '예' : '아니요'}</td></tr>
                ))}
              </tbody>
            </table>
          </div> : <p className="signature-guide">유효한 전자문진 제출 내역이 없습니다.</p>}
        </section>

        <section>
          <h2>3. 헌혈자 서명</h2>
          <p className="signature-guide">작성된 정보와 전자 문진 내용이 사실과 다름없음을 확인하며 헌혈 진행에 동의합니다.</p>
          <div className="signature-box"><canvas ref={canvasRef} /></div>
          <button className="clear-signature" type="button" onClick={clearSignature}>서명 초기화</button>
        </section>

        <button className="save-confirmation" type="submit" disabled={saving}>
          {saving ? '저장 중...' : '정보 확인 및 DB 저장'}
        </button>
      </form>
    </main>
  )
}

export default DonorCheckPage
