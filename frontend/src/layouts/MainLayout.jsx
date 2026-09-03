import {
  Outlet,
  useLocation,
  useNavigate,
} from 'react-router-dom'

import {
  useEffect,
  useState,
} from 'react'

import Header
  from '../components/header/Header.jsx'

import Footer
  from '../components/footer/Footer.jsx'

import ChatbotWidget
  from '../components/chatbot/ChatbotWidget.jsx'

import {
  getCurrentUser,
} from '../api/apiClient.js'


function MainLayout() {
  const location = useLocation()
  const navigate = useNavigate()

  const [
    isProfileChecking,
    setIsProfileChecking,
  ] = useState(true)

const [
  isChatbotOpen,
  setIsChatbotOpen,
] = useState(false)

  useEffect(() => {
    let isMounted = true

    async function checkProfileCompletion() {

      /*
       * 추가정보 화면 자체는 예외입니다.
       * AdditionalInfoPage 내부에서
       * 로그인 여부 및 완료 상태를 따로 확인합니다.
       */
      if (
        location.pathname === '/additional-info'
      ) {
        if (isMounted) {
          setIsProfileChecking(false)
        }

        return
      }

      try {
        setIsProfileChecking(true)

        const user =
          await getCurrentUser()

        if (!isMounted) {
          return
        }

        /*
         * 로그인되어 있지만 필수 정보가 부족하다면
         * 다른 페이지 이용을 막습니다.
         */
        if (user.additionalInfoRequired) {
          navigate(
            '/additional-info',
            {
              replace: true,
            }
          )

          return
        }

      } catch {
        /*
         * 비로그인 사용자는 /api/auth/me 요청이
         * 실패할 수 있습니다.
         *
         * 기존 공개 페이지 접근 정책은
         * 여기에서 별도로 변경하지 않습니다.
         */
      } finally {
        if (isMounted) {
          setIsProfileChecking(false)
        }
      }
    }


    checkProfileCompletion()

    return () => {
      isMounted = false
    }
  }, [
    location.pathname,
    navigate,
  ])


  /*
   * 검사 중 기존 화면이 잠깐 노출되는 것을 방지합니다.
   */
  if (isProfileChecking) {
    return (
      <div className="app">
        <div className="page-content" />
      </div>
    )
  }


  return (
    <div className="app">
      <Header
        onChatbotOpen={() =>
          setIsChatbotOpen(true)
        }
      />

      <div className="page-content">
        <Outlet />

        {/*
          Outlet은 현재 주소에 해당하는
          페이지가 들어가는 자리입니다.
        */}
      </div>

      <Footer />
      <ChatbotWidget
        isOpen={isChatbotOpen}
        onOpenChange={setIsChatbotOpen}
      />
    </div>
  )
}


export default MainLayout