import { useEffect, useRef, useState, type FormEvent } from 'react'
import { isApiError } from '../api/client'
import Header from '../components/Header'
import { clearAuthSession, getAuthSession } from '../utils/authStorage'
import { authRequest, isAuthSessionError } from '../utils/fetchUtil'
import './HomePage.css'

// 홈 화면 검색 응답 데이터 타입 정의
type MovieSearchResponse = {
  items?: unknown[]
}

// 로그인 상태 확인 응답 데이터 타입 정의
type AuthMeResponse = {
  loginId?: string
  nickname?: string
}

// 영화 검색 화면 구성
function HomePage() {
  // 검색어 입력값 상태 관리
  const [query, setQuery] = useState('')

  // 검색 요청 메시지 상태 관리
  const [message, setMessage] = useState('')

  // 검색 요청 진행 상태 관리
  const [isSubmitting, setIsSubmitting] = useState(false)

  // 인증 상태 반영용 렌더링 키 상태 관리
  const [, setAuthStateVersion] = useState(0)

  // 로그인 상태 확인 중복 요청 방지 참조 준비
  const hasCheckedAuthRef = useRef(false)

  // 홈 화면 로그아웃 상태 반영 처리
  function applyLoggedOutState() {
    // 로컬 세션 정리 처리
    clearAuthSession()

    // 헤더 재렌더링 유도
    setAuthStateVersion((previousVersion) => previousVersion + 1)
  }

  useEffect(() => {
    // 개발 모드 중복 실행 차단
    if (hasCheckedAuthRef.current) {
      return
    }

    // 로그인 상태 확인 실행 표시
    hasCheckedAuthRef.current = true

    // 로컬 세션 존재 여부 확인
    const session = getAuthSession()

    // 비로그인 상태 진입 분기 처리
    if (!session?.accessToken) {
      return
    }

    // 홈 진입 시 로그인 상태 확인 처리
    async function validateAuthSession() {
      try {
        // 액세스 토큰 기반 사용자 정보 요청 전송
        await authRequest<AuthMeResponse>('/auth/me', {
          method: 'GET',
        })
      } catch (error) {
        // 인증 세션 만료 분기 처리
        if (isAuthSessionError(error)) {
          applyLoggedOutState()
        }
      }
    }

    void validateAuthSession()
  }, [])

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
      // 인증 세션 만료 분기 처리
      if (isAuthSessionError(error)) {
        applyLoggedOutState()
        setMessage('로그인이 필요합니다.')
      } else if (isApiError(error)) {
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
