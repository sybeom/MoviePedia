import { useEffect, useRef, useState, type FormEvent } from 'react'
import { useLocation, useParams } from 'react-router-dom'
import { ApiError, request } from '../api/client'
import Header from '../components/Header'
import { getAuthSession } from '../utils/authStorage'
import { authRequest, isAuthSessionError } from '../utils/fetchUtil'
import './MovieDetailPage.css'

// 상세 이동 상태 타입 정의
type MovieDetailState = {
  movie?: {
    id?: string
    title?: string
    poster?: string
  }
}

// 제작 및 출연 데이터 타입 정의
type CreditMember = {
  name: string
  profile: string
  roleLabel: string
}

// 상세 화면 데이터 타입 정의
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
  globalRating: string
  credits: CreditMember[]
}

// 로그인 확인 응답 타입 정의
type AuthMeResponse = {
  loginId?: string
  nickname?: string
}

// 코멘트 작성 요청 타입 정의
type CreateCommentRequest = {
  nickname: string
  content: string
  rating: number
}

// 코멘트 목록 데이터 타입 정의
type MovieComment = {
  id: string
  nickname: string
  content: string
  rating: string
}

const STAR_COUNT = 5
const MAX_COMMENT_LENGTH = 100
const STAR_ICON_PATH =
  'M12 2.8c.38 0 .73.21.9.55l2.37 4.8 5.3.77c.75.11 1.05 1.03.5 1.56l-3.83 3.73.9 5.27c.13.74-.65 1.31-1.32.96L12 17.96l-4.82 2.53c-.67.35-1.45-.22-1.32-.96l.9-5.27-3.83-3.73c-.55-.53-.25-1.45.5-1.56l5.3-.77 2.37-4.8c.17-.34.52-.55.9-.55Z'
const commentRequestMap = new Map<string, Promise<MovieComment[]>>()

// 객체 데이터 여부 확인
function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null
}

// 문자열 또는 숫자 값 추출 처리
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

