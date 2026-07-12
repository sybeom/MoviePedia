import { request } from './client'
import { MEDIA_CONFIGS, type MediaType } from '../config/media'
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
  getCreditValue,
  normalizeMovieCommentDetail,
  normalizeMovieComments,
  normalizeMovieDetail,
  normalizeMovieTrailers,
} from '../utils/movieDetail'

function getMediaResourcePath(mediaType: MediaType) {
  return MEDIA_CONFIGS[mediaType].resourcePath
}

function getCommentMediaTypeValue(mediaType: MediaType) {
  return mediaType === 'series' ? 'TV' : 'MOVIE'
}

function getCommentApiBasePath(mediaType: MediaType) {
  return mediaType === 'series' ? '/series' : getMediaResourcePath(mediaType)
}

function getCommentResourcePath(
  mediaType: MediaType,
  movieId: string,
  seasonNum = '',
) {
  return mediaType === 'series' && seasonNum
    ? `${getCommentApiBasePath(mediaType)}/${movieId}/${seasonNum}/comments`
    : `${getCommentApiBasePath(mediaType)}/${movieId}/comments`
}

function createCommentSearchParams(
  mediaType: MediaType,
  extraParams?: Record<string, string>,
) {
  const searchParams = new URLSearchParams({
    mediaType: getCommentMediaTypeValue(mediaType),
    ...extraParams,
  })

  return searchParams
}

export async function fetchMovieDetail(
  movieId: string,
  mediaType: MediaType = 'movie',
  seasonNum = '',
) {
  const detailPath =
    mediaType === 'series' && seasonNum
      ? `${getMediaResourcePath(mediaType)}/${movieId}/${seasonNum}`
      : `${getMediaResourcePath(mediaType)}/${movieId}`

  const response = await request<unknown>(detailPath, {
    method: 'GET',
  })

  return normalizeMovieDetail(response)
}

export async function fetchMovieTrailers(
  movieCode: string,
  mediaType: MediaType = 'movie',
  seasonNum = '',
) {
  const videosPath =
    mediaType === 'series' && seasonNum
      ? `${getMediaResourcePath(mediaType)}/${movieCode}/${seasonNum}/videos`
      : `${getMediaResourcePath(mediaType)}/${movieCode}/videos`

  const response = await request<unknown>(videosPath, {
    method: 'GET',
  })

  return normalizeMovieTrailers(response)
}

export async function fetchMovieCredits(
  movieId: string,
  mediaType: MediaType = 'movie',
  seasonNum = '',
) {
  const creditsPath =
    mediaType === 'series' && seasonNum
      ? `${getMediaResourcePath(mediaType)}/${movieId}/${seasonNum}/credits`
      : `${getMediaResourcePath(mediaType)}/${movieId}/credits`

  const response = await request<unknown>(creditsPath, {
    method: 'GET',
  })

  if (Array.isArray(response)) {
    return getCreditValue({ data: response })
  }

  if (!response || typeof response !== 'object') {
    return []
  }

  return getCreditValue(response as Record<string, unknown>)
}

export function fetchMovieComments(
  movieId: string,
  page = 0,
  sort: 'latest' | 'oldest' = 'latest',
  mediaType: MediaType = 'movie',
  seasonNum = '',
) {
  const session = getAuthSession()
  const sortParam = sort === 'oldest' ? 'OLDEST' : 'LATEST'
  const searchParams = createCommentSearchParams(mediaType, {
    page: String(page),
    size: '20',
    sort: sortParam,
  })

  return request<unknown>(
    `${getCommentResourcePath(mediaType, movieId, seasonNum)}?${searchParams.toString()}`,
    {
      method: 'GET',
      headers: session?.accessToken
        ? {
            Authorization: `Bearer ${session.accessToken}`,
          }
        : undefined,
    },
  ).then((response) => normalizeMovieComments(response))
}

export function createMovieComment(
  movieId: string,
  body: CreateCommentRequest,
  mediaType: MediaType = 'movie',
  seasonNum = '',
) {
  const searchParams = createCommentSearchParams(mediaType)

  return authRequest<CreateCommentRequest>(
    `${getCommentResourcePath(mediaType, movieId, seasonNum)}?${searchParams.toString()}`,
    {
      method: 'POST',
      body,
    },
  )
}

export function updateMovieComment(
  movieId: string,
  commentId: string,
  body: UpdateCommentRequest,
  mediaType: MediaType = 'movie',
  seasonNum = '',
) {
  const searchParams = createCommentSearchParams(mediaType)

  return authRequest<UpdateCommentRequest>(
    `${getCommentResourcePath(mediaType, movieId, seasonNum)}/${commentId}/edit?${searchParams.toString()}`,
    {
      method: 'PATCH',
      body,
    },
  )
}

export function deleteMovieComment(
  movieId: string,
  commentId: string,
  body: DeleteCommentRequest,
  mediaType: MediaType = 'movie',
  seasonNum = '',
) {
  const searchParams = createCommentSearchParams(mediaType)

  return authRequest<DeleteCommentRequest>(
    `${getCommentResourcePath(mediaType, movieId, seasonNum)}/${commentId}?${searchParams.toString()}`,
    {
      method: 'DELETE',
      body,
    },
  )
}

export function fetchMovieCommentForEdit(
  movieId: string,
  commentId: string,
  mediaType: MediaType = 'movie',
  seasonNum = '',
) {
  const searchParams = createCommentSearchParams(mediaType)

  return authRequest<unknown>(
    `${getCommentResourcePath(mediaType, movieId, seasonNum)}/${commentId}/edit?${searchParams.toString()}`,
    {
      method: 'GET',
    },
  ).then((response) => normalizeMovieCommentDetail(response))
}

export function verifyCommentAuth() {
  return authRequest<AuthMeResponse>('/auth/me', {
    method: 'GET',
  })
}
