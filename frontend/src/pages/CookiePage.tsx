import { useEffect, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { exchangeJwt } from '../api/auth'
import { saveAuthSession } from '../utils/authStorage'
import './Auth.css'

// 쿠키 처리 안내 화면 구성
function CookiePage() {
  // 처리 완료 후 화면 이동 준비
  const navigate = useNavigate()

  // 중복 교환 요청 방지 참조 준비
  const hasRequestedRef = useRef(false)

  useEffect(() => {
    // 개발 모드 중복 실행 차단
    if (hasRequestedRef.current) {
      return
    }

    // 최초 교환 요청 실행 표시
    hasRequestedRef.current = true

    // 쿠키 기반 토큰 교환 처리
    async function handleExchange() {
      try {
        // 백엔드 쿠키 교환 요청 전송
        const exchangeResponse = await exchangeJwt()

        // 응답 데이터 존재 여부 확인
        if (!exchangeResponse) {
          throw new Error('토큰 교환 응답 데이터가 없습니다.')
        }

        // 로컬 저장소 세션 저장
        saveAuthSession(exchangeResponse)

        // 홈 화면 이동 처리
        navigate('/', { replace: true })
      } catch {
        // 로그인 화면 복귀 처리
        navigate('/login', { replace: true })
      }
    }

    void handleExchange()
  }, [navigate])

  return (
    <div className="app">
      <main className="auth-container">
        <section className="cookie-panel" aria-labelledby="cookie-title">
          <h1 id="cookie-title">로그인 처리 중입니다...</h1>
        </section>
      </main>
    </div>
  )
}

export default CookiePage
