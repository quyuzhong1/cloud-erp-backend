package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.server.wms.mapper.OverseasProviderWarehouseMapper;
import com.erp.server.wms.service.OverseasProviderWarehouseService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 海外物流商仓库 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Slf4j
@Service
public class OverseasProviderWarehouseServiceImpl extends SuperServiceImpl<OverseasProviderWarehouseMapper, OverseasProviderWarehouseEntity> implements OverseasProviderWarehouseService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(OverseasProviderWarehouseDTO.AddDTO addDTO) {
        OverseasProviderWarehouseEntity overseasProviderWarehouseEntity = new OverseasProviderWarehouseEntity();
        BeanMapperUtils.copy(addDTO, overseasProviderWarehouseEntity);

        // 数据处理
        handleData(overseasProviderWarehouseEntity);

        log.info("开始新增海外物流商仓库");
        boolean save = super.save(overseasProviderWarehouseEntity);
        if(!save) {
            throw new ServiceException("海外物流商仓库保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "海外物流商仓库" , overseasProviderWarehouseEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.OVERSEAS_PROVIDER.getCode(), overseasProviderWarehouseEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(overseasProviderWarehouseEntity.getId(), overseasProviderWarehouseEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(OverseasProviderWarehouseDTO.UpdateDTO updateDTO) {
        OverseasProviderWarehouseEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "海外物流商仓库"));
        OverseasProviderWarehouseEntity overseasProviderWarehouseEntity =  BeanMapperUtils.map(OverseasProviderWarehouseEntity.class, updateDTO);

        // 数据处理
        handleData(overseasProviderWarehouseEntity);
        log.info("编辑 开始修改海外物流商仓库数据，id：【{}】", old.getId());
        boolean save = super.updateById(overseasProviderWarehouseEntity);
        if(!save) {
            throw new ServiceException("海外物流商仓库保存失败");
        }

        // 记录主单操作日志
        log.info("编辑 开始记录海外物流商仓库日志数据，id：【{}】", overseasProviderWarehouseEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), overseasProviderWarehouseEntity.getId(), "海外物流商仓库");
        operateLogService.addModuleOperateLogByObj(old, overseasProviderWarehouseEntity, ModuleTypeEnum.OVERSEAS_PROVIDER.getCode(), overseasProviderWarehouseEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public OverseasProviderWarehouseEntity getByWarehouseId(String warehouseId) {
        return lambdaQuery()
                .eq(OverseasProviderWarehouseEntity::getWarehouseId, warehouseId)
                .last("LIMIT 1")
                .one();
    }

    @Override
    public OverseasProviderWarehouseEntity getByPlatform(String mainId,String platformWarehouseCode) {
        return lambdaQuery()
                .eq(OverseasProviderWarehouseEntity::getMainId, mainId)
                .eq(OverseasProviderWarehouseEntity::getPlatformWarehouseCode, platformWarehouseCode)
                .last("LIMIT 1")
                .one();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(OverseasProviderWarehouseEntity overseasProviderWarehouseEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
