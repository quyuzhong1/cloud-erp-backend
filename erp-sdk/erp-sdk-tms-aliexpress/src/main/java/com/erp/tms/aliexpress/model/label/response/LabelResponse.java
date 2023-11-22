package com.erp.tms.aliexpress.model.label.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName LabelResponse
 * @description: TODO
 * @date 2023年11月22日
 * @version: 1.0
 */
@Data
public class LabelResponse implements Serializable {
    private String errorDesc;
}
