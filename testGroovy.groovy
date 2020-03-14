
import java.time.LocalDateTime

import java.time.ZonedDateTime

LocalDateTime beginDate = ZonedDateTime.parse("2020-02-23T03:30:00Z").toLocalDateTime().plusHours(Integer.parseInt("-06"))

print beginDate.getHour()