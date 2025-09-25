package com.erp.rpc.tms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.entity.LogisticsAddressEntity;
import com.erp.model.tms.entity.LogisticsChannelBlacklistEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.vo.request.LogisticsQueryBaseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "erp-tms", contextId = "logistics",configuration = {FeignErrorDecoder.class})
public interface LogisticsFeign {

    /**
     * 根据虾皮订单号查询物流单号
     * @param logisticsQueryVOList
     * @return
     */
    @PostMapping("/feign/logistics/queryOrderList")
    ApiResult<List<LogisticsOrderResponseVO>> queryOrderList(@RequestBody List<LogisticsQueryBaseVO> logisticsQueryVOList);

    /**
     * 根据物流商id 获取到对应的渠道的启用禁用列表
     * @param supplierId
     * @return
     */
    @PostMapping("/feign/logistics/listBySupplierId")
    List<BaseIdDTO.CodeDTO> listBySupplierId(@RequestBody String supplierId);

    /**
     * 根据物流商id 获取到对应的渠道的启用禁用列表
     * @Author Luo_WG
     * @Date 2024/2/1 20:33
     * @param logisticsSupplierIds
     * @return java.util.List<com.common.business.dto.base.BaseIdDTO.CodeDTO>
     **/
    @PostMapping("/feign/logistics/listLogisticsChannel")
    List<LogisticsChannelDTO.ListSelectDTO> listLogisticsChannel(@RequestBody List<String> logisticsSupplierIds);

    @PostMapping("/feign/logistics/updateDisabledBySupplierId")
    Boolean updateDisabledBySupplierId(@RequestBody LogisticsSupplierDTO.UpdateDisabledDTO updateDisabledDTO);

    /**
     * 根据渠道id 获取渠道信息
     * @param channelId
     * @return
     */
    @PostMapping("/feign/logistics/getChannelById")
    LogisticsChannelEntity getChannelById(@RequestBody String channelId);
    /**
     * 根据渠道名称 获取渠道信息
     * @param channelName
     * @return
     */
    @PostMapping("/feign/logistics/getChannelByName")
    List<LogisticsChannelEntity> getChannelByName(@RequestBody String channelName);
    /**
     * 获取渠道 根据渠道名称
     * @param channelNameList
     * @return
     */
    @PostMapping("/feign/logistics/listChannelByNameList")
    List<LogisticsChannelEntity> listChannelByNameList(@RequestBody List<String> channelNameList);
    @PostMapping("/feign/logistics/getChannelInfoById")
    LogisticsChannelDTO.BaseDTO getChannelInfoById(@RequestBody String channelId);

    @PostMapping("/feign/logistics/listChannelInfoById")
    List<LogisticsChannelDTO.BaseDTO> listChannelInfoById(@RequestBody List<String> channelIds);

    @GetMapping("/feign/logistics/getLogisticsChannelConstraint")
    LogisticsChannelDTO.LogisticsChannelConstraintDTO getLogisticsChannelConstraint(@RequestParam(value = "channelId")String channelId,@RequestParam(value = "country")String country);
    /**
     * 获取地址信息
     * @param id
     * @return
     */
    @GetMapping("/feign/logistics/getLogisticsAddressById")
    LogisticsAddressEntity getLogisticsAddressById(@RequestParam("id")String id);


    @GetMapping("/feign/logistics/getScaleChannelByChannelById")
    LogisticsChannelDTO.SignShipDTO getScaleChannelByChannelById(@RequestParam("logisticsChannelId") String logisticsChannelId,
                                                                 @RequestParam("dictPlatform") String dictPlatform
    );

    /**
     * 根据地址类型获取地址列表
     *
     * @return
     */
    @PostMapping("/feign/logistics/listAddressByType")
    List<LogisticsAddressDTO.ListDTO> listAddressByType(@RequestBody @Validated LogisticsAddressDTO.AddressByTypeDTO dto);
    /**
     * 根据渠道汇总时间段内未更新运单号记录
     *
     * @return
     */
    @PostMapping("/feign/logistics/getWarnReportByChannel")
    List<LogisticsChannelDTO.WarnReportDTO> getWarnReportByChannel(@RequestBody LogisticsBillDetailQueryDTO query);

    /**
     * 接收track123物流轨迹同步数据
     * @return
     */
    @PostMapping("/feign/logistics/webhookByTrack123")
    void webhookByTrack123(@RequestBody LogisticsTrackDTO.TrackWebHookDTO dto);

    @GetMapping("/feign/logistics/estimateIsOutOfRangeDelivery")
    Boolean estimateIsOutOfRangeDelivery(@RequestParam("logisticsChannelId")String logisticsChannelId, @RequestParam("country")String country, @RequestParam("postCode")String postCode);

    /**
     * 更新销售订单预估运费
     *
     * @param pagingParamDTO
     * @return void
     * @author zdy
     * @date: 2024/12/10 17:35
     */
    @PostMapping("/feign/logistics/updateShippingCalculation")
    void updateShippingCalculation(@RequestBody @Validated ShippingCalculationDTO.PagingParamDTO pagingParamDTO);

    /**
     *sku成本-skuIds
     * @param queryB2BDTO
     * @return
     */
    @PostMapping("/feign/logistics/listSkuCostBySkuIds")
    List<InventorySkuCostDTO.SkuCostDTO> listSkuCostBySkuIds(@RequestBody InventorySkuCostDTO.QueryB2BDTO queryB2BDTO);
    /**
     * sku成本 订单明细
     * @param queryB2CDTO
     * @return
     */
    @PostMapping("/feign/logistics/listSkuCostByDetail")
    List<InventorySkuCostDTO.SkuCostDTO> listSkuCostByDetail(@RequestBody InventorySkuCostDTO.QueryB2CDTO queryB2CDTO);
    /**
     * sku成本 单个明细
     * @param queryDetailDTOList
     * @return
     */
    @PostMapping("/feign/logistics/listSkuCostByDetailList")
    List<InventorySkuCostDTO.SkuCostDTO> listSkuCostByDetailList(@RequestBody List<InventorySkuCostDTO.QueryDetailDTO> queryDetailDTOList);
    /**
     * 所有的物流渠道
     * @return
     */
    @GetMapping("/feign/logistics/listAll")
    List<BaseDropDownDTO.DisabledDTO> listAll();
    /**
     * 根据渠道查询黑名单
     * @param channelIdList
     * @return
     */
    @PostMapping("/feign/logistics/listChannelBlacklist")
    List<LogisticsChannelBlacklistEntity> listChannelBlacklist(@RequestBody List<String> channelIdList);

    /**
     * 根据skuId和仓库Id、orgId查询最新已审核单据的SKU成本（人民币）
     * @param queryDTO
     * @return
     */
    @PostMapping("/feign/logistics/getSkuCostInCNY")
    List<InventorySkuCostDTO.SkuCostCNYDTO> getSkuCostInCNY(@RequestBody InventorySkuCostDTO.SkuCostCNYQueryDTO queryDTO);
}
