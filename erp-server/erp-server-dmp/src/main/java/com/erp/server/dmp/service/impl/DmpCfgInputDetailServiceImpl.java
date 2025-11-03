package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.*;
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
import com.erp.model.dmp.enums.DmpInputTaskTaskTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.dmp.mapper.DmpCfgInputDetailMapper;
import com.erp.server.dmp.service.DmpBasicSystemService;
import com.erp.server.dmp.service.DmpCfgInputDetailService;
import com.erp.server.dmp.service.DmpCfgInputService;
import com.erp.server.dmp.service.OperateLogService;
import com.fasterxml.jackson.core.JsonParser;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
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
    private OperateLogService operateLogService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private DmpBasicSystemService dmpBasicSystemService;
    @Resource
    private DmpCfgInputService dmpCfgInputService;

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
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "拉取调度"));
        DmpCfgInputDetailEntity dmpCfgInputDetailEntity =  BeanMapperUtils.map(DmpCfgInputDetailEntity.class, updateDTO);

        // 数据处理
        handleData(dmpCfgInputDetailEntity, updateDTO);
        log.info("编辑 开始修改拉取调度数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpCfgInputDetailEntity);
        if(!save) {
            throw new ServiceException("拉取调度保存失败");
        }

        // 记录主单操作日志
            log.info("编辑 开始记录拉取调度日志数据，id：【{}】", dmpCfgInputDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpCfgInputDetailEntity.getId(), "拉取调度");
        // 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_CFG_OUTPUT_DETAIL.getCode(), dmpCfgInputDetailEntity.getId(), "更新【拉取调度】数据");
        return Boolean.TRUE;
    }

    @Override
    public List<DmpInoutDTO.ListDTO> listBySystemCodeAndBillType(List<String> systemCodeList, List<String> billTypeList, List<String> nextLevelIdList) {
        return baseMapper.listBySystemCodeAndBillType(systemCodeList, billTypeList, nextLevelIdList);
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpCfgInputDetailEntity dmpCfgInputDetailEntity, DmpCfgInputDetailDTO.CommonDTO updateDTO) {
        // 验证数据 & 数据赋值
        if (StringUtils.isBlank(updateDTO.getExtendJson())) {
            dmpCfgInputDetailEntity.setExtendJson("{}");
        } else {
            // 校验是否json格式
            if (!JSON.isValid(updateDTO.getExtendJson())) {
                throw new RuntimeException("extendJson 不是合法的 JSON 格式");
            }
            dmpCfgInputDetailEntity.setExtendJson(updateDTO.getExtendJson());
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
}
