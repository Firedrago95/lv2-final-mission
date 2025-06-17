package finalmission.running.dto.request;

import java.time.LocalTime;

public record UpdateRequest(
    LocalTime startAt,
    LocalTime endTime
) {
}
