import { useEffect, useRef, useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { request } from '../api/client'
import rating12Icon from '../assets/ratings/12.svg'
import rating15Icon from '../assets/ratings/15.svg'
import rating19Icon from '../assets/ratings/19.svg'
import ratingAllIcon from '../assets/ratings/all.svg'
import Header from '../components/Header'
import './HomePage.css'

// 영화 묶음 응답 타입 정의
type MovieCollectionResponse = {
  popular?: unknown[]
  upcoming?: unknown[]
  nowPlaying?: unknown[]
}

// 영화 카드 데이터 타입 정의
type MovieCard = {
  id: string
  rank: string
  title: string
  poster: string
  genre: string
  voteAverage: string
  certification: string
}

// 영화 섹션 속성 타입 정의
type MovieSectionProps = {
  title: string
  titleId: string
  movies: MovieCard[]
  placeholderMovies: MovieCard[]
  isLoading: boolean
  message: string
  currentPage: number
  totalPages: number
  visibleMovies: MovieCard[]
  onPrevPage: () => void
  onNextPage: () => void
  showRankBadge?: boolean
}

const MOVIES_PER_PAGE = 5
const PLACEHOLDER_CARD_COUNT = 5
const CERTIFICATION_ICON_MAP: Record<string, string> = {
  '12': rating12Icon,
  '15': rating15Icon,
  '19': rating19Icon,
  ALL: ratingAllIcon,
}

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

// 문자열 또는 숫자 필드 추출 처리
function getScalarStringValue(record: Record<string, unknown>, keys: string[]) {
  for (const key of keys) {
    const value = record[key]

    if (typeof value === 'string' && value.trim()) {
      return value
    }

    if (typeof value === 'number' && Number.isFinite(value)) {
      return String(value)
    }
  }

  return ''
}

// 영화 식별자 추출 처리
function getMovieIdentifier(record: Record<string, unknown>) {
  const directIdentifier = getScalarStringValue(record, [
    'movieCode',
    'movie_code',
    'id',
    'movieId',
    'movieCd',
    'code',
  ])

  if (directIdentifier) {
    return directIdentifier
  }

  const nestedCandidates = [record.movie, record.content, record.item, record.data]

  for (const candidate of nestedCandidates) {
    if (!isRecord(candidate)) {
      continue
    }

    const nestedIdentifier = getScalarStringValue(candidate, [
      'movieCode',
      'movie_code',
      'id',
      'movieId',
      'movieCd',
      'code',
    ])

    if (nestedIdentifier) {
      return nestedIdentifier
    }
  }

  return ''
}

// 장르 문자열 변환 처리
function getGenreValue(record: Record<string, unknown>) {
  const genreValue = record.genre

  if (Array.isArray(genreValue)) {
    return genreValue
      .map((value) => {
        if (typeof value === 'string' && value.trim()) {
          return value
        }

        if (isRecord(value)) {
          return getStringValue(value, ['name', 'genre'])
        }

        return ''
      })
      .filter(Boolean)
      .join(', ')
  }

  if (typeof genreValue === 'string' && genreValue.trim()) {
    return genreValue
  }

  return ''
}

// 대표 포스터 URL 추출 처리
function getPrimaryPosterUrl(poster: string) {
  return (
    poster
      .split('|')
      .map((url) => url.trim())
      .find(Boolean) ?? ''
  )
}

// 관람등급 문자열 추출 처리
function getCertificationValue(record: Record<string, unknown>) {
  const certification = getStringValue(record, ['certification'])

  if (!certification) {
    return ''
  }

  return certification.toUpperCase()
}

// 관람등급 아이콘 경로 조회 처리
function getCertificationIcon(certification: string) {
  return CERTIFICATION_ICON_MAP[certification] ?? ''
}

// 영화 목록 정규화 처리
function normalizeMovies(data: unknown): MovieCard[] {
  if (!Array.isArray(data)) {
    return []
  }

  return data.filter(isRecord).map((movie, index) => {
    const title = getStringValue(movie, ['title', 'movieNm', 'name', 'movieTitle']) || `영화 ${index + 1}`
    const rank = getScalarStringValue(movie, ['rank', 'rnum']) || String(index + 1)
    const poster = getPrimaryPosterUrl(
      getStringValue(movie, ['poster', 'posterUrl', 'imageUrl', 'posterPath', 'poster_path']),
    )
    const genre = getGenreValue(movie)
    const voteAverage = getScalarStringValue(movie, ['voteAverage', 'vote', 'rating', 'vote_average'])
    const certification = getCertificationValue(movie)

    return {
      id: getMovieIdentifier(movie) || `${title}-${rank}-${index}`,
      rank,
      title,
      poster,
      genre,
      voteAverage,
      certification,
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
    certification: '',
  }))
}

// 현재 페이지 노출 목록 계산 처리
function getVisibleMovies(movies: MovieCard[], page: number) {
  return Array.from({ length: MOVIES_PER_PAGE }, (_, index) => {
    const movieIndex = page * MOVIES_PER_PAGE + index

    return (
      movies[movieIndex] ?? {
        id: `movie-empty-${movieIndex}`,
        rank: '',
        title: '',
        poster: '',
        genre: '',
        voteAverage: '',
        certification: '',
      }
    )
  })
}

// 등급 아이콘 포함 제목 행 구성 처리
function MovieTitleRow({ movie }: { movie: MovieCard }) {
  const certificationIcon = getCertificationIcon(movie.certification)

  return (
    <div className="movie-title-row">
      {movie.certification
        ? certificationIcon
          ? (
            <img className="movie-certification-icon" src={certificationIcon} alt={`${movie.certification} 등급`} />
            )
          : (
            <span className="movie-certification-fallback">{movie.certification}</span>
            )
        : null}
      <h3>
        <span className="movie-title-text">{movie.title || ' '}</span>
      </h3>
    </div>
  )
}

// 영화 상세 이동 경로 계산 처리
function getMovieDetailPath(movie: MovieCard) {
  return `/movies/${movie.id}`
}

// 영화 섹션 공통 화면 구성
function MovieSection({
  title,
  titleId,
  movies,
  placeholderMovies,
  isLoading,
  message,
  currentPage,
  totalPages,
  visibleMovies,
  onPrevPage,
  onNextPage,
  showRankBadge = false,
}: MovieSectionProps) {
  return (
    <section className="movie-list-section" aria-labelledby={titleId}>
      <div className="movie-section-header">
        <div className="movie-section-copy">
          <h2 id={titleId}>{title}</h2>
        </div>
      </div>

      {isLoading ? (
        <div className="movie-carousel">
          <div className="movie-grid">
            {placeholderMovies.map((movie) => (
              <article className="movie-card-shell" key={movie.id}>
                <div className="movie-poster-shell" />
                <div className="movie-card-content movie-card-content-skeleton">
                  <div className="movie-title-shell" />
                  <div className="movie-meta-shell" />
                </div>
              </article>
            ))}
          </div>
        </div>
      ) : movies.length > 0 ? (
        <div className="movie-carousel">
          {movies.length > MOVIES_PER_PAGE ? (
            <>
              <button
                className="movie-nav-button movie-nav-button-left"
                type="button"
                onClick={onPrevPage}
                disabled={currentPage === 0}
                aria-label={`이전 ${title} 보기`}
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
                onClick={onNextPage}
                disabled={currentPage === totalPages - 1}
                aria-label={`다음 ${title} 보기`}
              >
                <span className="movie-nav-icon" aria-hidden="true">
                  <svg viewBox="0 0 24 24" focusable="false" aria-hidden="true">
                    <path d="m9.5 5 6.5 7-6.5 7" />
                  </svg>
                </span>
              </button>
            </>
          ) : null}

          <div className="movie-grid">
            {visibleMovies.map((movie) => (
              <article className={`movie-card-shell${movie.title ? '' : ' movie-card-shell-empty'}`} key={movie.id}>
                <Link
                  className="movie-card-link"
                  to={getMovieDetailPath(movie)}
                  state={{ movie: { id: movie.id, title: movie.title, poster: movie.poster } }}
                >
                  <div className="movie-poster-shell">
                    {movie.poster ? <img className="movie-poster-image" src={movie.poster} alt={movie.title} /> : null}
                    {showRankBadge && movie.rank ? <span className="movie-rank-badge">{movie.rank}</span> : null}
                  </div>
                </Link>
                <div className="movie-card-content">
                  <Link
                    className="movie-title-link"
                    to={getMovieDetailPath(movie)}
                    state={{ movie: { id: movie.id, title: movie.title, poster: movie.poster } }}
                  >
                    <MovieTitleRow movie={movie} />
                  </Link>
                  <p>{movie.genre || ' '}</p>
                  <p>{movie.voteAverage ? `평점 ${movie.voteAverage}` : ' '}</p>
                </div>
              </article>
            ))}
          </div>
        </div>
      ) : (
        <p className="movie-section-message">{message}</p>
      )}
    </section>
  )
}

// 홈 화면 구성
function HomePage() {
  // 검색어 상태 관리
  const [query, setQuery] = useState('')

  // 검색 메시지 상태 관리
  const [message, setMessage] = useState('')

  // 검색 진행 상태 관리
  const [isSubmitting, setIsSubmitting] = useState(false)

  // 인기 영화 목록 상태 관리
  const [popularMovies, setPopularMovies] = useState<MovieCard[]>([])

  // 현재 상영중 목록 상태 관리
  const [nowPlayingMovies, setNowPlayingMovies] = useState<MovieCard[]>([])

  // 개봉 예정작 목록 상태 관리
  const [upcomingMovies, setUpcomingMovies] = useState<MovieCard[]>([])

  // 인기 영화 로딩 상태 관리
  const [isPopularLoading, setIsPopularLoading] = useState(true)

  // 현재 상영중 로딩 상태 관리
  const [isNowPlayingLoading, setIsNowPlayingLoading] = useState(true)

  // 개봉 예정작 로딩 상태 관리
  const [isUpcomingLoading, setIsUpcomingLoading] = useState(true)

  // 인기 영화 메시지 상태 관리
  const [popularMessage, setPopularMessage] = useState('')

  // 현재 상영중 메시지 상태 관리
  const [nowPlayingMessage, setNowPlayingMessage] = useState('')

  // 개봉 예정작 메시지 상태 관리
  const [upcomingMessage, setUpcomingMessage] = useState('')

  // 인기 영화 페이지 상태 관리
  const [popularPage, setPopularPage] = useState(0)

  // 현재 상영중 페이지 상태 관리
  const [nowPlayingPage, setNowPlayingPage] = useState(0)

  // 개봉 예정작 페이지 상태 관리
  const [upcomingPage, setUpcomingPage] = useState(0)

  // 영화 목록 요청 중복 방지 참조 준비
  const hasLoadedMoviesRef = useRef(false)

  const popularPlaceholderMovies = createPlaceholderMovies('popular-placeholder')
  const nowPlayingPlaceholderMovies = createPlaceholderMovies('now-playing-placeholder')
  const upcomingPlaceholderMovies = createPlaceholderMovies('upcoming-placeholder')

  const popularCarouselMovies = popularMovies.length > 0 ? popularMovies : popularPlaceholderMovies
  const nowPlayingCarouselMovies = nowPlayingMovies.length > 0 ? nowPlayingMovies : nowPlayingPlaceholderMovies
  const upcomingCarouselMovies = upcomingMovies.length > 0 ? upcomingMovies : upcomingPlaceholderMovies

  const popularTotalPages = Math.max(1, Math.ceil(popularCarouselMovies.length / MOVIES_PER_PAGE))
  const nowPlayingTotalPages = Math.max(1, Math.ceil(nowPlayingCarouselMovies.length / MOVIES_PER_PAGE))
  const upcomingTotalPages = Math.max(1, Math.ceil(upcomingCarouselMovies.length / MOVIES_PER_PAGE))

  const visiblePopularMovies = getVisibleMovies(popularCarouselMovies, popularPage)
  const visibleNowPlayingMovies = getVisibleMovies(nowPlayingCarouselMovies, nowPlayingPage)
  const visibleUpcomingMovies = getVisibleMovies(upcomingCarouselMovies, upcomingPage)

  useEffect(() => {
    if (hasLoadedMoviesRef.current) {
      return
    }

    hasLoadedMoviesRef.current = true

    // 영화 목록 통합 조회 처리
    async function fetchMovies() {
      try {
        const response = await request<MovieCollectionResponse>('/movies', {
          method: 'GET',
        })

        const normalizedPopularMovies = normalizeMovies(response?.popular ?? [])
        const normalizedNowPlayingMovies = normalizeMovies(response?.nowPlaying ?? [])
        const normalizedUpcomingMovies = normalizeMovies(response?.upcoming ?? [])

        setPopularMovies(normalizedPopularMovies)
        setNowPlayingMovies(normalizedNowPlayingMovies)
        setUpcomingMovies(normalizedUpcomingMovies)

        setPopularPage(0)
        setNowPlayingPage(0)
        setUpcomingPage(0)

        setPopularMessage(normalizedPopularMovies.length === 0 ? '인기 영화가 아직 없습니다.' : '')
        setNowPlayingMessage(normalizedNowPlayingMovies.length === 0 ? '현재 상영중인 영화가 아직 없습니다.' : '')
        setUpcomingMessage(normalizedUpcomingMovies.length === 0 ? '개봉 예정작이 아직 없습니다.' : '')
      } catch {
        setPopularMessage('인기 영화를 불러오지 못했습니다.')
        setNowPlayingMessage('현재 상영중인 영화를 불러오지 못했습니다.')
        setUpcomingMessage('개봉 예정작을 불러오지 못했습니다.')
      } finally {
        setIsPopularLoading(false)
        setIsNowPlayingLoading(false)
        setIsUpcomingLoading(false)
      }
    }

    void fetchMovies()
  }, [])

  // 이전 인기 영화 페이지 이동 처리
  function handlePrevPopularPage() {
    setPopularPage((previousPage) => Math.max(previousPage - 1, 0))
  }

  // 다음 인기 영화 페이지 이동 처리
  function handleNextPopularPage() {
    setPopularPage((previousPage) => Math.min(previousPage + 1, popularTotalPages - 1))
  }

  // 이전 현재 상영중 페이지 이동 처리
  function handlePrevNowPlayingPage() {
    setNowPlayingPage((previousPage) => Math.max(previousPage - 1, 0))
  }

  // 다음 현재 상영중 페이지 이동 처리
  function handleNextNowPlayingPage() {
    setNowPlayingPage((previousPage) => Math.min(previousPage + 1, nowPlayingTotalPages - 1))
  }

  // 이전 개봉 예정작 페이지 이동 처리
  function handlePrevUpcomingPage() {
    setUpcomingPage((previousPage) => Math.max(previousPage - 1, 0))
  }

  // 다음 개봉 예정작 페이지 이동 처리
  function handleNextUpcomingPage() {
    setUpcomingPage((previousPage) => Math.min(previousPage + 1, upcomingTotalPages - 1))
  }

  // 검색 요청 제출 처리
  async function handleSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    setMessage('')
    setIsSubmitting(true)

    try {
      const response = await request<MovieCollectionResponse>('/movies', {
        method: 'GET',
      })

      const resultCount =
        (response?.popular?.length ?? 0) +
        (response?.nowPlaying?.length ?? 0) +
        (response?.upcoming?.length ?? 0)

      setMessage(`검색 요청이 전달되었습니다. 응답 항목 수 ${resultCount}`)
    } catch {
      setMessage('검색 요청에 실패했습니다.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="app">
      <Header showAuthActions />

      <main className="main-container">
        <section className="search-section" aria-labelledby="search-title">
          <h1 id="search-title">보고 싶은 영화를 찾아보세요.</h1>
          <form className="search-form" onSubmit={handleSearch}>
            <label className="sr-only" htmlFor="movie-search">
              영화 검색
            </label>
            <input
              id="movie-search"
              className="search-input"
              type="search"
              placeholder="영화 제목, 배우, 감독을 검색해보세요."
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
            <button className="search-button" type="submit" disabled={isSubmitting}>
              {isSubmitting ? '검색 중...' : '검색'}
            </button>
          </form>
          {message ? <p className="search-message">{message}</p> : null}
        </section>

        <MovieSection
          title="인기 영화"
          titleId="popular-movie-title"
          movies={popularMovies}
          placeholderMovies={popularPlaceholderMovies}
          isLoading={isPopularLoading}
          message={popularMessage}
          currentPage={popularPage}
          totalPages={popularTotalPages}
          visibleMovies={visiblePopularMovies}
          onPrevPage={handlePrevPopularPage}
          onNextPage={handleNextPopularPage}
          showRankBadge
        />

        <MovieSection
          title="현재 상영중"
          titleId="now-playing-movie-title"
          movies={nowPlayingMovies}
          placeholderMovies={nowPlayingPlaceholderMovies}
          isLoading={isNowPlayingLoading}
          message={nowPlayingMessage}
          currentPage={nowPlayingPage}
          totalPages={nowPlayingTotalPages}
          visibleMovies={visibleNowPlayingMovies}
          onPrevPage={handlePrevNowPlayingPage}
          onNextPage={handleNextNowPlayingPage}
        />

        <MovieSection
          title="개봉 예정작"
          titleId="upcoming-movie-title"
          movies={upcomingMovies}
          placeholderMovies={upcomingPlaceholderMovies}
          isLoading={isUpcomingLoading}
          message={upcomingMessage}
          currentPage={upcomingPage}
          totalPages={upcomingTotalPages}
          visibleMovies={visibleUpcomingMovies}
          onPrevPage={handlePrevUpcomingPage}
          onNextPage={handleNextUpcomingPage}
        />
      </main>
    </div>
  )
}

export default HomePage
