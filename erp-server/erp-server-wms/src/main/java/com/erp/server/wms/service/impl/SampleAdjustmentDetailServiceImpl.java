package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.SampleAdjustmentDetailEntity;
import com.erp.server.wms.mapper.SampleAdjustmentDetailMapper;
import com.erp.server.wms.service.SampleAdjustmentDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.wms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.SampleAdjustmentDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.apache.commons.lang3.StringUtils;
/**
 * <p>
 * 样品调整单明细表 服务实现类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-11-14
 */
@Slf4j
@Service
public class SampleAdjustmentDetailServiceImpl extends SuperServiceImpl<SampleAdjustmentDetailMapper, SampleAdjustmentDetailEntity> implements SampleAdjustmentDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SampleAdjustmentDetailDTO.AddDTO addDTO) {
        SampleAdjustmentDetailEntity sampleAdjustmentDetailEntity = new SampleAdjustmentDetailEntity();
        BeanMapperUtils.copy(addDTO, sampleAdjustmentDetailEntity);

        // 数据处理
        handleData(sampleAdjustmentDetailEntity);

        log.info("开始新增样品调整单明细单");
        boolean save = super.save(sampleAdjustmentDetailEntity);
        if(!save) {
            throw new ServiceException("样品调整单明细单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "样品调整单明细单" , sampleAdjustmentDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, sampleAdjustmentDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(sampleAdjustmentDetailEntity.getId(), sampleAdjustmentDetailEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SampleAdjustmentDetailDTO.UpdateDTO addOrUpdateDTO) {
        SampleAdjustmentDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "样品调整单明细单"));
        SampleAdjustmentDetailEntity sampleAdjustmentDetailEntity =  BeanMapperUtils.map(SampleAdjustmentDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(sampleAdjustmentDetailEntity);
        log.info("编辑 开始修改样品调整单明细单数据，id：【{}】", old.getId());
        boolean save = super.updateById(sampleAdjustmentDetailEntity);
        if(!save) {
            throw new ServiceException("样品调整单明细单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录样品调整单明细单日志数据，id：【{}】", sampleAdjustmentDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), sampleAdjustmentDetailEntity.getId(), "样品调整单明细单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, sampleAdjustmentDetailEntity, null, sampleAdjustmentDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public List<SampleAdjustmentDetailEntity> listByMainId(String mainId) {
        if(StringUtils.isBlank(mainId)){
            return new ArrayList<>();
        }
        return lambdaQuery().eq(SampleAdjustmentDetailEntity::getMainId, mainId).list();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(SampleAdjustmentDetailEntity sampleAdjustmentDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
