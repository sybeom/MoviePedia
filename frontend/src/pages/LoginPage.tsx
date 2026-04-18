import { Link } from 'react-router-dom'
import Header from '../components/Header'
import { preventFormSubmit } from '../utils/form'
import './Auth.css'

// 로그인 화면 구성
function LoginPage() {
  return (
    <div className="app">
      <Header />

      <main className="auth-container">
        <section className="login-panel" aria-labelledby="login-title">
          <p className="eyebrow">Welcome back</p>
          <h1 id="login-title">로그인</h1>
          <p className="login-copy">관심 있는 영화와 기록을 이어서 확인하세요.</p>

          <form className="login-form" onSubmit={preventFormSubmit}>
            <label htmlFor="login-id">아이디</label>
            <input id="login-id" type="text" placeholder="아이디를 입력하세요" autoComplete="username" />

            <label htmlFor="login-password">비밀번호</label>
            <input
              id="login-password"
              type="password"
              placeholder="비밀번호를 입력하세요"
              autoComplete="current-password"
            />

            <button className="submit-button" type="submit">
              로그인
            </button>
          </form>

          <Link className="secondary-action" to="/signup">
            회원가입
          </Link>
        </section>
      </main>
    </div>
  )
}

export default LoginPage
