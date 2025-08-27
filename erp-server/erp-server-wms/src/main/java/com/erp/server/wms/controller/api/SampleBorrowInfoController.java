package com.erp.server.wms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.validator.ValidList;
import com.common.core.utils.ExcelUtil;
import com.erp.model.wms.entity.SampleScrapInfoEntity;
import com.erp.server.wms.query.SampleBorrowInfoQueryHandler;
import com.erp.server.wms.query.SampleScrapInfoQueryHandler;
import com.erp.server.wms.service.SampleScrapInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.erp.server.wms.service.SampleBorrowInfoService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.SampleBorrowInfoDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.wms.entity.SampleBorrowInfoEntity;

/**
 * 样品借用单
 *
 * @author jack
 * @since 2025-08-20
 */
@Slf4j
@RestController
@LogSystemModule("样品借用单")
@RequestMapping("/sampleBorrowInfo")
public class SampleBorrowInfoController extends BaseController {

    @Resource
    private SampleBorrowInfoService sampleBorrowInfoService;

    /**
    * 新增
    * @author jack
    * @date:  2025-08-20
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "样品借用单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SampleBorrowInfoDTO.AddDTO dto) {
        return success(sampleBorrowInfoService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-08-20
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "样品借用单修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:sampleBorrowInfo:update",
        serviceClass = SampleBorrowInfoService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SampleBorrowInfoDTO.UpdateDTO dto) {
        sampleBorrowInfoService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:sampleBorrowInfo:paging",
            tableAlias = "sbi"
    )
    public ApiResult<List<SampleBorrowInfoDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(sampleBorrowInfoService.tabList(dto));
    }

    /**
    * 列表查询
    * @author jack
    * @date: 2025-08-20
    * @param dto
    * @return ApiResult<PagingVO<SampleBorrowInfoDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:sampleBorrowInfo:paging",
            tableAlias = "sbi"
    )
    @WebAdvanceQuery(handler = SampleBorrowInfoQueryHandler.class)
    public ApiResult<PagingVO<SampleBorrowInfoDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SampleBorrowInfoDTO.PagingParamDTO> dto) {
        return success(sampleBorrowInfoService.paging(dto));
    }

    /**z
    * 新增并提交审核
    * @author jack
    * @date:  2025-08-20
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated SampleBorrowInfoDTO.AddDTO dto) {
        BaseResultDTO.AddDTO result = sampleBorrowInfoService.addAndSubmit(dto);
        return success(result);
    }

    /**
    * 修改并提交审核
    * @author jack
    * @date:  2025-08-20
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleBorrowInfo:updateAndSubmit",
            serviceClass = SampleBorrowInfoService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated SampleBorrowInfoDTO.UpdateDTO dto) {
        sampleBorrowInfoService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author jack
    * @date:  2025-08-20
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleBorrowInfo:submit",
            serviceClass = SampleBorrowInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "样品借用单提交审核")
    public ApiResult<List<BatchResultDTO>> batchSubmit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<SampleBorrowInfoEntity> list = sampleBorrowInfoService.lambdaQuery().in(SampleBorrowInfoEntity::getId, ids).list();
		Map<String, SampleBorrowInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleBorrowInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = sampleBorrowInfoService.submit(id);
            }catch (Exception e){
                log.error("样品借用单 提交审核失败",e);
                SampleBorrowInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "样品借用单不存在, 提交失败");
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
    * @date:  2025-08-20
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleBorrowInfo:approve",
            serviceClass = SampleBorrowInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "样品借用单审核")
    public ApiResult<List<BatchResultDTO>> batchApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<SampleBorrowInfoEntity> list = sampleBorrowInfoService.lambdaQuery().in(SampleBorrowInfoEntity::getId, ids).list();
		Map<String, SampleBorrowInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleBorrowInfoEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = sampleBorrowInfoService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("样品借用单审核失败",e);
                SampleBorrowInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "样品借用单不存在, 审核失败");
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
    * @date:  2025-08-20
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleBorrowInfo:disApprove",
            serviceClass = SampleBorrowInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "样品借用单反审核")
    public ApiResult<List<BatchResultDTO>> batchDisApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<SampleBorrowInfoEntity> list = sampleBorrowInfoService.lambdaQuery().in(SampleBorrowInfoEntity::getId, ids).list();
		Map<String, SampleBorrowInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleBorrowInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = sampleBorrowInfoService.disApprove(id);
            }catch (Exception e){
                log.error("样品借用单反审核失败",e);
                SampleBorrowInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "样品借用单不存在, 反审核失败");
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
    * @date:  2025-08-20
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleBorrowInfo:delete",
            serviceClass = SampleBorrowInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "样品借用单删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<SampleBorrowInfoEntity> list = sampleBorrowInfoService.lambdaQuery().in(SampleBorrowInfoEntity::getId, ids).list();
		Map<String, SampleBorrowInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleBorrowInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = sampleBorrowInfoService.delete(id);
            }catch (Exception e){
                log.error("样品借用单删除失败",e);
                SampleBorrowInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "样品借用单不存在, 删除失败");
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
     * @date:  2025-08-20
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleBorrowInfo:invalid",
            serviceClass = SampleScrapInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "样品借用单作废")
    public ApiResult<List<BatchResultDTO>> batchInvalid(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<SampleBorrowInfoEntity> list = sampleBorrowInfoService.lambdaQuery().in(SampleBorrowInfoEntity::getId, ids).list();
        Map<String, SampleBorrowInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleBorrowInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = sampleBorrowInfoService.invalid(id);
            }catch (Exception e){
                log.error("样品借用单作废失败",e);
                SampleBorrowInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "样品借用单不存在, 作废失败");
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
    * @date:  2025-08-20
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:sampleBorrowInfo:cancelProcess",
            serviceClass = SampleBorrowInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "样品借用单撤销")
    public ApiResult<List<BatchResultDTO>> batchCancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<SampleBorrowInfoEntity> list = sampleBorrowInfoService.lambdaQuery().in(SampleBorrowInfoEntity::getId, ids).list();
        Map<String, SampleBorrowInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(SampleBorrowInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = sampleBorrowInfoService.cancelProcess(id);
            }catch (Exception e){
                log.error("样品借用单撤回流程失败",e);
                SampleBorrowInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "样品借用单不存在, 撤回流程失败");
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
    * @date:  2025-08-20
    * @param id
    * @return ApiResult<SampleBorrowInfoDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<SampleBorrowInfoDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(sampleBorrowInfoService.view(id));
    }

    /**
    * 导出Excel数据
    * @author jack
    * @date:  2025-08-20
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:sampleBorrowInfo:export",
            tableAlias = "sbi"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "样品借用单导出Excel数据")
    @WebAdvanceQuery(handler = SampleBorrowInfoQueryHandler.class)
    public  ApiResult<Object> exportList(@RequestBody @Validated SampleBorrowInfoDTO.PagingParamDTO dto, HttpServletResponse response) {
        sampleBorrowInfoService.exportList(dto, response);
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
        Boolean result = sampleBorrowInfoService.importFile(dto);
        return result ? success() : failure();
    }

    /**
     * 下载模板
     * @author jack
     * @date:  2025-08-20
     * @param response
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "样品借用单下载模板")
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        String standardPath = "classpath:excel/sampleBorrowInfoTemplate.xlsx";
        String standardExcelName = "sampleBorrowInfoTemplate.xlsx";
        ExcelUtil.downloadTemplate(standardPath, standardExcelName, response);
        return success();
    }


    /**
     * 生成样品归还视图信息
     * @author jack
     * @date:  2025-08-27
     * @param dto 包含ID列表的请求参数对象，用于指定需要生成归还视图的样品借阅记录
     * @return 返回包含样品归还视图信息的API结果对象，数据为SampleReturnView列表
     */
    @PostMapping("/generateSampleReturnView")
    public ApiResult<List<SampleBorrowInfoDTO.SampleReturnView>> generateSampleReturnView(@RequestBody @Validated BaseIdsDTO.DetailIdListDTO dto){
        return success(sampleBorrowInfoService.generateSampleReturnView(dto.getDetailIdList()));
    }


}
