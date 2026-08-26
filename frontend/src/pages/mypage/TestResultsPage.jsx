function TestResultsPage() {
  /*
   * 추후 헌혈기록 API 또는
   * 검사결과 상세 API를 연결합니다.
   *
   * 실제 검사 항목은 팀원 구현 결과를 받은 뒤
   * 서버 응답 구조에 맞춰 표시합니다.
   */

  return (
    <div className="mypage-page">

      <section className="mypage-page-description">
        <div>
          <span>
            BLOOD TEST RESULTS
          </span>

          <h3>
            혈액검사 결과
          </h3>

          <p>
            헌혈 후 등록된 검사 결과를
            헌혈일자별로 확인할 수 있습니다.
          </p>
        </div>
      </section>


      <section className="mypage-data-section">
        <header className="mypage-data-header">
          <div>
            <h3>
              검사결과
            </h3>

            <p>
              검사 결과가 등록된 헌혈기록만
              표시됩니다.
            </p>
          </div>

          <span>
            총 0건
          </span>
        </header>


        <div className="mypage-empty-state standalone">
          <strong>
            조회된 검사결과가 없습니다.
          </strong>

          <p>
            검사 결과가 등록되면
            헌혈일자와 검사 항목별 결과를
            확인할 수 있습니다.
          </p>
        </div>
      </section>

    </div>
  )
}


export default TestResultsPage