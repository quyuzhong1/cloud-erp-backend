package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.oms.entity.SoB2cFinanceEntity;
import com.erp.model.wms.entity.OverseasWarehouseInboundDetailEntity;
import com.erp.server.wms.mapper.OverseasWarehouseInboundDetailMapper;
import com.erp.server.wms.service.OverseasWarehouseInboundDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.OverseasWarehouseInboundDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 海外仓入库单详情 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2023-11-16
 */
@Slf4j
@Service
public class OverseasWarehouseInboundDetailServiceImpl extends SuperServiceImpl<OverseasWarehouseInboundDetailMapper, OverseasWarehouseInboundDetailEntity> implements OverseasWarehouseInboundDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(OverseasWarehouseInboundDetailDTO.AddDTO addDTO) {
        OverseasWarehouseInboundDetailEntity overseasWarehouseInboundDetailEntity = new OverseasWarehouseInboundDetailEntity();
        BeanMapperUtils.copy(addDTO, overseasWarehouseInboundDetailEntity);

        // 数据处理
        handleData(overseasWarehouseInboundDetailEntity);

        log.info("开始新增海外仓入库单详情");
        boolean save = super.save(overseasWarehouseInboundDetailEntity);
        if(!save) {
            throw new ServiceException("海外仓入库单详情保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "海外仓入库单详情" , overseasWarehouseInboundDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, overseasWarehouseInboundDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(overseasWarehouseInboundDetailEntity.getId(), overseasWarehouseInboundDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(OverseasWarehouseInboundDetailDTO.UpdateDTO updateDTO) {
        OverseasWarehouseInboundDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "海外仓入库单详情"));
        OverseasWarehouseInboundDetailEntity overseasWarehouseInboundDetailEntity =  BeanMapperUtils.map(OverseasWarehouseInboundDetailEntity.class, updateDTO);

        // 数据处理
        handleData(overseasWarehouseInboundDetailEntity);
        log.info("编辑 开始修改海外仓入库单详情数据，id：【{}】", old.getId());
        boolean save = super.updateById(overseasWarehouseInboundDetailEntity);
        if(!save) {
            throw new ServiceException("海外仓入库单详情保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录海外仓入库单详情日志数据，id：【{}】", overseasWarehouseInboundDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), overseasWarehouseInboundDetailEntity.getId(), "海外仓入库单详情");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, overseasWarehouseInboundDetailEntity, null, overseasWarehouseInboundDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(OverseasWarehouseInboundDetailEntity overseasWarehouseInboundDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public List<OverseasWarehouseInboundDetailEntity> getByMainId(String mainId) {
        return lambdaQuery().eq(OverseasWarehouseInboundDetailEntity::getMainId,mainId).list();
    }

}
