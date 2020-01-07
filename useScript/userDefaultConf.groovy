
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


Map<String, String> param = Map.of("tenantId", payload.get("id").asLong())




JsonNode boardlistjson = api.call("mspbots-core", "/boards/all", "get",param)


JsonNode prioritlistjson = api.call("mspbots-core", "/boards/priorities", "get",param)
println "开始调用rule接口"

println "tenantId:"+payload.get("id").asLong()
JsonNode ruleCount = api.call("mspbots-core", "/ticket/rules/rulecount", "get",param)
println ruleCount
println "11111111111111"
if(ruleCount.get("result").asLong() > 0){
    println "tenantId:"+payload.get("id").asLong()+" already has rules"
    return
}



ObjectMapper mapper = new ObjectMapper();

ArrayNode rulesJsonArr = mapper.createArrayNode();
for (json in boardlistjson) {
    ObjectNode ruleObj = mapper.createObjectNode();
	


    //AGREEMENT_POINT_RULE	Agreement MRR Points
    ObjectNode argreementObj = mapper.createObjectNode();
    argreementObj.put("tenantId",payload.get("id").asLong())
    argreementObj.put("board",json.get("cwId").asText())
    argreementObj.put("enable",true)
    argreementObj.put("name","AGREEMENT_POINT_RULE")
    argreementObj.put("displayName","Agreement MRR Points")

    ArrayNode argreementParamJson = mapper.createArrayNode();


    for (int j = 0; j < 5 ; j++) {
        ObjectNode argreementParamsObj = mapper.createObjectNode();
        argreementParamsObj.put("index",1);
        argreementParamsObj.put("amount",j*1000);
        argreementParamsObj.put("points",(j+1)*100);
        argreementParamsObj.put("selected",true);
        argreementParamJson.add(argreementParamsObj)
    }

    argreementObj.put("params",argreementParamJson)

    rulesJsonArr.add(argreementObj)



    //ASSIGN_USER_GRADE_RULE	Assigned Points
    ObjectNode assignObj = mapper.createObjectNode();
    assignObj.put("tenantId",payload.get("id").asLong())
    assignObj.put("board",json.get("cwId").asText())
    assignObj.put("enable",true)
    assignObj.put("name","ASSIGN_USER_GRADE_RULE")
    assignObj.put("displayName","Assigned Points")

    ArrayNode assignParamJson = mapper.createArrayNode();

    ObjectNode assignParamsObj = mapper.createObjectNode();

    assignParamsObj.put("index",0)
    assignParamsObj.put("points","1000")
    assignParamsObj.put("selected",true)
    assignParamsObj.put("condition","If this ticket is assigned to current user")
    assignParamJson.add(assignParamsObj)

    assignObj.put("params",assignParamJson)

    rulesJsonArr.add(assignObj)


    //CONTACT_TITLE_POINT_RULE	Contact Title Points


    ObjectNode contactObj = mapper.createObjectNode();
    contactObj.put("tenantId",payload.get("id").asLong())
    contactObj.put("board",json.get("cwId").asText())
    contactObj.put("enable",true)
    contactObj.put("name","CONTACT_TITLE_POINT_RULE")
    contactObj.put("displayName","Contact Title Points")

    ArrayNode contactParamJson = mapper.createArrayNode();
    // If title=CEO, president, manager, partner, points = 100
    ObjectNode contactCeoParamsObj = mapper.createObjectNode();
    contactCeoParamsObj.put("index",0)
    contactCeoParamsObj.put("title","CEO")
    contactCeoParamsObj.put("points",100)
    contactCeoParamsObj.put("selected",true)
    contactParamJson.add(contactCeoParamsObj)


    ObjectNode contactPresidentParamsObj = mapper.createObjectNode();
    contactPresidentParamsObj.put("index",0)
    contactPresidentParamsObj.put("title","president")
    contactPresidentParamsObj.put("points",100)
    contactPresidentParamsObj.put("selected",true)
    contactParamJson.add(contactPresidentParamsObj)

    ObjectNode contactManagerParamsObj = mapper.createObjectNode();
    contactManagerParamsObj.put("index",0)
    contactManagerParamsObj.put("title","manager")
    contactManagerParamsObj.put("points",100)
    contactManagerParamsObj.put("selected",true)
    contactParamJson.add(contactManagerParamsObj)

    ObjectNode contactPartnerParamsObj = mapper.createObjectNode();
    contactPartnerParamsObj.put("index",0)
    contactPartnerParamsObj.put("title","partner")
    contactPartnerParamsObj.put("points",100)
    contactPartnerParamsObj.put("selected",true)
    contactParamJson.add(contactPartnerParamsObj)



    contactObj.put("params",contactParamJson)

    rulesJsonArr.add(contactObj)


    //PRIORITY_POINT_RULE	Priority Points


    ObjectNode priorityObj = mapper.createObjectNode();
    priorityObj.put("tenantId",payload.get("id").asLong())
    priorityObj.put("board",json.get("cwId").asText())
    priorityObj.put("enable",true)
    priorityObj.put("name","PRIORITY_POINT_RULE")
    priorityObj.put("displayName","Priority Points")

    ArrayNode priorityParamJson = mapper.createArrayNode();


    for (int i = 0; i < prioritlistjson.size(); i++) {

        ObjectNode priority1ParamsObj = mapper.createObjectNode();

        def prioritName = prioritlistjson.get(i).get("name").asText()
        priority1ParamsObj.put("name",prioritName);
        priority1ParamsObj.put("index",i);

        if(prioritName.contains("Priority 1")){
            priority1ParamsObj.put("points",500);
        }else if (prioritName.contains("Priority 2")){
            priority1ParamsObj.put("points",400);
        }else if (prioritName.contains("Priority 3")){
            priority1ParamsObj.put("points",300);
        }else if (prioritName.contains("Priority 4")){
            priority1ParamsObj.put("points",200);
        }else{
            priority1ParamsObj.put("points",100);
        }

        priority1ParamsObj.put("selected",true);
        priority1ParamsObj.put("priorityId",prioritlistjson.get(i).get("cwId").asLong());
        priorityParamJson.add(priority1ParamsObj)
    }



    priorityObj.put("params",priorityParamJson)

    rulesJsonArr.add(priorityObj)


    //STATUS_POINTS_GRADE_RULE	Status   Points

    ObjectNode statusObj = mapper.createObjectNode();
    statusObj.put("tenantId",payload.get("id").asLong())
    statusObj.put("board",json.get("cwId").asText())
    statusObj.put("enable",true)
    statusObj.put("name","STATUS_POINTS_GRADE_RULE")
    statusObj.put("displayName","Status   Points")

    ArrayNode statusParamJson = mapper.createArrayNode();

    JsonNode statusArr = json.get("statuses")

    def i = 0;
    for (statusJson in statusArr) {


        i++;

        ObjectNode statusParamsObj = mapper.createObjectNode();

        statusParamsObj.put("index",i);
        statusParamsObj.put("status",statusJson.get("id").asLong());

        //selected
        //"closed","completed",“In progress”, "waiting", "scheduled",“cancelled”
        def statusName = statusJson.get("name").asText().toLowerCase()
        if(statusName.contains("closed") || statusName.contains("in progress")||statusName.contains("waiting")
                ||statusName.contains("scheduled") || statusName.contains("cancelled")||statusName.contains("completed")){
            statusParamsObj.put("selected",false);
			
			
        }else {
            statusParamsObj.put("selected",true);
			
        } 
		statusParamsObj.put("name",statusName);
		
		





        //points
        ArrayNode pointsJsonArr = mapper.createArrayNode();

        ObjectNode time10Obj = mapper.createObjectNode();
        time10Obj.put("time",10)
        if(statusName.contains("new")) {
            time10Obj.put("point",500)
        }else if(statusName.contains("escalated")){
            time10Obj.put("point",600)
        }else{
            time10Obj.put("point",10)
        }

        pointsJsonArr.add(time10Obj);




        ObjectNode time20Obj = mapper.createObjectNode();
        time20Obj.put("time",20)
        time20Obj.put("point",10)
        pointsJsonArr.add(time20Obj);

        ObjectNode time30Obj = mapper.createObjectNode();
        time30Obj.put("time",30)
        time30Obj.put("point",10)
        pointsJsonArr.add(time30Obj);

        ObjectNode time60Obj = mapper.createObjectNode();
        time60Obj.put("time",60)
        time60Obj.put("point",10)
        pointsJsonArr.add(time60Obj);


        ObjectNode time120Obj = mapper.createObjectNode();
        time120Obj.put("time",120)
        time120Obj.put("point",10)
        pointsJsonArr.add(time120Obj);

        ObjectNode time240Obj = mapper.createObjectNode();
        time240Obj.put("time",240)
        time240Obj.put("point",10)
        pointsJsonArr.add(time240Obj);


        ObjectNode time480Obj = mapper.createObjectNode();
        time480Obj.put("time",480)
        time480Obj.put("point",10)
        pointsJsonArr.add(time480Obj);

        ObjectNode time720Obj = mapper.createObjectNode();
        time720Obj.put("time",720)
        time720Obj.put("point",10)
        pointsJsonArr.add(time720Obj);

        ObjectNode time1440Obj = mapper.createObjectNode();
        time1440Obj.put("time",1440)
        time1440Obj.put("point",10)
        pointsJsonArr.add(time1440Obj);

        ObjectNode time2880Obj = mapper.createObjectNode();
        time2880Obj.put("time",2880)
        time2880Obj.put("point",10)
        pointsJsonArr.add(time2880Obj);

        ObjectNode time4320Obj = mapper.createObjectNode();
        time4320Obj.put("time",4320)
        time4320Obj.put("point",10)
        pointsJsonArr.add(time4320Obj);
      
        ObjectNode time7200Obj = mapper.createObjectNode();
        time7200Obj.put("time",7200)
        time7200Obj.put("point",10)
        pointsJsonArr.add(time7200Obj);
            
        ObjectNode time11520Obj = mapper.createObjectNode();
        time11520Obj.put("time",11520)
        time11520Obj.put("point",10)
        pointsJsonArr.add(time11520Obj);
                  
        ObjectNode time20106Obj = mapper.createObjectNode();
        time20106Obj.put("time",20106)
        time20106Obj.put("point",10)
        pointsJsonArr.add(time20106Obj);
                        
        ObjectNode time43200Obj = mapper.createObjectNode();
        time43200Obj.put("time",43200)
        time43200Obj.put("point",10)
        pointsJsonArr.add(time43200Obj);

        statusParamsObj.put("points",pointsJsonArr);
        statusParamJson.add(statusParamsObj)

    }

    statusObj.put("params",statusParamJson)


    rulesJsonArr.add(statusObj)


    //UPDATE_TIME_GRADE_RULE	Updated Time  Points

    ObjectNode updateObj = mapper.createObjectNode();
    updateObj.put("tenantId",payload.get("id").asLong())
    updateObj.put("board",json.get("cwId").asText())
    updateObj.put("enable",true)
    updateObj.put("name","UPDATE_TIME_GRADE_RULE")
    updateObj.put("displayName","Updated Time  Points")

    ArrayNode updateParamJson = mapper.createArrayNode();

    for (j in [4,3,2,1]) {
        ObjectNode updateParamsObj = mapper.createObjectNode();
        updateParamsObj.put("hours",j*24);
        updateParamsObj.put("index",j-1);
        updateParamsObj.put("title","Last update time > "+j*24+"H");
        updateParamsObj.put("points",j*100);
        updateParamsObj.put("selected",true);
        updateParamJson.add(updateParamsObj)
    }

    updateObj.put("params",updateParamJson)

    rulesJsonArr.add(updateObj)

}

return api.call("mspbots-core", "ticket/rules", "post",rulesJsonArr)

