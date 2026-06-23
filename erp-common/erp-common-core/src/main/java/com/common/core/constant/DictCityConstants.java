package com.common.core.constant;

public final class DictCityConstants {

    public static final String NO_DISTRICT_NAME = "无";

    private static final String NO_DISTRICT_ID_PREFIX = "__no_district__:";

    private DictCityConstants() {
    }

    public static String buildNoDistrictId(String cityId) {
        return NO_DISTRICT_ID_PREFIX + cityId;
    }

    public static boolean isNoDistrictId(String districtId) {
        return districtId != null && districtId.startsWith(NO_DISTRICT_ID_PREFIX);
    }
}
