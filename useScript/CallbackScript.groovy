

    Map<String, String> param = Map.of("objectId", 1,
                                   "type","ticket",
                                   "level","owner")

                                   
    api.call("mspbots-wise", "/callbacks/" + payload.get("id").asLong(), "post",param)

