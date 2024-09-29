package com.erp.rpc.tms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.tms.vo.response.CancelResponseVO;
import com.erp.model.tms.vo.response.InterceptResponseVO;
import feign.Request;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@FeignClient(name = "erp-tms", contextId = "logisticsBill",configuration = {FeignErrorDecoder.class})
public interface LogisticsBillFeign {

    /**
     * 新增物流单
     * @Author Luo_WG
     * @Date 2023/11/9 17:59
     * @param addDTOList
     * @return java.lang.Boolean
     **/
    @PostMapping("feign/logisticsBill/logisticsBillBatchSave")
    Boolean logisticsBillBatchSave(@RequestBody List<LogisticsBillDTO.AddDTO> addDTOList);


    /**
     * 根据来源id查询物流信息及跟踪号
     * @Author Luo_WG
     * @Date 2023/11/10 9:06
     * @param sourceIdList
     * @return java.util.List<com.erp.model.tms.dto.LogisticsBillDTO.LogisticsBillVo>
     **/
    @PostMapping("feign/logisticsBill/listLogisticsBillVoBySourceIds")
    List<LogisticsBillDTO.LogisticsBillVo> listLogisticsBillVoBySourceIds(@RequestBody List<String> sourceIdList);

    /**
     * 根据来源获取运输状态
     *
     * @param billVoList 来源
     * @return LogisticsBillDTO.LogisticsBillVo
     * @author hyj
     * @date 2024/5/11 10:39
     */
    @PostMapping("feign/logisticsBill/getTrackStatusByTrackNo")
    List<LogisticsBillDTO.LogisticsBillVo> getTrackStatusByTrackNo(@RequestBody List<LogisticsBillDTO.LogisticsBillVo> billVoList);

    /**
     * 添加物流单
     *@parms
     *@return 
     *@author yl
     *@date 2023-11-20
     */
    @PostMapping("feign/logisticsBill/addLogisticsBill")
    void addLogisticsBill(@RequestBody LogisticsBillDTO.AddDTO addDTO);

    /**
     * 获取物流单数据 用于查询轨迹
     * @param query
     * @return
     */
    @PostMapping("/feign/logisticsBill/listTrackDto")
    List<LogisticsTrackDTO.UpdateTrackDTO> listTrackDto(@RequestBody LogisticsBillDetailQueryDTO query);

    /**
     * 删除物流单
     * @return
     */
    @PostMapping("/feign/logisticsBill/removeLogisticsBill")
    Boolean removeLogisticsBill(@RequestBody LogisticsBillDTO.RemoveDTO dto);

    /**
     * 删除物流单
     * @return
     */
    @PostMapping("/feign/logisticsBill/updateTrackNo")
    Boolean updateTrackNo(@RequestBody LogisticsBillDTO.UpdateTrackNoDTO dto);

    /**
     * 自动生成物流单
     * @return
     */
    @PostMapping("/feign/logisticsBill/generateBill")
    LogisticsBillDTO.GenerateBillResultDTO generateBill(@RequestBody LogisticsBillDTO.GenerateBillDTO dto);

    /**
     * 取消物流单
     * @return
     */
    @PostMapping("/feign/logisticsBill/cancelBill")
    ApiResult<CancelResponseVO> cancelBill(@RequestBody LogisticsBillDTO.CancelBillDTO dto);

    /**
     * 拦截物流单
     * @return
     */
    @PostMapping("/feign/logisticsBill/interceptBill")
    ApiResult<InterceptResponseVO> interceptBill(@RequestBody LogisticsBillDTO.CancelBillDTO dto);

    /**
     * 根据物流跟踪单号或运单号查询物流单详情
     * @author will
     * @date 2024/7/3 17:36
     * @param logisticsCode
     * @return BaseDTO
     */
    @GetMapping("/feign/logisticsBill/getByTrackNoOrTransportNo")
    LogisticsBillDTO.BaseDTO getByTrackNoOrTransportNo(@RequestParam(value = "logisticsCode") String logisticsCode);

