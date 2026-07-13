package com.erp.server.plm.controller.feign;

import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.anno.LogAction;
import com.common.core.enums.LogActionEnum;
import com.erp.model.plm.dto.PilotApplicationDTO;
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
    private BomChangeService bomChangeService;

    @Resource
    private PilotApplicationService pilotApplicationService;

    @Resource
    private MoldInfoService moldInfoService;
    @Resource
    private MoldRefSkuService moldRefSkuService;

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
    @PostMapping("/bomInfoApprove")
    @LogAction(value = LogActionEnum.APPROVE, desc = "BOM审核通过")
    public Boolean bomInfoApprove(@RequestBody @Validated ApproveOneDTO dto) {
        BatchResultDTO resultDTO =  bomInfoService.approve(dto);
        return resultDTO.getSuccess();
    }

    /**
     * 产品信息-状态操作-审核通过
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/productDetailApprove")
    @LogAction(value = LogActionEnum.APPROVE, desc = "产品信息审核通过")
    public Boolean productDetailApprove(@RequestBody @Validated ApproveOneDTO dto) {
        BatchResultDTO resultDTO = productDetailService.approve(dto,Boolean.TRUE);
        return resultDTO.getSuccess();
    }

    /**
     * 项目任务-任务分页列表 -状态操作-审核通过
     * @return
     */
    @PostMapping("/projectTaskApprovalPass")
    @LogAction(value = LogActionEnum.APPROVE, desc = "项目任务审核通过")
    public Boolean projectTaskApprovalPass(@RequestBody @Validated TaskOperateDTO dto) {
        Boolean result = taskService.approvalPass(dto);
        return result;
    }

    /**
     * 项目任务-任务分页列表 -状态操作-审核不通过
     * @return
     */
    @PostMapping("/projectTaskApprovalNoPass")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "项目任务审核不通过")
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
    @PostMapping("/bomChangeApprove")
    @LogAction(value = LogActionEnum.APPROVE, desc = "BOM变更审核通过")
    public void bomChangeApprove(@RequestBody @Validated ApproveOneDTO dto) {
        bomChangeService.approve(dto);
    }

    /**
     * 试产量产  审核 通过
     *
     * @param
     * @return 新增结果
     */
    @PostMapping("/pilotApprovalPass")
    @LogAction(value = LogActionEnum.APPROVE, desc = "试产量产审核通过")
    public void pilotApprovalPass(@RequestBody @Validated ApproveOneDTO dto) {
        PilotApplicationDTO.ApproveDTO approveDTO = new PilotApplicationDTO.ApproveDTO();
        pilotApplicationService.approve(dto, approveDTO);
        pilotApplicationService.approvePilotApplicationNotice(dto.getId());
    }

    /**
     * 模具档案审核
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/moldInfoApprove")
    @LogAction(value = LogActionEnum.APPROVE, desc = "模具档案审核通过")
    public Boolean moldInfoApprove(@RequestBody @Validated ApproveOneDTO dto) {
        BatchResultDTO resultDTO = moldInfoService.approve(dto);
        return resultDTO.getSuccess();
    }
    /**
     * 模具档案审核
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/moldRefSkuApprove")
    @LogAction(value = LogActionEnum.APPROVE, desc = "模具关联SKU审核通过")
    public Boolean moldRefSkuApprove(@RequestBody @Validated ApproveOneDTO dto) {
        BatchResultDTO resultDTO = moldRefSkuService.approve(dto);
        return resultDTO.getSuccess();
    }
}
