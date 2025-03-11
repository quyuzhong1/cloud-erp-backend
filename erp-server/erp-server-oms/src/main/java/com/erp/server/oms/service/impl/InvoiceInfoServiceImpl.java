package com.erp.server.oms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.entity.DmpSoBillDetailEntity;
import com.erp.model.oms.dto.CfgVatInvoiceDTO;
import com.erp.model.oms.dto.InvoiceInfoDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.*;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.oms.mapper.InvoiceInfoMapper;
import com.erp.server.oms.sdk.invoice.AmazonUploadInvoiceService;
import com.erp.server.oms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_INVOICE_INFO;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_LOGISTICS_SUPPLIER;

/**
 * <p>
 * 上传记录 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2025-03-07
 */
@Slf4j
@Service
public class InvoiceInfoServiceImpl extends SuperServiceImpl<InvoiceInfoMapper, InvoiceInfoEntity> implements InvoiceInfoService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private InvoiceDetailService invoiceDetailService;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private SoB2cService soB2cService;

    @Resource
    private SoB2cDetailService soB2cDetailService;

    @Resource
    private ShopInfoService shopInfoService;

    @Resource
    private CfgVatInvoiceService cfgVatInvoiceService;

    @Resource
    private InvoiceInfoService service;

    @Resource
    private ListingInfoService listingInfoService;

    @Resource
    private AmazonUploadInvoiceService amazonUploadInvoiceService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(InvoiceInfoDTO.AddDTO addDTO) {
        InvoiceInfoEntity invoiceInfoEntity = new InvoiceInfoEntity();
        BeanMapperUtils.copy(addDTO, invoiceInfoEntity);

        // 数据处理
        handleData(invoiceInfoEntity);

        log.info("开始新增上传记录");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_INV);
        invoiceInfoEntity.setCode(code);
        boolean save = super.save(invoiceInfoEntity);
        if(!save) {
            throw new ServiceException("上传记录保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "上传记录" , invoiceInfoEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.INVOICE_INFO.getCode(), invoiceInfoEntity.getId(), "新增操作");
        // 新增明细（如果有明细的话）
        invoiceDetailService.batchAdd(invoiceInfoEntity.getId(), addDTO.getDetailList());
        return new BaseResultDTO.AddDTO(invoiceInfoEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(InvoiceInfoDTO.UpdateDTO addOrUpdateDTO) {
        InvoiceInfoEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "上传记录"));
        InvoiceInfoEntity invoiceInfoEntity =  BeanMapperUtils.map(InvoiceInfoEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(invoiceInfoEntity);
        log.info("编辑 开始修改上传记录数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(invoiceInfoEntity);
        if(!save) {
            throw new ServiceException("上传记录保存失败");
        }
        // 修改明细数据（包含增删改）（如果有明细的话）
        invoiceDetailService.batchUpdate(invoiceInfoEntity.getId(), addOrUpdateDTO.getDetailList());
        // 记录主单操作日志
            log.info("编辑 开始记录上传记录日志数据，单号：【{}】", invoiceInfoEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), invoiceInfoEntity.getCode(), "上传记录");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, invoiceInfoEntity, null, invoiceInfoEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<InvoiceInfoDTO.PagingViewDTO> paging(PagingDTO<InvoiceInfoDTO.PagingParamDTO> dto) {
        InvoiceInfoDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<InvoiceInfoDTO.PagingViewDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<InvoiceInfoDTO.PagingViewDTO> pageData = baseMapper.paging(query, params);
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }


    @Override
    public String downloadInvoice(String id) {
        InvoiceInfoEntity entity = super.getById(id);
        if (Objects.isNull(entity)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "上传记录");
        }
        return entity.getFileUrl();
    }

    @Override
    public List<BatchResultDTO> batchGenerateInvoice(List<String> ids) {
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        List<InvoiceInfoEntity> entities = super.listByIds(ids);
        List<String> soIds = entities.stream().map(InvoiceInfoEntity::getSoId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntityList = soB2cService.listByIds(soIds);
        List<String> shopIds = soB2cEntityList.stream().map(SoB2cEntity::getShopId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoEntityList = shopInfoService.listByIds(shopIds);
        List<CfgVatInvoiceEntity> cfgVatInvoiceEntities = cfgVatInvoiceService.listCfgByShopIds(shopIds);
        List<SoB2cDetailEntity> allSoB2cDetailEntityList = soB2cDetailService.listByMainIds(soIds);
        List<String> platformSkuNoList = allSoB2cDetailEntityList.stream().map(SoB2cDetailEntity::getPlatformSkuNo).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<ListingInfoEntity> listingInfoEntityList = listingInfoService.listByParam(RuleTypeEnum.PLATFORM.getCode(), soB2cEntityList.get(0).getDictPlatform(), platformSkuNoList);
        List<String> platformCodeList = soB2cEntityList.stream().map(SoB2cEntity::getPlatformCode).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        //查询亚马逊财务配送报告
        List<DmpSoBillDetailEntity> allDmpSoBillDetailEntityList = FeignQuery.create(DmpSoBillDetailEntity.class).in(DmpSoBillDetailEntity::getPlatformCode,platformCodeList)
                .in(DmpSoBillDetailEntity::getShopId,shopIds)
                .eq(DmpSoBillDetailEntity::getSourcePlatform, PlatformDictEnum.AMAZON.getCode()).list();
        List<InvoiceInfoEntity> addList = new ArrayList<>();
        List<InvoiceDetailEntity> addDetailList = new ArrayList<>();
        if(CollectionUtils.isEmpty(entities)){
            return new ArrayList<>();
        }
        for (InvoiceInfoEntity entity : entities) {
            CfgVatInvoiceEntity cfgVatInvoiceEntity = cfgVatInvoiceEntities.stream().filter(e -> CharSequenceUtil.isNotBlank(entity.getShopId()) && !e.getDisabled() && entity.getShopId().equals(e.getShopId())).findFirst().orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "发票配置"));
            SoB2cEntity soB2cEntity = soB2cEntityList.stream().filter(e -> CharSequenceUtil.isNotBlank(entity.getSoId()) && entity.getSoId().equals(e.getId())).findFirst().orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "b2c订单"));
            if (!PlatformDictEnum.AMAZON.getCode().equalsIgnoreCase(soB2cEntity.getDictPlatform()) || !soB2cEntity.hasPlatformWarehouseOrder()) {
                resultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(),soB2cEntity.getCode(),"只有亚马逊FBA订单允许生成发票"));
                continue;
            }
            List<SoB2cDetailEntity> soB2cDetailEntityList = allSoB2cDetailEntityList.stream().filter(e -> CharSequenceUtil.isNotBlank(e.getMainId()) && e.getMainId().equals(soB2cEntity.getId())).collect(Collectors.toList());
            DmpSoBillDetailEntity dmpSoBillDetailEntity = allDmpSoBillDetailEntityList.stream().filter(e ->e.getShopId().equals(soB2cEntity.getShopId())&& e.getPlatformCode().equals(soB2cEntity.getPlatformCode())).findFirst().orElse(null);
            InvoiceInfoEntity invoiceInfoEntity = buildInvoiceEntity(cfgVatInvoiceEntity, soB2cEntity, dmpSoBillDetailEntity);
            List<InvoiceDetailEntity> detailEntityList = buildInvoiceDetail(soB2cDetailEntityList, invoiceInfoEntity);
            addList.add(invoiceInfoEntity);
            addDetailList.addAll(detailEntityList);
        }
        //保存数据
        service.batchSave(addList,addDetailList);
        List<InvoiceInfoEntity> waitCreateVoiceList = addList.stream().filter(v->v.getStatus().equals(InvoiceInfoStatusEnum.INVOICE_SUCCESS.getCode())).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(waitCreateVoiceList)){
            return Collections.emptyList();
        }
        //生成PDF
        for (InvoiceInfoEntity invoiceInfoEntity : waitCreateVoiceList) {
            generateInvoicePdf(invoiceInfoEntity, cfgVatInvoiceEntities, soB2cEntityList, allSoB2cDetailEntityList, allDmpSoBillDetailEntityList, listingInfoEntityList);
            if(invoiceInfoEntity.getStatus().equals(InvoiceInfoStatusEnum.INVOICE_FAILED.getCode())){
                resultDTOList.add(BatchResultDTO.fail(invoiceInfoEntity.getId(),invoiceInfoEntity.getCode(),invoiceInfoEntity.getRemark()));
            }
        }
        service.saveBatch(waitCreateVoiceList);
        //开票成功并且配置为自动上传的，上传发票
        List<InvoiceInfoEntity> uploadInvoiceList = waitCreateVoiceList.stream().filter(v->{
            Boolean isCreatePdf = v.getStatus().equals(InvoiceInfoStatusEnum.INVOICE_SUCCESS.getCode());
            CfgVatInvoiceEntity cfgVatInvoiceEntity = cfgVatInvoiceEntities.stream().filter(e ->  !e.getDisabled() && v.getShopId().equals(e.getShopId())).findFirst().orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "发票配置"));
            return isCreatePdf && cfgVatInvoiceEntity.getIsAutoUpload();
        }).collect(Collectors.toList());
        //上传发票
        for (InvoiceInfoEntity invoiceInfoEntity : uploadInvoiceList) {
            SoB2cEntity soB2cEntity = soB2cEntityList.stream().filter(e -> CharSequenceUtil.isNotBlank(invoiceInfoEntity.getSoId()) && invoiceInfoEntity.getSoId().equals(e.getId())).findFirst().orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "b2c订单"));
            try {
                amazonUploadInvoiceService.uploadInvoice(soB2cEntity,invoiceInfoEntity.getFileUrl(),invoiceInfoEntity.getCode());
                soB2cEntity.setVatInvoiceStatus(SoB2cVatStatusEnum.UPLOAD_SUCCESS.getCode());
            }catch (Exception e){
                soB2cEntity.setVatInvoiceStatus(SoB2cVatStatusEnum.UPLOAD_FAILURE.getCode());
                log.error("亚马逊上传发票失败,{}",e.getMessage());
                resultDTOList.add(BatchResultDTO.fail(invoiceInfoEntity.getId(),invoiceInfoEntity.getCode(),StrUtil.format("亚马逊上传发票失败,{}",e.getMessage())));
            }
        }
        soB2cService.updateBatchById(soB2cEntityList);
        return resultDTOList;
    }

    private void generateInvoicePdf(InvoiceInfoEntity invoiceInfoEntity, List<CfgVatInvoiceEntity> cfgVatInvoiceEntities, List<SoB2cEntity> soB2cEntityList, List<SoB2cDetailEntity> allSoB2cDetailEntityList, List<DmpSoBillDetailEntity> allDmpSoBillDetailEntityList, List<ListingInfoEntity> listingInfoEntityList) {
        CfgVatInvoiceEntity cfgVatInvoiceEntity = cfgVatInvoiceEntities.stream().filter(e ->  !e.getDisabled() && invoiceInfoEntity.getShopId().equals(e.getShopId())).findFirst().orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "发票配置"));
        SoB2cEntity soB2cEntity = soB2cEntityList.stream().filter(e -> invoiceInfoEntity.getSoId().equals(e.getId())).findFirst().orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "b2c订单"));
        List<SoB2cDetailEntity> soB2cDetailEntityList = allSoB2cDetailEntityList.stream().filter(e ->  e.getMainId().equals(soB2cEntity.getId())).collect(Collectors.toList());
        List<DmpSoBillDetailEntity> dmpSoBillDetailEntityList = allDmpSoBillDetailEntityList.stream().filter(e ->e.getShopId().equals(soB2cEntity.getShopId())&& e.getPlatformCode().equals(soB2cEntity.getPlatformCode())).collect(Collectors.toList());
        DmpSoBillDetailEntity dmpSoBillDetailEntity = dmpSoBillDetailEntityList.get(0);
        CfgVatInvoiceDTO.InvoiceTemplateDTO invoiceTemplateDTO = new CfgVatInvoiceDTO.InvoiceTemplateDTO();
        invoiceTemplateDTO.setCustomerBillAddress(dmpSoBillDetailEntity.getAddress1()+" "+dmpSoBillDetailEntity.getAddress2()+" "+dmpSoBillDetailEntity.getAddress3()+" "+dmpSoBillDetailEntity.getCity()+" "+dmpSoBillDetailEntity.getState()+" "+dmpSoBillDetailEntity.getPostalCode()+" "+dmpSoBillDetailEntity.getCountry());
        invoiceTemplateDTO.setCompanyName(cfgVatInvoiceEntity.getCompanyName());
        invoiceTemplateDTO.setCompanyAddress(cfgVatInvoiceEntity.getCompanyAddress());
        invoiceTemplateDTO.setVatNo(cfgVatInvoiceEntity.getVatNo());
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        invoiceTemplateDTO.setBillCreateTime(LocalDateTime.now().format(dateTimeFormatter));
        invoiceTemplateDTO.setInvoiceCode(invoiceInfoEntity.getCode());
        invoiceTemplateDTO.setPlatformCreateTime(soB2cEntity.getPlatformOrderCreateTime().format(dateTimeFormatter));
        invoiceTemplateDTO.setPlatformCode(soB2cEntity.getPlatformCode());
        invoiceTemplateDTO.setCurrencyCode(soB2cEntity.getCurrency());
        String symbol = CurrencyEnum.getSymbolByCode(soB2cEntity.getCurrency());
        invoiceTemplateDTO.setCurrencySymbol(symbol);
        List<CfgVatInvoiceDTO.DetailDTO> detailDTOS = new ArrayList<>();
        for (SoB2cDetailEntity soB2cDetailEntity : soB2cDetailEntityList) {
            CfgVatInvoiceDTO.DetailDTO detailDTO = new CfgVatInvoiceDTO.DetailDTO();
            ListingInfoEntity listingInfoEntity = listingInfoEntityList.stream().filter(e ->  e.getPlatformSkuNo().equals(soB2cDetailEntity.getPlatformSkuNo())).findFirst().orElse(new ListingInfoEntity());
            detailDTO.setProductName(listingInfoEntity.getPlatformSkuName());
            detailDTO.setQty(soB2cDetailEntity.getQty());
            detailDTO.setTaxRate(cfgVatInvoiceEntity.getTaxRate());
            detailDTO.setTaxRateStr(detailDTO.getTaxRate() + "%");
            detailDTO.setPrice(soB2cDetailEntity.getPrice().divide(cfgVatInvoiceEntity.getTaxRate().add(BigDecimal.ONE),4, RoundingMode.HALF_UP));
            detailDTO.setPriceStr(symbol + detailDTO.getPrice());
            detailDTO.setTaxPrice(soB2cDetailEntity.getPrice());
            detailDTO.setTaxPriceStr(symbol + detailDTO.getTaxPrice());
            detailDTO.setTotalTaxPrice(soB2cDetailEntity.getPrice().multiply(new BigDecimal(soB2cDetailEntity.getQty())));
            detailDTO.setTotalTaxPriceStr(symbol + detailDTO.getTotalTaxPrice());
            detailDTO.setCurrencySymbol(symbol);
            detailDTOS.add(detailDTO);
        }
        invoiceTemplateDTO.setDetailDTOS(detailDTOS);
        BigDecimal shippingCost = dmpSoBillDetailEntityList.stream().map(DmpSoBillDetailEntity::getShippingPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        invoiceTemplateDTO.setShippingCost(shippingCost);
        invoiceTemplateDTO.setShippingCostStr(symbol + invoiceTemplateDTO.getShippingCost());
        BigDecimal discountAmount = dmpSoBillDetailEntityList.stream().map(DmpSoBillDetailEntity::getDiscountAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        invoiceTemplateDTO.setDiscount(discountAmount);
        invoiceTemplateDTO.setDiscountStr(symbol + invoiceTemplateDTO.getDiscount());
        BigDecimal SubtotalVatInclusive = detailDTOS.stream().map(CfgVatInvoiceDTO.DetailDTO::getTotalTaxPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        invoiceTemplateDTO.setInvoiceTotal(SubtotalVatInclusive.add(shippingCost).add(discountAmount));
        invoiceTemplateDTO.setInvoiceTotalStr(symbol + invoiceTemplateDTO.getInvoiceTotal());
        CfgVatInvoiceDTO.TotalDTO totalDTO = new CfgVatInvoiceDTO.TotalDTO();
        totalDTO.setTaxRate(cfgVatInvoiceEntity.getTaxRate());
        totalDTO.setTaxRateStr(totalDTO.getTaxRate() + "%");
        totalDTO.setItemTotal(SubtotalVatInclusive.divide(cfgVatInvoiceEntity.getTaxRate().add(BigDecimal.ONE),4, RoundingMode.HALF_UP));
        totalDTO.setItemTotalStr(symbol + totalDTO.getItemTotal());
        totalDTO.setVatTotal(invoiceTemplateDTO.getInvoiceTotal().subtract(totalDTO.getItemTotal()));
        totalDTO.setVatTotalStr(symbol +  totalDTO.getVatTotal());
        totalDTO.setCurrencySymbol(symbol);
        invoiceTemplateDTO.setTotalDTOS(totalDTO);
        try {
            String fileUrl = cfgVatInvoiceService.createVatInvoicePdf(invoiceTemplateDTO);
            invoiceInfoEntity.setBillCreateTime(LocalDateTime.now());
            invoiceInfoEntity.setFileUrl(fileUrl);
            soB2cEntity.setVatInvoiceStatus(SoB2cVatStatusEnum.WAIT_UPLOAD.getCode());
        }catch (Exception e){
            invoiceInfoEntity.setStatus(InvoiceInfoStatusEnum.INVOICE_FAILED.getCode());
            invoiceInfoEntity.setRemark(StrUtil.format("生成发票失败,{}",e.getMessage()));
            soB2cEntity.setVatInvoiceStatus(SoB2cVatStatusEnum.INVOICE_FAILED.getCode());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(List<InvoiceInfoEntity> addList, List<InvoiceDetailEntity> addDetailList) {
        this.saveBatch(addList);
        invoiceDetailService.saveBatch(addDetailList);
    }

    @Override
    public List<BatchResultDTO> batchUploadInvoice(List<String> ids) {
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        List<InvoiceInfoEntity> entities = super.listByIds(ids);
        List<String> soIds = entities.stream().map(InvoiceInfoEntity::getSoId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntityList = soB2cService.listByIds(soIds);
        List<SoB2cEntity> updateSoB2cEntityList = new ArrayList<>();
        for (InvoiceInfoEntity entity : entities) {
            if(!entity.getStatus().equals(InvoiceInfoStatusEnum.INVOICE_SUCCESS.getCode())){
                resultDTOList.add(BatchResultDTO.fail(entity.getId(),entity.getCode(),"不是已开票状态的发票不能上传发票"));
            }
            SoB2cEntity soB2cEntity = soB2cEntityList.stream().filter(e -> CharSequenceUtil.isNotBlank(entity.getSoId()) && entity.getSoId().equals(e.getId())).findFirst().orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "b2c订单"));
            try {
                amazonUploadInvoiceService.uploadInvoice(soB2cEntity,entity.getFileUrl(),entity.getCode());
                soB2cEntity.setVatInvoiceStatus(SoB2cVatStatusEnum.UPLOAD_SUCCESS.getCode());
            }catch (Exception e){
                soB2cEntity.setVatInvoiceStatus(SoB2cVatStatusEnum.UPLOAD_FAILURE.getCode());
                log.error("亚马逊上传发票失败",e);
                resultDTOList.add(BatchResultDTO.fail(entity.getId(),entity.getCode(),StrUtil.format("亚马逊上传发票失败,{}",e.getMessage())));
            }
            updateSoB2cEntityList.add(soB2cEntity);
        }
        if (CollUtil.isNotEmpty(updateSoB2cEntityList)){
            soB2cService.updateBatchById(updateSoB2cEntityList);
        }
        return resultDTOList;
    }

    @Override
    public Boolean export(InvoiceInfoDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("发票管理", EXPORT_INVOICE_INFO.getCode(), dto);
        return Boolean.TRUE;
    }

    private List<InvoiceDetailEntity> buildInvoiceDetail(List<SoB2cDetailEntity> soB2cDetailEntityList, InvoiceInfoEntity invoiceInfoEntity) {
        List<InvoiceDetailEntity> detailEntityList = new ArrayList<>();
        for (SoB2cDetailEntity soB2cDetailEntity : soB2cDetailEntityList) {
            InvoiceDetailEntity invoiceDetailEntity = new InvoiceDetailEntity();
            invoiceDetailEntity.setMainId(invoiceInfoEntity.getId());
            invoiceDetailEntity.setSkuId(soB2cDetailEntity.getSkuId());
            invoiceDetailEntity.setSkuNo(soB2cDetailEntity.getSkuNo());
            invoiceDetailEntity.setPlatformSkuNo(soB2cDetailEntity.getPlatformSkuNo());
            invoiceDetailEntity.setQty(soB2cDetailEntity.getQty());
            detailEntityList.add(invoiceDetailEntity);
        }
        return detailEntityList;
    }

    private InvoiceInfoEntity buildInvoiceEntity(CfgVatInvoiceEntity cfgVatInvoiceEntity, SoB2cEntity soB2cEntity, DmpSoBillDetailEntity dmpSoBillDetail) {
        InvoiceInfoEntity invoiceInfoEntity = new InvoiceInfoEntity();
        String businessNo = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_INV);
        invoiceInfoEntity.setId(IdWorker.getIdStr());
        invoiceInfoEntity.setCode(businessNo);
        invoiceInfoEntity.setCfgId(cfgVatInvoiceEntity.getId());
        invoiceInfoEntity.setInvoiceType(InvoiceInfoInvoiceTypeEnum.VAT.getCode());
        invoiceInfoEntity.setShopId(soB2cEntity.getShopId());
        invoiceInfoEntity.setSoId(soB2cEntity.getId());
        invoiceInfoEntity.setSoCode(soB2cEntity.getCode());
        invoiceInfoEntity.setPlatformCode(soB2cEntity.getPlatformCode());
        invoiceInfoEntity.setTemplateType(cfgVatInvoiceEntity.getTemplateType());
        //发票状态：查询亚马逊配送报告，没有则为开票中，有则为开票成功，后续生成PDF失败变更为开票失败
        if(Objects.isNull(dmpSoBillDetail)){
            invoiceInfoEntity.setStatus(InvoiceInfoStatusEnum.INVOICING.getCode());
        }else{
            invoiceInfoEntity.setStatus(InvoiceInfoStatusEnum.INVOICE_SUCCESS.getCode());
        }
        soB2cEntity.setVatInvoiceStatus(SoB2cVatStatusEnum.PENDING.getCode());
        return invoiceInfoEntity;
    }

    private void fillList(List<InvoiceInfoDTO.PagingViewDTO> records) {
        if (CollUtil.isEmpty(records)){
            return;
        }
//        List<String> skuIds = records.stream().map(InvoiceInfoDTO.PagingViewDTO::getSkuId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
//        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(skuIds);
        records.forEach(pagingViewDTO -> {
//            SkuVO skuVO = skuVOS.stream().filter(e -> CharSequenceUtil.isNotBlank(pagingViewDTO.getSkuId()) && pagingViewDTO.getSkuId().equals(e.getSkuId())).findFirst().orElse(null);
//            pagingViewDTO.setProductName(Objects.nonNull(skuVO) ? skuVO.getSkuName() : CharSequenceUtil.EMPTY);
            pagingViewDTO.setInvoiceTypeName(InvoiceInfoInvoiceTypeEnum.getName(pagingViewDTO.getCode()));
            pagingViewDTO.setTemplateTypeName(InvoiceInfoTemplateTypeEnum.getName(pagingViewDTO.getTemplateType()));
            pagingViewDTO.setStatusName(InvoiceInfoStatusEnum.getName(pagingViewDTO.getStatus()));
            pagingViewDTO.setUploadStatusName(InvoiceInfoUploadStatusEnum.getName(pagingViewDTO.getUploadStatus()));
        });
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(InvoiceInfoEntity invoiceInfoEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
