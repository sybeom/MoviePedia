import { request } from './client'
import { getAuthSession } from '../utils/authStorage'
import { authRequest } from '../utils/fetchUtil'
import type {
  AuthMeResponse,
  CreateCommentRequest,
  EditableMovieComment,
  MovieComment,
} from '../types/movieDetail'
import {
  normalizeEditableMovieComment,
  normalizeMovieComments,
  normalizeMovieDetail,
} from '../utils/movieDetail'

const commentRequestMap = new Map<string, Promise<MovieComment[]>>()

// 영화 상세 조회 처리
export async function fetchMovieDetail(movieId: string) {
  const response = await request<unknown>(`/movies/${movieId}`, {
    method: 'GET',
  })

  return normalizeMovieDetail(response)
}

// 코멘트 목록 요청 중복 방지 처리
export function fetchMovieComments(movieId: string) {
  const existingRequest = commentRequestMap.get(movieId)

  if (existingRequest) {
    return existingRequest
  }

  // 로그인 세션 조회 처리
  const session = getAuthSession()

  const requestPromise = request<unknown>(`/movies/${movieId}/comments`, {
    method: 'GET',
    headers: session?.accessToken
      ? {
          Authorization: `Bearer ${session.accessToken}`,
        }
      : undefined,
  })
    .then((response) => normalizeMovieComments(response))
    .finally(() => {
      commentRequestMap.delete(movieId)
    })

  commentRequestMap.set(movieId, requestPromise)

  return requestPromise
}

// 코멘트 작성 처리
export function createMovieComment(movieId: string, body: CreateCommentRequest) {
  return authRequest<CreateCommentRequest>(`/movies/${movieId}/comments`, {
    method: 'POST',
    body,
  })
}

// 단일 코멘트 수정 데이터 조회 처리
export function fetchMovieCommentForEdit(movieId: string, commentId: string) {
  return authRequest<unknown>(`/movies/${movieId}/comments/${commentId}`, {
    method: 'GET',
  }).then((response) => normalizeEditableMovieComment(response))
}

// 코멘트 작성 로그인 확인 처리
export function verifyCommentAuth() {
  return authRequest<AuthMeResponse>('/auth/me', {
    method: 'GET',
  })
}
