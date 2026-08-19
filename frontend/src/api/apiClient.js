
//쉽게 말하면 React가 Spring에 데이터를 요청할 때 공통으로 사용하는 도우미 파일입니다.

export async function apiRequest(url, options = {}) {
  const response = await fetch(url, {
    credentials: 'include',
    ...options,

    headers: {
      Accept: 'application/json',
      ...options.headers,
    },
  })

  if (!response.ok) {
    let message = `API 요청 실패: ${response.status}`

    try {
      const errorData = await response.json()

      if (errorData.message) {
        message = errorData.message
      }
    } catch {
      // JSON 형태의 오류 응답이 아닌 경우 기본 메시지 사용
    }

    throw new Error(message)
  }

  if (response.status === 204) {
    return null
  }

  const contentType = response.headers.get('content-type')

  if (contentType?.includes('application/json')) {
    return response.json()
  }

  return response.text()
}

// Spring에서 CSRF 토큰을 받아옵니다.
// 로그인·로그아웃처럼 데이터를 변경하는 요청을 보내기 전에 사용합니다.
export function getCsrfToken() {
  return apiRequest('/api/auth/csrf')
}


// 이메일과 비밀번호를 Spring Security로 전송합니다.
export async function login(email, password) {
  // 먼저 현재 세션에 해당하는 CSRF 토큰을 받아옵니다.
  const csrf = await getCsrfToken()

  // Spring Security 기본 로그인 방식은 JSON이 아니라
  // HTML form과 같은 형태의 데이터를 받습니다.
  const loginData = new URLSearchParams()

  loginData.append('email', email)
  loginData.append('password', password)

  // 실제 로그인 요청을 전송합니다.
  return apiRequest('/api/auth/login', {
    method: 'POST',

    headers: {
      // 전달하는 데이터가 HTML form 형태라는 것을 Spring에 알려줍니다.
      'Content-Type': 'application/x-www-form-urlencoded',

      // Spring이 알려준 헤더 이름에 CSRF 토큰을 담습니다.
      [csrf.headerName]: csrf.token,
    },

    // 이메일과 비밀번호가 담긴 데이터입니다.
    body: loginData,
  })
}

// 현재 로그인 세션을 종료합니다.
export async function logout() {
  // 로그아웃도 서버의 상태를 변경하므로 CSRF 토큰이 필요합니다.
  const csrf = await getCsrfToken()

  return apiRequest('/api/auth/logout', {
    method: 'POST',

    headers: {
      // Spring이 요구하는 헤더 이름에 CSRF 토큰을 담습니다.
      [csrf.headerName]: csrf.token,
    },
  })
}

// 현재 브라우저가 로그인된 상태인지 확인합니다.
// 로그인 상태라면 이메일과 권한 목록을 반환합니다.
export function getCurrentUser() {
  return apiRequest('/api/auth/me')
}