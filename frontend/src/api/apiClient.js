
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
    let duplicateTimes = []

    try {
      const errorData = await response.json()

      if (errorData.message) {
        message = errorData.message
      }
      // 서버가 반환한 입력칸별 오류를 보관
      if (errorData.errors) {
        fieldErrors = errorData.errors
      }
      if (Array.isArray(errorData.duplicateTimes)) {
        duplicateTimes =
          errorData.duplicateTimes
      }
    } catch {
      // JSON 형태의 오류 응답이 아닌 경우 기본 메시지 사용
    }
    // 회원가입 화면에서 입력칸 아래 오류를 표시할 때 사용
    const requestError = new Error(message)
    requestError.fieldErrors = fieldErrors
    requestError.duplicateTimes = duplicateTimes

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
 * 로그인한 회원의 추가 정보를 저장합니다.
 */
export async function updateAdditionalInfo(memberData) {
  const csrf = await getCsrfToken()

  return apiRequest(
    '/api/members/additional-info',
    {
      method: 'PUT',

      headers: {
        'Content-Type': 'application/json',
        [csrf.headerName]: csrf.token,
      },

      body: JSON.stringify(memberData),
    }
  )
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
 * ADMIN 헌혈센터 목록을 페이지 단위로 조회합니다.
 */
export function getAdminCenters(
  keyword = '',
  page = 0,
  size = 10
) {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })

  const trimmedKeyword = keyword.trim()

  if (trimmedKeyword) {
    params.set('keyword', trimmedKeyword)
  }

  return apiRequest(
    `/api/admin/centers?${params.toString()}`
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
export function getAdminMembers(
  keyword = '',
  role = '',
  page = 0,
  size = 10
) {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })

  const trimmedKeyword = keyword.trim()

  if (trimmedKeyword) {
    params.set('keyword', trimmedKeyword)
  }

  if (role) {
    params.set('role', role)
  }

  return apiRequest(
    `/api/admin/members?${params.toString()}`
  )
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

/*
 * ADMIN 예약 슬롯을 조회합니다.
 */
export function getAdminReservationSlots(
  centerId,
  reservationDate,
  donationType
) {
  const params = new URLSearchParams({
    centerId: String(centerId),
    reservationDate,
    donationType,
  })

  return apiRequest(
    `/api/admin/reservation-slots?${params.toString()}`
  )
}


/*
 * ADMIN이 예약 슬롯을 일괄 생성합니다.
 */
export async function createAdminReservationSlots(
  slotData
) {
  const csrf = await getCsrfToken()

  return apiRequest(
    '/api/admin/reservation-slots',
    {
      method: 'POST',

      headers: {
        'Content-Type': 'application/json',
        [csrf.headerName]: csrf.token,
      },

      body: JSON.stringify(slotData),
    }
  )
}


/*
 * ADMIN이 예약 슬롯의 정원 또는 활성 상태를 변경합니다.
 */
export async function updateAdminReservationSlot(
  slotId,
  slotData
) {
  const csrf = await getCsrfToken()

  return apiRequest(
    `/api/admin/reservation-slots/${slotId}`,
    {
      method: 'PATCH',

      headers: {
        'Content-Type': 'application/json',
        [csrf.headerName]: csrf.token,
      },

      body: JSON.stringify(slotData),
    }
  )
}

export async function deleteAdminReservationSlot(
  slotId
) {
  const csrf = await getCsrfToken()

  return apiRequest(
    `/api/admin/reservation-slots/${slotId}`,
    {
      method: 'DELETE',

      headers: {
        [csrf.headerName]: csrf.token,
      },
    }
  )
}

/*
 * ADMIN이 선택한 센터의
 * 공유 장비그룹 정보를 조회합니다.
 */
export function getAdminEquipmentGroups(
  centerId
) {
  const params = new URLSearchParams({
    centerId: String(centerId),
  })

  return apiRequest(
    `/api/admin/reservation-slots/equipment-groups?${params.toString()}`
  )
}


/*
 * ADMIN이 공유 장비그룹의
 * 장비 수량 또는 운영 상태를 변경합니다.
 */
export async function updateAdminEquipmentGroup(
  equipmentGroupId,
  equipmentGroupData
) {
  const csrf = await getCsrfToken()

  return apiRequest(
    `/api/admin/reservation-slots/equipment-groups/${equipmentGroupId}`,
    {
      method: 'PATCH',

      headers: {
        'Content-Type': 'application/json',
        [csrf.headerName]: csrf.token,
      },

      body: JSON.stringify(
        equipmentGroupData
      ),
    }
  )
}

