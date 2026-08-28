import {
  useEffect,
  useState,
} from 'react'

import {
  getMyDonationEligibility,
  getMyDonationHistory,
} from '../../api/apiClient.js'


const donationTypeLabels = {
  WHOLE_BLOOD: '전혈',
  PLATELET: '혈소판',
  PLASMA: '혈장',
  PLATELET_PLASMA: '혈소판혈장',
}


function DonationRecordsPage() {
  const [
    records,
    setRecords,
  ] = useState([])

  const [
    eligibility,
    setEligibility,
  ] = useState(null)

  const [
    loading,
    setLoading,
  ] = useState(true)

  const [
    error,
    setError,
  ] = useState('')


  useEffect(() => {
    let active = true

    async function loadRecords() {
      try {
        setLoading(true)
        setError('')

        const [
          historyData,
          eligibilityData,
        ] = await Promise.all([
          getMyDonationHistory(),
          getMyDonationEligibility(),
        ])

        if (active) {
          setRecords(
            Array.isArray(historyData)
              ? historyData
              : []
          )

          setEligibility(
            eligibilityData
              || null
          )
        }
      } catch (error) {
        if (active) {
          setRecords([])
          setEligibility(null)

          setError(
            error.message
            || '헌혈기록을 불러오지 못했습니다.'
          )
        }
      } finally {
        if (active) {
          setLoading(false)
        }
      }
    }

    loadRecords()

    return () => {
      active = false
    }
  }, [])


  return (
    <div className="mypage-page">
      {eligibility && (
        <section className="donation-eligibility-section">

          <div className="donation-eligibility-heading">
            <div>
              <span>
                NEXT DONATION
              </span>

              <h3>
                다음 헌혈 가능일
              </h3>

              <p>
                최근 헌혈 기록과 연간 횟수 제한을
                기준으로 계산됩니다.
              </p>
            </div>
          </div>


          <div className="donation-eligibility-grid">

            <article className="donation-eligibility-card">
              <span className="donation-eligibility-type">
                전혈
              </span>

              <strong>
                {
                  eligibility
                    .wholeBloodNextAvailableDate
                }
              </strong>

              <div className="donation-eligibility-meta">
                <span>
                  최근 1년
                </span>

                <b>
                  {
                    eligibility
                      .wholeBloodCountLastYear
                  }
                  {' / '}
                  {
                    eligibility
                      .wholeBloodAnnualLimit
                  }
                  회
                </b>
              </div>

              <div className="donation-eligibility-progress">
                <span
                  style={{
                    width: `${
                      Math.min(
                        100,
                        (
                          eligibility
                            .wholeBloodCountLastYear
                          / eligibility
                            .wholeBloodAnnualLimit
                        ) * 100
                      )
                    }%`,
                  }}
                />
              </div>
            </article>


            <article className="donation-eligibility-card">
              <span className="donation-eligibility-type">
                혈소판
              </span>

              <strong>
                {
                  eligibility
                    .plateletNextAvailableDate
                }
              </strong>

              <div className="donation-eligibility-meta">
                <span>
                  최근 1년
                </span>

                <b>
                  {
                    eligibility
                      .plateletCountLastYear
                  }
                  {' / '}
                  {
                    eligibility
                      .plateletAnnualLimit
                  }
                  회
                </b>
              </div>

              <div className="donation-eligibility-progress">
                <span
                  style={{
                    width: `${
                      Math.min(
                        100,
                        (
                          eligibility
                            .plateletCountLastYear
                          / eligibility
                            .plateletAnnualLimit
                        ) * 100
                      )
                    }%`,
                  }}
                />
              </div>
            </article>


            <article className="donation-eligibility-card">
              <span className="donation-eligibility-type">
                혈장
              </span>

              <strong>
                {
                  eligibility
                    .plasmaNextAvailableDate
                }
              </strong>

              <div className="donation-eligibility-meta">
                <span>
                  간격 제한
                </span>

                <b>
                  2주
                </b>
              </div>

              <div className="donation-eligibility-note">
                연간 횟수 제한 없음
              </div>
            </article>


            <article className="donation-eligibility-card">
              <span className="donation-eligibility-type">
                혈소판혈장
              </span>

              <strong>
                {
                  eligibility
                    .plateletPlasmaNextAvailableDate
                }
              </strong>

              <div className="donation-eligibility-meta">
                <span>
                  최근 1년
                </span>

                <b>
                  {
                    eligibility
                      .plateletCountLastYear
                  }
                  {' / '}
                  {
                    eligibility
                      .plateletAnnualLimit
                  }
                  회
                </b>
              </div>

              <div className="donation-eligibility-progress">
                <span
                  style={{
                    width: `${
                      Math.min(
                        100,
                        (
                          eligibility
                            .plateletCountLastYear
                          / eligibility
                            .plateletAnnualLimit
                        ) * 100
                      )
                    }%`,
                  }}
                />
              </div>
            </article>

          </div>

        </section>
      )}

      <section className="mypage-data-section">
        <header className="mypage-data-header">
          <div>
            <h3>
              헌혈기록
            </h3>

            <p>
              의료진의 헌혈 완료 처리 후
              헌혈기록이 표시됩니다.
            </p>
          </div>

          <span>
            총 {records.length}건
          </span>
        </header>


        {loading ? (
          <div className="mypage-empty-state standalone">
            <strong>
              헌혈기록을 불러오는 중입니다.
            </strong>

            <p>
              잠시만 기다려주세요.
            </p>
          </div>
        ) : error ? (
          <div className="mypage-record-error">
            {error}
          </div>
        ) : records.length === 0 ? (
          <div className="mypage-empty-state standalone">
            <strong>
              조회된 헌혈기록이 없습니다.
            </strong>

            <p>
              헌혈이 완료되면
              이곳에서 기록을 확인할 수 있습니다.
            </p>
          </div>
        ) : (
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

            <div className="mypage-table-body">
              {records.map(
                (record, index) => (
                  <article
                    key={record.id}
                    className="mypage-table-row"
                  >
                    <span data-label="구분">
                      {index + 1}
                    </span>

                    <span data-label="헌혈일자">
                      {record.donationDate}
                    </span>

                    <span data-label="헌혈종류">
                      {
                        donationTypeLabels[
                          record.donationType
                        ]
                        || record.donationType
                      }
                    </span>

                    <span data-label="혈액원">
                      {record.bloodCenterName}
                    </span>

                    <span data-label="헌혈장소">
                      {record.donationPlaceName}
                    </span>

                    <span data-label="검사결과">
                      <span className="mypage-result-pending">
                        개발 예정
                      </span>
                    </span>
                  </article>
                )
              )}
            </div>
          </div>
        )}
      </section>

    </div>
  )
}


export default DonationRecordsPage