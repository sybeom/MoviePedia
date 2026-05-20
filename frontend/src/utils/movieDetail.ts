import type {
  CreditMember,
  MovieComment,
  MovieCommentDetail,
  MovieDetailState,
  MovieDetailView,
} from '../types/movieDetail'

export const STAR_COUNT = 5
export const MAX_COMMENT_LENGTH = 100
export const STAR_ICON_PATH =
  'M12 2.8c.38 0 .73.21.9.55l2.37 4.8 5.3.77c.75.11 1.05 1.03.5 1.56l-3.83 3.73.9 5.27c.13.74-.65 1.31-1.32.96L12 17.96l-4.82 2.53c-.67.35-1.45-.22-1.32-.96l.9-5.27-3.83-3.73c-.55-.53-.25-1.45.5-1.56l5.3-.77 2.37-4.8c.17-.34.52-.55.9-.55Z'

// 객체 데이터 여부 확인
export function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null
}

// 문자열 또는 숫자 값 추출 처리
export function getStringValue(record: Record<string, unknown>, keys: string[]) {
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

// 불리언 값 추출 처리
export function getBooleanValue(record: Record<string, unknown>, keys: string[]) {
  for (const key of keys) {
    const value = record[key]

    if (typeof value === 'boolean') {
      return value
    }
  }

  return false
}

// 숫자 값 추출 처리
export function getNumberValue(record: Record<string, unknown>, keys: string[]) {
  for (const key of keys) {
    const value = record[key]

    if (typeof value === 'number' && Number.isFinite(value)) {
      return value
    }

    if (typeof value === 'string') {
      const parsedValue = Number(value)

      if (Number.isFinite(parsedValue)) {
        return parsedValue
      }
    }
  }

  return 0
}

// 영화 식별자 추출 처리
export function getMovieIdentifier(record: Record<string, unknown>) {
  const directIdentifier = getStringValue(record, ['movieCode', 'id', 'movieId', 'code'])

  if (directIdentifier) {
    return directIdentifier
  }

  const nestedCandidates = [record.movie, record.content, record.item, record.data]

  for (const candidate of nestedCandidates) {
    if (!isRecord(candidate)) {
      continue
    }

    const nestedIdentifier = getStringValue(candidate, ['movieCode', 'id', 'movieId', 'code'])

    if (nestedIdentifier) {
      return nestedIdentifier
    }
  }

  return ''
}

// 배열 문자열 결합 처리
export function getJoinedStringArrayValue(record: Record<string, unknown>, keys: string[]) {
  for (const key of keys) {
    const value = record[key]

    if (!Array.isArray(value)) {
      continue
    }

    const joinedValue = value
      .map((item) => {
        if (typeof item === 'string' && item.trim()) {
          return item
        }

        if (isRecord(item)) {
          return getStringValue(item, ['name'])
        }

        return ''
      })
      .filter(Boolean)
      .join(', ')

    if (joinedValue) {
      return joinedValue
    }
  }

  return ''
}

// 대표 이미지 URL 추출 처리
export function getPrimaryImageUrl(imageUrl: string) {
  return (
    imageUrl
      .split('|')
      .map((url) => url.trim())
      .find(Boolean) ?? ''
  )
}

// 코멘트 목록 추출 처리
export function getCommentListValue(data: unknown) {
  if (Array.isArray(data)) {
    return data
  }

  if (!isRecord(data)) {
    return []
  }

  const candidateKeys = ['comments', 'commentList', 'items', 'content', 'data']

  for (const key of candidateKeys) {
    const value = data[key]

    if (Array.isArray(value)) {
      return value
    }
  }

  return []
}

// 제작 및 출연 목록 정리 처리
export function getCreditValue(record: Record<string, unknown>) {
  const creditValue = record.credit

  if (!Array.isArray(creditValue)) {
    return []
  }

  return creditValue
    .filter(isRecord)
    .map((member) => {
      const role = getStringValue(member, ['role']).toUpperCase()

      if (role !== 'DIRECTOR' && role !== 'ACTOR') {
        return null
      }

      return {
        name: getStringValue(member, ['name']),
        profile: getPrimaryImageUrl(getStringValue(member, ['profile', 'profileUrl', 'profilePath'])),
        roleLabel: role === 'DIRECTOR' ? '감독' : '',
      }
    })
    .filter((member): member is CreditMember => member !== null)
    .filter((member) => member.name || member.profile)
}

// 만점 포함 평점 표시 문자열 반환 처리
export function getDisplayRatingWithScale(value: string, maxScore: number) {
  const normalizedValue = value.trim()

  if (!normalizedValue || normalizedValue.toLowerCase() === 'null') {
    return `- / ${maxScore}`
  }

  return `${normalizedValue} / ${maxScore}`
}

// 선택 평점 표시 문자열 반환 처리
export function getSelectedRatingLabel(value: number) {
  return value > 0 ? value.toFixed(1) : '-'
}

// 별 채움 비율 계산 처리
export function getStarFillPercent(starIndex: number, rating: number) {
  const starStart = starIndex
  const starEnd = starIndex + 1

  if (rating >= starEnd) {
    return 100
  }

  if (rating <= starStart) {
    return 0
  }

  return Math.max(0, Math.min(100, (rating - starStart) * 100))
}

// 상세 응답 정규화 처리
export function normalizeMovieDetail(data: unknown): MovieDetailView | null {
  if (!isRecord(data)) {
    return null
  }

  const id = getMovieIdentifier(data)
  const title = getStringValue(data, ['title', 'movieTitle', 'name'])
  const poster = getPrimaryImageUrl(getStringValue(data, ['poster', 'posterUrl', 'imageUrl', 'posterPath']))
  const backdrop = getPrimaryImageUrl(getStringValue(data, ['backdrop', 'backdropUrl', 'backdropPath']))
  const genres = getJoinedStringArrayValue(data, ['genres', 'genre'])
  const overview = getStringValue(data, ['overview', 'plot'])
  const releaseDate = getStringValue(data, ['releaseYear', 'releaseDate'])
  const originCountry = getJoinedStringArrayValue(data, ['country'])
  const runtime = getStringValue(data, ['runtime'])
  const globalRating = getStringValue(data, ['globalRating'])
  const credits = getCreditValue(data)

  if (!id && !title && !poster && !overview) {
    return null
  }

  return {
    id,
    title,
    poster,
    backdrop,
    genres,
    overview,
    releaseDate,
    originCountry,
    runtime,
    globalRating,
    credits,
  }
}

// 코멘트 목록 정규화 처리
export function normalizeMovieComments(data: unknown): MovieComment[] {
  return getCommentListValue(data)
    .filter(isRecord)
    .map((comment, index) => {
      const commentId =
        getStringValue(comment, ['commentId', 'id', 'code']) || `comment-${index}`

      return {
        id: getStringValue(comment, ['id', 'commentId', 'code']) || commentId,
        commentId,
        movieId: getStringValue(comment, ['movieId']),
        nickname: getStringValue(comment, ['nickname', 'writerNickname', 'author', 'writer']) || '익명',
        content: getStringValue(comment, ['content', 'comment']) || '-',
        rating: getStringValue(comment, ['rating', 'score', 'voteAverage']),
        isMine: getBooleanValue(comment, ['isMine']),
      }
    })
}

// 단일 코멘트 상세 정규화 처리
export function normalizeMovieCommentDetail(data: unknown): MovieCommentDetail | null {
  if (!isRecord(data)) {
    return null
  }

  const commentId = getStringValue(data, ['commentId', 'id', 'code'])
  const movieId = getStringValue(data, ['movieId'])
  const content = getStringValue(data, ['content', 'comment'])
  const nickname = getStringValue(data, ['nickname', 'writerNickname', 'author', 'writer']) || '익명'

  if (!commentId && !content) {
    return null
  }

  return {
    movieId,
    commentId,
    nickname,
    content,
    rating: getNumberValue(data, ['rating', 'score', 'voteAverage']),
    likeCount: getStringValue(data, ['likeCount', 'likes', 'likeCnt']),
  }
}

// 초기 상세 데이터 생성 처리
export function createInitialMovieDetail(movieId: string, movie?: MovieDetailState['movie']): MovieDetailView {
  return {
    id: movie?.id?.trim() || movieId,
    title: movie?.title?.trim() || '영화 상세',
    poster: movie?.poster?.trim() || '',
    backdrop: '',
    genres: '',
    overview: '',
    releaseDate: '',
    originCountry: '',
    runtime: '',
    globalRating: '',
    credits: [],
  }
}
