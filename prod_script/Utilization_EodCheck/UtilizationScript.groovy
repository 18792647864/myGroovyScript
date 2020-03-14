import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.time.format.DateTimeFormatter;

println "Utilization ------- Utilization"

Map<String, String> param = Map.of("tenantId", payload.get("tenantId").asLong(),
                                   "tenantUserId",payload.get("tenantUserId").asLong(),
                                   "enteredDateStart",payload.get("enteredDateStart").asText(),
                                   "enteredDateEnd",payload.get("enteredDateEnd").asText())




println "payload"
println payload

println "user"
println user

JsonNode timeEntryJson = api.call("mspbots-core", "/tenant/user-time-entry/tenant-user-list", "get",param)

Map<Long, String> ticketStatus = new HashMap<Long, String>()

println "timeEntryJson"
println timeEntryJson

double sumHour = 0
for(int i = 0;i < timeEntryJson.size();i++){

    println "i:"+i


    JsonNode chargeToId = timeEntryJson.get(i).get("chargeToId")
    println "chargeToId"
    println chargeToId

    if(chargeToId != null){
        String status
        if(!ticketStatus.containsKey(chargeToId.asLong())){
            println "chargeToId"
            println timeEntryJson.get(i).get("chargeToId").asLong()
            Map<String, String> ticketParam = Map.of("cwId", chargeToId.asLong())
            JsonNode ticketJson = api.call("mspbots-core", "/tickets/selectbycwId", "get",ticketParam)
            if(ticketJson != null){

                println "ticketJson"
                println ticketJson
                status = ticketJson.get("contactName").asText()
                println "111111"
                ticketStatus.put(chargeToId.asLong(),status)
                println "2222222"
            }
           
        }else{
            status = ticketStatus.get(chargeToId)

            println "3333333"
        }
        if(status != null){
             if(params.get("statuses").toLowerCase().continue(status.toLowerCase())){
                continue
                println "44444"
            }
        }

       
    }

    println "sumHour"
    println sumHour
    println timeEntryJson.get(i)
    println timeEntryJson.get(i).get("actualHours").asLong()
    println timeEntryJson.get(i).get("actualHours").asText()
    sumHour  = sumHour + timeEntryJson.get(i).get("actualHours").asDouble()

    println "55555555"

}


double todayUtilization = sumHour/8




Map<String, String> currentyearParam = Map.of("tenantId", payload.get("tenantId").asLong())
JsonNode utilizationJson = api.call("mspbots-core", "/utilization/listcurentyear", "get",currentyearParam)
double utilYearSum = 0;
double utilMonthSum = 0;
int utilMonthCount = 0;
double utilWeekSum = 0;
int utilWeekCount = 0;
def percent
LocalDateTime currentTime =  LocalDateTime.now()
for (utilization in utilizationJson) {
    percent = utilization.get("percent").asDouble()
    LocalDateTime createTime = LocalDateTime.parse(utilization.get("createTime").asText(),DateTimeFormatter.ISO_DATE_TIME)

    utilYearSum = utilYearSum + percent
    //current month
    if(createTime.getMonth().equals(currentTime.getMonth())){
        utilMonthSum = utilMonthSum + percent
        utilMonthCount ++
    }

    //is current week
    if(isCurrentWeek(currentTime,createTime)){
        utilWeekCount++
        utilWeekSum = utilWeekSum + percent
    }
}


double utilYearAvg = (utilYearSum + todayUtilization) / (utilizationJson.size() + 1);
double utilMonthAvg = (utilMonthSum + todayUtilization) / (utilMonthSum + 1);
double utilWeekAvg = (utilWeekSum + todayUtilization) / (utilWeekCount + 1);


def isCurrentWeek(LocalDateTime currentTime,LocalDateTime createTime){
    return createTime.getDayOfYear() >= currentTime.minusDays(getWeekDays(currentTime)).getDayOfYear();
}

def getWeekDays(LocalDateTime currentTime){
    String dayOfWeek = currentTime.getDayOfWeek().toString()
    Long result = 0;

    switch(dayOfWeek) {
        case "SUNDAY":
            result = 6
            break
        case "SATURDAY":
            result = 5
            break
        case "FRIDAY":
            result = 4
            break
        case "THURSDAY":
            result = 3
            break
        case "WEDNESDAY":
            result = 2
            break
        case "TUESDAY":
            result = 1
            break
        default:
            result = 0
            break;

    return result;
    }

}



Map<String, String> utilizationParam = Map.of("tenantId", payload.get("tenantId").asLong(),
                                   "userId",user.get("tenantUserId").asLong(),
                                   "percent",String.format("%.2f", todayUtilization*100),
                                   "utilWeekAvg",String.format("%.2f", utilWeekAvg*100),
                                   "utilMonthAvg",String.format("%.2f", utilMonthAvg*100),
                                   "utilYearAvg",String.format("%.2f", utilYearAvg*100))

//insert current utilization 
api.call("mspbots-core", "/utilization", "post",utilizationParam)


println "done"

