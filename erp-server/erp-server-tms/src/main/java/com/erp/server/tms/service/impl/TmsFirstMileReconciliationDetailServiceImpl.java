package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.TmsFirstMileReconciliationDetailEntity;
import com.erp.server.tms.mapper.TmsFirstMileReconciliationDetailMapper;
import com.erp.server.tms.service.TmsFirstMileReconciliationDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.TmsFirstMileReconciliationDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 头程对账单明细 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2024-03-25
 */
@Slf4j
@Service
public class TmsFirstMileReconciliationDetailServiceImpl extends SuperServiceImpl<TmsFirstMileReconciliationDetailMapper, TmsFirstMileReconciliationDetailEntity> implements TmsFirstMileReconciliationDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TmsFirstMileReconciliationDetailDTO.AddDTO addDTO) {
        TmsFirstMileReconciliationDetailEntity tmsFirstMileReconciliationDetailEntity = new TmsFirstMileReconciliationDetailEntity();
        BeanMapperUtils.copy(addDTO, tmsFirstMileReconciliationDetailEntity);

        // 数据处理
        handleData(tmsFirstMileReconciliationDetailEntity);

        log.info("开始新增头程对账单明细");
        boolean save = super.save(tmsFirstMileReconciliationDetailEntity);
        if(!save) {
            throw new ServiceException("头程对账单明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "头程对账单明细" , tmsFirstMileReconciliationDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, tmsFirstMileReconciliationDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(tmsFirstMileReconciliationDetailEntity.getId(), tmsFirstMileReconciliationDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TmsFirstMileReconciliationDetailDTO.UpdateDTO updateDTO) {
        TmsFirstMileReconciliationDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "头程对账单明细"));
        TmsFirstMileReconciliationDetailEntity tmsFirstMileReconciliationDetailEntity =  BeanMapperUtils.map(TmsFirstMileReconciliationDetailEntity.class, updateDTO);

        // 数据处理
        handleData(tmsFirstMileReconciliationDetailEntity);
        log.info("编辑 开始修改头程对账单明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(tmsFirstMileReconciliationDetailEntity);
        if(!save) {
            throw new ServiceException("头程对账单明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录头程对账单明细日志数据，id：【{}】", tmsFirstMileReconciliationDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), tmsFirstMileReconciliationDetailEntity.getId(), "头程对账单明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, tmsFirstMileReconciliationDetailEntity, null, tmsFirstMileReconciliationDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(TmsFirstMileReconciliationDetailEntity tmsFirstMileReconciliationDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
