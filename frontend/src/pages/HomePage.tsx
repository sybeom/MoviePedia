import { useCallback, useEffect, useRef, useState, type FormEvent } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import darkModeIcon from '../assets/icons/dark_mode.svg'
import lightModeIcon from '../assets/icons/light_mode.svg'
import loadingIcon from '../assets/icons/loading.svg'
import naverLoginButtonImage from '../assets/icons/NAVER_login.png'
import nextIcon from '../assets/icons/next.svg'
import previousIcon from '../assets/icons/previous.svg'
import rating12Icon from '../assets/ratings/12.svg'
import rating15Icon from '../assets/ratings/15.svg'
import rating19Icon from '../assets/ratings/19.svg'
import ratingAllIcon from '../assets/ratings/all.svg'
import searchIcon from '../assets/icons/search.svg'
import { login, logout } from '../api/auth'
import { isApiError, request } from '../api/client'
import HomeSearchResults from '../components/home/HomeSearchResults'
import { getMediaConfigByPath } from '../config/media'
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
  seasonNum: string
  poster: string
  genres: string[]
  certification: string
}

type BannerMovie = {
  code: string
  title: string
  backdrop: string
}

type MovieListPage = {
  movies: PopularMovie[]
  hasMore: boolean
}

type HomeTheme = 'dark' | 'light'
type MovieSortFilter = '최신순' | '오래된순'
type MovieReleaseFilter = '전체' | '개봉' | '미개봉'
type GenreOption = {
  label: string
  value: string
}

const SEARCH_DEBOUNCE_MS = 500
const HOME_THEME_STORAGE_KEY = 'moviepedia.home.theme'
const PRIMARY_NAV_ITEMS = ['영화', 'TV 시리즈']
const BANNER_AUTOPLAY_MS = 5000
const BANNER_TRANSITION_MS = 520
const TMDB_IMAGE_BASE_URL = 'https://image.tmdb.org/t/p/original'
const HOME_SORT_FILTERS: MovieSortFilter[] = ['최신순', '오래된순']
const HOME_RELEASE_FILTERS: MovieReleaseFilter[] = ['전체', '개봉', '미개봉']
const HOME_CERTIFICATION_ICON_MAP: Record<string, string> = {
  '12': rating12Icon,
  '15': rating15Icon,
  '19': rating19Icon,
  ALL: ratingAllIcon,
}

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

function getCertificationIcon(certification: string) {
  return HOME_CERTIFICATION_ICON_MAP[certification.toUpperCase()] ?? ''
}

function getStringArrayValue(record: Record<string, unknown>, keys: string[]) {
  for (const key of keys) {
    const value = record[key]

    if (Array.isArray(value)) {
      const nextValue = value
        .map((item) => {
          if (typeof item === 'string' && item.trim()) {
            return item.trim()
          }

          if (isRecord(item)) {
            return getStringValue(item, ['name', 'genre'])
          }

          return ''
        })
        .filter(Boolean)

      if (nextValue.length > 0) {
        return nextValue
      }
    }

    if (typeof value === 'string' && value.trim()) {
      const nextValue = value
        .split(/[,/|]/)
        .map((item) => item.trim())
        .filter(Boolean)

      if (nextValue.length > 0) {
        return nextValue
      }
    }
  }

  return []
}

function getMovieListValue(data: unknown) {
  if (Array.isArray(data)) {
    return data
  }

  if (!isRecord(data)) {
    return []
  }

  const candidateKeys = ['movies', 'content', 'items', 'list', 'data']

  for (const key of candidateKeys) {
    const value = data[key]

    if (Array.isArray(value)) {
      return value
    }
  }

  const nestedKeys = ['data', 'result', 'payload', 'page']

  for (const key of nestedKeys) {
    const value = data[key]

    if (!isRecord(value)) {
      continue
    }

    for (const candidateKey of candidateKeys) {
      const nestedValue = value[candidateKey]

      if (Array.isArray(nestedValue)) {
        return nestedValue
      }
    }
  }

  return []
}

