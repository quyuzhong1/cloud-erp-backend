package com.erp.server.plm.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.common.core.utils.ExcelUtil;
import com.erp.model.plm.dto.SkuStdCostDetailDTO;
import com.erp.model.plm.entity.SkuStdCostDetailEntity;
import com.erp.server.plm.query.SkuStdCostDetailQueryHandler;
import com.erp.server.plm.service.SkuStdCostDetailService;
import com.erp.server.plm.service.SkuStdCostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * sku标准成本表
 *
 * @author Jim
 * @since 2025-08-08
 */
@Slf4j
@RestController
@LogSystemModule("sku标准成本表")
@RequestMapping("/skuStdCost")
public class SkuStdCostDetailController extends BaseController {

    @Resource
    private SkuStdCostDetailService skuStdCostDetailService;


    /**
     * 价格变更列表(校验列表是否可变更)
     * @author Jim
     * @date: 2025-08-08
     * @param dto
     * @return ApiResult<List<SkuStdCostDetailDTO.ListDTO>>
     */
    @PostMapping("/changeList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:skuStdCost:paging",
            tableAlias = ""
    )
    public ApiResult<List<SkuStdCostDetailDTO.ListDTO>> changeList(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return success(skuStdCostDetailService.changeList(dto));
    }

    /**
    * 价格变更
    * @author Jim
    * @date:  2025-08-08
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/change")
    @LogAction(value = LogActionEnum.INSERT, desc = "sku标准成本表价格变更")
    public ApiResult<List<BatchResultDTO>> changeAdd(@RequestBody @Validated List<SkuStdCostDetailDTO.ChangeDTO> dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.size());
        List<String> ids = dto.stream().map(e -> e.getId()).distinct().collect(Collectors.toList());
        // 数据查询放入外层，处理结果统一更新或单条更新
        List<SkuStdCostDetailEntity> list = skuStdCostDetailService.lambdaQuery().in(SkuStdCostDetailEntity::getId, ids).list();
        Map<String, SkuStdCostDetailEntity> idEntityMap = list.stream().collect(Collectors.toMap(SkuStdCostDetailEntity::getId, w -> w));
        for (SkuStdCostDetailDTO.ChangeDTO changeDTO : dto) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = skuStdCostDetailService.changeAdd(changeDTO);
            }catch (Exception e){
                log.error("sku标准成本表价格变更失败",e);
                String id = changeDTO.getId();
                SkuStdCostDetailEntity entity = idEntityMap.get(changeDTO.getId());
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "sku标准成本表价格单不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getMainId(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 修改
    * @author Jim
    * @date:  2025-08-08
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "sku标准成本表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "plm:skuStdCost:update",
        serviceClass = SkuStdCostService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SkuStdCostDetailDTO.UpdateDTO dto) {
        skuStdCostDetailService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:skuStdCost:paging",
            tableAlias = ""
    )
    public ApiResult<List<SkuStdCostDetailDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(skuStdCostDetailService.tabList(dto));
    }

    /**
    * 列表查询
    * @author Jim
    * @date: 2025-08-08
    * @param dto
    * @return ApiResult<PagingVO<SkuStdCostDetailDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:skuStdCost:paging",
            tableAlias = ""
    )
    @WebAdvanceQuery(handler = SkuStdCostDetailQueryHandler.class)
    public ApiResult<PagingVO<SkuStdCostDetailDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SkuStdCostDetailDTO.PagingParamDTO> dto) {
        return success(skuStdCostDetailService.paging(dto));
    }

    /**
    * 修改并提交审核
    * @author Jim
    * @date:  2025-08-08
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:skuStdCost:updateAndSubmit",
            serviceClass = SkuStdCostService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated SkuStdCostDetailDTO.UpdateDTO dto) {
        skuStdCostDetailService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author Jim
    * @date:  2025-08-08
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:skuStdCost:submit",
            serviceClass = SkuStdCostService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "sku标准成本表提交审核")
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		//数据查询放入外层，处理结果统一更新或单条更新
		List<SkuStdCostDetailEntity> list = skuStdCostDetailService.lambdaQuery().in(SkuStdCostDetailEntity::getId, ids).list();
		Map<String, SkuStdCostDetailEntity> idEntityMap = list.stream().collect(Collectors.toMap(SkuStdCostDetailEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = skuStdCostDetailService.submit(id);
            }catch (Exception e){
                log.error("sku标准成本单 提交审核失败",e);
                SkuStdCostDetailEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "sku标准成本单不存在, 提交失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), entity.getId(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 审核
    * @author Jim
    * @date:  2025-08-08
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:skuStdCost:approve",
            serviceClass = SkuStdCostService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "sku标准成本表审核")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// 数据查询放入外层，处理结果统一更新或单条更新
		List<SkuStdCostDetailEntity> list = skuStdCostDetailService.lambdaQuery().in(SkuStdCostDetailEntity::getId, ids).list();
		Map<String, SkuStdCostDetailEntity> idEntityMap = list.stream().collect(Collectors.toMap(SkuStdCostDetailEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = skuStdCostDetailService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("sku标准成本单审核失败",e);
                SkuStdCostDetailEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "sku标准成本单不存在, 审核失败");
                    resultDTOS.add(approveResult);
                    continue;
                }
                approveResult = BatchResultDTO.fail(entity.getId(), entity.getId(), e.getMessage());
            }
            resultDTOS.add(approveResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 反审核
    * @author Jim
    * @date:  2025-08-08
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:skuStdCost:disApprove",
            serviceClass = SkuStdCostService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "sku标准成本表反审核")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// 数据查询放入外层，处理结果统一更新或单条更新
		List<SkuStdCostDetailEntity> list = skuStdCostDetailService.lambdaQuery().in(SkuStdCostDetailEntity::getId, ids).list();
		Map<String, SkuStdCostDetailEntity> idEntityMap = list.stream().collect(Collectors.toMap(SkuStdCostDetailEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = skuStdCostDetailService.disApprove(id);
            }catch (Exception e){
                log.error("sku标准成本单反审核失败",e);
                SkuStdCostDetailEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "sku标准成本单不存在, 反审核失败");
                    resultDTOS.add(disApproveResult);
                    continue;
                }
                disApproveResult = BatchResultDTO.fail(entity.getId(), entity.getId(), e.getMessage());
            }
            resultDTOS.add(disApproveResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
    * 删除
    * @author Jim
    * @date:  2025-08-08
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:skuStdCost:delete",
            serviceClass = SkuStdCostService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "sku标准成本表删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// 数据查询放入外层，处理结果统一更新或单条更新
		List<SkuStdCostDetailEntity> list = skuStdCostDetailService.lambdaQuery().in(SkuStdCostDetailEntity::getId, ids).list();
		Map<String, SkuStdCostDetailEntity> idEntityMap = list.stream().collect(Collectors.toMap(SkuStdCostDetailEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = skuStdCostDetailService.delete(id);
            }catch (Exception e){
                log.error("sku标准成本单删除失败",e);
                SkuStdCostDetailEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "sku标准成本单不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getId(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 撤销
    * @author Jim
    * @date:  2025-08-08
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:skuStdCost:cancelProcess",
            serviceClass = SkuStdCostService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "sku标准成本表撤销")
    public ApiResult<List<BatchResultDTO>> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        // 数据查询放入外层，处理结果统一更新或单条更新
        List<SkuStdCostDetailEntity> list = skuStdCostDetailService.lambdaQuery().in(SkuStdCostDetailEntity::getId, ids).list();
        Map<String, SkuStdCostDetailEntity> idEntityMap = list.stream().collect(Collectors.toMap(SkuStdCostDetailEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = skuStdCostDetailService.cancelProcess(id);
            }catch (Exception e){
                log.error("sku标准成本单撤回流程失败",e);
                SkuStdCostDetailEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "sku标准成本单不存在, 撤回流程失败");
                    resultDTOS.add(cancelResult);
                    continue;
                }
                cancelResult = BatchResultDTO.fail(entity.getId(), entity.getId(), e.getMessage());
            }
            resultDTOS.add(cancelResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 详情
    * @author Jim
    * @date:  2025-08-08
    * @param id
    * @return ApiResult<SkuStdCostDetailDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:skuStdCost:view",
            serviceClass = SkuStdCostService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<SkuStdCostDetailDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(skuStdCostDetailService.view(id));
    }

    /**
    * 导出Excel数据
    * @author Jim
    * @date:  2025-08-08
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:skuStdCost:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "sku标准成本表导出Excel数据")
    @WebAdvanceQuery(handler = SkuStdCostDetailQueryHandler.class)
    public void exportList(@RequestBody @Validated SkuStdCostDetailDTO.ExportDTO dto, HttpServletResponse response) {
        skuStdCostDetailService.exportList(dto, response);
    }

    /**
     * sku标准成本-导入
     * @author Jim
     * @date:  2025-08-08
     * @param excelFile
     * @param response
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "sku标准成本-导入")
    @PostMapping("/importFile")
    public ApiResult<Object> importFile(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        boolean flag = skuStdCostDetailService.importFile(excelFile, response);
        return flag ? this.success() : this.failure();
    }

    /**
     * 下载导入模板
     *
     * @author Jim
     * @date:  2025-08-08
     * @param response
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载导入模板")
    @GetMapping("/downloadTemplate")
    public ApiResult<?> downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/skuStdCostDetailTemplate.xlsx";
        String excelName = "templateSkuStdCostDetail.xlsx";
        ExcelUtil.downloadTemplate(path, excelName, response);
        return success();
    }
}
