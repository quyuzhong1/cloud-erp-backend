package com.sdk.tms.track123.constant;

/**
 * @author zdy
 * @ClassName PathConstants
 * @description: TODO
 * @date 2023年11月14日
 * @version: 1.0
 */
public interface PathConstants {

    String BASE_URL = "https://api.track123.com";

    //获取快递物流商列表 getCourierList
    String GET_COURIER_URL = "/gateway/open-api/tk/v2/courier/list";
    String GET_TRACK_URL = "/gateway/open-api/tk/v2/track/query";
    String REGISTER_LOGISTICS_NUMBER = "/gateway/open-api/tk/v2/track/import";
    String UPDATE_LOGISTICS_NUMBER = "/gateway/open-api/tk/v2.1/track/update";


    //获取海运物流商列表
    String OCEAN_GET_COURIER_URL = "/gateway/open-api/tk/v1/ocean/courier/list";
    String OCEAN_GET_TRACK_URL = "/gateway/open-api/tk/v1/ocean/track/query";
    String OCEAN_REGISTER_LOGISTICS_NUMBER = "/gateway/open-api/tk/v1/ocean/track/import";

    //获取空运物流列表
    String AVIATION_GET_COURIER_URL = "/gateway/open-api/tk/v1/aviation/courier/list";
    String AVIATION_GET_TRACK_URL = "/gateway/open-api/tk/v1/aviation/track/query";
    String AVIATION_REGISTER_LOGISTICS_NUMBER = "/gateway/open-api/tk/v1/aviation/track/import";
}
