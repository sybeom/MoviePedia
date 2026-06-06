import type { MovieComment } from '../../types/movieDetail'
import thumbsUpIcon from '../../assets/icons/thumbs_up.svg'
import thumbsDownIcon from '../../assets/icons/thumbs_down.svg'

type MovieCommentListProps = {
  comments: MovieComment[]
  isLoading: boolean
  onEditClick: (comment: MovieComment) => void
  onDeleteClick: (comment: MovieComment) => void
}

function MovieCommentList({
  comments,
  isLoading,
  onEditClick,
  onDeleteClick,
}: MovieCommentListProps) {
  return (
    <div className="movie-detail-comment-list">
      {isLoading && comments.length === 0 ? (
        <p className="movie-detail-comment-list-message">코멘트를 불러오는 중입니다...</p>
      ) : comments.length > 0 ? (
        comments.map((comment) => (
          <article className="movie-detail-comment-card" key={comment.id}>
            <div className="movie-detail-comment-card-side">
              <span className="movie-detail-comment-card-avatar" aria-hidden="true">
                {comment.nickname.slice(0, 1) || '?'}
              </span>
              <div className="movie-detail-comment-card-side-copy">
                <p className="movie-detail-comment-card-side-name">{comment.nickname}</p>
              </div>
              {comment.writtenByMe ? (
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
                      onDeleteClick(comment)
                    }}
                  >
                    삭제
                  </button>
                </div>
              ) : null}
            </div>

            <div className="movie-detail-comment-card-main">
              <header className="movie-detail-comment-card-main-header">
                <div className="movie-detail-comment-card-reaction-banner">
                  <img
                    className="movie-detail-comment-card-thumb"
                    src={comment.reactionType === 'DISLIKE' ? thumbsDownIcon : thumbsUpIcon}
                    alt=""
                    aria-hidden="true"
                  />
                  <div className="movie-detail-comment-card-reaction-copy">
                    <p className="movie-detail-comment-card-title">
                      {comment.reactionType === 'DISLIKE' ? '비추천' : '추천'}
                    </p>
                    <p className="movie-detail-comment-card-subtitle">
                      {comment.createdAt || '코멘트 기록'}
                    </p>
                  </div>
                </div>
              </header>
              <p className="movie-detail-comment-card-content">{comment.content}</p>
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
