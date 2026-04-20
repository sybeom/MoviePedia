import { useState, type FormEvent } from 'react'
import { signup } from '../api/auth'
import Header from '../components/Header'
import './Auth.css'

function SignupPage() {
  const [loginId, setLoginId] = useState('')
  const [password, setPassword] = useState('')
  const [nickname, setNickname] = useState('')
  const [message, setMessage] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  async function handleSignup(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setMessage('')
    setIsSubmitting(true)

    try {
      await signup({
        loginId,
        password,
        nickname,
      })

      setMessage('회원가입 요청이 완료되었습니다.')
      setLoginId('')
      setPassword('')
      setNickname('')
    } catch {
      setMessage('회원가입 요청에 실패했습니다. 잠시 후 다시 시도해주세요.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="app">
      <Header />

      <main className="auth-container">
        <section className="login-panel" aria-labelledby="signup-title">
          <h1 id="signup-title">회원가입</h1>

          <form className="login-form" onSubmit={handleSignup}>
            <label htmlFor="signup-id">아이디</label>
            <input
              id="signup-id"
              type="text"
              placeholder="사용할 아이디를 입력하세요"
              autoComplete="username"
              value={loginId}
              onChange={(event) => setLoginId(event.target.value)}
              required
            />

            <label htmlFor="signup-password">비밀번호</label>
            <input
              id="signup-password"
              type="password"
              placeholder="비밀번호를 입력하세요"
              autoComplete="new-password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
            />

            <label htmlFor="nickname">닉네임</label>
            <input
              id="nickname"
              type="text"
              placeholder="닉네임을 입력하세요"
              autoComplete="nickname"
              value={nickname}
              onChange={(event) => setNickname(event.target.value)}
              required
            />

            <button className="submit-button" type="submit" disabled={isSubmitting}>
              {isSubmitting ? '가입 요청 중...' : '가입하기'}
            </button>

            {message && <p className="form-message">{message}</p>}
          </form>
        </section>
      </main>
    </div>
  )
}

export default SignupPage
