package com.erp.model.tms.vo.request;

import com.erp.model.tms.entity.LogisticsAuthEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName ChanelQueryVO
 * @description: TODO
 * @date 2023年11月08日
 * @version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChanelQueryVO implements Serializable {
    /**
     * 运输方式：1 所有方式；2 国际快递；3 国际小包；4 专线；5 联邮通；6 其他；
     */
    private String transportMode;
    /**
     * 授权信息
     */
    LogisticsAuthEntity logisticsAuthEntity;
}
