
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
    let fieldErrors = {}

    try {
      const errorData = await response.json()

      if (errorData.message) {
        message = errorData.message
      }
      // 서버가 반환한 입력칸별 오류를 보관
      if (errorData.errors) {
        fieldErrors = errorData.errors
      }
    } catch {
      // JSON 형태의 오류 응답이 아닌 경우 기본 메시지 사용
    }
    // 회원가입 화면에서 입력칸 아래 오류를 표시할 때 사용
    const requestError = new Error(message)
    requestError.fieldErrors = fieldErrors

    throw requestError
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


// 아이디와 비밀번호를 Spring Security로 전송합니다.
export async function login(loginId, password) {
  // 먼저 현재 세션에 해당하는 CSRF 토큰을 받아옵니다.
  const csrf = await getCsrfToken()

  // Spring Security 기본 로그인 방식은 JSON이 아니라
  // HTML form과 같은 형태의 데이터를 받습니다.
  const loginData = new URLSearchParams()

  // SecurityConfig의 usernameParameter("loginId")와 이름이 같아야 합니다.
  loginData.append('loginId', loginId)
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

    // 아이디와 비밀번호가 담긴 데이터입니다.
    body: loginData,
  })
}

/*
 * React 회원가입 정보를 Spring에 JSON으로 전송합니다.
 */
export async function signup(memberData) {
  const csrf = await getCsrfToken()

  return apiRequest('/api/members/signup', {
    method: 'POST',

    headers: {
      'Content-Type': 'application/json',
      [csrf.headerName]: csrf.token,
    },

    body: JSON.stringify(memberData),
  })
}

/*
 * 아이디 중복 여부를 확인합니다.
 *
 * 반환 예:
 * { available: true }
 */
export function checkLoginId(loginId) {
  const encodedLoginId = encodeURIComponent(loginId)

  return apiRequest(
    `/api/members/check-login-id?loginId=${encodedLoginId}`
  )
}

/*
 * 이메일 중복 여부를 확인합니다.
 *
 * 반환 예:
 * { available: false }
 */
export function checkEmail(email) {
  const encodedEmail = encodeURIComponent(email)

  return apiRequest(
    `/api/members/check-email?email=${encodedEmail}`
  )
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
// 로그인 상태라면 사용자 식별값과 권한 목록을 반환합니다.
export function getCurrentUser() {
  return apiRequest('/api/auth/me')
}

/*
 * 헌혈센터 목록을 조회합니다.
 *
 * keyword가 없으면 전체 센터,
 * 있으면 센터명 또는 주소 기준으로 검색합니다.
 */
export function getCenters(keyword = '') {
  const trimmedKeyword = keyword.trim()

  if (!trimmedKeyword) {
    return apiRequest('/api/centers')
  }

  return apiRequest(
    `/api/centers?keyword=${encodeURIComponent(trimmedKeyword)}`
  )
}


/*
 * 헌혈센터 한 곳의 상세 정보를 조회합니다.
 */
export function getCenter(centerId) {
  return apiRequest(`/api/centers/${centerId}`)
}

/*
 * ADMIN이 신규 헌혈센터를 등록합니다.
 */
export async function createCenter(centerData) {
  const csrf = await getCsrfToken()

  return apiRequest(
    '/api/admin/centers',
    {
      method: 'POST',

      headers: {
        'Content-Type': 'application/json',
        [csrf.headerName]: csrf.token,
      },

      body: JSON.stringify(centerData),
    }
  )
}

export async function updateCenter(
  centerId,
  centerData
) {
  const csrf = await getCsrfToken()

  return apiRequest(
    `/api/admin/centers/${centerId}`,
    {
      method: 'PUT',

      headers: {
        'Content-Type': 'application/json',
        [csrf.headerName]: csrf.token,
      },

      body: JSON.stringify(centerData),
    }
  )
}

/*
 * ADMIN 회원 관리 화면에서 전체 회원 목록을 조회합니다.
 */
export function getAdminMembers() {
  return apiRequest('/api/admin/members')
}

/*
 * ADMIN이 회원의 권한을 변경합니다.
 */
export async function updateAdminMemberRole(
  memberId,
  role
) {
  const csrf = await getCsrfToken()

  return apiRequest(
    `/api/admin/members/${memberId}/role`,
    {
      method: 'PUT',

      headers: {
        'Content-Type': 'application/json',
        [csrf.headerName]: csrf.token,
      },

      body: JSON.stringify({
        role,
      }),
    }
  )
}