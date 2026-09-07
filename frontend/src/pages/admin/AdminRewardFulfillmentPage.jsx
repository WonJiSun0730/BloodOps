import { useCallback, useEffect, useState } from 'react'
import AdminSectionNav from '../../components/admin/AdminSectionNav.jsx'
import {
  addAdminTestPoints,
  cancelRewardOrder,
  completeRewardPickup,
  generateRewardVoucherCodes,
  getAdminRewardOrders,
  getAdminRewardProducts,
  getMedicalBloodUnitCenters,
} from '../../api/apiClient.js'
import './AdminRewardFulfillmentPage.css'

const PAGE_SIZE = 10
const statusNames = {
  COMPLETED: '발급 완료',
  READY_FOR_PICKUP: '수령 대기',
  PICKED_UP: '수령 완료',
  CANCELED: '취소·환불',
}

function AdminRewardFulfillmentPage() {
  const [products, setProducts] = useState([])
  const [centers, setCenters] = useState([])
  const [orders, setOrders] = useState([])
  const [centerId, setCenterId] = useState('')
  const [page, setPage] = useState(0)
  const [pageInfo, setPageInfo] = useState({ totalElements: 0, totalPages: 0 })
  const [inputs, setInputs] = useState({})
  const [testPoints, setTestPoints] = useState(10000)
  const [addingTestPoints, setAddingTestPoints] = useState(false)
  const [loading, setLoading] = useState(true)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([getAdminRewardProducts(), getMedicalBloodUnitCenters()])
      .then(([productData, centerData]) => {
        setProducts(Array.isArray(productData) ? productData : [])
        setCenters(Array.isArray(centerData) ? centerData : [])
      })
      .catch((requestError) => setError(requestError.message))
  }, [])

  const loadOrders = useCallback(async () => {
    try {
      setLoading(true)
      setError('')
      const data = await getAdminRewardOrders(centerId, page, PAGE_SIZE)
      setOrders(Array.isArray(data.content) ? data.content : [])
      setPageInfo({
        totalElements: Number(data.totalElements) || 0,
        totalPages: Number(data.totalPages) || 0,
      })
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setLoading(false)
    }
  }, [centerId, page])

  useEffect(() => {
    loadOrders()
  }, [loadOrders])

  const addTestPoints = async () => {
    try {
      setAddingTestPoints(true)
      setError('')
      setMessage('')
      const result = await addAdminTestPoints(testPoints)
      setMessage('관리자 본인에게 ' + Number(testPoints).toLocaleString() + 'P가 지급되었습니다. 현재 잔액: ' + Number(result.balance).toLocaleString() + 'P')
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setAddingTestPoints(false)
    }
  }

  const generate = async (id) => {
    const value = inputs[id] || { quantity: 10, validDays: 365 }
    try {
      setError('')
      await generateRewardVoucherCodes(id, value.quantity || 10, value.validDays || 365)
      setMessage('테스트용 상품권 코드가 입고되었습니다.')
      const productData = await getAdminRewardProducts()
      setProducts(Array.isArray(productData) ? productData : [])
    } catch (requestError) {
      setError(requestError.message)
    }
  }

  const action = async (id, type) => {
    if (type === 'cancel' && !window.confirm('교환을 취소하고 포인트와 재고를 복구하시겠습니까?')) return
    try {
      setError('')
      await (type === 'pickup' ? completeRewardPickup(id) : cancelRewardOrder(id))
      setMessage(type === 'pickup' ? '수령 완료 처리했습니다.' : '취소 및 포인트 환불을 완료했습니다.')
      await loadOrders()
    } catch (requestError) {
      setError(requestError.message)
    }
  }

  return (
    <main className="admin-reward-page">
      <AdminSectionNav />
      <header className="admin-reward-header">
        <span>REWARD FULFILLMENT</span>
        <h1>상품권·실물 수령 관리</h1>
        <p>테스트용 디지털 코드를 입고하고 실물 상품의 수령 상태를 처리합니다.</p>
      </header>

      {message && <p className="admin-reward-message success">{message}</p>}
      {error && <p className="admin-reward-message error" role="alert">{error}</p>}

      <section className="fulfillment-panel test-point-panel">
        <header><div><span>LOCAL TEST ONLY</span><h2>관리자 테스트 포인트</h2></div></header>
        <div className="test-point-form">
          <div><strong>관리자 본인 계정에만 지급됩니다.</strong><p>운영 배포 시 환경변수를 비활성화하거나 기능을 제거해야 합니다.</p></div>
          <label>지급 포인트<input type="number" min="1" max="1000000" step="1000" value={testPoints} onChange={(event) => setTestPoints(event.target.value)} /></label>
          <button type="button" disabled={addingTestPoints} onClick={addTestPoints}>{addingTestPoints ? '지급 중...' : '테스트 포인트 지급'}</button>
        </div>
      </section>

      <section className="fulfillment-panel">
        <header><h2>디지털 코드 입고</h2></header>
        {products.filter((product) => product.productType === 'DIGITAL_VOUCHER').map((product) => {
          const value = inputs[product.id] || { quantity: 10, validDays: 365 }
          return (
            <div className="voucher-stock-row" key={product.id}>
              <strong>{product.name}</strong>
              <span>사용 가능 {product.stockQuantity}개</span>
              <label>수량<input type="number" min="1" max="100" value={value.quantity} onChange={(event) => setInputs({ ...inputs, [product.id]: { ...value, quantity: event.target.value } })} /></label>
              <label>유효일<input type="number" min="1" max="3650" value={value.validDays} onChange={(event) => setInputs({ ...inputs, [product.id]: { ...value, validDays: event.target.value } })} /></label>
              <button type="button" onClick={() => generate(product.id)}>코드 생성·입고</button>
            </div>
          )
        })}
      </section>

      <section className="fulfillment-filter" aria-labelledby="fulfillment-center-filter-title">
        <div>
          <h2 id="fulfillment-center-filter-title">센터 선택</h2>
          <p>전체 센터 또는 실물 상품 수령 센터별 교환 내역을 조회합니다.</p>
        </div>
        <label>
          <span>조회 센터</span>
          <select value={centerId} onChange={(event) => { setCenterId(event.target.value); setPage(0) }}>
            <option value="">전체 센터</option>
            {centers.map((center) => (
              <option key={center.id} value={center.id}>{center.centerName} · {center.address}</option>
            ))}
          </select>
        </label>
      </section>

      <section className="fulfillment-panel">
        <header><h2>교환 처리 내역</h2><span>{pageInfo.totalElements}건</span></header>
        {loading ? (
          <p className="fulfillment-state">교환 처리 내역을 불러오는 중입니다.</p>
        ) : orders.length === 0 ? (
          <p className="fulfillment-state">선택한 조건에 해당하는 교환 내역이 없습니다.</p>
        ) : (
          <div className="fulfillment-orders">
            {orders.map((order) => (
              <article key={order.id}>
                <div>
                  <small>{order.productType === 'PHYSICAL' ? '실물 상품' : '디지털 상품권'}</small>
                  <strong>{order.productName}</strong>
                  {order.productType === 'PHYSICAL' ? (
                    <>
                      <span>수령 센터: {order.pickupCenterName || '-'}</span>
                      <span className="pickup-code">수령 코드: <strong>{order.pickupCode || '-'}</strong></span>
                    </>
                  ) : <span>{order.maskedVoucherCode || '-'}</span>}
                </div>
                <div><strong>{statusNames[order.status] || order.status}</strong><span>{Number(order.pointsSpent).toLocaleString()}P</span></div>
                {order.status === 'READY_FOR_PICKUP' && (
                  <div className="fulfillment-actions">
                    <button type="button" onClick={() => action(order.id, 'pickup')}>수령 완료</button>
                    <button type="button" className="danger" onClick={() => action(order.id, 'cancel')}>취소·환불</button>
                  </div>
                )}
              </article>
            ))}
          </div>
        )}
        {pageInfo.totalPages > 1 && (
          <nav className="fulfillment-pagination" aria-label="교환 처리 내역 페이지">
            <button type="button" disabled={page === 0} onClick={() => setPage((current) => current - 1)}>이전</button>
            <span>{page + 1} / {pageInfo.totalPages}</span>
            <button type="button" disabled={page + 1 >= pageInfo.totalPages} onClick={() => setPage((current) => current + 1)}>다음</button>
          </nav>
        )}
      </section>
    </main>
  )
}

export default AdminRewardFulfillmentPage
