function DonationRecordsPage() {
  /*
   * 추후 로그인 회원의 헌혈기록 API를 호출합니다.
   *
   * 예상 응답 항목:
   * - 헌혈일자
   * - 헌혈종류
   * - 혈액원
   * - 헌혈장소
   * - 검사결과 존재 여부
   */

  return (
    <div className="mypage-page">

      <section className="mypage-page-description">
        <div>
          <span>
            DONATION RECORDS
          </span>

          <h3>
            나의 헌혈기록
          </h3>

          <p>
            헌혈 완료 후 등록된 기록을
            최신순으로 확인할 수 있습니다.
          </p>
        </div>
      </section>


      <section className="mypage-data-section">
        <header className="mypage-data-header">
          <div>
            <h3>
              헌혈기록
            </h3>

            <p>
              의료진의 진료 완료 처리 후
              헌혈기록이 표시됩니다.
            </p>
          </div>

          <span>
            총 0건
          </span>
        </header>


        <div className="mypage-table-shell">
          <div
            className="mypage-table-header"
            aria-hidden="true"
          >
            <span>구분</span>
            <span>헌혈일자</span>
            <span>헌혈종류</span>
            <span>혈액원</span>
            <span>헌혈장소</span>
            <span>검사결과</span>
          </div>

          <div className="mypage-empty-state">
            <strong>
              조회된 헌혈기록이 없습니다.
            </strong>

            <p>
              헌혈이 완료되고 검사내역이 등록되면
              이곳에서 확인할 수 있습니다.
            </p>
          </div>
        </div>
      </section>

    </div>
  )
}


export default DonationRecordsPage