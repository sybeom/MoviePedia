import { useEffect, useRef } from 'react'
import { Route, Routes } from 'react-router-dom'
import HomePage from './pages/HomePage'
import CookiePage from './pages/CookiePage'
import LoginPage from './pages/LoginPage'
import MovieDetailPage from './pages/MovieDetailPage'
import SignupPage from './pages/SignupPage'
import { getAuthSession } from './utils/authStorage'
import { authRequest, isAuthSessionError } from './utils/fetchUtil'

// 로그인 확인 응답 타입 정의
type AuthMeResponse = {
  loginId?: string
  nickname?: string
}

// 앱 라우트 연결
function App() {
  // 앱 시작 로그인 확인 중복 방지 참조 준비
  const hasCheckedAuthRef = useRef(false)

  useEffect(() => {
    if (hasCheckedAuthRef.current) {
      return
    }

    hasCheckedAuthRef.current = true

    const session = getAuthSession()

    if (!session?.accessToken) {
      return
    }

    // 앱 시작 로그인 상태 확인 처리
    async function checkAuthSession() {
      try {
        await authRequest<AuthMeResponse>('/auth/me', {
          method: 'GET',
        })
      } catch (error) {
        if (!isAuthSessionError(error)) {
          console.error(error)
        }
      }
    }

    void checkAuthSession()
  }, [])

  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/movies/:movieId" element={<MovieDetailPage />} />
      <Route path="/cookie" element={<CookiePage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />
    </Routes>
  )
}

export default App
