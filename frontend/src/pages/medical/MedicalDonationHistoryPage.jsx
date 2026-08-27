import {
  useEffect,
  useState,
} from 'react'
import { useNavigate } from 'react-router-dom'

import {
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
  if (!value) {
    return '-'
  }

  return String(value)
    .replace('T', ' ')
    .slice(0, 19)
}


function MedicalDonationHistoryPage() {
  const navigate = useNavigate()
  const [records, setRecords] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true

    async function loadRecords() {
      try {
        const data = await getMedicalDonationHistory()

        if (active) {
          setRecords(Array.isArray(data) ? data : [])
        }
      } catch (requestError) {
        if (active) {
          setError(
            requestError.message
            || '헌혈 기록을 불러오지 못했습니다.',
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
    <main className="medical-donation-history">
      <header className="medical-donation-history__header">
        <div>
          <p>DONATION HISTORY</p>
          <h1>전체 헌혈 기록</h1>
          <span>
            donation_history에 저장된 완료 기록을 최신순으로 확인합니다.
          </span>
        </div>

        <button
          type="button"
          onClick={() => navigate('/mypage/medical')}
        >
          의료진 마이페이지
        </button>
      </header>

      <section className="medical-donation-history__panel">
        <div className="medical-donation-history__summary">
          <h2>헌혈 기록 목록</h2>
          <span>총 {records.length}건</span>
        </div>

        {loading ? (
          <div className="medical-donation-history__state">
            헌혈 기록을 불러오는 중입니다.
          </div>
        ) : error ? (
          <div className="medical-donation-history__state is-error">
            {error}
          </div>
        ) : records.length === 0 ? (
          <div className="medical-donation-history__state">
            저장된 헌혈 기록이 없습니다.
          </div>
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
                    <td>{record.id}</td>
                    <td>{record.memberId}</td>
                    <td className="is-name">{record.donorName}</td>
                    <td>{formatCreatedAt(record.createdAt)}</td>
                    <td>{record.donationDate}</td>
                    <td>
                      {donationTypeLabels[record.donationType]
                        || record.donationType}
                    </td>
                    <td>{record.bloodCenterName}</td>
                    <td>{record.donationPlaceName}</td>
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
