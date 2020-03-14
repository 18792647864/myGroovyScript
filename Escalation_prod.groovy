import com.fasterxml.jackson.databind.JsonNode
import java.util.Date
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Calendar

println "Escalation_params=====payload========:" + payload

LocalDateTime localDateTime = LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3)));

Date date = Date.from( localDateTime.atZone( ZoneId.systemDefault()).toInstant());

Map<String, Object> countParams = Map.of("tenantId", payload.get("tenantId").asLong(), "ruleId", payload.get("ruleId").asLong(), "userId", 
user.get("tenantUserId").asLong(),"createDate",date,"escalation",params.get("escalation").asBoolean())
JsonNode countUserMessages = api.call("mapbots-core", "/teams/messages/triggerEscalationList", countParams)
if (countUserMessages==null){
 return false 
}
println "Escalation_params=====count========:" + countUserMessages.get("userId") + "	" + countUserMessages.get("num")


String[] times = payload.get("times").asText().split(",")



String buttonString = "<br> --<span style='color:#999d9c;font-size:10px;'>[" + user.get("userName").asText() + "]&nbsp;[" + LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0, 3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm")) + " " + user.get("tzStr").asText() + "]</span>";

String triggerNameStr = "<span style='color:#FF7F00;font-weight:bold'>" + payload.get("triggerName").asText().replaceAll("(?=(?!^)[A-Z])", " ") + "</span> ";

println "Escalation_params=====compare========:" +  countUserMessages.get("num").asText() + " <==> " + times + " ===="+payload.get("triggerName")
if (Integer.parseInt(countUserMessages.get("num").asText()) == Integer.parseInt(times[0])) {

    JsonNode tenantUser = api.call("mapbots-core", "/tenant/user/" + user.get("tenantUserId").asLong() + "/info")

    JsonNode manager = api.call("mapbots-core", "/tenant/user/" + tenantUser.get("manager").asLong() + "/info")
  if (manager==null || manager.isEmpty()) {
    println "Escalation_params=====manager is null========:"
    return false
  }

     String str = getDetailString()

     println "Escalation_params=====getDetailString========:"+str

    String level1message = params.get("level1message").asText().replace("{user}", user.get("firstName").asText())
                                                        .replace("{triggerName}", triggerNameStr)
                                                        .replace("{time0}", Integer.parseInt(times[0])+"")
                                                        .replace("{managerName}", manager.get("firstname").asText() + " " + manager.get("lastname").asText())
                                                        .replace("{time1}", Integer.parseInt(times[1])+"");

    Map<String, String> paramUser = Map.of("teamsUserId", user.get("teamsUserId"),
            "tenantUserId", user.get("tenantUserId").asLong(),
            "tenantId", user.get("tenantId").asLong(),
            "ruleId", event.getId(),
            "escalation", Boolean.parseBoolean(params.get("sendEscalation").asText()),
            "frequency", "ignore",
            "send", params.get("send").asText(),
            "businessType", "Escalation",
            "message", level1message + str +buttonString);

    return api.call("mspbots-teams", "/message/send", "post", paramUser)
} else if (Integer.parseInt(countUserMessages.get("num").asText()) == Integer.parseInt(times[1])) {

    String str = getDetailString()

     println "Escalation_params=====getDetailStrin1g11========:"+str


    JsonNode tenantUser = api.call("mapbots-core", "/tenant/user/" + user.get("tenantUserId").asLong() + "/info")

    JsonNode manager = api.call("mapbots-core", "/tenant/user/" + tenantUser.get("manager").asLong() + "/info")
  if (manager==null || manager.isEmpty()) {
    println "Escalation_params=====manager is null========:"
    return false
  }

    JsonNode managerUp = api.call("mapbots-core", "/tenant/user/" + manager.get("manager").asLong() + "/info")
  
  if (managerUp==null || managerUp.isEmpty()) {
    println "Escalation_params=====managerUp is null========:"
    return false
  }

    Map<String, Object> paramMapping = Map.of("tenantId",manager.get("tenantId").asLong(),"tenantUserId",manager.get("id").asLong())

    JsonNode userMapping = api.call("mapbots-core", "/user-mapping/user",paramMapping)

    sendToUser(manager.get("id").asLong(),Integer.parseInt(times[1]))

    String level2message = params.get("level2message").asText().replace("{user}", user.get("firstName").asText())
                                                        .replace("{triggerName}", triggerNameStr)
                                                        .replace("{time0}", Integer.parseInt(times[0])+"")
                                                        .replace("{managerName}", manager.get("firstname").asText() + " " + manager.get("lastname").asText())
                                                        .replace("{time1}", Integer.parseInt(times[1])+"")
                                                        .replace("{managerUpName}", "<span style='color:#4caf50;font-weight:bold'>"+managerUp.get("firstname").asText() + " " + managerUp.get("lastname").asText()+"</span>")
                                                        .replace("{time2}", Integer.parseInt(times[2])+"");

 
    Map<String, String> paramManager = Map.of("teamsUserId", userMapping.get("teamsUserId"),
            "tenantUserId", manager.get("id").asLong(),
            "tenantId", manager.get("tenantId").asLong(),
            "ruleId", event.getId(),
            "escalation", Boolean.parseBoolean(params.get("sendEscalation").asText()),
            "type",params.get("type").asText(),
            "send", params.get("send").asText(),
            "frequency", "ignore",
            "businessType", "Escalation",
            "message", level2message + str + buttonString);
             
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
     String str = getDetailString()

     println "Escalation_params=====getDetailStrin1g2222========:"+str

    Map<String, String> param = Map.of("teamsUserId", userMapping.get("teamsUserId"),
            "tenantUserId", managerUp.get("id").asLong(),
            "tenantId", managerUp.get("tenantId").asLong(),
            "ruleId", event.getId(),
            "escalation", Boolean.parseBoolean(params.get("sendEscalation").asText()),
	        "type",params.get("type").asText(),
            "frequency", "ignore",
            "businessType", "Escalation",
            "send", params.get("send").asText(),
            "message", level3message +str+ buttonString);
    return api.call("mspbots-teams", "/message/send", "post", param)
}

