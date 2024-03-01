package com.erp.tms.aliexpress.model.handover.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName CloudPrintResponse
 * @description: 提供给ISV通过该接口获取面单云打印数据
 * @date 2024年02月05日
 * @version: 1.0
 */
@Data
public class CloudPrintResponse implements Serializable {
    /**
     * 面单云打印数据
     */
    @JSONField(name = "print_data")
    private String printData;
    /**
     * 面单云打印数据MD5加密串
     */
    @JSONField(name = "print_data_md5")
    private String printDataMd5;
}
