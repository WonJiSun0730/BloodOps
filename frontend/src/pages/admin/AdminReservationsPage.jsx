import {
  useEffect,
  useState,
} from 'react'

import {
  cancelAdminReservation,
  changeAdminReservationSlot,
  createAdminReservationSlots,
  deleteAdminReservationSlot,
  getAdminEquipmentGroups,
  getAdminReservation,
  getAdminReservations,
  getAdminReservationSlots,
  getCenters,
  updateAdminEquipmentGroup,
  updateAdminReservationSlot,
} from '../../api/apiClient.js'

import './AdminReservationsPage.css'

import ConfirmModal from '../../components/modal/ConfirmModal.jsx'

import AdminSectionNav
  from '../../components/admin/AdminSectionNav.jsx'


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

function getTodayDateString() {
  const now = new Date()

  const year = now.getFullYear()
  const month =
    String(now.getMonth() + 1).padStart(2, '0')
  const day =
    String(now.getDate()).padStart(2, '0')

  return `${year}-${month}-${day}`
}

function getReservationStatusValue(statusFilter) {
  if (statusFilter === '예약완료') {
    return 'RESERVED'
  }

  if (statusFilter === '취소') {
    return 'CANCELED'
  }

  return ''
}

function AdminReservationsPage() {
  const todayDate = getTodayDateString()

  const [keyword, setKeyword] = useState('')
  const [statusFilter, setStatusFilter] =
    useState('전체')

  const [reservations, setReservations] =
    useState([])

  const [reservationLoading, setReservationLoading] =
    useState(true)

  const [reservationPage, setReservationPage] =
    useState(0)

  const [reservationLastPage, setReservationLastPage] =
    useState(false)

  const [totalReservations, setTotalReservations] =
    useState(0)

  const [
    reservationLoadingMore,
    setReservationLoadingMore,
  ] = useState(false)

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

const [
  deleteTargetSlot,
  setDeleteTargetSlot,
] = useState(null)

const [
  slotDeleting,
  setSlotDeleting,
] = useState(false)

const [
  slotDeleteError,
  setSlotDeleteError,
] = useState('')

  const [centers, setCenters] =
    useState([])

  const [selectedCenterId, setSelectedCenterId] =
    useState('')

  const [centerSearchText, setCenterSearchText] =
    useState('')

  const [centerSearchOpen, setCenterSearchOpen] =
    useState(false)

  const [equipmentGroups, setEquipmentGroups] =
    useState([])

  const [equipmentGroupLoading, setEquipmentGroupLoading] =
    useState(false)

  const [equipmentGroupError, setEquipmentGroupError] =
    useState('')

  const [
    equipmentGroupCapacityDrafts,
    setEquipmentGroupCapacityDrafts,
  ] = useState({})

  const [
    updatingEquipmentGroupId,
    setUpdatingEquipmentGroupId,
  ] = useState(null)

  const [selectedDate, setSelectedDate] =
    useState('')

    function isPastSlot(slot) {
      if (!selectedDate || !slot?.startTime) {
        return false
      }

      const [
        year,
        month,
        day,
      ] = selectedDate
        .split('-')
        .map(Number)

      const [
        hour,
        minute,
      ] = slot.startTime
        .slice(0, 5)
        .split(':')
        .map(Number)

      const slotDateTime =
        new Date(
          year,
          month - 1,
          day,
          hour,
          minute
        )

      return slotDateTime < new Date()
    }

  const [selectedDonationType, setSelectedDonationType] =
    useState('WHOLE_BLOOD')

  const filteredCenters =
    centers.filter((center) =>
      center.centerName
        ?.toLowerCase()
        .includes(
          centerSearchText
            .trim()
            .toLowerCase()
        )
    )

  const [slots, setSlots] =
    useState([])

  const [slotLoading, setSlotLoading] =
    useState(false)

  const [slotError, setSlotError] =
    useState('')

  const [
    duplicateSlotTimes,
    setDuplicateSlotTimes,
  ] = useState([])

  const [slotSearched, setSlotSearched] =
    useState(false)

  const [slotStartTime, setSlotStartTime] =
    useState('09:00')

  const [slotEndTime, setSlotEndTime] =
    useState('18:00')

  const [slotInterval, setSlotInterval] =
    useState(30)

  const [slotCreating, setSlotCreating] =
    useState(false)

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

  async function loadEquipmentGroups(
    centerId
  ) {
    if (!centerId) {
      setEquipmentGroups([])
      setEquipmentGroupCapacityDrafts({})
      setEquipmentGroupError('')
      return
    }

    try {
      setEquipmentGroupLoading(true)
      setEquipmentGroupError('')

      const data =
        await getAdminEquipmentGroups(
          centerId
        )

      setEquipmentGroups(data)

      setEquipmentGroupCapacityDrafts(
        Object.fromEntries(
          data.map((group) => [
            group.id,
            group.capacity,
          ])
        )
      )
    } catch (error) {
      setEquipmentGroups([])
      setEquipmentGroupCapacityDrafts({})

      setEquipmentGroupError(
        error.message
        || '장비 정보를 불러오지 못했습니다.'
      )
    } finally {
      setEquipmentGroupLoading(false)
    }
  }

  async function handleEquipmentGroupUpdate(
    equipmentGroup
  ) {
    const newCapacity =
      Number(
        equipmentGroupCapacityDrafts[
          equipmentGroup.id
        ]
        ?? equipmentGroup.capacity
      )

    if (
      !Number.isInteger(newCapacity)
      || newCapacity < 0
    ) {
      setEquipmentGroupError(
        '장비 수량은 0대 이상의 정수여야 합니다.'
      )

      return
    }

    if (
      newCapacity
      === equipmentGroup.capacity
    ) {
      return
    }

    try {
      setUpdatingEquipmentGroupId(
        equipmentGroup.id
      )

      setEquipmentGroupError('')

      const updatedGroup =
        await updateAdminEquipmentGroup(
          equipmentGroup.id,
          {
            capacity: newCapacity,
          }
        )

      setEquipmentGroups(
        (currentGroups) =>
          currentGroups.map(
            (currentGroup) =>
              currentGroup.id === updatedGroup.id
                ? updatedGroup
                : currentGroup
          )
      )

      setEquipmentGroupCapacityDrafts(
        (currentDrafts) => ({
          ...currentDrafts,
          [updatedGroup.id]:
            updatedGroup.capacity,
        })
      )

    } catch (error) {
      setEquipmentGroupError(
        error.message
        || '장비 수량 변경에 실패했습니다.'
      )
    } finally {
      setUpdatingEquipmentGroupId(null)
    }
  }

  async function handleEquipmentGroupActiveToggle(
    equipmentGroup
  ) {
    try {
      setUpdatingEquipmentGroupId(
        equipmentGroup.id
      )

      setEquipmentGroupError('')

      const updatedGroup =
        await updateAdminEquipmentGroup(
          equipmentGroup.id,
          {
            active: !equipmentGroup.active,
          }
        )

      setEquipmentGroups(
        (currentGroups) =>
          currentGroups.map(
            (currentGroup) =>
              currentGroup.id === updatedGroup.id
                ? updatedGroup
                : currentGroup
          )
      )

    } catch (error) {
      setEquipmentGroupError(
        error.message
        || '장비 운영 상태 변경에 실패했습니다.'
      )
    } finally {
      setUpdatingEquipmentGroupId(null)
    }
  }

  async function loadReservations(
    searchKeyword = '',
    filter = '전체',
    page = 0,
    append = false
  ) {
    if (append) {
      setReservationLoadingMore(true)
    } else {
      setReservationLoading(true)
    }

    setReservationError('')

    try {
      const status =
        getReservationStatusValue(filter)

      const data =
        await getAdminReservations(
          searchKeyword,
          status,
          page,
          10
        )

      if (append) {
        setReservations((current) => [
          ...current,
          ...data.content,
        ])
      } else {
        setReservations(data.content)
      }

      setReservationPage(data.number)
      setReservationLastPage(data.last)
      setTotalReservations(data.totalElements)
    } catch (error) {
      if (!append) {
        setReservations([])
      }

      setReservationError(
        error.message
        || '예약 목록을 불러오지 못했습니다.'
      )
    } finally {
      setReservationLoading(false)
      setReservationLoadingMore(false)
    }
  }

useEffect(() => {
  const timer = setTimeout(() => {
    loadReservations(
      keyword,
      statusFilter,
      0,
      false
    )
  }, 300)

  return () => {
    clearTimeout(timer)
  }
}, [keyword, statusFilter])

function handleReservationListScroll(event) {
    if (
      reservationLoading
      || reservationLoadingMore
      || reservationLastPage
    ) {
      return
    }

    const element = event.currentTarget

    const distanceFromBottom =
      element.scrollHeight
      - element.scrollTop
      - element.clientHeight

    if (distanceFromBottom > 80) {
      return
    }

    loadReservations(
      keyword,
      statusFilter,
      reservationPage + 1,
      true
    )
  }

  async function handleSlotSearch() {
    if (selectedDate && selectedDate < todayDate) {
        setSlotError(
          '지난 날짜의 예약 슬롯은 조회하거나 수정할 수 없습니다.'
        )

        return
      }

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
  if (selectedDate && selectedDate < todayDate) {
      setSlotError(
        '지난 날짜에는 예약 슬롯을 생성할 수 없습니다.'
      )

      return
    }

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

  setDuplicateSlotTimes([])

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
    })

    const slotData =
      await getAdminReservationSlots(
        selectedCenterId,
        selectedDate,
        selectedDonationType
      )

    setSlots(slotData)
    setSlotSearched(true)
    setDuplicateSlotTimes([])
  } catch (error) {
      setDuplicateSlotTimes(
        error.duplicateTimes ?? []
      )

      setSlotError(
        error.message
        || '예약 슬롯 생성에 실패했습니다.'
      )
    } finally {
    setSlotCreating(false)
  }
}