    /**
     * 根据物流跟踪单号查询物流单详情
     * @Author Luo_WG
     * @Date 2023/12/14 15:45
     * @param trackNo
     * @return com.erp.model.tms.dto.LogisticsBillDTO.BaseDTO
     **/
    @GetMapping("/feign/logisticsBill/getLogisticsBillByTrackNo")
    LogisticsBillDTO.BaseDTO getLogisticsBillByTrackNo(@RequestParam(value = "trackNo") String trackNo);

    /**
     * 根据物流跟踪单号查询物流单详情
     * @Author Luo_WG
     * @Date 2023/12/14 15:45
     * @param transportNoList
     * @return com.erp.model.tms.dto.LogisticsBillDTO.BaseDTO
     **/
    @PostMapping("/feign/logisticsBill/listLogisticsBillByTrackNos")
    List<LogisticsBillDTO.BaseDTO> listLogisticsBillByTrackNos(@RequestBody List<String> transportNoList);

    /**
     * 打印物流面单/配货单
     * @Author Luo_WG
     * @Date 2023/12/20 14:34
     * @param list
     * @return java.util.List<com.erp.model.oms.dto.SoB2cDTO.WaybillDTO>
     **/
    @PostMapping("/feign/logisticsBill/printLogisticsWaybill")
    List<SoB2cDTO.WaybillDTO> printLogisticsWaybill(Request.Options options, @RequestBody List<LogisticsBillDTO.PrintLogisticsWaybillDTO> list);

    /**
     * 根据渠道id查询渠道打印类型
     * @Author Luo_WG
     * @Date 2023/12/20 17:12
     * @param channelIdList
     * @return java.util.List<com.erp.model.tms.entity.LogisticsPrintTypeEntity>
     **/
    @PostMapping("/feign/logisticsBill/listPrintTypeByChannelIds")
    List<LogisticsPrintTypeDTO.ViewDTO> listPrintTypeByChannelIds(@RequestBody List<String> channelIdList);

    /**
     * 批量根据销售出库单更新跟踪号
     * @param batchUpdateTrackNoDTOList
     * @return
     */
    @PostMapping("/feign/logisticsBill/updateBatchTrackNo")
    List<BatchResultDTO> updateBatchTrackNo(@RequestBody List<LogisticsBillDTO.BatchUpdateTrackNoDTO> batchUpdateTrackNoDTOList,@RequestParam(name = "isAdd") Boolean isAdd);

    /**
     * 查询跟踪号与出库单号Map
     * @param ids
     * @return
     */
    @PostMapping("/feign/logisticsBill/mapTrackNoAndSoOutId")
    Map<String, List<String>> mapTrackNoAndSoOutId(@RequestBody List<String> ids);

    /**
     * 根据高级查询条件查询出库Id
     * @return
     */
    @PostMapping("/feign/logisticsBill/listSoOutIdByQuery")
    List<String> listSoOutIdByQuery(@RequestBody AdvanceQueryContainer advanceQueryContainer);

    /**
     * 根据销售出库单 获取销售出库单自发货费用列表
     * @param ids
     * @return
     */
    @PostMapping("/feign/logisticsBill/listBillCostByOutstockIds")
    List<LogisticsBillCostDTO.OutStockDTO> listBillCostByOutstockIds(@RequestBody List<String> ids);

    /**
     * 根据sourceId删除物流单
     * @return
     */
    @PostMapping("/feign/logisticsBill/removeLogisticsBillBySourceId")
    Boolean removeLogisticsBillBySourceId(@RequestBody List<String> sourceId);

    /**
     * @description: 根据销售出库单id集合查询
     * @author Will
     * @date: 2024/5/20 16:04
     * @param outstockIdList
     * @return List<LogisticsBillEntity>
     */
    @PostMapping("/feign/logisticsBill/listBySoOutStockIdList")
    List<LogisticsBillEntity> listBySoOutStockIdList(@RequestBody List<String> outstockIdList);

    /**
     * 更新物流单重量
     * @param dto
     * @return
     */
    @PostMapping("/feign/logisticsBill/updateLogisticWeight")
    ApiResult<String> updateLogisticWeight(@RequestBody LogisticsBillDTO.UpdateWeight dto);
}
