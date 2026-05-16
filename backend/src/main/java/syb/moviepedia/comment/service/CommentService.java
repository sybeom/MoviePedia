package syb.moviepedia.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import syb.moviepedia.comment.dto.CommentDto;
import syb.moviepedia.comment.repository.CommentRepository;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;

    public void saveComment(Long movieId, CommentDto dto) {

    }
}
