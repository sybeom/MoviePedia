import { STAR_ICON_PATH, getDisplayRatingWithScale } from '../../utils/movieDetail'

type MovieDetailRatingsProps = {
  rating: string
  globalRating: string
}

// 평점 영역 구성
function MovieDetailRatings({ rating, globalRating }: MovieDetailRatingsProps) {
  return (
    <section className="movie-detail-ratings-shell" aria-label="영화 평점">
      <div className="movie-detail-ratings-section">
        <h2 className="movie-detail-ratings-title">평점</h2>
        <div className="movie-detail-ratings">
          <article className="movie-detail-rating-card">
            <p className="movie-detail-rating-label">피디아</p>
            <div className="movie-detail-rating-display">
              <span className="movie-detail-rating-star-icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" focusable="false" aria-hidden="true">
                  <path d={STAR_ICON_PATH} />
                </svg>
              </span>
              <p className="movie-detail-rating-value">{getDisplayRatingWithScale(rating, 5)}</p>
            </div>
          </article>

          <article className="movie-detail-rating-card">
            <p className="movie-detail-rating-label">글로벌</p>
            <div className="movie-detail-rating-display">
              <span className="movie-detail-rating-star-icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" focusable="false" aria-hidden="true">
                  <path d={STAR_ICON_PATH} />
                </svg>
              </span>
              <p className="movie-detail-rating-value">{getDisplayRatingWithScale(globalRating, 10)}</p>
            </div>
          </article>
        </div>
      </div>
    </section>
  )
}

export default MovieDetailRatings
