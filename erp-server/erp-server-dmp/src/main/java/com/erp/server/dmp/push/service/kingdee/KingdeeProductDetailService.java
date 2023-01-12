package com.erp.server.dmp.push.service.kingdee;

import java.util.Map;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/11 18:03
 */
public interface KingdeeProductDetailService {
    /**
     * @description: 发送数据到金蝶云星空
     * @author Will
     * @date: 2023/1/11 18:09
     * @param map
     */
    void pushProductDetail(Map<String, Object> map);
}
