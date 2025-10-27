package com.erp.server.scm.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.dto.ContractInfoDTO;
import com.erp.model.scm.entity.ContractInfoEntity;
import com.erp.server.scm.query.ContractInfoQueryHandler;
import com.erp.server.scm.service.CfgSupplierSalesService;
import com.erp.server.scm.service.ContractInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 合同管理表
 *
 * @author will
 * @since 2025-06-16
 */
@Slf4j
@RestController
@LogSystemModule("合同管理表")
@RequestMapping("/contractInfo")
public class ContractInfoController extends BaseController {

    @Resource
    private ContractInfoService contractInfoService;

    /**
    * 新增
    * @author will
    * @date:  2025-06-16
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "合同管理表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated ContractInfoDTO.AddDTO dto) {
        return success(contractInfoService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2025-06-16
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "合同管理表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "scm:contractInfo:update",
        serviceClass = ContractInfoService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated ContractInfoDTO.UpdateDTO dto) {
        contractInfoService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:contractInfo:paging",
            tableAlias = "ci"
    )
    public ApiResult<List<ContractInfoDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(contractInfoService.tabList(dto));
    }

    /**
    * 列表查询
    * @author will
    * @date: 2025-06-16
    * @param dto
    * @return ApiResult<PagingVO<ContractInfoDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:contractInfo:paging",
            tableAlias = "ci"
    )
    @WebAdvanceQuery(handler = ContractInfoQueryHandler.class)
    public ApiResult<PagingVO<ContractInfoDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<ContractInfoDTO.PagingParamDTO> dto) {
        return success(contractInfoService.paging(dto));
    }

    /**
    * 提交审核
    * @author will
    * @date:  2025-06-16
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:contractInfo:submit",
            serviceClass = ContractInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "合同管理表提交审核")
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<ContractInfoEntity> list = contractInfoService.lambdaQuery().in(ContractInfoEntity::getId, ids).list();
		Map<String, ContractInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(ContractInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = contractInfoService.submit(id);
            }catch (Exception e){
                log.error("合同管理单 提交审核失败",e);
                ContractInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "合同管理单不存在, 提交失败");
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
    * @author will
    * @date:  2025-06-16
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:contractInfo:approve",
            serviceClass = ContractInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "合同管理表审核")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<ContractInfoEntity> list = contractInfoService.lambdaQuery().in(ContractInfoEntity::getId, ids).list();
		Map<String, ContractInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(ContractInfoEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = contractInfoService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("合同管理单审核失败",e);
                ContractInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "合同管理单不存在, 审核失败");
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
    * @author will
    * @date:  2025-06-16
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:contractInfo:disApprove",
            serviceClass = ContractInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "合同管理表反审核")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<ContractInfoEntity> list = contractInfoService.lambdaQuery().in(ContractInfoEntity::getId, ids).list();
		Map<String, ContractInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(ContractInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = contractInfoService.disApprove(id);
            }catch (Exception e){
                log.error("合同管理单反审核失败",e);
                ContractInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "合同管理单不存在, 反审核失败");
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
    * @author will
    * @date:  2025-06-16
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:contractInfo:delete",
            serviceClass = ContractInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "合同管理表删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<ContractInfoEntity> list = contractInfoService.lambdaQuery().in(ContractInfoEntity::getId, ids).list();
		Map<String, ContractInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(ContractInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = contractInfoService.delete(id);
            }catch (Exception e){
                log.error("合同管理单删除失败",e);
                ContractInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "合同管理单不存在, 删除失败");
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
    * @author will
    * @date:  2025-06-16
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:contractInfo:cancelProcess",
            serviceClass = ContractInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "合同管理表撤销")
    public ApiResult<List<BatchResultDTO>> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<ContractInfoEntity> list = contractInfoService.lambdaQuery().in(ContractInfoEntity::getId, ids).list();
        Map<String, ContractInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(ContractInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = contractInfoService.cancelProcess(new ApproveDTO.CancelProcessDTO(id));
            }catch (Exception e){
                log.error("合同管理单撤回流程失败",e);
                ContractInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "合同管理单不存在, 撤回流程失败");
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
    * @author will
    * @date:  2025-06-16
    * @param id
    * @return ApiResult<ContractInfoDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:contractInfo:view",
            serviceClass = ContractInfoService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<ContractInfoDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(contractInfoService.view(id));
    }

    /**
    * 导出Excel数据
    * @author will
    * @date:  2025-06-16
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:contractInfo:export",
            tableAlias = "ci"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "合同管理表导出Excel数据")
    @WebAdvanceQuery(handler = ContractInfoQueryHandler.class)
    public ApiResult<Object> exportList(@RequestBody @Validated ContractInfoDTO.PagingParamDTO dto, HttpServletResponse response) {
        contractInfoService.exportList(dto, response);
        return success();
    }


    /**
     * 启用/停用
     * @author jack
     * @date:  2025-06-13
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/enable")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:contractInfo:enable",
            serviceClass = CfgSupplierSalesService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.UPDATE, desc = "合同管理启用/停用")
    public ApiResult<List<BatchResultDTO>> enable(@RequestBody @Validated  ContractInfoDTO.EnableStatusDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<ContractInfoEntity> list = contractInfoService.lambdaQuery().in(ContractInfoEntity::getId, ids).list();
        Map<String, ContractInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(ContractInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = contractInfoService.enable(id,dto.getDisabled());
            }catch (Exception e){
                log.error("合同管理更新失败",e);
                ContractInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "合同管理不存在, 更新失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = BatchResultDTO.fail(entity.getId(),  entity.getCode(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 导出附件
     * @author jack
     * @date:  2025-06-13
     * @param dto
     * @return StreamingResponseBody
     */
    @PostMapping("/exportZip")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:contractInfo:exportZip",
            tableAlias = "ci"
    )
    @WebAdvanceQuery(handler = ContractInfoQueryHandler.class)
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出附件")
    public ResponseEntity<StreamingResponseBody> exportZip(@RequestBody @Validated ContractInfoDTO.PagingParamDTO dto) {
        ExportZipResultDTO resultDTO = contractInfoService.exportZip(dto);
        // 编码文件名（兼容所有Java版本）
        String encodedFileName;
        try {
            encodedFileName = URLEncoder.encode(resultDTO.getFileName(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ServiceException("编码失败");
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename*=UTF-8''" + encodedFileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resultDTO.getResponseBody());
    }
}
