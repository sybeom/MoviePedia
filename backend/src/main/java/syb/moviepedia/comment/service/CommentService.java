package syb.moviepedia.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import syb.moviepedia.comment.domain.Comment;
import syb.moviepedia.comment.dto.CommentDto;
import syb.moviepedia.comment.dto.CommentResponseDto;
import syb.moviepedia.comment.repository.CommentRepository;
import syb.moviepedia.common.exception.CommentAlreadyExistsException;
import syb.moviepedia.common.exception.MemberNotFoundException;
import syb.moviepedia.common.exception.MovieNotFoundException;
import syb.moviepedia.member.domain.Member;
import syb.moviepedia.member.repository.MemberRepository;
import syb.moviepedia.movie.domain.Movie;
import syb.moviepedia.movie.repository.MovieRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentService {
    private final MovieRepository movieRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;

    // 모든 코멘트 목록
    public List<CommentResponseDto> getAllComments(Long code, String loinId) {
        Movie movie = movieRepository.findByCode(code)
                .orElseThrow(() -> new MovieNotFoundException("영화를 찾을 수 없습니다. 영화 코드: " + code));

        // 찾은 영화에서 loginId인 사람이 작성한 코멘트를 찾고 있으면 가장 앞으로 정렬
        List<Comment> comments = commentRepository.findByMovieIdWithMyCommentFirst(movie.getId(), loinId);

        return toCommentsDto(comments, loinId);
    }

    // 코멘트 저장
    public void saveComment(Long code, CommentDto dto) {
        Movie movie = movieRepository.findByCode(code).orElseThrow(
                () -> new MovieNotFoundException("영화를 찾을 수 없습니다. 영화 코드: " + code));

        Member member = memberRepository.findByNickname(dto.nickname()).orElseThrow(
                () -> new MemberNotFoundException("멤버를 찾을 수 없습니다. 닉네임:" + dto.nickname()));

        // 영화당 1코멘트만 가능하도록 하기 위함
        if (commentRepository.existsByMovieIdAndMemberId(movie.getId(), member.getId())) {
            throw new CommentAlreadyExistsException("이미 해당 영화에 코멘트를 작성하였습니다.");
        }

        // 코멘트 엔티티 생성
        Comment comment = Comment.builder()
                .nickname(dto.nickname())
                .content(dto.content())
                .rating(dto.rating())
                .movie(movie)
                .member(member)
                .like(0)
                .build();

        commentRepository.save(comment);
    }

    private List<CommentResponseDto> toCommentsDto(
            List<Comment> comments, String loinId) {
        return comments.stream().map(comment ->
                CommentResponseDto.builder()
                        .nickname(comment.getNickname())
                        .content(comment.getContent())
                        .rating(comment.getRating())
                        .isMine(comment.getMember().getLoginId().equals(loinId)) // 로그인 유저가 코멘트 작성자면 true
                        .build())
                .toList();
    }
}
