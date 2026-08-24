import { Link } from 'react-router-dom'

import BackgroundOrbit
    from '../components/decorations/BackgroundOrbit.jsx'

import './NotFoundPage.css'


function AccessDeniedPage() {
    return (
        <main className="error-page">

            <BackgroundOrbit />

            <section className="error-card">

                <div className="error-card__content">

                    <p className="error-card__code">
                        403
                    </p>

                    <h1 className="error-card__title">
                        접근 권한이 없습니다.
                    </h1>

                    <p className="error-card__description">
                        해당 페이지를 이용할 수 있는
                        <br className="error-card__desktop-break" />
                        권한이 없습니다.
                    </p>

                    <Link
                        to="/"
                        className="error-card__button"
                    >
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


export default AccessDeniedPage