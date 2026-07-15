import { useCallback, useEffect, useLayoutEffect, useMemo, useRef, useState, type FormEvent } from 'react'
import { useLocation, useNavigate, useSearchParams } from 'react-router-dom'
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
const BROWSE_SCROLL_STORAGE_KEY_PREFIX = 'moviepedia.browse.scroll:'
const BROWSE_LIST_STORAGE_KEY_PREFIX = 'moviepedia.browse.list:'

function getSearchRequestPath(resourcePath: string, mediaType: 'movie' | 'series', keyword: string) {
  const searchBasePath = mediaType === 'series' ? '/tv/search' : `${resourcePath}/search`

  return `${searchBasePath}?keyword=${encodeURIComponent(keyword)}`
}

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

function getInitialGenreFilters(searchParams: URLSearchParams) {
  const genres = searchParams.getAll('genre').filter(Boolean)
  return genres.length > 0 ? genres : ['ALL']
}

function getInitialSortFilter(searchParams: URLSearchParams): MediaSortFilter {
  return searchParams.get('sort') === 'OLDEST' ? '오래된순' : '최신순'
}

function getInitialReleaseFilter(searchParams: URLSearchParams): MediaReleaseFilter {
  const releaseStatus = searchParams.get('releaseStatus')

  if (releaseStatus === 'RELEASED') {
    return '개봉'
  }

  if (releaseStatus === 'UNRELEASED') {
    return '미개봉'
  }

  return '전체'
}

function getBrowseScrollStorageKey(pathname: string, search: string) {
  return `${BROWSE_SCROLL_STORAGE_KEY_PREFIX}${pathname}${search}`
}

function getBrowseListStorageKey(pathname: string, search: string) {
  return `${BROWSE_LIST_STORAGE_KEY_PREFIX}${pathname}${search}`
}

function isScrollableBrowseContainer(container: HTMLElement | null) {
  return Boolean(container && container.scrollHeight > container.clientHeight + 1)
}

function saveBrowseScrollPosition(storageKey: string, container: HTMLElement | null) {
  if (typeof window === 'undefined') {
    return
  }

  const scrollTop = isScrollableBrowseContainer(container)
    ? container?.scrollTop ?? 0
    : window.scrollY ?? 0
  window.sessionStorage.setItem(storageKey, String(scrollTop))
}

function restoreBrowseScrollPosition(storageKey: string, container: HTMLElement | null) {
  if (typeof window === 'undefined') {
    return true
  }

  const savedValue = window.sessionStorage.getItem(storageKey)

  if (!savedValue) {
    return true
  }

  const targetScrollTop = Number(savedValue)

  if (!Number.isFinite(targetScrollTop) || targetScrollTop <= 0) {
    window.sessionStorage.removeItem(storageKey)
    return true
  }

  window.scrollTo({ top: targetScrollTop, left: 0, behavior: 'auto' })
  if (isScrollableBrowseContainer(container)) {
    container?.scrollTo({ top: targetScrollTop, left: 0, behavior: 'auto' })
  }

  const currentScrollTop = isScrollableBrowseContainer(container)
    ? container?.scrollTop ?? 0
    : window.scrollY ?? 0
  const isRestored = Math.abs(currentScrollTop - targetScrollTop) < 4

  if (isRestored) {
    window.sessionStorage.removeItem(storageKey)
  }

  return isRestored
}

function readBrowseListSnapshot(storageKey: string) {
  if (typeof window === 'undefined') {
    return null
  }

  const rawValue = window.sessionStorage.getItem(storageKey)

  if (!rawValue) {
    return null
  }

  try {
    const parsedValue = JSON.parse(rawValue) as {
      movies: MediaCard[]
      page: number
      hasMore: boolean
    }

    if (!Array.isArray(parsedValue.movies)) {
      return null
    }

    return {
      movies: parsedValue.movies,
      page: Number.isFinite(parsedValue.page) ? parsedValue.page : 0,
      hasMore: Boolean(parsedValue.hasMore),
    }
  } catch {
    return null
  }
}

function writeBrowseListSnapshot(
  storageKey: string,
  snapshot: {
    movies: MediaCard[]
    page: number
    hasMore: boolean
  },
) {
  if (typeof window === 'undefined') {
    return
  }

  window.sessionStorage.setItem(storageKey, JSON.stringify(snapshot))
}

