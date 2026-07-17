import rating12Icon from '../assets/ratings/12.svg'
import rating15Icon from '../assets/ratings/15.svg'
import rating19Icon from '../assets/ratings/19.svg'
import ratingAllIcon from '../assets/ratings/all.svg'

export type SearchMediaItem = {
  code: string
  title: string
  seasonNum?: string
}

export type MediaCard = {
  code: string
  title: string
  seasonNum: string
  poster: string
  genres: string[]
  certification: string
}

export type BannerMediaItem = {
  code: string
  title: string
  backdrop: string
}

export type GenreOption = {
  label: string
  value: string
}

export type MediaListPage = {
  movies: MediaCard[]
  hasMore: boolean
}

export type MediaSortFilter = '최신순' | '오래된순'
export type MediaReleaseFilter = '전체' | '개봉' | '미개봉'

export const SEARCH_DEBOUNCE_MS = 500
export const HOME_THEME_STORAGE_KEY = 'moviepedia.home.theme'
export const HOME_SORT_FILTERS: MediaSortFilter[] = ['최신순', '오래된순']
export const HOME_RELEASE_FILTERS: MediaReleaseFilter[] = ['전체', '개봉', '미개봉']
export const TMDB_IMAGE_BASE_URL = 'https://image.tmdb.org/t/p/original'

const CERTIFICATION_ICON_MAP: Record<string, string> = {
  '12': rating12Icon,
  '15': rating15Icon,
  '19': rating19Icon,
  ALL: ratingAllIcon,
}

export function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null
}

export function getStringValue(record: Record<string, unknown>, keys: string[]) {
  for (const key of keys) {
    const value = record[key]

    if (typeof value === 'string' && value.trim()) {
      return value
    }
  }

  return ''
}

export function getScalarStringValue(record: Record<string, unknown>, keys: string[]) {
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

export function getCertificationIcon(certification: string) {
  return CERTIFICATION_ICON_MAP[certification.toUpperCase()] ?? ''
}

export function getStringArrayValue(record: Record<string, unknown>, keys: string[]) {
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

export function getMovieListValue(data: unknown) {
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

export function getSearchResultListValue(data: unknown) {
  if (Array.isArray(data)) {
    return data
  }

  if (!isRecord(data)) {
    return []
  }

  const exactKeys = ['searchResults', 'results', 'matches', 'data']

  for (const key of exactKeys) {
    const value = data[key]

    if (Array.isArray(value)) {
      return value
    }
  }

  const nestedData = data.data

  if (isRecord(nestedData)) {
    for (const key of exactKeys) {
      const value = nestedData[key]

      if (Array.isArray(value)) {
        return value
      }
    }
  }

  return []
}

export function normalizeMediaListPage(data: unknown): MediaListPage {
  const movies = normalizeMediaCards(getMovieListValue(data))

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

export function getGenreQueryValues(filters: string[]) {
  return filters.filter((filter) => filter !== 'ALL')
}

export function getSortQueryValue(filter: MediaSortFilter) {
  return filter === '오래된순' ? 'OLDEST' : 'LATEST'
}

export function getReleaseQueryValue(filter: MediaReleaseFilter) {
  if (filter === '개봉') {
    return 'RELEASED'
  }

  if (filter === '미개봉') {
    return 'UNRELEASED'
  }

  return ''
}

export function normalizeGenreOptions(data: unknown): GenreOption[] {
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

  return [
    { label: '전체', value: 'ALL' },
    ...normalizedOptions.filter((option) => option.value !== 'ALL'),
  ]
}

export function getImageSource(value: string) {
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

export function normalizeSearchMediaItems(data: unknown): SearchMediaItem[] {
  if (!Array.isArray(data)) {
    return []
  }

  return data
    .filter(isRecord)
    .map((value) => {
      const code = getScalarStringValue(value, ['code', 'movieCode', 'movieCd', 'seriesCode'])
      const title = getStringValue(value, ['title', 'movieNm', 'name', 'seriesNm'])
      const seasonNum = getScalarStringValue(value, ['seasonNum', 'season', 'seasonNumber'])

      return { code, title, seasonNum }
    })
    .filter((value) => value.code && value.title)
}

export function normalizeMediaCards(data: unknown): MediaCard[] {
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
        'mvCode',
        'seriesCode',
      ])
      const title = getStringValue(value, ['title', 'movieNm', 'name', 'seriesNm'])
      const seasonNum = getScalarStringValue(value, ['seasonNum', 'season', 'seasonNumber'])
      const poster = getImageSource(
        getStringValue(value, ['poster', 'posterPath', 'poster_path', 'image']),
      )
      const genres = getStringArrayValue(value, ['genres', 'genre', 'genreNames'])
      const certification =
        getStringValue(value, ['certification', 'rating', 'ageRating', 'grade']) || '등급 미정'

      return { code, title, seasonNum, poster, genres, certification }
    })
    .filter((value) => value.code && value.title)
}

export function normalizeBannerMediaItems(data: unknown): BannerMediaItem[] {
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
        'mvCode',
        'seriesCode',
      ])
      const title = getStringValue(value, ['title', 'movieNm', 'name', 'seriesNm'])
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
