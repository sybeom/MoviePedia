// 회원가입 폼 값 타입 정의
export type SignupFormValues = {
  loginId: string
  password: string
  nickname: string
}

// 회원가입 폼 에러 타입 정의
export type SignupFormErrors = Partial<Record<keyof SignupFormValues, string>>

// 아이디 검증 정규식 정의
const LOGIN_ID_PATTERN = /^[A-Za-z0-9]{5,10}$/

// 비밀번호 검증 정규식 정의
const PASSWORD_PATTERN = /^[A-Za-z0-9!@#$]{2,10}$/

// 닉네임 검증 정규식 정의
const NICKNAME_PATTERN = /^[A-Za-z0-9가-힣]{2,6}$/

// 회원가입 에러 문구 정의
const SIGNUP_ERROR_MESSAGES: Required<SignupFormErrors> = {
  loginId: '아이디는 5~10자의 영어, 숫자 조합이어야 합니다.',
  password: '비밀번호는 2~10자의 영어, 숫자, 특수문자(!@#$) 조합이어야 합니다.',
  nickname: '닉네임은 특수문자를 제외한 2~6자여야 합니다.',
}

// 회원가입 폼 전체 검증 처리
export function validateSignupForm(values: SignupFormValues): SignupFormErrors {
  const errors: SignupFormErrors = {}

  if (!LOGIN_ID_PATTERN.test(values.loginId)) {
    errors.loginId = SIGNUP_ERROR_MESSAGES.loginId
  }

  if (!PASSWORD_PATTERN.test(values.password)) {
    errors.password = SIGNUP_ERROR_MESSAGES.password
  }

  if (!NICKNAME_PATTERN.test(values.nickname)) {
    errors.nickname = SIGNUP_ERROR_MESSAGES.nickname
  }

  return errors
}

// 회원가입 폼 에러 존재 여부 확인
export function hasSignupFormErrors(errors: SignupFormErrors) {
  return Object.keys(errors).length > 0
}
