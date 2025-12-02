package com.erp.server.oms.controller.api;


import com.erp.model.wms.dto.SampleBorrowInfoDTO;
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
import com.erp.server.oms.service.KolPartnerInfoService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.KolPartnerInfoDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.oms.entity.KolPartnerInfoEntity;

/**
 * 企业达人库
 *
 * @author jack
 * @since 2025-12-02
 */
@Slf4j
@RestController
@LogSystemModule("企业达人库")
@RequestMapping("/kolPartnerInfo")
public class KolPartnerInfoController extends BaseController {

    @Resource
    private KolPartnerInfoService kolPartnerInfoService;

    /**
    * 新增
    * @author jack
    * @date:  2025-12-02
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "企业达人库新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated KolPartnerInfoDTO.AddDTO dto) {
        return success(kolPartnerInfoService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-12-02
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "企业达人库修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:kolPartnerInfo:update",
        serviceClass = KolPartnerInfoService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated KolPartnerInfoDTO.UpdateDTO dto) {
        kolPartnerInfoService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:kolPartnerInfo:paging",
            tableAlias = ""
    )
    public ApiResult<List<KolPartnerInfoDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(kolPartnerInfoService.tabList(dto));
    }

    /**
    * 列表查询
    * @author jack
    * @date: 2025-12-02
    * @param dto
    * @return ApiResult<PagingVO<KolPartnerInfoDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:kolPartnerInfo:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<KolPartnerInfoDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<KolPartnerInfoDTO.PagingParamDTO> dto) {
        return success(kolPartnerInfoService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author jack
    * @date:  2025-12-02
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated KolPartnerInfoDTO.AddDTO dto) {
        BaseResultDTO.AddDTO result = kolPartnerInfoService.addAndSubmit(dto);
        return success(result);
    }

    /**
    * 修改并提交审核
    * @author jack
    * @date:  2025-12-02
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:kolPartnerInfo:updateAndSubmit",
            serviceClass = KolPartnerInfoService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated KolPartnerInfoDTO.UpdateDTO dto) {
        kolPartnerInfoService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author jack
    * @date:  2025-12-02
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:kolPartnerInfo:submit",
            serviceClass = KolPartnerInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "企业达人库提交审核")
    public ApiResult<List<BatchResultDTO>> batchSubmit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<KolPartnerInfoEntity> list = kolPartnerInfoService.lambdaQuery().in(KolPartnerInfoEntity::getId, ids).list();
		Map<String, KolPartnerInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(KolPartnerInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = kolPartnerInfoService.submit(id);
            }catch (Exception e){
                log.error("企业达人库 提交审核失败",e);
                KolPartnerInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "企业达人库不存在, 提交失败");
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
    * @date:  2025-12-02
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:kolPartnerInfo:approve",
            serviceClass = KolPartnerInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "企业达人库审核")
    public ApiResult<List<BatchResultDTO>> batchApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<KolPartnerInfoEntity> list = kolPartnerInfoService.lambdaQuery().in(KolPartnerInfoEntity::getId, ids).list();
		Map<String, KolPartnerInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(KolPartnerInfoEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = kolPartnerInfoService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("企业达人库审核失败",e);
                KolPartnerInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "企业达人库不存在, 审核失败");
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
    * @date:  2025-12-02
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:kolPartnerInfo:disApprove",
            serviceClass = KolPartnerInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "企业达人库反审核")
    public ApiResult<List<BatchResultDTO>> batchDisApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<KolPartnerInfoEntity> list = kolPartnerInfoService.lambdaQuery().in(KolPartnerInfoEntity::getId, ids).list();
		Map<String, KolPartnerInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(KolPartnerInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = kolPartnerInfoService.disApprove(id);
            }catch (Exception e){
                log.error("企业达人库反审核失败",e);
                KolPartnerInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "企业达人库不存在, 反审核失败");
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
    * @date:  2025-12-02
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:kolPartnerInfo:delete",
            serviceClass = KolPartnerInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "企业达人库删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<KolPartnerInfoEntity> list = kolPartnerInfoService.lambdaQuery().in(KolPartnerInfoEntity::getId, ids).list();
		Map<String, KolPartnerInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(KolPartnerInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = kolPartnerInfoService.delete(id);
            }catch (Exception e){
                log.error("企业达人库删除失败",e);
                KolPartnerInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "企业达人库不存在, 删除失败");
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
    * @date:  2025-12-02
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:kolPartnerInfo:cancelProcess",
            serviceClass = KolPartnerInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "企业达人库撤销")
    public ApiResult<List<BatchResultDTO>> batchCancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        // TODO 数据查询放入外层，处理结果统一更新或单条更新
        List<KolPartnerInfoEntity> list = kolPartnerInfoService.lambdaQuery().in(KolPartnerInfoEntity::getId, ids).list();
        Map<String, KolPartnerInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(KolPartnerInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = kolPartnerInfoService.cancelProcess(id);
            }catch (Exception e){
                log.error("企业达人库撤回流程失败",e);
                KolPartnerInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "企业达人库不存在, 撤回流程失败");
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
    * @date:  2025-12-02
    * @param id
    * @return ApiResult<KolPartnerInfoDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:kolPartnerInfo:view",
            serviceClass = KolPartnerInfoService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<KolPartnerInfoDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(kolPartnerInfoService.view(id));
    }

    /**
    * 导出Excel数据
    * @author jack
    * @date:  2025-12-02
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:kolPartnerInfo:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "企业达人库导出Excel数据")
    public void exportList(@RequestBody @Validated KolPartnerInfoDTO.ExportDTO dto, HttpServletResponse response) {
        kolPartnerInfoService.exportList(dto, response);
    }



    /**
     * 达人下拉接口 (disabled = ture的需要置灰不能选择)
     * @author jack
     * @date:  2025-12-02
     * @param dto
     * @return
     */
    @PostMapping("/drop/down")
    public ApiResult<List<KolPartnerInfoDTO.DropDownDTO>> dropDown(@RequestBody KolPartnerInfoDTO.SelectDTO dto) {
        return success(kolPartnerInfoService.dropDown(dto));
    }


}
