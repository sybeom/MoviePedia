import { request } from './client'

// 회원가입 요청 데이터 타입 정의
export type SignupRequest = {
  loginId: string
  password: string
  nickname: string
}

// 회원가입 API 요청 처리
export function signup(data: SignupRequest) {
  return request<void>('/members', {
    method: 'POST',
    body: data,
  })
}
