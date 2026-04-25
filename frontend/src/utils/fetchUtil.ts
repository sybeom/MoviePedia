import { API_BASE_URL, ApiError, type ApiResult } from '../api/client'
import { clearAuthSession, getAuthSession, saveAuthSession } from './authStorage'

// 토큰 재발급 경로 상수 정의
const REFRESH_PATH = '/jwt/refresh'

// 인증 요청 옵션 타입 정의
type AuthRequestOptions = Omit<RequestInit, 'body' | 'headers'> & {
  body?: unknown
  headers?: Record<string, string>
}

// 토큰 재발급 응답 데이터 타입 정의
type RefreshResponse = {
  accessToken: string
  refreshToken?: string
  nickname?: string
}

// JSON 응답 여부 확인
function isJsonResponse(response: Response) {
  return response.headers.get('Content-Type')?.includes('application/json')
}

// 공통 API 응답 본문 변환 처리
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

// 인증 실패 후 로그인 화면 이동 처리
function redirectToLogin() {
  clearAuthSession()
  window.location.replace('/login')
}

// 토큰 재발급 요청 처리
async function refreshAccessToken() {
  const session = getAuthSession()

  // 리프레시 토큰 존재 여부 확인
  if (!session?.refreshToken) {
    redirectToLogin()
    throw new Error('리프레시 토큰이 없습니다.')
  }

  const response = await fetch(`${API_BASE_URL}${REFRESH_PATH}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      refreshToken: session.refreshToken,
    }),
  })

  const result = await parseApiResult<RefreshResponse>(response)

  // 재발급 실패 분기 처리
  if (!response.ok || !result?.data?.accessToken) {
    redirectToLogin()
    throw new ApiError(
      response.status,
      result?.message ?? '토큰 재발급에 실패했습니다.',
      result?.code,
      result?.errors,
    )
  }

  // 재발급 세션 저장 처리
  saveAuthSession({
    accessToken: result.data.accessToken,
    refreshToken: result.data.refreshToken ?? session.refreshToken,
    nickname: result.data.nickname ?? session.nickname,
  })

  return result.data.accessToken
}

// 인증 헤더 포함 fetch 실행 처리
async function executeAuthorizedFetch(path: string, options: AuthRequestOptions = {}) {
  const session = getAuthSession()
  const { body, headers, ...restOptions } = options

  // 액세스 토큰 존재 여부 확인
  if (!session?.accessToken) {
    redirectToLogin()
    throw new Error('액세스 토큰이 없습니다.')
  }

  return fetch(`${API_BASE_URL}${path}`, {
    ...restOptions,
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${session.accessToken}`,
      ...headers,
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  })
}

// 인증 필요 API 요청 처리
export async function authRequest<T>(
  path: string,
  options: AuthRequestOptions = {},
  hasRetried = false,
): Promise<T | undefined> {
  const response = await executeAuthorizedFetch(path, options)

  // 액세스 토큰 만료 분기 처리
  if (response.status === 401 && !hasRetried) {
    const refreshedAccessToken = await refreshAccessToken()
    const { body, headers, ...restOptions } = options

    const retryResponse = await fetch(`${API_BASE_URL}${path}`, {
      ...restOptions,
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${refreshedAccessToken}`,
        ...headers,
      },
      body: body === undefined ? undefined : JSON.stringify(body),
    })

    const retryResult = await parseApiResult<T>(retryResponse)

    // 재시도 실패 분기 처리
    if (!retryResponse.ok) {
      throw new ApiError(
        retryResponse.status,
        retryResult?.message ?? 'API 요청에 실패했습니다.',
        retryResult?.code,
        retryResult?.errors,
      )
    }

    return retryResult?.data
  }

  const result = await parseApiResult<T>(response)

  // 일반 요청 실패 분기 처리
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
