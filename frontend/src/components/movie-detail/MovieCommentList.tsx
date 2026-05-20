import { STAR_ICON_PATH } from '../../utils/movieDetail'
import type { MovieComment } from '../../types/movieDetail'

type MovieCommentListProps = {
  comments: MovieComment[]
  isLoading: boolean
  onCommentClick: (comment: MovieComment) => void
  onEditClick: (comment: MovieComment) => void
}

// 코멘트 목록 영역 구성
function MovieCommentList({
  comments,
  isLoading,
  onCommentClick,
  onEditClick,
}: MovieCommentListProps) {
  return (
    <div className="movie-detail-comment-list">
      {isLoading ? (
        <p className="movie-detail-comment-list-message">코멘트를 불러오는 중입니다...</p>
      ) : comments.length > 0 ? (
        comments.map((comment) => (
          <article
            className="movie-detail-comment-card"
            key={comment.id}
            role="button"
            tabIndex={0}
            onClick={() => onCommentClick(comment)}
            onKeyDown={(event) => {
              if (event.key === 'Enter' || event.key === ' ') {
                event.preventDefault()
                onCommentClick(comment)
              }
            }}
          >
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
            <div className="movie-detail-comment-card-footer">
              <button
                className="movie-detail-comment-like-button"
                type="button"
                onClick={(event) => {
                  event.stopPropagation()
                }}
              >
                좋아요
              </button>
              {comment.isMine ? (
                <div className="movie-detail-comment-card-owner-actions">
                  <button
                    className="movie-detail-comment-owner-button"
                    type="button"
                    onClick={(event) => {
                      event.stopPropagation()
                      onEditClick(comment)
                    }}
                  >
                    수정
                  </button>
                  <button
                    className="movie-detail-comment-owner-button"
                    type="button"
                    onClick={(event) => {
                      event.stopPropagation()
                    }}
                  >
                    삭제
                  </button>
                </div>
              ) : null}
            </div>
          </article>
        ))
      ) : (
        <p className="movie-detail-comment-list-message">아직 등록된 코멘트가 없습니다.</p>
      )}
    </div>
  )
}

export default MovieCommentList
