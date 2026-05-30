import type { FormEvent, RefObject } from 'react'
import MovieCommentForm from './MovieCommentForm'
import type { AuthMeResponse } from '../../types/movieDetail'

type MovieCommentModalProps = {
  title: string
  commentDraft: string
  selectedRating: number
  canWriteComment: boolean
  isSubmittingComment: boolean
  isCheckingCommentAuth: boolean
  submitLabel: string
  commentInputRef: RefObject<HTMLTextAreaElement | null>
  onClose: () => void
  onCommentDraftChange: (value: string) => void
  onSelectedRatingChange: (rating: number) => void
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
  onCommentFocus: () => Promise<AuthMeResponse | void>
}

// 코멘트 작성 및 수정 모달 구성
function MovieCommentModal({
  title,
  commentDraft,
  selectedRating,
  canWriteComment,
  isSubmittingComment,
  isCheckingCommentAuth,
  submitLabel,
  commentInputRef,
  onClose,
  onCommentDraftChange,
  onSelectedRatingChange,
  onSubmit,
  onCommentFocus,
}: MovieCommentModalProps) {
  return (
    <div className="movie-detail-comment-modal-layer" role="presentation">
      <button
        className="movie-detail-comment-modal-backdrop"
        type="button"
        aria-label="코멘트 모달 닫기"
        onClick={onClose}
      />
      <div
        className="movie-detail-comment-modal movie-detail-comment-modal-detail"
        role="dialog"
        aria-modal="true"
        aria-labelledby="movie-detail-comment-modal-title"
        onClick={(event) => {
          event.stopPropagation()
        }}
      >
        <div className="movie-detail-comment-modal-header">
          <div className="movie-detail-comment-modal-copy">
            <h3 id="movie-detail-comment-modal-title">{title}</h3>
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
          canWriteComment={canWriteComment}
          isSubmittingComment={isSubmittingComment}
          isCheckingCommentAuth={isCheckingCommentAuth}
          submitLabel={submitLabel}
          commentInputRef={commentInputRef}
          onCommentDraftChange={onCommentDraftChange}
          onSelectedRatingChange={onSelectedRatingChange}
          onSubmit={onSubmit}
          onCommentFocus={onCommentFocus}
        />
      </div>
    </div>
  )
}

export default MovieCommentModal
