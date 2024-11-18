package com.erp.server.bi.service;
import com.erp.model.bi.entity.BiTargetYearEntity;
import com.common.business.service.SuperService;
import com.erp.model.bi.dto.BiTargetYearDTO;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * <p>
 * 年度目标表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
public interface BiTargetYearService extends SuperService<BiTargetYearEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-09-13
    * @param dto
    * @return
    */
    String add(BiTargetYearDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-09-13
    * @param dto
    * @return
    */
    Boolean update(BiTargetYearDTO.UpdateDTO dto);

   /**
    * 获取指标完成值
    * @author yl
    * @date 2023-09-18 16:51
    * @param dto
    * @param flagStr
    * @return java.math.BigDecimal
    */
    BigDecimal getMetricsFinishValue(@Param("dto") BiTargetYearDTO.SearchDTO dto, String flagStr,String yearMonth,String settleRate);
}