/*
 * ADMIN 전체 예약 목록을 조회합니다.
 */
export function getAdminReservations(
  keyword = '',
  status = '',
  page = 0,
  size = 10
) {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })

  const trimmedKeyword = keyword.trim()

  if (trimmedKeyword) {
    params.set('keyword', trimmedKeyword)
  }

  if (status) {
    params.set('status', status)
  }

  return apiRequest(
    `/api/admin/reservations?${params.toString()}`
  )
}

/*
 * ADMIN 예약 상세 조회
 */
export function getAdminReservation(
  reservationId
) {
  return apiRequest(
    `/api/admin/reservations/${reservationId}`
  )
}

/*
 * ADMIN 예약 슬롯 변경
 */
export async function changeAdminReservationSlot(
  reservationId,
  slotId
) {
  const csrf = await getCsrfToken()

  return apiRequest(
    `/api/admin/reservations/${reservationId}/slot`,
    {
      method: 'PATCH',

      headers: {
        'Content-Type': 'application/json',
        [csrf.headerName]: csrf.token,
      },

      body: JSON.stringify({
        slotId,
      }),
    }
  )
}

/*
 * ADMIN 예약 취소
 */
export async function cancelAdminReservation(
  reservationId
) {
  const csrf = await getCsrfToken()

  return apiRequest(
    `/api/admin/reservations/${reservationId}/cancel`,
    {
      method: 'PATCH',

      headers: {
        [csrf.headerName]: csrf.token,
      },
    }
  )
}

/*
 * 로그인 사용자가 예약 가능한 슬롯을 조회합니다.
 */
export function getReservationSlots(
  centerId,
  reservationDate,
  donationType
) {
  const params = new URLSearchParams({
    centerId: String(centerId),
    reservationDate,
    donationType,
  })

  return apiRequest(
    `/api/reservations/slots?${params.toString()}`
  )
}


/*
 * 로그인 사용자가 예약을 생성합니다.
 */
export async function createReservation(slotId) {
  const csrf = await getCsrfToken()

  return apiRequest(
    '/api/reservations',
    {
      method: 'POST',

      headers: {
        'Content-Type': 'application/json',
        [csrf.headerName]: csrf.token,
      },

      body: JSON.stringify({
        slotId,
      }),
    }
  )
}

/*
 * 의료진 혈액 재고 관리 화면에서
 * blood_units DB 데이터를 페이지 단위로 조회합니다.
 */
export function getMedicalBloodUnits(
  filters = {},
  page = 0,
  size = 20
) {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })

  Object.entries(filters).forEach(([key, value]) => {
    if (
      value !== undefined
      && value !== null
      && String(value).trim()
    ) {
      params.set(key, String(value).trim())
    }
  })

  return apiRequest(
    `/api/medical/blood-units?${params.toString()}`
  )
}


/*
 * 헌혈자 확인 정보를 DB에서 조회합니다.
 */
export function getMedicalDonorConfirmation(
  confirmationId
) {
  return apiRequest(
    `/api/medical/donor-confirmations/${
      encodeURIComponent(confirmationId)
    }`
  )
}

export function getMedicalDonorQuestionnaire(confirmationId) {
  return apiRequest(
    `/api/medical/donor-confirmations/${
      encodeURIComponent(confirmationId)
    }/questionnaire`
  )
}

/*
 * 로그인한 회원 본인의
 * 완료된 헌혈기록을 조회합니다.
 */
export function getMyDonationHistory() {
  return apiRequest(
    '/api/donations/me'
  )
}

/*
 * 의료진이 전체 회원의 완료된 헌혈기록을 조회합니다.
 */
export function getMedicalDonationHistory() {
  return apiRequest(
    '/api/medical/donations'
  )
}

/*
 * 로그인한 회원의 예약 내역을 조회합니다.
 */
export function getMyReservations() {
  return apiRequest(
    '/api/reservations'
  )
}


/*
 * 로그인한 회원 본인의 예약을 취소합니다.
 */
export async function cancelMyReservation(
  reservationId
) {
  const csrf = await getCsrfToken()

  return apiRequest(
    `/api/reservations/${reservationId}/cancel`,
    {
      method: 'PATCH',

      headers: {
        [csrf.headerName]: csrf.token,
      },
    }
  )
}


/*
 * 로그인한 회원 본인의 예약 시간을 변경합니다.
 */
