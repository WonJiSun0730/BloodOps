function Footer() {
  return (
    <footer className="site-footer">
      <div className="footer-inner">

        <div className="footer-main">

          <div className="footer-brand">
            <strong>
              BloodOps
            </strong>

            <p>
              해당 사이트는 실제 헌혈 서비스가 아니며,
              교육과정 중 제작한 협업 프로젝트
              포트폴리오 결과물입니다.
            </p>

            <p className="footer-project-notice">
              실제 헌혈 예약 및 의료 상담 용도로
              사용할 수 없습니다.
            </p>
          </div>


          <div className="footer-info">

            <div className="footer-info-row">
              <span>
                PROJECT
              </span>

              <strong>
                Blood Donation Reservation
                & Management System
              </strong>
            </div>


            <div className="footer-info-row">
              <span>
                ACADEMY
              </span>

              <strong>
                인천 더조은컴퓨터아카데미
              </strong>
            </div>


            <div className="footer-info-row">
              <span>
                ADDRESS
              </span>

              <strong>
                인천광역시 부평구 부평1동 534-48
              </strong>
            </div>


            <div className="footer-project-links">
              <a
                href="https://docs.google.com/spreadsheets/d/1PYppCCXEs0IH7lp608D9EZ3kYg7zuWDx3CBUPFK5uFk/edit?usp=sharing"
                target="_blank"
                rel="noreferrer"
              >
                협업시트 구경하기

                <span aria-hidden="true">
                  ↗
                </span>
              </a>

{/*               <a */}
{/*                 href="https://github.com/WonJiSun0730/BloodOps" */}
{/*                 target="_blank" */}
{/*                 rel="noreferrer" */}
{/*               > */}
{/*                 GitHub Repository */}

{/*                 <span aria-hidden="true"> */}
{/*                   ↗ */}
{/*                 </span> */}
{/*               </a> */}
            </div>

          </div>

        </div>


        <div className="footer-divider" />


        <div className="footer-bottom">
          <p className="footer-copyright">
            © 2026 BloodOps.
            Educational Portfolio Project.
          </p>

          <span className="footer-disclaimer">
            Not an official blood donation service.
          </span>
        </div>

      </div>
    </footer>
  )
}

export default Footer