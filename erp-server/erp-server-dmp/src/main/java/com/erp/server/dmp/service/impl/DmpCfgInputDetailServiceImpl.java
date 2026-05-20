package com.erp.server.dmp.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.DmpInputFeignDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.DmpCfgInputDetailDTO;
import com.erp.model.dmp.dto.DmpInoutDTO;
import com.erp.model.dmp.entity.DmpBasicSystemEntity;
import com.erp.model.dmp.entity.DmpCfgInputDetailEntity;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.erp.model.dmp.entity.DmpCfgOutputDetailEntity;
import com.erp.model.dmp.enums.DmpCfgInputExecSystemEnum;
import com.erp.model.dmp.enums.DmpInputTaskTaskTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.dmp.inout.dto.request.DmpInputHotfixCreateRequest;
import com.erp.server.dmp.inout.handler.factory.DmpInputCreateFactory;
import com.erp.server.dmp.mapper.DmpCfgInputDetailMapper;
import com.erp.server.dmp.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
/**
 * <p>
 * 拉取调度 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2025-10-23
 */
@Slf4j
@Service
public class DmpCfgInputDetailServiceImpl extends SuperServiceImpl<DmpCfgInputDetailMapper, DmpCfgInputDetailEntity> implements DmpCfgInputDetailService {

