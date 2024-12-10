package com.erp.server.tms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.WriteTable;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.ThirdConstants;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.constant.EnumMessage;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.FmDeliveryLogisticsStatusEnum;
import com.erp.model.scm.dto.AttachmentDTO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.entity.SysPostUserEntity;
import com.erp.model.sys.vo.FsBatchSendMessageDTO;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.dto.excel.FmLogisticsBillCostExcelDTO;
import com.erp.model.tms.dto.excel.FmLogisticsBillExcelDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.*;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.dto.WmsCartonDetailDTO;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.model.wms.enums.PackingTaskStatusEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.sys.feign.SysPostFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.FirstMileDeliveryDetailFeign;
import com.erp.rpc.wms.feign.OverseaWarehouseInboundFeign;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.tms.convert.FmLogisticsConverter;
import com.erp.server.tms.convert.TmsFirstMileReconciliationConverter;
import com.erp.server.tms.listener.FmLogisticsBillCostExcelListener;
import com.erp.server.tms.listener.FmLogisticsBillExcelListener;
import com.erp.server.tms.mapper.LogisticsBillMapper;
import com.erp.server.tms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <p>
 * 头程物流单 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-03-19
 */
@Slf4j
@Service
public class TmsFirstMileLogisticServiceImpl extends SuperServiceImpl<LogisticsBillMapper, LogisticsBillEntity> implements TmsFirstMileLogisticService {

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private WmsFirstMileDeliveryFeign wmsFirstMileDeliveryFeign;

    @Resource
    private LogisticsBillCostService logisticsBillCostService;

    @Resource
    private ShippingTemplateService shippingTemplateService;

    @Resource
    private TmsCostDetailService logisticsBillCostDetailService;

    @Resource
    private AttachmentService attachmentService;

    @Resource
    private TmsCfgSailingService sailingService;

    @Resource
    private LogisticsChannelService logisticsChannelService;

    @Resource
    private LogisticsBillDetailService logisticsBillDetailService;

    @Resource
    private LogisticsSupplierService logisticsSupplierService;

    @Resource
    private ShopInfoFeign shopInfoFeign;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private LogisticsTrackService logisticsTrackService;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private CfgSettingService cfgSettingService;

    @Resource
    private SysPostFeign sysPostFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Value("${third.fs.appUrl}")
    private String fsAppUrl;

    @Resource
    private FsService fsService;
    @Lazy
    @Resource
    private TmsFirstMileReconciliationService tmsFirstMileReconciliationService;

    @Resource
    private TmsCfgCostService tmsCfgCostService;

    @Resource
    private LogisticsBillService logisticsBillService;

    @Resource
    @Lazy
    private TmsFirstMileLogisticService service;

    @Resource
    private ShippingCalculationService shippingCalculationService;

    @Resource
    private ShippingTemplateRuleService shippingTemplateRuleService;

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private TmsFirstMileReconciliationDetailService tmsFirstMileReconciliationDetailService;

