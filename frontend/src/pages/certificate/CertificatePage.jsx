import { useMemo, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { apiRequest, getCsrfToken, getCurrentUser } from '../../api/apiClient.js'
import './CertificatePage.css'

const TYPE='DONATION_CONFIRMATION'
const labels={WHOLE_BLOOD:'전혈',PLASMA:'혈장',PLATELET:'혈소판',PLATELET_PLASMA:'혈소판혈장'}
const date=(v)=>v?new Intl.DateTimeFormat('ko-KR').format(new Date(v)):'-'

function Document({data}){
 if(!data)return null
 return <section className="certificate-document">
  <p className="form-name">[별지 제4-1호 서식] 헌혈확인증명서(개인)</p>
  <h2>헌혈확인증명서(개인)</h2>
  <p className="page-number">NO. 1/1</p>
  <table className="certificate-info"><tbody>
   <tr>{data.purpose==='제출용'?<><th>발급번호</th><td>{data.issueNumber}</td><th>헌혈횟수</th><td>{data.donationCount}</td><th>사용용도</th><td>{data.purpose}</td></>:<><th>헌혈횟수</th><td colSpan="2">{data.donationCount}</td><th>사용용도</th><td colSpan="2">{data.purpose}</td></>}</tr>
   <tr><th>성명</th><td>{data.holderName}</td><th>생년월일</th><td>{date(data.birthDate)}</td><th>증명서</th><td>온라인 발급</td></tr>
  </tbody></table>
  <table className="donation-table"><thead><tr><th>번호</th><th>증서번호</th><th>헌혈종류</th><th>헌혈일자</th><th>헌혈장소</th></tr></thead>
   <tbody>{data.donations.map((d,i)=><tr key={d.id}><td>{i+1}</td><td>BO-{String(d.id).padStart(8,'0')}</td><td>{labels[d.donationType]||d.donationType}</td><td>{d.donationDate}</td><td>{d.donationPlaceName}</td></tr>)}</tbody>
  </table>
  <p className="statement">위와 같이 {data.periodStart} 부터 {data.periodEnd} 까지의 헌혈 사실을 확인함.</p>
  <p className="issue-date">{date(data.issuedAt)}</p><strong className="issuer">B l o o d O p s</strong>
 </section>
}

export default function CertificatePage(){
 const navigate=useNavigate()
 const location=useLocation()
 const today=useMemo(()=>new Date().toISOString().slice(0,10),[])
 const [tab,setTab]=useState('issue'),[message,setMessage]=useState(''),[loading,setLoading]=useState(false)
 const [form,setForm]=useState({certificateType:TYPE,purpose:'조회용',periodStart:'2000-01-01',periodEnd:today})
 const [numberParts,setNumberParts]=useState({prefix:'',issuedDate:'',serial:''})
 const [numberErrors,setNumberErrors]=useState({prefix:'',issuedDate:'',serial:''})
 const [issued,setIssued]=useState(null),[verified,setVerified]=useState(null)
 const send=async(url,body)=>{const csrf=await getCsrfToken();return apiRequest(url,{method:'POST',headers:{'Content-Type':'application/json',[csrf.headerName]:csrf.token},body:JSON.stringify(body)})}
 const issue=async e=>{e.preventDefault();setMessage('');setIssued(null);try{const currentUser=await getCurrentUser();if(!currentUser?.identifier){navigate('/login',{state:{from:location}});return}}catch{navigate('/login',{state:{from:location}});return}setLoading(true);try{const r=await send('/api/certificates',form);setIssued(r);setMessage(r.purpose==='제출용'?'제출용 헌혈확인증명서가 발급되었습니다.':'조회용 헌혈확인증명서가 생성되었습니다.')}catch(x){setMessage(x.message)}finally{setLoading(false)}}
 const updateNumberPart=(name,value)=>{const cleaned=name==='prefix'?value.replace(/[^a-zA-Z]/g,'').toUpperCase().slice(0,2):value.replace(/\D/g,'').slice(0,name==='issuedDate'?8:6);setNumberParts(previous=>({...previous,[name]:cleaned}));setNumberErrors(previous=>({...previous,[name]:''}));setMessage('')}
 const validateNumber=()=>{const errors={prefix:'',issuedDate:'',serial:''};if(numberParts.prefix!=='BO')errors.prefix='BO를 입력해주세요.';if(numberParts.issuedDate.length!==8){errors.issuedDate='발급일자 8자리를 입력해주세요.'}else{const year=Number(numberParts.issuedDate.slice(0,4)),month=Number(numberParts.issuedDate.slice(4,6)),day=Number(numberParts.issuedDate.slice(6,8)),value=new Date(year,month-1,day);if(value.getFullYear()!==year||value.getMonth()!==month-1||value.getDate()!==day)errors.issuedDate='유효한 발급일자를 입력해주세요.'}if(numberParts.serial.length!==6)errors.serial='일련번호 6자리를 입력해주세요.';setNumberErrors(errors);return !errors.prefix&&!errors.issuedDate&&!errors.serial}
 const verify=async e=>{e.preventDefault();setMessage('');setVerified(null);if(!validateNumber())return;setLoading(true);const issueNumber=[numberParts.prefix,numberParts.issuedDate,numberParts.serial].join('-');try{const r=await send('/api/certificates/verify',{certificateType:TYPE,issueNumber});setVerified(r);setMessage('정상 발급된 증명서입니다.')}catch(x){setMessage(x.message)}finally{setLoading(false)}}
 return <main className="certificate-page">
  <header className="certificate-hero"><span>CERTIFICATE SERVICE</span><h1>증명서 발급</h1><p>헌혈확인증명서를 온라인으로 발급하고 진위여부를 확인할 수 있습니다.</p></header>
  <div className="certificate-tabs" role="tablist" aria-label="증명서 서비스"><button type="button" role="tab" aria-selected={tab==='issue'} className={tab==='issue'?'active':''} onClick={()=>{setTab('issue');setMessage('');setVerified(null)}}>증명서 발급</button><button type="button" role="tab" aria-selected={tab==='verify'} className={tab==='verify'?'active':''} onClick={()=>{setTab('verify');setMessage('');setIssued(null)}}>진위여부 확인</button></div>
  <section className="certificate-panel">{tab==='issue'?<form onSubmit={issue}>
   <h2>헌혈확인증명서 신청</h2><p>온라인 발급만 제공합니다. 헌혈지원확인증명서와 우편·팩스 발급은 제외됩니다.</p>
   <label>증명서 종류<select disabled><option>헌혈확인증명서</option></select></label>
   <fieldset className="purpose-fieldset"><legend>사용 용도</legend><div className="purpose-options">{[["조회용","내 헌혈 기록을 확인합니다."],["제출용","발급번호가 표시되며 진위확인이 가능합니다."]].map(([value,description])=><label key={value} className={form.purpose===value?"purpose-option selected":"purpose-option"}><input type="radio" name="purpose" value={value} checked={form.purpose===value} onChange={e=>setForm({...form,purpose:e.target.value})}/><span><strong>{value}</strong><small>{description}</small></span></label>)}</div></fieldset>
   <div className="period"><label>조회 시작일<input type="date" required max={form.periodEnd} value={form.periodStart} onChange={e=>setForm({...form,periodStart:e.target.value})}/></label><label>조회 종료일<input type="date" required min={form.periodStart} max={today} value={form.periodEnd} onChange={e=>setForm({...form,periodEnd:e.target.value})}/></label></div>
   <button type="submit" className="primary" disabled={loading}>{loading?'처리 중...':form.purpose+' 증명서 발급'}</button>
  </form>:<form onSubmit={verify}>
   <h2>증명서 진위여부 확인</h2><p>제출용 증명서에 표시된 발급번호를 입력해주세요.</p>
   <label>증명서 종류<select disabled><option>헌혈확인증명서</option></select></label>
   <fieldset className="issue-number-fieldset"><legend>발급번호</legend><div className="issue-number-fields">
    <label><span>구분</span><input className={numberErrors.prefix?'input-error':''} value={numberParts.prefix} maxLength="2" autoComplete="off" aria-label="발급번호 구분" aria-invalid={Boolean(numberErrors.prefix)} placeholder="BO" onChange={e=>updateNumberPart('prefix',e.target.value)}/>{numberErrors.prefix&&<small className="issue-number-field-error">{numberErrors.prefix}</small>}</label>
    <span className="issue-number-separator" aria-hidden="true">-</span>
    <label><span>발급일자</span><input className={numberErrors.issuedDate?'input-error':''} value={numberParts.issuedDate} maxLength="8" inputMode="numeric" autoComplete="off" aria-label="발급번호 발급일자" aria-invalid={Boolean(numberErrors.issuedDate)} placeholder="YYYYMMDD" onChange={e=>updateNumberPart('issuedDate',e.target.value)}/>{numberErrors.issuedDate&&<small className="issue-number-field-error">{numberErrors.issuedDate}</small>}</label>
    <span className="issue-number-separator" aria-hidden="true">-</span>
    <label><span>일련번호</span><input className={numberErrors.serial?'input-error':''} value={numberParts.serial} maxLength="6" inputMode="numeric" autoComplete="off" aria-label="발급번호 일련번호" aria-invalid={Boolean(numberErrors.serial)} placeholder="000000" onChange={e=>updateNumberPart('serial',e.target.value)}/>{numberErrors.serial&&<small className="issue-number-field-error">{numberErrors.serial}</small>}</label>
   </div></fieldset>
   <button type="submit" className="primary" disabled={loading}>{loading?'조회 중...':'진위여부 확인'}</button>
   {verified&&<div className="verified"><strong>정상 발급</strong><span>{verified.holderName} · 헌혈 {verified.donationCount}회 · {date(verified.issuedAt)}</span></div>}
  </form>}
  {message&&<p className={(issued||verified)?'message success':'message'} role="alert">{message}</p>}</section>
  {issued&&<><Document data={issued}/><button type="button" className="print-button" onClick={()=>window.print()}>인쇄 / PDF 저장</button></>}
 </main>
}
