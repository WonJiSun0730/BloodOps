import { useNavigate } from 'react-router-dom'
import { getCurrentUser } from '../../api/apiClient.js'
import './MedicalMyPage.css'

function QuestionnaireIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M7 3h10a2 2 0 0 1 2 2v16H5V5a2 2 0 0 1 2-2Z" />
      <path d="M9 8h6M9 12h6M9 16h4" />
    </svg>
  )
}

function InventoryIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M4 7h16v14H4zM7 3h10v4H7z" />
      <path d="M9 13h6M12 10v6" />
    </svg>
  )
}

function DonationHistoryIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M6 3h12a2 2 0 0 1 2 2v16H4V5a2 2 0 0 1 2-2Z" />
      <path d="M8 8h8M8 12h8M8 16h5" />
    </svg>
  )
}

function MedicalMyPage() {
  const navigate = useNavigate()

  async function openQuestionnaire() {
    const user = await getCurrentUser()
    const stored = JSON.parse(sessionStorage.getItem('verifiedMedicalDonor') || '{}')
    const confirmationId = stored.owner === user.identifier ? stored.confirmationId : null

    navigate(
      confirmationId
        ? `/medical/donor_check/${confirmationId}`
        : '/medical/donor_identity',
    )
  }

  return (
    <main className="medical-my-page">
      <header className="medical-my-page__header">
        <p className="medical-my-page__eyebrow">MEDICAL WORKSPACE</p>
        <h1>의료진 마이페이지</h1>
        <p>헌혈자 문진을 확인하고 혈액 재고를 관리할 수 있습니다.</p>
      </header>

      <section
        className="medical-service-grid"
        aria-label="의료진 업무 메뉴"
      >
        <article className="medical-service-card">
          <span className="medical-service-card__icon">
            <QuestionnaireIcon />
          </span>
          <div className="medical-service-card__content">
            <span className="medical-service-card__tag">헌혈자 관리</span>
            <h2>문진 확인</h2>
            <p>
              신분 확인을 완료한 헌혈자의 정보와 전자 문진 내용을
              확인합니다.
            </p>
          </div>
          <button type="button" onClick={openQuestionnaire}>
            문진 확인하기
            <span aria-hidden="true">→</span>
          </button>
        </article>

        <article className="medical-service-card">
          <span className="medical-service-card__icon"><QuestionnaireIcon /></span>
          <div className="medical-service-card__content">
            <span className="medical-service-card__tag">당일 접수</span>
            <h2>당일 헌혈 예약자</h2>
            <p>오늘 방문 예정자를 확인하고 실제 헌혈이 끝난 예약을 완료 처리합니다.</p>
          </div>
          <button type="button" onClick={() => navigate('/medical/today-reservations')}>당일 예약자 보기<span aria-hidden="true">→</span></button>
        </article>

        <article className="medical-service-card">
          <span className="medical-service-card__icon"><QuestionnaireIcon /></span>
          <div className="medical-service-card__content">
            <span className="medical-service-card__tag">문진 조회</span>
            <h2>전자문진 작성자 목록</h2>
            <p>제출된 전자문진 작성자를 최신순으로 조회하고 확인이 필요한 내용을 살펴봅니다.</p>
          </div>
          <button type="button" onClick={() => navigate('/medical/questionnaires')}>작성자 목록 보기<span aria-hidden="true">→</span></button>
        </article>

        <article className="medical-service-card">
          <span className="medical-service-card__icon">
            <InventoryIcon />
          </span>
          <div className="medical-service-card__content">
            <span className="medical-service-card__tag">재고 관리</span>
            <h2>혈액 재고 관리</h2>
            <p>
              혈액 팩 목록과 보유 현황을 조회하고 신규 등록 및 수정합니다.
            </p>
          </div>
          <button
            type="button"
            onClick={() => navigate('/medical/blood_units_management')}
          >
            재고 관리하기
            <span aria-hidden="true">→</span>
          </button>
        </article>


        <article className="medical-service-card">
          <span className="medical-service-card__icon">
            <DonationHistoryIcon />
          </span>
          <div className="medical-service-card__content">
            <span className="medical-service-card__tag">헌혈 기록</span>
            <h2>전체 헌혈 기록</h2>
            <p>
              완료 처리되어 저장된 모든 헌혈자의 헌혈 기록을
              최신순으로 확인합니다.
            </p>
          </div>
          <button
            type="button"
            onClick={() => navigate('/medical/donation_history')}
          >
            헌혈 기록 보기
            <span aria-hidden="true">→</span>
          </button>
        </article>
      </section>
    </main>
  )
}

export default MedicalMyPage
