package com.erp.server.wms.service;

import com.common.business.enums.OmsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCancelInboundReq;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCancelOutboundReq;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCreateInboundReq;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCreateOutboundReq;

/**
 * 第三方海外仓接口
 */
public interface ThirdWarehouseService {

    /**
     * 获取平台标识
     */
    OmsPlatformEnum getPlatForm();

    /**
     * 仓库服务商授权
     */
    Boolean authorize(OverseasProviderDTO.AuthorizeParamDTO dto);

    /**
     * 入库单创建接口对接
     * @return 入库单号
     */
    ApiResult<String> createInboundBill(ThirdWarehouseCreateInboundReq createInboundReq);

    /**
     * 入库单取消接口对接
     */
    ApiResult<String> cancelInboundBill(ThirdWarehouseCancelInboundReq cancelInboundReq);

    /**
     * 订单发货对接海外仓出库创建接口
     * @return 出库单号
     */
    ApiResult<String> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq);

    /**
     * 出库取消接口
     */
    ApiResult<String> cancelOutboundBill(ThirdWarehouseCancelOutboundReq cancelOutboundReq);
}
