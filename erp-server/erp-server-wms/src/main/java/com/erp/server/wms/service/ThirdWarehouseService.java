package com.erp.server.wms.service;

import com.common.business.enums.OmsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.third.*;

import java.util.List;
import java.util.Map;

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
     * 查询产品数据
     * @return 入库单号
     */
    ApiResult<List<ThirdWarehouseSkuResp>> getSkuList(ThirdWarehouseProductReq productReq, String authId);

    /**
     * 入库单创建接口
     * @return 入库单号
     */
    ApiResult<String> createInboundBill(ThirdWarehouseCreateInboundReq createInboundReq, String authId);

    /**
     * 入库单编辑接口
     * @return 入库单号
     */
    ApiResult<String> editInboundBill(ThirdWarehouseCreateInboundReq createInboundReq,String authId);

    /**
     * 入库单取消接口
     */
    ApiResult<String> cancelInboundBill(ThirdWarehouseCancelInboundReq cancelInboundReq, String authId);

    /**
     * 订单发货对接海外仓出库创建接口
     * @return 出库单号
     */
    ApiResult<String> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq, String authId);


    /**
     * 出库取消接口 ThirdWarehouseCancelResultEnum
     */
    ApiResult<String> cancelOutboundBill(ThirdWarehouseCancelOutboundReq cancelOutboundReq, String authId);

    /**
     * 运费试算
     */
    ApiResult<List<ThirdWarehouseCalculateFeeResponse>> getCalculateFeeBatch(ThirdWarehouseCalculateFeeReq calculateFeeReq, String authId);

    /**
     * 上传文件
     * @param uploadFileReq
     * @param authId
     * @return
     */
    ApiResult<ThirdWarehouseUploadFileResponse> uploadFile(ThirdWarehouseUploadFileReq uploadFileReq, String authId);

    /**
     * 上传面单
     * @param uploadFileReq
     * @param authId
     * @return
     */
    ApiResult<ThirdWarehouseUploadOrderLabelResponse> uploadOrderLabel(ThirdWarehouseUploadOrderLabelReq uploadFileReq, String authId);

    /**
     * 上传交接文件
     * @param authId
     * @return
     */
    ApiResult<ThirdWarehouseUploadHandoverFileResponse> uploadHandoverFile(ThirdWarehouseUploadHandoverFileReq uploadHandoverFileReq, String authId);


    ApiResult<String> refreshToken(String authId, Map<String,Object> map);
}
