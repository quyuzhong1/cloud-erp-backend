package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.SampleTransferDetailEntity;
import com.erp.server.wms.mapper.SampleTransferDetailMapper;
import com.erp.server.wms.service.SampleTransferDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.SampleTransferDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 样品转移单明细表 服务实现类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-10-28
 */
@Slf4j
@Service
public class SampleTransferDetailServiceImpl extends SuperServiceImpl<SampleTransferDetailMapper, SampleTransferDetailEntity> implements SampleTransferDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SampleTransferDetailDTO.AddDTO addDTO) {
        SampleTransferDetailEntity sampleTransferDetailEntity = new SampleTransferDetailEntity();
        BeanMapperUtils.copy(addDTO, sampleTransferDetailEntity);

        // 数据处理
        handleData(sampleTransferDetailEntity);

        log.info("开始新增样品转移单明细单");
        boolean save = super.save(sampleTransferDetailEntity);
        if(!save) {
            throw new ServiceException("样品转移单明细单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "样品转移单明细单" , sampleTransferDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, sampleTransferDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(sampleTransferDetailEntity.getId(), sampleTransferDetailEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SampleTransferDetailDTO.UpdateDTO addOrUpdateDTO) {
        SampleTransferDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "样品转移单明细单"));
        SampleTransferDetailEntity sampleTransferDetailEntity =  BeanMapperUtils.map(SampleTransferDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(sampleTransferDetailEntity);
        log.info("编辑 开始修改样品转移单明细单数据，id：【{}】", old.getId());
        boolean save = super.updateById(sampleTransferDetailEntity);
        if(!save) {
            throw new ServiceException("样品转移单明细单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录样品转移单明细单日志数据，id：【{}】", sampleTransferDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), sampleTransferDetailEntity.getId(), "样品转移单明细单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, sampleTransferDetailEntity, null, sampleTransferDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public List<SampleTransferDetailEntity> listByMainId(String mainId) {
        return this.lambdaQuery()
                .eq(SampleTransferDetailEntity::getMainId, mainId)
                .eq(SampleTransferDetailEntity::getIsDeleted, false)
                .list();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(SampleTransferDetailEntity sampleTransferDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
