import { useCallback, useEffect, useState } from 'react'
import { createMedicalBloodUnit, getMedicalBloodUnits, updateMedicalBloodUnit } from '../../api/apiClient.js'
import './BloodUnitsManagementPage.css'

const PAGE_SIZE=20
const emptyForm={donorId:'',bloodType:'A',rhType:'+',gender:'',birthDate:'',unitId:'',productType:'전혈',collectionDate:'',expirationDate:'',status:'보유중'}
const date=(v)=>v?new Intl.DateTimeFormat('ko-KR').format(new Date(v)):'-'

export default function BloodUnitsManagementPage(){
 const [data,setData]=useState(null),[page,setPage]=useState(0),[loading,setLoading]=useState(true),[error,setError]=useState('')
 const [filters,setFilters]=useState({keyword:'',bloodType:'',productType:'',status:'',collectionFrom:'',collectionTo:'',expirationFrom:'',expirationTo:''})
 const [form,setForm]=useState(emptyForm),[editingId,setEditingId]=useState(null),[showForm,setShowForm]=useState(false)
 const load=useCallback(async()=>{try{setLoading(true);setError('');setData(await getMedicalBloodUnits(filters,page,PAGE_SIZE))}catch(e){setError(e.message||'혈액 재고를 불러오지 못했습니다.')}finally{setLoading(false)}},[filters,page])
 useEffect(()=>{load()},[load])
 function field(e){setFilters({...filters,[e.target.name]:e.target.value});setPage(0)}
 function edit(u){setEditingId(u.id);setForm({...emptyForm,...u,birthDate:u.birthDate||'',collectionDate:u.collectionDate?.slice(0,16)||'',expirationDate:u.expirationDate?.slice(0,16)||''});setShowForm(true)}
 async function save(e){e.preventDefault();try{setError('');const body={...form,birthDate:form.birthDate||null}; editingId?await updateMedicalBloodUnit(editingId,body):await createMedicalBloodUnit(body);setShowForm(false);setEditingId(null);setForm(emptyForm);await load()}catch(err){setError(err.message||'저장에 실패했습니다.')}}
 return <main className="blood-units-page">
  <header className="blood-units-header"><div><p className="blood-units-eyebrow">MEDICAL · BLOOD UNITS</p><h1>혈액 재고 관리 대시보드</h1></div><button type="button" onClick={()=>{setForm(emptyForm);setEditingId(null);setShowForm(true)}}>혈액 팩 추가</button></header>
  {error&&<div className="blood-units-alert" role="alert">{error}<button onClick={load}>다시 시도</button></div>}
  <section className="blood-units-summary"><article><span>검색 결과</span><strong>{data?.totalElements??0} 팩</strong></article><article><span>사용 가능</span><strong className="success">{data?.available??0} 팩</strong></article><article><span>유효기간 7일 이내</span><strong className="warning">{data?.expiringSoon??0} 팩</strong></article><article><span>폐기/보류</span><strong className="danger">{data?.disposedOrHeld??0} 팩</strong></article></section>
  <section className="blood-units-filters">
   <input name="keyword" value={filters.keyword} onChange={field} placeholder="Unit ID 또는 헌혈자 ID" />
   <select name="bloodType" value={filters.bloodType} onChange={field}><option value="">전체 혈액형</option>{['A','B','O','AB'].map(v=><option key={v}>{v}</option>)}</select>
   <select name="productType" value={filters.productType} onChange={field}><option value="">전체 제제</option>{['전혈','혈장','혈소판단종','혈소판다종'].map(v=><option key={v}>{v}</option>)}</select>
   <select name="status" value={filters.status} onChange={field}><option value="">전체 상태</option>{['보유중','예약','보류','폐기'].map(v=><option key={v}>{v}</option>)}</select>
   <label>채혈 시작<input name="collectionFrom" type="date" value={filters.collectionFrom} onChange={field}/></label><label>채혈 종료<input name="collectionTo" type="date" value={filters.collectionTo} onChange={field}/></label>
   <label>유효 시작<input name="expirationFrom" type="date" value={filters.expirationFrom} onChange={field}/></label><label>유효 종료<input name="expirationTo" type="date" value={filters.expirationTo} onChange={field}/></label>
  </section>
  <section className="blood-units-table-card"><div className="blood-units-table-wrap"><table><thead><tr><th>Unit ID</th><th>혈액형</th><th>제제 종류</th><th>채혈일</th><th>유효기간</th><th>상태</th><th>관리</th></tr></thead><tbody>
   {loading?<tr><td colSpan="7" className="empty">조회 중...</td></tr>:(data?.content??[]).map(u=><tr key={u.id}><td className="unit-id">{u.unitId}</td><td className="blood-type">{u.bloodType}{u.rhType}</td><td>{u.productType}</td><td>{date(u.collectionDate)}</td><td>{date(u.expirationDate)}</td><td>{u.status}</td><td><button type="button" onClick={()=>edit(u)}>수정</button></td></tr>)}</tbody></table></div>
   {data?.totalPages>1&&<nav className="blood-units-pagination"><button disabled={!page} onClick={()=>setPage(page-1)}>이전</button><span>{page+1} / {data.totalPages}</span><button disabled={page+1>=data.totalPages} onClick={()=>setPage(page+1)}>다음</button></nav>}
  </section>
  {showForm&&<div className="blood-unit-modal" role="dialog"><form onSubmit={save}><h2>{editingId?'혈액 팩 수정':'혈액 팩 추가'}</h2>{Object.entries(form).map(([k,v])=>k==='bloodType'||k==='rhType'||k==='productType'||k==='status'?null:<label key={k}>{k}<input name={k} type={k.includes('Date')?'datetime-local':'text'} value={v??''} onChange={e=>setForm({...form,[k]:e.target.value})}/></label>)}<label>혈액형<select value={form.bloodType} onChange={e=>setForm({...form,bloodType:e.target.value})}>{['A','B','O','AB'].map(v=><option key={v}>{v}</option>)}</select></label><label>Rh<select value={form.rhType} onChange={e=>setForm({...form,rhType:e.target.value})}><option>+</option><option>-</option></select></label><label>제제<input value={form.productType} onChange={e=>setForm({...form,productType:e.target.value})}/></label><label>상태<input value={form.status} onChange={e=>setForm({...form,status:e.target.value})}/></label><div><button type="button" onClick={()=>setShowForm(false)}>취소</button><button type="submit">저장</button></div></form></div>}
 </main>
}
