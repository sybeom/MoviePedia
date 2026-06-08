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
  normalizeMovieTrailers,
} from '../utils/movieDetail'

export async function fetchMovieDetail(movieId: string) {
  const response = await request<unknown>(`/movies/${movieId}`, {
    method: 'GET',
  })

  return normalizeMovieDetail(response)
}

export async function fetchMovieTrailers(movieCode: string) {
  const response = await request<unknown>(`/movies/${movieCode}/trailer`, {
    method: 'GET',
  })

  return normalizeMovieTrailers(response)
}

export function fetchMovieComments(
  movieId: string,
  page = 0,
  sort: 'latest' | 'oldest' = 'latest',
) {
  const session = getAuthSession()
  const sortParam = sort === 'oldest' ? 'OLDEST' : 'LATEST'
  const searchParams = new URLSearchParams({
    page: String(page),
    size: '20',
    sort: sortParam,
  })

  return request<unknown>(`/movies/${movieId}/comments?${searchParams.toString()}`, {
    method: 'GET',
    headers: session?.accessToken
      ? {
          Authorization: `Bearer ${session.accessToken}`,
        }
      : undefined,
  }).then((response) => normalizeMovieComments(response))
}

export function createMovieComment(movieId: string, body: CreateCommentRequest) {
  return authRequest<CreateCommentRequest>(`/movies/${movieId}/comments`, {
    method: 'POST',
    body,
  })
}

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

export function fetchMovieCommentForEdit(movieId: string, commentId: string) {
  return authRequest<unknown>(`/movies/${movieId}/comments/${commentId}/edit`, {
    method: 'GET',
  }).then((response) => normalizeMovieCommentDetail(response))
}

export function verifyCommentAuth() {
  return authRequest<AuthMeResponse>('/auth/me', {
    method: 'GET',
  })
}
