package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryDetailEntity;
import com.erp.server.wms.mapper.ThirdWarehouseDeliveryDetailMapper;
import com.erp.server.wms.service.ThirdWarehouseDeliveryDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.ThirdWarehouseDeliveryDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 三方仓发货单明细 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-10-17
 */
@Slf4j
@Service
public class ThirdWarehouseDeliveryDetailServiceImpl extends SuperServiceImpl<ThirdWarehouseDeliveryDetailMapper, ThirdWarehouseDeliveryDetailEntity> implements ThirdWarehouseDeliveryDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ThirdWarehouseDeliveryDetailDTO.AddDTO addDTO) {
        ThirdWarehouseDeliveryDetailEntity thirdWarehouseDeliveryDetailEntity = new ThirdWarehouseDeliveryDetailEntity();
        BeanMapperUtils.copy(addDTO, thirdWarehouseDeliveryDetailEntity);

        // 数据处理
        handleData(thirdWarehouseDeliveryDetailEntity);

        log.info("开始新增三方仓发货单明细");
        boolean save = super.save(thirdWarehouseDeliveryDetailEntity);
        if(!save) {
            throw new ServiceException("三方仓发货单明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "三方仓发货单明细" , thirdWarehouseDeliveryDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, thirdWarehouseDeliveryDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(thirdWarehouseDeliveryDetailEntity.getId(), thirdWarehouseDeliveryDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ThirdWarehouseDeliveryDetailDTO.UpdateDTO updateDTO) {
        ThirdWarehouseDeliveryDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "三方仓发货单明细"));
        ThirdWarehouseDeliveryDetailEntity thirdWarehouseDeliveryDetailEntity =  BeanMapperUtils.map(ThirdWarehouseDeliveryDetailEntity.class, updateDTO);

        // 数据处理
        handleData(thirdWarehouseDeliveryDetailEntity);
        log.info("编辑 开始修改三方仓发货单明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(thirdWarehouseDeliveryDetailEntity);
        if(!save) {
            throw new ServiceException("三方仓发货单明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录三方仓发货单明细日志数据，id：【{}】", thirdWarehouseDeliveryDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), thirdWarehouseDeliveryDetailEntity.getId(), "三方仓发货单明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, thirdWarehouseDeliveryDetailEntity, null, thirdWarehouseDeliveryDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ThirdWarehouseDeliveryDetailEntity thirdWarehouseDeliveryDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