function normalizeMovieListPage(data: unknown): MovieListPage {
  const movies = normalizePopularMovies(getMovieListValue(data))

  if (isRecord(data)) {
    const hasNextKeys = ['hasNext', 'hasMore', 'next', 'isNext']

    for (const key of hasNextKeys) {
      if (typeof data[key] === 'boolean') {
        return { movies, hasMore: data[key] as boolean }
      }
    }

    const lastKeys = ['last', 'isLast']

    for (const key of lastKeys) {
      if (typeof data[key] === 'boolean') {
        return { movies, hasMore: !(data[key] as boolean) }
      }
    }

    const pageValue = data.page

    if (isRecord(pageValue)) {
      if (typeof pageValue.hasNext === 'boolean') {
        return { movies, hasMore: pageValue.hasNext }
      }

      if (typeof pageValue.last === 'boolean') {
        return { movies, hasMore: !pageValue.last }
      }

      const currentPage = Number(pageValue.number)
      const totalPages = Number(pageValue.totalPages)

      if (Number.isFinite(currentPage) && Number.isFinite(totalPages) && totalPages > 0) {
        return {
          movies,
          hasMore: currentPage + 1 < totalPages,
        }
      }
    }
  }

  return {
    movies,
    hasMore: movies.length > 0,
  }
}

function getGenreQueryValues(filters: string[]) {
  return filters.filter((filter) => filter !== 'ALL')
}

function getSortQueryValue(filter: MovieSortFilter) {
  return filter === '오래된순' ? 'OLDEST' : 'LATEST'
}

function getReleaseQueryValue(filter: MovieReleaseFilter) {
  if (filter === '개봉') {
    return 'RELEASED'
  }

  if (filter === '미개봉') {
    return 'UNRELEASED'
  }

  return ''
}

function getReleaseFilterLabel(mediaType: 'movie' | 'series', filter: MovieReleaseFilter) {
  if (mediaType === 'series') {
    if (filter === '개봉') {
      return '공개'
    }

    if (filter === '미개봉') {
      return '미공개'
    }
  }

  return filter
}

