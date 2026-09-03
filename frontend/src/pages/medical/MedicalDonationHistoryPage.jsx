import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  getMedicalBloodUnitCenters,
  getMedicalDonationHistory,
} from '../../api/apiClient.js'
import './MedicalDonationHistoryPage.css'

const donationTypeLabels = {
  WHOLE_BLOOD: '전혈',
  PLATELET: '혈소판',
  PLASMA: '혈장',
  PLATELET_PLASMA: '혈소판혈장',
}

function formatCreatedAt(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}

function MedicalDonationHistoryPage() {
  const navigate = useNavigate()
  const [centers, setCenters] = useState([])
  const [centerId, setCenterId] = useState('')
  const [records, setRecords] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true
    getMedicalBloodUnitCenters()
      .then((data) => {
        if (active) setCenters(Array.isArray(data) ? data : [])
      })
      .catch((requestError) => {
        if (active) {
          setError(requestError.message || '센터 목록을 불러오지 못했습니다.')
        }
      })
    return () => {
      active = false
    }
  }, [])

  useEffect(() => {
    let active = true

    async function loadRecords() {
      try {
        setLoading(true)
        setError('')
        const data = await getMedicalDonationHistory(centerId)
        if (active) setRecords(Array.isArray(data) ? data : [])
      } catch (requestError) {
        if (active) {
          setError(requestError.message || '헌혈 기록을 불러오지 못했습니다.')
        }
      } finally {
        if (active) setLoading(false)
      }
    }

    loadRecords()
    return () => {
      active = false
    }
  }, [centerId])

  return (
    <main className="medical-donation-history">
      <header className="medical-donation-history__header">
        <div>
          <p>DONATION HISTORY</p>
          <h1>전체 헌혈 기록</h1>
          <span>완료된 헌혈 내역을 최신순으로 확인할 수 있습니다.</span>
        </div>
        <button type="button" onClick={() => navigate('/mypage/medical')}>
          의료진 마이페이지
        </button>
      </header>

      <section className="medical-donation-history__filter" aria-labelledby="history-center-filter-title">
        <div>
          <h2 id="history-center-filter-title">센터 선택</h2>
          <p>전체 센터 또는 선택한 센터에서 완료된 헌혈 기록을 조회합니다.</p>
        </div>
        <label>
          <span>조회 센터</span>
          <select value={centerId} onChange={(event) => setCenterId(event.target.value)}>
            <option value="">전체 센터</option>
            {centers.map((center) => (
              <option key={center.id} value={center.id}>
                {center.centerName} · {center.address}
              </option>
            ))}
          </select>
        </label>
      </section>

      <section className="medical-donation-history__panel">
        <div className="medical-donation-history__summary">
          <h2>헌혈 기록 목록</h2>
          <span>총 {records.length}건</span>
        </div>

        {loading ? (
          <div className="medical-donation-history__state">헌혈 기록을 불러오는 중입니다.</div>
        ) : error ? (
          <div className="medical-donation-history__state is-error" role="alert">{error}</div>
        ) : records.length === 0 ? (
          <div className="medical-donation-history__state">선택한 조건에 해당하는 헌혈 기록이 없습니다.</div>
        ) : (
          <div className="medical-donation-history__table-wrap">
            <table>
              <thead>
                <tr>
                  <th scope="col">기록 ID</th>
                  <th scope="col">회원 ID</th>
                  <th scope="col">헌혈자명</th>
                  <th scope="col">기록 생성일시</th>
                  <th scope="col">헌혈일</th>
                  <th scope="col">헌혈 종류</th>
                  <th scope="col">혈액원</th>
                  <th scope="col">헌혈 장소</th>
                </tr>
              </thead>
              <tbody>
                {records.map((record) => (
                  <tr key={record.id}>
                    <td data-label="기록 ID">{record.id}</td>
                    <td data-label="회원 ID">{record.memberId}</td>
                    <td data-label="헌혈자명" className="is-name">{record.donorName}</td>
                    <td data-label="기록 생성일시">{formatCreatedAt(record.createdAt)}</td>
                    <td data-label="헌혈일">{record.donationDate}</td>
                    <td data-label="헌혈 종류">{donationTypeLabels[record.donationType] || record.donationType}</td>
                    <td data-label="혈액원">{record.bloodCenterName}</td>
                    <td data-label="헌혈 장소">{record.donationPlaceName}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </main>
  )
}

export default MedicalDonationHistoryPage
