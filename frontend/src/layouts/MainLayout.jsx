import { Outlet } from 'react-router-dom'
import Header from '../components/header/Header.jsx'
import Footer from '../components/footer/Footer.jsx'

function MainLayout() {
  return (
    <div className="app">
      <Header />

      <div className="page-content">
        <Outlet />
{/*         Outlet은 현재 주소에 해당하는 페이지가 들어가는 자리입니다 */}
      </div>

      <Footer />
    </div>
  )
}

export default MainLayout