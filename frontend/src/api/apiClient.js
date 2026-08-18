
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