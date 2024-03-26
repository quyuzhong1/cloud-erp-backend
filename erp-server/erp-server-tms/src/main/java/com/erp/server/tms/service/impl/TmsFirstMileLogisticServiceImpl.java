package com.erp.server.tms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.WriteTable;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.InvoicesStatusEnum;
import com.common.business.enums.OrderTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.ExportUtil;
import com.common.business.vo.PagingVO;
import com.common.core.constant.EnumMessage;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.StrUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.dto.excel.KingdeeBankAccountExcelDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.FmDeliveryLogisticsStatusEnum;
import com.erp.model.scm.dto.AttachmentDTO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.dto.excel.FmLogisticsBillCostExcelDTO;
import com.erp.model.tms.dto.excel.FmLogisticsBillExcelDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.*;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.dto.WmsCartonDetailDTO;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.model.wms.enums.PackingStatusEnum;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.server.tms.convert.FmLogisticsConverter;
import com.erp.server.tms.listener.FmLogisticsBillCostExcelListener;
import com.erp.server.tms.listener.FmLogisticsBillExcelListener;
import com.erp.server.tms.mapper.LogisticsBillMapper;
import com.erp.server.tms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.jfree.chart.util.ExportUtils;
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
//        if(Objects.isNull(addDTO.getLogisticsOrderTime())){
//            addDTO.setLogisticsOrderTime(LocalDateTime.now());
//        }
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


        //设置附件信息
        Class<LogisticsBillEntity> credentialClass = LogisticsBillEntity.class;
        TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
        String type = tableName.value();
        attachmentService.batchSave(addDTO.getAttachmentUrlList(), addDTO.getAttachmentNameList(), type, tmsFirstMileLogisticEntity.getId());

        //更新发货单物流状态
        FirstMileDeliveryDTO.UpdateStatusDTO updateDeliveryDto = new FirstMileDeliveryDTO.UpdateStatusDTO();
        updateDeliveryDto.setIds(Arrays.asList(addDTO.getOutstockId()));
        updateDeliveryDto.setLogisticsStatus(FmDeliveryLogisticsStatusEnum.FINISH.code);
        if(!wmsFirstMileDeliveryFeign.updateStatus(updateDeliveryDto)){
            throw new ServiceException("发货单更新物流状态失败");
        }

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

    private LogisticsBillCostDTO.UpdateDTO packCostUpdateDTO(FirstMileDeliveryDTO.GenerateLogisticDTO generateLogisticDTO, TmsFirstMileLogisticDTO.CommonDTO updateDTO,LogisticsBillCostEntity oldEntity) {
        //装箱信息
        List<WmsCartonDetailDTO.ListPackingDetailDTO> packingDTOList = generateLogisticDTO.getPackingDTOList();
        LogisticsBillCostDTO.UpdateDTO costUpdateDTO = new LogisticsBillCostDTO.UpdateDTO();
        if(CollectionUtils.isNotEmpty(packingDTOList)){
            //预估实重
            BigDecimal actualWeight = packingDTOList.stream()
                    .map(WmsCartonDetailDTO.ListPackingDetailDTO::getPackageWeight)
                    .map(BigDecimal::new)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            costUpdateDTO.setActualWeight(actualWeight);
            //设置预估体积重 = 长宽高/材积
            ShippingTemplateEntity shippingTemplateEntity = shippingTemplateService.getByChannelId(updateDTO.getLogisticsChannelId());
            if(Objects.nonNull(shippingTemplateEntity) && shippingTemplateEntity.getVolumeSetting() > 0){
                BigDecimal totalSize = packingDTOList.stream()
                        .map(WmsCartonDetailDTO.ListPackingDetailDTO::getMultiplySize)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                costUpdateDTO.setActualWeight(actualWeight);
                costUpdateDTO.setVolumeWeight(totalSize.divide(BigDecimal.valueOf(shippingTemplateEntity.getVolumeSetting()),4, RoundingMode.HALF_UP));
            }
        }
        costUpdateDTO.setCurrency(updateDTO.getCurrency());
        costUpdateDTO.setRemark(updateDTO.getRemark());
        costUpdateDTO.setVolumeWeightLogistics(updateDTO.getActualVolumeWeight());
        costUpdateDTO.setWeightLogistics(updateDTO.getActualWeight());
        costUpdateDTO.setId(oldEntity.getId());
        return costUpdateDTO;
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
            generateLogisticDTO = this.getGenerateLogisticDTO(outstockId);
            if(generateLogisticDTO == null){
                throw new ServiceException("发货单不存在或者未装箱或已生成物流单");
            }
        }
        LogisticsBillEntity updateFirstMileLogisticEntity = FmLogisticsConverter.INSTANCE.addLogisticsBill(generateLogisticDTO,updateDTO);
        BeanUtil.copyProperties(updateFirstMileLogisticEntity,old, CopyOptions.create().setIgnoreNullValue(true));
        boolean save = super.updateById(old);
        if(!save) {
            throw new ServiceException("头程物流单保存失败");
        }

        //更新物流单明细
        if(!updateDTO.getCounterNo().equals(oldCounterNo)){
            //删除旧的，新增新的
            logisticsBillDetailService.removeByMainIds(Collections.singletonList(old.getId()));
            List<LogisticsBillDetailDTO.AddDTO> detailAddList = new ArrayList<>();
            LogisticsBillDetailDTO.AddDTO detailAddDto = new LogisticsBillDetailDTO.AddDTO();
            detailAddDto.setMainId(old.getId());
            detailAddDto.setTrackNo(updateDTO.getCounterNo());
            detailAddDto.setTrackStatus(FmLogisticTrackStatusEnum.WAIT_ORDER.getCode());
            detailAddList.add(detailAddDto);
            logisticsBillDetailService.add(old,detailAddList,false);
        }
        List<LogisticsBillDetailEntity> detailEntityList = logisticsBillDetailService.listByMainIds(Collections.singletonList(old.getId()));
        LogisticsBillDetailEntity billDetailEntity = CollectionUtils.isEmpty(detailEntityList)?new LogisticsBillDetailEntity():detailEntityList.get(0);
        //更新物流单费用及明细
        LogisticsBillCostEntity logisticsBillCostEntity = logisticsBillCostService.getByLogisticsBillId(old.getId());
        Optional.ofNullable(logisticsBillCostEntity).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "头程物流费用单"));
        LogisticsBillCostDTO.UpdateDTO updateCostDTO = this.packCostUpdateDTO(generateLogisticDTO,updateDTO,logisticsBillCostEntity);
        updateCostDTO.setLogisticsBillDetailId(billDetailEntity.getId());
        updateCostDTO.setTrackNo(old.getCounterNo());
        List<TmsFirstMileLogisticDTO.LogisticFee> logisticFeeList = CollectionUtil.isEmpty(updateDTO.getLogisticFeeList())?new ArrayList<>():updateDTO.getLogisticFeeList();
        List<TmsLogisticsBillCostDetailDTO.UpdateDTO> costDetailList = new ArrayList<>();
        for (TmsFirstMileLogisticDTO.LogisticFee logisticFee : logisticFeeList) {
            TmsLogisticsBillCostDetailDTO.UpdateDTO dto = new TmsLogisticsBillCostDetailDTO.UpdateDTO();
            dto.setCostValue(logisticFee.getEstimatedFee());
            dto.setCfgCostId(logisticFee.getCfgCostId());
            dto.setType(LogisticsBillCostTypeEnum.ESTIMATED.getCode());
            costDetailList.add(dto);
        }
        updateCostDTO.setCostDetailList(costDetailList);
        logisticsBillCostService.update(updateCostDTO);

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
    public List<LogisticsBillEntity> listBySourceIds(List<String> sourceIds) {
        if (CollectionUtil.isEmpty(sourceIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(LogisticsBillEntity::getSourceId, sourceIds).list();
    }
    @Override
    public List<TmsFirstMileLogisticDTO.TabListDTO> tabList() {
        List<TmsFirstMileLogisticDTO.TabListDTO> tabList = baseMapper.firstMileTabList(OrderTypeEnum.FIRST_MILE.getCode());
        List<TmsFirstMileLogisticDTO.TabListDTO> result = new ArrayList<>();
        for(FmLogisticTrackStatusEnum logisticTrackStatusEnum : FmLogisticTrackStatusEnum.values()){
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
            if(pagingVO.getActualHour() != null){
                int days = pagingVO.getActualHour() / 24; // 计算天数部分
                int remainingHours = pagingVO.getActualHour() % 24; // 计算剩余小时数部分
                String actualDesc = (days !=0 ? days+ "天":"") + remainingHours + "小时";
                pagingVO.setActualDesc(actualDesc);
            }
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
                if(pagingVO.getActualHour() != null){
                    pagingVO.setWarnHour(estimatedDay*24 - pagingVO.getActualHour());
                }
            }
            pagingVO.setCompleteWeight(pagingVO.getWeight()+pagingVO.getWeightUnit());
            pagingVO.setCompleteVolumeWeight(pagingVO.getVolumeWeight()+pagingVO.getWeightUnit());
            pagingVO.setCompleteEstimatedFee(pagingVO.getCurrencySymbol()+(Objects.isNull(pagingVO.getEstimatedFee())?"":pagingVO.getEstimatedFee()));
            pagingVO.setCompleteActualFee(pagingVO.getCurrencySymbol()+(Objects.isNull(pagingVO.getActualFee())?"":pagingVO.getActualFee()));
            if(pagingVO.getWarnHour()!=null){
                if(pagingVO.getWarnHour()> 72){
                    pagingVO.setWarnMsg("时效正常");
                }else if(pagingVO.getWarnHour() < 0){
                    pagingVO.setWarnMsg(StrUtil.format("[]小时后超期",pagingVO.getWarnHour()));
                }else{
                    pagingVO.setWarnMsg(StrUtil.format("已超期[]小时",pagingVO.getWarnHour()));
                }
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
        if(Objects.nonNull(dto.getActualHour())){
            int days = dto.getActualHour() / 24; // 计算天数部分
            int remainingHours = dto.getActualHour() % 24; // 计算剩余小时数部分
            String actualDesc = (days !=0 ? days+ "天":"") + remainingHours + "小时";
            dto.setActualDesc(actualDesc);
        }

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
            List<TmsLogisticsBillCostDetailDTO.CostCompareDTO> costCompareDTOList = logisticsBillCostDetailService.getCostCompareListById(logisticsBillCostEntity.getId());
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
        TmsFirstMileLogisticDTO.StatisticsVO statisticsResult = new TmsFirstMileLogisticDTO.StatisticsVO();
        //发货统计
        TmsFirstMileLogisticDTO.StatisticsVO.DeliveryStatistics deliveryStatistics = new TmsFirstMileLogisticDTO.StatisticsVO.DeliveryStatistics();
        FirstMileDeliveryDTO.StatisticsReq deliveryStaticsReq = new FirstMileDeliveryDTO.StatisticsReq();
        deliveryStaticsReq.setStatus(ApproveStatusEnum.APPROVE.getStatus());
        deliveryStaticsReq.setBeginDate(DateUtil.getStartOfMonth(-1));
        deliveryStaticsReq.setEndDate(DateUtil.getEndOfMonth(0));
        List<FirstMileDeliveryDTO.LogisticStatisticsDTO> deliveryLogisticDTOList = wmsFirstMileDeliveryFeign.logisticStatistics(deliveryStaticsReq);
        deliveryStatistics.setLastMonthDelivery(!deliveryLogisticDTOList.isEmpty() ?deliveryLogisticDTOList.get(0).getCount():0);
        deliveryStatistics.setThisMonthDelivery(deliveryLogisticDTOList.size()>1?deliveryLogisticDTOList.get(1).getCount():0);
        TmsFirstMileLogisticDTO.LogisticStatisticsReq logisticStatisticsReq = TmsFirstMileLogisticDTO.LogisticStatisticsReq.builder()
                .beginOrderTime(DateUtil.getStartOfMonth(-1))
                .endOrderTime(DateUtil.getEndOfMonth(0))
                .logisticStatusList(FmLogisticTrackStatusEnum.getStatusNotWaitOrder())
                .orderType(OrderTypeEnum.FIRST_MILE.getCode())
                .build();
        List<TmsFirstMileLogisticDTO.LogisticStatisticsDTO> logisticStatisticsDTOList = baseMapper.statistics(logisticStatisticsReq);
        deliveryStatistics.setLastMonthOrder(!logisticStatisticsDTOList.isEmpty() ?logisticStatisticsDTOList.get(0).getCount():0);
        deliveryStatistics.setThisMonthOrder(logisticStatisticsDTOList.size()>1?logisticStatisticsDTOList.get(1).getCount():0);
        statisticsResult.setDeliveryStatistics(deliveryStatistics);
        //对账统计
        TmsFirstMileLogisticDTO.StatisticsVO.ReconciliationStatistics reconciliationStatistics = baseMapper.reconciliationStatistics(OrderTypeEnum.FIRST_MILE.getCode());
        statisticsResult.setReconciliationStatistics(reconciliationStatistics);
        //超期统计
        List<TmsFirstMileLogisticDTO.OverdueDTO> overdueList = baseMapper.overdueStatistics(OrderTypeEnum.FIRST_MILE.getCode());
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
        List<BatchResultDTO> batchResultDTOList = new ArrayList<>();
        List<LogisticsBillDetailEntity> allDetailList = logisticsBillDetailService.listByMainIds(dto.getIds());
        List<LogisticsBillDetailEntity> updateDetailList = new ArrayList<>();
        List<LogisticsTrackEntity> addTrackList = new ArrayList<>();
        List<LogisticsBillEntity> updateBillList = new ArrayList<>();
        for(LogisticsBillEntity logisticsBillEntity : logisticsBillEntityList){
            List<LogisticsBillDetailEntity> detailEntityList = allDetailList.stream().filter(v->v.getMainId().equals(logisticsBillEntity.getId())).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(detailEntityList)){
                batchResultDTOList.add(BatchResultDTO.fail(logisticsBillEntity.getId(),logisticsBillEntity.getOutstockCode(),"明细为空"));
                continue;
            }
            detailEntityList.forEach(v->{
                v.setTrackStatus(dto.getLogisticsStatus());
            });
            updateDetailList.addAll(detailEntityList);

            if(statusEnum != FmLogisticTrackStatusEnum.WAIT_ORDER){
                LogisticsTrackEntity logisticsTrackEntity = new LogisticsTrackEntity();
                logisticsTrackEntity.setStatus(dto.getLogisticsStatus());
                logisticsTrackEntity.setTrackNo(logisticsBillEntity.getCounterNo());
                logisticsTrackEntity.setTrackTime(dto.getTime());
                logisticsTrackEntity.setContent(dto.getLogisticsTrack());
                addTrackList.add(logisticsTrackEntity);
            }
            if(statusEnum == FmLogisticTrackStatusEnum.ORDERED){
                logisticsBillEntity.setOrderTime(dto.getTime());
                updateBillList.add(logisticsBillEntity);
            }

            if(statusEnum == FmLogisticTrackStatusEnum.WAIT_ORDER){
                logisticsBillEntity.setOrderTime(null);
                updateBillList.add(logisticsBillEntity);
            }
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
        operateLogService.batchAddModuleOperateLog(StrUtil.format("用户【{}】变更物流状态为【{}】",commonService.getUserInfo().getUserName(),statusEnum.getName()), ModuleTypeEnum.LOGISTICS_BILL.getCode(), addPairList, "编辑操作");

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
                resultDTOList.add(BatchResultDTO.fail(entity.getId(),entity.getCounterNo(),StrUtil.format("状态为【{}】，不可重新更新",statusEnum.getName())));
                continue;
            }
            entity.setInvoicesStatus(statusEnum.getCode());
            updateList.add(entity);
        }
        if(CollectionUtils.isNotEmpty(updateList)){
            this.updateBatchById(updateList);
            List<Pair<String, String>> addPairList = updateList.stream().map(obj -> new Pair<>(obj.getId(), obj.getId())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog(StrUtil.format("用户【{}】变更发票状态为【{}】",commonService.getUserInfo().getUserName(),statusEnum.getName()), ModuleTypeEnum.LOGISTICS_BILL.getCode(), addPairList, "编辑操作");
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
        LogisticsSupplierEntity supplierEntity = logisticsSupplierService.getById(dto.getLogisticsSupplierId());
        if(Objects.isNull(supplierEntity)){
            throw new ServiceException("物流供应商为空");
        }
        LogisticsChannelEntity logisticsChannelEntity = logisticsChannelService.getById(dto.getLogisticsChannelId());
        if(Objects.isNull(logisticsChannelEntity)){
            throw new ServiceException("物流渠道为空");
        }
        if(!logisticsChannelEntity.getMainId().equals(supplierEntity.getId())){
            throw new ServiceException("物流渠道与物流供应商不匹配");
        }
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
        for(LogisticsBillEntity logisticsBillEntity : logisticsBillEntityList){
            LogisticsBillDetailEntity detailEntity = detailList.stream().filter(v->v.getMainId().equals(logisticsBillEntity.getId())).findFirst().orElse(null);
            if(Objects.nonNull(detailEntity) && !(detailEntity.getTrackStatus().equals(FmLogisticTrackStatusEnum.WAIT_ORDER.getCode()) || detailEntity.getTrackStatus().equals(FmLogisticTrackStatusEnum.ORDERED.getCode()))){
                resultDTOList.add(BatchResultDTO.fail(logisticsBillEntity.getId(),logisticsBillEntity.getCounterNo(),"只有待下单和已下单状态支持更改物流信息"));
            }
            logisticsBillEntity.setShippingMethod(dto.getShippingMethod());
            logisticsBillEntity.setChannelId(dto.getLogisticsChannelId());
            logisticsBillEntity.setLogisticsSupplierId(dto.getLogisticsSupplierId());
            updateList.add(logisticsBillEntity);
            //更新体积重
            FirstMileDeliveryDTO.GenerateLogisticDTO deliveryLogisticDto = generateLogisticDTOList.stream().filter(v->v.getOutstockId().equals(logisticsBillEntity.getOutstockId())).findFirst().orElse(null);
            if(Objects.nonNull(deliveryLogisticDto) && CollectionUtils.isNotEmpty(deliveryLogisticDto.getPackingDTOList()) && Objects.nonNull(shippingTemplateEntity)
                    && shippingTemplateEntity.getVolumeSetting()!= null && shippingTemplateEntity.getVolumeSetting() > 0){
                LogisticsBillCostEntity logisticsBillCostEntity = costList.stream().filter(v->v.getLogisticsBillId().equals(logisticsBillEntity.getId())).findFirst().orElse(null);
                if(Objects.nonNull(logisticsBillCostEntity)){
                    BigDecimal totalSize = deliveryLogisticDto.getPackingDTOList().stream()
                            .map(WmsCartonDetailDTO.ListPackingDetailDTO::getMultiplySize)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    logisticsBillCostEntity.setVolumeWeight(totalSize.divide(BigDecimal.valueOf(shippingTemplateEntity.getVolumeSetting()),4, RoundingMode.HALF_UP));
                    updateCostList.add(logisticsBillCostEntity);
                }
            }
        }

        if(CollectionUtils.isNotEmpty(updateList)){
            this.updateBatchById(updateList);
        }
        if(CollectionUtils.isNotEmpty(updateCostList)){
            logisticsBillCostService.updateBatchById(updateCostList);
        }
        return resultDTOList;
    }

    @Override
    public List<BatchResultDTO> generateReconciliation(TmsFirstMileLogisticDTO.GenerateReconciliationDTO dto) {
        return null;
    }

    @Override
    public Boolean importExcel(MultipartFile excelFile, HttpServletResponse response) throws Exception{
        FmLogisticsBillExcelListener billListener = new FmLogisticsBillExcelListener();
        EasyExcel.read(excelFile.getInputStream(), FmLogisticsBillExcelDTO.class, billListener).sheet(0).doRead();
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
        List<TmsLogisticsBillCostDetailDTO.CostViewDTO> allCostDetailEntityList = logisticsBillCostDetailService.listCostByMainIdList(costIdList);
        if(CollectionUtils.isEmpty(allCostDetailEntityList)){
            return list;
        }
        List<TmsFirstMileLogisticDTO.ExportCostDTO> resultList = new ArrayList<>();
        for (TmsFirstMileLogisticDTO.ExportCostDTO exportCostDTO : list) {
            List<TmsLogisticsBillCostDetailDTO.CostViewDTO> costDetailEntityList = allCostDetailEntityList.stream().filter(v->v.getMainId().equals(exportCostDTO.getCostId()) && StringUtils.isNotBlank(v.getType())).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(costDetailEntityList)){
                resultList.add(exportCostDTO);
                continue;
            }
            Map<String,List<TmsLogisticsBillCostDetailDTO.CostViewDTO>> costViewMap = costDetailEntityList.stream().collect(Collectors.groupingBy(TmsLogisticsBillCostDetailDTO.CostViewDTO::getCostName));
            costViewMap.forEach((key,value)->{
                TmsFirstMileLogisticDTO.ExportCostDTO costDTO = BeanUtil.copyProperties(exportCostDTO,TmsFirstMileLogisticDTO.ExportCostDTO.class);
                costDTO.setCostName(key);
                String currencySymbol = CurrencyEnum.getSymbolByCode(costDTO.getCurrency());
                //预计费用
                TmsLogisticsBillCostDetailDTO.CostViewDTO estimateCost = value.stream().filter(v->v.getType().equals(LogisticsBillCostTypeEnum.ESTIMATED.getCode())).findFirst().orElse(new TmsLogisticsBillCostDetailDTO.CostViewDTO());
                if(Objects.nonNull(estimateCost.getCostValue())){
                    costDTO.setCompleteEstimatedFee(currencySymbol+estimateCost.getCostValue());
                }
                //实际费用
                TmsLogisticsBillCostDetailDTO.CostViewDTO actualCost = value.stream().filter(v->v.getType().equals(LogisticsBillCostTypeEnum.ACTUAL.getCode())).findFirst().orElse(new TmsLogisticsBillCostDetailDTO.CostViewDTO());
                if(Objects.nonNull(actualCost.getCostValue())){
                    costDTO.setCompleteActualFee(currencySymbol+actualCost.getCostValue());
                }
                resultList.add(costDTO);
            });
        }
        return resultList;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> delete(List<String> ids) {
        List<LogisticsBillEntity> logisticsBillEntityList = listByIds(ids);
        if(CollectionUtils.isEmpty(logisticsBillEntityList)){
            throw new ServiceException("物流单为空");
        }
        List<String> mainIdList = logisticsBillEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<LogisticsBillDetailEntity> detailList = logisticsBillDetailService.listByMainIds(mainIdList);
        List<LogisticsBillCostEntity> costList = logisticsBillCostService.listByLogisticsBillIdList(mainIdList);
        List<TmsLogisticsBillCostDetailEntity> costDetailList = logisticsBillCostDetailService.listByMainIdList(costList.stream().map(BaseEntity::getId).collect(Collectors.toList()));
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        List<String> mainIds = new ArrayList<>();
        List<String> detailIds = new ArrayList<>();
        List<String> costIds = new ArrayList<>();
        List<String> costDetailIds = new ArrayList<>();

        List<String> outstockIds = new ArrayList<>();

        for(LogisticsBillEntity logisticsBillEntity : logisticsBillEntityList){
            LogisticsBillDetailEntity detailEntity = detailList.stream().filter(v->v.getMainId().equals(logisticsBillEntity.getId())).findFirst().orElse(null);
            if(Objects.nonNull(detailEntity) && !detailEntity.getTrackStatus().equals(FmLogisticTrackStatusEnum.WAIT_ORDER.getCode())){
                resultDTOList.add(BatchResultDTO.fail(logisticsBillEntity.getId(),logisticsBillEntity.getCounterNo(),"只有待下单可以删除"));
                continue;
            }
            mainIds.add(logisticsBillEntity.getId());
            outstockIds.add(logisticsBillEntity.getOutstockId());
            if(Objects.nonNull(detailEntity)){
                detailIds.add(detailEntity.getId());
            }
            List<LogisticsBillCostEntity> costEntityList = costList.stream().filter(v->v.getLogisticsBillId().equals(logisticsBillEntity.getId())).collect(Collectors.toList());
            List<String> costIdList = costEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(costIdList)){
                costIds.addAll(costIdList);
                List<TmsLogisticsBillCostDetailEntity> costDetailEntityList = costDetailList.stream().filter(v->costIdList.contains(v.getMainId())).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(costDetailEntityList)){
                    costDetailIds.addAll(costDetailEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList()));
                }
            }
        }
        if(CollectionUtils.isNotEmpty(mainIds)){
            this.removeByIds(mainIds);
        }

        if(CollectionUtils.isNotEmpty(detailIds)){
            logisticsBillDetailService.removeByIds(detailIds);
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
        return resultDTOList;
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

    @Override
    public List<LogisticsBillEntity> listByOutstcockCode(List<String> outstockCodeList) {
        if (CollectionUtil.isEmpty(outstockCodeList)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(LogisticsBillEntity::getOutstockCode, outstockCodeList).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateImport(List<LogisticsBillEntity> updateList, List<LogisticsBillDetailEntity> updateDetailList, List<LogisticsTrackEntity> addTrackList) {
        if(CollectionUtils.isNotEmpty(updateList)){
            this.updateBatchById(updateList);
        }
        if(CollectionUtils.isNotEmpty(updateDetailList)){
            logisticsBillDetailService.updateBatchById(updateDetailList);
        }
        if(CollectionUtils.isNotEmpty(addTrackList)){
            logisticsTrackService.saveBatch(addTrackList);
        }
    }

    @Override
    public List<LogisticsBillEntity> listByTransportNo(List<String> transportNoList) {
        if (CollectionUtil.isEmpty(transportNoList)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(LogisticsBillEntity::getTransportNo, transportNoList).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateImportCost(List<LogisticsBillCostEntity> updateCostList, List<TmsLogisticsBillCostDetailEntity> updateCostDetailList) {
        if(CollectionUtils.isNotEmpty(updateCostList)){
            logisticsBillCostService.updateBatchById(updateCostList);
        }
        if(CollectionUtils.isNotEmpty(updateCostDetailList)){
            logisticsBillCostDetailService.updateBatchById(updateCostDetailList);
        }
    }

}
