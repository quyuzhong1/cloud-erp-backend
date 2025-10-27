package com.erp.server.dmp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.*;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.StrUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.DmpCfgOutputDTO;
import com.erp.model.dmp.dto.DmpCfgOutputDTO;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.dmp.mapper.DmpCfgOutputMapper;
import com.erp.server.dmp.service.DmpCfgOutputService;
import com.erp.server.dmp.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
/**
 * <p>
 * 推送数据配置 服务实现类
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

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpCfgOutputDTO.AddDTO addDTO) {
        DmpCfgOutputEntity dmpCfgOutputEntity = new DmpCfgOutputEntity();
        BeanMapperUtils.copy(addDTO, dmpCfgOutputEntity);

        // 数据处理
        handleData(dmpCfgOutputEntity);

        log.info("开始新增推送数据配置");
        boolean save = super.save(dmpCfgOutputEntity);
        if(!save) {
            throw new ServiceException("推送数据配置保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "推送数据配置" , dmpCfgOutputEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpCfgOutputEntity.getId(), dmpCfgOutputEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpCfgOutputDTO.UpdateDTO updateDTO) {
        DmpCfgOutputEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "推送数据配置"));
        DmpCfgOutputEntity dmpCfgOutputEntity =  BeanMapperUtils.map(DmpCfgOutputEntity.class, updateDTO);

        // 数据处理
        handleData(dmpCfgOutputEntity);
        log.info("编辑 开始修改推送数据配置数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpCfgOutputEntity);
        if(!save) {
            throw new ServiceException("推送数据配置保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录推送数据配置日志数据，id：【{}】", dmpCfgOutputEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpCfgOutputEntity.getId(), "推送数据配置");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpCfgOutputEntity dmpCfgOutputEntity) {
    // TODO 验证数据 & 数据赋值
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
        resultList.add(new DmpCfgOutputDTO.TabListDTO("all", "全部", list.stream().mapToInt(DmpCfgOutputDTO.TabListDTO::getCount).sum()));
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
        DmpCfgOutputEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到推送数据配置数据"));
        // TODO 删除明细数据（如果有明细数据的话）

        // 删除主单数据
        log.info("删除 开始删除推送数据配置主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除推送数据配置日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getId(), "推送数据配置");
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "删除推送数据配置数据");
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.DELETE);
    }


    @Override
    public DmpCfgOutputDTO.ViewDTO view(String id) {
        DmpCfgOutputEntity dmpCfgOutputEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到推送数据配置数据"));
        DmpCfgOutputDTO.ViewDTO data = BeanMapperUtils.map(DmpCfgOutputDTO.ViewDTO.class, dmpCfgOutputEntity);
        // 数据填充处理
        fillOne(data);
        // TODO 查询明细数据（如果有的话）
        return data;
    }


    private void fillOne(DmpCfgOutputDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<DmpCfgOutputDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }

        // 属性赋值
        for(DmpCfgOutputDTO.ListDTO data : list) {
            // TODO 其他如需要显示名称的字段赋值
        }
    }

    @Override
    public BatchResultDTO enable(DmpCfgOutputEntity entity) {
        if (entity.getDisabled()) {
            entity.setDisabled(false);
            updateById(entity);
            String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】启用操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getId(), "推送配置");
            operateLogService.addModuleOperateLog(msg, null, entity.getId(), "启用【推送配置】数据");
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
            operateLogService.addModuleOperateLog(msg, null, entity.getId(), "禁用【推送配置】数据");
        } else {
            ServiceException.runError("该【推送配置】数据已禁用，无需重复操作");
        }
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.UPDATE);
    }
}
