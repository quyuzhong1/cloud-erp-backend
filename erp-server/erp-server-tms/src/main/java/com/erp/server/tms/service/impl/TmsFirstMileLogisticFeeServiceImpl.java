package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.TmsFirstMileLogisticFeeEntity;
import com.erp.server.tms.mapper.TmsFirstMileLogisticFeeMapper;
import com.erp.server.tms.service.TmsFirstMileLogisticFeeService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.TmsFirstMileLogisticFeeDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 头程物流单费用 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-03-19
 */
@Slf4j
@Service
public class TmsFirstMileLogisticFeeServiceImpl extends SuperServiceImpl<TmsFirstMileLogisticFeeMapper, TmsFirstMileLogisticFeeEntity> implements TmsFirstMileLogisticFeeService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TmsFirstMileLogisticFeeDTO.AddDTO addDTO) {
        TmsFirstMileLogisticFeeEntity tmsFirstMileLogisticFeeEntity = new TmsFirstMileLogisticFeeEntity();
        BeanMapperUtils.copy(addDTO, tmsFirstMileLogisticFeeEntity);

        // 数据处理
        handleData(tmsFirstMileLogisticFeeEntity);

        log.info("开始新增头程物流单费用");
        boolean save = super.save(tmsFirstMileLogisticFeeEntity);
        if(!save) {
            throw new ServiceException("头程物流单费用保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "头程物流单费用" , tmsFirstMileLogisticFeeEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, tmsFirstMileLogisticFeeEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(tmsFirstMileLogisticFeeEntity.getId(), tmsFirstMileLogisticFeeEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TmsFirstMileLogisticFeeDTO.UpdateDTO updateDTO) {
        TmsFirstMileLogisticFeeEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "头程物流单费用"));
        TmsFirstMileLogisticFeeEntity tmsFirstMileLogisticFeeEntity =  BeanMapperUtils.map(TmsFirstMileLogisticFeeEntity.class, updateDTO);

        // 数据处理
        handleData(tmsFirstMileLogisticFeeEntity);
        log.info("编辑 开始修改头程物流单费用数据，id：【{}】", old.getId());
        boolean save = super.updateById(tmsFirstMileLogisticFeeEntity);
        if(!save) {
            throw new ServiceException("头程物流单费用保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录头程物流单费用日志数据，id：【{}】", tmsFirstMileLogisticFeeEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), tmsFirstMileLogisticFeeEntity.getId(), "头程物流单费用");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, tmsFirstMileLogisticFeeEntity, null, tmsFirstMileLogisticFeeEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(TmsFirstMileLogisticFeeEntity tmsFirstMileLogisticFeeEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
