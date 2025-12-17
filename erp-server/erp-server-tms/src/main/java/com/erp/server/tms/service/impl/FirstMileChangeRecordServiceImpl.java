package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.FirstMileChangeRecordDTO;
import com.erp.model.tms.dto.FirstMileCostAllocationDTO;
import com.erp.model.tms.dto.FirstMileWeightAllocationDTO;
import com.erp.model.tms.entity.FirstMileChangeRecordEntity;
import com.erp.model.tms.entity.FirstMileSkuCostAllocationDetailEntity;
import com.erp.model.tms.enums.*;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.tms.convert.FirstMileChangeRecordConverter;
import com.erp.server.tms.mapper.FirstMileChangeRecordMapper;
import com.erp.server.tms.service.FirstMileChangeRecordService;
import com.erp.server.tms.service.FirstMileSkuCostAllocationDetailService;
import com.erp.server.tms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_FIRST_MILE_CHANGE_RECORD;

/**
 * <p>
 * 头程调整记录 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2025-05-12
 */
@Slf4j
@Service
public class FirstMileChangeRecordServiceImpl extends SuperServiceImpl<FirstMileChangeRecordMapper, FirstMileChangeRecordEntity> implements FirstMileChangeRecordService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private FirstMileSkuCostAllocationDetailService firstMileSkuCostAllocationDetailService;
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(FirstMileChangeRecordDTO.AddDTO addDTO) {
        FirstMileChangeRecordEntity firstMileChangeRecordEntity = new FirstMileChangeRecordEntity();
        BeanMapperUtils.copy(addDTO, firstMileChangeRecordEntity);
        // 数据处理
        handleData(firstMileChangeRecordEntity);
        log.info("开始新增头程调整记录");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_TCTZ);
        firstMileChangeRecordEntity.setCode(code);
        boolean save = super.save(firstMileChangeRecordEntity);
        if(!save) {
            throw new ServiceException("头程调整记录保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "头程调整记录" , firstMileChangeRecordEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FIRST_MILE_CHANGE_RECORD.getCode(), firstMileChangeRecordEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(firstMileChangeRecordEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(FirstMileChangeRecordDTO.UpdateDTO addOrUpdateDTO) {
        FirstMileChangeRecordEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "头程调整记录"));
        FirstMileChangeRecordEntity firstMileChangeRecordEntity =  BeanMapperUtils.map(FirstMileChangeRecordEntity.class, addOrUpdateDTO);
        // 数据处理
        handleData(firstMileChangeRecordEntity);
        log.info("编辑 开始修改头程调整记录数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(firstMileChangeRecordEntity);
        if(!save) {
            throw new ServiceException("头程调整记录保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录头程调整记录日志数据，单号：【{}】", firstMileChangeRecordEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), firstMileChangeRecordEntity.getCode(), "头程调整记录");
        operateLogService.addModuleOperateLogByObj(old, firstMileChangeRecordEntity, ModuleTypeEnum.FIRST_MILE_CHANGE_RECORD.getCode(), firstMileChangeRecordEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<FirstMileChangeRecordDTO.PagingVO> paging(PagingDTO<FirstMileChangeRecordDTO.PagingParamDTO> dto) {
        FirstMileChangeRecordDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<FirstMileChangeRecordDTO.PagingVO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<FirstMileChangeRecordDTO.PagingVO> pageData = baseMapper.paging(query, params);
        List<FirstMileChangeRecordDTO.PagingVO> list = pageData.getRecords();
        fillPagingDb(list);
        return new PagingVO<>(pageData);
    }

    @Override
    public void exportList(FirstMileChangeRecordDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("头程调整记录导出", EXPORT_TMS_FIRST_MILE_CHANGE_RECORD.getCode(), dto);
    }

    @Override
    public List<BatchResultDTO> checkSameDimension(List<FirstMileWeightAllocationDTO.ProductWeightDTO> dtoValidList) {
        if (CollUtil.isEmpty(dtoValidList)) {
            throw new ServiceException("请选择要修改的重量分摊记录");
        }
        List<BatchResultDTO> resultDTOS = new ArrayList<>();
        for (FirstMileWeightAllocationDTO.ProductWeightDTO e : dtoValidList) {
            //先判断校验范围
            if (FirstMileChangeRecordChangeRangeEnum.CURRENT.getCode().equals(e.getChangeRange())) {
                if (e.getProductWeight().compareTo(e.getNewProductWeight()) == 0) {
                    resultDTOS.add(new BatchResultDTO(e.getId(), e.getSourceCode(), "调整后重量与调整前重量一致", false));
                    continue;
                }
                //判断当前值是否存在相同的SKU
                dtoValidList.stream().filter(item -> !Objects.equals(item.getId(), e.getId())
                        && item.getSkuId().equals(e.getSkuId())
                        && item.getSourceCode().equals(e.getSourceCode())
                        && item.getBoxId().equals(e.getBoxId())
                        && item.getBusinessCode().equals(e.getBusinessCode())
                        && item.getPlatformSkuNo().equals(e.getPlatformSkuNo())
                        && item.getNewProductWeight().compareTo(e.getNewProductWeight()) != 0
                ).findFirst().ifPresent(item -> resultDTOS.add(new BatchResultDTO(item.getId(), item.getSourceCode(), CharSequenceUtil.format("【{}】修改值不一致，请重新修改",item.getSkuNo()), false)));
            } else if (FirstMileChangeRecordChangeRangeEnum.BOX.getCode().equals(e.getChangeRange())) {
                //判断是否同箱同SKU
                dtoValidList.stream().filter(item -> !Objects.equals(item.getId(), e.getId())
                        && item.getSkuId().equals(e.getSkuId())
                        && item.getSourceCode().equals(e.getSourceCode())
                        && item.getBoxId().equals(e.getBoxId())
                        && item.getBusinessCode().equals(e.getBusinessCode())
                        && item.getNewProductWeight().compareTo(e.getNewProductWeight()) != 0
                ).findFirst().ifPresent(item -> resultDTOS.add(new BatchResultDTO(item.getId(), item.getSourceCode(), CharSequenceUtil.format("【{}】修改值不一致，请重新修改",item.getSkuNo()), false)));
            } else if (FirstMileChangeRecordChangeRangeEnum.ORDER.getCode().equals(e.getChangeRange())) {
                //判断是否同单同SKU
                dtoValidList.stream().filter(item -> !Objects.equals(item.getId(), e.getId())
                        && item.getSkuId().equals(e.getSkuId())
                        && item.getSourceCode().equals(e.getSourceCode())
                        && item.getBusinessCode().equals(e.getBusinessCode())
                        && item.getNewProductWeight().compareTo(e.getNewProductWeight()) != 0
                ).findFirst().ifPresent(item -> resultDTOS.add(new BatchResultDTO(item.getId(), item.getSourceCode(), CharSequenceUtil.format("【{}】修改值不一致，请重新修改",item.getSkuNo()), false)));
            }
        }
        return resultDTOS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveProductWeight(List<FirstMileWeightAllocationDTO.ProductWeightDTO> dtoValidList) {
        if (CollUtil.isEmpty(dtoValidList)){
            return;
        }
        dtoValidList.forEach(e -> {
            //赋值调整单号
            e.setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_TCTZ));
        });
        List<FirstMileChangeRecordEntity> list = FirstMileChangeRecordConverter.INSTANCE.changeProductWeightDtoToEntityConvert(dtoValidList);
        saveProductWeightByEntity(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveProductWeightByEntity(List<FirstMileChangeRecordEntity> list) {
        //新增记录前修改原来的记录为非最新记录
        list.forEach(e -> {
            if (CharSequenceUtil.isBlank(e.getCode())){
                e.setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_TCTZ));
            }
            if (FirstMileChangeRecordChangeRangeEnum.ORDER.getCode().equals(e.getChangeRange())){
                this.lambdaUpdate()
                        .eq(FirstMileChangeRecordEntity::getSourceType, e.getSourceType())
                        .eq(FirstMileChangeRecordEntity::getBusinessCode, e.getBusinessCode())
                        .eq(FirstMileChangeRecordEntity::getDeliveryCode, e.getDeliveryCode())
                        .eq(FirstMileChangeRecordEntity::getLogisticsBillId, e.getLogisticsBillId())
                        .eq(FirstMileChangeRecordEntity::getSkuNo, e.getSkuNo())
                        .eq(FirstMileChangeRecordEntity::getPlatformSkuNo, e.getPlatformSkuNo())
                        .eq(FirstMileChangeRecordEntity::getCategory, e.getCategory())
                        .eq(FirstMileChangeRecordEntity::getCategoryField, e.getCategoryField())
                        .eq(FirstMileChangeRecordEntity::getIsLatest, Boolean.TRUE)
                        .set(FirstMileChangeRecordEntity::getIsLatest,Boolean.FALSE).update();
            }else if (FirstMileChangeRecordChangeRangeEnum.BOX.getCode().equals(e.getChangeRange())){
                List<String> changeRangeList = new ArrayList<>();
                changeRangeList.add(FirstMileChangeRecordChangeRangeEnum.BOX.getCode());
                changeRangeList.add(FirstMileChangeRecordChangeRangeEnum.CURRENT.getCode());
                this.lambdaUpdate()
                        .eq(FirstMileChangeRecordEntity::getSourceType, e.getSourceType())
                        .eq(FirstMileChangeRecordEntity::getBusinessCode, e.getBusinessCode())
                        .eq(FirstMileChangeRecordEntity::getDeliveryCode, e.getDeliveryCode())
                        .eq(FirstMileChangeRecordEntity::getLogisticsBillId, e.getLogisticsBillId())
                        .eq(FirstMileChangeRecordEntity::getSkuNo, e.getSkuNo())
                        .eq(FirstMileChangeRecordEntity::getBoxId, e.getBoxId())
                        .in(FirstMileChangeRecordEntity::getChangeRange, changeRangeList)
                        .eq(FirstMileChangeRecordEntity::getPlatformSkuNo, e.getPlatformSkuNo())
                        .eq(FirstMileChangeRecordEntity::getCategory, e.getCategory())
                        .eq(FirstMileChangeRecordEntity::getCategoryField, e.getCategoryField())
                        .eq(FirstMileChangeRecordEntity::getIsLatest, Boolean.TRUE)
                        .set(FirstMileChangeRecordEntity::getIsLatest,Boolean.FALSE).update();
            }else {
                this.lambdaUpdate()
                        .eq(FirstMileChangeRecordEntity::getSourceType, e.getSourceType())
                        .eq(FirstMileChangeRecordEntity::getBusinessCode, e.getBusinessCode())
                        .eq(FirstMileChangeRecordEntity::getDeliveryCode, e.getDeliveryCode())
                        .eq(FirstMileChangeRecordEntity::getLogisticsBillId, e.getLogisticsBillId())
                        .eq(FirstMileChangeRecordEntity::getSkuNo, e.getSkuNo())
                        .eq(FirstMileChangeRecordEntity::getBoxId, e.getBoxId())
                        .eq(FirstMileChangeRecordEntity::getSourceId, e.getSourceId())
                        .eq(FirstMileChangeRecordEntity::getChangeRange, FirstMileChangeRecordChangeRangeEnum.CURRENT.getCode())
                        .eq(FirstMileChangeRecordEntity::getPlatformSkuNo, e.getPlatformSkuNo())
                        .eq(FirstMileChangeRecordEntity::getCategory, e.getCategory())
                        .eq(FirstMileChangeRecordEntity::getCategoryField, e.getCategoryField())
                        .eq(FirstMileChangeRecordEntity::getIsLatest, Boolean.TRUE)
                        .set(FirstMileChangeRecordEntity::getIsLatest,Boolean.FALSE).update();
            }
        });
        //新增记录
        boolean saveBatch = this.saveBatch(list);
        if (!saveBatch){
            throw new ServiceException("头程调整记录保存失败");
        }
    }

    @Override
    public FirstMileChangeRecordEntity getProductWeightByParams(String sourceType, String deliveryId, String businessCode, String skuId, String categoryField, String sourceId, String boxId, String platformSkuNo) {
        //先获取当前单头程调整记录
        FirstMileChangeRecordEntity entity = this.lambdaQuery()
                .eq(FirstMileChangeRecordEntity::getSourceType, sourceType)
                .eq(FirstMileChangeRecordEntity::getDeliveryId, deliveryId)
                .eq(FirstMileChangeRecordEntity::getBusinessCode, businessCode)
                .eq(FirstMileChangeRecordEntity::getSkuId, skuId)
                .eq(FirstMileChangeRecordEntity::getPlatformSkuNo, platformSkuNo)
                .eq(FirstMileChangeRecordEntity::getCategoryField, categoryField)
                .eq(FirstMileChangeRecordEntity::getSourceId, sourceId)
                .eq(FirstMileChangeRecordEntity::getBoxId, boxId)
                .eq(FirstMileChangeRecordEntity::getChangeRange, FirstMileChangeRecordChangeRangeEnum.CURRENT.getCode())
                .eq(FirstMileChangeRecordEntity::getIsLatest, Boolean.TRUE)
                .orderByDesc(FirstMileChangeRecordEntity::getCreateTime)
                .last(" limit 1 ").one();
        if (Objects.nonNull(entity)){
            return entity;
        }
        //获取同箱同SKU的头程调整记录
        entity = this.lambdaQuery()
                .eq(FirstMileChangeRecordEntity::getSourceType, sourceType)
                .eq(FirstMileChangeRecordEntity::getDeliveryId, deliveryId)
                .eq(FirstMileChangeRecordEntity::getBusinessCode, businessCode)
                .eq(FirstMileChangeRecordEntity::getSkuId, skuId)
                .eq(FirstMileChangeRecordEntity::getPlatformSkuNo, platformSkuNo)
                .eq(FirstMileChangeRecordEntity::getBoxId, boxId)
                .eq(FirstMileChangeRecordEntity::getCategoryField, categoryField)
                .eq(FirstMileChangeRecordEntity::getChangeRange, FirstMileChangeRecordChangeRangeEnum.BOX.getCode())
                .eq(FirstMileChangeRecordEntity::getIsLatest, Boolean.TRUE)
                .orderByDesc(FirstMileChangeRecordEntity::getCreateTime)
                .last(" limit 1 ").one();
        if (Objects.nonNull(entity)){
            return entity;
        }
        //获取同单同SKU的头程调整记录
        return this.lambdaQuery()
                .eq(FirstMileChangeRecordEntity::getSourceType, sourceType)
                .eq(FirstMileChangeRecordEntity::getDeliveryId, deliveryId)
                .eq(FirstMileChangeRecordEntity::getBusinessCode, businessCode)
                .eq(FirstMileChangeRecordEntity::getSkuId, skuId)
                .eq(FirstMileChangeRecordEntity::getPlatformSkuNo, platformSkuNo)
                .eq(FirstMileChangeRecordEntity::getCategoryField, categoryField)
                .eq(FirstMileChangeRecordEntity::getChangeRange, FirstMileChangeRecordChangeRangeEnum.ORDER.getCode())
                .eq(FirstMileChangeRecordEntity::getIsLatest, Boolean.TRUE)
                .orderByDesc(FirstMileChangeRecordEntity::getCreateTime)
                .last(" limit 1 ").one();
    }

    @Override
    public List<BatchResultDTO> checkPackageSameDimension(List<FirstMileWeightAllocationDTO.PackageSizeDTO> dtoValidList) {
        if (CollUtil.isEmpty(dtoValidList)) {
            throw new ServiceException("请选择要修改的重量分摊记录");
        }
        List<BatchResultDTO> resultDTOS = new ArrayList<>();
        for (FirstMileWeightAllocationDTO.PackageSizeDTO dto : dtoValidList) {
            if (dto.getNewBoxHeight().compareTo(dto.getBoxHeight()) == 0 && dto.getNewBoxLength().compareTo(dto.getBoxLength()) == 0 && dto.getNewBoxWidth().compareTo(dto.getBoxWidth()) == 0 && dto.getNewOutStockWeight().compareTo(dto.getOutStockWeight()) == 0) {
                resultDTOS.add(new BatchResultDTO(dto.getId(), dto.getSourceCode(), "调整后重量/尺寸与调整前重量/尺寸一致", false));
            }
        }
        return resultDTOS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void savePackageWeight(List<FirstMileWeightAllocationDTO.PackageSizeDTO> dtoValidList) {
        if (CollUtil.isEmpty(dtoValidList)){
            return;
        }
        List<FirstMileChangeRecordEntity> entityList = new ArrayList<>();
        for (FirstMileWeightAllocationDTO.PackageSizeDTO dto : dtoValidList){
            if (dto.getNewOutStockWeight().compareTo(dto.getOutStockWeight()) != 0){
                //新增出库重量调整记录
                entityList.add(FirstMileChangeRecordConverter.INSTANCE.changePackageOutStockWeightDtoToEntityConvert(dto).setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_TCTZ)));
            }
            if (dto.getNewBoxHeight().compareTo(dto.getBoxHeight()) != 0 || dto.getNewBoxLength().compareTo(dto.getBoxLength()) != 0 || dto.getNewBoxWidth().compareTo(dto.getBoxWidth()) != 0){
                //新增尺寸调整记录
                entityList.add(FirstMileChangeRecordConverter.INSTANCE.changePackageSizeLengthDtoToEntityConvert(dto).setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_TCTZ)));
                entityList.add(FirstMileChangeRecordConverter.INSTANCE.changePackageSizeWidthDtoToEntityConvert(dto).setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_TCTZ)));
                entityList.add(FirstMileChangeRecordConverter.INSTANCE.changePackageSizeHeightDtoToEntityConvert(dto).setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_TCTZ)));
            }
        }
        if (CollUtil.isEmpty(entityList)){
            return;
        }
        savePackageByEntity(entityList);

    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void savePackageByEntity(List<FirstMileChangeRecordEntity> entityList) {
        //新增记录前修改原来的记录为非最新记录
        entityList.forEach(e -> {
            if (CharSequenceUtil.isBlank(e.getCode())){
                e.setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_TCTZ));
            }
            this.lambdaUpdate()
                    .eq(FirstMileChangeRecordEntity::getSourceType, e.getSourceType())
                    .eq(FirstMileChangeRecordEntity::getBusinessCode, e.getBusinessCode())
                    .eq(FirstMileChangeRecordEntity::getDeliveryCode, e.getDeliveryCode())
                    .eq(FirstMileChangeRecordEntity::getLogisticsBillId, e.getLogisticsBillId())
                    .eq(FirstMileChangeRecordEntity::getBoxId, e.getBoxId())
                    .eq(FirstMileChangeRecordEntity::getCategory, e.getCategory())
                    .eq(FirstMileChangeRecordEntity::getCategoryField, e.getCategoryField())
                    .eq(FirstMileChangeRecordEntity::getIsLatest,Boolean.TRUE)
                    .set(FirstMileChangeRecordEntity::getIsLatest,Boolean.FALSE).update();
        });
        //新增记录
        boolean saveBatch = this.saveBatch(entityList);
        if (!saveBatch){
            throw new ServiceException("头程调整记录保存失败");
        }
    }

    @Override
    public FirstMileChangeRecordEntity getOutStockWeightByParams(String sourceType, String deliveryId, String businessCode, String categoryField, String boxId) {
        return this.lambdaQuery()
                .eq(FirstMileChangeRecordEntity::getSourceType, sourceType)
                .eq(FirstMileChangeRecordEntity::getDeliveryId, deliveryId)
                .eq(FirstMileChangeRecordEntity::getBusinessCode, businessCode)
                .eq(FirstMileChangeRecordEntity::getBoxId, boxId)
                .eq(FirstMileChangeRecordEntity::getCategoryField, categoryField)
                .eq(FirstMileChangeRecordEntity::getIsLatest, Boolean.TRUE)
                .orderByDesc(FirstMileChangeRecordEntity::getCreateTime)
                .last(" limit 1 ").one();
    }

    @Override
    public List<BatchResultDTO> checkCostAllocationSameDimension(ValidList<FirstMileCostAllocationDTO.CostAllocationDTO> dtoValidList) {
        if (CollUtil.isEmpty(dtoValidList)) {
            throw new ServiceException("请选择要修改的费用分摊记录");
        }
        List<BatchResultDTO> resultDTOS = new ArrayList<>();
        for (FirstMileCostAllocationDTO.CostAllocationDTO dto : dtoValidList) {
            if (dto.getAllocatedWeight().equals(dto.getNewAllocatedWeight())
                    && dto.getMidPeriodTransitCost().equals(dto.getNewMidPeriodTransitCost())
                    && dto.getCurrentPeriodAllocatedCost().equals(dto.getNewCurrentPeriodAllocatedCost())
                    && dto.getEndPeriodTransitCost().equals(dto.getNewEndPeriodTransitCost())
                    && dto.getEndPeriodEstimatedCost().equals(dto.getNewEndPeriodEstimatedCost())) {
                resultDTOS.add(new BatchResultDTO(dto.getId(), dto.getSourceCode(), "调整后费用值与调整前费用值全部一致", false));
                continue;
            }
            //限制暂估账单不可调整本期/冲期初/期末在途；限制实际账单不可调整期末暂估
            if (ReconciliationBillTypeEnum.ESTIMATED.getCode().equals(dto.getBillSourceType())){
                if (dto.getMidPeriodTransitCost().compareTo(dto.getNewMidPeriodTransitCost()) != 0 || dto.getCurrentPeriodAllocatedCost().compareTo(dto.getNewCurrentPeriodAllocatedCost()) != 0
                        || dto.getEndPeriodTransitCost().compareTo(dto.getNewEndPeriodTransitCost()) != 0){
                    resultDTOS.add(new BatchResultDTO(dto.getId(), dto.getSourceCode(), CharSequenceUtil.format("【{}】暂估账单不可调整本期/冲期初/期末在途", dto.getSourceCode()), false));
                    continue;
                }
            }
            if (ReconciliationBillTypeEnum.ACTUAL.getCode().equals(dto.getBillSourceType())){
                if (dto.getEndPeriodEstimatedCost().compareTo(dto.getNewEndPeriodEstimatedCost())!= 0){
                    resultDTOS.add(new BatchResultDTO(dto.getId(), dto.getSourceCode(), CharSequenceUtil.format("【{}】实际账单不可调整期末暂估", dto.getSourceCode()), false));
                    continue;
                }
            }
            //分摊重量校验 提交失败：检验分摊重量-是否超出总重量：「SKU」存在历史分摊数据，不支持再次修改重量
            if (CharSequenceUtil.isNotBlank(dto.getNewAllocatedWeight()) && !Objects.equals(dto.getNewAllocatedWeight(),dto.getAllocatedWeight())){
                try {
                    BigDecimal newAllocatedWeight = new BigDecimal(dto.getNewAllocatedWeight());
                }catch (Exception e){
                    resultDTOS.add(new BatchResultDTO(dto.getId(), dto.getSourceCode(), CharSequenceUtil.format("【{}】分摊重量格式错误", dto.getSourceCode()), false));
                }
                //判断是否存在历史分摊数据
                List<FirstMileSkuCostAllocationDetailEntity> skuCostAllocationDetailEntityList = firstMileSkuCostAllocationDetailService.listByReportMonth(dto.getSourceId(), dto.getBusinessCode(), dto.getTransportNo(), dto.getSkuId(), dto.getPlatformSkuNo(), dto.getReportPeriodId());
                if (CollUtil.isNotEmpty(skuCostAllocationDetailEntityList)){
                    resultDTOS.add(new BatchResultDTO(dto.getId(), dto.getSourceCode(), CharSequenceUtil.format("【{}】存在历史分摊数据，不支持再次修改重量", dto.getSkuNo()), false));
                    continue;
                }
            }
            resultDTOS.add(new BatchResultDTO(dto.getId(), dto.getSourceCode(), "校验通过", true));
        }
        return resultDTOS;
    }

    @Override
    public void saveCostAllocation(ValidList<FirstMileCostAllocationDTO.CostAllocationDTO> dtoValidList) {
        if (CollUtil.isEmpty(dtoValidList)){
            return;
        }
        List<FirstMileChangeRecordEntity> entityList = new ArrayList<>();
        for (FirstMileCostAllocationDTO.CostAllocationDTO dto : dtoValidList){
            if (dto.getAllocatedWeight().compareTo(dto.getNewAllocatedWeight()) != 0){
                //新增分摊重量调整记录
                entityList.add(FirstMileChangeRecordConverter.INSTANCE.changeCostAllocatedWeightDtoToEntityConvert(dto).setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_TCTZ)));
                dto.setIsRetry(Boolean.TRUE);
            }
            if (dto.getMidPeriodTransitCost().compareTo(dto.getNewMidPeriodTransitCost()) != 0){
                //新增冲期初在途费用调整记录
                entityList.add(FirstMileChangeRecordConverter.INSTANCE.changeCostMidPeriodTransitDtoToEntityConvert(dto).setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_TCTZ)));
                dto.setIsRetry(Boolean.TRUE);
            }
            if (dto.getCurrentPeriodAllocatedCost().compareTo(dto.getNewCurrentPeriodAllocatedCost()) != 0){
                //新增本期分摊费用调整记录
                entityList.add(FirstMileChangeRecordConverter.INSTANCE.changeCostCurrentPeriodAllocatedDtoToEntityConvert(dto).setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_TCTZ)));
                dto.setIsRetry(Boolean.TRUE);
            }
            //存在本期或冲期初值时，需要把之前的期末在途修改设置未非最新
            if (Objects.nonNull(dto.getIsRetry()) && dto.getIsRetry()){
                this.lambdaUpdate()
                        .eq(FirstMileChangeRecordEntity::getSourceType, FirstMileChangeRecordSourceTypeEnum.FIRSTMILECOST.getCode())
                        .eq(FirstMileChangeRecordEntity::getBusinessCode, dto.getBusinessCode())
                        .eq(FirstMileChangeRecordEntity::getDeliveryCode, dto.getSourceCode())
                        .eq(FirstMileChangeRecordEntity::getLogisticsBillId, dto.getLogisticsBillId())
                        .eq(FirstMileChangeRecordEntity::getSkuId, dto.getSkuId())
                        .eq(FirstMileChangeRecordEntity::getCategory, dto.getFeeType())
                        .eq(FirstMileChangeRecordEntity::getCategoryField, FirstMileChangeRecordCategoryFieldEnum.END_PERIOD_TRANSIT_COST.getCode())
                        .eq(FirstMileChangeRecordEntity::getIsLatest,Boolean.TRUE)
                        .set(FirstMileChangeRecordEntity::getIsLatest,Boolean.FALSE).update();
            }
            if (dto.getEndPeriodTransitCost().compareTo(dto.getNewEndPeriodTransitCost()) != 0){
                //新增期末在途费用调整记录
                entityList.add(FirstMileChangeRecordConverter.INSTANCE.changeCostEndPeriodTransitDtoToEntityConvert(dto).setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_TCTZ)));
                if (!dto.getIsRetry()){
                    firstMileSkuCostAllocationDetailService.updateEndPeriodTransitCost(dto.getDetailId(), dto.getNewEndPeriodTransitCost());
                }
            }
            if (dto.getEndPeriodEstimatedCost().compareTo(dto.getNewEndPeriodEstimatedCost()) != 0){
                //新增期末暂估费用调整记录
                entityList.add(FirstMileChangeRecordConverter.INSTANCE.changeCostEndPeriodEstimatedDtoToEntityConvert(dto).setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_TCTZ)));
                if (!dto.getIsRetry()){
                    firstMileSkuCostAllocationDetailService.updateEndPeriodEstimatedCost(dto.getDetailId(), dto.getNewEndPeriodEstimatedCost());
                }
            }
            if (!Objects.equals(dto.getNewDetailRemark(), dto.getDetailRemark())){
                //新增分明细备注调整记录
                entityList.add(FirstMileChangeRecordConverter.INSTANCE.changeCostDetailRemarkDtoToEntityConvert(dto).setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_TCTZ)));
                if (!dto.getIsRetry()){
                    firstMileSkuCostAllocationDetailService.updateDetailRemark(dto.getDetailId(), dto.getNewDetailRemark());
                }
            }
        }
        saveCostByEntity(entityList);
    }

    @Override
    public void saveCostByEntity(List<FirstMileChangeRecordEntity> entityList) {
        if (CollUtil.isEmpty(entityList)){
            return;
        }
        //新增记录前修改原来的记录为非最新记录
        entityList.forEach(e -> {
            if (CharSequenceUtil.isBlank(e.getCode())){
                e.setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_TCTZ));
            }
            this.lambdaUpdate()
                    .eq(FirstMileChangeRecordEntity::getSourceType, e.getSourceType())
                    .eq(FirstMileChangeRecordEntity::getBusinessCode, e.getBusinessCode())
                    .eq(FirstMileChangeRecordEntity::getDeliveryCode, e.getDeliveryCode())
                    .eq(FirstMileChangeRecordEntity::getLogisticsBillId, e.getLogisticsBillId())
                    .eq(FirstMileChangeRecordEntity::getSkuId, e.getSkuId())
                    .eq(FirstMileChangeRecordEntity::getCategory, e.getCategory())
                    .eq(FirstMileChangeRecordEntity::getCategoryField, e.getCategoryField())
                    .eq(FirstMileChangeRecordEntity::getIsLatest,Boolean.TRUE)
                    .set(FirstMileChangeRecordEntity::getIsLatest,Boolean.FALSE).update();
        });
        //新增记录
        boolean saveBatch = this.saveBatch(entityList);
        if (!saveBatch){
            throw new ServiceException("头程调整记录保存失败");
        }
    }

    @Override
    public FirstMileChangeRecordEntity getCostAllocationByParams(String sourceType, String deliveryId, String businessCode, String categoryField, String skuId, String platformSkuNo, String category, String reportPeriodId, String id) {
        return this.lambdaQuery()
                .eq(FirstMileChangeRecordEntity::getSourceId, id)
                .eq(FirstMileChangeRecordEntity::getSourceType, sourceType)
                .eq(FirstMileChangeRecordEntity::getDeliveryId, deliveryId)
                .eq(FirstMileChangeRecordEntity::getBusinessCode, businessCode)
                .eq(FirstMileChangeRecordEntity::getCategoryField, categoryField)
                .eq(FirstMileChangeRecordEntity::getSkuId, skuId)
                .eq(FirstMileChangeRecordEntity::getPlatformSkuNo, platformSkuNo)
                .eq(FirstMileChangeRecordEntity::getCategory, category)
                .eq(FirstMileChangeRecordEntity::getReportPeriodId, reportPeriodId)
                .eq(FirstMileChangeRecordEntity::getIsLatest, Boolean.TRUE)
                .orderByDesc(FirstMileChangeRecordEntity::getCreateTime)
                .last(" limit 1 ").one();
    }

    @Override
    public void updateCostIsLatest(String sourceType, String businessCode, String sourceCode, String logisticsBillId, String skuId, String feeType, String categoryField) {
        this.lambdaUpdate()
                .eq(FirstMileChangeRecordEntity::getSourceType, sourceType)
                .eq(FirstMileChangeRecordEntity::getBusinessCode, businessCode)
                .eq(FirstMileChangeRecordEntity::getDeliveryCode, sourceCode)
                .eq(FirstMileChangeRecordEntity::getLogisticsBillId, logisticsBillId)
                .eq(FirstMileChangeRecordEntity::getSkuId, skuId)
                .eq(FirstMileChangeRecordEntity::getCategory, feeType)
                .eq(FirstMileChangeRecordEntity::getCategoryField, categoryField)
                .eq(FirstMileChangeRecordEntity::getIsLatest,Boolean.TRUE)
                .set(FirstMileChangeRecordEntity::getIsLatest,Boolean.FALSE).update();
    }

    @Override
    public FirstMileChangeRecordEntity getCostAllocationWeightByParams(String sourceType, String deliveryId, String businessCode, String categoryField, String skuId, String platformSkuNo) {
        return this.lambdaQuery()
                .eq(FirstMileChangeRecordEntity::getSourceType, sourceType)
                .eq(FirstMileChangeRecordEntity::getDeliveryId, deliveryId)
                .eq(FirstMileChangeRecordEntity::getBusinessCode, businessCode)
                .eq(FirstMileChangeRecordEntity::getCategoryField, categoryField)
                .eq(FirstMileChangeRecordEntity::getSkuId, skuId)
                .eq(FirstMileChangeRecordEntity::getPlatformSkuNo, platformSkuNo)
                .eq(FirstMileChangeRecordEntity::getIsLatest, Boolean.TRUE)
                .orderByDesc(FirstMileChangeRecordEntity::getCreateTime)
                .last(" limit 1 ").one();
    }

    private void fillPagingDb(List<FirstMileChangeRecordDTO.PagingVO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //添加类型名称
        list.forEach(e -> {
            if (FirstMileChangeRecordCategoryEnum.BOXNO.getCode().equals(e.getCategory()) && CharSequenceUtil.isNotBlank(e.getChangeRange())){
                e.setCategoryName(FirstMileChangeRecordCategoryEnum.getName(e.getCategory()) + "-" +  e.getBoxNo() + "-" + FirstMileChangeRecordChangeRangeEnum.getName(e.getChangeRange()));
            }else if (FirstMileChangeRecordCategoryEnum.BOXNO.getCode().equals(e.getCategory())){
                e.setCategoryName(FirstMileChangeRecordCategoryEnum.getName(e.getCategory()) + "-" +  e.getBoxNo());
            }else {
                e.setCategoryName(FirstMileChangeRecordCategoryEnum.getName(e.getCategory()));
            }
            e.setCategoryFieldName(FirstMileChangeRecordCategoryFieldEnum.getName(e.getCategoryField()));
            e.setChangeRangeName(FirstMileChangeRecordChangeRangeEnum.getName(e.getChangeRange()));
            e.setSourceTypeName(FirstMileChangeRecordSourceTypeEnum.getName(e.getSourceType()));
            e.setTypeName(FirstMileChangeRecordTypeEnum.getName(e.getType()));
        });
    }
    /**
    * 新增修改处理数据
    */
    private void handleData(FirstMileChangeRecordEntity firstMileChangeRecordEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