   @Resource
   private DmpCfgOutputDetailService dmpCfgOutputDetailService;

    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private DmpBasicSystemService dmpBasicSystemService;
    @Resource
    private DmpCfgInputService dmpCfgInputService;
    @Resource
    private DmpInputCreateFactory dmpInputCreateFactory;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpCfgInputDetailDTO.AddDTO addDTO) {
        DmpCfgInputDetailEntity dmpCfgInputDetailEntity = new DmpCfgInputDetailEntity();
        BeanMapperUtils.copy(addDTO, dmpCfgInputDetailEntity);

        // 数据处理
        handleData(dmpCfgInputDetailEntity, addDTO);

        log.info("开始新增拉取调度");
        boolean save = super.save(dmpCfgInputDetailEntity);
        if(!save) {
            throw new ServiceException("拉取调度保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "拉取调度" , dmpCfgInputDetailEntity.getId());
        //  此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        //  新增明细（如果有明细的话）
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_CFG_INPUT_DETAIL.getCode(), dmpCfgInputDetailEntity.getId(), "新增【拉取调度】数据");

        return new BaseResultDTO.AddDTO(dmpCfgInputDetailEntity.getId(), dmpCfgInputDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpCfgInputDetailDTO.UpdateDTO updateDTO) {
        DmpCfgInputDetailEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "拉取调度"));
        DmpCfgInputDetailEntity dmpCfgInputDetailEntity =  BeanMapperUtils.map(DmpCfgInputDetailEntity.class, updateDTO);

        DmpCfgInputEntity cfgInputEntity = dmpCfgInputService.getByIdOpt(old.getMainId()).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "拉取配置"));

        // 数据处理
        handleData(dmpCfgInputDetailEntity, updateDTO);
        log.info("编辑 开始修改拉取调度数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpCfgInputDetailEntity);
        if(!save) {
            throw new ServiceException("拉取调度保存失败");
        }

        // 记录主单操作日志
        log.info("编辑 开始记录拉取调度日志数据，id：【{}】", dmpCfgInputDetailEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑编号为【{}】的【{}】 ", UserContext.getDefaultLoginUser().getUserName(), cfgInputEntity.getCode(), "拉取调度");
        // 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpCfgInputDetailEntity, ModuleTypeEnum.DMP_CFG_INPUT_DETAIL.getCode(), dmpCfgInputDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<DmpInoutDTO.ListDTO> listBySystemCodeAndBillType(List<String> systemCodeList, List<String> billTypeList, List<String> nextLevelIdList) {
        return baseMapper.listBySystemCodeAndBillType(systemCodeList, billTypeList, nextLevelIdList);
    }

    @Override
    public void optionDmpCfgInputDetail(DmpInputFeignDTO.CfgOptionDTO cfgOptionDTO) {
        DmpCfgInputDetailEntity inputDetailEntity = optionCfgInputDetail(cfgOptionDTO);
        if (ObjUtil.isEmpty(inputDetailEntity)) {
            throw new ServiceException("飞书配置输入明细未找到，编码：" + cfgOptionDTO.getCode());
        }
        optionCfgOutputDetail(cfgOptionDTO, inputDetailEntity);
    }

    /**
     *  操作输出明细配置
     * @author will
     * @date 2025/11/24 10:51
     * @param cfgOptionDTO
     * @param detailEntity
     * @return void
     */
    private void optionCfgOutputDetail(DmpInputFeignDTO.CfgOptionDTO cfgOptionDTO, DmpCfgInputDetailEntity detailEntity) {
        //添加输出配置
        DmpCfgOutputDetailEntity outputDetailEntity = dmpCfgOutputDetailService.getDmpCfgOutputDetailByOption(detailEntity.getMainId(),detailEntity.getNextLevelId());
        if (ObjUtil.isEmpty(outputDetailEntity)) {
            throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, CharSequenceUtil.format("{}平台{}编码输出配置不存在,请检查", cfgOptionDTO.getSystem(), cfgOptionDTO.getCode()));
        }
        if (OperationTypeEnum.ADD.getStatus().equals(cfgOptionDTO.getOption()) || OperationTypeEnum.UPDATE.getStatus().equals(cfgOptionDTO.getOption())) {
            outputDetailEntity.setNextLevelId(cfgOptionDTO.getNextLevelId());
            dmpCfgOutputDetailService.saveOrUpdate(outputDetailEntity);
            return;
        }
        if (OperationTypeEnum.DELETE.getStatus().equals(cfgOptionDTO.getOption())) {
            if(CharSequenceUtil.isBlank(detailEntity.getId())) {
                //删除时如果没有id则说明配置不存在，无需删除
                return;
            }
            //删除
            super.removeById(outputDetailEntity.getId());
        }
    }


    /**
     * 操作输入明细配置
     * @author will
     * @date 2025/11/24 10:50
     * @param cfgOptionDTO
     * @return DmpCfgInputDetailEntity
     */
    private DmpCfgInputDetailEntity optionCfgInputDetail(DmpInputFeignDTO.CfgOptionDTO cfgOptionDTO) {
        //需要添加的输入配置不存在则新增，存在则修改
        DmpCfgInputDetailEntity detailEntity = baseMapper.getDmpCfgInputDetailByOption(cfgOptionDTO);
        if (ObjUtil.isEmpty(detailEntity)) {
            throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, CharSequenceUtil.format("{}平台{}编码输入配置不存在,请检查", cfgOptionDTO.getSystem(), cfgOptionDTO.getCode()));
        }
        if (CharSequenceUtil.isBlank(detailEntity.getId())) {
            LocalDateTime now = LocalDateTime.now();
            detailEntity.setNextLevelId(cfgOptionDTO.getNextLevelId());
            detailEntity.setLastTime(now);
            detailEntity.setNextTime(now);
            detailEntity.setIntervalTime(600);
            detailEntity.setOverrideTime(0);
            detailEntity.setMaxRetryCount(3);
            detailEntity.setExecTimeout(1200);
            detailEntity.setTaskType(DmpInputTaskTaskTypeEnum.NORMAL.getCode());
            detailEntity.setMaxIntervalTime(3);
        }
        if (OperationTypeEnum.ADD.getStatus().equals(cfgOptionDTO.getOption()) || OperationTypeEnum.UPDATE.getStatus().equals(cfgOptionDTO.getOption())) {
            //新增或更新
            detailEntity.setDisabled(Boolean.FALSE);
            detailEntity.setNextLevelId(cfgOptionDTO.getNextLevelId());
            super.saveOrUpdate(detailEntity);
            return detailEntity;
        }
        if (OperationTypeEnum.DELETE.getStatus().equals(cfgOptionDTO.getOption())) {
            if(CharSequenceUtil.isBlank(detailEntity.getId())) {
                //删除时如果没有id则说明配置不存在，无需删除
                return detailEntity;
            }
            //删除
            super.removeById(detailEntity.getId());
        }
        return detailEntity;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(DmpCfgInputDetailEntity dmpCfgInputDetailEntity, DmpCfgInputDetailDTO.CommonDTO updateDTO) {
        // 验证数据 & 数据赋值
        String extendJson = StringUtils.trim(updateDTO.getExtendJson());
        if (StringUtils.isBlank(extendJson)) {
            dmpCfgInputDetailEntity.setExtendJson("{}");
        } else {
            try {
                JSON.parse(extendJson);
            } catch (Exception e) {
                ServiceException.runError("【拓展json】不是合法的JSON格式");
            }
            dmpCfgInputDetailEntity.setExtendJson(extendJson);
        }
    }

    @Override
    public PagingVO<DmpCfgInputDetailDTO.ListDTO> paging(PagingDTO<DmpCfgInputDetailDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<DmpCfgInputDetailDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<DmpCfgInputDetailDTO.TabListDTO> tabList(PermissionsDTO param) {
        DmpCfgInputDetailDTO.PagingParamDTO searchParam = new DmpCfgInputDetailDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<DmpCfgInputDetailDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        List<DmpCfgInputDetailDTO.TabListDTO> resultList = new LinkedList<>();
//        resultList.add(new DmpCfgInputDetailDTO.TabListDTO("all", "全部", list.stream().mapToInt(DmpCfgInputDetailDTO.TabListDTO::getCount).sum()));
        resultList.addAll(list);
        List<String> existStatusList = list.stream().map(DmpCfgInputDetailDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        List<String> tabList = Arrays.asList("f", "t");
        tabList.forEach(status -> {
            if (!existStatusList.contains(status)) {
                resultList.add(new DmpCfgInputDetailDTO.TabListDTO(status, "t".equals(status) ? "停用" : "启用", 0));
            }
        });
        return resultList;
    }

    @Override
    public void exportList(DmpCfgInputDetailDTO.ExportDTO dto, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("拉取调度Excel导出", FileTaskEventEnum.EXPORT_DMP_CFG_INPUT_DETAIL.getCode(), dto);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        DmpCfgInputDetailEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到拉取调度数据"));
        // 只有待提交数据允许删除
        // TODO 删除明细数据（如果有明细数据的话）

        // 删除主单数据
        log.info("删除 开始删除拉取调度主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除拉取调度日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getId(), "拉取调度");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_CFG_OUTPUT_DETAIL.getCode(), entity.getId(), "删除拉取调度数据");
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.DELETE);
    }


    @Override
    public DmpCfgInputDetailDTO.ViewDTO view(String id) {
        DmpCfgInputDetailEntity dmpCfgInputDetailEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到拉取调度数据"));
        DmpCfgInputDetailDTO.ViewDTO data = BeanMapperUtils.map(DmpCfgInputDetailDTO.ViewDTO.class, dmpCfgInputDetailEntity);
        // 数据填充处理
        fillOne(data);
        // TODO 查询明细数据（如果有的话）
        return data;
    }

    private void fillOne(DmpCfgInputDetailDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        DmpCfgInputEntity inputEntity = dmpCfgInputService.getById(data.getMainId());
        if (null != inputEntity){
            data.setName(inputEntity.getName());
        }
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<DmpCfgInputDetailDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        List<String> systemIds = list.stream().map(DmpCfgInputDetailDTO.ListDTO::getSystemId).distinct().collect(Collectors.toList());
        Map<String, DmpBasicSystemEntity> systemMap = dmpBasicSystemService.lambdaQuery()
                .in(DmpBasicSystemEntity::getId, systemIds)
                .list()
                .stream()
                .collect(Collectors.toMap(DmpBasicSystemEntity::getId, Function.identity(), (v1, v2) -> v1));
        for (DmpCfgInputDetailDTO.ListDTO data : list) {
            DmpBasicSystemEntity systemEntity = systemMap.get(data.getSystemId());
            if (null != systemEntity) {
                data.setSystemCode(systemEntity.getCode());
                data.setSystemName(systemEntity.getName());
            }
            data.setTaskTypeName(DmpInputTaskTaskTypeEnum.getName(data.getTaskType()));
            data.setDisabledDesc(data.getDisabled() ? "停用":"启用");
        }
    }

    @Override
    public BatchResultDTO enable(DmpCfgInputDetailEntity entity) {
        if (entity.getDisabled()) {
            entity.setDisabled(false);
            updateById(entity);
            String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】启用操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getId(), "拉取调度");
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_CFG_OUTPUT_DETAIL.getCode(), entity.getId(), "启用【拉取调度】数据");
        } else {
            ServiceException.runError("该【拉取调度】数据已启用，无需重复操作");
        }
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.UPDATE);
    }

    @Override
    public BatchResultDTO disable(DmpCfgInputDetailEntity entity) {
        if (!entity.getDisabled()) {
            entity.setDisabled(true);
            updateById(entity);
            String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】禁用操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getId(), "拉取调度");
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_CFG_OUTPUT_DETAIL.getCode(), entity.getId(), "禁用【拉取调度】数据");
        } else {
            ServiceException.runError("该【拉取调度】数据已禁用，无需重复操作");
        }
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.UPDATE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO doTask(String id, DmpCfgInputDetailDTO.DoTaskDTO dto, DmpCfgInputEntity dmpCfgInputEntity, DmpCfgInputDetailEntity entity) {
        if (DmpCfgInputExecSystemEnum.DMP.getCode().equals(dmpCfgInputEntity.getExecSystem())){
            // 中台执行
            DmpInputHotfixCreateRequest dmpInputHotfixCreateRequest = buildDmpInputHotfixCreateRequest(dto, dmpCfgInputEntity, entity);
            dmpInputCreateFactory.doHotfixInputTask(dmpInputHotfixCreateRequest);
        } else if (DmpCfgInputExecSystemEnum.REST_CLOUD.getCode().equals(dmpCfgInputEntity.getExecSystem())){
            // RestCloud执行
            boolean restCloudCanRun = Arrays.asList(DmpInputTaskTaskTypeEnum.NORMAL.getCode(), DmpInputTaskTaskTypeEnum.HISTORY.getCode()).contains(dto.getTaskType());
            if (!restCloudCanRun){
                return BatchResultDTO.fail(id, id, "RestCloud执行系统只支持普通任务和历史任务，当前任务类型：" + DmpInputTaskTaskTypeEnum.getName(dto.getTaskType()));
            }
            DmpInputHotfixCreateRequest dmpInputHotfixCreateRequest = buildDmpInputHotfixCreateRequest(dto, dmpCfgInputEntity, entity);
            dmpInputCreateFactory.createHotfixInputTask(dmpInputHotfixCreateRequest);
        } else {
            return BatchResultDTO.fail(id, id,"不支持的执行系统类型：" + dmpCfgInputEntity.getExecSystem());
        }
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】生成任务操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getId(), "拉取任务");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_INPUT_TASK.getCode(), entity.getId(), "生成【拉取任务】数据");
        return BatchResultDTO.success(id, id, "生成拉取任务成功");
    }

    private static DmpInputHotfixCreateRequest buildDmpInputHotfixCreateRequest(DmpCfgInputDetailDTO.DoTaskDTO dto,
                                                                                DmpCfgInputEntity dmpCfgInputEntity,
                                                                                DmpCfgInputDetailEntity entity) {
        if (!dto.getStartTime().isBefore(dto.getEndTime())){
            ServiceException.runError("结束时间不能小于开始时间");
        }
        DmpInputHotfixCreateRequest dmpInputHotfixCreateRequest = new DmpInputHotfixCreateRequest();
        dmpInputHotfixCreateRequest.setCfgInputId(dmpCfgInputEntity.getId());
        dmpInputHotfixCreateRequest.setCfgInputDetailIdList(Collections.singletonList(entity.getId()));
        dmpInputHotfixCreateRequest.setTaskType(dto.getTaskType());
        dmpInputHotfixCreateRequest.setDetailExtendJson(dto.getCheckAndDetailExtendJson());
        dmpInputHotfixCreateRequest.setStartTime(dto.getStartTime());
        dmpInputHotfixCreateRequest.setEndTime(dto.getEndTime());
        dmpInputHotfixCreateRequest.setExecTimeout(null == entity.getExecTimeout() ? dto.getExecTimeout() : entity.getExecTimeout());
        dmpInputHotfixCreateRequest.setSplitFlag(dto.isSplitFlag());
        dmpInputHotfixCreateRequest.setNextExecTime(dto.getNextExecTime());
        return dmpInputHotfixCreateRequest;
    }
}
