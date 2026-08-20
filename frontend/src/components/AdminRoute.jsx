import { useEffect, useState } from 'react'
import { Navigate } from 'react-router-dom'

import { getCurrentUser } from '../api/apiClient.js'


function AdminRoute({ children }) {
  const [currentUser, setCurrentUser] = useState(null)
  const [loading, setLoading] = useState(true)


  useEffect(() => {
    getCurrentUser()
      .then((user) => {
        setCurrentUser(user)
      })
      .catch(() => {
        setCurrentUser(null)
      })
      .finally(() => {
        setLoading(false)
      })
  }, [])


  if (loading) {
    return (
      <main className="route-loading">
        <p>
          권한을 확인하는 중입니다.
        </p>
      </main>
    )
  }


  if (!currentUser) {
    return (
      <Navigate
        to="/login"
        replace
      />
    )
  }


  const isAdmin =
    currentUser.roles?.includes('ROLE_ADMIN')

  if (!isAdmin) {
    return (
      <Navigate
        to="/"
        replace
      />
    )
  }


  return children
}


export default AdminRoute