import { useEffect, useRef, useState } from 'react'
import { useLocation, useParams } from 'react-router-dom'
import { request } from '../api/client'
import Header from '../components/Header'
import './MovieDetailPage.css'

// 영화 상세 이동 상태 타입 정의
type MovieDetailState = {
  movie?: {
    id?: string
    title?: string
    poster?: string
  }
}

// 영화 상세 화면 데이터 타입 정의
type MovieDetailView = {
  id: string
  title: string
  poster: string
  backdrop: string
  genres: string
  overview: string
  releaseDate: string
  originCountry: string
  runtime: string
  voteAverage: string
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

    if (typeof value === 'number' && Number.isFinite(value)) {
      return String(value)
    }
  }

  return ''
}

// 영화 식별자 추출 처리
function getMovieIdentifier(record: Record<string, unknown>) {
  const directIdentifier = getStringValue(record, ['movieCode', 'movie_code', 'id', 'movieId', 'movieCd', 'code'])

  if (directIdentifier) {
    return directIdentifier
  }

  const nestedCandidates = [record.movie, record.content, record.item, record.data]

  for (const candidate of nestedCandidates) {
    if (!isRecord(candidate)) {
      continue
    }

    const nestedIdentifier = getStringValue(candidate, [
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

// 문자열 배열 결합 처리
function getJoinedStringArrayValue(record: Record<string, unknown>, keys: string[]) {
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
function getPrimaryImageUrl(imageUrl: string) {
  return (
    imageUrl
      .split('|')
      .map((url) => url.trim())
      .find(Boolean) ?? ''
  )
}

// 평점 표시 문자열 반환 처리
function getDisplayRating(value: string) {
  return value.trim() || '-'
}

// 영화 상세 응답 정규화 처리
function normalizeMovieDetail(data: unknown): MovieDetailView | null {
  if (!isRecord(data)) {
    return null
  }

  const id = getMovieIdentifier(data)
  const title = getStringValue(data, ['title', 'movieNm', 'name', 'movieTitle'])
  const poster = getPrimaryImageUrl(
    getStringValue(data, ['poster_path', 'poster', 'posterUrl', 'imageUrl', 'posterPath']),
  )
  const backdrop = getPrimaryImageUrl(
    getStringValue(data, ['backdrop_path', 'backdrop', 'backdropUrl', 'backdropPath']),
  )
  const genres = getJoinedStringArrayValue(data, ['genres', 'genre'])
  const overview = getStringValue(data, ['overview', 'plot'])
  const releaseDate = getStringValue(data, ['releaseYear', 'release_date', 'releaseDate', 'openDt'])
  const originCountry = getJoinedStringArrayValue(data, ['origin_country'])
  const runtime = getStringValue(data, ['runtime'])
  const voteAverage = getStringValue(data, ['vote_average', 'voteAverage', 'vote', 'rating'])

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
    voteAverage,
  }
}

// 영화 상세 기본값 생성 처리
function createInitialMovieDetail(movieId: string, movie?: MovieDetailState['movie']): MovieDetailView {
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
    voteAverage: '',
  }
}

// 영화 상세 화면 구성
function MovieDetailPage() {
  // URL 파라미터 조회 처리
  const { movieId } = useParams()
  const resolvedMovieId = movieId ?? ''

  // 이동 상태 조회 처리
  const location = useLocation()
  const state = location.state as MovieDetailState | null
  const initialMovie = state?.movie

  // 영화 상세 데이터 상태 관리
  const [movieDetail, setMovieDetail] = useState<MovieDetailView>(() =>
    createInitialMovieDetail(resolvedMovieId, initialMovie),
  )

  // 영화 상세 로딩 상태 관리
  const [isLoading, setIsLoading] = useState(Boolean(resolvedMovieId))

  // 영화 상세 메시지 상태 관리
  const [message, setMessage] = useState(resolvedMovieId ? '' : '영화 정보를 찾을 수 없습니다.')

  // 상세 요청 중복 방지 참조 준비
  const hasLoadedDetailRef = useRef(false)

  useEffect(() => {
    // 상세 페이지 진입 시 최상단 이동 처리
    window.scrollTo({
      top: 0,
      left: 0,
      behavior: 'auto',
    })
  }, [resolvedMovieId])

  useEffect(() => {
    if (hasLoadedDetailRef.current) {
      return
    }

    hasLoadedDetailRef.current = true

    if (!resolvedMovieId) {
      return
    }

    // 영화 상세 정보 조회 처리
    async function fetchMovieDetail() {
      try {
        const response = await request<unknown>(`/movies/${resolvedMovieId}`, {
          method: 'GET',
        })

        const normalizedDetail = normalizeMovieDetail(response)

        if (normalizedDetail) {
          setMovieDetail({
            id: normalizedDetail.id || resolvedMovieId,
            title: normalizedDetail.title || initialMovie?.title?.trim() || '영화 상세',
            poster: normalizedDetail.poster || initialMovie?.poster?.trim() || '',
            backdrop: normalizedDetail.backdrop,
            genres: normalizedDetail.genres,
            overview: normalizedDetail.overview,
            releaseDate: normalizedDetail.releaseDate,
            originCountry: normalizedDetail.originCountry,
            runtime: normalizedDetail.runtime,
            voteAverage: normalizedDetail.voteAverage,
          })
        } else {
          setMessage('영화 정보를 불러오지 못했습니다.')
        }
      } catch {
        setMessage('영화 정보를 불러오지 못했습니다.')
      } finally {
        setIsLoading(false)
      }
    }

    void fetchMovieDetail()
  }, [initialMovie?.poster, initialMovie?.title, resolvedMovieId])

  return (
    <div className="app">
      <Header showAuthActions transparentOnTop textOnlyAuthAction />

      <main className="movie-detail-page">
        <section className="movie-detail-hero" aria-labelledby="movie-detail-title">
          <div className="movie-detail-backdrop-shell">
            {movieDetail.backdrop ? (
              <img className="movie-detail-backdrop" src={movieDetail.backdrop} alt={movieDetail.title} />
            ) : null}
          </div>
          <div className="movie-detail-overlay" />

          <div className="movie-detail-hero-content">
            <div className="movie-detail-panel">
              <div className="movie-detail-summary">
                <div className="movie-detail-poster-shell">
                  {movieDetail.poster ? (
                    <img className="movie-detail-poster" src={movieDetail.poster} alt={movieDetail.title} />
                  ) : null}
                </div>

                <div className="movie-detail-copy">
                  <h1 id="movie-detail-title">{movieDetail.title}</h1>

                  <dl className="movie-detail-meta">
                    <div className="movie-detail-meta-row">
                      <dt>장르</dt>
                      <dd>{movieDetail.genres || '-'}</dd>
                    </div>
                    <div className="movie-detail-meta-row">
                      <dt>개봉일</dt>
                      <dd>{movieDetail.releaseDate || '-'}</dd>
                    </div>
                    <div className="movie-detail-meta-row">
                      <dt>국가</dt>
                      <dd>{movieDetail.originCountry || '-'}</dd>
                    </div>
                    <div className="movie-detail-meta-row">
                      <dt>상영시간</dt>
                      <dd>{movieDetail.runtime ? `${movieDetail.runtime}분` : '-'}</dd>
                    </div>
                  </dl>

                  <section className="movie-detail-overview-section">
                    <p className="movie-detail-overview">{movieDetail.overview || '줄거리 정보가 아직 없습니다.'}</p>
                  </section>

                  {isLoading ? <p className="movie-detail-message">영화 정보를 불러오는 중입니다...</p> : null}
                  {!isLoading && message ? <p className="movie-detail-message">{message}</p> : null}
                </div>
              </div>
            </div>
          </div>
        </section>

        <section className="movie-detail-ratings-shell" aria-label="영화 평점">
          <div className="movie-detail-ratings">
            <article className="movie-detail-rating-card">
              <p className="movie-detail-rating-label">피디아 평점</p>
              <p className="movie-detail-rating-value">-</p>
            </article>

            <article className="movie-detail-rating-card">
              <p className="movie-detail-rating-label">글로벌 평점</p>
              <p className="movie-detail-rating-value">{getDisplayRating(movieDetail.voteAverage)}</p>
            </article>
          </div>
        </section>

        <section className="movie-detail-comments-shell" aria-labelledby="movie-detail-comments-title">
          <div className="movie-detail-comments">
            <h2 id="movie-detail-comments-title">한줄 코멘트</h2>
          </div>
        </section>

        <section
          className="movie-detail-recommendations-shell"
          aria-labelledby="movie-detail-recommendations-title"
        >
          <div className="movie-detail-recommendations">
            <h2 id="movie-detail-recommendations-title">관련 추천 영화</h2>
          </div>
        </section>
      </main>
    </div>
  )
}

export default MovieDetailPage
