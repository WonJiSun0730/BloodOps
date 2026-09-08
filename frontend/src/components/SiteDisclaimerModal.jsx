import { useEffect, useState } from 'react'
import '../styles/site-disclaimer.css'

const STORAGE_KEY = 'bloodops_site_disclaimer_confirmed'

export default function SiteDisclaimerModal() {
  const [isOpen, setIsOpen] = useState(false)
  const [acknowledged, setAcknowledged] = useState(false)

  useEffect(() => {
    const confirmed = localStorage.getItem(STORAGE_KEY)

    if (confirmed !== 'true') {
      setIsOpen(true)
    }
  }, [])

  useEffect(() => {
    if (!isOpen) {
      return
    }

    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'

    return () => {
      document.body.style.overflow = previousOverflow
    }
  }, [isOpen])

  const handleConfirm = () => {
    if (!acknowledged) {
      return
    }

    localStorage.setItem(STORAGE_KEY, 'true')
    setIsOpen(false)
  }

  if (!isOpen) {
    return null
  }

  return (
    <div
      className="site-disclaimer-backdrop"
      role="presentation"
    >
      <section
        className="site-disclaimer-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="site-disclaimer-title"
        aria-describedby="site-disclaimer-description"
      >
        <div className="site-disclaimer-badge">
          서비스 이용 전 안내
        </div>

        <h2 id="site-disclaimer-title">
          BloodOps는 실제 헌혈기관의 공식 서비스가 아닙니다.
        </h2>

        <div
          id="site-disclaimer-description"
          className="site-disclaimer-content"
        >
          <p>
            본 사이트는 교육 및 포트폴리오 목적으로 제작된
            비상업적 팀 프로젝트입니다.
          </p>

          <ul>
            <li>
              대한적십자사, 혈액원, 헌혈의집 또는 기타 실제 헌혈기관이
              운영하는 공식 사이트가 아닙니다.
            </li>
            <li>
              사이트 내 예약, 헌혈 가능 여부, 혈액 정보 및 안내 내용은
              실제 의료 서비스나 공식 예약으로 사용할 수 없습니다.
            </li>
            <li>
              건강 상태와 헌혈 가능 여부에 대한 최종 판단은
              반드시 의료진 및 공식 헌혈기관의 안내를 따라야 합니다.
            </li>
          </ul>
        </div>

        <label className="site-disclaimer-confirm">
          <input
            type="radio"
            name="site-disclaimer-confirm"
            checked={acknowledged}
            onChange={() => setAcknowledged(true)}
          />

          <span>
            네, 저는 해당 사실을 명확히 인지했습니다.
          </span>
        </label>

        <button
          type="button"
          className="site-disclaimer-button"
          disabled={!acknowledged}
          onClick={handleConfirm}
        >
          사이트 이용하기
        </button>

        <p className="site-disclaimer-footnote">
          위 내용을 확인하지 않으면 사이트를 이용할 수 없습니다.
        </p>
      </section>
    </div>
  )
}