package com.erp.server.workflow.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.sys.vo.SysMenuVO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.model.workflow.dto.WorkOptionDTO;
import com.erp.model.workflow.entity.WorkOptionEntity;
import com.erp.model.workflow.enums.ApproveSearchOptionEnum;
import com.erp.model.workflow.enums.SysClassifyEnum;
import com.erp.model.workflow.vo.MyToDoTaskVO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.workflow.mapper.WorkOptionMapper;
import com.erp.server.workflow.service.CommonService;
import com.erp.server.workflow.service.ProcessTaskService;
import com.erp.server.workflow.service.WorkMenuService;
import com.erp.server.workflow.service.WorkOptionService;
import com.erp.server.workflow.utils.GetHttpGatewayIpPortUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.sql.rowset.serial.SerialException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

    /**
     * 待办模块-模块分类下拉
     * @Author Luo_WG
     * @Date 2023/4/21 10:49
     * @param sysClassify sysClassify
     * @return java.util.List<com.erp.model.workflow.dto.WorkOptionDTO.WaitDoMenu>
     **/
    @Override
    public List<WorkOptionDTO.WaitDoMenu> listWaitDoMenu(String sysClassify) {
        LoginUser userInfo = commonService.getUserInfo();
        List<String> roleIds = sysUserFeign.getRoleIdList(userInfo.getUid());
        List<SysMenuVO> leftMenuList = sysUserFeign.findLeftMenuByRoleIds(roleIds);
        leftMenuList.forEach(req -> {
            List<String> collect = req.getChildrenList().stream().map(SysMenuVO::getMenuUrl).distinct().collect(Collectors.toList());
        });

        List<String> collect = new ArrayList<>();
        List<WorkOptionDTO.WaitDoMenu> waitDoMenus = baseMapper.listWaitDoMenu(sysClassify);

        List<WorkOptionDTO.MyWorkOptionDTO> myWorkOptionDTOS = baseMapper.listMyWorkOption(userInfo.getUid());
        if (ObjectUtil.isNotEmpty(myWorkOptionDTOS)) {
            collect = myWorkOptionDTOS.stream().map(WorkOptionDTO.MyWorkOptionDTO::getModuleStatusId).collect(Collectors.toList());
        }
        for (WorkOptionDTO.WaitDoMenu req : waitDoMenus) {
            req.setName(req.getModuleClassify() + "-" + req.getModuleStatusName());
            if (collect.contains(req.getId())) {
                req.setSign(1);
            } else {
                req.setSign(0);
            }
        }
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
        List<String> collect = new ArrayList<>();
        LoginUser userInfo = commonService.getUserInfo();
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
        for (WorkOptionDTO.WaitDoMenu req : waitDoMenus) {
            if (req.getModuleClassify().equals("质检单")) {
                waitDoMenus.remove(req);
            }
        }
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
        if (dto.getType().equals("1")) {
            List<WorkOptionDTO.MyWorkOptionDTO> myWorkOptionDTOS = baseMapper.listMyWorkOption(userInfo.getUid());
            List<WorkOptionDTO.MyWorkOptionDTO> collect = myWorkOptionDTOS.stream().filter(req -> req.getModuleStatusId().equals(dto.getWorkMenuId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(collect)) {
                throw new ServiceException(ApiError.ERROR_940022);
            }
        } else {
            List<WorkOptionDTO.FrequentlyViewDTO> frequentlyViewDTOS = baseMapper.listFrequentlyView(userInfo.getUid());
            List<WorkOptionDTO.FrequentlyViewDTO> collect = frequentlyViewDTOS.stream().filter(req -> req.getModuleStatusId().equals(dto.getWorkMenuId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(collect)) {
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
     * 编辑修改模块
     * @Author Luo_WG
     * @Date 2023/4/20 19:45
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @Override
    public Boolean updateWaitDo(WorkOptionDTO.UpdateDTO dto) {
        LoginUser userInfo = commonService.getUserInfo();
        WorkOptionEntity byId = this.getById(dto.getId());
        if (byId.getType().equals("1")) {
            List<WorkOptionDTO.MyWorkOptionDTO> myWorkOptionDTOS = baseMapper.listMyWorkOption(userInfo.getUid());
            List<WorkOptionDTO.MyWorkOptionDTO> collect = myWorkOptionDTOS.stream().filter(req -> req.getModuleStatusId().equals(dto.getWorkMenuId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(collect)) {
                throw new ServiceException(ApiError.ERROR_940022);
            }
        } else {
            List<WorkOptionDTO.FrequentlyViewDTO> frequentlyViewDTOS = baseMapper.listFrequentlyView(userInfo.getUid());
            List<WorkOptionDTO.FrequentlyViewDTO> collect = frequentlyViewDTOS.stream().filter(req -> req.getModuleStatusId().equals(dto.getWorkMenuId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(collect)) {
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
                tableNumDTO.setModuleParam(myWorkOptionDTO.getModuleParam());
                myWorkOptionDTO.setPath(myWorkOptionDTO.getModuleUrl());
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
        frequentlyViewDTOS.forEach(req -> {
            req.setPathUrl(req.getModuleUrl());
            switch (SysClassifyEnum.getEnumByCode(req.getSysClassify())) {
                case PLM:
                    req.setModuleUrl("http://" + GetHttpGatewayIpPortUtils.IP + ":" + GetHttpGatewayIpPortUtils.PLM_PORT + req.getModuleUrl());
                    break;
                case SCM:
                    req.setModuleUrl("http://" + GetHttpGatewayIpPortUtils.IP + ":" + GetHttpGatewayIpPortUtils.SCM_PORT + req.getModuleUrl());
                    break;
                case WMS:
                    req.setModuleUrl("http://" + GetHttpGatewayIpPortUtils.IP + ":" + GetHttpGatewayIpPortUtils.WMS_PORT + req.getModuleUrl());
                    break;
                default:
                    break;
            }
        });
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

    /**
     * 删除
     * @Author Luo_WG
     * @Date 2023/4/24 13:03
     * @param id id
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean delete(String id) {
        return this.removeById(id);
    }

    private void getPlmModuleCount(WorkOptionDTO.TableNumDTO tableNumDTO, WorkOptionDTO.MyWorkOptionDTO myWorkOptionDTO, WorkOptionDTO.PendingViewDetailDTO pendingViewDetailDTO) {
        Integer tableNum = plmTaskFeign.getTableNum(tableNumDTO);
        BeanMapperUtils.copy(myWorkOptionDTO, pendingViewDetailDTO);
        pendingViewDetailDTO.setCount(0);
        pendingViewDetailDTO.setName(myWorkOptionDTO.getModuleClassify());
        pendingViewDetailDTO.setModuleUrl("http://" + GetHttpGatewayIpPortUtils.IP + ":" + GetHttpGatewayIpPortUtils.PLM_PORT + myWorkOptionDTO.getModuleUrl());
    }

    private void getScmModuleCount(WorkOptionDTO.TableNumDTO tableNumDTO, WorkOptionDTO.MyWorkOptionDTO myWorkOptionDTO, WorkOptionDTO.PendingViewDetailDTO pendingViewDetailDTO) {
        Integer tableNum = scmTaskFeign.getTableNum(tableNumDTO);
        BeanMapperUtils.copy(myWorkOptionDTO, pendingViewDetailDTO);
        pendingViewDetailDTO.setCount(tableNum);
        pendingViewDetailDTO.setName(myWorkOptionDTO.getModuleClassify());
        pendingViewDetailDTO.setModuleUrl("http://" + GetHttpGatewayIpPortUtils.IP + ":" + GetHttpGatewayIpPortUtils.SCM_PORT + myWorkOptionDTO.getModuleUrl());
    }

    private void getWmsModuleCount(WorkOptionDTO.TableNumDTO tableNumDTO, WorkOptionDTO.MyWorkOptionDTO myWorkOptionDTO, WorkOptionDTO.PendingViewDetailDTO pendingViewDetailDTO) {
        Integer tableNum = wmsTaskFeign.getTableNum(tableNumDTO);
        BeanMapperUtils.copy(myWorkOptionDTO, pendingViewDetailDTO);
        pendingViewDetailDTO.setCount(tableNum);
        pendingViewDetailDTO.setName(myWorkOptionDTO.getModuleClassify());
        pendingViewDetailDTO.setModuleUrl("http://" + GetHttpGatewayIpPortUtils.IP + ":" + GetHttpGatewayIpPortUtils.WMS_PORT + myWorkOptionDTO.getModuleUrl());
    }

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
            if (optionEnum.getCode().equals(ApproveSearchOptionEnum.WAITHANDLE.getCode())) {
                WorkOptionDTO.ApproveSearchOptionDTO approveSearchOptionDTO = new WorkOptionDTO.ApproveSearchOptionDTO();
                //获取我的待办数量
                List<WorkOptionDTO.Module> waitHandleCount = baseMapper.getWaitHandleCount(userId, ApproveStatusEnum.APPROVE_ING.getStatus(), optionEnum.getCode());
                Integer quantity = waitHandleCount.stream().map(WorkOptionDTO.Module::getQuantity).reduce(MathUtil.ZERO, Integer::sum);
                approveSearchOptionDTO.setStatus(optionEnum.getCode());
                approveSearchOptionDTO.setStatusName(ApproveSearchOptionEnum.getName(optionEnum.getCode()));
                approveSearchOptionDTO.setQuantity(quantity);
                for (WorkOptionDTO.Module module : waitHandleCount) {
                    module.setSysClassifyName(SysClassifyEnum.getName(module.getSysClassify()));
                }
                approveSearchOptionDTO.setModuleList(waitHandleCount);
                list.add(approveSearchOptionDTO);
            } else if (optionEnum.getCode().equals(ApproveSearchOptionEnum.ALREADYHANDLE.getCode())) {
                WorkOptionDTO.ApproveSearchOptionDTO approveSearchOptionDTO = new WorkOptionDTO.ApproveSearchOptionDTO();
                //获取我的已办数量
                List<WorkOptionDTO.Module> approveCount = baseMapper.getApproveCount(userId, ApproveStatusEnum.APPROVE.getStatus(), optionEnum.getCode());
                Integer quantity = approveCount.stream().map(WorkOptionDTO.Module::getQuantity).reduce(MathUtil.ZERO, Integer::sum);
                approveSearchOptionDTO.setStatus(optionEnum.getCode());
                approveSearchOptionDTO.setStatusName(ApproveSearchOptionEnum.getName(optionEnum.getCode()));
                approveSearchOptionDTO.setQuantity(quantity);
                for (WorkOptionDTO.Module module : approveCount) {
                    module.setSysClassifyName(SysClassifyEnum.getName(module.getSysClassify()));
                }
                approveSearchOptionDTO.setModuleList(approveCount);
                list.add(approveSearchOptionDTO);
            } else if (optionEnum.getCode().equals(ApproveSearchOptionEnum.CARBONCOPY.getCode())) {
                //获取我的抄送我的数量
                WorkOptionDTO.ApproveSearchOptionDTO approveSearchOptionDTO = new WorkOptionDTO.ApproveSearchOptionDTO();
                approveSearchOptionDTO.setStatus(optionEnum.getCode());
                approveSearchOptionDTO.setStatusName(ApproveSearchOptionEnum.getName(optionEnum.getCode()));
                approveSearchOptionDTO.setQuantity(0);
                approveSearchOptionDTO.setModuleList(new ArrayList<>());
                list.add(approveSearchOptionDTO);
            } else if (optionEnum.getCode().equals(ApproveSearchOptionEnum.INITIATE.getCode())) {
                WorkOptionDTO.ApproveSearchOptionDTO approveSearchOptionDTO = new WorkOptionDTO.ApproveSearchOptionDTO();
                //获取我的已发起数量
                List<WorkOptionDTO.Module> createCount = baseMapper.getCreateCount(userId, ApproveStatusEnum.APPROVE.getStatus(), optionEnum.getCode());
                Integer quantity = createCount.stream().map(WorkOptionDTO.Module::getQuantity).reduce(MathUtil.ZERO, Integer::sum);
                approveSearchOptionDTO.setStatus(optionEnum.getCode());
                approveSearchOptionDTO.setStatusName(ApproveSearchOptionEnum.getName(optionEnum.getCode()));
                approveSearchOptionDTO.setQuantity(quantity);
                for (WorkOptionDTO.Module module : createCount) {
                    module.setSysClassifyName(SysClassifyEnum.getName(module.getSysClassify()));
                }
                approveSearchOptionDTO.setModuleList(createCount);
                list.add(approveSearchOptionDTO);
            }
        }
        return list;
    }

    /**
     * 审批中心-列表
     * @Author Luo_WG
     * @Date 2023/5/11 15:32
     * @param dto dto
     * @return java.util.List<com.erp.model.workflow.dto.WorkOptionDTO.ApproveViewDTO>
     **/
    @Override
    public PagingVO<List<WorkOptionDTO.ApproveViewDTO>> approveView(PagingDTO<WorkOptionDTO.ApproveViewParamDTO> dto) {
        dto.getParams().setParam(dto.getParam());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        LoginUser userInfo = commonService.getUserInfo();
        dto.getParams().setUserId(userInfo.getUid());
        IPage<WorkOptionDTO.ApproveViewDTO> pageData = this.baseMapper.approveView(query, dto.getParams());
        if (CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO(new Page());
        }

        List<FindUserDTO> userList = sysUserFeign.getUserList();
        List<WorkOptionDTO.ApproveViewDTO> records = pageData.getRecords();
        records.forEach(req -> {
            if (StringUtils.isNotBlank(req.getApproveDuration())) {
                BigDecimal bigDecimal = BigDecimal.valueOf(Double.valueOf(req.getApproveDuration()));
                String value = String.valueOf(bigDecimal.divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_DOWN));
                req.setApproveDuration(value+" H");
            } else {
                req.setApproveDuration(0+" H");
            }
            req.setApproveStatusName(ApproveStatusEnum.getName(req.getApproveStatus()));
            FindUserDTO findUserDTO = userList.stream().filter(obj -> obj.getUserId().equals(req.getCreateUserName())).findFirst().orElse(new FindUserDTO());
            req.setCreateUserName(findUserDTO.getUserName());
        });
        return new PagingVO(pageData);
    }

}
