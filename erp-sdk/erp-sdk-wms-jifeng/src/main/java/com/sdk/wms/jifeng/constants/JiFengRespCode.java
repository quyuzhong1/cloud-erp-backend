package com.sdk.wms.jifeng.constants;

import com.sdk.wms.jifeng.dto.response.JiFengBaseResp;

/**
 * 极风 OMS 接口响应码。
 */
public final class JiFengRespCode {

    private JiFengRespCode() {
        throw new IllegalStateException("Utility JiFengRespCode class");
    }

    /** 成功 */
    public static final int SUCCESS = 0;

    /**
     * B2B 出库单在仓库中不存在（getB2BOrder 等查询接口）。
     * 业务侧可跳过同步，避免整批任务失败。
     */
    public static final int ORDER_NOT_FOUND_IN_WAREHOUSE = 70000;

    /**
     * 判定是否为「订单在仓库不存在」类错误（仅按平台错误码识别）。
     */
    public static boolean isOrderNotFoundInWarehouse(JiFengBaseResp<?> resp) {
        return resp != null
                && resp.getCode() != null
                && ORDER_NOT_FOUND_IN_WAREHOUSE == resp.getCode();
    }
}
