type HomeSearchResultsProps = {
  query: string
  titles: string[]
  isLoading: boolean
  isOpen: boolean
  activeIndex: number
}

// 홈 검색 목록 레이어 구성
function HomeSearchResults({
  query,
  titles,
  isLoading,
  isOpen,
  activeIndex,
}: HomeSearchResultsProps) {
  const trimmedQuery = query.trim()

  if (!trimmedQuery || !isOpen) {
    return null
  }

  return (
    <div className="home-search-results" role="listbox" aria-label="영화 검색 목록">
      {isLoading ? (
        <p className="home-search-result-empty">검색 중...</p>
      ) : titles.length > 0 ? (
        titles.map((title, index) => (
          <div
            className={`home-search-result-item${
              activeIndex === index ? ' home-search-result-item-active' : ''
            }`}
            key={`${title}-${index}`}
            role="option"
            aria-selected={activeIndex === index}
          >
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
