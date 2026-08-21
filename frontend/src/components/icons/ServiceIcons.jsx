function IconBase({ children, className = '' }) {
  return (
    <svg
      className={`service-svg-icon ${className}`}
      viewBox="0 0 48 48"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      aria-hidden="true"
    >
      {children}
    </svg>
  )
}


/* =========================================================
   헌혈의 집 찾기
   ========================================================= */

export function CenterIcon() {
  return (
    <IconBase>
      <path
        className="service-icon-line"
        d="M9 39V17H39V39"
      />

      <path
        className="service-icon-line"
        d="M15 17V9H33V17"
      />

      <path
        className="service-icon-line"
        d="M19 14H23M27 14H31"
      />

      <path
        className="service-icon-line"
        d="M15 23H19M23 23H27M31 23H35"
      />

      <path
        className="service-icon-line"
        d="M15 29H19M31 29H35"
      />

      <path
        className="service-icon-line"
        d="M21 39V31H27V39"
      />

      <path
        className="service-icon-accent"
        d="M23 9V5M21 7H25"
      />
    </IconBase>
  )
}


/* =========================================================
   헌혈 예약
   ========================================================= */

export function ReservationIcon() {
  return (
    <IconBase>
      <rect
        className="service-icon-line"
        x="8"
        y="11"
        width="32"
        height="29"
        rx="3"
      />

      <path
        className="service-icon-line"
        d="M8 18H40"
      />

      <path
        className="service-icon-line"
        d="M15 7V14M33 7V14"
      />

      <circle
        className="service-icon-line"
        cx="24"
        cy="28"
        r="6"
      />

      <path
        className="service-icon-accent"
        d="M24 24V28L27 30"
      />
    </IconBase>
  )
}


/* =========================================================
   전자문진
   ========================================================= */

export function QuestionnaireIcon() {
  return (
    <IconBase>
      <path
        className="service-icon-line"
        d="M12 6H31L37 12V42H12V6Z"
      />

      <path
        className="service-icon-line"
        d="M31 6V12H37"
      />

      <path
        className="service-icon-line"
        d="M18 18H31M18 24H31M18 30H26"
      />

      <path
        className="service-icon-accent"
        d="M27 35L30 38L37 30"
      />
    </IconBase>
  )
}


/* =========================================================
   프로모션
   ========================================================= */

export function PromotionIcon() {
  return (
    <IconBase>
      <rect
        className="service-icon-line"
        x="7"
        y="16"
        width="34"
        height="25"
        rx="3"
      />

      <path
        className="service-icon-line"
        d="M7 23H41"
      />

      <path
        className="service-icon-line"
        d="M24 16V41"
      />

      <path
        className="service-icon-accent"
        d="M24 16C20 16 16 14 16 11C16 8 18 7 20 8C22 9 24 12 24 16Z"
      />

      <path
        className="service-icon-accent"
        d="M24 16C28 16 32 14 32 11C32 8 30 7 28 8C26 9 24 12 24 16Z"
      />
    </IconBase>
  )
}


/* =========================================================
   헌혈내역
   ========================================================= */

export function RecordsIcon() {
  return (
    <IconBase>
      <rect
        className="service-icon-line"
        x="11"
        y="7"
        width="26"
        height="35"
        rx="3"
      />

      <path
        className="service-icon-line"
        d="M17 22H31M17 28H31M17 34H27"
      />

      <path
        className="service-icon-accent"
        d="M24 12C21.5 15 20 17 20 19C20 21.2 21.8 23 24 23C26.2 23 28 21.2 28 19C28 17 26.5 15 24 12Z"
      />
    </IconBase>
  )
}


/* =========================================================
   검사결과
   ========================================================= */

export function ResultsIcon() {
  return (
    <IconBase>
      <rect
        className="service-icon-line"
        x="8"
        y="8"
        width="32"
        height="32"
        rx="3"
      />

      <path
        className="service-icon-line"
        d="M14 16H34"
      />

      <path
        className="service-icon-line"
        d="M14 23H20M14 29H20M14 35H20"
      />

      <path
        className="service-icon-accent"
        d="M25 32L29 36L36 27"
      />
    </IconBase>
  )
}


/* =========================================================
   증명서 발급
   ========================================================= */

export function CertificateIcon() {
  return (
    <IconBase>
      <path
        className="service-icon-line"
        d="M12 6H30L36 12V31H12V6Z"
      />

      <path
        className="service-icon-line"
        d="M30 6V12H36"
      />

      <path
        className="service-icon-line"
        d="M17 18H31M17 24H28"
      />

      <path
        className="service-icon-accent"
        d="M18 31H37V40H18V31Z"
      />

      <path
        className="service-icon-line"
        d="M22 28V31M33 28V31"
      />
    </IconBase>
  )
}


/* =========================================================
   혈액사용 보류신청
   ========================================================= */

export function BloodHoldIcon() {
  return (
    <IconBase>
      <circle
        className="service-icon-line"
        cx="24"
        cy="24"
        r="16"
      />

      <path
        className="service-icon-line"
        d="M13 35L35 13"
      />

      <path
        className="service-icon-accent"
        d="M24 13C20.5 17.5 18.5 21 18.5 24C18.5 27 21 29.5 24 29.5C27 29.5 29.5 27 29.5 24C29.5 21 27.5 17.5 24 13Z"
      />
    </IconBase>
  )
}


/* =========================================================
   자주하는 질문
   ========================================================= */

export function FaqIcon() {
  return (
    <IconBase>
      <path
        className="service-icon-line"
        d="M8 10H40V34H28L22 40V34H8V10Z"
      />

      <path
        className="service-icon-accent"
        d="M20 19C20.5 16.5 22 15 24.5 15C27 15 29 16.5 29 19C29 21 28 22 25.5 23.5C24 24.5 24 25.5 24 27"
      />

      <circle
        className="service-icon-accent-fill"
        cx="24"
        cy="31"
        r="1.5"
      />
    </IconBase>
  )
}

export function LoginLockIcon() {
  return (
    <IconBase>
      <rect
        className="service-icon-line"
        x="11"
        y="21"
        width="26"
        height="20"
        rx="4"
      />

      <path
        className="service-icon-line"
        d="M16 21V16C16 11.6 19.6 8 24 8C28.4 8 32 11.6 32 16V21"
      />

      <circle
        className="service-icon-accent"
        cx="24"
        cy="30"
        r="2"
      />

      <path
        className="service-icon-accent"
        d="M24 32V35"
      />
    </IconBase>
  )
}