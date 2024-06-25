package com.erp.server.oms.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoB2cForeignDTO;
import com.erp.model.oms.entity.SoB2cEntity;

import java.util.List;

/**
 * <p>
 * B2C销售订单外部接口服务类
 * </p>
 *
 * @author lambda
 * @since 2023-12-20
 */
public interface SoB2cForeignService extends IService<SoB2cEntity> {

    PagingVO<SoB2cForeignDTO.OrderDeliveryResp> getOrderDeliveryInfo(PagingDTO<SoB2cForeignDTO.OrderDeliveryReq> orderDeliveryReq);
}
