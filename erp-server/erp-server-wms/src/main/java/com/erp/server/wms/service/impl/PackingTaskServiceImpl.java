package com.erp.server.wms.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.*;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.AutoGenerateBillDTO;
import com.erp.model.tms.enums.BillGenerateTimingEnum;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.dto.WmsCartonDetailDTO;
import com.erp.model.wms.dto.WmsCartonSpecDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.tms.feign.TmsDeclareBillFeign;
import com.erp.rpc.tms.feign.TmsFirstMileLogisticFeign;
import com.erp.server.wms.convert.PackingConverter;
import com.erp.server.wms.mapper.PackingTaskMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.PackingTaskDTO;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 装箱任务表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-07-02
 */
@Slf4j
@Service
public class PackingTaskServiceImpl extends SuperServiceImpl<PackingTaskMapper, PackingTaskEntity> implements PackingTaskService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private SoDeliveryNoticeDetailService soDeliveryNoticeDetailService;
    @Resource
    private PackingTaskDetailService packingTaskDetailService;
    @Autowired
    private FirstMileDeliveryDetailService firstMileDeliveryDetailService;
    @Resource
    private FirstMileDeliveryService firstMileDeliveryService;
    @Resource
    private OverseasWarehouseInboundService overseasWarehouseInboundService;
    @Resource
    private SoDeliveryNoticeService soDeliveryNoticeService;
    @Resource
    private WmsCartonSpecService wmsCartonSpecService;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private TmsFirstMileLogisticFeign tmsFirstMileLogisticFeign;
    @Resource
    private TmsDeclareBillFeign tmsDeclareBillFeign;
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(PackingTaskDTO.AddDTO addDTO) {
        PackingTaskEntity packingTaskEntity = new PackingTaskEntity();
        BeanMapperUtils.copy(addDTO, packingTaskEntity);

        // 数据处理
        handleData(packingTaskEntity);

        log.info("开始新增装箱任务单");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        packingTaskEntity.setCode(code);
        boolean save = super.save(packingTaskEntity);
        if(!save) {
            throw new ServiceException("装箱任务单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "装箱任务单" , packingTaskEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, packingTaskEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(packingTaskEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(PackingTaskDTO.UpdateDTO updateDTO) {
        PackingTaskEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "装箱任务单"));
        PackingTaskEntity packingTaskEntity =  BeanMapperUtils.map(PackingTaskEntity.class, updateDTO);

        // 数据处理
        handleData(packingTaskEntity);
        log.info("编辑 开始修改装箱任务单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(packingTaskEntity);
        if(!save) {
            throw new ServiceException("装箱任务单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录装箱任务单日志数据，单号：【{}】", packingTaskEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), packingTaskEntity.getCode(), "装箱任务单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, packingTaskEntity, null, packingTaskEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public void addPackingByB2BDelivery(SoDeliveryNoticeEntity soDeliveryNoticeEntity) {
        //关联单号是否已存在装箱任务
        List<PackingTaskEntity> taskEntityList = listBySourceIdAndSourceType(soDeliveryNoticeEntity.getId(), PickingSourceTypeEnum.B2B.getCode());
        if (CollectionUtils.isNotEmpty(taskEntityList)){
            return;
        }
        PackingTaskEntity packingTaskEntity = PackingConverter.INSTANCE.b2bDeliveryToPackingTask(soDeliveryNoticeEntity);
        //查询明细
        List<SoDeliveryNoticeDetailEntity> detailEntityList = soDeliveryNoticeDetailService.listDetailByMainId(soDeliveryNoticeEntity.getId());
        packingTaskEntity.setDeliveryQty(detailEntityList.stream().map(SoDeliveryNoticeDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO,Integer::sum));
        packingTaskEntity.setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.ZXRW));
        this.save(packingTaskEntity);
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "装箱任务单" , packingTaskEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PACKING_TASK.getCode(), packingTaskEntity.getId(), "新增操作");
        List<PackingTaskDetailEntity> taskDetailList = PackingConverter.INSTANCE.b2bDeliveryDetailToPackingTaskDetail(detailEntityList);
        taskDetailList.forEach(packingTaskDetailEntity -> packingTaskDetailEntity.setMainId(packingTaskEntity.getId()));
        //新增任务明细
        packingTaskDetailService.saveBatch(taskDetailList);
    }

    /**
     * 根据来源订单及类型统计是否已经创建任务
     * @param sourceId
     * @param sourceType
     * @return
     */
    @Override
    public List<PackingTaskEntity> listBySourceIdAndSourceType(String sourceId, String sourceType) {
        if (StringUtils.isBlank(sourceId) && StringUtils.isBlank(sourceType)){
            return Collections.emptyList();
        }
        return lambdaQuery().eq(PackingTaskEntity::getSourceId, sourceId).eq(PackingTaskEntity::getSourceType,sourceType).list();
    }

    @Override
    public WmsCartonSpecDTO.WmsCartonSpecView packingView(String id) {
        WmsCartonSpecDTO.WmsCartonSpecView cartonView = new WmsCartonSpecDTO.WmsCartonSpecView();
        //装箱任务
        PackingTaskEntity taskEntity = this.getById(id);
        if (Objects.isNull(taskEntity)){
            throw new ServiceException(ApiError.ERROR_92141);
        }
        if (PickingSourceTypeEnum.B2B.getCode().equals(taskEntity.getSourceType())){
            SoDeliveryNoticeEntity soDeliveryNoticeEntity = soDeliveryNoticeService.getById(id);
            if (Objects.isNull(soDeliveryNoticeEntity)){
                throw new ServiceException(ApiError.ERROR_92144);
            }
            //>仅可操作关联单号未审核通过时候可编辑修改
            if(ApproveStatusEnum.APPROVE.getStatus().equals(soDeliveryNoticeEntity.getApproveStatus())){
                throw new ServiceException(ApiError.ERROR_92140, soDeliveryNoticeEntity.getCode());
            }
        }else {
            FirstMileDeliveryEntity firstMileDeliveryEntity = firstMileDeliveryService.getById(id);
            if (Objects.isNull(firstMileDeliveryEntity)){
                throw new ServiceException(ApiError.ERROR_92142, taskEntity.getSourceCode());
            }
            //>仅可操作关联单号未审核通过时候可编辑修改
            if(firstMileDeliveryEntity.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())){
                throw new ServiceException(ApiError.ERROR_92140, firstMileDeliveryEntity.getCode());
            }
            //已下推入库单，不允许修改装箱信息
            OverseasWarehouseInboundEntity overseasWarehouseInbound = overseasWarehouseInboundService.getBySourceId(firstMileDeliveryEntity.getId(), OverseasInstockStatusEnum.CANCELED.getCode());
            if (ObjectUtil.isNotEmpty(overseasWarehouseInbound) && !OverseasInstockStatusEnum.TO_BE_SHIPPED.getCode().equals(overseasWarehouseInbound.getInstockStatus())) {
                throw new ServiceException(ApiError.OVERSEAS_WAREHOUSE_INBOUND_EXIST, overseasWarehouseInbound.getCode());
            }
        }
        //装箱任务
        cartonView.setTaskId(taskEntity.getId());
        cartonView = wmsCartonSpecService.getCartonViewByPackingTaskId(taskEntity);
        //查询装箱详情
        cartonView.setSourceId(taskEntity.getSourceId());
        cartonView.setSourceCode(taskEntity.getSourceCode());
        return cartonView;
    }

    @Override
    public List<WmsCartonSpecDTO.GroupSkuDTO> listGroupSkuById(String id) {
        List<WmsCartonSpecDTO.GroupSkuDTO> list = baseMapper.listGroupSkuByMainId(id);
        //查询产品信息
        List<String> skuIdList = list.stream().map(WmsCartonSpecDTO.GroupSkuDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);
        //查询已装箱数
        List<WmsCartonSpecDTO.PackingQtyDTO> packingQtyDTOS = wmsCartonSpecService.listPackingQtyByMainId(id, null);
        for (WmsCartonSpecDTO.GroupSkuDTO groupSkuDTO : list) {
            //待装箱数量=发货数量-已装箱数量
            int usePackQty = packingQtyDTOS.stream()
                    .filter(req -> req.getSkuId().equals(groupSkuDTO.getId())
                            && req.getSkuId().equals(groupSkuDTO.getSkuId()))
                    .mapToInt(WmsCartonSpecDTO.PackingQtyDTO::getPackQty).sum();
            groupSkuDTO.setWaitPackQty(groupSkuDTO.getDeliveryQty() - usePackQty);
            groupSkuDTO.setPackQty(usePackQty);
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(groupSkuDTO.getSkuId())).findFirst().orElse(new SkuVO());
            groupSkuDTO.setProductName(skuVO.getSkuName());
            groupSkuDTO.setSkuNo(skuVO.getSkuNo());
        }
        return list;
    }

    @Override
    public Boolean packingSave(WmsCartonSpecDTO.WmsCartonAdd dto) {
        PackingTaskEntity packingTask = this.getById(dto.getTaskId());
        if (Objects.isNull(packingTask)){
            throw new ServiceException(ApiError.ERROR_92141);
        }
        SoDeliveryNoticeEntity soDeliveryNoticeEntity = null;
        FirstMileDeliveryEntity firstMileDeliveryEntity = null;
        if (PickingSourceTypeEnum.B2B.getCode().equals(packingTask.getSourceType())){
            //待审核的数据可以上传装箱数据
            soDeliveryNoticeEntity = soDeliveryNoticeService.getById(dto.getSourceId());
            checkSoDeliveryNoticeStatus(soDeliveryNoticeEntity);
        }else {
            //待审核的数据可以上传装箱数据
            firstMileDeliveryEntity = firstMileDeliveryService.getById(dto.getSourceId());
            checkFirstMileStatus(firstMileDeliveryEntity);
        }
        //删除原装箱信息
        wmsCartonSpecService.deleteCarton(dto.getTaskId());
        //新增装箱信息
        for (WmsCartonSpecDTO.AddDTO addDTO : dto.getWmsCartonList()) {
            //新增装箱信息
            wmsCartonSpecService.add(addDTO, dto.getTaskId());
            //根据主表id分组sku查询发货及待装箱数
            List<WmsCartonSpecDTO.PackDateDTO> packDateDTOS = wmsCartonSpecService.listPackDateByPackingTaskId(dto.getTaskId());
//            List<String> ids = packDateDTOS.stream().map(WmsCartonSpecDTO.PackDateDTO::getId).distinct().collect(Collectors.toList());
            List<PackingTaskDetailEntity> taskDetailEntityList = packingTaskDetailService.listByMainIds(Collections.singletonList(dto.getTaskId()));
            //校验打包数量
            checkDeliveryQty(packDateDTOS, taskDetailEntityList);
        }
        //根据主表id分组sku查询发货及待装箱数
        List<WmsCartonSpecDTO.GroupSkuDTO> groupSkuList = this.listGroupSkuById(dto.getTaskId());
        //当所有产品待装箱数量为0时，状态自动变更为已装箱
        List<WmsCartonSpecDTO.GroupSkuDTO> groupSkuDTOList = groupSkuList.stream().filter(req -> req.getWaitPackQty() > 0).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(groupSkuDTOList) && PickingSourceTypeEnum.B2B.getCode().equals(packingTask.getSourceType())) {
            autoGenerateSoDeliveryNotice(packingTask,soDeliveryNoticeEntity);
        }else if (CollectionUtils.isEmpty(groupSkuDTOList) && (PickingSourceTypeEnum.FBA.getCode().equals(packingTask.getSourceType()) || PickingSourceTypeEnum.THIRD.getCode().equals(packingTask.getSourceType()))){
            autoGenerateFirstMileDelivery(packingTask,firstMileDeliveryEntity);
        } else {
            updatePackingStatus(packingTask.getSourceId(),packingTask.getSourceType(), PackingStatusEnum.NOT_PACKING.getCode());
        }
        return Boolean.TRUE;
    }

    @Override
    public WmsCartonSpecDTO.ListPackingDTO listPacking(String id) {
        WmsCartonSpecDTO.ListPackingDTO listPackingDTO = new WmsCartonSpecDTO.ListPackingDTO();
        PackingTaskEntity packingTask = this.getById(id);
        if (Objects.isNull(packingTask)){
            throw new ServiceException(ApiError.ERROR_92141);
        }
        if (PickingSourceTypeEnum.B2B.getCode().equals(packingTask.getSourceType())){
            SoDeliveryNoticeEntity soOutstock = soDeliveryNoticeService.getById(packingTask.getSourceId());
            if (Objects.isNull(soOutstock)){
                throw new ServiceException(ApiError.ERROR_92143);
            }
            listPackingDTO.setId(soOutstock.getId());
            listPackingDTO.setCode(soOutstock.getCode());
        }else {
            FirstMileDeliveryEntity firstMileDelivery = firstMileDeliveryService.getById(packingTask.getSourceId());
            if (Objects.isNull(firstMileDelivery)){
                throw new ServiceException(ApiError.NOT_EXIST_BILL, "发货单");
            }
            listPackingDTO.setId(firstMileDelivery.getId());
            listPackingDTO.setCode(firstMileDelivery.getCode());
        }
        //获取总箱数
        List<WmsCartonSpecEntity> cartonSpecEntityList = wmsCartonSpecService.listByMainIds(Collections.singletonList(id));
        int boxQty = cartonSpecEntityList.stream().mapToInt(WmsCartonSpecEntity::getBoxQty).sum();
        listPackingDTO.setBoxQty(boxQty);

        //箱子明细信息
        List<WmsCartonDetailDTO.ListPackingDetailDTO> detailList = baseMapper.listPackingDetail(Collections.singletonList(id));
        listPackingDTO.setDetailList(detailList);
        return listPackingDTO;
    }

    private void autoGenerateFirstMileDelivery(PackingTaskEntity packingTask, FirstMileDeliveryEntity firstMileDeliveryEntity) {
        updatePackingStatus(packingTask.getSourceId(),packingTask.getSourceType(), PackingStatusEnum.PACKING.getCode());
        //走TMS自动生成物流单逻辑
        AutoGenerateBillDTO autoGenerateBillDTO = AutoGenerateBillDTO.builder()
                .id(firstMileDeliveryEntity.getId())
                .billGenerateTimingEnum(BillGenerateTimingEnum.AFTER_PACKING)
                .sourceTypeEnum(SourceTypeEnum.FIRST_MILE_DELIVERY)
                .firstMileDeliveryEntity(firstMileDeliveryEntity)
                .build();
        try {
            if(FmDeliveryLogisticsStatusEnum.WAIT.equals(firstMileDeliveryEntity.getLogisticsStatus())){
                Boolean autoGenerateResult = tmsFirstMileLogisticFeign.autoGenerateFirstMileLogistic(autoGenerateBillDTO);
                if(autoGenerateResult){
                    FirstMileDeliveryDTO.UpdateStatusDTO updateStatusDTO = new FirstMileDeliveryDTO.UpdateStatusDTO();
                    updateStatusDTO.setIds(Collections.singletonList(firstMileDeliveryEntity.getId()));
                    updateStatusDTO.setLogisticsStatus(FmDeliveryLogisticsStatusEnum.FINISH.getCode());
                    firstMileDeliveryService.updateStatus(updateStatusDTO);
                }
            }
        }catch (Exception e){
            log.error("头程发货单{} 装箱后自动生成物流单失败>>>>>>{}", firstMileDeliveryEntity.getCode(), e.getMessage());
            throw new ServiceException(StrUtil.format("头程发货单{} 装箱后自动生成物流单失败>>>>>>{}", firstMileDeliveryEntity.getCode(), e.getMessage()));
        }

        try {
            if(WmsDeclareStatusEnum.WAIT.equals(firstMileDeliveryEntity.getDeclareStatus())){
                Boolean autoGenerateResult = tmsDeclareBillFeign.autoGenerateFirstMileDeclare(autoGenerateBillDTO);
                if(autoGenerateResult){
                    FirstMileDeliveryDTO.UpdateStatusDTO updateStatusDTO = new FirstMileDeliveryDTO.UpdateStatusDTO();
                    updateStatusDTO.setIds(Arrays.asList(firstMileDeliveryEntity.getId()));
                    updateStatusDTO.setDeclareStatus(WmsDeclareStatusEnum.FINISH.getCode());
                    firstMileDeliveryService.updateStatus(updateStatusDTO);
                }
            }
        }catch (Exception e){
            log.error("头程发货单{} 装箱后自动生成报关单失败>>>>>>{}", firstMileDeliveryEntity.getCode(), e.getMessage());
            throw new ServiceException(StrUtil.format("头程发货单{} 装箱后自动生成报关单失败>>>>>>{}", firstMileDeliveryEntity.getCode(), e.getMessage()));
        }
    }

    private void autoGenerateSoDeliveryNotice(PackingTaskEntity packingTask, SoDeliveryNoticeEntity soDeliveryNoticeEntity) {
//        updatePackingStatus(packingTask.getSourceId(),packingTask.getSourceType(), PackingStatusEnum.PACKING.getCode());
//        //走TMS自动生成报关单逻辑
//        if (!"CN".equalsIgnoreCase(soOutstock.getCountry()) && soOutstock.getDeclareStatus().equals(WmsDeclareStatusEnum.WAIT.getCode()) && soOutstock.getOrderType().equals(OrderTypeEnum.B2B.getCode())) {
//            //走TMS自动生成逻辑
//            AutoGenerateBillDTO autoGenerateBillDTO = AutoGenerateBillDTO.builder()
//                    .id(soOutstock.getId())
//                    .billGenerateTimingEnum(BillGenerateTimingEnum.AFTER_PACKING)
//                    .sourceTypeEnum(SourceTypeEnum.SO_OUTSTOCK)
//                    .soOutstockEntity(soOutstock)
//                    .build();
//            try {
//                Boolean autoGenerateResult = tmsDeclareBillFeign.autoGenerateB2bDeclare(autoGenerateBillDTO);
//                if(autoGenerateResult){
//                    TmsDeclareBillDTO.UpdateStatusDTO updateStatusDTO = new TmsDeclareBillDTO.UpdateStatusDTO();
//                    updateStatusDTO.setIds(Collections.singletonList(soOutstock.getId()));
//                    updateStatusDTO.setDeclareStatus(WmsDeclareStatusEnum.FINISH.getCode());
//                    soOutstockService.updateStatus(updateStatusDTO);
//                }
//            }catch (Exception e){
//                log.error("销售出库单{} 装箱后自动生成报关单失败>>>>>>{}", soOutstock.getCode(), e.getMessage());
//                throw new ServiceException(StrUtil.format("销售出库单{} 装箱后自动生成报关单失败>>>>>>{}", soOutstock.getCode(), e.getMessage()));
//            }
//        }
    }

    private void checkFirstMileStatus(FirstMileDeliveryEntity firstMileDeliveryEntity) {
        if (Objects.isNull(firstMileDeliveryEntity)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "发货单");
        }
        if (!ApproveStatusEnum.APPROVE_ING.getStatus().equals(firstMileDeliveryEntity.getApproveStatus())) {
            throw new ServiceException(ApiError.APPROVE_ING_IS_PACKING);
        }
        if(PackingStatusEnum.PACKING.getCode().equals(firstMileDeliveryEntity.getPackingStatus())
                && (FmDeliveryLogisticsStatusEnum.FINISH.equals(firstMileDeliveryEntity.getLogisticsStatus())
                || WmsDeclareStatusEnum.FINISH.equals(firstMileDeliveryEntity.getDeclareStatus()))){
            throw new ServiceException("物流单/报关单已生成，不支持修改");
        }
        //已装箱的数据，如果未下推入库单，或者下推的入库单待提交时，可以再次修改装箱信息，否则提示：已下推海外仓入库单【单号】，不允许修改装箱数据（装箱页面保存时校验）
        List<OverseasWarehouseInboundEntity> overseasWarehouseInboundEntities = overseasWarehouseInboundService.listBySourceIds(Collections.singletonList(firstMileDeliveryEntity.getId()));
        long count = overseasWarehouseInboundEntities.stream()
                .filter(req -> !req.getInstockStatus().equals(OverseasInstockStatusEnum.TO_BE_SHIPPED.getCode())
                        && !req.getInstockStatus().equals(OverseasInstockStatusEnum.CANCELED.getCode())
                ).count();
        if (count > 0) {
            List<String> codes = overseasWarehouseInboundEntities.stream().map(OverseasWarehouseInboundEntity::getCode).distinct().collect(Collectors.toList());
            throw new ServiceException(ApiError.OVERSEAS_WAREHOUSE_INBOUND_EXIST_NOT_UPDATE, String.join(",",codes));
        }
    }

    private void checkSoDeliveryNoticeStatus(SoDeliveryNoticeEntity soDeliveryNoticeEntity) {
        if (Objects.isNull(soDeliveryNoticeEntity)){
            throw new ServiceException(ApiError.ERROR_92144);
        }
        if (!ApproveStatusEnum.APPROVE_ING.getStatus().equals(soDeliveryNoticeEntity.getApproveStatus())) {
            throw new ServiceException(ApiError.APPROVE_ING_IS_PACKING);
        }
//        //已装箱状态并且已报关不允许再次修改装箱数据
//        if (PackingStatusEnum.PACKING.getCode().equals(soOutstock.getPackingStatus()) && WmsDeclareStatusEnum.FINISH.getCode().equals(soOutstock.getDeclareStatus())) {
//            throw new ServiceException(ApiError.SO_OUTSTOCK_NOT_PACKING);
//        }
//
//        //只允许B2B订单装箱
//        if (!OrderTypeEnum.B2B.getCode().equals(soOutstock.getOrderType())) {
//            throw new ServiceException(ApiError.B2B_ORDER_IS_PACK);
//        }
    }

    /**
     * 更新
     * @param sourceId
     * @param sourceType
     * @param status
     */
    private void updatePackingStatus(String sourceId, String sourceType, String status) {
        if (PickingSourceTypeEnum.B2B.getCode().equals(sourceType)){
            soDeliveryNoticeService.updatePackingStatus(sourceId,status);
        }else {
            firstMileDeliveryService.updatePackingStatus(sourceId,status);
        }
    }

    /**
     * 校验打包数量
     * @param packDateDTOS
     * @param taskDetailEntityList
     */
    private void checkDeliveryQty(List<WmsCartonSpecDTO.PackDateDTO> packDateDTOS, List<PackingTaskDetailEntity> taskDetailEntityList) {
        for (WmsCartonSpecDTO.PackDateDTO packDateDTO : packDateDTOS) {
            //发货数量
            int deliveryQty = taskDetailEntityList.stream().filter(req -> req.getSkuId().equals(packDateDTO.getSkuId())).mapToInt(PackingTaskDetailEntity::getDeliveryQty).sum();
            //待装箱数量=发货数量-所有已装箱数量
            int packQtySum = packDateDTOS.stream().filter(req -> req.getSkuId().equals(packDateDTO.getSkuId())).mapToInt(WmsCartonSpecDTO.PackDateDTO::getPackQty).sum();
            if (deliveryQty < packQtySum) {
                throw new ServiceException(ApiError.PACKING_QTY_NOT_GT_WAIT_PACKING_QTY, packDateDTO.getBoxSpecNo(), packDateDTO.getSkuNo());
            }
        }
    }
    @Override
    public void addPackingByFirstMileDelivery(FirstMileDeliveryEntity firstMileDeliveryEntity) {
        String demandType = firstMileDeliveryEntity.getDemandType();
//        String sourceType = FbaDemandTypeEnum.DEMAND_OVERSEAS_WAREHOUSE.getCode().equals(demandType)? PickingSourceTypeEnum.THIRD.getCode(): PickingSourceTypeEnum.FBA.getCode();
        //关联单号是否已存在装箱任务
        List<PackingTaskEntity> taskEntityList = listBySourceIdAndSourceType(firstMileDeliveryEntity.getId(), demandType);
        if (CollectionUtils.isNotEmpty(taskEntityList)){
            return;
        }
        PackingTaskEntity packingTaskEntity = PackingConverter.INSTANCE.firstMileDeliveryToPackingTask(firstMileDeliveryEntity,demandType);
        //查询明细
        List<FirstMileDeliveryDetailEntity> detailEntityList = firstMileDeliveryDetailService.listDetailByMainId(firstMileDeliveryEntity.getId());
        packingTaskEntity.setDeliveryQty(detailEntityList.stream().map(FirstMileDeliveryDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO,Integer::sum));
        packingTaskEntity.setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.ZXRW));
        this.save(packingTaskEntity);
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "装箱任务单" , packingTaskEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PACKING_TASK.getCode(), packingTaskEntity.getId(), "新增操作");
        List<PackingTaskDetailEntity> taskDetailList = PackingConverter.INSTANCE.firstMileDeliveryDetailToPackingTaskDetail(detailEntityList);
        taskDetailList.forEach(packingTaskDetailEntity -> packingTaskDetailEntity.setMainId(packingTaskEntity.getId()));
        //新增任务明细
        packingTaskDetailService.saveBatch(taskDetailList);
    }
    /**
     * 装箱任务-分页查询
     * @param dto
     * @return
     */
    @Override
    public PagingVO<PackingTaskDTO.PagingViewDTO> paging(PagingDTO<PackingTaskDTO.PagingParamDTO> dto) {
        Page<PackingTaskDTO.PagingViewDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        PackingTaskDTO.PagingParamDTO params = dto.getParams();
        IPage<PackingTaskDTO.PagingViewDTO> pageData = baseMapper.paging(query, params);
        //补充数据
        buildPackingTask(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    /**
     * 补充数据
     * @param records
     */
    private void buildPackingTask(List<PackingTaskDTO.PagingViewDTO> records) {
        if (CollectionUtils.isEmpty(records)){
            return;
        }
        List<String> taskIds = records.stream().map(PackingTaskDTO.PagingViewDTO::getId).distinct().collect(Collectors.toList());
        //装箱状态 称重状态 异常原因 装箱数量 装箱重量（设备更新） 拣货数量
        List<PackingTaskDTO.StatusDTO> statusDTOList = baseMapper.selectPackingStatusByIds(taskIds);
        Map<String, PackingTaskDTO.StatusDTO> statusDTOMap = statusDTOList.stream().collect(Collectors.toMap(PackingTaskDTO.StatusDTO::getId, Function.identity()));
        records.forEach(pagingViewDTO -> {
            PackingTaskDTO.StatusDTO statusDTO = statusDTOMap.get(pagingViewDTO.getId());
            if (Objects.nonNull(statusDTO)){
                String packingStatus = StringUtils.isBlank(statusDTO.getPackingStatus())? PackingTaskStatusEnum.UNPACKED.getCode() : statusDTO.getPackingStatus();
                pagingViewDTO.setPackingStatus(packingStatus);
                pagingViewDTO.setPackingStatusName(PackingTaskStatusEnum.getName(packingStatus));
                String weightingStatus = StringUtils.isBlank(statusDTO.getWeightingStatus()) ? PackingWeightStatusEnum.UNWEIGHTED.getCode() : statusDTO.getWeightingStatus();
                pagingViewDTO.setWeightingStatus(weightingStatus);
                pagingViewDTO.setWeightingStatusName(PackingWeightStatusEnum.getName(weightingStatus));
                pagingViewDTO.setErrorMsg(StringUtils.isBlank(statusDTO.getErrorMsg())? "" : statusDTO.getErrorMsg());
                pagingViewDTO.setPackedQty(Objects.isNull(statusDTO.getPackedQty())? MathUtil.ZERO: statusDTO.getPackedQty());
                pagingViewDTO.setPickedQty(statusDTO.getPickedQty());
                BigDecimal packageWeight = Objects.isNull(statusDTO.getPackingWeight()) ? BigDecimal.ZERO : MathUtil.divide(statusDTO.getPackingWeight(), MathUtil.BigDecimal_1000);
                pagingViewDTO.setPackageWeight(packageWeight);
                pagingViewDTO.setPackageWeightStr(packageWeight.toPlainString() + UnitEnum.WeightUnitEnum.KG.getName());
            }else {
                pagingViewDTO.setPackingStatus(PackingTaskStatusEnum.UNPACKED.getCode());
                pagingViewDTO.setPackingStatusName(PackingTaskStatusEnum.UNPACKED.getName());
                pagingViewDTO.setWeightingStatus(PackingWeightStatusEnum.UNWEIGHTED.getCode());
                pagingViewDTO.setWeightingStatusName(PackingWeightStatusEnum.UNWEIGHTED.getName());
                pagingViewDTO.setPackedQty(MathUtil.ZERO);
            }
            String sourceType = pagingViewDTO.getSourceType();
            pagingViewDTO.setSourceTypeName(PickingSourceTypeEnum.getName(sourceType));
        });
    }

    /**
     * 按照分类进行统计
     * @param dto
     * @return
     */
    @Override
    public List<PackingTaskDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<PackingTaskDTO.TypeCountDTO> countList = baseMapper.listTabCount(dto.getPermissionSql());
        //重构数据
        List<PackingTaskDTO.TabListDTO> tabList = new ArrayList<>(3);
        Integer unpackedCount = countList.stream().filter(e -> PackingTaskStatusEnum.UNPACKED.getCode().equals(e.getType())).map(PackingTaskDTO.TypeCountDTO::getCount).findFirst().orElse(MathUtil.ZERO);
        tabList.add(new PackingTaskDTO.TabListDTO(PackingTaskStatusEnum.UNPACKED.getCode(),PackingTaskStatusEnum.UNPACKED.getName(), unpackedCount));
        Integer packingCount = countList.stream().filter(e -> PackingTaskStatusEnum.PACKING.getCode().equals(e.getType())).map(PackingTaskDTO.TypeCountDTO::getCount).findFirst().orElse(MathUtil.ZERO);
        tabList.add(new PackingTaskDTO.TabListDTO(PackingTaskStatusEnum.PACKING.getCode(),PackingTaskStatusEnum.PACKING.getName(), packingCount));
        Integer packedCount = countList.stream().filter(e -> PackingTaskStatusEnum.PACKED.getCode().equals(e.getType())).map(PackingTaskDTO.TypeCountDTO::getCount).findFirst().orElse(MathUtil.ZERO);
        tabList.add(new PackingTaskDTO.TabListDTO(PackingTaskStatusEnum.PACKED.getCode(),PackingTaskStatusEnum.PACKED.getName(), packedCount));
        return tabList;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(PackingTaskEntity packingTaskEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
