import {
  useEffect,
  useRef,
  useState,
} from 'react'

import {
  Link,
  useSearchParams,
} from 'react-router-dom'

import {
  createReservation,
  getAvailableReservationDates,
  getCenters,
  getCurrentUser,
  getReservationSlots,
} from '../../api/apiClient.js'

import './ReservationCreatePage.css'

import ReservationCalendar
  from './ReservationCalendar.jsx'

import CenterSearchPanel
  from './CenterSearchPanel.jsx'


const donationTypes = [
  {
    value: 'WHOLE_BLOOD',
    label: '전혈',
  },
  {
    value: 'PLATELET',
    label: '혈소판',
  },
  {
    value: 'PLASMA',
    label: '혈장',
  },
  {
    value: 'PLATELET_PLASMA',
    label: '혈소판혈장',
  },
]

function formatTime(time) {
  if (!time) {
    return ''
  }

  return time.slice(0, 5)
}

function formatKoreanDate(dateValue) {
  if (!dateValue) {
    return ''
  }

  const [
    year,
    month,
    day,
  ] = dateValue.split('-')

  return `${year}년 ${month}월 ${day}일`
}


function formatResidentNumber(value) {
  const digits =
    value.replace(/\D/g, '')
      .slice(0, 13)

  if (digits.length <= 6) {
    return digits
  }

  return `${
    digits.slice(0, 6)
  }-${
    digits.slice(6)
  }`
}


