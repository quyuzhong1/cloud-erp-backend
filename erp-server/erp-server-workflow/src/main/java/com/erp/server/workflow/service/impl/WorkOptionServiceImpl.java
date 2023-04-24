package com.erp.server.workflow.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.LoginUser;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.workflow.dto.WorkOptionDTO;
import com.erp.model.workflow.entity.WorkMenuEntity;
import com.erp.model.workflow.entity.WorkOptionEntity;
import com.erp.model.workflow.enums.ApproveSearchOptionEnum;
import com.erp.model.workflow.enums.ModelTypeEnum;
import com.erp.model.workflow.enums.SysClassifyEnum;
import com.erp.model.workflow.vo.MyToDoTaskVO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.workflow.mapper.WorkOptionMapper;
import com.erp.server.workflow.service.CommonService;
import com.erp.server.workflow.service.ProcessTaskService;
import com.erp.server.workflow.service.WorkMenuService;
import com.erp.server.workflow.service.WorkOptionService;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

    @Resource
    private WorkMenuService workMenuService;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

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
    public Boolean addWaitDo(WorkOptionDTO.AddDTO dto) {
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
    public Boolean updateWaitDo(WorkOptionDTO.UpdateDTO dto) {
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
    public List<WorkOptionDTO.PendingViewDTO> listPendingView() {
        List<WorkOptionDTO.PendingViewDTO> list = new ArrayList<>();
        LoginUser userInfo = commonService.getUserInfo();
        List<WorkOptionDTO.MyWorkOptionDTO> myWorkOptionDTOS = baseMapper.listMyWorkOption(userInfo.getUid());
        List<SysClassifyEnum> sysClassifyEnums = SysClassifyEnum.getAll();
        for (SysClassifyEnum searchOptionEnum : sysClassifyEnums) {
            WorkOptionDTO.PendingViewDTO pendingViewDTO = new WorkOptionDTO.PendingViewDTO();
            List<WorkOptionDTO.PendingViewDetailDTO> pendingViewDetailDTOList = new ArrayList<>();
            pendingViewDTO.setSysClassify(searchOptionEnum.getCode());
            List<WorkOptionDTO.MyWorkOptionDTO> myWorkOptionDTOList = myWorkOptionDTOS.stream().filter(req -> req.getSysClassify().equals(searchOptionEnum.getCode())).collect(Collectors.toList());
            for (WorkOptionDTO.MyWorkOptionDTO myWorkOptionDTO : myWorkOptionDTOList) {
                WorkOptionDTO.PendingViewDetailDTO pendingViewDetailDTO = new WorkOptionDTO.PendingViewDetailDTO();
                WorkOptionDTO.TableNumDTO tableNumDTO = new WorkOptionDTO.TableNumDTO();
                tableNumDTO.setTableName(myWorkOptionDTO.getModuleCode());
                tableNumDTO.setApproveStatus(myWorkOptionDTO.getModuleStatus());
                switch (SysClassifyEnum.getEnumByCode(myWorkOptionDTO.getSysClassify())) {
                    case PLM:
                        getPlmModuleCount(tableNumDTO, myWorkOptionDTO, pendingViewDetailDTO);
                        break;
                    case SCM:
                        getScmModuleCount(tableNumDTO, myWorkOptionDTO, pendingViewDetailDTO);
                        break;
                    case WMS:
                        getWmsModuleCount(tableNumDTO, myWorkOptionDTO, pendingViewDetailDTO);
                        break;
                    default:
                        break;
                }
                pendingViewDetailDTOList.add(pendingViewDetailDTO);
            }
            pendingViewDTO.setList(pendingViewDetailDTOList);
            list.add(pendingViewDTO);
        }
        return list;
    }

    /**
     * 常用列表
     * @Author Luo_WG
     * @Date 2023/4/11 18:50
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.workflow.dto.WorkOptionDTO.frequentlyViewDTO>>
     **/
    @Override
    public List<WorkOptionDTO.FrequentlyViewDTO> listFrequentlyView() {
        LoginUser userInfo = commonService.getUserInfo();
        List<WorkOptionDTO.FrequentlyViewDTO> frequentlyViewDTOS = baseMapper.listFrequentlyView(userInfo.getUid());
        return frequentlyViewDTOS;
    }

    /**
     * 立项阶段列表
     * @Author Luo_WG
     * @Date 2023/4/12 9:33
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.workflow.dto.WorkOptionDTO.stageViewDTO>>
     **/
    @Override
    public List<WorkOptionDTO.StageViewDTO> stageView() {
        LoginUser userInfo = commonService.getUserInfo();
        List<WorkOptionDTO.StageViewDTO> stageViewDTOS = plmTaskFeign.stageView(userInfo.getUid());
        return stageViewDTOS;
    }

    private void getPlmModuleCount(WorkOptionDTO.TableNumDTO tableNumDTO, WorkOptionDTO.MyWorkOptionDTO myWorkOptionDTO, WorkOptionDTO.PendingViewDetailDTO pendingViewDetailDTO) {
        //Integer tableNum = scmTaskFeign.getTableNum(tableNumDTO);
        BeanMapperUtils.copy(myWorkOptionDTO, pendingViewDetailDTO);
        pendingViewDetailDTO.setCount(0);
        pendingViewDetailDTO.setName(myWorkOptionDTO.getModuleClassify());
    }

    private void getScmModuleCount(WorkOptionDTO.TableNumDTO tableNumDTO, WorkOptionDTO.MyWorkOptionDTO myWorkOptionDTO, WorkOptionDTO.PendingViewDetailDTO pendingViewDetailDTO) {
        Integer tableNum = scmTaskFeign.getTableNum(tableNumDTO);
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes()).getRequest();
        String localAddr = request.getLocalAddr();
        int serverPort = request.getServerPort();
        BeanMapperUtils.copy(myWorkOptionDTO, pendingViewDetailDTO);
        pendingViewDetailDTO.setCount(tableNum);
        pendingViewDetailDTO.setName(myWorkOptionDTO.getModuleClassify());
    }

    private void getWmsModuleCount(WorkOptionDTO.TableNumDTO tableNumDTO, WorkOptionDTO.MyWorkOptionDTO myWorkOptionDTO, WorkOptionDTO.PendingViewDetailDTO pendingViewDetailDTO) {
        Integer tableNum = wmsTaskFeign.getTableNum(tableNumDTO);
        BeanMapperUtils.copy(myWorkOptionDTO, pendingViewDetailDTO);
        pendingViewDetailDTO.setCount(tableNum);
        pendingViewDetailDTO.setName(myWorkOptionDTO.getModuleClassify());
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
