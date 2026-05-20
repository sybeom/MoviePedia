import { useEffect, useRef } from 'react'
import type { MovieCommentDetail } from '../../types/movieDetail'
import { STAR_ICON_PATH, getSelectedRatingLabel } from '../../utils/movieDetail'

type MovieCommentDetailModalProps = {
  title: string
  comment: MovieCommentDetail
  onClose: () => void
  onEdit: () => void
}

// 코멘트 상세 모달 구성
function MovieCommentDetailModal({ title, comment, onClose, onEdit }: MovieCommentDetailModalProps) {
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
        className="movie-detail-comment-modal movie-detail-comment-modal-detail"
        role="dialog"
        aria-modal="true"
        aria-labelledby="movie-detail-comment-detail-modal-title"
      >
        <div className="movie-detail-comment-modal-header">
          <div className="movie-detail-comment-modal-copy">
            <div className="movie-detail-comment-detail-title-row">
              <h3 id="movie-detail-comment-detail-modal-title">{title}</h3>
              <div className="movie-detail-comment-detail-rating">
                <span className="movie-detail-comment-detail-rating-star" aria-hidden="true">
                  <svg viewBox="0 0 24 24" focusable="false" aria-hidden="true">
                    <path d={STAR_ICON_PATH} />
                  </svg>
                </span>
                <span>{getSelectedRatingLabel(comment.rating)}</span>
              </div>
            </div>
            <p>{comment.nickname}</p>
          </div>
          <button
            className="movie-detail-comment-modal-close"
            type="button"
            aria-label="코멘트 상세 닫기"
            onClick={onClose}
          >
            ×
          </button>
        </div>

        <div className="movie-detail-comment-detail-body">
          <div className="movie-detail-comment-card-divider" aria-hidden="true" />

          <div className="movie-detail-comment-detail-content-shell">
            <p className="movie-detail-comment-detail-content">{comment.content || '-'}</p>
          </div>

          <div className="movie-detail-comment-detail-footer">
            <span className="movie-detail-comment-detail-like">좋아요 {comment.likeCount || '0'}</span>
            {comment.isMine ? (
              <button
                className="movie-detail-comment-owner-button"
                type="button"
                onClick={onEdit}
              >
                수정
              </button>
            ) : null}
          </div>
        </div>
      </div>
    </div>
  )
}

export default MovieCommentDetailModal
