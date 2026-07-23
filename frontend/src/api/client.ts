export const API_BASE_URL = 'http://localhost:8080'
export const NAVER_OAUTH_URL = `${API_BASE_URL}/oauth2/authorization/naver`

// 백엔드 공통 API 응답 타입 정의
export type ApiResult<T> = {
  code?: string
  message?: string
  data?: T
  errors?: unknown
}

// fetch 요청 옵션 타입 정의
type RequestOptions = Omit<RequestInit, 'body'> & {
  body?: unknown
}

// API 실패 응답 에러 타입 정의
export class ApiError extends Error {
  status: number
  code?: string
  errors?: unknown

  constructor(status: number, message: string, code?: string, errors?: unknown) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = code
    this.errors = errors
  }
}

// API 에러 타입 확인
export function isApiError(error: unknown): error is ApiError {
  return error instanceof ApiError
}

// JSON 응답 여부 확인
function isJsonResponse(response: Response) {
  return response.headers.get('Content-Type')?.includes('application/json')
}

// 서버 응답 본문 변환 처리
async function parseApiResult<T>(response: Response): Promise<ApiResult<T> | undefined> {
  const responseText = await response.text()

  if (!responseText) {
    return undefined
  }

  if (isJsonResponse(response)) {
    return JSON.parse(responseText) as ApiResult<T>
  }

  return {
    message: responseText,
  }
}

// fetch 공통 요청 처리
export async function request<T>(path: string, options: RequestOptions = {}): Promise<T | undefined> {
  const { body, headers, ...restOptions } = options
  const normalizedHeaders = new Headers(headers)

  if (body !== undefined) {
    normalizedHeaders.set('Content-Type', 'application/json')
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...restOptions,
    headers: normalizedHeaders,
    body: body === undefined ? undefined : JSON.stringify(body),
  })

  const result = await parseApiResult<T>(response)

  if (!response.ok) {
    throw new ApiError(
      response.status,
      result?.message ?? 'API 요청에 실패했습니다.',
      result?.code,
      result?.errors,
    )
  }

  return result?.data
}
