import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { clearAuthSession, getAuthSession } from '../utils/authStorage'
import './Header.css'

// Header 컴포넌트 props 타입 정의
type HeaderProps = {
  showAuthActions?: boolean
}

// 상단 공통 헤더 렌더링
function Header({ showAuthActions = false }: HeaderProps) {
  // 저장된 로그인 세션 상태 관리
  const [authSession, setAuthSession] = useState(() => getAuthSession())

  // 로그아웃 후 홈 화면 이동 함수 준비
  const navigate = useNavigate()

  // 로그아웃 버튼 클릭 처리
  function handleLogout() {
    clearAuthSession()
    setAuthSession(null)
    navigate('/')
  }

  return (
    <header className="site-header">
      <Link className="brand" to="/" aria-label="Movie Pedia home">
        Movie Pedia
      </Link>

      {showAuthActions &&
        (authSession ? (
          <button className="logout-button" type="button" onClick={handleLogout}>
            로그아웃
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
