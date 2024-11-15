package com.erp.tms.aliexpress.model.handover.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName SubbagDetail
 
 * @date 2024年02月05日
 * @version: 1.0
 */
@Data
public class SubbagDetail implements Serializable {
    /**
     *
     *子包id
     */
    @JSONField(name = "subbag_id")
    private Long subbagId;
    /**
     *
     *子包创建时间时间戳
     */
    @JSONField(name = "gmt_create")
    private String gmtCreate;
    /**
     *
     *子包号
     */
    @JSONField(name = "subbag_code")
    private String subbagCode;
}
