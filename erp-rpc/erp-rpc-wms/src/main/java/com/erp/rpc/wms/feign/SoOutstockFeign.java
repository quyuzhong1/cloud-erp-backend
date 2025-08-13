package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.AdvanceQueryContainer;
import com.erp.model.oms.dto.PlatformGenerateSoOutstockDTO;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.dto.SoDeliveryNoticeDetailDTO;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.dto.SoOutstockDetailDTO;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@FeignClient(name = "erp-wms", contextId = "soOutstock" ,configuration = {FeignErrorDecoder.class})
public interface SoOutstockFeign {

    /**
     * 根据来源明细id查询出库表
     * @param sourceDetailId sourceDetailId
     * @return java.util.List<com.erp.model.oms.entity.SoOutstockDetailEntity>
     * @Author Luo_WG
     * @Date 2023/5/15 15:03
     **/
    @PostMapping("feign/soOutstock/listDetailBySourceDetailId")
    List<SoOutstockDetailEntity> listDetailBySourceDetailId(@RequestBody List<String> sourceDetailId);

    /**
     * 销售订单ids获取销售出库单的数据
     * @Author Luo_WG
     * @Date 2023/5/25 15:42
     * @param soIds
     * @return java.util.List<com.erp.model.wms.entity.SoOutstockDetailEntity>
     **/
    @PostMapping("feign/soOutstock/listDetailBySoIds")
    List<SoOutstockDetailEntity> listDetailBySoIds(@RequestBody List<String> soIds);

    /**
     * 销售订单ids获取销售出库单主表信息
     * @Author Luo_WG
     * @Date 2023/5/25 15:42
     * @param soIds
     * @return java.util.List<com.erp.model.wms.entity.SoOutstockDetailEntity>
     **/
    @PostMapping("feign/soOutstock/listBySoIds")
    List<SoOutstockEntity> listBySoIds(@RequestBody List<String> soIds);

    @PostMapping("feign/soOutstock/listByIds")
    List<SoOutstockEntity> listByIds(@RequestBody List<String> ids);

    @PostMapping("feign/soOutstock/listDetailBySoDetailIds")
    List<SoOutstockDetailDTO.DeliveryQtyDTO> listDetailBySoDetailIds(List<String> soDetailIds);

    @PostMapping("feign/soOutstock/listByTrackNo")
    List<SoOutstockEntity> listByTrackNo(String trackNo);

    @PostMapping("feign/soOutstock/listByAdvanceQuery")
    List<SoOutstockEntity> listByAdvanceQuery(@RequestBody AdvanceQueryContainer advanceQueryContainer);
    /**
     * 获取销售出货单
     *
     * @param id
     * @return
     */
    @PostMapping("feign/soOutstock/getSoOutstockEntityById")
    public SoOutstockEntity getSoOutstockEntityById(@RequestParam(value = "id") String id);

    /**
     * 获取销售出货单 明细
     *
     * @param id
     * @return
     */
    @PostMapping("feign/soOutstock/getSoOutstockDetailByDetailId")
     List<SoOutstockDetailEntity> getSoOutstockDetailByDetailId(@RequestParam(value = "id") String id);

    /**
     * 生成b2c 销售出库单
     * @description
     * @param b2cSoId 销售订单-b2c
     * @author Lambda
     * @return Boolean
     * @create 2023-12-13 18:18
     */
    @PostMapping("feign/soOutstock/generateB2cSoOutstock")
    Boolean generateB2cSoOutstock(@RequestBody String b2cSoId);

    /**
     * 生成b2c 销售出库单
     * @description
     * @param generateB2cDTO
     * @author Lambda
     * @return Boolean
     * @create 2023-12-13 18:18
     */
    @PostMapping("feign/soOutstock/generateB2cSoOutstockByData")
    Boolean generateB2cSoOutstockByData(@RequestBody SoOutstockDTO.GenerateB2cDTO generateB2cDTO);

    /**
     * 单据下推 销售出库单
     */
    @PostMapping("feign/soOutstock/addB2bPushDownNo")
    Boolean addB2bPushDownNo(@RequestBody List<SoOutstockDTO.GenerateSoOutstockViewDTO> generateB2cDTO);
    /**
     * 生成b2c 销售出库单(平台拉取发货信息生成)
     */
    @PostMapping("feign/soOutstock/generateB2cSoOutstockByPlatformData")
    Boolean generateB2cSoOutstockByPlatformData(@RequestBody PlatformGenerateSoOutstockDTO platformGenerateSoOutstockDTO);

    @PostMapping("feign/soOutstock/getCanGenerateDeclare")
    List<TmsDeclareBillDTO.SoOutDTO> getCanGenerateDeclare(@RequestBody TmsDeclareBillDTO.QuerySourceDTO querySourceDTO);

    @PostMapping("feign/soOutstock/logisticStatistics")
    List<FirstMileDeliveryDTO.LogisticStatisticsDTO> logisticStatistics(@RequestBody FirstMileDeliveryDTO.StatisticsReq deliveryStaticsReq);

    @PostMapping("feign/soOutstock/updateStatus")
    Boolean updateStatus(@RequestBody TmsDeclareBillDTO.UpdateStatusDTO dto);

    /**
     * 重新生成销售出库单
     * @author Will
     * @date: 2024/4/28 10:29
     * @param ids
     * @return Boolean
     */
    @PostMapping("feign/soOutstock/afreshGenerateB2cOutstock")
    Boolean afreshGenerateB2cOutstock(@RequestBody List<String> ids);

    /**
     * 更新销售出库单价格
     * @param saveOrUpdateList
     */
    @PostMapping("feign/soOutstock/updateSoOutPrice")
    Boolean updateSoOutPrice(List<SoDetailEntity> saveOrUpdateList);

    /**
     * B2B退货订单没有关联订单时的计算规则
     * @author jack
     * @date: 2024-11-25
     * @param params
     * @return SoOutstockDTO.AmountDTO
     */
    @PostMapping("feign/soOutstock/listAmountBySkuIds")
    List<SoOutstockDTO.AmountDTO> listAmountBySkuIds(@RequestBody SoOutstockDTO.ListAmountParamDTO params);

    /**
     * 根据销售 销售订单详情ids 获取是否有下推的单据
     * @author jack
     * @date 2024-12-19
     * @param soDetailIds
     * @return
     */
    @PostMapping("feign/soOutstock/getPushDownBySoDetailIds")
    List<SoDeliveryNoticeDetailDTO.PushDownDTO> getPushDownBySoDetailIds(@RequestBody List<String> soDetailIds);


    /**
     * 根据skuId查询Doris最新出库时间
     * @author Jim
     * @date 2025-08-13
     * @return
     */
    @PostMapping("feign/soOutstock/mapLastOutstockDateBySkuIds")
    Map<String, LocalDate> mapLastOutstockDateBySkuIds(@RequestBody List<String> skuIds);
}
