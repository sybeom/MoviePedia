import { useCallback, useEffect, useRef, useState, type FormEvent } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router-dom'
import darkModeIcon from '../assets/icons/dark_mode.svg'
import lightModeIcon from '../assets/icons/light_mode.svg'
import naverLoginButtonImage from '../assets/icons/NAVER_login.png'
import { login, logout } from '../api/auth'
import { ApiError, isApiError } from '../api/client'
import {
  createMovieComment,
  deleteMovieComment,
  fetchMovieCommentForEdit,
  fetchMovieComments,
  fetchMovieDetail,
  updateMovieComment,
  verifyCommentAuth,
} from '../api/movieDetail'
import MovieCommentList from '../components/movie-detail/MovieCommentList'
import MovieCommentModal from '../components/movie-detail/MovieCommentModal'
import MovieDetailCredits from '../components/movie-detail/MovieDetailCredits'
import MovieDetailHero from '../components/movie-detail/MovieDetailHero'
import type {
  MovieComment,
  MovieCommentDetail,
  MovieDetailState,
  MovieDetailView,
} from '../types/movieDetail'
import {
  clearAuthSession,
  getAuthSession,
  saveAuthSession,
  subscribeAuthSessionChange,
  type AuthSession,
} from '../utils/authStorage'
import { isAuthSessionError } from '../utils/fetchUtil'
import { MAX_COMMENT_LENGTH, createInitialMovieDetail } from '../utils/movieDetail'
import './HomePage.css'
import './MovieDetailPage.css'

type CommentModalMode = 'create' | 'edit' | null
type HomeTheme = 'dark' | 'light'
type CommentSortOrder = 'latest' | 'oldest'

const HOME_THEME_STORAGE_KEY = 'moviepedia.home.theme'
const PRIMARY_NAV_ITEMS = ['영화', 'TV 시리즈']
const COMMENTS_PAGE_SIZE = 20

