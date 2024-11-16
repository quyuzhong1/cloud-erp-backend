package com.erp.tms.batong.model.label.request;

import cn.hutool.core.annotation.Alias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author Lambda
 * @Classname LabelResponse
 * @Description 标签
 * @Date 2024-01-15 11:06
 * @Created by yl
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabelRequest implements Serializable {

    @Alias("configInfo")
    @NotNull(message = "配置信息不能为空")
    private ConfigInfo configInfo;



    @Alias("listorder")
    @NotNull(message = "订单信息不能为空")
    @Size(min = 1, message = "订单信息不能为空")
    private List<ListOrder> orderList;

    /**
     * 标签纸张配置代码
     * 1：标签纸-地址标签
     * 2：标签纸-地址标签+报关单
     * 3：标签纸-地址标签+配货单
     * 4：标签纸-地址标签+报关单+配货单
     * 5：A4纸-地址标签
     * 6：A4纸-地址标签+报关单
     * 7：A4纸-地址标签+配货单
     * 8：A4纸-地址标签+报关单+配货单
     */
    @Alias("config_code")
    private String configCode;




}
