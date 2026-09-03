import { useEffect, useState } from 'react'
import { getCurrentUser } from '../../api/apiClient.js'
import './QnaPage.css'

const emptyForm = { title: '', content: '' }

const frequentlyAskedQuestions = [
  {
    question: '헌혈 참여 후 다음 헌혈까지 얼마나 기다려야 하나요?',
    answer: `다음 헌혈까지 기다려야 하는 기간은 가장 마지막으로 참여한 헌혈 종류에 따라 달라집니다.

  전혈헌혈 헌혈주기: 8주
  성분헌혈 헌혈주기: 2주

  가장 마지막으로 '전혈헌혈'에 참여하셨다면 다음 헌혈은 종류에 상관없이 8주 후에 가능하고, '성분헌혈'에 참여하셨다면 다음 헌혈은 종류에 상관없이 2주 후에 가능합니다.

  단, 최근 1년간 참여한 헌혈 횟수, 최근 혈액검사 결과, 복용 중인 약물 및 질병 유무 등에 따라 다음 헌혈 가능일이 더 늦어질 수 있습니다.`,
  },
  {
    question: '다음 헌혈 주기가 평소보다 더 길어요. 이유가 무엇인가요?',
    answer: `헌혈자의 건강을 위하여 헌혈횟수 제한을 두고 있습니다.
  전혈헌혈을 하였을 경우 8주 후, 성분헌혈을 하였을 경우 2주 후 같은 요일부터 다음 헌혈이 가능합니다.
  또한, 최근 1년 이내 전혈헌혈은 5회, 혈소판 및 혈소판혈장성분헌혈은 24회로 제한됩니다.
  ※ 혈장성분헌혈은 최근 1년 이내 횟수 제한이 없습니다.

  평소보다 다음 헌혈 주기가 더 길어진 경우라면 최근 1년 이내에 헌혈횟수를 초과했는지 확인이 필요합니다.
  최근 1년 이내에 전혈헌혈 횟수가 5회이면 전혈헌혈이 제한되며, 과거 1년 이내에 성분헌혈 횟수가 24회일 경우 혈소판 및 혈소판혈장성분헌혈이 제한됩니다.
  ※ 헌혈횟수 제한은 매년이 아닌 최근 1년을 기준으로 합니다.
  ※ 전혈헌혈과 혈소판 및 혈소판혈장성분헌혈의 총 횟수에 따라서도 다음 헌혈 주기가 길어질 수 있습니다.

  예) 2023년 4월 20일을 시작으로 2023년 12월 31일까지 총 5번의 전혈헌혈을 했을 경우, 다음 헌혈 가능일은 2024년 4월 20일입니다.
  2023년 4월 20일 전혈헌혈
  2023년 7월 15일 전혈헌혈
  2023년 8월 26일 전혈헌혈
  2023년 10월 28일 전혈헌혈
  2023년 12월 31일 전혈헌혈
  ▶ 다음 헌혈 가능일 : 2024년 4월 20일`,
  },
  {
    question: '해외여행을 다녀온 후 헌혈할 수 있나요?',
    answer: `해외여행을 한 후, 귀국해서 4주가 지나지 않았을 경우, 헌혈을 할 수 없습니다. (2024.1.1.기준)
  해외에서 발생한 여러 감염병과 바이러스들이 수혈을 통해 국내에 퍼지는 것을 예방하기 위해서 입니다.
  4주 기간 동안 잠복해 있는 전염병이 있는지, 증상이 나타나는지 살펴보고 이상이 없다면 헌혈에 참여가 가능합니다.

  또한, 국외 말라리아 관련 헌혈 제한지역을 방문한 경우에는 혈장성분헌혈만 가능하며, 전혈헌혈 및 혈소판성분헌혈을 할 수 없습니다.
  ※ 국외 말라리아 관련 헌혈 제한지역에 거주나 복무(연중 6개월 이상)할 경우 3년간, 여행(연중 1일 이상 ~ 6개월 미만 숙박)할 경우 1년간 전혈헌혈 및 혈소판성분헌혈이 불가하며, 혈장성분헌혈만 가능합니다.`,
  },
  {
    question: '헌혈 예약은 어디에서 확인하거나 취소할 수 있나요?',
    answer: '로그인 후 마이페이지의 예약 내역에서 본인의 예약 정보를 확인하고 취소할 수 있습니다.',
  },
  {
    question: '전자문진을 작성하면 바로 헌혈할 수 있나요?',
    answer: '전자문진은 헌혈 가능 여부를 미리 확인하는 절차입니다. 방문 당일 신분 확인과 의료진 상담 결과에 따라 최종 헌혈 가능 여부가 결정됩니다.',
  },
  {
    question: '헌혈 기록은 언제 확인할 수 있나요?',
    answer: '헌혈이 정상적으로 완료 처리된 뒤 마이페이지의 헌혈 기록에서 확인할 수 있습니다.',
  },
  {
    question: '성분헌혈이 무엇인가요?',
    answer: `▶ 성분헌혈이란?
  혈액은 크게 액체성분(혈장)과 세포성분(적혈구, 백혈구, 혈소판)으로 구성되는데 우리가 일반적으로 알고 있는 헌혈은 이런 혈액성분의 전체, 즉 전혈(全血, whole blood)을 헌혈하는 것입니다.
  성분헌혈은 성분채혈기를 이용하여 적혈구, 혈소판, 혈장, 또는 백혈구 성분만을 선택적으로 채혈하고 나머지 혈액 성분은 다시 헌혈자에게 되돌려 주는 헌혈방식입니다.
  예를 들어 혈장성분헌혈이라고 하면 헌혈자로부터 혈장만을 취하고 나머지 성분, 즉 세포 성분은 다시 헌혈자의 체내로 되돌려주는 것입니다. 전혈헌혈과는 달리 헌혈과정 중에 혈액의 구성성분을 분리하여야 하기 때문에 성분채혈기라는 특수한 장비를 사용하게 됩니다.

  ▶ 성분헌혈은 어떻게 하나요?
  성분채혈기를 이용하여 헌혈자로부터 우선 전혈을 채혈하여 성분채혈기 내에서 전혈이 각각의 구성성분으로 분리되고 이중 얻고자 하는 성분만 채집백에 옮겨지고 나머지 부분은 헌혈자에게 재주입 되며 이러한 과정을 여러 번 반복해야 하기 때문에 일반적인 전혈헌혈 보다는 헌혈하는 데에 시간이 더 소요됩니다(혈장성분헌혈 : 30-40분, 혈소판성분헌혈 및 혈소판혈장성분헌혈: 60-90분).`,
  },
  {
    question: '성분헌혈은 왜 필요한가요?',
    answer: `성분헌혈 중에서 혈소판 헌혈이 가장 많이 시행되고 있는데 성분헌혈로 얻은 성분채집 혈소판은 전혈로부터 성분 분리하여 얻은 혈소판제제 약 6단위에 해당되는 혈소판을 한 명의 헌혈자로부터 얻을 수 있습니다.
  약 6명이 헌혈한 혈소판제제를 수혈하는 것보다 한 명의 헌혈자로부터 얻은 성분채집 혈소판을 수혈하는 것이 혈소판감소증 환자에게 큰 도움이 됩니다. 성분헌혈로 얻은 혈장은 알부민, 혈액응고인자 등 중요한 혈액분획제제를 만드는데 주로 사용됩니다.`,
  },
  {
    question: '혈액은 어떻게 사용되나요?',
    answer: `- 전혈 : 심한 출혈환자(총 혈액량의 30% 이상 출혈), 신생아 교환수혈
  - 농축적혈구 : 산소운반 능력의 부족을 보이는 각종 빈혈 환자 총 혈액량의 15% 이상의 출혈환자, 장기이식 등
  - 농축혈소판 : 혈소판감소증, 혈소판기능장애 환자, 백혈병, 재생불량성 빈혈, 항암제 치료 등
  - 신선동결혈장 : 주로 혈액응고인자 결핍의 보충을 위해 사용
  - 동결침전제제 : 혈우병, 선천성 혹은 후천성 fibrinogen 결핍증, 제8응고인자 결핍증 및 산과적 합병증 또는 파종성혈관내응고 등 fibrinogen의 소모와 관계된 질환`,
  },
  {
    question: '헌혈 증명서는 어디에서 발급하나요?',
    answer: '로그인 후 증명서 발급 화면에서 발급 가능한 헌혈 기록을 선택해 신청할 수 있습니다.',
  },
  {
    question: '헌혈 포인트는 언제 지급되나요?',
    answer: '헌혈 완료 처리가 끝나면 헌혈 종류에 따른 포인트가 지급되며, 마이페이지의 내 포인트에서 내역을 확인할 수 있습니다.',
  },
  {
    question: '헌혈의 집 운영시간을 알고 싶은데요?',
    answer: `전국 15개 혈액원에서는 현재 140여개가 넘는 헌혈의 집을 운영하고 있으며 헌혈의 집 운영시간은 평일 09:00~18:00, 토요일 09:00~17:00까지를 기본으로 하고 있습니다.
    다만, 헌혈의 집별로 운영요일, 운영시간이 상이할 수 있습니다.

  홈페이지 [헌혈참여] - [헌혈의집 찾기] 에서 헌혈의집을 검색하고 [상세보기]를 통해 운영시간을 확인하시기 바랍니다.`,
  },
  {
    question: '헌혈예약은 무엇인가요?',
    answer: `헌혈하기 전에 헌혈장소, 날짜, 시간 및 헌혈종류를 미리 예약하고 헌혈에 참여하는 것을 말합니다.
  홈페이지(퀵메뉴-헌혈참여-헌혈예약)를 통해 예약하실 수 있으며 예약 후 헌혈에 참여하시면 헌혈의집에서 기다리는 시간이 줄어듭니다.`,
  },
  {
    question: '헌혈을 하려면 신분증이 꼭 필요한가요?',
    answer: `혈액관리법 시행규칙에 따라 개인신상정보가 확인된 분들만 헌혈에 참여하실 수 있도록 헌혈 전에 신분증을 확인합니다.
    신분증을 확인함으로써 헌혈자는 헌혈기록 및 검사결과의 정확한 관리가 가능하고 수혈자는 대리헌혈 및 검사목적의 헌혈이 방지되어 안전한 혈액을 수혈 받을 수 있습니다.`,
  },
  {
    question: '헌혈을 해도 건강에 지장이 없나요?',
    answer: `우리 몸은 체중 1kg당 남자는 약 80mL, 여자는 약 70mL의 혈액이 있습니다. 따라서 체중이 60kg인 남자는 약 4,800mL, 50kg인 여자는 약 3,500mL 정도의 혈액을 가지고 있으며, 매일 일정량 새로 만들어 집니다. 즉, 우리 몸은 충분한 양의 혈액을 가지고 있기 때문에 건강한 성인인 경우 320mL 또는 400mL 정도의 헌혈은 일상생활이나 건강에 영향을 주지 않습니다.`,
  },
  {
    question: '헌혈하기 전에 주의해야 할 점은 무엇인가요?',
    answer: `과음과 과로를 하지 않고 제때에 식사를 한 상태가 좋습니다. 헌혈 전에 적어도 72시간 동안은 혈소판 기능을 저하시키는 아스피린, 치료목적의 약물을 복용하지 않아야 하며, 전신상태가 양호한 경우에 헌혈에 참여하실 수 있습니다. 단, 복용하신 약물의 종류나 기타 건강상태에 따라 헌혈 유보 기간이 달라질 수 있습니다.`,
  },
  {
    question: '헌혈한 후에 원상태로 회복하는데 시간이 얼마나 걸리나요?',
    answer: `혈액량이나 혈장은 수일내, 적혈구도 수주 내에 헌혈 전 상태로 회복됩니다. 그 이전이더라도 헌혈로 인해 빈혈이 초래되는 것은 아니므로 헌혈자의 건강에 지장을 주는 것은 아니나 헌혈 당일에는 충분한 수분섭취 및 휴식을 취하시기 바랍니다.`,
  },
  {
    question: '혈액사용보류 신청이 무엇인가요?',
    answer: `혈액사용보류 신청이란 헌혈 전 문진 내용에 사실과 다르게 응답(질병력, 약물 복용력, 말라리아 관련 헌혈 제한(위험)지역여행 및 거주, 영국, 유럽 등 인간광우병 위험지역 거주 등)하거나 헌혈 후 발생한 질환으로 인해 수혈자의 안전을 지키기 위해서 본인의 혈액이 다른 사람에게 수혈되지 않도록 신청을 하는 것입니다.
  (신청방법 : 홈페이지 [헌혈활동] - [혈액 사용 보류신청])`,
  },
]

