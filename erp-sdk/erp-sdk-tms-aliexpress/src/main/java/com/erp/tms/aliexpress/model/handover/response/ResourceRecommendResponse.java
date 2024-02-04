package com.erp.tms.aliexpress.model.handover.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName ResourceRecommendResponse
 * @description: 揽收资源推荐
 * @date 2024年02月05日
 * @version: 1.0
 */
@Data
public class ResourceRecommendResponse implements Serializable {
    /**
     * 仓资源名称
     */
    @JSONField(name = "res_name")
    private String resName;
    /**
     * 发货物流服务编码
     */
    @JSONField(name = "schema_code")
    private String schemaCode;
    /**
     * 仓资源编码
     */
    @JSONField(name = "res_code")
    private String resCode;
}