export async function changeMyReservationSlot(
  reservationId,
  slotId
) {
  const csrf = await getCsrfToken()

  return apiRequest(
    `/api/reservations/${reservationId}/slot`,
    {
      method: 'PATCH',

      headers: {
        'Content-Type': 'application/json',
        [csrf.headerName]: csrf.token,
      },

      body: JSON.stringify({
        slotId,
      }),
    }
  )
}

/*
 * ADMIN 카카오 예약 메시지 설정을 조회합니다.
 */
export function getAdminKakaoMessageTemplate() {
  return apiRequest(
    '/api/admin/kakao-message-template'
  )
}


/*
 * ADMIN 카카오 예약 메시지 설정을 저장합니다.
 */
export async function updateAdminKakaoMessageTemplate(
  templateData
) {
  const csrf = await getCsrfToken()

  return apiRequest(
    '/api/admin/kakao-message-template',
    {
      method: 'PUT',

      headers: {
        'Content-Type': 'application/json',
        [csrf.headerName]: csrf.token,
      },

      body: JSON.stringify(templateData),
    }
  )
}


/*
 * 의료진이 확인한 헌혈자 정보와 서명을 DB에 저장합니다.
 */
export async function updateMedicalDonorConfirmation(
  confirmationId,
  confirmationData
) {
  const csrf = await getCsrfToken()

  return apiRequest(
    `/api/medical/donor-confirmations/${
      encodeURIComponent(confirmationId)
    }`,
    {
      method: 'PUT',

      headers: {
        'Content-Type': 'application/json',
        [csrf.headerName]: csrf.token,
      },

      body: JSON.stringify(
        confirmationData
      ),
    }
  )
}


export async function createMedicalBloodUnit(unitData) {
  const csrf = await getCsrfToken()
  return apiRequest('/api/medical/blood-units', { method: 'POST', headers: { 'Content-Type': 'application/json', [csrf.headerName]: csrf.token }, body: JSON.stringify(unitData) })
}

export async function updateMedicalBloodUnit(id, unitData) {
  const csrf = await getCsrfToken()
  return apiRequest(`/api/medical/blood-units/${id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json', [csrf.headerName]: csrf.token }, body: JSON.stringify(unitData) })
}

export async function verifyMedicalDonorIdentity(identityData) {
  const csrf = await getCsrfToken()
  return apiRequest('/api/medical/donor-confirmations/identity-verifications', { method: 'POST', headers: { 'Content-Type': 'application/json', [csrf.headerName]: csrf.token }, body: JSON.stringify(identityData) })
}

export function getQuestionnaireQuestions() {
  return apiRequest('/api/questionnaires/questions')
}

export function getLatestQuestionnaire() {
  return apiRequest('/api/questionnaires/me/latest')
}

export async function submitQuestionnaire(questionnaireData) {
  const csrf = await getCsrfToken()

  return apiRequest('/api/questionnaires', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      [csrf.headerName]: csrf.token,
    },
    body: JSON.stringify(questionnaireData),
  })
}


/*
 * 로그인한 회원의 포인트 잔액과 거래 내역을 조회합니다.
 */
export function getMyPoints() {
  return apiRequest('/api/rewards/me')
}

/*
 * 회원에게 노출할 활성 포인트 상품을 조회합니다.
 */
export function getRewardProducts() {
  return apiRequest('/api/rewards/products')
}

/*
 * ADMIN 포인트 상품과 재고를 조회합니다.
 */
export function getAdminRewardProducts() {
  return apiRequest('/api/admin/reward-products')
}

export async function createAdminRewardProduct(productData) {
  const csrf = await getCsrfToken()
  return apiRequest('/api/admin/reward-products', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      [csrf.headerName]: csrf.token,
    },
    body: JSON.stringify(productData),
  })
}

export async function updateAdminRewardProduct(productId, productData) {
  const csrf = await getCsrfToken()
  return apiRequest('/api/admin/reward-products/' + encodeURIComponent(productId), {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      [csrf.headerName]: csrf.token,
    },
    body: JSON.stringify(productData),
  })
}


export async function uploadAdminRewardProductImage(productId, imageFile) {
  const csrf = await getCsrfToken()
  const formData = new FormData()
  formData.append('image', imageFile)

  return apiRequest(
    '/api/admin/reward-products/' + encodeURIComponent(productId) + '/image',
    {
      method: 'PUT',
      headers: {
        [csrf.headerName]: csrf.token,
      },
      body: formData,
    }
  )
}
