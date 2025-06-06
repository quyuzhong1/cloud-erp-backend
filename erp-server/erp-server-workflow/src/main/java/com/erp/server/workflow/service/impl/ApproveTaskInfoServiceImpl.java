package com.erp.server.workflow.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.workflow.dto.ApproveTaskDetailDTO;
import com.erp.model.workflow.dto.ApproveTaskInfoDTO;
import com.erp.model.workflow.dto.ProcessDelegateDTO;
import com.erp.model.workflow.entity.ApproveTaskDetailEntity;
import com.erp.model.workflow.entity.ApproveTaskInfoEntity;
import com.erp.model.workflow.entity.CfgQueryOptionEntity;
import com.erp.model.workflow.enums.ApproveTaskStatusEnum;
import com.erp.model.workflow.enums.ApproveTaskTypeEnum;
import com.erp.model.workflow.enums.CfgProcessRuleTypeEnum;
import com.erp.model.workflow.enums.CfgQueryOptionFieldTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.workflow.mapper.ApproveTaskInfoMapper;
import com.erp.server.workflow.service.ApproveTaskDetailService;
import com.erp.server.workflow.service.ApproveTaskInfoService;
import com.erp.server.workflow.service.CfgQueryOptionService;
import com.erp.server.workflow.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_APPROVE_TASK;

/**
 * <p>
 * 三方生成查询 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-05-27
 */
