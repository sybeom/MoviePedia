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
  certification: string
  genres: string
  overview: string
  releaseDate: string
  originCountry: string
  runtime: string
  rating: string
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
  movieId: string
  nickname: string
  content: string
  reactionType: 'LIKE' | 'DISLIKE'
}

// 코멘트 수정 요청 데이터 타입 정의
export type UpdateCommentRequest = {
  movieId: string
  content: string
  reactionType: 'LIKE' | 'DISLIKE'
}

// 코멘트 삭제 요청 데이터 타입 정의
export type DeleteCommentRequest = {
  movieId: string
}

// 코멘트 수정 및 상세 조회 응답 타입 정의
export type MovieCommentDetail = {
  movieId: string
  commentId: string
  nickname: string
  content: string
  rating: number
  isMine?: boolean
}

// 코멘트 목록 데이터 타입 정의
export type MovieComment = {
  id: string
  commentId: string
  movieId: string
  nickname: string
  content: string
  reactionType: 'LIKE' | 'DISLIKE'
  writtenByMe: boolean
}

// 코멘트 목록 응답 데이터 타입 정의
export type MovieCommentsResponse = {
  movieId: string
  comments: MovieComment[]
}
