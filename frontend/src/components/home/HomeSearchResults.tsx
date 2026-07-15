import { Link } from 'react-router-dom'

type SearchMovie = {
  code: string
  title: string
  seasonNum?: string
}

type HomeSearchResultsProps = {
  query: string
  movies: SearchMovie[]
  isLoading: boolean
  isOpen: boolean
  activeIndex: number
  ariaLabel?: string
  buildDetailPath?: (code: string, seasonNum?: string) => string
}

function HomeSearchResults({
  query,
  movies,
  isLoading,
  isOpen,
  activeIndex,
  ariaLabel = '검색 목록',
  buildDetailPath = (code, seasonNum) => (seasonNum ? `/series/${code}/${seasonNum}` : `/movies/${code}`),
}: HomeSearchResultsProps) {
  const trimmedQuery = query.trim()

  if (!trimmedQuery || !isOpen) {
    return null
  }

  return (
    <div className="home-search-results" role="listbox" aria-label={ariaLabel}>
      {isLoading ? (
        <p className="home-search-result-empty">검색 중..</p>
      ) : movies.length > 0 ? (
        movies.map((movie, index) => (
          <Link
            className={`home-search-result-item${
              activeIndex === index ? ' home-search-result-item-active' : ''
            }`}
            key={`${movie.code}-${index}`}
            to={buildDetailPath(movie.code, movie.seasonNum)}
            role="option"
            aria-selected={activeIndex === index}
            state={{
              movie: { id: movie.code, title: movie.title, poster: '', seasonNum: movie.seasonNum ?? '' },
            }}
          >
            <p className="home-search-result-title">{movie.title}</p>
          </Link>
        ))
      ) : (
        <p className="home-search-result-empty">검색 결과가 없습니다.</p>
      )}
    </div>
  )
}

export default HomeSearchResults
