package com.erp.server.workflow.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.workflow.dto.WorkOptionDTO;
import com.erp.model.workflow.entity.WorkOptionEntity;
import com.erp.model.workflow.enums.ApproveSearchOptionEnum;
import com.erp.model.workflow.vo.MyToDoTaskVO;
import com.erp.server.workflow.mapper.WorkOptionMapper;
import com.erp.server.workflow.service.CommonService;
import com.erp.server.workflow.service.ProcessTaskService;
import com.erp.server.workflow.service.WorkOptionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  工作台选项表服务实现类
 * </p>
 *
 * @author LUO_WG
 * @since 2023-04-11
 */
@Service
public class WorkOptionServiceImpl extends SuperServiceImpl<WorkOptionMapper, WorkOptionEntity> implements WorkOptionService {

    @Resource
    private ProcessTaskService workflowFeign;

    @Resource
    private CommonService commonService;

    /**
     * 审批中心-下拉搜索选项
     * @Author Luo_WG
     * @Date 2023/4/12 11:58
     * @return java.util.List<com.erp.model.workflow.dto.WorkOptionDTO.ApproveSearchOptionDTO>
     **/
    @Override
    public List<WorkOptionDTO.ApproveSearchOptionDTO> approveSearchOption() {
        List<WorkOptionDTO.ApproveSearchOptionDTO> list = new ArrayList<>();
        String userId = commonService.getUserInfo().getUid();
        List<ApproveSearchOptionEnum> all = ApproveSearchOptionEnum.getAll();
        for (ApproveSearchOptionEnum optionEnum : all) {
            WorkOptionDTO.ApproveSearchOptionDTO approveSearchOptionDTO = new WorkOptionDTO.ApproveSearchOptionDTO();
            if (optionEnum.getCode().equals(ApproveSearchOptionEnum.WAITHANDLE.getCode())) {
                approveSearchOptionDTO.setStatus(optionEnum.getCode());
                //获取我的待办信息
                List<MyToDoTaskVO> myToDoTasks = workflowFeign.getMyToDoTasks(userId);
                List<String> collect = myToDoTasks.stream().map(MyToDoTaskVO::getBusinessTableId).collect(Collectors.toList());
                approveSearchOptionDTO.setQuantity(collect.size());

                approveSearchOptionDTO.setModuleList(null);
            }
        }
        return null;
    }
}
