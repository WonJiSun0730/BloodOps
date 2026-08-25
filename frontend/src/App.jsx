import { Route, Routes } from 'react-router-dom'

import MainLayout from './layouts/MainLayout.jsx'
import MainPage from './pages/main/MainPage.jsx'
import LoginPage from './pages/auth/LoginPage.jsx'
import SignupPage from './pages/auth/SignupPage.jsx'
import AdditionalInfoPage from './pages/auth/AdditionalInfoPage.jsx'
import CenterSearchPage from './pages/center/CenterSearchPage.jsx'
import AdminPage from './pages/admin/AdminPage.jsx'
import AdminRoute from './components/AdminRoute.jsx'
import NotFoundPage from './pages/NotFoundPage.jsx'
import AdminCentersPage from './pages/admin/AdminCentersPage.jsx'
import AdminReservationsPage from './pages/admin/AdminReservationsPage.jsx'
import AdminMembersPage from './pages/admin/AdminMembersPage.jsx'
import BloodUnitsManagementPage from './pages/medical/BloodUnitsManagementPage.jsx'

function App() {
  return (
    <Routes>
      <Route element={<MainLayout />}>
        <Route path="/" element={<MainPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/additional-info" element={<AdditionalInfoPage />} />
        <Route path="/centers" element={<CenterSearchPage />} />

        <Route
          path="/medical/blood_units_management"
          element={<BloodUnitsManagementPage />}
        />

        <Route
          path="/admin"
          element={<AdminRoute><AdminPage /></AdminRoute>}
        />
        <Route
          path="/admin/centers"
          element={<AdminRoute><AdminCentersPage /></AdminRoute>}
        />
        <Route
          path="/admin/reservations"
          element={<AdminRoute><AdminReservationsPage /></AdminRoute>}
        />
        <Route
          path="/admin/members"
          element={<AdminRoute><AdminMembersPage /></AdminRoute>}
        />

        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}

export default App
