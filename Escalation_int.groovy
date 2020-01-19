import com.fasterxml.jackson.databind.JsonNode
import java.util.Date
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

println "Escalation_params:"
println params

LocalDateTime localDateTime = LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3)));

Date date = Date.from( localDateTime.atZone( ZoneId.systemDefault()).toInstant());

Map<String, Object> params = Map.of("tenantId", payload.get("tenantId").asLong(), "ruleId", payload.get("ruleId").asLong(), "userId", 
user.get("tenantUserId").asLong(),"createDate",date,"escalation",Boolean.parseBoolean(params.get("escalation").asText()))
JsonNode countUserMessages = api.call("mapbots-core", "/teams/messages/triggerEscalationList", params)

println countUserMessages

println payload

String[] times = payload.get("times").asText().split(",")

println times

println Integer.parseInt(countUserMessages.get("num").asText())

println Integer.parseInt(times[0])

if (Integer.parseInt(countUserMessages.get("num").asText()) == Integer.parseInt(times[0])) {
    println "Escalation_:tenantUserId = " + user.get("tenantUserId").asLong()
    JsonNode tenantUser = api.call("mapbots-core", "/tenant/user/" + user.get("tenantUserId").asLong() + "/info")

    JsonNode manager = api.call("mapbots-core", "/tenant/user/" + tenantUser.get("manager").asLong() + "/info")

    Map<String, String> paramUser = Map.of("teamsUserId", user.get("teamsUserId"),
            "tenantUserId", user.get("tenantUserId").asLong(),
            "tenantId", user.get("tenantId").asLong(),
            "ruleId", payload.get("ruleId"),
            "escalation", Boolean.parseBoolean(params.get("messageescalation").asText()),
            "frequency", params.get("frequency").asText(),
            "message", "Dear " + user.get("firstName").asText()
            + ", you have triggered <span style='color:#FF7F00;font-weight:bold'>" + payload.get("triggerName").asText().replaceAll("(?=(?!^)[A-Z])", " ") + "</span> " + Integer.parseInt(times[0]) + " times this week."
            + " If you need help to fix this issue, please reach out to " + manager.get("firstname").asText() + " " + manager.get("lastname").asText()
            + ". The system will notify " + manager.get("firstname").asText() + " " + manager.get("lastname").asText() + " after " + Integer.parseInt(times[1]) + " times."
            + "<br> --<span style='color:#999d9c;font-size:10px;'>[" + user.get("userName").asText() + "]&nbsp;[" + LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0, 3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm")) + " " + user.get("tzStr").asText() + "]</span>");
    return api.call("mspbots-teams", "/message/send", "post", paramUser)
} else if (Integer.parseInt(countUserMessages.get("num").asText()) == Integer.parseInt(times[1])) {

     println "Escalation_:tenantUserId = " + user.get("tenantUserId").asLong() + ":::::manager:" + tenantUser.get("manager").asLong()

    JsonNode tenantUser = api.call("mapbots-core", "/tenant/user/" + user.get("tenantUserId").asLong() + "/info")

    JsonNode manager = api.call("mapbots-core", "/tenant/user/" + tenantUser.get("manager").asLong() + "/info")

    JsonNode managerUp = api.call("mapbots-core", "/tenant/user/" + manager.get("manager").asLong() + "/info")

    Map<String, Object> paramMapping = Map.of("tenantId",manager.get("tenantId").asLong(),"tenantUserId",manager.get("id").asLong())

    JsonNode userMapping = api.call("mapbots-core", "/user-mapping/user",paramMapping)

    sendToUser(manager.get("id").asLong(),Integer.parseInt(times[1]))

    Map<String, String> paramManager = Map.of("teamsUserId", userMapping.get("teamsUserId"),
            "tenantUserId", manager.get("id").asLong(),
            "tenantId", manager.get("tenantId").asLong(),
            "ruleId", payload.get("ruleId"),
            "escalation", Boolean.parseBoolean(params.get("messageescalation").asText()),
	        "type",params.get("type").asText(),
            "frequency", params.get("frequency").asText(),
            "message", "Dear " + manager.get("firstname").asText()
            + ", " + user.get("userName").asText()
            + " has triggered <span style='color:#FF7F00;font-weight:bold'>" + payload.get("triggerName").asText().replaceAll("(?=(?!^)[A-Z])", " ") + "</span> " + Integer.parseInt(times[1]) + " times this week. "
            + "Please reach out to " + user.get("firstName").asText() + " to help fix the issue"
            + ". The system will notify " + managerUp.get("firstname").asText() + " " + managerUp.get("lastname").asText() + " after " + Integer.parseInt(times[2]) + " times."
            + "<br> --<span style='color:#999d9c;font-size:10px;'>[" + user.get("userName").asText() + "]&nbsp;[" + LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0, 3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm")) + " " + user.get("tzStr").asText() + "]</span>");
    return api.call("mspbots-teams", "/message/send", "post", paramManager)
} else if (Integer.parseInt(countUserMessages.get("num").asText()) == Integer.parseInt(times[2])) {

    println "Escalation_:tenantUserId = " + user.get("tenantUserId").asLong() + 
            ":::::manager:" + tenantUser.get("manager").asLong()
            + ":::::managerUp:" + managerUp.get("id").asLong() 

    JsonNode tenantUser = api.call("mapbots-core", "/tenant/user/" + user.get("tenantUserId").asLong() + "/info")

    JsonNode manager = api.call("mapbots-core", "/tenant/user/" + tenantUser.get("manager").asLong() + "/info")

    JsonNode managerUp = api.call("mapbots-core", "/tenant/user/" + manager.get("manager").asLong() + "/info")

    Map<String, Object> paramMapping = Map.of("tenantId",managerUp.get("tenantId").asLong(),"tenantUserId",managerUp.get("id").asLong())

    JsonNode userMapping = api.call("mapbots-core", "/user-mapping/user",paramMapping)

    sendToUser(manager.get("id").asLong(),Integer.parseInt(times[2]))

    sendToManager(manager.get("id").asLong(),Integer.parseInt(times[2]))

    Map<String, String> param = Map.of("teamsUserId", userMapping.get("teamsUserId"),
            "tenantUserId", managerUp.get("id").asLong(),
            "tenantId", managerUp.get("tenantId").asLong(),
            "ruleId", payload.get("ruleId"),
            "escalation", Boolean.parseBoolean(params.get("messageescalation").asText()),
	        "type",params.get("type").asText(),
            "frequency", params.get("frequency").asText(),
            "message", "Dear " + managerUp.get("firstname").asText()
            +", " + user.get("userName").asText()
            +" has triggered <span style='color:#FF7F00;font-weight:bold'>" + payload.get("triggerName").asText().replaceAll("(?=(?!^)[A-Z])", " ") + "</span> " + Integer.parseInt(times[2]) + " times this week"
            +". Please reach out to "+user.get("firstName").asText()+" to help fix the issue."
            + "<br> --<span style='color:#999d9c;font-size:10px;'>[" + user.get("userName").asText() + "]&nbsp;[" + LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0, 3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm")) + " " + user.get("tzStr").asText() + "]</span>");
    return api.call("mspbots-teams", "/message/send", "post", param)
}