async function handleSlotActiveToggle(slot) {
  if (isPastSlot(slot)) {
      setSlotError(
        '이미 지난 예약 슬롯의 상태는 변경할 수 없습니다.'
      )

      return
    }

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

async function handleSlotDelete() {
  if (!deleteTargetSlot) {
    return
  }

  try {
    setSlotDeleting(true)
    setSlotDeleteError('')

    await deleteAdminReservationSlot(
      deleteTargetSlot.id
    )

    setSlots(
      (currentSlots) =>
        currentSlots.filter(
          (currentSlot) =>
            currentSlot.id
            !== deleteTargetSlot.id
        )
    )

    setDeleteTargetSlot(null)

  } catch (error) {
    setSlotDeleteError(
      error.message
      || '예약 슬롯 삭제에 실패했습니다.'
    )
  } finally {
    setSlotDeleting(false)
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

      <AdminSectionNav />

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

        <div className="admin-center-combobox">

          <input
            type="text"
            value={centerSearchText}
            placeholder="헌혈센터 검색"
            autoComplete="off"
            onFocus={() => {
              setCenterSearchOpen(true)
            }}
            onChange={(event) => {
              setCenterSearchText(
                event.target.value
              )

              // 직접 검색어를 수정하면
              // 기존 센터 선택은 해제
              setSelectedCenterId('')
              setCenterSearchOpen(true)

              // 기존 슬롯 조회 결과도 초기화
              setSlots([])
              setSlotSearched(false)
              setSlotError('')
              setDuplicateSlotTimes([])

              //기존 센터의 장비그룹 정보도 초기화
              setEquipmentGroups([])
              setEquipmentGroupCapacityDrafts({})
              setEquipmentGroupError('')
            }}

          />

          {centerSearchOpen && (
            <div className="admin-center-options">

              {filteredCenters.length === 0 ? (
                <div className="admin-center-option-empty">
                  검색 결과가 없습니다.
                </div>
              ) : (
                filteredCenters.map((center) => {
                  const inactive =
                    center.active === false

                  return (
                    <button
                      key={center.id}
                      type="button"
                      className={
                        `admin-center-option${
                          inactive
                            ? ' is-inactive'
                            : ''
                        }`
                      }
                      disabled={inactive}
                      onClick={() => {
                        setSelectedCenterId(
                          String(center.id)
                        )

                        setCenterSearchText(
                          center.centerName
                        )

                        setCenterSearchOpen(false)

                        setSlots([])
                        setSlotSearched(false)
                        setSlotError('')
                        setDuplicateSlotTimes([])

                        loadEquipmentGroups(
                          center.id
                        )
                      }}
                    >
                      <span>
                        {center.centerName}
                      </span>

                      {inactive && (
                        <span className="admin-center-inactive-label">
                          (운영 중단)
                        </span>
                      )}
                    </button>
                  )
                })
              )}

            </div>
          )}

        </div>


        <input
          type="date"
          value={selectedDate}
          min={todayDate}
          onChange={(event) => {
            setSelectedDate(
              event.target.value
            )

            setSlots([])
            setSlotSearched(false)
            setSlotError('')
            setDuplicateSlotTimes([])
          }}
        />


        <select
          value={selectedDonationType}
          onChange={(event) => {
            setSelectedDonationType(
              event.target.value
            )

            setSlots([])
            setSlotSearched(false)
            setSlotError('')
            setDuplicateSlotTimes([])
          }}
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

      {selectedCenterId && (
        <div className="admin-equipment-groups">

          <div className="admin-equipment-groups-header">
            <div>
              <strong>
                공유 장비 관리
              </strong>

              <span>
                같은 장비를 사용하는 헌혈 종류는
                장비 수량을 함께 사용합니다.
              </span>
            </div>
          </div>


          {equipmentGroupLoading && (
            <p className="admin-slot-empty">
              장비 정보를 불러오는 중입니다.
            </p>
          )}


          {equipmentGroupError && (
            <p className="admin-slot-error">
              {equipmentGroupError}
            </p>
          )}


          {!equipmentGroupLoading
            && !equipmentGroupError
            && equipmentGroups.length === 0 && (
              <p className="admin-slot-empty">
                등록된 장비그룹이 없습니다.
              </p>
          )}


          {!equipmentGroupLoading
            && equipmentGroups.map(
              (equipmentGroup) => {

                const groupLabel =
                  equipmentGroup.groupType
                    === 'WHOLE_BLOOD_PLASMA'
                    ? '전혈 · 혈장 공용 장비'
                    : '혈소판 · 혈소판혈장 공용 장비'

                const draftCapacity =
                  equipmentGroupCapacityDrafts[
                    equipmentGroup.id
                  ]
                  ?? equipmentGroup.capacity

                const invalidCapacity =
                  !Number.isInteger(
                    Number(draftCapacity)
                  )
                  || Number(draftCapacity) < 0

                const capacityChanged =
                  Number(draftCapacity)
                  !== equipmentGroup.capacity

                const updating =
                  updatingEquipmentGroupId
                  === equipmentGroup.id

                return (
                  <div
                    key={equipmentGroup.id}
                    className="admin-equipment-group-row"
                  >

                    <div className="admin-equipment-group-info">

                      <strong>
                        {groupLabel}
                      </strong>

                      <span>
                        장비 점유시간은 헌혈 종류별
                        정책값으로 자동 계산됩니다.
                      </span>

                    </div>


                    <div className="admin-equipment-group-capacity">

                      <span>
                        장비 수량
                      </span>

                      <input
                        type="number"
                        min="0"
                        step="1"
                        value={draftCapacity}
                        className={
                          invalidCapacity
                            ? 'is-invalid'
                            : ''
                        }
                        onChange={(event) => {
                          setEquipmentGroupCapacityDrafts(
                            (currentDrafts) => ({
                              ...currentDrafts,
                              [equipmentGroup.id]:
                                event.target.value,
                            })
                          )
                        }}
                        disabled={updating}
                      />

                      <span>
                        대
                      </span>

                      <button
                        type="button"
                        className="admin-row-action"
                        onClick={() =>
                          handleEquipmentGroupUpdate(
                            equipmentGroup
                          )
                        }
                        disabled={
                          updating
                          || invalidCapacity
                          || !capacityChanged
                        }
                      >
                        {updating
                          ? '저장 중'
                          : '저장'}
                      </button>

                    </div>


                    <span
                      className={
                        equipmentGroup.active
                          ? 'admin-slot-state active'
                          : 'admin-slot-state inactive'
                      }
                    >
                      {equipmentGroup.active
                        ? '운영 중'
                        : '운영 중단'}
                    </span>


                    <button
                      type="button"
                      className="admin-row-action"
                      onClick={() =>
                        handleEquipmentGroupActiveToggle(
                          equipmentGroup
                        )
                      }
                      disabled={updating}
                    >
                      {updating
                        ? '처리 중'
                        : equipmentGroup.active
                          ? '운영 중단'
                          : '운영 재개'}
                    </button>

                  </div>
                )
              }
            )}

        </div>
      )}

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
            className={
              `admin-slot-row${
                duplicateSlotTimes.includes(
                  slot.startTime.slice(0, 5)
                )
                  ? ' is-duplicate'
                  : ''
              }`
            }
          >
            <strong>
              {slot.startTime.slice(0, 5)}
              {' ~ '}
              {slot.endTime.slice(0, 5)}
            </strong>

            <div className="admin-slot-occupancy">
              <strong>
                장비 점유 {slot.reservedCount}
                {' / '}
                {slot.capacity}
              </strong>

              <span>
                가용 장비 {slot.remainingCapacity}대
              </span>
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


            <div className="admin-slot-actions">

              <button
                type="button"
                className="admin-row-action"
                onClick={() =>
                  handleSlotActiveToggle(slot)
                }
                disabled={
                  updatingSlotId === slot.id
                  || isPastSlot(slot)
                }
              >
                {updatingSlotId === slot.id
                  ? '처리 중'
                  : slot.active
                    ? '예약 중지'
                    : '예약 재개'}
              </button>

              <button
                type="button"
                className="admin-slot-delete-button"
                onClick={() => {
                  setSlotDeleteError('')
                  setDeleteTargetSlot(slot)
                }}
                disabled={slot.hasReservationHistory}
                aria-label={`${slot.startTime.slice(0, 5)} 예약 슬롯 삭제`}
                title={
                  slot.hasReservationHistory
                    ? '예약 이력이 있는 슬롯은 삭제할 수 없습니다.'
                    : '예약 슬롯 삭제'
                }
              >
                <svg
                  viewBox="0 0 24 24"
                  aria-hidden="true"
                >
                  <path
                    d="M9 3h6M7 7h10M8 7l.7 13h6.6L16 7M10 10v7M14 10v7"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="1.8"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  />
                </svg>
              </button>

            </div>
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
          총 {totalReservations}건
        </span>

      </section>


      <section
        className="admin-reservation-list"
        onScroll={handleReservationListScroll}
      >

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
          && reservations.length === 0 && (
            <p className="admin-slot-empty">
              조회된 예약이 없습니다.
            </p>
        )}

        {reservations.map(
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
                            slot.remainingCapacity <= 0
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
      open={Boolean(deleteTargetSlot)}
      title="예약 슬롯을 삭제하시겠습니까?"
      message={
        deleteTargetSlot
          ? `${deleteTargetSlot.startTime?.slice(0, 5)} ~ `
            + `${deleteTargetSlot.endTime?.slice(0, 5)} 슬롯을 삭제합니다. `
          : ''
      }
      confirmText={
        slotDeleting
          ? '삭제 중...'
          : '슬롯 삭제'
      }
      cancelText="닫기"
      errorMessage={slotDeleteError}
      danger
      onConfirm={handleSlotDelete}
      onCancel={() => {
        if (!slotDeleting) {
          setDeleteTargetSlot(null)
        }
      }}
    />

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