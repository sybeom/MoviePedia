import Header from '../components/Header'
import { preventFormSubmit } from '../utils/form'
import './HomePage.css'

function HomePage() {
  return (
    <div className="app">
      <Header showLogin />

      <main className="main-container">
        <section className="search-section" aria-labelledby="search-title">
          <h1 id="search-title">보고 싶은 영화를 찾아보세요</h1>
          <form className="search-form" onSubmit={preventFormSubmit}>
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

export default HomePage
