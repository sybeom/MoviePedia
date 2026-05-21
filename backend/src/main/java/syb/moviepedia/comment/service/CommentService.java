package syb.moviepedia.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import syb.moviepedia.comment.domain.Comment;
import syb.moviepedia.comment.dto.CommentDto;
import syb.moviepedia.comment.dto.CommentResponseDto;
import syb.moviepedia.comment.dto.CommentUpdateRequestDto;
import syb.moviepedia.comment.dto.EditCommentResponseDto;
import syb.moviepedia.comment.repository.CommentRepository;
import syb.moviepedia.common.exception.*;
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

    // 상세 보기
    public CommentDto getComment(Long id) {
        Comment comment = commentRepository.findById(id).orElseThrow(
                () -> new CommentNotFoundException("코멘트를 찾을 수 없습니다. id: " + id));

        return toCommentDto(comment);
    }

    // 수정 코멘트 조회
    public EditCommentResponseDto getEditComment(Long id, String loginId) {
        Member member = commentRepository.findByCommentId(id).orElseThrow(
                () -> new CommentMemberNotFound("코멘트 작성자를 찾지 못하였습니다. 코멘트 Id : " + id));

        // 액세스 토큰 파싱의 로그인 아이디와 코멘트 작성 로그인 아이디가 동일하면 수정 가능
        boolean editable = loginId != null && loginId.equals(member.getLoginId());

        Comment comment = commentRepository.findById(id).orElseThrow(
                () -> new CommentNotFoundException("코멘트를 찾을 수 없습니다. id: " + id));

        return toEditCommentDto(comment);
    }

    // 모든 코멘트 목록
    public List<CommentResponseDto> getAllComments(Long code, String loinId) {
        Movie movie = movieRepository.findByCode(code)
                .orElseThrow(() -> new MovieNotFoundException("영화를 찾을 수 없습니다. 영화 코드: " + code));

        // 찾은 영화에서 loginId인 사람이 작성한 코멘트를 찾고 있으면 가장 앞으로 정렬
        List<Comment> comments = commentRepository.findByMovieIdWithMyCommentFirst(movie.getId(), loinId);

        return toCommentListResponseDto(movie.getId(), comments, loinId);
    }

    // 저장
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

    // 수정
    public void updateContent(Long code, CommentUpdateRequestDto dto) {
        // TODO: 영화를 매번 찾아와야한다. 즉 쿼리가 한번씩 실행된다는 말이다.
        //  차라리 코멘트 작성할 때, 영화 아이디를 보내면 어떨까?
        Movie movie = movieRepository.findByCode(code).orElseThrow(
                () -> new MovieNotFoundException("영화를 찾을 수 없습니다. 영화 코드: " + code));
        Comment comment = commentRepository.findByMovieId(movie.getId()).orElseThrow(
                () -> new CommentNotFoundException("코멘트를 찾을 수 없습니다. 영화 코드: " + code));

        comment.updateContent(dto.content());
    }

    // 평점 업데이트
    public void updateRating(Long code, CommentUpdateRequestDto dto) {
        Movie movie = movieRepository.findByCode(code).orElseThrow(
                () -> new MovieNotFoundException("영화를 찾을 수 없습니다. 영화 코드: " + code));
        commentRepository.findByMovieId(movie.getId()).orElseThrow(
                ()-> new CommentNotFoundException("코멘트를 찾을 수 없습니다. 영화 코드: " + code));
    }

    // 엔티티 -> Comment Dto로 가공
    private CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .nickname(comment.getNickname())
                .content(comment.getContent())
                .rating(comment.getRating())
                .like(comment.getLike())
                .build();
    }

    // 엔티티 -> 수정 Comment Dto로 가공
    private EditCommentResponseDto toEditCommentDto(Comment comment) {
        return EditCommentResponseDto.builder()
                .content(comment.getContent())
                .rating(comment.getRating())
                .build();
    }

    // 엔티티 -> CommentResponseDTO로 가공 (리스트)
    private List<CommentResponseDto> toCommentListResponseDto(
            Long movieId,
            List<Comment> comments,
            String loinId) {
        return comments.stream().map(comment ->
                CommentResponseDto.builder()
                        .commentId(comment.getId())
                        .movieId(movieId)
                        .nickname(comment.getNickname())
                        .content(comment.getContent())
                        .rating(comment.getRating())
                        .like(comment.getLike())
                        .isMine(comment.getMember().getLoginId().equals(loinId)) // 로그인 유저가 코멘트 작성자면 true
                        .build())
                .toList();
    }
}
