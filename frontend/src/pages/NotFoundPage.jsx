import { Link } from 'react-router-dom'
import BackgroundOrbit from '../components/decorations/BackgroundOrbit.jsx'
import './NotFoundPage.css'

function NotFoundPage() {
    return (
        <main className="error-page">
            <BackgroundOrbit />

            <section className="error-card">
                <div className="error-card__content">
                    <p className="error-card__code">404</p>

                    <h1 className="error-card__title">
                        페이지를 찾을 수 없습니다.
                    </h1>

                    <p className="error-card__description">
                        요청하신 페이지가 존재하지 않거나
                        <br className="error-card__desktop-break" />
                        이동되었을 수 있습니다.
                    </p>

                    <Link to="/" className="error-card__button">
                        메인으로 돌아가기
                    </Link>
                </div>

                <div
                    className="error-card__mascot"
                    aria-hidden="true"
                >
          <span className="error-card__mascot-placeholder">
            BLOOD
            <br />
            OPS
          </span>
                </div>
            </section>
        </main>
    )
}

export default NotFoundPage