package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.DmpBasicSystemDTO;
import com.erp.model.dmp.dto.DmpCfgInputDTO;
import com.erp.model.dmp.entity.DmpBasicSystemEntity;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.plm.dto.DictControllerDTO;
import com.erp.model.scm.entity.ContractInfoEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.dmp.mapper.DmpBasicSystemMapper;
import com.erp.server.dmp.service.DmpBasicSystemService;
import com.erp.server.dmp.service.DmpCfgInputService;
import com.erp.server.dmp.service.DmpCfgOutputService;
import com.erp.server.dmp.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 平台管理 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2025-10-23
 */
@Slf4j
@Service
public class DmpBasicSystemServiceImpl extends SuperServiceImpl<DmpBasicSystemMapper, DmpBasicSystemEntity> implements DmpBasicSystemService {
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DmpCfgInputService dmpCfgInputService;
    @Resource
    private DmpCfgOutputService dmpCfgOutputService;


    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpBasicSystemDTO.AddDTO addDTO) {
        DmpBasicSystemEntity dmpBasicSystemEntity = new DmpBasicSystemEntity();
        BeanMapperUtils.copy(addDTO, dmpBasicSystemEntity);

        Integer count = lambdaQuery().eq(DmpBasicSystemEntity::getCode, addDTO.getCode()).count();
        if (count > 0) {
            ServiceException.runError("平台编码已存在，请修改后重新添加");
        }

        // 数据处理
        handleData(dmpBasicSystemEntity);

        log.info("开始新增平台管理");
        boolean save = super.save(dmpBasicSystemEntity);
        if (!save) {
            throw new ServiceException("平台管理保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "平台管理", dmpBasicSystemEntity.getCode());
        // 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_BASIC_SYSTEM.getCode(), dmpBasicSystemEntity.getCode(), "新增平台管理数据");

        return new BaseResultDTO.AddDTO(dmpBasicSystemEntity.getId(), dmpBasicSystemEntity.getCode());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpBasicSystemDTO.UpdateDTO updateDTO) {
        DmpBasicSystemEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "平台管理"));
        DmpBasicSystemEntity dmpBasicSystemEntity = BeanMapperUtils.map(DmpBasicSystemEntity.class, updateDTO);

        // 数据处理
        handleData(dmpBasicSystemEntity);
        log.info("编辑 开始修改平台管理数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(dmpBasicSystemEntity);
        if (!save) {
            throw new ServiceException("平台管理保存失败");
        }
        // 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录平台管理日志数据，单号：【{}】", dmpBasicSystemEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpBasicSystemEntity.getCode(), "平台管理");
        // 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_BASIC_SYSTEM.getCode(), dmpBasicSystemEntity.getCode(), "更新平台管理数据");

        return Boolean.TRUE;
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(DmpBasicSystemEntity dmpBasicSystemEntity) {
        // 验证数据 & 数据赋值
    }

    @Override
    public List<DictControllerDTO.DictDropDownDTO> listDmpBasicSystem() {
        return baseMapper.listDmpBasicSystem();
    }

    @Override
    public DmpBasicSystemEntity listByCode(String code) {
        return lambdaQuery().eq(DmpBasicSystemEntity::getCode, code).last("LIMIT 1").one();
    }


    @Override
    public PagingVO<DmpBasicSystemDTO.ListDTO> paging(PagingDTO<DmpBasicSystemDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<DmpBasicSystemDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<DmpBasicSystemDTO.TabListDTO> tabList(PermissionsDTO param) {
        DmpBasicSystemDTO.PagingParamDTO searchParam = new DmpBasicSystemDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<DmpBasicSystemDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        List<DmpBasicSystemDTO.TabListDTO> resultList = new LinkedList<>();
        resultList.add(new DmpBasicSystemDTO.TabListDTO("all", "全部", list.stream().mapToInt(DmpBasicSystemDTO.TabListDTO::getCount).sum()));
        resultList.addAll(list);
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(DmpBasicSystemDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        List<String> tabList = Arrays.asList("f", "t");
        tabList.forEach(status -> {
            if (!existStatusList.contains(status)) {
                resultList.add(new DmpBasicSystemDTO.TabListDTO(status, "t".equals(status) ? "停用" : "启用", 0));
            }
        });
        // 计算合计数量
        return resultList;
    }

    @Override
    public void exportList(DmpBasicSystemDTO.ExportDTO param, HttpServletResponse response) {
        List<DmpBasicSystemDTO.ListDTO> list = this.baseMapper.listExport(param);
        if (CollUtil.isEmpty(list)) {
            return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/dmpBasicSystem.xlsx";
        String name = "平台管理导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        DmpBasicSystemEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到平台管理数据"));
        Integer inputCount = dmpCfgInputService.lambdaQuery().eq(DmpCfgInputEntity::getSystemId, id).count();
        if (inputCount > 0) {
            ServiceException.runError("该平台管理已被拉取配置使用，无法删除");
        }
        Integer outputCount = dmpCfgOutputService.lambdaQuery().eq(DmpCfgOutputEntity::getSystemId, id).count();
        if (outputCount > 0) {
            ServiceException.runError("该平台管理已被推送配置使用，无法删除");
        }

        // 删除主单数据
        log.info("删除 开始删除平台管理主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除平台管理日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "平台管理");
        operateLogService.addModuleOperateLog(msg, null, entity.getCode(), "删除平台管理数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }


    @Override
    public DmpBasicSystemDTO.ViewDTO view(String id) {
        DmpBasicSystemEntity dmpBasicSystemEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到平台管理数据"));
        DmpBasicSystemDTO.ViewDTO data = BeanMapperUtils.map(DmpBasicSystemDTO.ViewDTO.class, dmpBasicSystemEntity);
        // 数据填充处理
        fillOne(data);
        // TODO 查询明细数据（如果有的话）
        return data;
    }

    private void fillOne(DmpBasicSystemDTO.ViewDTO data) {

    }


    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<DmpBasicSystemDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }

    }

    @Override
    public BatchResultDTO enable(DmpBasicSystemEntity entity) {
        if (entity.getDisabled()) {
            entity.setDisabled(false);
            updateById(entity);
            // 日志
            String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】启用操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "平台管理");
            operateLogService.addModuleOperateLog(msg, null, entity.getCode(), "启用平台管理数据");
        } else {
            ServiceException.runError("该平台管理数据已启用，无需重复操作");
        }
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.UPDATE);
    }

    @Override
    public BatchResultDTO disable(DmpBasicSystemEntity entity) {
        if (!entity.getDisabled()) {
            entity.setDisabled(true);
            updateById(entity);
            // 日志
            String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】禁用操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "禁用管理");
            operateLogService.addModuleOperateLog(msg, null, entity.getCode(), "禁用平台管理数据");
        } else {
            ServiceException.runError("该平台管理数据已禁用，无需重复操作");
        }
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.UPDATE);
    }
}
