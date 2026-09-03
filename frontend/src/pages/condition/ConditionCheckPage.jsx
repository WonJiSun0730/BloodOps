import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'

import {
  createConditionCheck,
  verifyConditionCheckAccess,
} from '../../api/apiClient.js'

const CONDITION_OPTIONS = [
  {
    value: 'GOOD',
    label: '좋아요',
    description: '평소와 비슷하고 특별히 불편하지 않아요.',
  },
  {
    value: 'CAUTION',
    label: '조금 불편해요',
    description: '어지럽거나 메스꺼움 등 가벼운 증상이 있어요.',
  },
  {
    value: 'BAD',
    label: '많이 불편해요',
    description: '휴식이나 주변의 도움이 필요할 정도예요.',
  },
]

const SYMPTOMS = [
  {
    key: 'dizziness',
    label: '어지러움',
  },
  {
    key: 'nausea',
    label: '메스꺼움',
  },
  {
    key: 'bleedingOrBruising',
    label: '출혈 또는 심한 멍',
  },
  {
    key: 'armPain',
    label: '헌혈한 팔의 통증',
  },
  {
    key: 'fainting',
    label: '실신 또는 실신할 것 같은 느낌',
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

  const [form, setForm] =
    useState(INITIAL_FORM)

  const [result, setResult] =
    useState(null)

  const [submitting, setSubmitting] =
    useState(false)

  const [error, setError] =
    useState('')

    useEffect(() => {
      let cancelled = false

      async function checkAccess() {
        try {
          setAccessChecking(true)
          setAccessError('')

          await verifyConditionCheckAccess(
            donationHistoryId
          )

          if (!cancelled) {
            setAccessAllowed(true)
          }
        } catch (requestError) {
          if (cancelled) return

          setAccessAllowed(false)

          if (requestError?.status === 403) {
            setAccessError(
              '본인의 헌혈 기록만 확인할 수 있습니다.'
            )
          } else if (requestError?.status === 404) {
            setAccessError(
              '헌혈 기록을 찾을 수 없습니다.'
            )
          } else {
            setAccessError(
              requestError?.message
              || '헌혈 기록을 확인하지 못했습니다.'
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
    setForm((current) => ({
      ...current,
      [key]: !current[key],
    }))
  }

  async function handleSubmit(event) {
    event.preventDefault()

    if (!form.overallCondition) {
      setError(
        '현재 전반적인 몸 상태를 선택해주세요.'
      )
      return
    }

    try {
      setSubmitting(true)
      setError('')

      const response =
        await createConditionCheck(
          donationHistoryId,
          {
            ...form,
            otherSymptom:
              form.otherSymptom.trim(),
          }
        )

      setResult(response)

      window.scrollTo({
        top: 0,
        behavior: 'smooth',
      })
    } catch (requestError) {
      setError(
        requestError.message
        || '컨디션 체크를 저장하지 못했습니다.'
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

  if (accessChecking) {
    return (
      <main className="condition-check-page">
        <div className="condition-check-card">
          <p>헌혈 기록을 확인하고 있습니다.</p>
        </div>
      </main>
    )
  }

  if (!accessAllowed) {
    return (
      <main className="condition-check-page">
        <div className="condition-check-card">
          <h1>접근할 수 없습니다</h1>
          <p>{accessError}</p>
        </div>
      </main>
    )
  }

  if (result) {
    return (
      <main className="condition-check-page">
        <section className="condition-check-result">
          <p>
            BLOODOPS AFTER CARE
          </p>

          <h1>
            컨디션 체크가 저장되었습니다
          </h1>

          <p>
            현재 상태에 맞는 안내를 확인해주세요.
          </p>

          {result.rewardGranted && (
            <div>
              <strong>
                첫 컨디션 체크 참여 보상
              </strong>

              <p>
                +{result.rewardPoints}P가
                지급되었습니다.
              </p>
            </div>
          )}

          {!result.rewardGranted && (
            <p>
              이번 헌혈에 대한 참여 보상은
              이미 지급되었습니다.
              컨디션 체크는 언제든 다시 할 수 있습니다.
            </p>
          )}

          <section>
            <h2>지금 이렇게 해주세요</h2>

            <ul>
              {result.guidance?.map(
                (guidance) => (
                  <li key={guidance}>
                    {guidance}
                  </li>
                )
              )}
            </ul>
          </section>

          <p>
            이 안내는 의학적 진단을 대신하지 않습니다.
            증상이 심하거나 계속되면
            의료기관에 문의해주세요.
          </p>

          <button
            type="button"
            onClick={handleRecheck}
          >
            다시 컨디션 체크하기
          </button>
        </section>
      </main>
    )
  }

  return (
    <main className="condition-check-page">
      <header>
        <p>
          BLOODOPS AFTER CARE
        </p>

        <h1>
          헌혈 후 컨디션 체크
        </h1>

        <p>
          헌혈하시느라 수고하셨습니다 😊
          지금 몸 상태는 어떠신가요?
        </p>
      </header>

      <form
        onSubmit={handleSubmit}
      >
        <section>
          <h2>
            전반적인 몸 상태
          </h2>

          <div>
            {CONDITION_OPTIONS.map(
              (option) => (
                <label
                  key={option.value}
                >
                  <input
                    type="radio"
                    name="overallCondition"
                    value={option.value}
                    checked={
                      form.overallCondition
                      === option.value
                    }
                    onChange={(event) =>
                      setForm(
                        (current) => ({
                          ...current,
                          overallCondition:
                            event.target.value,
                        })
                      )
                    }
                  />

                  <strong>
                    {option.label}
                  </strong>

                  <span>
                    {option.description}
                  </span>
                </label>
              )
            )}
          </div>
        </section>

        <section>
          <h2>
            해당되는 증상이 있나요?
          </h2>

          <p>
            여러 항목을 선택할 수 있습니다.
          </p>

          <div>
            {SYMPTOMS.map(
              (symptom) => (
                <label
                  key={symptom.key}
                >
                  <input
                    type="checkbox"
                    checked={
                      form[symptom.key]
                    }
                    onChange={() =>
                      handleSymptomChange(
                        symptom.key
                      )
                    }
                  />

                  <span>
                    {symptom.label}
                  </span>
                </label>
              )
            )}
          </div>
        </section>

        <section>
          <label>
            <strong>
              그 밖에 불편한 점
            </strong>

            <textarea
              maxLength={500}
              rows={4}
              value={
                form.otherSymptom
              }
              placeholder="다른 증상이 있다면 입력해주세요."
              onChange={(event) =>
                setForm(
                  (current) => ({
                    ...current,
                    otherSymptom:
                      event.target.value,
                  })
                )
              }
            />
          </label>

          <small>
            {form.otherSymptom.length}
            /500
          </small>
        </section>

        <aside>
          이 컨디션 체크는 헌혈 후 상태를
          확인하기 위한 간단한 안내입니다.
          응급상황이나 심한 증상이 있는 경우에는
          컨디션 체크 결과와 관계없이
          즉시 주변에 도움을 요청하거나
          의료기관에 문의해주세요.
        </aside>

        {error && (
          <p role="alert">
            {error}
          </p>
        )}

        <button
          type="submit"
          disabled={submitting}
        >
          {submitting
            ? '저장 중...'
            : '컨디션 체크 완료'}
        </button>
      </form>
    </main>
  )
}

export default ConditionCheckPage