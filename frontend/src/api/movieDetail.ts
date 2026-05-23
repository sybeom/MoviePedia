import { request } from './client'
import { getAuthSession } from '../utils/authStorage'
import { authRequest } from '../utils/fetchUtil'
import type {
  AuthMeResponse,
  CreateCommentRequest,
  DeleteCommentRequest,
  MovieCommentsResponse,
  UpdateCommentRequest,
} from '../types/movieDetail'
import {
  normalizeMovieCommentDetail,
  normalizeMovieComments,
  normalizeMovieDetail,
} from '../utils/movieDetail'

const commentRequestMap = new Map<string, Promise<MovieCommentsResponse>>()

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

// 코멘트 수정 요청 처리
export function updateMovieComment(
  movieId: string,
  commentId: string,
  body: UpdateCommentRequest,
) {
  return authRequest<UpdateCommentRequest>(`/movies/${movieId}/comments/${commentId}`, {
    method: 'PATCH',
    body,
  })
}

// 코멘트 삭제 요청 처리
export function deleteMovieComment(
  movieId: string,
  commentId: string,
  body: DeleteCommentRequest,
) {
  return authRequest<DeleteCommentRequest>(`/movies/${movieId}/comments/${commentId}`, {
    method: 'DELETE',
    body,
  })
}

// 코멘트 좋아요 요청 처리
export function likeMovieComment(movieId: string, commentId: string) {
  return authRequest<void>(`/movies/${movieId}/comments/${commentId}/like`, {
    method: 'POST',
  })
}

// 코멘트 좋아요 취소 요청 처리
export function unlikeMovieComment(movieId: string, commentId: string) {
  return authRequest<void>(`/movies/${movieId}/comments/${commentId}/like`, {
    method: 'DELETE',
  })
}

// 단일 코멘트 상세 조회 처리
export function fetchMovieCommentDetail(movieId: string, commentId: string) {
  const session = getAuthSession()

  return request<unknown>(`/movies/${movieId}/comments/${commentId}`, {
    method: 'GET',
    headers: session?.accessToken
      ? {
          Authorization: `Bearer ${session.accessToken}`,
        }
      : undefined,
  }).then((response) => normalizeMovieCommentDetail(response))
}

// 코멘트 수정 조회 처리
export function fetchMovieCommentForEdit(movieId: string, commentId: string) {
  return authRequest<unknown>(`/movies/${movieId}/comments/${commentId}/edit`, {
    method: 'GET',
  }).then((response) => normalizeMovieCommentDetail(response))
}

// 코멘트 작성 로그인 확인 처리
export function verifyCommentAuth() {
  return authRequest<AuthMeResponse>('/auth/me', {
    method: 'GET',
  })
}
