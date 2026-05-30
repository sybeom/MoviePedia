import thumbsUpIcon from '../../assets/icons/thumbs_up.svg'
import thumbsDownIcon from '../../assets/icons/thumbs_down.svg'
import type { FormEvent, RefObject } from 'react'
import type { AuthMeResponse } from '../../types/movieDetail'
import { MAX_COMMENT_LENGTH } from '../../utils/movieDetail'

type MovieCommentFormProps = {
  commentDraft: string
  selectedRating: number
  canWriteComment: boolean
  isSubmittingComment: boolean
  isCheckingCommentAuth: boolean
  submitLabel: string
  commentInputRef: RefObject<HTMLTextAreaElement | null>
  onCommentDraftChange: (value: string) => void
  onSelectedRatingChange: (rating: number) => void
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
  onCommentFocus: () => Promise<AuthMeResponse | void>
}

function MovieCommentForm({
  commentDraft,
  selectedRating,
  canWriteComment,
  isSubmittingComment,
  isCheckingCommentAuth,
  submitLabel,
  commentInputRef,
  onCommentDraftChange,
  onSelectedRatingChange,
  onSubmit,
  onCommentFocus,
}: MovieCommentFormProps) {
  const canClickCommentSubmit = canWriteComment && !isSubmittingComment
  const isThumbsUpActive = selectedRating > 0
  const isThumbsDownActive = selectedRating < 0

  return (
    <>
      <div className="movie-detail-comment-feedback-shell" aria-label="코멘트 반응 선택">
        <div
          className={`movie-detail-comment-feedback-button${
            isThumbsUpActive ? ' is-active' : ' is-inactive'
          }`}
          role="button"
          tabIndex={0}
          aria-pressed={isThumbsUpActive}
          onClick={() => onSelectedRatingChange(1)}
          onKeyDown={(event) => {
            if (event.key === 'Enter' || event.key === ' ') {
              event.preventDefault()
              onSelectedRatingChange(1)
            }
          }}
        >
          <img
            className="movie-detail-comment-feedback-icon"
            src={thumbsUpIcon}
            alt=""
            aria-hidden="true"
          />
        </div>
        <div
          className={`movie-detail-comment-feedback-button movie-detail-comment-feedback-button-down${
            isThumbsDownActive ? ' is-active' : ' is-inactive'
          }`}
          role="button"
          tabIndex={0}
          aria-pressed={isThumbsDownActive}
          onClick={() => onSelectedRatingChange(-1)}
          onKeyDown={(event) => {
            if (event.key === 'Enter' || event.key === ' ') {
              event.preventDefault()
              onSelectedRatingChange(-1)
            }
          }}
        >
          <img
            className="movie-detail-comment-feedback-icon"
            src={thumbsDownIcon}
            alt=""
            aria-hidden="true"
          />
        </div>
      </div>

      <form className="movie-detail-comment-form" onSubmit={onSubmit}>
        <label className="sr-only" htmlFor="movie-detail-comment-input">
          코멘트 입력
        </label>
        <div className="movie-detail-comment-input-shell">
          <textarea
            ref={commentInputRef}
            id="movie-detail-comment-input"
            className="movie-detail-comment-input"
            maxLength={MAX_COMMENT_LENGTH}
            readOnly={!canWriteComment}
            value={commentDraft}
            onChange={(event) => onCommentDraftChange(event.target.value)}
            onFocus={() => {
              void onCommentFocus()
            }}
            placeholder="감상한 영화에 대해 자유롭게 평가해보세요!"
          />
        </div>
        <div className="movie-detail-comment-footer">
          <p className="movie-detail-comment-count">{`${commentDraft.length}/${MAX_COMMENT_LENGTH}`}</p>
          <button
            className="movie-detail-comment-submit"
            type="submit"
            disabled={!canClickCommentSubmit || isCheckingCommentAuth}
          >
            {isSubmittingComment ? `${submitLabel} 중` : submitLabel}
          </button>
        </div>
      </form>
    </>
  )
}

export default MovieCommentForm
