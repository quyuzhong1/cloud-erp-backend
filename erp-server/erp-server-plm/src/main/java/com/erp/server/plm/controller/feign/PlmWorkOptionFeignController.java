package com.erp.server.plm.controller.feign;

import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
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
    @PostMapping("/bomInfoApprove")
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
    public Boolean productDetailApprove(@RequestBody @Validated ApproveOneDTO dto) {
        BatchResultDTO resultDTO = productDetailService.approve(dto,Boolean.TRUE);
        return resultDTO.getSuccess();
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
    @PostMapping("/productChangeApprove")
    public void productChangeApprove(@RequestBody @Validated ApproveOneDTO dto) {
        productChangeService.approve(dto);
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
        pilotApplicationService.approve(dto, approveDTO);
        pilotApplicationService.approvePilotApplicationNotice(dto.getId());
    }
}