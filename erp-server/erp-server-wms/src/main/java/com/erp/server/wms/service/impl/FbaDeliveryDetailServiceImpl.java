package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.erp.model.wms.dto.FbaShipmentDTO;
import com.erp.model.wms.entity.FbaDeliveryDetailEntity;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.FbaDeliveryDetailMapper;
import com.erp.server.wms.service.FbaDeliveryDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import feign.Feign;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.FbaDeliveryDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * FBA发货单明细表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
@Slf4j
@Service
public class FbaDeliveryDetailServiceImpl extends SuperServiceImpl<FbaDeliveryDetailMapper, FbaDeliveryDetailEntity> implements FbaDeliveryDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private ShopInfoFeign shopInfoFeign;
    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(FbaDeliveryDetailDTO.AddDTO addDTO) {
        FbaDeliveryDetailEntity fbaDeliveryDetailEntity = new FbaDeliveryDetailEntity();
        BeanMapperUtils.copy(addDTO, fbaDeliveryDetailEntity);

        // 数据处理
        handleData(fbaDeliveryDetailEntity);

        log.info("开始新增FBA发货单明细单");
        boolean save = super.save(fbaDeliveryDetailEntity);
        if(!save) {
            throw new ServiceException("FBA发货单明细单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "FBA发货单明细单" , fbaDeliveryDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, fbaDeliveryDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）
        return fbaDeliveryDetailEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(FbaDeliveryDetailDTO.UpdateDTO updateDTO) {
        FbaDeliveryDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "FBA发货单明细单"));
        FbaDeliveryDetailEntity fbaDeliveryDetailEntity =  BeanMapperUtils.map(FbaDeliveryDetailEntity.class, updateDTO);

        // 数据处理
        handleData(fbaDeliveryDetailEntity);
        log.info("编辑 开始修改FBA发货单明细单数据，id：【{}】", old.getId());
        boolean save = super.updateById(fbaDeliveryDetailEntity);
        if(!save) {
            throw new ServiceException("FBA发货单明细单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录FBA发货单明细单日志数据，id：【{}】", fbaDeliveryDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), fbaDeliveryDetailEntity.getId(), "FBA发货单明细单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, fbaDeliveryDetailEntity, null, fbaDeliveryDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<FbaDeliveryDetailEntity> listBySourceDetailIds(List<String> detailIds) {
        if (CollectionUtils.isEmpty(detailIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(FbaDeliveryDetailEntity::getSourceDetailId, detailIds).list();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(FbaDeliveryDetailEntity fbaDeliveryDetailEntity) {

    // TODO 验证数据 & 数据赋值
    }
}
