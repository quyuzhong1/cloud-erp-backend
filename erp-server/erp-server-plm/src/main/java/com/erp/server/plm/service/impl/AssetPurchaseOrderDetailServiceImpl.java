package com.erp.server.plm.service.impl;


import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.plm.entity.AssetPurchaseOrderDetailEntity;
import com.erp.server.plm.mapper.AssetPurchaseOrderDetailMapper;
import com.erp.server.plm.service.AssetPurchaseOrderDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.plm.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.plm.dto.AssetPurchaseOrderDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wtr
 * @since 2025-10-16
 */
@Slf4j
@Service
public class AssetPurchaseOrderDetailServiceImpl extends SuperServiceImpl<AssetPurchaseOrderDetailMapper, AssetPurchaseOrderDetailEntity> implements AssetPurchaseOrderDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AssetPurchaseOrderDetailDTO.AddDTO addDTO) {
        AssetPurchaseOrderDetailEntity assetPurchaseOrderDetailEntity = new AssetPurchaseOrderDetailEntity();
        BeanMapperUtils.copy(addDTO, assetPurchaseOrderDetailEntity);

        // 数据处理
        handleData(assetPurchaseOrderDetailEntity);

        log.info("开始新增");
        boolean save = super.save(assetPurchaseOrderDetailEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "" , assetPurchaseOrderDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addSysLogBySave(msg, null, assetPurchaseOrderDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(assetPurchaseOrderDetailEntity.getId(), assetPurchaseOrderDetailEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AssetPurchaseOrderDetailDTO.UpdateDTO addOrUpdateDTO) {
        AssetPurchaseOrderDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, ""));
        AssetPurchaseOrderDetailEntity assetPurchaseOrderDetailEntity =  BeanMapperUtils.map(AssetPurchaseOrderDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(assetPurchaseOrderDetailEntity);
        log.info("编辑 开始修改数据，id：【{}】", old.getId());
        boolean save = super.updateById(assetPurchaseOrderDetailEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录日志数据，id：【{}】", assetPurchaseOrderDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), assetPurchaseOrderDetailEntity.getId(), "");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addSysLogByUpdate(old, assetPurchaseOrderDetailEntity, null, assetPurchaseOrderDetailEntity.getId() , "", msg);
        return Boolean.TRUE;
    }

    @Override
    public Boolean endReceive(List<String> idList, String remark, Boolean b) {
        return null;
    }

    @Override
    public void add(List<AssetPurchaseOrderDetailDTO.AddDTO> detailList, String assetPurchaseOrderId) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<AssetPurchaseOrderDetailEntity> detailEntityList = new ArrayList<>();
        for (AssetPurchaseOrderDetailDTO.AddDTO addDTO : detailList) {
            AssetPurchaseOrderDetailEntity assetPurchaseOrderDetailEntity = new AssetPurchaseOrderDetailEntity();
            BeanMapperUtils.copy(addDTO, assetPurchaseOrderDetailEntity);
            assetPurchaseOrderDetailEntity.setMainId(assetPurchaseOrderId);
            assetPurchaseOrderDetailEntity.setTotalAmount(assetPurchaseOrderDetailEntity.getPurchaseQty().multiply(assetPurchaseOrderDetailEntity.getTaxPrice()));
            detailEntityList.add(assetPurchaseOrderDetailEntity);
        }
        super.saveBatch(detailEntityList);
    }

    @Override
    public void update(List<AssetPurchaseOrderDetailDTO.UpdateDTO> assetPurchaseOrderDetailDTOList, String assetPurchaseOrderId) {

    }


    /**
    * 新增修改处理数据
    */
    private void handleData(AssetPurchaseOrderDetailEntity assetPurchaseOrderDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
