import { useEffect, useRef, useState, type FormEvent } from 'react'
import { API_BASE_URL, request, isApiError } from '../api/client'
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

// 인기 영화 데이터 타입 정의
type WeeklyBoxOfficeMovie = {
  id: string
  rank: string
  title: string
  poster: string
  voteAverage: string
}

const WEEKLY_MOVIES_PER_PAGE = 5
const WEEKLY_MOVIE_CARD_COUNT = 10

// 객체 데이터 여부 확인
function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null
}

// 문자열 값 추출 처리
function getStringValue(record: Record<string, unknown>, keys: string[]) {
  for (const key of keys) {
    const value = record[key]

    if (typeof value === 'string' && value.trim()) {
      return value
    }
  }

  return ''
}

// 대표 포스터 URL 추출 처리
function getPrimaryPosterUrl(poster: string) {
  return poster.split('|').map((url) => url.trim()).find(Boolean) ?? ''
}

// 문자열 JSON 데이터 변환 처리
function parseBoxOfficeData(data: unknown) {
  if (typeof data !== 'string') {
    return data
  }

  try {
    return JSON.parse(data) as unknown
  } catch {
    return data
  }
}

// 배열 형태 영화 목록 추출 처리
function getWeeklyMovieSource(data: unknown) {
  const parsedData = parseBoxOfficeData(data)

  if (Array.isArray(parsedData)) {
    return parsedData
  }

  if (!isRecord(parsedData)) {
    return []
  }

  const candidateLists = [
    parsedData.weeklyBoxOfficeList,
    parsedData.boxOfficeResult,
    parsedData.items,
    parsedData.movies,
  ]

  for (const candidate of candidateLists) {
    if (Array.isArray(candidate)) {
      return candidate
    }

    if (isRecord(candidate) && Array.isArray(candidate.weeklyBoxOfficeList)) {
      return candidate.weeklyBoxOfficeList
    }
  }

  return []
}

// 인기 영화 응답 데이터 정규화 처리
function normalizeWeeklyMovies(data: unknown): WeeklyBoxOfficeMovie[] {
  return getWeeklyMovieSource(data)
    .filter(isRecord)
    .map((movie, index) => {
      const title = getStringValue(movie, ['title', 'movieNm', 'name']) || `영화 ${index + 1}`
      const rank = getStringValue(movie, ['rank', 'rnum']) || String(index + 1)
      const poster = getPrimaryPosterUrl(getStringValue(movie, ['poster', 'posterUrl', 'imageUrl']))
      const voteAverage = getStringValue(movie, ['voteAverage', 'vote', 'rating'])

      return {
        id: getStringValue(movie, ['movieCd', 'id']) || `${title}-${rank}-${index}`,
        rank,
        title,
        poster,
        voteAverage,
      }
    })
}

