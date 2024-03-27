package com.erp.server.tms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.TmsFirstMileLogisticDTO;
import com.erp.server.tms.query.TmsFirstMileLogisticQueryHandler;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.tms.service.TmsDeclareBillService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.TmsDeclareBillDTO;

import java.util.List;

/**
 * 头程报关单
 *
 * @author lrp
 * @since 2024-03-27
 */
@Slf4j
@RestController
@LogSystemModule("头程报关单")
@RequestMapping("/tmsFmDeclareBill")
public class TmsFmDeclareBillController extends BaseController {

    @Resource
    private TmsDeclareBillService tmsDeclareBillService;

    /**
     * tabList
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @GetMapping("/tabList")
    public ApiResult<List<TmsDeclareBillDTO.TabListDTO>> tabList() {
        return success(tmsDeclareBillService.tabList());
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
    public ApiResult<PagingVO<TmsDeclareBillDTO.PagingVO>> paging(@RequestBody @Valid PagingDTO<TmsDeclareBillDTO.PagingParamDTO> dto) {
        PagingVO<TmsDeclareBillDTO.PagingVO> pagingVO = tmsDeclareBillService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 列表统计
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @GetMapping("/statistics")
    public ApiResult<TmsDeclareBillDTO.StatisticsVO> statistics() {
        return success(tmsDeclareBillService.statistics());
    }

    /**
    * 新增
    * @author lrp
    * @date:  2024-03-27
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "头程报关单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated TmsDeclareBillDTO.AddDTO dto) {
        return success(tmsDeclareBillService.add(dto));
    }

    /**
    * 修改
    * @author lrp
    * @date:  2024-03-27
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "头程报关单修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:tmsDeclareBill:update",
        serviceClass = TmsDeclareBillService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated TmsDeclareBillDTO.UpdateDTO dto) {
        tmsDeclareBillService.update(dto);
        return success();
    }

    /**
     * 查询符合生成条件的发货单
     * @author lrp
     * @date:  2024-03-27
     * @return ApiResult
     */
    @GetMapping("/getCanGenerateDeliveryOrder")
    public ApiResult<List<TmsDeclareBillDTO.DeliveryDTO>> getCanGenerateDeliveryOrder() {
        return success(tmsDeclareBillService.getCanGenerateDeliveryOrder());
    }

    /**
     * 详情
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @GetMapping("/view")
    public ApiResult<TmsDeclareBillDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(tmsDeclareBillService.view(id));
    }

    /**
     * 更新状态为已报关
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @PostMapping("/updateToDeclare")
    @LogAction(value = LogActionEnum.UPDATE, desc = "头程报关单更新状态为已报关")
    public ApiResult<List<BatchResultDTO>> updateToDeclare(@RequestBody @Validated TmsDeclareBillDTO.UpdateDeclareStatusDTO dto) {
        return success(tmsDeclareBillService.updateToDeclare(dto));
    }

    /**
     * 取消报关
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @PostMapping("/cancelDeclare")
    @LogAction(value = LogActionEnum.UPDATE, desc = "头程报关单更新状态为取消报关")
    public ApiResult<List<BatchResultDTO>> cancelDeclare(@RequestBody @Validated TmsDeclareBillDTO.UpdateDeclareStatusDTO dto) {
        return success(tmsDeclareBillService.cancelDeclare(dto));
    }

    /**
     * 合并报关
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @PostMapping("/mergeDeclare")
    @LogAction(value = LogActionEnum.UPDATE, desc = "头程报关单合并报关")
    public ApiResult<List<BatchResultDTO>> mergeDeclare(@RequestBody @Validated TmsDeclareBillDTO.MergeDeclareDTO dto) {
        return success(tmsDeclareBillService.mergeDeclare(dto));
    }

    /**
     * 取消合并
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @PostMapping("/cancelMerge")
    @LogAction(value = LogActionEnum.UPDATE, desc = "头程报关单取消合并")
    public ApiResult<List<BatchResultDTO>> cancelMerge(@RequestBody @Validated TmsDeclareBillDTO.MergeDeclareDTO dto) {
        return success(tmsDeclareBillService.cancelMerge(dto));
    }

    /**
     * 删除报关单
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @PostMapping("/delete")
    @LogAction(value = LogActionEnum.UPDATE, desc = "头程报关单删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated TmsDeclareBillDTO.DeleteDTO dto) {
        return success(tmsDeclareBillService.delete(dto));
    }


    /**
     * 导出
     */
    @PostMapping("/export")
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出头程报关单")
    @WebAdvanceQuery
    public ApiResult export(@RequestBody @Valid TmsDeclareBillDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response) {
        tmsDeclareBillService.export(pagingParamDTO,response);
        return success();
    }

    /**
     * 导出报关
     */
    @PostMapping("/exportDeclare")
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出头程报关单报关信息")
    @WebAdvanceQuery
    public ApiResult exportDeclare(@RequestBody @Valid TmsDeclareBillDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response) {
        tmsDeclareBillService.exportDeclare(pagingParamDTO,response);
        return success();
    }
}
