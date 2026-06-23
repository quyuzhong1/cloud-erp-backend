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
import com.erp.model.scm.dto.SupplierPhaseDTO;
import com.erp.model.scm.entity.SupplierPhaseEntity;
import com.erp.server.scm.query.SupplierPhaseQueryHandler;
import com.erp.server.scm.service.SupplierPhaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 供应商阶段管理
 *
 * @author admin
 * @since 2023-03-15
 */
@Slf4j
@RestController
@LogSystemModule("供应商阶段审核")
@RequestMapping("/supplier/phase")
public class SupplierPhaseController extends BaseController {


    @Resource
    private SupplierPhaseService supplierPhaseService;

    /**
     * tab列表
     *
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:supplier:phase:paging",
            tableAlias = "sp"
    )
    public ApiResult<List<SupplierPhaseDTO.TabFlagDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<SupplierPhaseDTO.TabFlagDTO> tabList = supplierPhaseService.tabList(dto);
        return success(tabList);
    }

    /**
     * 供应商阶段分页列表
     *
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:supplier:phase:paging",
            tableAlias = "sp"
    )
    @WebAdvanceQuery(handler = SupplierPhaseQueryHandler.class)
    public ApiResult<PagingVO<SupplierPhaseDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<SupplierPhaseDTO.PagingParamDTO> dto) {
        PagingVO<SupplierPhaseDTO.PagingViewDTO> pagingVO = supplierPhaseService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 添加供应商阶段
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "添加供应商阶段")
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated SupplierPhaseDTO.AddDTO dto) {
        String id = supplierPhaseService.add(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 修改供应商阶段
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改供应商阶段")
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated SupplierPhaseDTO.UpdateDTO dto) {
        String id = supplierPhaseService.updateSupplierPhase(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 修改并审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并审核供应商阶段")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:supplier:phase:submit",
            serviceClass = SupplierPhaseService.class,
            keyIdName = "id"
    )
    public ApiResult updateAndSubmit(@RequestBody @Validated SupplierPhaseDTO.UpdateDTO dto) {
        Boolean result = supplierPhaseService.updateAndSubmit(dto);
        return result == true ? success() : failure();
    }

    /**
     * 提交并审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交供应商阶段")
    @PostMapping("/addAndSubmit")
    public ApiResult addAndSubmit(@RequestBody @Validated SupplierPhaseDTO.AddDTO dto) {
        Boolean result = supplierPhaseService.addAndSubmit(dto);
        return result == true ? success() : failure();
    }

    /**
     * 供应商阶段提交审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "供应商阶段提交审核")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:supplier:phase:submit",
            serviceClass = SupplierPhaseService.class,
            keyIdName = "ids"
    )
    public ApiResult submit(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = supplierPhaseService.submit(id);
            }catch (Exception e){
                log.error("供应商阶段 提交审核失败",e);
                SupplierPhaseEntity entity = supplierPhaseService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "供应商阶段不存在, 提交失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), entity.getTargetPhase(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 供应商阶段 详情
     *
     * @param dto
     * @return
     */
    @LogViewService
    @PostMapping("/view")
    public ApiResult<SupplierPhaseDTO.UpdateDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        SupplierPhaseDTO.UpdateDTO supplierPhase = supplierPhaseService.view(dto.getId());
        return success(supplierPhase);
    }


    /**
     * 供应商阶段审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "供应商阶段审核")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:supplier:phase:approve",
            serviceClass = SupplierPhaseService.class,
            keyIdName = "ids"
    )
    public ApiResult approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<String> ids = dto.getIds();
        for (String id : ids) {
            BatchResultDTO resultDTO;
            String flagCode = id;
            try {
                SupplierPhaseEntity entity = supplierPhaseService.getById(id);
                if (Objects.isNull(entity)) {
                    resultDTO = BatchResultDTO.fail(id,flagCode, "供应商阶段不存在");
                } else {
                    flagCode = entity.getTargetPhase();
                    resultDTO = supplierPhaseService.approve(entity,new ApproveOneDTO(id, dto.getType(), dto.getComment()));
                }
            } catch (Exception e) {
                log.error("供应商阶段审核失败>>>>{}", e);
                resultDTO = BatchResultDTO.fail(id,flagCode, e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 取消流程
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     * @author yl
     * @date 2023-03-23 17:57
     */
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销供应商阶段")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:supplier:phase:cancelProcess",
            serviceClass = SupplierPhaseService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = supplierPhaseService.cancelProcess(new ApproveDTO.BatchCancelProcessDTO(dto.getIds()));
        return result == true ? success() : failure();
    }


    /**
     * 删除供应商阶段
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除供应商阶段")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:supplier:phase:delete",
            serviceClass = SupplierPhaseService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOList = supplierPhaseService.deleteByIds(dto.getIds());
        return resultDTOList.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOList) : failure(resultDTOList);
    }

    /**
     * 导出供应商阶段审核
     * @author will
     * @date 2025/7/28 10:30
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出供应商阶段审核")
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:supplier:phase:paging",
            tableAlias = "sp"
    )
    @WebAdvanceQuery(handler = SupplierPhaseQueryHandler.class)
    public ApiResult export(@RequestBody @Valid SupplierPhaseDTO.PagingParamDTO dto) {
        supplierPhaseService.export(dto);
        return success();
    }
}
