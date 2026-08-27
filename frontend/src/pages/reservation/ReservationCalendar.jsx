import {
  useMemo,
  useState,
} from 'react'


function toDateKey(date) {
  const year =
    date.getFullYear()

  const month =
    String(
      date.getMonth() + 1
    ).padStart(2, '0')

  const day =
    String(
      date.getDate()
    ).padStart(2, '0')

  return `${year}-${month}-${day}`
}


function ReservationCalendar({
  value,
  onChange,
  availableDates = [],
  onVisibleMonthChange,
}) {
  const today =
    useMemo(() => {
      const now = new Date()

      return new Date(
        now.getFullYear(),
        now.getMonth(),
        now.getDate()
      )
    }, [])


  const initialMonth =
    value
      ? new Date(`${value}T00:00:00`)
      : today


  const [
    visibleMonth,
    setVisibleMonth,
  ] = useState(
    new Date(
      initialMonth.getFullYear(),
      initialMonth.getMonth(),
      1
    )
  )

  const availableDateSet =
    useMemo(
      () => new Set(availableDates),
      [availableDates]
    )

  const todayKey =
    toDateKey(today)

  const maxDate =
    new Date(
      today.getFullYear(),
      today.getMonth() + 2,
      today.getDate()
    )

  const currentMonthStart =
    new Date(
      today.getFullYear(),
      today.getMonth(),
      1
    )

  const isCurrentMonth =
    visibleMonth.getFullYear()
      === currentMonthStart.getFullYear()
    && visibleMonth.getMonth()
      === currentMonthStart.getMonth()

  const maxMonthStart =
    new Date(
      today.getFullYear(),
      today.getMonth() + 2,
      1
    )

  const isMaxMonth =
    visibleMonth.getFullYear()
      === maxMonthStart.getFullYear()
    && visibleMonth.getMonth()
      === maxMonthStart.getMonth()

  const year =
    visibleMonth.getFullYear()

  const month =
    visibleMonth.getMonth()


  const firstDayIndex =
    new Date(
      year,
      month,
      1
    ).getDay()


  const lastDate =
    new Date(
      year,
      month + 1,
      0
    ).getDate()


  const cells = []

  for (
    let index = 0;
    index < firstDayIndex;
    index += 1
  ) {
    cells.push(null)
  }


  for (
    let day = 1;
    day <= lastDate;
    day += 1
  ) {
    cells.push(
      new Date(
        year,
        month,
        day
      )
    )
  }


  function moveMonth(offset) {
    const nextMonth =
      new Date(
        year,
        month + offset,
        1
      )

    setVisibleMonth(nextMonth)

    onVisibleMonthChange?.(
      nextMonth.getFullYear(),
      nextMonth.getMonth() + 1
    )
  }

  return (
    <div className="reservation-calendar">

      <div className="reservation-calendar-header">

        <button
          type="button"
          onClick={() =>
            moveMonth(-1)
          }
          aria-label="이전 달"
          disabled={isCurrentMonth}
        >
          ‹
        </button>

        <strong>
          {year}년 {month + 1}월
        </strong>

        <button
          type="button"
          onClick={() =>
            moveMonth(1)
          }
          aria-label="다음 달"
          disabled={isMaxMonth}
        >
          ›
        </button>

      </div>


      <div className="reservation-calendar-weekdays">
        <span>일</span>
        <span>월</span>
        <span>화</span>
        <span>수</span>
        <span>목</span>
        <span>금</span>
        <span>토</span>
      </div>


      <div className="reservation-calendar-grid">

        {cells.map(
          (date, index) => {

            if (!date) {
              return (
                <span
                  key={`empty-${index}`}
                  className="reservation-calendar-empty"
                />
              )
            }


            const dateKey =
              toDateKey(date)

            const disabled =
              date <= today
              || date > maxDate

            const selected =
              dateKey === value

            const todayDate =
              dateKey === todayKey

            const available =
              availableDateSet.has(dateKey)

            return (
              <button
                key={dateKey}
                type="button"
                disabled={disabled}
                className={
                  `reservation-calendar-day${
                    available && !disabled
                      ? ' available'
                      : ''
                  }${
                    selected
                      ? ' selected'
                      : ''
                  }${
                    todayDate
                      ? ' today'
                      : ''
                  }`
                }
                onClick={() =>
                  onChange(
                    dateKey
                  )
                }
              >
                {date.getDate()}
              </button>
            )
          }
        )}

      </div>

    </div>
  )
}


export default ReservationCalendar