void sendToUser(Long managerId,times) {


    String str = getDetailString()

    println "Escalation_params=====getDetailStrin1g33333========:"+str

    
    String buttonStringUser = "<br> --<span style='color:#999d9c;font-size:10px;'>[" + user.get("userName").asText() + "]&nbsp;[" + LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0, 3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm")) + " " + user.get("tzStr").asText() + "]</span>";


    String triggerNameUser = "<span style='color:#FF7F00;font-weight:bold'>" + payload.get("triggerName").asText().replaceAll("(?=(?!^)[A-Z])", " ") + "</span> ";


    JsonNode manager = api.call("mapbots-core", "/tenant/user/" + managerId + "/info")

    String userMessageStr = "Dear {user}, you have triggered {triggerName} {time} times, your manager <span style='color:#4caf50;font-weight:bold'>{managerName}</span> has been notified and will reach out to you for help. Thanks!";

    String userMessage = userMessageStr.replace("{user}", user.get("firstName").asText())
                                .replace("{triggerName}", triggerNameUser)
                                .replace("{time}", times+"")
                                .replace("{managerName}", manager.get("firstname").asText() + " " + manager.get("lastname").asText());
    
    Map<String, String> toUser = Map.of("teamsUserId", user.get("teamsUserId"),
            "tenantUserId", user.get("tenantUserId").asLong(),
            "tenantId", user.get("tenantId").asLong(),
            "ruleId", event.getId(),
            "escalation", Boolean.parseBoolean(params.get("sendEscalation").asText()),
            "frequency", "ignore",
            "type","user",
            "send", params.get("send").asText(),
            "businessType", "Escalation",
            "message", userMessage +str+ buttonStringUser);



    api.call("mspbots-teams", "/message/send", "post", toUser)


}

void sendToManager(Long managerId,times) {

   String str = getDetailString()

     println "Escalation_params=====getDetailStrin1g444444========:"+str


     
    String buttonStringManager = "<br> --<span style='color:#999d9c;font-size:10px;'>[" + user.get("userName").asText() + "]&nbsp;[" + LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0, 3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm")) + " " + user.get("tzStr").asText() + "]</span>";



    String triggerNameManager = "<span style='color:#FF7F00;font-weight:bold'>" + payload.get("triggerName").asText().replaceAll("(?=(?!^)[A-Z])", " ") + "</span> ";


    JsonNode manager = api.call("mapbots-core", "/tenant/user/" + managerId + "/info")


    JsonNode managerUp = api.call("mapbots-core", "/tenant/user/" + manager.get("manager").asLong() + "/info")


    Map<String, Object> paramMapping = Map.of("tenantId",manager.get("tenantId").asLong(),"tenantUserId",manager.get("id").asLong())



    JsonNode userMapping = api.call("mapbots-core", "/user-mapping/user",paramMapping)


    String managerMessageStr = "Dear {managerName}, <span style='color:#FF7F00;font-weight:bold'>{user}</span> have triggered {triggerName} {time} times this week. Please reach out to {user} to help fixing the issue, your manager <span style='color:#4caf50;font-weight:bold'>{managerUpName}</span> has been notified.";


    String managerMessage = managerMessageStr.replace("{user}", user.get("firstName").asText())
                                        .replace("{triggerName}", triggerNameManager)
                                        .replace("{time}", times+"")
                                        .replace("{managerName}", manager.get("firstname").asText() + " " + manager.get("lastname").asText())
                                        .replace("{managerUpName}", managerUp.get("firstname").asText() + " " + managerUp.get("lastname").asText());

    Map<String, String> toUser = Map.of("teamsUserId", userMapping.get("teamsUserId"),
            "tenantUserId", manager.get("id").asLong(),
            "tenantId", manager.get("tenantId").asLong(),
            "ruleId", event.getId(),
            "escalation", Boolean.parseBoolean(params.get("sendEscalation").asText()),
	          "type",params.get("type").asText(),
            "send", params.get("send").asText(),
            "frequency", "ignore",
            "businessType", "Escalation",
            "message", managerMessage + str + buttonStringManager);

    api.call("mspbots-teams", "/message/send", "post", toUser)
}


String getDetailString() {

    int offset = 0;
    Date createDate = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String imptimeBegin = null;
    Calendar cal = Calendar.getInstance();
    cal.setTime(createDate);
    if (cal.get(Calendar.DAY_OF_WEEK) == 1) {
        cal.add(Calendar.DATE, -6);
        cal.add(Calendar.HOUR, offset);
        imptimeBegin = sdf.format(cal.getTime());
    } else if (cal.get(Calendar.DAY_OF_WEEK) == 2) {
        cal.add(Calendar.HOUR, offset);
        imptimeBegin = sdf.format(cal.getTime());
    } else {
        cal.add(Calendar.DATE, 2 - cal.get(Calendar.DAY_OF_WEEK));
        cal.add(Calendar.HOUR, offset);
        imptimeBegin = sdf.format(cal.getTime());
    }

    String detailString = "<br> --<span style='color:#999d9c;font-size:10px;'><a href='https://app.mspbots.ai/924232/report/MessageReport?tenantUserName="+user.get("userName").asText()+"&ruleId="+event.get("ruleId").asLong()+"&tenantId="+user.get("tenantId").asText()+"&createDateFrom="+imptimeBegin+"'>message</a></span>";

    return  detailString
}