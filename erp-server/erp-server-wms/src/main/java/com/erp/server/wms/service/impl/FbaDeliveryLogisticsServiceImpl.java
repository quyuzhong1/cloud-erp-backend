package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseIdsDTO;
import com.erp.model.wms.dto.FbaDeliveryDTO;
import com.erp.model.wms.entity.FbaDeliveryLogisticsEntity;
import com.erp.server.wms.mapper.FbaDeliveryLogisticsMapper;
import com.erp.server.wms.service.FbaDeliveryLogisticsService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.FbaDeliveryLogisticsDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * FBA发货单物流信息表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
@Slf4j
@Service
public class FbaDeliveryLogisticsServiceImpl extends SuperServiceImpl<FbaDeliveryLogisticsMapper, FbaDeliveryLogisticsEntity> implements FbaDeliveryLogisticsService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(FbaDeliveryLogisticsDTO.AddDTO addDTO) {
        FbaDeliveryLogisticsEntity fbaDeliveryLogisticsEntity = new FbaDeliveryLogisticsEntity();
        BeanMapperUtils.copy(addDTO, fbaDeliveryLogisticsEntity);

        // 数据处理
        handleData(fbaDeliveryLogisticsEntity);

        log.info("开始新增FBA发货单物流信息单");
        boolean save = super.save(fbaDeliveryLogisticsEntity);
        if(!save) {
            throw new ServiceException("FBA发货单物流信息单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "FBA发货单物流信息单" , fbaDeliveryLogisticsEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, fbaDeliveryLogisticsEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）
        return fbaDeliveryLogisticsEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(FbaDeliveryLogisticsDTO.UpdateDTO updateDTO) {
        FbaDeliveryLogisticsEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "FBA发货单物流信息单"));
        FbaDeliveryLogisticsEntity fbaDeliveryLogisticsEntity =  BeanMapperUtils.map(FbaDeliveryLogisticsEntity.class, updateDTO);

        // 数据处理
        handleData(fbaDeliveryLogisticsEntity);
        log.info("编辑 开始修改FBA发货单物流信息单数据，id：【{}】", old.getId());
        boolean save = super.updateById(fbaDeliveryLogisticsEntity);
        if(!save) {
            throw new ServiceException("FBA发货单物流信息单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录FBA发货单物流信息单日志数据，id：【{}】", fbaDeliveryLogisticsEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), fbaDeliveryLogisticsEntity.getId(), "FBA发货单物流信息单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, fbaDeliveryLogisticsEntity, null, fbaDeliveryLogisticsEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(FbaDeliveryLogisticsEntity fbaDeliveryLogisticsEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public List<FbaDeliveryDTO.DeliveryLogisticsView> updateLogisticsView(BaseIdsDTO.IdsDTO ids) {
        return null;
    }
}
