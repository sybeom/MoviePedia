import { useCallback, useEffect, useRef, useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import darkModeIcon from '../assets/icons/dark_mode.svg'
import lightModeIcon from '../assets/icons/light_mode.svg'
import loadingIcon from '../assets/icons/loading.svg'
import naverLoginButtonImage from '../assets/icons/NAVER_login.png'
import nextIcon from '../assets/icons/next.svg'
import previousIcon from '../assets/icons/previous.svg'
import searchIcon from '../assets/icons/search.svg'
import { login, logout } from '../api/auth'
import { isApiError, request } from '../api/client'
import HomeSearchResults from '../components/home/HomeSearchResults'
import {
  clearAuthSession,
  getAuthSession,
  saveAuthSession,
  subscribeAuthSessionChange,
  type AuthSession,
} from '../utils/authStorage'
import './HomePage.css'

type SearchMovie = {
  code: string
  title: string
}

type PopularMovie = {
  code: string
  title: string
  poster: string
}

type BannerMovie = {
  code: string
  title: string
  backdrop: string
}

type MovieCollectionResponse = {
  popular?: unknown
  nowPlaying?: unknown
  upcoming?: unknown
}

type HomeTheme = 'dark' | 'light'

const SEARCH_DEBOUNCE_MS = 500
const HOME_THEME_STORAGE_KEY = 'moviepedia.home.theme'
const PRIMARY_NAV_ITEMS = ['영화', 'TV 시리즈']
const BANNER_AUTOPLAY_MS = 5000
const BANNER_TRANSITION_MS = 520
const TMDB_IMAGE_BASE_URL = 'https://image.tmdb.org/t/p/original'

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null
}

function getStringValue(record: Record<string, unknown>, keys: string[]) {
  for (const key of keys) {
    const value = record[key]

    if (typeof value === 'string' && value.trim()) {
      return value
    }
  }

  return ''
}

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

function getImageSource(value: string) {
  const normalizedValue =
    value
      .split('|')
      .map((item) => item.trim())
      .find(Boolean) ?? ''

  if (!normalizedValue) {
    return ''
  }

  if (/^https?:\/\//i.test(normalizedValue)) {
    return normalizedValue
  }

  if (normalizedValue.startsWith('//')) {
    return `https:${normalizedValue}`
  }

  if (normalizedValue.startsWith('/')) {
    return `${TMDB_IMAGE_BASE_URL}${normalizedValue}`
  }

  return normalizedValue
}

function normalizeSearchMovies(data: unknown): SearchMovie[] {
  if (!Array.isArray(data)) {
    return []
  }

  return data
    .filter(isRecord)
    .map((value) => {
      const code = getScalarStringValue(value, ['code', 'movieCode', 'movieCd'])
      const title = getStringValue(value, ['title', 'movieNm', 'name'])

      return { code, title }
    })
    .filter((value) => value.code && value.title)
}

function normalizePopularMovies(data: unknown): PopularMovie[] {
  if (!Array.isArray(data)) {
    return []
  }

  return data
    .filter(isRecord)
    .map((value) => {
      const code = getScalarStringValue(value, ['code', 'movieCode', 'movieCd'])
      const title = getStringValue(value, ['title', 'movieNm', 'name'])
      const poster = getImageSource(getStringValue(value, ['poster', 'posterPath', 'poster_path']))

      return { code, title, poster }
    })
    .filter((value) => value.code && value.title)
}

function normalizeBannerMovies(data: unknown): BannerMovie[] {
  const list = Array.isArray(data)
    ? data
    : isRecord(data) && Array.isArray(data.banners)
      ? data.banners
      : isRecord(data) && Array.isArray(data.data)
        ? data.data
        : isRecord(data) && Array.isArray(data.nowPlaying)
          ? data.nowPlaying
          : []

  return list
    .filter(isRecord)
    .map((value) => {
      const code = getScalarStringValue(value, ['code', 'movieCode', 'movieCd'])
      const title = getStringValue(value, ['title', 'movieNm', 'name'])
      const backdrop = getImageSource(
        getStringValue(value, [
          'backdrop',
          'backdropUrl',
          'backdropPath',
          'backdrop_path',
          'poster',
          'posterPath',
          'poster_path',
        ]),
      )

      return { code, title, backdrop }
    })
    .filter((value) => value.code && value.title)
}