// 홈 화면 구성
function HomePage() {
  // 검색어 입력값 상태 관리
  const [query, setQuery] = useState('')

  // 검색 요청 메시지 상태 관리
  const [message, setMessage] = useState('')

  // 검색 요청 진행 상태 관리
  const [isSubmitting, setIsSubmitting] = useState(false)

  // 인기 영화 목록 상태 관리
  const [weeklyMovies, setWeeklyMovies] = useState<WeeklyBoxOfficeMovie[]>([])

  // 인기 영화 로딩 상태 관리
  const [isWeeklyLoading, setIsWeeklyLoading] = useState(true)

  // 인기 영화 안내 메시지 상태 관리
  const [weeklyMessage, setWeeklyMessage] = useState('')

  // 인기 영화 현재 페이지 상태 관리
  const [weeklyPage, setWeeklyPage] = useState(0)

  // 인증 상태 반영용 렌더링 키 상태 관리
  const [, setAuthStateVersion] = useState(0)

  // 로그인 상태 확인 중복 요청 방지 참조 준비
  const hasCheckedAuthRef = useRef(false)

  // 인기 영화 요청 중복 방지 참조 준비
  const hasLoadedWeeklyMoviesRef = useRef(false)

  // 인기 영화 전체 페이지 수 계산
  const weeklyCarouselMovies =
    weeklyMovies.length > 0
      ? weeklyMovies
      : Array.from({ length: WEEKLY_MOVIE_CARD_COUNT }, (_, index) => ({
          id: `weekly-empty-${index}`,
          rank: String(index + 1),
          title: '',
          poster: '',
          voteAverage: '',
        }))

  const weeklyTotalPages = Math.max(1, Math.ceil(weeklyCarouselMovies.length / WEEKLY_MOVIES_PER_PAGE))

  // 현재 페이지 기준 표시 영화 목록 계산
  const visibleWeeklyMovies = Array.from({ length: WEEKLY_MOVIES_PER_PAGE }, (_, index) => {
    const movieIndex = weeklyPage * WEEKLY_MOVIES_PER_PAGE + index

    return (
      weeklyCarouselMovies[movieIndex] ?? {
        id: `weekly-placeholder-${movieIndex}`,
        rank: '',
        title: '',
        poster: '',
        voteAverage: '',
      }
    )
  })

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

  useEffect(() => {
    // 개발 모드 중복 실행 차단
    if (hasLoadedWeeklyMoviesRef.current) {
      return
    }

    // 인기 영화 요청 실행 표시
    hasLoadedWeeklyMoviesRef.current = true

    // 인기 영화 목록 조회 처리
    async function fetchWeeklyBoxOfficeMovies() {
      try {
        // 인기 영화 요청 전송
        const response = await request<unknown>('/movies', {
          method: 'GET',
        })

        // 응답 데이터 정규화 처리
        const normalizedMovies = normalizeWeeklyMovies(response)
        setWeeklyMovies(normalizedMovies)
        setWeeklyPage(0)

        // 빈 목록 안내 메시지 반영
        if (normalizedMovies.length === 0) {
          setWeeklyMessage('인기 영화가 아직 없습니다.')
        }
      } catch {
        // 인기 영화 실패 메시지 반영
        setWeeklyMessage('인기 영화를 불러오지 못했습니다.')
      } finally {
        // 인기 영화 로딩 종료 반영
        setIsWeeklyLoading(false)
      }
    }

    void fetchWeeklyBoxOfficeMovies()
  }, [])

  // 이전 인기 영화 묶음 이동 처리
  function handlePrevWeeklyPage() {
    setWeeklyPage((previousPage) => Math.max(previousPage - 1, 0))
  }

  // 다음 인기 영화 묶음 이동 처리
  function handleNextWeeklyPage() {
    setWeeklyPage((previousPage) => Math.min(previousPage + 1, weeklyTotalPages - 1))
  }

  // 검색 폼 제출 처리
  async function handleSearch(event: FormEvent<HTMLFormElement>) {
    // 브라우저 기본 제출 동작 방지
    event.preventDefault()

    // 이전 요청 메시지 초기화
    setMessage('')
    setIsSubmitting(true)

    try {
      // 테스트용 영화 목록 요청 전송
      const response = await fetch(`${API_BASE_URL}/movies`, {
        method: 'GET',
      })

      // 검색 요청 완료 메시지 반영
      if (!response.ok) {
        throw new Error('영화 목록 요청에 실패했습니다.')
      }

      const responseBody = (await response.json()) as MovieSearchResponse
      const resultCount = responseBody?.items?.length ?? 0
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

        <section className="movie-list-section" aria-labelledby="weekly-box-office-title">
          <div className="movie-section-header">
            <div className="movie-section-copy">
              <h2 id="weekly-box-office-title">인기 영화</h2>
              <p>지금 가장 많이 찾는 영화들을 한눈에 볼 수 있습니다.</p>
            </div>
          </div>

          {isWeeklyLoading ? (
            <div className="movie-carousel">
              <div className="movie-grid">
                {Array.from({ length: 5 }).map((_, index) => (
                  <article className="movie-card-shell" key={`weekly-skeleton-${index}`}>
                    <div className="movie-poster-shell" />
                    <div className="movie-card-content movie-card-content-skeleton">
                      <div className="movie-title-shell" />
                      <div className="movie-meta-shell" />
                    </div>
                  </article>
                ))}
              </div>
            </div>
          ) : weeklyMovies.length > 0 ? (
            <div className="movie-carousel">
              {weeklyMovies.length > WEEKLY_MOVIES_PER_PAGE && (
                <>
                  <button
                    className="movie-nav-button movie-nav-button-left"
                    type="button"
                    onClick={handlePrevWeeklyPage}
                    disabled={weeklyPage === 0}
                    aria-label="이전 인기 영화 보기"
                  >
                    <span className="movie-nav-icon" aria-hidden="true">
                      <svg viewBox="0 0 24 24" focusable="false" aria-hidden="true">
                        <path d="M14.5 5 8 12l6.5 7" />
                      </svg>
                    </span>
                  </button>
                  <button
                    className="movie-nav-button movie-nav-button-right"
                    type="button"
                    onClick={handleNextWeeklyPage}
                    disabled={weeklyPage === weeklyTotalPages - 1}
                    aria-label="다음 인기 영화 보기"
                  >
                    <span className="movie-nav-icon" aria-hidden="true">
                      <svg viewBox="0 0 24 24" focusable="false" aria-hidden="true">
                        <path d="m9.5 5 6.5 7-6.5 7" />
                      </svg>
                    </span>
                  </button>
                </>
              )}

              <div className="movie-grid">
                {visibleWeeklyMovies.map((movie) => (
                  <article
                    className={`movie-card-shell${movie.title ? '' : ' movie-card-shell-empty'}`}
                    key={movie.id}
                  >
                    <div className="movie-poster-shell">
                      {movie.poster ? (
                        <img className="movie-poster-image" src={movie.poster} alt={movie.title} />
                      ) : null}
                      {movie.rank && <span className="movie-rank-badge">{movie.rank}</span>}
                    </div>
                    <div className="movie-card-content">
                      <h3>{movie.title || ' '}</h3>
                      <p>{movie.voteAverage ? `평점 ${movie.voteAverage}` : ' '}</p>
                    </div>
                  </article>
                ))}
              </div>
            </div>
          ) : (
            <p className="movie-section-message">{weeklyMessage}</p>
          )}
        </section>
      </main>
    </div>
  )
}

export default HomePage
