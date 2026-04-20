const API_BASE_URL = 'http://localhost:8080'

// fetch 요청 옵션 타입 정의
type RequestOptions = Omit<RequestInit, 'body'> & {
  body?: unknown
}

// 서버 응답 본문 변환 처리
async function parseResponse<T>(response: Response): Promise<T> {
  const contentType = response.headers.get('Content-Type')
  const responseText = await response.text()

  if (!responseText) {
    return undefined as T
  }

  if (contentType?.includes('application/json')) {
    return JSON.parse(responseText) as T
  }

  return responseText as T
}

// fetch 공통 요청 처리
export async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { body, headers, ...restOptions } = options

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...restOptions,
    headers: {
      'Content-Type': 'application/json',
      ...headers,
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  })

  if (!response.ok) {
    throw new Error('API 요청에 실패했습니다.')
  }

  return parseResponse<T>(response)
}
