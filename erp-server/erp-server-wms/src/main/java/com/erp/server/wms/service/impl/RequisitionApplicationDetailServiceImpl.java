package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.dto.RequisitionApplicationDTO;
import com.erp.model.wms.entity.RequisitionApplicationDetailEntity;
import com.erp.server.wms.mapper.RequisitionApplicationDetailMapper;
import com.erp.server.wms.service.RequisitionApplicationDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.RequisitionApplicationDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 要货申请单明细表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Slf4j
@Service
public class RequisitionApplicationDetailServiceImpl extends SuperServiceImpl<RequisitionApplicationDetailMapper, RequisitionApplicationDetailEntity> implements RequisitionApplicationDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(RequisitionApplicationDTO.AddDTO addDTO, String mainId) {
        RequisitionApplicationDetailEntity requisitionApplicationDetailEntity = new RequisitionApplicationDetailEntity();
        BeanMapperUtils.copy(addDTO, requisitionApplicationDetailEntity);

        // 数据处理
        handleData(requisitionApplicationDetailEntity);

        log.info("开始新增要货申请单明细单");
        boolean save = super.save(requisitionApplicationDetailEntity);
        if(!save) {
            throw new ServiceException("要货申请单明细单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "要货申请单明细单" , requisitionApplicationDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, requisitionApplicationDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(requisitionApplicationDetailEntity.getId(), requisitionApplicationDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(RequisitionApplicationDTO.UpdateDTO updateDTO, String mainId) {
        RequisitionApplicationDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "要货申请单明细单"));
        RequisitionApplicationDetailEntity requisitionApplicationDetailEntity =  BeanMapperUtils.map(RequisitionApplicationDetailEntity.class, updateDTO);

        // 数据处理
        handleData(requisitionApplicationDetailEntity);
        log.info("编辑 开始修改要货申请单明细单数据，id：【{}】", old.getId());
        boolean save = super.updateById(requisitionApplicationDetailEntity);
        if(!save) {
            throw new ServiceException("要货申请单明细单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录要货申请单明细单日志数据，id：【{}】", requisitionApplicationDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), requisitionApplicationDetailEntity.getId(), "要货申请单明细单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, requisitionApplicationDetailEntity, null, requisitionApplicationDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(RequisitionApplicationDetailEntity requisitionApplicationDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
