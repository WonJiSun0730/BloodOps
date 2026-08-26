function TestResultsPage() {
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
            확인할 수 있는 기능입니다.
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
              검사결과 조회 기능은
              추후 제공될 예정입니다.
            </p>
          </div>

          <span>
            준비 중
          </span>
        </header>


        <div className="mypage-empty-state standalone">
          <strong>
            검사결과 기능을 준비하고 있습니다.
          </strong>

          <p>
            관련 데이터 연동이 완료되면<br />
            헌혈일자별 검사결과를 확인할 수 있습니다.
          </p>
        </div>
      </section>

    </div>
  )
}


export default TestResultsPage