import type { FormEvent, RefObject } from 'react'
import type { AuthMeResponse } from '../../types/movieDetail'
import StarRating from './StarRating'
import { MAX_COMMENT_LENGTH } from '../../utils/movieDetail'

type MovieCommentFormProps = {
  commentDraft: string
  selectedRating: number
  hoverRating: number
  canWriteComment: boolean
  isSubmittingComment: boolean
  isCheckingCommentAuth: boolean
  commentInputRef: RefObject<HTMLTextAreaElement | null>
  onCommentDraftChange: (value: string) => void
  onSelectedRatingChange: (rating: number) => void
  onHoverRatingChange: (rating: number) => void
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
  onCommentFocus: () => Promise<AuthMeResponse | void>
}

// 코멘트 작성 영역 구성
function MovieCommentForm({
  commentDraft,
  selectedRating,
  hoverRating,
  canWriteComment,
  isSubmittingComment,
  isCheckingCommentAuth,
  commentInputRef,
  onCommentDraftChange,
  onSelectedRatingChange,
  onHoverRatingChange,
  onSubmit,
  onCommentFocus,
}: MovieCommentFormProps) {
  const canClickCommentSubmit = canWriteComment && !isSubmittingComment

  return (
    <>
      <StarRating
        selectedRating={selectedRating}
        hoverRating={hoverRating}
        onHoverRatingChange={onHoverRatingChange}
        onSelectedRatingChange={onSelectedRatingChange}
      />

      <form className="movie-detail-comment-form" onSubmit={onSubmit}>
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
            onChange={(event) => onCommentDraftChange(event.target.value)}
            onFocus={() => {
              void onCommentFocus()
            }}
            placeholder="영화에 대한 솔직한 평가를 남겨보세요!"
          />
        </div>
        <div className="movie-detail-comment-footer">
          <p className="movie-detail-comment-count">{`${commentDraft.length}/${MAX_COMMENT_LENGTH}`}</p>
          <button className="movie-detail-comment-submit" type="submit" disabled={!canClickCommentSubmit || isCheckingCommentAuth}>
            {isSubmittingComment ? '등록 중' : '작성'}
          </button>
        </div>
      </form>
    </>
  )
}

export default MovieCommentForm
