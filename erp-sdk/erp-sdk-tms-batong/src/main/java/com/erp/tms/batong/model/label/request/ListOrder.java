package com.erp.tms.batong.model.label.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author Lambda
 * @Classname ListOrder
 * @Description
 * @Date 2024-01-15 11:33
 * @Created by yl
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListOrder implements Serializable {


    @JSONField(name = "reference_no")
    @NotBlank(message = "客户参考号不能为空")
    private String referenceNo;
}
