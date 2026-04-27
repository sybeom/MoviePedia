import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { logout } from '../api/auth'
import { clearAuthSession, getAuthSession } from '../utils/authStorage'
import './Header.css'

// Header 컴포넌트 props 타입 정의
type HeaderProps = {
  showAuthActions?: boolean
}

// 상단 공통 헤더 렌더링
function Header({ showAuthActions = false }: HeaderProps) {
  // 현재 로그인 세션 상태 관리
  const [authSession, setAuthSession] = useState(() => getAuthSession())

  // 로그아웃 진행 상태 관리
  const [isLoggingOut, setIsLoggingOut] = useState(false)

  // 로그아웃 후 홈 화면 이동 준비
  const navigate = useNavigate()

  // 로그아웃 버튼 클릭 처리
  async function handleLogout() {
    // 중복 로그아웃 요청 차단
    if (isLoggingOut) {
      return
    }

    // 로그아웃 시작 상태 반영
    setIsLoggingOut(true)

    try {
      // 서버 로그아웃 요청 전송
      await logout()
    } finally {
      // 로컬 세션 정리 처리
      clearAuthSession()
      setAuthSession(null)

      // 홈 화면 이동 처리
      navigate('/')
      setIsLoggingOut(false)
    }
  }

  return (
    <header className="site-header">
      <Link className="brand" to="/" aria-label="Movie Pedia home">
        Movie Pedia
      </Link>

      {showAuthActions &&
        (authSession ? (
          <button className="logout-button" type="button" onClick={handleLogout} disabled={isLoggingOut}>
            {isLoggingOut ? '로그아웃 중...' : '로그아웃'}
          </button>
        ) : (
          <Link className="login-button" to="/login">
            로그인
          </Link>
        ))}
    </header>
  )
}

export default Header
