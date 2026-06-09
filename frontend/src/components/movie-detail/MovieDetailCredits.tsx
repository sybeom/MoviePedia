import { useEffect, useMemo, useRef, useState } from 'react'
import loadingIcon from '../../assets/icons/loading.svg'
import nextIcon from '../../assets/icons/next.svg'
import previousIcon from '../../assets/icons/previous.svg'
import type { CreditMember } from '../../types/movieDetail'

type MovieDetailCreditsProps = {
  credits: CreditMember[]
  isLoading: boolean
}

const CAST_CARD_WIDTH = 112
const CAST_CARD_GAP = 2
const CAST_SCROLL_STEP = CAST_CARD_WIDTH + CAST_CARD_GAP

function MovieDetailCredits({ credits, isLoading }: MovieDetailCreditsProps) {
  const orderedCredits = useMemo(() => {
    const directorCredits = credits.filter((member) => member.roleLabel === '감독')
    const actorCredits = credits.filter((member) => member.roleLabel !== '감독')

    return [...directorCredits, ...actorCredits]
  }, [credits])

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
  }, [orderedCredits])

  if (isLoading) {
    return (
      <section className="movie-detail-cast-shell" aria-label="제작 및 출연">
        <div className="movie-detail-cast-section">
          <h2>제작/출연</h2>
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

  if (orderedCredits.length === 0) {
    return (
      <section className="movie-detail-cast-shell" aria-label="제작 및 출연">
        <div className="movie-detail-cast-section">
          <h2>제작/출연</h2>
          <div className="movie-detail-section-empty">
            <p>등록된 제작, 출연 정보가 없습니다.</p>
          </div>
        </div>
      </section>
    )
  }

  function moveToPrevious() {
    viewportRef.current?.scrollBy({
      left: -CAST_SCROLL_STEP,
      behavior: 'smooth',
    })
  }

  function moveToNext() {
    viewportRef.current?.scrollBy({
      left: CAST_SCROLL_STEP,
      behavior: 'smooth',
    })
  }

  return (
    <section className="movie-detail-cast-shell" aria-label="제작 및 출연">
      <div className="movie-detail-cast-section">
        <h2>제작/출연</h2>

        <div className="movie-detail-cast-carousel">
          <button
            className="movie-detail-cast-carousel-button movie-detail-cast-carousel-button-previous"
            type="button"
            onClick={moveToPrevious}
            disabled={!canScrollPrevious}
            aria-label="이전 제작 및 출연"
          >
            <img
              className="movie-detail-cast-carousel-button-icon"
              src={previousIcon}
              alt=""
              aria-hidden="true"
            />
          </button>

          <div className="movie-detail-cast-viewport">
            <div className="movie-detail-cast-scroller" ref={viewportRef}>
              <div className="movie-detail-cast-track">
                {orderedCredits.map((member, index) => (
                  <article className="movie-detail-cast-card" key={`${member.name}-${index}`}>
                    <div className="movie-detail-cast-profile-shell">
                      {member.profile ? (
                        <img className="movie-detail-cast-profile" src={member.profile} alt={member.name} />
                      ) : null}
                    </div>
                    <p className="movie-detail-cast-role">{member.roleLabel || ' '}</p>
                    <p className="movie-detail-cast-name">{member.name || '-'}</p>
                  </article>
                ))}
              </div>
            </div>
          </div>

          <button
            className="movie-detail-cast-carousel-button movie-detail-cast-carousel-button-next"
            type="button"
            onClick={moveToNext}
            disabled={!canScrollNext}
            aria-label="다음 제작 및 출연"
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

export default MovieDetailCredits


