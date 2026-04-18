import { Link } from 'react-router-dom'
import './Header.css'

// Header 컴포넌트 props 타입 정의
type HeaderProps = {
  showLogin?: boolean
}

// 상단 공통 헤더 렌더링
function Header({ showLogin = false }: HeaderProps) {
  return (
    <header className="site-header">
      <Link className="brand" to="/" aria-label="Movie Pedia home">
        Movie Pedia
      </Link>
      {showLogin && (
        <Link className="login-button" to="/login">
          로그인
        </Link>
      )}
    </header>
  )
}

export default Header
