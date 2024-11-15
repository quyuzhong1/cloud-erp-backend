package com.erp.tms.aliexpress.model.query.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName ServiceResponse
 
 * @date 2023年12月29日
 * @version: 1.0
 */
@Data
public class ServiceResponse implements Serializable {
    @JSONField(name = "result_list")
    private List<ServiceResult> resultList;
}
