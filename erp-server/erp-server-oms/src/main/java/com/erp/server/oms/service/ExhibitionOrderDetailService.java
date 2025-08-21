package com.erp.server.oms.service;
import com.erp.model.oms.entity.ExhibitionOrderDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.ExhibitionOrderDetailDTO;

/**
 * <p>
 * 展会订单详情 服务类
 * </p>
 *
 * @author jack
 * @since 2025-08-20
 */
public interface ExhibitionOrderDetailService extends SuperService<ExhibitionOrderDetailEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ExhibitionOrderDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @return
    */
    Boolean update(ExhibitionOrderDetailDTO.UpdateDTO dto);


}
