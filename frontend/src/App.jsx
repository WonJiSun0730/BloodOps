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
import BloodStatusPage
  from './pages/blood-status/BloodStatusPage.jsx'

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
import MyPointsPage
  from './pages/mypage/MyPointsPage.jsx'
import MyRewardOrdersPage
  from './pages/mypage/MyRewardOrdersPage.jsx'

import QuestionnairePage
  from './pages/questionnaire/QuestionnairePage.jsx'
import CertificatePage
  from './pages/certificate/CertificatePage.jsx'

import AdminPage
  from './pages/admin/AdminPage.jsx'
import AdminCentersPage
  from './pages/admin/AdminCentersPage.jsx'
import AdminReservationsPage
  from './pages/admin/AdminReservationsPage.jsx'
import AdminMembersPage
  from './pages/admin/AdminMembersPage.jsx'
import AdminKakaoMessageTemplatePage
  from './pages/admin/AdminKakaoMessageTemplatePage.jsx'
import AdminRewardProductsPage
  from './pages/admin/AdminRewardProductsPage.jsx'
import AdminRewardFulfillmentPage
  from './pages/admin/AdminRewardFulfillmentPage.jsx'

import PromotionsPage from './pages/promotion/PromotionsPage.jsx'
import QnaPage from './pages/qna/QnaPage.jsx'
import PromotionDetailPage from './pages/promotion/PromotionDetailPage.jsx'

import BloodUnitsManagementPage
  from './pages/medical/BloodUnitsManagementPage.jsx'
import DonorIdentityPage
  from './pages/medical/DonorIdentityPage.jsx'
import DonorCheckPage
  from './pages/medical/DonorCheckPage.jsx'
import MedicalMyPage
  from './pages/medical/MedicalMyPage.jsx'
import MedicalDonationHistoryPage
  from './pages/medical/MedicalDonationHistoryPage.jsx'
import MedicalQuestionnaireListPage
  from './pages/medical/MedicalQuestionnaireListPage.jsx'
import MedicalTodayReservationsPage
  from './pages/medical/MedicalTodayReservationsPage.jsx'

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
          path="/blood-status"
          element={<BloodStatusPage />}
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

        <Route path="/promotion" element={<PromotionsPage />} />
        <Route path="/promotion/:promotionId" element={<PromotionDetailPage />} />
        <Route path="/promotions" element={<Navigate to="/promotion" replace />} />
        <Route path="/qna" element={<QnaPage />} />

        <Route
          path="/reservations/new"
          element={
            <MemberRoute>
              <ReservationCreatePage />
            </MemberRoute>
          }
        />

        <Route
          path="/questionnaire"
          element={<QuestionnairePage />}
        />

        <Route path="/certificate" element={<CertificatePage />} />


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
            path="/mypage/points"
            element={
              <MyPointsPage />
            }
          />

          <Route path="/mypage/rewards" element={<MyRewardOrdersPage />} />

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


        <Route element={<MedicalRoute><MyPageLayout /></MedicalRoute>}>
          <Route path="/mypage/medical" element={<MedicalMyPage />} />
          <Route path="/medical/blood_units_management" element={<BloodUnitsManagementPage />} />
          <Route path="/medical/donation_history" element={<MedicalDonationHistoryPage />} />
          <Route path="/medical/donor_identity" element={<DonorIdentityPage />} />
          <Route path="/medical/donor_check/:confirmationId" element={<DonorCheckPage />} />
          <Route path="/medical/questionnaires" element={<MedicalQuestionnaireListPage />} />
          <Route path="/medical/today-reservations" element={<MedicalTodayReservationsPage />} />
        </Route>


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
          path="/admin/reward-products"
          element={
            <AdminRoute>
              <AdminRewardProductsPage />
            </AdminRoute>
          }
        />

        <Route path="/admin/reward-fulfillment" element={<AdminRoute><AdminRewardFulfillmentPage /></AdminRoute>} />

        <Route
          path="/admin/kakao-message-template"
          element={
            <AdminRoute>
              <AdminKakaoMessageTemplatePage />
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
