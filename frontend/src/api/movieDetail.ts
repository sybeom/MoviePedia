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

export async function fetchMovieDetail(movieId: string) {
  const response = await request<unknown>(`/movies/${movieId}`, {
    method: 'GET',
  })

  return normalizeMovieDetail(response)
}

export function fetchMovieComments(movieId: string) {
  const existingRequest = commentRequestMap.get(movieId)

  if (existingRequest) {
    return existingRequest
  }

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

export function likeMovieComment(movieId: string, commentId: string) {
  return authRequest<void>(`/movies/${movieId}/comments/${commentId}/like`, {
    method: 'POST',
  })
}

export function unlikeMovieComment(movieId: string, commentId: string) {
  return authRequest<void>(`/movies/${movieId}/comments/${commentId}/like`, {
    method: 'DELETE',
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
