package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.TmsFirstMileLogisticEntity;
import com.erp.server.tms.mapper.TmsFirstMileLogisticMapper;
import com.erp.server.tms.service.TmsFirstMileLogisticService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.TmsFirstMileLogisticDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 头程物流单 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-03-19
 */
@Slf4j
@Service
public class TmsFirstMileLogisticServiceImpl extends SuperServiceImpl<TmsFirstMileLogisticMapper, TmsFirstMileLogisticEntity> implements TmsFirstMileLogisticService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TmsFirstMileLogisticDTO.AddDTO addDTO) {
        TmsFirstMileLogisticEntity tmsFirstMileLogisticEntity = new TmsFirstMileLogisticEntity();
        BeanMapperUtils.copy(addDTO, tmsFirstMileLogisticEntity);

        // 数据处理
        handleData(tmsFirstMileLogisticEntity);

        log.info("开始新增头程物流单");
        boolean save = super.save(tmsFirstMileLogisticEntity);
        if(!save) {
            throw new ServiceException("头程物流单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "头程物流单" , tmsFirstMileLogisticEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, tmsFirstMileLogisticEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(tmsFirstMileLogisticEntity.getId(), tmsFirstMileLogisticEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TmsFirstMileLogisticDTO.UpdateDTO updateDTO) {
        TmsFirstMileLogisticEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "头程物流单"));
        TmsFirstMileLogisticEntity tmsFirstMileLogisticEntity =  BeanMapperUtils.map(TmsFirstMileLogisticEntity.class, updateDTO);

        // 数据处理
        handleData(tmsFirstMileLogisticEntity);
        log.info("编辑 开始修改头程物流单数据，id：【{}】", old.getId());
        boolean save = super.updateById(tmsFirstMileLogisticEntity);
        if(!save) {
            throw new ServiceException("头程物流单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录头程物流单日志数据，id：【{}】", tmsFirstMileLogisticEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), tmsFirstMileLogisticEntity.getId(), "头程物流单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, tmsFirstMileLogisticEntity, null, tmsFirstMileLogisticEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(TmsFirstMileLogisticEntity tmsFirstMileLogisticEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
