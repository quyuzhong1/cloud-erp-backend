package com.erp.server.oms.service;
import com.erp.model.oms.entity.SoB2cRefundDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.RefundOrderDetailDTO;

/**
 * <p>
 * 退款订单明细 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-09-27
 */
public interface SoB2cRefundDetailService extends SuperService<SoB2cRefundDetailEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2024-09-27
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(RefundOrderDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author lrp
    * @date: 2024-09-27
    * @param dto
    * @return
    */
    Boolean update(RefundOrderDetailDTO.UpdateDTO dto);


}
