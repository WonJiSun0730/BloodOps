import { useEffect, useState } from 'react'
import { getMyRewardOrders, revealRewardVoucherCode } from '../../api/apiClient.js'
import './MyRewardOrdersPage.css'

const statusNames={COMPLETED:'발급 완료',READY_FOR_PICKUP:'수령 대기',PICKED_UP:'수령 완료',CANCELED:'취소·환불'}

function MyRewardOrdersPage(){
 const [orders,setOrders]=useState([]),[codes,setCodes]=useState({}),[error,setError]=useState('')
 useEffect(()=>{getMyRewardOrders().then(setOrders).catch(e=>setError(e.message))},[])
 const reveal=async(id)=>{try{const r=await revealRewardVoucherCode(id);setCodes(v=>({...v,[id]:r.code}))}catch(e){setError(e.message)}}
 const copy=async(code)=>{
  try{
   await navigator.clipboard.writeText(code)
   window.alert(
    '상품권 코드가 클립보드에 복사되었습니다.\n타인에게 노출되지 않도록 주의해주세요.'
   )
  }catch(copyError){
   setError('상품권 코드를 복사하지 못했습니다. 직접 선택하여 복사해주세요.')
  }
 }
 return (
  <div className="mypage-page reward-orders-page">
   <section className="mypage-page-description">
    <div>
     <span>POINT REWARDS</span>
     <h3>상품권·교환 내역</h3>
     <p>발급받은 상품권과 실물 상품 수령 정보를 확인할 수 있습니다.</p>
    </div>
   </section>
   <p className="reward-orders-guide">디지털 코드는 본인만 확인할 수 있습니다. 실물 상품은 수령 기한 안에 선택한 센터를 방문해 수령 코드를 보여주세요.</p>
  {error&&<p className="mypage-record-error">{error}</p>}
  {orders.length===0?<div className="mypage-empty-state"><strong>교환 내역이 없습니다.</strong><p>포인트 상품을 교환하면 이곳에 표시됩니다.</p></div>:
  <div className="reward-order-list">{orders.map(o=><article key={o.id}>
   <header><div><span>{o.productType==='DIGITAL_VOUCHER'?'DIGITAL':'PICKUP'}</span><h3>{o.productName}</h3></div><strong>{statusNames[o.status]||o.status}</strong></header>
   <dl><div><dt>사용 포인트</dt><dd>{o.pointsSpent.toLocaleString()}P</dd></div><div><dt>신청일</dt><dd>{new Date(o.createdAt).toLocaleString('ko-KR')}</dd></div>
   {o.productType==='PHYSICAL'?<><div><dt>수령 센터</dt><dd>{o.pickupCenterName}</dd></div><div><dt>수령 코드</dt><dd className="reward-order-code">{o.pickupCode}</dd></div><div><dt>수령 기한</dt><dd>{o.pickupDeadline?new Date(o.pickupDeadline).toLocaleString('ko-KR'):'-'}</dd></div></>:<><div><dt>상품권 코드</dt><dd className="reward-order-code">{codes[o.id]||o.maskedVoucherCode}</dd></div><div><dt>유효 기한</dt><dd>{o.voucherExpiresAt?new Date(o.voucherExpiresAt).toLocaleDateString('ko-KR'):'-'}</dd></div></>}</dl>
   {o.productType==='DIGITAL_VOUCHER'&&<div className="reward-order-actions">{!codes[o.id]&&<button onClick={()=>reveal(o.id)}>코드 보기</button>}{codes[o.id]&&<button onClick={()=>copy(codes[o.id])}>코드 복사</button>}</div>}
  </article>)}</div>}
  </div>
 )
}

export default MyRewardOrdersPage
