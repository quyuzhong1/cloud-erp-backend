package com.erp.server.wms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.OverseasWarehouseInboundReceivedDTO;
import com.erp.model.wms.entity.OverseasWarehouseInboundReceivedEntity;
import com.erp.server.wms.mapper.OverseasWarehouseInboundReceivedMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.OverseasWarehouseInboundReceivedService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
/**
 * <p>
 * 海外仓签收记录 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2023-11-16
 */
@Slf4j
@Service
public class OverseasWarehouseInboundReceivedServiceImpl extends SuperServiceImpl<OverseasWarehouseInboundReceivedMapper, OverseasWarehouseInboundReceivedEntity> implements OverseasWarehouseInboundReceivedService {
    @Resource
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(OverseasWarehouseInboundReceivedDTO.AddDTO addDTO) {
        OverseasWarehouseInboundReceivedEntity overseasWarehouseInboundReceivedEntity = new OverseasWarehouseInboundReceivedEntity();
        BeanMapperUtils.copy(addDTO, overseasWarehouseInboundReceivedEntity);

        // 数据处理
        handleData(overseasWarehouseInboundReceivedEntity);

        log.info("开始新增海外仓签收记录");
        boolean save = super.save(overseasWarehouseInboundReceivedEntity);
        if(!save) {
            throw new ServiceException("海外仓签收记录保存失败");
        }

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "海外仓签收记录" , overseasWarehouseInboundReceivedEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, overseasWarehouseInboundReceivedEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(overseasWarehouseInboundReceivedEntity.getId(), overseasWarehouseInboundReceivedEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(OverseasWarehouseInboundReceivedDTO.UpdateDTO updateDTO) {
        OverseasWarehouseInboundReceivedEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "海外仓签收记录"));
        OverseasWarehouseInboundReceivedEntity overseasWarehouseInboundReceivedEntity =  BeanMapperUtils.map(OverseasWarehouseInboundReceivedEntity.class, updateDTO);

        // 数据处理
        handleData(overseasWarehouseInboundReceivedEntity);
        log.info("编辑 开始修改海外仓签收记录数据，id：【{}】", old.getId());
        boolean save = super.updateById(overseasWarehouseInboundReceivedEntity);
        if(!save) {
            throw new ServiceException("海外仓签收记录保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录海外仓签收记录日志数据，id：【{}】", overseasWarehouseInboundReceivedEntity.getId());
            String msg = CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), overseasWarehouseInboundReceivedEntity.getId(), "海外仓签收记录");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, overseasWarehouseInboundReceivedEntity, null, overseasWarehouseInboundReceivedEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<OverseasWarehouseInboundReceivedEntity> listByDetailIds(List<String> detailIds) {
        return lambdaQuery()
                .in(OverseasWarehouseInboundReceivedEntity::getDetailId, detailIds)
                .orderByDesc(OverseasWarehouseInboundReceivedEntity::getCreateTime)
                .list();
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(OverseasWarehouseInboundReceivedEntity overseasWarehouseInboundReceivedEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
