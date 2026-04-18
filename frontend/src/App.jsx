import './App.css'

function App() {
  return (
    <div className="app">
      <header className="site-header">
        <a className="brand" href="/" aria-label="Movie Pedia home">
          Movie Pedia
        </a>
        <button className="login-button" type="button">
          로그인
        </button>
      </header>

      <main className="main-container">
        <section className="search-section" aria-labelledby="search-title">
          <h1 id="search-title">보고 싶은 영화를 찾아보세요</h1>
          <form className="search-form" onSubmit={(event) => event.preventDefault()}>
            <label className="sr-only" htmlFor="movie-search">
              영화 검색
            </label>
            <input
              id="movie-search"
              className="search-input"
              type="search"
              placeholder="영화 제목, 배우, 감독을 검색해보세요"
            />
            <button className="search-button" type="submit">
              검색
            </button>
          </form>
        </section>
      </main>
    </div>
  )
}

export default App
