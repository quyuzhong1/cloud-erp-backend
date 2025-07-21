package com.erp.server.workflow.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.UserRequestPermissionsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.workflow.dto.ProcessDelegateDTO;
import com.erp.model.workflow.entity.ProcessDelegateEntity;
import com.erp.model.workflow.enums.ProcessDelegateStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.sys.feign.aspect.DataPermissionAspect;
import com.erp.server.workflow.mapper.ProcessDelegateMapper;
import com.erp.server.workflow.service.OperateLogService;
import com.erp.server.workflow.service.ProcessDelegateService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PROCESS_DELEGATE;
import static com.erp.rpc.sys.feign.aspect.DataPermissionAspect.DATA_SCOPE_ALL;

/**
 * <p>
 * 委托审批 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-05-12
 */
@Slf4j
@Service
public class ProcessDelegateServiceImpl extends SuperServiceImpl<ProcessDelegateMapper, ProcessDelegateEntity> implements ProcessDelegateService {
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ProcessDelegateDTO.AddDTO addDTO) {
        // 数据处理
        List<ProcessDelegateEntity> list =  handleAddData(addDTO);
        //更新校验
        list.forEach(this::checkUpdateData);
        log.info("开始新增委托审批");
        boolean save = super.saveBatch(list);
        if(!save) {
            throw new ServiceException("委托审批保存失败");
        }
        // 操作日志
        List<Pair<String, String>> addPairList = list.stream().map(obj -> new Pair<>(obj.getId(),  CharSequenceUtil.format("新增-【{}】-【{}】-【{}】", ProcessDelegateStatusEnum.PENDING.getName(), SourceTypeEnum.getName(obj.getBusinessKey()) , obj.getCode()))).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("%s", ModuleTypeEnum.PROCESS_DELEGATE.getCode(), addPairList, "新增操作");
        return new BaseResultDTO.AddDTO(list.get(0).getId(), list.get(0).getCode());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ProcessDelegateDTO.UpdateDTO updateDTO) {
        ProcessDelegateEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "委托审批"));
        ProcessDelegateEntity entity =  BeanMapperUtils.map(ProcessDelegateEntity.class, updateDTO);
        //状态校验
        if (!ProcessDelegateStatusEnum.PENDING.getCode().equals(old.getStatus())) {
            throw new ServiceException(ApiError.PROCESS_DELEGATE_UPDATE);
        }
        //更新校验
        checkUpdateData(entity);
        log.info("编辑 开始修改委托审批数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(entity);
        if(!save) {
            throw new ServiceException("委托审批保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录委托审批日志数据，单号：【{}】", entity.getCode());
        operateLogService.addModuleOperateLogByObj(old, entity, ModuleTypeEnum.PROCESS_DELEGATE.getCode(), entity.getId(), "");
        return Boolean.TRUE;
    }


    @Override
    public List<ProcessDelegateDTO.TabListDTO> tabList(PermissionsDTO param) {
        ProcessDelegateDTO.PagingParamDTO searchParam = new ProcessDelegateDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<ProcessDelegateDTO.TabListDTO> tabList = this.baseMapper.tabList(searchParam);
        Map<String, ProcessDelegateDTO.TabListDTO> map = CollUtil.isEmpty(tabList) ? new HashMap<>() : tabList.stream().collect(Collectors.toMap(ProcessDelegateDTO.TabListDTO::getTabFlag, Function.identity()));
        ProcessDelegateStatusEnum[] values = ProcessDelegateStatusEnum.values();
        List<ProcessDelegateDTO.TabListDTO> list = new ArrayList<>();
        for (ProcessDelegateStatusEnum item : values) {
            ProcessDelegateDTO.TabListDTO resultDTO = new ProcessDelegateDTO.TabListDTO();
            ProcessDelegateDTO.TabListDTO tabListDTO = map.get(item.getCode());
            resultDTO.setCount(ObjectUtil.isEmpty(tabListDTO) ? MathUtil.ZERO : tabListDTO.getCount());
            resultDTO.setTabFlag(item.getCode());
            resultDTO.setTabFlagName(item.getName());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    public PagingVO<ProcessDelegateDTO.ListDTO> paging(PagingDTO<ProcessDelegateDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page<>(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<ProcessDelegateDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public ProcessDelegateDTO.ViewDTO view(String id) {
        ProcessDelegateEntity entity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到委托审批单数据"));
        ProcessDelegateDTO.ViewDTO viewDTO = BeanMapperUtils.map(ProcessDelegateDTO.ViewDTO.class, entity);
        return viewDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO closeDelegate(String id) {
        ProcessDelegateEntity entity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到委托审批单数据"));
        if (!ProcessDelegateStatusEnum.PENDING.getCode().equals(entity.getStatus())
                && !ProcessDelegateStatusEnum.RUNNING.getCode().equals(entity.getStatus())) {
            throw new ServiceException(ApiError.PROCESS_DELEGATE_CLOSE);
        }
        //更新终止时间和状态
        entity.setStatus(ProcessDelegateStatusEnum.ENDED.getCode());
        entity.setClosedTime(LocalDateTime.now());
        entity.setIsAuto(Boolean.FALSE);
        boolean isClose = this.updateById(entity);
        if (!isClose) {
            throw new ServiceException(ApiError.PROCESS_DELEGATE_CLOSE_ERROR);
        }
        //添加操作日志
        String msg = CharSequenceUtil.format("操作终止【{}】 ", entity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PROCESS_DELEGATE.getCode(), entity.getId(), "终止委托");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CLOSE);
    }

    @Override
    public void exportList(ProcessDelegateDTO.PagingParamDTO param) {
        downloadTaskFeign.saveDownloadTask("委托审批单导出", EXPORT_PROCESS_DELEGATE.getCode(), param);
    }

    @Override
    public List<ProcessDelegateEntity> getByProcessDefinitionId(String processDefinitionId) {
        return  baseMapper.getByProcessDefinitionId(processDefinitionId);
    }

    @Override
    public List<ProcessDelegateEntity> listNotEnded(LocalDateTime now) {
        return lambdaQuery()
                .ne(ProcessDelegateEntity::getStatus,ProcessDelegateStatusEnum.ENDED.getCode())
                .lt(ProcessDelegateEntity::getEffectiveTime,now)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatusJob(ProcessDelegateEntity entity, LocalDateTime now) {
        String status = "";
        /**
         * 1、当前时间在生效时间-失效时间之间时更新成运行中
         * 2、当前时间在生效时间之后更新成已结束
         */
        if (ProcessDelegateStatusEnum.PENDING.getCode().equals(entity.getStatus()) && (entity.getEffectiveTime().isBefore(now) || entity.getEffectiveTime().equals(now)) && entity.getExpireTime().isAfter(now)) {
            status = ProcessDelegateStatusEnum.RUNNING.getCode();
        } else if (ProcessDelegateStatusEnum.RUNNING.getCode().equals(entity.getStatus()) && entity.getExpireTime().isBefore(now)) {
            status = ProcessDelegateStatusEnum.ENDED.getCode();
            entity.setIsAuto(Boolean.TRUE);
        } else {
            //无需更新状态
            return;
        }
        //添加操作日志
        String msg = CharSequenceUtil.format("状态由【{}】变更为【{}】 ", ProcessDelegateStatusEnum.getName(entity.getStatus()),ProcessDelegateStatusEnum.getName(status));
        entity.setStatus(status);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PROCESS_DELEGATE.getCode(), entity.getId(), "定时更新");
        this.updateById(entity);
    }

    @Override
    public List<FindUserDTO> listStartUserId() {
        //当前登陆人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        List<String> roleIdList = sysUserFeign.getRoleIdList(userInfo.getUid());

        List<UserRequestPermissionsDTO> requestPermissionsList = sysUserFeign.getRequestPermissionsList(userInfo.getUid());
        Map<String,List<UserRequestPermissionsDTO>> map = CollUtil.isEmpty(requestPermissionsList) ? new HashMap<>() : requestPermissionsList.stream().collect(Collectors.groupingBy(UserRequestPermissionsDTO::getPermissionsCode));
        List<UserRequestPermissionsDTO> userRequestPermissionsList = map.get("workflow:processDelegate:add");
        if (CollUtil.isEmpty(userRequestPermissionsList) && !roleIdList.contains("1")) {
            throw new ServiceException("当前登陆人未找到新增权限");
        }
        UserRequestPermissionsDTO userRequestPermissionsDTO = CollUtil.isEmpty(userRequestPermissionsList) ? new UserRequestPermissionsDTO() : userRequestPermissionsList.get(0);
        if (roleIdList.contains("1")) {
            userRequestPermissionsDTO.setDataScope(DATA_SCOPE_ALL);
            userRequestPermissionsDTO.setPermissionsCode("workflow:processDelegate:add");
        }
        if (DataPermissionAspect.DATA_SCOPE_SELF.equals(userRequestPermissionsDTO.getDataScope())) {
            //个人权限
            return  sysUserFeign.getUserListByUserIds(Collections.singletonList(userInfo.getUid()));
        } else if (DataPermissionAspect.DATA_SCOPE_DEPT.equals(userRequestPermissionsDTO.getDataScope())) {
            //部门权限
            List<String> userIdList = sysUserFeign.getDepUserList(userInfo.getUid());
            if (CollUtil.isEmpty(userIdList)) {
                return Collections.emptyList();
            }
            List<FindUserDTO> userList = sysUserFeign.getUserList();
            if (CollUtil.isEmpty(userList)) {
                return Collections.emptyList();
            }
            return userList.stream().filter(obj -> userIdList.contains(obj.getUserId())).collect(Collectors.toList());
        } else {
            //全部权限
            return sysUserFeign.getUserList();
        }
    }

    /**
    * 批量新增数据转换
    * @author will
    * @date 2025/5/12 19:17
    * @param addDTO
    * @return List<ProcessDelegateEntity>
    */
    private List<ProcessDelegateEntity> handleAddData(ProcessDelegateDTO.AddDTO addDTO) {

        List<ProcessDelegateEntity> resultList = new ArrayList<>();
        for (String businessKey : addDTO.getBusinessKeyList()) {
            ProcessDelegateEntity processDelegateEntity = BeanUtil.toBean(addDTO, ProcessDelegateEntity.class);
            // 生成单号
            String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_LCWT);
            processDelegateEntity.setCode(code);
            processDelegateEntity.setBusinessKey(businessKey);
            resultList.add(processDelegateEntity);
        }
        return resultList;
    }

    /**
     * 更新校验
     * @author will
     * @date 2025/5/12 19:30
     * @param entity
     * @return void
     */
    private void checkUpdateData (ProcessDelegateEntity entity) {
        //时间校验
        if (entity.getEffectiveTime().isAfter(entity.getExpireTime()) || entity.getEffectiveTime().equals(entity.getExpireTime())) {
            throw new ServiceException(ApiError.PROCESS_DELEGATE_TIME_ERROR);
        }
        List<ProcessDelegateEntity> processDelegateList = this.listByBusinessKeyList(Collections.singletonList(entity.getBusinessKey()),entity.getStartUserId());

        for ( ProcessDelegateEntity detailEntity : processDelegateList) {
            //单据类型不能重复
            if (detailEntity.getId().equals(entity.getId())) {
                continue;
            }
            //时间不能重叠
            boolean overlap = LocalDateUtil.isOverlapLocalDateTime(entity.getEffectiveTime(), entity.getExpireTime(), detailEntity.getEffectiveTime(), detailEntity.getExpireTime());
            if (overlap) {
                throw new ServiceException(ApiError.PROCESS_PROCESS_DELEGATE_OVERLAP);
            }
        }
    }
    /**
     * 根据单据类型查询未结束数据
     * @author will
     * @date 2025/5/12 19:32
     * @param businessKeyList
     * @return List<ProcessDelegateEntity>
     */
    private List<ProcessDelegateEntity> listByBusinessKeyList (List<String> businessKeyList,String startUserId) {
        return lambdaQuery().in(ProcessDelegateEntity::getBusinessKey,businessKeyList).eq(ProcessDelegateEntity::getStartUserId,startUserId).ne(ProcessDelegateEntity::getStatus,ProcessDelegateStatusEnum.ENDED.getCode()).list();
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<ProcessDelegateDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        Map<String, String> map = userList.stream().collect(Collectors.toMap(FindUserDTO::getUserId, FindUserDTO::getUserName));

        for (ProcessDelegateDTO.ListDTO listDTO : list) {
            //状态名称
            if (ProcessDelegateStatusEnum.ENDED.getCode().equals(listDTO.getStatus())) {
                listDTO.setStatusName(ProcessDelegateStatusEnum.getName(listDTO.getStatus()) + (listDTO.getIsAuto() ? "" :"[终止]"));
            } else {
                listDTO.setStatusName(ProcessDelegateStatusEnum.getName(listDTO.getStatus()));
            }
            //单据名称
            listDTO.setBusinessKeyName(SourceTypeEnum.getName(listDTO.getBusinessKey()));
            listDTO.setStartUserName(map.get(listDTO.getStartUserId()));
            listDTO.setDelegateUserName(map.get(listDTO.getDelegateUserId()));
        }
    }
}
