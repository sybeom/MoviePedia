import { useCallback, useEffect, useRef, useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import darkModeIcon from '../assets/icons/dark_mode.svg'
import lightModeIcon from '../assets/icons/light_mode.svg'
import loadingIcon from '../assets/icons/loading.svg'
import arrowBackIcon from '../assets/icons/arrow_back.svg'
import rating12Icon from '../assets/ratings/12.svg'
import rating15Icon from '../assets/ratings/15.svg'
import rating19Icon from '../assets/ratings/19.svg'
import ratingAllIcon from '../assets/ratings/all.svg'
import searchIcon from '../assets/icons/search.svg'
import { request } from '../api/client'
import HomeSearchResults from '../components/home/HomeSearchResults'
import './MovieBrowsePage.css'

type BrowseTheme = 'dark' | 'light'
type MovieSortFilter = '최신순' | '오래된순'
type MovieReleaseFilter = '전체' | '개봉' | '미개봉'

type GenreOption = {
  label: string
  value: string
}

type SearchMovie = {
  code: string
  title: string
}

type MovieCard = {
  code: string
  title: string
  poster: string
  genres: string[]
  certification: string
}

type MovieListPage = {
  movies: MovieCard[]
  hasMore: boolean
}

const HOME_THEME_STORAGE_KEY = 'moviepedia.home.theme'
const TMDB_IMAGE_BASE_URL = 'https://image.tmdb.org/t/p/original'
const MOVIE_BROWSE_PAGE_SIZE = 30
const SEARCH_DEBOUNCE_MS = 500
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

function normalizePopularMovies(data: unknown): MovieCard[] {
  const source = getMovieListValue(data)

  return source
    .map((item) => {
      if (!isRecord(item)) {
        return null
      }

      const code = getScalarStringValue(item, ['code', 'mvCode', 'movieCode', 'id'])
      const title = getStringValue(item, ['title', 'name'])
      const poster = getImageSource(
        getStringValue(item, ['poster', 'posterPath', 'poster_path', 'image']),
      )
      const certification = getStringValue(item, ['certification', 'rating', 'grade'])
      const genres = getStringArrayValue(item, ['genres', 'genreNames', 'genre'])

      if (!code || !title) {
        return null
      }

      return { code, title, poster, certification, genres }
    })
    .filter((movie): movie is MovieCard => movie !== null)
}

function normalizeMovieListPage(data: unknown): MovieListPage {
  const movies = normalizePopularMovies(data)

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
    }
  }

  return { movies, hasMore: movies.length > 0 }
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

function getCertificationIcon(certification: string) {
  return HOME_CERTIFICATION_ICON_MAP[certification.toUpperCase()] ?? ''
}

