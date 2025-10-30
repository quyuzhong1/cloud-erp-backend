package com.erp.server.workflow.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.dto.TaskHandleDataDTO;
import com.erp.model.plm.dto.TaskOperateDTO;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.sys.entity.SysRoleMenuEntity;
import com.erp.model.workflow.dto.ApproveParamDTO;
import com.erp.model.workflow.dto.TaskShowDTO;
import com.erp.model.workflow.dto.WorkOptionDTO;
import com.erp.model.workflow.entity.ProcessManagementEntity;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.model.workflow.entity.WorkOptionEntity;
import com.erp.model.workflow.enums.ApproveSearchOptionEnum;
import com.erp.model.workflow.enums.SysClassifyEnum;
import com.erp.rpc.oms.feign.*;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.scm.feign.SupplierFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.workflow.mapper.WorkOptionMapper;
import com.erp.server.workflow.service.*;
import com.erp.server.workflow.utils.GetHttpGatewayIpPortUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 工作台选项表服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-04-11
 */
@Service
public class WorkOptionServiceImpl extends SuperServiceImpl<WorkOptionMapper, WorkOptionEntity> implements WorkOptionService {


    public static final String PROTOCOL = "http://";
    @Resource
    private ProcessTaskService workflowFeign;

    @Resource
    private WorkMenuService workMenuService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private OmsTaskFeign omsTaskFeign;

    @Resource
    private SoInfoFeign soInfoFeign;
    @Resource
    private SoB2cFeign soB2cFeign;
    @Resource
    private SoChangeFeign soChangeFeign;

    @Resource
    private CustomerFeign customerFeign;

    @Resource
    private SupplierFeign supplierFeign;

    @Resource
    private ProcessManagementService processManagementService;

    @Resource
    private ProcessTaskManagementService processTaskManagementService;

    @Resource
    private SoPriceFeign soPriceFeign;

    @Resource
    private ExhibitionOrderFeign exhibitionOrderFeign;

    /**
     * 待办模块-模块分类下拉
     *
     * @param sysClassify sysClassify
     * @return java.util.List<com.erp.model.workflow.dto.WorkOptionDTO.WaitDoMenu>
     * @Author Luo_WG
     * @Date 2023/4/21 10:49
     **/
    @Override
    public List<WorkOptionDTO.WaitDoMenu> listWaitDoMenu(String sysClassify) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        List<String> roleIds = sysUserFeign.getRoleIdList(userInfo.getUid());
        List<SysRoleMenuEntity> menuRefRoleByRoleIds = sysUserFeign.getMenuRefRoleByRoleIds(roleIds);
        List<String> collect = new ArrayList<>();
        List<WorkOptionDTO.WaitDoMenu> waitDoMenus = baseMapper.listWaitDoMenu(sysClassify);
        List<WorkOptionDTO.MyWorkOptionDTO> myWorkOptionDTOS = baseMapper.listMyWorkOption(userInfo.getUid());
        if (ObjectUtil.isNotEmpty(myWorkOptionDTOS)) {
            collect = myWorkOptionDTOS.stream().map(WorkOptionDTO.MyWorkOptionDTO::getModuleStatusId).collect(Collectors.toList());
        }

