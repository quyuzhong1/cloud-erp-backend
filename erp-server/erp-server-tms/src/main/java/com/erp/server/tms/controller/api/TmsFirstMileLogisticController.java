package com.erp.server.tms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.tms.dto.TmsFirstMileLogisticDTO;
import com.erp.server.tms.service.TmsFirstMileLogisticService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * 头程物流单
 * @author lrp
 * @since 2024-03-19
 */
@Slf4j
@RestController
@LogSystemModule("头程物流单")
@RequestMapping("/tmsFirstMileLogistic")
public class TmsFirstMileLogisticController extends BaseController {

    @Resource
    private TmsFirstMileLogisticService tmsFirstMileLogisticService;

    /**
     * tabList
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @GetMapping("/tabList")
    public ApiResult<List<TmsFirstMileLogisticDTO.TabListDTO>> tabList() {
        return success(tmsFirstMileLogisticService.tabList());
    }

    /**
     * 分页列表
     * @author lrp
     * @date:  2024-03-19
     * @param dto
     * @return ApiResult<String>
     */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<TmsFirstMileLogisticDTO.PagingVO>> paging(@RequestBody @Valid PagingDTO<TmsFirstMileLogisticDTO.PagingParamDTO> dto) {
        PagingVO<TmsFirstMileLogisticDTO.PagingVO> pagingVO = tmsFirstMileLogisticService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 列表统计
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @GetMapping("/statistics")
    public ApiResult<TmsFirstMileLogisticDTO.StatisticsVO> statistics() {
        return success(tmsFirstMileLogisticService.statistics());
    }

    /**
     * 新增
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "头程物流单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Valid TmsFirstMileLogisticDTO.AddDTO dto) {
        return success(tmsFirstMileLogisticService.add(dto));
    }

    /**
     * 编辑
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "头程物流单更新")
    public ApiResult<Boolean> update(@RequestBody @Valid TmsFirstMileLogisticDTO.UpdateDTO dto) {
        return success(tmsFirstMileLogisticService.update(dto));
    }

    /**
     * 详情
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @GetMapping("/view")
    public ApiResult<TmsFirstMileLogisticDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(tmsFirstMileLogisticService.view(id));
    }

    /**
     * 更新物流状态
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @PostMapping("/updateLogisticsStatus")
    @LogAction(value = LogActionEnum.UPDATE, desc = "头程物流单更新物流状态")
    public ApiResult<List<BatchResultDTO>> updateLogisticsStatus(@RequestBody @Valid TmsFirstMileLogisticDTO.UpdateLogisticsStatusDTO dto) {
        List<BatchResultDTO> resultDTOS = tmsFirstMileLogisticService.updateLogisticsStatus(dto);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 更新备注
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @PostMapping("/updateRemark")
    @LogAction(value = LogActionEnum.UPDATE, desc = "头程物流单更新备注")
    public ApiResult<Boolean> updateRemark(@RequestBody @Valid TmsFirstMileLogisticDTO.UpdateRemarkDTO dto) {
        return success(tmsFirstMileLogisticService.updateRemark(dto));
    }

    /**
     * 更新发票状态
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @PostMapping("/updateInvoicesStatus")
    @LogAction(value = LogActionEnum.UPDATE, desc = "头程物流单更新发票状态")
    public ApiResult<List<BatchResultDTO>> updateInvoicesStatus(@RequestBody @Valid TmsFirstMileLogisticDTO.UpdateInvoicesStatusDTO dto) {
        List<BatchResultDTO> resultDTOS = tmsFirstMileLogisticService.updateInvoicesStatus(dto);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 发票导出
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @PostMapping("/exportInvoices")
    @LogAction(value = LogActionEnum.EXPORT, desc = "头程物流单发票导出")
    public ApiResult<Boolean> exportInvoices(@RequestBody @Valid BaseIdsDTO.IdsDTO dto, HttpServletResponse response) {
        tmsFirstMileLogisticService.exportInvoices(dto.getIds(),response);
        return ApiResult.success();
    }
    /**
     * 更新渠道
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @PostMapping("/updateChannel")
    @LogAction(value = LogActionEnum.UPDATE, desc = "头程物流单更新渠道")
    public ApiResult<List<BatchResultDTO>> updateChannel(@RequestBody @Valid TmsFirstMileLogisticDTO.UpdateChannelDTO dto) {
        List<BatchResultDTO> resultDTOS = tmsFirstMileLogisticService.updateChannel(dto);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 生成对账单
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @PostMapping("/generateReconciliation")
    @LogAction(value = LogActionEnum.UPDATE, desc = "头程物流单生成对账单")
    public ApiResult<List<BatchResultDTO>> generateReconciliation(@RequestBody @Valid TmsFirstMileLogisticDTO.GenerateReconciliationDTO dto) {
        List<BatchResultDTO> resultDTOS = tmsFirstMileLogisticService.generateReconciliation(dto);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 下载模板
     */
    @GetMapping("/exportTemplate")
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载头程物流单模板")
    public ApiResult exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "excel/fmLogistics.xlsx";
        String excelName = "template.xlsx";
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), "ISO8859-1"));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_95131);
        }
        return success();
    }

    /**
     * 头程物流单导入
     */
    @PostMapping("/import")
    @LogAction(value = LogActionEnum.IMPORT, desc = "头程物流单导入")
    public ApiResult<Boolean> importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) throws Exception {
        return success(tmsFirstMileLogisticService.importExcel(excelFile,response));
    }

    /**
     * 导出
     */
    @PostMapping("/export")
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出头程物流单")
    @WebAdvanceQuery
    public ApiResult export(@RequestBody @Valid TmsFirstMileLogisticDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response) {
        tmsFirstMileLogisticService.export(pagingParamDTO,response);
        return success();
    }

    /**
     * 导出费用明细
     */
    @PostMapping("/exportFeeDetail")
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出头程物流单费用明细")
    public ApiResult exportFeeDetail(@RequestBody @Valid TmsFirstMileLogisticDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response) {
        tmsFirstMileLogisticService.exportFeeDetail(pagingParamDTO,response);
        return success();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @LogAction(value = LogActionEnum.DELETE, desc = "删除头程物流单")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = tmsFirstMileLogisticService.delete(dto.getIds());
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

//    /**
//     * 查询历史轨迹
//     */
//    @GetMapping("/getHistoryTrack")
//    public ApiResult<TmsFirstMileLogisticDTO.HistoryTrackDTO> getHistoryTrack(@RequestParam("id") String id) {
//        return success(tmsFirstMileLogisticService.getHistoryTrack(id));
//    }

    /**
     * 查询符合生成条件的发货单
     */
    @PostMapping("/getCanGenerateDeliveryOrder")
    public ApiResult<List<TmsFirstMileLogisticDTO.DeliveryDTO>> getCanGenerateDeliveryOrder(@RequestBody TmsFirstMileLogisticDTO.CanGenerateDeliveryDTO dto) {
        return success(tmsFirstMileLogisticService.getCanGenerateDeliveryOrder(dto));
    }

    /**
     * 选择渠道后返回对应数据
     */
    @PostMapping("getLogisticsAndShipping")
    public ApiResult<TmsFirstMileLogisticDTO.LogisticsDTO> getLogisticsAndShipping(@RequestBody TmsFirstMileLogisticDTO.CanGenerateDeliveryDTO dto){
        return success(tmsFirstMileLogisticService.getLogisticsAndShipping(dto));
    }
}
