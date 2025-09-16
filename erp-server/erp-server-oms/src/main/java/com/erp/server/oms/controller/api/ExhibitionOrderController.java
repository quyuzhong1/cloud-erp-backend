package com.erp.server.oms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.core.utils.ExcelUtil;
import com.erp.server.oms.query.ExhibitionOrderQueryHandler;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.oms.service.ExhibitionOrderService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.ExhibitionOrderDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.oms.entity.ExhibitionOrderEntity;

/**
 * 展会订单信息
 *
 * @author jack
 * @since 2025-08-29
 */
@Slf4j
@RestController
@LogSystemModule("展会订单信息")
@RequestMapping("/exhibitionOrder")
public class ExhibitionOrderController extends BaseController {

    @Resource
    private ExhibitionOrderService exhibitionOrderService;

    /**
    * 新增
    * @author jack
    * @date:  2025-08-29
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "展会订单信息新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated ExhibitionOrderDTO.AddDTO dto) {
        return success(exhibitionOrderService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-08-29
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "展会订单信息修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:exhibitionOrder:update",
        serviceClass = ExhibitionOrderService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated ExhibitionOrderDTO.UpdateDTO dto) {
        exhibitionOrderService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:exhibitionOrder:paging",
            tableAlias = "eo"
    )
    public ApiResult<List<ExhibitionOrderDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(exhibitionOrderService.tabList(dto));
    }

    /**
    * 列表查询
    * @author jack
    * @date: 2025-08-29
    * @param dto
    * @return ApiResult<PagingVO<ExhibitionOrderDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:exhibitionOrder:paging",
            tableAlias = "eo"
    )
    @WebAdvanceQuery(handler = ExhibitionOrderQueryHandler.class)
    public ApiResult<PagingVO<ExhibitionOrderDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<ExhibitionOrderDTO.PagingParamDTO> dto) {
        return success(exhibitionOrderService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author jack
    * @date:  2025-08-29
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated ExhibitionOrderDTO.AddDTO dto) {
        BaseResultDTO.AddDTO result = exhibitionOrderService.addAndSubmit(dto);
        return success(result);
    }

    /**
    * 修改并提交审核
    * @author jack
    * @date:  2025-08-29
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:exhibitionOrder:update",
            serviceClass = ExhibitionOrderService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated ExhibitionOrderDTO.UpdateDTO dto) {
        exhibitionOrderService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author jack
    * @date:  2025-08-29
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:exhibitionOrder:submit",
            serviceClass = ExhibitionOrderService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "展会订单信息提交审核")
    public ApiResult<List<BatchResultDTO>> batchSubmit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<ExhibitionOrderEntity> list = exhibitionOrderService.lambdaQuery().in(ExhibitionOrderEntity::getId, ids).list();
		Map<String, ExhibitionOrderEntity> idEntityMap = list.stream().collect(Collectors.toMap(ExhibitionOrderEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = exhibitionOrderService.submit(id);
            }catch (Exception e){
                log.error("展会订单信息 提交审核失败",e);
                ExhibitionOrderEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "展会订单信息不存在, 提交失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 审核
    * @author jack
    * @date:  2025-08-29
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:exhibitionOrder:approve",
            serviceClass = ExhibitionOrderService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "展会订单信息审核")
    public ApiResult<List<BatchResultDTO>> batchApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<ExhibitionOrderEntity> list = exhibitionOrderService.lambdaQuery().in(ExhibitionOrderEntity::getId, ids).list();
		Map<String, ExhibitionOrderEntity> idEntityMap = list.stream().collect(Collectors.toMap(ExhibitionOrderEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = exhibitionOrderService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("展会订单信息审核失败",e);
                ExhibitionOrderEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "展会订单信息不存在, 审核失败");
                    resultDTOS.add(approveResult);
                    continue;
                }
                approveResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(approveResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 反审核
    * @author jack
    * @date:  2025-08-29
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:exhibitionOrder:disApprove",
            serviceClass = ExhibitionOrderService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "展会订单信息反审核")
    public ApiResult<List<BatchResultDTO>> batchDisApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<ExhibitionOrderEntity> list = exhibitionOrderService.lambdaQuery().in(ExhibitionOrderEntity::getId, ids).list();
		Map<String, ExhibitionOrderEntity> idEntityMap = list.stream().collect(Collectors.toMap(ExhibitionOrderEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = exhibitionOrderService.disApprove(id);
            }catch (Exception e){
                log.error("展会订单信息反审核失败",e);
                ExhibitionOrderEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "展会订单信息不存在, 反审核失败");
                    resultDTOS.add(disApproveResult);
                    continue;
                }
                disApproveResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(disApproveResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
    * 删除
    * @author jack
    * @date:  2025-08-29
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:exhibitionOrder:delete",
            serviceClass = ExhibitionOrderService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "展会订单信息删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<ExhibitionOrderEntity> list = exhibitionOrderService.lambdaQuery().in(ExhibitionOrderEntity::getId, ids).list();
		Map<String, ExhibitionOrderEntity> idEntityMap = list.stream().collect(Collectors.toMap(ExhibitionOrderEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = exhibitionOrderService.delete(id);
            }catch (Exception e){
                log.error("展会订单信息删除失败",e);
                ExhibitionOrderEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "展会订单信息不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 作废
     * @author jack
     * @date:  2025-08-29
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:exhibitionOrder:invalid",
            serviceClass = ExhibitionOrderService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INVALID, desc = "展会订单信息作废")
    public ApiResult<List<BatchResultDTO>> batchInvalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<ExhibitionOrderEntity> list = exhibitionOrderService.lambdaQuery().in(ExhibitionOrderEntity::getId, ids).list();
        Map<String, ExhibitionOrderEntity> idEntityMap = list.stream().collect(Collectors.toMap(ExhibitionOrderEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = exhibitionOrderService.invalid(id,dto.getRemark());
            }catch (Exception e){
                log.error("展会订单信息作废失败",e);
                ExhibitionOrderEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "展会订单信息不存在, 作废失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 撤销
    * @author jack
    * @date:  2025-08-29
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:exhibitionOrder:cancelProcess",
            serviceClass = ExhibitionOrderService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "展会订单信息撤销")
    public ApiResult<List<BatchResultDTO>> batchCancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<ExhibitionOrderEntity> list = exhibitionOrderService.lambdaQuery().in(ExhibitionOrderEntity::getId, ids).list();
        Map<String, ExhibitionOrderEntity> idEntityMap = list.stream().collect(Collectors.toMap(ExhibitionOrderEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = exhibitionOrderService.cancelProcess(id);
            }catch (Exception e){
                log.error("展会订单信息撤回流程失败",e);
                ExhibitionOrderEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "展会订单信息不存在, 撤回流程失败");
                    resultDTOS.add(cancelResult);
                    continue;
                }
                cancelResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(cancelResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 详情
    * @author jack
    * @date:  2025-08-29
    * @param id
    * @return ApiResult<ExhibitionOrderDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @LogViewService
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:exhibitionOrder:update",
            serviceClass = ExhibitionOrderService.class,
            keyIdName = "id")
    public ApiResult<ExhibitionOrderDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(exhibitionOrderService.view(id));
    }

    /**
    * 导出Excel数据
    * @author jack
    * @date:  2025-08-29
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:exhibitionOrder:export",
            tableAlias = "eo"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "展会订单信息导出Excel数据")
    @WebAdvanceQuery(handler = ExhibitionOrderQueryHandler.class)
    public ApiResult<Object> exportList(@RequestBody @Validated ExhibitionOrderDTO.PagingParamDTO dto, HttpServletResponse response) {
        exhibitionOrderService.exportList(dto, response);
        return success();
    }

    /**
     *  异步导入
     * @author jack
     * @date:  2025-08-20
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入样品借用单")
    @PostMapping("/importFile")
    public ApiResult importExcel(@RequestBody BaseDTO.ImportDTO dto) {
        Boolean result = exhibitionOrderService.importFile(dto);
        return result ? success() : failure();
    }

    /**
     * 下载模板
     * @author jack
     * @date:  2025-08-20
     * @param response
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "展会订单下载模板")
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        String standardPath = "classpath:excel/exhibitionOrderTemplate.xlsx";
        String standardExcelName = "exhibitionOrderTemplate.xlsx";
        ExcelUtil.downloadTemplate(standardPath, standardExcelName, response);
        return success();
    }

    /**
     * 单据管理：根据id查询关联查询销售出库单
     * @author jack
     * @date:  2025-08-29
     */
    @GetMapping("/listSoOutstockByExhibitionId")
    public ApiResult<List<ExhibitionOrderDTO.DownstreamListDTO>>  listSoOutstockByExhibitionId(@RequestParam(value = "id",required = true)String id){
        return success(exhibitionOrderService.listSoOutstockByExhibitionId(id));
    }
    /**
     * 单据管理：根据id查询关联查询其他入库单
     * @author jack
     * @date:  2025-08-29
     */
    @GetMapping("/listOtherInstockByExhibitionId")
    public ApiResult<List<ExhibitionOrderDTO.DownstreamListDTO>>  listOtherInstockByExhibitionId(@RequestParam(value = "id",required = true)String id){
        return success(exhibitionOrderService.listOtherInstockByExhibitionId(id));
    }
    /**
     * 单据管理：根据id查询关联查询B2B销售单
     * @author jack
     * @date:  2025-08-29
     */
    @GetMapping("/listSoByExhibitionId")
    public ApiResult<List<ExhibitionOrderDTO.DownstreamListDTO>>  listSoByExhibitionId(@RequestParam(value = "id",required = true)String id){
        return success(exhibitionOrderService.listSoByExhibitionId(id));
    }



}
