package com.erp.server.tms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.wms.enums.PackingTaskStatusEnum;
import com.erp.model.wms.enums.WmsDeclareStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.tms.query.TmsFmDeclareQueryHandler;
import com.erp.server.tms.service.TmsDeclareBillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.*;

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
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    /**
     * tabList
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @GetMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:tmsFmDeclareBill:paging",
            tableAlias = "db"
    )
    public ApiResult<List<TmsDeclareBillDTO.TabListDTO>> tabList(PermissionsDTO permissionsDTO) {
        return success(tmsDeclareBillService.tabList(SourceTypeEnum.FM_DECLARE_BILL,permissionsDTO));
    }

    /**
     * 分页列表
     * @author lrp
     * @date:  2024-03-19
     * @param dto
     * @return ApiResult<String>
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = TmsFmDeclareQueryHandler.class)
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:tmsFmDeclareBill:paging",
            tableAlias = "db"
    )
    public ApiResult<PagingVO<TmsDeclareBillDTO.PagingVO>> paging(@RequestBody @Valid PagingDTO<TmsDeclareBillDTO.PagingParamDTO> dto) {
        dto.getParams().setType(SourceTypeEnum.FM_DECLARE_BILL.getCode());
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
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:tmsFmDeclareBill:paging",
            tableAlias = "db"
    )
    public ApiResult<TmsDeclareBillDTO.StatisticsVO> statistics(PermissionsDTO permissionsDTO) {
        return success(tmsDeclareBillService.statisticsByFm(permissionsDTO));
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
    public ApiResult<Boolean> add(@RequestBody @Validated TmsDeclareBillDTO.AddDTO dto) {
        return success(tmsDeclareBillService.addFmDeclare(dto));
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
        menuCode = "tms:tmsFmDeclareBill:update",
        serviceClass = TmsDeclareBillService.class,
        keyIdName = "id")
    public ApiResult<Object> update(@RequestBody @Validated TmsDeclareBillDTO.UpdateDTO dto) {
        tmsDeclareBillService.update(dto,SourceTypeEnum.FM_DECLARE_BILL);
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
        TmsDeclareBillDTO.QuerySourceDTO querySourceDTO = TmsDeclareBillDTO.QuerySourceDTO.builder()
                .packingStatus(PackingTaskStatusEnum.PACKED.getCode())
                .declareStatus(WmsDeclareStatusEnum.WAIT.getCode())
                .build();
        return success(tmsDeclareBillService.getCanGenerateDeliveryOrder(querySourceDTO));
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
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:tmsFmDeclareBill:updateToDeclare",
            serviceClass = TmsDeclareBillService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> updateToDeclare(@RequestBody @Validated TmsDeclareBillDTO.UpdateDeclareStatusDTO dto) {
        List<BatchResultDTO> resultDTOList = tmsDeclareBillService.updateToDeclare(dto,SourceTypeEnum.FM_DECLARE_BILL);
        return resultDTOList.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOList) : failure(resultDTOList);
    }

    /**
     * 取消报关
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @PostMapping("/cancelDeclare")
    @LogAction(value = LogActionEnum.UPDATE, desc = "头程报关单更新状态为取消报关")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:tmsFmDeclareBill:cancelDeclare",
            serviceClass = TmsDeclareBillService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> cancelDeclare(@RequestBody @Validated TmsDeclareBillDTO.UpdateDeclareStatusDTO dto) {
        List<BatchResultDTO> resultDTOList = tmsDeclareBillService.cancelDeclare(dto);
        return resultDTOList.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOList) : failure(resultDTOList);
    }

    /**
     * 合并报关
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @PostMapping("/mergeDeclare")
    @LogAction(value = LogActionEnum.UPDATE, desc = "头程报关单合并报关")
    public ApiResult<Boolean> mergeDeclare(@RequestBody @Validated TmsDeclareBillDTO.MergeDeclareDTO dto) {
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
        List<BatchResultDTO> resultDTOList = tmsDeclareBillService.cancelMerge(dto);
        return resultDTOList.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOList) : failure(resultDTOList);
    }

    /**
     * 删除报关单
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @PostMapping("/delete")
    @LogAction(value = LogActionEnum.UPDATE, desc = "头程报关单删除")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:tmsFmDeclareBill:delete",
            serviceClass = TmsDeclareBillService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated TmsDeclareBillDTO.DeleteDTO dto) {
        List<BatchResultDTO> resultDTOList = tmsDeclareBillService.delete(dto);
        return resultDTOList.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOList) : failure(resultDTOList);
    }


    /**
     * 导出
     */
    @PostMapping("/export")
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出头程报关单")
    public ApiResult<Object>export(@RequestBody @Valid TmsDeclareBillDTO.PagingParamDTO pagingParamDTO) {
        downloadTaskFeign.saveDownloadTask("头程报关单导出", EXPORT_TMS_TMS_FM_DECLARE_BILL.getCode(), pagingParamDTO);
        return success();
    }

    /**
     * 导出报关
     */
/*    @PostMapping("/exportDeclare")
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出头程报关单报关信息")
    public ApiResult<Object>exportDeclare(@RequestBody @Valid TmsDeclareBillDTO.PagingParamDTO pagingParamDTO){
        downloadTaskFeign.saveDownloadTask("头程报关单导出", EXPORT_TMS_TMS_FM_DECLARE_BILL_DECLARE.getCode(), pagingParamDTO);
        return success();
    }*/

    @PostMapping("/exportDeclare")
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出头程报关单报关信息")
    @WebAdvanceQuery(handler = TmsFmDeclareQueryHandler.class)
    public ApiResult<Object>exportDeclare(@RequestBody @Valid TmsDeclareBillDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response) throws IOException {
        pagingParamDTO.setType(SourceTypeEnum.FM_DECLARE_BILL.getCode());
        tmsDeclareBillService.exportDeclare(pagingParamDTO,response);
        return success();
    }
}
