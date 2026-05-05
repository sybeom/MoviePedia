import { useEffect, useRef, useState } from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'
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

// 대표 포스터 URL 추출 처리
function getPrimaryPosterUrl(poster: string) {
  return poster
    .split('|')
    .map((url) => url.trim())
    .find(Boolean) ?? ''
}

// 영화 상세 응답 정규화 처리
function normalizeMovieDetail(data: unknown): MovieDetailView | null {
  if (!isRecord(data)) {
    return null
  }

  const title = getStringValue(data, ['title', 'movieNm', 'name', 'movieTitle'])
  const poster = getPrimaryPosterUrl(
    getStringValue(data, ['poster', 'posterUrl', 'imageUrl', 'posterPath', 'poster_path']),
  )
  const id = getStringValue(data, ['id', 'movieId', 'movieCd'])

  if (!title && !poster && !id) {
    return null
  }

  return {
    id,
    title,
    poster,
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
  const [movieDetail, setMovieDetail] = useState<MovieDetailView>({
    id: initialMovie?.id?.trim() || resolvedMovieId,
    title: initialMovie?.title?.trim() || '영화 상세',
    poster: initialMovie?.poster?.trim() || '',
  })

  // 영화 상세 로딩 상태 관리
  const [isLoading, setIsLoading] = useState(Boolean(resolvedMovieId))

  // 영화 상세 메시지 상태 관리
  const [message, setMessage] = useState(resolvedMovieId ? '' : '영화 정보를 찾을 수 없습니다.')

  // 상세 요청 중복 방지 참조 준비
  const hasLoadedDetailRef = useRef(false)

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
      <Header showAuthActions />

      <main className="movie-detail-container">
        <div className="movie-detail-content">
          <Link className="movie-detail-back-link" to="/">
            홈으로
          </Link>

          <section className="movie-detail-hero" aria-labelledby="movie-detail-title">
            <div className="movie-detail-poster-shell">
              {movieDetail.poster ? (
                <img className="movie-detail-poster" src={movieDetail.poster} alt={movieDetail.title} />
              ) : null}
            </div>

            <div className="movie-detail-copy">
              <p className="movie-detail-id">{movieDetail.id || resolvedMovieId}</p>
              <h1 id="movie-detail-title">{movieDetail.title}</h1>
              {isLoading ? <p className="movie-detail-message">영화 정보를 불러오는 중입니다...</p> : null}
              {!isLoading && message ? <p className="movie-detail-message">{message}</p> : null}
            </div>
          </section>
        </div>
      </main>
    </div>
  )
}

export default MovieDetailPage
