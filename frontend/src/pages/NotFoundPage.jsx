import { Link } from 'react-router-dom'
import BackgroundOrbit from '../components/decorations/BackgroundOrbit.jsx'
import './NotFoundPage.css'

import error404Blob
  from '../assets/mascot/error-404-blob.png'

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
                  <div className="error-card__mascot-frame">
                    <img
                      src={error404Blob}
                      alt=""
                      className="error-card__mascot-image"
                    />
                  </div>
                </div>
            </section>
        </main>
    )
}

export default NotFoundPage