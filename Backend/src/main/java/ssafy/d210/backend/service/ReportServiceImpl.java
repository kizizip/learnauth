package ssafy.d210.backend.service;
//
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ssafy.d210.backend.dto.request.report.ReportRequest;
import ssafy.d210.backend.dto.response.report.ReportDetailResponse;
import ssafy.d210.backend.dto.response.report.ReportResponse;
import ssafy.d210.backend.entity.Report;
import ssafy.d210.backend.entity.UserLecture;
import ssafy.d210.backend.repository.ReportRepository;
import ssafy.d210.backend.repository.UserLectureRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService{

    // report data db 조회, 저장 위한 repository
    private final ReportRepository reportRepository;

    // 현재 로그인한 사용자의 UserLecture를 조회하기 위한 Repository : 구현 필요
    // JWT -> 현재 사용자 ID : 그 사용자 ID로 UserLecture 조회 "UserLectureRepository"
    // Long currentUserId = jwtUtil.getCurrentUserId(); -> 이런 식으로 id 추출
    private final UserLectureRepository userLectureRepository;

    @Override
    public List<ReportResponse> getReports(Long userId) {
        // ReportRequest, 200ok
        // userId를 이용해 UserLecture와 연관된 Report들 조회 (ReportRepository에 커스텀 메서드 추가)
        List<Report> reports = reportRepository.findByUserLectureUserId(userId);
        // 클라이언트에 반환할 DTO 리스트 만드는 변수
        List<ReportResponse> responseList = new ArrayList<>();
        // 조회된 report 순회하면서 제목 가져오기, ReportResponse DTO 생성해서 리스트 추가
        for (Report report : reports) {
            String title = report.getUserLecture().getLecture().getTitle();
            responseList.add(new ReportResponse(report.getId(), title, report.getReportType()));
        }
        return responseList;
    }

    @Override
    public ReportDetailResponse getReportDetail(Long reportId) {
        // reportId로 DB에서 report entity 찾기
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("해당 신고 id가 없습니다."));
        // 왜 신고 내역 없는지 확인
        if (report.getUserLecture().getLecture() == null) {
            throw new IllegalStateException("해당 신고에 연결된 강의 정보가 없습니다.");
        }
        // 강의 제목 가져오기 : report -> userLecture -> lecture -> title
        String title = report.getUserLecture().getLecture().getTitle();
        // 강의 제목, report entity에서 ReportType, ReportContent 바로 가져오기
        return new ReportDetailResponse(title, report.getReportType(), report.getReportContent());
    }

    @Override
    public ReportResponse createReport(ReportRequest request, Long userId) {

        // JWTToken으로 사용자 정보 가져오기 : 수정 필요할 수도
        UserLecture userLecture = userLectureRepository
                .findByUserIdAndLectureId(userId, request.getLectureId())
                .orElseThrow(() -> new RuntimeException("UserLecture가 없습니다."));

        // 새 report entity 생성
        Report report = new Report();
        report.setReportType(request.getReportType());
        report.setReportContent(request.getReportContent());
        Report savedReport = reportRepository.save(report);

        // UserLecture와 report 연관관계 설정
        userLecture.setReport(savedReport);
        userLectureRepository.save(userLecture);

        String title = userLecture.getLecture().getTitle();
        return new ReportResponse(savedReport.getId(), title, savedReport.getReportType());
    }
}