@Slf4j
@Service
public class ApproveTaskInfoServiceImpl extends SuperServiceImpl<ApproveTaskInfoMapper, ApproveTaskInfoEntity> implements ApproveTaskInfoService {
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private ApproveTaskDetailService approveTaskDetailService;
    @Autowired
    private CfgQueryOptionService cfgQueryOptionService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ApproveTaskInfoDTO.AddDTO addDTO) {
        ApproveTaskInfoEntity approveTaskInfoEntity = new ApproveTaskInfoEntity();
        BeanMapperUtils.copy(addDTO, approveTaskInfoEntity);

        // 数据处理
        handleData(approveTaskInfoEntity);

        log.info("开始新增三方生成查询");
        boolean save = super.save(approveTaskInfoEntity);
        if(!save) {
            throw new ServiceException("三方生成查询保存失败");
        }

        //添加明细数据
        approveTaskDetailService.add(addDTO.getDetailList(), approveTaskInfoEntity.getId());

        return new BaseResultDTO.AddDTO(approveTaskInfoEntity.getId(), approveTaskInfoEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ApproveTaskInfoDTO.UpdateDTO addOrUpdateDTO) {
        ApproveTaskInfoEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "三方生成查询"));
        ApproveTaskInfoEntity approveTaskInfoEntity =  BeanMapperUtils.map(ApproveTaskInfoEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(approveTaskInfoEntity);
        boolean save = super.updateById(approveTaskInfoEntity);
        if(!save) {
            throw new ServiceException("三方生成查询保存失败");
        }
        approveTaskDetailService.update(addOrUpdateDTO.getDetailList(),approveTaskInfoEntity.getId());
        return Boolean.TRUE;
    }

    @Override
    public List<ApproveTaskInfoDTO.TabListDTO> tabList(PermissionsDTO param) {
        ProcessDelegateDTO.PagingParamDTO searchParam = new ProcessDelegateDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<ApproveTaskInfoDTO.TabListDTO> tabList = this.baseMapper.tabList(searchParam);
        Map<String, ApproveTaskInfoDTO.TabListDTO> map = CollUtil.isEmpty(tabList) ? new HashMap<>() : tabList.stream().collect(Collectors.toMap(ApproveTaskInfoDTO.TabListDTO::getTabFlag, Function.identity()));
        ApproveTaskStatusEnum[] values = ApproveTaskStatusEnum.values();
        List<ApproveTaskInfoDTO.TabListDTO> list = new ArrayList<>();
        for (ApproveTaskStatusEnum item : values) {
            ApproveTaskInfoDTO.TabListDTO resultDTO = new ApproveTaskInfoDTO.TabListDTO();
            ApproveTaskInfoDTO.TabListDTO tabListDTO = map.get(item.getCode());
            resultDTO.setCount(ObjectUtil.isEmpty(tabListDTO) ? MathUtil.ZERO : tabListDTO.getCount());
            resultDTO.setTabFlag(item.getCode());
            resultDTO.setTabFlagName(item.getName());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    public PagingVO<ApproveTaskInfoDTO.ListDTO> paging(PagingDTO<ApproveTaskInfoDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page<>(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<ApproveTaskInfoDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public ApproveTaskInfoDTO.ViewDTO view(String id) {
        ApproveTaskInfoEntity entity = this.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.PROCESS_APPROVE_TASK_NOT_EXIST);
        }
        ApproveTaskInfoDTO.ViewDTO viewDTO = BeanMapperUtils.map(ApproveTaskInfoDTO.ViewDTO.class, entity);
        //来源平台
        viewDTO.setSourcePlatformName(CfgProcessRuleTypeEnum.getName(viewDTO.getSourcePlatform()));
        //流程类型
        viewDTO.setTypeName(ApproveTaskTypeEnum.getName(viewDTO.getType()));
        //执行状态
        viewDTO.setStatusName(ApproveTaskStatusEnum.getName(viewDTO.getStatus()));
        //数大臣单据名称
        viewDTO.setBussinessKeyName(SourceTypeEnum.getName(viewDTO.getBussinessKey()));
        //明细
        List<ApproveTaskDetailEntity> approveTaskDetailList = approveTaskDetailService.listByMainId(id);
        if (CollUtil.isEmpty(approveTaskDetailList)) {
            throw new ServiceException(ApiError.PROCESS_APPROVE_TASK_DETAIL_NOT_EXIST);
        }
        List<ApproveTaskDetailDTO.ViewDTO> viewDetailList = BeanUtil.copyToList(approveTaskDetailList, ApproveTaskDetailDTO.ViewDTO.class);
        Map<String, List<ApproveTaskDetailDTO.ViewDTO>> detailMap = viewDetailList.stream().collect(Collectors.groupingBy(ApproveTaskDetailDTO.ViewDTO::getEntityCode));


        List<String> sysFieldList = approveTaskDetailList.stream().map(ApproveTaskDetailEntity::getSysField).distinct().collect(Collectors.toList());
        List<CfgQueryOptionEntity> cfgQueryOptionList = cfgQueryOptionService.listBySysFieldList(entity.getBussinessKey(), sysFieldList);
        Map<String, CfgQueryOptionEntity> cfgQueryOptionMap = CollUtil.isEmpty(cfgQueryOptionList) ? new HashMap<>() : cfgQueryOptionList.stream().collect(Collectors.toMap(CfgQueryOptionEntity::getConditionField, Function.identity()));

        List<ApproveTaskInfoDTO.ViewDetailDTO> detailList = new ArrayList<>();
        for (Map.Entry<String, List<ApproveTaskDetailDTO.ViewDTO>> entry : detailMap.entrySet()) {
            String key = entry.getKey();
            List<ApproveTaskDetailDTO.ViewDTO> value = entry.getValue();
            for (ApproveTaskDetailDTO.ViewDTO detailDTO : value) {
                //第三方类型名称
                detailDTO.setThirdFieldTypeName(CfgQueryOptionFieldTypeEnum.getName(detailDTO.getThirdFieldType()));
                //数大臣类型名称
                detailDTO.setSysFieldTypeName(CfgQueryOptionFieldTypeEnum.getName(detailDTO.getSysFieldType()));
                //数大臣单据字段信息
                detailDTO.setCfgQueryOptionEntity(cfgQueryOptionMap.get(detailDTO.getSysField()));
            }
            ApproveTaskInfoDTO.ViewDetailDTO viewDetailDTO = new ApproveTaskInfoDTO.ViewDetailDTO();
            String entityName = CharSequenceUtil.isBlank(key) ? "基础信息" : value.get(0).getEntityName();
            viewDetailDTO.setEntityName(entityName);
            viewDetailDTO.setDetailList(value);
            detailList.add(viewDetailDTO);
        }
        viewDTO.setDetailList(detailList);
        return viewDTO;
    }

    @Override
    public void exportList(ApproveTaskInfoDTO.PagingParamDTO param) {
        downloadTaskFeign.saveDownloadTask("三方生成查询导出", EXPORT_APPROVE_TASK.getCode(), param);
    }

    @Override
    public BatchResultDTO afreshGenerate(String id) {
        ApproveTaskInfoEntity entity = this.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.PROCESS_APPROVE_TASK_NOT_EXIST);
        }
        return BatchResultDTO.success(entity.getId(), entity.getBussinessCode(), OperationTypeEnum.REGENERATE);
    }

    /**
     * 分页查询数据处理
     * @author will
     * @date 2025/5/27 10:56
     * @param list
     * @return void
     */
    private void fillList(List<ApproveTaskInfoDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        for (ApproveTaskInfoDTO.ListDTO listDTO : list) {
            //来源平台
            listDTO.setSourcePlatformName(CfgProcessRuleTypeEnum.getName(listDTO.getSourcePlatform()));
            //流程类型
            listDTO.setTypeName(ApproveTaskTypeEnum.getName(listDTO.getType()));
            //执行状态
            listDTO.setStatusName(ApproveTaskStatusEnum.getName(listDTO.getStatus()));
            //数大臣单据名称
            listDTO.setBussinessKeyName(SourceTypeEnum.getName(listDTO.getBussinessKey()));
        }
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(ApproveTaskInfoEntity approveTaskInfoEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
