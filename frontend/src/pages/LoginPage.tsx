import { useRef, useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { login } from '../api/auth'
import Header from '../components/Header'
import { saveAuthSession } from '../utils/authStorage'
import './Auth.css'

// 로그인 화면 구성
function LoginPage() {
  // 로그인 성공 후 홈 화면 이동 함수 준비
  const navigate = useNavigate()

  // 로그인 중복 제출 방지 참조값 준비
  const isSubmittingRef = useRef(false)

  // 로그인 입력값 상태 관리
  const [loginId, setLoginId] = useState('')
  const [password, setPassword] = useState('')

  // 요청 결과 메시지와 진행 상태 관리
  const [message, setMessage] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  // 로그인 폼 제출 처리
  async function handleLogin(event: FormEvent<HTMLFormElement>) {
    // 브라우저 기본 form 제출 방지
    event.preventDefault()

    // 이미 로그인 요청 중이면 추가 요청 차단
    if (isSubmittingRef.current) {
      return
    }

    // 요청 시작 상태 반영
    isSubmittingRef.current = true
    setMessage('')
    setIsSubmitting(true)

    try {
      // 입력된 아이디와 비밀번호로 로그인 요청
      const loginResponse = await login({
        loginId,
        password,
      })

      // 백엔드 공통 응답에 data가 없는 경우 실패 처리
      if (!loginResponse) {
        throw new Error('로그인 응답 데이터가 없습니다.')
      }

      // 토큰과 닉네임을 로컬 저장소에 저장
      saveAuthSession(loginResponse)

      // 로그인 성공 후 홈 화면 이동
      navigate('/')
    } catch {
      // 로그인 실패 안내 메시지 표시
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
          <p className="login-copy">관심 있는 영화와 기록을 이어서 확인하세요.</p>

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

            {message && <p className="form-message">{message}</p>}
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
