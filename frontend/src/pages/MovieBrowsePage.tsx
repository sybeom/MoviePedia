import { useCallback, useEffect, useRef, useState, type FormEvent } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import darkModeIcon from '../assets/icons/dark_mode.svg'
import lightModeIcon from '../assets/icons/light_mode.svg'
import loadingIcon from '../assets/icons/loading.svg'
import arrowBackIcon from '../assets/icons/arrow_back.svg'
import searchIcon from '../assets/icons/search.svg'
import { request } from '../api/client'
import HomeSearchResults from '../components/home/HomeSearchResults'
import { getMediaConfigByPath } from '../config/media'
import {
  HOME_RELEASE_FILTERS,
  HOME_SORT_FILTERS,
  HOME_THEME_STORAGE_KEY,
  SEARCH_DEBOUNCE_MS,
  type GenreOption,
  type MediaCard,
  type MediaReleaseFilter,
  type MediaSortFilter,
  type SearchMediaItem,
  getCertificationIcon,
  getGenreQueryValues,
  getReleaseQueryValue,
  getSortQueryValue,
  normalizeGenreOptions,
  normalizeMediaListPage,
  normalizeSearchMediaItems,
} from '../utils/mediaCatalog'
import './MovieBrowsePage.css'

type BrowseTheme = 'dark' | 'light'

const BROWSE_PAGE_SIZE = 30

function getReleaseFilterLabel(mediaType: 'movie' | 'series', filter: MediaReleaseFilter) {
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

function MovieBrowsePage() {
  const navigate = useNavigate()
  const location = useLocation()
  const mediaConfig = getMediaConfigByPath(location.pathname)
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
  const [selectedSortFilter, setSelectedSortFilter] = useState<MediaSortFilter>('최신순')
  const [selectedReleaseFilter, setSelectedReleaseFilter] = useState<MediaReleaseFilter>('전체')
  const [query, setQuery] = useState('')
  const [isSearchLoading, setIsSearchLoading] = useState(false)
  const [searchMovies, setSearchMovies] = useState<SearchMediaItem[]>([])
  const [isSearchResultsOpen, setIsSearchResultsOpen] = useState(false)
  const [activeSearchIndex, setActiveSearchIndex] = useState(-1)
  const [movies, setMovies] = useState<MediaCard[]>([])
  const [isGenresLoading, setIsGenresLoading] = useState(true)
  const [isMoviesLoading, setIsMoviesLoading] = useState(true)
  const [isLoadingMoreMovies, setIsLoadingMoreMovies] = useState(false)

  const toggleGenreFilter = useCallback((genreValue: string) => {
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
  }, [])

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
    let isMounted = true

    async function loadGenres() {
      setIsGenresLoading(true)

      try {
        const response = await request<unknown>(`${mediaConfig.resourcePath}/genres`, {
          method: 'GET',
        })

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
  }, [mediaConfig.resourcePath])

  const loadMoviesPage = useCallback(
    async (
      page: number,
      append: boolean,
      genreFilters: string[],
      sortFilter: MediaSortFilter,
      releaseFilter: MediaReleaseFilter,
    ) => {
      const searchParams = new URLSearchParams({
        page: String(page),
        size: String(BROWSE_PAGE_SIZE),
        sort: getSortQueryValue(sortFilter),
      })

      getGenreQueryValues(genreFilters).forEach((genreValue) => {
        searchParams.append('genre', genreValue)
      })

      const releaseValue = getReleaseQueryValue(releaseFilter)

      if (releaseValue) {
        searchParams.set('releaseStatus', releaseValue)
      }

      const response = await request<unknown>(
        `${mediaConfig.resourcePath}?${searchParams.toString()}`,
        { method: 'GET' },
      )
      const normalizedPage = normalizeMediaListPage(response)

      setMovies((previousMovies) =>
        append ? [...previousMovies, ...normalizedPage.movies] : normalizedPage.movies,
      )
      pageRef.current = page
      hasMoreRef.current = normalizedPage.hasMore
    },
    [mediaConfig.resourcePath],
  )

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
        hasMoreRef.current = false
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

        setSearchMovies(normalizeSearchMediaItems(response))
      } catch {
        if (isMounted) {
          setSearchMovies([])
        }
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
    function handlePointerDown(event: MouseEvent) {
      if (!searchBoxRef.current?.contains(event.target as Node)) {
        setIsSearchResultsOpen(false)
        setActiveSearchIndex(-1)
      }
    }

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') {
        setIsSearchResultsOpen(false)
        setActiveSearchIndex(-1)
      }
    }

    window.addEventListener('mousedown', handlePointerDown)
    window.addEventListener('keydown', handleKeyDown)

    return () => {
      window.removeEventListener('mousedown', handlePointerDown)
      window.removeEventListener('keydown', handleKeyDown)
    }
  }, [])

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

  const moveToMediaDetail = useCallback(
    (movie: MediaCard | SearchMediaItem) => {
      setIsSearchResultsOpen(false)
      setActiveSearchIndex(-1)
      navigate(mediaConfig.detailPath(movie.code), {
        state: {
          movie: {
            id: movie.code,
            title: movie.title,
            poster: 'poster' in movie ? movie.poster : '',
            seasonNum: 'seasonNum' in movie ? movie.seasonNum : '',
          },
        },
      })
    },
    [mediaConfig, navigate],
  )

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
      moveToMediaDetail(searchMovies[activeSearchIndex])
      return
    }

    setIsSearchLoading(true)

    try {
      const response = await request<unknown>(
        `${mediaConfig.resourcePath}/search?keyword=${encodeURIComponent(trimmedQuery)}`,
        { method: 'GET' },
      )

      const normalizedMovies = normalizeSearchMediaItems(response)
      setSearchMovies(normalizedMovies)
      setActiveSearchIndex(-1)
      setIsSearchResultsOpen(true)

      if (normalizedMovies.length === 1) {
        moveToMediaDetail(normalizedMovies[0])
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
        <button
          className="movie-browse-hero-brand"
          type="button"
          onClick={() => navigate(mediaConfig.homePath)}
        >
          <img className="movie-browse-hero-back-icon" src={arrowBackIcon} alt="" aria-hidden="true" />
          <span className="sr-only">홈으로 이동</span>
        </button>

        <div className="movie-browse-search-shell" ref={searchBoxRef}>
          <form className="movie-browse-search-form" onSubmit={handleSearchSubmit}>
            <label className="sr-only" htmlFor="movie-browse-search">
              {mediaConfig.searchLabel}
            </label>
            <input
              id="movie-browse-search"
              className="movie-browse-search-input"
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
                  moveToMediaDetail(searchMovies[activeSearchIndex])
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
            ariaLabel={`${mediaConfig.navLabel} 검색 목록`}
            buildDetailPath={mediaConfig.detailPath}
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
        <section className="movie-browse-filter-panel" aria-label={mediaConfig.filterAriaLabel}>
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
            <span className="movie-browse-filter-label">
              {mediaConfig.type === 'series' ? '공개 여부' : '개봉 여부'}
            </span>
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
                  {getReleaseFilterLabel(mediaConfig.type, filter)}
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
          <section className="movie-browse-grid" aria-label={mediaConfig.listAriaLabel}>
            {movies.map((movie) => (
              <button
                key={`${mediaConfig.type}-browse-${movie.code}`}
                className="movie-browse-card"
                type="button"
                onClick={() => moveToMediaDetail(movie)}
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
                  <p className="movie-browse-title">
                    {movie.title}
                    {mediaConfig.type === 'series' && movie.seasonNum ? ` 시즌 ${movie.seasonNum}` : ''}
                  </p>
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
