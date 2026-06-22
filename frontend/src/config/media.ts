export type MediaType = 'movie' | 'series'

export type MediaConfig = {
  type: MediaType
  resourcePath: '/movies' | '/series'
  homePath: '/' | '/series'
  browsePath: '/movies' | '/series/browse'
  detailPath: (code: string) => string
  navLabel: string
  searchLabel: string
  searchPlaceholder: string
  allItemsTitle: string
  currentItemsTitle: string
  bannerAriaLabel: string
  filterAriaLabel: string
  listAriaLabel: string
  writeLabel: string
}

export const MEDIA_CONFIGS: Record<MediaType, MediaConfig> = {
  movie: {
    type: 'movie',
    resourcePath: '/movies',
    homePath: '/',
    browsePath: '/movies',
    detailPath: (code) => `/movies/${code}`,
    navLabel: '영화',
    searchLabel: '영화 검색',
    searchPlaceholder: '영화 제목을 입력해보세요',
    allItemsTitle: '전체 영화',
    currentItemsTitle: '현재 상영중인 영화',
    bannerAriaLabel: '현재 상영중인 영화 배너',
    filterAriaLabel: '영화 필터',
    listAriaLabel: '전체 영화 목록',
    writeLabel: '작성하기',
  },
  series: {
    type: 'series',
    resourcePath: '/series',
    homePath: '/series',
    browsePath: '/series/browse',
    detailPath: (code) => `/series/${code}`,
    navLabel: 'TV 시리즈',
    searchLabel: '시리즈 검색',
    searchPlaceholder: '시리즈 제목을 입력해보세요',
    allItemsTitle: '전체 시리즈',
    currentItemsTitle: '현재 방영중인 시리즈',
    bannerAriaLabel: '현재 방영중인 시리즈 배너',
    filterAriaLabel: '시리즈 필터',
    listAriaLabel: '전체 시리즈 목록',
    writeLabel: '작성하기',
  },
}

export function getMediaTypeFromPath(pathname: string): MediaType {
  return pathname.startsWith('/series') ? 'series' : 'movie'
}

export function getMediaConfigByPath(pathname: string) {
  return MEDIA_CONFIGS[getMediaTypeFromPath(pathname)]
}
