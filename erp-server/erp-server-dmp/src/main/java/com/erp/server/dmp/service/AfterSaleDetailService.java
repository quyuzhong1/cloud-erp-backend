package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.AfterSaleDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.AfterSaleDetailDTO;

/**
 * <p>
 * 售后申请明细表 服务类
 * </p>
 *
 * @author jack
 * @since 2025-04-06
 */
public interface AfterSaleDetailService extends SuperService<AfterSaleDetailEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-04-06
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AfterSaleDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-04-06
    * @param dto
    * @return
    */
    Boolean update(AfterSaleDetailDTO.UpdateDTO dto);


}
