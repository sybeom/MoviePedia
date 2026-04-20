const API_BASE_URL = 'http://localhost:8080'

// fetch 요청 옵션 타입 정의
type RequestOptions = Omit<RequestInit, 'body'> & {
  body?: unknown
}

// API 실패 응답 에러 타입 정의
export class ApiError extends Error {
  status: number

  constructor(status: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

// API 에러 타입 확인
export function isApiError(error: unknown): error is ApiError {
  return error instanceof ApiError
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
    throw new ApiError(response.status, 'API 요청에 실패했습니다.')
  }

  return parseResponse<T>(response)
}
