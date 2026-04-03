package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.*;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.DmpCfgOutputDTO;
import com.erp.model.dmp.dto.DmpRestCloudDTO;
import com.erp.model.dmp.entity.DmpBasicSystemEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.enums.DmpCfgInputExecSystemEnum;
import com.erp.model.dmp.enums.DmpCfgOutputTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.dmp.inout.utils.DmpHandlerCache;
import com.erp.server.dmp.mapper.DmpCfgOutputMapper;
import com.erp.server.dmp.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
/**
 * <p>
 * 推送配置 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@Service
public class DmpCfgOutputServiceImpl extends SuperServiceImpl<DmpCfgOutputMapper, DmpCfgOutputEntity> implements DmpCfgOutputService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private DmpBasicSystemService dmpBasicSystemService;
    @Resource
    private DmpRestCloudService dmpRestCloudService;
    @Resource
    private DmpCfgInputConvertService dmpCfgInputConvertService;
    @Resource
    private DmpHandlerCache dmpHandlerCache;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpCfgOutputDTO.AddDTO addDTO) {
        DmpCfgOutputEntity dmpCfgOutputEntity = new DmpCfgOutputEntity();
        BeanMapperUtils.copy(addDTO, dmpCfgOutputEntity);

        // 数据处理
        handleData(dmpCfgOutputEntity, addDTO);

        log.info("开始新增推送配置");
        boolean save = super.save(dmpCfgOutputEntity);
        if(!save) {
            throw new ServiceException("推送配置保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "推送配置" , dmpCfgOutputEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_CFG_OUTPUT.getCode(), dmpCfgOutputEntity.getId(), "新增【推送配置】数据");

        return new BaseResultDTO.AddDTO(dmpCfgOutputEntity.getId(), dmpCfgOutputEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpCfgOutputDTO.UpdateDTO updateDTO) {
        DmpCfgOutputEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "推送配置"));
        DmpCfgOutputEntity dmpCfgOutputEntity =  BeanMapperUtils.map(DmpCfgOutputEntity.class, updateDTO);

        // 数据处理
        handleData(dmpCfgOutputEntity, updateDTO);
        log.info("编辑 开始修改推送配置数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpCfgOutputEntity);
        if(!save) {
            throw new ServiceException("推送配置保存失败");
        }

        // 记录主单操作日志
        log.info("编辑 开始记录推送配置日志数据，id：【{}】", dmpCfgOutputEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑编号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpCfgOutputEntity.getFlowCode(), "推送配置");
        //  此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpCfgOutputEntity, ModuleTypeEnum.DMP_CFG_OUTPUT.getCode(), dmpCfgOutputEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpCfgOutputEntity dmpCfgOutputEntity, DmpCfgOutputDTO.CommonDTO commonDTO) {
        // 验证数据 & 数据赋值
        String extendJson = StringUtils.trim(commonDTO.getExtendJson());
        if (StringUtils.isBlank(extendJson)) {
            dmpCfgOutputEntity.setExtendJson("{}");
        } else {
            try {
                JSON.parse(extendJson);
            } catch (Exception e) {
                ServiceException.runError("【拓展json】不是合法的JSON格式");
            }
            dmpCfgOutputEntity.setExtendJson(extendJson);
        }
        if(DmpCfgInputExecSystemEnum.DMP.getCode().equals(dmpCfgOutputEntity.getExecSystem())){
            if (StringUtils.isNotBlank(commonDTO.getInputConvertId())){
                dmpCfgOutputEntity.setInputConvertId(commonDTO.getInputConvertId());
            } else {
                if (StringUtils.isNotBlank(commonDTO.getInputConvertType())){
                    List<DmpCfgInputConvertEntity> inputConvertEntityList = dmpHandlerCache.getDmpCfgInputConvertEntityList(d -> true);
                    DmpCfgInputConvertEntity dmpCfgInputConvertEntity = inputConvertEntityList.stream()
                            .filter(e -> e.getType().equals(commonDTO.getInputConvertType()))
                            .findFirst()
                            .orElseThrow(() -> new ServiceException("DMP执行系统时，未找到配置的转内数据配置"));
                    dmpCfgOutputEntity.setInputConvertId(dmpCfgInputConvertEntity.getId());
                } else {
                    throw new ServiceException("DMP执行系统时，转内数据id或转内数据名称其一不能为空");
                }
            }
        } else if (DmpCfgInputExecSystemEnum.REST_CLOUD.getCode().equals(dmpCfgOutputEntity.getExecSystem())) {
            if (StringUtils.isBlank(commonDTO.getExecUrl())){
                throw new ServiceException("RestCloud执行系统时，执行路径不能为空");
            }
            DmpRestCloudDTO.PagingParamDTO paramDTO = new DmpRestCloudDTO.PagingParamDTO();
            // 判断ExecUrl是否有/？
            if (commonDTO.getExecUrl().startsWith("/")){
                paramDTO.setFlowUrl(commonDTO.getExecUrl());
            } else {
                paramDTO.setFlowUrl("/" + commonDTO.getExecUrl());
            }
            paramDTO.setTaskCfgType("output");
            PagingDTO<DmpRestCloudDTO.PagingParamDTO> dto = new PagingDTO<>();
            dto.setCurrPage(1);
            dto.setPageSize(1);
            dto.setParams(paramDTO);
            PagingVO<DmpRestCloudDTO.ListDTO> pagingVO = dmpRestCloudService.flowPaging(dto);
            if (CollectionUtils.isNotEmpty(pagingVO.getList())) {
                DmpRestCloudDTO.ListDTO flowData = pagingVO.getList().get(0);
                dmpCfgOutputEntity.setFlowName(flowData.getFlowName());
                dmpCfgOutputEntity.setFlowCode(flowData.getFlowCode());
                dmpCfgOutputEntity.setAppId(flowData.getAppId());
                dmpCfgOutputEntity.setExecUrl(flowData.getExecUrl());
            } else {
                throw new ServiceException("未找到对应的restCloud流程配置，请检查执行路径是否正确");
            }
        }
    }

    @Override
    public PagingVO<DmpCfgOutputDTO.ListDTO> paging(PagingDTO<DmpCfgOutputDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<DmpCfgOutputDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<DmpCfgOutputDTO.TabListDTO> tabList(PermissionsDTO param) {
        DmpCfgOutputDTO.PagingParamDTO searchParam = new DmpCfgOutputDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<DmpCfgOutputDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        List<DmpCfgOutputDTO.TabListDTO> resultList = new LinkedList<>();
//        resultList.add(new DmpCfgOutputDTO.TabListDTO("all", "全部", list.stream().mapToInt(DmpCfgOutputDTO.TabListDTO::getCount).sum()));
        resultList.addAll(list);
        List<String> existStatusList = list.stream().map(DmpCfgOutputDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        List<String> tabList = Arrays.asList("f", "t");
        tabList.forEach(status -> {
            if (!existStatusList.contains(status)) {
                resultList.add(new DmpCfgOutputDTO.TabListDTO(status, "t".equals(status) ? "停用" : "启用", 0));
            }
        });
        return resultList;
    }

    @Override
    public void exportList(DmpCfgOutputDTO.ExportDTO dto, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("推送配置Excel导出", FileTaskEventEnum.EXPORT_DMP_CFG_OUTPUT.getCode(), dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        DmpCfgOutputEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到推送配置数据"));
        // TODO 删除明细数据（如果有明细数据的话）

        // 删除主单数据
        log.info("删除 开始删除推送配置主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除推送配置日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getId(), "推送配置");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_CFG_OUTPUT.getCode(), entity.getId(), "删除推送配置数据");
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.DELETE);
    }


    @Override
    public DmpCfgOutputDTO.ViewDTO view(String id) {
        DmpCfgOutputEntity dmpCfgOutputEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到推送配置数据"));
        DmpCfgOutputDTO.ViewDTO data = BeanMapperUtils.map(DmpCfgOutputDTO.ViewDTO.class, dmpCfgOutputEntity);
        // 数据填充处理
        fillOne(data);
        return data;
    }


    private void fillOne(DmpCfgOutputDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        DmpBasicSystemEntity systemEntity = dmpBasicSystemService.getById(data.getSystemId());
        if (ObjectUtil.isEmpty(systemEntity)) {
            data.setSystemName("");
        } else {
            data.setSystemName(systemEntity.getName());
        }

        // restCloud获取流程信息
        if (DmpCfgInputExecSystemEnum.REST_CLOUD.getCode().equals(data.getExecSystem())) {
            if (StringUtils.isNotBlank(data.getExecUrl())) {
                DmpRestCloudDTO.PagingParamDTO paramDTO = new DmpRestCloudDTO.PagingParamDTO();
                // 判断ExecUrl是否有/？
                if (data.getExecUrl().startsWith("/")){
                    paramDTO.setFlowUrl(data.getExecUrl());
                } else {
                    paramDTO.setFlowUrl("/" + data.getExecUrl());
                }
                paramDTO.setTaskCfgType("output");
                PagingDTO<DmpRestCloudDTO.PagingParamDTO> dto = new PagingDTO<>();
                dto.setCurrPage(1);
                dto.setPageSize(1);
                dto.setParams(paramDTO);
                PagingVO<DmpRestCloudDTO.ListDTO> pagingVO = dmpRestCloudService.flowPaging(dto);
                if (CollectionUtils.isNotEmpty(pagingVO.getList())) {
                    DmpRestCloudDTO.ListDTO flowData = pagingVO.getList().get(0);
                    data.setFlowName(flowData.getFlowName());
                    data.setFlowCode(flowData.getFlowCode());
                    data.setFullName(flowData.getFullName());
                    data.setAppId(flowData.getAppId());
                }
            }
        }  else {
            // DMP
            DmpCfgInputConvertEntity inputConvertEntity = dmpCfgInputConvertService.getById(data.getInputConvertId());
            if (null != inputConvertEntity) {
                data.setInputConvertType(inputConvertEntity.getType());
                data.setInputConvertClass(inputConvertEntity.getConvertClass());
            }

        }
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<DmpCfgOutputDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }

        List<String> systemIds = list.stream().map(DmpCfgOutputDTO.ListDTO::getSystemId).distinct().collect(Collectors.toList());
        Map<String, String> systemMap = dmpBasicSystemService.lambdaQuery()
                .in(DmpBasicSystemEntity::getId, systemIds)
                .list()
                .stream()
                .collect(Collectors.toMap(DmpBasicSystemEntity::getId, DmpBasicSystemEntity::getName, (v1, v2) -> v1));

        // 属性赋值
        for(DmpCfgOutputDTO.ListDTO data : list) {
            data.setTypeName(DmpCfgOutputTypeEnum.getName(data.getType()));
            data.setSystemName(systemMap.getOrDefault(data.getSystemId(), ""));
            data.setDisabledDesc(data.getDisabled() ? "停用":"启用");
        }
    }

    @Override
    public BatchResultDTO enable(DmpCfgOutputEntity entity) {
        if (entity.getDisabled()) {
            entity.setDisabled(false);
            updateById(entity);
            String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】启用操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getId(), "推送配置");
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_CFG_OUTPUT.getCode(), entity.getId(), "启用【推送配置】数据");
        } else {
            ServiceException.runError("该【推送配置】数据已启用，无需重复操作");
        }
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.UPDATE);
    }

    @Override
    public BatchResultDTO disable(DmpCfgOutputEntity entity) {
        if (!entity.getDisabled()) {
            entity.setDisabled(true);
            updateById(entity);
            String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】禁用操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getId(), "推送配置");
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_CFG_OUTPUT.getCode(), entity.getId(), "禁用【推送配置】数据");
        } else {
            ServiceException.runError("该【推送配置】数据已禁用，无需重复操作");
        }
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.UPDATE);
    }

    @Override
    public DmpCfgOutputEntity viewEntity(String id) {
        return this.getById(id);
    }

    @Override
    public PagingVO<DmpCfgOutputDTO.ListDmpCfgOutputDTO> searchPaging(PagingDTO<DmpCfgOutputDTO.SimplePagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page<?> query = new Page<>(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<DmpCfgOutputDTO.ListDmpCfgOutputDTO> pageData = this.baseMapper.searchPaging(query, pagingParamDTO.getParams());
        return new PagingVO<>(pageData);
    }
}
