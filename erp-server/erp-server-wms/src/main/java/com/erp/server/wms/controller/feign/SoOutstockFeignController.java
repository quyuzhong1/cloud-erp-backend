package com.erp.server.wms.controller.feign;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.enums.DataAttributeEnum;
import com.common.message.constant.RedisKeyConstant;
import com.erp.model.oms.dto.ExhibitionOrderDTO;
import com.erp.model.oms.dto.PlatformGenerateSoOutstockDTO;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.dto.SoDeliveryNoticeDetailDTO;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.dto.SoOutstockDetailDTO;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.server.wms.service.SoOutstockDetailService;
import com.erp.server.wms.service.SoOutstockService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("feign/soOutstock")
public class SoOutstockFeignController {
    @Resource
    private SoOutstockDetailService soOutstockDetailService;

    @Resource
    private SoOutstockService soOutstockService;


    /**
     * 根据来源明细id查询出库表
     *
     * @param sourceDetailId sourceDetailId
     * @return java.util.List<com.erp.model.oms.entity.SoOutstockDetailEntity>
     * @Author Luo_WG
     * @Date 2023/5/15 15:03
     **/
    @PostMapping("/listDetailBySourceDetailId")
    public List<SoOutstockDetailEntity> listDetailBySourceDetailId(@RequestBody List<String> sourceDetailId) {
        return soOutstockDetailService.listDetailBySourceDetailId(sourceDetailId);
    }

    /**
     * 获取销售出货单
     *
     * @param id
     * @return
     */
    @PostMapping("/getSoOutstockEntityById")
    public SoOutstockEntity getSoOutstockEntityById(@RequestParam(value = "id") String id) {
        return soOutstockService.getById(id);
    }

    /**
     * 获取销售出货单 明细
     *
     * @param id
     * @return
     */
    @PostMapping("/getSoOutstockDetailByDetailId")
    public List<SoOutstockDetailEntity> getSoOutstockDetailByDetailId(@RequestParam(value = "id") String id) {
        return soOutstockDetailService.listByMainIds(Collections.singletonList(id));
    }

    /**
     * 销售订单ids获取销售出库单的数据
     *
     * @param soIds
     * @return java.util.List<com.erp.model.wms.entity.SoOutstockDetailEntity>
     * @Author Luo_WG
     * @Date 2023/5/25 15:42
     **/
    @PostMapping("/listDetailBySoIds")
    public List<SoOutstockDetailEntity> listDetailBySoIds(@RequestBody List<String> soIds) {
        return soOutstockDetailService.listDetailBySoIds(soIds);
    }

    /**
     * 销售订单ids获取销售出库单主表信息
     *
     * @param soIds
     * @return java.util.List<com.erp.model.wms.entity.SoOutstockEntity>
     * @Author Luo_WG
     * @Date 2023/5/25 15:42
     **/
    @PostMapping("/listBySoIds")
    List<SoOutstockEntity> listBySoIds(@RequestBody List<String> soIds) {
        return soOutstockService.listBySoIds(soIds);
    }


    @PostMapping("/listByIds")
    List<SoOutstockEntity> listByIds(@RequestBody List<String> ids) {
        if(CollectionUtils.isEmpty(ids)){
            return new ArrayList<>();
        }
        return soOutstockService.listByIds(ids);
    }

    /**
     * 根据销售订单详情ids 获取对应的出库详情
     *
     * @param soDetailIds
     * @return java.util.List<com.erp.model.wms.entity.SoOutstockDetailEntity>
     * @author yl
     * @date 2023-05-29 17:32
     */
    @PostMapping("/listDetailBySoDetailIds")
    List<SoOutstockDetailDTO.DeliveryQtyDTO> listDetailBySoDetailIds(@RequestBody List<String> soDetailIds) {
        return soOutstockDetailService.listDetailBySoDetailIds(soDetailIds);
    }

    /**
     * 根据任务单号获取销售出库信息
     *
     * @param trackNo
     * @return java.util.List<com.erp.model.wms.dto.SoOutstockDetailDTO.DeliveryQtyDTO>
     * @author yl
     * @date 2023-10-19 15:46
     */

    @PostMapping("/listByTrackNo")
    List<SoOutstockEntity> listByTrackNo(@RequestBody String trackNo) {
        return soOutstockService.listByTrackNo(trackNo);
    }

    @PostMapping("/listByAdvanceQuery")
    @WebAdvanceQuery
    List<SoOutstockEntity> listByAdvanceQuery(@RequestBody AdvanceQueryContainer container) {
        return soOutstockService.listByAdvanceQuery(container);
    }

