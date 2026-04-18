import Header from '../components/Header'
import { preventFormSubmit } from '../utils/form'
import './Auth.css'

// 회원가입 화면 구성
function SignupPage() {
  return (
    <div className="app">
      <Header />

      <main className="auth-container">
        <section className="login-panel" aria-labelledby="signup-title">
          <h1 id="signup-title">회원가입</h1>

          <form className="login-form" onSubmit={preventFormSubmit}>
            <label htmlFor="signup-id">아이디</label>
            <input id="signup-id" type="text" placeholder="사용할 아이디를 입력하세요" autoComplete="username" />

            <label htmlFor="signup-password">비밀번호</label>
            <input
              id="signup-password"
              type="password"
              placeholder="비밀번호를 입력하세요"
              autoComplete="new-password"
            />

            <label htmlFor="nickname">닉네임</label>
            <input id="nickname" type="text" placeholder="닉네임을 입력하세요" autoComplete="nickname" />

            <button className="submit-button" type="submit">
              가입하기
            </button>
          </form>
        </section>
      </main>
    </div>
  )
}

export default SignupPage
