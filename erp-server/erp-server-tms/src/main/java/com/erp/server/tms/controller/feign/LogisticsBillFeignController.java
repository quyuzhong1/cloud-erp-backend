package com.erp.server.tms.controller.feign;

import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.dto.LogisticsBillDetailQueryDTO;
import com.erp.model.tms.dto.LogisticsPrintTypeDTO;
import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.tms.entity.LogisticsPrintTypeEntity;
import com.erp.model.tms.vo.response.CancelResponseVO;
import com.erp.model.tms.vo.response.InterceptResponseVO;
import com.erp.server.tms.service.LogisticsBillCostService;
import com.erp.server.tms.service.LogisticsBillDetailService;
import com.erp.server.tms.service.LogisticsBillService;
import com.erp.server.tms.service.LogisticsPrintTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@LogSystemModule("物流单feign接口")
@RequestMapping("/feign/logisticsBill")
public class LogisticsBillFeignController {
    @Resource
    private LogisticsBillService logisticsBillService;
    @Resource
    private LogisticsBillDetailService logisticsBillDetailService;
    @Resource
    private LogisticsPrintTypeService logisticsPrintTypeService;
    @Resource
    private LogisticsBillCostService logisticsBillCostService;
    /**
     * 新增物流单
     *
     * @param addDTOList
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/11/9 17:59
     **/
    @PostMapping("/logisticsBillBatchSave")
    public Boolean logisticsBillBatchSave(@RequestBody List<LogisticsBillDTO.AddDTO> addDTOList) {
        Boolean flag = logisticsBillService.logisticsBillBatchSave(addDTOList);
        return flag;
    }

    /**
     * 添加物流单
     *
     * @return java.lang.Boolean
     * @parms addDTO
     * @author yl
     * @date 2023-11-17
     */
    @PostMapping("/addLogisticsBill")
    public Boolean addLogisticsBill(@RequestBody LogisticsBillDTO.AddDTO addDTO) {
        Boolean flag = logisticsBillService.add(addDTO);
        return flag;
    }

    @PostMapping("/removeLogisticsBill")
    public Boolean removeLogisticsBill(@RequestBody LogisticsBillDTO.RemoveDTO dto) {
        Boolean flag = logisticsBillService.remove(dto);
        return flag;
    }

    /**
     * 根据来源id查询物流信息及跟踪号
     *
     * @param sourceIdList
     * @return java.util.List<com.erp.model.tms.dto.LogisticsBillDTO.LogisticsBillVo>
     * @Author Luo_WG
     * @Date 2023/11/10 9:06
     **/
    @PostMapping("/listLogisticsBillVoBySourceIds")
    public List<LogisticsBillDTO.LogisticsBillVo> listLogisticsBillVoBySourceIds(@RequestBody List<String> sourceIdList) {
        List<LogisticsBillDTO.LogisticsBillVo> flag = logisticsBillService.listLogisticsBillVoBySourceIds(sourceIdList);
        return flag;
    }

    /**
     * 生成物流单 像物流商下單
     *
     * @return
     * @parms
     * @author yl
     * @date 2023-11-23
     */
    @PostMapping("/generateBill")
    public LogisticsBillDTO.GenerateBillResultDTO generateBill(@RequestBody @Valid LogisticsBillDTO.GenerateBillDTO dto) {
        return logisticsBillService.generateBill(dto);
    }
    
    /**
     * 取消物流单
     * @author yl
     * @date 2023-12-08 11:52
     * @param dto
     * @return 
     */
    @PostMapping("/cancelBill")
    public ApiResult<CancelResponseVO> cancelBill(@RequestBody @Valid LogisticsBillDTO.CancelBillDTO dto) {
        return logisticsBillService.cancelBill(dto);
    }

    /**
     * 拦截物流单
     * @param dto
     * @return
     */
    @PostMapping("/interceptBill")
    public ApiResult<InterceptResponseVO> interceptBill(@RequestBody @Valid LogisticsBillDTO.CancelBillDTO dto) {
        return logisticsBillService.interceptBill(dto);
    }

    /**
     * 获取物流单数据 用于查询轨迹
     *
     * @param query
     * @return
     */
    @PostMapping("/getLogisticsBillDetails")
    public PagingVO<LogisticsBillDetailEntity> getLogisticsBillDetails(@RequestBody LogisticsBillDetailQueryDTO query) {
        PagingVO<LogisticsBillDetailEntity> page = logisticsBillDetailService.getPage(query);
        return page;
    }


