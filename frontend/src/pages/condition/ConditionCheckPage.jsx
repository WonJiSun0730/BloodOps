import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'

import {
  createConditionCheck,
  verifyConditionCheckAccess,
} from '../../api/apiClient.js'

import './ConditionCheckPage.css'

const CONDITION_OPTIONS = [
  {
    value: 'GOOD',
    label: '좋아요',
    icon: '✓',
    description: '평소와 비슷하고 특별히 불편하지 않아요.',
  },
  {
    value: 'CAUTION',
    label: '조금 불편해요',
    icon: '!',
    description: '어지럽거나 메스꺼움 등 가벼운 증상이 있어요.',
  },
  {
    value: 'BAD',
    label: '많이 불편해요',
    icon: '!!',
    description: '휴식이나 주변의 도움이 필요할 정도예요.',
  },
]

const SYMPTOMS = [
  {
    key: 'dizziness',
    label: '어지러움',
    description: '앉거나 일어날 때 어지러워요.',
  },
  {
    key: 'nausea',
    label: '메스꺼움',
    description: '속이 울렁거리거나 불편해요.',
  },
  {
    key: 'bleedingOrBruising',
    label: '출혈 또는 심한 멍',
    description: '헌혈 부위의 출혈이 계속되거나 멍이 심해요.',
  },
  {
    key: 'armPain',
    label: '헌혈한 팔의 통증',
    description: '헌혈한 팔에 불편한 통증이 있어요.',
  },
  {
    key: 'fainting',
    label: '실신 또는 실신할 것 같은 느낌',
    description: '정신이 아득하거나 쓰러질 것 같아요.',
  },
]

const INITIAL_FORM = {
  overallCondition: '',
  dizziness: false,
  nausea: false,
  bleedingOrBruising: false,
  armPain: false,
  fainting: false,
  otherSymptom: '',
}

