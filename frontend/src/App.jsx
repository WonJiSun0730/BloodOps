import { Route, Routes } from 'react-router-dom'
import MainLayout from './layouts/MainLayout.jsx'
import MainPage from './pages/main/MainPage.jsx'
import LoginPage from './pages/auth/LoginPage.jsx'
import SignupPage from './pages/auth/SignupPage.jsx'
import NotFoundPage from './pages/NotFoundPage.jsx'

function App() {
  return (
    <Routes>
      <Route element={<MainLayout />}>
        <Route path="/" element={<MainPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}

export default App