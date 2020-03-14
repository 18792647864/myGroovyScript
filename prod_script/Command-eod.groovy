import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

println "TeamsCommand----eod"


println "payload"
println payload

println "user"
println user


String messageReceived = payload.get("message").asText()

if (messageReceived.toLowerCase().contains("eod")) {

    Map<String, String> param = Map.of("tenantId", user.get("tenantId").asLong(),
                                    "tenantUserId",user.get("tenantUserId").asLong(),
                                    "enteredDateStart",LocalDateTime.of(LocalDate.now(), LocalTime.MIN),
                                    "enteredDateEnd",LocalDateTime.of(LocalDate.now(), LocalTime.MAX))

    api.call("mspbots-teams", "/utilization/check", "post",param)
}

