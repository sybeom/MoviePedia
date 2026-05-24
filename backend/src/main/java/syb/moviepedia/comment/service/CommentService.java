package syb.moviepedia.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syb.moviepedia.comment.domain.Comment;
import syb.moviepedia.comment.dto.response.CommentDetailResponse;
import syb.moviepedia.comment.dto.request.CommentSaveRequest;
import syb.moviepedia.comment.dto.request.CommentUpdateRequest;
import syb.moviepedia.comment.dto.response.CommentEditResponse;
import syb.moviepedia.comment.dto.response.CommentListResponse;
import syb.moviepedia.comment.dto.response.CommentResponse;
import syb.moviepedia.comment.repository.CommentRepository;
import syb.moviepedia.common.exception.*;
import syb.moviepedia.like.repository.LikeRepository;
import syb.moviepedia.member.domain.Member;
import syb.moviepedia.member.repository.MemberRepository;
import syb.moviepedia.movie.domain.Movie;
import syb.moviepedia.movie.repository.MovieRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
    @Transactional
    public CommentDetailResponse getComment(Long id) {
        Comment comment = commentRepository.findById(id).orElseThrow(
                () -> new CommentNotFoundException("코멘트를 찾을 수 없습니다. id: " + id));

        return toCommentDto(comment);
    }

    // 수정 코멘트 조회
    @Transactional
    public CommentEditResponse getEditComment(Long id) {
        Comment comment = commentRepository.findById(id).orElseThrow(
                () -> new CommentNotFoundException("코멘트를 찾을 수 없습니다. id: " + id));

        return toEditCommentDto(comment);
    }

    // 모든 코멘트 목록
    @Transactional
    public CommentListResponse getAllComments(Long mvCode, String loginId) {
        Movie movie = movieRepository.findByCode(mvCode)
                .orElseThrow(() -> new MovieNotFoundException("영화를 찾을 수 없습니다. 영화 코드: " + mvCode));

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


            // 현재 로그인한 유저가 좋아요 누른 코멘트들의 좋아요 테이블의 아이디 목록
            List<Long> likeIds = likeRepository.findLikeIdsByMemberIdAndCommentIds(memberId, commentIds);

            likedIdSet = new HashSet<>(likeIds);
        }

        return CommentListResponse.builder()
                .movieId(movie.getId())
                .comments(toCommentListResponseDto(comments, loginId, likedIdSet))
                .build();
    }

    // 저장
    @Transactional
    public void saveComment(Long mvCode, CommentSaveRequest dto) {
        Movie movie = movieRepository.findByCode(mvCode).orElseThrow(
                () -> new MovieNotFoundException("영화를 찾을 수 없습니다. 영화 코드: " + mvCode));

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
                .likeCount(0)
                .build();

        commentRepository.save(comment);
        movie.saveRating(dto.rating());
    }

    @Transactional
    public void update(Long mvCode, String loginId, CommentUpdateRequest dto) {

        // 내가 작성한 코멘트찾기
        Comment comment = findMyCommentWithMovie(mvCode, dto.movieId(), loginId);

        Movie movie = comment.getMovie();

        Double oldRating = comment.getRating();
        Double newRating = dto.rating();

        comment.update(dto);

        // 평점 변화 있을 때만 업데이트
        if (!Objects.equals(oldRating, newRating)) {
            movie.updateRating(oldRating, newRating);
        }
    }

    @Transactional
    public void delete(Long mvCode, Long movieId, String loginId) {

        Comment comment = findMyCommentWithMovie(mvCode, movieId, loginId);

        Movie movie = comment.getMovie();
        movie.removeRating(comment.getRating());

        commentRepository.delete(comment);
    }

    // 내가 작성한 코멘트 조회시 영화도 함께 가져오기 (fetch join)
    private Comment findMyCommentWithMovie(Long mvCode, Long movieId, String loginId) {
        return commentRepository.findMyCommentWithMovie(mvCode, movieId, loginId)
                .orElseThrow(() -> new CommentNotFoundException("코멘트를 찾을 수 없습니다."));
    }

    // 엔티티 -> Comment Dto로 가공
    private CommentDetailResponse toCommentDto(Comment comment) {
        return CommentDetailResponse.builder()
                .nickname(comment.getNickname())
                .content(comment.getContent())
                .rating(comment.getRating())
                .likeCount(comment.getLikeCount())
                .build();
    }

    // 엔티티 -> 수정 Comment Dto로 가공
    private CommentEditResponse toEditCommentDto(Comment comment) {
        return CommentEditResponse.builder()
                .content(comment.getContent())
                .rating(comment.getRating())
                .build();
    }

    // 엔티티 -> CommentResponseDTO로 가공 (리스트)
    private List<CommentResponse> toCommentListResponseDto(
            List<Comment> comments,
            String loinId,
            Set<Long> likedIdsSet) {
        return comments.stream().map(comment ->
                        CommentResponse.builder()
                        .commentId(comment.getId())
                        .nickname(comment.getNickname())
                        .content(comment.getContent())
                        .rating(comment.getRating())
                        .likeCount(comment.getLikeCount())
                        .writtenByMe(comment.getMember().getLoginId().equals(loinId)) // 로그인 유저가 코멘트 작성자면 true
                        .likedByMe(likedIdsSet.contains(comment.getId())) // 각 코멘트에 대하여 id를 포함하고 있는지 검사, 포함하면 해당 코멘트에 좋아요 눌른 상태
                        .build())
                .toList();
    }
}
