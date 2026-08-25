import { useEffect, useState } from 'react'
import { Navigate, useLocation } from 'react-router-dom'

import { getCurrentUser } from '../api/apiClient.js'
import AccessDeniedPage from '../pages/AccessDeniedPage.jsx'

function MedicalRoute({ children }) {
  const location = useLocation()
  const [currentUser, setCurrentUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getCurrentUser()
      .then(setCurrentUser)
      .catch(() => setCurrentUser(null))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <main className="route-loading"><p>권한을 확인하는 중입니다.</p></main>
  if (!currentUser) return <Navigate to="/login" state={{ from: location }} replace />

  const canAccess = currentUser.roles?.some(
    (role) => role === 'ROLE_MEDICAL' || role === 'ROLE_ADMIN'
  )

  return canAccess ? children : <AccessDeniedPage />
}

export default MedicalRoute
