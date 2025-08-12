package com.erp.server.wms.controller.api;


import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.QcReportDTO;
import com.erp.model.wms.dto.QcRuleDTO;
import com.erp.server.wms.service.QcReportService;
import com.erp.server.wms.service.QcRuleService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 质检规则
 *
 * @author lambda
 * @since 2023-04-13
 */
@RestController
@LogSystemModule("质检规则")
@RequestMapping("qcRule")
public class QcRuleController extends BaseController {

    @Resource
    private QcRuleService qcRuleService;

    @Resource
    private QcReportService qcReportService;


    /**
     * 分页
     *
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:qcRule:paging",
            tableAlias = "qc_rule")
    @WebAdvanceQuery
    public ApiResult<PagingVO<QcRuleDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<QcRuleDTO.PagingParamDTO> dto) {
        PagingVO<QcRuleDTO.PagingViewDTO> pagingVO = qcRuleService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 添加
     *
     * @param
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "新增质检规则")
    @PostMapping("/add")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcRule:add",
            serviceClass = QcRuleService.class,
            keyIdName = "id")
    public ApiResult add(@RequestBody @Validated QcRuleDTO.AddDTO dto) {
        String id = qcRuleService.add(dto);
        return CharSequenceUtil.isNotBlank(id) ? success() : failure();
    }

    /**
     * 添加并提交
     *
     * @param
     * @return
     */
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交质检规则")
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcRule:add",
            serviceClass = QcRuleService.class,
            keyIdName = "id")
    public ApiResult addAndSubmit(@RequestBody @Validated QcRuleDTO.AddDTO dto) {
        Boolean result = qcRuleService.addAndSubmit(dto);
        return result ? success() : failure();
    }

    /**
     * 提交审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交质检规则")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcRule:submit",
            serviceClass = QcRuleService.class,
            keyIdName = "ids")
    public ApiResult submit(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean result = qcRuleService.submit(dto.getIds());
        return result == true ? success() : failure();
    }

    /**
     * 详情
     *
     * @param
     * @return
     */
    @LogViewService
    @PostMapping("/view")
    public ApiResult<QcRuleDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        QcRuleDTO.ViewDTO view = qcRuleService.view(dto.getId());
        return success(view);
    }

    /**
     * 修改
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改质检规则")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcRule:update",
            serviceClass = QcRuleService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated QcRuleDTO.UpdateDTO dto) {
        String id = qcRuleService.updateQcRule(dto);
        return CharSequenceUtil.isNotBlank(id) ? success() : failure();
    }

    /**
     * 修改并提交
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交质检规则")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcRule:update",
            serviceClass = QcRuleService.class,
            keyIdName = "id")
    public ApiResult updateAndSubmit(@RequestBody @Validated QcRuleDTO.UpdateDTO dto) {
        Boolean result = qcRuleService.updateAndSubmit(dto);
        return result ? success() : failure();
    }

    /**
     * 审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核质检规则")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcRule:approve",
            serviceClass = QcRuleService.class,
            keyIdName = "ids")
    public ApiResult audit(@RequestBody @Validated BaseApproveParamDTO dto) {
        Boolean result = qcRuleService.approve(dto);
        return result == true ? success() : failure();
    }

    /**
     * 反审核
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     * @author yl
     * @date 2023-03-22 11:56
     */
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核质检规则")
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcRule:disApprove",
            serviceClass = QcRuleService.class,
            keyIdName = "ids")
    public ApiResult disApprove(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean flag = qcRuleService.disApprove(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 撤销流程
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     * @author yl
     * @date 2023-03-22 11:56
     */
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销质检规则")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcRule:cancelProcess",
            serviceClass = QcRuleService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean flag = qcRuleService.cancelProcess(dto.getIds());
        return flag == true ? success() : failure();
    }


    /**
     * 删除
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除质检规则")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcRule:delete",
            serviceClass = QcRuleService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>>delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOList = qcRuleService.deleteByIds(dto.getIds());
        return resultDTOList.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOList) : failure(resultDTOList);
    }


    /**
     * 启用或者禁用
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "启用或禁用质检规则:id={id},状态值={state}(true=禁用,false=启用)")
    @PostMapping("/updateStatus")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcRule:updateStatus",
            serviceClass = QcRuleService.class,
            keyIdName = "id")
    public ApiResult updateStatus(@RequestBody @Validated UpdateStateDTO dto) {
        Boolean result = qcRuleService.updateDisabledState(dto);
        return result == true ? success() : failure();
    }

    /**
     * 根据质检类型获取到对应的 质检报告信息
     *
     * @param qcType
     * @return
     */
    @GetMapping("/getByQcType")
    public ApiResult<List<QcReportDTO.ListDTO>> getByQcType(@RequestParam("qcType") String qcType) {
        List<QcReportDTO.ListDTO> list = qcReportService.getByQcType(qcType);
        return success(list);
    }


}
