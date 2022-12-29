package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 销售指标数据统计数量返回模型
 *
 * @Author Cloud
 * @Date 2022/12/13 14:28
 */
@Data
@NoArgsConstructor
public class TargetSaleCountVO {

    private Integer value;

    public TargetSaleCountVO(Integer count) {
        this.value = null != count? count : 0;
    }
}
