// 로그인 세션 데이터 타입 정의
export type AuthSession = {
  accessToken: string
  refreshToken: string
  nickname: string
}

const ACCESS_TOKEN_KEY = 'moviepedia.accessToken'
const REFRESH_TOKEN_KEY = 'moviepedia.refreshToken'
const NICKNAME_KEY = 'moviepedia.nickname'

// 로그인 세션 저장 처리
export function saveAuthSession(session: AuthSession) {
  localStorage.setItem(ACCESS_TOKEN_KEY, session.accessToken)
  localStorage.setItem(REFRESH_TOKEN_KEY, session.refreshToken)
  localStorage.setItem(NICKNAME_KEY, session.nickname)
}

// 로그인 세션 조회 처리
export function getAuthSession(): AuthSession | null {
  const accessToken = localStorage.getItem(ACCESS_TOKEN_KEY)
  const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY)
  const nickname = localStorage.getItem(NICKNAME_KEY)

  if (!accessToken || !refreshToken || !nickname) {
    return null
  }

  return {
    accessToken,
    refreshToken,
    nickname,
  }
}

// 로그인 상태 확인
export function isLoggedIn() {
  return getAuthSession() !== null
}

// 로그인 세션 삭제 처리
export function clearAuthSession() {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
  localStorage.removeItem(NICKNAME_KEY)
}
