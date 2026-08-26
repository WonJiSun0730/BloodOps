import {
  useEffect,
  useState,
} from 'react'

import {
  useSearchParams,
} from 'react-router-dom'

import {
  createReservation,
  getCenters,
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

  async function handleCreateReservation() {
    if (!selectedSlotId) {
      setError(
        '예약할 시간을 선택해주세요.'
      )

      return
    }

    try {
      setSubmitting(true)
      setError('')
      setSuccessMessage('')

      await createReservation(
        selectedSlotId
      )

      setSuccessMessage(
        '헌혈 예약이 완료되었습니다.'
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

        <CenterSearchPanel
          centers={centers}
          selectedCenterId={centerId}
          initialKeyword={initialCenterKeyword}
          onSelectCenter={(nextCenterId) => {
              setCenterId(
                nextCenterId
              )

              setSlots([])
              setSelectedSlotId(null)
              setSuccessMessage('')
              setError('')
            }}
          />

        {centerId && (
          <>

        <section
          className="reservation-search-form"
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

            <div className="reservation-date-heading">
              <strong>
                예약 날짜
              </strong>

              {reservationDate && (
                <span>
                  선택일 {reservationDate}
                </span>
              )}
            </div>

            <ReservationCalendar
              value={reservationDate}
              onChange={(nextDate) => {
                setReservationDate(nextDate)

                setSlots([])
                setSelectedSlotId(null)
                setSuccessMessage('')
                setError('')
              }}
            />

          </div>

          <div className="reservation-donation-field">

            <strong>
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

                        setSlots([])
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

          </div>



        </section>


        {error && (
          <p className="reservation-error">
            {error}
          </p>
        )}

        {reservationDate && donationType && (
          <>

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
              예약 가능 {slots.length}개
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
                    selectedSlotId === slot.id
                      ? 'reservation-slot-card selected'
                      : 'reservation-slot-card'
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
                      {' ~ '}
                      {formatTime(slot.endTime)}
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

        {selectedSlotId && (
          <div className="reservation-selection-summary">

            <div>
              <span>
                선택한 예약 시간
              </span>

              <strong>
                {(() => {
                  const selectedSlot =
                    slots.find(
                      (slot) =>
                        slot.id === selectedSlotId
                    )

                  if (!selectedSlot) {
                    return '-'
                  }

                  return `${
                    formatTime(
                      selectedSlot.startTime
                    )
                  } ~ ${
                    formatTime(
                      selectedSlot.endTime
                    )
                  }`
                })()}
              </strong>
            </div>

            <span>
              예약 버튼을 누르면
              선택한 시간으로 예약됩니다.
            </span>

          </div>
        )}

        {successMessage && (
          <p className="reservation-success">
            {successMessage}
          </p>
        )}

        {slots.length > 0 && (
          <button
            type="button"
            onClick={handleCreateReservation}
            disabled={
              !selectedSlotId
              || submitting
            }
            className="reservation-submit-button"
          >
            {submitting
              ? '예약 처리 중...'
              : '선택한 시간으로 예약'}
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