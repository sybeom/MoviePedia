type HomeSearchResultsProps = {
  query: string
  titles: string[]
  isLoading: boolean
}

// 홈 검색 목록 레이어 구성
function HomeSearchResults({ query, titles, isLoading }: HomeSearchResultsProps) {
  const trimmedQuery = query.trim()

  if (!trimmedQuery) {
    return null
  }

  return (
    <div className="home-search-results" role="listbox" aria-label="영화 검색 목록">
      {isLoading ? (
        <p className="home-search-result-empty">검색 중...</p>
      ) : titles.length > 0 ? (
        titles.map((title, index) => (
          <div className="home-search-result-item" key={`${title}-${index}`} role="option">
            <p className="home-search-result-title">{title}</p>
          </div>
        ))
      ) : (
        <p className="home-search-result-empty">검색 결과가 없습니다.</p>
      )}
    </div>
  )
}

export default HomeSearchResults
