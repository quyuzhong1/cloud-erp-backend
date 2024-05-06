package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.AliexpressDeliveryDetailEntity;
import com.erp.server.wms.mapper.AliexpressDeliveryDetailMapper;
import com.erp.server.wms.service.AliexpressDeliveryDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.AliexpressDeliveryDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 速卖通发货单详情 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-05-06
 */
@Slf4j
@Service
public class AliexpressDeliveryDetailServiceImpl extends SuperServiceImpl<AliexpressDeliveryDetailMapper, AliexpressDeliveryDetailEntity> implements AliexpressDeliveryDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AliexpressDeliveryDetailDTO.AddDTO addDTO) {
        AliexpressDeliveryDetailEntity aliexpressDeliveryDetailEntity = new AliexpressDeliveryDetailEntity();
        BeanMapperUtils.copy(addDTO, aliexpressDeliveryDetailEntity);

        // 数据处理
        handleData(aliexpressDeliveryDetailEntity);

        log.info("开始新增速卖通发货单详情");
        boolean save = super.save(aliexpressDeliveryDetailEntity);
        if(!save) {
            throw new ServiceException("速卖通发货单详情保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "速卖通发货单详情" , aliexpressDeliveryDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, aliexpressDeliveryDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(aliexpressDeliveryDetailEntity.getId(), aliexpressDeliveryDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AliexpressDeliveryDetailDTO.UpdateDTO updateDTO) {
        AliexpressDeliveryDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "速卖通发货单详情"));
        AliexpressDeliveryDetailEntity aliexpressDeliveryDetailEntity =  BeanMapperUtils.map(AliexpressDeliveryDetailEntity.class, updateDTO);

        // 数据处理
        handleData(aliexpressDeliveryDetailEntity);
        log.info("编辑 开始修改速卖通发货单详情数据，id：【{}】", old.getId());
        boolean save = super.updateById(aliexpressDeliveryDetailEntity);
        if(!save) {
            throw new ServiceException("速卖通发货单详情保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录速卖通发货单详情日志数据，id：【{}】", aliexpressDeliveryDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), aliexpressDeliveryDetailEntity.getId(), "速卖通发货单详情");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, aliexpressDeliveryDetailEntity, null, aliexpressDeliveryDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(AliexpressDeliveryDetailEntity aliexpressDeliveryDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
