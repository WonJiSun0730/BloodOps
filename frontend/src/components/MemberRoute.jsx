import {
  useEffect,
  useState,
} from 'react'

import {
  Navigate,
  useLocation,
} from 'react-router-dom'

import {
  getCurrentUser,
} from '../api/apiClient.js'


function MemberRoute({
  children,
}) {
  const location = useLocation()

  const [
    currentUser,
    setCurrentUser,
  ] = useState(null)

  const [
    loading,
    setLoading,
  ] = useState(true)


  useEffect(() => {
    let active = true

    getCurrentUser()
      .then((user) => {
        if (active) {
          setCurrentUser(user)
        }
      })
      .catch(() => {
        if (active) {
          setCurrentUser(null)
        }
      })
      .finally(() => {
        if (active) {
          setLoading(false)
        }
      })

    return () => {
      active = false
    }
  }, [])


  if (loading) {
    return (
      <main className="route-loading">
        <p>
          로그인 정보를 확인하는 중입니다.
        </p>
      </main>
    )
  }


  if (!currentUser) {
    return (
      <Navigate
        to="/login"
        state={{
          from: location,
        }}
        replace
      />
    )
  }

  if (
    currentUser.additionalInfoRequired
    && location.pathname !== '/additional-info'
  ) {
    return (
      <Navigate
        to="/additional-info"
        replace
      />
    )
  }

  return children
}


export default MemberRoute