function MovieDetailPage() {
  const navigate = useNavigate()
  const { movieCode: movieCodeParam } = useParams()
  const resolvedMovieCode = movieCodeParam ?? ''
  const location = useLocation()
  const state = location.state as MovieDetailState | null
  const initialMovie = state?.movie
  const mainShellRef = useRef<HTMLElement | null>(null)
  const hasLoadedDetailRef = useRef(false)
  const commentInputRef = useRef<HTMLTextAreaElement | null>(null)
  const commentsSectionRef = useRef<HTMLElement | null>(null)

  const [authSession, setAuthSession] = useState<AuthSession | null>(() => getAuthSession())
  const [theme, setTheme] = useState<HomeTheme>(() => {
    if (typeof window === 'undefined') {
      return 'dark'
    }

    return window.localStorage.getItem(HOME_THEME_STORAGE_KEY) === 'light' ? 'light' : 'dark'
  })
  const [isLoggingOut, setIsLoggingOut] = useState(false)
  const [loginId, setLoginId] = useState('')
  const [password, setPassword] = useState('')
  const [loginMessage, setLoginMessage] = useState('')
  const [isLoginSubmitting, setIsLoginSubmitting] = useState(false)

  const [movieDetail, setMovieDetail] = useState<MovieDetailView>(() =>
    createInitialMovieDetail(resolvedMovieCode, initialMovie),
  )
  const [resolvedMovieRecordId, setResolvedMovieRecordId] = useState('')
  const [isLoading, setIsLoading] = useState(Boolean(resolvedMovieCode))
  const [message, setMessage] = useState(
    resolvedMovieCode ? '' : '영화 정보를 찾을 수 없습니다.',
  )

  const [commentDraft, setCommentDraft] = useState('')
  const [comments, setComments] = useState<MovieComment[]>([])
  const [isCommentsLoading, setIsCommentsLoading] = useState(Boolean(resolvedMovieCode))
  const [isLoadingMoreComments, setIsLoadingMoreComments] = useState(false)
  const [commentsPage, setCommentsPage] = useState(0)
  const [hasMoreComments, setHasMoreComments] = useState(true)
  const [selectedRating, setSelectedRating] = useState(0)
  const [canWriteComment, setCanWriteComment] = useState(false)
  const [isCheckingCommentAuth, setIsCheckingCommentAuth] = useState(false)
  const [isSubmittingComment, setIsSubmittingComment] = useState(false)
  const [commentModalMode, setCommentModalMode] = useState<CommentModalMode>(null)
  const [commentSortOrder, setCommentSortOrder] = useState<CommentSortOrder>('latest')
  const [editingCommentTarget, setEditingCommentTarget] = useState<{
    movieId: string
    commentId: string
  } | null>(null)

  const trimmedCommentDraft = commentDraft.trim()
  const isCommentLengthValid =
    trimmedCommentDraft.length >= 1 && trimmedCommentDraft.length <= MAX_COMMENT_LENGTH
  const isEditMode = commentModalMode === 'edit'
  const isCreateMode = commentModalMode === 'create'
  const isCommentModalOpen = isCreateMode || isEditMode
  const selectedReactionType = selectedRating > 0 ? 'LIKE' : 'DISLIKE'
  const commentModalSubmitLabel = isEditMode ? '수정' : '저장'
  const commentsPageRef = useRef(0)
  const isLoadingMoreCommentsRef = useRef(false)
  const hasMoreCommentsRef = useRef(true)
  const commentSortOrderRef = useRef<CommentSortOrder>('latest')
  const shouldWaitForCommentsScrollResetRef = useRef(false)

  useEffect(() => {
    function handleAuthSessionChange() {
      setAuthSession(getAuthSession())
    }

    return subscribeAuthSessionChange(handleAuthSessionChange)
  }, [])

  useEffect(() => {
    window.localStorage.setItem(HOME_THEME_STORAGE_KEY, theme)
  }, [theme])

  useEffect(() => {
    mainShellRef.current?.scrollTo({
      top: 0,
      left: 0,
      behavior: 'auto',
    })
  }, [resolvedMovieCode])

  useEffect(() => {
    hasLoadedDetailRef.current = false
  }, [resolvedMovieCode])

  useEffect(() => {
    commentsPageRef.current = commentsPage
  }, [commentsPage])

  useEffect(() => {
    isLoadingMoreCommentsRef.current = isLoadingMoreComments
  }, [isLoadingMoreComments])

  useEffect(() => {
    hasMoreCommentsRef.current = hasMoreComments
  }, [hasMoreComments])

  useEffect(() => {
    commentSortOrderRef.current = commentSortOrder
  }, [commentSortOrder])

  const loadMovieCommentsPage = useCallback(
    async (page: number, append: boolean, sortOrder: CommentSortOrder) => {
      const response = await fetchMovieComments(resolvedMovieCode, page, sortOrder)

      const nextComments = response.comments

      setResolvedMovieRecordId(response.movieId)
      setComments((previousComments) =>
        append ? [...previousComments, ...nextComments] : nextComments,
      )
      setCommentsPage(page)
      setHasMoreComments(nextComments.length === COMMENTS_PAGE_SIZE)

      commentsPageRef.current = page
      hasMoreCommentsRef.current = nextComments.length === COMMENTS_PAGE_SIZE
    },
    [resolvedMovieCode],
  )

  useEffect(() => {
    if (hasLoadedDetailRef.current || !resolvedMovieCode) {
      return
    }

    hasLoadedDetailRef.current = true

    async function loadMovieDetail() {
      try {
        const normalizedDetail = await fetchMovieDetail(resolvedMovieCode)

        if (!normalizedDetail) {
          setMessage('영화 정보를 불러오지 못했습니다.')
          return
        }

        setMovieDetail({
          id: normalizedDetail.id || resolvedMovieCode,
          title: normalizedDetail.title || initialMovie?.title?.trim() || '영화 상세',
          poster: normalizedDetail.poster || initialMovie?.poster?.trim() || '',
          backdrop: normalizedDetail.backdrop,
          certification: normalizedDetail.certification,
          genres: normalizedDetail.genres,
          overview: normalizedDetail.overview,
          releaseDate: normalizedDetail.releaseDate,
          originCountry: normalizedDetail.originCountry,
          runtime: normalizedDetail.runtime,
          score: normalizedDetail.score,
          rating: normalizedDetail.rating,
          globalRating: normalizedDetail.globalRating,
          credits: normalizedDetail.credits,
        })
        setMessage('')
      } catch {
        setMessage('영화 정보를 불러오지 못했습니다.')
      } finally {
        setIsLoading(false)
      }
    }

    void loadMovieDetail()
  }, [initialMovie?.poster, initialMovie?.title, resolvedMovieCode])

  useEffect(() => {
    let isMounted = true

    if (!resolvedMovieCode) {
      return () => {
        isMounted = false
      }
    }

    async function loadMovieComments() {
      try {
        if (!isMounted) {
          return
        }
        setComments([])
        setCommentsPage(0)
        setHasMoreComments(true)
        setIsLoadingMoreComments(false)
        commentsPageRef.current = 0
        hasMoreCommentsRef.current = true
        isLoadingMoreCommentsRef.current = false
        await loadMovieCommentsPage(0, false, commentSortOrderRef.current)
      } catch {
        if (!isMounted) {
          return
        }

        setComments([])
        setHasMoreComments(false)
      } finally {
        if (isMounted) {
          setIsCommentsLoading(false)
        }
      }
    }

    void loadMovieComments()

    return () => {
      isMounted = false
    }
  }, [loadMovieCommentsPage, resolvedMovieCode])

  useEffect(() => {
    const mainShell = mainShellRef.current

    async function loadNextCommentsPage() {
      if (
        isCommentsLoading ||
        isLoadingMoreCommentsRef.current ||
        !hasMoreCommentsRef.current ||
        shouldWaitForCommentsScrollResetRef.current
      ) {
        return
      }

      setIsLoadingMoreComments(true)
      isLoadingMoreCommentsRef.current = true

      try {
        await loadMovieCommentsPage(commentsPageRef.current + 1, true, commentSortOrder)
      } catch {
        // no-op
      } finally {
        setIsLoadingMoreComments(false)
        isLoadingMoreCommentsRef.current = false
      }
    }

    function getShellDistanceFromBottom() {
      const currentShell = mainShellRef.current

      if (!currentShell) {
        return Number.POSITIVE_INFINITY
      }

      return currentShell.scrollHeight - (currentShell.scrollTop + currentShell.clientHeight)
    }

    function getWindowDistanceFromBottom() {
      const scrollingElement = document.scrollingElement ?? document.documentElement

      return (
        scrollingElement.scrollHeight -
        (window.scrollY + window.innerHeight)
      )
    }

    function isShellActuallyScrolling() {
      const currentShell = mainShellRef.current

      if (!currentShell) {
        return false
      }

      return currentShell.scrollHeight > currentShell.clientHeight + 1
    }

    async function handleScroll() {
      const distanceFromBottom = isShellActuallyScrolling()
        ? getShellDistanceFromBottom()
        : getWindowDistanceFromBottom()

      if (shouldWaitForCommentsScrollResetRef.current) {
        if (distanceFromBottom > 240) {
          shouldWaitForCommentsScrollResetRef.current = false
        }

        return
      }

      if (distanceFromBottom > 120) {
        return
      }

      await loadNextCommentsPage()
    }

    if (mainShell) {
      mainShell.addEventListener('scroll', handleScroll, { passive: true })
    }

    window.addEventListener('scroll', handleScroll, { passive: true })

    return () => {
      if (mainShell) {
        mainShell.removeEventListener('scroll', handleScroll)
      }

      window.removeEventListener('scroll', handleScroll)
    }
  }, [commentSortOrder, isCommentsLoading, loadMovieCommentsPage])

  async function handleLogout() {
    if (isLoggingOut) {
      return
    }

    setIsLoggingOut(true)

    try {
      await logout()
    } finally {
      clearAuthSession()
      setIsLoggingOut(false)
    }
  }

  async function handleInlineLogin(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (isLoginSubmitting) {
      return
    }

    setIsLoginSubmitting(true)
    setLoginMessage('')

    try {
      const loginResponse = await login({ loginId, password })

      if (!loginResponse) {
        throw new Error('로그인 응답 데이터가 없습니다.')
      }

      saveAuthSession(loginResponse)
      setLoginId('')
      setPassword('')
      setLoginMessage('')
    } catch (error) {
      if (isApiError(error) && error.status === 401) {
        setLoginMessage(error.message)
      } else {
        setLoginMessage('아이디 또는 비밀번호를 다시 확인해주세요.')
      }
    } finally {
      setIsLoginSubmitting(false)
    }
  }

  async function _handleCommentInputFocus() {
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
      const response = await verifyCommentAuth()
      setCanWriteComment(true)
      return response
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

  function handleOpenCommentModal() {
    setEditingCommentTarget(null)
    setCommentDraft('')
    setSelectedRating(0)
    setCommentModalMode('create')
  }

  async function handleOpenCommentModalWithAuth() {
    if (isCheckingCommentAuth) {
      return
    }

    const session = getAuthSession()

    if (!session?.accessToken) {
      setCanWriteComment(false)
      alert('로그인이 필요한 서비스입니다.')
      return
    }

    setIsCheckingCommentAuth(true)

    try {
      await verifyCommentAuth()
      setCanWriteComment(true)
      handleOpenCommentModal()
    } catch {
      setCanWriteComment(false)
      alert('로그인이 필요한 서비스입니다.')
    } finally {
      setIsCheckingCommentAuth(false)
    }
  }

  async function handleCommentSortChange(nextSortOrder: CommentSortOrder) {
    if (nextSortOrder === commentSortOrder || isCommentsLoading || isLoadingMoreComments) {
      return
    }

    shouldWaitForCommentsScrollResetRef.current = true
    commentsSectionRef.current?.scrollIntoView({
      behavior: 'auto',
      block: 'start',
    })

    setCommentSortOrder(nextSortOrder)
    setIsCommentsLoading(true)
    setCommentsPage(0)
    setHasMoreComments(true)
    setIsLoadingMoreComments(false)
    commentsPageRef.current = 0
    hasMoreCommentsRef.current = true
    isLoadingMoreCommentsRef.current = false

    try {
      await loadMovieCommentsPage(0, false, nextSortOrder)
    } finally {
      setIsCommentsLoading(false)
    }
  }

  function handleCloseCommentModal() {
    setCommentModalMode(null)
    setEditingCommentTarget(null)
  }

  function applyEditableComment(comment: MovieCommentDetail) {
    setCommentDraft(comment.content)
    setSelectedRating(comment.rating)
  }

  async function handleCommentEditClick(comment: MovieComment) {
    try {
      const targetMovieId = comment.movieId || resolvedMovieRecordId
      const targetCommentId = comment.commentId || comment.id

      if (!targetMovieId || !targetCommentId) {
        return
      }

      const detail = await fetchMovieCommentForEdit(targetMovieId, targetCommentId)

      if (!detail) {
        return
      }

      setEditingCommentTarget({
        movieId: targetMovieId,
        commentId: targetCommentId,
      })
      setCanWriteComment(true)
      applyEditableComment(detail)
      setCommentModalMode('edit')
    } catch {
      // no-op
    }
  }

  async function handleCommentDeleteClick(comment: MovieComment) {
    const targetMovieId = comment.movieId || resolvedMovieRecordId
    const targetCommentId = comment.commentId || comment.id

    if (!targetMovieId || !targetCommentId) {
      return
    }

    const isConfirmed = window.confirm('정말 삭제하시겠습니까?')

    if (!isConfirmed) {
      return
    }

    try {
      await deleteMovieComment(resolvedMovieCode, targetCommentId, {
        movieId: targetMovieId,
      })

      setComments((previousComments) =>
        previousComments.filter((previousComment) => previousComment.id !== comment.id),
      )
    } catch {
      // no-op
    }
  }

  async function handleCommentSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const session = getAuthSession()

    if (!session?.nickname || ((!canWriteComment && !isEditMode) || isSubmittingComment)) {
      return
    }

    if (isCreateMode && selectedRating === 0) {
      alert('평점을 선택해주세요.')
      return
    }

    if (!isCommentLengthValid) {
      alert('코멘트는 1자 이상 300자 이하로 작성해주세요.')
      return
    }

    if (isEditMode) {
      if (!editingCommentTarget?.movieId || !editingCommentTarget?.commentId) {
        return
      }

      setIsSubmittingComment(true)

      try {
        await updateMovieComment(resolvedMovieCode, editingCommentTarget.commentId, {
          movieId: editingCommentTarget.movieId || movieDetail.id,
          content: trimmedCommentDraft,
          reactionType: selectedReactionType,
        })

        setCommentDraft('')
        setSelectedRating(0)
        setCommentModalMode(null)
        setEditingCommentTarget(null)
        setIsCommentsLoading(true)

        try {
          await loadMovieCommentsPage(0, false, commentSortOrder)
        } finally {
          setIsCommentsLoading(false)
        }
      } finally {
        setIsSubmittingComment(false)
      }

      return
    }

    setIsSubmittingComment(true)

    try {
      if (!resolvedMovieRecordId) {
        return
      }

      await createMovieComment(resolvedMovieCode, {
        movieId: resolvedMovieRecordId,
        nickname: session.nickname,
        content: trimmedCommentDraft,
        reactionType: selectedReactionType,
      })

      setCommentDraft('')
      setSelectedRating(0)
      setCommentModalMode(null)
      setIsCommentsLoading(true)

      try {
        await loadMovieCommentsPage(0, false, commentSortOrder)
      } finally {
        setIsCommentsLoading(false)
      }
    } catch (error) {
      if (error instanceof ApiError && error.status === 409) {
        alert('이미 이 영화에 코멘트를 작성했습니다.')
      }
    } finally {
      setIsSubmittingComment(false)
    }
  }

  const visibleMessage =
    !isLoading && message ? message : !isLoading && !movieDetail.backdrop ? '배경 이미지가 없습니다.' : ''

  return (
    <div className={`home-page home-page-${theme} movie-detail-screen`}>
      <div className="home-desktop-container">
        <aside className="home-sidebar movie-detail-sidebar" aria-label="메인 내비게이션">
          <div className="home-brand-block">
            <p className="home-brand-mark">MP</p>
            <div className="home-brand-copy">
              <strong>Movie Pedia</strong>
              <span>당신의 영화 취향</span>
            </div>
          </div>

          <nav className="home-nav">
            {PRIMARY_NAV_ITEMS.map((item, index) => (
              <button
                className={`home-nav-item${index === 0 ? ' home-nav-item-active' : ''}`}
                type="button"
                key={item}
                onClick={() => navigate('/')}
              >
                <span>{item}</span>
              </button>
            ))}
          </nav>
        </aside>

        <main className="home-main-shell movie-detail-main-shell" ref={mainShellRef}>
          <div className="movie-detail-page">
            <section className="movie-detail-visual-section">
              <MovieDetailHero
              movieDetail={movieDetail}
              isLoading={isLoading}
              message={visibleMessage}
              onBack={() => navigate('/')}
              />
            </section>

            <div className="movie-detail-content-area">
              <section className="movie-detail-detail-section">
                <MovieDetailCredits
                  key={movieDetail.id || resolvedMovieCode}
                  credits={movieDetail.credits}
                />
              </section>

              <section
                className="movie-detail-comments-shell"
                aria-labelledby="movie-detail-comments-title"
                ref={commentsSectionRef}
              >
              <div className="movie-detail-comments">
                <div className="movie-detail-section-header">
                  <h2 id="movie-detail-comments-title">코멘트</h2>
                  <div className="movie-detail-comments-header-actions">
                    <button
                      className="movie-detail-comment-open-button"
                      type="button"
                      onClick={() => {
                        void handleOpenCommentModalWithAuth()
                      }}
                    >
                      코멘트 남기기
                    </button>
                  </div>
                </div>

                <div className="movie-detail-comments-sort-row" aria-label="코멘트 정렬">
                  <button
                    className={`movie-detail-comments-sort-button${
                      commentSortOrder === 'latest' ? ' is-active' : ''
                    }`}
                    type="button"
                    onClick={() => {
                      void handleCommentSortChange('latest')
                    }}
                  >
                    최신순
                  </button>
                  <button
                    className={`movie-detail-comments-sort-button${
                      commentSortOrder === 'oldest' ? ' is-active' : ''
                    }`}
                    type="button"
                    onClick={() => {
                      void handleCommentSortChange('oldest')
                    }}
                  >
                    오래된순
                  </button>
                </div>

                <MovieCommentList
                  comments={comments}
                  isLoading={isCommentsLoading}
                  onEditClick={handleCommentEditClick}
                  onDeleteClick={handleCommentDeleteClick}
                />
              </div>
              </section>
            </div>
          </div>
        </main>

        <aside className="home-auth-panel movie-detail-auth-panel">
          <div className="home-auth-card">
            {authSession ? (
              <div className="home-auth-session">
                <p className="home-auth-session-nickname">{authSession.nickname ?? '사용자'}</p>
                <button
                  className="home-auth-logout-button"
                  type="button"
                  onClick={handleLogout}
                  disabled={isLoggingOut}
                >
                  {isLoggingOut ? '로그아웃 중...' : '로그아웃'}
                </button>
              </div>
            ) : (
              <form className="home-inline-login" onSubmit={handleInlineLogin} noValidate>
                <input
                  className="home-inline-login-input"
                  type="text"
                  placeholder="아이디"
                  autoComplete="username"
                  value={loginId}
                  onChange={(event) => setLoginId(event.target.value)}
                />
                <input
                  className="home-inline-login-input"
                  type="password"
                  placeholder="비밀번호"
                  autoComplete="current-password"
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                />
                {loginMessage ? <p className="home-inline-login-message">{loginMessage}</p> : null}
                <div className="home-inline-login-actions">
                  <button
                    className="home-signup-button"
                    type="button"
                    onClick={() => navigate('/signup')}
                  >
                    회원가입
                  </button>
                  <button className="home-auth-button" type="submit" disabled={isLoginSubmitting}>
                    {isLoginSubmitting ? '로그인 중...' : '로그인'}
                  </button>
                </div>
                <div className="home-social-login-divider" aria-hidden="true" />
                <a
                  className="home-social-login-button"
                  href="http://localhost:8080/oauth2/authorization/naver"
                >
                  <img
                    className="home-social-login-button-image"
                    src={naverLoginButtonImage}
                    alt="네이버로 로그인"
                  />
                </a>
              </form>
            )}
          </div>
        </aside>
      </div>

      <button
        className="home-theme-toggle"
        type="button"
        onClick={() => setTheme((currentTheme) => (currentTheme === 'dark' ? 'light' : 'dark'))}
        aria-label={theme === 'dark' ? '라이트 모드로 전환' : '다크 모드로 전환'}
        title={theme === 'dark' ? '라이트 모드' : '다크 모드'}
      >
        <img
          className="home-theme-toggle-icon"
          src={theme === 'dark' ? lightModeIcon : darkModeIcon}
          alt=""
          aria-hidden="true"
        />
      </button>

      {isCommentModalOpen ? (
        <MovieCommentModal
          title={movieDetail.title}
          commentDraft={commentDraft}
          selectedRating={selectedRating}
          showReactionSelector={!isEditMode}
          canWriteComment={isEditMode || canWriteComment}
          isSubmittingComment={isSubmittingComment}
          isCheckingCommentAuth={isCheckingCommentAuth}
          submitLabel={commentModalSubmitLabel}
          commentInputRef={commentInputRef}
          onClose={handleCloseCommentModal}
          onCommentDraftChange={setCommentDraft}
          onSelectedRatingChange={setSelectedRating}
          onSubmit={handleCommentSubmit}
        />
      ) : null}

    </div>
  )
}

export default MovieDetailPage
