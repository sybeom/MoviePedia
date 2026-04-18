import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import './index.css'
import App from './App'

// React 앱을 연결할 HTML 요소 조회
const rootElement = document.getElementById('root')

// React 시작에 필요한 root 요소 존재 확인
if (!rootElement) {
  throw new Error('Root element not found')
}

// React 앱 렌더링 시작
createRoot(rootElement).render(
  <StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </StrictMode>,
)
