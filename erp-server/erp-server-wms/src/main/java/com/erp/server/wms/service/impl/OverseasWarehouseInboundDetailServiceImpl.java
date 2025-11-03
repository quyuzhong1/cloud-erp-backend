package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import com.common.business.constant.ApproveType;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.OverseasInstockStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.FirstMileChangeRecordDTO;
import com.erp.model.tms.dto.FirstMileCostAllocationDTO;
import com.erp.model.wms.dto.OverseasWarehouseInboundDTO;
import com.erp.model.wms.dto.OverseasWarehouseInboundDetailDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.SignSourceTypeEnum;
import com.erp.rpc.tms.feign.FirstMileChangeRecordFeign;
import com.erp.rpc.tms.feign.TmsFirstMileLogisticFeign;
import com.erp.server.wms.convert.OverseasWarehouseInboundConverter;
import com.erp.server.wms.convert.WmsOverseasWarehouseInboundConverter;
import com.erp.server.wms.mapper.OverseasWarehouseInboundDetailMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

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
    private OverseasWarehouseInboundReceivedService overseasWarehouseInboundReceivedService;
    @Resource
    private OverseasWarehouseInboundService overseasWarehouseInboundService;
    @Resource
    private OverseasProviderWarehouseService overseasProviderWarehouseService;
    @Resource
    private TransferInfoService transferInfoService;
    @Resource
    private TmsFirstMileLogisticFeign tmsFirstMileLogisticFeign;
    @Resource
    private FirstMileChangeRecordFeign firstMileChangeRecordFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
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
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "海外仓入库单详情", overseasWarehouseInboundDetailEntity.getId());
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
        if (Objects.isNull(old)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "海外仓入库单详情");
        }
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
        String msg = CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), overseasWarehouseInboundDetailEntity.getId(), "海外仓入库单详情");
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
        return detailEntityList.stream().allMatch(e-> 0 == e.getDiffQty());}

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public List<BatchResultDTO> allManualReceived(List<OverseasWarehouseInboundDTO.ReceivedDTO> dtoList) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dtoList.size());

        LoginUser userInfo = UserContext.getDefaultLoginUser();
        // 主表ID， 详情
        Map<String, List<OverseasWarehouseInboundDetailEntity>> detailResultMap = new HashMap<>();
        // 主表ID， 主实体
        Map<String, OverseasWarehouseInboundEntity> mainResultMap = new HashMap<>();
        Map<String, Integer> receiverdMap = new HashMap<>();
        List<OverseasWarehouseInboundDetailEntity> detailEntityList = this.getByIds(dtoList.stream().map(OverseasWarehouseInboundDTO.ReceivedDTO::getDetailId).collect(Collectors.toList()));
        List<OverseasWarehouseInboundReceivedEntity> addReceivedList = new ArrayList<>();
        List<String> mainIdList = detailEntityList.stream().map(OverseasWarehouseInboundDetailEntity::getMainId).distinct().collect(Collectors.toList());
        List<OverseasWarehouseInboundEntity> overseasWarehouseInboundEntityList = overseasWarehouseInboundService.listByIds(mainIdList);
        for (OverseasWarehouseInboundDTO.ReceivedDTO dto : dtoList) {
            // 查询详情
            OverseasWarehouseInboundDetailEntity entity  = detailEntityList.stream()
                    .filter(e -> StringUtils.equals(e.getId(), dto.getDetailId()))
                    .findFirst()
                    .orElse(null);
            if (Objects.isNull(entity)){
                throw new ServiceException(ApiError.OVERSEAS_WAREHOUSE_INBOUND_DETAIL_NOT_EXIST);
            }
            // 校验
            // 查询提交的平台
            OverseasWarehouseInboundEntity mainEntity = overseasWarehouseInboundEntityList.stream().filter(v->v.getId().equals(entity.getMainId())).findFirst().orElse(null);
            if (Objects.isNull(mainEntity)){
                throw new ServiceException(ApiError.OVERSEAS_WAREHOUSE_INBOUND_NOT_EXIST);
            }
            // 非手动单
            if (CharSequenceUtil.isNotBlank(mainEntity.getDictPlatform())){
                OverseasProviderWarehouseEntity overseasProviderWarehouseEntity = overseasProviderWarehouseService.getByWarehouseIdWithNotDisabled(mainEntity.getToWarehouseId());
                if(Objects.nonNull(overseasProviderWarehouseEntity)){
                    String msg = CharSequenceUtil.format("【{}】已对接系统，请等待海外仓签收", mainEntity.getToWarehouseName());
                    throw new ServiceException(msg);
                }
            }
            if (!OverseasInstockStatusEnum.TO_BE_SIGNED.getCode().equalsIgnoreCase(mainEntity.getInstockStatus()) &&
                    !OverseasInstockStatusEnum.PARTIAL_SIGNED.getCode().equalsIgnoreCase(mainEntity.getInstockStatus())
            ){
                String msg = CharSequenceUtil.format("【{}】不等于待签收和部分签收，无法手动签收", mainEntity.getCode());
                throw new ServiceException(msg);
            }
            if (entity.getPackQty() < entity.getReceiveQty() + dto.getReceivedQty()){
                throw new ServiceException("当前签收数量大于剩余签收数量");
            }
            //时间校验 货件名称+SKU在填写月关联的头程分摊已生成，不可修改
            LocalDate date = dto.getReceiveDate().with(TemporalAdjusters.firstDayOfMonth());
            String sourceId = CharSequenceUtil.isNotBlank(mainEntity.getSourceId()) ? mainEntity.getSourceId() : "";//发货单id
            String code = CharSequenceUtil.isNotBlank(mainEntity.getCode()) ? mainEntity.getCode() : "";//业务单号
            FirstMileCostAllocationDTO.DetailDTO detailDTO = new FirstMileCostAllocationDTO.DetailDTO();
            detailDTO.setSourceId(sourceId);
            detailDTO.setBusinessCode(code);
            detailDTO.setReportMonth(date);
            //判断是否存在对应的头程分摊
            List<FirstMileCostAllocationDTO.DetailDTO> detailDTOS = tmsFirstMileLogisticFeign.getRecordBySourceIdAndCode(detailDTO);
            if (CollUtil.isNotEmpty(detailDTOS)){
                throw new ServiceException("发货单【{}】该月【{}】已生成头程分摊，不可修改",mainEntity.getSourceCode(),date);
            }
            entity.setReceiveQty(entity.getReceiveQty() + dto.getReceivedQty());
            entity.setDiffQty(entity.getDiffQty() + dto.getReceivedQty());
            entity.setReceiveTime(dto.getReceiveDate().atStartOfDay());
            // 计算在途数量
            int newTransportQty = entity.getPackQty() - entity.getReceiveQty();
            entity.setTransportQty(newTransportQty);
            if (Objects.equals(entity.getReceiveQty(), entity.getPackQty())){
                entity.setReceiveStatus("already");
            }
            // 添加签收记录
            OverseasWarehouseInboundReceivedEntity receivedEntity = new OverseasWarehouseInboundReceivedEntity(entity.getId(),
                    userInfo.getUserName(),
                    dto.getReceivedQty(),
                    dto.getReceiveDate().atStartOfDay(),
                    "",
                    SignSourceTypeEnum.MANUAL.getCode());
            addReceivedList.add(receivedEntity);
            // 添加主表
            mainResultMap.putIfAbsent(mainEntity.getId(), mainEntity);

            // 添加明细
            List<OverseasWarehouseInboundDetailEntity> currentDetailEntityList = detailResultMap.get(mainEntity.getId());
            if (CollectionUtils.isEmpty(currentDetailEntityList)){
                List<OverseasWarehouseInboundDetailEntity> currentList = new LinkedList<>();
                currentList.add(entity);
                detailResultMap.put(mainEntity.getId(), currentList);
            } else {
                currentDetailEntityList.add(entity);
                detailResultMap.put(mainEntity.getId(), currentDetailEntityList);
            }
            receiverdMap.put(receivedEntity.getDetailId(), receivedEntity.getReceiveQty());
        }
        this.updateBatchById(detailEntityList);
        overseasWarehouseInboundReceivedService.saveBatch(addReceivedList);

        for (Map.Entry<String, List<OverseasWarehouseInboundDetailEntity>> entry : detailResultMap.entrySet()) {
            // 主表
            OverseasWarehouseInboundEntity mainEntity = mainResultMap.get(entry.getKey());

            // 生成直接调拨单
            String transferOutId = overseasWarehouseInboundService.generateTransferOut(mainEntity, entry.getValue(), receiverdMap);
            if (CharSequenceUtil.isNotBlank(transferOutId)) {
                TransferInfoEntity entity = transferInfoService.getById(transferOutId);
                if (ObjUtil.isEmpty(entity)) {
                    throw new ServiceException(ApiError.ERROR_99047);
                }
                //提交
                transferInfoService.submit(entity, Boolean.FALSE);
                //审核
                if (Objects.nonNull(entity)){
                    try {
                        TransferInfoEntity approveEntity = transferInfoService.getById(transferOutId);
                        if (ObjUtil.isEmpty(approveEntity)) {
                            throw new ServiceException(ApiError.ERROR_99047);
                        }
                        transferInfoService.approve(approveEntity,ApproveType.PASS,"", null , Boolean.TRUE, Boolean.FALSE);
                    }catch (Exception e){
                        throw new ServiceException(e.getMessage());
                    }
                }
            } else {
                throw new ServiceException(ApiError.ERROR_GENERATE_TRANSFER_OUT);
            }
        }
        List<OverseasWarehouseInboundDetailEntity> allDetailEntityList = this.getByMainIds(mainIdList);
        // 主订单状态
        // 检查是否完全签收
        for (OverseasWarehouseInboundEntity overseasWarehouseInboundEntity : overseasWarehouseInboundEntityList) {
            List<OverseasWarehouseInboundDetailEntity> currentDetailEntityList = allDetailEntityList.stream()
                    .filter(e -> StringUtils.equals(e.getMainId(), overseasWarehouseInboundEntity.getId()))
                    .collect(Collectors.toList());
            if( currentDetailEntityList.stream().allMatch(e-> 0 == e.getDiffQty())){
                overseasWarehouseInboundEntity.setInstockStatus(OverseasInstockStatusEnum.AUTOMATIC_COMPLETION.getCode());
            }else{
                overseasWarehouseInboundEntity.setInstockStatus(OverseasInstockStatusEnum.PARTIAL_SIGNED.getCode());
            }
        }
        overseasWarehouseInboundService.updateBatchById(overseasWarehouseInboundEntityList);
        return resultDTOS;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> allChangeReceived(List<OverseasWarehouseInboundDTO.ReceivedDTO> dtoList) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dtoList.size());

        LoginUser userInfo = UserContext.getDefaultLoginUser();
        // 主表ID， 详情
        Map<String, List<OverseasWarehouseInboundDetailEntity>> detailResultMap = new HashMap<>();
        // 主表ID， 主实体
        Map<String, OverseasWarehouseInboundEntity> mainResultMap = new HashMap<>();
        Map<String, Integer> receiverdMap = new HashMap<>();
        List<OverseasWarehouseInboundDetailEntity> detailEntityList = this.getByIds(dtoList.stream().map(OverseasWarehouseInboundDTO.ReceivedDTO::getDetailId).collect(Collectors.toList()));
        List<OverseasWarehouseInboundReceivedEntity> addReceivedList = new ArrayList<>();
        List<String> mainIdList = detailEntityList.stream().map(OverseasWarehouseInboundDetailEntity::getMainId).distinct().collect(Collectors.toList());
        List<OverseasWarehouseInboundEntity> overseasWarehouseInboundEntityList = overseasWarehouseInboundService.listByIds(mainIdList);
        List<FirstMileChangeRecordDTO.AddDTO> changeRecordList = new ArrayList<>();
        for (OverseasWarehouseInboundDTO.ReceivedDTO dto : dtoList) {
            // 查询详情
            OverseasWarehouseInboundDetailEntity detailEntity  = detailEntityList.stream()
                    .filter(e -> StringUtils.equals(e.getId(), dto.getDetailId()))
                    .findFirst()
                    .orElse(null);
            if (Objects.isNull(detailEntity)){
                throw new ServiceException(ApiError.OVERSEAS_WAREHOUSE_INBOUND_DETAIL_NOT_EXIST);
            }
            // 校验
            // 查询提交的平台
            OverseasWarehouseInboundEntity entity = overseasWarehouseInboundEntityList.stream().filter(v->v.getId().equals(detailEntity.getMainId())).findFirst().orElse(null);
            if (Objects.isNull(entity)){
                throw new ServiceException(ApiError.OVERSEAS_WAREHOUSE_INBOUND_NOT_EXIST);
            }
            if (detailEntity.getPackQty() < detailEntity.getReceiveQty() + dto.getReceivedQty()){
                throw new ServiceException("当前签收数量大于剩余签收数量");
            }
            //时间校验 货件名称+SKU在填写月关联的头程分摊已生成，不可修改
            LocalDate date = dto.getReceiveDate().with(TemporalAdjusters.firstDayOfMonth());
            String sourceId = CharSequenceUtil.isNotBlank(entity.getSourceId()) ? entity.getSourceId() : "";//发货单id
            String code = CharSequenceUtil.isNotBlank(entity.getCode()) ? entity.getCode() : "";//业务单号
            FirstMileCostAllocationDTO.DetailDTO detailDTO = new FirstMileCostAllocationDTO.DetailDTO();
            detailDTO.setSourceId(sourceId);
            detailDTO.setBusinessCode(code);
            detailDTO.setReportMonth(date);
            //判断是否存在对应的头程分摊
            List<FirstMileCostAllocationDTO.DetailDTO> detailDTOS = tmsFirstMileLogisticFeign.getRecordBySourceIdAndCode(detailDTO);
            if (CollUtil.isNotEmpty(detailDTOS)){
                throw new ServiceException("发货单【{}】该月【{}】已生成头程分摊，不可修改",entity.getSourceCode(),date);
            }
            detailEntity.setReceiveQty(detailEntity.getReceiveQty() + dto.getReceivedQty());
            detailEntity.setDiffQty(detailEntity.getDiffQty() + dto.getReceivedQty());
            detailEntity.setReceiveTime(dto.getReceiveDate().atStartOfDay());
            // 计算在途数量
            int newTransportQty = detailEntity.getPackQty() - detailEntity.getReceiveQty();
            detailEntity.setTransportQty(newTransportQty);
            if (Objects.equals(detailEntity.getReceiveQty(), detailEntity.getPackQty())){
                detailEntity.setReceiveStatus("already");
            }
            // 添加签收记录
            OverseasWarehouseInboundReceivedEntity receivedEntity = new OverseasWarehouseInboundReceivedEntity(detailEntity.getId(),
                    userInfo.getUserName(),
                    dto.getReceivedQty(),
                    dto.getReceiveDate().atStartOfDay(),
                    "",
                    SignSourceTypeEnum.CHANGE.getCode());
            addReceivedList.add(receivedEntity);
            // 添加主表
            mainResultMap.putIfAbsent(entity.getId(), entity);

            // 添加明细
            List<OverseasWarehouseInboundDetailEntity> currentDetailEntityList = detailResultMap.get(entity.getId());
            if (CollectionUtils.isEmpty(currentDetailEntityList)){
                List<OverseasWarehouseInboundDetailEntity> currentList = new LinkedList<>();
                currentList.add(detailEntity);
                detailResultMap.put(entity.getId(), currentList);
            } else {
                currentDetailEntityList.add(detailEntity);
                detailResultMap.put(entity.getId(), currentDetailEntityList);
            }
            receiverdMap.put(receivedEntity.getDetailId(), receivedEntity.getReceiveQty());
            // 头程调整记录
            changeRecordList.add(OverseasWarehouseInboundConverter.INSTANCE.convertOverseasToChangeRecord(entity, detailEntity, receivedEntity));
            operateLogService.addModuleOperateLog(CharSequenceUtil.format("单号【{}】SKU【{}】新增了一个调整记录,签收【{}】时间【{}】", code, detailEntity.getSkuNo(),receivedEntity.getReceiveQty(),receivedEntity.getReceiveTime()), ModuleTypeEnum.OVERSEAS_WAREHOUSE_INBOUND.getCode(), entity.getId(), "调整签收");
        }
        //调整记录新增
        firstMileChangeRecordFeign.batchAdd(changeRecordList);
        this.updateBatchById(detailEntityList);
        overseasWarehouseInboundReceivedService.saveBatch(addReceivedList);
        List<OverseasWarehouseInboundDetailEntity> allDetailEntityList = this.getByMainIds(mainIdList);
        // 主订单状态
        // 检查是否完全签收
        for (OverseasWarehouseInboundEntity overseasWarehouseInboundEntity : overseasWarehouseInboundEntityList) {
            List<OverseasWarehouseInboundDetailEntity> currentDetailEntityList = allDetailEntityList.stream()
                    .filter(e -> StringUtils.equals(e.getMainId(), overseasWarehouseInboundEntity.getId()))
                    .collect(Collectors.toList());
            if( currentDetailEntityList.stream().allMatch(e-> 0 == e.getDiffQty())){
                overseasWarehouseInboundEntity.setInstockStatus(OverseasInstockStatusEnum.AUTOMATIC_COMPLETION.getCode());
            }else{
                overseasWarehouseInboundEntity.setInstockStatus(OverseasInstockStatusEnum.PARTIAL_SIGNED.getCode());
            }
        }
        overseasWarehouseInboundService.updateBatchById(overseasWarehouseInboundEntityList);
        return resultDTOS;

    }

}
