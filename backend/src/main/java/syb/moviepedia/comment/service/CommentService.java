package syb.moviepedia.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import syb.moviepedia.comment.domain.Comment;
import syb.moviepedia.comment.dto.CommentDto;
import syb.moviepedia.comment.repository.CommentRepository;
import syb.moviepedia.common.exception.DuplicateCommentException;
import syb.moviepedia.common.exception.MemberNotFoundException;
import syb.moviepedia.common.exception.MovieNotFoundException;
import syb.moviepedia.member.domain.Member;
import syb.moviepedia.member.repository.MemberRepository;
import syb.moviepedia.movie.domain.Movie;
import syb.moviepedia.movie.repository.MovieRepository;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final MovieRepository movieRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;

    // 코멘트 저장
    public void saveComment(Long code, CommentDto dto) {
        Movie movie = movieRepository.findByCode(code).orElseThrow(
                () -> new MovieNotFoundException("영화를 찾을 수 없습니다. 영화 코드: " + code));

        Member member = memberRepository.findByNickname(dto.nickname()).orElseThrow(
                () -> new MemberNotFoundException("멤버를 찾을 수 없습니다. 닉네임:" + dto.nickname()));

        // 영화당 1코멘트만 가능하도록 하기 위함
        if (commentRepository.existsByMovieIdAndMemberId(movie.getId(), member.getId())) {
            throw new DuplicateCommentException("이미 해당 영화에 코멘트를 작성하였습니다.");
        }

        Comment comment = Comment.builder()
                .nickname(dto.nickname())
                .content(dto.content())
                .rating(dto.rating())
                .movie(movie)
                .member(member)
                .build();

        commentRepository.save(comment);
    }
}
