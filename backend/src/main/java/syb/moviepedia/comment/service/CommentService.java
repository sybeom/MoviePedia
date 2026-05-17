package syb.moviepedia.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import syb.moviepedia.comment.domain.Comment;
import syb.moviepedia.comment.dto.CommentDto;
import syb.moviepedia.comment.repository.CommentRepository;
import syb.moviepedia.common.exception.MovieNotFoundException;
import syb.moviepedia.movie.domain.Movie;
import syb.moviepedia.movie.repository.MovieRepository;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final MovieRepository movieRepository;
    private final CommentRepository commentRepository;

    public void saveComment(Long code, CommentDto dto) {

        Movie movie = movieRepository.findByCode(code).orElseThrow(
                () -> new MovieNotFoundException("영화를 찾을 수 없습니다. 영화 코드: " + code));

        Comment comment = Comment.builder()
                .nickname(dto.nickname())
                .content(dto.content())
                .rating(dto.rating())
                .movie(movie)
                .build();
        commentRepository.save(comment);
    }
}
