package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.erp.model.dmp.dto.DmpCfgInputDetailDTO;
import com.erp.model.dmp.dto.DmpCfgOutputDetailDTO;
import com.erp.model.dmp.entity.DmpBasicSystemEntity;
import com.erp.model.dmp.entity.DmpCfgOutputDetailEntity;
import com.erp.model.dmp.enums.DmpInputTaskTaskTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.dmp.mapper.DmpCfgOutputDetailMapper;
import com.erp.server.dmp.service.DmpBasicSystemService;
import com.erp.server.dmp.service.DmpCfgOutputDetailService;
import com.erp.server.dmp.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
/**
 * <p>
 * 推送调度 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@Service
public class DmpCfgOutputDetailServiceImpl extends SuperServiceImpl<DmpCfgOutputDetailMapper, DmpCfgOutputDetailEntity> implements DmpCfgOutputDetailService {
    
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private DmpBasicSystemService dmpBasicSystemService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpCfgOutputDetailDTO.AddDTO addDTO) {
        DmpCfgOutputDetailEntity dmpCfgOutputDetailEntity = new DmpCfgOutputDetailEntity();
        BeanMapperUtils.copy(addDTO, dmpCfgOutputDetailEntity);

        // 数据处理
        handleData(dmpCfgOutputDetailEntity);

        log.info("开始新增推送调度");
        boolean save = super.save(dmpCfgOutputDetailEntity);
        if(!save) {
            throw new ServiceException("推送调度保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "推送调度" , dmpCfgOutputDetailEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_CFG_OUTPUT_DETAIL.getCode(), dmpCfgOutputDetailEntity.getId(), "新增【推送调度】数据");

        return new BaseResultDTO.AddDTO(dmpCfgOutputDetailEntity.getId(), dmpCfgOutputDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpCfgOutputDetailDTO.UpdateDTO updateDTO) {
        DmpCfgOutputDetailEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "推送调度"));
        DmpCfgOutputDetailEntity dmpCfgOutputDetailEntity =  BeanMapperUtils.map(DmpCfgOutputDetailEntity.class, updateDTO);

        // 数据处理
        handleData(dmpCfgOutputDetailEntity);
        log.info("编辑 开始修改推送调度数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpCfgOutputDetailEntity);
        if(!save) {
            throw new ServiceException("推送调度保存失败");
        }

        // 记录主单操作日志
        log.info("编辑 开始记录推送调度日志数据，id：【{}】", dmpCfgOutputDetailEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpCfgOutputDetailEntity.getId(), "推送调度");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_CFG_OUTPUT_DETAIL.getCode(), dmpCfgOutputDetailEntity.getId(), "修改【推送调度】数据");
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpCfgOutputDetailEntity dmpCfgOutputDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }


    @Override
    public PagingVO<DmpCfgOutputDetailDTO.ListDTO> paging(PagingDTO<DmpCfgOutputDetailDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<DmpCfgOutputDetailDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<DmpCfgOutputDetailDTO.TabListDTO> tabList(PermissionsDTO param) {
        DmpCfgOutputDetailDTO.PagingParamDTO searchParam = new DmpCfgOutputDetailDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<DmpCfgOutputDetailDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        List<DmpCfgOutputDetailDTO.TabListDTO> resultList = new LinkedList<>();
//        resultList.add(new DmpCfgOutputDetailDTO.TabListDTO("all", "全部", list.stream().mapToInt(DmpCfgOutputDetailDTO.TabListDTO::getCount).sum()));
        resultList.addAll(list);
        List<String> existStatusList = list.stream().map(DmpCfgOutputDetailDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        List<String> tabList = Arrays.asList("f", "t");
        tabList.forEach(status -> {
            if (!existStatusList.contains(status)) {
                resultList.add(new DmpCfgOutputDetailDTO.TabListDTO(status, "t".equals(status) ? "停用" : "启用", 0));
            }
        });
        return resultList;
    }

    @Override
    public void exportList(DmpCfgOutputDetailDTO.ExportDTO dto, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("推送调度Excel导出", FileTaskEventEnum.EXPORT_DMP_CFG_OUTPUT_DETAIL.getCode(), dto);
    }



    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        DmpCfgOutputDetailEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到推送配置信息数据"));
        // 只有待提交数据允许删除
//        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
//            throw new ServiceException(ApiError.ERROR_98032);
//        }
        // TODO 删除明细数据（如果有明细数据的话）

        // 删除主单数据
        log.info("删除 开始删除推送配置信息主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除推送配置信息日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getId(), "推送配置信息");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_CFG_OUTPUT_DETAIL.getCode(), entity.getId(), "删除推送配置信息数据");
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.DELETE);
    }





    @Override
    public DmpCfgOutputDetailDTO.ViewDTO view(String id) {
        DmpCfgOutputDetailEntity dmpCfgEtlEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到推送配置信息数据"));
        DmpCfgOutputDetailDTO.ViewDTO data = BeanMapperUtils.map(DmpCfgOutputDetailDTO.ViewDTO.class, dmpCfgEtlEntity);
        // 数据填充处理
        fillOne(data);
        // TODO 查询明细数据（如果有的话）
        return data;
    }

    private void fillOne(DmpCfgOutputDetailDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
    }


    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<DmpCfgOutputDetailDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        List<String> systemIds = list.stream().map(DmpCfgOutputDetailDTO.ListDTO::getSystemId).distinct().collect(Collectors.toList());
        Map<String, DmpBasicSystemEntity> systemMap = dmpBasicSystemService.lambdaQuery()
                .in(DmpBasicSystemEntity::getId, systemIds)
                .list()
                .stream()
                .collect(Collectors.toMap(DmpBasicSystemEntity::getId, Function.identity(), (v1, v2) -> v1));
        for (DmpCfgOutputDetailDTO.ListDTO data : list) {
            DmpBasicSystemEntity systemEntity = systemMap.get(data.getSystemId());
            if (null != systemEntity) {
                data.setSystemCode(systemEntity.getCode());
                data.setSystemName(systemEntity.getName());
            }
        }

    }

    @Override
    public BatchResultDTO enable(DmpCfgOutputDetailEntity entity) {
        if (entity.getDisabled()) {
            entity.setDisabled(false);
            updateById(entity);
            // 可选：添加操作日志
            String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】启用操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getId(), "推送调度");
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_CFG_OUTPUT_DETAIL.getCode(), entity.getId(), "启用【推送调度】数据");
        } else {
            ServiceException.runError("该【推送调度】数据已启用，无需重复操作");
        }
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.UPDATE);
    }

    @Override
    public BatchResultDTO disable(DmpCfgOutputDetailEntity entity) {
        if (!entity.getDisabled()) {
            entity.setDisabled(true);
            updateById(entity);
            // 可选：添加操作日志
            String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】禁用操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getId(), "推送调度");
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DMP_CFG_OUTPUT_DETAIL.getCode(), entity.getId(), "禁用【推送调度】数据");
        } else {
            ServiceException.runError("该【推送调度】数据已禁用，无需重复操作");
        }
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.UPDATE);
    }
}