function ConditionCheckPage() {
  const { donationHistoryId } = useParams()

  const [accessChecking, setAccessChecking] = useState(true)
  const [accessAllowed, setAccessAllowed] = useState(false)
  const [accessError, setAccessError] = useState('')

  const [form, setForm] = useState(INITIAL_FORM)
  const [result, setResult] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [emergencyModalOpen, setEmergencyModalOpen] =
    useState(false)

  const [centerContact, setCenterContact] = useState({
    name: '',
    phone: '',
  })

  useEffect(() => {
    let cancelled = false

    async function checkAccess() {
      try {
        setAccessChecking(true)
        setAccessError('')

        const accessResponse =
          await verifyConditionCheckAccess(
            donationHistoryId,
          )

        if (!cancelled) {
          setAccessAllowed(true)

          setCenterContact({
            name: accessResponse.centerName || '',
            phone: accessResponse.centerPhone || '',
          })
        }
      } catch (requestError) {
        if (cancelled) return

        setAccessAllowed(false)

        if (requestError?.status === 403) {
          setAccessError(
            '본인의 헌혈 기록만 확인할 수 있습니다.',
          )
        } else if (requestError?.status === 404) {
          setAccessError(
            '헌혈 기록을 찾을 수 없습니다.',
          )
        } else {
          setAccessError(
            requestError?.message
            || '헌혈 기록을 확인하지 못했습니다.',
          )
        }
      } finally {
        if (!cancelled) {
          setAccessChecking(false)
        }
      }
    }

    checkAccess()

    return () => {
      cancelled = true
    }
  }, [donationHistoryId])

  function handleSymptomChange(key) {
    setForm((current) => {
      const nextValue = !current[key]

      if (
        nextValue
        && key === 'fainting'
      ) {
        setEmergencyModalOpen(true)
      }

      return {
        ...current,
        [key]: nextValue,
      }
    })
  }

  async function handleSubmit(event) {
    event.preventDefault()

    if (!form.overallCondition) {
      setError('현재 전반적인 몸 상태를 선택해주세요.')
      return
    }

    try {
      setSubmitting(true)
      setError('')

      const response = await createConditionCheck(
        donationHistoryId,
        {
          ...form,
          otherSymptom: form.otherSymptom.trim(),
        },
      )

      setResult(response)

      window.scrollTo({
        top: 0,
        behavior: 'smooth',
      })
    } catch (requestError) {
      setError(
        requestError.message
        || '컨디션 체크를 저장하지 못했습니다.',
      )
    } finally {
      setSubmitting(false)
    }
  }

  function handleRecheck() {
    setForm(INITIAL_FORM)
    setResult(null)
    setError('')

    window.scrollTo({
      top: 0,
      behavior: 'smooth',
    })
  }

  function closeEmergencyModal() {
    setEmergencyModalOpen(false)
  }

  function callEmergency() {
    setEmergencyModalOpen(false)
    window.location.href = 'tel:119'
  }

  function callDonationCenter() {
    if (!centerContact.phone) {
      return
    }

    const phone = centerContact.phone.replace(/\D/g, '')

    if (!phone) {
      return
    }

    setEmergencyModalOpen(false)
    window.location.href = `tel:${phone}`
  }

  if (accessChecking) {
    return (
      <main className="condition-check-page">
        <section className="condition-check-state-card">
          <div className="condition-check-loader" />

          <p className="condition-check-eyebrow">
            BLOODOPS AFTER CARE
          </p>

          <h1>헌혈 기록 확인 중</h1>

          <p className="condition-check-state-description">
            컨디션 체크를 시작하기 전에
            헌혈 기록을 확인하고 있습니다.
          </p>
        </section>
      </main>
    )
  }

  if (!accessAllowed) {
    return (
      <main className="condition-check-page">
        <section className="condition-check-state-card">
          <div
            className="condition-check-state-icon"
            aria-hidden="true"
          >
            !
          </div>

          <p className="condition-check-eyebrow">
            BLOODOPS AFTER CARE
          </p>

          <h1>접근할 수 없습니다</h1>

          <p className="condition-check-state-description">
            {accessError}
          </p>
        </section>
      </main>
    )
  }

  if (result) {
    return (
      <main className="condition-check-page">
        <div className="condition-check-container">
          <section className="condition-check-result">
            <div className="condition-check-result-header">
              <div
                className="condition-check-result-icon"
                aria-hidden="true"
              >
                ✓
              </div>

              <p className="condition-check-eyebrow">
                BLOODOPS AFTER CARE
              </p>

              <h1>컨디션 체크가 저장되었습니다</h1>

              <p>
                현재 상태에 맞는 안내를 확인해주세요.
                필요할 때 언제든 다시 체크할 수 있습니다.
              </p>
            </div>

            {result.rewardGranted ? (
              <section className="condition-check-reward-card">
                <div>
                  <span className="condition-check-reward-label">
                    FIRST CHECK REWARD
                  </span>

                  <strong>
                    첫 컨디션 체크 참여 보상
                  </strong>

                  <p>
                    이번 헌혈의 첫 컨디션 체크에
                    참여해주셔서 감사합니다.
                  </p>
                </div>

                <div className="condition-check-reward-points">
                  +{result.rewardPoints}
                  <span>P</span>
                </div>
              </section>
            ) : (
              <section className="condition-check-reward-card condition-check-reward-card--completed">
                <div>
                  <span className="condition-check-reward-label">
                    RECHECK
                  </span>

                  <strong>
                    참여 보상 지급 완료
                  </strong>

                  <p>
                    이번 헌혈의 첫 참여 보상은
                    이미 지급되었습니다.
                    컨디션 체크는 횟수 제한 없이 다시 할 수 있습니다.
                  </p>
                </div>

                <div
                  className="condition-check-reward-check"
                  aria-hidden="true"
                >
                  ✓
                </div>
              </section>
            )}

            <section className="condition-check-guidance-card">
              <div className="condition-check-section-heading">
                <span className="condition-check-step">
                  AFTER CARE
                </span>

                <div>
                  <h2>지금 이렇게 해주세요</h2>
                  <p>
                    입력한 상태를 바탕으로 안내해드려요.
                  </p>
                </div>
              </div>

              <ul className="condition-check-guidance-list">
                {result.guidance?.map((guidance) => (
                  <li key={guidance}>
                    <span aria-hidden="true">✓</span>
                    <p>{guidance}</p>
                  </li>
                ))}
              </ul>
            </section>

            <div className="condition-check-medical-notice">
              <span aria-hidden="true">i</span>

              <p>
                이 안내는 의학적 진단을 대신하지 않습니다.
                증상이 심하거나 계속되면 의료기관에 문의해주세요.
              </p>
            </div>

            <button
              className="condition-check-secondary-button"
              type="button"
              onClick={handleRecheck}
            >
              다시 컨디션 체크하기
            </button>
          </section>
        </div>
      </main>
    )
  }

  return (
    <main className="condition-check-page">
      <div className="condition-check-container">
        <header className="condition-check-hero">
          <div className="condition-check-hero-content">
            <p className="condition-check-eyebrow">
              BLOODOPS AFTER CARE
            </p>

            <h1>
              헌혈 후
              <strong> 컨디션 체크</strong>
            </h1>

            <p className="condition-check-hero-description">
              헌혈하시느라 수고하셨습니다.
              <br />
              지금 몸 상태는 어떠신가요?
            </p>
          </div>

          <div className="condition-check-hero-side">
            <span>AFTER CARE</span>
            <strong>01</strong>
            <p>
              간단한 체크로
              <br />
              헌혈 후 상태를 확인해보세요.
            </p>
          </div>
        </header>

        <form
          className="condition-check-form"
          onSubmit={handleSubmit}
        >
          <section className="condition-check-section">
            <div className="condition-check-section-heading">
              <span className="condition-check-step">
                STEP 01
              </span>

              <div>
                <h2>전반적인 몸 상태</h2>
                <p>
                  지금 느끼는 상태와 가장 가까운 항목을
                  하나 선택해주세요.
                </p>
              </div>
            </div>

            <div className="condition-check-condition-grid">
              {CONDITION_OPTIONS.map((option) => {
                const selected =
                  form.overallCondition === option.value

                return (
                  <label
                    className={[
                      'condition-check-option',
                      `condition-check-option--${option.value.toLowerCase()}`,
                      selected ? 'is-selected' : '',
                    ].join(' ')}
                    key={option.value}
                  >
                    <input
                      type="radio"
                      name="overallCondition"
                      value={option.value}
                      checked={selected}
                      onChange={(event) => {
                        const value = event.target.value

                        setForm((current) => ({
                          ...current,
                          overallCondition: value,
                        }))

                        if (value === 'BAD') {
                          setEmergencyModalOpen(true)
                        }
                      }}
                    />

                    <span
                      className="condition-check-option-icon"
                      aria-hidden="true"
                    >
                      {option.icon}
                    </span>

                    <span className="condition-check-option-copy">
                      <strong>{option.label}</strong>
                      <small>{option.description}</small>
                    </span>

                    <span
                      className="condition-check-option-check"
                      aria-hidden="true"
                    >
                      ✓
                    </span>
                  </label>
                )
              })}
            </div>
          </section>

          <section className="condition-check-section">
            <div className="condition-check-section-heading">
              <span className="condition-check-step">
                STEP 02
              </span>

              <div>
                <h2>해당되는 증상이 있나요?</h2>
                <p>
                  현재 느끼고 있는 증상을 모두 선택해주세요.
                  해당되는 항목이 없다면 선택하지 않아도 됩니다.
                </p>
              </div>
            </div>

            <div className="condition-check-symptom-grid">
              {SYMPTOMS.map((symptom) => {
                const selected = form[symptom.key]

                return (
                  <label
                    className={[
                      'condition-check-symptom',
                      selected ? 'is-selected' : '',
                    ].join(' ')}
                    key={symptom.key}
                  >
                    <input
                      type="checkbox"
                      checked={selected}
                      onChange={() =>
                        handleSymptomChange(symptom.key)
                      }
                    />

                    <span
                      className="condition-check-checkbox"
                      aria-hidden="true"
                    >
                      ✓
                    </span>

                    <span className="condition-check-symptom-copy">
                      <strong>{symptom.label}</strong>
                      <small>{symptom.description}</small>
                    </span>
                  </label>
                )
              })}
            </div>
          </section>

          <section className="condition-check-section">
            <div className="condition-check-section-heading">
              <span className="condition-check-step">
                STEP 03
              </span>

              <div>
                <h2>그 밖에 불편한 점</h2>
                <p>
                  선택 항목에 없는 증상이나 전달하고 싶은
                  내용이 있다면 작성해주세요.
                </p>
              </div>
            </div>

            <div className="condition-check-textarea-wrap">
              <textarea
                maxLength={500}
                rows={5}
                value={form.otherSymptom}
                placeholder="예: 헌혈 후 평소보다 피곤하고 팔에 약간의 뻐근함이 있어요."
                onChange={(event) =>
                  setForm((current) => ({
                    ...current,
                    otherSymptom: event.target.value,
                  }))
                }
              />

              <span className="condition-check-character-count">
                {form.otherSymptom.length} / 500
              </span>
            </div>
          </section>

          <aside className="condition-check-safety-notice">
            <div
              className="condition-check-safety-icon"
              aria-hidden="true"
            >
              !
            </div>

            <div>
              <strong>
                심한 증상이 있다면 체크 결과를 기다리지 마세요.
              </strong>

              <p>
                응급상황이거나 증상이 심한 경우에는
                즉시 주변에 도움을 요청하거나
                의료기관에 문의해주세요.
              </p>
            </div>
          </aside>

          {error && (
            <p
              className="condition-check-error"
              role="alert"
            >
              {error}
            </p>
          )}

          <div className="condition-check-submit-area">
            <p>
              입력한 내용은 헌혈 후 상태 확인을 위해
              저장됩니다.
            </p>

            <button
              className="condition-check-submit-button"
              type="submit"
              disabled={submitting}
            >
              {submitting
                ? '저장 중...'
                : '컨디션 체크 완료'}
              {!submitting && (
                <span aria-hidden="true">→</span>
              )}
            </button>
          </div>
        </form>

        {emergencyModalOpen && (
          <div
            className="condition-emergency-backdrop"
            role="presentation"
            onMouseDown={(event) => {
              if (event.target === event.currentTarget) {
                closeEmergencyModal()
              }
            }}
          >
            <section
              className="condition-emergency-modal"
              role="dialog"
              aria-modal="true"
              aria-labelledby="condition-emergency-title"
            >
              <div
                className="condition-emergency-icon"
                aria-hidden="true"
              >
                !
              </div>

              <p className="condition-emergency-eyebrow">
                SAFETY CHECK
              </p>

              <h2 id="condition-emergency-title">
                지금 바로 도움이 필요하신가요?
              </h2>

              <p className="condition-emergency-description">
                증상이 심하거나 혼자 대처하기 어렵다면
                체크를 계속하기보다 먼저 도움을 요청해주세요.
              </p>

              <div className="condition-emergency-actions">
                <button
                  className="condition-emergency-call"
                  type="button"
                  onClick={callEmergency}
                >
                  <span className="condition-emergency-action-icon">
                    119
                  </span>

                  <span>
                    <strong>119 긴급전화</strong>
                    <small>
                      응급상황이라면 즉시 도움을 요청하세요.
                    </small>
                  </span>

                  <span aria-hidden="true">→</span>
                </button>

                <button
                  className="condition-emergency-center"
                  type="button"
                  disabled={!centerContact.phone}
                  onClick={callDonationCenter}
                >
                  <span className="condition-emergency-action-icon">
                    ☎
                  </span>

                  <span>
                    <strong>
                      {centerContact.name
                        ? `${centerContact.name}에 문의`
                        : '헌혈센터에 문의'}
                    </strong>

                    <small>
                      {centerContact.phone
                        ? centerContact.phone
                        : '등록된 센터 연락처가 없습니다.'}
                    </small>
                  </span>

                  <span aria-hidden="true">→</span>
                </button>
              </div>

              <button
                className="condition-emergency-continue"
                type="button"
                onClick={closeEmergencyModal}
              >
                괜찮아요, 체크를 계속할게요
              </button>

              <p className="condition-emergency-footnote">
                전화 버튼을 선택해도 BloodOps에서
                자동으로 통화를 연결하지 않습니다.
              </p>
            </section>
          </div>
        )}

      </div>
    </main>
  )
}

export default ConditionCheckPage