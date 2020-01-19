import com.fasterxml.jackson.databind.JsonNode
import java.util.Date
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

println "Escalation_params=============:" + params

LocalDateTime localDateTime = LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3)));

Date date = Date.from( localDateTime.atZone( ZoneId.systemDefault()).toInstant());

Map<String, Object> countParams = Map.of("tenantId", payload.get("tenantId").asLong(), "ruleId", payload.get("ruleId").asLong(), "userId", 
user.get("tenantUserId").asLong(),"createDate",date,"escalation",Boolean.parseBoolean(params.get("escalation").asText()))
JsonNode countUserMessages = api.call("mapbots-core", "/teams/messages/triggerEscalationList", countParams)

println "Escalation_params=====countUserMessages========:" + countUserMessages


String[] times = payload.get("times").asText().split(",")

println "Escalation_params=====times========:" +  times


String buttonString = "<br> --<span style='color:#999d9c;font-size:10px;'>[" + user.get("userName").asText() + "]&nbsp;[" + LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0, 3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm")) + " " + user.get("tzStr").asText() + "]</span>";

String triggerNameStr = "<span style='color:#FF7F00;font-weight:bold'>" + payload.get("triggerName").asText().replaceAll("(?=(?!^)[A-Z])", " ") + "</span> ";

println "Escalation_params=====compare========:" +  countUserMessages.get("num").asText() + "  " + times[0]
if (Integer.parseInt(countUserMessages.get("num").asText()) == Integer.parseInt(times[0])) {

    JsonNode tenantUser = api.call("mapbots-core", "/tenant/user/" + user.get("tenantUserId").asLong() + "/info")

    JsonNode manager = api.call("mapbots-core", "/tenant/user/" + tenantUser.get("manager").asLong() + "/info")


    String level1message = params.get("level1message").asText().replace("{user}", user.get("firstName").asText())
                                                        .replace("{triggerName}", triggerNameStr)
                                                        .replace("{time0}", Integer.parseInt(times[0])+"")
                                                        .replace("{managerName}", manager.get("firstname").asText() + " " + manager.get("lastname").asText())
                                                        .replace("{time1}", Integer.parseInt(times[1])+"");

    Map<String, String> paramUser = Map.of("teamsUserId", user.get("teamsUserId"),
            "tenantUserId", user.get("tenantUserId").asLong(),
            "tenantId", user.get("tenantId").asLong(),
            "ruleId", event.getId(),
            "escalation", Boolean.parseBoolean(params.get("messageescalation").asText()),
            "frequency", params.get("frequency").asText(),
            "send", params.get("send").asText(),
            "businessType", event.getScope(),
            "message", level1message + buttonString);
  println "Escalation_params=====compare========:" +  paramUser
    return api.call("mspbots-teams", "/message/send", "post", paramUser)
} else if (Integer.parseInt(countUserMessages.get("num").asText()) == Integer.parseInt(times[1])) {

    JsonNode tenantUser = api.call("mapbots-core", "/tenant/user/" + user.get("tenantUserId").asLong() + "/info")
   println 11
    JsonNode manager = api.call("mapbots-core", "/tenant/user/" + tenantUser.get("manager").asLong() + "/info")
  println 2222
    JsonNode managerUp = api.call("mapbots-core", "/tenant/user/" + manager.get("manager").asLong() + "/info")
 println 33333
    Map<String, Object> paramMapping = Map.of("tenantId",manager.get("tenantId").asLong(),"tenantUserId",manager.get("id").asLong())
 println 4444
    JsonNode userMapping = api.call("mapbots-core", "/user-mapping/user",paramMapping)
 println 55555
    sendToUser(manager.get("id").asLong(),Integer.parseInt(times[1]))
 println 66666
    String level2message = params.get("level2message").asText().replace("{user}", user.get("firstName").asText())
                                                        .replace("{triggerName}", triggerNameStr)
                                                        .replace("{time0}", Integer.parseInt(times[0])+"")
                                                        .replace("{managerName}", manager.get("firstname").asText() + " " + manager.get("lastname").asText())
                                                        .replace("{time1}", Integer.parseInt(times[1])+"")
                                                        .replace("{managerUpName}", managerUp.get("firstname").asText() + " " + managerUp.get("lastname").asText())
                                                        .replace("{time2}", Integer.parseInt(times[2])+"");

 println 777777
    Map<String, String> paramManager = Map.of("teamsUserId", userMapping.get("teamsUserId"),
            "tenantUserId", manager.get("id").asLong(),
            "tenantId", manager.get("tenantId").asLong(),
            "ruleId", payload.get("ruleId"),
            "escalation", Boolean.parseBoolean(params.get("messageescalation").asText()),
            "type",params.get("type").asText(),
            "send", params.get("send").asText(),
            "frequency", params.get("frequency").asText(),
            "message", level2message + buttonString);
             println 8888888
    return api.call("mspbots-teams", "/message/send", "post", paramManager)
} else if (Integer.parseInt(countUserMessages.get("num").asText()) == Integer.parseInt(times[2])) {


    JsonNode tenantUser = api.call("mapbots-core", "/tenant/user/" + user.get("tenantUserId").asLong() + "/info")

    JsonNode manager = api.call("mapbots-core", "/tenant/user/" + tenantUser.get("manager").asLong() + "/info")

    JsonNode managerUp = api.call("mapbots-core", "/tenant/user/" + manager.get("manager").asLong() + "/info")

    Map<String, Object> paramMapping = Map.of("tenantId",managerUp.get("tenantId").asLong(),"tenantUserId",managerUp.get("id").asLong())

    JsonNode userMapping = api.call("mapbots-core", "/user-mapping/user",paramMapping)

    sendToUser(manager.get("id").asLong(),Integer.parseInt(times[2]))



    sendToManager(manager.get("id").asLong(),Integer.parseInt(times[2]))


    String level3message = params.get("level3message").asText().replace("{user}", user.get("firstName").asText())
                                                        .replace("{triggerName}", triggerNameStr)
                                                        .replace("{managerUpName}", managerUp.get("firstname").asText() + " " + managerUp.get("lastname").asText())
                                                        .replace("{time2}", Integer.parseInt(times[2])+"");


    Map<String, String> param = Map.of("teamsUserId", userMapping.get("teamsUserId"),
            "tenantUserId", managerUp.get("id").asLong(),
            "tenantId", managerUp.get("tenantId").asLong(),
            "ruleId", payload.get("ruleId"),
            "escalation", Boolean.parseBoolean(params.get("messageescalation").asText()),
	        "type",params.get("type").asText(),
            "frequency", params.get("frequency").asText(),
             "send", params.get("send").asText(),
            "message", level3message + buttonString);
    return api.call("mspbots-teams", "/message/send", "post", param)
}

