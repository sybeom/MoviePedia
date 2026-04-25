import { useRef, useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { login } from '../api/auth'
import Header from '../components/Header'
import { saveAuthSession } from '../utils/authStorage'
import './Auth.css'

// 로그인 화면 구성
function LoginPage() {
  // 로그인 성공 후 홈 화면 이동 준비
  const navigate = useNavigate()

  // 로그인 중복 제출 방지 참조 준비
  const isSubmittingRef = useRef(false)

  // 로그인 입력값 상태 관리
  const [loginId, setLoginId] = useState('')
  const [password, setPassword] = useState('')

  // 요청 결과 메시지와 진행 상태 관리
  const [message, setMessage] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  // 로그인 폼 제출 처리
  async function handleLogin(event: FormEvent<HTMLFormElement>) {
    // 브라우저 기본 제출 동작 방지
    event.preventDefault()

    // 중복 요청 차단
    if (isSubmittingRef.current) {
      return
    }

    // 요청 시작 상태 반영
    isSubmittingRef.current = true
    setMessage('')
    setIsSubmitting(true)

    try {
      // 로그인 요청 전송
      const loginResponse = await login({
        loginId,
        password,
      })

      // 응답 데이터 존재 여부 확인
      if (!loginResponse) {
        throw new Error('로그인 응답 데이터가 없습니다.')
      }

      // 로그인 세션 저장
      saveAuthSession(loginResponse)

      // 홈 화면 이동 처리
      navigate('/')
    } catch {
      // 로그인 실패 메시지 반영
      setMessage('로그인 요청에 실패했습니다. 아이디와 비밀번호를 확인해주세요.')
    } finally {
      // 요청 종료 상태 반영
      isSubmittingRef.current = false
      setIsSubmitting(false)
    }
  }

  return (
    <div className="app">
      <Header />

      <main className="auth-container">
        <section className="login-panel" aria-labelledby="login-title">
          <p className="eyebrow">Welcome back</p>
          <h1 id="login-title">로그인</h1>

          <form className="login-form" onSubmit={handleLogin} noValidate>
            <label htmlFor="login-id">아이디</label>
            <input
              id="login-id"
              type="text"
              placeholder="아이디를 입력하세요"
              autoComplete="username"
              value={loginId}
              onChange={(event) => setLoginId(event.target.value)}
            />

            <label htmlFor="login-password">비밀번호</label>
            <input
              id="login-password"
              type="password"
              placeholder="비밀번호를 입력하세요"
              autoComplete="current-password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
            />

            <button className="submit-button" type="submit" disabled={isSubmitting}>
              {isSubmitting ? '로그인 요청 중...' : '로그인'}
            </button>

            <a className="secondary-action" href="http://localhost:8080/oauth2/authorization/naver">
              네이버로 로그인하기
            </a>

            {message && <p className="form-message">{message}</p>}
          </form>

          <div className="auth-footer">
            <Link className="text-action" to="/signup">
              회원가입
            </Link>
          </div>
        </section>
      </main>
    </div>
  )
}

export default LoginPage
