package ssafy.d210.backend.service;
//
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.response.lecture.*;
import ssafy.d210.backend.entity.*;
import ssafy.d210.backend.enumeration.response.HereStatus;
import ssafy.d210.backend.repository.*;
import ssafy.d210.backend.util.ResponseUtil;

import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class LectureServiceImpl implements LectureService{

    private final UserRepository userRepository;
    private final LectureRepository lectureRepository;
    private final SubLectureRepository subLectureRepository;
    private final UserLectureRepository userLectureRepository;
    private final UserLectureTimeRepository userLectureTimeRepository;
    private final ResponseUtil responseUtil;

    @Override
    public ResponseSuccessDto<List<LectureInfoListResponse>> getLecturesByCategory(String category, int page) {
        // 카테고리별 강의 목록 조회
        int offset = (page - 1) * 12;
        List<LectureInfoListResponse> lectures = lectureRepository.getLecturesByCategory(category, offset);
        log.info("Category {} page {} lectures: {}", category, page, lectures);

        // 결과 검증 및 로깅
        if (lectures.isEmpty()) {
            log.warn("No lectures found for category {}", category);
        }

        ResponseSuccessDto<List<LectureInfoListResponse>> res = responseUtil.successResponse(lectures, HereStatus.SUCCESS_LECTURE_CATEGORY);
        return res;
    }

    @Override
    public ResponseSuccessDto<RecommendedLectureResponse> getRecommendedLectures() {

        RecommendedLectureResponse lectures = new RecommendedLectureResponse();
        List<LectureInfoListResponse> mostCompletedLectures = lectureRepository.getMostFinishedLectures();
        List<LectureInfoListResponse> randomLectures = lectureRepository.getRandomLectures();
        List<LectureInfoListResponse> recentLectures = lectureRepository.getNewestLectures();

        lectures.setMostCompletedLectures(mostCompletedLectures);
        lectures.setRandomLectures(randomLectures);
        lectures.setRecentLectures(recentLectures);

        ResponseSuccessDto<RecommendedLectureResponse> res = responseUtil.successResponse(lectures, HereStatus.SUCCESS_LECTURE);
        return res;
    }

    @Override
    public ResponseSuccessDto<LectureDetailResponse> getLectureDetail(Long lectureId, Long userId) {
        LectureDetail lecture = lectureRepository.getLectureById(lectureId);
        log.info("result: {}", lecture.getTitle());

        if (lecture == null) {
            log.warn("No lecture found for lectureId {}", lectureId);
            return responseUtil.successResponse(null, HereStatus.SUCCESS_LECTURE_DETAIL);
        }

        LectureDetailResponse lectureDetail = convertToLectureDetailResponse(lecture);
//        LectureDetailResponse lectureDetail = LectureDetailResponse.builder().build();
        List<Integer> subLectureIdList = lectureRepository.getSublecturesById(lectureId);

        if (subLectureIdList.isEmpty()) {
            log.warn("No subLecture found for lectureId {}", lectureId);
        } else {
            List<SubLectureDetailResponse> subLectureDetail = lectureRepository.getUserLectureTime(subLectureIdList);
            if (subLectureDetail.isEmpty()) log.warn("No userLectureTime found for subLectureIdList {}", subLectureIdList);
            lectureDetail.setSubLectures(subLectureDetail);
            log.info("subLectureDetail: {}", subLectureDetail.get(0));
        }

        UserLecture userLecture = userLectureRepository.getUserLectureById(lectureId, userId);

        if (userLecture == null) {
            log.warn("No userLecture found for lectureId {} and userId {}", lectureId, userId);
        } else {
            lectureDetail.setUserLectureId(userLecture.getId());
            lectureDetail.setRecentLectureId(userLecture.getRecentLectureId());
        }

        int studentCount = userLectureRepository.countUserLectureByLectureId(lectureId);
        lectureDetail.setStudentCount(studentCount);

        log.info("Lecture Detail : {}", lectureDetail);

        ResponseSuccessDto<LectureDetailResponse> res = responseUtil.successResponse(lectureDetail, HereStatus.SUCCESS_LECTURE_DETAIL);
        return res;
    }

    @Override
    public ResponseSuccessDto<LectureSearchResponse> searchLectures(String keyword, int page) {
        int pageSize = 12;

        // 전체 결과 수
        int total = lectureRepository.countLecturesByKeyword(keyword);

        // pageable 객체 생성
        Pageable pageable = PageRequest.of(page-1, pageSize);

        // 페이징 처리된 검색 결과 조회
        List<LectureInfoResponse> pagedList = lectureRepository.searchLecturesByKeywordPaged(keyword, pageable);


        LectureSearchResponse response = new LectureSearchResponse();
        response.setTotalResults(total);
        response.setCurrentPage(page);
        response.setSearchResults(pagedList);
        return responseUtil.successResponse(response, HereStatus.SUCCESS_LECTURE_SEARCH);
    }

    @Override
    public ResponseSuccessDto<Object> purchaseLecture(Long userId, Long lectureId) {
        UserLecture userLecture = new UserLecture();
        Optional<User> user = userRepository.findById(userId);
        Optional<Lecture> lecture = lectureRepository.findById(lectureId);
        userLecture.createUserLecture(user.get(), lecture.get());

        UserLecture savedUserLecture = userLectureRepository.save(userLecture);

        List<SubLecture> subLectureList = subLectureRepository.findSubLectureByLectureIdOrderById(lectureId);

        for (SubLecture subLecture : subLectureList) {
            UserLectureTime userLectureTime = new UserLectureTime();
            userLectureTime.createUserLectureTime(savedUserLecture, subLecture);
            userLectureTimeRepository.save(userLectureTime);
        }
        ResponseSuccessDto<Object> res = responseUtil.successResponse("ok", HereStatus.SUCCESS_LECTURE_BUY);
        return res;
    }

    @Override
    public ResponseSuccessDto<List<LectureResponse>> getPurchasedLectures(Long userId) {
        // 사용자 ID로 구매한 강의 목록 조회
        List<LectureProfile> lectureProfiles = lectureRepository.getPurchasedLectures(userId);
        log.info("UserId: {} lectures: {}", userId, lectureProfiles);

        // 결과 검증 및 로깅
        if (lectureProfiles.isEmpty()) {
            log.warn("No lectures found for userId {}", userId);
        }

        List<LectureResponse> lectures = lectureProfiles.stream()
                .map(this::convertToLectureResponse)
                .toList();

        ResponseSuccessDto<List<LectureResponse>> res = responseUtil.successResponse(lectures, HereStatus.SUCCESS_LECTURE);
        return res;
    }

    @Override
    public ResponseSuccessDto<List<LectureResponse>> getParticipatedLectures(Long userId) {
        // 사용자가 참여한 강의 목록 조회
        List<LectureProfile> lectureProfiles = lectureRepository.getParticipatedLectures(userId);
        log.info("UserId: {} lectures: {}", userId, lectureProfiles);

        // 결과 검증 및 로깅
        if (lectureProfiles.isEmpty()) {
            log.warn("No lectures found for userId {}", userId);
        }

        List<LectureResponse> lectures = lectureProfiles.stream()
                .map(this::convertToLectureResponse)
                .toList();

        ResponseSuccessDto<List<LectureResponse>> res = responseUtil.successResponse(lectures, HereStatus.SUCCESS_LECTURE);
        return res;
    }

    private LectureDetailResponse convertToLectureDetailResponse(LectureDetail lectureDetail) {
        return LectureDetailResponse.builder()
                .lecturer(lectureDetail.getLecturer())
                .lectureUrl(lectureDetail.getLectureUrl())
                .price(lectureDetail.getPrice())
                .goal(lectureDetail.getGoal())
                .title(lectureDetail.getTitle())
                .description(lectureDetail.getDescription())
                .lectureId(lectureDetail.getLectureId())
                .categoryName(lectureDetail.getCategoryName())
                .build();
    }

    private LectureResponse convertToLectureResponse(LectureProfile profile) {
        int subLectureCount = subLectureRepository.countSubLecturesByLectureId(profile.getLectureId());
        int finishedSubLectureCount = userLectureTimeRepository.countUserLectureTimesByLectureId(profile.getLectureId());

        return LectureResponse.builder()
                .isLecturer(profile.getIsLecturer())
                .title(profile.getTitle())
                .lecturer(profile.getLecturer())
                .categoryName(profile.getCategoryName())
                .lectureId(profile.getLectureId())
                .learningRate(subLectureCount * 1.0 / finishedSubLectureCount)
                .build();
    }
}
