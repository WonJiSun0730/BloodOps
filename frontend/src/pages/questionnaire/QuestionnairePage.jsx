import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'

import {
  getQuestionnaireQuestions,
  submitQuestionnaire,
} from '../../api/apiClient.js'

import './QuestionnairePage.css'

const STEP_SIZE = 4
const NAME_PATTERN = /^(?:[가-힣]{2,20}|[A-Za-z]+(?:[ '-][A-Za-z]+)*)$/

function getResidentIdentity(front, back) {
  if (front.length !== 6 || back.length !== 7) {
    return { error: '주민등록번호 13자리를 모두 입력해주세요.' }
  }

  const genderCode = Number(back[0])
  const century = [1, 2, 5, 6].includes(genderCode)
    ? 1900
    : [3, 4, 7, 8].includes(genderCode) ? 2000 : null

  if (!century) {
    return { error: '주민등록번호 뒤 첫 자리 형식을 확인해주세요.' }
  }

  const year = century + Number(front.slice(0, 2))
  const month = Number(front.slice(2, 4))
  const day = Number(front.slice(4, 6))
  const birthDate = new Date(year, month - 1, day)
  if (
    birthDate.getFullYear() !== year
    || birthDate.getMonth() !== month - 1
    || birthDate.getDate() !== day
  ) {
    return { error: '생년월일 형식을 확인해주세요.' }
  }

  const today = new Date()
  let age = today.getFullYear() - year
  if (
    today.getMonth() < month - 1
    || (today.getMonth() === month - 1 && today.getDate() < day)
  ) {
    age -= 1
  }

  if (age < 16 || age >= 70) {
    return {
      error: '혈액관리법에 의거 만 16세 미만과 만 70세 이상인 분은 헌혈이 불가합니다.',
    }
  }

  return { age, gender: genderCode % 2 === 1 ? 'MALE' : 'FEMALE' }
}


function formatDateTime(value) {
  if (!value) return ''
  return new Intl.DateTimeFormat('ko-KR', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}

function QuestionnairePage() {
  const [questions, setQuestions] = useState([])
  const [answers, setAnswers] = useState({})
  const [answerDetails, setAnswerDetails] = useState({})
  const [identityTouched, setIdentityTouched] = useState(false)
  const [identity, setIdentity] = useState({
    donorName: '', residentFront: '', residentBack: '',
  })
  const [weightKg, setWeightKg] = useState('')
  const [consented, setConsented] = useState(false)
  const [step, setStep] = useState(0)
  const [latest, setLatest] = useState(null)
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    let mounted = true

    getQuestionnaireQuestions()
      .then((questionData) => {
        if (!mounted) return
        setQuestions(questionData)
      })
      .catch((requestError) => {
        if (mounted) setError(requestError.message)
      })
      .finally(() => {
        if (mounted) setLoading(false)
      })

    return () => { mounted = false }
  }, [])

  const totalQuestionSteps = Math.ceil(questions.length / STEP_SIZE)
  const totalSteps = totalQuestionSteps + 2
  const visibleQuestions = useMemo(() => {
    if (step < 1 || step > totalQuestionSteps) return []
    const start = (step - 1) * STEP_SIZE
    return questions.slice(start, start + STEP_SIZE)
  }, [questions, step, totalQuestionSteps])

  const residentIdentity = getResidentIdentity(
    identity.residentFront,
    identity.residentBack
  )
  const nameError = identity.donorName.trim() && !NAME_PATTERN.test(identity.donorName.trim())
    ? '성명은 한글 2~20자 또는 올바른 영문 이름 형식으로 입력해주세요.'
    : ''
  const minimumWeight = residentIdentity.gender === 'MALE' ? 50 : 45
  const weightError = weightKg && residentIdentity.gender && Number(weightKg) < minimumWeight
    ? `${residentIdentity.gender === 'MALE' ? '남성' : '여성'}은 체중 ${minimumWeight}kg 이상이어야 헌혈할 수 있습니다.`
    : ''

  const currentQuestionsAnswered = visibleQuestions.every(
    (question) => answers[question.id]
      && (answers[question.id] !== 'NO' || answerDetails[question.id]?.trim())
  )

  function moveNext() {
    setError('')
    if (step === 0) {
      setIdentityTouched(true)
      if (!identity.donorName.trim() || nameError) {
        setError(nameError || '성명을 입력해주세요.')
        return
      }
      if (residentIdentity.error) {
        setError(residentIdentity.error)
        return
      }
      if (!weightKg || weightError) {
        setError(weightError || '체중을 입력해주세요.')
        return
      }
    }
    if (visibleQuestions.length && !currentQuestionsAnswered) {
      setError('현재 단계의 모든 항목에 답변해주세요.')
      return
    }
    setStep((current) => Math.min(current + 1, totalSteps - 1))
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  async function handleSubmit(event) {
    event.preventDefault()
    if (!consented) {
      setError('개인정보 수집 및 문진 작성에 동의해주세요.')
      return
    }

    try {
      setSubmitting(true)
      setError('')
      const result = await submitQuestionnaire({
        donorName: identity.donorName.trim(),
        residentRegistrationNumber:
          `${identity.residentFront}-${identity.residentBack}`,
        answers,
        answerDetails,
        weightKg: Number(weightKg),
        consented,
      })
      setLatest(result)
      setStep(totalSteps)
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) {
    return <main className="questionnaire-page"><p>전자문진을 불러오는 중입니다.</p></main>
  }

  if (step === totalSteps && latest) {
    const needsReview = latest.status === 'REVIEW_REQUIRED'
    return (
      <main className="questionnaire-page">
        <section className={`questionnaire-result ${needsReview ? 'is-review' : 'is-complete'}`}>
          <span className="result-mark" aria-hidden="true">{needsReview ? '!' : '✓'}</span>
          <p className="questionnaire-eyebrow">문진 제출 완료</p>
          <h1>{needsReview ? '의료진 확인이 필요한 항목이 있습니다' : '전자문진이 정상적으로 제출되었습니다'}</h1>
          <p>{latest.notice}</p>
          <dl className="submission-summary">
            <div><dt>접수번호</dt><dd>{latest.receiptCode}</dd></div>
            <div><dt>작성자</dt><dd>{latest.donorName} ({latest.maskedResidentRegistrationNumber})</dd></div>
            <div><dt>제출 시각</dt><dd>{formatDateTime(latest.submittedAt)}</dd></div>
            <div><dt>유효 시각</dt><dd>{formatDateTime(latest.expiresAt)}</dd></div>
          </dl>
          {needsReview && (
            <div className="review-box">
              <h2>현장에서 확인할 내용</h2>
              <ul>{latest.reviewReasons.map((reason) => <li key={reason}>{reason}</li>)}</ul>
            </div>
          )}
          <div className="questionnaire-actions">
            <Link className="questionnaire-button secondary" to="/">홈으로</Link>
            <button className="questionnaire-button" type="button" onClick={() => {
              setAnswers({}); setAnswerDetails({}); setIdentity({ donorName: '', residentFront: '', residentBack: '' }); setIdentityTouched(false); setWeightKg(''); setConsented(false); setStep(0)
            }}>새 문진 작성</button>
          </div>
        </section>
      </main>
    )
  }

  return (
    <main className="questionnaire-page">
      <header className="questionnaire-header">
        <p className="questionnaire-eyebrow">BLOODOPS PRE-CHECK</p>
        <h1>전자문진</h1>
        <p>방문 전 건강 상태를 미리 확인합니다. 정확한 헌혈 가능 여부는 현장 의료진이 최종 판단합니다.</p>
      </header>

      <div className="questionnaire-progress" aria-label={`전체 ${totalSteps}단계 중 ${Math.min(step + 1, totalSteps)}단계`}>
        <div className="progress-copy"><span>진행 단계</span><strong>{Math.round(((step + 1) / totalSteps) * 100)}%</strong></div>
        <div className="progress-track"><span style={{ width: `${((step + 1) / totalSteps) * 100}%` }} /></div>
      </div>

      <form className="questionnaire-card" onSubmit={handleSubmit}>
        {step === 0 && (
          <section>
            <h2>작성 전 확인</h2>
            <div className="notice-box">
              <strong>안내사항</strong>
              <ul>
                <li><strong>혈액관리법에 의거 만 16세 미만과 만 70세 이상인 분은 헌혈이 불가하오니 양해해 주시기 바랍니다.</strong></li>
                <li>전자문진은 제출일로부터 3일 동안 유효합니다.</li>
                <li>방문 시 사진이 부착된 신분증을 반드시 지참해주세요.</li>
                <li>문진 결과와 관계없이 현장 검진에서 헌혈이 제한될 수 있습니다.</li>
              </ul>
            </div>
            <div className="questionnaire-identity-grid">
              <label>
                <span>성명</span>
                <input aria-invalid={Boolean(identityTouched && nameError)} value={identity.donorName} onChange={(event) => setIdentity({ ...identity, donorName: event.target.value })} autoComplete="name" required />
                {identityTouched && nameError && <small className="field-error" role="alert">{nameError}</small>}
              </label>
              <fieldset>
                <legend>주민등록번호</legend>
                <div>
                  <input inputMode="numeric" autoComplete="off" aria-label="주민등록번호 앞 6자리" value={identity.residentFront} onChange={(event) => setIdentity({ ...identity, residentFront: event.target.value.replace(/\D/g, '').slice(0, 6) })} required />
                  <span>−</span>
                  <input type="password" inputMode="numeric" autoComplete="off" aria-label="주민등록번호 뒤 7자리" value={identity.residentBack} onChange={(event) => setIdentity({ ...identity, residentBack: event.target.value.replace(/\D/g, '').slice(0, 7) })} required />
                </div>
                {identityTouched && residentIdentity.error && <small className="field-error" role="alert">{residentIdentity.error}</small>}
              </fieldset>
            </div>
            <p className="identity-security-note">주민등록번호 원문은 저장하지 않으며 신분 확인용 HMAC 해시와 마스킹 값만 보관합니다.</p>
            <label className="weight-field">
              <span>현재 체중</span>
              <span className="weight-input"><input aria-invalid={Boolean(weightError)} type="number" min={residentIdentity.gender ? minimumWeight : 20} max="300" step="0.1" value={weightKg} onChange={(event) => setWeightKg(event.target.value)} required /> kg</span>
              {weightError && <small className="field-error weight-error" role="alert">{weightError}</small>}
            </label>
          </section>
        )}

        {visibleQuestions.length > 0 && (
          <section>
            <p className="section-label">건강 상태 확인 {step} / {totalQuestionSteps}</p>
            <h2>해당하는 답변을 선택해주세요</h2>
            <div className="question-list">
              {visibleQuestions.map((question, index) => (
                <fieldset className="question-item" key={question.id}>
                  <legend><span>{(step - 1) * STEP_SIZE + index + 1}</span>{question.question}</legend>
                  <p>{question.helpText}</p>
                  <div className="answer-options">
                    <label className={answers[question.id] === 'YES' ? 'selected' : ''}><input type="radio" name={question.id} value="YES" checked={answers[question.id] === 'YES'} onChange={() => {
                      setAnswers({ ...answers, [question.id]: 'YES' })
                      setAnswerDetails((current) => {
                        const next = { ...current }
                        delete next[question.id]
                        return next
                      })
                    }} />예</label>
                    <label className={answers[question.id] === 'NO' ? 'selected' : ''}><input type="radio" name={question.id} value="NO" checked={answers[question.id] === 'NO'} onChange={() => setAnswers({ ...answers, [question.id]: 'NO' })} />아니요</label>
                  </div>
                  {answers[question.id] === 'NO' && (
                    <label className="answer-detail">
                      <span>상세내용을 입력해주세요.</span>
                      <textarea
                        value={answerDetails[question.id] || ''}
                        onChange={(event) => setAnswerDetails({
                          ...answerDetails,
                          [question.id]: event.target.value.slice(0, 1000),
                        })}
                        rows="3"
                        maxLength="1000"
                        required
                      />
                      <small>{(answerDetails[question.id] || '').length} / 1000자</small>
                    </label>
                  )}
                </fieldset>
              ))}
            </div>
          </section>
        )}

        {step === totalSteps - 1 && (
          <section>
            <p className="section-label">최종 확인</p>
            <h2>답변을 확인하고 제출해주세요</h2>
            <div className="answer-review">
              {questions.map((question) => <div key={question.id}><span>{question.question}{answers[question.id] === 'NO' && <small>{answerDetails[question.id]}</small>}</span><strong>{answers[question.id] === 'YES' ? '예' : '아니요'}</strong></div>)}
            </div>
            <label className="consent-check"><input type="checkbox" checked={consented} onChange={(event) => setConsented(event.target.checked)} /><span>전자문진 작성 및 건강정보 수집·이용 안내를 확인했으며 제출에 동의합니다.</span></label>
          </section>
        )}

        {error && <p className="questionnaire-error" role="alert">{error}</p>}

        <div className="questionnaire-actions">
          {step > 0 && <button className="questionnaire-button secondary" type="button" onClick={() => setStep((current) => current - 1)}>이전</button>}
          {step < totalSteps - 1 ? <button className="questionnaire-button" type="button" onClick={moveNext}>다음</button> : <button className="questionnaire-button" type="submit" disabled={submitting}>{submitting ? '제출 중…' : '문진 제출'}</button>}
        </div>
      </form>
    </main>
  )
}

export default QuestionnairePage
