import { Route, Routes } from 'react-router-dom'
import HomePage from './pages/HomePage'
import CookiePage from './pages/CookiePage'
import LoginPage from './pages/LoginPage'
import MovieDetailPage from './pages/MovieDetailPage'
import SignupPage from './pages/SignupPage'

// 앱 라우트 연결
function App() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/movies/:movieId" element={<MovieDetailPage />} />
      <Route path="/cookie" element={<CookiePage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />
    </Routes>
  )
}

export default App