async function request(url, options = {}) {
  const response = await fetch(url, { credentials: 'include', ...options })
  if (!response.ok) {
    let message = `요청 처리에 실패했습니다. (${response.status})`
    try { message = (await response.json()).message || message } catch { /* 응답 본문 없음 */ }
    throw new Error(message)
  }
  if (response.status === 204) return null
  return response.json()
}

async function mutation(url, method, body) {
  const csrf = await request('/api/auth/csrf')
  return request(url, {
    method,
    headers: {
      'Content-Type': 'application/json',
      [csrf.headerName]: csrf.token,
    },
    body: body ? JSON.stringify(body) : undefined,
  })
}

function QnaPage() {
  const [result, setResult] = useState({ content: [], totalPages: 0, number: 0 })
  const [keyword, setKeyword] = useState('')
  const [searchKeyword, setSearchKeyword] = useState('')
  const [selected, setSelected] = useState(null)
  const [form, setForm] = useState(emptyForm)
  const [answer, setAnswer] = useState('')
  const [mode, setMode] = useState('list')
  const [user, setUser] = useState(null)
  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(false)

  const isAdmin = user?.roles?.includes('ROLE_ADMIN')

  const loadList = async (page = 0, term = searchKeyword) => {
    setLoading(true)
    setMessage('')
    try {
      const data = await request(`/api/qna?page=${page}&keyword=${encodeURIComponent(term)}`)
      setResult(data)
      setMode('list')
      setSelected(null)
    } catch (error) {
      setMessage(error.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    getCurrentUser().then(setUser).catch(() => setUser(null))
    loadList(0, '')
  }, [])

  const openDetail = async id => {
    setLoading(true)
    try {
      const data = await request(`/api/qna/${id}`)
      setSelected(data)
      setAnswer(data.answer || '')
      setMode('detail')
      setMessage('')
    } catch (error) {
      setMessage(error.message)
    } finally {
      setLoading(false)
    }
  }

  const submitQuestion = async event => {
    event.preventDefault()
    setLoading(true)
    try {
      const url = mode === 'edit' ? `/api/qna/${selected.id}` : '/api/qna'
      const saved = await mutation(url, mode === 'edit' ? 'PUT' : 'POST', form)
      setSelected(saved)
      setMode('detail')
      setMessage(mode === 'edit' ? '질문이 수정되었습니다.' : '질문이 등록되었습니다.')
    } catch (error) {
      setMessage(error.message)
    } finally {
      setLoading(false)
    }
  }

  const deleteQuestion = async () => {
    if (!window.confirm('이 질문을 삭제하시겠습니까?')) return
    setLoading(true)
    try {
      await mutation(`/api/qna/${selected.id}`, 'DELETE')
      await loadList(0)
      setMessage('질문이 삭제되었습니다.')
    } catch (error) {
      setMessage(error.message)
      setLoading(false)
    }
  }

  const submitAnswer = async event => {
    event.preventDefault()
    setLoading(true)
    try {
      const saved = await mutation(`/api/qna/${selected.id}/answer`, 'PUT', { answer })
      setSelected(saved)
      setMessage('답변이 등록되었습니다.')
    } catch (error) {
      setMessage(error.message)
    } finally {
      setLoading(false)
    }
  }

  const startWrite = () => {
    if (!user) {
      setMessage('로그인 후 질문을 작성할 수 있습니다.')
      return
    }
    setForm(emptyForm)
    setMode('write')
    setMessage('')
  }

  const startEdit = () => {
    setForm({ title: selected.title, content: selected.content })
    setMode('edit')
    setMessage('')
  }

  return (
    <main className="qna-page">
      <header className="qna-heading">
        <p className="qna-eyebrow">고객지원</p>
        <h1>Q&amp;A</h1>
        <p>헌혈 예약과 BloodOps 이용 중 궁금한 내용을 남겨주세요.</p>
      </header>

      {message && <div className="qna-message" role="alert">{message}</div>}

      {mode === 'list' && (
        <section className="qna-faq" aria-labelledby="qna-faq-title">
          <div className="qna-section-heading">
            <p>FAQ</p>
            <h2 id="qna-faq-title">자주 묻는 질문</h2>
          </div>
          <div className="qna-faq-list">
            {frequentlyAskedQuestions.map((item, index) => (
              <details key={item.question}>
                <summary>
                  <span className="qna-faq-number">Q{index + 1}</span>
                  <span>{item.question}</span>
                </summary>
                <div className="qna-faq-answer">
                  <strong aria-hidden="true">A</strong>
                  <p>{item.answer}</p>
                </div>
              </details>
            ))}
          </div>
        </section>
      )}

      {mode === 'list' && (
        <>
          <div className="qna-toolbar">
            <form onSubmit={event => {
              event.preventDefault()
              const term = keyword.trim()
              setSearchKeyword(term)
              loadList(0, term)
            }}>
              <label className="sr-only" htmlFor="qna-search">질문 검색</label>
              <input id="qna-search" value={keyword}
                onChange={event => setKeyword(event.target.value)}
                placeholder="제목 또는 내용 검색" maxLength="100" />
              <button type="submit">검색</button>
            </form>
            <button className="qna-primary" type="button" onClick={startWrite}>질문하기</button>
          </div>

          <div className="qna-table-wrap">
            <table className="qna-table">
              <thead><tr><th>번호</th><th>상태</th><th>제목</th><th>작성자</th><th>등록일</th></tr></thead>
              <tbody>
                {!loading && result.content.length === 0 && (
                  <tr><td colSpan="5" className="qna-empty">등록된 질문이 없습니다.</td></tr>
                )}
                {result.content.map(item => (
                  <tr key={item.id} onClick={() => openDetail(item.id)}>
                    <td>{item.id}</td>
                    <td><span className={item.answered ? 'answered' : 'waiting'}>
                      {item.answered ? '답변완료' : '답변대기'}
                    </span></td>
                    <td className="qna-title">{item.title}</td>
                    <td>{item.authorName}</td>
                    <td>{new Date(item.createdAt).toLocaleDateString('ko-KR')}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <nav className="qna-pagination" aria-label="Q&A 페이지">
            {Array.from({ length: result.totalPages }, (_, index) => (
              <button key={index} className={result.number === index ? 'active' : ''}
                onClick={() => loadList(index)} aria-current={result.number === index ? 'page' : undefined}>
                {index + 1}
              </button>
            ))}
          </nav>
        </>
      )}

      {(mode === 'write' || mode === 'edit') && (
        <form className="qna-form" onSubmit={submitQuestion}>
          <label htmlFor="qna-title">제목 <span aria-hidden="true">*</span></label>
          <input id="qna-title" value={form.title} maxLength="150" required
            onChange={event => setForm({ ...form, title: event.target.value })} />
          <label htmlFor="qna-content">문의 내용 <span aria-hidden="true">*</span></label>
          <textarea id="qna-content" value={form.content} maxLength="5000" required
            onChange={event => setForm({ ...form, content: event.target.value })} />
          <p className="qna-guide">개인정보와 주민등록번호는 입력하지 마세요.</p>
          <div className="qna-actions">
            <button type="button" onClick={() => selected ? setMode('detail') : loadList(0)}>취소</button>
            <button className="qna-primary" disabled={loading}>저장</button>
          </div>
        </form>
      )}

      {mode === 'detail' && selected && (
        <article className="qna-detail">
          <div className="qna-detail-header">
            <span className={selected.answered ? 'answered' : 'waiting'}>
              {selected.answered ? '답변완료' : '답변대기'}
            </span>
            <h2>{selected.title}</h2>
            <p>{selected.authorName} · {new Date(selected.createdAt).toLocaleString('ko-KR')}</p>
          </div>
          <section><h3>문의 내용</h3><p>{selected.content}</p></section>
          <section className="qna-answer"><h3>답변</h3>
            <p>{selected.answer || '관리자가 문의 내용을 확인하고 있습니다.'}</p>
          </section>

          {isAdmin && (
            <form className="qna-answer-form" onSubmit={submitAnswer}>
              <label htmlFor="qna-answer">관리자 답변</label>
              <textarea id="qna-answer" value={answer} maxLength="5000" required
                onChange={event => setAnswer(event.target.value)} />
              <button className="qna-primary" disabled={loading}>답변 저장</button>
            </form>
          )}

          <div className="qna-actions">
            <button onClick={() => loadList(result.number)}>목록</button>
            {selected.mine && !selected.answered && <button onClick={startEdit}>수정</button>}
            {(selected.mine || isAdmin) && <button className="qna-danger" onClick={deleteQuestion}>삭제</button>}
          </div>
        </article>
      )}
    </main>
  )
}

export default QnaPage
