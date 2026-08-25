import {
  useEffect,
  useState,
} from 'react'

import {
  cancelMyReservation,
  changeMyReservationSlot,
  getMyReservations,
  getReservationSlots,
} from '../../api/apiClient.js'

import './ReservationListPage.css'


const donationTypeLabels = {
  WHOLE_BLOOD: '전혈',
  PLATELET: '혈소판',
  PLASMA: '혈장',
  PLATELET_PLASMA: '혈소판혈장',
}

const reservationStatusLabels = {
  RESERVED: '예약 완료',
  CANCELED: '예약 취소',
}


function ReservationListPage() {
  const [
    reservations,
    setReservations,
  ] = useState([])

  const [loading, setLoading] =
    useState(true)

  const [error, setError] =
    useState('')

  const [
    cancelingId,
    setCancelingId,
  ] = useState(null)

  const [
    successMessage,
    setSuccessMessage,
  ] = useState('')

const [
  editingReservationId,
  setEditingReservationId,
] = useState(null)

const [
  changeSlots,
  setChangeSlots,
] = useState([])

const [
  selectedChangeSlotId,
  setSelectedChangeSlotId,
] = useState(null)

const [
  changeLoading,
  setChangeLoading,
] = useState(false)

const [
  changingId,
  setChangingId,
] = useState(null)


  useEffect(() => {
    async function loadReservations() {
      try {
        setLoading(true)
        setError('')

        const data =
          await getMyReservations()

        setReservations(data)
      } catch (error) {
        setReservations([])

        setError(
          error.message
          || '예약 내역을 불러오지 못했습니다.'
        )
      } finally {
        setLoading(false)
      }
    }

    loadReservations()
  }, [])

  async function handleCancel(
    reservationId
  ) {
    const confirmed =
      window.confirm(
        '이 예약을 취소하시겠습니까?'
      )

    if (!confirmed) {
      return
    }

    try {
      setCancelingId(
        reservationId
      )

      setError('')
      setSuccessMessage('')

      await cancelMyReservation(
        reservationId
      )

      const refreshed =
        await getMyReservations()

      setReservations(
        refreshed
      )

      setSuccessMessage(
        '예약이 취소되었습니다.'
      )
    } catch (error) {
      setError(
        error.message
        || '예약 취소 중 오류가 발생했습니다.'
      )
    } finally {
      setCancelingId(null)
    }
  }

  async function handleOpenChange(
    reservation
  ) {
    if (
      editingReservationId
        === reservation.id
    ) {
      setEditingReservationId(null)
      setChangeSlots([])
      setSelectedChangeSlotId(null)

      return
    }

    try {
      setChangeLoading(true)
      setError('')
      setSuccessMessage('')
      setSelectedChangeSlotId(null)

      const data =
        await getReservationSlots(
          reservation.centerId,
          reservation.reservationDate,
          reservation.donationType
        )

      const availableSlots =
        data.filter(
          (slot) =>
            slot.id !== reservation.slotId
        )

      setChangeSlots(
        availableSlots
      )

      setEditingReservationId(
        reservation.id
      )
    } catch (error) {
      setChangeSlots([])

      setError(
        error.message
        || '변경 가능한 시간을 불러오지 못했습니다.'
      )
    } finally {
      setChangeLoading(false)
    }
  }

  async function handleChange(
    reservationId
  ) {
    if (!selectedChangeSlotId) {
      setError(
        '변경할 시간을 선택해주세요.'
      )

      return
    }

    try {
      setChangingId(
        reservationId
      )

      setError('')
      setSuccessMessage('')

      await changeMyReservationSlot(
        reservationId,
        selectedChangeSlotId
      )

      const refreshed =
        await getMyReservations()

      setReservations(
        refreshed
      )

      setEditingReservationId(null)
      setChangeSlots([])
      setSelectedChangeSlotId(null)

      setSuccessMessage(
        '예약 시간이 변경되었습니다.'
      )
    } catch (error) {
      setError(
        error.message
        || '예약 변경 중 오류가 발생했습니다.'
      )
    } finally {
      setChangingId(null)
    }
  }

  return (
    <main className="reservation-list-page">

      <section className="reservation-list-header">
        <span>
          MY RESERVATIONS
        </span>

        <h1>
          예약 내역
        </h1>

        <p>
          신청한 헌혈 예약을 확인할 수 있습니다.
        </p>
      </section>


      <section className="reservation-list-content">

        {loading && (
          <p>
            예약 내역을 불러오는 중입니다.
          </p>
        )}


        {error && (
          <p className="reservation-list-error">
            {error}
          </p>
        )}

        {successMessage && (
          <p className="reservation-list-success">
            {successMessage}
          </p>
        )}

        {!loading
          && !error
          && reservations.length === 0 && (
          <p>
            예약 내역이 없습니다.
          </p>
        )}


        {!loading
          && !error
          && reservations.length > 0 && (
          <div className="reservation-list">
            {reservations.map(
              (reservation) => (
                <article
                  key={reservation.id}
                  className="reservation-list-card"
                >
                  <div>
                    <strong>
                      {reservation.centerName}
                    </strong>

                    <span>
                      {
                        reservationStatusLabels[
                          reservation.status
                        ] || reservation.status
                      }
                    </span>
                  </div>

                  <dl>
                    <div>
                      <dt>
                        예약 날짜
                      </dt>

                      <dd>
                        {reservation.reservationDate}
                      </dd>
                    </div>

                    <div>
                      <dt>
                        예약 시간
                      </dt>

                      <dd>
                        {reservation.startTime}
                        {' ~ '}
                        {reservation.endTime}
                      </dd>
                    </div>

                    <div>
                      <dt>
                        헌혈 종류
                      </dt>

                      <dd>
                        {
                          donationTypeLabels[
                            reservation.donationType
                          ]
                          || reservation.donationType
                        }
                      </dd>
                    </div>
                  </dl>

                  {reservation.status === 'RESERVED' && (
                    <div className="reservation-action-area">

                      <button
                        type="button"
                        onClick={() =>
                          handleOpenChange(
                            reservation
                          )
                        }
                        disabled={changeLoading}
                        className="reservation-change-button"
                      >
                        {editingReservationId
                          === reservation.id
                          ? '변경 닫기'
                          : '예약 변경'}
                      </button>

                      <button
                        type="button"
                        onClick={() =>
                          handleCancel(
                            reservation.id
                          )
                        }
                        disabled={
                          cancelingId
                            === reservation.id
                        }
                        className="reservation-cancel-button"
                      >
                        {cancelingId
                          === reservation.id
                          ? '취소 처리 중...'
                          : '예약 취소'}
                      </button>

                    </div>
                  )}

                  {editingReservationId
                    === reservation.id && (
                    <div className="reservation-change-area">

                      {changeLoading ? (
                        <p>
                          변경 가능한 시간을
                          불러오는 중입니다.
                        </p>
                      ) : changeSlots.length === 0 ? (
                        <p>
                          변경 가능한 다른 시간이 없습니다.
                        </p>
                      ) : (
                        <>
                          <div className="reservation-change-slots">

                            {changeSlots.map(
                              (slot) => (
                                <button
                                  key={slot.id}
                                  type="button"
                                  onClick={() =>
                                    setSelectedChangeSlotId(
                                      slot.id
                                    )
                                  }
                                  className={
                                    selectedChangeSlotId
                                      === slot.id
                                      ? 'reservation-change-slot selected'
                                      : 'reservation-change-slot'
                                  }
                                >
                                  {slot.startTime}
                                  {' ~ '}
                                  {slot.endTime}

                                  <span>
                                    잔여 {slot.remainingCapacity}명
                                  </span>
                                </button>
                              )
                            )}

                          </div>

                          <button
                            type="button"
                            onClick={() =>
                              handleChange(
                                reservation.id
                              )
                            }
                            disabled={
                              !selectedChangeSlotId
                              || changingId
                                === reservation.id
                            }
                            className="reservation-change-confirm-button"
                          >
                            {changingId
                              === reservation.id
                              ? '변경 처리 중...'
                              : '선택한 시간으로 변경'}
                          </button>
                        </>
                      )}

                    </div>
                  )}

                </article>
              )
            )}
          </div>
        )}

      </section>

    </main>
  )
}


export default ReservationListPage