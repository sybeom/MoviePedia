import type { FormEvent } from 'react'

// 폼 제출 시 브라우저 새로고침 방지 처리
export function preventFormSubmit(event: FormEvent<HTMLFormElement>) {
  event.preventDefault()
}
