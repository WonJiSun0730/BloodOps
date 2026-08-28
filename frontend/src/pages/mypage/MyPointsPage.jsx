import { useEffect, useState } from 'react'
import {
  getMyPoints,
  getRewardProducts,
  purchaseRewardProduct,
} from '../../api/apiClient.js'
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
  const [selectedProduct, setSelectedProduct] = useState(null)
  const [purchasing, setPurchasing] = useState(false)
  const [message, setMessage] = useState('')
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

  useEffect(() => {
    if (!selectedProduct) return undefined
    const closeOnEscape = (event) => {
      if (event.key === 'Escape') setSelectedProduct(null)
    }
    document.body.style.overflow = 'hidden'
    window.addEventListener('keydown', closeOnEscape)
    return () => {
      document.body.style.overflow = ''
      window.removeEventListener('keydown', closeOnEscape)
    }
  }, [selectedProduct])

  const purchase = async () => {
    if (!selectedProduct || purchasing) return
    setPurchasing(true); setError(''); setMessage('')
    try {
      const result = await purchaseRewardProduct(selectedProduct.id)
      const [points, rewardProducts] = await Promise.all([
        getMyPoints(),
        getRewardProducts(),
      ])
      setPointData(points)
      setProducts(rewardProducts)
      setSelectedProduct(null)
      setMessage(result.productName + ' 구매가 완료되었습니다. ' + result.pointsSpent.toLocaleString() + 'P가 사용되었습니다.')
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setPurchasing(false)
    }
  }

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

      {message && <p className="point-feedback success">{message}</p>}
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
        <header><div><h3>포인트 상품</h3><p>상품을 선택하면 상세 정보와 원본 이미지를 확인할 수 있습니다.</p></div><span>{products.length}개</span></header>
        <div className="reward-product-grid">
          {products.map((product) => (
            <button
              type="button"
              key={product.id}
              className={'reward-product-card ' + (product.stockQuantity === 0 ? 'sold-out' : '')}
              onClick={() => { setSelectedProduct(product); setError(''); setMessage('') }}
            >
              {product.imageUrl ? <img className="reward-product-image" src={product.imageUrl} alt={product.name} /> : <span className="reward-product-image placeholder">NO IMAGE</span>}
              <span className="reward-product-status">{product.stockQuantity === 0 ? '품절' : '교환 가능'}</span>
              <strong>{product.name}</strong>
              <p>{product.description}</p>
              <span className="reward-product-meta"><strong>{product.pointPrice.toLocaleString()}P</strong><small>재고 {product.stockQuantity}개</small></span>
            </button>
          ))}
        </div>
      </section>

      {selectedProduct && (
        <div
          className="reward-detail-backdrop"
          role="presentation"
          onMouseDown={(event) => {
            if (event.target === event.currentTarget) setSelectedProduct(null)
          }}
        >
          <section className="reward-detail-modal" role="dialog" aria-modal="true" aria-labelledby="reward-detail-title">
            <button type="button" className="reward-detail-close" aria-label="상품 상세 닫기" onClick={() => setSelectedProduct(null)}>×</button>
            <div className="reward-detail-image-shell">
              {selectedProduct.imageUrl
                ? <img src={selectedProduct.imageUrl} alt={selectedProduct.name} />
                : <span>NO IMAGE</span>}
            </div>
            <div className="reward-detail-content">
              <span className="reward-detail-eyebrow">POINT REWARD</span>
              <h3 id="reward-detail-title">{selectedProduct.name}</h3>
              <p>{selectedProduct.description}</p>
              <dl>
                <div><dt>필요 포인트</dt><dd>{selectedProduct.pointPrice.toLocaleString()}P</dd></div>
                <div><dt>현재 보유 포인트</dt><dd>{Number(pointData.balance).toLocaleString()}P</dd></div>
                <div><dt>남은 재고</dt><dd>{selectedProduct.stockQuantity}개</dd></div>
              </dl>
              <button
                type="button"
                className="reward-purchase-button"
                disabled={
                  purchasing
                  || selectedProduct.stockQuantity === 0
                  || Number(pointData.balance) < selectedProduct.pointPrice
                }
                onClick={purchase}
              >
                {purchasing
                  ? '구매 처리 중...'
                  : selectedProduct.stockQuantity === 0
                    ? '품절된 상품입니다'
                    : Number(pointData.balance) < selectedProduct.pointPrice
                      ? '포인트가 부족합니다'
                      : '구매하기'}
              </button>
            </div>
          </section>
        </div>
      )}
    </div>
  )
}

export default MyPointsPage
