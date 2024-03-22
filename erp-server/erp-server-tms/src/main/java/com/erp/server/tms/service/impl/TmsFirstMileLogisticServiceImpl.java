package com.erp.server.tms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.InvoicesStatusEnum;
import com.common.business.enums.OrderTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.FmDeliveryLogisticsStatusEnum;
import com.erp.model.scm.dto.AttachmentDTO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.FmLogisticTrackStatusEnum;
import com.erp.model.tms.enums.FmTimeLineEnum;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.model.tms.dto.LogisticsBillDetailDTO;
import com.erp.model.tms.dto.TmsFirstMileLogisticDTO;
import com.erp.model.tms.dto.TmsLogisticsBillCostDetailDTO;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.LogisticsSupplierEntity;
import com.erp.model.tms.entity.ShippingTemplateEntity;
import com.erp.model.tms.enums.LogisticTrackStatusEnum;
import com.erp.model.tms.enums.LogisticsBillCostTypeEnum;
import com.erp.model.tms.enums.ReconciliationStatusEnum;
import com.erp.model.tms.enums.ShippingBillingMethodEnum;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.dto.WmsCartonDetailDTO;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.model.wms.enums.PackingStatusEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.server.tms.convert.FmLogisticsConverter;
import com.erp.server.tms.mapper.LogisticsBillMapper;
import com.erp.server.tms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
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

    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private CommonService commonService;

    @Resource
    private WmsFirstMileDeliveryFeign wmsFirstMileDeliveryFeign;

    @Resource
    private LogisticsBillCostService logisticsBillCostService;

    @Resource
    private ShippingTemplateService shippingTemplateService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private TmsLogisticsBillCostDetailService logisticsBillCostDetailService;

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
    private LogisticsTrackService logisticsTrackService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TmsFirstMileLogisticDTO.AddDTO addDTO) {
        //发货单id
        String outstockId = addDTO.getOutstockId();
        FirstMileDeliveryDTO.GenerateLogisticDTO generateLogisticDTO = this.getGenerateLogisticDTO(outstockId);
        if(generateLogisticDTO == null){
            throw new ServiceException("发货单不存在或者未装箱或已生成物流单");
        }

        if(Objects.isNull(addDTO.getLogisticsOrderTime())){
            addDTO.setLogisticsOrderTime(LocalDateTime.now());
        }
        LocalDateTime shipTime = sailingService.calculateShipTime(addDTO.getLogisticsChannelId(),LocalDateTime.now());

        //新增物流单
        LogisticsBillEntity tmsFirstMileLogisticEntity = FmLogisticsConverter.INSTANCE.addLogisticsBill(generateLogisticDTO,addDTO);
        if(StringUtils.isBlank(tmsFirstMileLogisticEntity.getRemark())){
            tmsFirstMileLogisticEntity.setRemark(generateLogisticDTO.getRemark());
        }
        tmsFirstMileLogisticEntity.setShipTime(shipTime);
        log.info("开始新增头程物流单");
        boolean save = super.save(tmsFirstMileLogisticEntity);
        if(!save) {
            throw new ServiceException("头程物流单保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "头程物流单" , tmsFirstMileLogisticEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LOGISTICS_BILL.getCode(), tmsFirstMileLogisticEntity.getId(), "新增操作");

        //新增物流费用单
        LogisticsBillCostDTO.AddDTO costAddDTO = this.packCostAddDTO(generateLogisticDTO,addDTO,tmsFirstMileLogisticEntity);

        //物流费用单明细
        List<TmsFirstMileLogisticDTO.LogisticFee> logisticFeeList = addDTO.getLogisticFeeList();
        List<TmsLogisticsBillCostDetailDTO.AddDTO> costDetailList = new ArrayList<>();
        for (TmsFirstMileLogisticDTO.LogisticFee logisticFee : logisticFeeList) {
            TmsLogisticsBillCostDetailDTO.AddDTO dto = new TmsLogisticsBillCostDetailDTO.AddDTO();
            dto.setCostValue(logisticFee.getEstimatedFee());
            dto.setCfgCostId(logisticFee.getCfgCostId());
            dto.setType(LogisticsBillCostTypeEnum.ESTIMATED.getCode());
            costDetailList.add(dto);
        }
        costAddDTO.setCostDetailList(costDetailList);
        logisticsBillCostService.add(costAddDTO);

        //新增物流单
        List<LogisticsBillDetailDTO.AddDTO> detailAddList = new ArrayList<>();
        LogisticsBillDetailDTO.AddDTO detailAddDto = new LogisticsBillDetailDTO.AddDTO();
        detailAddDto.setMainId(tmsFirstMileLogisticEntity.getId());
        detailAddDto.setTrackNo(tmsFirstMileLogisticEntity.getCounterNo());
        detailAddDto.setTrackStatus(FmLogisticTrackStatusEnum.ORDERED.getCode());
        detailAddList.add(detailAddDto);
        logisticsBillDetailService.add(tmsFirstMileLogisticEntity,detailAddList);

        //设置附件信息
        Class<LogisticsBillEntity> credentialClass = LogisticsBillEntity.class;
        TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
        String type = tableName.value();
        attachmentService.batchSave(addDTO.getAttachmentUrlList(), addDTO.getAttachmentNameList(), type, tmsFirstMileLogisticEntity.getId());

        //更新发货单物流状态
        FirstMileDeliveryDTO.UpdateStatusDTO updateDeliveryDto = new FirstMileDeliveryDTO.UpdateStatusDTO();
        updateDeliveryDto.setId(addDTO.getOutstockId());
        updateDeliveryDto.setLogisticsStatus(FmDeliveryLogisticsStatusEnum.FINISH.code);
        if(!wmsFirstMileDeliveryFeign.updateStatus(updateDeliveryDto)){
            throw new ServiceException("发货单更新物流状态失败");
        }

        return new BaseResultDTO.AddDTO(tmsFirstMileLogisticEntity.getId(), tmsFirstMileLogisticEntity.getOutstockCode());
    }

    private LogisticsBillCostDTO.AddDTO packCostAddDTO(FirstMileDeliveryDTO.GenerateLogisticDTO generateLogisticDTO, TmsFirstMileLogisticDTO.AddDTO addDTO,LogisticsBillEntity tmsFirstMileLogisticEntity) {
        //装箱信息
        List<WmsCartonDetailDTO.ListPackingDetailDTO> packingDTOList = generateLogisticDTO.getPackingDTOList();
        LogisticsBillCostDTO.AddDTO costAddDTO = new LogisticsBillCostDTO.AddDTO();
        costAddDTO.setReconciliationStatus(ReconciliationStatusEnum.TO_BE_GENERATED.getCode());
        costAddDTO.setLogisticsBillId(tmsFirstMileLogisticEntity.getId());
        if(CollectionUtils.isNotEmpty(packingDTOList)){
            //预估实重
            BigDecimal actualWeight = packingDTOList.stream()
                    .map(WmsCartonDetailDTO.ListPackingDetailDTO::getPackageWeight)
                    .map(BigDecimal::new)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            costAddDTO.setActualWeight(actualWeight);
            //设置预估体积重 = 长宽高/材积
            ShippingTemplateEntity shippingTemplateEntity = shippingTemplateService.getByChannelId(addDTO.getLogisticsChannelId());
            if(Objects.nonNull(shippingTemplateEntity) && shippingTemplateEntity.getVolumeSetting() > 0){
                BigDecimal totalSize = packingDTOList.stream()
                        .map(WmsCartonDetailDTO.ListPackingDetailDTO::getMultiplySize)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                costAddDTO.setActualWeight(actualWeight);
                costAddDTO.setVolumeWeight(totalSize.divide(BigDecimal.valueOf(shippingTemplateEntity.getVolumeSetting()),4, RoundingMode.HALF_UP));
            }
        }
        costAddDTO.setCurrency(addDTO.getCurrency());
        costAddDTO.setRemark(addDTO.getRemark());
        costAddDTO.setTransportNo(addDTO.getTransportNo());
        costAddDTO.setChannelId(addDTO.getLogisticsChannelId());
        costAddDTO.setVolumeWeightLogistics(addDTO.getActualVolumeWeight());
        costAddDTO.setWeightLogistics(addDTO.getActualWeight());
        return costAddDTO;
    }

    private FirstMileDeliveryDTO.GenerateLogisticDTO getGenerateLogisticDTO(String outstockId){
        FirstMileDeliveryDTO.GenerateLogisticReqDTO dto = new FirstMileDeliveryDTO.GenerateLogisticReqDTO();
        dto.setIds(Arrays.asList(outstockId));
        dto.setPackingStatus(PackingStatusEnum.PACKING.getCode());
        dto.setLogisticsStatus(FmDeliveryLogisticsStatusEnum.WAIT.code);
        List<FirstMileDeliveryDTO.GenerateLogisticDTO> generateLogisticDTO = wmsFirstMileDeliveryFeign.getGenerateLogisticDTO(dto);
        if(CollectionUtil.isEmpty(generateLogisticDTO)){
            return null;
        }
        return generateLogisticDTO.get(0);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TmsFirstMileLogisticDTO.UpdateDTO updateDTO) {
        LogisticsBillEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "头程物流单"));
        LogisticsBillEntity tmsFirstMileLogisticEntity =  BeanMapperUtils.map(LogisticsBillEntity.class, updateDTO);

        // 数据处理
        handleData(tmsFirstMileLogisticEntity);
        log.info("编辑 开始修改头程物流单数据，id：【{}】", old.getId());
        boolean save = super.updateById(tmsFirstMileLogisticEntity);
        if(!save) {
            throw new ServiceException("头程物流单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录头程物流单日志数据，id：【{}】", tmsFirstMileLogisticEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), tmsFirstMileLogisticEntity.getId(), "头程物流单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, tmsFirstMileLogisticEntity, null, tmsFirstMileLogisticEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<LogisticsBillEntity> listBySourceIds(List<String> sourceIds) {
        if (CollectionUtil.isEmpty(sourceIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(LogisticsBillEntity::getSourceId, sourceIds).list();
    }
    @Override
    public List<TmsFirstMileLogisticDTO.TabListDTO> tabList() {
        return null;
    }

    @Override
    public PagingVO<TmsFirstMileLogisticDTO.PagingVO> paging(PagingDTO<TmsFirstMileLogisticDTO.PagingParamDTO> dto) {
        TmsFirstMileLogisticDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        params.setOrderType(OrderTypeEnum.FIRST_MILE.getCode());
        IPage<TmsFirstMileLogisticDTO.PagingVO> pageData = baseMapper.firstMilePaging(query, params);
        List<TmsFirstMileLogisticDTO.PagingVO> list = pageData.getRecords();
        fillPagingDb(list);
        return new PagingVO<>(pageData);
    }

    private void fillPagingDb(List<TmsFirstMileLogisticDTO.PagingVO> list) {
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        List<String> channelIdList = list.stream().map(TmsFirstMileLogisticDTO.PagingVO::getLogisticsChannelId).collect(Collectors.toList());
        List<LogisticsChannelEntity> logisticsChannelEntityList = logisticsChannelService.listByIds(channelIdList);

        List<String> logisticsSupplierIdList = list.stream().map(TmsFirstMileLogisticDTO.PagingVO::getLogisticsSupplierId).collect(Collectors.toList());
        List<LogisticsSupplierEntity> logisticsSupplierEntityList = logisticsSupplierService.listByIds(logisticsSupplierIdList);

        List<String> outstockIdList = list.stream().map(TmsFirstMileLogisticDTO.PagingVO::getOutstockId).collect(Collectors.toList());

        FirstMileDeliveryDTO.GenerateLogisticReqDTO dto = new FirstMileDeliveryDTO.GenerateLogisticReqDTO();
        dto.setIds(outstockIdList);
        List<FirstMileDeliveryDTO.GenerateLogisticDTO> generateLogisticDTOList = wmsFirstMileDeliveryFeign.getGenerateLogisticDTO(dto);

        for (TmsFirstMileLogisticDTO.PagingVO pagingVO : list) {
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

            //处理发货单相关信息
            FirstMileDeliveryDTO.GenerateLogisticDTO deliveryDto = generateLogisticDTOList.stream().filter(v->v.getOutstockId().equals(pagingVO.getOutstockId())).findFirst().orElse(null);
            if(Objects.nonNull(deliveryDto)){
                pagingVO.setFromWarehouseName(deliveryDto.getFromWarehouseName());
                pagingVO.setToWarehouseName(deliveryDto.getToWarehouseName());
                pagingVO.setCurrencySymbol(CurrencyEnum.getSymbolByCode(pagingVO.getCurrency()));
                pagingVO.setToAddress(deliveryDto.getToAddress());
                pagingVO.setBoxCount(CollectionUtils.isNotEmpty(deliveryDto.getPackingDTOList())?deliveryDto.getPackingDTOList().size():0);
            }

            //处理实际时效和预警
            int days = pagingVO.getActualHour() / 24; // 计算天数部分
            int remainingHours = pagingVO.getActualHour() % 24; // 计算剩余小时数部分
            String actualDesc = (days !=0 ? days+ "天":"") + remainingHours + "小时";
            pagingVO.setActualDesc(actualDesc);
            if(StringUtils.isNotBlank(logisticsChannelEntity.getEffectiveTime()) && StringUtils.isNotBlank(logisticsChannelEntity.getEffectiveTimeUnit())){
                //0为默认值，不处理
                if(logisticsChannelEntity.getEffectiveTime().equals("0")){
                    continue;
                }
                //判断是否是数字，不是数字忽略
                String regex = "\\d*[1-9]+\\d*";
                Pattern pattern = Pattern.compile(regex);
                if(!pattern.matcher(logisticsChannelEntity.getEffectiveTime()).matches()){
                    continue;
                }
                int estimatedDay = Integer.parseInt(logisticsChannelEntity.getEffectiveTime());
                pagingVO.setEstimatedDay(estimatedDay);
                //现在单位只有天
                pagingVO.setEstimatedTimeUnit("天");
                pagingVO.setEstimatedTimeDesc(pagingVO.getEstimatedDay() + pagingVO.getEstimatedTimeUnit());
                //设置预警
                pagingVO.setWarnHour(estimatedDay*24 - pagingVO.getActualHour());
            }
        }
    }

    private void fillViewDb(TmsFirstMileLogisticDTO.ViewDTO dto) {
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
        dto.setBillingMethodName(logisticsDTO.getBillingMethodName());
        dto.setEstimatedDay(logisticsDTO.getEstimatedDay());
        dto.setEstimatedTimeUnit(logisticsDTO.getEstimatedTimeUnit());
        dto.setEstimatedTimeDesc(logisticsDTO.getEstimatedTimeDesc());
        dto.setLogisticsChannelName(logisticsDTO.getLogisticsChannelName());

        //处理实际时效
        int days = dto.getActualHour() / 24; // 计算天数部分
        int remainingHours = dto.getActualHour() % 24; // 计算剩余小时数部分
        String actualDesc = (days !=0 ? days+ "天":"") + remainingHours + "小时";
        dto.setActualDesc(actualDesc);

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
                if(logisticsDTO.getVolumeSetting() != null && logisticsDTO.getVolumeSetting() > 0){
                    deliveryDTO.getPackingDTOList().forEach(v -> {
                        v.setVolumeWeight(v.getMultiplySize().divide(BigDecimal.valueOf(logisticsDTO.getVolumeSetting()), 4, RoundingMode.HALF_UP));
                    });
                }
                dto.setPackingDTOList(deliveryDTO.getPackingDTOList());
            }
        }
        //处理费用信息
        LogisticsBillCostEntity logisticsBillCostEntity = logisticsBillCostService.getByLogisticsBillId(dto.getId());
        if(Objects.nonNull(logisticsBillCostEntity)){
            List<TmsLogisticsBillCostDetailDTO.CostCompareDTO> costCompareDTOList = logisticsBillCostDetailService.getCostCompareListById(logisticsBillCostEntity.getLogisticsBillId());
            dto.setFeeViewList( BeanUtil.copyToList(costCompareDTOList,TmsFirstMileLogisticDTO.FeeViewDTO.class));
        }
        //处理时间线
        TmsFirstMileLogisticDTO.TimeInfoDTO timeInfoDTO = new TmsFirstMileLogisticDTO.TimeInfoDTO();
        timeInfoDTO.setApproveTime(CollectionUtils.isNotEmpty(generateLogisticDTO)?generateLogisticDTO.get(0).getApproveTime():null);
        timeInfoDTO.setLogisticOrderTime(dto.getOrderTime());
        timeInfoDTO.setShipTime(dto.getShipTime());
        timeInfoDTO.setSignTime(dto.getSignTime());
        LogisticsTrackDTO.ViewDTO viewDTO = logisticsTrackService.listByTrackNo(dto.getCounterNo());
        timeInfoDTO.setTrackingTime(viewDTO.getList().stream().filter(v->v.getStatus().equals(FmLogisticTrackStatusEnum.TRACK_ING.getCode())).findFirst().orElse(new LogisticsTrackDTO.ListDTO()).getTrackTime());
        timeInfoDTO.setArrivedTime(viewDTO.getList().stream().filter(v->v.getStatus().equals(FmLogisticTrackStatusEnum.ARRIVED.getCode())).findFirst().orElse(new LogisticsTrackDTO.ListDTO()).getTrackTime());
        dto.setTimeLineList(FmTimeLineEnum.convertToViewList(timeInfoDTO));

    }

    @Override
    public TmsFirstMileLogisticDTO.StatisticsVO statistics() {
        return null;
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
    public List<BatchResultDTO> updateLogisticsStatus(TmsFirstMileLogisticDTO.UpdateLogisticsStatusDTO dto) {
        return null;
    }

    @Override
    public List<BatchResultDTO> updateInvoicesStatus(TmsFirstMileLogisticDTO.UpdateInvoicesStatusDTO dto) {
        return null;
    }

    @Override
    public void exportInvoices(List<String> ids, HttpServletResponse response) {

    }

    @Override
    public List<BatchResultDTO> updateChannel(TmsFirstMileLogisticDTO.UpdateChannelDTO dto) {
        return null;
    }

    @Override
    public List<BatchResultDTO> generateReconciliation(TmsFirstMileLogisticDTO.GenerateReconciliationDTO dto) {
        return null;
    }

    @Override
    public void exportTemplate(HttpServletRequest request, HttpServletResponse response) {

    }

    @Override
    public Boolean importExcel(MultipartFile excelFile, HttpServletResponse response) {
        return null;
    }

    @Override
    public void export(TmsFirstMileLogisticDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response) {

    }

    @Override
    public void exportFeeDetail(TmsFirstMileLogisticDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response) {

    }

    @Override
    public List<BatchResultDTO> delete(List<String> ids) {
        return null;
    }

    @Override
    public TmsFirstMileLogisticDTO.HistoryTrackDTO getHistoryTrack(String id) {
        return null;
    }

    @Override
    public List<TmsFirstMileLogisticDTO.DeliveryDTO> getCanGenerateDeliveryOrder(TmsFirstMileLogisticDTO.CanGenerateDeliveryDTO dto) {
        FirstMileDeliveryDTO.GenerateLogisticReqDTO reqDto = new FirstMileDeliveryDTO.GenerateLogisticReqDTO();
        reqDto.setIds(Arrays.asList(dto.getOutstockId()));
        reqDto.setPackingStatus(PackingStatusEnum.PACKING.getCode());
        reqDto.setLogisticsStatus(FmDeliveryLogisticsStatusEnum.WAIT.code);
        List<FirstMileDeliveryDTO.GenerateLogisticDTO> generateLogisticDTO = wmsFirstMileDeliveryFeign.getGenerateLogisticDTO(reqDto);
        List<TmsFirstMileLogisticDTO.DeliveryDTO> result = BeanUtil.copyToList(generateLogisticDTO,TmsFirstMileLogisticDTO.DeliveryDTO.class);
        //渠道为空设置体积重
        if(StringUtils.isNotBlank(dto.getLogisticsChannelId())){
            ShippingTemplateEntity shippingTemplateEntity =  shippingTemplateService.getByChannelId(dto.getLogisticsChannelId());
            if(Objects.nonNull(shippingTemplateEntity) && shippingTemplateEntity.getVolumeSetting()>0){
                for (TmsFirstMileLogisticDTO.DeliveryDTO deliveryDTO : result) {
                    if(CollectionUtil.isNotEmpty(deliveryDTO.getPackingDTOList())){
                        deliveryDTO.getPackingDTOList().forEach(v-> {
                            v.setVolumeWeight(v.getMultiplySize().divide(BigDecimal.valueOf(shippingTemplateEntity.getVolumeSetting()),4,RoundingMode.HALF_UP));
                        });
                    }
                }
            }
        }
        result.forEach(v->{
            v.setLogisticsStatusName(FmLogisticTrackStatusEnum.WAIT_ORDER.getName());
            v.setFromCountryName("中国");
        });
        return result;
    }

    @Override
    public Boolean updateRemark(TmsFirstMileLogisticDTO.UpdateRemarkDTO dto) {
        return null;
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
            }
        }

        ShippingTemplateEntity shippingTemplateEntity = shippingTemplateService.getByChannelId(result.getLogisticsChannelId());
        if(Objects.nonNull(shippingTemplateEntity)){
            result.setBillingMethod(shippingTemplateEntity.getBillingMethod());
            result.setBillingMethodName(EnumMessage.getNameByCode(ShippingBillingMethodEnum.class,shippingTemplateEntity.getBillingMethod()));
            result.setVolumeSetting(shippingTemplateEntity.getVolumeSetting());
        }
        if(StringUtils.isNotBlank(dto.getOutstockId()) && Objects.nonNull(shippingTemplateEntity) && shippingTemplateEntity.getVolumeSetting() > 0){
            FirstMileDeliveryDTO.GenerateLogisticReqDTO reqDto = new FirstMileDeliveryDTO.GenerateLogisticReqDTO();
            reqDto.setIds(Arrays.asList(dto.getOutstockId()));
            List<FirstMileDeliveryDTO.GenerateLogisticDTO> generateLogisticDTO = wmsFirstMileDeliveryFeign.getGenerateLogisticDTO(reqDto);
            List<TmsFirstMileLogisticDTO.DeliveryDTO> deliveryDTOList = BeanUtil.copyToList(generateLogisticDTO,TmsFirstMileLogisticDTO.DeliveryDTO.class);
            //设置体积重
            if(CollectionUtils.isNotEmpty(deliveryDTOList)){
                TmsFirstMileLogisticDTO.DeliveryDTO deliveryDTO = deliveryDTOList.get(0);
                if (CollectionUtil.isNotEmpty(deliveryDTO.getPackingDTOList())) {
                    deliveryDTO.getPackingDTOList().forEach(v -> {
                        v.setVolumeWeight(v.getMultiplySize().divide(BigDecimal.valueOf(shippingTemplateEntity.getVolumeSetting()), 4, RoundingMode.HALF_UP));
                    });
                    result.setPackingDTOList(deliveryDTO.getPackingDTOList());
                }
            }
        }
        return result;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(LogisticsBillEntity tmsFirstMileLogisticEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
