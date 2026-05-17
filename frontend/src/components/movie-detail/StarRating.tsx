import { STAR_COUNT, STAR_ICON_PATH, getSelectedRatingLabel, getStarFillPercent } from '../../utils/movieDetail'

type StarRatingProps = {
  selectedRating: number
  hoverRating: number
  onHoverRatingChange: (rating: number) => void
  onSelectedRatingChange: (rating: number) => void
}

// 별점 선택 영역 구성
function StarRating({
  selectedRating,
  hoverRating,
  onHoverRatingChange,
  onSelectedRatingChange,
}: StarRatingProps) {
  const displayedRating = hoverRating || selectedRating

  return (
    <div className="movie-detail-comment-rating-shell">
      <div
        className="movie-detail-rating-stars"
        onMouseLeave={() => onHoverRatingChange(0)}
        aria-label={`선택한 별점 ${getSelectedRatingLabel(selectedRating)}`}
      >
        {Array.from({ length: STAR_COUNT }, (_, index) => {
          const fillPercent = getStarFillPercent(index, displayedRating)

          return (
            <div className="movie-detail-rating-star-shell" key={`rating-star-${index + 1}`}>
              <span className="movie-detail-rating-star-base" aria-hidden="true">
                <svg viewBox="0 0 24 24" focusable="false" aria-hidden="true">
                  <path d={STAR_ICON_PATH} />
                </svg>
              </span>
              <span
                className="movie-detail-rating-star-fill"
                aria-hidden="true"
                style={{ clipPath: `inset(0 ${100 - fillPercent}% 0 0)` }}
              >
                <svg viewBox="0 0 24 24" focusable="false" aria-hidden="true">
                  <path d={STAR_ICON_PATH} />
                </svg>
              </span>
              <button
                className="movie-detail-rating-star-hit movie-detail-rating-star-hit-left"
                type="button"
                aria-label={`${index + 0.5}점 선택`}
                onMouseEnter={() => onHoverRatingChange(index + 0.5)}
                onFocus={() => onHoverRatingChange(index + 0.5)}
                onClick={() => onSelectedRatingChange(index + 0.5)}
              />
              <button
                className="movie-detail-rating-star-hit movie-detail-rating-star-hit-right"
                type="button"
                aria-label={`${index + 1}점 선택`}
                onMouseEnter={() => onHoverRatingChange(index + 1)}
                onFocus={() => onHoverRatingChange(index + 1)}
                onClick={() => onSelectedRatingChange(index + 1)}
              />
            </div>
          )
        })}
        {displayedRating > 0 ? (
          <span className="movie-detail-rating-value movie-detail-rating-value-inline">
            {getSelectedRatingLabel(displayedRating)}
          </span>
        ) : null}
      </div>
    </div>
  )
}

export default StarRating
