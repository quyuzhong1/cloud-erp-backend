package com.erp.server.workflow.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.workflow.dto.ProcessDelegateDTO;
import com.erp.model.workflow.entity.ProcessDelegateEntity;
import com.erp.model.workflow.enums.ProcessDelegateStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
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
        //时间校验
        if (addDTO.getEffectiveTime().isAfter(addDTO.getExpireTime()) || addDTO.getEffectiveTime().equals(addDTO.getExpireTime())) {
            throw new ServiceException(ApiError.PROCESS_DELEGATE_TIME_ERROR);
        }
        // 数据处理
       List<ProcessDelegateEntity> list =  handleAddData(addDTO);

        log.info("开始新增委托审批");
        boolean save = super.saveBatch(list);
        if(!save) {
            throw new ServiceException("委托审批保存失败");
        }
        // 操作日志
        List<Pair<String, String>> addPairList = list.stream().map(obj -> new Pair<>(obj.getId(),  CharSequenceUtil.format("新增-【{}】-【{}】-【{}】", ProcessDelegateStatusEnum.PENDING.getCode(), SourceTypeEnum.getName(obj.getBusinessKey()) , obj.getCode()))).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("新增了一条SKU【%s】", ModuleTypeEnum.PROCESS_DELEGATE.getCode(), addPairList, "新增操作");
        return new BaseResultDTO.AddDTO(list.get(0).getId(), list.get(0).getCode());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ProcessDelegateDTO.UpdateDTO updateDTO) {
        ProcessDelegateEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "委托审批"));
        ProcessDelegateEntity entity =  BeanMapperUtils.map(ProcessDelegateEntity.class, updateDTO);
        //更新校验
        checkUpdateData(old,entity);
        log.info("编辑 开始修改委托审批数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(entity);
        if(!save) {
            throw new ServiceException("委托审批保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录委托审批日志数据，单号：【{}】", entity.getCode());
        String msg = CharSequenceUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "委托审批");
        operateLogService.addModuleOperateLogByObj(old, entity, ModuleTypeEnum.PROCESS_DELEGATE.getCode(), entity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public List<ProcessDelegateDTO.TabListDTO> tabList(PermissionsDTO param) {
        ProcessDelegateDTO.PagingParamDTO searchParam = new ProcessDelegateDTO.PagingParamDTO();
        List<ProcessDelegateDTO.TabListDTO> tabList = this.baseMapper.tabList(searchParam);
        Map<String, ProcessDelegateDTO.TabListDTO> map = CollUtil.isEmpty(tabList) ? new HashMap<>() : tabList.stream().collect(Collectors.toMap(ProcessDelegateDTO.TabListDTO::getTabFlag, Function.identity()));
        ProcessDelegateStatusEnum[] values = ProcessDelegateStatusEnum.values();
        List<ProcessDelegateDTO.TabListDTO> list = new ArrayList<>();
        for (ProcessDelegateStatusEnum item : values) {
            searchParam.setPermissionSql(param.getPermissionSql());
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

   /**
    * 批量新增数据转换
    * @author will
    * @date 2025/5/12 19:17
    * @param addDTO
    * @return List<ProcessDelegateEntity>
    */
    private List<ProcessDelegateEntity> handleAddData(ProcessDelegateDTO.AddDTO addDTO) {
        //委托信息
        List<ProcessDelegateEntity> processDelegateList = listByBusinessKeyList(addDTO.getBusinessKeyList());
        Map<String, ProcessDelegateEntity> map = CollUtil.isEmpty(processDelegateList) ? new HashMap<>() : processDelegateList.stream().collect(Collectors.toMap(ProcessDelegateEntity::getBusinessKey, Function.identity()));

        List<ProcessDelegateEntity> resultList = new ArrayList<>();
        for (String businessKey : addDTO.getBusinessKeyList()) {
            ProcessDelegateEntity processDelegateEntity = BeanUtil.toBean(addDTO, ProcessDelegateEntity.class);
            ProcessDelegateEntity oldEntity = map.get(businessKey);
            if (ObjectUtil.isNotEmpty(oldEntity)) {
                throw new ServiceException(ApiError.PROCESS_DELEGATE_BUSINESS_KEY_EXIST,SourceTypeEnum.getName(businessKey));
            }
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
     * @param old
     * @param entity
     * @return void
     */
    private void checkUpdateData (ProcessDelegateEntity old,ProcessDelegateEntity entity) {
        //状态校验
        if (!ProcessDelegateStatusEnum.PENDING.getCode().equals(old.getStatus())) {
            throw new ServiceException(ApiError.PROCESS_DELEGATE_UPDATE);
        }
        //时间校验
        if (entity.getEffectiveTime().isAfter(entity.getExpireTime()) || entity.getEffectiveTime().equals(entity.getExpireTime())) {
            throw new ServiceException(ApiError.PROCESS_DELEGATE_TIME_ERROR);
        }
        List<ProcessDelegateEntity> processDelegateList = this.listByBusinessKeyList(Collections.singletonList(entity.getBusinessKey()));
        if (CollUtil.isNotEmpty(processDelegateList) && !processDelegateList.get(0).getId().equals(entity.getId())) {
            throw new ServiceException(ApiError.PROCESS_DELEGATE_BUSINESS_KEY_ERROR,processDelegateList.get(0).getCode());
        }
    }
    /**
     * 根据单据类型查询
     * @author will
     * @date 2025/5/12 19:32
     * @param businessKeyList
     * @return List<ProcessDelegateEntity>
     */
    private List<ProcessDelegateEntity> listByBusinessKeyList (List<String> businessKeyList) {
        return lambdaQuery().in(ProcessDelegateEntity::getBusinessKey,businessKeyList).list();
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
            listDTO.setStatusName(ProcessDelegateStatusEnum.getName(listDTO.getStatus()));
            //单据名称
            listDTO.setBusinessKeyName(SourceTypeEnum.getName(listDTO.getBusinessKey()));
            listDTO.setStartUserName(map.get(listDTO.getStartUserId()));
            listDTO.setDelegateUserName(map.get(listDTO.getDelegateUserId()));
        }
    }
}
