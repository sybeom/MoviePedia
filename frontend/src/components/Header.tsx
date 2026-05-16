import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { logout } from '../api/auth'
import { clearAuthSession, getAuthSession, subscribeAuthSessionChange, type AuthSession } from '../utils/authStorage'
import './Header.css'

// Header 컴포넌트 props 타입 정의
type HeaderProps = {
  showAuthActions?: boolean
  transparentOnTop?: boolean
  textOnlyAuthAction?: boolean
}

// 상단 공통 헤더 구성
function Header({
  showAuthActions = false,
  transparentOnTop = false,
  textOnlyAuthAction = false,
}: HeaderProps) {
  // 현재 로그인 세션 조회
  const [authSession, setAuthSession] = useState<AuthSession | null>(() => getAuthSession())

  // 로그아웃 진행 상태 관리
  const [isLoggingOut, setIsLoggingOut] = useState(false)

  // 스크롤 최상단 상태 관리
  const [isAtTop, setIsAtTop] = useState(true)

  // 로그아웃 후 화면 이동 준비
  const navigate = useNavigate()

  useEffect(() => {
    // 로그인 세션 변경 반영 처리
    function handleAuthSessionChange() {
      setAuthSession(getAuthSession())
    }

    return subscribeAuthSessionChange(handleAuthSessionChange)
  }, [])

  useEffect(() => {
    if (!transparentOnTop) {
      return
    }

    // 스크롤 위치 반영 처리
    function handleScrollPosition() {
      setIsAtTop(window.scrollY <= 4)
    }

    handleScrollPosition()
    window.addEventListener('scroll', handleScrollPosition, { passive: true })

    return () => {
      window.removeEventListener('scroll', handleScrollPosition)
    }
  }, [transparentOnTop])

  // 헤더 클래스 문자열 구성
  const headerClassName = [
    'site-header',
    transparentOnTop ? 'site-header-overlay' : '',
    transparentOnTop && isAtTop ? 'site-header-transparent' : '',
    textOnlyAuthAction ? 'site-header-text-auth' : '',
  ]
    .filter(Boolean)
    .join(' ')

  // 로그아웃 버튼 클릭 처리
  async function handleLogout() {
    if (isLoggingOut) {
      return
    }

    setIsLoggingOut(true)

    try {
      await logout()
    } finally {
      clearAuthSession()
      navigate('/')
      setIsLoggingOut(false)
    }
  }

  return (
    <header className={headerClassName}>
      <Link className="brand" to="/" aria-label="Movie Pedia home">
        Movie Pedia
      </Link>

      {showAuthActions
        ? authSession
          ? (
            <button className="logout-button" type="button" onClick={handleLogout} disabled={isLoggingOut}>
              {isLoggingOut ? '로그아웃 중...' : '로그아웃'}
            </button>
            )
          : (
            <Link className="login-button" to="/login">
              로그인
            </Link>
            )
        : null}
    </header>
  )
}

export default Header