function normalizeGenreOptions(data: unknown): GenreOption[] {
  const source = Array.isArray(data)
    ? data
    : isRecord(data) && Array.isArray(data.data)
      ? data.data
      : isRecord(data) && isRecord(data.data) && Array.isArray(data.data.genres)
        ? data.data.genres
        : isRecord(data) && Array.isArray(data.genres)
          ? data.genres
        : []

  const normalizedOptions = source
    .map((item) => {
      if (!isRecord(item)) {
        return null
      }

      const label = getStringValue(item, ['label', 'name', 'text'])
      const value = getScalarStringValue(item, ['genreCode', 'genreId', 'value', 'code', 'id'])

      if (!label || !value) {
        return null
      }

      return { label, value }
    })
    .filter((option): option is GenreOption => option !== null)

  return [{ label: '전체', value: 'ALL' }, ...normalizedOptions.filter((option) => option.value !== 'ALL')]
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
      const code = getScalarStringValue(value, [
        'code',
        'movieCode',
        'movieCd',
        'tvcode',
        'tvCode',
        'seriesCode',
      ])
      const title = getStringValue(value, ['title', 'movieNm', 'name'])
      const seasonNum = getScalarStringValue(value, ['seasonNum', 'season', 'seasonNumber'])
      const poster = getImageSource(getStringValue(value, ['poster', 'posterPath', 'poster_path']))
      const genres = getStringArrayValue(value, ['genres', 'genre'])
      const certification = getStringValue(value, ['certification', 'rating', 'ageRating'])

      return { code, title, seasonNum, poster, genres, certification }
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
      const code = getScalarStringValue(value, [
        'code',
        'movieCode',
        'movieCd',
        'tvcode',
        'tvCode',
        'seriesCode',
      ])
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
  const location = useLocation()
  const mediaConfig = getMediaConfigByPath(location.pathname)
  const searchBoxRef = useRef<HTMLDivElement | null>(null)
  const mainShellRef = useRef<HTMLElement | null>(null)
  const allMoviesLoadTriggerRef = useRef<HTMLDivElement | null>(null)
  const allMoviesSectionRef = useRef<HTMLElement | null>(null)

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

  const [genreOptions, setGenreOptions] = useState<GenreOption[]>([{ label: '전체', value: 'ALL' }])
  const [bannerMovies, setBannerMovies] = useState<BannerMovie[]>([])
  const [isBannerLoading, setIsBannerLoading] = useState(true)
  const [isGenresLoading, setIsGenresLoading] = useState(true)
  const [bannerPage, setBannerPage] = useState(0)
  const [bannerDirection, setBannerDirection] = useState<'next' | 'previous'>('next')
  const [isBannerSliding, setIsBannerSliding] = useState(false)
  const [selectedGenreFilters, setSelectedGenreFilters] = useState<string[]>(['ALL'])
  const [selectedSortFilter, setSelectedSortFilter] = useState<MovieSortFilter>('최신순')
  const [selectedReleaseFilter, setSelectedReleaseFilter] = useState<MovieReleaseFilter>('전체')
  const [isExpandedMovieView, setIsExpandedMovieView] = useState(false)
  const [allMovies, setAllMovies] = useState<PopularMovie[]>([])
  const [allMoviesPage, setAllMoviesPage] = useState(0)
  const [hasMoreAllMovies, setHasMoreAllMovies] = useState(true)
  const [isAllMoviesLoading, setIsAllMoviesLoading] = useState(true)
  const [isLoadingMoreAllMovies, setIsLoadingMoreAllMovies] = useState(false)
  const bannerTransitionTimeoutRef = useRef<number | null>(null)
  const allMoviesPageRef = useRef(0)
  const hasMoreAllMoviesRef = useRef(true)
  const isLoadingMoreAllMoviesRef = useRef(false)

  function toggleGenreFilter(genreValue: string) {
    setSelectedGenreFilters((previousFilters) => {
      if (genreValue === 'ALL') {
        return ['ALL']
      }

      const nextFilters = previousFilters.filter((filter) => filter !== 'ALL')

      if (nextFilters.includes(genreValue)) {
        const removedFilters = nextFilters.filter((filter) => filter !== genreValue)
        return removedFilters.length > 0 ? removedFilters : ['ALL']
      }

      return [...nextFilters, genreValue]
    })
  }

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
    allMoviesPageRef.current = allMoviesPage
  }, [allMoviesPage])

  useEffect(() => {
    hasMoreAllMoviesRef.current = hasMoreAllMovies
  }, [hasMoreAllMovies])

  useEffect(() => {
    isLoadingMoreAllMoviesRef.current = isLoadingMoreAllMovies
  }, [isLoadingMoreAllMovies])

  useEffect(() => {
    if (!isExpandedMovieView || !mainShellRef.current || !allMoviesSectionRef.current) {
      return
    }

    mainShellRef.current.scrollTo({
      top: Math.max(0, allMoviesSectionRef.current.offsetTop - 12),
      behavior: 'smooth',
    })
  }, [isExpandedMovieView])

  useEffect(() => {
    let isMounted = true

    async function loadBannerMovies() {
      setIsBannerLoading(true)

      try {
        const response =
          mediaConfig.type === 'series'
            ? await request<unknown>('/tv/banners', {
                method: 'GET',
              })
            : await request<unknown>(`${mediaConfig.resourcePath}/banners`, {
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
  }, [mediaConfig.resourcePath, mediaConfig.type])

  useEffect(() => {
    let isMounted = true

    async function loadGenreOptions() {
      setIsGenresLoading(true)

      try {
        const searchParams = new URLSearchParams({
          mediaType: mediaConfig.type === 'series' ? 'TV' : 'MOVIE',
        })
        const response = await request<unknown>(
          mediaConfig.type === 'series'
            ? `/tv/genres?${searchParams.toString()}`
            : `${mediaConfig.resourcePath}/genres?${searchParams.toString()}`,
          {
            method: 'GET',
          },
        )

        if (!isMounted) {
          return
        }

        setGenreOptions(normalizeGenreOptions(response))
      } catch {
        if (!isMounted) {
          return
        }

        setGenreOptions([{ label: '전체', value: 'ALL' }])
      } finally {
        if (isMounted) {
          setIsGenresLoading(false)
        }
      }
    }

    void loadGenreOptions()

    return () => {
      isMounted = false
    }
  }, [mediaConfig.resourcePath, mediaConfig.type])

  const loadAllMoviesPage = useCallback(
    async (
      page: number,
      append: boolean,
      genreFilters: string[],
      sortFilter: MovieSortFilter,
      releaseFilter: MovieReleaseFilter,
    ) => {
      const searchParams = new URLSearchParams({
        page: String(page),
        size: '10',
        sort: getSortQueryValue(sortFilter),
      })

      const genreValues = getGenreQueryValues(genreFilters)

      genreValues.forEach((genreValue) => {
        searchParams.append('genre', genreValue)
      })

      const releaseValue = getReleaseQueryValue(releaseFilter)

      if (releaseValue) {
        searchParams.set('releaseStatus', releaseValue)
      }

      const response = await request<unknown>(
        `${mediaConfig.resourcePath}?${searchParams.toString()}`,
        {
        method: 'GET',
        },
      )
      const normalizedPage = normalizeMovieListPage(response)

      setAllMovies((previousMovies) =>
        append ? [...previousMovies, ...normalizedPage.movies] : normalizedPage.movies,
      )
      setAllMoviesPage(page)
      setHasMoreAllMovies(normalizedPage.hasMore)

      allMoviesPageRef.current = page
      hasMoreAllMoviesRef.current = normalizedPage.hasMore
    },
    [mediaConfig.resourcePath],
  )

  useEffect(() => {
    let isMounted = true

    async function loadInitialAllMovies() {
      setIsAllMoviesLoading(true)
      setAllMovies([])
      setAllMoviesPage(0)
      setHasMoreAllMovies(true)

      allMoviesPageRef.current = 0
      hasMoreAllMoviesRef.current = true

      try {
        await loadAllMoviesPage(
          0,
          false,
          selectedGenreFilters,
          selectedSortFilter,
          selectedReleaseFilter,
        )
      } catch {
        if (!isMounted) {
          return
        }

        setAllMovies([])
        setHasMoreAllMovies(false)
      } finally {
        if (isMounted) {
          setIsAllMoviesLoading(false)
        }
      }
    }

    void loadInitialAllMovies()

    return () => {
      isMounted = false
    }
  }, [loadAllMoviesPage, selectedGenreFilters, selectedReleaseFilter, selectedSortFilter])

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
          `${mediaConfig.resourcePath}/search?keyword=${encodeURIComponent(trimmedQuery)}`,
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
  }, [mediaConfig.resourcePath, query])

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
    navigate(mediaConfig.detailPath(movie.code), {
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
        `${mediaConfig.resourcePath}/search?keyword=${encodeURIComponent(trimmedQuery)}`,
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

  useEffect(() => {
    const triggerElement = allMoviesLoadTriggerRef.current

    if (!triggerElement) {
      return
    }

    const mainShellElement = mainShellRef.current
    const observerRoot =
      mainShellElement && mainShellElement.scrollHeight > mainShellElement.clientHeight + 4
        ? mainShellElement
        : null

    const observer = new IntersectionObserver(
      (entries) => {
        const firstEntry = entries[0]

        if (!firstEntry?.isIntersecting) {
          return
        }

        if (isAllMoviesLoading || isLoadingMoreAllMoviesRef.current || !hasMoreAllMoviesRef.current) {
          return
        }

        setIsLoadingMoreAllMovies(true)
        isLoadingMoreAllMoviesRef.current = true

        void loadAllMoviesPage(
          allMoviesPageRef.current + 1,
          true,
          selectedGenreFilters,
          selectedSortFilter,
          selectedReleaseFilter,
        )
          .catch(() => {
            setHasMoreAllMovies(false)
            hasMoreAllMoviesRef.current = false
          })
          .finally(() => {
            setIsLoadingMoreAllMovies(false)
            isLoadingMoreAllMoviesRef.current = false
          })
      },
      {
        root: observerRoot,
        rootMargin: '0px 0px 240px 0px',
        threshold: 0.01,
      },
    )

    observer.observe(triggerElement)

    return () => {
      observer.disconnect()
    }
  }, [
    isAllMoviesLoading,
    loadAllMoviesPage,
    selectedGenreFilters,
    selectedReleaseFilter,
    selectedSortFilter,
  ])

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

  return (
    <div
      className={`home-page home-page-${theme}${isExpandedMovieView ? ' home-page-expanded' : ''}`}
    >
      <div
        className={`home-desktop-container${
          isExpandedMovieView ? ' home-desktop-container-expanded' : ''
        }`}
      >
        <main
          className={`home-main-shell${isExpandedMovieView ? ' home-main-shell-expanded' : ''}`}
          ref={mainShellRef}
        >
          {!isExpandedMovieView ? (
            <section className="home-search-section">
            <div className="search-box-shell" ref={searchBoxRef}>
              <form className="home-search-form" onSubmit={handleSearch}>
                <label className="sr-only" htmlFor="movie-search">
                  {mediaConfig.searchLabel}
                </label>
                <input
                  id="movie-search"
                  className="home-search-input"
                  type="search"
                  placeholder={mediaConfig.searchPlaceholder}
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
                ariaLabel={`${mediaConfig.navLabel} 검색 목록`}
                buildDetailPath={mediaConfig.detailPath}
              />
            </div>

            {message ? <p className="home-search-message">{message}</p> : null}
            </section>
          ) : null}

          {!isExpandedMovieView ? (
            <section className="home-banner-section" aria-label={mediaConfig.bannerAriaLabel}>
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
                  aria-label={`이전 ${mediaConfig.currentItemsTitle}`}
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
                  aria-label={`다음 ${mediaConfig.currentItemsTitle}`}
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
          ) : null}

          {isExpandedMovieView ? (
            <section className="home-expanded-view-hero" aria-labelledby="home-expanded-view-title">
              <h1 id="home-expanded-view-title">{mediaConfig.allItemsTitle}</h1>
              <button
                className="home-expand-view-button home-expand-view-button-hero"
                type="button"
                onClick={() => setIsExpandedMovieView(false)}
              >
                기본 보기
              </button>
            </section>
          ) : null}

          <section
            className={`home-movie-grid-section${
              isExpandedMovieView ? ' home-movie-grid-section-expanded' : ''
            }`}
            aria-labelledby="home-all-movies-title"
            ref={allMoviesSectionRef}
          >
            {isExpandedMovieView ? null : (
              <div className="home-popular-section-header">
                <h2 id="home-all-movies-title">{mediaConfig.allItemsTitle}</h2>
                <button
                  className="home-expand-view-button"
                  type="button"
                  onClick={() => navigate(mediaConfig.browsePath)}
                  aria-pressed="false"
                >
                  펼쳐보기
                </button>
              </div>
            )}

            <div className={`home-filter-panel${isExpandedMovieView ? ' home-filter-panel-expanded' : ''}`}>
              <div className="home-filter-group">
                <span className="home-filter-group-label">장르</span>
                <div className="home-genre-filter-row" role="tablist" aria-label="장르 필터">
                  {genreOptions.map((genre) => (
                    <button
                      key={genre.value}
                      className={`home-genre-filter-button${
                        selectedGenreFilters.includes(genre.value)
                          ? ' home-genre-filter-button-active'
                          : ''
                      }`}
                      type="button"
                      onClick={() => toggleGenreFilter(genre.value)}
                      aria-pressed={selectedGenreFilters.includes(genre.value)}
                    >
                      {genre.label}
                    </button>
                  ))}
                </div>
              </div>

              <div className="home-filter-group">
                <span className="home-filter-group-label">
                  {mediaConfig.type === 'series' ? '공개 여부' : '개봉 여부'}
                </span>
                <div className="home-genre-filter-row" role="tablist" aria-label="개봉 상태 필터">
                  {HOME_RELEASE_FILTERS.map((filter) => (
                    <button
                      key={filter}
                      className={`home-genre-filter-button${
                        filter === selectedReleaseFilter ? ' home-genre-filter-button-active' : ''
                      }`}
                      type="button"
                      onClick={() => setSelectedReleaseFilter(filter)}
                      aria-pressed={filter === selectedReleaseFilter}
                    >
                      {getReleaseFilterLabel(mediaConfig.type, filter)}
                    </button>
                  ))}
                </div>
              </div>

              <div className="home-filter-group">
                <span className="home-filter-group-label">정렬</span>
                <div className="home-genre-filter-row" role="tablist" aria-label="정렬 필터">
                  {HOME_SORT_FILTERS.map((sort) => (
                    <button
                      key={sort}
                      className={`home-genre-filter-button${
                        sort === selectedSortFilter ? ' home-genre-filter-button-active' : ''
                      }`}
                      type="button"
                      onClick={() => setSelectedSortFilter(sort)}
                      aria-pressed={sort === selectedSortFilter}
                    >
                      {sort}
                    </button>
                  ))}
                </div>
              </div>
            </div>

            {isAllMoviesLoading || isGenresLoading ? (
              <div className="home-popular-loading" aria-live="polite">
                <img
                  className="home-popular-loading-icon"
                  src={loadingIcon}
                  alt=""
                  aria-hidden="true"
                />
              </div>
            ) : allMovies.length > 0 ? (
              <div className={`home-movie-grid${isExpandedMovieView ? ' home-movie-grid-expanded' : ''}`}>
                {allMovies.map((movie) => (
                  <button
                    key={`all-movie-${movie.code}`}
                    className="home-movie-grid-card"
                    type="button"
                    onClick={() => moveToMovieDetail(movie)}
                    aria-label={`${movie.title} 상세 보기`}
                  >
                    <div className="home-movie-grid-poster-shell">
                      {movie.poster ? (
                        <img
                          className="home-movie-grid-poster"
                          src={movie.poster}
                          alt={`${movie.title} 포스터`}
                        />
                      ) : (
                        <div className="home-movie-grid-poster home-popular-poster-fallback">
                          <span>{movie.title}</span>
                        </div>
                      )}
                    </div>
                    <div className="home-movie-grid-title-row">
                      <p className="home-movie-grid-title">
                        {movie.title}
                        {mediaConfig.type === 'series' && movie.seasonNum ? ` 시즌 ${movie.seasonNum}` : ''}
                      </p>
                      {movie.certification ? (
                        <div className="home-movie-grid-certification">
                          {getCertificationIcon(movie.certification) ? (
                            <img
                              className="home-movie-grid-certification-icon"
                              src={getCertificationIcon(movie.certification)}
                              alt={`${movie.certification} 관람등급`}
                            />
                          ) : (
                            <span className="home-movie-grid-certification-text">
                              {movie.certification}
                            </span>
                          )}
                        </div>
                      ) : null}
                    </div>
                    {movie.genres.length > 0 ? (
                      <p className="home-movie-grid-genres">{movie.genres.join(' / ')}</p>
                    ) : null}
                  </button>
                ))}
              </div>
            ) : (
              <p className="home-popular-empty">데이터를 불러오지 못하였습니다.</p>
            )}

            {isLoadingMoreAllMovies ? (
              <div className="home-movie-grid-loading-more" aria-live="polite">
                <img
                  className="home-popular-loading-icon"
                  src={loadingIcon}
                  alt=""
                  aria-hidden="true"
                />
              </div>
            ) : null}

            <div className="home-movie-grid-load-trigger" ref={allMoviesLoadTriggerRef} aria-hidden="true" />
          </section>
        </main>
      </div>

      {!isExpandedMovieView ? <aside className="home-sidebar" aria-label="메인 내비게이션">
        <div className="home-brand-block">
          <p className="home-brand-mark">MP</p>
          <div className="home-brand-copy">
            <strong>Movie Pedia</strong>
            <span>당신의 영화 취향</span>
          </div>
        </div>

        <nav className="home-nav">
          {PRIMARY_NAV_ITEMS.map((item) => (
            <button
              className={`home-nav-item${
                item === mediaConfig.navLabel ? ' home-nav-item-active' : ''
              }`}
              type="button"
              key={item}
              onClick={() => navigate(item === 'TV 시리즈' ? '/series' : '/')}
            >
              <span>{item}</span>
            </button>
          ))}
        </nav>
      </aside> : null}

      {!isExpandedMovieView ? <aside className="home-auth-panel">
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
      </aside> : null}

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

