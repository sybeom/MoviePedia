import type { CreditMember } from '../../types/movieDetail'

type MovieDetailCreditsProps = {
  credits: CreditMember[]
}

// 제작 및 출연 영역 구성
function MovieDetailCredits({ credits }: MovieDetailCreditsProps) {
  const directorCredits = credits.filter((member) => member.roleLabel === '감독')
  const actorCredits = credits.filter((member) => member.roleLabel !== '감독')

  if (credits.length === 0) {
    return null
  }

  return (
    <section className="movie-detail-cast-shell" aria-label="제작 및 출연">
      <div className="movie-detail-cast-section">
        <h2>제작/출연</h2>
        <div className="movie-detail-cast-list-shell">
          {directorCredits.length > 0 ? (
            <div className="movie-detail-cast-director-row">
              {directorCredits.map((member, index) => (
                <article className="movie-detail-cast-card movie-detail-cast-card-director" key={`director-${member.name}-${index}`}>
                  <div className="movie-detail-cast-profile-shell">
                    {member.profile ? (
                      <img className="movie-detail-cast-profile" src={member.profile} alt={member.name} />
                    ) : null}
                  </div>
                  <p className="movie-detail-cast-name">{member.name || '-'}</p>
                  <p className="movie-detail-cast-role">{member.roleLabel || ' '}</p>
                </article>
              ))}
            </div>
          ) : null}

          {actorCredits.length > 0 ? (
            <div className="movie-detail-cast-grid">
              {actorCredits.map((member, index) => (
                <article className="movie-detail-cast-card" key={`actor-${member.name}-${index}`}>
                  <div className="movie-detail-cast-profile-shell">
                    {member.profile ? (
                      <img className="movie-detail-cast-profile" src={member.profile} alt={member.name} />
                    ) : null}
                  </div>
                  <p className="movie-detail-cast-name">{member.name || '-'}</p>
                  <p className="movie-detail-cast-role">{member.roleLabel || ' '}</p>
                </article>
              ))}
            </div>
          ) : null}
        </div>
      </div>
    </section>
  )
}

export default MovieDetailCredits
