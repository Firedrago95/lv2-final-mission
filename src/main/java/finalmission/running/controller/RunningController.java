package finalmission.running.controller;

import finalmission.member.auth.argumentresolver.LoginMember;
import finalmission.member.dto.response.LoginInfo;
import finalmission.running.dto.request.ReservationRequest;
import finalmission.running.dto.request.UpdateRequest;
import finalmission.running.dto.response.ReservationResponse;
import finalmission.running.dto.response.SessionSimpleResponse;
import finalmission.running.service.RunningService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RunningController {

    private final RunningService runningService;

    @PostMapping("/runningSessions")
    public ResponseEntity<ReservationResponse> createRunningSession(
        @RequestBody ReservationRequest request,
        @LoginMember LoginInfo loginInfo) {
        return ResponseEntity.ok(runningService.createRunningReservation(request, loginInfo));
    }

    @PostMapping("/runningSessions/{id}/participants")
    public ResponseEntity<ReservationResponse> joinRunningSession(
        @PathVariable("id") Long id,
        @LoginMember LoginInfo loginInfo) {
        return ResponseEntity.ok(runningService.joinRunningReservation(id, loginInfo));
    }

    @GetMapping("/runningSessions")
    public ResponseEntity<List<SessionSimpleResponse>> searchSimpleInfos(
        @LoginMember LoginInfo loginInfo
    ) {
        return ResponseEntity.ok(runningService.searchAllSimpleInfos());
    }

    @GetMapping("/runningSessions/{id}")
    public ResponseEntity<ReservationResponse> searchInfos(
        @LoginMember LoginInfo loginInfo,
        @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(runningService.searchInfos(id, loginInfo));
    }

    @PatchMapping("/runningSessions/{id}")
    public ResponseEntity<ReservationResponse> updateRunningTime(
        @LoginMember LoginInfo loginInfo,
        @PathVariable("id") Long id,
        @RequestBody UpdateRequest updateRequest
    ) {
        return ResponseEntity.ok(runningService.updateRunningTime(id, updateRequest, loginInfo));
    }

    @DeleteMapping("/runningSession/{id}")
    public ResponseEntity<Void> deleteSession(
        @LoginMember LoginInfo loginInfo,
        @PathVariable("id") Long id
    ) {
        runningService.deleteSession(id, loginInfo);
        return ResponseEntity.noContent().build();
    }
}
