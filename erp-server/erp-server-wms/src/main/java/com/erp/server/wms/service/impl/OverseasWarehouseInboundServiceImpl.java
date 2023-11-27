package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.OptChangeTypeEnum;
import com.common.business.enums.OverseasInstockStatusEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.entity.SoB2cFinanceEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.SubcontractChangeDTO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.wms.dto.OverseasInventoryDTO;
import com.erp.model.wms.entity.OverseasWarehouseInboundDetailEntity;
import com.erp.model.wms.entity.OverseasWarehouseInboundEntity;
import com.erp.model.wms.enums.OverseasFinishStatusEnum;
import com.erp.model.wms.enums.OverseasInstockTypeEnum;
import com.erp.server.wms.mapper.OverseasWarehouseInboundMapper;
import com.erp.server.wms.service.OverseasWarehouseInboundService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.OverseasWarehouseInboundDTO;

import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

/**
 * <p>
 * 海外仓入库单 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2023-11-16
 */
@Slf4j
@Service
public class OverseasWarehouseInboundServiceImpl extends SuperServiceImpl<OverseasWarehouseInboundMapper, OverseasWarehouseInboundEntity> implements OverseasWarehouseInboundService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(OverseasWarehouseInboundDTO.AddDTO addDTO) {
        OverseasWarehouseInboundEntity overseasWarehouseInboundEntity = new OverseasWarehouseInboundEntity();
        BeanMapperUtils.copy(addDTO, overseasWarehouseInboundEntity);

        // 数据处理
        handleData(overseasWarehouseInboundEntity);

        log.info("开始新增海外仓入库单");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        overseasWarehouseInboundEntity.setCode(code);
        boolean save = super.save(overseasWarehouseInboundEntity);
        if (!save) {
            throw new ServiceException("海外仓入库单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "海外仓入库单", overseasWarehouseInboundEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, overseasWarehouseInboundEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(overseasWarehouseInboundEntity.getId(), code);
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(OverseasWarehouseInboundDTO.UpdateDTO updateDTO) {
        OverseasWarehouseInboundEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "海外仓入库单"));
        OverseasWarehouseInboundEntity overseasWarehouseInboundEntity = BeanMapperUtils.map(OverseasWarehouseInboundEntity.class, updateDTO);

        // 数据处理
        handleData(overseasWarehouseInboundEntity);
        log.info("编辑 开始修改海外仓入库单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(overseasWarehouseInboundEntity);
        if (!save) {
            throw new ServiceException("海外仓入库单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录海外仓入库单日志数据，单号：【{}】", overseasWarehouseInboundEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), overseasWarehouseInboundEntity.getCode(), "海外仓入库单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, overseasWarehouseInboundEntity, null, overseasWarehouseInboundEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public OverseasWarehouseInboundEntity getByCode(String receivingCode) {
        return lambdaQuery().eq(OverseasWarehouseInboundEntity::getCode, receivingCode).one();
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(OverseasWarehouseInboundEntity overseasWarehouseInboundEntity) {
        // TODO 验证数据 & 数据赋值
    }


    @Override
    public PagingVO<OverseasWarehouseInboundDTO.ListDTO> paging(PagingDTO<OverseasWarehouseInboundDTO.PagingParamDTO> dto) {
        OverseasWarehouseInboundDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<?> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<OverseasWarehouseInboundDTO.ListDTO> pageData = baseMapper.paging(query, params);
        if (CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO manualFinish(OverseasWarehouseInboundDTO.FinishDTO dto) {

        OverseasWarehouseInboundEntity entity = this.getById(dto.getId());
        Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.OVERSEAS_WAREHOUSE_INBOUND_NOT_EXIST));
        // TODO 校验

        entity.setFinishStatus(OverseasFinishStatusEnum.MANUAL.getCode());
        entity.setFinishReason(dto.getFinishReason());
        // 详情更新签收数量
        if (!this.updateById(entity)){
            throw new ServiceException("海外仓入库单更新失败");
        }
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.UPDATE_STATUS);
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<OverseasWarehouseInboundDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for (OverseasWarehouseInboundDTO.ListDTO data : list) {
            // 入库类型名称
            data.setInstockTypeName(OverseasInstockTypeEnum.getNameByCode(data.getInstockType()));
            // 入库状态名称
            data.setInstockStatus(OverseasInstockStatusEnum.getName(data.getInstockStatus()));
            // 完结状态名称
            data.setFinishStatusName(OverseasFinishStatusEnum.getNameByCode(data.getFinishStatus()));
        }
    }

}
