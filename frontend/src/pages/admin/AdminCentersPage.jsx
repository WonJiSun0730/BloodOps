import { useEffect, useState } from 'react'

import {
  createCenter,
  getCenters,
  updateCenter,
} from '../../api/apiClient.js'

import AdminSectionNav
  from '../../components/admin/AdminSectionNav.jsx'

import './AdminCentersPage.css'


const DONATION_TYPES = [
  '전혈',
  '혈장',
  '혈소판',
  '혈소판혈장',
]

function parseHours(value) {
  if (!value || !value.includes('~')) {
    return {
      start: '',
      end: '',
    }
  }

  const [start, end] =
    value.split('~').map((time) => time.trim())

  return {
    start,
    end,
  }
}


function buildHours(start, end) {
  if (!start && !end) {
    return ''
  }

  if (!start || !end) {
    throw new Error(
      '운영시간의 시작과 종료 시간을 모두 입력해주세요.'
    )
  }


  const timePattern =
    /^([01]\d|2[0-3]):[0-5]\d$/


  if (
    !timePattern.test(start)
    ||
    !timePattern.test(end)
  ) {
    throw new Error(
      '운영시간을 HH:mm 형식으로 입력해주세요.'
    )
  }


  if (start >= end) {
    throw new Error(
      '종료 시간은 시작 시간보다 늦어야 합니다.'
    )
  }


  return `${start} ~ ${end}`
}

function getTimeRangeError(start, end) {
  if (!start && !end) {
    return ''
  }

  if (!start || !end) {
    return '시작 시간과 종료 시간을 모두 입력해주세요.'
  }

  const timePattern =
    /^([01]\d|2[0-3]):[0-5]\d$/

  if (
    !timePattern.test(start)
    ||
    !timePattern.test(end)
  ) {
    return '시간은 HH:mm 형식으로 입력해주세요.'
  }

  if (start >= end) {
    return '종료 시간은 시작 시간보다 늦어야 합니다.'
  }

  return ''
}


