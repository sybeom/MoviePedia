import { useEffect, useRef, useState, type FormEvent } from 'react'
import { API_BASE_URL, isApiError, request } from '../api/client'
import Header from '../components/Header'
import { clearAuthSession, getAuthSession } from '../utils/authStorage'
import { authRequest, isAuthSessionError } from '../utils/fetchUtil'
import './HomePage.css'

// 검색 응답 데이터 타입 정의
type MovieSearchResponse = {
  items?: unknown[]
}

// 로그인 상태 확인 응답 데이터 타입 정의
type AuthMeResponse = {
  loginId?: string
  nickname?: string
}

// 인기 영화 카드 데이터 타입 정의
type MovieCard = {
  id: string
  rank: string
  title: string
  poster: string
  genre: string
  voteAverage: string
}

const MOVIES_PER_PAGE = 5
const PLACEHOLDER_CARD_COUNT = 5

// 객체 데이터 여부 확인
function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null
}

// 문자열 필드 추출 처리
function getStringValue(record: Record<string, unknown>, keys: string[]) {
  for (const key of keys) {
    const value = record[key]

    if (typeof value === 'string' && value.trim()) {
      return value
    }
  }

  return ''
}

// 장르 문자열 변환 처리
function getGenreValue(record: Record<string, unknown>) {
  const genreValue = record.genre

  if (Array.isArray(genreValue)) {
    return genreValue
      .filter((value): value is string => typeof value === 'string' && value.trim().length > 0)
      .join(', ')
  }

  if (typeof genreValue === 'string' && genreValue.trim()) {
    return genreValue
  }

  return ''
}

// 대표 포스터 URL 추출 처리
function getPrimaryPosterUrl(poster: string) {
  return poster
    .split('|')
    .map((url) => url.trim())
    .find(Boolean) ?? ''
}

// 문자열 JSON 응답 파싱 처리
function parseMovieData(data: unknown) {
  if (typeof data !== 'string') {
    return data
  }

  try {
    return JSON.parse(data) as unknown
  } catch {
    return data
  }
}

// 영화 목록 원본 배열 추출 처리
function getMovieSource(data: unknown) {
  const parsedData = parseMovieData(data)

  if (Array.isArray(parsedData)) {
    return parsedData
  }

  if (!isRecord(parsedData)) {
    return []
  }

  const candidateLists = [
    parsedData.items,
    parsedData.movies,
    parsedData.weeklyBoxOfficeList,
    parsedData.results,
    parsedData.content,
  ]

  for (const candidate of candidateLists) {
    if (Array.isArray(candidate)) {
      return candidate
    }
  }

  if (isRecord(parsedData.boxOfficeResult) && Array.isArray(parsedData.boxOfficeResult.weeklyBoxOfficeList)) {
    return parsedData.boxOfficeResult.weeklyBoxOfficeList
  }

  return []
}

// 영화 목록 정규화 처리
function normalizeMovies(data: unknown): MovieCard[] {
  return getMovieSource(data)
    .filter(isRecord)
    .map((movie, index) => {
      const title = getStringValue(movie, ['title', 'movieNm', 'name']) || `영화 ${index + 1}`
      const rank = getStringValue(movie, ['rank', 'rnum']) || String(index + 1)
      const poster = getPrimaryPosterUrl(getStringValue(movie, ['poster', 'posterUrl', 'imageUrl']))
      const voteAverage = getStringValue(movie, ['voteAverage', 'vote', 'rating'])
      const genre = getGenreValue(movie)

      return {
        id: getStringValue(movie, ['movieCd', 'id']) || `${title}-${rank}-${index}`,
        rank,
        title,
        poster,
        genre,
        voteAverage,
      }
    })
}

// 빈 카드 목록 생성 처리
function createPlaceholderMovies(prefix: string, count = PLACEHOLDER_CARD_COUNT): MovieCard[] {
  return Array.from({ length: count }, (_, index) => ({
    id: `${prefix}-${index}`,
    rank: '',
    title: '',
    poster: '',
    genre: '',
    voteAverage: '',
  }))
}

