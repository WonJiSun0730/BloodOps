import { useState } from 'react'
import { useNavigate } from 'react-router-dom'

import './BloodHoldCheckPage.css'

const HOLD_REASON = {
  FALSE_QUESTIONNAIRE:
    'FALSE_QUESTIONNAIRE',

  POST_DONATION_DISEASE:
    'POST_DONATION_DISEASE',

  MEDICAL_OR_MEDICATION:
    'MEDICAL_OR_MEDICATION',

  MALARIA_RISK:
    'MALARIA_RISK',

  VCJD_RISK:
    'VCJD_RISK',
}

const HOLD_REASON_OPTIONS = [
  {
    value:
      HOLD_REASON.FALSE_QUESTIONNAIRE,
    label:
      '문진사항에 사실과 다르게 응답하고 헌혈한 경우',
  },
  {
    value:
      HOLD_REASON.POST_DONATION_DISEASE,
    label:
      '헌혈 후 질병 발병',
  },
  {
    value:
      HOLD_REASON.MEDICAL_OR_MEDICATION,
    label:
      '과거병력 및 약물 복용',
  },
  {
    value:
      HOLD_REASON.MALARIA_RISK,
    label:
      '말라리아 위험지역 여행 및 거주',
  },
  {
    value:
      HOLD_REASON.VCJD_RISK,
    label:
      '변형 크로이츠펠트-야콥병 위험지역의 헌혈배제',
  },
]

async function requestDiseaseRisk(
  diseaseName
) {
  const csrfResponse =
    await fetch(
      '/api/auth/csrf',
      {
        credentials:
          'include',
      }
    )

  if (!csrfResponse.ok) {
    throw new Error(
      '보안 정보를 불러오지 못했습니다.'
    )
  }

  const csrf =
    await csrfResponse.json()

  const response =
    await fetch(
      '/api/blood-hold/disease-risk',
      {
        method: 'POST',
        credentials:
          'include',
        headers: {
          'Content-Type':
            'application/json',
          [csrf.headerName]:
            csrf.token,
        },
        body:
          JSON.stringify({
            diseaseName,
          }),
      }
    )

  if (!response.ok) {
    throw new Error(
      '질병 정보를 확인하지 못했습니다.'
    )
  }

  return response.json()
}

