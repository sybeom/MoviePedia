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
import syb.moviepedia.like.repository.LikeRepository;
import syb.moviepedia.member.domain.Member;
import syb.moviepedia.member.repository.MemberRepository;
import syb.moviepedia.movie.domain.Movie;
import syb.moviepedia.movie.repository.MovieRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentService {
    private final MovieRepository movieRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

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

        Comment comment = commentRepository.findById(id).orElseThrow(
                () -> new CommentNotFoundException("코멘트를 찾을 수 없습니다. id: " + id));

        return toEditCommentDto(comment);
    }

    // 모든 코멘트 목록
    public List<CommentResponseDto> getAllComments(Long code, String loginId) {
        Movie movie = movieRepository.findByCode(code)
                .orElseThrow(() -> new MovieNotFoundException("영화를 찾을 수 없습니다. 영화 코드: " + code));

        // 찾은 영화에서 loginId인 사람이 작성한 코멘트를 찾고 있으면 가장 앞으로 정렬
        List<Comment> comments = commentRepository.findByMovieIdWithMyCommentFirst(movie.getId(), loginId);

        // 코멘트 목록들 id만 추출
        List<Long> commentIds = comments.stream().map(Comment::getId).toList();

        // 중복 제거 목적이 아닌 성능때문에 사용
        Set<Long> likedIdSet = Set.of(); // List를 사용하면 비교할 때 앞에서부터 하나하나 비교하기 때문

        // 로그인한 경우에만 멤버 조회 + 좋아요 여부 조회
        if (loginId != null && !commentIds.isEmpty()) {
            // 현재 로그인 중인 멤버 ID
            Member member = memberRepository.findByLoginId(loginId).orElseThrow(
                    () -> new MemberNotFoundException("멤버를 찾을 수 없습니다. loginId: " + loginId));
            Long memberId = member.getId();


            // 현재 로그인한 유저가 좋아요 누른 코멘트들의 좋아요 아이디 목록
            List<Long> likeIds = likeRepository.findLikeIdsByMemberIdAndCommentIds(memberId, commentIds);

            likedIdSet = new HashSet<>(likeIds);
        }

        return toCommentListResponseDto(movie.getId(), comments, loginId, likedIdSet);
    }

    // TODO: Transactional 안했는데 어케 등록됐찌?
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
    // TODO: Transactional 넣지 않고 수정되는지 실험해보기
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
    // TODO: Transactional 넣지 않고 수정되는지 실험해보기
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
            String loinId,
            Set<Long> likedIdsSet) {
        return comments.stream().map(comment ->
                CommentResponseDto.builder()
                        .commentId(comment.getId())
                        .movieId(movieId)
                        .nickname(comment.getNickname())
                        .content(comment.getContent())
                        .rating(comment.getRating())
                        .like(comment.getLike())
                        .writtenByMe(comment.getMember().getLoginId().equals(loinId)) // 로그인 유저가 코멘트 작성자면 true
                        .likedByMe(likedIdsSet.contains(comment.getId())) // 각 코멘트에 대하여 id를 포함하고 있는지 검사, 포함하면 해당 코멘트에 좋아요 눌른 상태
                        .build())
                .toList();
    }
}
