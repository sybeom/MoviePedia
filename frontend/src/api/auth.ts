import { request } from './client'

export type SignupRequest = {
  loginId: string
  password: string
  nickname: string
}

export function signup(data: SignupRequest) {
  return request<void>('/signup', {
    method: 'POST',
    body: data,
  })
}
