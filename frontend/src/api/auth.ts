import { request } from './client'

// 회원가입 요청 데이터 타입 정의
export type SignupRequest = {
  loginId: string
  password: string
  nickname: string
}

// 로그인 요청 데이터 타입 정의
export type LoginRequest = {
  loginId: string
  password: string
}

// 로그인 응답 데이터 타입 정의
export type LoginResponse = {
  accessToken: string
  refreshToken: string
  nickname: string
}

// 쿠키 교환 응답 데이터 타입 정의
export type ExchangeJwtResponse = {
  accessToken: string
  refreshToken: string
  nickname?: string
}

// 회원가입 API 요청 처리
export function signup(data: SignupRequest) {
  return request<void>('/members', {
    method: 'POST',
    body: data,
  })
}

// 로그인 API 요청 처리
export function login(data: LoginRequest) {
  return request<LoginResponse>('/login', {
    method: 'POST',
    body: data,
  })
}

// 쿠키 기반 토큰 교환 요청 처리
export function exchangeJwt() {
  return request<ExchangeJwtResponse>('/jwt/exchange', {
    method: 'POST',
    credentials: 'include',
  })
}
