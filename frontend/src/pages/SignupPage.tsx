import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { signup } from '../api/auth'
import { isApiError } from '../api/client'
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

// 중복 검증 대상 필드 타입 정의
type DuplicateSignupField = 'loginId' | 'nickname'

// 회원가입 화면 구성
function SignupPage() {
  // 회원가입 성공 후 로그인 화면 이동 준비
  const navigate = useNavigate()

  // 회원가입 입력값 상태 관리
  const [loginId, setLoginId] = useState('')
  const [password, setPassword] = useState('')
  const [nickname, setNickname] = useState('')

  // 서버 응답 기반 필드 에러 상태 관리
  const [validationErrors, setValidationErrors] = useState<SignupFormErrors>({})

  // 요청 결과 메시지와 진행 상태 관리
  const [message, setMessage] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  // 현재 입력값 객체 구성
  const formValues: SignupFormValues = {
    loginId,
    password,
    nickname,
  }

  // 제출 결과 기준 필드별 경고 문구 표시 여부 확인
  function shouldShowFieldError(fieldName: SignupFieldName) {
    return Boolean(validationErrors[fieldName])
  }

  // 중복 필드 목록 추출 처리
  function getDuplicateFields(errors: unknown): DuplicateSignupField[] {
    // 배열 형태 응답 여부 확인
    if (!Array.isArray(errors)) {
      return []
    }

    // 중복 허용 대상 필드 추출 처리
    return errors.filter(
      (field): field is DuplicateSignupField => field === 'loginId' || field === 'nickname',
    )
  }

  // 회원가입 중복 필드 에러 처리
  function handleDuplicateSignupError(errors: unknown) {
    // 중복 필드 목록 추출 처리
    const duplicateFields = getDuplicateFields(errors)
    const duplicateFieldErrors: SignupFormErrors = {}

    // 아이디 중복 메시지 반영
    if (duplicateFields.includes('loginId')) {
      duplicateFieldErrors.loginId = '이미 사용중인 아이디입니다.'
    }

    // 닉네임 중복 메시지 반영
    if (duplicateFields.includes('nickname')) {
      duplicateFieldErrors.nickname = '이미 사용중인 닉네임입니다.'
    }

    setValidationErrors(duplicateFieldErrors)
  }

  // 회원가입 폼 제출 처리
  async function handleSignup(event: FormEvent<HTMLFormElement>) {
    // 브라우저 기본 form 제출 방지
    event.preventDefault()

    // 이전 메시지 초기화
    setMessage('')

    // 프론트 입력값 검증 결과 계산
    const currentErrors = validateSignupForm(formValues)
    setValidationErrors(currentErrors)

    // 형식 검증 실패 시 서버 요청 차단
    if (hasSignupFormErrors(currentErrors)) {
      return
    }

    // 서버 요청 시작 상태 반영
    setIsSubmitting(true)

    try {
      // 회원가입 요청 전송
      await signup({
        loginId,
        password,
        nickname,
      })

      // 회원가입 성공 후 로그인 화면 이동
      navigate('/login')
    } catch (error) {
      // 중복 필드 응답 분기 처리
      if (isApiError(error) && error.status === 400 && error.code === 'DUPLICATE_FIELD') {
        handleDuplicateSignupError(error.errors)
        return
      }

      // 일반 실패 메시지 반영
      setMessage('회원가입 요청에 실패했습니다. 잠시 후 다시 시도해주세요.')
    } finally {
      // 서버 요청 종료 상태 반영
      setIsSubmitting(false)
    }
  }

  return (
    <div className="app">
      <Header />

      <main className="auth-container">
        <section className="login-panel" aria-labelledby="signup-title">
          <h1 id="signup-title">회원가입</h1>

          <form className="login-form signup-form" onSubmit={handleSignup} noValidate>
            <div className="field-group">
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
              />
              <div className="field-message-slot">
                {shouldShowFieldError('loginId') && (
                  <p className="field-message" id="signup-id-error">
                    {validationErrors.loginId}
                  </p>
                )}
              </div>
            </div>

            <div className="field-group">
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
              />
              <div className="field-message-slot">
                {shouldShowFieldError('password') && (
                  <p className="field-message" id="signup-password-error">
                    {validationErrors.password}
                  </p>
                )}
              </div>
            </div>

            <div className="field-group">
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
              />
              <div className="field-message-slot">
                {shouldShowFieldError('nickname') && (
                  <p className="field-message" id="nickname-error">
                    {validationErrors.nickname}
                  </p>
                )}
              </div>
            </div>

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
