import { useEffect, useMemo, useRef, useState } from 'react'
import loadingIcon from '../../assets/icons/loading.svg'
import nextIcon from '../../assets/icons/next.svg'
import playIcon from '../../assets/icons/play.svg'
import previousIcon from '../../assets/icons/previous.svg'
import type { TrailerItem } from '../../types/movieDetail'

type MovieDetailTrailersProps = {
  trailers: TrailerItem[]
  isLoading: boolean
}

const TRAILER_CARD_WIDTH = 220
const TRAILER_CARD_GAP = 10
const TRAILER_SCROLL_STEP = TRAILER_CARD_WIDTH + TRAILER_CARD_GAP

function MovieDetailTrailers({ trailers, isLoading }: MovieDetailTrailersProps) {
  const orderedTrailers = useMemo(
    () => trailers.filter((trailer) => trailer.title || trailer.thumbnail),
    [trailers],
  )
  const viewportRef = useRef<HTMLDivElement | null>(null)
  const [canScrollPrevious, setCanScrollPrevious] = useState(false)
  const [canScrollNext, setCanScrollNext] = useState(false)

  useEffect(() => {
    const viewport = viewportRef.current

    if (!viewport) {
      return
    }

    const updateScrollState = () => {
      const maxScrollLeft = Math.max(0, viewport.scrollWidth - viewport.clientWidth)

      setCanScrollPrevious(viewport.scrollLeft > 1)
      setCanScrollNext(viewport.scrollLeft < maxScrollLeft - 1)
    }

    updateScrollState()
    viewport.addEventListener('scroll', updateScrollState, { passive: true })
    window.addEventListener('resize', updateScrollState)

    return () => {
      viewport.removeEventListener('scroll', updateScrollState)
      window.removeEventListener('resize', updateScrollState)
    }
  }, [orderedTrailers])

  if (isLoading) {
    return (
      <section className="movie-detail-trailer-shell" aria-label="트레일러">
        <div className="movie-detail-trailer-section">
          <h2>트레일러</h2>
          <div className="movie-detail-section-loading">
            <img
              className="movie-detail-section-loading-icon"
              src={loadingIcon}
              alt=""
              aria-hidden="true"
            />
          </div>
        </div>
      </section>
    )
  }

  if (orderedTrailers.length === 0) {
    return null
  }

  function moveToPrevious() {
    viewportRef.current?.scrollBy({
      left: -TRAILER_SCROLL_STEP,
      behavior: 'smooth',
    })
  }

  function moveToNext() {
    viewportRef.current?.scrollBy({
      left: TRAILER_SCROLL_STEP,
      behavior: 'smooth',
    })
  }

  return (
    <section className="movie-detail-trailer-shell" aria-label="트레일러">
      <div className="movie-detail-trailer-section">
        <h2>트레일러</h2>

        <div className="movie-detail-trailer-carousel">
          <button
            className="movie-detail-cast-carousel-button movie-detail-trailer-carousel-button-previous"
            type="button"
            onClick={moveToPrevious}
            disabled={!canScrollPrevious}
            aria-label="이전 트레일러"
          >
            <img
              className="movie-detail-cast-carousel-button-icon"
              src={previousIcon}
              alt=""
              aria-hidden="true"
            />
          </button>

          <div className="movie-detail-cast-viewport">
            <div className="movie-detail-trailer-scroller" ref={viewportRef}>
              <div className="movie-detail-trailer-track">
                {orderedTrailers.map((trailer, index) => {
                  const primaryLabel = trailer.typeLabel || trailer.title || '트레일러'
                  const cardContent = (
                    <>
                      <div className="movie-detail-trailer-thumbnail-shell">
                        {trailer.thumbnail ? (
                          <img
                            className="movie-detail-trailer-thumbnail"
                            src={trailer.thumbnail}
                            alt={trailer.title || primaryLabel}
                          />
                        ) : (
                          <div className="movie-detail-trailer-thumbnail movie-detail-trailer-thumbnail-fallback">
                            <span>{trailer.title || primaryLabel}</span>
                          </div>
                        )}
                        <span className="movie-detail-trailer-play-badge" aria-hidden="true">
                          <img
                            className="movie-detail-trailer-play-icon"
                            src={playIcon}
                            alt=""
                          />
                        </span>
                      </div>
                      <p className="movie-detail-trailer-title">{primaryLabel}</p>
                    </>
                  )

                  return trailer.videoUrl ? (
                    <a
                      className="movie-detail-trailer-card"
                      key={`${trailer.title}-${index}`}
                      href={trailer.videoUrl}
                      target="_blank"
                      rel="noreferrer"
                    >
                      {cardContent}
                    </a>
                  ) : (
                    <article className="movie-detail-trailer-card" key={`${trailer.title}-${index}`}>
                      {cardContent}
                    </article>
                  )
                })}
              </div>
            </div>
          </div>

          <button
            className="movie-detail-cast-carousel-button movie-detail-trailer-carousel-button-next"
            type="button"
            onClick={moveToNext}
            disabled={!canScrollNext}
            aria-label="다음 트레일러"
          >
            <img
              className="movie-detail-cast-carousel-button-icon"
              src={nextIcon}
              alt=""
              aria-hidden="true"
            />
          </button>
        </div>
      </div>
    </section>
  )
}

export default MovieDetailTrailers
