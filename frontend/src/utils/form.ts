import type { FormEvent } from 'react'

export function preventFormSubmit(event: FormEvent<HTMLFormElement>) {
  event.preventDefault()
}
