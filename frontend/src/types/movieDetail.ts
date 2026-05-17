// 상세 이동 상태 타입 정의
export type MovieDetailState = {
  movie?: {
    id?: string
    title?: string
    poster?: string
  }
}

// 제작 및 출연 데이터 타입 정의
export type CreditMember = {
  name: string
  profile: string
  roleLabel: string
}

// 상세 화면 데이터 타입 정의
export type MovieDetailView = {
  id: string
  title: string
  poster: string
  backdrop: string
  genres: string
  overview: string
  releaseDate: string
  originCountry: string
  runtime: string
  globalRating: string
  credits: CreditMember[]
}

// 로그인 확인 응답 타입 정의
export type AuthMeResponse = {
  loginId?: string
  nickname?: string
}

// 코멘트 작성 요청 타입 정의
export type CreateCommentRequest = {
  nickname: string
  content: string
  rating: number
}

// 코멘트 목록 데이터 타입 정의
export type MovieComment = {
  id: string
  nickname: string
  content: string
  rating: string
}
