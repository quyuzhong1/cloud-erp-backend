package com.erp.server.plm.controller.feign;

import com.common.business.dto.base.ApproveOneDTO;
import com.erp.model.plm.dto.AuditParamDTO;
import com.erp.model.plm.dto.PilotApplicationDTO;
import com.erp.model.plm.dto.ProductDetailOperateDTO;
import com.erp.model.plm.dto.TaskOperateDTO;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.workflow.dto.WorkOptionDTO;
import com.erp.server.plm.service.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("feign/plmWorkOption")
public class PlmWorkOptionFeignController {
    @Resource
    private WorkOptionService workOptionService;

    @Resource
    private BomInfoService bomInfoService;

    @Resource
    private ProductDetailService productDetailService;

    @Resource
    private ProjectTaskService taskService;

    @Resource
    private ProductChangeService productChangeService;

    @Resource
    private PilotApplicationService pilotApplicationService;

    /**
     * 根据入参查询单据数量
     * @Author Luo_WG
     * @Date 2023/4/21 15:34
     **/
    @PostMapping("/getTableNum")
    public List<WorkOptionDTO.MyWorkOptionDTO> getTableNum(@RequestBody List<WorkOptionDTO.MyWorkOptionDTO> myWorkOptionDTOList) {
        return workOptionService.getTableNum(myWorkOptionDTOList);
    }

    /**
     * bom  审核 通过
     * @param
     * @return 新增结果
     */
    @PostMapping("/bomInfoApprovalPass")
    public void bomInfoApprovalPass(@RequestBody @Validated AuditParamDTO dto) {
        bomInfoService.approvalPass(dto);
    }

    /**
     * bom  审核 不通过
     * @param
     * @return 新增结果
     */
    @PostMapping("/bomInfoApprovalNoPass")
    public void bomInfoApprovalNoPass(@RequestBody @Validated AuditParamDTO dto) {
        bomInfoService.approvalNoPass(dto);
    }

    /**
     * 产品信息-状态操作-审核通过
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/productDetailApprovalPass")
    public Boolean productDetailApprovalPass(@RequestBody @Validated ProductDetailOperateDTO dto) {
        Boolean result = productDetailService.approvalPass(dto,Boolean.TRUE);
        return result;
    }

    /**
     * 产品信息-状态操作-审核不通过
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/productDetailApprovalNoPass")
    public Boolean productDetailApprovalNoPass(@RequestBody @Validated ProductDetailOperateDTO dto) {
        Boolean result = productDetailService.approvalReject(dto);
        return result;
    }

    /**
     * 项目任务-任务分页列表 -状态操作-审核通过
     * @return
     */
    @PostMapping("/projectTaskApprovalPass")
    public Boolean projectTaskApprovalPass(@RequestBody @Validated TaskOperateDTO dto) {
        Boolean result = taskService.approvalPass(dto);
        return result;
    }

    /**
     * 项目任务-任务分页列表 -状态操作-审核不通过
     * @return
     */
    @PostMapping("/projectTaskApprovalNoPass")
    public Boolean projectTaskApprovalNoPass(@RequestBody @Validated TaskOperateDTO dto) {
        Boolean result = taskService.approvalReject(dto);
        return result;
    }

    /**
     * 根据任务id获取产品id
     * @return
     */
    @PostMapping("/getProductIdByTaskId")
    public ProjectTaskEntity getProductIdByTaskId(@RequestBody String taskId) {
        ProjectTaskEntity entity = taskService.getById(taskId);
        return entity;
    }

    /**
     * change 审核 通过
     *
     * @param
     * @return 新增结果
     */
    @PostMapping("/productChangeApprovalPass")
    public void productChangeApprovalPass(@RequestBody @Validated AuditParamDTO dto) {
        productChangeService.approvalPass(dto);
    }

    /**
     * change  审核 不通过
     *
     * @param
     * @return 新增结果
     */
    @PostMapping("/productChangeApprovalNoPass")
    public void productChangeApprovalNoPass(@RequestBody @Validated AuditParamDTO dto) {
        productChangeService.approvalNoPass(dto);
    }

    /**
     * 试产量产  审核 通过
     *
     * @param
     * @return 新增结果
     */
    @PostMapping("/pilotApprovalPass")
    public void pilotApprovalPass(@RequestBody @Validated ApproveOneDTO dto) {
        PilotApplicationDTO.ApproveDTO approveDTO = new PilotApplicationDTO.ApproveDTO();
        pilotApplicationService.approve(dto,approveDTO);
    }

    /**
     * 试产量产  审核 不通过
     *
     * @param
     * @return 新增结果
     */
    @PostMapping("/pilotApprovalNoPass")
    public void pilotApprovalNoPass(@RequestBody @Validated ApproveOneDTO dto) {
        PilotApplicationDTO.ApproveDTO approveDTO = new PilotApplicationDTO.ApproveDTO();
        pilotApplicationService.approve(dto,approveDTO);
    }
}