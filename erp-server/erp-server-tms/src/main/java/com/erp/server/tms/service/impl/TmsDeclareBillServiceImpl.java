package com.erp.server.tms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.OrderTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.RedisUtil;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.constant.EnumMessage;
import com.common.core.dto.ExcelData;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.model.plm.entity.BasicDictEntity;
import com.erp.model.plm.enums.CombinationDeclareTypeEnums;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.tms.dto.AutoGenerateBillDTO;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.DictBasicDTO;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.*;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.dto.SoDeliveryNoticeDTO;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.model.wms.enums.PackingTaskStatusEnum;
import com.erp.model.wms.enums.WmsDeclareStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.SoDeliveryNoticeFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.server.tms.mapper.TmsDeclareBillMapper;
import com.erp.server.tms.service.*;
import com.erp.server.tms.utils.DeclarationGenerationService;
import com.google.common.collect.Lists;
import freemarker.template.utility.StringUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 报关单 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-03-27
 */
@Slf4j
@Service
public class TmsDeclareBillServiceImpl extends SuperServiceImpl<TmsDeclareBillMapper, TmsDeclareBillEntity> implements TmsDeclareBillService {
    @Resource
    private OperateLogService operateLogService;

    /**
     * 限制报关单最多sku
     */
    private final int limitSkuNo = 50;

    @Resource
    private WmsFirstMileDeliveryFeign wmsFirstMileDeliveryFeign;

    @Resource
    private SoOutstockFeign soOutstockFeign;

    @Resource
    private TmsFirstMileLogisticService fmLogisticService;

    @Resource
    private LogisticsBillService logisticService;

    @Resource
    private LogisticsSupplierService logisticsSupplierService;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private TmsDeclareBillDetailService detailService;

    @Resource
    @Lazy
    private TmsDeclareBillServiceImpl service;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysDictFeign sysDictFeign;

    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Autowired
    private SoDeliveryNoticeFeign soDeliveryNoticeFeign;

    @Resource
    private DeliveryDeclareDetailMidService deliveryDeclareDetailMidService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean addFmDeclare(TmsDeclareBillDTO.AddDTO addDTO) {
        TmsDeclareBillDTO.QuerySourceDTO querySourceDTO = TmsDeclareBillDTO.QuerySourceDTO.builder()
//                .packingStatus(PackingTaskStatusEnum.PACKED.getCode())
                .declareStatus(WmsDeclareStatusEnum.WAIT.getCode())
                .ids(Arrays.asList(addDTO.getSourceId()))
                .build();
        if (!Boolean.TRUE.equals(addDTO.getIsAuto())) {
            querySourceDTO.setPackingStatus(PackingTaskStatusEnum.PACKED.getCode());
        }
        List<TmsDeclareBillDTO.DeliveryDTO> deliveryDTOList = wmsFirstMileDeliveryFeign.getCanGenerateDeclare(querySourceDTO);
        if (CollectionUtils.isEmpty(deliveryDTOList)) {
            throw new ServiceException("没有可生成报关单的发货单");
        }
        TmsDeclareBillDTO.DeliveryDTO deliveryDTO = deliveryDTOList.get(0);

        // 新增页面下推保存逻辑：按前端提交的合并明细直接生成报关单
        if (CollUtil.isNotEmpty(addDTO.getMergeDetailList())) {
            List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> mergeDetailList = addDTO.getMergeDetailList().stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            Set<String> sourceDetailIdSet = mergeDetailList.stream()
                    .map(TmsDeclareBillDTO.MergeDeclareBillDetailDTO::getSourceDeliveryDetailList)
                    .filter(CollUtil::isNotEmpty)
                    .flatMap(Collection::stream)
                    .filter(Objects::nonNull)
                    .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getSourceDetailId)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toSet());
            if (CollUtil.isEmpty(sourceDetailIdSet)) {
                throw new ServiceException("未找到来源明细，无法保存报关单");
            }

            List<DeliveryDeclareDetailMidEntity> existsMidList = deliveryDeclareDetailMidService.lambdaQuery()
                    .eq(DeliveryDeclareDetailMidEntity::getSourceType, SourceTypeEnum.FM_DECLARE_BILL.getCode())
                    .in(DeliveryDeclareDetailMidEntity::getSourceDetailId, sourceDetailIdSet)
                    .list();
            List<DeliveryDeclareDetailMidEntity> generatedMidList = existsMidList.stream()
                    .filter(item -> StringUtils.isNotBlank(item.getDeclareId())
                            || DeliveryDeclareDetailMidGenerateStatusEnum.FINISH.getCode().equals(item.getGenerateStatus()))
                    .collect(Collectors.toList());
            if (CollUtil.isNotEmpty(generatedMidList)) {
                String repeatSourceCode = generatedMidList.stream()
                        .map(DeliveryDeclareDetailMidEntity::getSourceCode)
                        .filter(StringUtils::isNotBlank)
                        .distinct()
                        .collect(Collectors.joining("、"));
                throw new ServiceException(StringUtils.isBlank(repeatSourceCode)
                        ? "所选明细已生成报关单，请勿重复保存"
                        : CharSequenceUtil.format("来源单【{}】已生成报关单，请勿重复保存", repeatSourceCode));
            }

            TmsDeclareBillEntity declareBillEntity = new TmsDeclareBillEntity();
            BeanMapperUtils.copy(addDTO, declareBillEntity);
            BeanMapperUtils.copy(deliveryDTO, declareBillEntity);
            declareBillEntity.setDeclareStatus(com.erp.model.tms.enums.DeclareStatusEnum.WAIT.getCode());
            declareBillEntity.setType(SourceTypeEnum.FM_DECLARE_BILL.getCode());
            declareBillEntity.setDeclareDate(Objects.isNull(declareBillEntity.getDeclareDate()) ? LocalDate.now() : declareBillEntity.getDeclareDate());
            declareBillEntity.setShippingFee(Objects.isNull(declareBillEntity.getShippingFee()) ? BigDecimal.ZERO : declareBillEntity.getShippingFee());
            declareBillEntity.setInsuranceFee(Objects.isNull(declareBillEntity.getInsuranceFee()) ? BigDecimal.ZERO : declareBillEntity.getInsuranceFee());
            declareBillEntity.setOtherFee(Objects.isNull(declareBillEntity.getOtherFee()) ? BigDecimal.ZERO : declareBillEntity.getOtherFee());
            declareBillEntity.setGrossWeight(Objects.isNull(declareBillEntity.getGrossWeight()) ? BigDecimal.ZERO : declareBillEntity.getGrossWeight());
            declareBillEntity.setNetWeight(Objects.isNull(declareBillEntity.getNetWeight()) ? BigDecimal.ZERO : declareBillEntity.getNetWeight());

