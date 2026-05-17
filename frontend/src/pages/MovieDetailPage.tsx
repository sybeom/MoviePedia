import { useEffect, useRef, useState, type FormEvent } from 'react'
import { useLocation, useParams } from 'react-router-dom'
import { ApiError } from '../api/client'
import { createMovieComment, fetchMovieComments, fetchMovieDetail, verifyCommentAuth } from '../api/movieDetail'
import Header from '../components/Header'
import MovieCommentForm from '../components/movie-detail/MovieCommentForm'
import MovieCommentList from '../components/movie-detail/MovieCommentList'
import MovieDetailCredits from '../components/movie-detail/MovieDetailCredits'
import MovieDetailHero from '../components/movie-detail/MovieDetailHero'
import MovieDetailRatings from '../components/movie-detail/MovieDetailRatings'
import { getAuthSession } from '../utils/authStorage'
import { isAuthSessionError } from '../utils/fetchUtil'
import { MAX_COMMENT_LENGTH, createInitialMovieDetail } from '../utils/movieDetail'
import type { MovieComment, MovieDetailState, MovieDetailView } from '../types/movieDetail'
import './MovieDetailPage.css'

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

  // 상세 요청 중복 방지 참조 준비
  const hasLoadedDetailRef = useRef(false)

  // 코멘트 입력창 참조 준비
  const commentInputRef = useRef<HTMLTextAreaElement | null>(null)

  const trimmedCommentDraft = commentDraft.trim()
  const isCommentLengthValid =
    trimmedCommentDraft.length >= 1 && trimmedCommentDraft.length <= MAX_COMMENT_LENGTH

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

    // 영화 상세 조회 실행 처리
    async function loadMovieDetail() {
      try {
        const normalizedDetail = await fetchMovieDetail(resolvedMovieId)

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

    void loadMovieDetail()
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
    async function loadMovieComments() {
      try {
        const response = await fetchMovieComments(resolvedMovieId)

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

    void loadMovieComments()

    return () => {
      isMounted = false
    }
  }, [resolvedMovieId])

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

  // 코멘트 제출 기본 동작 방지 처리
  async function handleCommentSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const session = getAuthSession()

    // 로그인 세션 존재 여부 확인 처리
    if (!session?.nickname || !canWriteComment || isSubmittingComment) {
      return
    }

    // 평점 선택 여부 확인 처리
    if (selectedRating <= 0) {
      alert('평점을 선택해주세요.')
      return
    }

    // 코멘트 글자 수 조건 확인 처리
    if (!isCommentLengthValid) {
      alert('코멘트는 1자 이상 100자 이하로 작성해주세요.')
      return
    }

    setIsSubmittingComment(true)

    try {
      await createMovieComment(resolvedMovieId, {
        nickname: session.nickname,
        content: trimmedCommentDraft,
        rating: selectedRating,
      })

      setCommentDraft('')
      setSelectedRating(0)
      setHoverRating(0)
      setIsCommentsLoading(true)

      try {
        const commentsResponse = await fetchMovieComments(resolvedMovieId)
        setComments(commentsResponse)
      } finally {
        setIsCommentsLoading(false)
      }
    } catch (error) {
      // 중복 코멘트 작성 응답 분기 처리
      if (error instanceof ApiError && error.status === 409) {
        alert('이미 이 영화에 코멘트를 작성했습니다.')
      }
    } finally {
      setIsSubmittingComment(false)
    }
  }

  return (
    <div className="app">
      <Header showAuthActions transparentOnTop textOnlyAuthAction />

      <main className="movie-detail-page">
        <MovieDetailHero movieDetail={movieDetail} isLoading={isLoading} message={message} />

        <MovieDetailCredits credits={movieDetail.credits} />

        <MovieDetailRatings globalRating={movieDetail.globalRating} />

        <section className="movie-detail-comments-shell" aria-labelledby="movie-detail-comments-title">
          <div className="movie-detail-comments">
            <h2 id="movie-detail-comments-title">한줄 코멘트</h2>

            <MovieCommentForm
              commentDraft={commentDraft}
              selectedRating={selectedRating}
              hoverRating={hoverRating}
              canWriteComment={canWriteComment}
              isSubmittingComment={isSubmittingComment}
              isCheckingCommentAuth={isCheckingCommentAuth}
              commentInputRef={commentInputRef}
              onCommentDraftChange={setCommentDraft}
              onSelectedRatingChange={setSelectedRating}
              onHoverRatingChange={setHoverRating}
              onSubmit={handleCommentSubmit}
              onCommentFocus={handleCommentInputFocus}
            />

            <MovieCommentList comments={comments} isLoading={isCommentsLoading} />
          </div>
        </section>
      </main>
    </div>
  )
}

export default MovieDetailPage