function BloodHoldCheckPage() {
  const navigate =
    useNavigate()

  const [
    selectedReasons,
    setSelectedReasons,
  ] =
    useState([])

  const [
    diseaseName,
    setDiseaseName,
  ] =
    useState('')

  const [
    diseaseRisk,
    setDiseaseRisk,
  ] =
    useState(null)

  const [
    isCheckingDisease,
    setIsCheckingDisease,
  ] =
    useState(false)

  const [
    errorMessage,
    setErrorMessage,
  ] =
    useState('')

  const hasPostDonationDisease =
    selectedReasons.includes(
      HOLD_REASON
        .POST_DONATION_DISEASE
    )

  const hasSelectedReason =
    selectedReasons.length > 0

  const requiresDiseaseCheck =
    hasPostDonationDisease

  const hasCompletedDiseaseCheck =
    !requiresDiseaseCheck
    || (
      diseaseRisk
      && diseaseRisk.diseaseName
        === diseaseName.trim()
    )

  const canContinue =
    hasSelectedReason
    && hasCompletedDiseaseCheck
    && !isCheckingDisease

  function handleReasonChange(
    reason
  ) {
    setErrorMessage('')

    setSelectedReasons(
      (currentReasons) => {
        if (
          currentReasons.includes(
            reason
          )
        ) {
          return currentReasons.filter(
            (currentReason) =>
              currentReason
                !== reason
          )
        }

        return [
          ...currentReasons,
          reason,
        ]
      }
    )

    if (
      reason
      === HOLD_REASON
        .POST_DONATION_DISEASE
      && hasPostDonationDisease
    ) {
      setDiseaseName('')
      setDiseaseRisk(null)
    }
  }

  function handleDiseaseNameChange(
    event
  ) {
    setDiseaseName(
      event.target.value
    )

    /*
     * 판정 후 질병명을 수정했다면
     * 기존 판정 결과를 그대로 사용하지 않습니다.
     */
    setDiseaseRisk(null)
    setErrorMessage('')
  }

  async function handleDiseaseCheck() {
    const normalizedDiseaseName =
      diseaseName.trim()

    if (
      !normalizedDiseaseName
    ) {
      setErrorMessage(
        '발병한 질병명을 입력해주세요.'
      )

      return
    }

    setIsCheckingDisease(true)
    setDiseaseRisk(null)
    setErrorMessage('')

    try {
      const result =
        await requestDiseaseRisk(
          normalizedDiseaseName
        )

      setDiseaseRisk(result)

    } catch (error) {
      setErrorMessage(
        error?.message
        || '질병 정보를 확인하지 못했습니다.'
      )

    } finally {
      setIsCheckingDisease(false)
    }
  }

  function handleContinue() {
    if (
      !hasSelectedReason
    ) {
      setErrorMessage(
        '해당하는 항목을 하나 이상 선택해주세요.'
      )

      return
    }

    if (
      requiresDiseaseCheck
      && !diseaseName.trim()
    ) {
      setErrorMessage(
        '발병한 질병명을 입력해주세요.'
      )

      return
    }

    if (
      requiresDiseaseCheck
      && !hasCompletedDiseaseCheck
    ) {
      setErrorMessage(
        '질병의 전염 가능성을 먼저 확인해주세요.'
      )

      return
    }

    const checkResult = {
      reasons:
        selectedReasons,

      disease:
        hasPostDonationDisease
          ? diseaseRisk
          : null,

      visitRestricted:
        hasPostDonationDisease
        && diseaseRisk
        && (
          diseaseRisk.visitRisk
            === 'VISIT_RESTRICTED'
          || diseaseRisk.visitRisk
            === 'UNCERTAIN'
        ),
    }

    /*
     * 새로고침하더라도
     * 가이드 화면에서 체크 결과를
     * 다시 사용할 수 있도록
     * 세션에 임시 보관합니다.
     */
    sessionStorage.setItem(
      'bloodHoldCheckResult',
      JSON.stringify(
        checkResult
      )
    )

    navigate(
      '/mypage/blood-hold'
    )
  }

  return (
    <main
      className="blood-hold-check-page"
    >
      <section
        className="blood-hold-check-container"
        aria-labelledby="blood-hold-check-title"
      >
        <header
          className="blood-hold-check-header"
        >
          <p
            className="blood-hold-check-eyebrow"
          >
            BloodOps
          </p>

          <h1
            id="blood-hold-check-title"
          >
            혈액사용보류 요청
          </h1>

          <p>
            헌혈 후 확인된 사항이 있다면
            아래 항목을 먼저 확인해주세요.
          </p>
        </header>

        <section
          className="blood-hold-check-info"
        >
          <h2>
            혈액사용보류신청이란
          </h2>

          <p>
            헌혈 전 문진내용에
            사실과 다르게 응답하거나
            헌혈 후 발생한 질환 등으로
            수혈자의 안전에 영향을 줄
            가능성이 있는 경우,
            헌혈자의 연락이 필요합니다.
          </p>
        </section>

        <section
          className="blood-hold-check-form"
        >
          <div
            className="blood-hold-check-section-heading"
          >
            <h2>
              해당하는 경우를
              선택해주세요.
            </h2>

            <p>
              하나 이상 선택할 수 있습니다.
            </p>
          </div>

          <div
            className="blood-hold-reason-list"
          >
            {HOLD_REASON_OPTIONS.map(
              (option) => {
                const checked =
                  selectedReasons
                    .includes(
                      option.value
                    )

                return (
                  <label
                    key={
                      option.value
                    }
                    className={
                      checked
                        ? 'blood-hold-reason-item is-selected'
                        : 'blood-hold-reason-item'
                    }
                  >
                    <input
                      type="checkbox"
                      checked={
                        checked
                      }
                      onChange={() =>
                        handleReasonChange(
                          option.value
                        )
                      }
                    />

                    <span
                      className="blood-hold-reason-checkbox"
                      aria-hidden="true"
                    />

                    <span>
                      {option.label}
                    </span>
                  </label>
                )
              }
            )}
          </div>

          {hasPostDonationDisease && (
            <div
              className="blood-hold-disease-section"
            >
              <div>
                <h3>
                  발병한 질병을
                  입력해주세요.
                </h3>

                <p>
                  입력한 질병명은
                  직접 방문 전 주의가
                  필요한지 확인하는 데
                  사용됩니다.
                </p>
              </div>

              <div
                className="blood-hold-disease-input-row"
              >
                <label
                  htmlFor="blood-hold-disease-name"
                  className="sr-only"
                >
                  질병명
                </label>

                <input
                  id="blood-hold-disease-name"
                  type="text"
                  value={
                    diseaseName
                  }
                  onChange={
                    handleDiseaseNameChange
                  }
                  placeholder="예: 독감"
                  autoComplete="off"
                  disabled={
                    isCheckingDisease
                  }
                />

                <button
                  type="button"
                  onClick={
                    handleDiseaseCheck
                  }
                  disabled={
                    isCheckingDisease
                    || !diseaseName.trim()
                  }
                >
                  {isCheckingDisease
                    ? '확인 중...'
                    : '전염 가능성 확인'}
                </button>
              </div>

              {diseaseRisk && (
                <div
                  className={
                    `blood-hold-disease-result ${
                      diseaseRisk.visitRisk
                        === 'VISIT_ALLOWED'
                        ? 'is-allowed'
                        : 'is-caution'
                    }`
                  }
                  role="status"
                >
                  <strong>
                    {diseaseRisk.visitRisk
                      === 'VISIT_ALLOWED'
                      ? '전화 문의 후 안내에 따라주세요.'
                      : '직접 방문 전 전화 문의가 필요합니다.'}
                  </strong>

                  <p>
                    {diseaseRisk.reason}
                  </p>

                  {!diseaseRisk.aiAvailable && (
                    <p>
                      현재 자동 확인 서비스가
                      원활하지 않아
                      안전을 위해 직접 방문
                      길찾기를 제한합니다.
                    </p>
                  )}
                </div>
              )}
            </div>
          )}

          <aside
            className="blood-hold-check-notice"
          >
            <strong>
              혈액사용보류 요청은
              헌혈증서에 표기된 혈액원으로
              연락하는 것이 우선입니다.
            </strong>

            <p>
              이 확인 결과는 의료 진단이나
              방문 가능 여부를 확정하는
              판정이 아닙니다.
              혈액원의 안내를 우선해주세요.
            </p>
          </aside>

          {errorMessage && (
            <p
              className="blood-hold-check-error"
              role="alert"
            >
              {errorMessage}
            </p>
          )}

          <div
            className="blood-hold-check-actions"
          >
            <button
              type="button"
              className="blood-hold-check-next-button"
              onClick={
                handleContinue
              }
              disabled={
                !canContinue
              }
            >
              혈액원 연락처 확인하기
            </button>
          </div>
        </section>
      </section>
    </main>
  )
}

export default BloodHoldCheckPage