void sendToUser(Long managerId,times) {


    
    String buttonStringUser = "<br> --<span style='color:#999d9c;font-size:10px;'>[" + user.get("userName").asText() + "]&nbsp;[" + LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0, 3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm")) + " " + user.get("tzStr").asText() + "]</span>";


    String triggerNameUser = "<span style='color:#FF7F00;font-weight:bold'>" + payload.get("triggerName").asText().replaceAll("(?=(?!^)[A-Z])", " ") + "</span> ";


    JsonNode manager = api.call("mapbots-core", "/tenant/user/" + managerId + "/info")

    String userMessageStr = "Dear {user}, you have triggered {triggerName}{time}times, your manager {managerName} has been notified and will reach out to you for help. Thanks!";

    String userMessage = userMessageStr.replace("{user}", user.get("firstName").asText())
                                .replace("{triggerName}", triggerNameUser)
                                .replace("{time}", times+"")
                                .replace("{managerName}", manager.get("firstname").asText() + " " + manager.get("lastname").asText());
    println  "sendToUser66666"
    Map<String, String> toUser = Map.of("teamsUserId", user.get("teamsUserId"),
            "tenantUserId", user.get("tenantUserId").asLong(),
            "tenantId", user.get("tenantId").asLong(),
            "ruleId", payload.get("ruleId"),
            "escalation", Boolean.parseBoolean(params.get("messageescalation").asText()),
            "frequency", params.get("frequency").asText(),
            "type","user",
            "send", params.get("send").asText(),
            "message", userMessage + buttonStringUser);



    api.call("mspbots-teams", "/message/send", "post", toUser)


}

void sendToManager(Long managerId,times) {



     
    String buttonStringManager = "<br> --<span style='color:#999d9c;font-size:10px;'>[" + user.get("userName").asText() + "]&nbsp;[" + LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0, 3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm")) + " " + user.get("tzStr").asText() + "]</span>";



    String triggerNameManager = "<span style='color:#FF7F00;font-weight:bold'>" + payload.get("triggerName").asText().replaceAll("(?=(?!^)[A-Z])", " ") + "</span> ";


    JsonNode manager = api.call("mapbots-core", "/tenant/user/" + managerId + "/info")


    JsonNode managerUp = api.call("mapbots-core", "/tenant/user/" + manager.get("manager").asLong() + "/info")


    Map<String, Object> paramMapping = Map.of("tenantId",manager.get("tenantId").asLong(),"tenantUserId",manager.get("id").asLong())



    JsonNode userMapping = api.call("mapbots-core", "/user-mapping/user",paramMapping)


    String managerMessageStr = "Dear {managerName}, {user} have triggered {triggerName} {time} times this week. Please reach out to {user} to help fixing the issue,your manager {managerUpName} has been notified.";


    String managerMessage = managerMessageStr.replace("{user}", user.get("firstName").asText())
                                        .replace("{triggerName}", triggerNameManager)
                                        .replace("{time}", times+"")
                                        .replace("{managerName}", manager.get("firstname").asText() + " " + manager.get("lastname").asText())
                                        .replace("{managerUpName}", managerUp.get("firstname").asText() + " " + managerUp.get("lastname").asText());

    Map<String, String> toUser = Map.of("teamsUserId", userMapping.get("teamsUserId"),
            "tenantUserId", manager.get("id").asLong(),
            "tenantId", manager.get("tenantId").asLong(),
            "ruleId", payload.get("ruleId"),
            "escalation", Boolean.parseBoolean(params.get("messageescalation").asText()),
	        "type",params.get("type").asText(),
            "send", params.get("send").asText(),
            "frequency", params.get("frequency").asText(),
            "message", managerMessage + buttonStringManager);

    api.call("mspbots-teams", "/message/send", "post", toUser)
}

println "Escalation_end"