// 홈 화면 구성
function HomePage() {
  // 검색어 상태 관리
  const [query, setQuery] = useState('')

  // 검색 요청 메시지 상태 관리
  const [message, setMessage] = useState('')

  // 검색 요청 진행 상태 관리
  const [isSubmitting, setIsSubmitting] = useState(false)

  // 인기 영화 목록 상태 관리
  const [popularMovies, setPopularMovies] = useState<MovieCard[]>([])

  // 인기 영화 로딩 상태 관리
  const [isPopularLoading, setIsPopularLoading] = useState(true)

  // 인기 영화 안내 메시지 상태 관리
  const [popularMessage, setPopularMessage] = useState('')

  // 인기 영화 페이지 상태 관리
  const [popularPage, setPopularPage] = useState(0)

  // 인증 상태 반영 유도 상태 관리
  const [, setAuthStateVersion] = useState(0)

  // 로그인 상태 확인 중복 방지 참조 준비
  const hasCheckedAuthRef = useRef(false)

  // 인기 영화 요청 중복 방지 참조 준비
  const hasLoadedPopularMoviesRef = useRef(false)

  const placeholderMovies = createPlaceholderMovies('placeholder')

  // 현재 상영중 임시 카드 목록 준비
  const nowPlayingPlaceholderMovies = createPlaceholderMovies('now-playing')

  // 개봉 예정작 임시 카드 목록 준비
  const upcomingPlaceholderMovies = createPlaceholderMovies('upcoming')

  // 캐러셀 표시용 영화 목록 계산
  const carouselMovies = popularMovies.length > 0 ? popularMovies : placeholderMovies

  // 캐러셀 전체 페이지 수 계산
  const totalPages = Math.max(1, Math.ceil(carouselMovies.length / MOVIES_PER_PAGE))

  // 현재 페이지 기준 표시 영화 목록 계산
  const visibleMovies = Array.from({ length: MOVIES_PER_PAGE }, (_, index) => {
    const movieIndex = popularPage * MOVIES_PER_PAGE + index

    return (
      carouselMovies[movieIndex] ?? {
        id: `empty-${movieIndex}`,
        rank: '',
        title: '',
        poster: '',
        genre: '',
        voteAverage: '',
      }
    )
  })

  // 로그아웃 상태 반영 처리
  function applyLoggedOutState() {
    clearAuthSession()
    setAuthStateVersion((previousVersion) => previousVersion + 1)
  }

  useEffect(() => {
    if (hasCheckedAuthRef.current) {
      return
    }

    hasCheckedAuthRef.current = true

    const session = getAuthSession()

    if (!session?.accessToken) {
      return
    }

    // 홈 진입 시 로그인 상태 검증 처리
    async function validateAuthSession() {
      try {
        await authRequest<AuthMeResponse>('/auth/me', {
          method: 'GET',
        })
      } catch (error) {
        if (isAuthSessionError(error)) {
          applyLoggedOutState()
        }
      }
    }

    void validateAuthSession()
  }, [])

  useEffect(() => {
    if (hasLoadedPopularMoviesRef.current) {
      return
    }

    hasLoadedPopularMoviesRef.current = true

    // 인기 영화 목록 조회 처리
    async function fetchPopularMovies() {
      try {
        const response = await request<unknown>('/movies', {
          method: 'GET',
        })

        const normalizedMovies = normalizeMovies(response)
        setPopularMovies(normalizedMovies)
        setPopularPage(0)

        if (normalizedMovies.length === 0) {
          setPopularMessage('인기 영화가 아직 없습니다.')
        }
      } catch {
        setPopularMessage('인기 영화를 불러오지 못했습니다.')
      } finally {
        setIsPopularLoading(false)
      }
    }

    void fetchPopularMovies()
  }, [])

  // 이전 영화 묶음 이동 처리
  function handlePrevPopularPage() {
    setPopularPage((previousPage) => Math.max(previousPage - 1, 0))
  }

  // 다음 영화 묶음 이동 처리
  function handleNextPopularPage() {
    setPopularPage((previousPage) => Math.min(previousPage + 1, totalPages - 1))
  }

  // 검색 요청 제출 처리
  async function handleSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    setMessage('')
    setIsSubmitting(true)

    try {
      const response = await fetch(`${API_BASE_URL}/movies`, {
        method: 'GET',
      })

      if (!response.ok) {
        throw new Error('영화 목록 요청이 실패했습니다.')
      }

      const responseBody = (await response.json()) as MovieSearchResponse
      const resultCount = responseBody?.items?.length ?? 0
      setMessage(`검색 요청이 전달됐습니다. 응답 항목 수 ${resultCount}`)
    } catch (error) {
      if (isApiError(error)) {
        setMessage(error.message)
      } else {
        setMessage('검색 요청이 실패했습니다.')
      }
    } finally {
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

        <section className="movie-list-section" aria-labelledby="popular-movie-title">
          <div className="movie-section-header">
            <div className="movie-section-copy">
              <h2 id="popular-movie-title">인기 영화</h2>
              <p>지금 가장 많이 찾는 영화들을 한눈에 볼 수 있습니다.</p>
            </div>
          </div>

          {isPopularLoading ? (
            <div className="movie-carousel">
              <div className="movie-grid">
                {placeholderMovies.map((movie) => (
                  <article className="movie-card-shell" key={`popular-skeleton-${movie.id}`}>
                    <div className="movie-poster-shell" />
                    <div className="movie-card-content movie-card-content-skeleton">
                      <div className="movie-title-shell" />
                      <div className="movie-meta-shell" />
                    </div>
                  </article>
                ))}
              </div>
            </div>
          ) : popularMovies.length > 0 ? (
            <div className="movie-carousel">
              {popularMovies.length > MOVIES_PER_PAGE && (
                <>
                  <button
                    className="movie-nav-button movie-nav-button-left"
                    type="button"
                    onClick={handlePrevPopularPage}
                    disabled={popularPage === 0}
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
                    onClick={handleNextPopularPage}
                    disabled={popularPage === totalPages - 1}
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
                {visibleMovies.map((movie) => (
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
                      <p>{movie.genre || ' '}</p>
                      <p>{movie.voteAverage ? `평점 ${movie.voteAverage}` : ' '}</p>
                    </div>
                  </article>
                ))}
              </div>
            </div>
          ) : (
            <p className="movie-section-message">{popularMessage}</p>
          )}
        </section>

        <section className="movie-list-section" aria-labelledby="now-playing-movie-title">
          <div className="movie-section-header">
            <div className="movie-section-copy">
              <h2 id="now-playing-movie-title">현재 상영중</h2>
              <p>지금 극장에서 만날 수 있는 영화들을 같은 형식으로 이어서 살펴볼 수 있습니다.</p>
            </div>
          </div>

          <div className="movie-carousel">
            <div className="movie-grid">
              {nowPlayingPlaceholderMovies.map((movie) => (
                <article className="movie-card-shell" key={movie.id}>
                  <div className="movie-poster-shell movie-poster-shell-placeholder" />
                  <div className="movie-card-content movie-card-content-skeleton">
                    <div className="movie-title-shell" />
                    <div className="movie-meta-shell" />
                  </div>
                </article>
              ))}
            </div>
          </div>
        </section>

        <section className="movie-list-section" aria-labelledby="upcoming-movie-title">
          <div className="movie-section-header">
            <div className="movie-section-copy">
              <h2 id="upcoming-movie-title">개봉 예정작</h2>
              <p>곧 만날 수 있는 영화들을 같은 형식으로 이어서 살펴볼 수 있습니다.</p>
            </div>
          </div>

          <div className="movie-carousel">
            <div className="movie-grid">
              {upcomingPlaceholderMovies.map((movie) => (
                <article className="movie-card-shell" key={movie.id}>
                  <div className="movie-poster-shell movie-poster-shell-placeholder" />
                  <div className="movie-card-content movie-card-content-skeleton">
                    <div className="movie-title-shell" />
                    <div className="movie-meta-shell" />
                  </div>
                </article>
              ))}
            </div>
          </div>
        </section>
      </main>
    </div>
  )
}

export default HomePage
