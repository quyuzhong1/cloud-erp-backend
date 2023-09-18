package com.erp.server.bi.service;
import com.erp.model.bi.entity.BiTargetYearEntity;
import com.common.business.service.SuperService;
import com.erp.model.bi.dto.BiTargetYearDTO;

import java.util.List;

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
     * 获取到对应设置的目标值
     * @param dto
     * @return
     */
    List<BiTargetYearDTO.YearMonthValueDTO> listYearMonthValue(BiTargetYearDTO.SearchDTO dto);
}
