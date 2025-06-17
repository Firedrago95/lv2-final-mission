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
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@NoArgsConstructor
@Getter
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "running_session_id")
    RunningSession runningSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    Member member;

    private Participant(Long id, RunningSession runningSession, Member member) {
        setRunningSession(runningSession);
        validateCreator(runningSession, member);
        this.id = id;
        this.member = member;
    }

    private void validateCreator(RunningSession runningSession, Member member) {
        if (runningSession.getCreator().equals(member)) {
            throw new ReservationException("세션 생성자는 참가자가 될 수 없습니다.");
        }
    }

    public void setRunningSession(RunningSession runningSession) {
        this.runningSession = runningSession;
        if (!runningSession.contains(this)) {
            runningSession.addParticipant(this);
        }
    }

    public static Participant createWithoutId(RunningSession runningSession, Member member) {
        return new Participant(null, runningSession, member);
    }

    public boolean hasSameRunningSession(RunningSession runningSession) {
        return this.runningSession.equals(runningSession);
    }

    public boolean hasMember(Member findMember) {
        return this.member.equals(findMember);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Participant that = (Participant) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