function HomePage() {
  const navigate = useNavigate()
  const searchBoxRef = useRef<HTMLDivElement | null>(null)

  const [authSession, setAuthSession] = useState<AuthSession | null>(() => getAuthSession())
  const [theme, setTheme] = useState<HomeTheme>(() => {
    if (typeof window === 'undefined') {
      return 'dark'
    }

    return window.localStorage.getItem(HOME_THEME_STORAGE_KEY) === 'light' ? 'light' : 'dark'
  })
  const [isLoggingOut, setIsLoggingOut] = useState(false)
  const [loginId, setLoginId] = useState('')
  const [password, setPassword] = useState('')
  const [loginMessage, setLoginMessage] = useState('')
  const [isLoginSubmitting, setIsLoginSubmitting] = useState(false)

  const [query, setQuery] = useState('')
  const [message, setMessage] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isSearchLoading, setIsSearchLoading] = useState(false)
  const [searchMovies, setSearchMovies] = useState<SearchMovie[]>([])
  const [isSearchResultsOpen, setIsSearchResultsOpen] = useState(false)
  const [activeSearchIndex, setActiveSearchIndex] = useState(-1)

  const [popularMovies, setPopularMovies] = useState<PopularMovie[]>([])
  const [bannerMovies, setBannerMovies] = useState<BannerMovie[]>([])
  const [nowPlayingMovies, setNowPlayingMovies] = useState<PopularMovie[]>([])
  const [upcomingMovies, setUpcomingMovies] = useState<PopularMovie[]>([])
  const [isPopularLoading, setIsPopularLoading] = useState(true)
  const [isBannerLoading, setIsBannerLoading] = useState(true)
  const [bannerPage, setBannerPage] = useState(0)
  const [bannerDirection, setBannerDirection] = useState<'next' | 'previous'>('next')
  const [isBannerSliding, setIsBannerSliding] = useState(false)
  const [popularPage, setPopularPage] = useState(0)
  const [nowPlayingPage, setNowPlayingPage] = useState(0)
  const [upcomingPage, setUpcomingPage] = useState(0)
  const bannerTransitionTimeoutRef = useRef<number | null>(null)

  useEffect(() => {
    function handleAuthSessionChange() {
      setAuthSession(getAuthSession())
    }

    return subscribeAuthSessionChange(handleAuthSessionChange)
  }, [])

  useEffect(() => {
    window.localStorage.setItem(HOME_THEME_STORAGE_KEY, theme)
  }, [theme])

  useEffect(() => {
    let isMounted = true

    async function loadMovieCollections() {
      setIsPopularLoading(true)

      try {
        const response = await request<MovieCollectionResponse>('/movies', {
          method: 'GET',
        })

        if (!isMounted) {
          return
        }

        setPopularMovies(normalizePopularMovies(response?.popular))
        setNowPlayingMovies(normalizePopularMovies(response?.nowPlaying))
        setUpcomingMovies(normalizePopularMovies(response?.upcoming))
        setPopularPage(0)
        setNowPlayingPage(0)
        setUpcomingPage(0)
      } catch {
        if (!isMounted) {
          return
        }

        setPopularMovies([])
        setNowPlayingMovies([])
        setUpcomingMovies([])
      } finally {
        if (isMounted) {
          setIsPopularLoading(false)
        }
      }
    }

    void loadMovieCollections()

    return () => {
      isMounted = false
    }
  }, [])

  useEffect(() => {
    let isMounted = true

    async function loadBannerMovies() {
      setIsBannerLoading(true)

      try {
        const response = await request<unknown>('/movies/banner', {
          method: 'GET',
        })

        if (!isMounted) {
          return
        }

        setBannerMovies(normalizeBannerMovies(response))
        setIsBannerSliding(false)
        setBannerPage(0)
      } catch {
        if (!isMounted) {
          return
        }

        setBannerMovies([])
        setIsBannerSliding(false)
        setBannerPage(0)
      } finally {
        if (isMounted) {
          setIsBannerLoading(false)
        }
      }
    }

    void loadBannerMovies()

    return () => {
      isMounted = false
    }
  }, [])

  useEffect(() => {
    const trimmedQuery = query.trim()

    if (!trimmedQuery) {
      return
    }

    let isMounted = true
    const debounceTimer = window.setTimeout(async () => {
      setIsSearchLoading(true)

      try {
        const response = await request<unknown>(
          `/movies/search?keyword=${encodeURIComponent(trimmedQuery)}`,
          { method: 'GET' },
        )

        if (!isMounted) {
          return
        }

        setSearchMovies(normalizeSearchMovies(response))
        setActiveSearchIndex(-1)
        setIsSearchResultsOpen(true)
      } catch {
        if (!isMounted) {
          return
        }

        setSearchMovies([])
        setActiveSearchIndex(-1)
      } finally {
        if (isMounted) {
          setIsSearchLoading(false)
        }
      }
    }, SEARCH_DEBOUNCE_MS)

    return () => {
      isMounted = false
      window.clearTimeout(debounceTimer)
    }
  }, [query])

  useEffect(() => {
    function handleDocumentMouseDown(event: MouseEvent) {
      if (!searchBoxRef.current?.contains(event.target as Node)) {
        setIsSearchResultsOpen(false)
        setActiveSearchIndex(-1)
      }
    }

    function handleDocumentKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') {
        setIsSearchResultsOpen(false)
        setActiveSearchIndex(-1)
      }
    }

    document.addEventListener('mousedown', handleDocumentMouseDown)
    document.addEventListener('keydown', handleDocumentKeyDown)

    return () => {
      document.removeEventListener('mousedown', handleDocumentMouseDown)
      document.removeEventListener('keydown', handleDocumentKeyDown)
    }
  }, [])

  function moveToMovieDetail(movie: SearchMovie | PopularMovie | BannerMovie) {
    setIsSearchResultsOpen(false)
    setActiveSearchIndex(-1)
    navigate(`/movies/${movie.code}`, {
      state: {
        movie: {
          id: movie.code,
          title: movie.title,
          poster: 'poster' in movie ? movie.poster : '',
        },
      },
    })
  }

  async function handleLogout() {
    if (isLoggingOut) {
      return
    }

    setIsLoggingOut(true)

    try {
      await logout()
    } finally {
      clearAuthSession()
      setIsLoggingOut(false)
    }
  }

  async function handleInlineLogin(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (isLoginSubmitting) {
      return
    }

    setIsLoginSubmitting(true)
    setLoginMessage('')

    try {
      const loginResponse = await login({ loginId, password })

      if (!loginResponse) {
        throw new Error('로그인 응답 데이터가 없습니다.')
      }

      saveAuthSession(loginResponse)
      setLoginId('')
      setPassword('')
      setLoginMessage('')
    } catch (error) {
      if (isApiError(error) && error.status === 401) {
        setLoginMessage(error.message)
      } else {
        setLoginMessage('아이디와 비밀번호를 다시 확인해주세요.')
      }
    } finally {
      setIsLoginSubmitting(false)
    }
  }

  async function handleSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const trimmedQuery = query.trim()
    setMessage('')

    if (!trimmedQuery) {
      setSearchMovies([])
      setActiveSearchIndex(-1)
      return
    }

    if (activeSearchIndex >= 0 && searchMovies[activeSearchIndex]) {
      moveToMovieDetail(searchMovies[activeSearchIndex])
      return
    }

    setIsSubmitting(true)
    setIsSearchLoading(true)

    try {
      const response = await request<unknown>(
        `/movies/search?keyword=${encodeURIComponent(trimmedQuery)}`,
        { method: 'GET' },
      )

      const normalizedMovies = normalizeSearchMovies(response)
      setSearchMovies(normalizedMovies)
      setActiveSearchIndex(-1)
      setIsSearchResultsOpen(true)

      if (normalizedMovies.length === 1) {
        moveToMovieDetail(normalizedMovies[0])
      }
    } catch {
      setSearchMovies([])
      setActiveSearchIndex(-1)
      setMessage('검색 요청에 실패했습니다.')
    } finally {
      setIsSubmitting(false)
      setIsSearchLoading(false)
    }
  }

  const popularPageCount = popularMovies.length
  const bannerPageCount = bannerMovies.length
  const visibleBannerMovie = bannerMovies[bannerPage] ?? null
  const previousBannerIndex =
    bannerPageCount > 0 ? (bannerPage - 1 + bannerPageCount) % bannerPageCount : -1
  const nextBannerIndex = bannerPageCount > 0 ? (bannerPage + 1) % bannerPageCount : -1
  const bannerSlides =
    bannerPageCount > 1
      ? [
          bannerMovies[previousBannerIndex],
          bannerMovies[bannerPage],
          bannerMovies[nextBannerIndex],
        ].filter((movie): movie is BannerMovie => Boolean(movie))
      : visibleBannerMovie
        ? [visibleBannerMovie]
        : []
  const visiblePopularMovie = popularMovies[popularPage] ?? null
  const visiblePopularCards = popularMovies
    .map((movie, index) => ({ movie, offset: index - popularPage }))
    .filter(({ offset }) => offset >= -2 && offset <= 2)
  const nowPlayingPageCount = nowPlayingMovies.length
  const visibleNowPlayingMovie = nowPlayingMovies[nowPlayingPage] ?? null
  const visibleNowPlayingCards = nowPlayingMovies
    .map((movie, index) => ({ movie, offset: index - nowPlayingPage }))
    .filter(({ offset }) => offset >= -2 && offset <= 2)
  const upcomingPageCount = upcomingMovies.length
  const visibleUpcomingMovie = upcomingMovies[upcomingPage] ?? null
  const visibleUpcomingCards = upcomingMovies
    .map((movie, index) => ({ movie, offset: index - upcomingPage }))
    .filter(({ offset }) => offset >= -2 && offset <= 2)

  function moveToPreviousPopularMovie() {
    setPopularPage((page) => Math.max(0, page - 1))
  }

  function moveToNextPopularMovie() {
    setPopularPage((page) => Math.min(popularPageCount - 1, page + 1))
  }

  const startBannerTransition = useCallback(
    (direction: 'next' | 'previous', allowWrap = false) => {
      if (bannerPageCount <= 1 || isBannerSliding) {
        return
      }

      if (direction === 'next' && bannerPage >= bannerPageCount - 1 && !allowWrap) {
        return
      }

      if (direction === 'previous' && bannerPage <= 0 && !allowWrap) {
        return
      }

      if (bannerTransitionTimeoutRef.current) {
        window.clearTimeout(bannerTransitionTimeoutRef.current)
        bannerTransitionTimeoutRef.current = null
      }

      setBannerDirection(direction)
      setIsBannerSliding(true)

      bannerTransitionTimeoutRef.current = window.setTimeout(() => {
        setBannerPage((currentPage) => {
          if (direction === 'next') {
            return allowWrap ? (currentPage + 1) % bannerPageCount : Math.min(bannerPageCount - 1, currentPage + 1)
          }

          return allowWrap
            ? (currentPage - 1 + bannerPageCount) % bannerPageCount
            : Math.max(0, currentPage - 1)
        })
        setIsBannerSliding(false)
        bannerTransitionTimeoutRef.current = null
      }, BANNER_TRANSITION_MS)
    },
    [bannerPage, bannerPageCount, isBannerSliding],
  )

  function moveToPreviousBannerMovie() {
    startBannerTransition('previous')
  }

  function moveToNextBannerMovie() {
    startBannerTransition('next')
  }

  function moveToPreviousNowPlayingMovie() {
    setNowPlayingPage((page) => Math.max(0, page - 1))
  }

  function moveToNextNowPlayingMovie() {
    setNowPlayingPage((page) => Math.min(nowPlayingPageCount - 1, page + 1))
  }

  function moveToPreviousUpcomingMovie() {
    setUpcomingPage((page) => Math.max(0, page - 1))
  }

  function moveToNextUpcomingMovie() {
    setUpcomingPage((page) => Math.min(upcomingPageCount - 1, page + 1))
  }

  useEffect(() => {
    if (isBannerLoading || bannerPageCount <= 1 || isBannerSliding) {
      return
    }

    const autoplayTimer = window.setInterval(() => {
      startBannerTransition('next', true)
    }, BANNER_AUTOPLAY_MS)

    return () => {
      window.clearInterval(autoplayTimer)
    }
  }, [bannerPageCount, isBannerLoading, isBannerSliding, startBannerTransition])

  useEffect(() => {
    return () => {
      if (bannerTransitionTimeoutRef.current) {
        window.clearTimeout(bannerTransitionTimeoutRef.current)
      }
    }
  }, [])

  useEffect(() => {
    return () => {
      if (bannerTransitionTimeoutRef.current) {
        window.clearTimeout(bannerTransitionTimeoutRef.current)
        bannerTransitionTimeoutRef.current = null
      }
    }
  }, [bannerPage])

  function renderBannerSlide(movie: BannerMovie, index: number) {
    return (
      <button
        key={`banner-slide-${movie.code}-${index}-${bannerPage}`}
        className="home-banner-card home-banner-slide"
        type="button"
        onClick={() => moveToMovieDetail(movie)}
        aria-label={`${movie.title} 상세 보기`}
      >
        <div className="home-banner-poster-shell">
          {movie.backdrop ? (
            <img className="home-banner-poster" src={movie.backdrop} alt={`${movie.title} 배너`} />
          ) : (
            <div className="home-banner-poster home-popular-poster-fallback">
              <span>{movie.title}</span>
            </div>
          )}
          <div className="home-banner-overlay" aria-hidden="true" />
          <div className="home-banner-copy">
            <h2>{movie.title}</h2>
          </div>
        </div>
      </button>
    )
  }

  function renderMovieSection({
    title,
    visibleMovie,
    visibleCards,
    page,
    pageCount,
    onPrevious,
    onNext,
  }: {
    title: string
    visibleMovie: PopularMovie | null
    visibleCards: Array<{ movie: PopularMovie; offset: number }>
    page: number
    pageCount: number
    onPrevious: () => void
    onNext: () => void
  }) {
    return (
      <section className="home-popular-section">
        <div className="home-popular-section-header">
          <h2>{title}</h2>
        </div>

        {isPopularLoading ? (
          <div className="home-popular-loading" aria-live="polite">
            <img
              className="home-popular-loading-icon"
              src={loadingIcon}
              alt=""
              aria-hidden="true"
            />
          </div>
        ) : visibleMovie ? (
          <div className="home-popular-carousel">
            <button
              className="home-popular-side-button"
              type="button"
              onClick={onPrevious}
              disabled={page === 0}
              aria-label={`이전 ${title}`}
            >
              <img className="home-popular-side-button-icon" src={previousIcon} alt="" aria-hidden="true" />
            </button>

            <div className="home-popular-track">
              {visibleCards.map(({ movie, offset }) => {
                const positionClass =
                  offset === 0
                    ? 'home-popular-card-current'
                    : offset === -1
                      ? 'home-popular-card-previous'
                      : offset === 1
                        ? 'home-popular-card-next'
                        : offset < 0
                          ? 'home-popular-card-off-left'
                          : 'home-popular-card-off-right'

                const isCurrent = offset === 0
                const posterShellClass = isCurrent
                  ? 'home-popular-poster-shell'
                  : 'home-popular-preview-poster-shell'
                const posterClass = isCurrent
                  ? 'home-popular-poster'
                  : 'home-popular-preview-poster'
                const titleClass = isCurrent ? 'home-popular-title' : 'home-popular-preview-title'

                return (
                  <button
                    className={`home-popular-card ${positionClass}`}
                    type="button"
                    key={`${title}-${movie.code}`}
                    onClick={() => moveToMovieDetail(movie)}
                    aria-label={`${movie.title} 상세 보기`}
                  >
                    <div className={posterShellClass}>
                      {movie.poster ? (
                        <img
                          className={posterClass}
                          src={movie.poster}
                          alt={isCurrent ? `${movie.title} 포스터` : ''}
                          aria-hidden={isCurrent ? undefined : 'true'}
                        />
                      ) : (
                        <div className={`${posterClass} home-popular-poster-fallback`}>
                          <span>{movie.title}</span>
                        </div>
                      )}
                    </div>
                    <p className={titleClass}>{movie.title}</p>
                  </button>
                )
              })}
            </div>

            <button
              className="home-popular-side-button"
              type="button"
              onClick={onNext}
              disabled={page >= pageCount - 1}
              aria-label={`다음 ${title}`}
            >
              <img className="home-popular-side-button-icon" src={nextIcon} alt="" aria-hidden="true" />
            </button>
          </div>
        ) : (
          <p className="home-popular-empty">데이터를 불러오지 못하였습니다.</p>
        )}
      </section>
    )
  }

  return (
    <div className={`home-page home-page-${theme}`}>
      <div className="home-desktop-container">
        <main className="home-main-shell">
          <section className="home-search-section">
            <div className="search-box-shell" ref={searchBoxRef}>
              <form className="home-search-form" onSubmit={handleSearch}>
                <label className="sr-only" htmlFor="movie-search">
                  영화 검색
                </label>
                <input
                  id="movie-search"
                  className="home-search-input"
                  type="search"
                  placeholder="영화 제목을 입력해보세요"
                  value={query}
                  onChange={(event) => {
                    const nextQuery = event.target.value
                    setQuery(nextQuery)

                    if (!nextQuery.trim()) {
                      setSearchMovies([])
                      setIsSearchLoading(false)
                      setIsSearchResultsOpen(false)
                      setActiveSearchIndex(-1)
                      return
                    }

                    setActiveSearchIndex(-1)
                    setIsSearchResultsOpen(true)
                  }}
                  onFocus={() => {
                    if (query.trim()) {
                      setIsSearchResultsOpen(true)
                    }
                  }}
                  onKeyDown={(event) => {
                    if (!query.trim() || searchMovies.length === 0) {
                      return
                    }

                    if (event.key === 'ArrowDown') {
                      event.preventDefault()
                      setIsSearchResultsOpen(true)
                      setActiveSearchIndex((previousIndex) =>
                        previousIndex < searchMovies.length - 1 ? previousIndex + 1 : previousIndex,
                      )
                      return
                    }

                    if (event.key === 'ArrowUp') {
                      event.preventDefault()
                      setIsSearchResultsOpen(true)
                      setActiveSearchIndex((previousIndex) => {
                        if (previousIndex === -1) {
                          return searchMovies.length - 1
                        }

                        return previousIndex > 0 ? previousIndex - 1 : 0
                      })
                      return
                    }

                    if (
                      event.key === 'Enter' &&
                      activeSearchIndex >= 0 &&
                      searchMovies[activeSearchIndex]
                    ) {
                      event.preventDefault()
                      moveToMovieDetail(searchMovies[activeSearchIndex])
                      return
                    }

                    if (event.key === 'Escape') {
                      setIsSearchResultsOpen(false)
                      setActiveSearchIndex(-1)
                    }
                  }}
                />
                <button className="home-search-button" type="submit" disabled={isSubmitting}>
                  <span className="sr-only">{isSubmitting ? '검색 중' : '검색'}</span>
                  <img className="home-search-button-icon" src={searchIcon} alt="" aria-hidden="true" />
                </button>
              </form>

              <HomeSearchResults
                query={query}
                movies={searchMovies}
                isLoading={isSearchLoading}
                isOpen={isSearchResultsOpen}
                activeIndex={activeSearchIndex}
              />
            </div>

            {message ? <p className="home-search-message">{message}</p> : null}
          </section>

          <section className="home-banner-section" aria-label="현재 상영중인 영화 배너">
            {isBannerLoading ? (
              <div className="home-popular-loading" aria-live="polite">
                <img
                  className="home-popular-loading-icon"
                  src={loadingIcon}
                  alt=""
                  aria-hidden="true"
                />
              </div>
            ) : visibleBannerMovie ? (
              <div className="home-banner-carousel">
                <button
                  className="home-banner-side-button home-banner-side-button-previous"
                  type="button"
                  onClick={moveToPreviousBannerMovie}
                  disabled={bannerPage === 0}
                  aria-label="이전 현재 상영중인 영화"
                >
                  <img className="home-popular-side-button-icon" src={previousIcon} alt="" aria-hidden="true" />
                </button>

                <div className="home-banner-stage">
                  <div
                    className={`home-banner-track${
                      isBannerSliding ? ` home-banner-track-sliding-${bannerDirection}` : ''
                    }`}
                  >
                    {bannerSlides.map((movie, index) => renderBannerSlide(movie, index))}
                  </div>
                </div>

                <button
                  className="home-banner-side-button home-banner-side-button-next"
                  type="button"
                  onClick={moveToNextBannerMovie}
                  disabled={bannerPage >= bannerPageCount - 1}
                  aria-label="다음 현재 상영중인 영화"
                >
                  <img className="home-popular-side-button-icon" src={nextIcon} alt="" aria-hidden="true" />
                </button>

                {bannerPageCount > 1 ? (
                  <div className="home-banner-indicators" aria-label="배너 위치">
                    {bannerMovies.map((movie, index) => (
                      <button
                        key={`banner-indicator-${movie.code}`}
                        className={`home-banner-indicator${
                          index === bannerPage ? ' home-banner-indicator-active' : ''
                        }`}
                        type="button"
                        onClick={() => {
                          if (isBannerSliding || index === bannerPage) {
                            return
                          }

                          if (index === bannerPage + 1 || (bannerPage === bannerPageCount - 1 && index === 0)) {
                            startBannerTransition('next', index === 0)
                            return
                          }

                          if (index === bannerPage - 1 || (bannerPage === 0 && index === bannerPageCount - 1)) {
                            startBannerTransition('previous', index === bannerPageCount - 1)
                            return
                          }

                          setBannerPage(index)
                        }}
                        aria-label={`${movie.title} 배너로 이동`}
                        aria-pressed={index === bannerPage}
                      />
                    ))}
                  </div>
                ) : null}
              </div>
            ) : (
              <p className="home-popular-empty">데이터를 불러오지 못하였습니다.</p>
            )}
          </section>

          {renderMovieSection({
            title: '인기 영화',
            visibleMovie: visiblePopularMovie,
            visibleCards: visiblePopularCards,
            page: popularPage,
            pageCount: popularPageCount,
            onPrevious: moveToPreviousPopularMovie,
            onNext: moveToNextPopularMovie,
          })}

          {renderMovieSection({
            title: '현재 상영중인 영화',
            visibleMovie: visibleNowPlayingMovie,
            visibleCards: visibleNowPlayingCards,
            page: nowPlayingPage,
            pageCount: nowPlayingPageCount,
            onPrevious: moveToPreviousNowPlayingMovie,
            onNext: moveToNextNowPlayingMovie,
          })}

          {renderMovieSection({
            title: '개봉 예정 영화',
            visibleMovie: visibleUpcomingMovie,
            visibleCards: visibleUpcomingCards,
            page: upcomingPage,
            pageCount: upcomingPageCount,
            onPrevious: moveToPreviousUpcomingMovie,
            onNext: moveToNextUpcomingMovie,
          })}
        </main>
      </div>

      <aside className="home-sidebar" aria-label="메인 내비게이션">
        <div className="home-brand-block">
          <p className="home-brand-mark">MP</p>
          <div className="home-brand-copy">
            <strong>Movie Pedia</strong>
            <span>당신의 영화 취향</span>
          </div>
        </div>

        <nav className="home-nav">
          {PRIMARY_NAV_ITEMS.map((item, index) => (
            <button
              className={`home-nav-item${index === 0 ? ' home-nav-item-active' : ''}`}
              type="button"
              key={item}
            >
              <span>{item}</span>
            </button>
          ))}
        </nav>
      </aside>

      <aside className="home-auth-panel">
        <div className="home-auth-card">
          {authSession ? (
            <div className="home-auth-session">
              <p className="home-auth-session-nickname">{authSession.nickname ?? '사용자'}</p>
              <button
                className="home-auth-logout-button"
                type="button"
                onClick={handleLogout}
                disabled={isLoggingOut}
              >
                {isLoggingOut ? '로그아웃 중..' : '로그아웃'}
              </button>
            </div>
          ) : (
            <form className="home-inline-login" onSubmit={handleInlineLogin} noValidate>
              <input
                className="home-inline-login-input"
                type="text"
                placeholder="아이디"
                autoComplete="username"
                value={loginId}
                onChange={(event) => setLoginId(event.target.value)}
              />
              <input
                className="home-inline-login-input"
                type="password"
                placeholder="비밀번호"
                autoComplete="current-password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
              />
              {loginMessage ? <p className="home-inline-login-message">{loginMessage}</p> : null}
              <div className="home-inline-login-actions">
                <button className="home-signup-button" type="button" onClick={() => navigate('/signup')}>
                  회원가입
                </button>
                <button className="home-auth-button" type="submit" disabled={isLoginSubmitting}>
                  {isLoginSubmitting ? '로그인 중..' : '로그인'}
                </button>
              </div>
              <div className="home-social-login-divider" aria-hidden="true" />
              <a className="home-social-login-button" href="http://localhost:8080/oauth2/authorization/naver">
                <img
                  className="home-social-login-button-image"
                  src={naverLoginButtonImage}
                  alt="네이버로 로그인"
                />
              </a>
            </form>
          )}
        </div>
      </aside>

      <button
        className="home-theme-toggle"
        type="button"
        onClick={() => setTheme((currentTheme) => (currentTheme === 'dark' ? 'light' : 'dark'))}
        aria-label={theme === 'dark' ? '라이트 모드로 전환' : '다크 모드로 전환'}
        title={theme === 'dark' ? '라이트 모드' : '다크 모드'}
      >
        <img
          className="home-theme-toggle-icon"
          src={theme === 'dark' ? lightModeIcon : darkModeIcon}
          alt=""
          aria-hidden="true"
        />
      </button>
    </div>
  )
}

export default HomePage