function AdminCentersPage() {
  const [keyword, setKeyword] = useState('')
  const [centers, setCenters] = useState([])
  const [loading, setLoading] = useState(true)
  const [errorMessage, setErrorMessage] = useState('')
  const [selectedCenter, setSelectedCenter] = useState(null)

  const [drawerMode, setDrawerMode] = useState('edit')

  const [editForm, setEditForm] = useState({
    active: true,

    weekdayStart: '',
    weekdayEnd: '',

    saturdayStart: '',
    saturdayEnd: '',

    sundayStart: '',
    sundayEnd: '',

    holidayStart: '',
    holidayEnd: '',

    donationTypes: [],

    amenities: '',
    directions: '',
    parkingInfo: '',
  })

  const [createInfo, setCreateInfo] = useState({
    bloodCenterName: '',
    centerName: '',
    centerType: '',
    address: '',
    phone: '',
  })

  const [saving, setSaving] = useState(false)

  const weekdayTimeError =
    getTimeRangeError(
      editForm.weekdayStart,
      editForm.weekdayEnd
    )

  const saturdayTimeError =
    getTimeRangeError(
      editForm.saturdayStart,
      editForm.saturdayEnd
    )

  const sundayTimeError =
    getTimeRangeError(
      editForm.sundayStart,
      editForm.sundayEnd
    )

  const holidayTimeError =
    getTimeRangeError(
      editForm.holidayStart,
      editForm.holidayEnd
    )


  const hasTimeError =
    Boolean(
      weekdayTimeError
      || saturdayTimeError
      || sundayTimeError
      || holidayTimeError
    )

  function handleOpenEdit(center) {
    setDrawerMode('edit')

    const weekday =
      parseHours(center.weekdayHours)

    const saturday =
      parseHours(center.saturdayHours)

    const sunday =
      parseHours(center.sundayHours)

    const holiday =
      parseHours(center.holidayHours)

    setSelectedCenter(center)

    setEditForm({
      active: center.active,

      weekdayStart: weekday.start,
      weekdayEnd: weekday.end,

      saturdayStart: saturday.start,
      saturdayEnd: saturday.end,

      sundayStart: sunday.start,
      sundayEnd: sunday.end,

      holidayStart: holiday.start,
      holidayEnd: holiday.end,

      donationTypes:
        center.donationTypes
          ? center.donationTypes
              .split(',')
              .map((type) => type.trim())
          : [],

      amenities: center.amenities ?? '',
      directions: center.directions ?? '',
      parkingInfo: center.parkingInfo ?? '',
    })
  }

  function handleOpenCreate() {
    setDrawerMode('create')

    setSelectedCenter({
      id: null,
    })


    setCreateInfo({
      bloodCenterName: '',
      centerName: '',
      centerType: '',
      address: '',
      phone: '',
    })


    setEditForm({
      active: true,

      weekdayStart: '',
      weekdayEnd: '',

      saturdayStart: '',
      saturdayEnd: '',

      sundayStart: '',
      sundayEnd: '',

      holidayStart: '',
      holidayEnd: '',

      donationTypes: [],

      amenities: '',
      directions: '',
      parkingInfo: '',
    })
  }

  function handleCloseEdit() {
    setSelectedCenter(null)
  }


  function handleEditChange(event) {
    const { name, value } = event.target

    setEditForm((previous) => ({
      ...previous,
      [name]: value,
    }))
  }

  function handleCreateInfoChange(event) {
    const { name, value } = event.target

    setCreateInfo((previous) => ({
      ...previous,
      [name]: value,
    }))
  }

  function handleTimeChange(event) {
    const { name } = event.target

    const digits =
      event.target.value
        .replace(/\D/g, '')
        .slice(0, 4)


    if (!digits) {
      setEditForm((previous) => ({
        ...previous,
        [name]: '',
      }))

      return
    }


    let hour = digits.slice(0, 2)
    let minute = digits.slice(2, 4)


    if (
      hour.length === 2
      && Number(hour) > 23
    ) {
      hour = '23'
    }


    if (
      minute.length === 2
      && Number(minute) > 59
    ) {
      minute = '59'
    }


    let value = hour

    if (digits.length >= 3) {
      value = `${hour}:${minute}`
    }


    setEditForm((previous) => ({
      ...previous,
      [name]: value,
    }))
  }

  function handleActiveChange(active) {
    setEditForm((previous) => ({
      ...previous,
      active,
    }))
  }

  function handleDonationTypeChange(type) {
    setEditForm((previous) => {
      const selected =
        previous.donationTypes.includes(type)

      return {
        ...previous,

        donationTypes: selected
          ? previous.donationTypes.filter(
              (item) => item !== type
            )
          : [
              ...previous.donationTypes,
              type,
            ],
      }
    })
  }

  async function handleSaveEdit(event) {
    event.preventDefault()

    if (!selectedCenter || saving) {
      return
    }


    if (
      drawerMode === 'create'
      && (
        !createInfo.bloodCenterName.trim()
        || !createInfo.centerName.trim()
        || !createInfo.address.trim()
      )
    ) {
      alert(
        '혈액원, 센터명, 주소는 반드시 입력해주세요.'
      )

      return
    }


    setSaving(true)

    try {
      const centerData = {
        active: editForm.active,

        weekdayHours:
          buildHours(
            editForm.weekdayStart,
            editForm.weekdayEnd
          ),

        saturdayHours:
          buildHours(
            editForm.saturdayStart,
            editForm.saturdayEnd
          ),

        sundayHours:
          buildHours(
            editForm.sundayStart,
            editForm.sundayEnd
          ),

        holidayHours:
          buildHours(
            editForm.holidayStart,
            editForm.holidayEnd
          ),

        donationTypes:
          editForm.donationTypes.join(', '),

        amenities: editForm.amenities,
        directions: editForm.directions,
        parkingInfo: editForm.parkingInfo,
      }

      if (drawerMode === 'create') {

        const createdCenter =
          await createCenter({
            ...createInfo,
            ...centerData,
          })

        setCenters((previous) => [
          createdCenter,
          ...previous,
        ])

      } else {

        const updatedCenter =
          await updateCenter(
            selectedCenter.id,
            centerData
          )

        setCenters((previous) =>
          previous.map((center) =>
            center.id === updatedCenter.id
              ? updatedCenter
              : center
          )
        )
      }

      setSelectedCenter(null)

    } catch (error) {
      console.error(
        '헌혈센터 수정 실패:',
        error
      )

      alert(
        error.message
              || '헌혈센터 정보를 수정하지 못했습니다.'
      )
    } finally {
      setSaving(false)
    }
}


  useEffect(() => {
    loadCenters()
  }, [])


  async function loadCenters(searchKeyword = '') {
    setLoading(true)
    setErrorMessage('')

    try {
      const data = await getCenters(searchKeyword)
      setCenters(data)
    } catch (error) {
      console.error('헌혈센터 조회 실패:', error)

      setErrorMessage(
        '헌혈센터 정보를 불러오지 못했습니다.'
      )
    } finally {
      setLoading(false)
    }
  }


  function handleSearch(event) {
    event.preventDefault()

    loadCenters(keyword)
  }


  return (
    <main className="admin-centers-page">

      <section className="admin-centers-header">

        <div>
          <span className="admin-page-eyebrow">
            ADMIN / CENTER
          </span>

          <h1>
            헌혈센터 관리
          </h1>

          <p>
            헌혈센터의 기본 정보와 운영 상태를
            조회하고 관리합니다.
          </p>
        </div>

        <button
          type="button"
          className="admin-primary-button"
          onClick={handleOpenCreate}
        >
          + 센터 등록
        </button>

      </section>

      <AdminSectionNav />

      <section className="admin-centers-toolbar">

        <form
          className="admin-center-search"
          onSubmit={handleSearch}
        >
          <input
            type="search"
            value={keyword}
            onChange={(event) =>
              setKeyword(event.target.value)
            }
            placeholder="센터명 또는 지역 검색"
            aria-label="헌혈센터 검색"
          />

          <button
            type="submit"
            className="admin-center-search-button"
          >
            검색
          </button>
        </form>

        <span className="admin-center-count">
          총 {centers.length}개
        </span>

      </section>


      <section className="admin-center-list">

        <div className="admin-center-list-header">
          <span>센터명</span>
          <span>지역</span>
          <span>연락처</span>
          <span>상태</span>
          <span>관리</span>
        </div>


        {loading && (
          <div className="admin-center-message">
            센터 정보를 불러오는 중입니다.
          </div>
        )}

        {!loading && errorMessage && (
          <div className="admin-center-message error">
            {errorMessage}
          </div>
        )}

        {!loading &&
          !errorMessage &&
          centers.length === 0 && (
            <div className="admin-center-message">
              등록된 헌혈센터가 없습니다.
            </div>
          )}

        {!loading &&
          !errorMessage &&
          centers.map((center) => (
          <article
            key={center.id}
            className="admin-center-row"
          >
            <strong>
              {center.centerName}
            </strong>

            <span>
              {center.address}
            </span>

            <span>
              {center.phone}
            </span>

            <span
              className={
                center.active
                  ? 'admin-center-status active'
                  : 'admin-center-status inspection'
              }
            >
              {center.active ? '운영' : '운영 중단'}
            </span>

            <button
              type="button"
              className="admin-row-action"
              onClick={() => handleOpenEdit(center)}
            >
              수정
            </button>
          </article>
        ))}

      </section>

      {selectedCenter && (
        <>
          <button
            type="button"
            className="admin-center-drawer-backdrop"
            onClick={handleCloseEdit}
            aria-label={
              drawerMode === 'create'
                ? '센터 등록 닫기'
                : '센터 수정 닫기'
            }
          />

          <aside
            className="admin-center-drawer"
            aria-label={
              drawerMode === 'create'
                ? '헌혈센터 등록'
                : '헌혈센터 정보 수정'
            }
          >
            <div className="admin-center-drawer-header">
              <div>
                <span className="admin-center-drawer-label">
                  {drawerMode === 'create'
                    ? '센터 신규 등록'
                    : '센터 정보 수정'}
                </span>

                <h2>
                  {drawerMode === 'create'
                    ? '새 헌혈센터'
                    : selectedCenter.centerName}
                </h2>

                <p>
                  {drawerMode === 'create'
                    ? '신규 헌혈센터 정보를 입력해주세요.'
                    : selectedCenter.address}
                </p>
              </div>

              <button
                type="button"
                className="admin-center-drawer-close"
                onClick={handleCloseEdit}
                aria-label="닫기"
              >
                ×
              </button>
            </div>

            {drawerMode === 'edit' ? (

              <div className="admin-center-public-info">

                <div>
                  <span>혈액원</span>

                  <strong>
                    {selectedCenter.bloodCenterName || '-'}
                  </strong>
                </div>

                <div>
                  <span>구분</span>

                  <strong>
                    {selectedCenter.centerType || '-'}
                  </strong>
                </div>

                <div>
                  <span>전화번호</span>

                  <strong>
                    {selectedCenter.phone || '-'}
                  </strong>
                </div>

              </div>

            ) : (

              <div className="admin-center-create-info">

                <label className="admin-center-edit-field">
                  <span>혈액원</span>

                  <input
                    type="text"
                    name="bloodCenterName"
                    value={createInfo.bloodCenterName}
                    onChange={handleCreateInfoChange}
                    placeholder="예: 서울중앙혈액원"
                  />
                </label>


                <label className="admin-center-edit-field">
                  <span>센터명</span>

                  <input
                    type="text"
                    name="centerName"
                    value={createInfo.centerName}
                    onChange={handleCreateInfoChange}
                    placeholder="예: 강남 헌혈의 집"
                  />
                </label>


                <label className="admin-center-edit-field">
                  <span>구분</span>

                  <input
                    type="text"
                    name="centerType"
                    value={createInfo.centerType}
                    onChange={handleCreateInfoChange}
                    placeholder="예: 헌혈의집"
                  />
                </label>


                <label className="admin-center-edit-field">
                  <span>주소</span>

                  <input
                    type="text"
                    name="address"
                    value={createInfo.address}
                    onChange={handleCreateInfoChange}
                    placeholder="센터 주소"
                  />
                </label>


                <label className="admin-center-edit-field">
                  <span>전화번호</span>

                  <input
                    type="tel"
                    name="phone"
                    value={createInfo.phone}
                    onChange={handleCreateInfoChange}
                    placeholder="예: 02-1234-5678"
                  />
                </label>

              </div>

            )}

            <form
              className="admin-center-edit-form"
              onSubmit={handleSaveEdit}
            >
              <div className="admin-center-edit-field">
                <span>센터 상태</span>

                <div className="admin-center-status-options">
                  <button
                    type="button"
                    className={
                      editForm.active
                        ? 'selected'
                        : ''
                    }
                    onClick={() =>
                      handleActiveChange(true)
                    }
                  >
                    운영
                  </button>

                  <button
                    type="button"
                    className={
                      !editForm.active
                        ? 'selected'
                        : ''
                    }
                    onClick={() =>
                      handleActiveChange(false)
                    }
                  >
                    운영 중단
                  </button>
                </div>
              </div>


              <div className="admin-center-edit-field">
                <span>평일 운영시간</span>

                <div
                  className={
                    weekdayTimeError
                      ? 'admin-center-time-range error'
                      : 'admin-center-time-range'
                  }
                >
                  <input
                    type="text"
                    name="weekdayStart"
                    value={editForm.weekdayStart}
                    onChange={handleTimeChange}
                    inputMode="numeric"
                    maxLength="5"
                    placeholder="09:00"
                  />

                  <span>~</span>

                  <input
                    type="text"
                    name="weekdayEnd"
                    value={editForm.weekdayEnd}
                    onChange={handleTimeChange}
                    inputMode="numeric"
                    maxLength="5"
                    placeholder="18:00"
                  />
                </div>

                {weekdayTimeError && (
                  <small className="admin-center-field-error">
                    {weekdayTimeError}
                  </small>
                )}
              </div>


              <div className="admin-center-edit-field">
                <span>토요일 운영시간</span>

                <div
                  className={
                    saturdayTimeError
                      ? 'admin-center-time-range error'
                      : 'admin-center-time-range'
                  }
                >
                    <input
                      type="text"
                      name="saturdayStart"
                      value={editForm.saturdayStart}
                      onChange={handleTimeChange}
                      inputMode="numeric"
                      maxLength="5"
                      placeholder="09:00"
                    />

                    <span>~</span>

                    <input
                      type="text"
                      name="saturdayEnd"
                      value={editForm.saturdayEnd}
                      onChange={handleTimeChange}
                      inputMode="numeric"
                      maxLength="5"
                      placeholder="18:00"
                    />
                </div>
                {saturdayTimeError && (
                  <small className="admin-center-field-error">
                    {saturdayTimeError}
                  </small>
                )}
              </div>

              <div className="admin-center-edit-field">
                <span>일요일 운영시간</span>

                <div
                  className={
                    sundayTimeError
                      ? 'admin-center-time-range error'
                      : 'admin-center-time-range'
                  }
                >
                    <input
                      type="text"
                      name="sundayStart"
                      value={editForm.sundayStart}
                      onChange={handleTimeChange}
                      inputMode="numeric"
                      maxLength="5"
                      placeholder="09:00"
                    />

                    <span>~</span>

                    <input
                      type="text"
                      name="sundayEnd"
                      value={editForm.sundayEnd}
                      onChange={handleTimeChange}
                      inputMode="numeric"
                      maxLength="5"
                      placeholder="18:00"
                    />
                </div>
                {sundayTimeError && (
                  <small className="admin-center-field-error">
                    {sundayTimeError}
                  </small>
                )}
              </div>

              <div className="admin-center-edit-field">
                <span>공휴일 운영시간</span>

                <div
                  className={
                    holidayTimeError
                      ? 'admin-center-time-range error'
                      : 'admin-center-time-range'
                  }
                >
                    <input
                      type="text"
                      name="holidayStart"
                      value={editForm.holidayStart}
                      onChange={handleTimeChange}
                      inputMode="numeric"
                      maxLength="5"
                      placeholder="09:00"
                    />

                    <span>~</span>

                    <input
                      type="text"
                      name="holidayEnd"
                      value={editForm.holidayEnd}
                      onChange={handleTimeChange}
                      inputMode="numeric"
                      maxLength="5"
                      placeholder="18:00"
                    />
                </div>
                {holidayTimeError && (
                  <small className="admin-center-field-error">
                    {holidayTimeError}
                  </small>
                )}
              </div>

              <div className="admin-center-edit-field">
                <span>헌혈 종류</span>

                <div className="admin-center-donation-options">
                  {DONATION_TYPES.map((type) => (
                    <label
                      key={type}
                      className={
                        editForm.donationTypes.includes(type)
                          ? 'selected'
                          : ''
                      }
                    >
                      <input
                        type="checkbox"
                        checked={
                          editForm.donationTypes.includes(type)
                        }
                        onChange={() =>
                          handleDonationTypeChange(type)
                        }
                      />

                      <span>
                        {type}
                      </span>
                    </label>
                  ))}
                </div>
              </div>


              <label className="admin-center-edit-field">
                <span>편의시설</span>

                <input
                  type="text"
                  name="amenities"
                  value={editForm.amenities}
                  onChange={handleEditChange}
                  placeholder="예: 주차, 엘리베이터"
                />
              </label>


              <label className="admin-center-edit-field">
                <span>찾아오시는 길</span>

                <textarea
                  name="directions"
                  value={editForm.directions}
                  onChange={handleEditChange}
                  rows="3"
                />
              </label>


              <label className="admin-center-edit-field">
                <span>주차정보</span>

                <textarea
                  name="parkingInfo"
                  value={editForm.parkingInfo}
                  onChange={handleEditChange}
                  rows="3"
                />
              </label>


              <div className="admin-center-drawer-actions">
                <button
                  type="button"
                  onClick={handleCloseEdit}
                >
                  취소
                </button>

                <button
                  type="submit"
                  className="primary"
                  disabled={
                    saving
                    || hasTimeError
                  }
                >
                  {saving
                    ? '저장 중...'
                    : drawerMode === 'create'
                      ? '센터 등록'
                      : '저장'}
                </button>
              </div>
            </form>
          </aside>
        </>
      )}

    </main>
  )
}

export default AdminCentersPage