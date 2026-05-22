import { useMemo, useState } from 'react'
import { STAR_ICON_PATH } from '../../utils/movieDetail'
import type { MovieComment } from '../../types/movieDetail'
import likeIcon from '../../assets/icons/like.svg'

type MovieCommentListProps = {
  comments: MovieComment[]
  isLoading: boolean
  onCommentClick: (comment: MovieComment) => void
  onEditClick: (comment: MovieComment) => void
  onLikeClick: (comment: MovieComment) => Promise<boolean>
}

// 코멘트 목록 영역 구성
function MovieCommentList({
  comments,
  isLoading,
  onCommentClick,
  onEditClick,
  onLikeClick,
}: MovieCommentListProps) {
  // 좋아요 추가 활성화 상태 관리
  const [likedCommentIds, setLikedCommentIds] = useState<Record<string, boolean>>({})
  const [likingCommentIds, setLikingCommentIds] = useState<Record<string, boolean>>({})

  // 코멘트 목록 기준 좋아요 수 계산
  const baseLikeCounts = useMemo(
    () =>
      comments.reduce<Record<string, number>>((accumulator, comment) => {
        const parsedLikeCount = Number(comment.likeCount)

        accumulator[comment.id] = Number.isFinite(parsedLikeCount) ? parsedLikeCount : 0
        return accumulator
      }, {}),
    [comments],
  )

  // 좋아요 토글 처리
  async function handleLikeClick(comment: MovieComment) {
    const commentId = comment.id
    const isCurrentlyLiked = (likedCommentIds[commentId] ?? false) || comment.likedByMe
    const isCurrentlyLiking = likingCommentIds[commentId] ?? false

    if (isCurrentlyLiked || isCurrentlyLiking) {
      return
    }

    setLikingCommentIds((previousLikingCommentIds) => ({
      ...previousLikingCommentIds,
      [commentId]: true,
    }))

    try {
      const isSuccess = await onLikeClick(comment)

      if (!isSuccess) {
        return
      }

      setLikedCommentIds((previousLikedCommentIds) => ({
        ...previousLikedCommentIds,
        [commentId]: true,
      }))
    } finally {
      setLikingCommentIds((previousLikingCommentIds) => ({
        ...previousLikingCommentIds,
        [commentId]: false,
      }))
    }
  }

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
              {(() => {
                const isLiked = (likedCommentIds[comment.id] ?? false) || comment.likedByMe
                const isLiking = likingCommentIds[comment.id] ?? false
                const displayedLikeCount = Math.max(
                  0,
                  (baseLikeCounts[comment.id] ?? 0) +
                    (likedCommentIds[comment.id] && !comment.likedByMe ? 1 : 0),
                )

                return (
                  <button
                    className={`movie-detail-comment-like-button${
                      isLiked ? ' is-active' : ''
                    }`}
                    type="button"
                    disabled={isLiked || isLiking}
                    onClick={(event) => {
                      event.stopPropagation()
                      void handleLikeClick(comment)
                    }}
                  >
                    <img src={likeIcon} alt="" aria-hidden="true" />
                    <span>{displayedLikeCount}</span>
                  </button>
                )
              })()}
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