// 코멘트 목록 추출 처리
function getCommentListValue(data: unknown) {
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
function getCreditValue(record: Record<string, unknown>) {
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
function getDisplayRatingWithScale(value: string, maxScore: number) {
  const normalizedValue = value.trim()

  if (!normalizedValue || normalizedValue.toLowerCase() === 'null') {
    return `- / ${maxScore}`
  }

  return `${normalizedValue} / ${maxScore}`
}

// 선택 평점 표시 문자열 반환 처리
function getSelectedRatingLabel(value: number) {
  return value > 0 ? value.toFixed(1) : '-'
}

// 별 채움 비율 계산 처리
function getStarFillPercent(starIndex: number, rating: number) {
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
function normalizeMovieDetail(data: unknown): MovieDetailView | null {
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
function normalizeMovieComments(data: unknown): MovieComment[] {
  return getCommentListValue(data)
    .filter(isRecord)
    .map((comment, index) => ({
      id: getStringValue(comment, ['id', 'commentId', 'code']) || `comment-${index}`,
      nickname: getStringValue(comment, ['nickname', 'writerNickname', 'author', 'writer']) || '익명',
      content: getStringValue(comment, ['content', 'comment']) || '-',
      rating: getStringValue(comment, ['rating', 'score', 'voteAverage']),
    }))
}

// 코멘트 목록 요청 중복 방지 처리
function fetchMovieCommentsRequest(movieId: string) {
  const existingRequest = commentRequestMap.get(movieId)

  if (existingRequest) {
    return existingRequest
  }

  const requestPromise = request<unknown>(`/movies/${movieId}/comments`, {
    method: 'GET',
  })
    .then((response) => normalizeMovieComments(response))
    .finally(() => {
      commentRequestMap.delete(movieId)
    })

  commentRequestMap.set(movieId, requestPromise)

  return requestPromise
}

// 초기 상세 데이터 생성 처리
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
    globalRating: '',
    credits: [],
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

  // 상세 데이터 상태 관리
  const [movieDetail, setMovieDetail] = useState<MovieDetailView>(() =>
    createInitialMovieDetail(resolvedMovieId, initialMovie),
  )

  // 로딩 상태 관리
  const [isLoading, setIsLoading] = useState(Boolean(resolvedMovieId))

  // 안내 메시지 상태 관리
  const [message, setMessage] = useState(resolvedMovieId ? '' : '영화 정보를 찾을 수 없습니다.')

  // 코멘트 입력값 상태 관리
  const [commentDraft, setCommentDraft] = useState('')

  // 코멘트 목록 상태 관리
  const [comments, setComments] = useState<MovieComment[]>([])

  // 코멘트 목록 로딩 상태 관리
  const [isCommentsLoading, setIsCommentsLoading] = useState(Boolean(resolvedMovieId))

  // 사용자 별점 상태 관리
  const [selectedRating, setSelectedRating] = useState(0)

  // 사용자 별점 미리보기 상태 관리
  const [hoverRating, setHoverRating] = useState(0)

  // 코멘트 작성 가능 상태 관리
  const [canWriteComment, setCanWriteComment] = useState(false)

  // 코멘트 로그인 확인 상태 관리
  const [isCheckingCommentAuth, setIsCheckingCommentAuth] = useState(false)

  // 코멘트 전송 상태 관리
  const [isSubmittingComment, setIsSubmittingComment] = useState(false)

  // 코멘트 전송 메시지 상태 관리
  const [commentSubmitMessage, setCommentSubmitMessage] = useState('')

  // 상세 요청 중복 방지 참조 준비
  const hasLoadedDetailRef = useRef(false)

  // 코멘트 입력창 참조 준비
  const commentInputRef = useRef<HTMLTextAreaElement | null>(null)

  const displayedRating = hoverRating || selectedRating
  const trimmedCommentDraft = commentDraft.trim()
  const isCommentLengthValid =
    trimmedCommentDraft.length >= 1 && trimmedCommentDraft.length <= MAX_COMMENT_LENGTH
  const canClickCommentSubmit = canWriteComment && !isSubmittingComment
  const directorCredits = movieDetail.credits.filter((member) => member.roleLabel === '감독')
  const actorCredits = movieDetail.credits.filter((member) => member.roleLabel !== '감독')

  // 상세 페이지 최상단 이동 처리
  useEffect(() => {
    window.scrollTo({
      top: 0,
      left: 0,
      behavior: 'auto',
    })
  }, [resolvedMovieId])

  // 상세 정보 조회 처리
  useEffect(() => {
    if (hasLoadedDetailRef.current) {
      return
    }

    hasLoadedDetailRef.current = true

    if (!resolvedMovieId) {
      return
    }

    // 영화 상세 조회 처리
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
            globalRating: normalizedDetail.globalRating,
            credits: normalizedDetail.credits,
          })
          setMessage('')
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

  // 코멘트 목록 조회 처리
  useEffect(() => {
    let isMounted = true

    if (!resolvedMovieId) {
      return () => {
        isMounted = false
      }
    }

    // 코멘트 목록 조회 실행 처리
    async function fetchComments() {
      try {
        const response = await fetchMovieCommentsRequest(resolvedMovieId)

        if (!isMounted) {
          return
        }

        setComments(response)
      } catch {
        if (!isMounted) {
          return
        }

        setComments([])
      } finally {
        if (isMounted) {
          setIsCommentsLoading(false)
        }
      }
    }

    void fetchComments()

    return () => {
      isMounted = false
    }
  }, [resolvedMovieId])

  // 코멘트 제출 기본 동작 방지 처리
  async function handleCommentSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const session = getAuthSession()

    // 로그인 세션 존재 여부 확인 처리
    if (!canClickCommentSubmit || !session?.nickname) {
      return
    }

    // 평점 선택 여부 확인 처리
    if (displayedRating <= 0) {
      alert('평점을 선택해주세요.')
      return
    }

    // 코멘트 글자 수 조건 확인 처리
    if (!isCommentLengthValid) {
      alert('코멘트는 1자 이상 100자 이하로 작성해주세요.')
      return
    }

    setIsSubmittingComment(true)
    setCommentSubmitMessage('')

    try {
      await authRequest<CreateCommentRequest>(`/movies/${resolvedMovieId}/comments`, {
        method: 'POST',
        body: {
          nickname: session.nickname,
          content: trimmedCommentDraft,
          rating: displayedRating,
        },
      })

      setCommentDraft('')
      setSelectedRating(0)
      setHoverRating(0)

      setIsCommentsLoading(true)

      try {
        const commentsResponse = await fetchMovieCommentsRequest(resolvedMovieId)
        setComments(commentsResponse)
      } finally {
        setIsCommentsLoading(false)
      }
    } catch (error) {
      // 중복 코멘트 작성 응답 분기 처리
      if (error instanceof ApiError && error.status === 409) {
        alert('이미 이 영화에 코멘트를 작성했습니다.')
      }

      setCommentSubmitMessage('')
    } finally {
      setIsSubmittingComment(false)
    }
  }

  // 코멘트 작성 로그인 확인 처리
  async function handleCommentInputFocus() {
    if (isCheckingCommentAuth) {
      return
    }

    const session = getAuthSession()

    if (!session?.accessToken) {
      setCanWriteComment(false)
      alert('로그인이 필요한 서비스입니다.')
      commentInputRef.current?.blur()
      return
    }

    setIsCheckingCommentAuth(true)

    try {
      await authRequest<AuthMeResponse>('/auth/me', {
        method: 'GET',
      })

      setCanWriteComment(true)
    } catch (error) {
      setCanWriteComment(false)
      alert('로그인이 필요한 서비스입니다.')

      if (isAuthSessionError(error) || (error instanceof ApiError && error.status === 401)) {
        commentInputRef.current?.blur()
      }
    } finally {
      setIsCheckingCommentAuth(false)
    }
  }

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

                <div className="movie-detail-copy-shell">
                  <div className="movie-detail-copy">
                    <h1 id="movie-detail-title">{movieDetail.title}</h1>

                    <dl className="movie-detail-meta">
                      <div className="movie-detail-meta-row">
                        <dt>장르</dt>
                        <dd>{movieDetail.genres || '-'}</dd>
                      </div>
                      <div className="movie-detail-meta-row">
                        <dt>개봉</dt>
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
                      <h2>줄거리</h2>
                      <p className="movie-detail-overview">{movieDetail.overview || '-'}</p>
                    </section>

                    {isLoading ? <p className="movie-detail-message">영화 정보를 불러오는 중입니다...</p> : null}
                    {!isLoading && message ? <p className="movie-detail-message">{message}</p> : null}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        {movieDetail.credits.length > 0 ? (
          <section className="movie-detail-cast-shell" aria-label="제작 및 출연">
            <div className="movie-detail-cast-section">
              <h2>제작/출연</h2>
              <div className="movie-detail-cast-list-shell">
                {directorCredits.length > 0 ? (
                  <div className="movie-detail-cast-director-row">
                    {directorCredits.map((member, index) => (
                      <article className="movie-detail-cast-card movie-detail-cast-card-director" key={`director-${member.name}-${index}`}>
                        <div className="movie-detail-cast-profile-shell">
                          {member.profile ? (
                            <img className="movie-detail-cast-profile" src={member.profile} alt={member.name} />
                          ) : null}
                        </div>
                        <p className="movie-detail-cast-name">{member.name || '-'}</p>
                        <p className="movie-detail-cast-role">{member.roleLabel || ' '}</p>
                      </article>
                    ))}
                  </div>
                ) : null}

                {actorCredits.length > 0 ? (
                  <div className="movie-detail-cast-grid">
                    {actorCredits.map((member, index) => (
                      <article className="movie-detail-cast-card" key={`actor-${member.name}-${index}`}>
                        <div className="movie-detail-cast-profile-shell">
                          {member.profile ? (
                            <img className="movie-detail-cast-profile" src={member.profile} alt={member.name} />
                          ) : null}
                        </div>
                        <p className="movie-detail-cast-name">{member.name || '-'}</p>
                        <p className="movie-detail-cast-role">{member.roleLabel || ' '}</p>
                      </article>
                    ))}
                  </div>
                ) : null}
              </div>
            </div>
          </section>
        ) : null}

        <section className="movie-detail-ratings-shell" aria-label="영화 평점">
          <div className="movie-detail-ratings-section">
            <h2 className="movie-detail-ratings-title">평점</h2>
            <div className="movie-detail-ratings">
              <article className="movie-detail-rating-card">
                <p className="movie-detail-rating-label">피디아</p>
                <div className="movie-detail-rating-display">
                  <span className="movie-detail-rating-star-icon" aria-hidden="true">
                    <svg viewBox="0 0 24 24" focusable="false" aria-hidden="true">
                      <path d={STAR_ICON_PATH} />
                    </svg>
                  </span>
                  <p className="movie-detail-rating-value">{getDisplayRatingWithScale('', 5)}</p>
                </div>
              </article>

              <article className="movie-detail-rating-card">
                <p className="movie-detail-rating-label">글로벌</p>
                <div className="movie-detail-rating-display">
                  <span className="movie-detail-rating-star-icon" aria-hidden="true">
                    <svg viewBox="0 0 24 24" focusable="false" aria-hidden="true">
                      <path d={STAR_ICON_PATH} />
                    </svg>
                  </span>
                  <p className="movie-detail-rating-value">{getDisplayRatingWithScale(movieDetail.globalRating, 10)}</p>
                </div>
              </article>
            </div>
          </div>
        </section>

        <section className="movie-detail-comments-shell" aria-labelledby="movie-detail-comments-title">
          <div className="movie-detail-comments">
            <h2 id="movie-detail-comments-title">한줄 코멘트</h2>

            <div className="movie-detail-comment-rating-shell">
              <div
                className="movie-detail-rating-stars"
                onMouseLeave={() => setHoverRating(0)}
                aria-label={`선택한 별점 ${getSelectedRatingLabel(selectedRating)}`}
              >
                {Array.from({ length: STAR_COUNT }, (_, index) => {
                  const fillPercent = getStarFillPercent(index, displayedRating)

                  return (
                    <div className="movie-detail-rating-star-shell" key={`rating-star-${index + 1}`}>
                      <span className="movie-detail-rating-star-base" aria-hidden="true">
                        <svg viewBox="0 0 24 24" focusable="false" aria-hidden="true">
                          <path d={STAR_ICON_PATH} />
                        </svg>
                      </span>
                      <span
                        className="movie-detail-rating-star-fill"
                        aria-hidden="true"
                        style={{ clipPath: `inset(0 ${100 - fillPercent}% 0 0)` }}
                      >
                        <svg viewBox="0 0 24 24" focusable="false" aria-hidden="true">
                          <path d={STAR_ICON_PATH} />
                        </svg>
                      </span>
                      <button
                        className="movie-detail-rating-star-hit movie-detail-rating-star-hit-left"
                        type="button"
                        aria-label={`${index + 0.5}점 선택`}
                        onMouseEnter={() => setHoverRating(index + 0.5)}
                        onFocus={() => setHoverRating(index + 0.5)}
                        onClick={() => setSelectedRating(index + 0.5)}
                      />
                      <button
                        className="movie-detail-rating-star-hit movie-detail-rating-star-hit-right"
                        type="button"
                        aria-label={`${index + 1}점 선택`}
                        onMouseEnter={() => setHoverRating(index + 1)}
                        onFocus={() => setHoverRating(index + 1)}
                        onClick={() => setSelectedRating(index + 1)}
                      />
                    </div>
                  )
                })}
                {displayedRating > 0 ? (
                  <span className="movie-detail-rating-value movie-detail-rating-value-inline">
                    {getSelectedRatingLabel(displayedRating)}
                  </span>
                ) : null}
              </div>
            </div>

            <form className="movie-detail-comment-form" onSubmit={handleCommentSubmit}>
              <label className="sr-only" htmlFor="movie-detail-comment-input">
                한줄 코멘트 입력
              </label>
              <div className="movie-detail-comment-input-shell">
                <textarea
                  ref={commentInputRef}
                  id="movie-detail-comment-input"
                  className="movie-detail-comment-input"
                  maxLength={MAX_COMMENT_LENGTH}
                  readOnly={!canWriteComment}
                  value={commentDraft}
                  onChange={(event) => setCommentDraft(event.target.value)}
                  onFocus={() => {
                    void handleCommentInputFocus()
                  }}
                  placeholder="영화에 대한 솔직한 평가를 남겨보세요!"
                />
              </div>
              <div className="movie-detail-comment-footer">
                <p className="movie-detail-comment-count">{`${commentDraft.length}/${MAX_COMMENT_LENGTH}`}</p>
                <button className="movie-detail-comment-submit" type="submit" disabled={!canClickCommentSubmit}>
                  {isSubmittingComment ? '등록 중' : '작성'}
                </button>
              </div>
            </form>
            {commentSubmitMessage ? <p className="movie-detail-comment-message">{commentSubmitMessage}</p> : null}

            <div className="movie-detail-comment-list">
              {isCommentsLoading ? (
                <p className="movie-detail-comment-list-message">코멘트를 불러오는 중입니다...</p>
              ) : comments.length > 0 ? (
                comments.map((comment) => (
                  <article className="movie-detail-comment-card" key={comment.id}>
                    <div className="movie-detail-comment-card-header">
                      <div className="movie-detail-comment-card-profile">
                        <span className="movie-detail-comment-card-avatar" aria-hidden="true">
                          {comment.nickname.slice(0, 1) || '?'}
                        </span>
                        <p className="movie-detail-comment-card-nickname">{comment.nickname}</p>
                      </div>
                      <div className="movie-detail-comment-card-rating">
                        <span className="movie-detail-comment-card-rating-star" aria-hidden="true">
                          <svg viewBox="0 0 24 24" focusable="false" aria-hidden="true">
                            <path d={STAR_ICON_PATH} />
                          </svg>
                        </span>
                        <span>{comment.rating || '-'}</span>
                      </div>
                    </div>
                    <div className="movie-detail-comment-card-divider" aria-hidden="true" />
                    <p className="movie-detail-comment-card-content">{comment.content}</p>
                  </article>
                ))
              ) : (
                <p className="movie-detail-comment-list-message">아직 등록된 코멘트가 없습니다.</p>
              )}
            </div>
          </div>
        </section>
      </main>
    </div>
  )
}

export default MovieDetailPage