void sendToUser(Long managerId,times) {

    JsonNode manager = api.call("mapbots-core", "/tenant/user/" + managerId + "/info")

    Map<String, String> toUser = Map.of("teamsUserId", user.get("teamsUserId"),
            "tenantUserId", user.get("tenantUserId").asLong(),
            "tenantId", user.get("tenantId").asLong(),
            "ruleId", payload.get("ruleId"),
            "escalation", Boolean.parseBoolean(params.get("messageescalation").asText()),
            "frequency", params.get("frequency").asText(),
            "message", "Dear " +user.get("firstName").asText()
            +", you have triggered <span style='color:#FF7F00;font-weight:bold'>" + payload.get("triggerName").asText().replaceAll("(?=(?!^)[A-Z])", " ") + "</span> " + times + " times"
            +", your manager "+manager.get("firstname").asText()+" "+manager.get("lastname").asText()+" has been notified and will reach out to you for help. Thanks!"
            + "<br> --<span style='color:#999d9c;font-size:10px;'>[" + user.get("userName").asText() + "]&nbsp;[" + LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0, 3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm")) + " " + user.get("tzStr").asText() + "]</span>");
    api.call("mspbots-teams", "/message/send", "post", toUser)
}

void sendToManager(Long managerId,times) {

    JsonNode manager = api.call("mapbots-core", "/tenant/user/" + managerId + "/info")

    JsonNode managerUp = api.call("mapbots-core", "/tenant/user/" + manager.get("manager").asLong() + "/info")

    Map<String, Object> paramMapping = Map.of("tenantId",manager.get("tenantId").asLong(),"tenantUserId",manager.get("id").asLong())

    JsonNode userMapping = api.call("mapbots-core", "/user-mapping/user",paramMapping)

    Map<String, String> toUser = Map.of("teamsUserId", userMapping.get("teamsUserId"),
            "tenantUserId", manager.get("id").asLong(),
            "tenantId", manager.get("tenantId").asLong(),
            "ruleId", payload.get("ruleId"),
            "escalation", Boolean.parseBoolean(params.get("messageescalation").asText()),
	        "type",params.get("type").asText(),
            "frequency", params.get("frequency").asText(),
            "message", "Dear " + manager.get("firstname").asText()
            +", " + user.get("userName").asText()
            +" have triggered <span style='color:#FF7F00;font-weight:bold'>" + payload.get("triggerName").asText().replaceAll("(?=(?!^)[A-Z])", " ") + "</span> " + times + " times this week"
            +". Please reach out to "+user.get("firstName").asText()+" to help fixing the issue, "
            +"your manager " + managerUp.get("firstname").asText() + " " + managerUp.get("lastname").asText()+" has been notified."
            + "<br> --<span style='color:#999d9c;font-size:10px;'>[" + user.get("userName").asText() + "]&nbsp;[" + LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0, 3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm")) + " " + user.get("tzStr").asText() + "]</span>");
    api.call("mspbots-teams", "/message/send", "post", toUser)
}



println "Escalation_end"