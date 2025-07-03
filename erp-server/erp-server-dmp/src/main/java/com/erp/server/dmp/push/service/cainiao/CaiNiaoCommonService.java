package com.erp.server.dmp.push.service.cainiao;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.DmpSyncTaskDTO;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
import com.erp.oms.aliexpress.dto.AliExpressShopInfoDTO;
import com.erp.rpc.oms.feign.OmsListingInfoFeign;
import com.erp.server.dmp.service.DmpOutputTaskRecordService;
import com.erp.wms.aliexpress.model.ApiResponseDTO;
import com.erp.wms.aliexpress.model.product.AliexpressProductDTO;
import com.erp.wms.aliexpress.model.returnorder.AliexpressReturnInstockDTO;
import com.erp.wms.aliexpress.model.returnorder.ApiReturnOrderResponseDTO;
import com.erp.wms.aliexpress.service.AliexpressWarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;


@Slf4j
@Service
public class CaiNiaoCommonService {

    @Resource
    private AliexpressWarehouseService aliexpressWarehouseService;

    @Resource
    private OmsListingInfoFeign omsListingInfoFeign;

    @Resource
    private DmpOutputTaskRecordService dmpOutputTaskRecordService;

    /**
     * 推送listing
     */
    public ApiResult<?> pushListing(Object ext) {
        AliexpressProductDTO aliexpressProductDTO = JSONUtil.toBean(ext.toString(), AliexpressProductDTO.class);
        //查询是否推送成功过，有则为更新，无则为新增
        DmpSyncTaskDTO.OneDTO oneDTO = new DmpSyncTaskDTO.OneDTO();
        oneDTO.setSourceId(aliexpressProductDTO.getListingId());
        oneDTO.setTargetPlatform(OmsPlatformEnum.CAI_NIAO.getCode());
        DmpPushTaskDTO.SyncInfoDTO syncInfoDTO = dmpOutputTaskRecordService.getSuccessData(oneDTO);
        if(Objects.isNull(syncInfoDTO)){
            aliexpressProductDTO.setActionType("add");
        }else{
            aliexpressProductDTO.setActionType("update");
        }
        try {
            ApiResponseDTO apiResponseDTO = aliexpressWarehouseService.pushListing(aliexpressProductDTO);
            if (apiResponseDTO.isSuccess()) {
                // 处理成功逻辑
                String itemId = apiResponseDTO.getResult().getData().getItemId();
                omsListingInfoFeign.updatePlatformSkuId(aliexpressProductDTO.getListingId(),itemId);
                //更新
                return ApiResult.success("推送菜鸟仓listing成功，itemId：" + itemId, apiResponseDTO.getResult().getData());
            } else {
                // 处理失败逻辑
                String errorMsg = apiResponseDTO.getErrorResponse().getMsg() + apiResponseDTO.getErrorResponse().getSubMsg();
                log.error("推送菜鸟仓listing失败，参数：{}，错误信息：{}", JSON.toJSONString(aliexpressProductDTO), JSON.toJSONString(apiResponseDTO));
                return ApiResult.error("推送菜鸟仓listing失败，异常：" + errorMsg);
            }
        }catch (Exception e){
            log.error("推送菜鸟仓listing失败，参数：{}，异常：", JSON.toJSONString(aliexpressProductDTO), e);
            return ApiResult.error("推送菜鸟仓listing失败，异常：" + e.getMessage());
        }
    }


    /**
     * 推送退货入库单
     */
    public ApiResult<?> pushReturnOrder(Object ext) {
        AliexpressReturnInstockDTO aliexpressProductDTO = JSONUtil.toBean(ext.toString(), AliexpressReturnInstockDTO.class);
        try {
            ApiReturnOrderResponseDTO apiResponseDTO = aliexpressWarehouseService.createReturnInstockOrder(aliexpressProductDTO);
            if (apiResponseDTO.isSuccess()) {
                //更新
                return ApiResult.success("推送菜鸟仓退货入库单成功");
            } else {
                // 处理失败逻辑
                String errorMsg = apiResponseDTO.getErrorResponse().getMsg() + apiResponseDTO.getErrorResponse().getSubMsg();
                log.error("推送菜鸟仓退货入库单失败，参数：{}，错误信息：{}", JSON.toJSONString(aliexpressProductDTO), JSON.toJSONString(apiResponseDTO));
                return ApiResult.error("推送菜鸟仓退货入库单失败，异常：" + errorMsg);
            }
        }catch (Exception e){
            log.error("推送菜鸟仓退货入库单失败，参数：{}，异常：", JSON.toJSONString(aliexpressProductDTO), e);
            return ApiResult.error("推送菜鸟仓退货入库单失败，异常：" + e.getMessage());
        }
    }
}