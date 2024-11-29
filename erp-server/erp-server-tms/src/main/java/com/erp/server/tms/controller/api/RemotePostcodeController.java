package com.erp.server.tms.controller.api;


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
import com.erp.server.tms.service.RemotePostcodeService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.RemotePostcodeDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.tms.entity.RemotePostcodeEntity;

/**
 * 偏远邮编组
 *
 * @author jack
 * @since 2024-11-29
 */
@Slf4j
@RestController
@LogSystemModule("偏远邮编组")
@RequestMapping("/remotePostcode")
public class RemotePostcodeController extends BaseController {

    @Resource
    private RemotePostcodeService remotePostcodeService;

    /**
    * 新增
    * @author jack
    * @date:  2024-11-29
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "偏远邮编组新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated RemotePostcodeDTO.AddDTO dto) {
        return success(remotePostcodeService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2024-11-29
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "偏远邮编组修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:remotePostcode:update",
        serviceClass = RemotePostcodeService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated RemotePostcodeDTO.UpdateDTO dto) {
        remotePostcodeService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:remotePostcode:paging",
            tableAlias = ""
    )
    public ApiResult<List<RemotePostcodeDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(remotePostcodeService.tabList(dto));
    }

    /**
    * 列表查询
    * @author jack
    * @date: 2024-11-29
    * @param dto
    * @return ApiResult<PagingVO<RemotePostcodeDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:remotePostcode:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<RemotePostcodeDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<RemotePostcodeDTO.PagingParamDTO> dto) {
        return success(remotePostcodeService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author jack
    * @date:  2024-11-29
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated RemotePostcodeDTO.AddDTO dto) {
        BaseResultDTO.AddDTO result = remotePostcodeService.addAndSubmit(dto);
        return success(result);
    }

    /**
    * 修改并提交审核
    * @author jack
    * @date:  2024-11-29
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:remotePostcode:updateAndSubmit",
            serviceClass = RemotePostcodeService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated RemotePostcodeDTO.UpdateDTO dto) {
        remotePostcodeService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author jack
    * @date:  2024-11-29
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:remotePostcode:submit",
            serviceClass = RemotePostcodeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "偏远邮编组提交审核")
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<RemotePostcodeEntity> list = remotePostcodeService.lambdaQuery().in(RemotePostcodeEntity::getId, ids).list();
		Map<String, RemotePostcodeEntity> idEntityMap = list.stream().collect(Collectors.toMap(RemotePostcodeEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = remotePostcodeService.submit(id);
            }catch (Exception e){
                log.error("偏远邮编组 提交审核失败",e);
                RemotePostcodeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "偏远邮编组不存在, 提交失败");
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
    * @date:  2024-11-29
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:remotePostcode:approve",
            serviceClass = RemotePostcodeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "偏远邮编组审核")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<RemotePostcodeEntity> list = remotePostcodeService.lambdaQuery().in(RemotePostcodeEntity::getId, ids).list();
		Map<String, RemotePostcodeEntity> idEntityMap = list.stream().collect(Collectors.toMap(RemotePostcodeEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = remotePostcodeService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("偏远邮编组审核失败",e);
                RemotePostcodeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "偏远邮编组不存在, 审核失败");
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
    * @date:  2024-11-29
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:remotePostcode:disApprove",
            serviceClass = RemotePostcodeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "偏远邮编组反审核")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<RemotePostcodeEntity> list = remotePostcodeService.lambdaQuery().in(RemotePostcodeEntity::getId, ids).list();
		Map<String, RemotePostcodeEntity> idEntityMap = list.stream().collect(Collectors.toMap(RemotePostcodeEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = remotePostcodeService.disApprove(id);
            }catch (Exception e){
                log.error("偏远邮编组反审核失败",e);
                RemotePostcodeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "偏远邮编组不存在, 反审核失败");
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
    * @date:  2024-11-29
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:remotePostcode:delete",
            serviceClass = RemotePostcodeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "偏远邮编组删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<RemotePostcodeEntity> list = remotePostcodeService.lambdaQuery().in(RemotePostcodeEntity::getId, ids).list();
		Map<String, RemotePostcodeEntity> idEntityMap = list.stream().collect(Collectors.toMap(RemotePostcodeEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = remotePostcodeService.delete(id);
            }catch (Exception e){
                log.error("偏远邮编组删除失败",e);
                RemotePostcodeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "偏远邮编组不存在, 删除失败");
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
    * @date:  2024-11-29
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:remotePostcode:cancelProcess",
            serviceClass = RemotePostcodeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "偏远邮编组撤销")
    public ApiResult<List<BatchResultDTO>> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        // TODO 数据查询放入外层，处理结果统一更新或单条更新
        List<RemotePostcodeEntity> list = remotePostcodeService.lambdaQuery().in(RemotePostcodeEntity::getId, ids).list();
        Map<String, RemotePostcodeEntity> idEntityMap = list.stream().collect(Collectors.toMap(RemotePostcodeEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = remotePostcodeService.cancelProcess(id);
            }catch (Exception e){
                log.error("偏远邮编组撤回流程失败",e);
                RemotePostcodeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "偏远邮编组不存在, 撤回流程失败");
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
    * @date:  2024-11-29
    * @param id
    * @return ApiResult<RemotePostcodeDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:remotePostcode:view",
            serviceClass = RemotePostcodeService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<RemotePostcodeDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(remotePostcodeService.view(id));
    }

    /**
    * 导出Excel数据
    * @author jack
    * @date:  2024-11-29
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:remotePostcode:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "偏远邮编组导出Excel数据")
    public void exportList(@RequestBody @Validated RemotePostcodeDTO.ExportDTO dto, HttpServletResponse response) {
        remotePostcodeService.exportList(dto, response);
    }


}
