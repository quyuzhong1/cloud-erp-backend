package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
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
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.DmpCfgInputDTO;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.dmp.mapper.DmpCfgInputMapper;
import com.erp.server.dmp.service.DmpCfgInputService;
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
 * 输入信息 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2025-10-23
 */
@Slf4j
@Service
public class DmpCfgInputServiceImpl extends SuperServiceImpl<DmpCfgInputMapper, DmpCfgInputEntity> implements DmpCfgInputService {
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpCfgInputDTO.AddDTO addDTO) {
        DmpCfgInputEntity dmpCfgInputEntity = new DmpCfgInputEntity();
        BeanMapperUtils.copy(addDTO, dmpCfgInputEntity);

        // 数据处理
        handleData(dmpCfgInputEntity);

        log.info("开始新增输入信息");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        dmpCfgInputEntity.setCode(code);
        boolean save = super.save(dmpCfgInputEntity);
        if(!save) {
            throw new ServiceException("输入信息保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "输入信息" , dmpCfgInputEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpCfgInputEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpCfgInputDTO.UpdateDTO updateDTO) {
        DmpCfgInputEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "输入信息"));
        DmpCfgInputEntity dmpCfgInputEntity =  BeanMapperUtils.map(DmpCfgInputEntity.class, updateDTO);

        // 数据处理
        handleData(dmpCfgInputEntity);
        log.info("编辑 开始修改输入信息数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(dmpCfgInputEntity);
        if(!save) {
            throw new ServiceException("输入信息保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录输入信息日志数据，单号：【{}】", dmpCfgInputEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpCfgInputEntity.getCode(), "输入信息");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpCfgInputEntity dmpCfgInputEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public List<DmpCfgInputDTO.ListDmpCfgInputDTO> listDmpCfgInput(String id) {
        return baseMapper.listDmpCfgInput(id);
    }

    @Override
    public List<String> listBySystemIdAndTaskType(String systemId, List<String> taskTypeList) {
        return baseMapper.listBySystemIdAndTaskType(systemId, taskTypeList);
    }

    @Override
    public List<DmpCfgInputDTO.ListDmpCfgInputDTO> allDmpCfgInput() {
        List<DmpCfgInputEntity> list = lambdaQuery().select(DmpCfgInputEntity::getId, DmpCfgInputEntity::getName)
                .list();
        List<DmpCfgInputDTO.ListDmpCfgInputDTO> resultList = BeanMapperUtils.copyList(DmpCfgInputDTO.ListDmpCfgInputDTO.class, list);
        return resultList;
    }

    @Override
    public PagingVO<DmpCfgInputDTO.ListDTO> paging(PagingDTO<DmpCfgInputDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<DmpCfgInputDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<DmpCfgInputDTO.TabListDTO> tabList(PermissionsDTO param) {
        DmpCfgInputDTO.PagingParamDTO searchParam = new DmpCfgInputDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<DmpCfgInputDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        List<DmpCfgInputDTO.TabListDTO> resultList = new LinkedList<>();
//        resultList.add(new DmpCfgInputDTO.TabListDTO("all", "全部", list.stream().mapToInt(DmpCfgInputDTO.TabListDTO::getCount).sum()));
        resultList.addAll(list);
        List<String> existStatusList = list.stream().map(DmpCfgInputDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        List<String> tabList = Arrays.asList("f", "t");
        tabList.forEach(status -> {
            if (!existStatusList.contains(status)) {
                resultList.add(new DmpCfgInputDTO.TabListDTO(status, "t".equals(status) ? "停用" : "启用", 0));
            }
        });
        return resultList;
    }

    @Override
    public void exportList(DmpCfgInputDTO.ExportDTO dto, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("拉取配置Excel导出", FileTaskEventEnum.EXPORT_DMP_CFG_INPUT.getCode(), dto);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        DmpCfgInputEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到输入信息数据"));
        // TODO 删除明细数据（如果有明细数据的话）

        // 删除主单数据
        log.info("删除 开始删除输入信息主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除输入信息日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "输入信息");
        operateLogService.addModuleOperateLog(msg, null, entity.getCode(), "删除输入信息数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    @Override
    public DmpCfgInputDTO.ViewDTO view(String id) {
        DmpCfgInputEntity dmpCfgInputEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到输入信息数据"));
        DmpCfgInputDTO.ViewDTO data = BeanMapperUtils.map(DmpCfgInputDTO.ViewDTO.class, dmpCfgInputEntity);
        // 数据填充处理
        fillOne(data);
        // TODO 查询明细数据（如果有的话）
        return data;
    }

    private void fillOne(DmpCfgInputDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<DmpCfgInputDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }

        // 属性赋值
        for(DmpCfgInputDTO.ListDTO data : list) {
//            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            // TODO 其他如需要显示名称的字段赋值
        }
    }

    @Override
    public BatchResultDTO enable(DmpCfgInputEntity entity) {
        if (entity.getDisabled()) {
            entity.setDisabled(false);
            updateById(entity);
            String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】启用操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "拉取配置");
            operateLogService.addModuleOperateLog(msg, null, entity.getCode(), "启用【拉取配置】数据");
        } else {
            ServiceException.runError("该【拉取配置】数据已启用，无需重复操作");
        }
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.UPDATE);
    }

    @Override
    public BatchResultDTO disable(DmpCfgInputEntity entity) {
        if (!entity.getDisabled()) {
            entity.setDisabled(true);
            updateById(entity);
            String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】禁用操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "拉取配置");
            operateLogService.addModuleOperateLog(msg, null, entity.getCode(), "禁用【拉取配置】数据");
        } else {
            ServiceException.runError("该【拉取配置】数据已禁用，无需重复操作");
        }
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.UPDATE);
    }


}
