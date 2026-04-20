import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { signup } from '../api/auth'
import Header from '../components/Header'
import {
  hasSignupFormErrors,
  validateSignupForm,
  type SignupFormErrors,
  type SignupFormValues,
} from '../utils/signupValidation'
import './Auth.css'

// 회원가입 필드 이름 타입 정의
type SignupFieldName = keyof SignupFormValues

// 회원가입 화면 구성
function SignupPage() {
  // 회원가입 성공 후 로그인 화면 이동 함수 준비
  const navigate = useNavigate()

  // 회원가입 입력값 상태 관리
  const [loginId, setLoginId] = useState('')
  const [password, setPassword] = useState('')
  const [nickname, setNickname] = useState('')

  // 회원가입 버튼 클릭 시점의 검증 결과 상태 관리
  const [validationErrors, setValidationErrors] = useState<SignupFormErrors>({})

  // 요청 결과 메시지와 진행 상태 관리
  const [message, setMessage] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  // 현재 폼 값 객체 구성
  const formValues: SignupFormValues = {
    loginId,
    password,
    nickname,
  }

  // 제출 결과 기준 필드별 경고 문구 표시 여부 확인
  function shouldShowFieldError(fieldName: SignupFieldName) {
    return Boolean(validationErrors[fieldName])
  }

  // 회원가입 폼 제출 처리
  async function handleSignup(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setMessage('')

    const currentErrors = validateSignupForm(formValues)
    setValidationErrors(currentErrors)

    if (hasSignupFormErrors(currentErrors)) {
      return
    }

    setIsSubmitting(true)

    try {
      await signup({
        loginId,
        password,
        nickname,
      })

      navigate('/login')
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
              aria-invalid={shouldShowFieldError('loginId')}
              aria-describedby="signup-id-error"
              required
            />
            {shouldShowFieldError('loginId') && (
              <p className="field-message" id="signup-id-error">
                {validationErrors.loginId}
              </p>
            )}

            <label htmlFor="signup-password">비밀번호</label>
            <input
              id="signup-password"
              type="password"
              placeholder="비밀번호를 입력하세요"
              autoComplete="new-password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              aria-invalid={shouldShowFieldError('password')}
              aria-describedby="signup-password-error"
              required
            />
            {shouldShowFieldError('password') && (
              <p className="field-message" id="signup-password-error">
                {validationErrors.password}
              </p>
            )}

            <label htmlFor="nickname">닉네임</label>
            <input
              id="nickname"
              type="text"
              placeholder="닉네임을 입력하세요"
              autoComplete="nickname"
              value={nickname}
              onChange={(event) => setNickname(event.target.value)}
              aria-invalid={shouldShowFieldError('nickname')}
              aria-describedby="nickname-error"
              required
            />
            {shouldShowFieldError('nickname') && (
              <p className="field-message" id="nickname-error">
                {validationErrors.nickname}
              </p>
            )}

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
