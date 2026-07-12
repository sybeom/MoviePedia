package syb.moviepedia.comment.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syb.moviepedia.comment.domain.Comment;
import syb.moviepedia.comment.dto.request.CommentSaveRequest;
import syb.moviepedia.comment.dto.request.CommentUpdateRequest;
import syb.moviepedia.comment.dto.response.CommentEditResponse;
import syb.moviepedia.comment.dto.response.CommentListResponse;
import syb.moviepedia.comment.dto.response.CommentResponse;
import syb.moviepedia.comment.repository.CommentRepository;
import syb.moviepedia.common.MediaType;
import syb.moviepedia.common.SortType;
import syb.moviepedia.common.exception.*;
import syb.moviepedia.member.domain.Member;
import syb.moviepedia.member.repository.MemberRepository;
import syb.moviepedia.movie.domain.Movie;
import syb.moviepedia.movie.repository.MovieRepository;
import syb.moviepedia.tv.domain.TV;
import syb.moviepedia.tv.repsitory.TVRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentService {
    private final MovieRepository movieRepository;
    private final TVRepository tvRepo;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;


    // 수정 코멘트 조회
    @Transactional
    public CommentEditResponse getEditComment(Long id) {
        Comment comment = commentRepository.findById(id).orElseThrow(
                () -> new CommentNotFoundException("코멘트를 찾을 수 없습니다. id: " + id));

        return toEditCommentDto(comment);
    }

    // 영화 코멘트 조회
    @Transactional
    public CommentListResponse getMovieComments(
            MediaType mediaType,
            Integer mvCode,
            Pageable pageable,
            String loginId,
            SortType sortType) {

        if (mediaType != MediaType.MOVIE ) {
            throw new IllegalArgumentException(("잘못된 미디어 타입입니다."));
        }

        Movie movie = movieRepository.findByCode(mvCode)
                .orElseThrow(() -> new MovieNotFoundException("영화를 찾을 수 없습니다. 영화 코드: " + mvCode));

        Sort sort = switch (sortType) {
            case LATEST -> Sort.by(Sort.Direction.DESC, "createdDateAt");
            case OLDEST -> Sort.by(Sort.Direction.ASC, "createdDateAt");
        };

        // 정렬 조건 추가된 Pageable
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        // 코멘트 조회
        Slice<Comment> comments = commentRepository.findByCommentsMovieCode(mvCode, sortedPageable);


        return CommentListResponse.builder()
                .id(movie.getId())
                .comments(toCommentListResponseDto(comments, loginId))
                .build();
    }

    // 영화 코멘트 저장
    @Transactional
    public void saveMovieComment(Integer mvCode, MediaType mediaType, CommentSaveRequest dto) {
        if (mediaType != MediaType.MOVIE) { // 영화 타입이 아니면
            throw new IllegalArgumentException("잘못된 미디어 타입입니다. 타입: " + mediaType);
        }

        Movie mv = movieRepository.findByCode(mvCode).orElseThrow(
                () -> new MovieNotFoundException("영화를 찾을 수 없습니다. 영화 코드: " + mvCode));

        Member member = memberRepository.findByNickname(dto.nickname()).orElseThrow(
                () -> new MemberNotFoundException("멤버를 찾을 수 없습니다. 닉네임:" + dto.nickname()));

        // 영화당 1 코멘트만 가능하도록 하기 위함
        if (commentRepository.existsByMediaTypeAndCodeAndMemberId(mediaType, mvCode, member.getId())) {
            throw new CommentAlreadyExistsException("이미 해당 영화에 코멘트를 작성하였습니다.");
        }

        // 코멘트 엔티티 생성
        Comment comment = Comment.builder()
                .nickname(dto.nickname())
                .content(dto.content())
                .mediaType(mediaType)
                .code(mvCode)
                .movie(mv)
                .member(member)
                .reactionType(dto.reactionType())
                .createdDateAt(LocalDateTime.now())
                .build();

        commentRepository.save(comment);

        mv.increaseCommentStats(dto.reactionType()); // 코멘트 수, 좋아요 수 상태 업데이트
    }

    // TV 코멘트 조회
    @Transactional
    public CommentListResponse getTVComments(
            MediaType mediaType,
            Integer seriesCode,
            Integer seasonNum,
            Pageable pageable,
            String loginId,
            SortType sortType) {

        if (mediaType != MediaType.TV ) {
            throw new IllegalArgumentException(("잘못된 미디어 타입입니다."));
        }

        TV tv = tvRepo.findBySeriesCodeAndSeasonNum(seriesCode,seasonNum)
                .orElseThrow(() -> new MovieNotFoundException("TV를 찾을 수 없습니다. 시리즈 코드: " + seriesCode + ", 시즌: " + seasonNum));

        Sort sort = switch (sortType) {
            case LATEST -> Sort.by(Sort.Direction.DESC, "createdDateAt");
            case OLDEST -> Sort.by(Sort.Direction.ASC, "createdDateAt");
        };

        // 정렬 조건 추가된 Pageable
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        // 코멘트 조회
        Slice<Comment> comments = commentRepository.findByCommentsSeriesCodeAndSeasonNum(seriesCode, seasonNum, sortedPageable);


        return CommentListResponse.builder()
                .id(tv.getId())
                .comments(toCommentListResponseDto(comments, loginId))
                .build();
    }

    // TV 코멘트 저장
    @Transactional
    public void saveTVComment(Integer seriesCode, Integer seasonNum, MediaType mediaType, CommentSaveRequest dto) {
        if (mediaType != MediaType.TV) { // 영화 타입이 아니면
            throw new IllegalArgumentException("잘못된 미디어 타입입니다. 타입: " + mediaType);
        }

        TV tv = tvRepo.findBySeriesCodeAndSeasonNum(seriesCode, seasonNum).orElseThrow(
                () -> new MovieNotFoundException("TV를 찾을 수 없습니다. 시리즈 코드: " + seriesCode + ", 시즌: " + seasonNum));

        Member member = memberRepository.findByNickname(dto.nickname()).orElseThrow(
                () -> new MemberNotFoundException("멤버를 찾을 수 없습니다. 닉네임:" + dto.nickname()));

        // 영화당 1 코멘트만 가능하도록 하기 위함
        if (commentRepository.existsByMediaTypeAndCodeAndMemberId(mediaType, seriesCode, member.getId())) {
            throw new CommentAlreadyExistsException("이미 해당 TV 시즌에 코멘트를 작성하였습니다.");
        }

        // 코멘트 엔티티 생성
        Comment comment = Comment.builder()
                .mediaType(mediaType)
                .code(seriesCode)
                .seasonNum(seasonNum)
                .nickname(dto.nickname())
                .content(dto.content())
                .member(member)
                .reactionType(dto.reactionType())
                .createdDateAt(LocalDateTime.now())
                .tv(tv)
                .build();

        commentRepository.save(comment);

        tv.increaseCommentStats(dto.reactionType()); // 코멘트 수, 좋아요 수 상태 업데이트
    }

    @Transactional
    public void update(Long mvCode, String loginId, CommentUpdateRequest dto) {

        // 내가 작성한 코멘트찾기
        Comment comment = findMyCommentWithMovie(mvCode, dto.movieId(), loginId);

        comment.update(dto);
    }

    @Transactional
    public void delete(Long mvCode, Long movieId, String loginId) {

        Comment comment = findMyCommentWithMovie(mvCode, movieId, loginId);
        commentRepository.delete(comment);

        Movie movie = comment.getMovie();
        movie.decreaseCommentStats(); // 코멘트 수, 좋아요 수 상태 감소
    }

    // 내가 작성한 코멘트 조회시 영화도 함께 가져오기 (fetch join)
    private Comment findMyCommentWithMovie(Long mvCode, Long movieId, String loginId) {
        return commentRepository.findMyCommentWithMovie(mvCode, movieId, loginId)
                .orElseThrow(() -> new CommentNotFoundException("코멘트를 찾을 수 없습니다."));
    }

    // 엔티티 -> 수정 Comment Dto로 가공
    private CommentEditResponse toEditCommentDto(Comment comment) {
        return CommentEditResponse.builder()
                .content(comment.getContent())
                .build();
    }

    // 엔티티 -> CommentResponseDTO로 가공 (리스트)
    private List<CommentResponse> toCommentListResponseDto(
            Slice<Comment> comments, String loginId
    ) {
        return comments.getContent().stream()
                .map(comment -> {
                    boolean writtenByMe = Objects.equals(comment.getMember().getLoginId(), loginId);

                    return CommentResponse.builder()
                            .commentId(comment.getId())
                            .nickname(comment.getNickname())
                            .content(comment.getContent())
                            .reactionType(comment.getReactionType())
                            .createdAt(comment.getCreatedDateAt().toLocalDate())
                            .writtenByMe(writtenByMe)
                            .build();
                })
                .toList();
    }
}
