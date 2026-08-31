import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getCenters, getMyPoints, getRewardProducts, purchaseRewardProduct } from '../../api/apiClient.js'
import './MyPointsPage.css'

const donationNames = { WHOLE_BLOOD:'전혈', PLASMA:'혈장', PLATELET:'혈소판단종', PLATELET_PLASMA:'혈소판다종' }

function MyPointsPage() {
  const [pointData,setPointData]=useState({balance:0,transactions:[]})
  const [products,setProducts]=useState([])
  const [centers,setCenters]=useState([])
  const [selected,setSelected]=useState(null)
  const [centerId,setCenterId]=useState('')
  const [centerSearch,setCenterSearch]=useState('')
  const [purchasing,setPurchasing]=useState(false)
  const [confirming,setConfirming]=useState(false)
  const [message,setMessage]=useState('')
  const [error,setError]=useState('')

  const reload=()=>Promise.all([getMyPoints(),getRewardProducts()]).then(([p,r])=>{setPointData(p);setProducts(r)})
  useEffect(() => {
    reload().catch((requestError) => {
      setError(requestError.message)
    })
  }, [])

  const selectProduct = async (product) => {
    setSelected(product)
    setConfirming(false)
    setCenterId('')
    setCenterSearch('')
    setCenters([])
    setError('')
    setMessage('')

    if (product.productType !== 'PHYSICAL') {
      return
    }

    try {
      const centerData = await getCenters()
      setCenters(
        Array.isArray(centerData)
          ? centerData
          : (centerData.content || [])
      )
    } catch (requestError) {
      setError(
        requestError.message
        || '수령센터 목록을 불러오지 못했습니다.'
      )
    }
  }

  const filteredCenters = centers.filter((center) => {
    const keyword = centerSearch.trim().toLowerCase()

    if (!keyword) {
      return true
    }

    return (
      center.centerName?.toLowerCase().includes(keyword)
      || center.address?.toLowerCase().includes(keyword)
    )
  })

  const openPurchaseConfirmation = () => {
    if (
      selected?.productType === 'PHYSICAL'
      && !centerId
    ) {
      setError(
        '수령할 헌혈의 집을 선택해주세요.'
      )
      return
    }

    setError('')
    setConfirming(true)
  }

  const purchase=async()=>{
    if(!selected||purchasing)return
    if((selected.productType||'PHYSICAL')==='PHYSICAL'&&!centerId){setError('수령할 헌혈의 집을 선택해주세요.');return}
    setPurchasing(true);setError('');setMessage('')
    try{
      const pickupCenterId =
        selected.productType === 'PHYSICAL'
          ? centerId
          : null

      const result =
        await purchaseRewardProduct(
          selected.id,
          pickupCenterId
        )
      await reload(); setSelected(null);setConfirming(false);setCenterId('')
      setMessage(result.productType==='DIGITAL_VOUCHER'
        ? '상품권 교환이 완료되었습니다. 상품권·교환 내역에서 코드를 확인하세요.'
        : '실물 상품 교환 신청이 완료되었습니다. 수령 코드: '+result.pickupCode)
    }catch(e){setError(e.message)}finally{setPurchasing(false)}
  }

  return <div className="mypage-page reward-point-page">
    <section className="mypage-page-description">
      <div>
        <span>DONATION POINT</span>
        <h3>내 포인트</h3>
        <p>헌혈로 적립한 포인트와 교환 가능한 상품을 확인할 수 있습니다.</p>
      </div>
    </section>

    <section className="point-balance-card"><div><span>AVAILABLE POINT</span><h3>사용 가능한 포인트</h3><p>포인트는 헌혈 완료 시에만 적립되며 현금 충전은 지원하지 않습니다.</p></div><strong>{Number(pointData.balance).toLocaleString()}P</strong></section>
    {message&&<p className="point-feedback success">{message} <Link to="/mypage/rewards">내역 보기</Link></p>}
    {error&&<p className="mypage-record-error">{error}</p>}
    <section className="point-policy-grid">{[['전혈','5,000P'],['혈장','8,000P'],['혈소판단종','10,000P'],['혈소판다종','13,000P']].map(([n,p])=><article key={n}><span>{n}</span><strong>{p}</strong></article>)}</section>
    <section className="point-section"><header><div><h3>포인트 내역</h3><p>최근 적립 및 사용 내역입니다.</p></div></header>
      {pointData.transactions.length===0?<div className="mypage-empty-state"><strong>포인트 내역이 없습니다.</strong></div>:<div className="point-history-list">{pointData.transactions.map(t=><article key={t.id}><div><strong>{t.description}</strong><span>{donationNames[t.donationType]||'포인트'}</span></div><div><strong className={t.points>=0?'earned':'used'}>{t.points>=0?'+':''}{t.points.toLocaleString()}P</strong><time>{new Date(t.createdAt).toLocaleString('ko-KR')}</time></div></article>)}</div>}
    </section>
    <section className="point-section"><header><div><h3>포인트 상품</h3><p>상품을 눌러 상세 정보와 교환 방법을 확인하세요.</p></div><span>{products.length}개</span></header>
      <div className="reward-product-grid">{products.map(p=><button type="button" key={p.id} className={'reward-product-card '+(p.stockQuantity===0?'sold-out':'')} onClick={() => selectProduct(p)}>
        {p.imageUrl?<img className="reward-product-image" src={p.imageUrl} alt={p.name}/>:<span className="reward-product-image placeholder">NO IMAGE</span>}
        <span className="reward-product-status">{p.stockQuantity===0?'품절':p.productType==='DIGITAL_VOUCHER'?'즉시 발급':'센터 수령'}</span><strong>{p.name}</strong><p>{p.description}</p><span className="reward-product-meta"><strong>{p.pointPrice.toLocaleString()}P</strong><small>재고 {p.stockQuantity}개</small></span>
      </button>)}</div>
    </section>
    {selected&&<div className="reward-detail-backdrop" role="presentation" onMouseDown={e=>{if(e.target===e.currentTarget){setSelected(null);setConfirming(false)}}}>
      <section className="reward-detail-modal" role="dialog" aria-modal="true"><button type="button" className="reward-detail-close" onClick={()=>{setSelected(null);setConfirming(false)}}>×</button>
        <div className="reward-detail-image-shell">{selected.imageUrl?<img src={selected.imageUrl} alt={selected.name}/>:<span>NO IMAGE</span>}</div>
        <div className="reward-detail-content"><span className="reward-detail-eyebrow">{selected.productType==='DIGITAL_VOUCHER'?'DIGITAL VOUCHER':'PHYSICAL REWARD'}</span><h3>{selected.name}</h3><p>{selected.description}</p>
          <dl><div><dt>필요 포인트</dt><dd>{selected.pointPrice.toLocaleString()}P</dd></div><div><dt>보유 포인트</dt><dd>{Number(pointData.balance).toLocaleString()}P</dd></div><div><dt>재고</dt><dd>{selected.stockQuantity}개</dd></div></dl>
          {selected.productType === 'PHYSICAL' && (
            <div className="reward-pickup-select">
              <label htmlFor="reward-center-search">
                수령할 헌혈의 집
              </label>
              <input
                id="reward-center-search"
                type="search"
                value={centerSearch}
                placeholder="센터명 또는 주소 검색"
                autoComplete="off"
                onChange={(event) => {
                  setCenterSearch(event.target.value)
                }}
              />
              <div
                className="reward-center-options"
                role="listbox"
                aria-label="수령센터 검색 결과"
              >
                {filteredCenters.length === 0 ? (
                  <p>검색 결과가 없습니다.</p>
                ) : (
                  filteredCenters.map((center) => (
                    <button
                      type="button"
                      key={center.id}
                      role="option"
                      aria-selected={
                        String(centerId)
                        === String(center.id)
                      }
                      className={
                        String(centerId)
                        === String(center.id)
                          ? 'selected'
                          : ''
                      }
                      onClick={() => {
                        setCenterId(center.id)
                      }}
                    >
                      <strong>{center.centerName}</strong>
                      <span>{center.address}</span>
                    </button>
                  ))
                )}
              </div>
              <small>
                신청 후 7일 이내 방문하여
                수령 코드를 보여주세요.
              </small>
            </div>
          )}
          {confirming ? (
            <section
              className="reward-purchase-confirmation"
              aria-live="polite"
            >
              <span>FINAL CONFIRMATION</span>
              <h4>포인트 상품을 교환하시겠습니까?</h4>
              <p>
                <strong>{selected.name}</strong> 교환에
                {' '}{selected.pointPrice.toLocaleString()}P가 사용됩니다.
              </p>
              <dl>
                <div>
                  <dt>교환 후 예상 잔액</dt>
                  <dd>
                    {
                      (
                        Number(pointData.balance)
                        - selected.pointPrice
                      ).toLocaleString()
                    }P
                  </dd>
                </div>
                <div>
                  <dt>수령 방식</dt>
                  <dd>
                    {selected.productType === 'DIGITAL_VOUCHER'
                      ? '디지털 코드 즉시 발급'
                      : '선택한 헌혈의 집 방문 수령'}
                  </dd>
                </div>
              </dl>
              <p className="reward-confirmation-warning">
                교환 완료 후에는 관리자 취소 처리가 필요한 경우를 제외하고
                사용자가 직접 취소할 수 없습니다.
              </p>
              <div className="reward-confirmation-actions">
                <button
                  type="button"
                  className="secondary"
                  disabled={purchasing}
                  onClick={() => {
                    setConfirming(false)
                  }}
                >
                  다시 확인
                </button>
                <button
                  type="button"
                  disabled={purchasing}
                  onClick={purchase}
                >
                  {purchasing
                    ? '교환 처리 중...'
                    : '확인하고 교환하기'}
                </button>
              </div>
            </section>
          ) : (
            <button
              type="button"
              className="reward-purchase-button"
              disabled={
                purchasing
                || selected.stockQuantity === 0
                || Number(pointData.balance)
                  < selected.pointPrice
              }
              onClick={openPurchaseConfirmation}
            >
              {selected.stockQuantity === 0
                ? '품절된 상품입니다'
                : Number(pointData.balance)
                  < selected.pointPrice
                  ? '포인트가 부족합니다'
                  : '포인트로 교환하기'}
            </button>
          )}
        </div>
      </section></div>}
  </div>
}
export default MyPointsPage
