package finalmission.running.domain;

import finalmission.member.domain.Member;
import finalmission.running.exception.ReservationException;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@NoArgsConstructor
@Getter
public class RunningSession {

    public static final int MINIMUM_MINUTES = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    Member creator;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "runningSession")
    List<Participant> participants = new ArrayList<>();

    LocalDate date;

    LocalTime startAt;

    LocalTime endTime;

    private RunningSession(Long id, Member creator, LocalDate date, LocalTime startAt, LocalTime endTime) {
        validateDate(date, startAt);
        validateTime(startAt, endTime);
        this.id = id;
        this.creator = creator;
        this.date = date;
        this.startAt = startAt;
        this.endTime = endTime;
    }

    private void validateDate(LocalDate date, LocalTime startAt) {
        if (date.isBefore(LocalDate.now())) {
            throw new ReservationException("과거 날짜에 예약할 수 없습니다.");
        }
        if (date.isEqual(LocalDate.now()) && startAt.isBefore(LocalTime.now())) {
            throw new ReservationException("지나간 시간에 예약할 수 없습니다.");
        }
    }

    private void validateTime(LocalTime startAt, LocalTime endTime) {
        if (!startAt.isBefore(endTime)) {
            throw new ReservationException("시작시간은 완료시간보다 이전이어야 합니다.");
        }
        if (startAt.plusMinutes(MINIMUM_MINUTES).isAfter(endTime)) {
            throw new ReservationException("세션은 최소 10분 이상 지속되어야합니다.");
        }
    }

    public static RunningSession createWithoutId(Member creator, LocalDate date, LocalTime startAt, LocalTime endTime) {
        return new RunningSession(null, creator, date, startAt, endTime);
    }

    public boolean contains(Participant participant) {
        return this.participants.contains(participant);
    }

    public void addParticipant(Participant participant) {
        this.participants.add(participant);
        if (!participant.hasSameRunningSession(this)) {
            participant.setRunningSession(this);
        }
    }

    public boolean isCreatorOrParticipants(Member findMember) {
        return isCreator(findMember)
            || participants.stream()
            .anyMatch(participant -> participant.hasMember(findMember));
    }

    public boolean isCreator(Member member) {
        return this.creator.equals(member);
    }

    public void setStartAt(LocalTime startAt) {
        this.startAt = startAt;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RunningSession that = (RunningSession) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
