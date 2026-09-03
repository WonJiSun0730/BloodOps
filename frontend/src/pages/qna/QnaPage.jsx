import { useEffect, useState } from 'react'
import { getCurrentUser } from '../../api/apiClient.js'
import './QnaPage.css'

const emptyForm = { title: '', content: '' }

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
    headers: { 'Content-Type': 'application/json', [csrf.headerName]: csrf.token },
    body: body ? JSON.stringify(body) : undefined,
  })
}

const FAQ_PAGE_SIZE = 10

function QnaPage() {
  const [result, setResult] = useState({ content: [], totalPages: 0, number: 0 })
  const [selected, setSelected] = useState(null)
  const [form, setForm] = useState(emptyForm)
  const [mode, setMode] = useState('list')
  const [user, setUser] = useState(null)
  const [message, setMessage] = useState('')
  const [loginGuide, setLoginGuide] = useState('')
  const [loading, setLoading] = useState(false)

  const loadMyQuestions = async (page = 0) => {
    setLoading(true)
    try {
      const data = await request(`/api/qna/me?page=${page}`)
      setResult(data)
      setMode('list')
      setSelected(null)
      setMessage('')
    } catch (error) {
      setMessage(error.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    getCurrentUser()
      .then(currentUser => {
        setUser(currentUser)
        return request('/api/qna/me?page=0')
      })
      .then(setResult)
      .catch(() => setUser(null))
  }, [])

  const openDetail = item => {
    setSelected(item)
    setMode('detail')
    setMessage('')
  }

  const startWrite = () => {
    if (!user) {
      setLoginGuide('로그인 후 질문을 작성할 수 있습니다.')
      return
    }
    setLoginGuide('')
    setForm(emptyForm)
    setSelected(null)
    setMode('write')
    setMessage('')
  }

  const startEdit = () => {
    setForm({ title: selected.title, content: selected.content })
    setMode('edit')
    setMessage('')
  }

  const submitQuestion = async event => {
    event.preventDefault()
    setLoading(true)
    try {
      const editing = mode === 'edit'
      const saved = await mutation(
        editing ? `/api/qna/${selected.id}` : '/api/qna',
        editing ? 'PUT' : 'POST',
        form
      )
      setSelected(saved)
      setMode('detail')
      setMessage(editing ? '질문이 수정되었습니다.' : '질문이 등록되었습니다.')
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
      await loadMyQuestions(0)
      setMessage('질문이 삭제되었습니다.')
    } catch (error) {
      setMessage(error.message)
      setLoading(false)
    }
  }

  return (
    <main className="qna-page">
      <header className="qna-heading">
        <p className="qna-eyebrow">고객지원</p>
        <h1>Q&amp;A</h1>
        <p>BloodOps 이용 중 궁금한 내용을 남기고 관리자 답변을 확인하세요.</p>
      </header>

      {message && <div className="qna-message" role="alert">{message}</div>}

      {mode === 'list' && (
        <>
          <section className="qna-my-section" aria-labelledby="qna-my-title">
            <div className="qna-my-header">
              <div className="qna-section-heading">
                <p>MY Q&amp;A</p>
                <h2 id="qna-my-title">내가 남긴 질문</h2>
              </div>
              <div className="qna-write-area">
                <button className="qna-primary" type="button" onClick={startWrite}>질문하기</button>
                {loginGuide && <p className="qna-login-guide" role="alert">{loginGuide}</p>}
              </div>
            </div>

            {user ? (
              <>
                <div className="qna-table-wrap">
                  <table className="qna-table">
                    <thead><tr><th>번호</th><th>상태</th><th>제목</th><th>등록일</th></tr></thead>
                    <tbody>
                      {!loading && result.content.length === 0 && (
                        <tr><td colSpan="4" className="qna-empty">작성한 질문이 없습니다.</td></tr>
                      )}
                      {result.content.map(item => (
                        <tr key={item.id} onClick={() => openDetail(item)}>
                          <td>{item.id}</td>
                          <td><span className={item.answered ? 'answered' : 'waiting'}>
                            {item.answered ? '답변완료' : '답변대기'}
                          </span></td>
                          <td className="qna-title">{item.title}</td>
                          <td>{new Date(item.createdAt).toLocaleDateString('ko-KR')}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
                <nav className="qna-pagination" aria-label="내 질문 페이지">
                  {Array.from({ length: result.totalPages }, (_, index) => (
                    <button key={index} className={result.number === index ? 'active' : ''}
                      onClick={() => loadMyQuestions(index)}
                      aria-current={result.number === index ? 'page' : undefined}>
                      {index + 1}
                    </button>
                  ))}
                </nav>
              </>
            ) : (
              <p className="qna-my-empty">로그인하면 본인이 남긴 질문과 관리자 답변을 확인할 수 있습니다.</p>
            )}
          </section>
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
            <button type="button" onClick={() => selected ? setMode('detail') : setMode('list')}>취소</button>
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
            <p>{new Date(selected.createdAt).toLocaleString('ko-KR')}</p>
          </div>
          <section><h3>문의 내용</h3><p>{selected.content}</p></section>
          <section className="qna-answer"><h3>관리자 답변</h3>
            <p>{selected.answer || '관리자가 문의 내용을 확인하고 있습니다.'}</p>
          </section>
          <div className="qna-actions">
            <button onClick={() => setMode('list')}>목록</button>
            {!selected.answered && <button onClick={startEdit}>수정</button>}
            <button className="qna-danger" onClick={deleteQuestion}>삭제</button>
          </div>
        </article>
      )}
    </main>
  )
}

export default QnaPage
