import {
  useEffect,
  useMemo,
  useState,
} from 'react'

import {
  cancelAdminReservation,
  changeAdminReservationSlot,
  createAdminReservationSlots,
  getAdminReservation,
  getAdminReservations,
  getAdminReservationSlots,
  getCenters,
  updateAdminReservationSlot,
} from '../../api/apiClient.js'

import './AdminReservationsPage.css'

import ConfirmModal from '../../components/ConfirmModal.jsx'


const reservationStatuses = [
  '전체',
  '예약완료',
  '취소',
]

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

function AdminReservationsPage() {
  const [keyword, setKeyword] = useState('')
  const [statusFilter, setStatusFilter] =
    useState('전체')

  const [reservations, setReservations] =
    useState([])

  const [reservationLoading, setReservationLoading] =
    useState(true)

  const [reservationError, setReservationError] =
    useState('')

  const [
    selectedReservation,
    setSelectedReservation,
  ] = useState(null)

  const [
    reservationDetailLoading,
    setReservationDetailLoading,
  ] = useState(false)

  const [
    reservationDetailError,
    setReservationDetailError,
  ] = useState('')

  const [
    reservationSlotOptions,
    setReservationSlotOptions,
  ] = useState([])

  const [
    selectedChangeSlotId,
    setSelectedChangeSlotId,
  ] = useState('')

  const [
    reservationSlotChanging,
    setReservationSlotChanging,
  ] = useState(false)

  const [
    reservationSlotChangeError,
    setReservationSlotChangeError,
  ] = useState('')

  const [
    cancelTargetReservation,
    setCancelTargetReservation,
  ] = useState(null)

  const [
    reservationCancelling,
    setReservationCancelling,
  ] = useState(false)

  const [centers, setCenters] =
    useState([])

  const [selectedCenterId, setSelectedCenterId] =
    useState('')

  const [selectedDate, setSelectedDate] =
    useState('')

  const [selectedDonationType, setSelectedDonationType] =
    useState('WHOLE_BLOOD')

  const [slots, setSlots] =
    useState([])

  const [slotLoading, setSlotLoading] =
    useState(false)

  const [slotError, setSlotError] =
    useState('')

  const [slotSearched, setSlotSearched] =
    useState(false)

  const [slotStartTime, setSlotStartTime] =
    useState('09:00')

  const [slotEndTime, setSlotEndTime] =
    useState('18:00')

  const [slotInterval, setSlotInterval] =
    useState(30)

  const [slotCapacity, setSlotCapacity] =
    useState(5)

  const [slotCreating, setSlotCreating] =
    useState(false)

  const [slotCapacityDrafts, setSlotCapacityDrafts] =
    useState({})

  const [updatingSlotId, setUpdatingSlotId] =
    useState(null)

  useEffect(() => {
    async function loadCenters() {
      try {
        const centerData =
          await getCenters()

        setCenters(centerData)
      } catch (error) {
        setSlotError(
          error.message
          || '헌혈센터 목록을 불러오지 못했습니다.'
        )
      }
    }

    loadCenters()
  }, [])

  useEffect(() => {
      async function loadReservations() {
        try {
          setReservationLoading(true)
          setReservationError('')

          const reservationData =
            await getAdminReservations()

          setReservations(reservationData)
        } catch (error) {
          setReservations([])

          setReservationError(
            error.message
            || '예약 목록을 불러오지 못했습니다.'
          )
        } finally {
          setReservationLoading(false)
        }
      }

      loadReservations()
    }, [])

const filteredReservations = useMemo(() => {
  const normalizedKeyword =
    keyword.trim().toLowerCase()

  return reservations.filter(
    (reservation) => {
      const matchesKeyword =
        !normalizedKeyword
        ||
        reservation.memberName
          .toLowerCase()
          .includes(normalizedKeyword)
        ||
        reservation.centerName
          .toLowerCase()
          .includes(normalizedKeyword)

      const statusLabel =
        reservation.status === 'RESERVED'
          ? '예약완료'
          : '취소'

      const matchesStatus =
        statusFilter === '전체'
        ||
        statusLabel === statusFilter

      return matchesKeyword && matchesStatus
    }
  )
}, [
  reservations,
  keyword,
  statusFilter,
])

  async function handleSlotSearch() {
    if (
      !selectedCenterId
      || !selectedDate
      || !selectedDonationType
    ) {
      setSlotError(
        '센터, 날짜, 헌혈종류를 모두 선택해주세요.'
      )

      return
    }

    try {
      setSlotLoading(true)
      setSlotError('')

      const slotData =
        await getAdminReservationSlots(
          selectedCenterId,
          selectedDate,
          selectedDonationType
        )

      setSlots(slotData)
      setSlotSearched(true)
    } catch (error) {
      setSlots([])
      setSlotSearched(true)

      setSlotError(
        error.message
        || '예약 슬롯을 불러오지 못했습니다.'
      )
    } finally {
      setSlotLoading(false)
    }
  }

async function handleSlotCreate() {
  if (
    !selectedCenterId
    || !selectedDate
    || !selectedDonationType
  ) {
    setSlotError(
      '센터, 날짜, 헌혈종류를 모두 선택해주세요.'
    )

    return
  }

  try {
    setSlotCreating(true)
    setSlotError('')

    await createAdminReservationSlots({
      centerId: Number(selectedCenterId),
      reservationDate: selectedDate,
      donationType: selectedDonationType,
      startTime: slotStartTime,
      endTime: slotEndTime,
      intervalMinutes: Number(slotInterval),
      capacity: Number(slotCapacity),
    })

    const slotData =
      await getAdminReservationSlots(
        selectedCenterId,
        selectedDate,
        selectedDonationType
      )

    setSlots(slotData)
    setSlotSearched(true)
  } catch (error) {
    setSlotError(
      error.message
      || '예약 슬롯 생성에 실패했습니다.'
    )
  } finally {
    setSlotCreating(false)
  }
}

async function handleSlotCapacityUpdate(slot) {
  const newCapacity =
    Number(
      slotCapacityDrafts[slot.id]
      ?? slot.capacity
    )

  if (newCapacity <= 0) {
    setSlotError(
      '예약 정원은 1명 이상이어야 합니다.'
    )

    return
  }

  if (newCapacity === slot.capacity) {
    return
  }

  try {
    setUpdatingSlotId(slot.id)
    setSlotError('')

    const updatedSlot =
      await updateAdminReservationSlot(
        slot.id,
        {
          capacity: newCapacity,
        }
      )

    setSlots((currentSlots) =>
      currentSlots.map((currentSlot) =>
        currentSlot.id === updatedSlot.id
          ? updatedSlot
          : currentSlot
      )
    )

    setSlotCapacityDrafts((currentDrafts) => {
      const nextDrafts = {
        ...currentDrafts,
      }

      delete nextDrafts[slot.id]

      return nextDrafts
    })
  } catch (error) {
    setSlotError(
      error.message
      || '예약 정원 변경에 실패했습니다.'
    )
  } finally {
    setUpdatingSlotId(null)
  }
}

async function handleSlotActiveToggle(slot) {
  try {
    setUpdatingSlotId(slot.id)
    setSlotError('')

    const updatedSlot =
      await updateAdminReservationSlot(
        slot.id,
        {
          active: !slot.active,
        }
      )

    setSlots((currentSlots) =>
      currentSlots.map((currentSlot) =>
        currentSlot.id === updatedSlot.id
          ? updatedSlot
          : currentSlot
      )
    )
  } catch (error) {
    setSlotError(
      error.message
      || '예약 슬롯 상태 변경에 실패했습니다.'
    )
  } finally {
    setUpdatingSlotId(null)
  }
}

async function handleReservationDetail(
  reservationId
) {
  try {
    setReservationDetailLoading(true)
    setReservationDetailError('')
    setReservationSlotChangeError('')
    setReservationSlotOptions([])
    setSelectedChangeSlotId('')

    const reservation =
      await getAdminReservation(
        reservationId
      )

    setSelectedReservation(
      reservation
    )

    const slotData =
      await getAdminReservationSlots(
        reservation.centerId,
        reservation.reservationDate,
        reservation.donationType
      )

    setReservationSlotOptions(
      slotData
    )
  } catch (error) {
    setReservationDetailError(
      error.message
      || '예약 상세 정보를 불러오지 못했습니다.'
    )
  } finally {
    setReservationDetailLoading(false)
  }
}

async function handleReservationSlotChange() {
  if (
    !selectedReservation
    || !selectedChangeSlotId
  ) {
    setReservationSlotChangeError(
      '변경할 예약 시간을 선택해주세요.'
    )

    return
  }

  try {
    setReservationSlotChanging(true)
    setReservationSlotChangeError('')

    const updatedReservation =
      await changeAdminReservationSlot(
        selectedReservation.id,
        Number(selectedChangeSlotId)
      )

    /*
     * 상세 패널 즉시 갱신
     */
    setSelectedReservation(
      updatedReservation
    )

    /*
     * 하단 ADMIN 예약 목록도 즉시 갱신
     */
    setReservations(
      (currentReservations) =>
        currentReservations.map(
          (reservation) =>
            reservation.id
              === updatedReservation.id
              ? updatedReservation
              : reservation
        )
    )

    /*
     * 현재 위쪽 슬롯 관리 영역에서
     * 같은 센터/날짜/헌혈종류를 보고 있다면
     * 점유 수도 즉시 다시 불러옵니다.
     */
    if (
      String(selectedCenterId)
        === String(updatedReservation.centerId)
      &&
      selectedDate
        === updatedReservation.reservationDate
      &&
      selectedDonationType
        === updatedReservation.donationType
      &&
      slotSearched
    ) {
      const slotData =
        await getAdminReservationSlots(
          selectedCenterId,
          selectedDate,
          selectedDonationType
        )

      setSlots(slotData)
    }

    /*
     * 상세 패널 슬롯 후보도 다시 조회해서
     * 점유/잔여 정보 최신화
     */
    const refreshedSlotOptions =
      await getAdminReservationSlots(
        updatedReservation.centerId,
        updatedReservation.reservationDate,
        updatedReservation.donationType
      )

    setReservationSlotOptions(
      refreshedSlotOptions
    )

    setSelectedChangeSlotId('')
  } catch (error) {
    setReservationSlotChangeError(
      error.message
      || '예약 시간 변경에 실패했습니다.'
    )
  } finally {
    setReservationSlotChanging(false)
  }
}

async function handleReservationCancel() {
  if (!cancelTargetReservation) {
    return
  }

  try {
    setReservationCancelling(true)
    setReservationError('')

    const updatedReservation =
      await cancelAdminReservation(
        cancelTargetReservation.id
      )

    setReservations((currentReservations) =>
      currentReservations.map((reservation) =>
        reservation.id === updatedReservation.id
          ? updatedReservation
          : reservation
      )
    )

    /*
     * 현재 조회 중인 슬롯이 있다면
     * 취소 직후 점유 현황도 다시 조회합니다.
     */
    if (
      selectedCenterId
      && selectedDate
      && selectedDonationType
      && slotSearched
    ) {
      const slotData =
        await getAdminReservationSlots(
          selectedCenterId,
          selectedDate,
          selectedDonationType
        )

      setSlots(slotData)
    }

    setCancelTargetReservation(null)
  } catch (error) {
    setReservationError(
      error.message
      || '예약 취소에 실패했습니다.'
    )
  } finally {
    setReservationCancelling(false)
  }
}

  return (
    <main className="admin-reservations-page">

      <section className="admin-reservations-header">

        <div>
          <span className="admin-page-eyebrow">
            ADMIN / RESERVATION
          </span>

          <h1>
            예약 관리
          </h1>

          <p>
            헌혈 예약 현황을 조회하고
            예약 상태를 관리합니다.
          </p>
        </div>

      </section>

      <section className="admin-slot-management">

      <div className="admin-slot-management-header">
        <div>
          <span className="admin-page-eyebrow">
            RESERVATION SLOT
          </span>

          <h2>
            예약 슬롯 관리
          </h2>

          <p>
            센터별 예약 가능 시간과 정원을 관리합니다.
          </p>
        </div>
      </div>


      <div className="admin-slot-search">

        <select
          value={selectedCenterId}
          onChange={(event) =>
            setSelectedCenterId(event.target.value)
          }
        >
          <option value="">
            헌혈센터 선택
          </option>

          {centers.map((center) => (
            <option
              key={center.id}
              value={center.id}
            >
              {center.centerName}
            </option>
          ))}
        </select>


        <input
          type="date"
          value={selectedDate}
          onChange={(event) =>
            setSelectedDate(event.target.value)
          }
        />


        <select
          value={selectedDonationType}
          onChange={(event) =>
            setSelectedDonationType(
              event.target.value
            )
          }
        >
          {donationTypes.map((type) => (
            <option
              key={type.value}
              value={type.value}
            >
              {type.label}
            </option>
          ))}
        </select>


        <button
          type="button"
          className="admin-row-action"
          onClick={handleSlotSearch}
          disabled={slotLoading}
        >
          {slotLoading
            ? '조회 중...'
            : '슬롯 조회'}
        </button>

      </div>

      <div className="admin-slot-create">

        <label>
          시작 시간
          <input
            type="time"
            value={slotStartTime}
            onChange={(event) =>
              setSlotStartTime(event.target.value)
            }
          />
        </label>

        <label>
          종료 시간
          <input
            type="time"
            value={slotEndTime}
            onChange={(event) =>
              setSlotEndTime(event.target.value)
            }
          />
        </label>

        <label>
          간격(분)
          <input
            type="number"
            min="1"
            value={slotInterval}
            onChange={(event) =>
              setSlotInterval(event.target.value)
            }
          />
        </label>

        <label>
          기본 정원
          <input
            type="number"
            min="1"
            value={slotCapacity}
            onChange={(event) =>
              setSlotCapacity(event.target.value)
            }
          />
        </label>

        <button
          type="button"
          className="admin-row-action"
          onClick={handleSlotCreate}
          disabled={slotCreating}
        >
          {slotCreating
            ? '생성 중...'
            : '슬롯 일괄 생성'}
        </button>

      </div>

      {slotError && (
        <p className="admin-slot-error">
          {slotError}
        </p>
      )}


      <div className="admin-slot-list">

        {slots.length === 0
          && !slotLoading
          && !slotError && (
            <p className="admin-slot-empty">
              {slotSearched
                ? '등록된 예약 슬롯이 없습니다.'
                : '조회할 센터와 날짜를 선택해주세요.'}
            </p>
        )}


        {slots.map((slot) => (
          <article
            key={slot.id}
            className="admin-slot-row"
          >
            <strong>
              {slot.startTime.slice(0, 5)}
              {' ~ '}
              {slot.endTime.slice(0, 5)}
            </strong>

            <div className="admin-slot-occupancy">
              <strong>
                {slot.reservedCount}
                {' / '}
                {slot.capacity}
              </strong>

              <span>
                잔여 {slot.remainingCapacity}명
              </span>
            </div>

            <div className="admin-slot-capacity">
              <span>
                정원
              </span>

              <input
                type="number"
                min="1"
                value={
                  slotCapacityDrafts[slot.id]
                  ?? slot.capacity
                }
                onChange={(event) =>
                  setSlotCapacityDrafts(
                    (currentDrafts) => ({
                      ...currentDrafts,
                      [slot.id]: event.target.value,
                    })
                  )
                }
                disabled={
                  updatingSlotId === slot.id
                }
              />

              <button
                type="button"
                className="admin-row-action"
                onClick={() =>
                  handleSlotCapacityUpdate(slot)
                }
                disabled={
                  updatingSlotId === slot.id
                  ||
                  Number(
                    slotCapacityDrafts[slot.id]
                    ?? slot.capacity
                  ) === slot.capacity
                }
              >
                저장
              </button>
            </div>


            <span
              className={
                slot.active
                  ? 'admin-slot-state active'
                  : 'admin-slot-state inactive'
              }
            >
              {slot.active
                ? '예약 가능'
                : '예약 중지'}
            </span>


            <button
              type="button"
              className="admin-row-action"
              onClick={() =>
                handleSlotActiveToggle(slot)
              }
              disabled={
                updatingSlotId === slot.id
              }
            >
              {updatingSlotId === slot.id
                ? '처리 중'
                : slot.active
                  ? '예약 중지'
                  : '예약 재개'}
            </button>
          </article>
        ))}

      </div>

            </section>


      <section className="admin-reservation-toolbar">

        <div className="admin-reservation-search">
          <input
            type="search"
            value={keyword}
            onChange={(event) =>
              setKeyword(event.target.value)
            }
            placeholder="회원명 또는 센터명 검색"
            aria-label="예약 검색"
          />
        </div>


        <div
          className="admin-reservation-filter"
          aria-label="예약 상태 필터"
        >
          {reservationStatuses.map((status) => (
            <button
              key={status}
              type="button"
              className={
                statusFilter === status
                  ? 'active'
                  : ''
              }
              onClick={() =>
                setStatusFilter(status)
              }
            >
              {status}
            </button>
          ))}
        </div>


        <span className="admin-reservation-count">
          총 {filteredReservations.length}건
        </span>

      </section>


      <section className="admin-reservation-list">

        <div className="admin-reservation-list-header">
          <span>회원</span>
          <span>헌혈센터</span>
          <span>예약일</span>
          <span>시간</span>
          <span>헌혈종류</span>
          <span>상태</span>
          <span>관리</span>
        </div>

        {reservationLoading && (
          <p className="admin-slot-empty">
            예약 목록을 불러오는 중입니다.
          </p>
        )}

        {reservationError && (
          <p className="admin-slot-error">
            {reservationError}
          </p>
        )}

        {!reservationLoading
          && !reservationError
          && filteredReservations.length === 0 && (
            <p className="admin-slot-empty">
              조회된 예약이 없습니다.
            </p>
        )}

        {filteredReservations.map(
          (reservation) => (
            <article
              key={reservation.id}
              className="admin-reservation-row"
            >
              <strong>
                {reservation.memberName}
              </strong>

              <span>
                {reservation.centerName}
              </span>

              <span>
                {reservation.reservationDate}
              </span>

              <span>
                {reservation.startTime?.slice(0, 5)}
              </span>

              <span>
                {
                  donationTypes.find(
                    (type) =>
                      type.value === reservation.donationType
                  )?.label
                  ?? reservation.donationType
                }
              </span>

              <span
                className={
                  reservation.status === 'RESERVED'
                    ? 'admin-reservation-status confirmed'
                    : 'admin-reservation-status cancelled'
                }
              >
                {reservation.status === 'RESERVED'
                  ? '예약완료'
                  : '취소'}
              </span>

              <div className="admin-reservation-actions">
                <button
                  type="button"
                  className="admin-row-action"
                  onClick={() =>
                    handleReservationDetail(
                      reservation.id
                    )
                  }
                >
                  상세
                </button>

                {reservation.status === 'RESERVED' && (
                  <button
                    type="button"
                    className="admin-row-action"
                    onClick={() =>
                      setCancelTargetReservation(
                        reservation
                      )
                    }
                  >
                    취소
                  </button>
                )}
              </div>

            </article>
          )
        )}

      </section>

    {(
      selectedReservation
      || reservationDetailLoading
      || reservationDetailError
    ) && (
      <div
        className="admin-reservation-detail-backdrop"
        onClick={() => {
          if (!reservationDetailLoading) {
            setSelectedReservation(null)
            setReservationDetailError('')
          }
        }}
      >
        <aside
          className="admin-reservation-detail-panel"
          onClick={(event) =>
            event.stopPropagation()
          }
        >
          <div className="admin-reservation-detail-header">
            <div>
              <span className="admin-page-eyebrow">
                RESERVATION DETAIL
              </span>

              <h2>
                예약 상세
              </h2>
            </div>

            <button
              type="button"
              className="admin-row-action"
              onClick={() => {
                setSelectedReservation(null)
                setReservationDetailError('')
              }}
              disabled={reservationDetailLoading}
            >
              닫기
            </button>
          </div>

          {reservationDetailLoading && (
            <p className="admin-slot-empty">
              예약 정보를 불러오는 중입니다.
            </p>
          )}

          {reservationDetailError && (
            <p className="admin-slot-error">
              {reservationDetailError}
            </p>
          )}

          {selectedReservation
            && !reservationDetailLoading && (
            <div className="admin-reservation-detail-body">

              <div className="admin-reservation-detail-item">
                <span>예약 번호</span>
                <strong>
                  {selectedReservation.id}
                </strong>
              </div>

              <div className="admin-reservation-detail-item">
                <span>회원명</span>
                <strong>
                  {selectedReservation.memberName}
                </strong>
              </div>

              <div className="admin-reservation-detail-item">
                <span>이메일</span>
                <strong>
                  {selectedReservation.memberEmail}
                </strong>
              </div>

              <div className="admin-reservation-detail-item">
                <span>헌혈센터</span>
                <strong>
                  {selectedReservation.centerName}
                </strong>
              </div>

              <div className="admin-reservation-detail-item">
                <span>예약일</span>
                <strong>
                  {selectedReservation.reservationDate}
                </strong>
              </div>

              <div className="admin-reservation-detail-item">
                <span>예약 시간</span>
                <strong>
                  {selectedReservation.startTime?.slice(0, 5)}
                  {' ~ '}
                  {selectedReservation.endTime?.slice(0, 5)}
                </strong>
              </div>

              <div className="admin-reservation-detail-item">
                <span>헌혈 종류</span>
                <strong>
                  {
                    donationTypes.find(
                      (type) =>
                        type.value
                        === selectedReservation.donationType
                    )?.label
                    ?? selectedReservation.donationType
                  }
                </strong>
              </div>

              <div className="admin-reservation-detail-item">
                <span>상태</span>
                <strong>
                  {selectedReservation.status === 'RESERVED'
                    ? '예약완료'
                    : '취소'}
                </strong>
              </div>

              <div className="admin-reservation-detail-item">
                <span>슬롯 ID</span>
                <strong>
                  {selectedReservation.slotId}
                </strong>
              </div>

              {selectedReservation.status === 'RESERVED' && (
                <div className="admin-reservation-slot-change">

                  <div className="admin-reservation-slot-change-header">
                    <strong>
                      예약 시간 변경
                    </strong>

                    <span>
                      현재 {selectedReservation.startTime?.slice(0, 5)}
                      {' ~ '}
                      {selectedReservation.endTime?.slice(0, 5)}
                    </span>
                  </div>

                  <select
                    value={selectedChangeSlotId}
                    onChange={(event) =>
                      setSelectedChangeSlotId(
                        event.target.value
                      )
                    }
                    disabled={reservationSlotChanging}
                  >
                    <option value="">
                      변경할 시간 선택
                    </option>

                    {reservationSlotOptions
                      .filter(
                        (slot) =>
                          slot.id !== selectedReservation.slotId
                          && slot.active
                      )
                      .map((slot) => (
                        <option
                          key={slot.id}
                          value={slot.id}
                          disabled={
                            slot.reservedCount >= slot.capacity
                          }
                        >
                          {slot.startTime.slice(0, 5)}
                          {' ~ '}
                          {slot.endTime.slice(0, 5)}
                          {' / '}
                          잔여 {slot.remainingCapacity}명
                        </option>
                      ))}
                  </select>

                  {reservationSlotChangeError && (
                    <p className="admin-slot-error">
                      {reservationSlotChangeError}
                    </p>
                  )}

                  <button
                    type="button"
                    className="admin-row-action"
                    onClick={handleReservationSlotChange}
                    disabled={
                      !selectedChangeSlotId
                      || reservationSlotChanging
                    }
                  >
                    {reservationSlotChanging
                      ? '변경 중...'
                      : '예약 시간 변경'}
                  </button>

                </div>
              )}

            </div>
          )}
        </aside>
      </div>
    )}

    <ConfirmModal
    open={Boolean(cancelTargetReservation)}
    title="예약을 취소하시겠습니까?"
    message={
      cancelTargetReservation
        ? `${cancelTargetReservation.memberName}님의 `
          + `${cancelTargetReservation.reservationDate} `
          + `${cancelTargetReservation.startTime?.slice(0, 5)} `
          + '예약을 취소합니다.'
        : ''
    }
    confirmText={
      reservationCancelling
        ? '취소 처리 중...'
        : '예약 취소'
    }
    cancelText="닫기"
    danger
    onConfirm={handleReservationCancel}
    onCancel={() => {
      if (!reservationCancelling) {
        setCancelTargetReservation(null)
      }
    }}
  />

    </main>
  )
}


export default AdminReservationsPage