function ReservationCreatePage() {
  const [
    searchParams,
  ] = useSearchParams()

  const [centers, setCenters] =
    useState([])

  const [centerId, setCenterId] =
    useState('')

  const [
    initialCenterKeyword,
    setInitialCenterKeyword,
  ] = useState('')

  const [
    reservationDate,
    setReservationDate,
  ] = useState('')

  const [
    availableDates,
    setAvailableDates,
  ] = useState([])

  const [
    calendarMonth,
    setCalendarMonth,
  ] = useState(() => {
    const now = new Date()

    return {
      year: now.getFullYear(),
      month: now.getMonth() + 1,
    }
  })

  const [
    donationType,
    setDonationType,
  ] = useState('')

  const [slots, setSlots] =
    useState([])

  const [
    selectedSlotId,
    setSelectedSlotId,
  ] = useState(null)

  const [
    donorName,
    setDonorName,
  ] = useState('')

  const [
    phone,
    setPhone,
  ] = useState('')

  const [
    residentRegistrationNumber,
    setResidentRegistrationNumber,
  ] = useState('')

  const [
    privacyConsented,
    setPrivacyConsented,
  ] = useState(false)

  const [
    memberInfoLoaded,
    setMemberInfoLoaded,
  ] = useState(false)

  const [
    submitting,
    setSubmitting,
  ] = useState(false)

  const [
    successMessage,
    setSuccessMessage,
  ] = useState('')

  const [loading, setLoading] =
    useState(false)

  const [error, setError] =
    useState('')

  const [
    submitNotice,
    setSubmitNotice,
  ] = useState('')

  const donorNameRef =
    useRef(null)

  const phoneRef =
    useRef(null)

  const residentNumberRef =
    useRef(null)

  const privacyConsentRef =
    useRef(null)

  const centerStepRef =
    useRef(null)

  const conditionStepRef =
    useRef(null)

  const slotStepRef =
    useRef(null)

  const confirmStepRef =
    useRef(null)

function scrollToStep(stepRef) {
  if (!stepRef?.current) {
    return
  }

  stepRef.current.scrollIntoView({
    behavior: 'smooth',
    block: 'start',
  })
}

  useEffect(() => {
    async function loadCenters() {
      try {
        const data =
          await getCenters()

        setCenters(data)
      } catch (error) {
        setError(
          error.message
          || '헌혈센터 목록을 불러오지 못했습니다.'
        )
      }
    }

    loadCenters()
  }, [])

  useEffect(() => {
    let cancelled = false

    async function loadMemberInfo() {
      try {
        const user =
          await getCurrentUser()

        if (cancelled) {
          return
        }

        setDonorName(
          user?.name || ''
        )

        setPhone(
          user?.phone || ''
        )

        setMemberInfoLoaded(true)
      } catch {
        if (!cancelled) {
          setMemberInfoLoaded(true)
        }
      }
    }

    loadMemberInfo()

    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    const requestedCenterId =
      searchParams.get('centerId')

    if (
      !requestedCenterId
      || centers.length === 0
    ) {
      return
    }

    const requestedCenter =
      centers.find(
        (center) =>
          String(center.id)
            === requestedCenterId
      )

    if (
      !requestedCenter
      || requestedCenter.active === false
    ) {
      return
    }

    setCenterId(
      requestedCenterId
    )

    setInitialCenterKeyword(
      requestedCenter.centerName
    )
  }, [
    centers,
    searchParams,
  ])

  useEffect(() => {
    if (!centerId) {
      setAvailableDates([])
      return
    }

    let cancelled = false

    async function loadAvailableDates() {
      try {
        const data =
          await getAvailableReservationDates(
            centerId,
            calendarMonth.year,
            calendarMonth.month
          )

        if (!cancelled) {
          setAvailableDates(
            Array.isArray(data)
              ? data
              : []
          )
        }
      } catch {
        if (!cancelled) {
          setAvailableDates([])
        }
      }
    }

    loadAvailableDates()

    return () => {
      cancelled = true
    }
  }, [
    centerId,
    calendarMonth,
  ])

  useEffect(() => {
    if (
      !centerId
      || !reservationDate
      || !donationType
    ) {
      setSlots([])
      setSelectedSlotId(null)
      return
    }

    let cancelled = false

    async function loadReservationSlots() {
      try {
        setLoading(true)
        setError('')
        setSuccessMessage('')
        setSelectedSlotId(null)

        const data =
          await getReservationSlots(
            centerId,
            reservationDate,
            donationType
          )

        if (!cancelled) {
          setSlots(data)
        }
      } catch (error) {
        if (!cancelled) {
          setSlots([])

          setError(
            error.message
            || '예약 가능한 시간을 불러오지 못했습니다.'
          )
        }
      } finally {
        if (!cancelled) {
          setLoading(false)
        }
      }
    }

    loadReservationSlots()

    return () => {
      cancelled = true
    }
  }, [
    centerId,
    reservationDate,
    donationType,
  ])

  const selectedCenter =
    centers.find(
      (center) =>
        String(center.id)
          === String(centerId)
    )

  const selectedSlot =
    slots.find(
      (slot) =>
        slot.id === selectedSlotId
    )

  const selectedDonationType =
    donationTypes.find(
      (type) =>
        type.value === donationType
    )

  const selectedCenterName =
    selectedCenter?.centerName || ''

  const displayCenterName =
    selectedCenterName
      ? selectedCenterName.startsWith(
          '헌혈의집'
        )
        ? selectedCenterName
        : `헌혈의집 ${selectedCenterName}`
      : ''

  const availableSlotCount =
    slots.filter(
      (slot) =>
        slot.remainingCapacity > 0
    ).length

function handleReservationSubmitAttempt() {
  let message = ''
  let targetRef = null

  if (!donorName.trim()) {
    message =
      '예약자 성명을 입력해주세요.'

    targetRef =
      donorNameRef
  } else if (!phone.trim()) {
    message =
      '휴대전화 번호를 입력해주세요.'

    targetRef =
      phoneRef
  } else if (
    residentRegistrationNumber
      .replace(/\D/g, '')
      .length !== 13
  ) {
    message =
      '주민등록번호 13자리를 입력해주세요.'

    targetRef =
      residentNumberRef
  } else if (!privacyConsented) {
    message =
      '개인정보 수집 및 이용에 동의해주세요.'

    targetRef =
      privacyConsentRef
  }

  if (message) {
    setSubmitNotice(
      message
    )

    scrollToStep(
      confirmStepRef
    )

    window.setTimeout(() => {
      targetRef?.current?.focus()
    }, 350)

    return
  }

  setSubmitNotice('')

  handleCreateReservation()
}

  async function handleCreateReservation() {
    if (!selectedSlotId) {
      setError(
        '예약할 시간을 선택해주세요.'
      )

      return
    }

    if (!donorName.trim()) {
      setError(
        '예약자 성명을 입력해주세요.'
      )

      return
    }

    if (!phone.trim()) {
      setError(
        '휴대전화를 입력해주세요.'
      )

      return
    }

    const residentDigits =
      residentRegistrationNumber
        .replace(/\D/g, '')

    if (residentDigits.length !== 13) {
      setError(
        '주민등록번호 13자리를 입력해주세요.'
      )

      return
    }

    if (!privacyConsented) {
      setError(
        '개인정보 수집 및 이용에 동의해주세요.'
      )

      return
    }

    if (!selectedSlot) {
      setError(
        '선택한 예약 시간을 확인할 수 없습니다.'
      )

      return
    }

    try {
      setSubmitting(true)
      setError('')
      setSuccessMessage('')

      await createReservation({
        slotId: selectedSlotId,
        donorName: donorName.trim(),
        residentRegistrationNumber,
        phone: phone.trim(),
        privacyConsented,
      })

      const completedMessage =
        `${displayCenterName}, ${
          formatKoreanDate(
            reservationDate
          )
        } ${
          formatTime(
            selectedSlot.startTime
          )
        } ${
          selectedDonationType?.label || ''
        }헌혈을 선택하셨습니다.`

      setSuccessMessage(
        completedMessage
      )

      const refreshedSlots =
        await getReservationSlots(
          centerId,
          reservationDate,
          donationType
        )

      setSlots(
        refreshedSlots
      )

      setSelectedSlotId(null)
      setResidentRegistrationNumber('')
      setPrivacyConsented(false)

      setAvailableDates(
        (previousDates) =>
          previousDates
      )
    } catch (error) {
      setError(
        error.message
        || '예약 처리 중 오류가 발생했습니다.'
      )
    } finally {
      setSubmitting(false)
    }
  }


  return (
    <main className="reservation-create-page">

      <section className="reservation-create-header">
        <span>
          BLOOD DONATION RESERVATION
        </span>

        <h1>
          헌혈 예약
        </h1>

        <p>
          헌혈센터와 날짜를 선택하고
          예약 가능한 시간을 확인하세요.
        </p>
      </section>


      <section className="reservation-create-content">

        <div
          ref={centerStepRef}
          className="reservation-step-anchor"
        >
        <CenterSearchPanel
          centers={centers}
          selectedCenterId={centerId}
          initialKeyword={initialCenterKeyword}
          onNextStep={() =>
              scrollToStep(
                conditionStepRef
              )
            }
          onSelectCenter={(nextCenterId) => {
            if (
              String(nextCenterId)
                === String(centerId)
            ) {
              return
            }

            setCenterId(
              nextCenterId
            )

            setReservationDate('')
            setDonationType('')
            setAvailableDates([])
            setSlots([])
            setSelectedSlotId(null)
            setSuccessMessage('')
            setError('')
          }}
        />
        </div>

        {centerId && (
          <>

        <section
          ref={conditionStepRef}
          className="reservation-search-form reservation-step-anchor"
        >
          <div className="reservation-condition-heading">
            <span>
              STEP 02
            </span>

            <div>
              <h2>
                예약 조건 선택
              </h2>

              <p>
                날짜와 헌혈 종류를 선택한 뒤
                예약 가능한 시간을 확인하세요.
              </p>
            </div>
          </div>

          <div className="reservation-date-field">

            <ReservationCalendar
              value={reservationDate}
              availableDates={availableDates}
              onVisibleMonthChange={(year, month) => {
                setCalendarMonth({
                  year,
                  month,
                })
              }}
              onChange={(nextDate) => {
                setReservationDate(nextDate)

                setSelectedSlotId(null)
                setSuccessMessage('')
                setError('')
              }}
            />

          </div>

          <div className="reservation-donation-field">

            <div className="reservation-selected-date-card">
              <span>
                예약 날짜
              </span>

              <strong>
                {reservationDate
                  ? reservationDate
                  : '날짜를 선택해주세요'}
              </strong>
            </div>

            <strong className="reservation-donation-title">
              헌혈 종류
            </strong>

            <div className="reservation-donation-options">

              {donationTypes.map(
                (type) => {

                  const selected =
                    donationType === type.value

                  return (
                    <button
                      key={type.value}
                      type="button"
                      className={
                        selected
                          ? 'reservation-donation-option selected'
                          : 'reservation-donation-option'
                      }
                      onClick={() => {
                        setDonationType(
                          type.value
                        )

                        setSelectedSlotId(null)
                        setSuccessMessage('')
                        setError('')
                      }}
                    >
                      {type.label}
                    </button>
                  )
                }
              )}

            </div>

          <div className="reservation-calendar-guide">

            <div className="reservation-calendar-guide-header">
              <strong>
                예약 안내
              </strong>

              <span>
                최대 2개월 이내 예약 가능
              </span>
            </div>

            <div className="reservation-calendar-guide-item">
              <span className="reservation-guide-dot available" />

              <div>
                <strong>
                  예약 가능 날짜
                </strong>

                <p>
                  예약 가능한 시간이 있는 날짜입니다.
                </p>
              </div>
            </div>

            <div className="reservation-calendar-guide-item">
              <span className="reservation-guide-dot unavailable" />

              <div>
                <strong>
                  예약 불가 날짜
                </strong>

                <p>
                  슬롯이 없거나 모든 시간이 마감되었습니다.
                </p>
              </div>
            </div>

            <div className="reservation-calendar-guide-notice">
              당일 예약은 불가능합니다.
            </div>

          </div>

          </div>

      <div className="reservation-step-navigation">

        <button
          type="button"
          className="reservation-step-back"
          onClick={() =>
            scrollToStep(
              centerStepRef
            )
          }
        >
          <span aria-hidden="true">
            ←
          </span>

          <span>
            센터 다시 보기
          </span>
        </button>


        <div className="reservation-step-progress">
          <span>
            STEP 02
          </span>

          <strong>
            {reservationDate && donationType
              ? '예약 조건 선택 완료'
              : '날짜와 헌혈 종류를 선택해주세요'}
          </strong>
        </div>


        <button
          type="button"
          className="reservation-step-next"
          disabled={
            !reservationDate
            || !donationType
          }
          onClick={() =>
            scrollToStep(
              slotStepRef
            )
          }
        >
          <span>
            예약 시간 선택
          </span>

          <span
            className="reservation-step-arrow"
            aria-hidden="true"
          >
            →
          </span>
        </button>

      </div>

        </section>


        {error && (
          <p className="reservation-error">
            {error}
          </p>
        )}

        {reservationDate && donationType && (
          <>

        <div
          ref={slotStepRef}
          className="reservation-step-anchor"
        >

        <section className="reservation-slot-heading">

          <div>
            <span>
              STEP 03
            </span>

            <h2>
              예약 시간 선택
            </h2>

            <p>
              예약 가능한 시간 중
              원하는 시간을 선택해주세요.
            </p>
          </div>

          {slots.length > 0 && (
            <span className="reservation-slot-count">
              예약 가능 {availableSlotCount}개
            </span>
          )}

        </section>

        <div className="reservation-slot-area">

          {loading ? (
            <div className="reservation-slot-empty">
              <strong>
                예약 가능한 시간을 확인하고 있습니다.
              </strong>

              <p>
                잠시만 기다려주세요.
              </p>
            </div>
          ) : slots.length === 0 ? (
            <div className="reservation-slot-empty">
              <strong>
                예약 가능한 시간이 없습니다.
              </strong>

              <p>
                다른 날짜 또는 헌혈 종류를 선택해주세요.
              </p>
            </div>
          ) : (
            slots.map(
              (slot) => (
                <article
                  key={slot.id}
                  className={
                    `reservation-slot-card${
                      selectedSlotId === slot.id
                        ? ' selected'
                        : ''
                    }${
                      slot.remainingCapacity <= 0
                        ? ' closed'
                        : ''
                    }`
                  }
                >
                  <button
                    type="button"
                    disabled={
                      slot.remainingCapacity <= 0
                    }
                    onClick={() =>
                      setSelectedSlotId(
                        slot.id
                      )
                    }
                  >
                    <strong>
                      {formatTime(slot.startTime)}
                    </strong>

                    <span>
                      {slot.remainingCapacity > 0
                        ? '예약 가능'
                        : '예약 마감'}
                    </span>
                  </button>
                </article>
              )
            )
          )}

        </div>

        <div className="reservation-step-navigation">

          <button
            type="button"
            className="reservation-step-back"
            onClick={() =>
              scrollToStep(
                conditionStepRef
              )
            }
          >
            <span aria-hidden="true">
              ←
            </span>

            <span>
              예약 조건 다시보기
            </span>
          </button>


          <div className="reservation-step-progress">
            <span>
              STEP 03
            </span>

            <strong>
              {selectedSlotId
                ? `${formatTime(
                    selectedSlot?.startTime
                  )} 선택 완료`
                : '예약 시간을 선택해주세요'}
            </strong>
          </div>


          <button
            type="button"
            className="reservation-step-next"
            disabled={!selectedSlotId}
            onClick={() =>
              scrollToStep(
                confirmStepRef
              )
            }
          >
            <span>
              예약 정보 확인
            </span>

            <span
              className="reservation-step-arrow"
              aria-hidden="true"
            >
              →
            </span>
          </button>

        </div>

        </div>

        {selectedSlotId && selectedSlot && (
          <section
            ref={confirmStepRef}
            className="reservation-confirm-section reservation-step-anchor"
          >

            <div className="reservation-confirm-heading">
              <span>
                STEP 04
              </span>

              <h2>
                예약 정보 확인
              </h2>

              <p>
                선택한 예약 정보와 예약자 정보를
                확인해주세요.
              </p>
            </div>


            <div className="reservation-confirm-summary">

              <div>
                <span>
                  헌혈센터
                </span>

                <strong>
                  {displayCenterName}
                </strong>
              </div>

              <div>
                <span>
                  예약 날짜
                </span>

                <strong>
                  {formatKoreanDate(
                    reservationDate
                  )}
                </strong>
              </div>

              <div>
                <span>
                  예약 시간
                </span>

                <strong>
                  {formatTime(
                    selectedSlot.startTime
                  )}
                </strong>
              </div>

              <div>
                <span>
                  헌혈 종류
                </span>

                <strong>
                  {selectedDonationType?.label}
                  헌혈
                </strong>
              </div>

            </div>


            <div className="reservation-personal-card">

              <div className="reservation-personal-heading">
                <div>
                  <strong>
                    예약자 정보
                  </strong>

                  <p>
                    회원정보에 등록된 항목은
                    자동으로 입력됩니다.
                  </p>
                </div>

                {memberInfoLoaded && (
                  <span>
                    회원정보 확인 완료
                  </span>
                )}
              </div>


              <div className="reservation-personal-grid">

                <label>
                  <span>
                    성명
                  </span>

                  <input
                    ref={donorNameRef}
                    type="text"
                    value={donorName}
                    onChange={(event) => {
                      setDonorName(
                        event.target.value
                      )

                      setSubmitNotice('')
                    }}
                    placeholder="성명을 입력해주세요"
                    maxLength={100}
                  />
                </label>


                <label>
                  <span>
                    휴대전화
                  </span>

                  <input
                    type="tel"
                    value={phone}
                    onChange={(event) =>
                      setPhone(
                        event.target.value
                      )
                    }
                    placeholder="010-1234-5678"
                    maxLength={20}
                  />
                </label>


                <label className="reservation-resident-field">
                  <span>
                    주민등록번호
                  </span>

                  <input
                    ref={residentNumberRef}
                    type="text"
                    value={
                      residentRegistrationNumber
                    }
                    onChange={(event) => {
                      setResidentRegistrationNumber(
                        formatResidentNumber(
                          event.target.value
                        )
                      )

                      setSubmitNotice('')
                    }}
                    placeholder="000000-0000000"
                    inputMode="numeric"
                    autoComplete="off"
                    maxLength={14}
                  />

                  <small>
                    주민등록번호 원문은 저장하지 않으며
                    식별용 해시값과 마스킹 정보만 저장합니다.
                  </small>
                </label>

              </div>

            </div>


            <label className="reservation-privacy-card">

              <input
                ref={privacyConsentRef}
                type="checkbox"
                checked={privacyConsented}
                onChange={(event) => {
                  setPrivacyConsented(
                    event.target.checked
                  )

                  setSubmitNotice('')
                }}
              />

              <span className="reservation-privacy-check" />

              <div>
                <strong>
                  개인정보 수집 및 이용에 동의합니다.
                </strong>

                <p>
                  헌혈 예약 및 예약자 확인을 위해
                  성명, 휴대전화 및 주민등록번호의
                  식별 정보를 처리합니다.
                  예약 관련 개인정보의 보유기간은
                  예약일로부터 5년입니다.
                </p>
              </div>

            </label>

            <div
              className="
                reservation-step-navigation
                reservation-step-navigation-final
              "
            >

              <button
                type="button"
                className="reservation-step-back"
                onClick={() =>
                  scrollToStep(
                    slotStepRef
                  )
                }
              >
                <span aria-hidden="true">
                  ←
                </span>

                <span>
                  시간 다시 선택
                </span>
              </button>


              <div className="reservation-step-progress">
                <span>
                  STEP 04
                </span>

                <strong>
                  예약 정보를 확인해주세요
                </strong>
              </div>

            </div>

          </section>
        )}

        {successMessage && (
          <section className="reservation-complete-card">

            <div
              className="reservation-complete-icon"
              aria-hidden="true"
            >
              ✓
            </div>

            <div className="reservation-complete-content">

              <span>
                RESERVATION COMPLETE
              </span>

              <h2>
                헌혈 예약이 완료되었습니다.
              </h2>

              <p>
                {successMessage}
              </p>

              <small>
                예약 내역은{' '}
                <Link
                  to="/reservations"
                  className="reservation-complete-link"
                >
                  마이페이지 → 헌혈 예약 내역
                </Link>
                에서 다시 확인할 수 있습니다.
              </small>

            </div>

          </section>
        )}

        {selectedSlotId && submitNotice && (
          <div
            className="reservation-submit-notice"
            role="status"
          >
            <div
              className="reservation-submit-notice-icon"
              aria-hidden="true"
            >
              !
            </div>

            <div>
              <strong>
                예약 정보를 확인해주세요
              </strong>

              <p>
                {submitNotice}
              </p>
            </div>
          </div>
        )}

        {selectedSlotId && (
          <button
            type="button"
            onClick={
              handleReservationSubmitAttempt
            }
            disabled={submitting}
            aria-disabled={
              !donorName.trim()
              || !phone.trim()
              || residentRegistrationNumber
                .replace(/\D/g, '')
                .length !== 13
              || !privacyConsented
            }
            className={
              `reservation-submit-button${
                !donorName.trim()
                || !phone.trim()
                || residentRegistrationNumber
                  .replace(/\D/g, '')
                  .length !== 13
                || !privacyConsented
                  ? ' incomplete'
                  : ''
              }`
            }
          >
            {submitting
              ? '예약 처리 중...'
              : '헌혈 예약 확정'}
          </button>
        )}

          </>
        )}

        </>
      )}

      </section>

    </main>
  )
}


export default ReservationCreatePage