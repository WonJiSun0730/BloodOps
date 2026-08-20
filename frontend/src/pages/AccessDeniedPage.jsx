import { Link } from 'react-router-dom'


function AccessDeniedPage() {
  return (
    <main className="access-denied-page">
      <div className="access-denied-card">

        <span className="access-denied-code">
          403
        </span>

        <h1>
          접근 권한이 없습니다
        </h1>

        <p>
          해당 페이지를 이용할 수 있는
          권한이 없습니다.
        </p>

        <Link
          to="/"
          className="admin-row-action"
        >
          메인으로 돌아가기
        </Link>

      </div>
    </main>
  )
}


export default AccessDeniedPage