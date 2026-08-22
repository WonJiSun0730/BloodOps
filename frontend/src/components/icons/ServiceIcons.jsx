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
        d="M8 41V16H40V41"
      />

      <path
        className="service-icon-line"
        d="M15 16V7H33V16"
      />

      <path
        className="service-icon-accent"
        d="M24 9V14M21.5 11.5H26.5"
      />

      <path
        className="service-icon-line"
        d="M14 23H19M29 23H34"
      />

      <path
        className="service-icon-line"
        d="M14 29H19M29 29H34"
      />

      <path
        className="service-icon-line"
        d="M20 41V32H28V41"
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
        d="M18 18H31M18 24H31M18 30H24"
      />

      <path
        className="service-icon-accent"
        d="M25 35L28 38L35 31"
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
        d="M17 27H31M17 33H31M17 38H27"
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
      <path
        className="service-icon-accent"
        d="M22 8H29V15H22V8Z"
      />

      <path
        className="service-icon-line"
        d="M25.5 15V21"
      />

      <path
        className="service-icon-line"
        d="M29 18C34 20 36 24 36 29C36 35 31 39 25 39"
      />

      <path
        className="service-icon-line"
        d="M17 22L24 29"
      />

      <path
        className="service-icon-line"
        d="M14 25L20 19"
      />

      <path
        className="service-icon-line"
        d="M13 39H38"
      />

      <path
        className="service-icon-line"
        d="M18 34H29"
      />

      <circle
        className="service-icon-accent-fill"
        cx="23"
        cy="31"
        r="2"
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
      {/* 출력되는 문서 */}
      <path
        className="service-icon-line"
        d="M16 7H32V18H16V7Z"
      />

      {/* 프린터 본체 */}
      <path
        className="service-icon-line"
        d="M11 18H37C39.2 18 41 19.8 41 22V33H34"
      />

      <path
        className="service-icon-line"
        d="M14 33H7V22C7 19.8 8.8 18 11 18"
      />

      {/* 출력된 증명서 */}
      <path
        className="service-icon-line"
        d="M14 28H34V41H14V28Z"
      />

      {/* 증명서 내용 */}
      <path
        className="service-icon-line"
        d="M19 33H29M19 37H26"
      />

      {/* 프린터 상태 표시 */}
      <circle
        className="service-icon-accent-fill"
        cx="35"
        cy="23"
        r="1.8"
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
        d="M24 15C21 19 19.5 21.8 19.5 24.5C19.5 27.4 21.5 29.5 24 29.5C26.5 29.5 28.5 27.4 28.5 24.5C28.5 21.8 27 19 24 15Z"
        transform="translate(0 1)"
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
        d="M6 8H42V35H28L21 42V35H6V8Z"
      />

      <path
        className="service-icon-accent"
        d="M20.5 18.5C21 16.2 22.4 15 24.5 15C26.8 15 28.5 16.4 28.5 18.5C28.5 20.3 27.5 21.3 25.5 22.5C24.2 23.3 24 24.2 24 25.5"
      />

      <circle
        className="service-icon-accent-fill"
        cx="24"
        cy="29.5"
        r="1.3"
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