            List<TmsDeclareBillDetailEntity> detailEntityList = new ArrayList<>(mergeDetailList.size());
            for (TmsDeclareBillDTO.MergeDeclareBillDetailDTO detailDTO : mergeDetailList) {
                TmsDeclareBillDetailEntity detailEntity = new TmsDeclareBillDetailEntity();
                detailEntity.setSkuNo(detailDTO.getSkuNo());
                detailEntity.setCustomsCode(detailDTO.getHsCode());
                detailEntity.setDeclareChineseName(detailDTO.getProductNameCn());
                detailEntity.setDeclareElement(detailDTO.getDeclareElement());
                detailEntity.setDeclareUnit(detailDTO.getUnit());
                detailEntity.setPrice(Objects.isNull(detailDTO.getUnitPrice()) ? BigDecimal.ZERO : detailDTO.getUnitPrice());
                detailEntity.setQty(Objects.isNull(detailDTO.getQty()) ? 0 : detailDTO.getQty());
                detailEntity.setDeclareCurrency(detailDTO.getDeclareCurrency());
                detailEntity.setDeclareCurrencySymbol(detailDTO.getDeclareCurrencySymbol());
                detailEntityList.add(detailEntity);
            }
            if (CollUtil.isEmpty(detailEntityList)) {
                throw new ServiceException("请选择需要保存的报关明细");
            }
            Set<String> boxNoSet = mergeDetailList.stream()
                    .map(TmsDeclareBillDTO.MergeDeclareBillDetailDTO::getSourceDeliveryDetailList)
                    .filter(CollUtil::isNotEmpty)
                    .flatMap(Collection::stream)
                    .filter(Objects::nonNull)
                    .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getBoxNo)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toSet());
            declareBillEntity.setBoxQty(boxNoSet.size());
            BaseResultDTO.AddDTO addResult = service.add(declareBillEntity, detailEntityList, SourceTypeEnum.FM_DECLARE_BILL, false);

            List<DeliveryDeclareDetailMidEntity> addMidList = new ArrayList<>();
            for (int i = 0; i < mergeDetailList.size(); i++) {
                TmsDeclareBillDTO.MergeDeclareBillDetailDTO declareDetail = mergeDetailList.get(i);
                TmsDeclareBillDetailEntity billDetailEntity = detailEntityList.get(i);
                if (CollUtil.isEmpty(declareDetail.getSourceDeliveryDetailList())) {
                    continue;
                }
                for (TmsDeclareBillDTO.SourceDeliveryDetailDTO sourceDetail : declareDetail.getSourceDeliveryDetailList()) {
                    DeliveryDeclareDetailMidEntity midEntity = new DeliveryDeclareDetailMidEntity();
                    midEntity.setDeclareStatus(DeclareStatusEnum.WAIT.getCode());
                    midEntity.setGenerateStatus(DeliveryDeclareDetailMidGenerateStatusEnum.FINISH.getCode());
                    midEntity.setSourceType(SourceTypeEnum.FM_DECLARE_BILL.getCode());
                    midEntity.setSourceId(StringUtils.defaultIfBlank(sourceDetail.getSourceId(), addDTO.getSourceId()));
                    midEntity.setSourceCode(StringUtils.defaultString(sourceDetail.getSourceCode()));
                    midEntity.setSourceDetailId(StringUtils.defaultString(sourceDetail.getSourceDetailId()));
                    midEntity.setBusinessId(StringUtils.defaultString(sourceDetail.getBusinessId()));
                    midEntity.setBusinessCode(StringUtils.defaultString(sourceDetail.getBusinessCode()));
                    midEntity.setContractNo(addResult.getCode());
                    midEntity.setSkuId(StringUtils.defaultString(sourceDetail.getSkuId()));
                    midEntity.setSkuNo(StringUtils.defaultString(sourceDetail.getSkuNo()));
                    midEntity.setComboSkuNo(StringUtils.defaultString(sourceDetail.getComboSkuNo()));
                    midEntity.setCurrency(StringUtils.defaultString(declareDetail.getDeclareCurrency()));
                    midEntity.setCurrencySymbol(StringUtils.defaultString(declareDetail.getDeclareCurrencySymbol()));
                    midEntity.setDeclareId(addResult.getId());
                    midEntity.setDeclareCode(addResult.getCode());
                    midEntity.setDeclareDetailId(billDetailEntity.getId());
                    midEntity.setBoxNo(StringUtils.defaultString(sourceDetail.getBoxNo()));
                    midEntity.setHsCode(StringUtils.defaultString(declareDetail.getHsCode()));
                    midEntity.setProductNameCn(StringUtils.defaultString(declareDetail.getProductNameCn()));
                    midEntity.setDeclareElement(StringUtils.defaultString(declareDetail.getDeclareElement()));
                    midEntity.setUnit(StringUtils.defaultString(declareDetail.getUnit()));
                    midEntity.setUnitPrice(Objects.isNull(declareDetail.getUnitPrice()) ? BigDecimal.ZERO : declareDetail.getUnitPrice());
                    midEntity.setQty(Objects.isNull(sourceDetail.getQty()) ? 0 : sourceDetail.getQty());
                    addMidList.add(midEntity);
                }
            }
            if (CollUtil.isNotEmpty(addMidList)) {
                deliveryDeclareDetailMidService.saveBatch(addMidList);
            }
            updateSourceDeclareStatus(SourceTypeEnum.FM_DECLARE_BILL.getCode(), addMidList);
            return Boolean.TRUE;
        }

        TmsDeclareBillEntity baseTmsDeclareBillEntity = new TmsDeclareBillEntity();
        BeanMapperUtils.copy(addDTO, baseTmsDeclareBillEntity);
        BeanMapperUtils.copy(deliveryDTO, baseTmsDeclareBillEntity);
        baseTmsDeclareBillEntity.setDeclareStatus(com.erp.model.tms.enums.DeclareStatusEnum.WAIT.getCode());
        baseTmsDeclareBillEntity.setType(SourceTypeEnum.FM_DECLARE_BILL.getCode());
        //50个明细为一个报关单
        List<TmsDeclareBillDTO.ProductDetail> allProductDetailList = deliveryDTO.getProductDetailList();
        if(CollectionUtils.isEmpty(allProductDetailList)){
            throw new ServiceException("没有可生成报关单的明细");
        }
        List<List<TmsDeclareBillDTO.ProductDetail>> productDetailListList = Lists.partition(allProductDetailList, limitSkuNo);
        for(List<TmsDeclareBillDTO.ProductDetail> productDetailList : productDetailListList){
            TmsDeclareBillEntity tmsDeclareBillEntity = BeanUtil.copyProperties(baseTmsDeclareBillEntity,TmsDeclareBillEntity.class);
            List<TmsDeclareBillDetailEntity> detailEntityList = BeanUtil.copyToList(productDetailList,TmsDeclareBillDetailEntity.class);
            tmsDeclareBillEntity.setNetWeight(productDetailList.stream().filter(v->Objects.nonNull(v.getNetWeight())).map(v->v.getNetWeight().multiply(new BigDecimal(v.getQty())).divide(new BigDecimal(1000),4, RoundingMode.HALF_UP)).reduce(BigDecimal.ZERO, BigDecimal::add));
            service.add(tmsDeclareBillEntity,detailEntityList,SourceTypeEnum.FIRST_MILE_DELIVERY,false);
        }
        //更新发货单的报关状态
        if (!Boolean.TRUE.equals(addDTO.getIsAuto())) {
            FirstMileDeliveryDTO.UpdateStatusDTO dto = new FirstMileDeliveryDTO.UpdateStatusDTO();
            dto.setIds(Arrays.asList(addDTO.getSourceId()));
            dto.setDeclareStatus(WmsDeclareStatusEnum.FINISH.getCode());
            wmsFirstMileDeliveryFeign.updateStatus(dto);
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public BaseResultDTO.AddDTO add(TmsDeclareBillEntity tmsDeclareBillEntity,List<TmsDeclareBillDetailEntity> detailEntityList,SourceTypeEnum sourceTypeEnum,boolean isMerged) {

        //合并的话不生成合同号
        if(!isMerged){
            //生成合同号
            String code = this.generateContractCode(sourceTypeEnum);
            tmsDeclareBillEntity.setCode(code);
        }

        log.info("开始新增报关单");
        boolean save = super.save(tmsDeclareBillEntity);
        if(!save) {
            throw new ServiceException("报关单保存失败");
        }
        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】【{}】【{}】合同号为【{}】", UserContext.getDefaultLoginUser().getUserName(),isMerged?"合并":"新增", "报关单" , tmsDeclareBillEntity.getCode());
        operateLogService.addModuleOperateLog(msg, sourceTypeEnum.getCode(), tmsDeclareBillEntity.getId(), "新增操作");
        detailService.add(tmsDeclareBillEntity,detailEntityList);
        return new BaseResultDTO.AddDTO(tmsDeclareBillEntity.getId(), tmsDeclareBillEntity.getCode());
    }

    private String generateContractCode(SourceTypeEnum sourceTypeEnum) {
        String key = CharSequenceUtil.format(RedisCacheConstants.TMS_DECLARE_CODE,sourceTypeEnum.getCode(), DateUtil.currentYMD());
        Object value = redisUtil.get(key);
        int number;
        if(value == null) {
            number = 1;
        }else{
            number = (Integer) value;
            number++;
        }
        redisUtil.set(key,number,86400);
        return CharSequenceUtil.format("{}{}{}", "HT", DateUtil.currentYMD(), StringUtil.leftPad(String.valueOf(number), 3, "0"));
    }
    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Override
    public Boolean update(TmsDeclareBillDTO.UpdateDTO updateDTO,SourceTypeEnum sourceTypeEnum) {
        TmsDeclareBillEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "报关单"));
        if (!CharSequenceUtil.equals(old.getType(), sourceTypeEnum.getCode())) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_BILL_TYPE_MISMATCH);
        }
        if(!old.getDeclareStatus().equals(com.erp.model.tms.enums.DeclareStatusEnum.WAIT.getCode())){
            throw new ServiceException("报关单状态不是待确认，不能编辑");
        }
        List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> mergeDetailList = updateDTO.getMergeDetailList().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(mergeDetailList)) {
            throw new ServiceException("请选择需要保存的报关明细");
        }
        Set<String> sourceDetailIdSet = mergeDetailList.stream()
                .map(TmsDeclareBillDTO.MergeDeclareBillDetailDTO::getSourceDeliveryDetailList)
                .filter(CollUtil::isNotEmpty)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getSourceDetailId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        if (CollUtil.isEmpty(sourceDetailIdSet)) {
            throw new ServiceException("未找到来源明细，无法保存报关单");
        }
        List<DeliveryDeclareDetailMidEntity> existsMidList = deliveryDeclareDetailMidService.lambdaQuery()
                .in(DeliveryDeclareDetailMidEntity::getSourceDetailId, sourceDetailIdSet)
                .ne(DeliveryDeclareDetailMidEntity::getDeclareId, old.getId())
                .list();
        List<DeliveryDeclareDetailMidEntity> generatedMidList = existsMidList.stream()
                .filter(item -> StringUtils.isNotBlank(item.getDeclareId())
                        || DeliveryDeclareDetailMidGenerateStatusEnum.FINISH.getCode().equals(item.getGenerateStatus()))
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(generatedMidList)) {
            String repeatSourceCode = generatedMidList.stream()
                    .map(DeliveryDeclareDetailMidEntity::getSourceCode)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.joining("、"));
            throw new ServiceException(StringUtils.isBlank(repeatSourceCode)
                    ? "所选明细已生成报关单，请勿重复保存"
                    : CharSequenceUtil.format("来源单【{}】已生成报关单，请勿重复保存", repeatSourceCode));
        }
        if(Objects.isNull(updateDTO.getShippingFee())){
            updateDTO.setShippingFee(BigDecimal.ZERO);
        }
        if(Objects.isNull(updateDTO.getInsuranceFee())){
            updateDTO.setInsuranceFee(BigDecimal.ZERO);
        }
        if(Objects.isNull(updateDTO.getOtherFee())){
            updateDTO.setOtherFee(BigDecimal.ZERO);
        }
        TmsDeclareBillEntity tmsDeclareBillEntity =  BeanMapperUtils.map(TmsDeclareBillEntity.class, updateDTO);
        Set<String> boxNoSet = mergeDetailList.stream()
                .map(TmsDeclareBillDTO.MergeDeclareBillDetailDTO::getSourceDeliveryDetailList)
                .filter(CollUtil::isNotEmpty)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getBoxNo)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        tmsDeclareBillEntity.setBoxQty(boxNoSet.size());
        tmsDeclareBillEntity.setCode(old.getCode());
        boolean save = super.updateById(tmsDeclareBillEntity);
        if(!save) {
            throw new ServiceException("报关单保存失败");
        }
        detailService.deleteDetailByMainIdList(Collections.singletonList(old.getId()));
        List<TmsDeclareBillDetailEntity> detailEntityList = new ArrayList<>(mergeDetailList.size());
        for (TmsDeclareBillDTO.MergeDeclareBillDetailDTO detailDTO : mergeDetailList) {
            TmsDeclareBillDetailEntity detailEntity = new TmsDeclareBillDetailEntity();
            detailEntity.setMainId(old.getId());
            detailEntity.setSkuNo(detailDTO.getSkuNo());
            detailEntity.setCustomsCode(detailDTO.getHsCode());
            detailEntity.setDeclareChineseName(detailDTO.getProductNameCn());
            detailEntity.setDeclareElement(detailDTO.getDeclareElement());
            detailEntity.setDeclareUnit(detailDTO.getUnit());
            detailEntity.setPrice(Objects.isNull(detailDTO.getUnitPrice()) ? BigDecimal.ZERO : detailDTO.getUnitPrice());
            detailEntity.setQty(Objects.isNull(detailDTO.getQty()) ? 0 : detailDTO.getQty());
            detailEntity.setDeclareCurrency(detailDTO.getDeclareCurrency());
            detailEntity.setDeclareCurrencySymbol(detailDTO.getDeclareCurrencySymbol());
            detailEntityList.add(detailEntity);
        }
        if (CollUtil.isEmpty(detailEntityList)) {
            throw new ServiceException("请选择需要保存的报关明细");
        }
        if (!detailService.saveBatch(detailEntityList)) {
            throw new ServiceException("报关单明细保存失败");
        }
        deliveryDeclareDetailMidService.lambdaUpdate()
                .eq(DeliveryDeclareDetailMidEntity::getDeclareId, old.getId())
                .remove();
        List<DeliveryDeclareDetailMidEntity> addMidList = new ArrayList<>();
        for (int i = 0; i < mergeDetailList.size(); i++) {
            TmsDeclareBillDTO.MergeDeclareBillDetailDTO declareDetail = mergeDetailList.get(i);
            TmsDeclareBillDetailEntity billDetailEntity = detailEntityList.get(i);
            if (CollUtil.isEmpty(declareDetail.getSourceDeliveryDetailList())) {
                continue;
            }
            for (TmsDeclareBillDTO.SourceDeliveryDetailDTO sourceDetail : declareDetail.getSourceDeliveryDetailList()) {
                DeliveryDeclareDetailMidEntity midEntity = new DeliveryDeclareDetailMidEntity();
                midEntity.setDeclareStatus(DeclareStatusEnum.WAIT.getCode());
                midEntity.setGenerateStatus(DeliveryDeclareDetailMidGenerateStatusEnum.FINISH.getCode());
                midEntity.setSourceType(old.getType());
                midEntity.setSourceId(StringUtils.defaultString(sourceDetail.getSourceId()));
                midEntity.setSourceCode(StringUtils.defaultString(sourceDetail.getSourceCode()));
                midEntity.setSourceDetailId(StringUtils.defaultString(sourceDetail.getSourceDetailId()));
                midEntity.setBusinessId(StringUtils.defaultString(sourceDetail.getBusinessId()));
                midEntity.setBusinessCode(StringUtils.defaultString(sourceDetail.getBusinessCode()));
                midEntity.setContractNo(old.getCode());
                midEntity.setSkuId(StringUtils.defaultString(sourceDetail.getSkuId()));
                midEntity.setSkuNo(StringUtils.defaultString(sourceDetail.getSkuNo()));
                midEntity.setComboSkuNo(StringUtils.defaultString(sourceDetail.getComboSkuNo()));
                midEntity.setCurrency(StringUtils.defaultString(declareDetail.getDeclareCurrency()));
                midEntity.setCurrencySymbol(StringUtils.defaultString(declareDetail.getDeclareCurrencySymbol()));
                midEntity.setDeclareId(old.getId());
                midEntity.setDeclareCode(old.getCode());
                midEntity.setDeclareDetailId(billDetailEntity.getId());
                midEntity.setBoxNo(StringUtils.defaultString(sourceDetail.getBoxNo()));
                midEntity.setHsCode(StringUtils.defaultString(declareDetail.getHsCode()));
                midEntity.setProductNameCn(StringUtils.defaultString(declareDetail.getProductNameCn()));
                midEntity.setDeclareElement(StringUtils.defaultString(declareDetail.getDeclareElement()));
                midEntity.setUnit(StringUtils.defaultString(declareDetail.getUnit()));
                midEntity.setUnitPrice(Objects.isNull(declareDetail.getUnitPrice()) ? BigDecimal.ZERO : declareDetail.getUnitPrice());
                midEntity.setQty(Objects.isNull(sourceDetail.getQty()) ? 0 : sourceDetail.getQty());
                addMidList.add(midEntity);
            }
        }
        if (CollUtil.isNotEmpty(addMidList)) {
            deliveryDeclareDetailMidService.saveBatch(addMidList);
        }
        updateSourceDeclareStatus(old.getType(),addMidList);
        log.info("编辑 开始记录报关单日志数据，单号：【{}】", tmsDeclareBillEntity.getCode());
        String msg = CharSequenceUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), tmsDeclareBillEntity.getCode(), "报关单");
        operateLogService.addModuleOperateLogByObj(old, tmsDeclareBillEntity, sourceTypeEnum.getCode(), tmsDeclareBillEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<TmsDeclareBillDTO.TabListDTO> tabList(SourceTypeEnum sourceTypeEnum, PermissionsDTO permissionsDTO) {
        List<TmsDeclareBillDTO.TabListDTO> tabList = baseMapper.tabList(sourceTypeEnum.getCode(),permissionsDTO.getPermissionSql());
        List<TmsDeclareBillDTO.TabListDTO> result = new ArrayList<>();
        for(com.erp.model.tms.enums.DeclareStatusEnum statusEnum : com.erp.model.tms.enums.DeclareStatusEnum.values()){
            TmsDeclareBillDTO.TabListDTO tabListDTO = new TmsDeclareBillDTO.TabListDTO();
            tabListDTO.setTabFlag(statusEnum.getCode());
            tabListDTO.setTabFlagName(statusEnum.getName());
            TmsDeclareBillDTO.TabListDTO queryResult = tabList.stream().filter(v->v.getTabFlag().equals(tabListDTO.getTabFlag())).findFirst().orElse(new TmsDeclareBillDTO.TabListDTO());
            tabListDTO.setCount(queryResult.getCount());
            result.add(tabListDTO);
        }
        return result;
    }

    @Override
    public PagingVO<TmsDeclareBillDTO.PagingVO> paging(PagingDTO<TmsDeclareBillDTO.PagingParamDTO> dto) {
        TmsDeclareBillDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<TmsDeclareBillDTO.PagingVO> pageData = new Page<>();
        if (CharSequenceUtil.equals(SourceTypeEnum.B2B_DECLARE_BILL.getCode(),params.getType())) {
            //B2B报关单查询
             pageData = baseMapper.b2bDeclarePaging(query, params);
        } else {
            //头程报关单查询
             pageData = baseMapper.firstMilePaging(query, params);
        }
        List<TmsDeclareBillDTO.PagingVO> list = pageData.getRecords();
        fillPagingDb(list,params.getType());
        return new PagingVO<>(pageData);
    }

    @Override
    public TmsDeclareBillDTO.StatisticsVO statisticsByFm(PermissionsDTO permissionsDTO) {
        TmsDeclareBillDTO.StatisticsVO statisticsVO = new TmsDeclareBillDTO.StatisticsVO();
        List<TmsDeclareBillDTO.StatisticsAllDTO> statisticsAllDTOList = this.baseMapper.statistics(TmsDeclareBillDTO.StatisticsDTO.builder()
                        .beginDate(DateUtil.getStartOfMonth(-1))
                        .endDate(DateUtil.getEndOfMonth(0))
                        .declareStatus(com.erp.model.tms.enums.DeclareStatusEnum.DECLARED.getCode())
                        .type(SourceTypeEnum.FM_DECLARE_BILL.getCode())
                .build(),permissionsDTO.getPermissionSql());
        FirstMileDeliveryDTO.StatisticsReq deliveryStaticsReq = new FirstMileDeliveryDTO.StatisticsReq();
        deliveryStaticsReq.setStatus(ApproveStatusEnum.APPROVE.getStatus());
        deliveryStaticsReq.setBeginDate(DateUtil.getStartOfMonth(-1));
        deliveryStaticsReq.setEndDate(DateUtil.getEndOfMonth(0));
        List<FirstMileDeliveryDTO.LogisticStatisticsDTO> deliveryLogisticDTOList;
        try {
            deliveryLogisticDTOList = wmsFirstMileDeliveryFeign.logisticStatistics(deliveryStaticsReq);
        }catch (ServiceException e){
            deliveryLogisticDTOList = new ArrayList<>();
        }
        statisticsVO.setLastMonthDelivery(deliveryLogisticDTOList.stream().filter(v->v.getMonth().equals(LocalDate.now().minusMonths(1).getMonthValue())).findFirst().orElse(new FirstMileDeliveryDTO.LogisticStatisticsDTO()).getCount());
        statisticsVO.setThisMonthDelivery(deliveryLogisticDTOList.stream().filter(v->v.getMonth().equals(LocalDate.now().getMonthValue())).findFirst().orElse(new FirstMileDeliveryDTO.LogisticStatisticsDTO()).getCount());

        statisticsVO.setLastMonthDeclare(statisticsAllDTOList.stream().filter(v->v.getMonth().equals(LocalDate.now().minusMonths(1).getMonthValue())).findFirst().orElse(new TmsDeclareBillDTO.StatisticsAllDTO()).getCount());
        statisticsVO.setThisMonthDeclare(statisticsAllDTOList.stream().filter(v->v.getMonth().equals(LocalDate.now().getMonthValue())).findFirst().orElse(new TmsDeclareBillDTO.StatisticsAllDTO()).getCount());
        return statisticsVO;
    }

    private void fillPagingDb(List<TmsDeclareBillDTO.PagingVO> list,String type) {
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        List<DictBasicDTO.ViewDTO> declareTypeDict = dictBasicService.getByKey(DictBasicEnum.DECLARE_DECLARE_TYPE.getType());
        List<String> sourceCodes = list.stream()
                .map(TmsDeclareBillDTO.PagingVO::getSourceCode)
                .filter(StringUtils::isNotBlank)
                .flatMap(code -> Arrays.stream(code.split(",")))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());

        //处理供应商
        List<LogisticsBillEntity> logisticsList =  fmLogisticService.listByOutstcockCode(sourceCodes);
        List<String> supplierIds = logisticsList.stream().map(LogisticsBillEntity::getLogisticsSupplierId).distinct().collect(Collectors.toList());
        List<LogisticsSupplierEntity> supplierList = CollectionUtils.isNotEmpty(supplierIds)?logisticsSupplierService.listByIds(supplierIds):new ArrayList<>();

        if(type.equals(SourceTypeEnum.FM_DECLARE_BILL.getCode())){
            list.forEach(v->{
                //供应商
                LogisticsBillEntity logistics = logisticsList.stream().filter(e->e.getOutstockCode().equals(v.getSourceCode())).findFirst().orElse(null);
                if(logistics!=null){
                    LogisticsSupplierEntity supplier = supplierList.stream().filter(e->e.getId().equals(logistics.getLogisticsSupplierId())).findFirst().orElse(new LogisticsSupplierEntity());
                    v.setLogisticsSupplierId(supplier.getId());
                    v.setLogisticsSupplierName(supplier.getSupplierName());
                }
                //发货类型
                v.setBusinessTypeName(FmDeclareSourceTypeEnum.getName(v.getBusinessType()));
            });
        }else if (type.equals(SourceTypeEnum.B2B_DECLARE_BILL.getCode())){
            list.forEach(v->{
                //供应商
                LogisticsBillEntity logistics = logisticsList.stream().filter(e->e.getOutstockCode().equals(v.getSourceCode())).findFirst().orElse(null);
                if(logistics!=null){
                    LogisticsSupplierEntity supplier = supplierList.stream().filter(e->e.getId().equals(logistics.getLogisticsSupplierId())).findFirst().orElse(new LogisticsSupplierEntity());
                    v.setLogisticsSupplierId(supplier.getId());
                    v.setLogisticsSupplierName(supplier.getSupplierName());
                }
                //发货类型
                v.setBusinessTypeName(OrderTypeEnum.getName(v.getBusinessType()));
            });
        }

        list.forEach(v->{
            v.setDeclareStatusName(EnumMessage.getNameByCode(com.erp.model.tms.enums.DeclareStatusEnum.class,v.getDeclareStatus()));
            DictBasicDTO.ViewDTO declareType = declareTypeDict.stream().filter(e->e.getCode().equals(v.getDeclareType())).findFirst().orElse(new DictBasicDTO.ViewDTO());
            v.setDeclareTypeName(declareType.getName());
        });
    }

    @Override
    public List<TmsDeclareBillDTO.DeliveryDTO> getCanGenerateDeliveryOrder(TmsDeclareBillDTO.QuerySourceDTO querySourceDTO) {
        List<TmsDeclareBillDTO.DeliveryDTO> deliveryDTOList = wmsFirstMileDeliveryFeign.getCanGenerateDeclare(querySourceDTO);
        List<String> sourceCodes = deliveryDTOList.stream().map(TmsDeclareBillDTO.DeliveryDTO::getSourceCode).collect(Collectors.toList());
        List<LogisticsBillEntity> logisticsBillEntityList = fmLogisticService.listByOutstcockCode(sourceCodes);
        List<String> supplierIds = logisticsBillEntityList.stream().map(LogisticsBillEntity::getLogisticsSupplierId).distinct().collect(Collectors.toList());
        List<LogisticsSupplierEntity> logisticsSupplierEntityList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(supplierIds)){
            logisticsSupplierEntityList = logisticsSupplierService.listByIds(supplierIds);
        }
        for (TmsDeclareBillDTO.DeliveryDTO deliveryDTO : deliveryDTOList) {
            LogisticsBillEntity logisticsBillEntity = logisticsBillEntityList.stream().filter(v->v.getOutstockId().equals(deliveryDTO.getSourceId())).findFirst().orElse(new LogisticsBillEntity());
            LogisticsSupplierEntity logisticsSupplierEntity = logisticsSupplierEntityList.stream().filter(v->v.getId().equals(logisticsBillEntity.getLogisticsSupplierId())).findFirst().orElse(new LogisticsSupplierEntity());
            deliveryDTO.setShippingMethod(logisticsBillEntity.getShippingMethod());
            deliveryDTO.setShippingMethodName(LogisticsMethodEnum.getName(logisticsBillEntity.getShippingMethod()));
            deliveryDTO.setLogisticsSupplierId(logisticsBillEntity.getLogisticsSupplierId());
            deliveryDTO.setLogisticsSupplierName(logisticsSupplierEntity.getSupplierName());
            deliveryDTO.setCounterNo(logisticsBillEntity.getCounterNo());
        }
        return deliveryDTOList;
    }

    @Override
    public TmsDeclareBillDTO.ViewDTO view(String id) {
        TmsDeclareBillEntity entity = this.getById(id);
        Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, "报关单"));
        TmsDeclareBillDTO.ViewDTO viewDTO = BeanUtil.copyProperties(entity,TmsDeclareBillDTO.ViewDTO.class);
        //根据报关单id查询来源信息
        TmsDeclareBillDTO.ListBillSourceDTO listBillSourceDTO = listSourceByDeclareBillId(id);

        List<TmsDeclareBillDetailEntity> detailEntityList = detailService.listByMainIds(Arrays.asList(entity.getId()));

        List<DeliveryDeclareDetailMidEntity> midEntityList = deliveryDeclareDetailMidService.listByDeclareBillIdList(Collections.singletonList(id));
        Map<String, List<DeliveryDeclareDetailMidEntity>> midGroupMap = CollUtil.isEmpty(midEntityList)
                ? new HashMap<>()
                : midEntityList.stream()
                .filter(item -> StringUtils.isNotBlank(item.getDeclareDetailId()))
                .collect(Collectors.groupingBy(DeliveryDeclareDetailMidEntity::getDeclareDetailId));
        List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> mergeDetailList = detailEntityList.stream().map(detailEntity -> {
            TmsDeclareBillDTO.MergeDeclareBillDetailDTO mergeDetailDTO = new TmsDeclareBillDTO.MergeDeclareBillDetailDTO();
            mergeDetailDTO.setId(detailEntity.getId());
            mergeDetailDTO.setSkuNo(detailEntity.getSkuNo());
            mergeDetailDTO.setHsCode(detailEntity.getCustomsCode());
            mergeDetailDTO.setProductNameCn(detailEntity.getDeclareChineseName());
            mergeDetailDTO.setDeclareElement(detailEntity.getDeclareElement());
            mergeDetailDTO.setUnit(detailEntity.getDeclareUnit());
            mergeDetailDTO.setUnitPrice(Objects.isNull(detailEntity.getPrice()) ? BigDecimal.ZERO : detailEntity.getPrice());
            mergeDetailDTO.setQty(Objects.isNull(detailEntity.getQty()) ? 0 : detailEntity.getQty());
            mergeDetailDTO.setDeclareCurrency(detailEntity.getDeclareCurrency());
            mergeDetailDTO.setDeclareCurrencySymbol(detailEntity.getDeclareCurrencySymbol());
            List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailList = midGroupMap
                    .getOrDefault(detailEntity.getId(), Collections.emptyList())
                    .stream()
                    .map(this::buildSourceDeliveryDetailDTO)
                    .collect(Collectors.toList());
            mergeDetailDTO.setSourceDeliveryDetailList(sourceDetailList);
            return mergeDetailDTO;
        }).collect(Collectors.toList());
        viewDTO.setMergeDetailList(mergeDetailList);
        if(entity.getType().equals(SourceTypeEnum.FM_DECLARE_BILL.getCode())){
            List<TmsDeclareBillDTO.DeliveryDTO> deliveryDTOList = this.getCanGenerateDeliveryOrder(TmsDeclareBillDTO.QuerySourceDTO.builder().ids(listBillSourceDTO.getSourceIdList()).build());
            if(CollectionUtils.isEmpty(deliveryDTOList)){
                throw new ServiceException("未找到发货单信息");
            }
            List<TmsDeclareBillDTO.PackingDTO> allPackDTOList = deliveryDTOList.stream()
                    .filter(v -> CollectionUtils.isNotEmpty(v.getPackingDTOList()))
                    .flatMap(v -> v.getPackingDTOList().stream())
                    .collect(Collectors.toList());
            if(deliveryDTOList.size()>1){
                deliveryDTOList = deliveryDTOList.stream().filter(v->listBillSourceDTO.getSourceCodeList().contains(v.getSourceCode())).collect(Collectors.toList());
            }
            allPackDTOList.forEach(v->v.setSku(v.getBoxDesc()));
            viewDTO.setPackingDTOList(allPackDTOList);

            //物流供应商ids
            String logisticsSupplierIds = deliveryDTOList.stream().map(TmsDeclareBillDTO.DeliveryDTO::getLogisticsSupplierId).distinct().collect(Collectors.joining(";"));
            viewDTO.setLogisticsSupplierId(logisticsSupplierIds);
            //物流供应商名称
            String logisticsSupplierNames = deliveryDTOList.stream().map(TmsDeclareBillDTO.DeliveryDTO::getLogisticsSupplierId).distinct().collect(Collectors.joining(";"));
            viewDTO.setLogisticsSupplierName(logisticsSupplierNames);
        }else if(entity.getType().equals(SourceTypeEnum.B2B_DECLARE_BILL.getCode())){
            List<TmsDeclareBillDTO.SoOutDTO> deliveryDTOList = this.getCanGenerateSoOut(TmsDeclareBillDTO.QuerySourceDTO.builder().ids(listBillSourceDTO.getSourceIdList()).build());
            if(CollectionUtils.isEmpty(deliveryDTOList)){
                throw new ServiceException("未找到销售出库单信息");
            }
            List<TmsDeclareBillDTO.PackingDTO> allPackDTOList = deliveryDTOList.stream()
                    .filter(v -> CollectionUtils.isNotEmpty(v.getPackingDTOList()))
                    .flatMap(v -> v.getPackingDTOList().stream())
                    .collect(Collectors.toList());
            if(deliveryDTOList.size()>1){
                deliveryDTOList = deliveryDTOList.stream().filter(v-> listBillSourceDTO.getSourceCodeList().contains(v.getSourceCode())).collect(Collectors.toList());
            }
            allPackDTOList.forEach(v->v.setSku(v.getBoxDesc()));
            viewDTO.setPackingDTOList(allPackDTOList);

            //物流供应商ids
            String logisticsSupplierIds = deliveryDTOList.stream().map(TmsDeclareBillDTO.SoOutDTO::getLogisticsSupplierId).distinct().collect(Collectors.joining(";"));
            viewDTO.setLogisticsSupplierId(logisticsSupplierIds);
            //物流供应商名称
            String logisticsSupplierNames = deliveryDTOList.stream().map(TmsDeclareBillDTO.SoOutDTO::getLogisticsSupplierId).distinct().collect(Collectors.joining(";"));
            viewDTO.setLogisticsSupplierName(logisticsSupplierNames);
        }

        fillViewDTO(viewDTO);
        return viewDTO;
    }

    /**
     * 根据报关单id查询来源信息
     * @author will
     * @date 2026/4/20 18:33
     * @param declareBillId
     * @return com.erp.model.tms.dto.TmsDeclareBillDTO.ListBillSourceDTO
     */
    private TmsDeclareBillDTO.ListBillSourceDTO listSourceByDeclareBillId(String declareBillId) {
        List<TmsDeclareBillDTO.BillSourceDTO> billSourceDTOList = baseMapper.listSourceByDeclareBillId(declareBillId);
        if (CollUtil.isEmpty(billSourceDTOList)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_BILL_EXISTS_NOT_SOURCE);
        }
        List<String> sourceIdList = billSourceDTOList.stream().map(TmsDeclareBillDTO.BillSourceDTO::getSourceId).distinct().collect(Collectors.toList());
        List<String> sourceCodeList = billSourceDTOList.stream().map(TmsDeclareBillDTO.BillSourceDTO::getSourceCode).distinct().collect(Collectors.toList());
        List<String> businessIdList = billSourceDTOList.stream().map(TmsDeclareBillDTO.BillSourceDTO::getBusinessId).distinct().collect(Collectors.toList());
        List<String> businessCodeList = billSourceDTOList.stream().map(TmsDeclareBillDTO.BillSourceDTO::getBusinessCode).distinct().collect(Collectors.toList());
        return TmsDeclareBillDTO.ListBillSourceDTO.builder()
                .sourceIdList(sourceIdList)
                .sourceCodeList(sourceCodeList)
                .businessIdList(businessIdList)
                .businessCodeList(businessCodeList)
                .build();
    }

    private void fillViewDTO(TmsDeclareBillDTO.ViewDTO viewDTO) {
        //处理字典值
        List<DictBasicEntity> dictBasicEntityList = dictBasicService.getByKeyList(Arrays.asList(DictBasicEnum.DECLARE_DECLARE_TYPE.getType(),
                DictBasicEnum.DECLARE_SUPERVISION_METHOD.getType(),
                DictBasicEnum.DECLARE_NATURE_LEVY.getType(),
                DictBasicEnum.DECLARE_PACK_TYPE.getType(),
                DictBasicEnum.DECLARE_TRANSACTION_METHOD.getType()));
        viewDTO.setDeclareTypeName(dictBasicEntityList.stream().filter(v->v.getType().equals(DictBasicEnum.DECLARE_DECLARE_TYPE.getType())&&v.getCode().equals(viewDTO.getDeclareType())).map(DictBasicEntity::getName).findFirst().orElse(""));
        viewDTO.setDictSupervisionMethodName(dictBasicEntityList.stream().filter(v->v.getType().equals(DictBasicEnum.DECLARE_SUPERVISION_METHOD.getType())&&v.getCode().equals(viewDTO.getDictSupervisionMethod())).map(DictBasicEntity::getName).findFirst().orElse(""));
        viewDTO.setDictNatureLevyName(dictBasicEntityList.stream().filter(v->v.getType().equals(DictBasicEnum.DECLARE_NATURE_LEVY.getType())&&v.getCode().equals(viewDTO.getDictNatureLevy())).map(DictBasicEntity::getName).findFirst().orElse(""));
        viewDTO.setDictPackTypeName(dictBasicEntityList.stream().filter(v->v.getType().equals(DictBasicEnum.DECLARE_PACK_TYPE.getType())&&v.getCode().equals(viewDTO.getDictPackType())).map(DictBasicEntity::getName).findFirst().orElse(""));
        viewDTO.setDictTransactionMethodName(dictBasicEntityList.stream().filter(v->v.getType().equals(DictBasicEnum.DECLARE_TRANSACTION_METHOD.getType())&&v.getCode().equals(viewDTO.getDictTransactionMethod())).map(DictBasicEntity::getName).findFirst().orElse(""));

        List<DictCountryEntity> sourceCountryList = sysDictFeign.listCountryByIds(Arrays.asList(viewDTO.getToArea(),viewDTO.getToPort()));
        Map<String,String> sourceCountryMap = sourceCountryList.stream().collect(Collectors.toMap(DictCountryEntity::getId,DictCountryEntity::getNameCn,(v1,v2)->v1));
        viewDTO.setToArea(sourceCountryMap.containsKey(viewDTO.getToArea())?sourceCountryMap.get(viewDTO.getToArea()): viewDTO.getToArea());
        viewDTO.setToPort(sourceCountryMap.containsKey(viewDTO.getToPort())?sourceCountryMap.get(viewDTO.getToPort()): viewDTO.getToPort());
        //处理发货人
        if(StringUtils.isNotBlank(viewDTO.getSenderId())){
            SysAccountingCompanyEntity sysAccountingCompanyEntity = sysUserFeign.getCompanyById(viewDTO.getSenderId());
            if(Objects.nonNull(sysAccountingCompanyEntity)){
                viewDTO.setSenderName(sysAccountingCompanyEntity.getCompanyName());
            }
        }
    }

    @Override
    public TmsDeclareBillDTO.DeclareStatusDetailDTO declareStatusDetail(String id, SourceTypeEnum sourceTypeEnum) {
        TmsDeclareBillEntity entity = getDeclareBillByIdAndType(id, sourceTypeEnum);
        TmsDeclareBillDTO.DeclareStatusDetailDTO detailDTO = new TmsDeclareBillDTO.DeclareStatusDetailDTO();
        detailDTO.setId(entity.getId());
        detailDTO.setDeclareStatus(entity.getDeclareStatus());
        detailDTO.setDeclareStatusName(DeclareStatusEnum.getName(entity.getDeclareStatus()));
        if (Objects.nonNull(entity.getDeclarConfirmDate())) {
            detailDTO.setDeclarConfirmDate(entity.getDeclarConfirmDate());
        }
        detailDTO.setDeclarUserId(entity.getDeclarUserId());
        detailDTO.setDeclarUserName(entity.getDeclarUserName());
        if (DeclareStatusEnum.WAIT.getCode().equals(entity.getDeclareStatus())) {
            LoginUser loginUser = UserContext.getDefaultLoginUser();
            detailDTO.setDeclarConfirmDate(LocalDate.now());
            detailDTO.setDeclarUserId(loginUser.getUid());
            detailDTO.setDeclarUserName(loginUser.getUserName());
        }
        return detailDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean confirmDeclareStatus(TmsDeclareBillDTO.ConfirmDeclareStatusDTO dto, SourceTypeEnum sourceTypeEnum) {
        TmsDeclareBillEntity entity = getDeclareBillByIdAndType(dto.getId(), sourceTypeEnum);
        DeclareStatusEnum targetStatus = DeclareStatusEnum.getEnum(dto.getDeclareStatus());
        String currentStatus = entity.getDeclareStatus();
        if (DeclareStatusEnum.WAIT.getCode().equals(currentStatus) && DeclareStatusEnum.CONFIRMED.equals(targetStatus)) {
            validateDeclareConfirm(entity);
            fillDeclareConfirmUser(dto);
            updateDeclareStatus(entity, targetStatus.getCode(), dto.getDeclarConfirmDate(), dto.getDeclarUserId(), dto.getDeclarUserName());
            return Boolean.TRUE;
        }
        if (DeclareStatusEnum.CONFIRMED.getCode().equals(currentStatus) && DeclareStatusEnum.WAIT.equals(targetStatus)) {
            updateDeclareStatus(entity, targetStatus.getCode(), null, null, null);
            return Boolean.TRUE;
        }
        if (DeclareStatusEnum.CONFIRMED.getCode().equals(currentStatus) && DeclareStatusEnum.DECLARED.equals(targetStatus)) {
            updateDeclareStatus(entity, targetStatus.getCode(), entity.getDeclarConfirmDate(), entity.getDeclarUserId(), entity.getDeclarUserName());
            return Boolean.TRUE;
        }
        if (DeclareStatusEnum.DECLARED.getCode().equals(currentStatus) && DeclareStatusEnum.WAIT.equals(targetStatus)) {
            updateDeclareStatus(entity, targetStatus.getCode(), null, null, null);
            return Boolean.TRUE;
        }
        throw new ServiceException(ApiError.LOGISTICS_DECLARE_STATUS_UPDATE_FORBIDDEN, DeclareStatusEnum.getName(currentStatus), targetStatus.getName());
    }

    private TmsDeclareBillEntity getDeclareBillByIdAndType(String id, SourceTypeEnum sourceTypeEnum) {
        TmsDeclareBillEntity entity = this.getById(id);
        Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, "报关单"));
        if (!sourceTypeEnum.getCode().equals(entity.getType())) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_BILL_TYPE_MISMATCH);
        }
        return entity;
    }
    private void validateDeclareConfirm(TmsDeclareBillEntity entity) {
        if (StringUtils.isBlank(entity.getDeclareType())) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_STATUS_DETAIL_REQUIRED, "报关类型");
        }
        List<TmsDeclareBillDetailEntity> detailEntityList = detailService.listByMainIds(Collections.singletonList(entity.getId()));
        if (CollectionUtils.isEmpty(detailEntityList)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_STATUS_DETAIL_REQUIRED, "产品明细");
        }
        validateDeclareDetailField(detailEntityList, TmsDeclareBillDetailEntity::getDeclareCurrency, "币制");
        validateDeclareDetailField(detailEntityList, TmsDeclareBillDetailEntity::getSourceCountry, "原产国(地区)");
        validateDeclareDetailField(detailEntityList, TmsDeclareBillDetailEntity::getToCountry, "最终目的国(地区)");
        validateDeclareDetailField(detailEntityList, TmsDeclareBillDetailEntity::getSourceCargo, "境内货源地");
        validateDeclareDetailField(detailEntityList, TmsDeclareBillDetailEntity::getExemption, "征免");
    }

    private void validateDeclareDetailField(List<TmsDeclareBillDetailEntity> detailEntityList, Function<TmsDeclareBillDetailEntity, String> getter, String fieldName) {
        for (TmsDeclareBillDetailEntity detailEntity : detailEntityList) {
            if (StringUtils.isBlank(getter.apply(detailEntity))) {
                throw new ServiceException(ApiError.LOGISTICS_DECLARE_STATUS_DETAIL_REQUIRED, fieldName);
            }
        }
        Set<String> valueSet = detailEntityList.stream().map(getter).collect(Collectors.toSet());
        if (valueSet.size() > 1) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_STATUS_DETAIL_INCONSISTENT, fieldName);
        }
    }

    private void fillDeclareConfirmUser(TmsDeclareBillDTO.ConfirmDeclareStatusDTO dto) {
        if (Objects.isNull(dto.getDeclarConfirmDate())) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_STATUS_CONFIRM_DATE_REQUIRED);
        }
        LoginUser loginUser = UserContext.getDefaultLoginUser();
        if (StringUtils.isBlank(dto.getDeclarUserId())) {
            dto.setDeclarUserId(loginUser.getUid());
        }
        if (StringUtils.isBlank(dto.getDeclarUserName()) && dto.getDeclarUserId().equals(loginUser.getUid())) {
            dto.setDeclarUserName(loginUser.getUserName());
        }
        if (StringUtils.isBlank(dto.getDeclarUserName())) {
            FindUserDTO userDTO = sysUserFeign.getUserByUserId(dto.getDeclarUserId());
            if (Objects.nonNull(userDTO)) {
                dto.setDeclarUserName(userDTO.getUserName());
            }
        }
        if (StringUtils.isBlank(dto.getDeclarUserName())) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_STATUS_CONFIRM_USER_REQUIRED);
        }
    }

    private void updateDeclareStatus(TmsDeclareBillEntity entity, String declareStatus, LocalDate declarConfirmDate, String declarUserId, String declarUserName) {
        this.lambdaUpdate()
                .eq(TmsDeclareBillEntity::getId, entity.getId())
                .set(TmsDeclareBillEntity::getDeclareStatus, declareStatus)
                .set(TmsDeclareBillEntity::getDeclarConfirmDate, Objects.isNull(declarConfirmDate) ? LocalDate.now() : declarConfirmDate)
                .set(TmsDeclareBillEntity::getDeclarUserId, Objects.isNull(declarUserId) ? "" : declarUserId)
                .set(TmsDeclareBillEntity::getDeclarUserName, Objects.isNull(declarUserName) ? "" : declarUserName)
                .update(new TmsDeclareBillEntity());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean mergeDeclare(TmsDeclareBillDTO.MergeDeclareDTO dto) {
        if(StringUtils.isBlank(dto.getCode())){
            throw new ServiceException("合同协议号不能为空");
        }
        List<TmsDeclareBillEntity> entityList = this.listByIds(dto.getIds());
        if(CollectionUtils.isEmpty(entityList)){
            throw new ServiceException("没有需要合并的报关单");
        }
        // 校验合并条件
        if(entityList.stream().anyMatch(v->!v.getDeclareStatus().equals(com.erp.model.tms.enums.DeclareStatusEnum.WAIT.getCode()))){
            throw new ServiceException("仅支持待确认的报关单合并");
        }
        TmsDeclareBillEntity mergedEntity = entityList.stream().filter(v->v.getCode().equals(dto.getCode())).findFirst().orElse(null);
        if(Objects.isNull(mergedEntity)){
            throw new ServiceException("选择的报关单中没有该表头");
        }
        if(entityList.stream().anyMatch(v->StringUtils.isBlank(v.getCountryName())) ||
        entityList.stream().map(TmsDeclareBillEntity::getCountryName).collect(Collectors.toSet()).size() > 1){
            throw new ServiceException("不同国家报关单不能合并");
        }

        List<String> outOutCodeList = new ArrayList<>();
        List<LogisticsBillEntity> logisticsBillEntityList = fmLogisticService.listByOutstcockCode(outOutCodeList);
        logisticsBillEntityList = logisticsBillEntityList.stream().filter(v->StringUtils.isNotBlank(v.getLogisticsSupplierId())).collect(Collectors.toList());
        if(logisticsBillEntityList.size()!= entityList.size()
                || logisticsBillEntityList.stream().map(LogisticsBillEntity::getLogisticsSupplierId).collect(Collectors.toSet()).size() > 1){
            throw new ServiceException("不同物流商的报关单不能合并");
        }
        List<String> ids = entityList.stream().map(TmsDeclareBillEntity::getId).collect(Collectors.toList());
        List<TmsDeclareBillDetailEntity> detailList = detailService.listByMainIds(ids);

        //合并明细相同sku
        // 根据 skuId 进行分组，并对数量进行求和
        List<TmsDeclareBillDetailEntity> mergedDetails = new ArrayList<>(detailList.stream()
                .collect(Collectors.toMap(
                        TmsDeclareBillDetailEntity::getSkuId,
                        Function.identity(),
                        (existing, replacement) -> {
                            // 合并数量
                            existing.setQty(existing.getQty() + replacement.getQty());
                            // 其他字段取第一个出现的值
                            return existing;
                        }
                ))
                .values());
        if(mergedDetails.size()>limitSkuNo){
            throw new ServiceException(CharSequenceUtil.format("合并后SKU数量超过限制，最多合并{}个SKU",limitSkuNo));
        }
        mergedEntity.setNetWeight(entityList.stream().map(TmsDeclareBillEntity::getNetWeight).reduce(BigDecimal.ZERO,BigDecimal::add));
        mergedEntity.setGrossWeight(entityList.stream().map(TmsDeclareBillEntity::getGrossWeight).reduce(BigDecimal.ZERO,BigDecimal::add));
        mergedEntity.setShippingFee(entityList.stream().map(TmsDeclareBillEntity::getShippingFee).reduce(BigDecimal.ZERO,BigDecimal::add));
        mergedEntity.setInsuranceFee(entityList.stream().map(TmsDeclareBillEntity::getInsuranceFee).reduce(BigDecimal.ZERO,BigDecimal::add));
        mergedEntity.setOtherFee(entityList.stream().map(TmsDeclareBillEntity::getOtherFee).reduce(BigDecimal.ZERO,BigDecimal::add));
        mergedEntity.setBoxQty(entityList.stream().mapToInt(TmsDeclareBillEntity::getBoxQty).sum());
        //保存合并后的数据
        mergedEntity.setId(null);
        mergedDetails.forEach(v-> v.setId(null));
        this.add(mergedEntity,mergedDetails,SourceTypeEnum.FM_DECLARE_BILL,true);
        //更新原数据为作废
        if(!this.updateToInvalid(ids)){
            throw new ServiceException("更新原数据为作废失败");
        }
        return true;
    }

    public boolean updateToInvalid(List<String> ids) {
        if(CollectionUtils.isEmpty(ids)){
            return false;
        }
        return this.lambdaUpdate().in(TmsDeclareBillEntity::getId,ids)
                .set(TmsDeclareBillEntity::getDeclareStatus, com.erp.model.tms.enums.DeclareStatusEnum.INVALID.getCode())
                .update();
    }


    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public List<BatchResultDTO> delete(TmsDeclareBillDTO.DeleteDTO dto) {
        List<TmsDeclareBillEntity> entityList = this.listByIds(dto.getIds());
        List<BatchResultDTO> resultList = new ArrayList<>();
        List<String> removeIds = new ArrayList<>();
        List<String> updateFhdSourceIds = new ArrayList<>();
        List<String> updateOutSourceIds = new ArrayList<>();
        for (TmsDeclareBillEntity entity : entityList) {
            if(!entity.getDeclareStatus().equals(com.erp.model.tms.enums.DeclareStatusEnum.WAIT.getCode())){
                resultList.add(BatchResultDTO.fail(entity.getId(),entity.getCode(),"只有待确认的单据才能删除"));
                continue;
            }
            resultList.add(BatchResultDTO.success(entity.getId(),entity.getCode(),"删除成功"));
            removeIds.add(entity.getId());
        }
        if(CollectionUtils.isNotEmpty(removeIds)){
            this.removeByIds(removeIds);
        }
        if(CollectionUtils.isNotEmpty(updateFhdSourceIds)){
            updateFhdSourceIds = updateFhdSourceIds.stream().distinct().collect(Collectors.toList());
            FirstMileDeliveryDTO.UpdateStatusDTO updateStatusDTO = new FirstMileDeliveryDTO.UpdateStatusDTO();
            updateStatusDTO.setIds(updateFhdSourceIds);
            updateStatusDTO.setDeclareStatus(WmsDeclareStatusEnum.WAIT.code);
            wmsFirstMileDeliveryFeign.updateStatus(updateStatusDTO);
        }
        if(CollectionUtils.isNotEmpty(updateOutSourceIds)){
            updateOutSourceIds = updateOutSourceIds.stream().distinct().collect(Collectors.toList());
            SoDeliveryNoticeDTO.DeclareStatusDTO updateStatusDTO = new SoDeliveryNoticeDTO.DeclareStatusDTO();
            updateStatusDTO.setIds(updateOutSourceIds);
            updateStatusDTO.setDeclareStatus(WmsDeclareStatusEnum.WAIT.code);
            soDeliveryNoticeFeign.updateDeclareStatus(updateStatusDTO);
        }
        return resultList;
    }

    private void fillExport(List<TmsDeclareBillDTO.ExportDTO> list) {
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        List<String> ids = list.stream().map(TmsDeclareBillDTO.ExportDTO::getId).collect(Collectors.toList());
        List<TmsDeclareBillDetailEntity> detailList = detailService.listByMainIds(ids);
        List<TmsDeclareBillDTO.ExportProductDetail> allExportProductDetailList = BeanUtil.copyToList(detailList,TmsDeclareBillDTO.ExportProductDetail.class);
        List<String> sourceCodeList = list.stream().map(TmsDeclareBillDTO.ExportDTO::getSourceCode).distinct().collect(Collectors.toList());
        List<LogisticsBillEntity> logisticsBillEntityList = fmLogisticService.listByOutstcockCode(sourceCodeList);
        List<String> orgIdList = list.stream().map(TmsDeclareBillDTO.ExportDTO::getSenderId).distinct().collect(Collectors.toList());
        List<SysAccountingCompanyEntity> allAccountingCompanyEntityList = sysUserFeign.listCompanyById(orgIdList);
        List<DictBasicEntity> dictBasicEntityList = dictBasicService.getByKeyList(Arrays.asList(DictBasicEnum.DECLARE_DECLARE_TYPE.getType(),
                DictBasicEnum.DECLARE_SUPERVISION_METHOD.getType(),
                DictBasicEnum.DECLARE_NATURE_LEVY.getType(),
                DictBasicEnum.DECLARE_PACK_TYPE.getType(),
                DictBasicEnum.DECLARE_TRANSACTION_METHOD.getType()));
        List<BasicDictEntity> sysDictBasicEntityList = plmTaskFeign.listDictByType("declareUnit");
        List<String> sourceCountryIdList = allExportProductDetailList.stream().map(TmsDeclareBillDTO.ExportProductDetail::getSourceCountry).collect(Collectors.toList());
        List<DictCountryEntity> sourceCountryList = sysDictFeign.listCountryByIds(sourceCountryIdList);
        Map<String,String> sourceCountryMap = sourceCountryList.stream().collect(Collectors.toMap(DictCountryEntity::getId,DictCountryEntity::getNameCn,(v1,v2)->v1));

        for (TmsDeclareBillDTO.ExportDTO exportDTO : list) {
            SysAccountingCompanyEntity accountingCompanyEntity = allAccountingCompanyEntityList.stream().filter(v->v.getId().equals(exportDTO.getSenderId())).findFirst().orElse(new SysAccountingCompanyEntity());
            exportDTO.setSenderName(accountingCompanyEntity.getCompanyName());

            LogisticsBillEntity logisticsBillEntity = logisticsBillEntityList.stream().filter(v->v.getOutstockCode().equals(exportDTO.getSourceCode())).findFirst().orElse(new LogisticsBillEntity());
            exportDTO.setShippingMethodName(LogisticsMethodEnum.getName(logisticsBillEntity.getShippingMethod()));
            if(exportDTO.getType().equals(SourceTypeEnum.FM_DECLARE_BILL.getCode())){
                exportDTO.setTransportNo(logisticsBillEntity.getCounterNo());
            }

            exportDTO.setDictSupervisionMethodName(dictBasicEntityList.stream().filter(v->v.getType().equals(DictBasicEnum.DECLARE_SUPERVISION_METHOD.getType())&&v.getCode().equals(exportDTO.getDictSupervisionMethod())).map(DictBasicEntity::getName).findFirst().orElse(""));
            exportDTO.setDictNatureLevyName(dictBasicEntityList.stream().filter(v->v.getType().equals(DictBasicEnum.DECLARE_NATURE_LEVY.getType())&&v.getCode().equals(exportDTO.getDictNatureLevy())).map(DictBasicEntity::getName).findFirst().orElse(""));
            exportDTO.setDictPackTypeName(dictBasicEntityList.stream().filter(v->v.getType().equals(DictBasicEnum.DECLARE_PACK_TYPE.getType())&&v.getCode().equals(exportDTO.getDictPackType())).map(DictBasicEntity::getName).findFirst().orElse(""));
            exportDTO.setDictTransactionMethodName(dictBasicEntityList.stream().filter(v->v.getType().equals(DictBasicEnum.DECLARE_TRANSACTION_METHOD.getType())&&v.getCode().equals(exportDTO.getDictTransactionMethod())).map(DictBasicEntity::getName).findFirst().orElse(""));

            List<TmsDeclareBillDTO.ExportProductDetail> exportProductDetailList = allExportProductDetailList.stream().filter(v->v.getMainId().equals(exportDTO.getId())).collect(Collectors.toList());
            for (int i = 0; i < exportProductDetailList.size(); i++) {
                TmsDeclareBillDTO.ExportProductDetail detail = exportProductDetailList.get(i);
                detail.setRowNum(i+1);
                detail.setTotalPrice(detail.getPrice().multiply(new BigDecimal(detail.getQty())));
                detail.setDeclareCurrencyName(CurrencyEnum.getNameByCode(detail.getDeclareCurrency()));
                BasicDictEntity unitDTO = sysDictBasicEntityList.stream().filter(v->v.getValue().equals(detail.getDeclareUnit())).findFirst().orElse(new BasicDictEntity());
                detail.setDeclareUnitName(unitDTO.getName());
                detail.setSourceCountryName(sourceCountryMap.get(detail.getSourceCountry()));
                detail.setToCountryName(exportDTO.getCountryName());
            }
            exportDTO.setTotalQty(exportProductDetailList.stream().mapToInt(TmsDeclareBillDTO.ExportProductDetail::getQty).sum());
            exportDTO.setTotalPrice(exportProductDetailList.stream().map(TmsDeclareBillDTO.ExportProductDetail::getTotalPrice).reduce(BigDecimal.ZERO,BigDecimal::add));
            exportDTO.setProductDetailList(exportProductDetailList);
        }
    }

    @Override
    public List<TmsDeclareBillEntity> listBySourceIds(List<String> sourceIds) {
        if (CollectionUtil.isEmpty(sourceIds)) {
            return Collections.emptyList();
        }

        return  new ArrayList<>();//lambdaQuery().in(TmsDeclareBillEntity::getSourceId, sourceIds).list();//TODO
    }

    @Override
    public List<TmsDeclareBillDTO.SoOutDTO> getCanGenerateSoOut(TmsDeclareBillDTO.QuerySourceDTO querySourceDTO) {
        List<TmsDeclareBillDTO.SoOutDTO> deliveryDTOList = soDeliveryNoticeFeign.listPackingDetailByIdList(querySourceDTO);

        //销售出库单编码
        List<String> soOutstockCodes = deliveryDTOList.stream().map(TmsDeclareBillDTO.SoOutDTO::getSoOutstockCode).collect(Collectors.toList());
        List<LogisticsBillEntity> logisticsBillEntityList = logisticService.listByOutstockCodeList(soOutstockCodes);
        List<String> supplierIds = logisticsBillEntityList.stream().map(LogisticsBillEntity::getLogisticsSupplierId).distinct().collect(Collectors.toList());
        List<LogisticsSupplierEntity> logisticsSupplierEntityList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(supplierIds)){
            logisticsSupplierEntityList = logisticsSupplierService.listByIds(supplierIds);
        }
        for (TmsDeclareBillDTO.SoOutDTO deliveryDTO : deliveryDTOList) {
            LogisticsBillEntity logisticsBillEntity = logisticsBillEntityList.stream().filter(v->v.getOutstockId().equals(deliveryDTO.getSourceId())).findFirst().orElse(new LogisticsBillEntity());
            LogisticsSupplierEntity logisticsSupplierEntity = logisticsSupplierEntityList.stream().filter(v->v.getId().equals(logisticsBillEntity.getLogisticsSupplierId())).findFirst().orElse(new LogisticsSupplierEntity());
            deliveryDTO.setShippingMethod(logisticsBillEntity.getShippingMethod());
            deliveryDTO.setShippingMethodName(LogisticsMethodEnum.getName(logisticsBillEntity.getShippingMethod()));
            deliveryDTO.setLogisticsSupplierId(logisticsBillEntity.getLogisticsSupplierId());
            deliveryDTO.setLogisticsSupplierName(logisticsSupplierEntity.getSupplierName());
        }
        return deliveryDTOList;
    }

    @Override
    public TmsDeclareBillDTO.StatisticsVO statisticsBySoOut(PermissionsDTO permissionsDTO) {
        TmsDeclareBillDTO.StatisticsVO statisticsVO = new TmsDeclareBillDTO.StatisticsVO();
        List<TmsDeclareBillDTO.StatisticsAllDTO> statisticsAllDTOList = this.baseMapper.statistics(TmsDeclareBillDTO.StatisticsDTO.builder()
                .beginDate(DateUtil.getStartOfMonth(-1))
                .endDate(DateUtil.getEndOfMonth(0))
                .declareStatus(com.erp.model.tms.enums.DeclareStatusEnum.DECLARED.getCode())
                .type(SourceTypeEnum.B2B_DECLARE_BILL.getCode())
                .build(),permissionsDTO.getPermissionSql());
        FirstMileDeliveryDTO.StatisticsReq deliveryStaticsReq = new FirstMileDeliveryDTO.StatisticsReq();
        deliveryStaticsReq.setStatus(ApproveStatusEnum.APPROVE.getStatus());
        deliveryStaticsReq.setBeginDate(DateUtil.getStartOfMonth(-1));
        deliveryStaticsReq.setEndDate(DateUtil.getEndOfMonth(0));
        deliveryStaticsReq.setOrderType(OrderTypeEnum.B2B.getCode());
        List<FirstMileDeliveryDTO.LogisticStatisticsDTO> deliveryLogisticDTOList;
        try {
            deliveryLogisticDTOList = soOutstockFeign.logisticStatistics(deliveryStaticsReq);
        }catch (ServiceException e){
            deliveryLogisticDTOList = new ArrayList<>();
        }
        statisticsVO.setLastMonthDelivery(deliveryLogisticDTOList.stream().filter(v->v.getMonth().equals(LocalDate.now().minusMonths(1).getMonthValue())).findFirst().orElse(new FirstMileDeliveryDTO.LogisticStatisticsDTO()).getCount());
        statisticsVO.setThisMonthDelivery(deliveryLogisticDTOList.stream().filter(v->v.getMonth().equals(LocalDate.now().getMonthValue())).findFirst().orElse(new FirstMileDeliveryDTO.LogisticStatisticsDTO()).getCount());

        statisticsVO.setLastMonthDeclare(statisticsAllDTOList.stream().filter(v->v.getMonth().equals(LocalDate.now().minusMonths(1).getMonthValue())).findFirst().orElse(new TmsDeclareBillDTO.StatisticsAllDTO()).getCount());
        statisticsVO.setThisMonthDeclare(statisticsAllDTOList.stream().filter(v->v.getMonth().equals(LocalDate.now().getMonthValue())).findFirst().orElse(new TmsDeclareBillDTO.StatisticsAllDTO()).getCount());
        return statisticsVO;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean addB2BDeclare(TmsDeclareBillDTO.AddDTO addDTO) {
        TmsDeclareBillDTO.QuerySourceDTO querySourceDTO = TmsDeclareBillDTO.QuerySourceDTO.builder()
//                .packingStatus(PackingTaskStatusEnum.PACKED.getCode())
                .declareStatus(WmsDeclareStatusEnum.WAIT.getCode())
                .ids(Arrays.asList(addDTO.getSourceId()))
                .build();
        if (!Boolean.TRUE.equals(addDTO.getIsAuto())) {
            querySourceDTO.setPackingStatus(PackingTaskStatusEnum.PACKED.getCode());
        }
        List<TmsDeclareBillDTO.DeliveryDTO> deliveryDTOList = wmsFirstMileDeliveryFeign.getCanGenerateDeclare(querySourceDTO);
        if (CollectionUtils.isEmpty(deliveryDTOList)) {
            throw new ServiceException("没有可生成报关单的发货单");
        }
        TmsDeclareBillDTO.DeliveryDTO deliveryDTO = deliveryDTOList.get(0);

        // 新增页面下推保存逻辑：按前端提交的合并明细直接生成报关单
        if (CollUtil.isNotEmpty(addDTO.getMergeDetailList())) {
            List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> mergeDetailList = addDTO.getMergeDetailList().stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            Set<String> sourceDetailIdSet = mergeDetailList.stream()
                    .map(TmsDeclareBillDTO.MergeDeclareBillDetailDTO::getSourceDeliveryDetailList)
                    .filter(CollUtil::isNotEmpty)
                    .flatMap(Collection::stream)
                    .filter(Objects::nonNull)
                    .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getSourceDetailId)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toSet());
            if (CollUtil.isEmpty(sourceDetailIdSet)) {
                throw new ServiceException("未找到来源明细，无法保存报关单");
            }

            List<DeliveryDeclareDetailMidEntity> existsMidList = deliveryDeclareDetailMidService.lambdaQuery()
                    .eq(DeliveryDeclareDetailMidEntity::getSourceType, SourceTypeEnum.SO_DELIVERY_NOTICE.getCode())
                    .in(DeliveryDeclareDetailMidEntity::getSourceDetailId, sourceDetailIdSet)
                    .list();
            List<DeliveryDeclareDetailMidEntity> generatedMidList = existsMidList.stream()
                    .filter(item -> StringUtils.isNotBlank(item.getDeclareId())
                            || DeliveryDeclareDetailMidGenerateStatusEnum.FINISH.getCode().equals(item.getGenerateStatus()))
                    .collect(Collectors.toList());
            if (CollUtil.isNotEmpty(generatedMidList)) {
                String repeatSourceCode = generatedMidList.stream()
                        .map(DeliveryDeclareDetailMidEntity::getSourceCode)
                        .filter(StringUtils::isNotBlank)
                        .distinct()
                        .collect(Collectors.joining("、"));
                throw new ServiceException(StringUtils.isBlank(repeatSourceCode)
                        ? "所选明细已生成报关单，请勿重复保存"
                        : CharSequenceUtil.format("来源单【{}】已生成报关单，请勿重复保存", repeatSourceCode));
            }

            TmsDeclareBillEntity declareBillEntity = new TmsDeclareBillEntity();
            BeanMapperUtils.copy(addDTO, declareBillEntity);
            BeanMapperUtils.copy(deliveryDTO, declareBillEntity);
            declareBillEntity.setDeclareStatus(com.erp.model.tms.enums.DeclareStatusEnum.WAIT.getCode());
            declareBillEntity.setType(SourceTypeEnum.SO_DELIVERY_NOTICE.getCode());
            declareBillEntity.setDeclareDate(Objects.isNull(declareBillEntity.getDeclareDate()) ? LocalDate.now() : declareBillEntity.getDeclareDate());
            declareBillEntity.setShippingFee(Objects.isNull(declareBillEntity.getShippingFee()) ? BigDecimal.ZERO : declareBillEntity.getShippingFee());
            declareBillEntity.setInsuranceFee(Objects.isNull(declareBillEntity.getInsuranceFee()) ? BigDecimal.ZERO : declareBillEntity.getInsuranceFee());
            declareBillEntity.setOtherFee(Objects.isNull(declareBillEntity.getOtherFee()) ? BigDecimal.ZERO : declareBillEntity.getOtherFee());
            declareBillEntity.setGrossWeight(Objects.isNull(declareBillEntity.getGrossWeight()) ? BigDecimal.ZERO : declareBillEntity.getGrossWeight());
            declareBillEntity.setNetWeight(Objects.isNull(declareBillEntity.getNetWeight()) ? BigDecimal.ZERO : declareBillEntity.getNetWeight());

            List<TmsDeclareBillDetailEntity> detailEntityList = new ArrayList<>(mergeDetailList.size());
            for (TmsDeclareBillDTO.MergeDeclareBillDetailDTO detailDTO : mergeDetailList) {
                TmsDeclareBillDetailEntity detailEntity = new TmsDeclareBillDetailEntity();
                detailEntity.setSkuNo(detailDTO.getSkuNo());
                detailEntity.setCustomsCode(detailDTO.getHsCode());
                detailEntity.setDeclareChineseName(detailDTO.getProductNameCn());
                detailEntity.setDeclareElement(detailDTO.getDeclareElement());
                detailEntity.setDeclareUnit(detailDTO.getUnit());
                detailEntity.setPrice(Objects.isNull(detailDTO.getUnitPrice()) ? BigDecimal.ZERO : detailDTO.getUnitPrice());
                detailEntity.setQty(Objects.isNull(detailDTO.getQty()) ? 0 : detailDTO.getQty());
                detailEntity.setDeclareCurrency(detailDTO.getDeclareCurrency());
                detailEntity.setDeclareCurrencySymbol(detailDTO.getDeclareCurrencySymbol());
                detailEntityList.add(detailEntity);
            }
            if (CollUtil.isEmpty(detailEntityList)) {
                throw new ServiceException("请选择需要保存的报关明细");
            }
            Set<String> boxNoSet = mergeDetailList.stream()
                    .map(TmsDeclareBillDTO.MergeDeclareBillDetailDTO::getSourceDeliveryDetailList)
                    .filter(CollUtil::isNotEmpty)
                    .flatMap(Collection::stream)
                    .filter(Objects::nonNull)
                    .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getBoxNo)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toSet());
            declareBillEntity.setBoxQty(boxNoSet.size());
            BaseResultDTO.AddDTO addResult = service.add(declareBillEntity, detailEntityList, SourceTypeEnum.SO_DELIVERY_NOTICE, false);

            List<DeliveryDeclareDetailMidEntity> addMidList = new ArrayList<>();
            for (int i = 0; i < mergeDetailList.size(); i++) {
                TmsDeclareBillDTO.MergeDeclareBillDetailDTO declareDetail = mergeDetailList.get(i);
                TmsDeclareBillDetailEntity billDetailEntity = detailEntityList.get(i);
                if (CollUtil.isEmpty(declareDetail.getSourceDeliveryDetailList())) {
                    continue;
                }
                for (TmsDeclareBillDTO.SourceDeliveryDetailDTO sourceDetail : declareDetail.getSourceDeliveryDetailList()) {
                    DeliveryDeclareDetailMidEntity midEntity = new DeliveryDeclareDetailMidEntity();
                    midEntity.setDeclareStatus(DeclareStatusEnum.WAIT.getCode());
                    midEntity.setGenerateStatus(DeliveryDeclareDetailMidGenerateStatusEnum.FINISH.getCode());
                    midEntity.setSourceType(SourceTypeEnum.SO_DELIVERY_NOTICE.getCode());
                    midEntity.setSourceId(StringUtils.defaultIfBlank(sourceDetail.getSourceId(), addDTO.getSourceId()));
                    midEntity.setSourceCode(StringUtils.defaultString(sourceDetail.getSourceCode()));
                    midEntity.setSourceDetailId(StringUtils.defaultString(sourceDetail.getSourceDetailId()));
                    midEntity.setBusinessId(StringUtils.defaultString(sourceDetail.getBusinessId()));
                    midEntity.setBusinessCode(StringUtils.defaultString(sourceDetail.getBusinessCode()));
                    midEntity.setContractNo(addResult.getCode());
                    midEntity.setSkuId(StringUtils.defaultString(sourceDetail.getSkuId()));
                    midEntity.setSkuNo(StringUtils.defaultString(sourceDetail.getSkuNo()));
                    midEntity.setComboSkuNo(StringUtils.defaultString(sourceDetail.getComboSkuNo()));
                    midEntity.setCurrency(StringUtils.defaultString(declareDetail.getDeclareCurrency()));
                    midEntity.setCurrencySymbol(StringUtils.defaultString(declareDetail.getDeclareCurrencySymbol()));
                    midEntity.setDeclareId(addResult.getId());
                    midEntity.setDeclareCode(addResult.getCode());
                    midEntity.setDeclareDetailId(billDetailEntity.getId());
                    midEntity.setBoxNo(StringUtils.defaultString(sourceDetail.getBoxNo()));
                    midEntity.setHsCode(StringUtils.defaultString(declareDetail.getHsCode()));
                    midEntity.setProductNameCn(StringUtils.defaultString(declareDetail.getProductNameCn()));
                    midEntity.setDeclareElement(StringUtils.defaultString(declareDetail.getDeclareElement()));
                    midEntity.setUnit(StringUtils.defaultString(declareDetail.getUnit()));
                    midEntity.setUnitPrice(Objects.isNull(declareDetail.getUnitPrice()) ? BigDecimal.ZERO : declareDetail.getUnitPrice());
                    midEntity.setQty(Objects.isNull(sourceDetail.getQty()) ? 0 : sourceDetail.getQty());
                    addMidList.add(midEntity);
                }
            }
            if (CollUtil.isNotEmpty(addMidList)) {
                deliveryDeclareDetailMidService.saveBatch(addMidList);
            }
            updateSourceDeclareStatus(SourceTypeEnum.SO_DELIVERY_NOTICE.getCode(), addMidList);
            return Boolean.TRUE;
        }

        TmsDeclareBillEntity baseTmsDeclareBillEntity = new TmsDeclareBillEntity();
        BeanMapperUtils.copy(addDTO, baseTmsDeclareBillEntity);
        BeanMapperUtils.copy(deliveryDTO, baseTmsDeclareBillEntity);
        baseTmsDeclareBillEntity.setDeclareStatus(com.erp.model.tms.enums.DeclareStatusEnum.WAIT.getCode());
        baseTmsDeclareBillEntity.setType(SourceTypeEnum.SO_DELIVERY_NOTICE.getCode());
        //50个明细为一个报关单
        List<TmsDeclareBillDTO.ProductDetail> allProductDetailList = deliveryDTO.getProductDetailList();
        if(CollectionUtils.isEmpty(allProductDetailList)){
            throw new ServiceException("没有可生成报关单的明细");
        }
        List<List<TmsDeclareBillDTO.ProductDetail>> productDetailListList = Lists.partition(allProductDetailList, limitSkuNo);
        for(List<TmsDeclareBillDTO.ProductDetail> productDetailList : productDetailListList){
            TmsDeclareBillEntity tmsDeclareBillEntity = BeanUtil.copyProperties(baseTmsDeclareBillEntity,TmsDeclareBillEntity.class);
            List<TmsDeclareBillDetailEntity> detailEntityList = BeanUtil.copyToList(productDetailList,TmsDeclareBillDetailEntity.class);
            tmsDeclareBillEntity.setNetWeight(productDetailList.stream().filter(v->Objects.nonNull(v.getNetWeight())).map(v->v.getNetWeight().multiply(new BigDecimal(v.getQty())).divide(new BigDecimal(1000),4, RoundingMode.HALF_UP)).reduce(BigDecimal.ZERO, BigDecimal::add));
            service.add(tmsDeclareBillEntity,detailEntityList,SourceTypeEnum.SO_DELIVERY_NOTICE,false);
        }
        //更新发货单的报关状态
        if (!Boolean.TRUE.equals(addDTO.getIsAuto())) {
            FirstMileDeliveryDTO.UpdateStatusDTO dto = new FirstMileDeliveryDTO.UpdateStatusDTO();
            dto.setIds(Arrays.asList(addDTO.getSourceId()));
            dto.setDeclareStatus(WmsDeclareStatusEnum.FINISH.getCode());
            wmsFirstMileDeliveryFeign.updateStatus(dto);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean autoGenerateFirstMileDeclare(AutoGenerateBillDTO autoGenerateBillDTO) {
        if(StringUtils.isBlank(autoGenerateBillDTO.getId()) || Objects.isNull(autoGenerateBillDTO.getSourceTypeEnum()) || Objects.isNull(autoGenerateBillDTO.getBillGenerateTimingEnum())){
            return false;
        }
        //自动下推需要校验配置
        if(Boolean.TRUE.equals(autoGenerateBillDTO.getCheckCfg())){
            CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingEnum.BILL_AUTO_ADD.getCode());
            if(cfgSettingEntity.getDisabled()){
                return false;
            }
            CfgSettingValueDTO.BillAutoAddDTO dto = BeanUtil.toBean(cfgSettingEntity.getDataJson(), CfgSettingValueDTO.BillAutoAddDTO.class);
            if(Objects.isNull(dto) || Objects.isNull(dto.getIsAutoFirstMileDeclare()) || !dto.getIsAutoFirstMileDeclare() ||
                    StringUtils.isBlank(dto.getFirstMileDeclareGenerateTiming()) || !dto.getFirstMileDeclareGenerateTiming().equals(autoGenerateBillDTO.getBillGenerateTimingEnum().getCode())){
                return false;
            }
        }
        //生成报关单
        TmsDeclareBillDTO.AddDTO addDTO  = new TmsDeclareBillDTO.AddDTO();
        addDTO.setSourceId(autoGenerateBillDTO.getId());
        addDTO.setDeclareType(DeclareDeclareTypeEnum.INDEPENDENT.getCode());
        addDTO.setDictSupervisionMethod(DeclareSupervisionMethodEnum.COMMONLY.getCode());
        addDTO.setDictNatureLevy(DeclareNatureLevyEnum.COMMONLY.getCode());
        if(Objects.nonNull(autoGenerateBillDTO.getFirstMileDeliveryEntity())){
            addDTO.setToArea(autoGenerateBillDTO.getFirstMileDeliveryEntity().getCountryId());
            addDTO.setToPort(autoGenerateBillDTO.getFirstMileDeliveryEntity().getCountryId());
        }
        //发货公司
        CfgSettingEntity declareSetting = cfgSettingService.getByKey(CfgSettingEnum.DECLARE_CUSTOMS.getCode());
        if(Objects.nonNull(declareSetting) && Objects.nonNull(declareSetting.getDataJson().get("id"))){
            String senderId = declareSetting.getDataJson().get("id").toString();
            addDTO.setSenderId(senderId);
        }
        if(Objects.nonNull(declareSetting) && Objects.nonNull(declareSetting.getDataJson().get("receiverName"))){
            String receiverName = declareSetting.getDataJson().get("receiverName").toString();
            addDTO.setReceiverName(receiverName);
        }
        addDTO.setDictPackType(DeclarePackTypeEnum.CARTON.getCode());
        addDTO.setDictTransactionMethod(DeclareTransactionMethodEnum.EXW.getCode());
        addDTO.setIsAuto(true);
        this.addFmDeclare(addDTO);
        return true;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public Boolean autoGenerateB2bDeclare(AutoGenerateBillDTO autoGenerateBillDTO) {
        if(StringUtils.isBlank(autoGenerateBillDTO.getId()) || Objects.isNull(autoGenerateBillDTO.getSourceTypeEnum()) || Objects.isNull(autoGenerateBillDTO.getBillGenerateTimingEnum())){
            return false;
        }
        //自动下推需要校验配置
        if(Boolean.TRUE.equals(autoGenerateBillDTO.getCheckCfg())){
            CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingEnum.BILL_AUTO_ADD.getCode());
            if(Objects.isNull(cfgSettingEntity) || cfgSettingEntity.getDisabled()){
                return false;
            }
            CfgSettingValueDTO.BillAutoAddDTO dto = BeanUtil.toBean(cfgSettingEntity.getDataJson(), CfgSettingValueDTO.BillAutoAddDTO.class);
            if(Objects.isNull(dto) || Objects.isNull(dto.getIsAutoB2BDeclare()) || !dto.getIsAutoB2BDeclare() ||
                    StringUtils.isBlank(dto.getB2BDeclareGenerateTiming()) || !dto.getB2BDeclareGenerateTiming().equals(autoGenerateBillDTO.getBillGenerateTimingEnum().getCode())){
                return false;
            }
        }

        //生成报关单
        TmsDeclareBillDTO.AddDTO addDTO  = new TmsDeclareBillDTO.AddDTO();
        addDTO.setSourceId(autoGenerateBillDTO.getId());
        addDTO.setDeclareType(DeclareDeclareTypeEnum.INDEPENDENT.getCode());
        addDTO.setDictSupervisionMethod(DeclareSupervisionMethodEnum.COMMONLY.getCode());
        addDTO.setDictNatureLevy(DeclareNatureLevyEnum.COMMONLY.getCode());
        if(Objects.nonNull(autoGenerateBillDTO.getSoOutstockEntity())){
            addDTO.setToArea(autoGenerateBillDTO.getSoOutstockEntity().getCountry());
            addDTO.setToPort(autoGenerateBillDTO.getSoOutstockEntity().getCountry());
        }
        //发货公司
        CfgSettingEntity declareSetting = cfgSettingService.getByKey(CfgSettingEnum.DECLARE_CUSTOMS.getCode());
        if(Objects.nonNull(declareSetting) && Objects.nonNull(declareSetting.getDataJson().get("id"))){
            String senderId = declareSetting.getDataJson().get("id").toString();
            addDTO.setSenderId(senderId);
        }
        if(Objects.nonNull(declareSetting) && Objects.nonNull(declareSetting.getDataJson().get("receiverName"))){
            String receiverName = declareSetting.getDataJson().get("receiverName").toString();
            addDTO.setReceiverName(receiverName);
        }
        addDTO.setDictPackType(DeclarePackTypeEnum.CARTON.getCode());
        addDTO.setDictTransactionMethod(DeclareTransactionMethodEnum.EXW.getCode());
        addDTO.setIsAuto(true);
        this.addB2BDeclare(addDTO);
        return true;
    }


    @Override
    public PagingVO<TmsDeclareBillDTO.PagingVO> export(PagingDTO<TmsDeclareBillDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getParams().getPermissionSql());
        IPage<TmsDeclareBillDTO.PagingVO> pageData = baseMapper.paging(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        fillPagingDb(pageData.getRecords(), dto.getParams().getType());
        return new PagingVO<>(pageData);
    }

    @Override
    public void exportDeclare(TmsDeclareBillDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response) throws IOException {
        pagingParamDTO.setExportDeclareStatus(Arrays.asList(com.erp.model.tms.enums.DeclareStatusEnum.DECLARED.getCode(), com.erp.model.tms.enums.DeclareStatusEnum.WAIT.getCode()));
        List<TmsDeclareBillDTO.ExportDTO> list = baseMapper.exportDeclare(pagingParamDTO);
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        fillExport(list);
        String excelPath = "excel/declareExport.xlsx";
        String name = "报关单导出";
        //超过一行数据压缩成zip
        if(list.size() == 1){
            TmsDeclareBillDTO.ExportDTO exportDTO = list.get(0);
            // 导出数据
            StringBuffer sb = new StringBuffer();
            String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
            sb.append(date).append(name);
            try {
                new ExcelPrintUtils().patchExport(exportDTO.getProductDetailList(),exportDTO, response, sb.toString(), excelPath);
            } catch (Exception e) {
                throw new ServiceException(ApiError.FILE_EXPORT_FAILED);
            }
        }else{
            List<ExcelData> excelDataList = new ArrayList<>();
            int temp = 1;
            for (TmsDeclareBillDTO.ExportDTO exportDTO : list) {
                ExcelData excelData = new ExcelData();
                excelData.setData(exportDTO);
                excelData.setDetailList(exportDTO.getProductDetailList());
                excelData.setFilename("报关单"+exportDTO.getCode()+".xlsx");
                excelDataList.add(excelData);
                temp++;
            }
            ExcelPrintUtils.exportZipStream(excelDataList,response,excelPath,"报关单"+DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP));
        }
    }

    @Override
    public BatchResultDTO updateRemark(String id,String remark) {
        TmsDeclareBillEntity entity = this.getById(id);
        if(Objects.isNull(entity)){
            return BatchResultDTO.fail(id,"","报关单不存在");
        }
        entity.setRemark(remark);
        boolean update = this.updateById(entity);
        if(update){
            return BatchResultDTO.success(id,entity.getCode(),"备注更新成功");
        }else{
            return BatchResultDTO.fail(id,entity.getCode(),"备注更新失败");
        }
    }

    @Override
    public List<TmsDeclareBillDTO.SplitDeclareDTO> listSplitFmDetail(String id) {
        TmsDeclareBillEntity declareBillEntity = super.getById(id);
        if(Objects.isNull(declareBillEntity)){
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_INFO_NOT_FOUND);
        }
        if (!CharSequenceUtil.equals(declareBillEntity.getType(), SourceTypeEnum.FM_DECLARE_BILL.getCode())) {
            throw new ServiceException("非头程报关单，无法查看拆分明细");
        }
        if (!CharSequenceUtil.equals(declareBillEntity.getDeclareStatus(), DeclareStatusEnum.WAIT.getCode())) {
            throw new ServiceException("仅待确认报关单可以拆分明细");
        }

        List<DeliveryDeclareDetailMidEntity> deliveryDeclareDetailMidList = deliveryDeclareDetailMidService.listByDeclareBillIdList(Collections.singletonList(id));
        if (CollUtil.isEmpty(deliveryDeclareDetailMidList)) {
            throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC,"报关明细关联信息");
        }

        List<TmsDeclareBillDTO.SplitDeclareDTO> splitDeclareDTOList = new ArrayList<>();
        Map<String, List<DeliveryDeclareDetailMidEntity>> map = deliveryDeclareDetailMidList.stream().collect(Collectors.groupingBy(obj -> obj.getSourceId().concat(obj.getBoxNo())));
        for ( Map.Entry<String, List<DeliveryDeclareDetailMidEntity>> entry : map.entrySet()) {
            TmsDeclareBillDTO.SplitDeclareDTO splitDeclareDTO = new TmsDeclareBillDTO.SplitDeclareDTO();
            List<DeliveryDeclareDetailMidEntity> value = entry.getValue();
            splitDeclareDTO.setId(id);
            splitDeclareDTO.setBoxNo(value.get(0).getBoxNo());
            splitDeclareDTO.setSourceId(value.get(0).getSourceId());
            //sku信息描述格式：skuNo*qty,skuNo*qty
            String skuDesc = value.stream().map(obj -> CharSequenceUtil.format("{}*{}", obj.getSkuNo(), obj.getQty())).collect(Collectors.joining(","));
            splitDeclareDTO.setSkuDesc(skuDesc);
            List<TmsDeclareBillDTO.SplitDetailDTO> splitDetailDTOList = value.stream().map(obj -> new TmsDeclareBillDTO.SplitDetailDTO(obj.getSourceDetailId(), obj.getSkuId(), obj.getSkuNo(), obj.getQty())).collect(Collectors.toList());
            splitDeclareDTO.setSkuDetailList(splitDetailDTOList);
            splitDeclareDTOList.add(splitDeclareDTO);
        }
        return splitDeclareDTOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchAddSplitFmDetail(TmsDeclareBillDTO.AddSplitDeclareDTO declareDTO) {
        if (CollUtil.isEmpty(declareDTO.getSplitDeclareDTOList())) {
            return Boolean.TRUE;
        }
        List<String> declareIdList = declareDTO.getSplitDeclareDTOList().stream().map(TmsDeclareBillDTO.SplitDeclareDTO::getId).distinct().collect(Collectors.toList());
        if (declareIdList.size() != MathUtil.ONE) {
            throw new ServiceException("批量添加拆分明细只能关联同一张报关单");
        }
        TmsDeclareBillEntity declareBillEntity = super.getById(declareIdList.get(0));
        if (ObjectUtil.isEmpty(declareBillEntity)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_INFO_NOT_FOUND);
        }
        if (!CharSequenceUtil.equals(declareBillEntity.getType(), SourceTypeEnum.FM_DECLARE_BILL.getCode())) {
            throw new ServiceException("非头程报关单，无法添加拆分明细");
        }
        if (!CharSequenceUtil.equals(declareBillEntity.getDeclareStatus(), DeclareStatusEnum.WAIT.getCode())) {
            throw new ServiceException("仅待确认报关单可以拆分明细");
        }

        //删除原本的报关单
        deleteDeclareBillById(declareBillEntity.getId());

        List<String> ids = declareDTO.getSplitDeclareDTOList().stream()
                .filter(Objects::nonNull)
                .map(TmsDeclareBillDTO.SplitDeclareDTO::getSourceId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDeliveryDetailList = deliveryDeclareDetailMidService.listSourceByDeclareIdList(ids);
        for (TmsDeclareBillDTO.SplitDeclareDTO splitDeclareDTO : declareDTO.getSplitDeclareDTOList()) {

            //按规则合并数据
            List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> batchSourceList = sourceDeliveryDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSourceId(), splitDeclareDTO.getSourceId()) && CharSequenceUtil.equals(obj.getBoxNo(), splitDeclareDTO.getBoxNo())).collect(Collectors.toList());
            List<TmsDeclareBillDTO.MergeDeclareBillDTO> mergeDeclareBillDTOS = autoMergeDeclareBillView(new TmsDeclareBillDTO.AutoMergeDeclareBillViewDTO(Boolean.TRUE, batchSourceList));

            //保存合并数据
            batchAddMergeDetail(SourceTypeEnum.FM_DECLARE_BILL.getCode(),mergeDeclareBillDTOS);
        }
        return Boolean.TRUE;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDeclareBillById (String id) {
        //删除发货明细数据
        deliveryDeclareDetailMidService.deleteDeliveryDeclareDetailMid(Collections.singletonList(id));
        //删除明细数据
        detailService.deleteDetailByMainIdList(Collections.singletonList(id));
        //删除主表数据
        super.removeById(id);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchAddSplitB2bDetail(TmsDeclareBillDTO.AddSplitDeclareDTO declareDTO) {
        if (CollUtil.isEmpty(declareDTO.getSplitDeclareDTOList())) {
            return Boolean.TRUE;
        }
        List<String> declareIdList = declareDTO.getSplitDeclareDTOList().stream().map(TmsDeclareBillDTO.SplitDeclareDTO::getId).distinct().collect(Collectors.toList());
        if (declareIdList.size() != MathUtil.ONE) {
            throw new ServiceException("批量添加拆分明细只能关联同一张报关单");
        }
        TmsDeclareBillEntity declareBillEntity = super.getById(declareIdList.get(0));
        if (ObjectUtil.isEmpty(declareBillEntity)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_INFO_NOT_FOUND);
        }
        if (!CharSequenceUtil.equals(declareBillEntity.getType(), SourceTypeEnum.B2B_DECLARE_BILL.getCode())) {
            throw new ServiceException("非B2B报关单，无法添加拆分明细");
        }
        if (!CharSequenceUtil.equals(declareBillEntity.getDeclareStatus(), DeclareStatusEnum.WAIT.getCode())) {
            throw new ServiceException("仅待确认报关单可以拆分明细");
        }

        List<String> ids = declareDTO.getSplitDeclareDTOList().stream()
                .filter(Objects::nonNull)
                .map(TmsDeclareBillDTO.SplitDeclareDTO::getSourceId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDeliveryDetailList = deliveryDeclareDetailMidService.listSourceByDeclareIdList(ids);
        for (TmsDeclareBillDTO.SplitDeclareDTO splitDeclareDTO : declareDTO.getSplitDeclareDTOList()) {

            //按规则合并数据
            List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> batchSourceList = sourceDeliveryDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSourceId(), splitDeclareDTO.getSourceId()) && CharSequenceUtil.equals(obj.getBoxNo(), splitDeclareDTO.getBoxNo())).collect(Collectors.toList());
            List<TmsDeclareBillDTO.MergeDeclareBillDTO> mergeDeclareBillDTOS = autoMergeDeclareBillView(new TmsDeclareBillDTO.AutoMergeDeclareBillViewDTO(Boolean.TRUE, batchSourceList));

            //保存合并数据
            batchAddMergeDetail(SourceTypeEnum.B2B_DECLARE_BILL.getCode(),mergeDeclareBillDTOS);
        }
        return Boolean.TRUE;
    }

    @Override
    public List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> listBeforeMergeDetail(List<String> ids) {
        List<TmsDeclareBillEntity> tmsDeclareBillList = super.listByIds(ids);
        if (CollUtil.isEmpty(tmsDeclareBillList)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_INFO_NOT_FOUND);
        }
        //仅待确认报关单支持操作合并
        tmsDeclareBillList.stream().filter(obj -> !CharSequenceUtil.equals(obj.getDeclareStatus(), DeclareStatusEnum.WAIT.getCode()))
                .findFirst()
                .ifPresent(obj -> {
                    String declareCode = CharSequenceUtil.blankToDefault(obj.getCode(), obj.getId());
                    throw new ServiceException(CharSequenceUtil.format("报关单【{}】仅待确认状态可以操作合并", declareCode));
                });
        return deliveryDeclareDetailMidService.listSourceByDeclareIdList(ids);
    }

    @Override
    public List<TmsDeclareBillDTO.MergeDeclareBillDTO> listAfterMergeDetail( List<String> ids) {
        List<TmsDeclareBillEntity> tmsDeclareBillList = super.listByIds(ids);
        if (CollUtil.isEmpty(tmsDeclareBillList)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_INFO_NOT_FOUND);
        }
        //仅待确认报关单支持操作合并
        tmsDeclareBillList.stream().filter(obj -> !CharSequenceUtil.equals(obj.getDeclareStatus(), DeclareStatusEnum.WAIT.getCode()))
                .findFirst()
                .ifPresent(obj -> {
                    String declareCode = CharSequenceUtil.blankToDefault(obj.getCode(), obj.getId());
                    throw new ServiceException(CharSequenceUtil.format("报关单【{}】仅待确认状态可以操作合并", declareCode));
                });
        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDeliveryDetailList = deliveryDeclareDetailMidService.listSourceByDeclareIdList(ids);
        return autoMergeDeclareBillView(new TmsDeclareBillDTO.AutoMergeDeclareBillViewDTO(Boolean.TRUE,sourceDeliveryDetailList));
    }


    /**
     * 处理合并前报关信息
     * @author will
     * @date 2026/4/27 17:32
     * @param viewDTO
     */
    @Override
    public  List<TmsDeclareBillDTO.MergeDeclareBillDTO> autoMergeDeclareBillView(TmsDeclareBillDTO.AutoMergeDeclareBillViewDTO viewDTO) {
        if (CollectionUtils.isEmpty(viewDTO.getSourceDeliveryDetailList())) {
            return Collections.emptyList();
        }
        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> result = new ArrayList<>();

        //产品物流信息
        List<String> skuIdList = viewDTO.getSourceDeliveryDetailList().stream().map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailDTO.ProductLogisticDTO> productLogisticsList = plmTaskFeign.listProductLogisticsByIds(skuIdList);
        Map<String, ProductDetailDTO.ProductLogisticDTO> logisticsMap = CollUtil.isEmpty(productLogisticsList) ? new HashMap<>() : productLogisticsList.stream().collect(Collectors.toMap(ProductDetailDTO.ProductLogisticDTO::getSkuId,item -> item));

        //查询单位名称
        List<BasicDictEntity> declareUnitList = FeignQuery.create(BasicDictEntity.class).eq(BasicDictEntity::getType, "declareUnit").list();
        Map<String, String> declareUnitNameMap = CollUtil.isEmpty(declareUnitList) ? new HashMap<>() : declareUnitList.stream().collect(Collectors.toMap(BasicDictEntity::getValue, BasicDictEntity::getName, (a, b) -> a));

        //币别明细
        List<DictCurrencyEntity> dictCurrencyList = sysUserFeign.currencyList();
        Map<String, String> currencyMap = CollUtil.isEmpty(dictCurrencyList) ? new HashMap<>() : dictCurrencyList.stream().collect(Collectors.toMap(DictCurrencyEntity::getId, DictCurrencyEntity::getName));

        for (TmsDeclareBillDTO.SourceDeliveryDetailDTO detailDTO : viewDTO.getSourceDeliveryDetailList()) {
            //产品物流信息赋值
            ProductDetailDTO.ProductLogisticDTO productLogisticsDTO = logisticsMap.get(detailDTO.getSkuId());
            if (Objects.nonNull(productLogisticsDTO)) {
                fillDeclareInfo(detailDTO, productLogisticsDTO, declareUnitNameMap, currencyMap);
            }
            if(Objects.nonNull(productLogisticsDTO)
                    && CombinationDeclareTypeEnums.SPLIT.getCode().equals(productLogisticsDTO.getCombinationDeclareType())
                    && Boolean.TRUE.equals(productLogisticsDTO.getIsCombination())
                    && CollUtil.isNotEmpty(productLogisticsDTO.getChildList())){
                //拆分申报的组合品，拆成子SKU
                for (ProductDetailDTO.ProductLogisticDTO logisticDTO : productLogisticsDTO.getChildList()) {
                    TmsDeclareBillDTO.SourceDeliveryDetailDTO sourceDeliveryDetailDTO = new TmsDeclareBillDTO.SourceDeliveryDetailDTO();
                    BeanUtil.copyProperties(detailDTO, sourceDeliveryDetailDTO);
                    sourceDeliveryDetailDTO.setSkuId(logisticDTO.getSkuId());
                    sourceDeliveryDetailDTO.setSkuNo(logisticDTO.getSkuNo());
                    sourceDeliveryDetailDTO.setComboSkuNo(productLogisticsDTO.getSkuNo());
                    sourceDeliveryDetailDTO.setQty((detailDTO.getQty() == null ? 0 : detailDTO.getQty()) * (logisticDTO.getChildQty() == null ? 1 : logisticDTO.getChildQty()));
                    fillDeclareInfo(sourceDeliveryDetailDTO, logisticDTO, declareUnitNameMap, currencyMap);
                    result.add(sourceDeliveryDetailDTO);
                }
            }else {
                result.add(detailDTO);
            }
        }
        DeclarationGenerationService declarationGenerationService = new DeclarationGenerationService();
        return declarationGenerationService.generateMergeBillDetails(result, viewDTO.getIsMerge());
    }

    /**
     * 报关信息赋值
     * @author will
     * @date 2026/4/29 14:48
     * @param detailDTO
     * @param productLogisticsDTO
     * @param declareUnitNameMap
     * @param currencyMap
     */
    private void fillDeclareInfo(TmsDeclareBillDTO.SourceDeliveryDetailDTO detailDTO,
                                 ProductDetailDTO.ProductLogisticDTO productLogisticsDTO,
                                 Map<String, String> declareUnitNameMap,
                                 Map<String, String> currencyMap) {
        detailDTO.setHsCode(productLogisticsDTO.getCustomsCode());
        detailDTO.setProductNameCn(productLogisticsDTO.getDeclareChineseName());
        detailDTO.setDeclareElement(productLogisticsDTO.getDeclareElement());
        detailDTO.setUnit(productLogisticsDTO.getDeclareUnit());
        detailDTO.setUnitName(declareUnitNameMap.get(productLogisticsDTO.getDeclareUnit()));
        detailDTO.setUnitPrice(productLogisticsDTO.getPrice());
        detailDTO.setDeclareCurrency(productLogisticsDTO.getDeclareCurrency());
        detailDTO.setDeclareCurrencySymbol(productLogisticsDTO.getDeclareCurrencySymbol());
        detailDTO.setDeclareCurrencyName(currencyMap.get(productLogisticsDTO.getDeclareCurrency()));
    }

    /**
     * 根据报关明细中间表自动生成报关单
     * @author jack
     * @date 2026/4/30 16:35
     * @param dto
     * @return java.lang.Boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean autoGenerateDeclareBillByMid(AutoGenerateBillDTO dto) {
        if (Objects.isNull(dto) || StringUtils.isBlank(dto.getId()) || Objects.isNull(dto.getSourceTypeEnum())) {
            return Boolean.FALSE;
        }
        String declareBillType = getDeclareBillTypeByMidSource(dto.getSourceTypeEnum());
        if (StringUtils.isBlank(declareBillType)) {
            return Boolean.FALSE;
        }
        List<DeliveryDeclareDetailMidEntity> midList = deliveryDeclareDetailMidService.lambdaQuery()
                .eq(DeliveryDeclareDetailMidEntity::getSourceId, dto.getId())
                .eq(DeliveryDeclareDetailMidEntity::getSourceType, dto.getSourceTypeEnum().getCode())
                .eq(DeliveryDeclareDetailMidEntity::getGenerateStatus, DeliveryDeclareDetailMidGenerateStatusEnum.WAIT.getCode())
                .list();
        if (CollUtil.isEmpty(midList)) {
            return Boolean.TRUE;
        }
        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailList = midList.stream()
                .map(this::buildSourceDeliveryDetailDTO)
                .collect(Collectors.toList());
        DeclarationGenerationService declarationGenerationService = new DeclarationGenerationService();
        List<TmsDeclareBillDTO.MergeDeclareBillDTO> mergeList = declarationGenerationService.generateMergeBillDetails(sourceDetailList, Boolean.FALSE);
        return batchAddMergeMidDetail(declareBillType, mergeList, midList);
    }

    /**
     * 转换中间表来源类型为报关单类型
     * @author jack
     * @date 2026/4/30 16:35
     * @param sourceTypeEnum
     * @return java.lang.String
     */
    private String getDeclareBillTypeByMidSource(SourceTypeEnum sourceTypeEnum) {
        if (SourceTypeEnum.FIRST_MILE_DELIVERY == sourceTypeEnum) {
            return SourceTypeEnum.FM_DECLARE_BILL.getCode();
        }
        if (SourceTypeEnum.SO_DELIVERY_NOTICE == sourceTypeEnum) {
            return SourceTypeEnum.B2B_DECLARE_BILL.getCode();
        }
        return "";
    }

    /**
     * 构建合并报关来源明细
     * @author jack
     * @date 2026/4/30 16:35
     * @param entity
     * @return com.erp.model.tms.dto.TmsDeclareBillDTO.SourceDeliveryDetailDTO
     */
    private TmsDeclareBillDTO.SourceDeliveryDetailDTO buildSourceDeliveryDetailDTO(DeliveryDeclareDetailMidEntity entity) {
        TmsDeclareBillDTO.SourceDeliveryDetailDTO detailDTO = new TmsDeclareBillDTO.SourceDeliveryDetailDTO();
        detailDTO.setSourceId(entity.getSourceId());
        detailDTO.setSourceCode(entity.getSourceCode());
        detailDTO.setSourceType(entity.getSourceType());
        detailDTO.setSourceDetailId(entity.getSourceDetailId());
        detailDTO.setBusinessId(entity.getBusinessId());
        detailDTO.setBusinessCode(entity.getBusinessCode());
        detailDTO.setBoxNo(entity.getBoxNo());
        detailDTO.setSkuId(entity.getSkuId());
        detailDTO.setSkuNo(entity.getSkuNo());
        detailDTO.setHsCode(entity.getHsCode());
        detailDTO.setProductNameCn(entity.getProductNameCn());
        detailDTO.setDeclareElement(entity.getDeclareElement());
        detailDTO.setUnit(entity.getUnit());
        detailDTO.setUnitPrice(entity.getUnitPrice());
        detailDTO.setQty(entity.getQty());
        detailDTO.setDeclareCurrency(entity.getCurrency());
        detailDTO.setDeclareCurrencySymbol(entity.getCurrencySymbol());
        return detailDTO;
    }

    /**
     * 保存由中间表生成的合并报关单
     * @author jack
     * @date 2026/4/30 16:35
     * @param type
     * @param list
     * @param midList
     * @return java.lang.Boolean
     */
    private Boolean batchAddMergeMidDetail(String type, List<TmsDeclareBillDTO.MergeDeclareBillDTO> list, List<DeliveryDeclareDetailMidEntity> midList) {
        if (CollUtil.isEmpty(list) || CollUtil.isEmpty(midList)) {
            return Boolean.TRUE;
        }
        Map<String, DeliveryDeclareDetailMidEntity> midMap = midList.stream()
                .collect(Collectors.toMap(this::buildSourceDetailKey, item -> item, (a, b) -> a));
        List<DeliveryDeclareDetailMidEntity> updateMidList = new ArrayList<>();
        for (TmsDeclareBillDTO.MergeDeclareBillDTO mergeDeclareBillDTO : list) {
            if (Objects.isNull(mergeDeclareBillDTO) || CollUtil.isEmpty(mergeDeclareBillDTO.getDeclareBillList())) {
                continue;
            }
            List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> declareBillList = mergeDeclareBillDTO.getDeclareBillList();
            List<TmsDeclareBillDetailEntity> detailEntityList = new ArrayList<>(declareBillList.size());
            for (TmsDeclareBillDTO.MergeDeclareBillDetailDTO detailDTO : declareBillList) {
                TmsDeclareBillDetailEntity detailEntity = new TmsDeclareBillDetailEntity();
                detailEntity.setSkuNo(detailDTO.getSkuNo());
                detailEntity.setCustomsCode(detailDTO.getHsCode());
                detailEntity.setDeclareChineseName(detailDTO.getProductNameCn());
                detailEntity.setDeclareElement(detailDTO.getDeclareElement());
                detailEntity.setDeclareUnit(detailDTO.getUnit());
                detailEntity.setPrice(Objects.isNull(detailDTO.getUnitPrice()) ? BigDecimal.ZERO : detailDTO.getUnitPrice());
                detailEntity.setQty(Objects.isNull(detailDTO.getQty()) ? 0 : detailDTO.getQty());
                detailEntity.setDeclareCurrency(detailDTO.getDeclareCurrency());
                detailEntity.setDeclareCurrencySymbol(detailDTO.getDeclareCurrencySymbol());
                detailEntityList.add(detailEntity);
            }
            if (CollUtil.isEmpty(detailEntityList)) {
                continue;
            }

            TmsDeclareBillEntity declareBillEntity = new TmsDeclareBillEntity();
            declareBillEntity.setType(type);
            declareBillEntity.setDeclareStatus(com.erp.model.tms.enums.DeclareStatusEnum.WAIT.getCode());
            declareBillEntity.setDeclareDate(LocalDate.now());
            declareBillEntity.setNetWeight(BigDecimal.ZERO);
            declareBillEntity.setGrossWeight(BigDecimal.ZERO);
            declareBillEntity.setShippingFee(BigDecimal.ZERO);
            declareBillEntity.setInsuranceFee(BigDecimal.ZERO);
            declareBillEntity.setOtherFee(BigDecimal.ZERO);
            Set<String> boxNoSet = declareBillList.stream()
                    .map(TmsDeclareBillDTO.MergeDeclareBillDetailDTO::getSourceDeliveryDetailList)
                    .filter(CollUtil::isNotEmpty)
                    .flatMap(Collection::stream)
                    .filter(Objects::nonNull)
                    .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getBoxNo)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toSet());
            declareBillEntity.setBoxQty(boxNoSet.size());

            BaseResultDTO.AddDTO addResult = service.add(declareBillEntity, detailEntityList, SourceTypeEnum.getEnum(type), false);

            for (int i = 0; i < declareBillList.size(); i++) {
                TmsDeclareBillDTO.MergeDeclareBillDetailDTO declareDetail = declareBillList.get(i);
                TmsDeclareBillDetailEntity billDetailEntity = detailEntityList.get(i);
                if (CollUtil.isEmpty(declareDetail.getSourceDeliveryDetailList())) {
                    continue;
                }
                for (TmsDeclareBillDTO.SourceDeliveryDetailDTO sourceDetail : declareDetail.getSourceDeliveryDetailList()) {
                    DeliveryDeclareDetailMidEntity midEntity = midMap.get(buildSourceDetailKey(sourceDetail));
                    if (Objects.isNull(midEntity)) {
                        continue;
                    }
                    midEntity.setDeclareStatus(DeclareStatusEnum.WAIT.getCode());
                    midEntity.setGenerateStatus(DeliveryDeclareDetailMidGenerateStatusEnum.FINISH.getCode());
                    midEntity.setContractNo(addResult.getCode());
                    midEntity.setDeclareId(addResult.getId());
                    midEntity.setDeclareCode(addResult.getCode());
                    midEntity.setDeclareDetailId(billDetailEntity.getId());
                    updateMidList.add(midEntity);
                }
            }
        }
        if (CollUtil.isNotEmpty(updateMidList)) {
            deliveryDeclareDetailMidService.updateBatchById(updateMidList);
        }
        updateSourceDeclareStatus(type, updateMidList);
        return Boolean.TRUE;
    }

    /**
     * 构建来源明细匹配键
     * @author jack
     * @date 2026/4/30 16:35
     * @param entity
     * @return java.lang.String
     */
    private String buildSourceDetailKey(DeliveryDeclareDetailMidEntity entity) {
        return CharSequenceUtil.join("|",
                StringUtils.defaultString(entity.getSourceId()),
                StringUtils.defaultString(entity.getSourceDetailId()),
                StringUtils.defaultString(entity.getBoxNo()),
                StringUtils.defaultString(entity.getSkuId()));
    }

    /**
     * 构建来源明细匹配键
     * @author jack
     * @date 2026/4/30 16:35
     * @param detailDTO
     * @return java.lang.String
     */
    private String buildSourceDetailKey(TmsDeclareBillDTO.SourceDeliveryDetailDTO detailDTO) {
        return CharSequenceUtil.join("|",
                StringUtils.defaultString(detailDTO.getSourceId()),
                StringUtils.defaultString(detailDTO.getSourceDetailId()),
                StringUtils.defaultString(detailDTO.getBoxNo()),
                StringUtils.defaultString(detailDTO.getSkuId()));
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean batchAddMergeDetail(String type, List<TmsDeclareBillDTO.MergeDeclareBillDTO> list) {
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException(ApiError.BILL_SELECTION_REQUIRED);
        }
        List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> mergeDetailList = list.stream()
                .filter(Objects::nonNull)
                .map(TmsDeclareBillDTO.MergeDeclareBillDTO::getDeclareBillList)
                .filter(CollUtil::isNotEmpty)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(mergeDetailList)) {
            throw new ServiceException("请选择需要保存的报关明细");
        }

        Set<String> sourceDetailIdSet = mergeDetailList.stream()
                .map(TmsDeclareBillDTO.MergeDeclareBillDetailDTO::getSourceDeliveryDetailList)
                .filter(CollUtil::isNotEmpty)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getSourceDetailId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        if (CollUtil.isEmpty(sourceDetailIdSet)) {
            throw new ServiceException("未找到来源明细，无法保存报关单");
        }

        List<DeliveryDeclareDetailMidEntity> existsMidList = deliveryDeclareDetailMidService.lambdaQuery()
                .eq(DeliveryDeclareDetailMidEntity::getSourceType, type)
                .in(DeliveryDeclareDetailMidEntity::getSourceDetailId, sourceDetailIdSet)
                .list();
        List<DeliveryDeclareDetailMidEntity> generatedMidList = existsMidList.stream()
                .filter(item -> StringUtils.isNotBlank(item.getDeclareId())
                        || DeliveryDeclareDetailMidGenerateStatusEnum.FINISH.getCode().equals(item.getGenerateStatus()))
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(generatedMidList)) {
            String repeatSourceCode = generatedMidList.stream()
                    .map(DeliveryDeclareDetailMidEntity::getSourceCode)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.joining("、"));
            throw new ServiceException(StringUtils.isBlank(repeatSourceCode)
                    ? "所选明细已生成报关单，请勿重复保存"
                    : CharSequenceUtil.format("来源单【{}】已生成报关单，请勿重复保存", repeatSourceCode));
        }
        List<DeliveryDeclareDetailMidEntity> addMidList = new ArrayList<>();
        for (TmsDeclareBillDTO.MergeDeclareBillDTO mergeDeclareBillDTO : list) {
            if (Objects.isNull(mergeDeclareBillDTO) || CollUtil.isEmpty(mergeDeclareBillDTO.getDeclareBillList())) {
                continue;
            }
            List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> declareBillList = mergeDeclareBillDTO.getDeclareBillList();
            List<TmsDeclareBillDetailEntity> detailEntityList = new ArrayList<>(declareBillList.size());
            for (TmsDeclareBillDTO.MergeDeclareBillDetailDTO detailDTO : declareBillList) {
                TmsDeclareBillDetailEntity detailEntity = new TmsDeclareBillDetailEntity();
                detailEntity.setSkuNo(detailDTO.getSkuNo());
                detailEntity.setCustomsCode(detailDTO.getHsCode());
                detailEntity.setDeclareChineseName(detailDTO.getProductNameCn());
                detailEntity.setDeclareElement(detailDTO.getDeclareElement());
                detailEntity.setDeclareUnit(detailDTO.getUnit());
                detailEntity.setPrice(Objects.isNull(detailDTO.getUnitPrice()) ? BigDecimal.ZERO : detailDTO.getUnitPrice());
                detailEntity.setQty(Objects.isNull(detailDTO.getQty()) ? 0 : detailDTO.getQty());
                detailEntity.setDeclareCurrency(detailDTO.getDeclareCurrency());
                detailEntity.setDeclareCurrencySymbol(detailDTO.getDeclareCurrencySymbol());
                detailEntityList.add(detailEntity);
            }
            if (CollUtil.isEmpty(detailEntityList)) {
                continue;
            }

            TmsDeclareBillEntity declareBillEntity = new TmsDeclareBillEntity();
            declareBillEntity.setType(type);
            declareBillEntity.setDeclareStatus(com.erp.model.tms.enums.DeclareStatusEnum.WAIT.getCode());
            declareBillEntity.setDeclareDate(LocalDate.now());
            declareBillEntity.setNetWeight(BigDecimal.ZERO);
            declareBillEntity.setGrossWeight(BigDecimal.ZERO);
            declareBillEntity.setShippingFee(BigDecimal.ZERO);
            declareBillEntity.setInsuranceFee(BigDecimal.ZERO);
            declareBillEntity.setOtherFee(BigDecimal.ZERO);
            Set<String> boxNoSet = declareBillList.stream()
                    .map(TmsDeclareBillDTO.MergeDeclareBillDetailDTO::getSourceDeliveryDetailList)
                    .filter(CollUtil::isNotEmpty)
                    .flatMap(Collection::stream)
                    .filter(Objects::nonNull)
                    .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getBoxNo)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toSet());
            declareBillEntity.setBoxQty(boxNoSet.size());

            BaseResultDTO.AddDTO addResult = add(declareBillEntity, detailEntityList, SourceTypeEnum.getEnum(type), false);

            for (int i = 0; i < declareBillList.size(); i++) {
                TmsDeclareBillDTO.MergeDeclareBillDetailDTO declareDetail = declareBillList.get(i);
                TmsDeclareBillDetailEntity billDetailEntity = detailEntityList.get(i);
                if (CollUtil.isEmpty(declareDetail.getSourceDeliveryDetailList())) {
                    continue;
                }
                for (TmsDeclareBillDTO.SourceDeliveryDetailDTO sourceDetail : declareDetail.getSourceDeliveryDetailList()) {
                    DeliveryDeclareDetailMidEntity midEntity = new DeliveryDeclareDetailMidEntity();
                    midEntity.setDeclareStatus(DeclareStatusEnum.WAIT.getCode());
                    midEntity.setGenerateStatus(DeliveryDeclareDetailMidGenerateStatusEnum.FINISH.getCode());
                    midEntity.setSourceType(type);
                    midEntity.setSourceId(StringUtils.defaultString(sourceDetail.getSourceId()));
                    midEntity.setSourceCode(StringUtils.defaultString(sourceDetail.getSourceCode()));
                    midEntity.setSourceDetailId(StringUtils.defaultString(sourceDetail.getSourceDetailId()));
                    midEntity.setBusinessId(StringUtils.defaultString(sourceDetail.getBusinessId()));
                    midEntity.setBusinessCode(StringUtils.defaultString(sourceDetail.getBusinessCode()));
                    midEntity.setContractNo(addResult.getCode());
                    midEntity.setSkuId(StringUtils.defaultString(sourceDetail.getSkuId()));
                    midEntity.setSkuNo(StringUtils.defaultString(sourceDetail.getSkuNo()));
                    midEntity.setComboSkuNo(StringUtils.defaultString(sourceDetail.getComboSkuNo()));
                    midEntity.setCurrency(StringUtils.defaultString(declareDetail.getDeclareCurrency()));
                    midEntity.setCurrencySymbol(StringUtils.defaultString(declareDetail.getDeclareCurrencySymbol()));
                    midEntity.setDeclareId(addResult.getId());
                    midEntity.setDeclareCode(addResult.getCode());
                    midEntity.setDeclareDetailId(billDetailEntity.getId());
                    midEntity.setBoxNo(StringUtils.defaultString(sourceDetail.getBoxNo()));
                    midEntity.setHsCode(StringUtils.defaultString(declareDetail.getHsCode()));
                    midEntity.setProductNameCn(StringUtils.defaultString(declareDetail.getProductNameCn()));
                    midEntity.setDeclareElement(StringUtils.defaultString(declareDetail.getDeclareElement()));
                    midEntity.setUnit(StringUtils.defaultString(declareDetail.getUnit()));
                    midEntity.setUnitPrice(Objects.isNull(declareDetail.getUnitPrice()) ? BigDecimal.ZERO : declareDetail.getUnitPrice());
                    midEntity.setQty(Objects.isNull(sourceDetail.getQty()) ? 0 : sourceDetail.getQty());
                    addMidList.add(midEntity);
                }
            }
        }
        if (CollUtil.isNotEmpty(addMidList)) {
            deliveryDeclareDetailMidService.saveBatch(addMidList);
        }
        //更新报关状态
        updateSourceDeclareStatus(type,addMidList);
        return Boolean.TRUE;
    }

    /**
     * 更新来源数据报关状态
     * @author will
     * @date 2026/4/30 11:58
     * @param type
     * @param addMidList
     */
    private void updateSourceDeclareStatus(String type,List<DeliveryDeclareDetailMidEntity> addMidList) {
        if (CollUtil.isEmpty(addMidList)) {
            return;
        }
        List<String> sourceIdList = addMidList.stream().map(DeliveryDeclareDetailMidEntity::getSourceId).distinct().collect(Collectors.toList());

        //更新源单据报关状态
        if (CharSequenceUtil.equals(type,SourceTypeEnum.FM_DECLARE_BILL.getCode())) {
            //头程报关单
            FirstMileDeliveryDTO.UpdateStatusDTO dto = new FirstMileDeliveryDTO.UpdateStatusDTO(sourceIdList,null,WmsDeclareStatusEnum.FINISH.getCode());
            wmsFirstMileDeliveryFeign.updateStatus(dto);
        } else {
            //b2b报关单
            SoDeliveryNoticeDTO.DeclareStatusDTO dto = new SoDeliveryNoticeDTO.DeclareStatusDTO(sourceIdList, WmsDeclareStatusEnum.FINISH.getCode());
            soDeliveryNoticeFeign.updateDeclareStatus(dto);
        }
    }

    @Override
    public List<TmsDeclareBillDTO.SplitDeclareDTO> listSplitB2bDetail(String id) {
        TmsDeclareBillEntity declareBillEntity = super.getById(id);
        if(Objects.isNull(declareBillEntity)){
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_INFO_NOT_FOUND);
        }
        if (!CharSequenceUtil.equals(declareBillEntity.getType(), SourceTypeEnum.B2B_DECLARE_BILL.getCode())) {
            throw new ServiceException("非B2B报关单，无法查看拆分明细");
        }
        if (!CharSequenceUtil.equals(declareBillEntity.getDeclareStatus(), DeclareStatusEnum.WAIT.getCode())) {
            throw new ServiceException("仅待确认报关单可以拆分明细");
        }

        List<DeliveryDeclareDetailMidEntity> deliveryDeclareDetailMidList = deliveryDeclareDetailMidService.listByDeclareBillIdList(Collections.singletonList(id));
        if (CollUtil.isEmpty(deliveryDeclareDetailMidList)) {
            throw new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC,"报关明细关联信息");
        }


        List<TmsDeclareBillDTO.SplitDeclareDTO> splitDeclareDTOList = new ArrayList<>();
        Map<String, List<DeliveryDeclareDetailMidEntity>> map = deliveryDeclareDetailMidList.stream().collect(Collectors.groupingBy(obj -> obj.getSourceId().concat(obj.getBoxNo())));
        for ( Map.Entry<String, List<DeliveryDeclareDetailMidEntity>> entry : map.entrySet()) {
            TmsDeclareBillDTO.SplitDeclareDTO splitDeclareDTO = new TmsDeclareBillDTO.SplitDeclareDTO();
            List<DeliveryDeclareDetailMidEntity> value = entry.getValue();
            splitDeclareDTO.setBoxNo(value.get(0).getBoxNo());
            splitDeclareDTO.setSourceId(value.get(0).getSourceId());
            //sku信息描述格式：skuNo*qty,skuNo*qty
            String skuDesc = value.stream().map(obj -> CharSequenceUtil.format("{}*{}", obj.getSkuNo(), obj.getQty())).collect(Collectors.joining(","));
            splitDeclareDTO.setSkuDesc(skuDesc);
            List<TmsDeclareBillDTO.SplitDetailDTO> splitDetailDTOList = value.stream().map(obj -> new TmsDeclareBillDTO.SplitDetailDTO(obj.getSourceDetailId(), obj.getSkuId(), obj.getSkuNo(), obj.getQty())).collect(Collectors.toList());
            splitDeclareDTO.setSkuDetailList(splitDetailDTOList);
            splitDeclareDTOList.add(splitDeclareDTO);
        }
        return splitDeclareDTOList;
    }

    /**
     * 产品添加数据处理
     * @author will
     * @date 2026/4/22 17:56
     * @param list
     */
    private void handleNotGenerateData(List<TmsDeclareBillDTO.NotGenerateDetailDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        //查询币别名称
        List<String> currencyList = list.stream().map(TmsDeclareBillDTO.NotGenerateDetailDTO::getDeclareCurrency).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        List<DictCurrencyEntity> currencyEntityList = FeignQuery.create(DictCurrencyEntity.class).in(DictCurrencyEntity::getId, currencyList).list();

        //查询单位名称
        List<BasicDictEntity> declareUnitList = FeignQuery.create(BasicDictEntity.class).eq(BasicDictEntity::getType, "declareUnit").list();

        //查询原产国名称
        List<String> sourceCountryIdList = list.stream().map(TmsDeclareBillDTO.NotGenerateDetailDTO::getSourceCountry).distinct().collect(Collectors.toList());
        List<DictCountryEntity> sourceCountryList = sysDictFeign.listCountryByIds(sourceCountryIdList);

        for (TmsDeclareBillDTO.NotGenerateDetailDTO dto : list) {
            //币别名称
            DictCurrencyEntity currencyEntity = currencyEntityList.stream().filter(v -> v.getId().equals(dto.getDeclareCurrency())).findFirst().orElse(null);
            if (Objects.nonNull(currencyEntity)) {
                dto.setDeclareCurrencyName(currencyEntity.getName());
            }
            //报关单位名称
            BasicDictEntity unitEntity = declareUnitList.stream().filter(v -> v.getValue().equals(dto.getDeclareUnit())).findFirst().orElse(null);
            if (Objects.nonNull(unitEntity)) {
                dto.setDeclareUnitName(unitEntity.getName());
            }
            //国家名称
            DictCountryEntity countryEntity = sourceCountryList.stream().filter(v -> v.getId().equals(dto.getSourceCountry())).findFirst().orElse(null);
            if (Objects.nonNull(countryEntity)) {
                dto.setSourceCountryName(countryEntity.getNameCn());
            }
        }
    }
}
