package com.erp.server.oms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
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
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.ConditionElement;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.server.rule.SpElServer;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.MathUtil;
import com.common.message.constant.DistributeKeyConstant;
import com.erp.model.dmp.entity.DmpSoBillDetailEntity;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.*;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.oms.mapper.InvoiceInfoMapper;
import com.erp.server.oms.sdk.invoice.AmazonUploadInvoiceService;
import com.erp.server.oms.sdk.invoice.NfeInvoiceService;
import com.erp.server.oms.service.*;
import com.sdk.third.tf.dto.CancelInvoiceResponseDTO;
import com.google.common.collect.Lists;
import com.sdk.third.tf.dto.NfeInvoiceDTO;
import com.sdk.third.tf.dto.ReturnInvoiceResponseDTO;
import com.xxl.job.core.context.XxlJobHelper;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_INVOICE_INFO;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_INVOICE_XML;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_INVOICE_PDF;

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
    private SoB2cReceiverService soB2cReceiverService;

    @Resource
    private ShopInfoService shopInfoService;
    @Lazy
    @Resource
    private CfgVatInvoiceService cfgVatInvoiceService;

    @Resource
    @Lazy
    private InvoiceInfoService service;

    @Resource
    private ListingInfoService listingInfoService;

    @Resource
    private AmazonUploadInvoiceService amazonUploadInvoiceService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    
    @Resource
    private com.erp.rpc.file.feign.FileFeign fileFeign;

    @Resource
    private InvoiceTaxService invoiceTaxService;

    @Lazy
    @Resource
    private CfgInvoiceSettingDetailService cfgInvoiceSettingDetailService;

    @Resource
    private InvoiceUpdateHisService invoiceUpdateHisService;

    @Resource
    private SkuMappingService skuMappingService;

    @Resource
    @Lazy
    private NfeInvoiceService nfeInvoiceService;
    @Lazy
    @Resource
    private OmsAttachmentService omsAttachmentService;

    @Resource
    @Lazy
    private CfgInvoiceSettingService cfgInvoiceSettingService;
    @Resource
    private CfgRuleInvoiceAmountService cfgRuleInvoiceAmountService;
    @Resource
    private RuleConditionService ruleConditionService;
    @Resource
    private SpElServer spElServer;

    @Resource
    @Qualifier("soB2cTabExecutorPool")
    private ExecutorService executorPool;

    @Value("${fdfs.publicUrl}")
    private String fdfsPubUrl;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
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
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "上传记录"));
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
    public PagingVO<InvoiceInfoDTO.PagingViewDTO> paging(PagingDTO<InvoiceInfoDTO.PagingParamDTO> dto, Boolean isExport) {
        InvoiceInfoDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<InvoiceInfoDTO.PagingViewDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<InvoiceInfoDTO.PagingViewDTO> pageData = baseMapper.paging(query, params);
        fillList(pageData.getRecords(), isExport);
        return new PagingVO(pageData);
    }


    @Override
    public String downloadInvoice(String id) {
        InvoiceInfoEntity entity = super.getById(id);
        if (Objects.isNull(entity)){
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "上传记录");
        }
        return entity.getFileUrl();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DistributeLocker(businessType = DistributeKeyConstant.INVOICE_INFO_KEY,keyName = "id",waiteTime = 60)
    public BatchResultDTO batchGenerateNfeInvoice(String id,Boolean isAsync) {
        log.warn("开始生成nfe发票，订单id：【{}】,是否异步：【{}】",id,isAsync);
        SoB2cEntity soB2cEntity = soB2cService.getById(id);
        if(ObjUtil.isEmpty(soB2cEntity)){
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "b2c订单");
        }
        if (SoB2cNfeStatusEnum.INVOICING.getCode().equals(soB2cEntity.getNfeInvoiceStatus())) {
            throw new ServiceException(ApiError.FIN_INVOICE_CREATING_REGENERATE_FORBIDDEN);
        }

        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listByMainIds(Collections.singletonList(id));
        if (CollUtil.isEmpty(soB2cDetailEntityList)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "b2c订单明细");
        }

        List<String> skuIdList = soB2cDetailEntityList.stream().map(SoB2cDetailEntity::getSkuId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(skuIdList);
        Map<String, String> skuNameMap = new HashMap<>();
        if (CollUtil.isNotEmpty(skuVOS)){
            skuNameMap = skuVOS.stream().collect(Collectors.toMap(SkuVO::getSkuId, SkuVO::getSkuName));
        }
        List<CfgInvoiceSettingDetailEntity> cfgInvoiceSettingDetailList = cfgInvoiceSettingDetailService.listByShopIdList(Collections.singletonList(soB2cEntity.getShopId()));
        //店铺集合
        ShopInfoEntity shopInfo = FeignQuery.getById(ShopInfoEntity.class, soB2cEntity.getShopId());
        if (ObjUtil.isEmpty(shopInfo)) {
            throw new ServiceException(ApiError.SHOP_NOT_FOUND);
        }
        //验证店铺是否管理
        if (CollUtil.isEmpty(cfgInvoiceSettingDetailList)) {
            throw new ServiceException(ApiError.SHOP_INVOICE_NOT_BIND_COMPANY,shopInfo.getName());
        }
        //已存在的记录
        List<InvoiceInfoEntity> existList = this.listBySoIds(Collections.singletonList(id));
        Map<String, List<CfgInvoiceSettingDetailEntity>> map = cfgInvoiceSettingDetailList.stream().collect(Collectors.groupingBy(CfgInvoiceSettingDetailEntity::getShopId));
        //店铺
        List<CfgInvoiceSettingDetailEntity> invoiceSettingDetailList = map.get(soB2cEntity.getShopId());
        if (CollUtil.isEmpty(invoiceSettingDetailList)) {
            throw new ServiceException(ApiError.SHOP_INVOICE_NOT_BIND_COMPANY,shopInfo.getName());
        }
        //开票中不再生成
        SoB2cEntity finalSoB2cEntity = soB2cEntity;
        InvoiceInfoEntity existInvoiceInfoEntity = existList.stream().filter(e -> e.getSoId().equals(finalSoB2cEntity.getId())
                && InvoiceNatureEnum.ORDINARY.getCode().equals(e.getInvoiceNature())
                && (e.getStatus().equals(InvoiceInfoStatusEnum.INVOICING.getCode()) || e.getStatus().equals(InvoiceInfoStatusEnum.INVOICE_SUCCESS.getCode()))).findFirst().orElse(null);
        if(Objects.nonNull(existInvoiceInfoEntity)){
            return BatchResultDTO.fail(soB2cEntity.getId(), soB2cEntity.getCode(), CharSequenceUtil.format("发票性质为【{}】已存在开票中/开票成功的发票不生成新的开票任务",InvoiceNatureEnum.ORDINARY.getName()));
        }
        InvoiceInfoEntity invoiceInfoEntity = buildNfeInvoiceEntity(soB2cEntity);
        List<InvoiceDetailEntity> detailEntityList = buildInvoiceDetail(soB2cDetailEntityList, invoiceInfoEntity,skuNameMap);
        //删除开票失败的数据
        service.removeFailedBySoId(id, Collections.singletonList(InvoiceInfoInvoiceTypeEnum.NFE.getCode()));
        //保存数据
        service.batchSave(Collections.singletonList(invoiceInfoEntity),detailEntityList);

        soB2cEntity.setNfeInvoiceStatus(SoB2cNfeStatusEnum.INVOICING.getCode());
        soB2cService.updateById(soB2cEntity);
        //异步生成发票,调用第三方
        if (isAsync){
            //异步推送mq
            CompletableFuture.supplyAsync(() -> {
                nfeInvoiceService.createInvoiceProcess(soB2cEntity,invoiceInfoEntity);
                return true;
            });
            return BatchResultDTO.success(soB2cEntity.getId(), soB2cEntity.getCode(), "执行异步生成发票,请稍后查看发票生成状态");
        }else {
            //同步生成发票,调用第三方
            try {
                Boolean result = nfeInvoiceService.createInvoiceV2(soB2cEntity);
                if (result){
                    //添加日志
                    operateLogService.addModuleOperateLog(CharSequenceUtil.format("用户【{}】销售订单【{}】生成NF-e发票【{}】",UserContext.getDefaultLoginUser().getUserName(),soB2cEntity.getCode(),invoiceInfoEntity.getCode()), ModuleTypeEnum.SO_B2C.getCode(), soB2cEntity.getId(), "生成NF-e发票操作");
                    return BatchResultDTO.success(soB2cEntity.getId(), soB2cEntity.getCode(), "生成发票成功");
                }else {
                    //添加日志
                    operateLogService.addModuleOperateLog(CharSequenceUtil.format("用户【{}】销售订单【{}】生成NF-e发票【{}】",UserContext.getDefaultLoginUser().getUserName(),soB2cEntity.getCode(),invoiceInfoEntity.getCode()), ModuleTypeEnum.SO_B2C.getCode(), soB2cEntity.getId(), "开票失败");
                    return BatchResultDTO.success(soB2cEntity.getId(), soB2cEntity.getCode(), "生成发票失败");
                }
            }catch (Exception e){
                InvoiceInfoEntity entity = this.getInvoicingBySoId(soB2cEntity.getId());
                entity.setStatus(InvoiceInfoStatusEnum.INVOICE_FAILED.getCode());
                entity.setRemark(e.getMessage());
                this.updateNfeStatusById(entity);
                operateLogService.addModuleOperateLog(e.getMessage(), ModuleTypeEnum.INVOICE_INFO.getCode(), soB2cEntity.getId(),"开票失败");
                return BatchResultDTO.fail(soB2cEntity.getId(), soB2cEntity.getCode(), "生成发票失败:" + e.getMessage());
            }
        }

    }


    /**
     * 发票主表信息
     * @author will
     * @date 2025/4/9 14:49
     * @param soB2cEntity
     * @return InvoiceInfoEntity
     */
    private InvoiceInfoEntity buildNfeInvoiceEntity(SoB2cEntity soB2cEntity) {
        InvoiceInfoEntity invoiceInfoEntity = new InvoiceInfoEntity();
        String businessNo = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_INV);
        invoiceInfoEntity.setId(IdWorker.getIdStr());
        invoiceInfoEntity.setCode(businessNo);
        invoiceInfoEntity.setInvoiceType(InvoiceInfoInvoiceTypeEnum.NFE.getCode());
        invoiceInfoEntity.setShopId(soB2cEntity.getShopId());
        invoiceInfoEntity.setSoId(soB2cEntity.getId());
        invoiceInfoEntity.setSoCode(soB2cEntity.getCode());
        invoiceInfoEntity.setPlatformCode(soB2cEntity.getPlatformCode());
        invoiceInfoEntity.setTemplateType(InvoiceInfoTemplateTypeEnum.OFFICIAL.getCode());
        invoiceInfoEntity.setBillCreateTime(LocalDateTime.now());
        //发票状态
        invoiceInfoEntity.setStatus(InvoiceInfoStatusEnum.INVOICING.getCode());
        if (CharSequenceUtil.equals(soB2cEntity.getDictPlatform(),PlatformDictEnum.ALI_EXPRESS.getCode())) {
            invoiceInfoEntity.setUploadStatus(InvoiceInfoUploadStatusEnum.NOT_NEED_UPLOAD.getCode());
        }
        return invoiceInfoEntity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> batchGenerateVatInvoice(List<String> ids) {
        if(CollectionUtils.isEmpty(ids)){
            return Collections.emptyList();
        }
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        List<SoB2cEntity> soB2cEntityList = soB2cService.listByIds(ids);
        if(CollectionUtils.isEmpty(soB2cEntityList)){
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "b2c订单");
        }
        List<String> shopIds = soB2cEntityList.stream().map(SoB2cEntity::getShopId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<CfgVatInvoiceEntity> cfgVatInvoiceEntities = cfgVatInvoiceService.listCfgByShopIds(shopIds);
        List<SoB2cDetailEntity> allSoB2cDetailEntityList = soB2cDetailService.listByMainIds(ids);
        List<String> platformSkuNoList = allSoB2cDetailEntityList.stream().map(SoB2cDetailEntity::getPlatformSkuNo).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<String> skuIdList = allSoB2cDetailEntityList.stream().map(SoB2cDetailEntity::getSkuId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(skuIdList);
        Map<String, String> skuNameMap = new HashMap<>();
        if (CollUtil.isNotEmpty(skuVOS)){
            skuNameMap = skuVOS.stream().collect(Collectors.toMap(SkuVO::getSkuId, SkuVO::getSkuName));
        }
        List<ListingInfoEntity> listingInfoEntityList = listingInfoService.listByParam(RuleTypeEnum.B2C_PLATFORM.getCode(), soB2cEntityList.get(0).getDictPlatform(), platformSkuNoList);
        List<String> platformCodeList = soB2cEntityList.stream().map(SoB2cEntity::getPlatformCode).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        //已存在的记录
        List<InvoiceInfoEntity> existList = this.listBySoIds(ids);
        //查询亚马逊财务配送报告
        List<DmpSoBillDetailEntity> allDmpSoBillDetailEntityList = FeignQuery.create(DmpSoBillDetailEntity.class).in(DmpSoBillDetailEntity::getPlatformCode,platformCodeList)
                .in(DmpSoBillDetailEntity::getShopId,shopIds)
                .eq(DmpSoBillDetailEntity::getSourcePlatform, PlatformDictEnum.AMAZON.getCode()).list();
        List<InvoiceInfoEntity> addList = new ArrayList<>();
        List<InvoiceDetailEntity> addDetailList = new ArrayList<>();
        if(CollectionUtils.isEmpty(soB2cEntityList)){
            return new ArrayList<>();
        }
        for (SoB2cEntity entity : soB2cEntityList) {
            CfgVatInvoiceEntity cfgVatInvoiceEntity = cfgVatInvoiceEntities.stream().filter(e -> CharSequenceUtil.isNotBlank(entity.getShopId()) && !e.getDisabled() && entity.getShopId().equals(e.getShopId())).findFirst().orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "发票配置"));
            SoB2cEntity soB2cEntity = soB2cEntityList.stream().filter(e -> CharSequenceUtil.isNotBlank(entity.getId()) && entity.getId().equals(e.getId())).findFirst().orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "b2c订单"));
            if (!PlatformDictEnum.AMAZON.getCode().equalsIgnoreCase(soB2cEntity.getDictPlatform()) || !soB2cEntity.hasPlatformWarehouseOrder()) {
                resultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(),soB2cEntity.getCode(),"只有亚马逊FBA订单允许生成发票"));
                continue;
            }
            if(!soB2cEntity.getBillStatus().equals(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode())){
                resultDTOList.add(BatchResultDTO.fail(soB2cEntity.getId(),soB2cEntity.getCode(),"只有已发货的订单允许生成发票"));
                continue;
            }
            //开票中不再生成
            InvoiceInfoEntity existInvoiceInfoEntity = existList.stream().filter(e -> e.getSoId().equals(soB2cEntity.getId()) && e.getStatus().equals(InvoiceInfoStatusEnum.INVOICING.getCode())).findFirst().orElse(null);
            if(Objects.nonNull(existInvoiceInfoEntity)){
                resultDTOList.add(BatchResultDTO.fail(entity.getId(),entity.getCode(),"已存在开票中的发票"));
                continue;
            }

            InvoiceInfoEntity existInvoiceUploadingInfoEntity = existList.stream().filter(e -> e.getSoId().equals(soB2cEntity.getId()) && e.getUploadStatus().equals(InvoiceInfoUploadStatusEnum.UPLOADING.getCode())).findFirst().orElse(null);
            if(Objects.nonNull(existInvoiceUploadingInfoEntity)){
                resultDTOList.add(BatchResultDTO.fail(entity.getId(),entity.getCode(),"已存在上传中的发票"));
                continue;
            }
            List<SoB2cDetailEntity> soB2cDetailEntityList = allSoB2cDetailEntityList.stream().filter(e -> CharSequenceUtil.isNotBlank(e.getMainId()) && e.getMainId().equals(soB2cEntity.getId())).collect(Collectors.toList());
            DmpSoBillDetailEntity dmpSoBillDetailEntity = allDmpSoBillDetailEntityList.stream().filter(e ->e.getShopId().equals(soB2cEntity.getShopId())&& e.getPlatformCode().equals(soB2cEntity.getPlatformCode())).findFirst().orElse(null);
            InvoiceInfoEntity invoiceInfoEntity = buildInvoiceEntity(cfgVatInvoiceEntity, soB2cEntity, dmpSoBillDetailEntity);
            List<InvoiceDetailEntity> detailEntityList = buildInvoiceDetail(soB2cDetailEntityList, invoiceInfoEntity,skuNameMap);
            addList.add(invoiceInfoEntity);
            addDetailList.addAll(detailEntityList);
        }

        List<InvoiceInfoEntity> waitCreateVoiceList = addList.stream().filter(v->v.getStatus().equals(InvoiceInfoStatusEnum.INVOICE_SUCCESS.getCode())).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(waitCreateVoiceList)){
            if(CollectionUtils.isNotEmpty(addList)){
                //保存数据
                service.batchSave(addList,addDetailList);
                soB2cService.updateBatchById(soB2cEntityList);
            }
            return resultDTOList;
        }
        //生成PDF
        addList.stream().filter(v->v.getStatus().equals(InvoiceInfoStatusEnum.INVOICE_SUCCESS.getCode())).forEach(invoiceInfoEntity -> {
            generateInvoicePdf(invoiceInfoEntity, cfgVatInvoiceEntities, soB2cEntityList, allSoB2cDetailEntityList, allDmpSoBillDetailEntityList, listingInfoEntityList);
            if(invoiceInfoEntity.getStatus().equals(InvoiceInfoStatusEnum.INVOICE_FAILED.getCode())){
                resultDTOList.add(BatchResultDTO.fail(invoiceInfoEntity.getId(),invoiceInfoEntity.getCode(),invoiceInfoEntity.getRemark()));
            }
        });
        //开票成功并且配置为自动上传的，上传发票
        addList.stream().filter(v->v.getStatus().equals(InvoiceInfoStatusEnum.INVOICE_SUCCESS.getCode())).filter(v->{
            CfgVatInvoiceEntity cfgVatInvoiceEntity = cfgVatInvoiceEntities.stream().filter(e ->  !e.getDisabled() && v.getShopId().equals(e.getShopId())).findFirst().orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "发票配置"));
            return cfgVatInvoiceEntity.getIsAutoUpload();}).forEach(invoiceInfoEntity -> {
            SoB2cEntity soB2cEntity = soB2cEntityList.stream().filter(e -> CharSequenceUtil.isNotBlank(invoiceInfoEntity.getSoId()) && invoiceInfoEntity.getSoId().equals(e.getId())).findFirst().orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "b2c订单"));
            try {
                String feedId = amazonUploadInvoiceService.uploadInvoice(soB2cEntity,invoiceInfoEntity.getFileUrl(),invoiceInfoEntity.getCode());
                invoiceInfoEntity.setUploadTime(LocalDateTime.now());
                invoiceInfoEntity.setQueryId(feedId);
                invoiceInfoEntity.setUploadStatus(InvoiceInfoUploadStatusEnum.UPLOADING.getCode());
            }catch (Exception e){
                invoiceInfoEntity.setUploadStatus(InvoiceInfoUploadStatusEnum.UPLOAD_FAILED.getCode());
                invoiceInfoEntity.setRemark(StrUtil.format("上传发票失败",e.getMessage()));
                log.error("亚马逊上传发票失败",e);
                resultDTOList.add(BatchResultDTO.fail(invoiceInfoEntity.getId(),invoiceInfoEntity.getCode(),e.getMessage()));
            }
        });
        //保存数据
        service.batchSave(addList,addDetailList);
        soB2cService.updateBatchById(soB2cEntityList);
        return resultDTOList;
    }

    private void generateInvoicePdf(InvoiceInfoEntity invoiceInfoEntity, List<CfgVatInvoiceEntity> cfgVatInvoiceEntities, List<SoB2cEntity> soB2cEntityList, List<SoB2cDetailEntity> allSoB2cDetailEntityList, List<DmpSoBillDetailEntity> allDmpSoBillDetailEntityList, List<ListingInfoEntity> listingInfoEntityList) {
        CfgVatInvoiceEntity cfgVatInvoiceEntity = cfgVatInvoiceEntities.stream().filter(e ->  !e.getDisabled() && invoiceInfoEntity.getShopId().equals(e.getShopId())).findFirst().orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "发票配置"));
        SoB2cEntity soB2cEntity = soB2cEntityList.stream().filter(e -> invoiceInfoEntity.getSoId().equals(e.getId())).findFirst().orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "b2c订单"));
        //增加发票模板对接校验
        if (!CfgVatInvoiceTemplateTypeEnum.ERP.getCode().equals(cfgVatInvoiceEntity.getTemplateType())){
            invoiceInfoEntity.setStatus(InvoiceInfoStatusEnum.INVOICE_FAILED.getCode());
            invoiceInfoEntity.setRemark(StrUtil.format("生成发票失败,未对接{}",CfgVatInvoiceTemplateTypeEnum.getName(cfgVatInvoiceEntity.getTemplateType())));
            soB2cEntity.setVatInvoiceStatus(SoB2cVatStatusEnum.INVOICE_FAILED.getCode());
            return;
        }
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
        invoiceTemplateDTO.setPlatformCode("#" + soB2cEntity.getPlatformCode());
        invoiceTemplateDTO.setCurrencyCode(soB2cEntity.getCurrency());
        String symbol = CurrencyEnum.getSymbolByCode(soB2cEntity.getCurrency());
        invoiceTemplateDTO.setCurrencySymbol(symbol);
        BigDecimal taxRate = cfgVatInvoiceEntity.getTaxRate().divide(MathUtil.BigDecimal_100, 2, BigDecimal.ROUND_DOWN);
        List<CfgVatInvoiceDTO.DetailDTO> detailDTOS = new ArrayList<>();
        for (SoB2cDetailEntity soB2cDetailEntity : soB2cDetailEntityList) {
            CfgVatInvoiceDTO.DetailDTO detailDTO = new CfgVatInvoiceDTO.DetailDTO();
            ListingInfoEntity listingInfoEntity = listingInfoEntityList.stream().filter(e ->  e.getPlatformSkuNo().equals(soB2cDetailEntity.getPlatformSkuNo())).findFirst().orElse(new ListingInfoEntity());
            detailDTO.setProductName(listingInfoEntity.getPlatformSkuName());
            detailDTO.setQty(soB2cDetailEntity.getQty());
            detailDTO.setTaxRate(cfgVatInvoiceEntity.getTaxRate());
            detailDTO.setTaxRateStr(detailDTO.getTaxRate().setScale(2,BigDecimal.ROUND_DOWN) + "%");
            detailDTO.setPrice(soB2cDetailEntity.getPrice().divide(taxRate.add(BigDecimal.ONE),2, BigDecimal.ROUND_DOWN));
            detailDTO.setPriceStr(symbol + detailDTO.getPrice().setScale(2,BigDecimal.ROUND_DOWN));
            detailDTO.setTaxPrice(soB2cDetailEntity.getPrice());
            detailDTO.setTaxPriceStr(symbol + detailDTO.getTaxPrice().setScale(2,BigDecimal.ROUND_DOWN));
            detailDTO.setTotalTaxPrice(soB2cDetailEntity.getPrice().multiply(new BigDecimal(soB2cDetailEntity.getQty())));
            detailDTO.setTotalTaxPriceStr(symbol + detailDTO.getTotalTaxPrice().setScale(2,BigDecimal.ROUND_DOWN));
            detailDTO.setCurrencySymbol(symbol);
            detailDTOS.add(detailDTO);
        }
        invoiceTemplateDTO.setDetailDTOS(detailDTOS);
        BigDecimal shippingCost = dmpSoBillDetailEntityList.stream().map(DmpSoBillDetailEntity::getShippingPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        invoiceTemplateDTO.setShippingCost(shippingCost);
        invoiceTemplateDTO.setShippingCostStr(symbol + invoiceTemplateDTO.getShippingCost().setScale(2,BigDecimal.ROUND_DOWN));
        BigDecimal discountAmount = dmpSoBillDetailEntityList.stream().map(v->v.getDiscountAmount().add(v.getShippingDiscount())).reduce(BigDecimal.ZERO, BigDecimal::add);
        invoiceTemplateDTO.setDiscount(discountAmount);
        invoiceTemplateDTO.setDiscountStr(symbol + invoiceTemplateDTO.getDiscount().setScale(2,BigDecimal.ROUND_DOWN));
        BigDecimal SubtotalVatInclusive = detailDTOS.stream().map(CfgVatInvoiceDTO.DetailDTO::getTotalTaxPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        invoiceTemplateDTO.setInvoiceTotal(SubtotalVatInclusive.add(shippingCost).add(discountAmount));
        invoiceTemplateDTO.setInvoiceTotalStr(symbol + invoiceTemplateDTO.getInvoiceTotal().setScale(2,BigDecimal.ROUND_DOWN));
        CfgVatInvoiceDTO.TotalDTO totalDTO = new CfgVatInvoiceDTO.TotalDTO();
        totalDTO.setTaxRate(cfgVatInvoiceEntity.getTaxRate());
        totalDTO.setTaxRateStr(totalDTO.getTaxRate().setScale(2,BigDecimal.ROUND_DOWN) + "%");
        totalDTO.setItemTotal(invoiceTemplateDTO.getInvoiceTotal().divide(taxRate.add(BigDecimal.ONE),2, BigDecimal.ROUND_DOWN));
        totalDTO.setItemTotalStr(symbol + totalDTO.getItemTotal().setScale(2,BigDecimal.ROUND_DOWN));
        totalDTO.setVatTotal(invoiceTemplateDTO.getInvoiceTotal().subtract(totalDTO.getItemTotal()));
        totalDTO.setVatTotalStr(symbol +  totalDTO.getVatTotal().setScale(2,BigDecimal.ROUND_DOWN));
        totalDTO.setCurrencySymbol(symbol);
        invoiceTemplateDTO.setTotalDTOS(totalDTO);
        try {
            String fileUrl = cfgVatInvoiceService.createVatInvoicePdf(invoiceTemplateDTO);
            invoiceInfoEntity.setBillCreateTime(LocalDateTime.now());
            invoiceInfoEntity.setFileUrl(fileUrl);
            invoiceInfoEntity.setStatus(InvoiceInfoStatusEnum.INVOICE_SUCCESS.getCode());
            invoiceInfoEntity.setUploadStatus(InvoiceInfoUploadStatusEnum.WAIT_UPLOAD.getCode());
            soB2cEntity.setVatInvoiceStatus(SoB2cVatStatusEnum.WAIT_UPLOAD.getCode());
        }catch (Exception e){
            log.error("生成发票失败",e);
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
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO batchUploadInvoice(String id) {
        InvoiceInfoEntity entity = super.getById(id);
        SoB2cEntity soB2cEntity = soB2cService.getById(entity.getSoId());
        if(!entity.getStatus().equals(InvoiceInfoStatusEnum.INVOICE_SUCCESS.getCode())){
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),"不是已开票状态的发票不能上传发票");
        }
        if(entity.getUploadStatus().equals(InvoiceInfoUploadStatusEnum.UPLOADING.getCode())){
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),"上传中不能上传发票");
        }
        //VAT发票
        if (InvoiceInfoInvoiceTypeEnum.VAT.getCode().equals(entity.getInvoiceType())) {
            try {
                //VAT发票
                uploadVatInvoice(entity,soB2cEntity);
            } catch (Exception e){
                entity.setUploadStatus(InvoiceInfoUploadStatusEnum.UPLOAD_FAILED.getCode());
                soB2cEntity.setVatInvoiceStatus(SoB2cVatStatusEnum.UPLOAD_FAILURE.getCode());
                return BatchResultDTO.fail(entity.getId(),entity.getCode(),StrUtil.format("上传发票失败,{}",e.getMessage()));
            }
            service.updateById(entity);
            soB2cService.updateById(soB2cEntity);
        } else if (InvoiceInfoInvoiceTypeEnum.NFE.getCode().equals(entity.getInvoiceType())) {
           return uploadNfeInvoice(soB2cEntity, id);
        }
        return BatchResultDTO.fail(entity.getId(),entity.getCode(),"上传发票成功");
    }

    /**
     * 上传Vat发票
     * @date 2025/4/14 19:09
     * @param entity
     * @param soB2cEntity
     * @return void
     */
    private void uploadVatInvoice (InvoiceInfoEntity entity,SoB2cEntity soB2cEntity) throws Exception {
        //VAT发票
        String feedId = amazonUploadInvoiceService.uploadInvoice(soB2cEntity, entity.getFileUrl(), entity.getCode());
        entity.setQueryId(feedId);
        entity.setUploadStatus(InvoiceInfoUploadStatusEnum.UPLOADING.getCode());
        entity.setUploadTime(LocalDateTime.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO uploadNfeInvoice (SoB2cEntity soB2cEntity,String invoiceId) {
        InvoiceInfoEntity invoiceInfoEntity = this.getById(invoiceId);
        if (ObjUtil.isEmpty(invoiceInfoEntity)) {
            throw new ServiceException(ApiError.FIN_INVOICE_NOT_FOUND);
        }
        if (InvoiceInfoUploadStatusEnum.NOT_NEED_UPLOAD.getCode().equals(invoiceInfoEntity.getUploadStatus())) {
            throw new ServiceException("发票无需上传");
        }
        //上传nfe
        String uploadStatus = InvoiceInfoUploadStatusEnum.UPLOAD_SUCCESS.getCode();
        try  {
            nfeInvoiceService.uploadNfeInvoice(soB2cEntity);
        } catch (Exception e) {
            log.error("上传文件失败，返回信息{}", e.getMessage());
            uploadStatus = InvoiceInfoUploadStatusEnum.UPLOAD_FAILED.getCode();
        }
        //更新上传状态
        this.updateInvoiceUploadStatus(invoiceId,uploadStatus);
        //更新b2c上传状态
        soB2cService.updateNfeInvoiceStatus(soB2cEntity.getId(), uploadStatus);

        return BatchResultDTO.fail(invoiceInfoEntity.getId(),invoiceInfoEntity.getCode(),InvoiceInfoUploadStatusEnum.UPLOAD_SUCCESS.getCode().equals(uploadStatus) ? "上传发票成功" : "上传发票失败");
    }

    /**
     * 更新上传状态
     * @author will
     * @date 2025/4/21 10:36
     * @param id
     * @param uploadStatus
     * @return void
     */
    private void updateInvoiceUploadStatus (String id,String uploadStatus) {
        lambdaUpdate().eq(InvoiceInfoEntity::getId,id)
                .set(InvoiceInfoEntity::getUploadStatus,uploadStatus)
                .set(InvoiceInfoEntity::getUploadTime,LocalDateTime.now())
                .update();
    }

    @Override
    public Boolean export(InvoiceInfoDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("发票管理", EXPORT_INVOICE_INFO.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public List<InvoiceInfoEntity> listBySoIds(List<String> soIds) {
        if(CollectionUtils.isEmpty(soIds)){
            return Collections.emptyList();
        }
        return lambdaQuery().in(InvoiceInfoEntity::getSoId,soIds).list();
    }

    @Override
    public void retryInvoice(String jobParam) {
        JSONObject jsonObject = JSONObject.parseObject(jobParam);
        List<InvoiceInfoEntity> allInvoiceInfoEntityList;
        if(Objects.nonNull(jsonObject) && Objects.nonNull(jsonObject.getBoolean("queryHistory")) && jsonObject.getBoolean("queryHistory")){
            allInvoiceInfoEntityList = lambdaQuery().eq(InvoiceInfoEntity::getStatus, InvoiceInfoStatusEnum.INVOICING.getCode()).list();

        }else{
            //查三个月内开票中的发票
            LocalDateTime queryTime = LocalDateTime.now().minusMonths(3);
            allInvoiceInfoEntityList = lambdaQuery().eq(InvoiceInfoEntity::getStatus, InvoiceInfoStatusEnum.INVOICING.getCode()).ge(InvoiceInfoEntity::getCreateTime,queryTime).list();
        }
        if(CollectionUtils.isEmpty(allInvoiceInfoEntityList)){
            XxlJobHelper.log("没有需要重试的开票中的发票");
            log.warn("没有需要重试的开票中的发票");
            return;
        }
        List<List<InvoiceInfoEntity>> partitionedList = Lists.partition(allInvoiceInfoEntityList, 1000);
        for (List<InvoiceInfoEntity> invoiceInfoEntityList : partitionedList) {
            List<String> soIds = invoiceInfoEntityList.stream().map(InvoiceInfoEntity::getSoId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
            List<SoB2cEntity> soB2cEntityList = soB2cService.listByIds(soIds);
            List<String> shopIds = soB2cEntityList.stream().map(SoB2cEntity::getShopId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
            List<String> platformCodeList = soB2cEntityList.stream().map(SoB2cEntity::getPlatformCode).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
            List<CfgVatInvoiceEntity> cfgVatInvoiceEntities = cfgVatInvoiceService.listCfgByShopIds(shopIds);
            List<SoB2cDetailEntity> allSoB2cDetailEntityList = soB2cDetailService.listByMainIds(soIds);
            List<String> platformSkuNoList = allSoB2cDetailEntityList.stream().map(SoB2cDetailEntity::getPlatformSkuNo).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
            List<ListingInfoEntity> listingInfoEntityList = listingInfoService.listByParam(RuleTypeEnum.B2C_PLATFORM.getCode(), soB2cEntityList.get(0).getDictPlatform(), platformSkuNoList);

            //查询亚马逊财务配送报告
            List<DmpSoBillDetailEntity> allDmpSoBillDetailEntityList = FeignQuery.create(DmpSoBillDetailEntity.class).in(DmpSoBillDetailEntity::getPlatformCode,platformCodeList)
                    .in(DmpSoBillDetailEntity::getShopId,shopIds)
                    .eq(DmpSoBillDetailEntity::getSourcePlatform, PlatformDictEnum.AMAZON.getCode()).list();
            List<InvoiceInfoEntity> updateList = new ArrayList<>();
            for (InvoiceInfoEntity invoiceInfoEntity : invoiceInfoEntityList) {
                DmpSoBillDetailEntity dmpSoBillDetailEntity = allDmpSoBillDetailEntityList.stream().filter(e ->e.getShopId().equals(invoiceInfoEntity.getShopId())&& e.getPlatformCode().equals(invoiceInfoEntity.getPlatformCode())).findFirst().orElse(null);
                if(Objects.isNull(dmpSoBillDetailEntity)){
                    log.warn("{}亚马逊财务配送报告不存在",invoiceInfoEntity.getCode());
                    continue;
                }
                generateInvoicePdf(invoiceInfoEntity, cfgVatInvoiceEntities, soB2cEntityList, allSoB2cDetailEntityList, allDmpSoBillDetailEntityList, listingInfoEntityList);
                updateList.add(invoiceInfoEntity);
            }
            this.autoUploadInvoice(updateList, cfgVatInvoiceEntities, soB2cEntityList, new ArrayList<>());
            service.updateBatchById(updateList);
            soB2cService.updateBatchById(soB2cEntityList);
        }
    }

    /**
     * 查询上传中的发票最新状态
     */
    @Override
    public void queryUploadingInvoice() throws Exception {
        List<InvoiceInfoEntity> invoiceInfoEntityList = lambdaQuery().eq(InvoiceInfoEntity::getInvoiceType,InvoiceInfoInvoiceTypeEnum.VAT.getCode()).eq(InvoiceInfoEntity::getUploadStatus, InvoiceInfoUploadStatusEnum.UPLOADING.getCode()).list();
        invoiceInfoEntityList = invoiceInfoEntityList.stream().filter(e -> CharSequenceUtil.isNotBlank(e.getQueryId()) && CharSequenceUtil.isNotBlank(e.getShopId())).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(invoiceInfoEntityList)){
            return;
        }
        invoiceInfoEntityList = invoiceInfoEntityList.stream()
                .limit(30)
                .collect(Collectors.toList());
        List<String> soIds = invoiceInfoEntityList.stream().map(InvoiceInfoEntity::getSoId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntityList = soB2cService.listByIds(soIds);
        List<SoB2cEntity> updateSoB2cList = new ArrayList<>();
        List<InvoiceInfoEntity> updateList = new ArrayList<>();
        for (InvoiceInfoEntity invoiceInfoEntity : invoiceInfoEntityList) {
            SoB2cEntity soB2cEntity = soB2cEntityList.stream().filter(e -> CharSequenceUtil.isNotBlank(invoiceInfoEntity.getSoId()) && invoiceInfoEntity.getSoId().equals(e.getId())).findFirst().orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "b2c订单"));
            try {
                ApiResult<Object> result = amazonUploadInvoiceService.getInvoiceResult(invoiceInfoEntity.getQueryId(),invoiceInfoEntity.getShopId());
                if(result.isSuccess()){
                    invoiceInfoEntity.setUploadStatus(InvoiceInfoUploadStatusEnum.UPLOAD_SUCCESS.getCode());
                    invoiceInfoEntity.setRemark("");
                    soB2cEntity.setVatInvoiceStatus(SoB2cVatStatusEnum.UPLOAD_SUCCESS.getCode());
                    updateSoB2cList.add(soB2cEntity);
                    updateList.add(invoiceInfoEntity);
                }else if(!result.getCode().equals(300)){
                    if(result.getMsg().contains("There is already a document with same invoice number")){
                        invoiceInfoEntity.setUploadStatus(InvoiceInfoUploadStatusEnum.UPLOAD_SUCCESS.getCode());
                        invoiceInfoEntity.setRemark("");
                        soB2cEntity.setVatInvoiceStatus(SoB2cVatStatusEnum.UPLOAD_SUCCESS.getCode());
                        updateSoB2cList.add(soB2cEntity);
                        updateList.add(invoiceInfoEntity);
                    }else{
                        invoiceInfoEntity.setUploadStatus(InvoiceInfoUploadStatusEnum.UPLOAD_FAILED.getCode());
                        invoiceInfoEntity.setQueryResult(result.getMsg());
                        invoiceInfoEntity.setRemark("上传发票失败"+result.getMsg());
                        soB2cEntity.setVatInvoiceStatus(SoB2cVatStatusEnum.UPLOAD_FAILURE.getCode());
                        updateSoB2cList.add(soB2cEntity);
                        updateList.add(invoiceInfoEntity);
                    }
                }
            }catch (Exception e){
                //应该是亚马逊异常
                log.error("{}亚马逊查询发票异常",soB2cEntity.getCode(),e);
                invoiceInfoEntity.setUploadStatus(InvoiceInfoUploadStatusEnum.UPLOADING.getCode());
                invoiceInfoEntity.setQueryResult("亚马逊查询发票结果异常"+e.getMessage());
                invoiceInfoEntity.setRemark("亚马逊查询发票结果异常"+e.getMessage());
                updateList.add(invoiceInfoEntity);
            }finally {
                TimeUnit.SECONDS.sleep(90);
            }
        }
        if (CollectionUtils.isNotEmpty(updateList)){
            service.updateBatchById(updateList);
        }
        if(CollectionUtils.isNotEmpty(updateSoB2cList)){
            soB2cService.updateBatchById(updateSoB2cList);
        }
    }

    @Override
    public BatchResultDTO cancelInvoice(String id,String remark) {
        InvoiceInfoEntity invoiceInfoEntity = this.getById(id);
        if (ObjUtil.isEmpty(invoiceInfoEntity)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "开票清单");
        }
        //仅nf-e发票支持操作
        if (!CharSequenceUtil.equals(InvoiceInfoInvoiceTypeEnum.NFE.getCode(), invoiceInfoEntity.getInvoiceType())) {
            throw new ServiceException(ApiError.FIN_INVOICE_NFE_ONLY_SUPPORTED);
        }
        //开票成功
        if (!InvoiceInfoStatusEnum.INVOICE_SUCCESS.getCode().equals(invoiceInfoEntity.getStatus())) {
            throw new ServiceException(ApiError.FIN_INVOICE_OPERATION_NOT_ALLOWED);
        }

        //取消发票,调用第三方（使用新接口V2）
        try {
            CancelInvoiceResponseDTO.CancelInvoiceDataDTO responseData = nfeInvoiceService.cancelInvoiceV2(invoiceInfoEntity, remark);
            // cancelInvoiceV2方法内部已经更新了invoiceNature和cancelReason，以及上传了新的XML
            // 如果上传平台失败，会在remark中记录失败原因
            return BatchResultDTO.success(id, invoiceInfoEntity.getCode(), "取消发票成功");
        } catch (Exception e) {
            // 取消失败时，记录失败原因
            invoiceInfoEntity.setRemark(CharSequenceUtil.format("取消发票失败: {}", e.getMessage()));
            this.updateById(invoiceInfoEntity);
            return BatchResultDTO.fail(id, invoiceInfoEntity.getCode(), CharSequenceUtil.format("取消发票失败: {}", e.getMessage()));
        }
    }

    @Override
    public BatchResultDTO returnInvoice(String id,String remark,String returnTaxCode) {
        InvoiceInfoEntity invoiceInfoEntity = this.getById(id);
        if (ObjUtil.isEmpty(invoiceInfoEntity)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "开票清单");
        }
        //仅nf-e发票支持操作
        if (!CharSequenceUtil.equals(InvoiceInfoInvoiceTypeEnum.NFE.getCode(), invoiceInfoEntity.getInvoiceType())) {
            throw new ServiceException(ApiError.FIN_INVOICE_NFE_ONLY_SUPPORTED);
        }
        //开票成功
        if (!InvoiceInfoStatusEnum.INVOICE_SUCCESS.getCode().equals(invoiceInfoEntity.getStatus())) {
            throw new ServiceException(ApiError.FIN_INVOICE_OPERATION_NOT_ALLOWED);
        }
        //退货发票,调用第三方（使用新接口V2）
        try {
            ReturnInvoiceResponseDTO.ReturnInvoiceDataDTO responseData = nfeInvoiceService.returnInvoiceV2(invoiceInfoEntity, remark, returnTaxCode);
            // 退货成功，更新发票性质为退货
            invoiceInfoEntity.setCancelReason(remark);
            invoiceInfoEntity.setInvoiceNature(InvoiceNatureEnum.RETURN_INVOICE.getCode());
            invoiceInfoEntity.setReturnTaxCode(returnTaxCode);
            // 更新退货发票的UUID和chave
            if (CharSequenceUtil.isNotBlank(responseData.getUuid())) {
                invoiceInfoEntity.setQueryId(responseData.getUuid());
            }
            if (CharSequenceUtil.isNotBlank(responseData.getChave())) {
                invoiceInfoEntity.setQueryKey(responseData.getChave());
                invoiceInfoEntity.setPlatformInvoiceNo(responseData.getChave());
            }
            // 如果返回了新的XML，已经在上传方法中处理
            this.updateById(invoiceInfoEntity);
            return BatchResultDTO.success(id, invoiceInfoEntity.getCode(), "退货发票成功");
        } catch (Exception e) {
            // 退货失败时，记录失败原因
            invoiceInfoEntity.setRemark(CharSequenceUtil.format("退货发票失败: {}", e.getMessage()));
            this.updateById(invoiceInfoEntity);
            return BatchResultDTO.fail(id, invoiceInfoEntity.getCode(), CharSequenceUtil.format("退货发票失败: {}", e.getMessage()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO notNeedInvoice(String soId, String remark,String invoiceType) {
        SoB2cEntity soB2cEntity = soB2cService.getById(soId);
        if (ObjUtil.isEmpty(soB2cEntity)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "销售订单");
        }
        if(invoiceType.equals(InvoiceInfoInvoiceTypeEnum.NFE.getCode())) {
            //无需开票
            if (!SoB2cNfeStatusEnum.PENDING.getCode().equals(soB2cEntity.getNfeInvoiceStatus()) && !SoB2cNfeStatusEnum.INVOICE_FAILURE.getCode().equals(soB2cEntity.getNfeInvoiceStatus())) {
                throw new ServiceException(ApiError.FIN_INVOICE_NOT_REQUIRED_ONLY_PENDING_OR_FAILED);
            }
        }
        if(invoiceType.equals(InvoiceInfoInvoiceTypeEnum.VAT.getCode())) {
            //无需开票
            if (!SoB2cNfeStatusEnum.PENDING.getCode().equals(soB2cEntity.getVatInvoiceStatus()) && !SoB2cNfeStatusEnum.INVOICE_FAILURE.getCode().equals(soB2cEntity.getVatInvoiceStatus())) {
                throw new ServiceException(ApiError.FIN_INVOICE_NOT_REQUIRED_ONLY_PENDING_OR_FAILED);
            }
        }
        if (InvoiceInfoInvoiceTypeEnum.NFE.getCode().equals(invoiceType)) {
            soB2cEntity.setNfeInvoiceStatus(SoB2cNfeStatusEnum.NOT_NEED_INVOICE.getCode());
        } else if (InvoiceInfoInvoiceTypeEnum.VAT.getCode().equals(invoiceType)) {
            soB2cEntity.setVatInvoiceStatus(SoB2cVatStatusEnum.NOT_NEED_INVOICE.getCode());
        } else {
            throw new ServiceException(ApiError.FIN_INVOICE_NFE_ONLY_SUPPORTED);
        }
        soB2cService.updateById(soB2cEntity);

        //删除开票失败的开票清单
        service.removeFailedBySoId(soId,Arrays.asList(InvoiceInfoInvoiceTypeEnum.VAT.getCode(),InvoiceInfoInvoiceTypeEnum.NFE.getCode()));

        //添加日志
        operateLogService.addModuleOperateLog(CharSequenceUtil.format("销售订单【{}】{}无需开票",soB2cEntity.getCode(),InvoiceInfoInvoiceTypeEnum.getName(invoiceType)), ModuleTypeEnum.SO_B2C.getCode(), soB2cEntity.getId(), "无需开票操作");
        return BatchResultDTO.success(soId, soB2cEntity.getCode(), "无需开票成功");
    }
    
    /**
     * 根据销售订单删除
     * @author will 
     * @date 2025/4/9 15:45
     * @param soId 
     * @return void
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFailedBySoId (String soId,List<String> invoiceTypeList) {
        InvoiceInfoEntity invoiceInfoEntity = this.getFailedBySoId(soId,invoiceTypeList);
        if (ObjUtil.isEmpty(invoiceInfoEntity)) {
            return;
        }
        //删除明细信息
        invoiceDetailService.removeByMainId(invoiceInfoEntity.getId());
        //删除主表信息
        this.removeById(invoiceInfoEntity.getId());
    }

    @Override
    public InvoiceInfoEntity getInvoicingBySoId(String id) {
        return lambdaQuery().eq(InvoiceInfoEntity::getSoId,id).eq(InvoiceInfoEntity::getStatus,InvoiceInfoStatusEnum.INVOICING.getCode()).last("limit 1").one();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNfeStatusById(InvoiceInfoEntity invoiceInfoEntity) {
        //更新销售订单nfe发票状态
        String nfeInvoiceStatus = SoB2cNfeStatusEnum.WAIT_UPLOAD.getCode();
        if (InvoiceInfoStatusEnum.INVOICE_FAILED.getCode().equals(invoiceInfoEntity.getStatus())) {
            nfeInvoiceStatus = SoB2cNfeStatusEnum.INVOICE_FAILURE.getCode();
        } else if (InvoiceInfoUploadStatusEnum.UPLOAD_FAILED.getCode().equals(invoiceInfoEntity.getUploadStatus())) {
            nfeInvoiceStatus = SoB2cNfeStatusEnum.UPLOAD_FAILURE.getCode();
        } else if (InvoiceInfoUploadStatusEnum.UPLOAD_SUCCESS.getCode().equals(invoiceInfoEntity.getUploadStatus())) {
            nfeInvoiceStatus = SoB2cNfeStatusEnum.UPLOAD_SUCCESS.getCode();
        } else if (InvoiceInfoUploadStatusEnum.NOT_NEED_UPLOAD.getCode().equals(invoiceInfoEntity.getUploadStatus())) {
            nfeInvoiceStatus = SoB2cNfeStatusEnum.NOT_NEED_UPLOAD.getCode();
        }
        soB2cService.updateNfeInvoiceStatus(invoiceInfoEntity.getSoId(),nfeInvoiceStatus);
        soB2cReceiverService.updateInvoiceAddress(invoiceInfoEntity.getSoId(),invoiceInfoEntity.getInvoiceAddress());
        //更新开票清单数据
        this.updateById(invoiceInfoEntity);
    }

    @Override
    public InvoiceInfoDTO.AttachDTO getNewInvoicedAttachBySoId(String soId,String invoiceType,String attachmentType) {
        return baseMapper.getNewInvoicedAttachBySoId(soId,invoiceType,attachmentType);
    }

    @Override
    public void initNfeInvoiceKey() {
        //查询符合条件数据
        List<InvoiceInfoEntity> invoiceInfoEntityList = lambdaQuery()
                .eq(InvoiceInfoEntity::getInvoiceType,InvoiceInfoInvoiceTypeEnum.NFE.getCode())
                .eq(InvoiceInfoEntity::getStatus,InvoiceInfoStatusEnum.INVOICE_SUCCESS.getCode())
                .eq(InvoiceInfoEntity::getQueryKey,CharSequenceUtil.EMPTY)
                .list();
        List<List<InvoiceInfoEntity>> partition = ListUtil.partition(invoiceInfoEntityList, 100);
        for (List<InvoiceInfoEntity> invoiceInfoEntities : partition) {
            List<String> ids = invoiceInfoEntities.stream().map(InvoiceInfoEntity::getId).collect(Collectors.toList());
            List<OmsAttachmentDTO.UpdateDTO> updateDTOList = omsAttachmentService.getByBusinessIds(ids);
            if (CollUtil.isEmpty(updateDTOList)){
                continue;
            }
            for (InvoiceInfoEntity entity : invoiceInfoEntities){
                OmsAttachmentDTO.UpdateDTO updateDTO = updateDTOList.stream().filter(e -> AttachmentTypeEnum.INVOICE_INFO_XML.getCode().equals(e.getType()) && Objects.equals(entity.getId(), e.getBusinessId())).findFirst().orElse(null);
                if (Objects.isNull(updateDTO)){
                    continue;
                }
                String url = FastDFSClientUtil.publicUrl + updateDTO.getAttachUrl();
                String queryKey = nfeInvoiceService.getQueryKey(url);
                if (CharSequenceUtil.isNotBlank(queryKey)){
                    this.lambdaUpdate().eq(InvoiceInfoEntity::getId,entity.getId()).set(InvoiceInfoEntity::getQueryKey,queryKey).update();
                }
            }
        }
    }

    @Override
    public InvoiceInfoDTO.ProductAmountRuleResultDTO productAmountRule(SoB2cEntity soB2cEntity) {
        InvoiceInfoDTO.ProductAmountRuleResultDTO ruleResultDTO = new InvoiceInfoDTO.ProductAmountRuleResultDTO();
        if (CharSequenceUtil.isBlank(soB2cEntity.getShopId()) || CharSequenceUtil.isBlank(soB2cEntity.getDictPlatform())){
            ruleResultDTO.setIsMatch(Boolean.FALSE);
            ruleResultDTO.setMsg("店铺或平台类型为空");
            return ruleResultDTO;
        }
        List<CfgInvoiceSettingDetailEntity> detailEntityList = cfgInvoiceSettingDetailService.listInvoiceSettingDetail(soB2cEntity.getDictPlatform(), soB2cEntity.getShopId());
        if (CollUtil.isEmpty(detailEntityList)){
            ruleResultDTO.setIsMatch(Boolean.FALSE);
            ruleResultDTO.setMsg("无可以匹配的发票规则");
            return ruleResultDTO;
        }
        //构建匹配参数
        Map<String, Object> map = handleMatchJson(soB2cEntity);

        List<String> cfgIds = detailEntityList.stream().map(CfgInvoiceSettingDetailEntity::getMainId).distinct().collect(Collectors.toList());
        List<CfgRuleInvoiceAmountEntity> ruleInvoiceProductAmountEntityList = cfgRuleInvoiceAmountService.listRuleByPriority(cfgIds);
        List<String> ruleIdList = ruleInvoiceProductAmountEntityList.stream().map(CfgRuleInvoiceAmountEntity::getId).collect(Collectors.toList());
        //规则条件
        List<RuleConditionEntity> allRuleConditionList = ruleConditionService.listDbRuleIds(ruleIdList);
        for (CfgRuleInvoiceAmountEntity ruleInvoiceProductAmountEntity : ruleInvoiceProductAmountEntityList) {
            String ruleId = ruleInvoiceProductAmountEntity.getId();
            List<RuleConditionEntity> ruleConditionList = allRuleConditionList.stream().
                    filter(r -> r.getRuleId().equals(ruleId)).
                    sorted(Comparator.comparing(RuleConditionEntity::getIndex)).collect(Collectors.toList());
            if (CollUtil.isEmpty(ruleConditionList)){
                continue;
            }
            List<ConditionElement> conditionElementList = BeanMapper.copyList(ruleConditionList, ConditionElement.class);
            //获取到表达式
            Boolean matchResult = spElServer.matchExpressionByConditionList(conditionElementList, map, "");
            if (matchResult){
                ruleResultDTO.setIsMatch(Boolean.TRUE);
                CfgInvoiceSettingDetailEntity cfgInvoiceSettingDetailEntity = detailEntityList.stream().filter(e -> e.getMainId().equals(ruleInvoiceProductAmountEntity.getCfgId())).findFirst().orElse(null);
                ruleResultDTO.setInvoiceSettingDetail(cfgInvoiceSettingDetailEntity);
                ruleResultDTO.setInvoiceSetting(cfgInvoiceSettingService.getById(cfgInvoiceSettingDetailEntity.getMainId()));
                ruleResultDTO.setDictInvoiceRule(ruleInvoiceProductAmountEntity.getDictInvoiceRule());
                ruleResultDTO.setRatio(ruleInvoiceProductAmountEntity.getRatio());
                return ruleResultDTO;
            }
        }
        ruleResultDTO.setIsMatch(Boolean.FALSE);
        ruleResultDTO.setMsg("未匹配到符合条件规则");
        return ruleResultDTO;
    }

    @Override
    public InvoiceInfoEntity findLatestInvoice(String soId) {
        if (CharSequenceUtil.isBlank(soId)){
            return null;
        }
        List<InvoiceInfoEntity> list = lambdaQuery().eq(InvoiceInfoEntity::getSoId, soId)
                .orderByDesc(InvoiceInfoEntity::getCreateTime).list();
        return CollUtil.isEmpty(list) ? null : list.get(0);
    }

    private Map<String, Object> handleMatchJson(SoB2cEntity soB2cEntity) {
        Map<String, Object> map = new HashMap<>();
        if (Objects.isNull(soB2cEntity)){
            return map;
        }
        List<SoB2cReceiverEntity> receiverEntityList = soB2cReceiverService.listByMainIds(Collections.singletonList(soB2cEntity.getId()));
        if (CollUtil.isEmpty(receiverEntityList)){
            return map;
        }
        SoB2cReceiverEntity receiverEntity = receiverEntityList.get(0);
        map.put("provinceName",receiverEntity.getProvinceName());
        map.put("taxidType",receiverEntity.getTaxidType());
        return map;
    }

    /**
     * 根据xml文件获取queryKey
     * @author zdy
     * @date 2025/7/18 15:37
     * @param linkXml
     * @return
     */
    private String getQueryKey(String linkXml) {
        if (CharSequenceUtil.isEmpty(linkXml)) {
            return "";
        }
        try {
            SAXReader reader = new SAXReader();
            Document document = reader.read(new URL(linkXml));
            Element root = document.getRootElement();
            String queryKey = root.element("NFe").element("infNFe").attributeValue("Id");
            return queryKey;
        } catch (Exception e) {
            log.error("获取queryKey失败,xml文件:{}异常：{}",linkXml, e.getMessage());
            return "";
        }
    }
    /**
     * 查询进行中数据
     * @author will
     * @date 2025/4/14 16:08
     * @return List<InvoiceInfoEntity>
     */
    private List<InvoiceInfoEntity> listNfeUploading () {
        return lambdaQuery().eq(InvoiceInfoEntity::getUploadStatus, InvoiceInfoUploadStatusEnum.UPLOADING.getCode())
                .eq(InvoiceInfoEntity::getInvoiceType,InvoiceInfoInvoiceTypeEnum.NFE.getCode())
                .ne(InvoiceInfoEntity::getQueryId,"")
                .ne(InvoiceInfoEntity::getShopId,"")
                .list();
    }

    /**
     * 根据销售订单id查询失败开票信息
     * @author will 
     * @date 2025/4/9 15:42
     * @param soId 
     * @return InvoiceInfoEntity
     */
    private InvoiceInfoEntity getFailedBySoId (String soId,List<String> invoiceTypeList) {
        return lambdaQuery().eq(InvoiceInfoEntity::getSoId,soId)
                .in(InvoiceInfoEntity::getInvoiceType,invoiceTypeList)
                .eq(InvoiceInfoEntity::getStatus, InvoiceInfoStatusEnum.INVOICE_FAILED.getCode())
                .last("limit 1")
                .one();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateCce(InvoiceInfoDTO.UpdateCceDTO dto) {
        InvoiceInfoEntity invoiceInfoEntity = this.getById(dto.getId());
        if (ObjUtil.isEmpty(invoiceInfoEntity)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "开票清单");
        }
        //仅nf-e发票支持操作
        if (!CharSequenceUtil.equals(InvoiceInfoInvoiceTypeEnum.NFE.getCode(), invoiceInfoEntity.getInvoiceType())) {
            throw new ServiceException(ApiError.FIN_INVOICE_NFE_ONLY_SUPPORTED);
        }
        //开票成功
        if (!InvoiceInfoStatusEnum.INVOICE_SUCCESS.getCode().equals(invoiceInfoEntity.getStatus())) {
            throw new ServiceException(ApiError.FIN_INVOICE_OPERATION_NOT_ALLOWED);
        }

        //重新上传
        NfeInvoiceDTO.NfeCceDTO nfeCceDTO = new NfeInvoiceDTO.NfeCceDTO();
        nfeCceDTO.setId(invoiceInfoEntity.getQueryId());
        nfeCceDTO.setJustificativa(dto.getContent());
        nfeInvoiceService.updateCceInvoice(invoiceInfoEntity,nfeCceDTO);

        //更新发票性质
        invoiceInfoEntity.setInvoiceNature(InvoiceNatureEnum.CC_E.getCode());
        this.updateById(invoiceInfoEntity);
        //添加修改记录
        InvoiceUpdateHisDTO.AddDTO addDTO = new InvoiceUpdateHisDTO.AddDTO();
        addDTO.setInvoiceInfoId(invoiceInfoEntity.getId());
        addDTO.setContent(dto.getContent());
        invoiceUpdateHisService.add(addDTO);
        return BatchResultDTO.success(invoiceInfoEntity.getId(), invoiceInfoEntity.getCode(), "修改CC-e发票成功");
    }

    @Override
    public InvoiceInfoDTO.ViewCceDTO viewCce(String id) {
        InvoiceInfoDTO.ViewCceDTO viewCceDTO = new InvoiceInfoDTO.ViewCceDTO();
        InvoiceInfoEntity invoiceInfoEntity = this.getById(id);
        if (ObjUtil.isEmpty(invoiceInfoEntity)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "开票清单");
        }
        //仅nf-e发票支持操作
        if (!CharSequenceUtil.equals(InvoiceInfoInvoiceTypeEnum.NFE.getCode(), invoiceInfoEntity.getInvoiceType())) {
            throw new ServiceException(ApiError.FIN_INVOICE_NFE_ONLY_SUPPORTED);
        }
        //开票成功
        if (!InvoiceInfoStatusEnum.INVOICE_SUCCESS.getCode().equals(invoiceInfoEntity.getStatus())) {
            throw new ServiceException(ApiError.FIN_INVOICE_OPERATION_NOT_ALLOWED);
        }
        Integer count = invoiceUpdateHisService.countByInvoiceInfoId(id);
        viewCceDTO.setCode(invoiceInfoEntity.getCode());
        viewCceDTO.setCount(count);
        return viewCceDTO;
    }

    @Override
    public InvoiceInfoDTO.ExportResultDTO exportXml(InvoiceInfoDTO.PagingParamDTO dto) {
        // 创建异步导出任务
        String taskId = downloadTaskFeign.saveDownloadTask("导出发票XML", EXPORT_INVOICE_XML.getCode(), dto);
        InvoiceInfoDTO.ExportResultDTO resultDTO = new InvoiceInfoDTO.ExportResultDTO();
        resultDTO.setTaskId(taskId);
        return resultDTO;
    }

    @Override
    public InvoiceInfoDTO.ExportResultDTO exportPdf(InvoiceInfoDTO.PagingParamDTO dto) {
        // 创建异步导出任务
        String taskId = downloadTaskFeign.saveDownloadTask("导出发票PDF", EXPORT_INVOICE_PDF.getCode(), dto);
        InvoiceInfoDTO.ExportResultDTO resultDTO = new InvoiceInfoDTO.ExportResultDTO();
        resultDTO.setTaskId(taskId);
        return resultDTO;
    }

    @Override
    public List<InvoiceTaxDTO.CheckGenerateInvoiceDTO> checkGenerateInvoice(List<String> soIdList, Boolean isCheckInvoiceTax) {
        if (CollUtil.isEmpty(soIdList)) {
            throw new ServiceException(ApiError.BILL_SELECTION_REQUIRED);
        }
        //销售订单
        List<SoB2cEntity> soB2cEntityList = soB2cService.listByIds(soIdList);
        if (CollUtil.isEmpty(soB2cEntityList)){
            throw new ServiceException(ApiError.SO_NOT_FOUND);
        }
        long count = soB2cEntityList.stream().filter(obj -> SoB2cNfeStatusEnum.INVOICING.getCode().equals(obj.getNfeInvoiceStatus())).count();
        if (count > 0 && Objects.nonNull(isCheckInvoiceTax) && isCheckInvoiceTax) {
            throw new ServiceException(ApiError.FIN_INVOICE_CREATING_REGENERATE_FORBIDDEN);
        }
        Map<String, SoB2cEntity> soB2cMap = soB2cEntityList.stream().collect(Collectors.toMap(SoB2cEntity::getId, Function.identity()));
        List<String> shopIdList = soB2cEntityList.stream().map(SoB2cEntity::getShopId).distinct().collect(Collectors.toList());
        List<String> platformList = soB2cEntityList.stream().map(SoB2cEntity::getDictPlatform).distinct().collect(Collectors.toList());
        //买家地址
        List<SoB2cReceiverEntity> receiverEntityList = soB2cReceiverService.listByMainIds(soIdList);
        List<SoB2cDetailEntity> soB2cDetailList = soB2cDetailService.listByMainIds(soIdList);
        if (CollUtil.isEmpty(soB2cDetailList)) {
            throw new ServiceException(ApiError.BILL_DETAIL_DATA_NOT_FOUND);
        }
        List<String> platformSkuNoList = soB2cDetailList.stream().map(SoB2cDetailEntity::getPlatformSkuNo).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());

        if (CollUtil.isEmpty(platformSkuNoList)) {
            throw new ServiceException("选择订单无平台SKU不支持开票");
        }
        //查询nfe发票配置信息
        List<CfgInvoiceSettingDetailEntity> cfgInvoiceSettingDetailList = cfgInvoiceSettingDetailService.listByShopIdList(shopIdList);
        Map<String, CfgInvoiceSettingDetailEntity> cfgInvoiceSettingDetailMap = cfgInvoiceSettingDetailList.stream().collect(Collectors.toMap(CfgInvoiceSettingDetailEntity::getShopId, Function.identity()));
        
        //店铺信息
        List<ShopInfoEntity> shopInfoList = shopInfoService.listByIds(shopIdList);
        Map<String, String> shopMap = shopInfoList.stream().collect(Collectors.toMap(ShopInfoEntity::getId,ShopInfoEntity::getName));

        // 查询该店铺所有平台sku
        ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
        paramDTO.setShopIdList(shopIdList);
        paramDTO.setPlatformList(platformList);
        paramDTO.setType(RuleTypeEnum.B2C_PLATFORM.getCode());
        paramDTO.setPlatformSkuNoList(platformSkuNoList);
        // 所有包含历史映射关系
        List<ListingInfoWithSkuMappingDTO> listingInfoEntityList = skuMappingService.findListDto(paramDTO);
        Map<String, List<ListingInfoWithSkuMappingDTO>> listingMap = listingInfoEntityList.stream().distinct().collect(Collectors.groupingBy(obj -> CharSequenceUtil.format("{}-{}-{}",obj.getPlatform(),obj.getPlatformSkuNo(),obj.getShopId())));

        //listingId集合
        List<String> listingIdList = listingInfoEntityList.stream().map(ListingInfoWithSkuMappingDTO::getListingId).distinct().collect(Collectors.toList());
        //查询发票税务信息
        List<InvoiceTaxEntity> invoiceTaxList = invoiceTaxService.listByListingIdList(listingIdList);
        Map<String, InvoiceTaxEntity> taxMap = invoiceTaxList.stream().collect(Collectors.toMap(InvoiceTaxEntity::getListingId, Function.identity()));

        List<InvoiceTaxDTO.CheckGenerateInvoiceDTO> resultList = new ArrayList<>();
        for (SoB2cDetailEntity soB2cDetailEntity : soB2cDetailList) {
            InvoiceTaxDTO.CheckGenerateInvoiceDTO viewDTO = new InvoiceTaxDTO.CheckGenerateInvoiceDTO();
            //销售订单信息
            SoB2cEntity soB2cEntity =  soB2cMap.get(soB2cDetailEntity.getMainId());
            if (ObjUtil.isEmpty(soB2cEntity)) {
                continue;
            }
            CfgInvoiceSettingDetailEntity cfgInvoiceSettingDetailEntity = cfgInvoiceSettingDetailMap.get(soB2cEntity.getShopId());
            if (ObjUtil.isEmpty(cfgInvoiceSettingDetailEntity)) {
                continue;
            }

//            if (!CharSequenceUtil.equals(soB2cEntity.getDictPlatform(), PlatformDictEnum.ALI_EXPRESS.getCode()) && !CharSequenceUtil.equals(soB2cEntity.getDictPlatform(), PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode())){
//                continue;
//            }
            //listing信息
            List<ListingInfoWithSkuMappingDTO> listingInfoWithSkuMappingList = listingMap.get(CharSequenceUtil.format("{}-{}-{}", soB2cEntity.getDictPlatform(), soB2cDetailEntity.getPlatformSkuNo(),soB2cEntity.getShopId()));
            //税务信息
            InvoiceTaxEntity invoiceTaxEntity = CollUtil.isEmpty(listingInfoWithSkuMappingList) ? new InvoiceTaxEntity() : listingInfoWithSkuMappingList.stream().filter(obj -> ObjUtil.isNotEmpty(taxMap.get(obj.getListingId()))).map(obj -> taxMap.get(obj.getListingId())).findFirst().orElse(new InvoiceTaxEntity());
            Boolean isGenerateInvoiceTax = invoiceTaxService.checkInvoiceTax(invoiceTaxEntity);
            if (isGenerateInvoiceTax && Objects.nonNull(isCheckInvoiceTax) && isCheckInvoiceTax) {
                continue;
            }
            BeanUtil.copyProperties(invoiceTaxEntity,viewDTO);
            viewDTO.setIsGenerateInvoiceTax(isGenerateInvoiceTax);
            viewDTO.setPlatformSkuNo(soB2cDetailEntity.getPlatformSkuNo());
            viewDTO.setPlatformSkuName(CollUtil.isEmpty(listingInfoWithSkuMappingList) ? "" : listingInfoWithSkuMappingList.get(0).getPlatformSkuName());
            viewDTO.setPlatform(soB2cEntity.getDictPlatform());
            viewDTO.setShopId(soB2cEntity.getShopId());
            viewDTO.setSoId(soB2cEntity.getId());
            viewDTO.setShopName(shopMap.get(soB2cEntity.getShopId()));
            viewDTO.setUnit(CharSequenceUtil.isBlank(viewDTO.getUnit()) ? "UN" : viewDTO.getUnit());
            //同州cfop
            if (CharSequenceUtil.isBlank(viewDTO.getSameStateTaxCode())) {
                //给默认值
                if (TaxTypeEnum.PURCHASE_SALE.getCode().equals(cfgInvoiceSettingDetailEntity.getTaxType())) {
                    viewDTO.setSameStateTaxCode(NfeCfopEnum.PURCHASE_SALE_SAME_CFOP.getCode());
                } else if (TaxTypeEnum.SELF_SALE.getCode().equals(cfgInvoiceSettingDetailEntity.getTaxType())) {
                    viewDTO.setSameStateTaxCode(NfeCfopEnum.SELF_SALE_SAME_CFOP.getCode());
                }
            }
            //跨州cfop
            if (CharSequenceUtil.isBlank(viewDTO.getDiffStateTaxCode())) {
                //给默认值
                if (TaxTypeEnum.PURCHASE_SALE.getCode().equals(cfgInvoiceSettingDetailEntity.getTaxType())) {
                    viewDTO.setDiffStateTaxCode(NfeCfopEnum.PURCHASE_SALE_DIFF_CFOP.getCode());
                } else if (TaxTypeEnum.SELF_SALE.getCode().equals(cfgInvoiceSettingDetailEntity.getTaxType())) {
                    viewDTO.setDiffStateTaxCode(NfeCfopEnum.SELF_SALE_DIFF_CFOP.getCode());
                }
            }
            //发票地址
            SoB2cReceiverEntity soB2cReceiverEntity = receiverEntityList.stream().filter(e -> e.getMainId().equals(soB2cEntity.getId())).findFirst().orElse(null);
            if (Objects.nonNull(soB2cReceiverEntity)){
                viewDTO.setInvoiceAddress(soB2cReceiverEntity.getInvoiceAddress());
            }
            resultList.add(viewDTO);
        }
        return resultList.stream().distinct().collect(Collectors.toList());
    }

    private List<InvoiceInfoEntity> autoUploadInvoice(List<InvoiceInfoEntity> waitCreateVoiceList, List<CfgVatInvoiceEntity> cfgVatInvoiceEntities, List<SoB2cEntity> soB2cEntityList, List<BatchResultDTO> resultDTOList) {
        List<InvoiceInfoEntity> uploadInvoiceList = waitCreateVoiceList.stream().filter(v->{
            boolean isCreatePdf = v.getStatus().equals(InvoiceInfoStatusEnum.INVOICE_SUCCESS.getCode());
            CfgVatInvoiceEntity cfgVatInvoiceEntity = cfgVatInvoiceEntities.stream().filter(e ->  !e.getDisabled() && v.getShopId().equals(e.getShopId())).findFirst().orElse(null);

            return isCreatePdf && Objects.nonNull(cfgVatInvoiceEntity) && cfgVatInvoiceEntity.getIsAutoUpload();
        }).collect(Collectors.toList());
        //上传发票
        for (InvoiceInfoEntity invoiceInfoEntity : uploadInvoiceList) {
            SoB2cEntity soB2cEntity = soB2cEntityList.stream().filter(e -> CharSequenceUtil.isNotBlank(invoiceInfoEntity.getSoId()) && invoiceInfoEntity.getSoId().equals(e.getId())).findFirst().orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "b2c订单"));
            try {
                String feedId = amazonUploadInvoiceService.uploadInvoice(soB2cEntity,invoiceInfoEntity.getFileUrl(),invoiceInfoEntity.getCode());
                soB2cEntity.setVatInvoiceStatus(SoB2cVatStatusEnum.UPLOAD_SUCCESS.getCode());
                invoiceInfoEntity.setQueryId(feedId);
                invoiceInfoEntity.setUploadStatus(InvoiceInfoUploadStatusEnum.UPLOADING.getCode());
                invoiceInfoEntity.setUploadTime(LocalDateTime.now());
            }catch (Exception e){
                soB2cEntity.setVatInvoiceStatus(SoB2cVatStatusEnum.UPLOAD_FAILURE.getCode());
                log.error("亚马逊上传发票失败,{}",e.getMessage());
                resultDTOList.add(BatchResultDTO.fail(invoiceInfoEntity.getId(),invoiceInfoEntity.getCode(),StrUtil.format("亚马逊上传发票失败,{}",e.getMessage())));
            }
        }
        return uploadInvoiceList;
    }

    private List<InvoiceDetailEntity> buildInvoiceDetail(List<SoB2cDetailEntity> soB2cDetailEntityList, InvoiceInfoEntity invoiceInfoEntity, Map<String, String> skuNameMap) {
        List<InvoiceDetailEntity> detailEntityList = new ArrayList<>();
        for (SoB2cDetailEntity soB2cDetailEntity : soB2cDetailEntityList) {
            InvoiceDetailEntity invoiceDetailEntity = new InvoiceDetailEntity();
            invoiceDetailEntity.setMainId(invoiceInfoEntity.getId());
            invoiceDetailEntity.setSourceDetailId(soB2cDetailEntity.getId());
            invoiceDetailEntity.setSkuId(soB2cDetailEntity.getSkuId());
            invoiceDetailEntity.setSkuNo(soB2cDetailEntity.getSkuNo());
            invoiceDetailEntity.setProductName(skuNameMap.getOrDefault(soB2cDetailEntity.getSkuId(), ""));
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
        invoiceInfoEntity.setCompanyName(cfgVatInvoiceEntity.getCompanyName());
        invoiceInfoEntity.setSellerTaxNo(cfgVatInvoiceEntity.getVatNo());
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

    private void fillList(List<InvoiceInfoDTO.PagingViewDTO> records, Boolean isExport) {
        if (CollUtil.isEmpty(records)){
            return;
        }
        List<String> shopIds = records.stream().map(InvoiceInfoDTO.PagingViewDTO::getShopId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoEntityList = shopInfoService.listByIds(shopIds);
//        List<String> skuIds = records.stream().map(InvoiceInfoDTO.PagingViewDTO::getSkuId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
//        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(skuIds);
        records.forEach(pagingViewDTO -> {
//            SkuVO skuVO = skuVOS.stream().filter(e -> CharSequenceUtil.isNotBlank(pagingViewDTO.getSkuId()) && pagingViewDTO.getSkuId().equals(e.getSkuId())).findFirst().orElse(null);
//            pagingViewDTO.setProductName(Objects.nonNull(skuVO) ? skuVO.getSkuName() : CharSequenceUtil.EMPTY);
            pagingViewDTO.setInvoiceTypeName(InvoiceInfoInvoiceTypeEnum.getName(pagingViewDTO.getInvoiceType()));
            ShopInfoEntity shopInfoEntity = shopInfoEntityList.stream().filter(e -> CharSequenceUtil.isNotBlank(pagingViewDTO.getShopId()) && pagingViewDTO.getShopId().equals(e.getId())).findFirst().orElse(null);
            pagingViewDTO.setShopName(Objects.nonNull(shopInfoEntity) ? shopInfoEntity.getName() : CharSequenceUtil.EMPTY);
            pagingViewDTO.setTemplateTypeName(InvoiceInfoTemplateTypeEnum.getName(pagingViewDTO.getTemplateType()));
            pagingViewDTO.setStatusName(InvoiceInfoStatusEnum.getName(pagingViewDTO.getStatus()));
            pagingViewDTO.setUploadStatusName(InvoiceInfoUploadStatusEnum.getName(pagingViewDTO.getUploadStatus()));
            if(isExport && StringUtils.isNotBlank(pagingViewDTO.getFileUrl())){
                pagingViewDTO.setFileUrl(fdfsPubUrl + pagingViewDTO.getFileUrl());
            }
            pagingViewDTO.setInvoiceNatureName(InvoiceNatureEnum.getName(pagingViewDTO.getInvoiceNature()));
            if (CharSequenceUtil.isNotBlank(pagingViewDTO.getStartCode())){
                pagingViewDTO.setStartCodeStr(pagingViewDTO.getNo()+"/"+pagingViewDTO.getStartCode());
            }
        });
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(InvoiceInfoEntity invoiceInfoEntity) {
    // TODO 验证数据 & 数据赋值
    }


    /**
     * 下载Zip
     * @author will
     * @date 2025/4/10 10:23
     * @param exportAttachList
     * @return StreamingResponseBody
     */
    @Override
    public StreamingResponseBody downloadZip(List<InvoiceInfoDTO.ExportAttachDTO> exportAttachList) {
        return outputStream -> {
            // 创建一个不关闭底层流的 ZipOutputStream 包装类
            ZipOutputStream zipOut = new ZipOutputStream(outputStream) {
                private boolean finished = false;
                
                @Override
                public void finish() throws java.io.IOException {
                    if (!finished) {
                        super.finish();
                        finished = true;
                    }
                }
                
                @Override
                public void close() throws java.io.IOException {
                    // 只调用 finish()，不关闭底层流，让 Spring 自己管理 outputStream
                    finish();
                    // 不调用 super.close()，避免关闭底层流
                }
            };
            
            try {
                // 用于记录每个文件名出现的次数，避免重复文件名
                Map<String, Integer> fileNameCountMap = new HashMap<>();
                
                // 使用并发下载所有文件内容，单个文件失败不影响其他文件
                List<CompletableFuture<Pair<String, byte[]>>> downloadFutures = exportAttachList.stream()
                        .map(attachDTO -> CompletableFuture.supplyAsync(() -> {
                            String fileName = attachDTO.getAttachName();
                            // 如果附件名称为空，从URL提取文件名
                            if (CharSequenceUtil.isBlank(fileName)) {
                                String[] split = attachDTO.getAttachUrl().split("/");
                                fileName = split[split.length - 1];
                            }
                            try {
                                byte[] content = FastDFSClientUtil.getFileByte(attachDTO.getAttachUrl());
                                return Pair.of(fileName, content);
                            } catch (Exception e) {
                                log.error("文件下载失败: {}", attachDTO.getAttachUrl(), e);
                                // 返回 null 标记失败，而不是抛出异常，避免影响其他文件
                                return null;
                            }
                        }, executorPool))
                        .collect(Collectors.toList());

                // 等待所有下载任务完成（不抛出异常，允许部分失败）
                CompletableFuture.allOf(downloadFutures.toArray(new CompletableFuture[0])).join();

                int successCount = 0;
                int failCount = 0;
                
                // 单线程按顺序写入ZIP
                for (CompletableFuture<Pair<String, byte[]>> future : downloadFutures) {
                    try {
                        Pair<String, byte[]> fileData = future.get(); // 获取下载结果
                        
                        // 跳过下载失败的文件（返回null）
                        if (fileData == null) {
                            failCount++;
                            continue;
                        }
                        
                        String fileName = fileData.getKey();
                        byte[] content = fileData.getValue();
                        
                        if (content == null || content.length == 0) {
                            log.warn("文件内容为空，跳过: {}", fileName);
                            failCount++;
                            continue;
                        }
                        
                        // 处理重复文件名
                        String uniqueFileName = generateUniqueFileName(fileName, fileNameCountMap);
                        
                        zipOut.putNextEntry(new ZipEntry(uniqueFileName));
                        zipOut.write(content);
                        zipOut.closeEntry();
                        successCount++;
                    } catch (Exception e) {
                        log.error("写入ZIP文件失败: {}", e.getMessage(), e);
                        failCount++;
                        // 继续处理下一个文件，不中断整个流程
                    }
                }
                
                // 如果所有文件都失败了，抛出异常
                if (successCount == 0) {
                    throw new ServiceException("所有文件下载或写入失败，无法生成ZIP文件");
                }
                
                if (failCount > 0) {
                    log.warn("ZIP文件生成完成，成功: {} 个，失败: {} 个", successCount, failCount);
                }
                
                // 显式调用 finish() 确保 ZIP 文件结构完整（写入中央目录结束标记）
                zipOut.finish();
                zipOut.flush();
            } catch (Exception e) {
                log.error("压缩包生成失败", e);
                // 确保在异常情况下也完成ZIP文件
                try {
                    zipOut.finish();
                } catch (Exception ex) {
                    log.error("异常情况下完成ZIP文件失败", ex);
                }
                throw new ServiceException("压缩包生成失败: " + e.getMessage(), e);
            }
        };
    }

    @Override
    public List<InvoiceInfoDTO.ExportAttachDTO> listExportUrl(InvoiceInfoDTO.PagingParamDTO dto, String type) {
        return baseMapper.listExportUrl(dto, type);
    }

    @Override
    public String buildInvoiceAttachZip(InvoiceInfoDTO.PagingParamDTO dto, String type) {
        log.info("开始构建发票附件ZIP文件，type={}", type);
        
        // 1. 查询附件URL列表
        List<InvoiceInfoDTO.ExportAttachDTO> exportResultList = baseMapper.listExportUrl(dto, type);
        
        if (CollUtil.isEmpty(exportResultList)) {
            throw new ServiceException(ApiError.FILE_EXPORT_DATA_EMPTY);
        }
        
        // 2. 构建文件URL列表和文件名映射
        Map<String, List<String>> folderStructure = new HashMap<>();
        Map<String, String> fileUrlToNameMap = new HashMap<>();
        List<String> fileUrlList = new ArrayList<>();
        
        for (InvoiceInfoDTO.ExportAttachDTO attachDTO : exportResultList) {
            String fileUrl = attachDTO.getAttachUrl();
            String fileName = attachDTO.getAttachName();
            
            if (StrUtil.isBlank(fileUrl)) {
                continue;
            }
            
            // 如果附件名称为空，从URL提取文件名
            if (StrUtil.isBlank(fileName)) {
                String[] split = fileUrl.split("/");
                fileName = split[split.length - 1];
            }
            
            fileUrlList.add(fileUrl);
            fileUrlToNameMap.put(fileUrl, fileName);
        }
        
        if (fileUrlList.isEmpty()) {
            throw new ServiceException(ApiError.FILE_EXPORT_DATA_EMPTY);
        }
        
        // 所有文件放在根目录
        folderStructure.put("", fileUrlList);
        
        // 3. 构建CreateZipDTO并调用文件中心创建ZIP
        com.erp.model.file.dto.FileDTO.CreateZipDTO createZipDTO = com.erp.model.file.dto.FileDTO.CreateZipDTO.builder()
                .folderStructure(folderStructure)
                .fileUrlToNameMap(fileUrlToNameMap)
                .build();
        
        String zipUrl = fileFeign.createZipFromFolderStructure(createZipDTO);
        
        if (StrUtil.isBlank(zipUrl)) {
            throw new ServiceException(ApiError.FILE_ZIP_CREATE_FAILED, "");
        }
        
        log.info("构建发票附件ZIP文件完成，type={}, zipUrl={}, count={}", type, zipUrl, exportResultList.size());
        return zipUrl;
    }

    /**
     * 生成唯一的文件名，避免重复
     *
     * @param baseFileName 原始文件名
     * @param fileNameCountMap 记录已出现的文件名及其次数
     * @return 唯一文件名
     */
    private String generateUniqueFileName(String baseFileName, Map<String, Integer> fileNameCountMap) {
        int count = fileNameCountMap.getOrDefault(baseFileName, 0);
        fileNameCountMap.put(baseFileName, count + 1);

        if (count == 0) {
            return baseFileName;
        }

        // 插入递增序号防止重复
        String nameWithoutExt = "";
        String ext = "";

        int dotIndex = baseFileName.lastIndexOf(".");
        if (dotIndex > 0) {
            nameWithoutExt = baseFileName.substring(0, dotIndex);
            ext = baseFileName.substring(dotIndex);
        } else {
            nameWithoutExt = baseFileName;
        }

        return nameWithoutExt + "_" + count + ext;
    }


}
