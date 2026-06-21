import backIcon from '../../assets/icons/back.svg'
import thumbsUpIcon from '../../assets/icons/thumbs_up.svg'
import rating12Icon from '../../assets/ratings/12.svg'
import rating15Icon from '../../assets/ratings/15.svg'
import rating19Icon from '../../assets/ratings/19.svg'
import ratingAllIcon from '../../assets/ratings/all.svg'
import type { MovieDetailView } from '../../types/movieDetail'
import { getDisplayScorePercent } from '../../utils/movieDetail'

type MovieDetailHeroProps = {
  movieDetail: MovieDetailView
  isLoading: boolean
  message: string
  onBack: () => void
}

const CERTIFICATION_ICON_MAP: Record<string, string> = {
  '12': rating12Icon,
  '15': rating15Icon,
  '19': rating19Icon,
  ALL: ratingAllIcon,
}

function getMetaParts(parts: string[]) {
  return parts.map((part) => part.trim()).filter(Boolean)
}

function getCertificationIcon(certification: string) {
  return CERTIFICATION_ICON_MAP[certification.toUpperCase()] ?? ''
}

function MetaFragments({ parts }: { parts: string[] }) {
  return (
    <>
      {parts.map((part, index) => (
        <span className="movie-detail-meta-fragment" key={`${part}-${index}`}>
          {index > 0 ? <span className="movie-detail-meta-divider">|</span> : null}
          <span>{part}</span>
        </span>
      ))}
    </>
  )
}

function MovieDetailHero({ movieDetail, isLoading, message, onBack }: MovieDetailHeroProps) {
  const heroImage = movieDetail.poster || movieDetail.backdrop
  const certificationIcon = getCertificationIcon(movieDetail.certification)
  const firstLineParts = getMetaParts([
    movieDetail.releaseDate,
    movieDetail.originCountry,
    movieDetail.runtime ? `${movieDetail.runtime}분` : '',
  ])
  const secondLineParts = getMetaParts([movieDetail.genres])

  return (
    <section className="movie-detail-hero" aria-labelledby="movie-detail-title">
      <div className="movie-detail-backdrop-shell">
        {heroImage ? (
          <img className="movie-detail-backdrop" src={heroImage} alt={movieDetail.title} />
        ) : null}
      </div>

      <div className="movie-detail-hero-content">
        <div className="movie-detail-action-row">
          <button className="movie-detail-back-button" type="button" onClick={onBack} aria-label="뒤로 가기">
            <img className="movie-detail-back-button-icon" src={backIcon} alt="" aria-hidden="true" />
          </button>
        </div>

        <div className="movie-detail-hero-copy">
          <div className="movie-detail-title-row">
            <h1 id="movie-detail-title">{movieDetail.title}</h1>
          </div>

          <div className="movie-detail-meta-lines" aria-label="영화 상세 정보">
            <p className="movie-detail-meta-line">
              {firstLineParts.length > 0 ? <MetaFragments parts={firstLineParts} /> : '-'}
            </p>

            <p className="movie-detail-meta-line">
              {secondLineParts.length > 0 || certificationIcon ? (
                <>
                  {secondLineParts.length > 0 ? <MetaFragments parts={secondLineParts} /> : null}
                  {secondLineParts.length > 0 && certificationIcon ? (
                    <span className="movie-detail-meta-divider">|</span>
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

          <p className="movie-detail-overview">{movieDetail.overview || '-'}</p>

          {!isLoading && message ? <p className="movie-detail-message">{message}</p> : null}

          <div className="movie-detail-score-strip">
            <div className="movie-detail-score-divider" aria-hidden="true" />
            <div className="movie-detail-score-row">
              <img className="movie-detail-score-icon" src={thumbsUpIcon} alt="" aria-hidden="true" />
              <div className="movie-detail-score-copy">
                <span className="movie-detail-score-label">피디아 지수</span>
                <strong className="movie-detail-score-value">
                  {getDisplayScorePercent(movieDetail.score)}
                </strong>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}

export default MovieDetailHero
