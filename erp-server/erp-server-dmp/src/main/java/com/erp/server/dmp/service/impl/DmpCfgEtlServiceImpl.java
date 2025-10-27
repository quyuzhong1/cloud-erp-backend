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
import com.erp.model.dmp.dto.DmpCfgEtlDTO;
import com.erp.model.dmp.entity.DmpCfgEtlEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.dmp.mapper.DmpCfgEtlMapper;
import com.erp.server.dmp.service.DmpCfgEtlService;
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
 * etl配置信息 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2025-07-21
 */
@Slf4j
@Service
public class DmpCfgEtlServiceImpl extends SuperServiceImpl<DmpCfgEtlMapper, DmpCfgEtlEntity> implements DmpCfgEtlService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpCfgEtlDTO.AddDTO addDTO) {
        DmpCfgEtlEntity dmpCfgEtlEntity = new DmpCfgEtlEntity();
        BeanMapperUtils.copy(addDTO, dmpCfgEtlEntity);

        // 数据处理
        handleData(dmpCfgEtlEntity);

        log.info("开始新增etl配置信息");
        boolean save = super.save(dmpCfgEtlEntity);
        if(!save) {
            throw new ServiceException("etl配置信息保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "etl配置信息" , dmpCfgEtlEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpCfgEtlEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpCfgEtlEntity.getId(), dmpCfgEtlEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpCfgEtlDTO.UpdateDTO addOrUpdateDTO) {
        DmpCfgEtlEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "etl配置信息"));
        DmpCfgEtlEntity dmpCfgEtlEntity =  BeanMapperUtils.map(DmpCfgEtlEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(dmpCfgEtlEntity);
        log.info("编辑 开始修改etl配置信息数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpCfgEtlEntity);
        if(!save) {
            throw new ServiceException("etl配置信息保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录etl配置信息日志数据，id：【{}】", dmpCfgEtlEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpCfgEtlEntity.getId(), "etl配置信息");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpCfgEtlEntity, null, dmpCfgEtlEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpCfgEtlEntity dmpCfgEtlEntity) {
    // TODO 验证数据 & 数据赋值
    }


    @Override
    public PagingVO<DmpCfgEtlDTO.ListDTO> paging(PagingDTO<DmpCfgEtlDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<DmpCfgEtlDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<DmpCfgEtlDTO.TabListDTO> tabList(PermissionsDTO param) {
        DmpCfgEtlDTO.PagingParamDTO searchParam = new DmpCfgEtlDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<DmpCfgEtlDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        List<DmpCfgEtlDTO.TabListDTO> resultList = new LinkedList<>();
        resultList.add(new DmpCfgEtlDTO.TabListDTO("all", "全部", list.stream().mapToInt(DmpCfgEtlDTO.TabListDTO::getCount).sum()));
        resultList.addAll(list);
        List<String> existStatusList = list.stream().map(DmpCfgEtlDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        List<String> tabList = Arrays.asList("f", "t");
        tabList.forEach(status -> {
            if (!existStatusList.contains(status)) {
                resultList.add(new DmpCfgEtlDTO.TabListDTO(status, "t".equals(status) ? "停用" : "启用", 0));
            }
        });
        return resultList;
    }

    @Override
    public void exportList(DmpCfgEtlDTO.ExportDTO dto, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("清洗调度Excel导出", FileTaskEventEnum.EXPORT_DMP_CFG_ETL.getCode(), dto);
    }
    
    

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        DmpCfgEtlEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到etl配置信息数据"));
        // 只有待提交数据允许删除
//        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
//            throw new ServiceException(ApiError.ERROR_98032);
//        }
        // TODO 删除明细数据（如果有明细数据的话）

        // 删除主单数据
        log.info("删除 开始删除etl配置信息主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除etl配置信息日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getId(), "etl配置信息");
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "删除etl配置信息数据");
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.DELETE);
    }





    @Override
    public DmpCfgEtlDTO.ViewDTO view(String id) {
        DmpCfgEtlEntity dmpCfgEtlEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到etl配置信息数据"));
        DmpCfgEtlDTO.ViewDTO data = BeanMapperUtils.map(DmpCfgEtlDTO.ViewDTO.class, dmpCfgEtlEntity);
        // 数据填充处理
        fillOne(data);
        // TODO 查询明细数据（如果有的话）
        return data;
    }
   
    private void fillOne(DmpCfgEtlDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
    }
    

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<DmpCfgEtlDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }

        // 属性赋值
        for(DmpCfgEtlDTO.ListDTO data : list) {
            // TODO 其他如需要显示名称的字段赋值
        }
    }

    @Override
    public BatchResultDTO enable(DmpCfgEtlEntity entity) {
        if (entity.getDisabled()) {
            entity.setDisabled(false);
            updateById(entity);
            // 日志
            String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】启用操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getExecUrl(), "清洗调度");
            operateLogService.addModuleOperateLog(msg, null, entity.getExecUrl(), "启用【清洗调度】数据");
        } else {
            ServiceException.runError("该【清洗调度】数据已启用，无需重复操作");
        }
        return BatchResultDTO.success(entity.getId(), entity.getExecUrl(), OperationTypeEnum.UPDATE);
    }

    @Override
    public BatchResultDTO disable(DmpCfgEtlEntity entity) {
        if (!entity.getDisabled()) {
            entity.setDisabled(true);
            updateById(entity);
            // 日志
            String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】禁用操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getExecUrl(), "清洗调度");
            operateLogService.addModuleOperateLog(msg, null, entity.getExecUrl(), "禁用【清洗调度】数据");
        } else {
            ServiceException.runError("该【清洗调度】数据已禁用，无需重复操作");
        }
        return BatchResultDTO.success(entity.getId(), entity.getExecUrl(), OperationTypeEnum.UPDATE);
    }
}
