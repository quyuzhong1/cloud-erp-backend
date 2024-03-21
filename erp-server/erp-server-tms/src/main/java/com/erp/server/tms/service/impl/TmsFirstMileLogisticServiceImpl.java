package com.erp.server.tms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.enums.FmDeliveryLogisticsStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.model.tms.dto.TmsFirstMileLogisticDTO;
import com.erp.model.tms.dto.TmsLogisticsBillCostDetailDTO;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.tms.entity.ShippingTemplateEntity;
import com.erp.model.tms.enums.LogisticTrackStatusEnum;
import com.erp.model.tms.enums.ReconciliationStatusEnum;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.dto.WmsCartonDetailDTO;
import com.erp.model.wms.enums.PackingStatusEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
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
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
        //查询汇率
        BigDecimal rate;
        if(addDTO.getCurrency().equals(CurrencyEnum.CNY.getCurrencyCode())){
            rate = BigDecimal.ONE;
        }else{
            rate = dmpTaskFeign.getRate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), addDTO.getCurrency());
            if(rate == null){
                throw new ServiceException("汇率为空，请维护汇率后再提交");
            }
        }

        //新增物流单
        LogisticsBillEntity tmsFirstMileLogisticEntity = FmLogisticsConverter.INSTANCE.addLogisticsBill(generateLogisticDTO,addDTO);
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
        LogisticsBillCostDTO.AddDTO costAddDTO = this.packCostAddDTO(generateLogisticDTO,addDTO,tmsFirstMileLogisticEntity,rate);
        BaseResultDTO.AddDTO costDTO = logisticsBillCostService.add(costAddDTO);

        //新增物流费用单明细
        List<TmsFirstMileLogisticDTO.LogisticFee> logisticFeeList = addDTO.getLogisticFeeList();
        for (TmsFirstMileLogisticDTO.LogisticFee logisticFee : logisticFeeList) {
            TmsLogisticsBillCostDetailDTO.AddDTO dto = new TmsLogisticsBillCostDetailDTO.AddDTO();
            dto.setCurrency(addDTO.getCurrency());
            dto.setExchangeRate(rate);
            dto.setCostValue(logisticFee.getEstimatedFee());
            dto.setCfgCostId(logisticFee.getCfgCostId());
            dto.setMainId(costDTO.getId());
            dto.setType("estimated");
            logisticsBillCostDetailService.add(dto);
        }

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

    private LogisticsBillCostDTO.AddDTO packCostAddDTO(FirstMileDeliveryDTO.GenerateLogisticDTO generateLogisticDTO, TmsFirstMileLogisticDTO.AddDTO addDTO,LogisticsBillEntity tmsFirstMileLogisticEntity,BigDecimal rate) {
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
        costAddDTO.setExchangeRate(rate);
        return costAddDTO;
    }

    private FirstMileDeliveryDTO.GenerateLogisticDTO getGenerateLogisticDTO(String outstockId){
        FirstMileDeliveryDTO.GenerateLogisticReqDTO dto = new FirstMileDeliveryDTO.GenerateLogisticReqDTO();
        dto.setId(outstockId);
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
        return null;
    }

    @Override
    public TmsFirstMileLogisticDTO.StatisticsVO statistics() {
        return null;
    }

    @Override
    public TmsFirstMileLogisticDTO.ViewDTO view(String id) {
        return null;
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
        reqDto.setId(dto.getOutstockId());
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
            v.setLogisticsStatusName(LogisticTrackStatusEnum.WAIT_ORDER.getName());
            v.setFromCountryName("中国");
        });
        return result;
    }

    @Override
    public Boolean updateRemark(TmsFirstMileLogisticDTO.UpdateRemarkDTO dto) {
        return null;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(LogisticsBillEntity tmsFirstMileLogisticEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
