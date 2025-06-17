package finalmission.running.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

public record SessionSimpleResponse(
    LocalDate date,
    LocalTime startAt,
    LocalTime endTime
) {
}
