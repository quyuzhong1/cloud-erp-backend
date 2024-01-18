package com.erp.tms.batong.model.label.request;

import cn.hutool.core.annotation.Alias;
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


    @Alias("reference_no")
    @NotBlank(message = "客户参考号不能为空")
    private String referenceNo;
}
