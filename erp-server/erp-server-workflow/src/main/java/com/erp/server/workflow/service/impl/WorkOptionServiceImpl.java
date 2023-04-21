package com.erp.server.workflow.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.LoginUser;
import com.erp.model.workflow.dto.WorkOptionDTO;
import com.erp.model.workflow.entity.WorkOptionEntity;
import com.erp.model.workflow.enums.ApproveSearchOptionEnum;
import com.erp.model.workflow.enums.ModelTypeEnum;
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
     * 待办模块-模块分类下拉
     * @Author Luo_WG
     * @Date 2023/4/21 10:49
     * @param sysClassify sysClassify
     * @return java.util.List<com.erp.model.workflow.dto.WorkOptionDTO.WaitDoMenu>
     **/
    @Override
    public List<WorkOptionDTO.WaitDoMenu> listWaitDoMenu(String sysClassify) {
        List<WorkOptionDTO.WaitDoMenu> waitDoMenus = baseMapper.listWaitDoMenu(sysClassify);
        waitDoMenus.forEach(req -> {
            req.setName(req.getModuleClassify() + "-" + req.getModuleStatusName());
        });
        return waitDoMenus;
    }

    /**
     * 常用模块-模块分类下拉
     * @Author Luo_WG
     * @Date 2023/4/21 10:50
     * @param sysClassify sysClassify
     * @return java.util.List<com.erp.model.workflow.dto.WorkOptionDTO.WaitDoMenu>
     **/
    @Override
    public List<WorkOptionDTO.WaitDoMenu> listOftenMenu(String sysClassify) {
        List<WorkOptionDTO.WaitDoMenu> waitDoMenus = baseMapper.listOftenMenu(sysClassify);
        waitDoMenus.forEach(req -> {
            req.setName(req.getModuleClassify());
        });
        return waitDoMenus;
    }

    /**
     * 新增模块
     * @Author Luo_WG
     * @Date 2023/4/20 19:45
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @Override
    public Boolean addWaitDo(WorkOptionDTO.addDTO dto) {
        LoginUser userInfo = commonService.getUserInfo();
        WorkOptionEntity workOptionEntity = new WorkOptionEntity();
        workOptionEntity.setOptionUserId(userInfo.getUid());
        workOptionEntity.setOptionUserMame(userInfo.getUserName());
        workOptionEntity.setWorkMenuId(dto.getWorkMenuId());
        workOptionEntity.setModuleUrl(dto.getModuleUrl());
        workOptionEntity.setModuleParam(dto.getModuleParam());
        workOptionEntity.setType(dto.getType());
        return this.save(workOptionEntity);
    }

    /**
     * 编辑修改模块
     * @Author Luo_WG
     * @Date 2023/4/20 19:45
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @Override
    public Boolean updateWaitDo(WorkOptionDTO.updateDTO dto) {
        LoginUser userInfo = commonService.getUserInfo();
        WorkOptionEntity workOptionEntity = new WorkOptionEntity();
        workOptionEntity.setOptionUserId(userInfo.getUid());
        workOptionEntity.setOptionUserMame(userInfo.getUserName());
        workOptionEntity.setWorkMenuId(dto.getWorkMenuId());
        workOptionEntity.setModuleUrl(dto.getModuleUrl());
        workOptionEntity.setModuleParam(dto.getModuleParam());
        workOptionEntity.setId(dto.getId());
        return this.updateById(workOptionEntity);
    }

    /**
     * 代办列表
     * @Author Luo_WG
     * @Date 2023/4/11 18:48
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.PurchaseReturnOrderDTO.PagingViewDTO>>
     **/
    @Override
    public List<WorkOptionDTO.PendingViewDTO> pendingView() {
        LoginUser userInfo = commonService.getUserInfo();
        List<WorkOptionEntity> list = lambdaQuery().eq(WorkOptionEntity::getOptionUserId, userInfo.getUid()).list();

        return null;
    }


    /**
     * 审批中心-下拉搜索选项
     * @Author Luo_WG
     * @Date 2023/4/12 11:58
     * @return java.util.List<com.erp.model.workflow.dto.WorkOptionDTO.ApproveSearchOptionDTO>
     **/
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