        List<WorkOptionDTO.WaitDoMenu> returnWaitDoMenus = new ArrayList<>();
        for (WorkOptionDTO.WaitDoMenu req : waitDoMenus) {
            req.setName(req.getModuleClassify() + "-" + req.getModuleStatusName());
            if (collect.contains(req.getId())) {
                req.setSign(1);
            } else {
                req.setSign(0);
            }
            String menuId = menuRefRoleByRoleIds.stream().filter(obj -> obj.getMenuId().equals(req.getSysMenuId())).map(SysRoleMenuEntity::getMenuId).distinct().findFirst().orElse("");
            if (StringUtils.isNotBlank(menuId)) {
                returnWaitDoMenus.add(req);
            }
        }
        return returnWaitDoMenus;
    }


    /**
     * 常用模块-模块分类下拉
     *
     * @param sysClassify sysClassify
     * @return java.util.List<com.erp.model.workflow.dto.WorkOptionDTO.WaitDoMenu>
     * @Author Luo_WG
     * @Date 2023/4/21 10:50
     **/
    @Override
    public List<WorkOptionDTO.WaitDoMenu> listOftenMenu(String sysClassify) {
        List<String> collect = new ArrayList<>();
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        List<WorkOptionDTO.FrequentlyViewDTO> frequentlyViewDTOS = baseMapper.listFrequentlyView(userInfo.getUid());
        if (ObjectUtil.isNotEmpty(frequentlyViewDTOS)) {
            collect = frequentlyViewDTOS.stream().map(WorkOptionDTO.FrequentlyViewDTO::getModuleStatusId).collect(Collectors.toList());
        }
        List<WorkOptionDTO.WaitDoMenu> waitDoMenus = baseMapper.listOftenMenu(sysClassify);
        for (WorkOptionDTO.WaitDoMenu req : waitDoMenus) {
            req.setName(req.getModuleClassify());
            if (collect.contains(req.getId())) {
                req.setSign(1);
            } else {
                req.setSign(0);
            }
        }

        return waitDoMenus.stream().filter(req -> !req.getModuleClassify().equals("质检单")).collect(Collectors.toList());
    }

    /**
     * 新增模块
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/20 19:45
     **/
    @Override
    public Boolean addWaitDo(WorkOptionDTO.AddDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        if (dto.getType().equals("1")) {
            List<WorkOptionDTO.MyWorkOptionDTO> myWorkOptionDTOS = baseMapper.listMyWorkOption(userInfo.getUid());
            List<WorkOptionDTO.MyWorkOptionDTO> collect = myWorkOptionDTOS.stream().filter(req -> req.getModuleStatusId().equals(dto.getWorkMenuId())).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(collect)) {
                throw new ServiceException(ApiError.ERROR_940022);
            }
        } else {
            List<WorkOptionDTO.FrequentlyViewDTO> frequentlyViewDTOS = baseMapper.listFrequentlyView(userInfo.getUid());
            List<WorkOptionDTO.FrequentlyViewDTO> collect = frequentlyViewDTOS.stream().filter(req -> req.getModuleStatusId().equals(dto.getWorkMenuId())).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(collect)) {
                throw new ServiceException(ApiError.ERROR_940022);
            }
        }
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
     * 新增常用模块
     * @Author Luo_WG
     * @Date 2023/7/7 16:28
     * @param dto
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean addOften(WorkOptionDTO.AddOftenDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        WorkOptionEntity workOptionEntity = new WorkOptionEntity();
        workOptionEntity.setOptionUserId(userInfo.getUid());
        workOptionEntity.setOptionUserMame(userInfo.getUserName());
        workOptionEntity.setModuleUrl(dto.getModuleUrl());
        workOptionEntity.setType(dto.getType());
        workOptionEntity.setModuleName(dto.getModuleName());
        return this.save(workOptionEntity);
    }

    /**
     * 编辑修改模块
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/20 19:45
     **/
    @Override
    public Boolean updateWaitDo(WorkOptionDTO.UpdateDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        WorkOptionEntity byId = this.getById(dto.getId());
        if (byId.getType().equals("1")) {
            List<WorkOptionDTO.MyWorkOptionDTO> myWorkOptionDTOS = baseMapper.listMyWorkOption(userInfo.getUid());
            List<WorkOptionDTO.MyWorkOptionDTO> collect = myWorkOptionDTOS.stream().filter(req -> req.getModuleStatusId().equals(dto.getWorkMenuId())).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(collect)) {
                throw new ServiceException(ApiError.ERROR_940022);
            }
        } else {
            List<WorkOptionDTO.FrequentlyViewDTO> frequentlyViewDTOS = baseMapper.listFrequentlyView(userInfo.getUid());
            List<WorkOptionDTO.FrequentlyViewDTO> collect = frequentlyViewDTOS.stream().filter(req -> req.getModuleStatusId().equals(dto.getWorkMenuId())).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(collect)) {
                throw new ServiceException(ApiError.ERROR_940022);
            }
        }
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
     * 常用列表
     *
     * @return com.common.core.controller.vo.ApiResult<java.util.List < com.erp.model.workflow.dto.WorkOptionDTO.frequentlyViewDTO>>
     * @Author Luo_WG
     * @Date 2023/4/11 18:50
     **/
    @Override
    public List<WorkOptionDTO.FrequentlyViewDTO> listFrequentlyView() {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        List<WorkOptionDTO.FrequentlyViewDTO> frequentlyViewDTOS = baseMapper.listFrequentlyView(userInfo.getUid());
        frequentlyViewDTOS.forEach(req -> {
            req.setPathUrl(req.getModuleUrl());
            SysClassifyEnum enumByCode = SysClassifyEnum.getEnumByCode(req.getSysClassify());
            if (enumByCode != null) {
                switch (enumByCode) {
                    case PLM:
                        req.setModuleUrl(PROTOCOL + GetHttpGatewayIpPortUtils.IP + ":" + GetHttpGatewayIpPortUtils.PLM_PORT + req.getModuleUrl());
                        break;
                    case SCM:
                        req.setModuleUrl(PROTOCOL+ GetHttpGatewayIpPortUtils.IP + ":" + GetHttpGatewayIpPortUtils.SCM_PORT + req.getModuleUrl());
                        break;
                    case WMS:
                    case FM:
                        req.setModuleUrl(PROTOCOL + GetHttpGatewayIpPortUtils.IP + ":" + GetHttpGatewayIpPortUtils.WMS_PORT + req.getModuleUrl());
                        break;
                    case OMS:
                        req.setModuleUrl(PROTOCOL + GetHttpGatewayIpPortUtils.IP + ":" + GetHttpGatewayIpPortUtils.OMS_PORT + req.getModuleUrl());
                        break;
                    default:
                        break;
                }
            }
        });
        return frequentlyViewDTOS;
    }

    /**
     * 立项阶段列表
     *
     * @return com.common.core.controller.vo.ApiResult<java.util.List < com.erp.model.workflow.dto.WorkOptionDTO.stageViewDTO>>
     * @Author Luo_WG
     * @Date 2023/4/12 9:33
     **/
    @Override
    public List<WorkOptionDTO.StageViewDTO> stageView() {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        return plmTaskFeign.stageView(userInfo.getUid());
    }

    /**
     * 删除
     *
     * @param id id
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/4/24 13:03
     **/
    @Override
    public Boolean delete(String id) {
        return removeById(id);
    }

    private List<WorkOptionDTO.MyWorkOptionDTO> listTableNum(List<WorkOptionDTO.MyWorkOptionDTO> myWorkOptionDTOList, String sysClassify) {
        switch (SysClassifyEnum.getEnumByCode(sysClassify)) {
            case PLM:
                return plmTaskFeign.getTableNum(myWorkOptionDTOList);
            case SCM:
                return scmTaskFeign.getTableNum(myWorkOptionDTOList);
            case WMS:
            case FM:
                return wmsTaskFeign.getTableNum(myWorkOptionDTOList);
            case OMS:
                return omsTaskFeign.getTableNum(myWorkOptionDTOList);
            default:
                break;
        }
        return new ArrayList<>();
    }
    /**
     * 待办列表
     *
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO < com.erp.model.wms.dto.PurchaseReturnOrderDTO.PagingViewDTO>>
     * @Author Luo_WG
     * @Date 2023/4/11 18:48
     **/
    @Override
    public List<WorkOptionDTO.PendingViewDTO> listPendingView() {
        List<WorkOptionDTO.PendingViewDTO> list = new ArrayList<>();
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        List<WorkOptionDTO.MyWorkOptionDTO> myWorkOptionDTOS = baseMapper.listMyWorkOption(userInfo.getUid());
        List<SysClassifyEnum> sysClassifyEnums = SysClassifyEnum.getAll();
        for (SysClassifyEnum searchOptionEnum : sysClassifyEnums) {
            WorkOptionDTO.PendingViewDTO pendingViewDTO = new WorkOptionDTO.PendingViewDTO();
            List<WorkOptionDTO.PendingViewDetailDTO> pendingViewDetailDTOList = new ArrayList<>();
            pendingViewDTO.setSysClassify(searchOptionEnum.getCode());
            List<WorkOptionDTO.MyWorkOptionDTO> myWorkOptionDTOList = myWorkOptionDTOS.stream().filter(req -> req.getSysClassify().equals(searchOptionEnum.getCode())).collect(Collectors.toList());
            List<WorkOptionDTO.MyWorkOptionDTO> myWorkOptionList = listTableNum(myWorkOptionDTOList, searchOptionEnum.getCode());
            for (WorkOptionDTO.MyWorkOptionDTO myWorkOptionDTO : myWorkOptionList) {
                WorkOptionDTO.PendingViewDetailDTO pendingViewDetailDTO = new WorkOptionDTO.PendingViewDetailDTO();
                myWorkOptionDTO.setPath(myWorkOptionDTO.getModuleUrl());
                BeanMapperUtils.copy(myWorkOptionDTO, pendingViewDetailDTO);
                pendingViewDetailDTO.setName(myWorkOptionDTO.getModuleClassify());
                pendingViewDetailDTO.setCount(myWorkOptionDTO.getTableNumber());
                switch (SysClassifyEnum.getEnumByCode(myWorkOptionDTO.getSysClassify())) {
                    case PLM:
                        pendingViewDetailDTO.setModuleUrl(PROTOCOL+ GetHttpGatewayIpPortUtils.IP + ":" + GetHttpGatewayIpPortUtils.PLM_PORT + myWorkOptionDTO.getModuleUrl());
                        break;
                    case SCM:
                        pendingViewDetailDTO.setModuleUrl(PROTOCOL + GetHttpGatewayIpPortUtils.IP + ":" + GetHttpGatewayIpPortUtils.SCM_PORT + myWorkOptionDTO.getModuleUrl());
                        break;
                    case WMS:
                    case FM:
                        pendingViewDetailDTO.setModuleUrl(PROTOCOL + GetHttpGatewayIpPortUtils.IP + ":" + GetHttpGatewayIpPortUtils.WMS_PORT + myWorkOptionDTO.getModuleUrl());
                        break;
                    case OMS:
                        pendingViewDetailDTO.setModuleUrl(PROTOCOL + GetHttpGatewayIpPortUtils.IP + ":" + GetHttpGatewayIpPortUtils.OMS_PORT + myWorkOptionDTO.getModuleUrl());
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
     * 审批中心-下拉搜索选项
     *
     * @return java.util.List<com.erp.model.workflow.dto.WorkOptionDTO.ApproveSearchOptionDTO>
     * @Author Luo_WG
     * @Date 2023/4/12 11:58
     **/
    @Override
    public List<WorkOptionDTO.ApproveSearchOptionDTO> approveSearchOption() {
        List<WorkOptionDTO.ApproveSearchOptionDTO> list = new ArrayList<>();
        String userId = UserContext.getDefaultLoginUser().getUid();
        List<ApproveSearchOptionEnum> all = ApproveSearchOptionEnum.getAll();
        WorkOptionDTO.ApproveViewParamDTO paramDTO = new WorkOptionDTO.ApproveViewParamDTO();
        for (ApproveSearchOptionEnum optionEnum : all) {
            WorkOptionDTO.ApproveSearchOptionDTO approveSearchOptionDTO = new WorkOptionDTO.ApproveSearchOptionDTO();
            paramDTO.setStatus(optionEnum.getCode());
            paramDTO.setUserId(userId);
            List<WorkOptionDTO.Module> modules = baseMapper.approveViewCount(paramDTO);
            Integer quantity = modules.stream().map(WorkOptionDTO.Module::getQuantity).reduce(MathUtil.ZERO, Integer::sum);
            approveSearchOptionDTO.setStatus(optionEnum.getCode());
            approveSearchOptionDTO.setStatusName(ApproveSearchOptionEnum.getName(optionEnum.getCode()));
            approveSearchOptionDTO.setQuantity(quantity);
            for (WorkOptionDTO.Module module : modules) {
                module.setSysClassifyName(SysClassifyEnum.getName(module.getSysClassify()));
            }
            approveSearchOptionDTO.setModuleList(modules);
            list.add(approveSearchOptionDTO);
        }
        return list;
    }

    /**
     * 审批中心-列表
     *
     * @param dto dto
     * @return java.util.List<com.erp.model.workflow.dto.WorkOptionDTO.ApproveViewDTO>
     * @Author Luo_WG
     * @Date 2023/5/11 15:32
     **/
    @Override
    public PagingVO<WorkOptionDTO.ApproveViewDTO> approveView(PagingDTO<WorkOptionDTO.ApproveViewParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<?> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        dto.getParams().setUserId(userInfo.getUid());
        IPage<WorkOptionDTO.ApproveViewDTO> pageData = this.baseMapper.approveView(query, dto.getParams());
        if (CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(new Page<>());
        }

        List<FindUserDTO> userList = sysUserFeign.getUserList();
        List<WorkOptionDTO.ApproveViewDTO> records = pageData.getRecords();
        List<String> businessIds = records.stream().map(WorkOptionDTO.ApproveViewDTO::getBusinessId).distinct().collect(Collectors.toList());

        List<ProcessTaskManagementEntity> processTaskManagementEntities = processTaskManagementService.listProcessByBusinessId(businessIds);

        records.forEach(req -> {
            List<String> curApproveName = processTaskManagementEntities.stream().filter(obj -> req.getBusinessId().equals(obj.getBusinessId()) && obj.getTaskStatus().equals(ApproveStatusEnum.APPROVE_ING)).map(ProcessTaskManagementEntity::getCurApproveName).distinct().collect(Collectors.toList());
            String userName = StringUtils.join(curApproveName, ",");
            req.setApproveUserName(userName);

            if (StringUtils.isNotBlank(req.getApproveDuration())) {
                BigDecimal bigDecimal = BigDecimal.valueOf(Double.valueOf(req.getApproveDuration()));
                String value = String.valueOf(bigDecimal.divide(BigDecimal.valueOf(3600), 2, BigDecimal.ROUND_DOWN));
                req.setApproveDuration(value + " H");
            } else {
                req.setApproveDuration(0 + " H");
            }
            if (StringUtils.isNotBlank(req.getCancelProcessParam())) {
                req.setCancelProcessParam(JSONUtil.toJsonStr(CharSequenceUtil.format(req.getCancelProcessParam(), req.getBusinessId())));
            }
            req.setApproveStatusName(ApproveStatusEnum.getName(req.getApproveStatus()));
            FindUserDTO findUserDTO = userList.stream().filter(obj -> obj.getUserId().equals(req.getCreateUserId())).findFirst().orElse(new FindUserDTO());
            req.setCreateUserName(findUserDTO.getUserName());
        });
        return new PagingVO<>(pageData);
    }

    @Override
    public Boolean approve(ApproveParamDTO dto) {
        BaseApproveParamDTO paramDTO = new BaseApproveParamDTO();
        BeanMapperUtils.copy(dto, paramDTO);
        paramDTO.setIds(Arrays.asList(dto.getId()));
        ProcessTaskManagementEntity taskManagementEntity = processTaskManagementService.getById(dto.getId());
        ProcessManagementEntity entity = processManagementService.getByProcessInstanceId(taskManagementEntity.getProcessInstanceId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_94000);
        }
        dto.setId(entity.getBusinessId());
        String sysClassifyByCode = workMenuService.getSysClassifyByCode(entity.getBusinessKey());
        switch (SysClassifyEnum.getEnumByCode(sysClassifyByCode)) {
            case PLM :
                plmApprove(dto, entity);
                break;
            case SCM :
                scmApprove(dto, entity);
                break;
            case WMS:
            case FM:
                wmsApprove(dto, entity);
                break;
            case OMS:
                omsApprove(dto, entity);
                break;
            default:
                throw new ServiceException(ApiError.ERROR_94006);
        }
        return Boolean.TRUE;
    }

    private Boolean plmApprove(ApproveParamDTO dto, ProcessManagementEntity entity) {
        switch (SourceTypeEnum.getByCode(entity.getBusinessKey())) {
            case PILOT_APPLICATION:
                ApproveOneDTO approveOneDTO = new ApproveOneDTO();
                approveOneDTO.setId(dto.getId());
                approveOneDTO.setComment(dto.getComment());
                approveOneDTO.setType(dto.getType());
                plmTaskFeign.pilotApprovalPass(approveOneDTO);
                break;
            case PRODUCT_BOM_INFO:
                ApproveOneDTO bomApproveOneDTO = new ApproveOneDTO();
                bomApproveOneDTO.setId(dto.getId());
                bomApproveOneDTO.setComment(dto.getComment());
                bomApproveOneDTO.setType(dto.getType());
                plmTaskFeign.bomInfoApprove(bomApproveOneDTO);
                break;
            case PRODUCT_DETAIL:
                ApproveOneDTO paramDTO = new ApproveOneDTO();
                paramDTO.setId(dto.getId());
                paramDTO.setComment(dto.getComment());
                paramDTO.setType(dto.getType());
                plmTaskFeign.productDetailApprove(paramDTO);
                break;
            case PROJECT_TASK:
                LoginUser userInfo = UserContext.getDefaultLoginUser();
                ProjectTaskEntity taskEntity = plmTaskFeign.getProductIdByTaskId(dto.getId());
                List<TaskShowDTO> workflowList = workflowFeign.queryMyToDo(userInfo.getUid());
                TaskOperateDTO taskOperateDTO = new TaskOperateDTO();
                TaskHandleDataDTO taskHandleDataDTO = new TaskHandleDataDTO();
                taskHandleDataDTO.setTaskId(dto.getId());
                taskHandleDataDTO.setProcessId(taskEntity.getProcessId());
                TaskShowDTO workflowTask = workflowList.stream().filter(w -> w.getProcessInstanceId().equals(taskEntity.getProcessId())).findFirst().orElse(null);
                if (workflowTask != null) {
                    taskHandleDataDTO.setProcessTaskId(workflowTask.getTaskId());
                }
                taskOperateDTO.setTaskDataList(Arrays.asList(taskHandleDataDTO));
                taskOperateDTO.setProductId(taskEntity.getProductId());
                taskOperateDTO.setComment(dto.getComment());
                plmTaskFeign.projectTaskApprovalPass(taskOperateDTO);
                break;
            case PRODUCT_CHANGE:
                ApproveOneDTO approveDTO = new ApproveOneDTO();
                approveDTO.setId(dto.getId());
                approveDTO.setComment(dto.getComment());
                approveDTO.setType(dto.getType());
                plmTaskFeign.productChangeApprove(approveDTO);
                break;
            default:
                throw new ServiceException(ApiError.ERROR_94006);
        }
        return Boolean.TRUE;
    }

    private Boolean scmApprove(ApproveParamDTO dto, ProcessManagementEntity entity) {
        BaseApproveParamDTO baseApproveParamDTO = new BaseApproveParamDTO();
        baseApproveParamDTO.setIds(Arrays.asList(dto.getId()));
        baseApproveParamDTO.setType(dto.getType());
        baseApproveParamDTO.setComment(dto.getComment());

        ApproveOneDTO approveOneDTO = new ApproveOneDTO();
        approveOneDTO.setType(dto.getType());
        approveOneDTO.setId(dto.getId());
        approveOneDTO.setComment(dto.getComment());
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        switch (SourceTypeEnum.getByCode(entity.getBusinessKey())) {
            case PURCHASE_PRICE_CHANGE:
                resultDTOList = scmTaskFeign.purchasePriceChangeApprove(baseApproveParamDTO);
                break;
            case SALES_DEMAND:
                resultDTOList = scmTaskFeign.salesDemandApprove(baseApproveParamDTO);
                break;
            case PURCHASE_APPLICATION:
                resultDTOList = scmTaskFeign.purchaseApplicationApprove(baseApproveParamDTO);
                break;
            case PURCHASE_ORDER:
                scmTaskFeign.purchaseOrderApprove(approveOneDTO);
                break;
            case PURCHASE_CHANGE:
                resultDTOList = scmTaskFeign.purchaseChangeApprove(baseApproveParamDTO);
                break;
            case PURCHASE_PRICE:
                resultDTOList = scmTaskFeign.purchasePriceApprove(baseApproveParamDTO);
                break;
            case SUPPLIER:
                resultDTOList = supplierFeign.supplierApprove(baseApproveParamDTO);
                break;
            case SUBCONTRACT_ORDER:
                scmTaskFeign.subcontractOrderApprove(approveOneDTO);
                break;
            default:
                throw new ServiceException(ApiError.ERROR_94006);
        }
        BatchResultDTO resultDTO = resultDTOList.stream().filter(req -> !req.getSuccess()).findFirst().orElse(null);
        if (resultDTO != null) {
            throw new ServiceException(resultDTO.getMsg());
        }
        return Boolean.TRUE;
    }

    private Boolean wmsApprove(ApproveParamDTO dto, ProcessManagementEntity entity) {
        BaseApproveParamDTO baseApproveParamDTO = new BaseApproveParamDTO();
        baseApproveParamDTO.setIds(Arrays.asList(dto.getId()));
        baseApproveParamDTO.setType(dto.getType());
        baseApproveParamDTO.setComment(dto.getComment());
        baseApproveParamDTO.setDeliveryDate(dto.getDeliveryDate());
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        switch (SourceTypeEnum.getByCode(entity.getBusinessKey())) {
            case QC_INFO:
                break;
            case PO_RECEIVE:
                resultDTOList = wmsTaskFeign.warehouseReceiveApprove(baseApproveParamDTO);
                break;
            case PO_INSTOCK:
                resultDTOList = wmsTaskFeign.poInstockApprove(baseApproveParamDTO);
                break;
            case PO_RETURN:
                resultDTOList = wmsTaskFeign.purchaseReturnOrderApprove(baseApproveParamDTO);
                break;
            case TRANSFER_APPLICATION:
                resultDTOList = wmsTaskFeign.transferApplicationApprove(baseApproveParamDTO);
                break;
            case STOCKTAKING_TASK:
                wmsTaskFeign.stocktakingTaskApprove(baseApproveParamDTO);
                break;
            case FIRST_MILE_DELIVERY:
                wmsTaskFeign.fbaDeliveryApprove(baseApproveParamDTO);
                break;
            case DELIVERY_PLAN:
                wmsTaskFeign.deliveryPlanApprove(baseApproveParamDTO);
                break;
            case TRANSFER_INFO:
                resultDTOList = wmsTaskFeign.transferInfoApprove(baseApproveParamDTO);
                break;
            case SO_DELIVERY_NOTICE_CHANGE:
                resultDTOList = wmsTaskFeign.noticeChangeApprove(baseApproveParamDTO);
                break;
            case REQUISITION_APPLICATION_CHANGE:
                resultDTOList = wmsTaskFeign.requisitionChangeApprove(baseApproveParamDTO);
                break;
            case OTHER_INSTOCK:
                resultDTOList = wmsTaskFeign.otherInstockApprove(baseApproveParamDTO);
                break;
            case OTHER_OUTSTOCK:
                resultDTOList = wmsTaskFeign.otherOutstockApprove(baseApproveParamDTO);
                break;
            case TRANSFER_IN:
                resultDTOList = wmsTaskFeign.otherOutstockApprove(baseApproveParamDTO);
                break;
            case TRANSFER_OUT:
                resultDTOList = wmsTaskFeign.transferOutApprove(baseApproveParamDTO);
                break;
            case SAMPLE_RECIPIENT:
                resultDTOList = wmsTaskFeign.sampleRecipientApprove(baseApproveParamDTO);
                break;
            case SAMPLE_RETURN_INFO:
                resultDTOList = wmsTaskFeign.sampleReturnApprove(baseApproveParamDTO);
                break;
            case SAMPLE_BORROW_INFO:
                resultDTOList = wmsTaskFeign.sampleBorrowApprove(baseApproveParamDTO);
                break;
            case SAMPLE_SCRAP_INFO:
                resultDTOList = wmsTaskFeign.sampleScrapApprove(baseApproveParamDTO);
                break;
            case SAMPLE_BACK_INFO:
                resultDTOList = wmsTaskFeign.sampleBackApprove(baseApproveParamDTO);
                break;
            case SAMPLE_LEDGER_INIT:
                resultDTOList = wmsTaskFeign.sampleLedgerInitApprove(baseApproveParamDTO);
                break;
                
            default:
                throw new ServiceException(ApiError.ERROR_94006);
        }
        BatchResultDTO resultDTO = resultDTOList.stream().filter(req -> !req.getSuccess()).findFirst().orElse(null);
        if (resultDTO != null) {
            throw new ServiceException(resultDTO.getMsg());
        }
        return Boolean.TRUE;
    }

    private Boolean omsApprove(ApproveParamDTO dto, ProcessManagementEntity entity) {
        BaseApproveParamDTO baseApproveParamDTO = new BaseApproveParamDTO();
        baseApproveParamDTO.setIds(Arrays.asList(dto.getId()));
        baseApproveParamDTO.setType(dto.getType());
        baseApproveParamDTO.setComment(dto.getComment());
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        switch (SourceTypeEnum.getByCode(entity.getBusinessKey())) {
            case SO_INFO:
                resultDTOList = soInfoFeign.approve(baseApproveParamDTO);
                break;
            case SO_B2C:
                resultDTOList = soB2cFeign.approve(baseApproveParamDTO);
                break;
            case SO_CHANGE:
                ApiResult<List<BatchResultDTO>> approve = soChangeFeign.approve(baseApproveParamDTO);
                resultDTOList = approve.getData();
                break;
            case CUSTOMER_INFO:
                ApiResult<List<BatchResultDTO>> apiResult = customerFeign.approve(baseApproveParamDTO);
                resultDTOList = apiResult.getData();
                break;
            case SO_PRICE:
                ApiResult<List<BatchResultDTO>>  soPriceResult  = soPriceFeign.approve(baseApproveParamDTO);
                resultDTOList = soPriceResult.getData();
                break;
            case SO_PRICE_CHANGE:
                ApiResult<List<BatchResultDTO>> soPriceChangeResult  =  soPriceFeign.approveChange(baseApproveParamDTO);
                resultDTOList = soPriceChangeResult.getData();
                break;
            case EXHIBITION_ORDER:
                resultDTOList = exhibitionOrderFeign.approve(baseApproveParamDTO);
                break;
            default:
                throw new ServiceException(ApiError.ERROR_94006);
        }
        BatchResultDTO resultDTO = resultDTOList.stream().filter(req -> !req.getSuccess()).findFirst().orElse(null);
        if (resultDTO != null) {
            throw new ServiceException(resultDTO.getMsg());
        }
        return Boolean.TRUE;
    }
}
