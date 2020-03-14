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

String buttonString = "<br> --<span style='color:#999d9c;font-size:10px;'>[" + user.get("userName").asText() + "]&nbsp;[" + LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0, 3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm")) + " " + user.get("tzStr").asText() + "]</span>");

String triggerName = "<span style='color:#FF7F00;font-weight:bold'>" + payload.get("triggerName").asText().replaceAll("(?=(?!^)[A-Z])", " ") + "</span> ";

if (Integer.parseInt(countUserMessages.get("num").asText()) == Integer.parseInt(times[0])) {
    println "Escalation_:tenantUserId = " + user.get("tenantUserId").asLong()
    JsonNode tenantUser = api.call("mapbots-core", "/tenant/user/" + user.get("tenantUserId").asLong() + "/info")

    JsonNode manager = api.call("mapbots-core", "/tenant/user/" + tenantUser.get("manager").asLong() + "/info")

  
    
    String level1message = params.get("level1message").asText().replace("{user}", user.get("firstName").asText())
                                                        .replace("{triggerName}", triggerName)
                                                        .replace("{time0}", Integer.parseInt(times[0])+"")
                                                        .replace("{managerName}", manager.get("firstname").asText() + " " + manager.get("lastname").asText())
                                                        .replace("{time1}", Integer.parseInt(times[1])+"")

    Map<String, String> paramUser = Map.of("teamsUserId", user.get("teamsUserId"),
            "tenantUserId", user.get("tenantUserId").asLong(),
            "tenantId", user.get("tenantId").asLong(),
            "ruleId", payload.get("ruleId"),
            "escalation", Boolean.parseBoolean(params.get("messageescalation").asText()),
            "frequency", params.get("frequency").asText(),
            "message", level1message + buttonString;
    return api.call("mspbots-teams", "/message/send", "post", paramUser)
} else if (Integer.parseInt(countUserMessages.get("num").asText()) == Integer.parseInt(times[1])) {

     println "Escalation_:tenantUserId = " + user.get("tenantUserId").asLong() + ":::::manager:" + tenantUser.get("manager").asLong()

    JsonNode tenantUser = api.call("mapbots-core", "/tenant/user/" + user.get("tenantUserId").asLong() + "/info")

    JsonNode manager = api.call("mapbots-core", "/tenant/user/" + tenantUser.get("manager").asLong() + "/info")

    JsonNode managerUp = api.call("mapbots-core", "/tenant/user/" + manager.get("manager").asLong() + "/info")

    Map<String, Object> paramMapping = Map.of("tenantId",manager.get("tenantId").asLong(),"tenantUserId",manager.get("id").asLong())

    JsonNode userMapping = api.call("mapbots-core", "/user-mapping/user",paramMapping)

    sendToUser(manager.get("id").asLong(),Integer.parseInt(times[1]))

    String level2message = params.get("level2message").asText().replace("{user}", user.get("firstName").asText())
                                                        .replace("{triggerName}", triggerName)
                                                        .replace("{time0}", Integer.parseInt(times[0])+"")
                                                        .replace("{managerName}", manager.get("firstname").asText() + " " + manager.get("lastname").asText())
                                                        .replace("{time1}", Integer.parseInt(times[1])+"")
                                                        .replace("{managerUpName}", managerUp.get("firstname").asText() + " " + managerUp.get("lastname").asText())
                                                        .replace("{time2}", Integer.parseInt(times[2])+"");


    Map<String, String> paramManager = Map.of("teamsUserId", userMapping.get("teamsUserId"),
            "tenantUserId", manager.get("id").asLong(),
            "tenantId", manager.get("tenantId").asLong(),
            "ruleId", payload.get("ruleId"),
            "escalation", Boolean.parseBoolean(params.get("messageescalation").asText()),
	    "type",params.get("type").asText(),
            "frequency", params.get("frequency").asText(),
            "message", level2message + buttonString;
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

    String level3message = params.get("level3message").asText().replace("{user}", user.get("firstName").asText())
                                                        .replace("{triggerName}", triggerName)
                                                        .replace("{managerUpName}", managerUp.get("firstname").asText() + " " + managerUp.get("lastname").asText())
                                                        .replace("{time2}", Integer.parseInt(times[2])+"");


    Map<String, String> param = Map.of("teamsUserId", userMapping.get("teamsUserId"),
            "tenantUserId", managerUp.get("id").asLong(),
            "tenantId", managerUp.get("tenantId").asLong(),
            "ruleId", payload.get("ruleId"),
            "escalation", Boolean.parseBoolean(params.get("messageescalation").asText()),
	    "type",params.get("type").asText(),
            "frequency", params.get("frequency").asText(),
            "message", level3message + buttonString;
    return api.call("mspbots-teams", "/message/send", "post", param)
}

void sendToUser(Long managerId,times) {

    JsonNode manager = api.call("mapbots-core", "/tenant/user/" + managerId + "/info")

     String userMessageStr = "Dear {user}, you have triggered {triggerName}{time}times, your manager {managerName} has been notified and will reach out to you for help. Thanks!";

    String userMessage = userMessageStr.replace("{user}", user.get("firstName").asText())
                                .replace("{triggerName}", triggerName)
                                .replace("{time}", times+"")
                                .replace("{managerName}", manager.get("firstname").asText() + " " + manager.get("lastname").asText());

    Map<String, String> toUser = Map.of("teamsUserId", user.get("teamsUserId"),
            "tenantUserId", user.get("tenantUserId").asLong(),
            "tenantId", user.get("tenantId").asLong(),
            "ruleId", payload.get("ruleId"),
            "escalation", Boolean.parseBoolean(params.get("messageescalation").asText()),
            "frequency", params.get("frequency").asText(),
            "message", userMessage + buttonString;
    api.call("mspbots-teams", "/message/send", "post", toUser)
}

void sendToManager(Long managerId,times) {

    JsonNode manager = api.call("mapbots-core", "/tenant/user/" + managerId + "/info")

    JsonNode managerUp = api.call("mapbots-core", "/tenant/user/" + manager.get("manager").asLong() + "/info")

    Map<String, Object> paramMapping = Map.of("tenantId",manager.get("tenantId").asLong(),"tenantUserId",manager.get("id").asLong())

    JsonNode userMapping = api.call("mapbots-core", "/user-mapping/user",paramMapping)

    String managerMessageStr = "Dear {managerName}, {user} have triggered {triggerName} {time} times this week. Please reach out to {user} to help fixing the issue,your manager {managerUpName} has been notified.";

    String managerMessage = managerMessageStr.replace("{user}", user.get("firstName").asText())
                                        .replace("{triggerName}", triggerName)
                                        .replace("{time}", times+"")
                                        .replace("{managerName}", manager.get("firstname").asText() + " " + manager.get("lastname").asText())
                                        .replace("{managerUpName}", managerUp.get("firstname").asText() + " " + managerUp.get("lastname").asText());

    Map<String, String> toUser = Map.of("teamsUserId", userMapping.get("teamsUserId"),
            "tenantUserId", manager.get("id").asLong(),
            "tenantId", manager.get("tenantId").asLong(),
            "ruleId", payload.get("ruleId"),
            "escalation", Boolean.parseBoolean(params.get("messageescalation").asText()),
	        "type",params.get("type").asText(),
            "frequency", params.get("frequency").asText(),
            "message", managerMessage + buttonString;
    api.call("mspbots-teams", "/message/send", "post", toUser)
}

println "Escalation_end"