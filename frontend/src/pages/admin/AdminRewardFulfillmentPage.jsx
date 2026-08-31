import { useEffect, useState } from 'react'
import AdminSectionNav from '../../components/admin/AdminSectionNav.jsx'
import { cancelRewardOrder, completeRewardPickup, generateRewardVoucherCodes, getAdminRewardOrders, getAdminRewardProducts } from '../../api/apiClient.js'
import './AdminRewardFulfillmentPage.css'

const statusNames={COMPLETED:'발급 완료',READY_FOR_PICKUP:'수령 대기',PICKED_UP:'수령 완료',CANCELED:'취소·환불'}
function AdminRewardFulfillmentPage(){
 const [products,setProducts]=useState([]),[orders,setOrders]=useState([]),[inputs,setInputs]=useState({}),[message,setMessage]=useState(''),[error,setError]=useState('')
 const load=()=>Promise.all([getAdminRewardProducts(),getAdminRewardOrders()]).then(([p,o])=>{setProducts(p);setOrders(o)})
 useEffect(()=>{load().catch(e=>setError(e.message))},[])
 const generate=async(id)=>{const v=inputs[id]||{quantity:10,validDays:365};try{await generateRewardVoucherCodes(id,v.quantity||10,v.validDays||365);setMessage('테스트용 상품권 코드가 입고되었습니다.');await load()}catch(e){setError(e.message)}}
 const action=async(id,type)=>{if(type==='cancel'&&!window.confirm('교환을 취소하고 포인트와 재고를 복구하시겠습니까?'))return;try{await(type==='pickup'?completeRewardPickup(id):cancelRewardOrder(id));setMessage(type==='pickup'?'수령 완료 처리했습니다.':'취소 및 포인트 환불을 완료했습니다.');await load()}catch(e){setError(e.message)}}
 return <main className="admin-reward-page"><AdminSectionNav/><header className="admin-reward-header"><span>REWARD FULFILLMENT</span><h1>상품권·실물 수령 관리</h1><p>테스트용 디지털 코드를 입고하고 실물 상품의 수령 상태를 처리합니다.</p></header>
 {message&&<p className="admin-reward-message success">{message}</p>}{error&&<p className="admin-reward-message error">{error}</p>}
 <section className="fulfillment-panel"><header><h2>디지털 코드 입고</h2></header>{products.filter(p=>p.productType==='DIGITAL_VOUCHER').map(p=>{const v=inputs[p.id]||{quantity:10,validDays:365};return <div className="voucher-stock-row" key={p.id}><strong>{p.name}</strong><span>사용 가능 {p.stockQuantity}개</span><label>수량<input type="number" min="1" max="100" value={v.quantity} onChange={e=>setInputs({...inputs,[p.id]:{...v,quantity:e.target.value}})}/></label><label>유효일<input type="number" min="1" max="3650" value={v.validDays} onChange={e=>setInputs({...inputs,[p.id]:{...v,validDays:e.target.value}})}/></label><button onClick={()=>generate(p.id)}>코드 생성·입고</button></div>})}</section>
 <section className="fulfillment-panel"><header><h2>교환 처리 내역</h2><span>{orders.length}건</span></header><div className="fulfillment-orders">{orders.map(o=><article key={o.id}><div><small>{o.productType}</small><strong>{o.productName}</strong><span>{o.pickupCenterName||o.maskedVoucherCode||'-'}</span></div><div><strong>{statusNames[o.status]||o.status}</strong><span>{o.pointsSpent.toLocaleString()}P</span></div>{o.status==='READY_FOR_PICKUP'&&<div className="fulfillment-actions"><button onClick={()=>action(o.id,'pickup')}>수령 완료</button><button className="danger" onClick={()=>action(o.id,'cancel')}>취소·환불</button></div>}</article>)}</div></section>
 </main>
}
export default AdminRewardFulfillmentPage
