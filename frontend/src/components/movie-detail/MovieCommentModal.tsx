import { useEffect, useRef, type FormEvent, type RefObject } from 'react'
import MovieCommentForm from './MovieCommentForm'
import type { AuthMeResponse } from '../../types/movieDetail'

type MovieCommentModalProps = {
  title: string
  commentDraft: string
  selectedRating: number
  hoverRating: number
  canWriteComment: boolean
  isSubmittingComment: boolean
  isCheckingCommentAuth: boolean
  submitLabel: string
  commentInputRef: RefObject<HTMLTextAreaElement | null>
  onClose: () => void
  onCommentDraftChange: (value: string) => void
  onSelectedRatingChange: (rating: number) => void
  onHoverRatingChange: (rating: number) => void
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
  onCommentFocus: () => Promise<AuthMeResponse | void>
}

// 코멘트 작성 및 수정 모달 구성
function MovieCommentModal({
  title,
  commentDraft,
  selectedRating,
  hoverRating,
  canWriteComment,
  isSubmittingComment,
  isCheckingCommentAuth,
  submitLabel,
  commentInputRef,
  onClose,
  onCommentDraftChange,
  onSelectedRatingChange,
  onHoverRatingChange,
  onSubmit,
  onCommentFocus,
}: MovieCommentModalProps) {
  const isEditMode = submitLabel === '수정'

  // 모달 내부 클릭 감지용 참조 준비
  const modalRef = useRef<HTMLDivElement | null>(null)

  // 모달 외부 클릭 시 닫기 처리
  useEffect(() => {
    function handlePointerDown(event: MouseEvent | TouchEvent) {
      const target = event.target

      if (!(target instanceof Node)) {
        return
      }

      if (modalRef.current?.contains(target)) {
        return
      }

      onClose()
    }

    document.addEventListener('mousedown', handlePointerDown)
    document.addEventListener('touchstart', handlePointerDown)

    return () => {
      document.removeEventListener('mousedown', handlePointerDown)
      document.removeEventListener('touchstart', handlePointerDown)
    }
  }, [onClose])

  return (
    <div className="movie-detail-comment-modal-layer" role="presentation">
      <div className="movie-detail-comment-modal-backdrop" aria-hidden="true" />
      <div
        ref={modalRef}
        className={`movie-detail-comment-modal${isEditMode ? ' movie-detail-comment-modal-detail' : ''}`}
        role="dialog"
        aria-modal="true"
        aria-labelledby="movie-detail-comment-modal-title"
      >
        <div className="movie-detail-comment-modal-header">
          <div className="movie-detail-comment-modal-copy">
            <h3 id="movie-detail-comment-modal-title">{title}</h3>
            {!isEditMode ? <p>감상한 영화에 대해 자유롭게 평가해보세요!</p> : null}
          </div>
          <button
            className="movie-detail-comment-modal-close"
            type="button"
            aria-label="코멘트 모달 닫기"
            onClick={onClose}
          >
            ×
          </button>
        </div>

        <MovieCommentForm
          commentDraft={commentDraft}
          selectedRating={selectedRating}
          hoverRating={hoverRating}
          canWriteComment={canWriteComment}
          isSubmittingComment={isSubmittingComment}
          isCheckingCommentAuth={isCheckingCommentAuth}
          submitLabel={submitLabel}
          commentInputRef={commentInputRef}
          onCommentDraftChange={onCommentDraftChange}
          onSelectedRatingChange={onSelectedRatingChange}
          onHoverRatingChange={onHoverRatingChange}
          onSubmit={onSubmit}
          onCommentFocus={onCommentFocus}
        />
      </div>
    </div>
  )
}

export default MovieCommentModal
