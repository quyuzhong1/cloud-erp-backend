package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.common.business.constant.ApproveType;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.OverseasInstockStatusEnum;
import com.common.business.vo.LoginUser;
import com.erp.model.wms.dto.FbaShipmentDTO;
import com.erp.model.wms.dto.OverseasWarehouseInboundDTO;
import com.erp.model.wms.entity.FbaShipmentReceiveEntity;
import com.erp.model.oms.entity.SoB2cFinanceEntity;
import com.erp.model.wms.entity.OverseasWarehouseInboundDetailEntity;
import com.erp.model.wms.entity.OverseasWarehouseInboundEntity;
import com.erp.model.wms.entity.OverseasWarehouseInboundReceivedEntity;
import com.erp.server.wms.convert.FbaShipmentConverter;
import com.erp.server.wms.convert.WmsOverseasWarehouseInboundConverter;
import com.erp.server.wms.mapper.OverseasWarehouseInboundDetailMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.OverseasWarehouseInboundDetailDTO;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;

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
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private CommonService commonService;
    @Resource
    private OverseasWarehouseInboundReceivedService overseasWarehouseInboundReceivedService;
    @Resource
    private OverseasWarehouseInboundService overseasWarehouseInboundService;
    @Resource
    private TransferInfoService transferInfoService;

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
        if (!save) {
            throw new ServiceException("海外仓入库单详情保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "海外仓入库单详情", overseasWarehouseInboundDetailEntity.getId());
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
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "海外仓入库单详情"));
        OverseasWarehouseInboundDetailEntity overseasWarehouseInboundDetailEntity = BeanMapperUtils.map(OverseasWarehouseInboundDetailEntity.class, updateDTO);

        // 数据处理
        handleData(overseasWarehouseInboundDetailEntity);
        log.info("编辑 开始修改海外仓入库单详情数据，id：【{}】", old.getId());
        boolean save = super.updateById(overseasWarehouseInboundDetailEntity);
        if (!save) {
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

    @Override
    public List<OverseasWarehouseInboundDTO.ReceiveRecordView> listReceiveRecord(String detailId) {
        List<OverseasWarehouseInboundReceivedEntity> list = overseasWarehouseInboundReceivedService.listByDetailIds(Collections.singletonList(detailId));
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream()
                .map(WmsOverseasWarehouseInboundConverter.INSTANCE::receivedEntityToView)
                .collect(Collectors.toList());
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(OverseasWarehouseInboundDetailEntity overseasWarehouseInboundDetailEntity) {
        // TODO 验证数据 & 数据赋值
    }

    @Override
    public List<OverseasWarehouseInboundDetailEntity> getByMainId(String mainId) {
        return lambdaQuery().eq(OverseasWarehouseInboundDetailEntity::getMainId, mainId).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO manualReceived(OverseasWarehouseInboundDTO.ReceivedDTO dto) {
        // 查询详情
        OverseasWarehouseInboundDetailEntity entity = this.getById(dto.getDetailId());
        Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.OVERSEAS_WAREHOUSE_INBOUND_DETAIL_NOT_EXIST));
        // 校验
        // 查询提交的平台
        OverseasWarehouseInboundEntity mainEntity = overseasWarehouseInboundService.getById(entity.getMainId());
        Optional.ofNullable(mainEntity).orElseThrow(() -> new ServiceException(ApiError.OVERSEAS_WAREHOUSE_INBOUND_NOT_EXIST));
        // 非手动单
        if (StringUtils.isNotBlank(mainEntity.getDictPlatform())){
            String msg = StrUtil.format("【{}】已对接系统，请等待海外仓签收", mainEntity.getToWarehouseName());
            throw new ServiceException(msg);
        }
        if (!OverseasInstockStatusEnum.TO_BE_SIGNED.getCode().equalsIgnoreCase(mainEntity.getInstockStatus()) &&
                !OverseasInstockStatusEnum.PARTIAL_SIGNED.getCode().equalsIgnoreCase(mainEntity.getInstockStatus())
        ){
            String msg = StrUtil.format("【{}】不等于待签收和部分签收，无法手动签收", mainEntity.getCode());
            throw new ServiceException(msg);
        }
        if (entity.getPackQty() < entity.getReceiveQty() + dto.getReceivedQty()){
            throw new ServiceException("当前签收数量大于剩余签收数量");
        }

        entity.setReceiveQty(entity.getReceiveQty() + dto.getReceivedQty());
        entity.setDiffQty(entity.getDiffQty() + dto.getReceivedQty());
        if (Objects.equals(entity.getReceiveQty(), entity.getPackQty())){
            entity.setReceiveStatus("already");
        }
        // 详情更新签收数量
        if (!this.updateById(entity)) {
            throw new ServiceException("海外仓入库单详情更新失败");
        }
        // 主订单状态
        // 检查是否完全签收
        Boolean allReceive = this.checkAllReceiveByMainId(entity.getMainId());
        if (allReceive){
            mainEntity.setInstockStatus(OverseasInstockStatusEnum.SIGNED.getCode());
        } else {
            mainEntity.setInstockStatus(OverseasInstockStatusEnum.PARTIAL_SIGNED.getCode());
        }
        // 保存主表
        if (!overseasWarehouseInboundService.updateById(mainEntity)){
            throw new ServiceException("更新入库状态失败");
        }

        LoginUser userInfo = commonService.getUserInfo();
        // 添加签收记录
        OverseasWarehouseInboundReceivedEntity receivedEntity = new OverseasWarehouseInboundReceivedEntity(entity.getId(),
                userInfo.getUserName(),
                dto.getReceivedQty(),
                LocalDateTime.now(ZoneId.systemDefault()));
        if (!overseasWarehouseInboundReceivedService.save(receivedEntity)) {
            throw new ServiceException("海外仓入库单签收保存失败");
        }
        // 生成直接调拨单
        String transferOutId = overseasWarehouseInboundService.generateTransferOut(mainEntity, entity, receivedEntity);
        if (StringUtils.isNotBlank(transferOutId)) {
            //提交
            transferInfoService.submit(Collections.singletonList(transferOutId));
            //审核
            BaseApproveParamDTO baseApproveParamDTO = new BaseApproveParamDTO();
            baseApproveParamDTO.setIds(Collections.singletonList(transferOutId));
            baseApproveParamDTO.setType(ApproveType.PASS);
            transferInfoService.approve(baseApproveParamDTO, Boolean.TRUE);
        } else {
            throw new ServiceException(ApiError.ERROR_GENERATE_TRANSFER_OUT);
        }
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.UPDATE_STATUS);
    }

    @Override
    public List<OverseasWarehouseInboundDetailEntity> getByMainIds(List<String> mainIdList) {
        return lambdaQuery()
                .in(OverseasWarehouseInboundDetailEntity::getMainId, mainIdList)
                .list();
    }

    @Override
    public List<OverseasWarehouseInboundDetailEntity> getByIds(List<String> idList) {
        return lambdaQuery()
                .in(OverseasWarehouseInboundDetailEntity::getId, idList)
                .list();
    }

    @Override
    public Boolean checkAllReceiveByMainId(String mainId) {
        List<OverseasWarehouseInboundDetailEntity> detailEntityList = this.getByMainId(mainId);
        if (CollectionUtils.isEmpty(detailEntityList)){
            throw new ServiceException("数据异常：无详情");
        }
        return detailEntityList.stream().allMatch(e-> e.getPackQty() <= e.getReceiveQty());}

}
