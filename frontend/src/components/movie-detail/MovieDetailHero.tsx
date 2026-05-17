import type { MovieDetailView } from '../../types/movieDetail'

type MovieDetailHeroProps = {
  movieDetail: MovieDetailView
  isLoading: boolean
  message: string
}

// 상세 상단 영역 구성
function MovieDetailHero({ movieDetail, isLoading, message }: MovieDetailHeroProps) {
  return (
    <section className="movie-detail-hero" aria-labelledby="movie-detail-title">
      <div className="movie-detail-backdrop-shell">
        {movieDetail.backdrop ? (
          <img className="movie-detail-backdrop" src={movieDetail.backdrop} alt={movieDetail.title} />
        ) : null}
      </div>
      <div className="movie-detail-overlay" />

      <div className="movie-detail-hero-content">
        <div className="movie-detail-panel">
          <div className="movie-detail-summary">
            <div className="movie-detail-poster-shell">
              {movieDetail.poster ? (
                <img className="movie-detail-poster" src={movieDetail.poster} alt={movieDetail.title} />
              ) : null}
            </div>

            <div className="movie-detail-copy-shell">
              <div className="movie-detail-copy">
                <h1 id="movie-detail-title">{movieDetail.title}</h1>

                <dl className="movie-detail-meta">
                  <div className="movie-detail-meta-row">
                    <dt>장르</dt>
                    <dd>{movieDetail.genres || '-'}</dd>
                  </div>
                  <div className="movie-detail-meta-row">
                    <dt>개봉</dt>
                    <dd>{movieDetail.releaseDate || '-'}</dd>
                  </div>
                  <div className="movie-detail-meta-row">
                    <dt>국가</dt>
                    <dd>{movieDetail.originCountry || '-'}</dd>
                  </div>
                  <div className="movie-detail-meta-row">
                    <dt>상영시간</dt>
                    <dd>{movieDetail.runtime ? `${movieDetail.runtime}분` : '-'}</dd>
                  </div>
                </dl>

                <section className="movie-detail-overview-section">
                  <h2>줄거리</h2>
                  <p className="movie-detail-overview">{movieDetail.overview || '-'}</p>
                </section>

                {isLoading ? <p className="movie-detail-message">영화 정보를 불러오는 중입니다...</p> : null}
                {!isLoading && message ? <p className="movie-detail-message">{message}</p> : null}
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}

export default MovieDetailHero