    /**
     * 生成B2C销售出库单
     * @param b2cSoId
     * @return
     */
    @PostMapping("/generateB2cSoOutstock")
    Boolean generateB2cSoOutstock(@RequestBody String b2cSoId) {
        return soOutstockService.generateB2cSoOutstock(b2cSoId);
    }

    /**
     * 平台自动拉取数据-生成B2C销售出库单
     * @return
     */
    @PostMapping("/generateB2cSoOutstockByPlatformData")
    Boolean generateB2cSoOutstockByPlatformData(@RequestBody PlatformGenerateSoOutstockDTO platformDeliveryDetailDTO) {
        String redissonKey = CharSequenceUtil.format("{}:{}", RedisKeyConstant.SO_STOCK_KEY, platformDeliveryDetailDTO.getThirdCode());
        return soOutstockService.generateB2cSoOutstockByPlatformData(platformDeliveryDetailDTO, redissonKey);
    }

    /**
     * 生成B2C销售出库单
     * @param generateB2cDTO
     * @return
     */
    @PostMapping("/generateB2cSoOutstockByData")
    Boolean generateB2cSoOutstockByData(@RequestBody SoOutstockDTO.GenerateB2cDTO generateB2cDTO) {
        return soOutstockService.generateB2cSoOutstock(generateB2cDTO);
    }
    /**
     * 单据下推 销售出库单
     * @param generateB2cDTO
     * @return
     */
    @PostMapping("/addB2bPushDownNo")
    Boolean addB2bPushDownNo(@RequestBody List<SoOutstockDTO.GenerateSoOutstockViewDTO> generateB2cDTO) {
        return soOutstockService.addB2bPushDownNo(generateB2cDTO);
    }
    /**
     * 查询封装报关信息
     * @return
     */
    @PostMapping("/getCanGenerateDeclare")
    List<TmsDeclareBillDTO.SoOutDTO> getCanGenerateDeclare(@RequestBody TmsDeclareBillDTO.QuerySourceDTO querySourceDTO) {
        return soOutstockService.getCanGenerateDeclare(querySourceDTO);
    }

    /**
     * 统计状态
     * @return
     */
    @PostMapping("/logisticStatistics")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:so:outstock:paging",
            tableAlias = "so"
    )
    List<FirstMileDeliveryDTO.LogisticStatisticsDTO> logisticStatistics(@RequestBody FirstMileDeliveryDTO.StatisticsReq deliveryStaticsReq) {
        return soOutstockService.logisticStatistics(deliveryStaticsReq);
    }


    /**
     * 更新状态
     * @return
     */
    @PostMapping("/updateStatus")
    Boolean updateStatus(@RequestBody TmsDeclareBillDTO.UpdateStatusDTO dto) {
        return soOutstockService.updateStatus(dto);
    }

    /**
     * 重新生成销售出库单
     * @author Will
     * @date: 2024/4/28 10:26
     * @param ids
     * @return Boolean
     */
    @PostMapping("/afreshGenerateB2cOutstock")
    Boolean afreshGenerateB2cOutstock(@RequestBody List<String> ids) {
        return soOutstockService.afreshGenerateB2cOutstock(ids);
    }


    /**
     * 更新销售出库单价格
     */
    @PostMapping("/updateSoOutPrice")
    Boolean updateSoOutPrice(@RequestBody List<SoDetailEntity> soDetailEntityList) {
        return soOutstockDetailService.updateSoOutPrice(soDetailEntityList);
    }

    /**
     * B2B退货订单没有关联订单时的计算规则
     * @author jack
     * @date: 2024-11-25
     * @param params
     * @return SoOutstockDTO.AmountDTO
     */
    @PostMapping("/listAmountBySkuIds")
    List<SoOutstockDTO.AmountDTO> listAmountBySkuIds(@RequestBody SoOutstockDTO.ListAmountParamDTO params){
        return soOutstockService.listAmountBySkuIds(params);
    }

    /**
     * 根据销售 销售订单详情ids 获取是否有下推的单据
     * @author jack
     * @date 2024-12-19
     * @param soDetailIds
     * @return
     */
    @PostMapping("/getPushDownBySoDetailIds")
    public List<SoDeliveryNoticeDetailDTO.PushDownDTO> getPushDownBySoDetailIds(@RequestBody List<String> soDetailIds) {
        return soOutstockDetailService.getPushDownBySoDetailIds(soDetailIds);
    }

    @GetMapping("/listSoOutstockByExhibitionId")
    public List<ExhibitionOrderDTO.DownstreamListDTO> listSoOutstockByExhibitionId(@RequestParam(value = "exhibitionId") String exhibitionId) {
        return soOutstockService.listSoOutstockByExhibitionId(exhibitionId);
    }
}



