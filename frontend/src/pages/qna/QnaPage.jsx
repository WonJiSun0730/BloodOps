import { useEffect, useState } from 'react'
import { getCurrentUser } from '../../api/apiClient.js'
import './QnaPage.css'

const emptyForm = { title: '', content: '' }

const frequentlyAskedQuestions = [
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
    question: '헌혈 증명서는 어디에서 발급하나요?',
    answer: '로그인 후 증명서 발급 화면에서 발급 가능한 헌혈 기록을 선택해 신청할 수 있습니다.',
  },
  {
    question: '헌혈 포인트는 언제 지급되나요?',
    answer: '헌혈 완료 처리가 끝나면 헌혈 종류에 따른 포인트가 지급되며, 마이페이지의 내 포인트에서 내역을 확인할 수 있습니다.',
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
