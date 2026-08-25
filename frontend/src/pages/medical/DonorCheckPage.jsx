import { useEffect, useRef, useState } from 'react'
import { Navigate, useParams } from 'react-router-dom'
import {
  getMedicalDonorConfirmation,
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

const questionnaire = [
  ['오늘 몸 상태가 헌혈하기에 양호합니까?', '예'],
  ['최근 3일 이내에 약물을 복용하거나 주사를 맞은 적이 있습니까?', '아니오'],
  ['최근 1개월 이내에 해외를 방문한 적이 있습니까?', '아니오'],
  ['최근 1년 이내에 문신, 피어싱, 사혈침 시술을 받은 적이 있습니까?', '아니오'],
]

function DonorCheckPage() {
  const { confirmationId } = useParams()
  const identityVerified = sessionStorage.getItem('verifiedDonorConfirmationId') === String(confirmationId)
  const canvasRef = useRef(null)
  const drawingRef = useRef(false)
  const [form, setForm] = useState(initialForm)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    let mounted = true

    async function loadConfirmation() {
      if (!identityVerified) { setLoading(false); return }
      try {
        setLoading(true)
        setError('')
        const data = await getMedicalDonorConfirmation(confirmationId)

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
  }, [loading])

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
          <div className="questionnaire-wrap">
            <table>
              <thead><tr><th>문진 항목</th><th>응답</th></tr></thead>
              <tbody>
                {questionnaire.map(([question, answer]) => (
                  <tr key={question}><td>{question}</td><td className="answer">{answer}</td></tr>
                ))}
              </tbody>
            </table>
          </div>
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
