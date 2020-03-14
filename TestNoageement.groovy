import com.fasterxml.jackson.databind.JsonNode

Map<String, Object> companyParams = Map.of("tenantId", 2,
                                             "companyId", 45469)
api.call("mspbots-wise", "/web/wise/companies/agreement", "get",companyParams);
