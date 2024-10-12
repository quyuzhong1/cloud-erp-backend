package com.erp.server.oms.service;
import com.erp.model.oms.entity.SoB2cReturnDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.SoB2cReturnDetailDTO;

import java.util.List;

/**
 * <p>
 * b2c退货订单明细 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-10-09
 */
public interface SoB2cReturnDetailService extends SuperService<SoB2cReturnDetailEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2024-10-09
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SoB2cReturnDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author lrp
    * @date: 2024-10-09
    * @param dto
    * @return
    */
    Boolean update(SoB2cReturnDetailDTO.UpdateDTO dto);
    List<SoB2cReturnDetailEntity> listByMainIds(List<String> mainIds);
    boolean deleteByMainIds(List<String> mainIds);
}