function MovieBrowsePage() {
  const navigate = useNavigate()
  const mainRef = useRef<HTMLElement | null>(null)
  const loadTriggerRef = useRef<HTMLDivElement | null>(null)
  const searchBoxRef = useRef<HTMLDivElement | null>(null)
  const pageRef = useRef(0)
  const hasMoreRef = useRef(true)
  const isLoadingMoreRef = useRef(false)

  const [theme, setTheme] = useState<BrowseTheme>(() => {
    if (typeof window === 'undefined') {
      return 'dark'
    }

    return window.localStorage.getItem(HOME_THEME_STORAGE_KEY) === 'light' ? 'light' : 'dark'
  })
  const [genreOptions, setGenreOptions] = useState<GenreOption[]>([{ label: '전체', value: 'ALL' }])
  const [selectedGenreFilters, setSelectedGenreFilters] = useState<string[]>(['ALL'])
  const [selectedSortFilter, setSelectedSortFilter] = useState<MovieSortFilter>('최신순')
  const [selectedReleaseFilter, setSelectedReleaseFilter] = useState<MovieReleaseFilter>('전체')
  const [query, setQuery] = useState('')
  const [isSearchLoading, setIsSearchLoading] = useState(false)
  const [searchMovies, setSearchMovies] = useState<SearchMovie[]>([])
  const [isSearchResultsOpen, setIsSearchResultsOpen] = useState(false)
  const [activeSearchIndex, setActiveSearchIndex] = useState(-1)
  const [movies, setMovies] = useState<MovieCard[]>([])
  const [isGenresLoading, setIsGenresLoading] = useState(true)
  const [isMoviesLoading, setIsMoviesLoading] = useState(true)
  const [isLoadingMoreMovies, setIsLoadingMoreMovies] = useState(false)

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
    window.localStorage.setItem(HOME_THEME_STORAGE_KEY, theme)
  }, [theme])

  useEffect(() => {
    document.documentElement.classList.add('movie-browse-root')
    document.documentElement.classList.toggle('movie-browse-root-light', theme === 'light')
    document.documentElement.classList.toggle('movie-browse-root-dark', theme === 'dark')
    document.body.classList.add('movie-browse-body')
    document.body.classList.toggle('movie-browse-body-light', theme === 'light')
    document.body.classList.toggle('movie-browse-body-dark', theme === 'dark')

    return () => {
      document.documentElement.classList.remove(
        'movie-browse-root',
        'movie-browse-root-light',
        'movie-browse-root-dark',
      )
      document.body.classList.remove(
        'movie-browse-body',
        'movie-browse-body-light',
        'movie-browse-body-dark',
      )
    }
  }, [theme])

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

  useEffect(() => {
    pageRef.current = 0
  }, [])

  const loadMoviesPage = useCallback(
    async (
      page: number,
      append: boolean,
      genreFilters: string[],
      sortFilter: MovieSortFilter,
      releaseFilter: MovieReleaseFilter,
    ) => {
      const searchParams = new URLSearchParams({
        page: String(page),
        size: String(MOVIE_BROWSE_PAGE_SIZE),
        sort: getSortQueryValue(sortFilter),
      })

      getGenreQueryValues(genreFilters).forEach((genreValue) => {
        searchParams.append('genre', genreValue)
      })

      const releaseValue = getReleaseQueryValue(releaseFilter)

      if (releaseValue) {
        searchParams.set('releaseStatus', releaseValue)
      }

      const response = await request<unknown>(`/movies?${searchParams.toString()}`, {
        method: 'GET',
      })
      const normalizedPage = normalizeMovieListPage(response)

      setMovies((previousMovies) =>
        append ? [...previousMovies, ...normalizedPage.movies] : normalizedPage.movies,
      )
      pageRef.current = page
      hasMoreRef.current = normalizedPage.hasMore
    },
    [],
  )

  useEffect(() => {
    let isMounted = true

    async function loadGenres() {
      setIsGenresLoading(true)

      try {
        const response = await request<unknown>('/movies/genres', { method: 'GET' })

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

    void loadGenres()

    return () => {
      isMounted = false
    }
  }, [])

  useEffect(() => {
    let isMounted = true

    async function loadInitialMovies() {
      setIsMoviesLoading(true)
      setMovies([])
      pageRef.current = 0
      hasMoreRef.current = true

      try {
        await loadMoviesPage(
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

        setMovies([])
      } finally {
        if (isMounted) {
          setIsMoviesLoading(false)
        }
      }
    }

    void loadInitialMovies()

    return () => {
      isMounted = false
    }
  }, [loadMoviesPage, selectedGenreFilters, selectedReleaseFilter, selectedSortFilter])

  useEffect(() => {
    const triggerElement = loadTriggerRef.current

    if (!triggerElement) {
      return
    }

    const observer = new IntersectionObserver(
      (entries) => {
        const firstEntry = entries[0]

        if (!firstEntry?.isIntersecting) {
          return
        }

        if (isMoviesLoading || isLoadingMoreRef.current || !hasMoreRef.current) {
          return
        }

        setIsLoadingMoreMovies(true)
        isLoadingMoreRef.current = true

        void loadMoviesPage(
          pageRef.current + 1,
          true,
          selectedGenreFilters,
          selectedSortFilter,
          selectedReleaseFilter,
        )
          .catch(() => {
            hasMoreRef.current = false
          })
          .finally(() => {
            setIsLoadingMoreMovies(false)
            isLoadingMoreRef.current = false
          })
      },
      {
        root: null,
        rootMargin: '0px 0px 320px 0px',
        threshold: 0.01,
      },
    )

    observer.observe(triggerElement)

    return () => {
      observer.disconnect()
    }
  }, [isMoviesLoading, loadMoviesPage, selectedGenreFilters, selectedReleaseFilter, selectedSortFilter])

  function moveToMovieDetail(movie: MovieCard) {
    setIsSearchResultsOpen(false)
    setActiveSearchIndex(-1)
    navigate(`/movies/${movie.code}`, {
      state: {
        movie: {
          id: movie.code,
          title: movie.title,
          poster: movie.poster,
        },
      },
    })
  }

  function moveToSearchMovieDetail(movie: SearchMovie) {
    setIsSearchResultsOpen(false)
    setActiveSearchIndex(-1)
    navigate(`/movies/${movie.code}`, {
      state: {
        movie: {
          id: movie.code,
          title: movie.title,
          poster: '',
        },
      },
    })
  }

  async function handleSearchSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const trimmedQuery = query.trim()

    if (!trimmedQuery) {
      setSearchMovies([])
      setActiveSearchIndex(-1)
      setIsSearchResultsOpen(false)
      return
    }

    if (activeSearchIndex >= 0 && searchMovies[activeSearchIndex]) {
      moveToSearchMovieDetail(searchMovies[activeSearchIndex])
      return
    }

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
        moveToSearchMovieDetail(normalizedMovies[0])
      }
    } catch {
      setSearchMovies([])
      setActiveSearchIndex(-1)
    } finally {
      setIsSearchLoading(false)
    }
  }

  return (
    <div className={`movie-browse-page movie-browse-page-${theme}`}>
      <header className="movie-browse-hero">
        <button className="movie-browse-hero-brand" type="button" onClick={() => navigate('/')}>
          <img className="movie-browse-hero-back-icon" src={arrowBackIcon} alt="" aria-hidden="true" />
          <span className="sr-only">홈으로 이동</span>
        </button>

        <div className="movie-browse-search-shell" ref={searchBoxRef}>
          <form className="movie-browse-search-form" onSubmit={handleSearchSubmit}>
            <label className="sr-only" htmlFor="movie-browse-search">
              영화 검색
            </label>
            <input
              id="movie-browse-search"
              className="movie-browse-search-input"
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
                  moveToSearchMovieDetail(searchMovies[activeSearchIndex])
                  return
                }

                if (event.key === 'Escape') {
                  setIsSearchResultsOpen(false)
                  setActiveSearchIndex(-1)
                }
              }}
            />
            <button className="movie-browse-search-button" type="submit">
              <span className="sr-only">검색</span>
              <img src={searchIcon} alt="" aria-hidden="true" />
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

        <div className="movie-browse-hero-actions">
          <button
            className="movie-browse-theme-button"
            type="button"
            onClick={() => setTheme((currentTheme) => (currentTheme === 'dark' ? 'light' : 'dark'))}
            aria-label={theme === 'dark' ? '라이트 모드로 전환' : '다크 모드로 전환'}
          >
            <img src={theme === 'dark' ? lightModeIcon : darkModeIcon} alt="" aria-hidden="true" />
          </button>
        </div>
      </header>

      <main className="movie-browse-content" ref={mainRef}>
        <section className="movie-browse-filter-panel" aria-label="영화 필터">
          <div className="movie-browse-filter-group">
            <span className="movie-browse-filter-label">장르</span>
            <div className="movie-browse-filter-row movie-browse-filter-row-genres">
              {genreOptions.map((genre) => (
                <button
                  key={genre.value}
                  className={`movie-browse-filter-button${
                    selectedGenreFilters.includes(genre.value)
                      ? ' movie-browse-filter-button-active'
                      : ''
                  }`}
                  type="button"
                  onClick={() => toggleGenreFilter(genre.value)}
                >
                  {genre.label}
                </button>
              ))}
            </div>
          </div>

          <div className="movie-browse-filter-group">
            <span className="movie-browse-filter-label">개봉 여부</span>
            <div className="movie-browse-filter-row">
              {HOME_RELEASE_FILTERS.map((filter) => (
                <button
                  key={filter}
                  className={`movie-browse-filter-button${
                    filter === selectedReleaseFilter ? ' movie-browse-filter-button-active' : ''
                  }`}
                  type="button"
                  onClick={() => setSelectedReleaseFilter(filter)}
                >
                  {filter}
                </button>
              ))}
            </div>
          </div>

          <div className="movie-browse-filter-group">
            <span className="movie-browse-filter-label">정렬</span>
            <div className="movie-browse-filter-row">
              {HOME_SORT_FILTERS.map((sort) => (
                <button
                  key={sort}
                  className={`movie-browse-filter-button${
                    sort === selectedSortFilter ? ' movie-browse-filter-button-active' : ''
                  }`}
                  type="button"
                  onClick={() => setSelectedSortFilter(sort)}
                >
                  {sort}
                </button>
              ))}
            </div>
          </div>
        </section>

        {isMoviesLoading || isGenresLoading ? (
          <div className="movie-browse-loading" aria-live="polite">
            <img className="movie-browse-loading-icon" src={loadingIcon} alt="" aria-hidden="true" />
          </div>
        ) : movies.length > 0 ? (
          <section className="movie-browse-grid" aria-label="전체 영화 목록">
            {movies.map((movie) => (
              <button
                key={`browse-movie-${movie.code}`}
                className="movie-browse-card"
                type="button"
                onClick={() => moveToMovieDetail(movie)}
                aria-label={`${movie.title} 상세 보기`}
              >
                <div className="movie-browse-poster-shell">
                  {movie.poster ? (
                    <img className="movie-browse-poster" src={movie.poster} alt={`${movie.title} 포스터`} />
                  ) : (
                    <div className="movie-browse-poster movie-browse-poster-fallback">
                      <span>{movie.title}</span>
                    </div>
                  )}
                </div>

                <div className="movie-browse-title-row">
                  <p className="movie-browse-title">{movie.title}</p>
                  {movie.certification ? (
                    getCertificationIcon(movie.certification) ? (
                      <img
                        className="movie-browse-certification-icon"
                        src={getCertificationIcon(movie.certification)}
                        alt={`${movie.certification} 관람등급`}
                      />
                    ) : (
                      <span className="movie-browse-certification-text">{movie.certification}</span>
                    )
                  ) : null}
                </div>

                {movie.genres.length > 0 ? (
                  <p className="movie-browse-genres">{movie.genres.join(' / ')}</p>
                ) : null}
              </button>
            ))}
          </section>
        ) : (
          <p className="movie-browse-empty">데이터를 불러오지 못하였습니다.</p>
        )}

        {isLoadingMoreMovies ? (
          <div className="movie-browse-loading-more" aria-live="polite">
            <img className="movie-browse-loading-icon" src={loadingIcon} alt="" aria-hidden="true" />
          </div>
        ) : null}

        <div className="movie-browse-load-trigger" ref={loadTriggerRef} aria-hidden="true" />
      </main>
    </div>
  )
}

export default MovieBrowsePage
