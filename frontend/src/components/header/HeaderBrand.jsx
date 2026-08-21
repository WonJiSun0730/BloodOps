import { Link } from 'react-router-dom'

import bloodOpsLogo
  from '../../assets/images/bloodops-logo.png'

function HeaderBrand() {
  return (
    <Link
      className="brand"
      to="/"
      aria-label="BloodOps 메인으로 이동"
    >
      <img
        className="brand-logo"
        src={bloodOpsLogo}
        alt=""
        aria-hidden="true"
      />

      <span className="brand-copy">
        <strong className="brand-name">
          BloodOps
        </strong>

        <span className="brand-subtitle brand-subtitle-full">
          BLOOD DONATION MANAGEMENT SYSTEM
        </span>

        <span className="brand-subtitle brand-subtitle-short">
          BLOOD DONATION
        </span>
      </span>
    </Link>
  )
}

export default HeaderBrand