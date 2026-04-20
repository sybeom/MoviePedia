import { Link } from 'react-router-dom'
import './Header.css'

type HeaderProps = {
  showLogin?: boolean
}

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
