package com.sdk.tms.track123.constant;

/**
 * @author zdy
 * @ClassName PathConstants

 * @date 2023年11月14日
 * @version: 1.0
 */
public class PathConstants {

    private PathConstants(){}
    
    public static final String BASE_URL = "https://api.track123.com";

    //获取快递物流商列表 getCourierList
    public static final String GET_COURIER_URL = "/gateway/open-api/tk/v2/courier/list";
    public static final String GET_TRACK_URL = "/gateway/open-api/tk/v2/track/query";
    public static final String REGISTER_LOGISTICS_NUMBER = "/gateway/open-api/tk/v2/track/import";


    //获取海运物流商列表
    public static final String OCEAN_GET_COURIER_URL = "/gateway/open-api/tk/v1/ocean/courier/list";
    public static final String OCEAN_GET_TRACK_URL = "/gateway/open-api/tk/v1/ocean/track/query";
    public static final String OCEAN_REGISTER_LOGISTICS_NUMBER = "/gateway/open-api/tk/v1/ocean/track/import";

    //获取空运物流列表
    public static final String AVIATION_GET_COURIER_URL = "/gateway/open-api/tk/v1/aviation/courier/list";
    public static final String AVIATION_GET_TRACK_URL = "/gateway/open-api/tk/v1/aviation/track/query";
    public static final String AVIATION_REGISTER_LOGISTICS_NUMBER = "/gateway/open-api/tk/v1/aviation/track/import";
}
