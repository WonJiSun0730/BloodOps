import {
  useEffect,
  useState,
} from 'react'

import {
  createReservation,
  getCenters,
  getReservationSlots,
} from '../../api/apiClient.js'

import './ReservationCreatePage.css'


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


function ReservationCreatePage() {
  const [centers, setCenters] =
    useState([])

  const [centerId, setCenterId] =
    useState('')

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


  async function handleSearchSlots(
    event
  ) {
    event.preventDefault()

    if (
      !centerId
      || !reservationDate
      || !donationType
    ) {
      setError(
        '센터, 날짜, 헌혈 종류를 모두 선택해주세요.'
      )

      return
    }

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

      setSlots(data)
    } catch (error) {
      setSlots([])

      setError(
        error.message
        || '예약 가능한 시간을 불러오지 못했습니다.'
      )
    } finally {
      setLoading(false)
    }
  }

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

        <form
          onSubmit={handleSearchSlots}
          className="reservation-search-form"
        >
          <label>
            헌혈센터

            <select
              value={centerId}
              onChange={(event) =>
                setCenterId(
                  event.target.value
                )
              }
            >
              <option value="">
                센터를 선택해주세요
              </option>

              {centers.map(
                (center) => (
                  <option
                    key={center.id}
                    value={center.id}
                  >
                    {center.centerName}
                  </option>
                )
              )}
            </select>
          </label>


          <label>
            예약 날짜

            <input
              type="date"
              value={reservationDate}
              onChange={(event) =>
                setReservationDate(
                  event.target.value
                )
              }
            />
          </label>


          <label>
            헌혈 종류

            <select
              value={donationType}
              onChange={(event) =>
                setDonationType(
                  event.target.value
                )
              }
            >
              <option value="">
                헌혈 종류를 선택해주세요
              </option>

              {donationTypes.map(
                (type) => (
                  <option
                    key={type.value}
                    value={type.value}
                  >
                    {type.label}
                  </option>
                )
              )}
            </select>
          </label>


          <button
            type="submit"
            disabled={loading}
          >
            {loading
              ? '조회 중...'
              : '예약 가능 시간 조회'}
          </button>
        </form>


        {error && (
          <p className="reservation-error">
            {error}
          </p>
        )}


        <div className="reservation-slot-area">

          {slots.length === 0 ? (
            <p>
              조건을 선택하면 예약 가능한 시간이 표시됩니다.
            </p>
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
                    onClick={() =>
                      setSelectedSlotId(
                        slot.id
                      )
                    }
                  >
                    <strong>
                      {slot.startTime}
                      {' ~ '}
                      {slot.endTime}
                    </strong>

                    <span>
                      잔여 {slot.remainingCapacity}명
                    </span>
                  </button>
                </article>
              )
            )
          )}

        </div>

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

      </section>

    </main>
  )
}


export default ReservationCreatePage