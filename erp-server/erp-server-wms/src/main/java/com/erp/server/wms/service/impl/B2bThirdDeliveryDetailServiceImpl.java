package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.B2bThirdDeliveryDetailEntity;
import com.erp.server.wms.mapper.B2bThirdDeliveryDetailMapper;
import com.erp.server.wms.service.B2bThirdDeliveryDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.B2bThirdDeliveryDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * B2B三方发货单明细 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2025-11-26
 */
@Slf4j
@Service
public class B2bThirdDeliveryDetailServiceImpl extends SuperServiceImpl<B2bThirdDeliveryDetailMapper, B2bThirdDeliveryDetailEntity> implements B2bThirdDeliveryDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(B2bThirdDeliveryDetailDTO.AddDTO addDTO) {
        B2bThirdDeliveryDetailEntity b2bThirdDeliveryDetailEntity = new B2bThirdDeliveryDetailEntity();
        BeanMapperUtils.copy(addDTO, b2bThirdDeliveryDetailEntity);

        // 数据处理
        handleData(b2bThirdDeliveryDetailEntity);

        log.info("开始新增B2B三方发货单明细");
        boolean save = super.save(b2bThirdDeliveryDetailEntity);
        if(!save) {
            throw new ServiceException("B2B三方发货单明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "B2B三方发货单明细" , b2bThirdDeliveryDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, b2bThirdDeliveryDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(b2bThirdDeliveryDetailEntity.getId(), b2bThirdDeliveryDetailEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(B2bThirdDeliveryDetailDTO.UpdateDTO addOrUpdateDTO) {
        B2bThirdDeliveryDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "B2B三方发货单明细"));
        B2bThirdDeliveryDetailEntity b2bThirdDeliveryDetailEntity =  BeanMapperUtils.map(B2bThirdDeliveryDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(b2bThirdDeliveryDetailEntity);
        log.info("编辑 开始修改B2B三方发货单明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(b2bThirdDeliveryDetailEntity);
        if(!save) {
            throw new ServiceException("B2B三方发货单明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录B2B三方发货单明细日志数据，id：【{}】", b2bThirdDeliveryDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), b2bThirdDeliveryDetailEntity.getId(), "B2B三方发货单明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, b2bThirdDeliveryDetailEntity, null, b2bThirdDeliveryDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<B2bThirdDeliveryDetailEntity> listByMainIds(List<String> ids) {
        if (CollUtil.isEmpty(ids)){
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(B2bThirdDeliveryDetailEntity::getMainId,ids).list();
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(B2bThirdDeliveryDetailEntity b2bThirdDeliveryDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
