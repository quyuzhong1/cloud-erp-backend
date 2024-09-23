package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.ReportOrderDemandDTO;
import com.erp.model.wms.entity.ReportOrderDemandEntity;
import com.erp.server.wms.mapper.ReportOrderDemandMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.ReportOrderDemandService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author will
 * @since 2024-09-23
 */
@Slf4j
@Service
public class ReportOrderDemandServiceImpl extends SuperServiceImpl<ReportOrderDemandMapper, ReportOrderDemandEntity> implements ReportOrderDemandService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ReportOrderDemandDTO.AddDTO addDTO) {
        ReportOrderDemandEntity reportOrderDemandEntity = new ReportOrderDemandEntity();
        BeanMapperUtils.copy(addDTO, reportOrderDemandEntity);

        // 数据处理
        handleData(reportOrderDemandEntity);

        log.info("开始新增");
        boolean save = super.save(reportOrderDemandEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "" , reportOrderDemandEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, reportOrderDemandEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(reportOrderDemandEntity.getId(), reportOrderDemandEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ReportOrderDemandDTO.UpdateDTO updateDTO) {
        ReportOrderDemandEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, ""));
        ReportOrderDemandEntity reportOrderDemandEntity =  BeanMapperUtils.map(ReportOrderDemandEntity.class, updateDTO);

        // 数据处理
        handleData(reportOrderDemandEntity);
        log.info("编辑 开始修改数据，id：【{}】", old.getId());
        boolean save = super.updateById(reportOrderDemandEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录日志数据，id：【{}】", reportOrderDemandEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), reportOrderDemandEntity.getId(), "");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, reportOrderDemandEntity, null, reportOrderDemandEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<ReportOrderDemandDTO.ListDTO> paging(PagingDTO<ReportOrderDemandDTO.PagingParamDTO> dto) {
        return null;
    }

    @Override
    public Boolean exportExcel(ReportOrderDemandDTO.PagingParamDTO dto) {
        return null;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ReportOrderDemandEntity reportOrderDemandEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
