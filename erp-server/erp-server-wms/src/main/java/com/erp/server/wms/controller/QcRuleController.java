package com.erp.server.wms.controller;


import com.common.business.dto.base.*;
import com.common.business.vo.PagingVO;
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
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated QcRuleDTO.AddDTO dto) {
        String id = qcRuleService.add(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 添加并提交
     *
     * @param
     * @return
     */
    @PostMapping("/addAndSubmit")
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
    @PostMapping("/submit")
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
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated QcRuleDTO.UpdateDTO dto) {
        String id = qcRuleService.updateQcRule(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 修改并提交
     *
     * @param dto
     * @return
     */
    @PostMapping("/updateAndSubmit")
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
    @PostMapping("/approve")
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
    @PostMapping("/disApprove")
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
    @PostMapping("/cancelProcess")
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
    @PostMapping("/delete")
    public ApiResult delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean result = qcRuleService.deleteByIds(dto.getIds());
        return result == true ? success() : failure();
    }


    /**
     * 启用或者禁用
     *
     * @param dto
     * @return
     */
    @PostMapping("/updateStatus")
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
