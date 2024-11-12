package com.erp.server.mrp.service;
import com.erp.model.mrp.entity.CalcSalesInfoDimEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.mrp.dto.CalcSalesInfoDimDTO;

/**
 * <p>
 * 销量试算表 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
 */
public interface CalcSalesInfoDimService extends SuperService<CalcSalesInfoDimEntity> {

    /**
    * 新增
    * @author liaohui
    * @date: 2024-11-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CalcSalesInfoDimDTO.AddDTO dto);

    /**
    * 修改
    * @author liaohui
    * @date: 2024-11-11
    * @param dto
    * @return
    */
    Boolean update(CalcSalesInfoDimDTO.UpdateDTO dto);


}
