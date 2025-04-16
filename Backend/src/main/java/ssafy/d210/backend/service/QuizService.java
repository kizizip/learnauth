package ssafy.d210.backend.service;
//
import org.springframework.stereotype.Service;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.request.quiz.QuizRequest;
import ssafy.d210.backend.dto.request.quiz.QuizResultRequest;
import ssafy.d210.backend.dto.response.quiz.QuizResponse;

import java.util.List;


public interface QuizService {

    // 강의별 퀴즈 목록 조회
    public ResponseSuccessDto<List<QuizResponse>> getQuizzes(Long lectureId);

    // 퀴즈 제출
    public ResponseSuccessDto<Boolean> submitQuiz(Long lectureId, QuizResultRequest request);

}