    /**
     * 更改运单号
     *
     * @return
     * @parms
     * @author yl
     * @date 2023-11-24
     */
    @PostMapping("/updateTrackNo")
    public Boolean updateTrackNo(@RequestBody LogisticsBillDTO.UpdateTrackNoDTO billDTO) {
        Boolean result = logisticsBillDetailService.updateTrackNo(billDTO);
        return result;
    }

    /**
     * 根据物流跟踪单号查询物流单详情
     * @Author Luo_WG
     * @Date 2023/12/14 15:45
     * @param trackNo
     * @return com.erp.model.tms.dto.LogisticsBillDTO.BaseDTO
     **/
    @GetMapping("/getLogisticsBillByTrackNo")
    public LogisticsBillDTO.BaseDTO getLogisticsBillByTrackNo(@RequestParam(value = "trackNo") String trackNo) {
        LogisticsBillDTO.BaseDTO entity = logisticsBillService.getBaseByTrackNo(trackNo);
        return entity;
    }
    /**
     * 根据物流跟踪单号查询物流单详情
     * @Author Luo_WG
     * @Date 2023/12/14 15:45
     * @param transportNoList
     * @return com.erp.model.tms.dto.LogisticsBillDTO.BaseDTO
     **/
    @PostMapping("/listLogisticsBillByTrackNos")
    public List<LogisticsBillDTO.BaseDTO> listLogisticsBillByTrackNos(@RequestBody List<String> transportNoList) {
        List<LogisticsBillDTO.BaseDTO> list = logisticsBillService.listLogisticsBillByTransportNos(transportNoList);
        return list;
    }

    /**
     * 打印物流面单/配货单
     * @Author Luo_WG
     * @Date 2023/12/20 14:34
     * @param list
     * @return java.util.List<com.erp.model.oms.dto.SoB2cDTO.WaybillDTO>
     **/
    @PostMapping("/printLogisticsWaybill")
    public List<SoB2cDTO.WaybillDTO> printLogisticsWaybill(@RequestBody List<LogisticsBillDTO.PrintLogisticsWaybillDTO> list) {
        List<SoB2cDTO.WaybillDTO> waybillDTOList = logisticsBillService.printLogisticsWaybill(list);
        return waybillDTOList;
    }

    /**
     * 根据渠道id查询渠道打印类型
     * @Author Luo_WG
     * @Date 2023/12/20 17:12
     * @param channelIdList
     * @return java.util.List<com.erp.model.tms.entity.LogisticsPrintTypeEntity>
     **/
    @PostMapping("/listPrintTypeByChannelIds")
    public List<LogisticsPrintTypeDTO.ViewDTO> listPrintTypeByChannelIds(@RequestBody List<String> channelIdList) {
        List<LogisticsPrintTypeDTO.ViewDTO> logisticsPrintTypeEntities = logisticsPrintTypeService.listByChannelIds(channelIdList);
        return logisticsPrintTypeEntities;
    }

    /**
     * 批量根据销售出库单更新跟踪号
     * @param batchUpdateTrackNoDTOList
     * @return
     */
    @PostMapping("/updateBatchTrackNo")
    public List<BatchResultDTO> updateBatchTrackNo(@RequestBody List<LogisticsBillDTO.BatchUpdateTrackNoDTO> batchUpdateTrackNoDTOList,@RequestParam(name = "isAdd") Boolean isAdd){
        return logisticsBillService.updateBatchTrackNo(batchUpdateTrackNoDTOList,isAdd);
    }

    /**
     * 查询跟踪号与出库单号Map
     * @param ids
     * @return
     */
    @PostMapping("/mapTrackNoAndSoOutId")
    public Map<String, List<String>> mapTrackNoAndSoOutId(@RequestBody List<String> ids){
        return logisticsBillService.mapTrackNoAndSoOutId(ids);
    }

    /**
     * 根据高级查询条件查询出库Id
     * @return
     */
    @PostMapping("/listSoOutIdByQuery")
    @WebAdvanceQuery
    public List<String> listSoOutIdByQuery(@RequestBody AdvanceQueryContainer advanceQueryContainer){
        return logisticsBillService.listSoOutIdByQuery(advanceQueryContainer);
    }

    /**
     * 根据销售出库单 获取销售出库单自发货费用列表
     * @param ids
     * @return
     */
    @PostMapping("/listBillCostByOutstockIds")
    public List<LogisticsBillCostDTO.OutStockDTO> listBillCostByOutstockIds(@RequestBody List<String> ids){
        return logisticsBillCostService.listBillCostByOutstockIds(ids);
    }
}
