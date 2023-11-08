package com.erp.model.tms.vo.request;

import com.erp.model.tms.entity.LogisticsAuthEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName LogisticsQueryVO
 * @description: 查询类
 * @date 2023年11月06日
 * @version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogisticsQueryBaseVO implements Serializable {
    /**
     * 发货单号
     */
    private String deliveryNo;
    /**
     * 运单号
     */
    private String transportNo;
    /**
     * 跟踪号
     */
    private List<String> trackNoList;
    /**
     * 授权信息
     */
    private LogisticsAuthEntity logisticsAuthEntity;

}
