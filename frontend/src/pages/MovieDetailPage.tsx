import { useEffect, useRef, useState, type FormEvent, type MouseEvent } from 'react'
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

// 가로 스크롤 상태 타입 정의
type HorizontalScrollState = {
  isScrollable: boolean
  thumbWidth: number
  thumbOffset: number
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

const MIN_THUMB_WIDTH = 72
const STAR_COUNT = 5
const MAX_COMMENT_LENGTH = 100
const STAR_ICON_PATH =
  'M12 2.8c.38 0 .73.21.9.55l2.37 4.8 5.3.77c.75.11 1.05 1.03.5 1.56l-3.83 3.73.9 5.27c.13.74-.65 1.31-1.32.96L12 17.96l-4.82 2.53c-.67.35-1.45-.22-1.32-.96l.9-5.27-3.83-3.73c-.55-.53-.25-1.45.5-1.56l5.3-.77 2.37-4.8c.17-.34.52-.55.9-.55Z'

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

// 평점 표시 문자열 반환 처리
function getDisplayRating(value: string) {
  const normalizedValue = value.trim()

  if (!normalizedValue || normalizedValue.toLowerCase() === 'null') {
    return '-'
  }

  return normalizedValue
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

// 가로 스크롤 상태 계산 처리
function calculateHorizontalScrollState(element: HTMLDivElement): HorizontalScrollState {
  const viewportWidth = element.clientWidth
  const contentWidth = element.scrollWidth
  const scrollLeft = element.scrollLeft

  if (contentWidth <= viewportWidth || viewportWidth === 0) {
    return {
      isScrollable: false,
      thumbWidth: 0,
      thumbOffset: 0,
    }
  }

  const trackWidth = viewportWidth
  const thumbWidth = Math.max(MIN_THUMB_WIDTH, (viewportWidth / contentWidth) * trackWidth)
  const movableDistance = trackWidth - thumbWidth
  const maxScrollLeft = contentWidth - viewportWidth
  const thumbOffset = maxScrollLeft > 0 ? (scrollLeft / maxScrollLeft) * movableDistance : 0

  return {
    isScrollable: true,
    thumbWidth,
    thumbOffset,
  }
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

  // 출연진 스크롤 상태 관리
  const [creditScrollState, setCreditScrollState] = useState<HorizontalScrollState>({
    isScrollable: false,
    thumbWidth: 0,
    thumbOffset: 0,
  })

  // 상세 요청 중복 방지 참조 준비
  const hasLoadedDetailRef = useRef(false)

  // 출연진 스크롤 영역 참조 준비
  const creditListRef = useRef<HTMLDivElement | null>(null)

  // 코멘트 입력창 참조 준비
  const commentInputRef = useRef<HTMLTextAreaElement | null>(null)

  // 스크롤 드래그 시작 좌표 참조 준비
  const dragStartXRef = useRef(0)
  const dragStartScrollLeftRef = useRef(0)

  const displayedRating = hoverRating || selectedRating
  const trimmedCommentDraft = commentDraft.trim()
  const isCommentLengthValid =
    trimmedCommentDraft.length >= 1 && trimmedCommentDraft.length <= MAX_COMMENT_LENGTH
  const canClickCommentSubmit = canWriteComment && !isSubmittingComment

  // 상세 페이지 최상단 이동 처리
  useEffect(() => {
    window.scrollTo({
      top: 0,
      left: 0,
      behavior: 'auto',
    })
  }, [resolvedMovieId])

  // 출연진 스크롤 상태 반영 처리
  useEffect(() => {
    const element = creditListRef.current

    if (!element) {
      return
    }

    const scrollElement = element

    // 출연진 스크롤 상태 갱신 처리
    function updateCreditScrollState() {
      setCreditScrollState(calculateHorizontalScrollState(scrollElement))
    }

    updateCreditScrollState()
    scrollElement.addEventListener('scroll', updateCreditScrollState, { passive: true })
    window.addEventListener('resize', updateCreditScrollState)

    return () => {
      scrollElement.removeEventListener('scroll', updateCreditScrollState)
      window.removeEventListener('resize', updateCreditScrollState)
    }
  }, [movieDetail.credits])

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

  // 출연진 스크롤바 드래그 시작 처리
  function handleCreditScrollbarThumbMouseDown(event: MouseEvent<HTMLButtonElement>) {
    const element = creditListRef.current

    if (!element || !creditScrollState.isScrollable) {
      return
    }

    const scrollElement = element

    event.preventDefault()
    dragStartXRef.current = event.clientX
    dragStartScrollLeftRef.current = scrollElement.scrollLeft

    const viewportWidth = scrollElement.clientWidth
    const contentWidth = scrollElement.scrollWidth
    const maxScrollLeft = contentWidth - viewportWidth
    const movableDistance = viewportWidth - creditScrollState.thumbWidth

    // 출연진 스크롤 드래그 이동 처리
    function handleMouseMove(moveEvent: globalThis.MouseEvent) {
      if (movableDistance <= 0 || maxScrollLeft <= 0) {
        return
      }

      const deltaX = moveEvent.clientX - dragStartXRef.current
      const scrollRatio = maxScrollLeft / movableDistance
      scrollElement.scrollLeft = dragStartScrollLeftRef.current + deltaX * scrollRatio
    }

    // 출연진 스크롤 드래그 종료 처리
    function handleMouseUp() {
      window.removeEventListener('mousemove', handleMouseMove)
      window.removeEventListener('mouseup', handleMouseUp)
    }

    window.addEventListener('mousemove', handleMouseMove)
    window.addEventListener('mouseup', handleMouseUp)
  }

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
    } catch {
      setCommentSubmitMessage('코멘트를 등록하지 못했습니다.')
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
                <div className="movie-detail-cast-list" ref={creditListRef}>
                  {movieDetail.credits.map((member, index) => (
                    <article className="movie-detail-cast-card" key={`${member.name}-${index}`}>
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

                {creditScrollState.isScrollable ? (
                  <div className="movie-detail-cast-custom-scrollbar" aria-hidden="true">
                    <button
                      className="movie-detail-cast-custom-scrollbar-thumb"
                      type="button"
                      style={{
                        width: `${creditScrollState.thumbWidth}px`,
                        transform: `translateX(${creditScrollState.thumbOffset}px)`,
                      }}
                      onMouseDown={handleCreditScrollbarThumbMouseDown}
                      tabIndex={-1}
                    />
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
                <p className="movie-detail-rating-value">-</p>
              </article>

              <article className="movie-detail-rating-card">
                <p className="movie-detail-rating-label">글로벌</p>
                <p className="movie-detail-rating-value">{getDisplayRating(movieDetail.globalRating)}</p>
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
                  placeholder="이 영화에 대한 생각을 남겨보세요."
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
          </div>
        </section>

        <section className="movie-detail-recommendations-shell" aria-labelledby="movie-detail-recommendations-title">
          <div className="movie-detail-recommendations">
            <h2 id="movie-detail-recommendations-title">관련 추천 영화</h2>
          </div>
        </section>
      </main>
    </div>
  )
}

export default MovieDetailPage
