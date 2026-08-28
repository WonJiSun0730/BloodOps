import { useEffect, useState } from 'react'
import { getMyPoints, getRewardProducts } from '../../api/apiClient.js'
import './MyPointsPage.css'

const donationNames = {
  WHOLE_BLOOD: '전혈',
  PLASMA: '혈장',
  PLATELET: '혈소판단종',
  PLATELET_PLASMA: '혈소판다종',
}

function MyPointsPage() {
  const [pointData, setPointData] = useState({ balance: 0, transactions: [] })
  const [products, setProducts] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true
    Promise.all([getMyPoints(), getRewardProducts()])
      .then(([points, rewardProducts]) => {
        if (!active) return
        setPointData(points)
        setProducts(rewardProducts)
      })
      .catch((requestError) => {
        if (active) setError(requestError.message)
      })
    return () => { active = false }
  }, [])

  return (
    <div className="mypage-page reward-point-page">
      <section className="point-balance-card">
        <div>
          <span>AVAILABLE POINT</span>
          <h3>사용 가능한 포인트</h3>
          <p>헌혈 완료 후 헌혈 종류에 따른 포인트가 자동으로 적립됩니다.</p>
        </div>
        <strong>{Number(pointData.balance).toLocaleString()}P</strong>
      </section>

      {error && <p className="mypage-record-error">{error}</p>}

      <section className="point-policy-grid" aria-label="헌혈 종류별 포인트">
        {[['전혈', '5,000P'], ['혈장', '8,000P'], ['혈소판단종', '10,000P'], ['혈소판다종', '13,000P']].map(([name, points]) => (
          <article key={name}><span>{name}</span><strong>{points}</strong></article>
        ))}
      </section>

      <section className="point-section">
        <header><div><h3>포인트 내역</h3><p>최근 적립 및 사용 내역입니다.</p></div></header>
        {pointData.transactions.length === 0 ? (
          <div className="mypage-empty-state"><strong>포인트 내역이 없습니다.</strong><p>헌혈 완료 후 자동으로 표시됩니다.</p></div>
        ) : (
          <div className="point-history-list">
            {pointData.transactions.map((transaction) => (
              <article key={transaction.id}>
                <div><strong>{transaction.description}</strong><span>{donationNames[transaction.donationType] || '포인트'}</span></div>
                <div><strong className={transaction.points >= 0 ? 'earned' : 'used'}>{transaction.points >= 0 ? '+' : ''}{transaction.points.toLocaleString()}P</strong><time>{new Date(transaction.createdAt).toLocaleString('ko-KR')}</time></div>
              </article>
            ))}
          </div>
        )}
      </section>

      <section className="point-section">
        <header><div><h3>포인트 상품</h3><p>관리자가 등록한 교환 가능 상품입니다.</p></div><span>{products.length}개</span></header>
        <div className="reward-product-grid">
          {products.map((product) => (
            <article key={product.id} className={product.stockQuantity === 0 ? 'sold-out' : ''}>
              {product.imageUrl ? <img className="reward-product-image" src={product.imageUrl} alt={product.name} /> : <div className="reward-product-image placeholder">NO IMAGE</div>}
              <span>{product.stockQuantity === 0 ? '품절' : '교환 가능'}</span>
              <h4>{product.name}</h4><p>{product.description}</p>
              <div><strong>{product.pointPrice.toLocaleString()}P</strong><small>재고 {product.stockQuantity}개</small></div>
            </article>
          ))}
        </div>
      </section>
    </div>
  )
}

export default MyPointsPage
