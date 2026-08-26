import {
  Navigate,
  Route,
  Routes,
} from 'react-router-dom'

import MainLayout
  from './layouts/MainLayout.jsx'
import MyPageLayout
  from './layouts/MyPageLayout.jsx'

import MainPage
  from './pages/main/MainPage.jsx'

import LoginPage
  from './pages/auth/LoginPage.jsx'
import SignupPage
  from './pages/auth/SignupPage.jsx'
import AdditionalInfoPage
  from './pages/auth/AdditionalInfoPage.jsx'
import AccountRecoveryPage
  from './pages/auth/AccountRecoveryPage.jsx'

import CenterSearchPage
  from './pages/center/CenterSearchPage.jsx'

import ReservationCreatePage
  from './pages/reservation/ReservationCreatePage.jsx'
import ReservationListPage
  from './pages/reservation/ReservationListPage.jsx'

import MyPageHomePage
  from './pages/mypage/MyPageHomePage.jsx'
import DonationRecordsPage
  from './pages/mypage/DonationRecordsPage.jsx'
import TestResultsPage
  from './pages/mypage/TestResultsPage.jsx'

import QuestionnairePage
  from './pages/questionnaire/QuestionnairePage.jsx'

import AdminPage
  from './pages/admin/AdminPage.jsx'
import AdminCentersPage
  from './pages/admin/AdminCentersPage.jsx'
import AdminReservationsPage
  from './pages/admin/AdminReservationsPage.jsx'
import AdminMembersPage
  from './pages/admin/AdminMembersPage.jsx'

import BloodUnitsManagementPage
  from './pages/medical/BloodUnitsManagementPage.jsx'
import DonorIdentityPage
  from './pages/medical/DonorIdentityPage.jsx'
import DonorCheckPage
  from './pages/medical/DonorCheckPage.jsx'
import MedicalMyPage
  from './pages/medical/MedicalMyPage.jsx'

import AdminRoute
  from './components/AdminRoute.jsx'
import MedicalRoute
  from './components/MedicalRoute.jsx'
import MemberRoute
  from './components/MemberRoute.jsx'

import NotFoundPage
  from './pages/NotFoundPage.jsx'


function App() {
  return (
    <Routes>
      <Route element={<MainLayout />}>

        <Route
          path="/"
          element={<MainPage />}
        />

        <Route
          path="/login"
          element={<LoginPage />}
        />

        <Route
          path="/signup"
          element={<SignupPage />}
        />

        <Route
          path="/find-id"
          element={<AccountRecoveryPage />}
        />

        <Route
          path="/change-password"
          element={<AccountRecoveryPage />}
        />

        <Route
          path="/additional-info"
          element={<AdditionalInfoPage />}
        />

        <Route
          path="/centers"
          element={<CenterSearchPage />}
        />

        <Route
          path="/reservations/new"
          element={<ReservationCreatePage />}
        />

        <Route
          path="/questionnaire"
          element={<QuestionnairePage />}
        />


        <Route
          element={
            <MemberRoute>
              <MyPageLayout />
            </MemberRoute>
          }
        >
          <Route
            path="/mypage"
            element={
              <MyPageHomePage />
            }
          />

          <Route
            path="/mypage/records"
            element={
              <DonationRecordsPage />
            }
          />

          <Route
            path="/mypage/results"
            element={
              <TestResultsPage />
            }
          />

          <Route
            path="/reservations"
            element={
              <ReservationListPage />
            }
          />
        </Route>


        <Route
          path="/records"
          element={
            <Navigate
              to="/mypage/records"
              replace
            />
          }
        />

        <Route
          path="/records/results"
          element={
            <Navigate
              to="/mypage/results"
              replace
            />
          }
        />


        <Route
          path="/mypage/medical"
          element={
            <MedicalRoute>
              <MedicalMyPage />
            </MedicalRoute>
          }
        />

        <Route
          path="/medical/blood_units_management"
          element={
            <MedicalRoute>
              <BloodUnitsManagementPage />
            </MedicalRoute>
          }
        />

        <Route
          path="/medical/donor_identity"
          element={
            <MedicalRoute>
              <DonorIdentityPage />
            </MedicalRoute>
          }
        />

        <Route
          path="/medical/donor_check/:confirmationId"
          element={
            <MedicalRoute>
              <DonorCheckPage />
            </MedicalRoute>
          }
        />


        <Route
          path="/admin"
          element={
            <AdminRoute>
              <AdminPage />
            </AdminRoute>
          }
        />

        <Route
          path="/admin/centers"
          element={
            <AdminRoute>
              <AdminCentersPage />
            </AdminRoute>
          }
        />

        <Route
          path="/admin/reservations"
          element={
            <AdminRoute>
              <AdminReservationsPage />
            </AdminRoute>
          }
        />

        <Route
          path="/admin/members"
          element={
            <AdminRoute>
              <AdminMembersPage />
            </AdminRoute>
          }
        />


        <Route
          path="*"
          element={<NotFoundPage />}
        />

      </Route>
    </Routes>
  )
}


export default App