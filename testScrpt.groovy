import com.fasterxml.jackson.databind.JsonNode


ScriptInfo sinfo = new ScriptInfo();
JsonNode payload =  sinfo.getPayload();
JsonNode user =  sinfo.getUser();



println payload.get("tenantUserId")
println user
