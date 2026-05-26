import rating12Icon from '../../assets/ratings/12.svg'
import rating15Icon from '../../assets/ratings/15.svg'
import rating19Icon from '../../assets/ratings/19.svg'
import ratingAllIcon from '../../assets/ratings/all.svg'
import type { MovieDetailView } from '../../types/movieDetail'

type MovieDetailHeroProps = {
  movieDetail: MovieDetailView
  isLoading: boolean
  message: string
}

const CERTIFICATION_ICON_MAP: Record<string, string> = {
  '12': rating12Icon,
  '15': rating15Icon,
  '19': rating19Icon,
  ALL: ratingAllIcon,
}

// 비어 있지 않은 메타 정보만 추출
function getMetaParts(parts: string[]) {
  return parts.map((part) => part.trim()).filter(Boolean)
}

// 관람등급 아이콘 조회
function getCertificationIcon(certification: string) {
  return CERTIFICATION_ICON_MAP[certification] ?? ''
}

// 메타 조각 출력
function MetaFragments({ parts }: { parts: string[] }) {
  return (
    <>
      {parts.map((part, index) => (
        <span className="movie-detail-meta-fragment" key={`${part}-${index}`}>
          {index > 0 ? <span className="movie-detail-meta-divider">·</span> : null}
          <span>{part}</span>
        </span>
      ))}
    </>
  )
}

// 상세 상단 영역 구성
function MovieDetailHero({ movieDetail, isLoading, message }: MovieDetailHeroProps) {
  const firstLineParts = getMetaParts([
    movieDetail.releaseDate,
    movieDetail.runtime ? `${movieDetail.runtime}분` : '',
    movieDetail.genres,
  ])
  const secondLineParts = getMetaParts([movieDetail.originCountry])
  const certificationIcon = getCertificationIcon(movieDetail.certification)

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

                <div className="movie-detail-meta-lines" aria-label="영화 기본 정보">
                  <p className="movie-detail-meta-line">
                    {firstLineParts.length > 0 ? <MetaFragments parts={firstLineParts} /> : '-'}
                  </p>

                  <p className="movie-detail-meta-line movie-detail-meta-line-with-icon">
                    {secondLineParts.length > 0 || certificationIcon ? (
                      <>
                        {secondLineParts.length > 0 ? <MetaFragments parts={secondLineParts} /> : null}
                        {secondLineParts.length > 0 && certificationIcon ? (
                          <span className="movie-detail-meta-divider">·</span>
                        ) : null}
                        {certificationIcon ? (
                          <span className="movie-detail-meta-fragment">
                            <img
                              className="movie-detail-certification-icon"
                              src={certificationIcon}
                              alt={`${movieDetail.certification} 관람등급`}
                            />
                          </span>
                        ) : null}
                      </>
                    ) : (
                      '-'
                    )}
                  </p>
                </div>

                <section className="movie-detail-overview-section">
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
