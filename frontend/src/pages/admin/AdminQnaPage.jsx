import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import './AdminQnaPage.css'

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

async function saveAnswer(id, answer) {
  const csrf = await request('/api/auth/csrf')
  return request(`/api/qna/${id}/answer`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', [csrf.headerName]: csrf.token },
    body: JSON.stringify({ answer }),
  })
}

function AdminQnaPage() {
  const [result, setResult] = useState({ content: [], totalPages: 0, number: 0 })
  const [selected, setSelected] = useState(null)
  const [answer, setAnswer] = useState('')
  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(false)

  const load = async (page = 0) => {
    setLoading(true)
    try {
      const data = await request(`/api/admin/qna?page=${page}`)
      setResult(data)
      setSelected(null)
      setMessage('')
    } catch (error) {
      setMessage(error.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load(0) }, [])

  const selectQuestion = item => {
    setSelected(item)
    setAnswer(item.answer || '')
    setMessage('')
  }

  const submit = async event => {
    event.preventDefault()
    setLoading(true)
    try {
      const saved = await saveAnswer(selected.id, answer)
      setSelected(saved)
      setResult(current => ({
        ...current,
        content: current.content.map(item => item.id === saved.id ? saved : item),
      }))
      setMessage('답변이 저장되었습니다.')
    } catch (error) {
      setMessage(error.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="admin-qna-page">
      <div className="admin-qna-top">
        <div>
          <span>ADMIN · Q&amp;A</span>
          <h1>회원 질문 관리</h1>
          <p>회원이 남긴 질문을 확인하고 답변을 등록합니다.</p>
        </div>
        <Link to="/admin">관리자 홈</Link>
      </div>

      {message && <div className="admin-qna-message" role="alert">{message}</div>}

      <div className="admin-qna-layout">
        <section>
          <div className="admin-qna-table-wrap">
            <table>
              <thead><tr><th>번호</th><th>상태</th><th>제목</th><th>작성자</th><th>등록일</th></tr></thead>
              <tbody>
                {!loading && result.content.length === 0 && (
                  <tr><td colSpan="5" className="admin-qna-empty">등록된 질문이 없습니다.</td></tr>
                )}
                {result.content.map(item => (
                  <tr key={item.id} className={selected?.id === item.id ? 'selected' : ''}
                    onClick={() => selectQuestion(item)}>
                    <td>{item.id}</td>
                    <td><span className={item.answered ? 'answered' : 'waiting'}>
                      {item.answered ? '답변완료' : '답변대기'}
                    </span></td>
                    <td className="title">{item.title}</td>
                    <td>{item.authorName}</td>
                    <td>{new Date(item.createdAt).toLocaleDateString('ko-KR')}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <nav className="admin-qna-pagination" aria-label="회원 질문 페이지">
            {Array.from({ length: result.totalPages }, (_, index) => (
              <button key={index} className={result.number === index ? 'active' : ''}
                onClick={() => load(index)}>{index + 1}</button>
            ))}
          </nav>
        </section>

        <aside className="admin-qna-detail">
          {selected ? (
            <>
              <h2>{selected.title}</h2>
              <p className="meta">{selected.authorName} · {new Date(selected.createdAt).toLocaleString('ko-KR')}</p>
              <section><h3>문의 내용</h3><p>{selected.content}</p></section>
              <form onSubmit={submit}>
                <label htmlFor="admin-qna-answer">관리자 답변</label>
                <textarea id="admin-qna-answer" value={answer} maxLength="5000" required
                  onChange={event => setAnswer(event.target.value)} />
                <button disabled={loading}>답변 저장</button>
              </form>
            </>
          ) : <p className="placeholder">답변할 질문을 선택해주세요.</p>}
        </aside>
      </div>
    </main>
  )
}

export default AdminQnaPage