    @Resource
    private LogisticsCarrierService logisticsCarrierService;
    @Lazy
    @Resource
    private FirstMileWeightAllocationService firstMileWeightAllocationService;
    @Lazy
    @Resource
    private FirstMileEstimatedBillService firstMileEstimatedBillService;
    @Resource
    private OverseaWarehouseInboundFeign overseaWarehouseInboundFeign;
    @Resource
    private FirstMileDeliveryDetailFeign firstMileDeliveryDetailFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TmsFirstMileLogisticDTO.AddDTO addDTO) {
        //发货单id
        String outstockId = addDTO.getOutstockId();
        FirstMileDeliveryDTO.GenerateLogisticDTO generateLogisticDTO = this.getGenerateLogisticDTO(outstockId,addDTO.getIsAuto());
        if(generateLogisticDTO == null){
            throw new ServiceException("发货单不存在或者未装箱或已生成物流单");
        }
        //一个发货单仅可下推一次
        if (StringUtils.isNotBlank(outstockId)){
            List<LogisticsBillEntity> list = this.lambdaQuery().eq(LogisticsBillEntity::getOutstockId, outstockId).list();
            if (CollectionUtils.isNotEmpty(list)){
                throw new ServiceException("发货单已下推物流单，不能重复下推");
            }
        }
        //新增物流单
        LogisticsBillEntity tmsFirstMileLogisticEntity = FmLogisticsConverter.INSTANCE.addLogisticsBill(generateLogisticDTO,addDTO);
        if(StringUtils.isBlank(tmsFirstMileLogisticEntity.getRemark())){
            tmsFirstMileLogisticEntity.setRemark(generateLogisticDTO.getRemark());
        }
        if(StringUtils.isNotBlank(addDTO.getTransportNo())){
            List<LogisticsBillEntity> logisticsBillEntityList = this.listByTransportNo(Arrays.asList(addDTO.getTransportNo()));
            if(CollectionUtils.isNotEmpty(logisticsBillEntityList)){
                throw new ServiceException("运单号已存在，不能重复新增");
            }
        }
        log.info("开始新增头程物流单");
        boolean save = super.save(tmsFirstMileLogisticEntity);
        if(!save) {
            throw new ServiceException("头程物流单保存失败");
        }
        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "头程物流单" , tmsFirstMileLogisticEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LOGISTICS_BILL.getCode(), tmsFirstMileLogisticEntity.getId(), "新增操作");

        //新增物流费用单
        LogisticsBillCostDTO.AddDTO costAddDTO = this.packCostAddDTO(generateLogisticDTO,addDTO,tmsFirstMileLogisticEntity);

        //新增物流单明细
        List<LogisticsBillDetailDTO.AddDTO> detailAddList = new ArrayList<>();
        LogisticsBillDetailDTO.AddDTO detailAddDto = new LogisticsBillDetailDTO.AddDTO();
        detailAddDto.setMainId(tmsFirstMileLogisticEntity.getId());
        detailAddDto.setTrackNo(tmsFirstMileLogisticEntity.getCounterNo());
        detailAddDto.setTrackStatus(FmLogisticTrackStatusEnum.WAIT_ORDER.getCode());
        detailAddList.add(detailAddDto);
        logisticsBillDetailService.add(tmsFirstMileLogisticEntity,detailAddList,false);

        List<LogisticsBillDetailEntity> detailEntityList = logisticsBillDetailService.listByMainIds(Collections.singletonList(tmsFirstMileLogisticEntity.getId()));
        LogisticsBillDetailEntity billDetailEntity = CollectionUtils.isEmpty(detailEntityList)?new LogisticsBillDetailEntity():detailEntityList.get(0);

        costAddDTO.setLogisticsBillDetailId(billDetailEntity.getId());
        costAddDTO.setTrackNo(tmsFirstMileLogisticEntity.getCounterNo());
        //物流费用单明细
        List<TmsCfgCostEntity> tmsCfgCostList = tmsCfgCostService.listCostAttributionAndCategory(DictCostAttributionEnum.FIRST_MILE.getCode(), DictCostCategoryEnum.SHIPPING_COST.getCode());
        TmsCfgCostEntity defaultCost = new TmsCfgCostEntity();
        if(CollectionUtils.isNotEmpty(tmsCfgCostList)){
            defaultCost = tmsCfgCostList.get(0);
        }
        List<TmsFirstMileLogisticDTO.LogisticFee> logisticFeeList = addDTO.getLogisticFeeList() == null?new ArrayList<>():addDTO.getLogisticFeeList();
        List<TmsCostDetailDTO.AddDTO> costDetailList = new ArrayList<>();
        for (TmsFirstMileLogisticDTO.LogisticFee logisticFee : logisticFeeList) {
            if(StringUtils.isBlank(logisticFee.getCfgCostId())){
                continue;
            }
            BigDecimal fee = logisticFee.getEstimatedFee();
            if(logisticFee.getCfgCostId().equals(defaultCost.getId()) && Objects.isNull(fee) && StringUtils.isNotBlank(addDTO.getLogisticsChannelId())){
                try {
                    TmsFirstMileLogisticDTO.CalculateShippingCostDTO dto = new TmsFirstMileLogisticDTO.CalculateShippingCostDTO();
                    dto.setChannelId(addDTO.getLogisticsChannelId());
                    dto.setOutstockId(outstockId);
                    fee = this.calculateShippingCost(dto);
                }catch (ServiceException e){
                    //业务异常不影响这个新增逻辑
                }
            }
            TmsCostDetailDTO.AddDTO dto = new TmsCostDetailDTO.AddDTO();
            dto.setCostValue(fee);
            dto.setCfgCostId(logisticFee.getCfgCostId());
            dto.setSourceType(SourceTypeEnum.FIRST_MILE_LOGISTICS_BILL_COST.getCode());
            dto.setType(LogisticsBillCostTypeEnum.ESTIMATED.getCode());
            costDetailList.add(dto);
        }
        costAddDTO.setCostDetailList(costDetailList);
        logisticsBillCostService.add(costAddDTO);


        //设置附件信息
        Class<LogisticsBillEntity> credentialClass = LogisticsBillEntity.class;
        TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
        String type = tableName.value();
        attachmentService.batchSave(addDTO.getAttachmentUrlList(), addDTO.getAttachmentNameList(), type, tmsFirstMileLogisticEntity.getId());

        //更新发货单物流状态
        if(!addDTO.getIsAuto()){
            FirstMileDeliveryDTO.UpdateStatusDTO updateDeliveryDto = new FirstMileDeliveryDTO.UpdateStatusDTO();
            updateDeliveryDto.setIds(Arrays.asList(addDTO.getOutstockId()));
            updateDeliveryDto.setLogisticsStatus(FmDeliveryLogisticsStatusEnum.FINISH.code);
            if(!wmsFirstMileDeliveryFeign.updateStatus(updateDeliveryDto)){
                throw new ServiceException("发货单更新物流状态失败");
            }
        }

        //新增暂估账单
        firstMileEstimatedBillService.add(tmsFirstMileLogisticEntity.getId());
        return new BaseResultDTO.AddDTO(tmsFirstMileLogisticEntity.getId(), tmsFirstMileLogisticEntity.getOutstockCode());
    }


    private LogisticsBillCostDTO.AddDTO packCostAddDTO(FirstMileDeliveryDTO.GenerateLogisticDTO generateLogisticDTO, TmsFirstMileLogisticDTO.CommonDTO addDTO,LogisticsBillEntity tmsFirstMileLogisticEntity) {
        //装箱信息
        List<WmsCartonDetailDTO.ListPackingDetailDTO> packingDTOList = generateLogisticDTO.getPackingDTOList();
        LogisticsBillCostDTO.AddDTO costAddDTO = new LogisticsBillCostDTO.AddDTO();
        costAddDTO.setReconciliationStatus(ReconciliationStatusEnum.TO_BE_GENERATED.getCode());
        costAddDTO.setLogisticsBillId(tmsFirstMileLogisticEntity.getId());
        if(CollectionUtils.isNotEmpty(packingDTOList)){
            //预估实重
            BigDecimal actualWeight = packingDTOList.stream()
                    .map(WmsCartonDetailDTO.ListPackingDetailDTO::getPackageWeight)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            costAddDTO.setActualWeight(actualWeight);
            //设置预估体积重 = 长宽高/材积
            if(StringUtils.isNotBlank(addDTO.getLogisticsChannelId())){
                LogisticsChannelEntity channelEntity = logisticsChannelService.getById(addDTO.getLogisticsChannelId());
//                ShippingTemplateEntity shippingTemplateEntity = shippingTemplateService.getByChannelId(addDTO.getLogisticsChannelId());
                if(Objects.nonNull(channelEntity) && channelEntity.getVolumeSetting() > 0){
                    BigDecimal totalSize = packingDTOList.stream()
                            .map(WmsCartonDetailDTO.ListPackingDetailDTO::getMultiplySize)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    costAddDTO.setVolumeWeight(totalSize.divide(BigDecimal.valueOf(channelEntity.getVolumeSetting()),4, RoundingMode.HALF_UP));
                }
            }
        }
        costAddDTO.setCurrency(addDTO.getCurrency());
        costAddDTO.setRemark(addDTO.getRemark());
        costAddDTO.setChannelId(addDTO.getLogisticsChannelId());
        costAddDTO.setVolumeWeightLogistics(addDTO.getActualVolumeWeight());
        costAddDTO.setWeightLogistics(addDTO.getActualWeight());
        return costAddDTO;
    }

    private LogisticsBillCostDTO.UpdateDTO packCostUpdateDTO(FirstMileDeliveryDTO.GenerateLogisticDTO generateLogisticDTO, TmsFirstMileLogisticDTO.CommonDTO updateDTO,LogisticsBillCostEntity oldEntity) {
        //装箱信息
        List<WmsCartonDetailDTO.ListPackingDetailDTO> packingDTOList = generateLogisticDTO.getPackingDTOList();
        LogisticsBillCostDTO.UpdateDTO costUpdateDTO = new LogisticsBillCostDTO.UpdateDTO();
        if(CollectionUtils.isNotEmpty(packingDTOList)){
            //预估实重
            BigDecimal actualWeight = packingDTOList.stream()
                    .map(WmsCartonDetailDTO.ListPackingDetailDTO::getPackageWeight)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            costUpdateDTO.setActualWeight(actualWeight);
            if(StringUtils.isNotBlank(updateDTO.getLogisticsChannelId())){
                //设置预估体积重 = 长宽高/材积
                LogisticsChannelEntity channelEntity = logisticsChannelService.getById(updateDTO.getLogisticsChannelId());
//                ShippingTemplateEntity shippingTemplateEntity = shippingTemplateService.getByChannelId(updateDTO.getLogisticsChannelId());
                if(Objects.nonNull(channelEntity) && channelEntity.getVolumeSetting() > 0){
                    BigDecimal totalSize = packingDTOList.stream()
                            .map(WmsCartonDetailDTO.ListPackingDetailDTO::getMultiplySize)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    costUpdateDTO.setVolumeWeight(totalSize.divide(BigDecimal.valueOf(channelEntity.getVolumeSetting()),4, RoundingMode.HALF_UP));
                }
            }
        }
        costUpdateDTO.setCurrency(updateDTO.getCurrency());
        costUpdateDTO.setRemark(updateDTO.getRemark());
        costUpdateDTO.setVolumeWeightLogistics(Objects.isNull(updateDTO.getActualVolumeWeight())?BigDecimal.ZERO:updateDTO.getActualVolumeWeight());
        costUpdateDTO.setWeightLogistics(Objects.isNull(updateDTO.getActualWeight())?BigDecimal.ZERO:updateDTO.getActualWeight());
        costUpdateDTO.setId(oldEntity.getId());
        return costUpdateDTO;
    }

    private FirstMileDeliveryDTO.GenerateLogisticDTO getGenerateLogisticDTO(String outstockId,Boolean isAuto){
        FirstMileDeliveryDTO.GenerateLogisticReqDTO dto = new FirstMileDeliveryDTO.GenerateLogisticReqDTO();
        dto.setIds(Arrays.asList(outstockId));
        if(!isAuto){
            dto.setPackingStatus(PackingTaskStatusEnum.PACKED.getCode());
        }
        dto.setLogisticsStatus(FmDeliveryLogisticsStatusEnum.WAIT.code);
        List<FirstMileDeliveryDTO.GenerateLogisticDTO> generateLogisticDTO = wmsFirstMileDeliveryFeign.getGenerateLogisticDTO(dto);
        if(CollectionUtil.isEmpty(generateLogisticDTO)){
            return null;
        }
        FirstMileDeliveryDTO.GenerateLogisticDTO generateLogisticDTO1 = generateLogisticDTO.get(0);
        if(CharSequenceUtil.isNotBlank(outstockId)){
            List<FirstMileDeliveryDTO.BusinessDTO> businessDTOList = wmsFirstMileDeliveryFeign.getBusinessCodeByIds(Collections.singletonList(outstockId));
            generateLogisticDTO1.setBusinessCode(CollectionUtils.isNotEmpty(businessDTOList) ? businessDTOList.get(0).getBusinessCode() : "");
        }
        return generateLogisticDTO1;
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TmsFirstMileLogisticDTO.UpdateDTO updateDTO) {
        LogisticsBillEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "头程物流单"));
        String oldCounterNo = old.getCounterNo();
        String oldOutstockId = old.getOutstockId();
        //更新物流单
        String outstockId = updateDTO.getOutstockId();
        FirstMileDeliveryDTO.GenerateLogisticDTO generateLogisticDTO = new FirstMileDeliveryDTO.GenerateLogisticDTO();
        if(!outstockId.equals(old.getOutstockId())){
            generateLogisticDTO = this.getGenerateLogisticDTO(outstockId,false);
            if(generateLogisticDTO == null){
                throw new ServiceException("发货单不存在或者未装箱或已生成物流单");
            }
        }
        LogisticsBillEntity updateFirstMileLogisticEntity = FmLogisticsConverter.INSTANCE.addLogisticsBill(generateLogisticDTO,updateDTO);
        BeanUtil.copyProperties(updateFirstMileLogisticEntity,old, CopyOptions.create().setIgnoreNullValue(true));
        //校验运单号是否重复
        if(StringUtils.isNotBlank(updateDTO.getTransportNo()) && !updateDTO.getTransportNo().equals(old.getTransportNo())){
            List<LogisticsBillEntity> logisticsBillEntityList = this.listByTransportNo(Arrays.asList(updateDTO.getTransportNo()));
            if(CollectionUtils.isNotEmpty(logisticsBillEntityList)){
                throw new ServiceException("运单号已存在，修改失败");
            }
        }
        boolean save = super.updateById(old);
        if(!save) {
            throw new ServiceException("头程物流单保存失败");
        }

        //更新物流单明细
        if(!updateDTO.getCounterNo().equals(oldCounterNo)){
            //删除旧的，新增新的
            List<LogisticsBillDetailEntity> logisticsBillDetailEntityList = logisticsBillDetailService.listByMainIds(Collections.singletonList(old.getId()));
            logisticsBillDetailService.removeByMainIds(Collections.singletonList(old.getId()),false);
            List<LogisticsBillDetailDTO.AddDTO> detailAddList = new ArrayList<>();
            LogisticsBillDetailDTO.AddDTO detailAddDto = new LogisticsBillDetailDTO.AddDTO();
            detailAddDto.setMainId(old.getId());
            detailAddDto.setTrackNo(updateDTO.getCounterNo());
            detailAddDto.setTrackStatus(CollectionUtils.isNotEmpty(logisticsBillDetailEntityList)?logisticsBillDetailEntityList.get(0).getTrackStatus():FmLogisticTrackStatusEnum.WAIT_ORDER.getCode());
            detailAddList.add(detailAddDto);
            logisticsBillDetailService.add(old,detailAddList,false);
        }
        List<LogisticsBillDetailEntity> detailEntityList = logisticsBillDetailService.listByMainIds(Collections.singletonList(old.getId()));
        LogisticsBillDetailEntity billDetailEntity = CollectionUtils.isEmpty(detailEntityList)?new LogisticsBillDetailEntity():detailEntityList.get(0);
        //更新物流单费用及明细
        List<LogisticsBillCostEntity> logisticsBillCostEntityList = logisticsBillCostService.getByLogisticsBillIds(Collections.singletonList(old.getId()));
        Optional.ofNullable(logisticsBillCostEntityList).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "头程物流费用单"));
        LogisticsBillCostDTO.UpdateDTO updateCostDTO = this.packCostUpdateDTO(generateLogisticDTO,updateDTO,logisticsBillCostEntityList.get(0));
        updateCostDTO.setLogisticsBillDetailId(billDetailEntity.getId());
        updateCostDTO.setTrackNo(old.getCounterNo());
        List<TmsFirstMileLogisticDTO.LogisticFee> logisticFeeList = CollectionUtil.isEmpty(updateDTO.getLogisticFeeList())?new ArrayList<>():updateDTO.getLogisticFeeList();
        List<TmsCostDetailDTO.UpdateDTO> costDetailList = new ArrayList<>();
        for (TmsFirstMileLogisticDTO.LogisticFee logisticFee : logisticFeeList) {
            TmsCostDetailDTO.UpdateDTO dto = new TmsCostDetailDTO.UpdateDTO();
            dto.setCostValue(logisticFee.getEstimatedFee());
            dto.setCfgCostId(logisticFee.getCfgCostId());
            dto.setType(LogisticsBillCostTypeEnum.ESTIMATED.getCode());
            costDetailList.add(dto);
        }
        updateCostDTO.setCostDetailList(costDetailList);
        logisticsBillCostService.update(updateCostDTO,Boolean.FALSE);

        //更新发货单物流状态
        if(!oldOutstockId.equals(updateDTO.getOutstockId())){
            FirstMileDeliveryDTO.UpdateStatusDTO updateOldDTO = new FirstMileDeliveryDTO.UpdateStatusDTO();
            updateOldDTO.setIds(Arrays.asList(oldOutstockId));
            updateOldDTO.setLogisticsStatus(FmDeliveryLogisticsStatusEnum.WAIT.code);
            if(!wmsFirstMileDeliveryFeign.updateStatus(updateOldDTO)){
                throw new ServiceException("发货单更新物流状态失败");
            }

            FirstMileDeliveryDTO.UpdateStatusDTO updateNewDTO = new FirstMileDeliveryDTO.UpdateStatusDTO();
            updateNewDTO.setIds(Arrays.asList(updateDTO.getOutstockId()));
            updateNewDTO.setLogisticsStatus(FmDeliveryLogisticsStatusEnum.FINISH.code);
            if(!wmsFirstMileDeliveryFeign.updateStatus(updateNewDTO)){
                throw new ServiceException("发货单更新物流状态失败");
            }
        }
        //设置附件信息
        Class<LogisticsBillEntity> credentialClass = LogisticsBillEntity.class;
        TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
        String type = tableName.value();
        attachmentService.batchSave(updateDTO.getAttachmentUrlList(), updateDTO.getAttachmentNameList(), type, old.getId());

        return Boolean.TRUE;
    }

    @Override
    public List<LogisticsBillEntity> listByOutstockIds(List<String> outstockIds) {
        if (CollectionUtil.isEmpty(outstockIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(LogisticsBillEntity::getOutstockId, outstockIds).list();
    }
    @Override
    public List<TmsFirstMileLogisticDTO.TabListDTO> tabList(TmsFirstMileLogisticDTO.PagingParamDTO dto) {
        List<TmsFirstMileLogisticDTO.TabListDTO> tabList = baseMapper.firstMileTabList(OrderTypeEnum.FIRST_MILE.getCode(),dto.getPermissionSql());
        List<TmsFirstMileLogisticDTO.TabListDTO> result = new ArrayList<>();
        for(FmLogisticTrackStatusEnum logisticTrackStatusEnum : FmLogisticTrackStatusEnum.values()){
            if(logisticTrackStatusEnum == FmLogisticTrackStatusEnum.EXCEPTION){
                continue;
            }
            TmsFirstMileLogisticDTO.TabListDTO tabListDTO = new TmsFirstMileLogisticDTO.TabListDTO();
            tabListDTO.setTabFlag(logisticTrackStatusEnum.getCode());
            tabListDTO.setTabFlagName(logisticTrackStatusEnum.getName());
            TmsFirstMileLogisticDTO.TabListDTO queryResult = tabList.stream().filter(v->v.getTabFlag().equals(tabListDTO.getTabFlag())).findFirst().orElse(new TmsFirstMileLogisticDTO.TabListDTO());
            tabListDTO.setCount(queryResult.getCount());
            result.add(tabListDTO);
        }
        return result;
    }

    @Override
    public PagingVO<TmsFirstMileLogisticDTO.PagingVO> paging(PagingDTO<TmsFirstMileLogisticDTO.PagingParamDTO> dto) {
        TmsFirstMileLogisticDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        params.setOrderType(OrderTypeEnum.FIRST_MILE.getCode());
        IPage<TmsFirstMileLogisticDTO.PagingVO> pageData = baseMapper.firstMilePaging(query, params);
        fillPagingDb(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public List<TmsFirstMileLogisticDTO.PagingVO> hasWarnPaging(TmsFirstMileLogisticDTO.PagingParamDTO dto) {
        dto.setOrderType(OrderTypeEnum.FIRST_MILE.getCode());
        List<TmsFirstMileLogisticDTO.PagingVO> list =  baseMapper.hasWarnPaging(dto);
        fillPagingDb(list);
        return list;
    }

    private void fillPagingDb(List<TmsFirstMileLogisticDTO.PagingVO> list) {
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        List<String> ids = list.stream().map(TmsFirstMileLogisticDTO.PagingVO::getId).distinct().collect(Collectors.toList());
        List<String> channelIdList = list.stream().map(TmsFirstMileLogisticDTO.PagingVO::getLogisticsChannelId).collect(Collectors.toList());
        List<LogisticsChannelEntity> logisticsChannelEntityList = logisticsChannelService.listByIds(channelIdList);

        List<String> logisticsSupplierIdList = list.stream().map(TmsFirstMileLogisticDTO.PagingVO::getLogisticsSupplierId).collect(Collectors.toList());
        List<LogisticsSupplierEntity> logisticsSupplierEntityList = logisticsSupplierService.listByIds(logisticsSupplierIdList);

        List<String> outstockIdList = list.stream().map(TmsFirstMileLogisticDTO.PagingVO::getOutstockId).collect(Collectors.toList());

        FirstMileDeliveryDTO.GenerateLogisticReqDTO dto = new FirstMileDeliveryDTO.GenerateLogisticReqDTO();
        dto.setIds(outstockIdList);
        List<FirstMileDeliveryDTO.GenerateLogisticDTO> generateLogisticDTOList = wmsFirstMileDeliveryFeign.getGenerateLogisticDTO(dto);
        List<String> carrierIds = list.stream().map(TmsFirstMileLogisticDTO.PagingVO::getCarrierId).distinct().collect(Collectors.toList());
        List<LogisticsCarrierEntity> carrierList = logisticsCarrierService.listByIds(carrierIds);
        //根据物流单获取对账单数据
        List<TmsFirstMileLogisticDTO.ReconciliationDTO> reconciliationDTOList = tmsFirstMileReconciliationService.listReconciliationAndCostByBillIds(ids);
        list.forEach(pagingVO ->{
            //对账单信息填充
            TmsFirstMileLogisticDTO.ReconciliationDTO reconciliationDTO = reconciliationDTOList.stream().filter(e -> CharSequenceUtil.isNotBlank(e.getLogisticsBillId()) && Objects.equals(e.getLogisticsBillId(), pagingVO.getId())).findFirst().orElse(null);
            if (Objects.nonNull(reconciliationDTO)){
                pagingVO.setReconciliationStatus(reconciliationDTO.getReconciliationStatus());
                pagingVO.setWeight(reconciliationDTO.getActualWeight());
                pagingVO.setVolumeWeight(reconciliationDTO.getVolumeWeight());
                pagingVO.setWeightUnit(reconciliationDTO.getWeightUnit());
                pagingVO.setCurrency(reconciliationDTO.getCurrency());
            }
            //处理枚举值
            pagingVO.setLogisticsStatusName(EnumMessage.getNameByCode(FmLogisticTrackStatusEnum.class,pagingVO.getLogisticsStatus()));
            pagingVO.setInvoicesStatusName(EnumMessage.getNameByCode(InvoicesStatusEnum.class,pagingVO.getInvoicesStatus()));
            pagingVO.setReconciliationStatusName(EnumMessage.getNameByCode(ReconciliationStatusEnum.class,pagingVO.getReconciliationStatus()));
            pagingVO.setShippingMethodName(EnumMessage.getNameByCode(LogisticsMethodEnum.class,pagingVO.getShippingMethod()));

            //处理渠道，供应商
            LogisticsChannelEntity logisticsChannelEntity = logisticsChannelEntityList.stream().filter(v->v.getId().equals(pagingVO.getLogisticsChannelId())).findFirst().orElse(new LogisticsChannelEntity());
            pagingVO.setLogisticsChannelName(logisticsChannelEntity.getName());

            LogisticsSupplierEntity logisticsSupplierEntity = logisticsSupplierEntityList.stream().filter(v->v.getId().equals(pagingVO.getLogisticsSupplierId())).findFirst().orElse(new LogisticsSupplierEntity());
            pagingVO.setLogisticsSupplierName(logisticsSupplierEntity.getSupplierName());

            if (StringUtils.isNotEmpty(pagingVO.getCarrierId())){
                LogisticsCarrierEntity carrier = carrierList.stream().filter(e -> e.getId().equals(pagingVO.getCarrierId())).findFirst().orElse(null);
                pagingVO.setCarrierName(Objects.nonNull(carrier)? carrier.getCarrierCn() : pagingVO.getCarrierId());
            }

            //处理发货单相关信息
            FirstMileDeliveryDTO.GenerateLogisticDTO deliveryDto = generateLogisticDTOList.stream().filter(v->v.getOutstockId().equals(pagingVO.getOutstockId())).findFirst().orElse(null);
            if(Objects.nonNull(deliveryDto)){
                pagingVO.setFromWarehouseName(deliveryDto.getFromWarehouseName());
                pagingVO.setToWarehouseName(deliveryDto.getToWarehouseName());
                pagingVO.setCurrencySymbol(CurrencyEnum.getSymbolByCode(pagingVO.getCurrency()));
                pagingVO.setToAddress(deliveryDto.getToAddress());
                pagingVO.setBoxCount(CollectionUtils.isNotEmpty(deliveryDto.getPackingDTOList())?deliveryDto.getPackingDTOList().size():0);
            }

            pagingVO.setCompleteWeight(pagingVO.getWeight()+pagingVO.getWeightUnit());
            pagingVO.setCompleteVolumeWeight(pagingVO.getVolumeWeight()+pagingVO.getWeightUnit());
            pagingVO.setCompleteEstimatedFee(pagingVO.getCurrencySymbol()+(Objects.isNull(pagingVO.getEstimatedFee())?"":pagingVO.getEstimatedFee()));
            pagingVO.setCompleteActualFee(pagingVO.getCurrencySymbol()+(Objects.isNull(pagingVO.getActualFee())?"":pagingVO.getActualFee()));
            //处理实际时效和预警
            pagingVO.setActualDesc(getActualDesc(pagingVO.getLogisticsStatus(),pagingVO.getOrderTime(),pagingVO.getSignTime(),pagingVO.getShipTime()));
            if(StringUtils.isNotBlank(logisticsChannelEntity.getEffectiveTime()) && !logisticsChannelEntity.getEffectiveTime().equals("0")
                    && StringUtils.isNotBlank(logisticsChannelEntity.getEffectiveTimeUnit())){
                //判断是否是数字，不是数字的话不计算预警，直接返回中文
                String regex = "\\d*[1-9]+\\d*";
                Pattern pattern = Pattern.compile(regex);
                if(!pattern.matcher(logisticsChannelEntity.getEffectiveTime()).matches()){
                    pagingVO.setEstimatedTimeDesc(logisticsChannelEntity.getEffectiveTime());
                }else {
                    int estimatedDay = Integer.parseInt(logisticsChannelEntity.getEffectiveTime());
                    pagingVO.setEstimatedDay(estimatedDay);
                    //现在单位只有天
                    pagingVO.setEstimatedTimeUnit("天");
                    pagingVO.setEstimatedTimeDesc(pagingVO.getEstimatedDay() + pagingVO.getEstimatedTimeUnit());
                    //设置预警
                    if(pagingVO.getActualHour() != null){
                        pagingVO.setWarnHour(estimatedDay*24 - pagingVO.getActualHour());
                    }
                }
            }
            if(pagingVO.getWarnHour()!=null){
                if(pagingVO.getWarnHour()> 72){
                    pagingVO.setWarnMsg("时效正常");
                }else if(pagingVO.getWarnHour() < 0){
                    pagingVO.setWarnMsg(CharSequenceUtil.format("[]小时后超期",pagingVO.getWarnHour()));
                }else{
                    pagingVO.setWarnMsg(CharSequenceUtil.format("已超期[]小时",pagingVO.getWarnHour()));
                }
            }
        });
    }

    private String getActualDesc(String logisticsStatus, LocalDateTime orderTime, LocalDateTime signTime, LocalDateTime shipTime) {
        String actualDesc = StrUtil.EMPTY;
        //无下单时间：默认展示为空
        if (Objects.isNull(orderTime)){
            return actualDesc;
        }
        Duration duration = null;
        if (Objects.isNull(shipTime)){
            if (!FmLogisticTrackStatusEnum.SIGN.getCode().equals(logisticsStatus)){
                //有下单时间无开船时间-运输中：[当前时间-下单时间]
                duration = Duration.between(LocalDateTime.now(), orderTime);
            }else {
                //有下单时间无开船时间-已签收：[签收时间-下单时间]
                duration = Duration.between(signTime, orderTime);
            }
        }else {
            if (!FmLogisticTrackStatusEnum.SIGN.getCode().equals(logisticsStatus)){
                //有下单时间有开船时间-运输中：[当前时间-开船时间]
                duration = Duration.between(LocalDateTime.now(), shipTime);
            }else {
                //有下单时间有开船时间-已签收：[签收时间-开船时间]
                duration = Duration.between(signTime, shipTime);
            }
        }
        if (Objects.nonNull(duration)){
            actualDesc = duration.toDays() + "天" + duration.toHours() % 24 + "小时";
        }
        return actualDesc;
    }

    private void fillViewDb(TmsFirstMileLogisticDTO.ViewDTO dto) {
        List<TmsFirstMileLogisticDTO.ReconciliationDTO> reconciliationDTOS = tmsFirstMileReconciliationService.listReconciliationAndCostByBillIds(Collections.singletonList(dto.getId()));
        if (CollectionUtils.isNotEmpty(reconciliationDTOS)){
            TmsFirstMileLogisticDTO.ReconciliationDTO reconciliationDTO = reconciliationDTOS.get(0);
            dto.setActualWeight(reconciliationDTO.getWeightLogistics());
            dto.setActualVolumeWeight(reconciliationDTO.getVolumeWeightLogistics());
            dto.setCurrency(reconciliationDTO.getCurrency());
        }
        //设置店铺负责人
        if(StringUtils.isNotBlank(dto.getShopId())){
            ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(dto.getShopId());
            if(Objects.nonNull(shopInfoEntity)){
                dto.setChargeName(shopInfoEntity.getChargeName());
            }
        }
        //处理枚举值
        dto.setLogisticsStatusName(EnumMessage.getNameByCode(FmLogisticTrackStatusEnum.class,dto.getLogisticsStatus()));
        dto.setShippingMethodName(EnumMessage.getNameByCode(LogisticsMethodEnum.class,dto.getShippingMethod()));
        dto.setCurrencySymbol(CurrencyEnum.getSymbolByCode(dto.getCurrency()));
        //处理渠道相关
        TmsFirstMileLogisticDTO.CanGenerateDeliveryDTO canGenerateDeliveryDTO = new TmsFirstMileLogisticDTO.CanGenerateDeliveryDTO();
        canGenerateDeliveryDTO.setLogisticsChannelId(dto.getLogisticsChannelId());
        TmsFirstMileLogisticDTO.LogisticsDTO logisticsDTO = this.getLogisticsAndShipping(canGenerateDeliveryDTO);
        dto.setFeeRuleName(logisticsDTO.getFeeRuleName());
        dto.setEstimatedDay(logisticsDTO.getEstimatedDay());
        dto.setEstimatedTimeUnit(logisticsDTO.getEstimatedTimeUnit());
        dto.setEstimatedTimeDesc(logisticsDTO.getEstimatedTimeDesc());
        dto.setLogisticsChannelName(logisticsDTO.getLogisticsChannelName());
        //处理实际时效
        dto.setActualDesc(getActualDesc(dto.getLogisticsStatus(),dto.getOrderTime(),dto.getSignTime(),dto.getShipTime()));
        //设置物流商信息
        LogisticsSupplierEntity logisticsSupplierEntity = logisticsSupplierService.getById(dto.getLogisticsSupplierId());
        if(Objects.nonNull(logisticsSupplierEntity)){
            dto.setLogisticsSupplierName(logisticsSupplierEntity.getSupplierName());
        }

        //设置附件信息
        List<AttachmentDTO.UpdateDTO> attachmentList = attachmentService.getByBusinessId(dto.getId());
        List<String> attachmentUrlList = attachmentList.stream().map(AttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
        List<String> attachmentNameList = attachmentList.stream().map(AttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList());
        dto.setAttachmentUrlList(attachmentUrlList);
        dto.setAttachmentNameList(attachmentNameList);

        //处理发货单数据
        FirstMileDeliveryDTO.GenerateLogisticReqDTO reqDto = new FirstMileDeliveryDTO.GenerateLogisticReqDTO();
        reqDto.setIds(Arrays.asList(dto.getOutstockId()));
        List<FirstMileDeliveryDTO.GenerateLogisticDTO> generateLogisticDTO = wmsFirstMileDeliveryFeign.getGenerateLogisticDTO(reqDto);
        List<TmsFirstMileLogisticDTO.DeliveryDTO> deliveryDTOList = BeanUtil.copyToList(generateLogisticDTO,TmsFirstMileLogisticDTO.DeliveryDTO.class);
        if(CollectionUtils.isNotEmpty(deliveryDTOList)){
            TmsFirstMileLogisticDTO.DeliveryDTO deliveryDTO = deliveryDTOList.get(0);
            dto.setFromWarehouseName(deliveryDTO.getFromWarehouseName());
            dto.setFromCountryName("中国");
            dto.setFromAddress(deliveryDTO.getFromAddress());
            dto.setToWarehouseName(deliveryDTO.getToWarehouseName());
            dto.setToAddress(deliveryDTO.getToAddress());
            if(CollectionUtils.isNotEmpty(deliveryDTO.getPackingDTOList())){
                LogisticsChannelEntity channelEntity = logisticsChannelService.getById(dto.getLogisticsChannelId());
                if(Objects.nonNull(channelEntity) && channelEntity.getVolumeSetting() != null && channelEntity.getVolumeSetting() > 0){
                    deliveryDTO.getPackingDTOList().forEach(v -> {
                        v.setVolumeWeight(v.getMultiplySize().divide(BigDecimal.valueOf(channelEntity.getVolumeSetting()), 4, RoundingMode.HALF_UP));
                    });
                }
                dto.setPackingDTOList(deliveryDTO.getPackingDTOList());
            }
        }
        //处理费用信息
        List<LogisticsBillCostEntity> logisticsBillCostEntityList = logisticsBillCostService.getByLogisticsBillIds(Collections.singletonList(dto.getId()));
        if(CollectionUtils.isNotEmpty(logisticsBillCostEntityList)){
            List<String> ids = logisticsBillCostEntityList.stream().map(LogisticsBillCostEntity::getId).distinct().collect(Collectors.toList());
            List<TmsCostDetailDTO.CostCompareDTO> costCompareDTOList = logisticsBillCostDetailService.getCostCompareListByIds(ids);
            dto.setLogisticFeeList( BeanUtil.copyToList(costCompareDTOList,TmsFirstMileLogisticDTO.FeeViewDTO.class));
            BigDecimal totalEstimatedFee = costCompareDTOList.stream().filter(e -> Objects.nonNull(e.getEstimatedFee())).map(TmsCostDetailDTO.CostCompareDTO::getEstimatedFee).reduce(BigDecimal.ZERO,BigDecimal::add);
            if(dto.getCurrency().equals(CurrencyEnum.CNY.getCurrencyCode())){
                dto.setTotalEstimatedFee(totalEstimatedFee);
            }else{
                //查询汇率
                BigDecimal rate = CharSequenceUtil.isBlank(dto.getCurrency()) ? null : dmpTaskFeign.getRate(logisticsBillCostEntityList.get(0).getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), dto.getCurrency());
                if(Objects.nonNull(rate)){
                    dto.setTotalEstimatedFee(totalEstimatedFee.multiply(rate));
                }
            }
        }else {
            dto.setLogisticFeeList(Collections.emptyList());
        }

        //处理时间线
        TmsFirstMileLogisticDTO.TimeInfoDTO timeInfoDTO = new TmsFirstMileLogisticDTO.TimeInfoDTO();
        timeInfoDTO.setApproveTime(CollectionUtils.isNotEmpty(generateLogisticDTO)?generateLogisticDTO.get(0).getApproveTime():null);
        timeInfoDTO.setLogisticOrderTime(dto.getOrderTime());
        timeInfoDTO.setShipTime(dto.getShipTime());
        timeInfoDTO.setSignTime(dto.getSignTime());
        if(StringUtils.isNotBlank(dto.getCounterNo())){
            LogisticsTrackDTO.ViewDTO viewDTO = logisticsTrackService.listByTrackNo(dto.getCounterNo());
            timeInfoDTO.setTrackingTime(viewDTO.getList().stream().filter(v->v.getStatus().equals(FmLogisticTrackStatusEnum.TRACK_ING.getCode())).findFirst().orElse(new LogisticsTrackDTO.ListDTO()).getTrackTime());
            timeInfoDTO.setArrivedTime(viewDTO.getList().stream().filter(v->v.getStatus().equals(FmLogisticTrackStatusEnum.ARRIVED.getCode())).findFirst().orElse(new LogisticsTrackDTO.ListDTO()).getTrackTime());

        }
        dto.setTimeLineList(FmTimeLineEnum.convertToViewList(timeInfoDTO));
        //船司航司名称
        if (StringUtils.isNotEmpty(dto.getCarrierId())){
            LogisticsCarrierEntity carrier = logisticsCarrierService.getById(dto.getCarrierId());
            dto.setCarrierName(Objects.nonNull(carrier)? carrier.getCarrierCn() : dto.getCarrierId());
        }

    }

    @Override
    public TmsFirstMileLogisticDTO.StatisticsVO statistics(TmsFirstMileLogisticDTO.PagingParamDTO dto) {
        TmsFirstMileLogisticDTO.StatisticsVO statisticsResult = new TmsFirstMileLogisticDTO.StatisticsVO();
        //发货统计
        TmsFirstMileLogisticDTO.StatisticsVO.DeliveryStatistics deliveryStatistics = new TmsFirstMileLogisticDTO.StatisticsVO.DeliveryStatistics();
        FirstMileDeliveryDTO.StatisticsReq deliveryStaticsReq = new FirstMileDeliveryDTO.StatisticsReq();
        deliveryStaticsReq.setStatus(ApproveStatusEnum.APPROVE.getStatus());
        deliveryStaticsReq.setBeginDate(DateUtil.getStartOfMonth(-1));
        deliveryStaticsReq.setEndDate(DateUtil.getEndOfMonth(0));
        List<FirstMileDeliveryDTO.LogisticStatisticsDTO> deliveryLogisticDTOList = wmsFirstMileDeliveryFeign.logisticStatistics(deliveryStaticsReq);
        deliveryStatistics.setLastMonthDelivery(deliveryLogisticDTOList.stream().filter(v->v.getMonth().equals(LocalDate.now().minusMonths(1).getMonthValue())).findFirst().orElse(new FirstMileDeliveryDTO.LogisticStatisticsDTO()).getCount());
        deliveryStatistics.setThisMonthDelivery(deliveryLogisticDTOList.stream().filter(v->v.getMonth().equals(LocalDate.now().getMonthValue())).findFirst().orElse(new FirstMileDeliveryDTO.LogisticStatisticsDTO()).getCount());
        TmsFirstMileLogisticDTO.LogisticStatisticsReq logisticStatisticsReq = TmsFirstMileLogisticDTO.LogisticStatisticsReq.builder()
                .beginOrderTime(DateUtil.getStartOfMonth(-1))
                .endOrderTime(DateUtil.getEndOfMonth(0))
                .logisticStatusList(FmLogisticTrackStatusEnum.getStatusNotWaitOrder())
                .orderType(OrderTypeEnum.FIRST_MILE.getCode())
                .build();
        List<TmsFirstMileLogisticDTO.LogisticStatisticsDTO> logisticStatisticsDTOList = baseMapper.statistics(logisticStatisticsReq,dto.getPermissionSql());
        deliveryStatistics.setLastMonthOrder(logisticStatisticsDTOList.stream().filter(v->v.getMonth().equals(LocalDate.now().minusMonths(1).getMonthValue())).findFirst().orElse(new TmsFirstMileLogisticDTO.LogisticStatisticsDTO()).getCount());
        deliveryStatistics.setThisMonthOrder(logisticStatisticsDTOList.stream().filter(v->v.getMonth().equals(LocalDate.now().getMonthValue())).findFirst().orElse(new TmsFirstMileLogisticDTO.LogisticStatisticsDTO()).getCount());
        statisticsResult.setDeliveryStatistics(deliveryStatistics);
        //对账统计
        TmsFirstMileLogisticDTO.StatisticsVO.ReconciliationStatistics reconciliationStatistics = baseMapper.reconciliationStatistics(OrderTypeEnum.FIRST_MILE.getCode(),dto.getPermissionSql());
        statisticsResult.setReconciliationStatistics(reconciliationStatistics);
        //超期统计
        List<TmsFirstMileLogisticDTO.OverdueDTO> overdueList = baseMapper.overdueStatistics(OrderTypeEnum.FIRST_MILE.getCode(),dto.getPermissionSql());
        overdueList.forEach(v->{
            //判断是否是数字，不是数字忽略
            String regex = "\\d*[1-9]+\\d*";
            Pattern pattern = Pattern.compile(regex);
            if(!pattern.matcher(v.getEffectiveTime()).matches()){
                return;
            }
            int estimatedDay = Integer.parseInt(v.getEffectiveTime());
            if(Objects.nonNull(v.getActualHour())){
                v.setRemainingTime(estimatedDay*24 - v.getActualHour());
            }
        });
        TmsFirstMileLogisticDTO.StatisticsVO.OverdueStatistics overdueStatistics = new TmsFirstMileLogisticDTO.StatisticsVO.OverdueStatistics();
        overdueStatistics.setAlmostOverdue((int) overdueList.stream().filter(v -> Objects.nonNull(v.getRemainingTime()) && v.getRemainingTime() >= 0 && v.getRemainingTime() <=72 ).count());
        overdueStatistics.setExpired((int) overdueList.stream().filter(v -> Objects.nonNull(v.getRemainingTime()) && v.getRemainingTime() < 0).count());
        statisticsResult.setOverdueStatistics(overdueStatistics);
        return statisticsResult;
    }

    @Override
    public TmsFirstMileLogisticDTO.ViewDTO view(String id) {
        LogisticsBillEntity old = super.getById(id);
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "物流单"));
        TmsFirstMileLogisticDTO.ViewDTO dto = baseMapper.firstMileView(id);
        this.fillViewDb(dto);
        return dto;
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> updateLogisticsStatus(TmsFirstMileLogisticDTO.UpdateLogisticsStatusDTO dto) {
        List<LogisticsBillEntity> logisticsBillEntityList = listByIds(dto.getIds());
        if(CollectionUtils.isEmpty(logisticsBillEntityList)){
            throw new ServiceException("物流单为空");
        }
        FmLogisticTrackStatusEnum statusEnum = EnumMessage.getByCode(FmLogisticTrackStatusEnum.class,(dto.getLogisticsStatus()));
        if(statusEnum == null){
            throw new ServiceException("物流状态为空");
        }
        if(statusEnum != FmLogisticTrackStatusEnum.WAIT_ORDER && Objects.isNull(dto.getTime())){
            throw new ServiceException("时间不能为空");
        }
        List<String> outIds = logisticsBillEntityList.stream().map(LogisticsBillEntity::getOutstockId).collect(Collectors.toList());
        List<FirstMileDeliveryEntity> firstMileDeliveryEntityList = wmsFirstMileDeliveryFeign.listByIds(outIds);
        List<BatchResultDTO> batchResultDTOList = new ArrayList<>();
        List<LogisticsBillDetailEntity> allDetailList = logisticsBillDetailService.listByMainIds(dto.getIds());
        List<LogisticsBillCostEntity> allCostList = logisticsBillCostService.listByLogisticsBillIdList(dto.getIds());
        List<LogisticsBillDetailEntity> updateDetailList = new ArrayList<>();
        List<LogisticsTrackEntity> addTrackList = new ArrayList<>();
        List<LogisticsBillEntity> updateBillList = new ArrayList<>();
        for(LogisticsBillEntity logisticsBillEntity : logisticsBillEntityList){
            List<LogisticsBillDetailEntity> detailEntityList = allDetailList.stream().filter(v->v.getMainId().equals(logisticsBillEntity.getId())).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(detailEntityList)){
                batchResultDTOList.add(BatchResultDTO.fail(logisticsBillEntity.getId(),logisticsBillEntity.getOutstockCode(),"明细为空"));
                continue;
            }
            LogisticsBillCostEntity logisticsBillCostEntity = allCostList.stream().filter(v->v.getLogisticsBillId().equals(logisticsBillEntity.getId())).findFirst().orElse(null);
            if(Objects.isNull(logisticsBillCostEntity)){
                batchResultDTOList.add(BatchResultDTO.fail(logisticsBillEntity.getId(),logisticsBillEntity.getOutstockCode(),"物流费用为空"));
                continue;
            }
            if(!(logisticsBillCostEntity.getReconciliationStatus().equals(ReconciliationStatusEnum.INVALID.getCode()) ||logisticsBillCostEntity.getReconciliationStatus().equals(ReconciliationStatusEnum.TO_BE_GENERATED.getCode()))){
                batchResultDTOList.add(BatchResultDTO.fail(logisticsBillEntity.getId(),logisticsBillEntity.getOutstockCode(),"已生成对账单，不能更新物流状态"));
                continue;
            }

            if(statusEnum == FmLogisticTrackStatusEnum.ORDERED && StringUtils.isBlank(logisticsBillEntity.getChannelId())){
                batchResultDTOList.add(BatchResultDTO.fail(logisticsBillEntity.getId(),logisticsBillEntity.getOutstockCode(),"尚未填写渠道信息，请填写后更新"));
                continue;
            }

            if(statusEnum == FmLogisticTrackStatusEnum.SIGN && StringUtils.isBlank(logisticsBillEntity.getTransportNo())){
                batchResultDTOList.add(BatchResultDTO.fail(logisticsBillEntity.getId(),logisticsBillEntity.getOutstockCode(),"尚未填写物流跟踪号，请填写后更新"));
                continue;
            }
            FmLogisticTrackStatusEnum nowStatusEnum = EnumMessage.getByCode(FmLogisticTrackStatusEnum.class,(detailEntityList.get(0).getTrackStatus()));
            if(FmLogisticTrackStatusEnum.WAIT_ORDER == nowStatusEnum && FmLogisticTrackStatusEnum.ORDERED != statusEnum){
                batchResultDTOList.add(BatchResultDTO.fail(logisticsBillEntity.getId(),logisticsBillEntity.getOutstockCode(),"待下单状态只能更新为已下单"));
                continue;
            }

            if(FmLogisticTrackStatusEnum.WAIT_ORDER != nowStatusEnum && FmLogisticTrackStatusEnum.WAIT_ORDER == statusEnum){
                batchResultDTOList.add(BatchResultDTO.fail(logisticsBillEntity.getId(),logisticsBillEntity.getOutstockCode(),CharSequenceUtil.format("{}状态不能更新为待下单",Objects.isNull(nowStatusEnum)?"":nowStatusEnum.getName())));
                continue;
            }

            if(FmLogisticTrackStatusEnum.ORDERED != nowStatusEnum &&  FmLogisticTrackStatusEnum.WAIT_ORDER != nowStatusEnum && FmLogisticTrackStatusEnum.ORDERED == statusEnum){
                batchResultDTOList.add(BatchResultDTO.fail(logisticsBillEntity.getId(),logisticsBillEntity.getOutstockCode(),CharSequenceUtil.format("{}状态不能更新为已下单",Objects.isNull(nowStatusEnum)?"":nowStatusEnum.getName())));
                continue;
            }
            FirstMileDeliveryEntity firstMileDeliveryEntity = firstMileDeliveryEntityList.stream().filter(v->v.getId().equals(logisticsBillEntity.getOutstockId())).findFirst().orElse(null);
            if(Objects.isNull(firstMileDeliveryEntity)){
                batchResultDTOList.add(BatchResultDTO.fail(logisticsBillEntity.getId(),logisticsBillEntity.getOutstockCode(),"未找到对应的发货单"));
                continue;
            }
            if(!firstMileDeliveryEntity.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus()) && (statusEnum == FmLogisticTrackStatusEnum.TRACK_ING || statusEnum == FmLogisticTrackStatusEnum.ARRIVED ||statusEnum == FmLogisticTrackStatusEnum.SIGN ||statusEnum == FmLogisticTrackStatusEnum.INSPECTING)){
                batchResultDTOList.add(BatchResultDTO.fail(logisticsBillEntity.getId(),logisticsBillEntity.getOutstockCode(),CharSequenceUtil.format("关联单据{}尚未审核通过无法提交",firstMileDeliveryEntity.getCode())));
                continue;
            }

//            if(StringUtils.isBlank(logisticsBillEntity.getCounterNo()) && FmLogisticTrackStatusEnum.ORDERED != statusEnum){
//                batchResultDTOList.add(BatchResultDTO.fail(logisticsBillEntity.getId(),logisticsBillEntity.getOutstockCode(),"尚未填写柜号，请填写后更新"));
//                continue;
//            }
            logisticsBillEntity.setDeliveryTime(firstMileDeliveryEntity.getApproveTime());
            detailEntityList.forEach(v->{
                if(statusEnum == FmLogisticTrackStatusEnum.SIGN){
                    v.setSignTime(dto.getTime());
                }
                v.setTrackStatus(dto.getLogisticsStatus());
            });
            updateDetailList.addAll(detailEntityList);

            LogisticsTrackEntity logisticsTrackEntity = new LogisticsTrackEntity();
            logisticsTrackEntity.setStatus(dto.getLogisticsStatus());
            logisticsTrackEntity.setTrackNo(logisticsBillEntity.getCounterNo());
            logisticsTrackEntity.setTrackTime(Objects.isNull(dto.getTime())?LocalDateTime.now():dto.getTime());
            logisticsTrackEntity.setContent(StringUtils.isBlank(dto.getLogisticsTrack())?"":dto.getLogisticsTrack());
            addTrackList.add(logisticsTrackEntity);
            //如果状态为已下单，更新开船时间
            if(statusEnum == FmLogisticTrackStatusEnum.ORDERED){
                logisticsBillEntity.setOrderTime(dto.getTime());
                LocalDateTime shipTime = sailingService.calculateShipTime(logisticsBillEntity.getChannelId(),dto.getTime());
                logisticsBillEntity.setShipTime(shipTime);
            }

            updateBillList.add(logisticsBillEntity);
        }
        if(CollectionUtils.isNotEmpty(updateDetailList)){
            logisticsBillDetailService.updateBatchById(updateDetailList);
        }
        if(CollectionUtils.isNotEmpty(addTrackList)){
            logisticsTrackService.saveBatch(addTrackList);
        }
        if(CollectionUtils.isNotEmpty(updateBillList)){
            this.updateBatchById(updateBillList);
        }
        List<Pair<String, String>> addPairList = logisticsBillEntityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getId())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(CharSequenceUtil.format("用户【{}】变更物流状态为【{}】",UserContext.getDefaultLoginUser().getUserName(),statusEnum.getName()), ModuleTypeEnum.LOGISTICS_BILL.getCode(), addPairList, "编辑操作");

        return batchResultDTOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> updateInvoicesStatus(TmsFirstMileLogisticDTO.UpdateInvoicesStatusDTO dto) {
        List<LogisticsBillEntity> logisticsBillEntityList = listByIds(dto.getIds());
        if(CollectionUtils.isEmpty(logisticsBillEntityList)){
            throw new ServiceException("物流单为空");
        }
        InvoicesStatusEnum statusEnum = EnumMessage.getByCode(InvoicesStatusEnum.class,(dto.getInvoicesStatus()));
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        List<LogisticsBillEntity> updateList = new ArrayList<>();
        if(statusEnum == null){
            throw new ServiceException("状态不存在");
        }
        for(LogisticsBillEntity entity : logisticsBillEntityList){
            if(entity.getInvoicesStatus() != null && entity.getInvoicesStatus().equals(statusEnum.getCode())){
                resultDTOList.add(BatchResultDTO.fail(entity.getId(),entity.getCounterNo(), CharSequenceUtil.format("状态为【{}】，不可重新更新",statusEnum.getName())));
                continue;
            }
            entity.setInvoicesStatus(statusEnum.getCode());
            updateList.add(entity);
        }
        if(CollectionUtils.isNotEmpty(updateList)){
            this.updateBatchById(updateList);
            List<Pair<String, String>> addPairList = updateList.stream().map(obj -> new Pair<>(obj.getId(), obj.getId())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog(CharSequenceUtil.format("用户【{}】变更发票状态为【{}】",UserContext.getDefaultLoginUser().getUserName(),statusEnum.getName()), ModuleTypeEnum.LOGISTICS_BILL.getCode(), addPairList, "编辑操作");
        }
        return resultDTOList;
    }

    @Override
    public void exportInvoices(List<String> ids, HttpServletResponse response) {

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> updateChannel(TmsFirstMileLogisticDTO.UpdateChannelDTO dto) {
        List<LogisticsBillEntity> logisticsBillEntityList = listByIds(dto.getIds());
        if(CollectionUtils.isEmpty(logisticsBillEntityList)){
            throw new ServiceException("物流单为空");
        }
        LogisticsMethodEnum logisticsMethodEnum =  LogisticsMethodEnum.getByCode(dto.getShippingMethod());
        if(logisticsMethodEnum == null){
            throw new ServiceException("运输方式为空");
        }
        LogisticsSupplierEntity logisticsSupplierEntity = logisticsSupplierService.getById(dto.getLogisticsSupplierId());
        if(Objects.isNull(logisticsSupplierEntity)){
            throw new ServiceException("物流供应商为空");
        }
        SupplierEntity supplierEntity = FeignQuery.getById(SupplierEntity.class,logisticsSupplierEntity.getSupplierId());
        if(Objects.isNull(supplierEntity)){
            throw new ServiceException("供应商为空");
        }
        LogisticsChannelEntity logisticsChannelEntity = logisticsChannelService.getById(dto.getLogisticsChannelId());
        if(Objects.isNull(logisticsChannelEntity)){
            throw new ServiceException("物流渠道为空");
        }
        if(!logisticsChannelEntity.getMainId().equals(logisticsSupplierEntity.getId())){
            throw new ServiceException("物流渠道与物流供应商不匹配");
        }
        List<String> shopIdList = logisticsBillEntityList.stream().map(LogisticsBillEntity::getShopId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoEntityList = shopInfoFeign.listShopInfoByIds(shopIdList);
        List<String> mainIdList = logisticsBillEntityList.stream().map(LogisticsBillEntity::getId).collect(Collectors.toList());
        List<String> outstockIdList = logisticsBillEntityList.stream().map(LogisticsBillEntity::getOutstockId).collect(Collectors.toList());
        List<LogisticsBillCostEntity> costList = logisticsBillCostService.listByLogisticsBillIdList(mainIdList);
        List<LogisticsBillDetailEntity> detailList = logisticsBillDetailService.listByMainIds(mainIdList);
        FirstMileDeliveryDTO.GenerateLogisticReqDTO deliveryDto = new FirstMileDeliveryDTO.GenerateLogisticReqDTO();
        deliveryDto.setIds(outstockIdList);
        List<FirstMileDeliveryDTO.GenerateLogisticDTO> generateLogisticDTOList = wmsFirstMileDeliveryFeign.getGenerateLogisticDTO(deliveryDto);
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        List<LogisticsBillEntity> updateList = new ArrayList<>();
        List<LogisticsBillCostEntity> updateCostList = new ArrayList<>();
        ShippingTemplateEntity shippingTemplateEntity = shippingTemplateService.getByChannelId(dto.getLogisticsChannelId());
        List<TmsFirstMileLogisticDTO.MsgDTO> msgDTOList = new ArrayList<>();
        for(LogisticsBillEntity logisticsBillEntity : logisticsBillEntityList){
            String oldChannelId = logisticsBillEntity.getChannelId();
            LogisticsBillDetailEntity detailEntity = detailList.stream().filter(v->v.getMainId().equals(logisticsBillEntity.getId())).findFirst().orElse(null);
            if(Objects.nonNull(detailEntity) && !(detailEntity.getTrackStatus().equals(FmLogisticTrackStatusEnum.WAIT_ORDER.getCode()) || detailEntity.getTrackStatus().equals(FmLogisticTrackStatusEnum.ORDERED.getCode()))){
                resultDTOList.add(BatchResultDTO.fail(logisticsBillEntity.getId(),logisticsBillEntity.getCounterNo(),"只有待下单和已下单状态支持更改物流信息"));
                continue;
            }
            LogisticsBillCostEntity logisticsBillCostEntity = costList.stream().filter(v->v.getLogisticsBillId().equals(logisticsBillEntity.getId())).findFirst().orElse(null);
            if(Objects.isNull(logisticsBillCostEntity)){
                resultDTOList.add(BatchResultDTO.fail(logisticsBillEntity.getId(),logisticsBillEntity.getOutstockCode(),"物流费用为空"));
                continue;
            }
            if(!(logisticsBillCostEntity.getReconciliationStatus().equals(ReconciliationStatusEnum.INVALID.getCode()) ||logisticsBillCostEntity.getReconciliationStatus().equals(ReconciliationStatusEnum.TO_BE_GENERATED.getCode()))){
                resultDTOList.add(BatchResultDTO.fail(logisticsBillEntity.getId(),logisticsBillEntity.getOutstockCode(),"已生成对账单，不能更新物流状态"));
                continue;
            }
            logisticsBillEntity.setShippingMethod(dto.getShippingMethod());
            logisticsBillEntity.setChannelId(dto.getLogisticsChannelId());
            logisticsBillEntity.setLogisticsSupplierId(dto.getLogisticsSupplierId());
            logisticsBillEntity.setCarrierId(dto.getCarrierId());
            updateList.add(logisticsBillEntity);
            logisticsBillCostEntity.setCurrency(supplierEntity.getPayCurrency());
            updateCostList.add(logisticsBillCostEntity);
            //更新体积重
            FirstMileDeliveryDTO.GenerateLogisticDTO deliveryLogisticDto = generateLogisticDTOList.stream().filter(v->v.getOutstockId().equals(logisticsBillEntity.getOutstockId())).findFirst().orElse(null);
            if(Objects.nonNull(deliveryLogisticDto) && CollectionUtils.isNotEmpty(deliveryLogisticDto.getPackingDTOList())
                    && logisticsChannelEntity.getVolumeSetting()!= null && logisticsChannelEntity.getVolumeSetting() > 0){
                BigDecimal totalSize = deliveryLogisticDto.getPackingDTOList().stream()
                        .map(WmsCartonDetailDTO.ListPackingDetailDTO::getMultiplySize)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                logisticsBillCostEntity.setVolumeWeight(totalSize.divide(BigDecimal.valueOf(logisticsChannelEntity.getVolumeSetting()),4, RoundingMode.HALF_UP));
            }
            //设置消息发送
            if(Objects.nonNull(detailEntity) && detailEntity.getTrackStatus().equals(FmLogisticTrackStatusEnum.ORDERED.getCode()) && !oldChannelId.equals(dto.getLogisticsChannelId())){
                TmsFirstMileLogisticDTO.MsgDTO msgDTO = new TmsFirstMileLogisticDTO.MsgDTO();
                ShopInfoEntity shopInfoEntity = shopInfoEntityList.stream().filter(v->v.getId().equals(logisticsBillEntity.getShopId())).findFirst().orElse(null);
                if(Objects.nonNull(shopInfoEntity) && StringUtils.isNotBlank(shopInfoEntity.getChargeId())){
                    msgDTO.setShopChargeIdList(Arrays.asList(shopInfoEntity.getChargeId()));
                }
                String titleContent = CharSequenceUtil.format("{}将物流渠道更换为{}，请知悉", UserContext.getDefaultLoginUser().getUserName(),logisticsSupplierEntity.getSupplierName()+"-"+logisticsChannelEntity.getName());
                String msgContent = CharSequenceUtil.format("通知类型：更换渠道通知\n货件单号：{}\n发货单号: {}\n店铺:{}",logisticsBillEntity.getSourceCode(),logisticsBillEntity.getOutstockCode(),logisticsBillEntity.getShopName());
                msgDTO.setTitleContent(titleContent);
                msgDTO.setMessageContent(msgContent);
                msgDTOList.add(msgDTO);
            }
        }

        if(CollectionUtils.isNotEmpty(updateList)){
            this.updateBatchById(updateList);
        }
        if(CollectionUtils.isNotEmpty(updateCostList)){
            logisticsBillCostService.updateBatchById(updateCostList);
        }
        msgDTOList.forEach(v->{
            service.sendMsgWhenChannelChange(v.getShopChargeIdList(),v.getTitleContent(),v.getMessageContent());
        });
        return resultDTOList;
    }

    @Async
    @Override
    public void sendMsgWhenChannelChange(List<String> shopChargeIdList,String titleContent,String messageContent){
        CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingEnum.NOTIC.getCode());
        if(Objects.isNull(cfgSettingEntity)){
            return;
        }

        CfgSettingValueDTO.NoticeDTO noticeDTO = JSONUtil.toBean(cfgSettingEntity.getDataJson(),CfgSettingValueDTO.NoticeDTO.class);
        if(Objects.isNull(noticeDTO)){
            return;
        }
        List<String> sendUserIds = new ArrayList<>();
        if(noticeDTO.getIsChannelShopCharge() && CollectionUtils.isNotEmpty(shopChargeIdList)){
            sendUserIds.addAll(shopChargeIdList);
        }
        if(CollectionUtils.isNotEmpty(noticeDTO.getChannelUserIdList())){
            sendUserIds.addAll(noticeDTO.getChannelUserIdList());
        }
        //处理岗位，获取岗位下全部人
        if(CollectionUtils.isNotEmpty(noticeDTO.getChannelPostIdList())){
            //岗位id
            List<String> postIdList = noticeDTO.getChannelPostIdList();
            List<SysPostUserEntity> userEntityList = sysPostFeign.getUserIdByPostIds(postIdList);
            if(CollectionUtils.isNotEmpty(userEntityList)){
                sendUserIds.addAll(userEntityList.stream().map(SysPostUserEntity::getUserId).distinct().collect(Collectors.toList()));
            }
        }
        //没有需要发送的人员
        if(CollectionUtils.isEmpty(sendUserIds)){
            return;
        }
        sendUserIds = sendUserIds.stream().distinct().collect(Collectors.toList());
        //获取飞书的unionid 与用户关系
        List<ThirdUnionDTO> unionIdList = sysUserFeign.getThirdUnionId(ThirdConstants.FS_PLATFORM);
        FsBatchSendMessageDTO sendMessage = new FsBatchSendMessageDTO();
        //过滤出有飞书配置的用户
        List<String> finalSendUserIds = sendUserIds;

        unionIdList =  unionIdList.stream().filter(u -> finalSendUserIds.contains(u.getUserId())).collect(Collectors.toList());
        List<String> unionIds = unionIdList.stream().map(ThirdUnionDTO::getThirdUnionId).distinct().collect(Collectors.toList());
        if(CollectionUtils.isEmpty(unionIds)){
            return;
        }
        sendMessage.setUnionIds(unionIds);
        Map contentMap = fsService.getCardMessageMap(titleContent , messageContent, fsAppUrl,false);
        sendMessage.setContentMap(contentMap);
        //发送消息
        fsService.sendMessage(sendMessage);
    }

    @Override
    public List<TmsFirstMileLogisticDTO.WaitSubmitListDTO> waitSubmitReconciliation(List<String> ids) {
        List<TmsFirstMileLogisticDTO.WaitSubmitListDTO> list = tmsFirstMileReconciliationService.listByApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        if (CollectionUtils.isEmpty(list)){
            return list;
        }
        // 校验物理商是否一致
        List<LogisticsBillEntity> entityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(entityList)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "头程物流单");
        }
        List<String> logisticsSupperIds = entityList.stream()
                .map(LogisticsBillEntity::getLogisticsSupplierId)
                .distinct()
                .collect(Collectors.toList());
        if (logisticsSupperIds.size() > 1){
            throw new ServiceException("物流单的物流商不一致");
        }
        // 显示对应物流商对账单
        return list.stream()
                .filter(e->logisticsSupperIds.contains(e.getLogisticsSupplierId()))
                .collect(Collectors.toList());

    }

    @Override
    public IPage<TmsFirstMileReconciliationDetailDTO.ListDTO> waitReconciliationPaging(Page<?> query, TmsFirstMileReconciliationDetailDTO.PagingParamDTO params) {
        return this.baseMapper.waitReconciliationPaging(query, params,
                OrderTypeEnum.FIRST_MILE.getCode(),
                null,
                FmLogisticTrackStatusEnum.SIGN.getCode()
        );
    }

    @Override
    public List<TmsFirstMileReconciliationDetailDTO.ListDTO> listReconciliationByMainIds(List<String> logisticsBillIds) {
        return this.baseMapper.waitReconciliationList(
                OrderTypeEnum.FIRST_MILE.getCode(),
                "",
                "",
                logisticsBillIds,
                null,
                null,
                null,
                null);
    }

    @Override
    public List<TmsFirstMileReconciliationDetailDTO.ListDTO> listByTransportNoListAndSupplierIds(List<String> transportNoList, List<String> logisticsSupplierIdList) {
        return this.baseMapper.waitReconciliationList(
                OrderTypeEnum.FIRST_MILE.getCode(),
                "",
                "",
                null,
                transportNoList,
                logisticsSupplierIdList,
                null,
                null);
    }

    @Override
    public List<Map<String,Object>> getTrackStatusList() {
        List<Map<String,Object>> result = new ArrayList<>();
        for(FmLogisticTrackStatusEnum logisticTrackStatusEnum : FmLogisticTrackStatusEnum.values()){
            if(logisticTrackStatusEnum == FmLogisticTrackStatusEnum.EXCEPTION){
                continue;
            }
            Map<String,Object> map = new HashMap<>();
            map.put("code",logisticTrackStatusEnum.getCode());
            map.put("value",logisticTrackStatusEnum.getName());
            result.add(map);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO singleGenerateReconciliation(String id, String reconciliationId, List<LocalDate> dateList, Map<String, TmsFirstMileReconciliationEntity> currentMainEntityMap,String reconciliationType) {
        // 校验物理商是否一致
        List<TmsFirstMileReconciliationDetailDTO.ListDTO> sourceDetailList = this.listReconciliationByMainIds(Collections.singletonList(id));
        if (CollectionUtils.isEmpty(sourceDetailList)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "物流单");
        }
        String currency = sourceDetailList.stream().map(TmsFirstMileReconciliationDetailDTO.ListDTO::getCurrency).filter(StrUtil::isNotBlank).findFirst().orElse("");
        if (CharSequenceUtil.isBlank(currency)){
            throw new ServiceException("物流单费用配置币种类型不能为空");
        }
        //发货单
        sourceDetailList.forEach(e -> {
            e.setReconciliationId(reconciliationId);
            if (CharSequenceUtil.isBlank(reconciliationId)){
                e.setReconciliationStatus(null);
            }
        });
        boolean notSign = sourceDetailList.stream()
                .anyMatch(e -> !LogisticTrackStatusEnum.SIGN.getCode().equalsIgnoreCase(e.getTransportStatus()));
        if (notSign){
            throw new ServiceException("物流单未签收");
        }

        // 填充信息
        tmsFirstMileReconciliationDetailService.fillWaitReconciliationList(sourceDetailList, reconciliationId);
        TmsFirstMileReconciliationDetailDTO.ListDTO curListDTO = sourceDetailList.stream().findFirst().orElse(null);
        if (null == curListDTO){
            throw new ServiceException("数据异常, 明细为空");
        }
        //头程对账单明细查询
        List<TmsFirstMileReconciliationDetailEntity> tmsFirstMileReconciliationDetailEntityList = tmsFirstMileReconciliationDetailService.listBySourceIds(Collections.singletonList(id), DetailReconciliationTypeEnum.ACTUAL.getCode());
        // 是否当前新增账单
        boolean currenAddMainEntity = false;
        String mainKey = "";
        // 查询对账单ID
        TmsFirstMileReconciliationEntity reconciliationEntity = null;
        if (StringUtils.isNotBlank(reconciliationId)){
            reconciliationEntity = tmsFirstMileReconciliationService.getById(reconciliationId);
            if (null == reconciliationEntity){
                reconciliationEntity = tmsFirstMileReconciliationService.getByCode(reconciliationId);
                if (null == reconciliationEntity){
                    throw new ServiceException(ApiError.NOT_EXIST_BILL, "对账单");
                }
            }
            if (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equalsIgnoreCase(reconciliationEntity.getApproveStatus())){
                throw new ServiceException("对账单不处于待提交");
            }
            if (!reconciliationEntity.getCurrency().equalsIgnoreCase(curListDTO.getCurrency())){
                throw new ServiceException("对账单币种与当前物流单币种不一致");
            }
            reconciliationEntity.setUpdateTime(LocalDateTime.now());
        } else {
            TmsFirstMileReconciliationDetailDTO.ListDTO listDTO = sourceDetailList.get(0);
            String logisticsSupplierId =  listDTO.getLogisticsSupplierId();
            String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_TCZD);
            if (CollectionUtils.isEmpty(dateList)){
                throw new ServiceException("周期日期不能为空");
            }
            if (dateList.size() < 2){
                throw new ServiceException("周期日期不能为空");
            }
            LocalDate startDate = dateList.get(0);
            LocalDate endDate = dateList.get(1);
            mainKey = CharSequenceUtil.format("{}_{}_{}_{}", logisticsSupplierId, curListDTO.getCurrency(), startDate, endDate);
            //一个头程物流单可生成多次对账单-限制同一个单同一个月份仅可生成一次
            LocalDate dayOfMonth = endDate.withDayOfMonth(1);
            List<TmsFirstMileReconciliationDetailEntity> detailEntityList1 = tmsFirstMileReconciliationDetailEntityList.stream().filter(e -> Objects.nonNull(e) && CharSequenceUtil.isNotBlank(e.getMainId()) && dayOfMonth.equals(e.getReconciliationMonth())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(detailEntityList1)){
                List<String> sourceCodes = detailEntityList1.stream().map(TmsFirstMileReconciliationDetailEntity::getSourceCode).distinct().collect(Collectors.toList());
                throw new ServiceException(ApiError.ERROR_92260,String.join(",",sourceCodes), dayOfMonth);
            }
            // 之前已添加账单
            reconciliationEntity = currentMainEntityMap.get(mainKey);
            if (null == reconciliationEntity){
                TmsFirstMileReconciliationEntity existEntity = tmsFirstMileReconciliationService.getByGenerate(logisticsSupplierId, startDate, endDate, curListDTO.getCurrency());
                if (null != existEntity){
                    throw new ServiceException("当前周期和币种的对账单已存在");
                }
                reconciliationEntity = new TmsFirstMileReconciliationEntity(code,startDate, endDate, logisticsSupplierId, curListDTO.getCurrency());
                // 当前新增
                currenAddMainEntity = true;
            }
        }
        CurrencyDTO.ViewDTO currencyView = null;
        if (StringUtils.isNotBlank(reconciliationEntity.getCurrency())) {
            currencyView = tmsFirstMileReconciliationDetailService.getCurrencyView(reconciliationEntity.getCurrency());
        }

        // 当前提交可能存在上次一事务的历史明细
        List<TmsFirstMileReconciliationDetailDTO.ListDTO> oldListDTO = new LinkedList<>();
        if (null != reconciliationEntity.getId()){
            // 查询历史明细
            List<TmsFirstMileReconciliationDetailEntity> oldDetailEntityList = tmsFirstMileReconciliationDetailService.listByMainIds(Collections.singletonList(reconciliationEntity.getId()));
            if (CollectionUtils.isNotEmpty(oldDetailEntityList)){
                oldListDTO = BeanMapperUtils.copyList(TmsFirstMileReconciliationDetailDTO.ListDTO.class, oldDetailEntityList);
                tmsFirstMileReconciliationDetailService.fillDetailList(oldListDTO, reconciliationEntity.getCurrency(), currencyView);
            }
        }
        if(Objects.isNull(reconciliationEntity.getReconciliationMonth()) && Objects.nonNull(reconciliationEntity.getEndDate())){
            reconciliationEntity.setReconciliationMonth(reconciliationEntity.getEndDate().withDayOfMonth(1));
        }
        if (CharSequenceUtil.isBlank(reconciliationEntity.getCurrency()) || CurrencyEnum.CNY.getCurrencyCode().equals(reconciliationEntity.getCurrency())){
            reconciliationEntity.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
            reconciliationEntity.setExchangeRate(BigDecimal.ONE);
        }else {
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            BigDecimal rate = dmpTaskFeign.getRate(currentDate, currency);
            if (Objects.isNull(rate)){
                throw new ServiceException(ApiError.ERROR_EXCHANGE_RATE_NOT_EXIST, LocalDate.now(), currency);
            }
            reconciliationEntity.setExchangeRate(rate);
        }
        // 保存头程对账单
        tmsFirstMileReconciliationService.saveOrUpdate(reconciliationEntity);
        //当前明细对账单次数
        int reconciliationCount = 1;
        if (currenAddMainEntity){
            // 添加到当前账单记录
            currentMainEntityMap.put(mainKey, reconciliationEntity);
            TmsFirstMileReconciliationDetailEntity maxDetailEntity = tmsFirstMileReconciliationDetailEntityList.stream()
                    .filter(e -> Objects.nonNull(e) && StringUtils.isNotBlank(e.getMainId()))
                    .max(Comparator.comparing(TmsFirstMileReconciliationDetailEntity::getReconciliationCount)).orElse(null);
            if (Objects.nonNull(maxDetailEntity)){
                reconciliationCount = maxDetailEntity.getReconciliationCount() + 1;
            }
        }else {
            //不是新增取当前对账单内的对账次数
            TmsFirstMileReconciliationDetailEntity maxDetailEntity = tmsFirstMileReconciliationDetailEntityList.stream()
                    .filter(e -> Objects.nonNull(e) && StringUtils.isNotBlank(e.getMainId()) && Objects.equals(e.getMainId(), reconciliationId))
                    .findFirst().orElse(null);
            if (Objects.nonNull(maxDetailEntity)){
                reconciliationCount = maxDetailEntity.getReconciliationCount();
            }else {
                //其他对账单是否存在记录
                TmsFirstMileReconciliationDetailEntity maxDetailEntity2 = tmsFirstMileReconciliationDetailEntityList.stream()
                        .filter(e -> Objects.nonNull(e) && StringUtils.isNotBlank(e.getMainId()))
                        .max(Comparator.comparing(TmsFirstMileReconciliationDetailEntity::getReconciliationCount)).orElse(null);
                if (Objects.nonNull(maxDetailEntity2)){
                    reconciliationCount = maxDetailEntity2.getReconciliationCount() + 1;
                }
            }
        }


        // 保存明细
        // 生成实际和差异记录
        List<TmsFirstMileReconciliationDetailDTO.ListDTO> saveListDTO = tmsFirstMileReconciliationDetailService.generateAllTypeDTO(curListDTO, reconciliationCount,Boolean.FALSE);
        TmsFirstMileReconciliationDTO.UpdateDTO updateDTO = new TmsFirstMileReconciliationDTO.UpdateDTO();
        updateDTO.setId(reconciliationEntity.getId());

        List<TmsFirstMileReconciliationDetailDTO.UpdateDTO> detailDTOList;
        if (CollectionUtils.isNotEmpty(oldListDTO)){
            // 添加历史明细
            saveListDTO = resetSaveData(saveListDTO, oldListDTO);
        }
        detailDTOList = TmsFirstMileReconciliationConverter.INSTANCE.convertDetailDTOList(saveListDTO);
        //修改明细对账类型
        if (CharSequenceUtil.isNotBlank(reconciliationType)){
            detailDTOList.forEach(e -> e.setReconciliationType(reconciliationType));
        }
        updateDTO.setDetailList(detailDTOList);
        // 修改明细数据（包含增删改）
        tmsFirstMileReconciliationDetailService.update(updateDTO.getDetailList(), reconciliationEntity);

        // 更新已成功对账单
        List<String> sourceIds = Collections.singletonList(curListDTO.getSourceId());
        this.updateReconciliation(sourceIds, ReconciliationStatusEnum.TO_BE_CONFIRM.getCode(), reconciliationEntity.getId());

        return BatchResultDTO.success(id, curListDTO.getTransportNo(), OperationTypeEnum.ADD);
    }


    private List<TmsFirstMileReconciliationDetailDTO.ListDTO> resetSaveData(List<TmsFirstMileReconciliationDetailDTO.ListDTO> saveListDTO, List<TmsFirstMileReconciliationDetailDTO.ListDTO> oldListDTO) {
        if (CollectionUtils.isEmpty(oldListDTO)){
            oldListDTO = saveListDTO;
            return oldListDTO;
        }
        if (CollectionUtils.isEmpty(saveListDTO)){
            return oldListDTO;
        }

        List<TmsFirstMileReconciliationDetailDTO.ListDTO> finalOldListDTO = oldListDTO;
        saveListDTO.forEach(e -> {
            TmsFirstMileReconciliationDetailDTO.ListDTO listDTO = finalOldListDTO.stream().filter(f -> CharSequenceUtil.isNotBlank(e.getType()) && CharSequenceUtil.isNotBlank(e.getSourceId())
                    && CharSequenceUtil.isNotBlank(f.getType()) && CharSequenceUtil.isNotBlank(f.getSourceId())
                    && Objects.equals(f.getType(), e.getType()) && Objects.equals(e.getSourceId(), f.getSourceId())).findFirst().orElse(null);
            if (Objects.isNull(listDTO)){
                finalOldListDTO.add(e);
            }
        });
        return finalOldListDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateReconciliation(List<String> mainId, String status,String reconciliationId) {
        if (CollectionUtils.isEmpty(mainId)){
            return;
        }
        boolean update = logisticsBillCostService.lambdaUpdate()
                .set(LogisticsBillCostEntity::getReconciliationStatus, status)
                .in(LogisticsBillCostEntity::getLogisticsBillId, mainId)
                .eq(CharSequenceUtil.isNotBlank(reconciliationId), LogisticsBillCostEntity::getReconciliationId, reconciliationId)
                .update();
    }

    @Override
    public Boolean importExcel(MultipartFile excelFile, HttpServletResponse response) throws Exception{
        //物流信息
        FmLogisticsBillExcelListener billListener = new FmLogisticsBillExcelListener();
        EasyExcel.read(excelFile.getInputStream(), FmLogisticsBillExcelDTO.class, billListener).sheet(0).doRead();
        //物流费用
        FmLogisticsBillCostExcelListener costListener = new FmLogisticsBillCostExcelListener();
        EasyExcel.read(excelFile.getInputStream(), FmLogisticsBillCostExcelDTO.class, costListener).sheet(1).doRead();
        List<FmLogisticsBillExcelDTO> errorBillList = billListener.getErrorList();
        List<FmLogisticsBillCostExcelDTO> errorCostList = costListener.getErrorList();
        if(CollectionUtils.isNotEmpty(errorCostList) || CollectionUtils.isNotEmpty(errorBillList)){
            String fileName = new String("物流单导出失败.xlsx".getBytes(), "UTF-8");
            response.addHeader("Content-Disposition", "filename=" + fileName);
            response.setContentType("application/vnd.ms-excel");
            ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build();
            if(CollectionUtils.isEmpty(errorBillList)){
                WriteSheet writeSheet1 = EasyExcel.writerSheet(0, "物流费用").build();
                WriteTable writeTable = EasyExcel.writerTable(0).head(FmLogisticsBillCostExcelDTO.class).needHead(true).build();
                excelWriter.write(errorCostList, writeSheet1,writeTable);
            }else if (CollectionUtils.isEmpty(errorCostList)){
                WriteSheet writeSheet1 = EasyExcel.writerSheet(0, "物流信息").build();
                WriteTable writeTable = EasyExcel.writerTable(0).head(FmLogisticsBillExcelDTO.class).needHead(true).build();
                excelWriter.write(errorBillList, writeSheet1,writeTable);
            }else{
                WriteSheet writeSheet1 = EasyExcel.writerSheet(0, "物流信息").build();
                WriteSheet writeSheet2 = EasyExcel.writerSheet(1, "物流费用").build();
                WriteTable writeTable = EasyExcel.writerTable(0).head(FmLogisticsBillExcelDTO.class).needHead(true).build();
                WriteTable writeTable2 = EasyExcel.writerTable(1).head(FmLogisticsBillCostExcelDTO.class).needHead(true).build();
                excelWriter.write(errorBillList, writeSheet1,writeTable);
                excelWriter.write(errorCostList, writeSheet2,writeTable2);
            }
            excelWriter.finish();
            response.flushBuffer();
            return false;
        }
        return true;
    }

    @Override
    public void export(TmsFirstMileLogisticDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response) {
        pagingParamDTO.setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(1, Integer.MAX_VALUE,false);
        pagingParamDTO.setOrderType(OrderTypeEnum.FIRST_MILE.getCode());
        IPage<TmsFirstMileLogisticDTO.PagingVO> pageData = baseMapper.firstMilePaging(query, pagingParamDTO);
        List<TmsFirstMileLogisticDTO.PagingVO> list = pageData.getRecords();
        fillPagingDb(list);
        ExcelUtil.export("物流单"+ DateUtil.currentYMD(),"物流单",list,TmsFirstMileLogisticDTO.PagingVO.class,response);
    }

    @Override
    public void exportFeeDetail(TmsFirstMileLogisticDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response) {
        pagingParamDTO.setPermissionSql(pagingParamDTO.getPermissionSql());
        pagingParamDTO.setOrderType(OrderTypeEnum.FIRST_MILE.getCode());
        List<TmsFirstMileLogisticDTO.ExportCostDTO> list = baseMapper.firstMileFeeCostExport( pagingParamDTO);
        List<TmsFirstMileLogisticDTO.ExportCostDTO> handleList = fillCostExportDb(list);
        ExcelUtil.export("物流费用明细单"+ DateUtil.currentYMD(),"物流费用明细单",handleList,TmsFirstMileLogisticDTO.ExportCostDTO.class,response);
    }

    private List<TmsFirstMileLogisticDTO.ExportCostDTO> fillCostExportDb(List<TmsFirstMileLogisticDTO.ExportCostDTO> list) {
        List<String> costIdList = list.stream().map(TmsFirstMileLogisticDTO.ExportCostDTO::getCostId).distinct().collect(Collectors.toList());
        List<TmsCostDetailDTO.CostViewDTO> allCostDetailEntityList = logisticsBillCostDetailService.listCostByMainIdList(costIdList);
        if(CollectionUtils.isEmpty(allCostDetailEntityList)){
            return list;
        }
        List<TmsFirstMileLogisticDTO.ExportCostDTO> resultList = new ArrayList<>();
        for (TmsFirstMileLogisticDTO.ExportCostDTO exportCostDTO : list) {
            List<TmsCostDetailDTO.CostViewDTO> costDetailEntityList = allCostDetailEntityList.stream().filter(v->v.getMainId().equals(exportCostDTO.getCostId()) && StringUtils.isNotBlank(v.getType())).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(costDetailEntityList)){
                resultList.add(exportCostDTO);
                continue;
            }
            Map<String,List<TmsCostDetailDTO.CostViewDTO>> costViewMap = costDetailEntityList.stream().collect(Collectors.groupingBy(TmsCostDetailDTO.CostViewDTO::getCostName));
            costViewMap.forEach((key,value)->{
                TmsFirstMileLogisticDTO.ExportCostDTO costDTO = BeanUtil.copyProperties(exportCostDTO,TmsFirstMileLogisticDTO.ExportCostDTO.class);
                costDTO.setCostName(key);
                String currencySymbol = CurrencyEnum.getSymbolByCode(costDTO.getCurrency());
                //预计费用
                TmsCostDetailDTO.CostViewDTO estimateCost = value.stream().filter(v->v.getType().equals(LogisticsBillCostTypeEnum.ESTIMATED.getCode())).findFirst().orElse(new TmsCostDetailDTO.CostViewDTO());
                if(Objects.nonNull(estimateCost.getCostValue())){
                    costDTO.setCompleteEstimatedFee(currencySymbol+estimateCost.getCostValue());
                }
                //实际费用
                TmsCostDetailDTO.CostViewDTO actualCost = value.stream().filter(v->v.getType().equals(LogisticsBillCostTypeEnum.ACTUAL.getCode())).findFirst().orElse(new TmsCostDetailDTO.CostViewDTO());
                if(Objects.nonNull(actualCost.getCostValue())){
                    costDTO.setCompleteActualFee(currencySymbol+actualCost.getCostValue());
                }
                resultList.add(costDTO);
            });
        }
        return resultList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(LogisticsBillEntity logisticsBillEntity) {
        //是否下推头程重量分摊，下推则不允许删除
        List<FirstMileWeightAllocationEntity> firstMileWeightAllocationEntityList = firstMileWeightAllocationService.listByLogisticsBillIds(Collections.singletonList(logisticsBillEntity.getId()));
        if (CollectionUtils.isNotEmpty(firstMileWeightAllocationEntityList)){
            return BatchResultDTO.fail(logisticsBillEntity.getId(),logisticsBillEntity.getCounterNo(),"存在头程重量分摊，不能删除物流单");
        }
        List<LogisticsBillDetailEntity> detailList = logisticsBillDetailService.listByMainIds(Collections.singletonList(logisticsBillEntity.getId()));
        List<LogisticsBillCostEntity> costList = logisticsBillCostService.listByLogisticsBillIdList(Collections.singletonList(logisticsBillEntity.getId()));
        List<TmsCostDetailEntity> costDetailList = logisticsBillCostDetailService.listByMainIdList(costList.stream().map(BaseEntity::getId).collect(Collectors.toList()));
        List<String> costIds = new ArrayList<>();
        List<String> costDetailIds = new ArrayList<>();
        List<String> outstockIds = new ArrayList<>();
        LogisticsBillDetailEntity detailEntity = detailList.stream().filter(v->v.getMainId().equals(logisticsBillEntity.getId())).findFirst().orElse(null);
        if(Objects.nonNull(detailEntity) && !detailEntity.getTrackStatus().equals(FmLogisticTrackStatusEnum.WAIT_ORDER.getCode())){
            return BatchResultDTO.fail(logisticsBillEntity.getId(),logisticsBillEntity.getCounterNo(),"只有待下单可以删除");
        }
        outstockIds.add(logisticsBillEntity.getOutstockId());
        List<LogisticsBillCostEntity> costEntityList = costList.stream().filter(v->v.getLogisticsBillId().equals(logisticsBillEntity.getId())).collect(Collectors.toList());
        List<String> costIdList = costEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(costIdList)){
            costIds.addAll(costIdList);
            List<TmsCostDetailEntity> costDetailEntityList = costDetailList.stream().filter(v->costIdList.contains(v.getMainId())).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(costDetailEntityList)){
                costDetailIds.addAll(costDetailEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList()));
            }
        }
        if (CharSequenceUtil.isNotBlank(logisticsBillEntity.getId())){
            this.removeById(logisticsBillEntity.getId());
            //删除暂估账单
            firstMileEstimatedBillService.removeByLogisticsBillId(logisticsBillEntity.getId());
        }
        if(Objects.nonNull(detailEntity) && CharSequenceUtil.isNotBlank(detailEntity.getId())){
            logisticsBillDetailService.removeById(detailEntity.getId());
        }
        if(CollectionUtils.isNotEmpty(costIds)){
            logisticsBillCostService.removeByIds(costIds);
        }
        if(CollectionUtils.isNotEmpty(costDetailIds)){
            logisticsBillCostDetailService.removeByIds(costDetailIds);
        }
        if(CollectionUtils.isNotEmpty(outstockIds)){
            FirstMileDeliveryDTO.UpdateStatusDTO dto = new FirstMileDeliveryDTO.UpdateStatusDTO();
            dto.setIds(outstockIds);
            dto.setLogisticsStatus(FmDeliveryLogisticsStatusEnum.WAIT.code);
            wmsFirstMileDeliveryFeign.updateStatus(dto);
        }
        return BatchResultDTO.success(logisticsBillEntity.getId(),logisticsBillEntity.getCounterNo(),"删除操作成功");
    }

    @Override
    public TmsFirstMileLogisticDTO.HistoryTrackDTO getHistoryTrack(String id) {
        return null;
    }

    @Override
    public List<TmsFirstMileLogisticDTO.DeliveryDTO> getCanGenerateDeliveryOrder(TmsFirstMileLogisticDTO.CanGenerateDeliveryDTO dto) {
        FirstMileDeliveryDTO.GenerateLogisticReqDTO reqDto = new FirstMileDeliveryDTO.GenerateLogisticReqDTO();
        if(StringUtils.isNotBlank(dto.getOutstockId())){
            reqDto.setIds(Arrays.asList(dto.getOutstockId()));
        }
        if (CharSequenceUtil.isNotBlank(dto.getSearchKey())){
            reqDto.setSearchKey(dto.getSearchKey());
        }
        reqDto.setPackingStatus(PackingTaskStatusEnum.PACKED.getCode());
        reqDto.setLogisticsStatus(FmDeliveryLogisticsStatusEnum.WAIT.code);
        List<FirstMileDeliveryDTO.GenerateLogisticDTO> generateLogisticDTO = wmsFirstMileDeliveryFeign.getGenerateLogisticDTO(reqDto);
        List<TmsFirstMileLogisticDTO.DeliveryDTO> result = BeanUtil.copyToList(generateLogisticDTO,TmsFirstMileLogisticDTO.DeliveryDTO.class);
        //渠道为空设置体积重
        LogisticsChannelEntity channelEntity;
        if(StringUtils.isNotBlank(dto.getLogisticsChannelId())){
            channelEntity = logisticsChannelService.getById(dto.getLogisticsChannelId());
//            ShippingTemplateEntity shippingTemplateEntity =  shippingTemplateService.getByChannelId(dto.getLogisticsChannelId());
//            if(Objects.nonNull(channelEntity) && channelEntity.getVolumeSetting()>0){
//                for (TmsFirstMileLogisticDTO.DeliveryDTO deliveryDTO : result) {
//                    if(CollUtil.isNotEmpty(deliveryDTO.getPackingDTOList())){
//                        deliveryDTO.getPackingDTOList().forEach(v-> {
//                            v.setVolumeWeight(v.getMultiplySize().divide(BigDecimal.valueOf(channelEntity.getVolumeSetting()),4,RoundingMode.HALF_UP));
//                        });
//                    }
//                }
//            }
        } else {
            channelEntity = null;
        }

        List<String> shopIdList = result.stream().map(TmsFirstMileLogisticDTO.DeliveryDTO::getShopId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoEntityList = shopInfoFeign.listShopInfoByIds(shopIdList);
        result.forEach(v->{
            ShopInfoEntity shopInfoEntity = shopInfoEntityList.stream().filter(s->s.getId().equals(v.getShopId())).findFirst().orElse(null);
            if(Objects.nonNull(shopInfoEntity)){
                v.setChargeName(shopInfoEntity.getChargeName());
            }
            v.setLogisticsStatusName(FmLogisticTrackStatusEnum.WAIT_ORDER.getName());
            v.setFromCountryName("中国");
            //渠道为空设置体积重
            if(StringUtils.isNotBlank(dto.getLogisticsChannelId()) && CollUtil.isNotEmpty(v.getPackingDTOList()) && Objects.nonNull(channelEntity)){
                v.getPackingDTOList().forEach(f-> {
                    f.setVolumeWeight(f.getMultiplySize().divide(BigDecimal.valueOf(channelEntity.getVolumeSetting()),4,RoundingMode.HALF_UP));
                });
            }
        });
        return result;
    }

    @Override
    public Boolean updateRemark(TmsFirstMileLogisticDTO.UpdateRemarkDTO dto) {
        return lambdaUpdate().eq(LogisticsBillEntity::getId,dto.getId()).set(LogisticsBillEntity::getRemark,dto.getRemark()).update();
    }

    @Override
    public TmsFirstMileLogisticDTO.LogisticsDTO getLogisticsAndShipping(TmsFirstMileLogisticDTO.CanGenerateDeliveryDTO dto) {
        TmsFirstMileLogisticDTO.LogisticsDTO result = new TmsFirstMileLogisticDTO.LogisticsDTO();
        if(StringUtils.isBlank(dto.getLogisticsChannelId())){
            return result;
        }
        LogisticsChannelEntity logisticsChannelEntity = logisticsChannelService.getById(dto.getLogisticsChannelId());
        if(Objects.isNull(logisticsChannelEntity)){
            throw new ServiceException("渠道为空");
        }
        LogisticsSupplierEntity logisticsSupplierEntity = logisticsSupplierService.getById(logisticsChannelEntity.getMainId());
        if(Objects.isNull(logisticsSupplierEntity)){
            throw new ServiceException("物流供应商为空");
        }
        SupplierEntity supplier = scmTaskFeign.getSupplierById(logisticsSupplierEntity.getSupplierId());
        if(Objects.isNull(supplier)){
            throw new ServiceException("物流供应商为空");
        }
        result.setCurrency(supplier.getPayCurrency());
        result.setCurrencyName(Objects.isNull(CurrencyEnum.getByCode(supplier.getPayCurrency()))?"":CurrencyEnum.getByCode(supplier.getPayCurrency()).getCurrencyName());
        result.setLogisticsChannelName(logisticsChannelEntity.getName());
        result.setLogisticsChannelId(dto.getLogisticsChannelId());
        //0为默认值，不处理
        if(!logisticsChannelEntity.getEffectiveTime().equals("0")){
            //判断是否是数字，不是数字忽略
            String regex = "\\d*[1-9]+\\d*";
            Pattern pattern = Pattern.compile(regex);
            if(pattern.matcher(logisticsChannelEntity.getEffectiveTime()).matches()){
                int estimatedDay = Integer.parseInt(logisticsChannelEntity.getEffectiveTime());
                result.setEstimatedDay(estimatedDay);
                //现在单位只有天
                result.setEstimatedTimeUnit("天");
                result.setEstimatedTimeDesc(result.getEstimatedDay() + result.getEstimatedTimeUnit());
            }else{
                result.setEstimatedTimeDesc(logisticsChannelEntity.getEffectiveTime());
            }
        }

        result.setFeeRule(logisticsChannelEntity.getFeeRule());
        result.setFeeRuleName(ShippingFeeRuleEnum.getName(logisticsChannelEntity.getFeeRule()));
        LogisticsChannelEntity channelEntity = logisticsChannelService.getById(result.getLogisticsChannelId());
//        ShippingTemplateEntity shippingTemplateEntity = shippingTemplateService.getByChannelId(result.getLogisticsChannelId());
        if(Objects.nonNull(channelEntity)){
            result.setVolumeSetting(channelEntity.getVolumeSetting());
        }
        if(StringUtils.isNotBlank(dto.getOutstockId()) && Objects.nonNull(channelEntity) && channelEntity.getVolumeSetting() > 0){
            FirstMileDeliveryDTO.GenerateLogisticReqDTO reqDto = new FirstMileDeliveryDTO.GenerateLogisticReqDTO();
            reqDto.setIds(Arrays.asList(dto.getOutstockId()));
            List<FirstMileDeliveryDTO.GenerateLogisticDTO> generateLogisticDTO = wmsFirstMileDeliveryFeign.getGenerateLogisticDTO(reqDto);
            List<TmsFirstMileLogisticDTO.DeliveryDTO> deliveryDTOList = BeanUtil.copyToList(generateLogisticDTO,TmsFirstMileLogisticDTO.DeliveryDTO.class);
            //设置体积重
            if(CollectionUtils.isNotEmpty(deliveryDTOList)){
                TmsFirstMileLogisticDTO.DeliveryDTO deliveryDTO = deliveryDTOList.get(0);
                if (CollUtil.isNotEmpty(deliveryDTO.getPackingDTOList())) {
                    deliveryDTO.getPackingDTOList().forEach(v -> {
                        v.setVolumeWeight(v.getMultiplySize().divide(BigDecimal.valueOf(channelEntity.getVolumeSetting()), 4, RoundingMode.HALF_UP));
                    });
                    result.setPackingDTOList(deliveryDTO.getPackingDTOList());
                }
            }
        }
        return result;
    }

    @Override
    public List<LogisticsBillEntity> listByOutstcockCode(List<String> outstockCodeList) {
        if (CollectionUtil.isEmpty(outstockCodeList)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(LogisticsBillEntity::getOutstockCode, outstockCodeList).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateImport(List<LogisticsBillEntity> updateList, List<LogisticsBillDetailEntity> updateDetailList, List<LogisticsTrackEntity> addTrackList, List<LogisticsBillCostEntity> updateCostList) {
        if(CollectionUtils.isNotEmpty(updateList)){
            this.updateBatchById(updateList);
        }
        if(CollectionUtils.isNotEmpty(updateDetailList)){
            logisticsBillDetailService.updateBatchById(updateDetailList);
        }
        if(CollectionUtils.isNotEmpty(addTrackList)){
            logisticsTrackService.saveBatch(addTrackList);
        }
        if(CollectionUtils.isNotEmpty(updateCostList)){
            logisticsBillCostService.updateBatchById(updateCostList);
        }
    }

    @Override
    public List<LogisticsBillEntity> listByTransportNo(List<String> transportNoList) {
        if (CollectionUtil.isEmpty(transportNoList)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(LogisticsBillEntity::getTransportNo, transportNoList).eq(LogisticsBillEntity::getOrderType,OrderTypeEnum.FIRST_MILE.getCode()).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateImportCost(List<LogisticsBillCostEntity> updateCostList, List<TmsCostDetailEntity> updateCostDetailList) {
        if(CollectionUtils.isNotEmpty(updateCostList)){
            logisticsBillCostService.updateBatchById(updateCostList);
        }
        if(CollectionUtils.isNotEmpty(updateCostDetailList)){
            logisticsBillCostDetailService.updateBatchById(updateCostDetailList);
        }
    }

    @Override
    public BigDecimal calculateShippingCost(TmsFirstMileLogisticDTO.CalculateShippingCostDTO dto) {
        LogisticsChannelEntity channelEntity = logisticsChannelService.getById(dto.getChannelId());
        if(Objects.isNull(channelEntity)){
            throw new ServiceException("物流渠道为空");
        }

        ShippingTemplateEntity shippingTemplateEntity = shippingTemplateService.getByChannelId(dto.getChannelId());
        if(Objects.isNull(shippingTemplateEntity)){
            throw new ServiceException("运费模板为空");
        }
        FirstMileDeliveryDTO.GenerateLogisticReqDTO deliveryDto = new FirstMileDeliveryDTO.GenerateLogisticReqDTO();
        deliveryDto.setIds(Arrays.asList(dto.getOutstockId()));
        List<FirstMileDeliveryDTO.GenerateLogisticDTO> generateLogisticDTO = wmsFirstMileDeliveryFeign.getGenerateLogisticDTO(deliveryDto);
        if(CollectionUtils.isEmpty(generateLogisticDTO)){
            throw new ServiceException("未获取发货单信息");
        }
        FirstMileDeliveryDTO.GenerateLogisticDTO logisticDTO = generateLogisticDTO.get(0);
        List<WmsCartonDetailDTO.ListPackingDetailDTO> packingDetailDTOList = logisticDTO.getPackingDTOList();
        if(CollectionUtils.isEmpty(packingDetailDTOList)){
            throw new ServiceException("发货单装箱信息为空，无法计算");
        }
        if(logisticDTO.getSourceType().equals(SourceTypeEnum.FBA_SHIPMENT.getCode()) && !ShippingTemplateTypeEnum.ENUM_WAREHOUSE.getCode().equals(shippingTemplateEntity.getType())){
            throw new ServiceException("FBA发货单不支持非仓库类型模板计算");
        }
        if(logisticDTO.getSourceType().equals(SourceTypeEnum.DELIVERY_PLAN.getCode()) && !ShippingTemplateTypeEnum.ENUM_COUNTRY.getCode().equals(shippingTemplateEntity.getType())){
            throw new ServiceException("海外仓发货单不支持非国家类型模板计算");
        }

        BigDecimal totalWeight = packingDetailDTOList.stream()
                .map(WmsCartonDetailDTO.ListPackingDetailDTO::getPackageWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if(totalWeight.compareTo(BigDecimal.ZERO) == 0){
            return BigDecimal.ZERO;
        }

        ShippingTemplateRuleDTO.ViewParamDTO viewParamDTO = new ShippingTemplateRuleDTO.ViewParamDTO();
        viewParamDTO.setMainId(shippingTemplateEntity.getId());
        viewParamDTO.setFromCountry("CN");
        if(logisticDTO.getSourceType().equals(SourceTypeEnum.DELIVERY_PLAN.getCode())){
            viewParamDTO.setToCountry(logisticDTO.getToCountry());
        }
        if(logisticDTO.getSourceType().equals(SourceTypeEnum.FBA_SHIPMENT.getCode())){
            viewParamDTO.setToWarehouseName(logisticDTO.getToWarehouseName());
        }
        viewParamDTO.setWeight(totalWeight);
        ShippingTemplateRuleEntity shippingTemplateRuleEntity = shippingTemplateRuleService.getShippingTemplateRule(viewParamDTO);
        if(Objects.isNull(shippingTemplateRuleEntity)){
            throw new ServiceException("运费模板规则为空");
        }
        BigDecimal maxLength = packingDetailDTOList.stream()
                .map(WmsCartonDetailDTO.ListPackingDetailDTO::getLength)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal maxWidth = packingDetailDTOList.stream()
                .map(WmsCartonDetailDTO.ListPackingDetailDTO::getWidth)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal maxHeight = packingDetailDTOList.stream()
                .map(WmsCartonDetailDTO.ListPackingDetailDTO::getHeight)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        return shippingCalculationService.calculationFinalShippingCost(shippingTemplateEntity,shippingTemplateRuleEntity,channelEntity,totalWeight,maxLength,maxWidth,maxHeight).getTotalShippingCost();
    }

    @Override
    public List<TmsFirstMileReconciliationDetailDTO.ListDTO> listAutoGenerateFirstMileReconciliation(LocalDate startDate, LocalDate endDate) {
        return this.baseMapper.waitReconciliationList(
                OrderTypeEnum.FIRST_MILE.getCode(),
                ReconciliationStatusEnum.TO_BE_GENERATED.getCode(),
                FmLogisticTrackStatusEnum.SIGN.getCode(),
                null,
                null,
                null,
                startDate,
                endDate

        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO autoGenerateFirstMileLogistic(AutoGenerateBillDTO autoGenerateBillDTO) {
        if(StringUtils.isBlank(autoGenerateBillDTO.getId()) || Objects.isNull(autoGenerateBillDTO.getSourceTypeEnum()) || Objects.isNull(autoGenerateBillDTO.getBillGenerateTimingEnum())){
            return BatchResultDTO.fail(autoGenerateBillDTO.getId(),"", "账单id,数据来源类型，生成方式不能为空");
        }
        CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingEnum.BILL_AUTO_ADD.getCode());
        if(cfgSettingEntity.getDisabled()){
            return BatchResultDTO.fail(autoGenerateBillDTO.getId(),"", "头程物流单生成配置不存在");
        }
        CfgSettingValueDTO.BillAutoAddDTO dto = BeanUtil.toBean(cfgSettingEntity.getDataJson(), CfgSettingValueDTO.BillAutoAddDTO.class);
        if(Objects.isNull(dto) || Objects.isNull(dto.getIsAutoLogistics()) || !dto.getIsAutoLogistics() ||
                StringUtils.isBlank(dto.getLogisticsGenerateTiming()) || !dto.getLogisticsGenerateTiming().equals(autoGenerateBillDTO.getBillGenerateTimingEnum().getCode())){
            return BatchResultDTO.fail(autoGenerateBillDTO.getId(),"", "头程物流单生成方式不一致");
        }
        //生成物流单
        TmsFirstMileLogisticDTO.AddDTO addDTO = new TmsFirstMileLogisticDTO.AddDTO();
        addDTO.setOutstockId(autoGenerateBillDTO.getId());
        addDTO.setIsAuto(true);
        this.add(addDTO);
        return BatchResultDTO.success(autoGenerateBillDTO.getId(),"", "头程物流单创建成功");
    }

    @Override
    public List<TmsFirstMileLogisticDTO.WeightAllocationDTO> assembleFirstMileEstimatedList() {
        return baseMapper.assembleFirstMileEstimatedList(null);
    }

    @Override
    public BatchResultDTO pushWeightAllocation(String id) throws InterruptedException {
        //重量分摊基础数据
        List<TmsFirstMileLogisticDTO.WeightAllocationDTO> list = baseMapper.assembleFirstMileEstimatedList(Collections.singletonList(id));
        if(list.isEmpty()){
            return BatchResultDTO.fail(id, id, "只有下单后的物流单才能推送重量分摊");
        }
        return firstMileWeightAllocationService.add(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO generateLogisticsBill(FirstMileDeliveryEntity firstMileDeliveryEntity) {
        if (!ApproveStatusEnum.APPROVE.getStatus().equals(firstMileDeliveryEntity.getApproveStatus())){
            return BatchResultDTO.fail(firstMileDeliveryEntity.getId(),firstMileDeliveryEntity.getCode(),"只有已审核发货单才能下推物流单");
        }
        if(!com.erp.model.wms.enums.FmDeliveryLogisticsStatusEnum.WAIT.equals(firstMileDeliveryEntity.getLogisticsStatus())){
            return BatchResultDTO.fail(firstMileDeliveryEntity.getId(),firstMileDeliveryEntity.getCode(),"物流单只有未生成状态才能下推");
        }

        TmsFirstMileLogisticDTO.AddDTO addDTO = new TmsFirstMileLogisticDTO.AddDTO();
        addDTO.setOutstockId(firstMileDeliveryEntity.getId());
        //走TMS生成物流单逻辑
        this.add(addDTO);
        return BatchResultDTO.success(addDTO.getOutstockId(),"", "头程物流单创建成功");
    }
}
