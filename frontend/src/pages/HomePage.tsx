import { useState, type FormEvent } from 'react'
import { isApiError } from '../api/client'
import Header from '../components/Header'
import { authRequest } from '../utils/fetchUtil'
import './HomePage.css'

// 홈 화면 검색 응답 데이터 타입 정의
type MovieSearchResponse = {
  items?: unknown[]
}

// 영화 검색 화면 구성
function HomePage() {
  // 검색어 입력값 상태 관리
  const [query, setQuery] = useState('')

  // 검색 요청 메시지 상태 관리
  const [message, setMessage] = useState('')

  // 검색 요청 진행 상태 관리
  const [isSubmitting, setIsSubmitting] = useState(false)

  // 검색 폼 제출 처리
  async function handleSearch(event: FormEvent<HTMLFormElement>) {
    // 브라우저 기본 제출 동작 방지
    event.preventDefault()

    // 이전 요청 메시지 초기화
    setMessage('')
    setIsSubmitting(true)

    try {
      // 인증 헤더 포함 검색 요청 전송
      const response = await authRequest<MovieSearchResponse>('/movies', {
        method: 'GET',
      })

      // 검색 요청 완료 메시지 반영
      const resultCount = response?.items?.length ?? 0
      setMessage(`검색 요청을 전달했습니다. 응답 항목 수: ${resultCount}`)
    } catch (error) {
      // API 에러 메시지 분기 처리
      if (isApiError(error)) {
        setMessage(error.message)
      } else {
        setMessage('검색 요청에 실패했습니다.')
      }
    } finally {
      // 요청 종료 상태 반영
      setIsSubmitting(false)
    }
  }

  return (
    <div className="app">
      <Header showAuthActions />

      <main className="main-container">
        <section className="search-section" aria-labelledby="search-title">
          <h1 id="search-title">보고 싶은 영화를 찾아보세요</h1>
          <form className="search-form" onSubmit={handleSearch}>
            <label className="sr-only" htmlFor="movie-search">
              영화 검색
            </label>
            <input
              id="movie-search"
              className="search-input"
              type="search"
              placeholder="영화 제목, 배우, 감독을 검색해보세요"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
            <button className="search-button" type="submit" disabled={isSubmitting}>
              {isSubmitting ? '검색 중...' : '검색'}
            </button>
          </form>
          {message && <p className="search-message">{message}</p>}
        </section>
      </main>
    </div>
  )
}

export default HomePage