function markCurrentBrowseHistoryEntryForRestore() {
  if (typeof window === 'undefined') {
    return
  }

  const currentHistoryState = window.history.state ?? {}
  const currentUserState =
    currentHistoryState.usr && typeof currentHistoryState.usr === 'object'
      ? currentHistoryState.usr
      : {}

  window.history.replaceState(
    {
      ...currentHistoryState,
      usr: {
        ...currentUserState,
        restoreFromDetail: true,
      },
    },
    '',
  )
}

function clearCurrentBrowseHistoryEntryRestoreMark() {
  if (typeof window === 'undefined') {
    return
  }

  const currentHistoryState = window.history.state ?? {}
  const currentUserState =
    currentHistoryState.usr && typeof currentHistoryState.usr === 'object'
      ? { ...currentHistoryState.usr }
      : null

  if (!currentUserState || !('restoreFromDetail' in currentUserState)) {
    return
  }

  delete currentUserState.restoreFromDetail

  window.history.replaceState(
    {
      ...currentHistoryState,
      usr: currentUserState,
    },
    '',
  )
}

function MovieBrowsePage() {
  const navigate = useNavigate()
  const location = useLocation()
  const [searchParams, setSearchParams] = useSearchParams()
  const mediaConfig = getMediaConfigByPath(location.pathname)
  const shouldRestoreFromDetail =
    typeof location.state === 'object' &&
    location.state !== null &&
    'restoreFromDetail' in location.state &&
    location.state.restoreFromDetail === true
  const scrollStorageKey = getBrowseScrollStorageKey(location.pathname, location.search)
  const listStorageKey = getBrowseListStorageKey(location.pathname, location.search)
  const initialListSnapshot = useMemo(
    () => (shouldRestoreFromDetail ? readBrowseListSnapshot(listStorageKey) : null),
    [listStorageKey, shouldRestoreFromDetail],
  )
  const mainRef = useRef<HTMLElement | null>(null)
  const loadTriggerRef = useRef<HTMLDivElement | null>(null)
  const searchBoxRef = useRef<HTMLDivElement | null>(null)
  const pageRef = useRef(initialListSnapshot?.page ?? 0)
  const hasMoreRef = useRef(initialListSnapshot?.hasMore ?? true)
  const isLoadingMoreRef = useRef(false)
  const shouldRestoreScrollRef = useRef(true)

  const [theme, setTheme] = useState<BrowseTheme>(() => {
    if (typeof window === 'undefined') {
      return 'dark'
    }

    return window.localStorage.getItem(HOME_THEME_STORAGE_KEY) === 'light' ? 'light' : 'dark'
  })
  const [genreOptions, setGenreOptions] = useState<GenreOption[]>([{ label: '전체', value: 'ALL' }])
  const [selectedGenreFilters, setSelectedGenreFilters] = useState<string[]>(() =>
    getInitialGenreFilters(searchParams),
  )
  const [selectedSortFilter, setSelectedSortFilter] = useState<MediaSortFilter>(() =>
    getInitialSortFilter(searchParams),
  )
  const [selectedReleaseFilter, setSelectedReleaseFilter] = useState<MediaReleaseFilter>(() =>
    getInitialReleaseFilter(searchParams),
  )
  const [query, setQuery] = useState('')
  const [isSearchLoading, setIsSearchLoading] = useState(false)
  const [searchMovies, setSearchMovies] = useState<SearchMediaItem[]>([])
  const [isSearchResultsOpen, setIsSearchResultsOpen] = useState(false)
  const [activeSearchIndex, setActiveSearchIndex] = useState(-1)
  const [movies, setMovies] = useState<MediaCard[]>(() => initialListSnapshot?.movies ?? [])
  const [isGenresLoading, setIsGenresLoading] = useState(true)
  const [isMoviesLoading, setIsMoviesLoading] = useState(() => initialListSnapshot === null)
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
    shouldRestoreScrollRef.current = true
  }, [scrollStorageKey])

  useEffect(() => {
    if (shouldRestoreFromDetail) {
      return
    }

    shouldRestoreScrollRef.current = false
    window.sessionStorage.removeItem(scrollStorageKey)
    window.sessionStorage.removeItem(listStorageKey)
    clearCurrentBrowseHistoryEntryRestoreMark()
  }, [listStorageKey, scrollStorageKey, shouldRestoreFromDetail])

  useEffect(() => {
    if (!shouldRestoreFromDetail) {
      return
    }

    return () => {
      writeBrowseListSnapshot(listStorageKey, {
        movies,
        page: pageRef.current,
        hasMore: hasMoreRef.current,
      })
    }
  }, [listStorageKey, movies, shouldRestoreFromDetail])

  useEffect(() => {
    const nextSearchParams = new URLSearchParams()

    getGenreQueryValues(selectedGenreFilters).forEach((genreValue) => {
      nextSearchParams.append('genre', genreValue)
    })

    nextSearchParams.set('sort', getSortQueryValue(selectedSortFilter))

    const releaseValue = getReleaseQueryValue(selectedReleaseFilter)

    if (releaseValue) {
      nextSearchParams.set('releaseStatus', releaseValue)
    }

    if (nextSearchParams.toString() !== searchParams.toString()) {
      setSearchParams(nextSearchParams, { replace: true })
    }
  }, [
    searchParams,
    selectedGenreFilters,
    selectedReleaseFilter,
    selectedSortFilter,
    setSearchParams,
  ])

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
    const currentMainRef = mainRef.current

    return () => {
      saveBrowseScrollPosition(scrollStorageKey, currentMainRef)
    }
  }, [scrollStorageKey])

  useLayoutEffect(() => {
    if (shouldRestoreScrollRef.current === false) {
      return
    }

    if (!shouldRestoreFromDetail || isGenresLoading || isMoviesLoading) {
      return
    }

    const rafId = window.requestAnimationFrame(() => {
      if (restoreBrowseScrollPosition(scrollStorageKey, mainRef.current)) {
        shouldRestoreScrollRef.current = false
        clearCurrentBrowseHistoryEntryRestoreMark()
      }
    })

    return () => {
      window.cancelAnimationFrame(rafId)
    }
  }, [isGenresLoading, isMoviesLoading, movies.length, scrollStorageKey, shouldRestoreFromDetail])

  useEffect(() => {
    let isMounted = true

    async function loadGenres() {
      setIsGenresLoading(true)

      try {
        const searchParams = new URLSearchParams({
          mediaType: mediaConfig.type === 'series' ? 'TV' : 'MOVIE',
        })
        const response = await request<unknown>(
          `${mediaConfig.resourcePath}/genres?${searchParams.toString()}`,
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

    void loadGenres()

    return () => {
      isMounted = false
    }
  }, [mediaConfig.resourcePath, mediaConfig.type])

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
      const cachedSnapshot = shouldRestoreFromDetail ? initialListSnapshot : null

      if (cachedSnapshot) {
        setMovies(cachedSnapshot.movies)
        pageRef.current = cachedSnapshot.page
        hasMoreRef.current = cachedSnapshot.hasMore
        setIsMoviesLoading(false)
        return
      }

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
  }, [
    initialListSnapshot,
    listStorageKey,
    loadMoviesPage,
    selectedGenreFilters,
    selectedReleaseFilter,
    selectedSortFilter,
    shouldRestoreFromDetail,
  ])

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
          getSearchRequestPath(mediaConfig.resourcePath, mediaConfig.type, trimmedQuery),
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
  }, [mediaConfig.resourcePath, mediaConfig.type, query])

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

        if (
          shouldRestoreScrollRef.current ||
          isMoviesLoading ||
          isLoadingMoreRef.current ||
          !hasMoreRef.current
        ) {
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
      saveBrowseScrollPosition(scrollStorageKey, mainRef.current)
      markCurrentBrowseHistoryEntryForRestore()
      writeBrowseListSnapshot(listStorageKey, {
        movies,
        page: pageRef.current,
        hasMore: hasMoreRef.current,
      })
      const seasonNum = 'seasonNum' in movie ? movie.seasonNum : ''

      navigate(mediaConfig.detailPath(movie.code, seasonNum), {
        state: {
          from: `${location.pathname}${location.search}`,
          movie: {
            id: movie.code,
            title: movie.title,
            poster: 'poster' in movie ? movie.poster : '',
            seasonNum,
          },
        },
      })
    },
    [location.pathname, location.search, listStorageKey, mediaConfig, movies, navigate, scrollStorageKey],
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
        getSearchRequestPath(mediaConfig.resourcePath, mediaConfig.type, trimmedQuery),
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
          onClick={() => {
            saveBrowseScrollPosition(scrollStorageKey, mainRef.current)
            writeBrowseListSnapshot(listStorageKey, {
              movies,
              page: pageRef.current,
              hasMore: hasMoreRef.current,
            })
            navigate(mediaConfig.homePath)
          }}
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
