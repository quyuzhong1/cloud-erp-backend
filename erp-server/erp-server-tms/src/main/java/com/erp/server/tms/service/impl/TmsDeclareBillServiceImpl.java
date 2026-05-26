package com.erp.server.tms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.converters.ConverterKeyBuild;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.alibaba.excel.write.metadata.fill.FillWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.constant.RedisCacheConstants;
import com.common.message.constant.DistributeKeyConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
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
import com.common.core.excel.*;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.model.plm.entity.BasicDictEntity;
import com.erp.model.plm.entity.ProductPackEntity;
import com.erp.model.plm.enums.CombinationDeclareTypeEnums;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.tms.dto.CfgSettingDTO;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.DictBasicDTO;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.*;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.dto.SoDeliveryNoticeDTO;
import com.erp.model.wms.dto.WmsCartonDetailDTO;
import com.erp.model.wms.dto.WmsCartonSpecDTO;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.model.wms.entity.PackingTaskEntity;
import com.erp.model.wms.entity.SoDeliveryNoticeEntity;
import com.erp.model.wms.enums.FbaDemandTypeEnum;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.model.wms.enums.PackingTaskStatusEnum;
import com.erp.model.wms.enums.WmsDeclareStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.plm.feign.ProductPackFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.PackingTaskFeign;
import com.erp.rpc.wms.feign.SoDeliveryNoticeFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.server.tms.mapper.TmsDeclareBillMapper;
import com.erp.server.tms.service.*;
import com.erp.server.tms.utils.DeclarationGenerationService;
import com.erp.server.tms.utils.DeclareMergeDefaults;
import com.google.common.collect.Lists;
import freemarker.template.utility.StringUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.erp.model.tms.enums.CfgSettingEnum.CONTRACT_AGREEMENT_NO;

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

    @Resource
    private CfgDeclareRuleService cfgDeclareRuleService;

    @Resource
    private ProductPackFeign productPackFeign;

    @Resource
    private PackingTaskFeign packingTaskFeign;

    @Resource
    private CustomerFeign customerFeign;

    @Resource
    private SoInfoFeign soInfoFeign;

    @Resource
    private LogisticsChannelService logisticsChannelService;

    /**
     * 多 sheet 报关单导出模板（sheet0 报关单 / sheet1 合同 / sheet2 发票 / sheet3 装箱单 / sheet4 装箱明细）
     */
    private static final String DECLARE_MULTI_EXCEL_PATH = "excel/declareExportMulti.xlsx";

    /**
     * 多 sheet 报关单单次导出条数上限
     *
     * <p>每条记录都会重新打开模板渲染多个 sheet 并写入 ZIP，IO/CPU 开销随条数线性增长，
     * 限制 100 条避免大批量导出拖垮接口。</p>
     */
    private static final int DECLARE_MULTI_EXPORT_LIMIT = 100;

    /**
     * ZIP 批量导出存在单条渲染失败时置 true，供前端下载完成后提示用户
     */
    private static final String HEADER_EXPORT_PARTIAL_FAILURE = "X-Export-Partial-Failure";

    /**
     * ZIP 批量导出渲染失败的报关单号，逗号分隔
     */
    private static final String HEADER_EXPORT_FAILED_CODES = "X-Export-Failed-Codes";

    /**
     * 贸易国默认值。
     * 业务要求新增报关单时贸易国默认中国香港；如果后续后端有更精确的国家字典码，可以再调整。
     */
    private static final String DEFAULT_TRADING_AREA = "HK";

    /**
     * 目的国"中国大陆"字典码（与 {@code com.erp.model.sys.enums.DictValueEnum#CN} 一致）。
     * 目的国为中国大陆的来源单不生成报关单。
     */
    private static final String MAINLAND_CHINA_COUNTRY_CODE = "CN";


    @Override
    @DistributeLocker(
            businessType = DistributeKeyConstant.TMS_DECLARE_BILL_SOURCE_KEY,
            keyName = "addDTO.mergeDetailList.sourceDeliveryDetailList.sourceId",
            maxRetries = 1,
            unlockAfterTx = true
    )
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean addFmDeclare(TmsDeclareBillDTO.AddDTO addDTO) {
        List<String> sourceIdList = resolveAddSourceIdList(addDTO);
        TmsDeclareBillDTO.QuerySourceDTO querySourceDTO = TmsDeclareBillDTO.QuerySourceDTO.builder()
//                .packingStatus(PackingTaskStatusEnum.PACKED.getCode())
                .declareStatus(WmsDeclareStatusEnum.WAIT.getCode())
                .ids(sourceIdList)
                .build();
        if (!Boolean.TRUE.equals(addDTO.getIsAuto())) {
            querySourceDTO.setPackingStatus(PackingTaskStatusEnum.PACKED.getCode());
        }
        List<TmsDeclareBillDTO.DeliveryDTO> deliveryDTOList = wmsFirstMileDeliveryFeign.getCanGenerateDeclare(querySourceDTO);
        if (CollectionUtils.isEmpty(deliveryDTOList)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_GENERATABLE_DELIVERY_NOT_FOUND);
        }
        TmsDeclareBillDTO.DeliveryDTO deliveryDTO = deliveryDTOList.get(0);
        // 新增页面下推保存逻辑：按前端提交的合并明细直接生成报关单
        if (CollUtil.isNotEmpty(addDTO.getMergeDetailList())) {
            List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> mergeDetailList = prepareSubmittedMergeDetailList(addDTO.getMergeDetailList(), addDTO.getIsMerge());
            validateDeclareMergeDetails(mergeDetailList);
            // 目的国为中国大陆的来源单不生成报关单，任一勾选行命中即整批失败、不入库，
            // 来源单 declare_status 保持 WAIT，错误信息列出所有命中的来源单号。
            validateDestCountryNotMainlandChina(mergeDetailList);
            Set<String> sourceKeySet = collectSourceKeySet(mergeDetailList);
            validateSourceNotGenerated(sourceKeySet, collectSourceIdSet(mergeDetailList), SourceTypeEnum.FIRST_MILE_DELIVERY.getCode(), null);

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
            //  复用 batchAddMergeDetail 链路里的 calculateSelectedNetWeight，
            // 按 SKU × qty 真实累加，且已经处理了组合品 SPLIT 拆分。
            declareBillEntity.setNetWeight(calculateSelectedNetWeight(flattenMergeSourceDetails(mergeDetailList)));
            // 贸易国默认中国香港。
            applyTradingAreaDefault(declareBillEntity);

            List<TmsDeclareBillDetailEntity> detailEntityList = new ArrayList<>(mergeDetailList.size());
            for (TmsDeclareBillDTO.MergeDeclareBillDetailDTO detailDTO : mergeDetailList) {
                TmsDeclareBillDetailEntity detailEntity = new TmsDeclareBillDetailEntity();
                mapMergeDeclareDetailToEntity(detailDTO, detailEntity);
                detailEntityList.add(detailEntity);
            }
            if (CollUtil.isEmpty(detailEntityList)) {
                throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_SAVE_REQUIRED);
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

            List<DeliveryDeclareDetailMidEntity> addMidList = buildDeclareDetailMidList(mergeDetailList, detailEntityList,
                    SourceTypeEnum.FIRST_MILE_DELIVERY.getCode(), firstSourceId(sourceIdList), addResult.getId(), addResult.getCode());
            if (CollUtil.isNotEmpty(addMidList)) {
                deliveryDeclareDetailMidService.saveBatch(addMidList);
            }
            updateSourceDeclareStatus(SourceTypeEnum.FM_DECLARE_BILL.getCode(), addMidList);
            return Boolean.TRUE;
        }

        // fallback 路径按 deliveryDTO 维度直接生成，提前判断目的国是否为中国大陆，
        // 命中即整批失败（同 Path A 的语义），列出所有命中的来源单号，来源单 declare_status 保持 WAIT。
        List<String> mainlandSourceCodes = deliveryDTOList.stream()
                .filter(Objects::nonNull)
                .filter(item -> isMainlandChinaCountry(item.getCountry()))
                .map(TmsDeclareBillDTO.DeliveryDTO::getSourceCode)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(mainlandSourceCodes)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DEST_COUNTRY_CN_NOT_GENERATE,
                    String.join("、", mainlandSourceCodes));
        }
        TmsDeclareBillEntity baseTmsDeclareBillEntity = new TmsDeclareBillEntity();
        BeanMapperUtils.copy(addDTO, baseTmsDeclareBillEntity);
        BeanMapperUtils.copy(deliveryDTO, baseTmsDeclareBillEntity);
        baseTmsDeclareBillEntity.setDeclareStatus(com.erp.model.tms.enums.DeclareStatusEnum.WAIT.getCode());
        baseTmsDeclareBillEntity.setType(SourceTypeEnum.FM_DECLARE_BILL.getCode());
        // 贸易国默认中国香港。
        applyTradingAreaDefault(baseTmsDeclareBillEntity);
        //50个明细为一个报关单
        List<TmsDeclareBillDTO.ProductDetail> allProductDetailList = deliveryDTO.getProductDetailList();
        if(CollectionUtils.isEmpty(allProductDetailList)){
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_GENERATABLE_DETAIL_NOT_FOUND);
        }
        //自动生成报关单前校验海关编码/报关品名/申报要素/单位/币种/单价是否完整，
        // 缺失则提示对应来源单据 + SKU 缺哪些字段，不进入入库流程。
        validateAutoFmDeclareProductDetails(deliveryDTO.getSourceCode(), allProductDetailList);
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
            dto.setIds(sourceIdList);
            dto.setDeclareStatus(WmsDeclareStatusEnum.FINISH.getCode());
            wmsFirstMileDeliveryFeign.updateStatus(dto);
        }
        return true;
    }

    /**
     * 从新增报关单的合并后明细中解析来源单据id集合。
     *
     * @param addDTO 新增报关单参数
     * @return 来源单据id集合
     */
    private List<String> resolveAddSourceIdList(TmsDeclareBillDTO.AddDTO addDTO) {
        // 新增入口不再接收单独 sourceId，来源以底层拆分后的 sourceDeliveryDetailList 为准。
        List<String> sourceIdList = Optional.ofNullable(addDTO.getMergeDetailList()).orElse(Collections.emptyList()).stream()
                .map(TmsDeclareBillDTO.MergeDeclareBillDetailDTO::getSourceDeliveryDetailList)
                .filter(CollUtil::isNotEmpty)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getSourceId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(sourceIdList)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_SOURCE_DETAIL_NOT_FOUND_FOR_SAVE);
        }
        return sourceIdList;
    }

    /**
     * 获取首个来源单据id，用于兼容中间表构建中的 fallbackSourceId。
     *
     * @param sourceIdList 来源单据id集合
     * @return 首个来源单据id
     */
    private String firstSourceId(List<String> sourceIdList) {
        return CollUtil.isEmpty(sourceIdList) ? "" : sourceIdList.get(0);
    }

    /**
     * 把合并明细打平成 {@code List<SourceDeliveryDetailDTO>}，供 {@link #calculateSelectedNetWeight} 复用。
     */
    private List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> flattenMergeSourceDetails(
            List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> mergeDetailList) {
        if (CollUtil.isEmpty(mergeDetailList)) {
            return Collections.emptyList();
        }
        return mergeDetailList.stream()
                .filter(Objects::nonNull)
                .map(TmsDeclareBillDTO.MergeDeclareBillDetailDTO::getSourceDeliveryDetailList)
                .filter(CollUtil::isNotEmpty)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 贸易国默认值兜底。trading_area 是 tms_declare_bill 本表列，可以保存侧赋值。
     */
    private void applyTradingAreaDefault(TmsDeclareBillEntity entity) {
        if (entity != null && StringUtils.isBlank(entity.getTradingArea())) {
            entity.setTradingArea(DEFAULT_TRADING_AREA);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResultDTO.AddDTO add(TmsDeclareBillEntity tmsDeclareBillEntity,List<TmsDeclareBillDetailEntity> detailEntityList,SourceTypeEnum sourceTypeEnum,boolean isMerged) {

        //合并的话不生成合同号；若调用方已预设合同号（如拆分保存 base_1、base_2）则不再覆盖
        if (!isMerged && StringUtils.isBlank(tmsDeclareBillEntity.getCode())) {
            String code = this.generateContractCode(tmsDeclareBillEntity,sourceTypeEnum);
            tmsDeclareBillEntity.setCode(code);
        }

        log.info("开始新增报关单");
        boolean save = super.save(tmsDeclareBillEntity);
        if(!save) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_BILL_SAVE_FAILED);
        }
        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】【{}】【{}】合同号为【{}】", UserContext.getDefaultLoginUser().getUserName(),isMerged?"合并":"新增", "报关单" , tmsDeclareBillEntity.getCode());
        operateLogService.addModuleOperateLog(msg, sourceTypeEnum.getCode(), tmsDeclareBillEntity.getId(), "新增操作");
        detailService.add(tmsDeclareBillEntity,detailEntityList);
        return new BaseResultDTO.AddDTO(tmsDeclareBillEntity.getId(), tmsDeclareBillEntity.getCode());
    }

    private String generateContractCode(TmsDeclareBillEntity tmsDeclareBillEntity,SourceTypeEnum sourceTypeEnum) {
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

        //查询报关单头编码配置
        CfgSettingDTO.ViewDTO settingViewDTO = cfgSettingService.getSetting(CONTRACT_AGREEMENT_NO.getCode());
        if (ObjectUtil.isEmpty(settingViewDTO) || CollUtil.isEmpty(settingViewDTO.getContractAgreementNoList())) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_CONTRACT_AGREEMENT_NO_NOT_FOUND);
        }
        String contractAgreementNo = settingViewDTO.getContractAgreementNoList().stream().filter(obj -> CharSequenceUtil.equals(obj.getCompanyId(), tmsDeclareBillEntity.getSenderId())).map(CfgSettingValueDTO.ContractAgreementNoDTO::getContractAgreementNo).findFirst().orElse("");
        if (StringUtils.isBlank(contractAgreementNo)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_CONTRACT_AGREEMENT_NO_FAILED,tmsDeclareBillEntity.getSenderName());
        }
        return CharSequenceUtil.format("{}{}{}", contractAgreementNo, DateUtil.currentYMD(), StringUtil.leftPad(String.valueOf(number), 3, "0"));
    }
    /**
    * 修改
    */
    @DistributeLocker(
            businessType = DistributeKeyConstant.TMS_DECLARE_BILL_ID_KEY,
            keyName = "updateDTO.id",
            maxRetries = 1,
            unlockAfterTx = true
    )
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Override
    public Boolean update(TmsDeclareBillDTO.UpdateDTO updateDTO,SourceTypeEnum sourceTypeEnum) {
        TmsDeclareBillEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "报关单"));
        if (!CharSequenceUtil.equals(old.getType(), sourceTypeEnum.getCode())) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_BILL_TYPE_MISMATCH);
        }
        String sourceType = resolveDeclareSourceType(old.getType());
        if(!old.getDeclareStatus().equals(com.erp.model.tms.enums.DeclareStatusEnum.WAIT.getCode())){
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_WAIT_STATUS_REQUIRED_FOR_EDIT);
        }
        List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> mergeDetailList = prepareSubmittedMergeDetailList(updateDTO.getMergeDetailList(), updateDTO.getIsMerge());
        validateDeclareMergeDetails(mergeDetailList);
        validateUpdateImmutableSourceFields(old.getId(), mergeDetailList);
        Set<String> sourceKeySet = collectSourceKeySet(mergeDetailList);
        validateSourceNotGenerated(sourceKeySet, collectSourceIdSet(mergeDetailList), sourceType, old.getId());
        if(Objects.isNull(updateDTO.getShippingFee())){
            updateDTO.setShippingFee(BigDecimal.ZERO);
        }
        if(Objects.isNull(updateDTO.getInsuranceFee())){
            updateDTO.setInsuranceFee(BigDecimal.ZERO);
        }
        if(Objects.isNull(updateDTO.getOtherFee())){
            updateDTO.setOtherFee(BigDecimal.ZERO);
        }
        TmsDeclareBillEntity tmsDeclareBillEntity = new TmsDeclareBillEntity();
        BeanMapper.copy(old,tmsDeclareBillEntity);
        BeanMapper.copy(updateDTO,tmsDeclareBillEntity);


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
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_BILL_SAVE_FAILED);
        }
        List<DeliveryDeclareDetailMidEntity> oldMidList = deliveryDeclareDetailMidService.listByDeclareBillIdList(Collections.singletonList(old.getId()));
        detailService.deleteDetailByMainIdList(Collections.singletonList(old.getId()));
        List<TmsDeclareBillDetailEntity> detailEntityList = new ArrayList<>(mergeDetailList.size());
        for (TmsDeclareBillDTO.MergeDeclareBillDetailDTO detailDTO : mergeDetailList) {
            TmsDeclareBillDetailEntity detailEntity = new TmsDeclareBillDetailEntity();
            detailEntity.setMainId(old.getId());
            mapMergeDeclareDetailToEntity(detailDTO, detailEntity);
            detailEntityList.add(detailEntity);
        }
        if (CollUtil.isEmpty(detailEntityList)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_SAVE_REQUIRED);
        }
        if (!detailService.saveBatch(detailEntityList)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_SAVE_FAILED);
        }
        List<DeliveryDeclareDetailMidEntity> addMidList = buildDeclareDetailMidList(mergeDetailList, detailEntityList,
                sourceType, null, old.getId(), old.getCode());
        saveOrRestoreUpdateMidData(oldMidList, addMidList, old.getId(), old.getCode());
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

        if (SourceTypeEnum.B2B_DECLARE_BILL.getCode().equals(type)) {
            // 通过中间表反查 source_id 后批量取 SoDeliveryNotice.carrier_*，
            fillB2bDeclareCarrier(list);
        } else if (SourceTypeEnum.FM_DECLARE_BILL.getCode().equals(type)) {
            fillFmDeclareSupplier(list);
        }

        list.forEach(v->{
            v.setDeclareStatusName(EnumMessage.getNameByCode(com.erp.model.tms.enums.DeclareStatusEnum.class,v.getDeclareStatus()));
            DictBasicDTO.ViewDTO declareType = declareTypeDict.stream().filter(e->e.getCode().equals(v.getDeclareType())).findFirst().orElse(new DictBasicDTO.ViewDTO());
            v.setDeclareTypeName(declareType.getName());
        });
    }

    /**
     * B2B 报关单列表：从中间表反查发货通知单 id -> 批量取 SoDeliveryNotice -> 取承运商字段 -> 写回 logisticsSupplierId/Name。
     * 多个发货通知（合并报关）的承运商按逗号去重拼接。
     */
    private void fillB2bDeclareCarrier(List<TmsDeclareBillDTO.PagingVO> list) {
        List<String> declareIds = list.stream()
                .map(TmsDeclareBillDTO.PagingVO::getId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        Map<String, List<String>> declareSourceIdMap = Collections.emptyMap();
        Map<String, SoDeliveryNoticeEntity> noticeMap = Collections.emptyMap();
        if (CollectionUtils.isNotEmpty(declareIds)) {
            List<DeliveryDeclareDetailMidEntity> midList = deliveryDeclareDetailMidService.listByDeclareBillIdList(declareIds);
            if (CollectionUtils.isNotEmpty(midList)) {
                declareSourceIdMap = midList.stream()
                        .filter(m -> SourceTypeEnum.SO_DELIVERY_NOTICE.getCode().equals(m.getSourceType()))
                        .filter(m -> StringUtils.isNotBlank(m.getSourceId()))
                        .collect(Collectors.groupingBy(
                                DeliveryDeclareDetailMidEntity::getDeclareId,
                                Collectors.mapping(DeliveryDeclareDetailMidEntity::getSourceId,
                                        Collectors.toCollection(LinkedHashSet::new))))
                        .entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> new ArrayList<>(e.getValue())));
                List<String> allSourceIds = declareSourceIdMap.values().stream()
                        .flatMap(Collection::stream)
                        .distinct()
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(allSourceIds)) {
                    try {
                        List<SoDeliveryNoticeEntity> notices = soDeliveryNoticeFeign.listByIds(allSourceIds);
                        if (CollUtil.isNotEmpty(notices)) {
                            noticeMap = notices.stream()
                                    .filter(n -> StringUtils.isNotBlank(n.getId()))
                                    .collect(Collectors.toMap(SoDeliveryNoticeEntity::getId, n -> n, (a, b) -> a));
                        }
                    } catch (Exception e) {
                        log.warn("B2B 报关单列表填充承运商失败: {}", e.getMessage());
                    }
                }
            }
        }
        Map<String, List<String>> finalDeclareSourceMap = declareSourceIdMap;
        Map<String, SoDeliveryNoticeEntity> finalNoticeMap = noticeMap;
        list.forEach(v -> {
            List<String> sourceIds = finalDeclareSourceMap.getOrDefault(v.getId(), Collections.emptyList());
            if (CollectionUtils.isNotEmpty(sourceIds) && !finalNoticeMap.isEmpty()) {
                List<SoDeliveryNoticeEntity> matched = sourceIds.stream()
                        .map(finalNoticeMap::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                String carrierIds = matched.stream()
                        .map(SoDeliveryNoticeEntity::getCarrierId)
                        .filter(StringUtils::isNotBlank)
                        .distinct()
                        .collect(Collectors.joining(","));
                String carrierNames = matched.stream()
                        .map(SoDeliveryNoticeEntity::getCarrierName)
                        .filter(StringUtils::isNotBlank)
                        .distinct()
                        .collect(Collectors.joining(","));
                if (StringUtils.isNotBlank(carrierIds)) {
                    v.setLogisticsSupplierId(carrierIds);
                }
                if (StringUtils.isNotBlank(carrierNames)) {
                    v.setLogisticsSupplierName(carrierNames);
                }
            }
            v.setBusinessTypeName(OrderTypeEnum.getName(v.getBusinessType()));
        });
    }

    /**
     * 头程报关单列表：物流商沿用 logistics_bill 链路（按 outstockCode 关联），与历史口径保持一致。
     */
    private void fillFmDeclareSupplier(List<TmsDeclareBillDTO.PagingVO> list) {
        List<String> sourceCodes = list.stream()
                .map(TmsDeclareBillDTO.PagingVO::getSourceCode)
                .filter(StringUtils::isNotBlank)
                .flatMap(code -> Arrays.stream(code.split(",")))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<LogisticsBillEntity> logisticsList = CollectionUtils.isEmpty(sourceCodes)
                ? Collections.emptyList()
                : fmLogisticService.listByOutstcockCode(sourceCodes);
        List<String> supplierIds = logisticsList.stream()
                .map(LogisticsBillEntity::getLogisticsSupplierId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<LogisticsSupplierEntity> supplierList = CollectionUtils.isNotEmpty(supplierIds)
                ? logisticsSupplierService.listByIds(supplierIds)
                : new ArrayList<>();
        list.forEach(v -> {
            LogisticsBillEntity logistics = logisticsList.stream()
                    .filter(e -> e.getOutstockCode().equals(v.getSourceCode()))
                    .findFirst().orElse(null);
            if (logistics != null) {
                LogisticsSupplierEntity supplier = supplierList.stream()
                        .filter(e -> e.getId().equals(logistics.getLogisticsSupplierId()))
                        .findFirst().orElse(new LogisticsSupplierEntity());
                v.setLogisticsSupplierId(supplier.getId());
                v.setLogisticsSupplierName(supplier.getSupplierName());
            }
            v.setBusinessTypeName(FbaDemandTypeEnum.getName(v.getBusinessType()));
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
        //  装箱信息体积重量按"长*宽*高 / 渠道材积"回填，渠道取发货单关联的物流单的 channel_id。
        // 这里改成批量查询渠道，避免每个 DTO 都走一次 getById。
        Map<String, LogisticsChannelEntity> channelMap = loadChannelMap(logisticsBillEntityList);
        for (TmsDeclareBillDTO.DeliveryDTO deliveryDTO : deliveryDTOList) {
            LogisticsBillEntity logisticsBillEntity = logisticsBillEntityList.stream().filter(v->v.getOutstockId().equals(deliveryDTO.getSourceId())).findFirst().orElse(new LogisticsBillEntity());
            LogisticsSupplierEntity logisticsSupplierEntity = logisticsSupplierEntityList.stream().filter(v->v.getId().equals(logisticsBillEntity.getLogisticsSupplierId())).findFirst().orElse(new LogisticsSupplierEntity());
            deliveryDTO.setShippingMethod(logisticsBillEntity.getShippingMethod());
            deliveryDTO.setShippingMethodName(LogisticsMethodEnum.getName(logisticsBillEntity.getShippingMethod()));
            deliveryDTO.setLogisticsSupplierId(logisticsBillEntity.getLogisticsSupplierId());
            deliveryDTO.setLogisticsSupplierName(logisticsSupplierEntity.getSupplierName());
            deliveryDTO.setCounterNo(logisticsBillEntity.getCounterNo());
            fillPackingVolumeWeight(deliveryDTO.getPackingDTOList(), channelMap.get(logisticsBillEntity.getChannelId()));
        }
        return deliveryDTOList;
    }

    /**
     * 批量加载物流单对应的渠道实体，供装箱信息体积重计算使用。
     * @author will
     * @date 2026/5/25 11:50
     * @param logisticsBillList 物流单集合
     * @return Map(channelId -> LogisticsChannelEntity)
     */
    private Map<String, LogisticsChannelEntity> loadChannelMap(List<LogisticsBillEntity> logisticsBillList) {
        if (CollUtil.isEmpty(logisticsBillList)) {
            return Collections.emptyMap();
        }
        List<String> channelIds = logisticsBillList.stream()
                .map(LogisticsBillEntity::getChannelId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(channelIds)) {
            return Collections.emptyMap();
        }
        List<LogisticsChannelEntity> channelList = logisticsChannelService.listByIds(channelIds);
        if (CollUtil.isEmpty(channelList)) {
            return Collections.emptyMap();
        }
        return channelList.stream()
                .filter(Objects::nonNull)
                .filter(item -> StringUtils.isNotBlank(item.getId()))
                .collect(Collectors.toMap(LogisticsChannelEntity::getId, Function.identity(), (a, b) -> a));
    }

    /**
     * 按"长*宽*高 / 渠道材积"回填装箱明细的体积重，
     * 公式与 TmsFirstMileLogisticServiceImpl.getCanGenerateDeliveryOrder 保持一致（保留 4 位、HALF_UP）。
     * 渠道为空或材积设置 &lt;= 0 时跳过，不会清掉已有的 volumeWeight。
     * @author will
     * @date 2026/5/25 11:50
     * @param packingDTOList 装箱明细
     * @param channelEntity 渠道实体（来自发货单关联的物流单 channel_id）
     */
    private void fillPackingVolumeWeight(List<TmsDeclareBillDTO.PackingDTO> packingDTOList,
                                         LogisticsChannelEntity channelEntity) {
        if (CollUtil.isEmpty(packingDTOList)
                || Objects.isNull(channelEntity)
                || Objects.isNull(channelEntity.getVolumeSetting())
                || channelEntity.getVolumeSetting() <= 0) {
            return;
        }
        BigDecimal divisor = BigDecimal.valueOf(channelEntity.getVolumeSetting());
        packingDTOList.forEach(packingDTO -> {
            if (Objects.isNull(packingDTO) || Objects.isNull(packingDTO.getMultiplySize())) {
                return;
            }
            packingDTO.setVolumeWeight(packingDTO.getMultiplySize().divide(divisor, 4, RoundingMode.HALF_UP));
        });
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
            mergeDetailDTO.setLeadSkuId(detailEntity.getSkuId());
            mergeDetailDTO.setSkuNo(detailEntity.getSkuNo());
            mergeDetailDTO.setHsCode(detailEntity.getCustomsCode());
            mergeDetailDTO.setProductNameCn(detailEntity.getDeclareChineseName());
            mergeDetailDTO.setDeclareElement(detailEntity.getDeclareElement());
            mergeDetailDTO.setUnit(detailEntity.getDeclareUnit());
            mergeDetailDTO.setUnitPrice(Objects.isNull(detailEntity.getPrice()) ? BigDecimal.ZERO : detailEntity.getPrice());
            mergeDetailDTO.setQty(Objects.isNull(detailEntity.getQty()) ? 0 : detailEntity.getQty());
            if (Objects.nonNull(detailEntity.getPrice()) && Objects.nonNull(detailEntity.getQty())) {
                mergeDetailDTO.setTotalAmount(detailEntity.getPrice().multiply(BigDecimal.valueOf(detailEntity.getQty())).setScale(4, RoundingMode.HALF_UP));
            }
            mergeDetailDTO.setDeclareCurrency(detailEntity.getDeclareCurrency());
            mergeDetailDTO.setDeclareCurrencySymbol(detailEntity.getDeclareCurrencySymbol());
            mergeDetailDTO.setSourceCountry(detailEntity.getSourceCountry());
            mergeDetailDTO.setSourceCountryName(detailEntity.getSourceCountryName());
            mergeDetailDTO.setToCountry(detailEntity.getToCountry());
            mergeDetailDTO.setToCountryName(detailEntity.getToCountryName());
            mergeDetailDTO.setSourceCargo(detailEntity.getSourceCargo());
            mergeDetailDTO.setExemption(detailEntity.getExemption());
            List<DeliveryDeclareDetailMidEntity> curMids = midGroupMap.getOrDefault(detailEntity.getId(), Collections.emptyList());
            String businessOrderNos = curMids.stream()
                    .map(DeliveryDeclareDetailMidEntity::getBusinessCode)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .sorted()
                    .collect(Collectors.joining(","));
            mergeDetailDTO.setBusinessOrderNos(businessOrderNos);
            List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailList = curMids.stream()
                    .map(this::buildSourceDeliveryDetailDTO)
                    .collect(Collectors.toList());
            mergeDetailDTO.setSourceDeliveryDetailList(sourceDetailList);
            return mergeDetailDTO;
        }).collect(Collectors.toList());
        viewDTO.setMergeDetailList(mergeDetailList);
        if(entity.getType().equals(SourceTypeEnum.FM_DECLARE_BILL.getCode())){
            List<TmsDeclareBillDTO.DeliveryDTO> deliveryDTOList = this.getCanGenerateDeliveryOrder(TmsDeclareBillDTO.QuerySourceDTO.builder().ids(listBillSourceDTO.getSourceIdList()).build());
            if(CollectionUtils.isEmpty(deliveryDTOList)){
                throw new ServiceException(ApiError.LOGISTICS_DECLARE_DELIVERY_NOT_FOUND);
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
            String logisticsSupplierIds = deliveryDTOList.stream().map(TmsDeclareBillDTO.DeliveryDTO::getLogisticsSupplierId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.joining(";"));
            viewDTO.setLogisticsSupplierId(logisticsSupplierIds);
            //物流供应商名称
            String logisticsSupplierNames = deliveryDTOList.stream().map(TmsDeclareBillDTO.DeliveryDTO::getLogisticsSupplierName).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.joining(";"));
            viewDTO.setLogisticsSupplierName(logisticsSupplierNames);
            // 运输方式 / 柜号都不再落 tms_declare_bill，详情时按发货单关联的 logistics_bill 反查回显。
            // 多发货单合并时用 ";" 拼接（与 supplier 的处理保持一致）。
            String shippingMethods = deliveryDTOList.stream()
                    .map(TmsDeclareBillDTO.DeliveryDTO::getShippingMethod)
                    .filter(CharSequenceUtil::isNotBlank)
                    .distinct()
                    .collect(Collectors.joining(";"));
            viewDTO.setShippingMethod(shippingMethods);
            String shippingMethodNames = deliveryDTOList.stream()
                    .map(TmsDeclareBillDTO.DeliveryDTO::getShippingMethodName)
                    .filter(CharSequenceUtil::isNotBlank)
                    .distinct()
                    .collect(Collectors.joining(";"));
            viewDTO.setShippingMethodName(shippingMethodNames);
            // 柜号 (counter_no) 没落 tms_declare_bill，按发货单关联的 logistics_bill 反查回显。
            // 提运单号 (transport_no) 是 tms_declare_bill 本表列：addFmDeclare 这条路径里没写；
            // batchAddMergeDetail 写了，BeanUtil.copyProperties(entity, ViewDTO) 已经搬到 viewDTO，这里不覆盖。
            String counterNos = deliveryDTOList.stream()
                    .map(TmsDeclareBillDTO.DeliveryDTO::getCounterNo)
                    .filter(CharSequenceUtil::isNotBlank)
                    .distinct()
                    .collect(Collectors.joining(";"));
            viewDTO.setCounterNo(counterNos);
        }else if(entity.getType().equals(SourceTypeEnum.B2B_DECLARE_BILL.getCode())){
            List<TmsDeclareBillDTO.SoOutDTO> deliveryDTOList = this.getCanGenerateSoOut(TmsDeclareBillDTO.QuerySourceDTO.builder().ids(listBillSourceDTO.getSourceIdList()).build());
            if(CollectionUtils.isEmpty(deliveryDTOList)){
                throw new ServiceException(ApiError.LOGISTICS_DECLARE_SO_OUT_NOT_FOUND);
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
            String logisticsSupplierIds = deliveryDTOList.stream().map(TmsDeclareBillDTO.SoOutDTO::getLogisticsSupplierId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.joining(";"));
            viewDTO.setLogisticsSupplierId(logisticsSupplierIds);
            //物流供应商名称
            String logisticsSupplierNames = deliveryDTOList.stream().map(TmsDeclareBillDTO.SoOutDTO::getLogisticsSupplierName).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.joining(";"));
            viewDTO.setLogisticsSupplierName(logisticsSupplierNames);
            //  运输方式 / 柜号同 FM，按发货通知单关联的 logistics_bill 反查回显。
            String shippingMethods = deliveryDTOList.stream()
                    .map(TmsDeclareBillDTO.SoOutDTO::getShippingMethod)
                    .filter(CharSequenceUtil::isNotBlank)
                    .distinct()
                    .collect(Collectors.joining(";"));
            viewDTO.setShippingMethod(shippingMethods);
            String shippingMethodNames = deliveryDTOList.stream()
                    .map(TmsDeclareBillDTO.SoOutDTO::getShippingMethodName)
                    .filter(CharSequenceUtil::isNotBlank)
                    .distinct()
                    .collect(Collectors.joining(";"));
            viewDTO.setShippingMethodName(shippingMethodNames);
            // B2B 的 transport_no 是页面手动录入，BeanUtil.copyProperties 已经把 entity.transport_no 搬到 viewDTO，
            // 这里只补 counterNo（来自物流单），二者互不覆盖。
            String counterNos = deliveryDTOList.stream()
                    .map(TmsDeclareBillDTO.SoOutDTO::getCounterNo)
                    .filter(CharSequenceUtil::isNotBlank)
                    .distinct()
                    .collect(Collectors.joining(";"));
            viewDTO.setCounterNo(counterNos);
        }

        fillViewDTO(viewDTO);
        return viewDTO;
    }

    @Override
    public List<TmsDeclareBillDTO.BatchUpdateFieldDropDownDTO> batchUpdateFieldDropDown(String type) {
        CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingEnum.DECLARE_BATCH_UPDATE_FIELD.getCode());
        if (ObjectUtil.isEmpty(cfgSettingEntity) || ObjectUtil.isEmpty(cfgSettingEntity.getDataJson())) {
            return Collections.emptyList();
        }
        List<TmsDeclareBillDTO.BatchUpdateFieldDropDownDTO> dropDownList = parseBatchUpdateFieldDropDown(type,cfgSettingEntity.getDataJson());
        dropDownList.sort(Comparator.comparing(TmsDeclareBillDTO.BatchUpdateFieldDropDownDTO::getIndex, Comparator.nullsLast(Integer::compareTo)));
        return dropDownList;
    }

    private List<TmsDeclareBillDTO.BatchUpdateFieldDropDownDTO> parseBatchUpdateFieldDropDown(String type,JSONObject dataJson) {
        if (ObjectUtil.isEmpty(dataJson)) {
            return new ArrayList<>();
        }
        List<TmsDeclareBillDTO.BatchUpdateFieldDropDownDTO> data = JSONUtil.toList(dataJson.getJSONArray("data"), TmsDeclareBillDTO.BatchUpdateFieldDropDownDTO.class).stream().filter(e -> e.getType().contains(type)).collect(Collectors.toList());;
        return data;
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
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public BatchResultDTO confirmDeclareStatus(TmsDeclareBillDTO.ConfirmDeclareStatusDTO dto, SourceTypeEnum sourceTypeEnum) {
        String id = Optional.ofNullable(dto.getIds()).orElse(Collections.emptyList()).stream()
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .orElse("");
        if (StringUtils.isBlank(id)) {
            return BatchResultDTO.fail("", "", "id不能为空");
        }
        TmsDeclareBillEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            return BatchResultDTO.fail(id, id, "报关单不存在");
        }
        try {
            Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.COMMON_NOT_EXIST_GENERIC, "报关单"));
            if (!sourceTypeEnum.getCode().equals(entity.getType())) {
                throw new ServiceException(ApiError.LOGISTICS_DECLARE_BILL_TYPE_MISMATCH);
            }
            DeclareStatusEnum targetStatus = DeclareStatusEnum.getEnum(dto.getDeclareStatus());
            if (Objects.isNull(targetStatus)) {
                throw new ServiceException(ApiError.LOGISTICS_DECLARE_STATUS_INVALID);
            }
            if (DeclareStatusEnum.WAIT.getCode().equals(entity.getDeclareStatus()) && DeclareStatusEnum.CONFIRMED.equals(targetStatus)) {
                fillDeclareConfirmUser(dto);
            }
            confirmDeclareStatusSingle(entity, dto, targetStatus, sourceTypeEnum);
            return BatchResultDTO.success(entity.getId(), entity.getCode(), "报关状态更新成功");
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("报关状态更新失败，id:{}", id, e);
            String msg = StringUtils.isNotBlank(e.getMessage()) ? e.getMessage() : "报关状态更新失败";
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), msg);
        }
    }

    private void confirmDeclareStatusSingle(TmsDeclareBillEntity entity, TmsDeclareBillDTO.ConfirmDeclareStatusDTO dto,
                                            DeclareStatusEnum targetStatus, SourceTypeEnum sourceTypeEnum) {
        String currentStatus = entity.getDeclareStatus();
        if (DeclareStatusEnum.WAIT.getCode().equals(currentStatus) && DeclareStatusEnum.CONFIRMED.equals(targetStatus)) {
            validateDeclareConfirm(entity);
            updateDeclareStatus(entity, targetStatus.getCode(), dto.getDeclareConfirmDate(), dto.getDeclareUserId(), dto.getDeclareUserName());
            String declareStatusMsg = CharSequenceUtil.format("{}变更为{}", DeclareStatusEnum.getName(currentStatus), DeclareStatusEnum.getName(targetStatus.getCode()));
            operateLogService.addModuleOperateLog(declareStatusMsg, sourceTypeEnum.getCode(), entity.getId(), "更新状态操作");
            return;
        }
        if (DeclareStatusEnum.CONFIRMED.getCode().equals(currentStatus) && DeclareStatusEnum.WAIT.equals(targetStatus)) {
            updateDeclareStatus(entity, targetStatus.getCode(), null, null, null);
            String declareStatusMsg = CharSequenceUtil.format("{}变更为{}", DeclareStatusEnum.getName(currentStatus), DeclareStatusEnum.getName(targetStatus.getCode()));
            operateLogService.addModuleOperateLog(declareStatusMsg, sourceTypeEnum.getCode(), entity.getId(), "更新状态操作");
            return;
        }
        if (DeclareStatusEnum.CONFIRMED.getCode().equals(currentStatus) && DeclareStatusEnum.DECLARED.equals(targetStatus)) {
            updateDeclareStatus(entity, targetStatus.getCode(), entity.getDeclareConfirmDate(), entity.getDeclareUserId(), entity.getDeclareUserName());
            String declareStatusMsg = CharSequenceUtil.format("{}变更为{}", DeclareStatusEnum.getName(currentStatus), DeclareStatusEnum.getName(targetStatus.getCode()));
            operateLogService.addModuleOperateLog(declareStatusMsg, sourceTypeEnum.getCode(), entity.getId(), "更新状态操作");
            return;
        }
        if (DeclareStatusEnum.DECLARED.getCode().equals(currentStatus) && DeclareStatusEnum.WAIT.equals(targetStatus)) {
            updateDeclareStatus(entity, targetStatus.getCode(), null, null, null);
            String declareStatusMsg = CharSequenceUtil.format("{}变更为{}", DeclareStatusEnum.getName(currentStatus), DeclareStatusEnum.getName(targetStatus.getCode()));
            operateLogService.addModuleOperateLog(declareStatusMsg, sourceTypeEnum.getCode(), entity.getId(), "更新状态操作");
            return;
        }
        throw new ServiceException(ApiError.LOGISTICS_DECLARE_STATUS_UPDATE_FORBIDDEN, DeclareStatusEnum.getName(currentStatus), targetStatus.getName());
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
        if (Objects.isNull(dto.getDeclareConfirmDate())) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_STATUS_CONFIRM_DATE_REQUIRED);
        }
        LoginUser loginUser = UserContext.getDefaultLoginUser();
        if (StringUtils.isBlank(dto.getDeclareUserId())) {
            dto.setDeclareUserId(loginUser.getUid());
            dto.setDeclareUserName(loginUser.getUserName());
        }else {
            FindUserDTO userDTO = sysUserFeign.getUserByUserId(dto.getDeclareUserId());
            if (Objects.nonNull(userDTO)) {
                dto.setDeclareUserName(userDTO.getUserName());
            }
        }
        if (StringUtils.isBlank(dto.getDeclareUserName())) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_STATUS_CONFIRM_USER_REQUIRED);
        }
    }

    private void updateDeclareStatus(TmsDeclareBillEntity entity, String declareStatus, LocalDate declareConfirmDate, String declareUserId, String declareUserName) {
        this.lambdaUpdate()
                .eq(TmsDeclareBillEntity::getId, entity.getId())
                .set(TmsDeclareBillEntity::getDeclareStatus, declareStatus)
                .set(TmsDeclareBillEntity::getDeclareConfirmDate, Objects.isNull(declareConfirmDate) ? null : declareConfirmDate)
                .set(TmsDeclareBillEntity::getDeclareUserId, Objects.isNull(declareUserId) ? "" : declareUserId)
                .set(TmsDeclareBillEntity::getDeclareUserName, Objects.isNull(declareUserName) ? "" : declareUserName)
                .update(new TmsDeclareBillEntity());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public List<BatchResultDTO> delete(TmsDeclareBillDTO.DeleteDTO dto) {
        List<TmsDeclareBillEntity> entityList = this.listByIds(dto.getIds());
        List<BatchResultDTO> resultList = new ArrayList<>();
        List<String> removeIds = new ArrayList<>();
        for (TmsDeclareBillEntity entity : entityList) {
            if(!entity.getDeclareStatus().equals(com.erp.model.tms.enums.DeclareStatusEnum.WAIT.getCode())){
                resultList.add(BatchResultDTO.fail(entity.getId(),entity.getCode(),"只有待确认的单据才能删除"));
                continue;
            }
            resultList.add(BatchResultDTO.success(entity.getId(),entity.getCode(),"删除成功"));
            removeIds.add(entity.getId());
        }
        if (CollectionUtils.isEmpty(removeIds)) {
            return resultList;
        }
        //查询中间表数据
        List<DeliveryDeclareDetailMidEntity> deliveryDeclareDetailMidList = deliveryDeclareDetailMidService.listByDeclareBillIdList(removeIds);
        if (CollUtil.isEmpty(deliveryDeclareDetailMidList)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_NOT_FOUND);
        }
        if(CollectionUtils.isNotEmpty(removeIds)){
            this.removeByIds(removeIds);
        }
        //删除明细数据
        detailService.deleteDetailByMainIdList(removeIds);
        //中间表恢复为待生成，不删除历史来源明细
        deliveryDeclareDetailMidService.restoreWaitGenerateByDeclareBillIds(removeIds);
        updateWaitStatusForNoGeneratedSources(deliveryDeclareDetailMidList);
        return resultList;
    }

    /**
     * 对恢复为待生成的来源单据回写待报关状态。
     *
     * @param deliveryDeclareDetailMidList 本次恢复的中间表明细
     */
    private void updateWaitStatusForNoGeneratedSources(List<DeliveryDeclareDetailMidEntity> deliveryDeclareDetailMidList) {
        if (CollUtil.isEmpty(deliveryDeclareDetailMidList)) {
            return;
        }
        // 头程来源只在不存在其它已生成中间表时回写为待报关。
        List<String> fmSourceIdList = deliveryDeclareDetailMidList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSourceType(), SourceTypeEnum.FIRST_MILE_DELIVERY.getCode()))
                .map(DeliveryDeclareDetailMidEntity::getSourceId).distinct().collect(Collectors.toList());
        fmSourceIdList = filterNoGeneratedSourceIds(fmSourceIdList, SourceTypeEnum.FIRST_MILE_DELIVERY.getCode());
        if(CollectionUtils.isNotEmpty(fmSourceIdList)){
            FirstMileDeliveryDTO.UpdateStatusDTO updateStatusDTO = new FirstMileDeliveryDTO.UpdateStatusDTO();
            updateStatusDTO.setIds(fmSourceIdList);
            updateStatusDTO.setDeclareStatus(WmsDeclareStatusEnum.WAIT.code);
            wmsFirstMileDeliveryFeign.updateStatus(updateStatusDTO);
        }

        // B2B来源只在不存在其它已生成中间表时回写为待报关。
        List<String> b2bSourceIdList = deliveryDeclareDetailMidList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSourceType(), SourceTypeEnum.SO_DELIVERY_NOTICE.getCode()))
                .map(DeliveryDeclareDetailMidEntity::getSourceId).distinct().collect(Collectors.toList());
        b2bSourceIdList = filterNoGeneratedSourceIds(b2bSourceIdList, SourceTypeEnum.SO_DELIVERY_NOTICE.getCode());
        if(CollectionUtils.isNotEmpty(b2bSourceIdList)){
            SoDeliveryNoticeDTO.DeclareStatusDTO updateStatusDTO = new SoDeliveryNoticeDTO.DeclareStatusDTO();
            updateStatusDTO.setIds(b2bSourceIdList);
            updateStatusDTO.setDeclareStatus(WmsDeclareStatusEnum.WAIT.code);
            soDeliveryNoticeFeign.updateDeclareStatus(updateStatusDTO);
        }
    }

    /**
     * 过滤仍存在已生成中间表的来源单据。
     *
     * @param sourceIds 候选来源单据id集合
     * @param sourceType 来源类型
     * @return 可回写待报关状态的来源单据id集合
     */
    private List<String> filterNoGeneratedSourceIds(List<String> sourceIds, String sourceType) {
        if (CollectionUtils.isEmpty(sourceIds)) {
            return Collections.emptyList();
        }
        // 查询同来源单据下是否仍有已生成报关信息。
        List<DeliveryDeclareDetailMidEntity> generatedMidList = deliveryDeclareDetailMidService.lambdaQuery()
                .in(DeliveryDeclareDetailMidEntity::getSourceId, sourceIds)
                .eq(DeliveryDeclareDetailMidEntity::getSourceType, sourceType)
                .eq(DeliveryDeclareDetailMidEntity::getGenerateStatus, DeliveryDeclareDetailMidGenerateStatusEnum.FINISH.getCode())
                .list();
        if (CollUtil.isEmpty(generatedMidList)) {
            return sourceIds;
        }
        // 有已生成记录的来源单据不能回写待报关。
        Set<String> generatedSourceIdSet = generatedMidList.stream()
                .map(DeliveryDeclareDetailMidEntity::getSourceId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        return sourceIds.stream()
                .filter(sourceId -> !generatedSourceIdSet.contains(sourceId))
                .collect(Collectors.toList());
    }

    /**
     * 编辑报关单时同步新旧中间表。
     *
     * @param oldMidList 编辑前已绑定当前报关单的中间表
     * @param newMidList 编辑后需要绑定当前报关单的中间表
     * @param declareId 报关单id
     * @param declareCode 报关单号
     */
    private void saveOrRestoreUpdateMidData(List<DeliveryDeclareDetailMidEntity> oldMidList,
                                            List<DeliveryDeclareDetailMidEntity> newMidList,
                                            String declareId,
                                            String declareCode) {
        // 用来源单据+箱号+SKU识别同一来源箱明细。
        Map<String, DeliveryDeclareDetailMidEntity> oldMidMap = CollUtil.isEmpty(oldMidList)
                ? new HashMap<>()
                : oldMidList.stream().collect(Collectors.toMap(this::buildSourceDetailKey, item -> item, (a, b) -> a));
        Set<String> retainedOldMidIds = new HashSet<>();
        List<DeliveryDeclareDetailMidEntity> addMidList = new ArrayList<>();
        for (DeliveryDeclareDetailMidEntity newMid : Optional.ofNullable(newMidList).orElse(Collections.emptyList())) {
            DeliveryDeclareDetailMidEntity oldMid = oldMidMap.get(buildSourceDetailKey(newMid));
            if (Objects.isNull(oldMid)) {
                // 新增的来源箱明细直接保存为已生成。
                addMidList.add(newMid);
                continue;
            }
            // 保留的来源箱明细复用旧中间表行，并更新报关关联。
            retainedOldMidIds.add(oldMid.getId());
            updateRetainedMidData(oldMid, newMid, declareId, declareCode);
        }
        if (CollUtil.isNotEmpty(addMidList)) {
            deliveryDeclareDetailMidService.saveBatch(addMidList);
        }
        List<DeliveryDeclareDetailMidEntity> restoreMidList = Optional.ofNullable(oldMidList).orElse(Collections.emptyList()).stream()
                .filter(item -> !retainedOldMidIds.contains(item.getId()))
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(restoreMidList)) {
            return;
        }
        // 编辑时被删除的来源箱明细恢复为待生成。
        deliveryDeclareDetailMidService.restoreWaitGenerateByIds(restoreMidList.stream()
                .map(DeliveryDeclareDetailMidEntity::getId)
                .collect(Collectors.toList()));
        // 删除来源后，必要时回写来源单据为待报关。
        updateWaitStatusForNoGeneratedSources(restoreMidList);
    }

    /**
     * 更新编辑后仍保留的中间表行。
     *
     * @param oldMid 旧中间表行
     * @param newMid 新构建的中间表数据
     * @param declareId 报关单id
     * @param declareCode 报关单号
     */
    private void updateRetainedMidData(DeliveryDeclareDetailMidEntity oldMid,
                                       DeliveryDeclareDetailMidEntity newMid,
                                       String declareId,
                                       String declareCode) {
        // 复用旧行主键，只刷新报关关联和可能变更的申报字段。
        deliveryDeclareDetailMidService.lambdaUpdate()
                .eq(DeliveryDeclareDetailMidEntity::getId, oldMid.getId())
                .set(DeliveryDeclareDetailMidEntity::getGenerateStatus, DeliveryDeclareDetailMidGenerateStatusEnum.FINISH.getCode())
                .set(DeliveryDeclareDetailMidEntity::getDeclareId, declareId)
                .set(DeliveryDeclareDetailMidEntity::getDeclareCode, declareCode)
                .set(DeliveryDeclareDetailMidEntity::getDeclareDetailId, newMid.getDeclareDetailId())
                .set(DeliveryDeclareDetailMidEntity::getContractNo, declareCode)
                .set(DeliveryDeclareDetailMidEntity::getQty, newMid.getQty())
                .set(DeliveryDeclareDetailMidEntity::getCurrency, newMid.getCurrency())
                .set(DeliveryDeclareDetailMidEntity::getCurrencySymbol, newMid.getCurrencySymbol())
                .set(DeliveryDeclareDetailMidEntity::getHsCode, newMid.getHsCode())
                .set(DeliveryDeclareDetailMidEntity::getProductNameCn, newMid.getProductNameCn())
                .set(DeliveryDeclareDetailMidEntity::getDeclareElement, newMid.getDeclareElement())
                .set(DeliveryDeclareDetailMidEntity::getUnit, newMid.getUnit())
                .set(DeliveryDeclareDetailMidEntity::getUnitPrice, newMid.getUnitPrice())
                .update();
    }

    private void fillExport(List<TmsDeclareBillDTO.ExportDTO> list) {
        fillExport(list, loadDeclareExportAccountingCompanyMap(list));
    }

    private void fillExport(List<TmsDeclareBillDTO.ExportDTO> list,
                            Map<String, SysAccountingCompanyEntity> accountingCompanyMap) {
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        List<String> ids = list.stream().map(TmsDeclareBillDTO.ExportDTO::getId).collect(Collectors.toList());
        List<TmsDeclareBillDetailEntity> detailList = detailService.listByMainIds(ids);
        List<TmsDeclareBillDTO.ExportProductDetail> allExportProductDetailList = BeanUtil.copyToList(detailList,TmsDeclareBillDTO.ExportProductDetail.class);
        List<String> sourceCodeList = list.stream().map(TmsDeclareBillDTO.ExportDTO::getSourceCode).distinct().collect(Collectors.toList());
        List<LogisticsBillEntity> logisticsBillEntityList = fmLogisticService.listByOutstcockCode(sourceCodeList);

        Map<String, SysAccountingCompanyEntity> allAccountingCompanyMap = accountingCompanyMap;
        if (Objects.isNull(allAccountingCompanyMap)) {
            allAccountingCompanyMap = loadDeclareExportAccountingCompanyMap(list);
        }

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
            SysAccountingCompanyEntity senderCompanyEntity = allAccountingCompanyMap.get(exportDTO.getSenderId());
            if(Objects.nonNull(senderCompanyEntity)){
                exportDTO.setSenderCode(senderCompanyEntity.getUsciCode()+"("+senderCompanyEntity.getCompanyHsCode()+")");
            }
            SysAccountingCompanyEntity receiverCompanyEntity = allAccountingCompanyMap.get(exportDTO.getReceiverId());
            if(Objects.nonNull(receiverCompanyEntity)){
                exportDTO.setReceiverCode(receiverCompanyEntity.getUsciCode()+"("+receiverCompanyEntity.getCompanyHsCode()+")");
            }

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
                detail.setBusinessCode(exportDTO.getBusinessCode());
            }
            exportDTO.setTotalQty(exportProductDetailList.stream().mapToInt(TmsDeclareBillDTO.ExportProductDetail::getQty).sum());
            exportDTO.setTotalPrice(exportProductDetailList.stream().map(TmsDeclareBillDTO.ExportProductDetail::getTotalPrice).reduce(BigDecimal.ZERO,BigDecimal::add));
            exportDTO.setProductDetailList(exportProductDetailList);
        }
    }

    /**
     * 报关单导出：批量加载发货人 / 收货人核算公司，供 fillExport 与 fillExportMulti 共用，避免重复 Feign
     */
    private Map<String, SysAccountingCompanyEntity> loadDeclareExportAccountingCompanyMap(List<TmsDeclareBillDTO.ExportDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }
        Set<String> orgIdSet = new HashSet<>();
        for (TmsDeclareBillDTO.ExportDTO dto : list) {
            if (StringUtils.isNotBlank(dto.getSenderId())) {
                orgIdSet.add(dto.getSenderId());
            }
            if (StringUtils.isNotBlank(dto.getReceiverId())) {
                orgIdSet.add(dto.getReceiverId());
            }
        }
        if (orgIdSet.isEmpty()) {
            return Collections.emptyMap();
        }
        return Optional.ofNullable(sysUserFeign.listCompanyById(new ArrayList<>(orgIdSet)))
                .orElse(Collections.emptyList())
                .stream()
                .collect(Collectors.toMap(SysAccountingCompanyEntity::getId, Function.identity(), (v1, v2) -> v1));
    }

    @Override
    public List<TmsDeclareBillEntity> listBySourceIds(List<String> sourceIds) {
        if (CollectionUtil.isEmpty(sourceIds)) {
            return Collections.emptyList();
        }

        List<DeliveryDeclareDetailMidEntity> deliveryDeclareDetailMidList = deliveryDeclareDetailMidService.listBySourceIdList(sourceIds);
        if (CollUtil.isEmpty(deliveryDeclareDetailMidList)) {
            return Collections.emptyList();
        }
        List<String> declareIds =deliveryDeclareDetailMidList.stream().map(DeliveryDeclareDetailMidEntity::getDeclareId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        if (CollectionUtil.isEmpty(declareIds)) {
            return Collections.emptyList();
        }
        return this.listByIds(declareIds);
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
        //  与 FM 端 getCanGenerateDeliveryOrder 对齐，按发货通知单关联的物流单 channel_id 批量加载渠道，
        // 用于装箱明细的体积重回填。
        Map<String, LogisticsChannelEntity> channelMap = loadChannelMap(logisticsBillEntityList);
        for (TmsDeclareBillDTO.SoOutDTO deliveryDTO : deliveryDTOList) {
            LogisticsBillEntity logisticsBillEntity = logisticsBillEntityList.stream().filter(v->v.getOutstockId().equals(deliveryDTO.getSourceId())).findFirst().orElse(new LogisticsBillEntity());
            LogisticsSupplierEntity logisticsSupplierEntity = logisticsSupplierEntityList.stream().filter(v->v.getId().equals(logisticsBillEntity.getLogisticsSupplierId())).findFirst().orElse(new LogisticsSupplierEntity());
            deliveryDTO.setShippingMethod(logisticsBillEntity.getShippingMethod());
            deliveryDTO.setShippingMethodName(LogisticsMethodEnum.getName(logisticsBillEntity.getShippingMethod()));
            deliveryDTO.setLogisticsSupplierId(logisticsBillEntity.getLogisticsSupplierId());
            deliveryDTO.setLogisticsSupplierName(logisticsSupplierEntity.getSupplierName());
            //  与 FM 端 getCanGenerateDeliveryOrder 对齐，柜号需要带出来用于保存到 transport_no
            deliveryDTO.setCounterNo(logisticsBillEntity.getCounterNo());
            fillPackingVolumeWeight(deliveryDTO.getPackingDTOList(), channelMap.get(logisticsBillEntity.getChannelId()));
        }
        return deliveryDTOList;
    }

    /**
     * 根据选中SKU查询报关表头信息
     * @author will
     * @date 2026/5/7 14:47
     * @param dto
     * @param sourceTypeEnum
     * @return com.erp.model.tms.dto.TmsDeclareBillDTO.SelectedSkuHeaderDTO
     */
    @Override
    public TmsDeclareBillDTO.SelectedSkuHeaderDTO querySelectedSkuHeader(TmsDeclareBillDTO.SelectedSkuHeaderParamDTO dto,
                                                                         SourceTypeEnum sourceTypeEnum) {
        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> selectedDetailList = dto.getSourceDeliveryDetailList().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(selectedDetailList)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_SELECTED_SKU_REQUIRED);
        }
        List<String> sourceIdList = selectedDetailList.stream()
                .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getSourceId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(sourceIdList)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_SOURCE_HEADER_NOT_FOUND);
        }

        TmsDeclareBillDTO.SelectedSkuHeaderDTO headerDTO = new TmsDeclareBillDTO.SelectedSkuHeaderDTO();
        List<TmsDeclareBillDTO.PackingDTO> packingDTOList;
        List<LogisticsBillEntity> logisticsBillList;
        if (SourceTypeEnum.FM_DECLARE_BILL == sourceTypeEnum) {
            List<TmsDeclareBillDTO.DeliveryDTO> deliveryDTOList = getCanGenerateDeliveryOrder(TmsDeclareBillDTO.QuerySourceDTO.builder().ids(sourceIdList).build());
            packingDTOList = deliveryDTOList.stream()
                    .filter(item -> CollUtil.isNotEmpty(item.getPackingDTOList()))
                    .flatMap(item -> item.getPackingDTOList().stream())
                    .collect(Collectors.toList());
            logisticsBillList = fmLogisticService.listByOutstcockCode(deliveryDTOList.stream()
                    .map(TmsDeclareBillDTO.DeliveryDTO::getSourceCode)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList()));
        } else if (SourceTypeEnum.B2B_DECLARE_BILL == sourceTypeEnum) {
            List<TmsDeclareBillDTO.SoOutDTO> deliveryDTOList = getCanGenerateSoOut(TmsDeclareBillDTO.QuerySourceDTO.builder().ids(sourceIdList).build());
            packingDTOList = deliveryDTOList.stream()
                    .filter(item -> CollUtil.isNotEmpty(item.getPackingDTOList()))
                    .flatMap(item -> item.getPackingDTOList().stream())
                    .collect(Collectors.toList());
            logisticsBillList = logisticService.listByOutstockCodeList(deliveryDTOList.stream()
                    .map(TmsDeclareBillDTO.SoOutDTO::getSoOutstockCode)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList()));
        } else {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_BILL_TYPE_MISMATCH);
        }

        fillHeaderLogistics(headerDTO, logisticsBillList);
        fillHeaderWeight(headerDTO, selectedDetailList, packingDTOList);
        return headerDTO;
    }

    /**
     * 填充报关表头物流信息
     * @author will
     * @date 2026/5/7 14:47
     * @param headerDTO
     * @param logisticsBillList
     */
    private void fillHeaderLogistics(TmsDeclareBillDTO.SelectedSkuHeaderDTO headerDTO,
                                     List<LogisticsBillEntity> logisticsBillList) {
        if (CollUtil.isEmpty(logisticsBillList)) {
            headerDTO.setShippingMethod("");
            headerDTO.setShippingMethodName("");
            headerDTO.setLogisticsSupplierId("");
            headerDTO.setLogisticsSupplierName("");
            headerDTO.setTransportNo("");
            return;
        }
        headerDTO.setShippingMethod(joinDistinct(logisticsBillList.stream()
                .map(LogisticsBillEntity::getShippingMethod)
                .collect(Collectors.toList())));
        headerDTO.setShippingMethodName(joinDistinct(logisticsBillList.stream()
                .map(item -> LogisticsMethodEnum.getName(item.getShippingMethod()))
                .collect(Collectors.toList())));
        headerDTO.setLogisticsSupplierId(joinDistinct(logisticsBillList.stream()
                .map(LogisticsBillEntity::getLogisticsSupplierId)
                .collect(Collectors.toList())));
        List<String> supplierIds = logisticsBillList.stream()
                .map(LogisticsBillEntity::getLogisticsSupplierId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        Map<String, String> supplierNameMap = CollUtil.isEmpty(supplierIds)
                ? new HashMap<>()
                : logisticsSupplierService.listByIds(supplierIds).stream()
                .collect(Collectors.toMap(LogisticsSupplierEntity::getId, LogisticsSupplierEntity::getSupplierName, (a, b) -> a));
        headerDTO.setLogisticsSupplierName(joinDistinct(logisticsBillList.stream()
                .map(item -> supplierNameMap.get(item.getLogisticsSupplierId()))
                .collect(Collectors.toList())));
        headerDTO.setTransportNo(joinDistinct(logisticsBillList.stream()
                .map(item -> StringUtils.defaultIfBlank(item.getTransportNo(), item.getCounterNo()))
                .collect(Collectors.toList())));
    }

    /**
     * 填充报关表头件数和重量
     * @author will
     * @date 2026/5/7 14:47
     * @param headerDTO
     * @param selectedDetailList
     * @param packingDTOList
     */
    private void fillHeaderWeight(TmsDeclareBillDTO.SelectedSkuHeaderDTO headerDTO,
                                  List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> selectedDetailList,
                                  List<TmsDeclareBillDTO.PackingDTO> packingDTOList) {
        Set<String> selectedBoxKeySet = selectedDetailList.stream()
                .filter(item -> StringUtils.isNotBlank(item.getBoxNo()))
                .map(this::buildSelectedBoxKeyList)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        Set<String> selectedBoxCountKeySet = selectedDetailList.stream()
                .filter(item -> StringUtils.isNotBlank(item.getBoxNo()))
                .map(item -> buildSelectedBoxKey(StringUtils.defaultIfBlank(item.getSourceId(), item.getSourceCode()), item.getBoxNo()))
                .collect(Collectors.toSet());
        List<TmsDeclareBillDTO.PackingDTO> selectedPackingList = packingDTOList.stream()
                .filter(item -> selectedBoxKeySet.contains(buildSelectedBoxKey(item.getSourceId(), item.getBoxNo()))
                        || selectedBoxKeySet.contains(buildSelectedBoxKey(item.getSourceCode(), item.getBoxNo())))
                .collect(Collectors.toList());
        headerDTO.setBoxQty(selectedBoxCountKeySet.size());
        headerDTO.setGrossWeight(selectedPackingList.stream()
                .collect(Collectors.toMap(this::buildPackingBoxKey, item -> parseWeight(item.getPackageWeight()), (a, b) -> a))
                .values()
                .stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        headerDTO.setNetWeight(calculateSelectedNetWeight(selectedDetailList));
    }

    /**
     * 计算选中SKU净重
     * @author will
     * @date 2026/5/7 14:47
     * @param selectedDetailList
     * @return java.math.BigDecimal
     */
    private BigDecimal calculateSelectedNetWeight(List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> selectedDetailList) {
        List<String> skuIdList = selectedDetailList.stream()
                .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getSkuId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(skuIdList)) {
            return BigDecimal.ZERO;
        }
        List<ProductDetailDTO.ProductLogisticDTO> productLogisticsList = plmTaskFeign.listProductLogisticsByIds(skuIdList);
        Map<String, ProductDetailDTO.ProductLogisticDTO> logisticsMap = CollUtil.isEmpty(productLogisticsList)
                ? new HashMap<>()
                : productLogisticsList.stream().collect(Collectors.toMap(ProductDetailDTO.ProductLogisticDTO::getSkuId, item -> item, (a, b) -> a));
        BigDecimal netWeight = BigDecimal.ZERO;
        for (TmsDeclareBillDTO.SourceDeliveryDetailDTO detailDTO : selectedDetailList) {
            ProductDetailDTO.ProductLogisticDTO productLogisticsDTO = logisticsMap.get(detailDTO.getSkuId());
            if (Objects.isNull(productLogisticsDTO)) {
                continue;
            }
            netWeight = netWeight.add(calculateProductNetWeight(productLogisticsDTO, detailDTO.getQty()));
        }
        return netWeight;
    }

    /**
     * 计算产品净重
     * @author will
     * @date 2026/5/7 14:47
     * @param productLogisticsDTO
     * @param qty
     * @return java.math.BigDecimal
     */
    private BigDecimal calculateProductNetWeight(ProductDetailDTO.ProductLogisticDTO productLogisticsDTO, Integer qty) {
        int qtyValue = Objects.isNull(qty) ? 0 : qty;
        if (CombinationDeclareTypeEnums.SPLIT.getCode().equals(productLogisticsDTO.getCombinationDeclareType())
                && Boolean.TRUE.equals(productLogisticsDTO.getIsCombination())
                && CollUtil.isNotEmpty(productLogisticsDTO.getChildList())) {
            return productLogisticsDTO.getChildList().stream()
                    .map(item -> calculateSingleSkuNetWeight(item.getNetWeight(), qtyValue * (Objects.isNull(item.getChildQty()) ? 1 : item.getChildQty())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return calculateSingleSkuNetWeight(productLogisticsDTO.getNetWeight(), qtyValue);
    }

    /**
     * 计算单个SKU净重
     * @author will
     * @date 2026/5/7 14:47
     * @param netWeight
     * @param qty
     * @return java.math.BigDecimal
     */
    private BigDecimal calculateSingleSkuNetWeight(BigDecimal netWeight, Integer qty) {
        if (Objects.isNull(netWeight) || Objects.isNull(qty)) {
            return BigDecimal.ZERO;
        }
        return netWeight.multiply(BigDecimal.valueOf(qty)).divide(new BigDecimal(1000), 4, RoundingMode.HALF_UP);
    }

    /**
     * 构建选中箱号匹配键集合
     * @author will
     * @date 2026/5/7 14:47
     * @param detailDTO
     * @return java.util.List<java.lang.String>
     */
    private List<String> buildSelectedBoxKeyList(TmsDeclareBillDTO.SourceDeliveryDetailDTO detailDTO) {
        List<String> keyList = new ArrayList<>();
        if (StringUtils.isNotBlank(detailDTO.getSourceId())) {
            keyList.add(buildSelectedBoxKey(detailDTO.getSourceId(), detailDTO.getBoxNo()));
        }
        if (StringUtils.isNotBlank(detailDTO.getSourceCode())) {
            keyList.add(buildSelectedBoxKey(detailDTO.getSourceCode(), detailDTO.getBoxNo()));
        }
        return keyList;
    }

    /**
     * 构建装箱箱号匹配键
     * @author will
     * @date 2026/5/7 14:47
     * @param packingDTO
     * @return java.lang.String
     */
    private String buildPackingBoxKey(TmsDeclareBillDTO.PackingDTO packingDTO) {
        return buildSelectedBoxKey(StringUtils.defaultIfBlank(packingDTO.getSourceId(), packingDTO.getSourceCode()), packingDTO.getBoxNo());
    }

    /**
     * 构建选中箱号匹配键
     * @author will
     * @date 2026/5/7 14:47
     * @param sourceKey
     * @param boxNo
     * @return java.lang.String
     */
    private String buildSelectedBoxKey(String sourceKey, String boxNo) {
        return CharSequenceUtil.join("|", StringUtils.defaultString(sourceKey), StringUtils.defaultString(boxNo));
    }

    /**
     * 拼接去重字符串
     * @author will
     * @date 2026/5/7 14:47
     * @param valueList
     * @return java.lang.String
     */
    private String joinDistinct(List<String> valueList) {
        if (CollUtil.isEmpty(valueList)) {
            return "";
        }
        return valueList.stream()
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.joining(","));
    }

    /**
     * 转换重量
     * @author will
     * @date 2026/5/7 14:47
     * @param weight
     * @return java.math.BigDecimal
     */
    private BigDecimal parseWeight(String weight) {
        if (StringUtils.isBlank(weight)) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(weight.trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
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
    @DistributeLocker(
            businessType = DistributeKeyConstant.TMS_DECLARE_BILL_SOURCE_KEY,
            keyName = "addDTO.mergeDetailList.sourceDeliveryDetailList.sourceId",
            maxRetries = 1,
            unlockAfterTx = true
    )
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean addB2BDeclare(TmsDeclareBillDTO.AddDTO addDTO) {
        List<String> sourceIdList = resolveAddSourceIdList(addDTO);
        TmsDeclareBillDTO.QuerySourceDTO querySourceDTO = TmsDeclareBillDTO.QuerySourceDTO.builder()
//                .packingStatus(PackingTaskStatusEnum.PACKED.getCode())
                .declareStatus(WmsDeclareStatusEnum.WAIT.getCode())
                .ids(sourceIdList)
                .build();
        if (!Boolean.TRUE.equals(addDTO.getIsAuto())) {
            querySourceDTO.setPackingStatus(PackingTaskStatusEnum.PACKED.getCode());
        }
        List<TmsDeclareBillDTO.SoOutDTO> deliveryDTOList = this.getCanGenerateSoOut(querySourceDTO);
        if (CollectionUtils.isEmpty(deliveryDTOList)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_GENERATABLE_SO_DELIVERY_NOT_FOUND);
        }
        TmsDeclareBillDTO.SoOutDTO deliveryDTO = deliveryDTOList.get(0);
        // 新增页面下推保存逻辑：按前端提交的合并明细直接生成报关单
        if (CollUtil.isNotEmpty(addDTO.getMergeDetailList())) {
            List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> mergeDetailList = prepareSubmittedMergeDetailList(addDTO.getMergeDetailList(), addDTO.getIsMerge());
            validateDeclareMergeDetails(mergeDetailList);
            // 与 FM 对齐，目的国为中国大陆则整批失败，列出命中的来源单号，来源单状态不变。
            validateDestCountryNotMainlandChina(mergeDetailList);
            Set<String> sourceKeySet = collectSourceKeySet(mergeDetailList);
            validateSourceNotGenerated(sourceKeySet, collectSourceIdSet(mergeDetailList), SourceTypeEnum.SO_DELIVERY_NOTICE.getCode(), null);

            TmsDeclareBillEntity declareBillEntity = new TmsDeclareBillEntity();
            BeanMapperUtils.copy(addDTO, declareBillEntity);
            BeanMapperUtils.copy(deliveryDTO, declareBillEntity);
            declareBillEntity.setDeclareStatus(com.erp.model.tms.enums.DeclareStatusEnum.WAIT.getCode());
            declareBillEntity.setType(SourceTypeEnum.B2B_DECLARE_BILL.getCode());
            declareBillEntity.setDeclareDate(Objects.isNull(declareBillEntity.getDeclareDate()) ? LocalDate.now() : declareBillEntity.getDeclareDate());
            declareBillEntity.setShippingFee(Objects.isNull(declareBillEntity.getShippingFee()) ? BigDecimal.ZERO : declareBillEntity.getShippingFee());
            declareBillEntity.setInsuranceFee(Objects.isNull(declareBillEntity.getInsuranceFee()) ? BigDecimal.ZERO : declareBillEntity.getInsuranceFee());
            declareBillEntity.setOtherFee(Objects.isNull(declareBillEntity.getOtherFee()) ? BigDecimal.ZERO : declareBillEntity.getOtherFee());
            declareBillEntity.setGrossWeight(Objects.isNull(declareBillEntity.getGrossWeight()) ? BigDecimal.ZERO : declareBillEntity.getGrossWeight());
            // B2B 同 FM，新合并路径复用 calculateSelectedNetWeight 真实累加净重。
            declareBillEntity.setNetWeight(calculateSelectedNetWeight(flattenMergeSourceDetails(mergeDetailList)));
            // 贸易国默认中国香港。
            applyTradingAreaDefault(declareBillEntity);

            List<TmsDeclareBillDetailEntity> detailEntityList = new ArrayList<>(mergeDetailList.size());
            for (TmsDeclareBillDTO.MergeDeclareBillDetailDTO detailDTO : mergeDetailList) {
                TmsDeclareBillDetailEntity detailEntity = new TmsDeclareBillDetailEntity();
                mapMergeDeclareDetailToEntity(detailDTO, detailEntity);
                detailEntityList.add(detailEntity);
            }
            if (CollUtil.isEmpty(detailEntityList)) {
                throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_SAVE_REQUIRED);
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
            BaseResultDTO.AddDTO addResult = service.add(declareBillEntity, detailEntityList, SourceTypeEnum.B2B_DECLARE_BILL, false);

            List<DeliveryDeclareDetailMidEntity> addMidList = buildDeclareDetailMidList(mergeDetailList, detailEntityList,
                    SourceTypeEnum.SO_DELIVERY_NOTICE.getCode(), firstSourceId(sourceIdList), addResult.getId(), addResult.getCode());
            if (CollUtil.isNotEmpty(addMidList)) {
                deliveryDeclareDetailMidService.saveBatch(addMidList);
            }
            updateSourceDeclareStatus(SourceTypeEnum.B2B_DECLARE_BILL.getCode(), addMidList);
            return Boolean.TRUE;
        }

        TmsDeclareBillEntity baseTmsDeclareBillEntity = new TmsDeclareBillEntity();
        BeanMapperUtils.copy(addDTO, baseTmsDeclareBillEntity);
        BeanMapperUtils.copy(deliveryDTO, baseTmsDeclareBillEntity);
        baseTmsDeclareBillEntity.setDeclareStatus(com.erp.model.tms.enums.DeclareStatusEnum.WAIT.getCode());
        baseTmsDeclareBillEntity.setType(SourceTypeEnum.B2B_DECLARE_BILL.getCode());
        throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_SAVE_REQUIRED);
    }


    @Override
    public PagingVO<TmsDeclareBillDTO.PagingVO> export(PagingDTO<TmsDeclareBillDTO.PagingParamDTO> dto) {
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
    public void exportDeclare(TmsDeclareBillDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response) throws IOException {
        pagingParamDTO.setExportDeclareStatus(Arrays.asList(DeclareStatusEnum.DECLARED.getCode(), DeclareStatusEnum.WAIT.getCode(), DeclareStatusEnum.CONFIRMED.getCode()));
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
    public void exportDeclareMulti(TmsDeclareBillDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response) throws IOException {
        // 复用单 sheet 导出的状态过滤口径，保持业务边界一致
        pagingParamDTO.setExportDeclareStatus(Arrays.asList(DeclareStatusEnum.DECLARED.getCode(), DeclareStatusEnum.WAIT.getCode(), DeclareStatusEnum.CONFIRMED.getCode()));
        List<TmsDeclareBillDTO.ExportDTO> list = baseMapper.exportDeclare(pagingParamDTO);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.FILE_EXPORT_DATA_EMPTY);
        }
        if (list.size() > DECLARE_MULTI_EXPORT_LIMIT) {
            throw new ServiceException(ApiError.FILE_EXPORT_SIZE_EXCEED_LIMIT, DECLARE_MULTI_EXPORT_LIMIT);
        }

        Map<String, SysAccountingCompanyEntity> accountingCompanyMap = loadDeclareExportAccountingCompanyMap(list);
        fillExport(list, accountingCompanyMap);
        fillExportMulti(list, accountingCompanyMap);

        String name = "报关单导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);

        if (list.size() == 1) {
            TmsDeclareBillDTO.ExportDTO exportDTO = list.get(0);
            try {
                writeMultiSheet(exportDTO, response, date + name);
            } catch (Exception e) {
                log.error("多 sheet 报关单导出失败, 单号【{}】", exportDTO.getCode(), e);
                throw new ServiceException(ApiError.FILE_EXPORT_FAILED);
            }
            return;
        }

        try {
            writeMultiSheetZip(list, response, "报关单" + date);
        } catch (Exception e) {
            log.error("多 sheet 报关单 ZIP 导出失败", e);
            throw new ServiceException(ApiError.FILE_EXPORT_FAILED);
        }
    }

    /**
     * 在 fillExport 基础上补充多 sheet 导出所需的合同 / 发票 / 装箱单 / 装箱明细 信息
     *
     * <p>业务规则：</p>
     * <ul>
     *   <li>卖方地址：复用 fillExport 已批量加载的核算公司 companyAddress</li>
     *   <li>买方地址：头程按 ExportDTO.receiverId 查核算公司；B2B 按中间表关联的发货通知单
     *       receive_address，为空时再取客户主数据 mail_address</li>
     *   <li>合同号 = code；合同日期 = declareDate；目的地 = countryName；付款条件 = dictTransactionMethodName</li>
     *   <li>币别取明细首行币别，多币别仅打 warn 不抛异常</li>
     *   <li>装箱单 sheet 明细净重 = product_pack.net_weight × qty（4 位精度），TOTAL 净重为明细累加</li>
     *   <li>装箱明细 sheet 走 sourceCode -&gt; packing_task -&gt; wms_carton_spec -&gt; wms_carton_detail，
     *       装箱重量取 wms_carton_detail.gross_weight（即装箱 SKU 在该箱内的预计毛重）</li>
     * </ul>
     */
    private void fillExportMulti(List<TmsDeclareBillDTO.ExportDTO> list,
                                 Map<String, SysAccountingCompanyEntity> accountingCompanyMap) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        Map<String, SysAccountingCompanyEntity> companyMap = Objects.isNull(accountingCompanyMap)
                ? loadDeclareExportAccountingCompanyMap(list)
                : accountingCompanyMap;

        // 装箱单 sheet 明细净重：批量拿 product_pack.net_weight
        List<String> allSkuIdList = list.stream()
                .map(TmsDeclareBillDTO.ExportDTO::getProductDetailList)
                .filter(CollUtil::isNotEmpty)
                .flatMap(Collection::stream)
                .map(TmsDeclareBillDTO.ExportProductDetail::getSkuId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        Map<String, BigDecimal> skuNetWeightMap = CollectionUtils.isEmpty(allSkuIdList)
                ? Collections.emptyMap()
                : Optional.ofNullable(productPackFeign.listBySkuIds(allSkuIdList))
                .orElse(Collections.emptyList())
                .stream()
                .filter(e -> StringUtils.isNotBlank(e.getSkuId()))
                .collect(Collectors.toMap(
                        ProductPackEntity::getSkuId,
                        e -> Objects.isNull(e.getNetWeight()) ? BigDecimal.ZERO : e.getNetWeight(),
                        (v1, v2) -> v1));

        // 装箱明细 sheet：sourceCode -> packing_task -> wms_carton_spec -> wms_carton_detail
//        List<String> allDeliveryCodeList = list.stream()
//                .map(TmsDeclareBillDTO.ExportDTO::getSourceCode)
//                .filter(StringUtils::isNotBlank)
//                .distinct()
//                .collect(Collectors.toList());
        String type = list.stream().map(TmsDeclareBillDTO.ExportDTO::getType).filter(StringUtils::isNotBlank).findFirst().orElse("");
        List<String> allSourceCodeList = new ArrayList<>();

        if(Objects.equals(type,SourceTypeEnum.FM_DECLARE_BILL.getCode())){
            //获取头程发货单号
            List<String> collect = list.stream()
                    .map(TmsDeclareBillDTO.ExportDTO::getSourceCode)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
            List<FirstMileDeliveryEntity> firstMileDeliveryEntities = FeignQuery.create(FirstMileDeliveryEntity.class)
                    .in(FirstMileDeliveryEntity::getCode, collect)
                    .eq(FirstMileDeliveryEntity::getIsDeleted,false)
                    .list();
            allSourceCodeList = firstMileDeliveryEntities.stream().map(FirstMileDeliveryEntity::getSourceCode).collect(Collectors.toList());

            Map<String, String> firstMileDeliveryMap = firstMileDeliveryEntities.stream().collect(Collectors.toMap(e -> e.getCode(), e -> e.getSourceCode(), (o1, o2) -> o1));
            for (TmsDeclareBillDTO.ExportDTO exportDTO : list) {
                exportDTO.setSourceCode(firstMileDeliveryMap.get(exportDTO.getSourceCode()));
            }
        }else {
            allSourceCodeList = list.stream()
                .map(TmsDeclareBillDTO.ExportDTO::getSourceCode)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        }

        // sourceCode -> taskIdList，用于按 sourceCode 归集 cartonSpecView
        Map<String, List<String>> sourceCodeToTaskIdsMap = Collections.emptyMap();
        // taskId -> sourceCode，反查回填
        Map<String, String> taskIdToSourceCodeMap = Collections.emptyMap();
        if (CollUtil.isNotEmpty(allSourceCodeList)) {
            List<PackingTaskEntity> packingTaskList = Optional.ofNullable(packingTaskFeign.listBySourceCodes(allSourceCodeList))
                    .orElse(Collections.emptyList());
            sourceCodeToTaskIdsMap = packingTaskList.stream()
                    .filter(e -> StringUtils.isNotBlank(e.getSourceCode()) && StringUtils.isNotBlank(e.getId()))
                    .collect(Collectors.groupingBy(
                            PackingTaskEntity::getSourceCode,
                            Collectors.mapping(PackingTaskEntity::getId, Collectors.toList())));
            taskIdToSourceCodeMap = packingTaskList.stream()
                    .filter(e -> StringUtils.isNotBlank(e.getId()) && StringUtils.isNotBlank(e.getSourceCode()))
                    .collect(Collectors.toMap(PackingTaskEntity::getId, PackingTaskEntity::getSourceCode, (v1, v2) -> v1));
        }
        // taskId -> WmsCartonSpecView，便于按 sourceCode 关联回 ExportDTO
        Map<String, WmsCartonSpecDTO.WmsCartonSpecView> taskIdToCartonViewMap = Collections.emptyMap();
        List<String> allTaskIdList = sourceCodeToTaskIdsMap.values().stream()
                .filter(CollUtil::isNotEmpty)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(allTaskIdList)) {
            taskIdToCartonViewMap = Optional.ofNullable(packingTaskFeign.listCartonSpecByTaskIds(allTaskIdList))
                    .orElse(Collections.emptyList())
                    .stream()
                    .filter(e -> StringUtils.isNotBlank(e.getTaskId()))
                    .collect(Collectors.toMap(WmsCartonSpecDTO.WmsCartonSpecView::getTaskId, Function.identity(), (v1, v2) -> v1));
        }

        Map<String, String> b2bBuyerAddressByDeclareId = buildB2bMultiSheetBuyerAddressMap(list);

        for (TmsDeclareBillDTO.ExportDTO exportDTO : list) {
            String buyerAddress = resolveBuyerAddress(exportDTO, exportDTO.getReceiverId(), companyMap, b2bBuyerAddressByDeclareId);
            SysAccountingCompanyEntity sellerCompany = companyMap.get(exportDTO.getSenderId());
            String sellerAddress = Objects.isNull(sellerCompany) ? "" : Objects.toString(sellerCompany.getCompanyAddress(), "");
            String sellerMobile = Objects.isNull(sellerCompany) ? "" : Objects.toString(sellerCompany.getContactMobile(), "");
            exportDTO.setContractInfo(buildContractInfo(exportDTO, sellerAddress,sellerMobile, buyerAddress));
            exportDTO.setInvoiceInfo(buildInvoiceInfo(exportDTO));

            // 装箱单 sheet：明细按报关商品维度循环，主表 + TOTAL 合计
            List<TmsDeclareBillDTO.PackingListItem> packingListItemList =
                    convertPackingListItemList(exportDTO.getProductDetailList(), skuNetWeightMap);
            exportDTO.setPackingListInfo(buildPackingListInfo(exportDTO, packingListItemList));

            // 装箱明细 sheet：按 sourceCode -> task -> carton -> SKU 三层展开
            List<WmsCartonSpecDTO.WmsCartonSpecView> currentViewList = Collections.emptyList();
            List<String> currentTaskIdList = sourceCodeToTaskIdsMap.getOrDefault(exportDTO.getSourceCode(), Collections.emptyList());
            if (CollUtil.isNotEmpty(currentTaskIdList) && !taskIdToCartonViewMap.isEmpty()) {
                currentViewList = currentTaskIdList.stream()
                        .map(taskIdToCartonViewMap::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }
            exportDTO.setPackingDetailItemList(buildPackingDetailItemList(exportDTO.getSourceCode(), currentViewList, taskIdToSourceCodeMap));
        }
    }

    /**
     * 解析合同 sheet 买方地址
     *
     * <p>头程：receiverId 对应核算公司 companyAddress。B2B：中间表 source_type=soDeliveryNotice 的 source_id
     * 为发货通知单主键，优先取通知单 receive_address；为空时再按 customer_id 批量查客户 mail_address。</p>
     */
    private String resolveBuyerAddress(TmsDeclareBillDTO.ExportDTO exportDTO,
                                       String receiverId,
                                       Map<String, SysAccountingCompanyEntity> receiverCompanyMap,
                                       Map<String, String> b2bBuyerAddressByDeclareId) {
        if (CharSequenceUtil.equals(exportDTO.getType(), SourceTypeEnum.FM_DECLARE_BILL.getCode())) {
            if (StringUtils.isBlank(receiverId)) {
                return "";
            }
            SysAccountingCompanyEntity company = receiverCompanyMap.get(receiverId);
            return Objects.isNull(company) ? "" : Objects.toString(company.getCompanyAddress(), "");
        }
        if (CharSequenceUtil.equals(exportDTO.getType(), SourceTypeEnum.B2B_DECLARE_BILL.getCode())) {
            if (StringUtils.isBlank(exportDTO.getId()) || CollUtil.isEmpty(b2bBuyerAddressByDeclareId)) {
                return "";
            }
            return Objects.toString(b2bBuyerAddressByDeclareId.get(exportDTO.getId()), "");
        }
        return "";
    }

    /**
     * B2B 多 sheet 导出：报关单 id -&gt; 买方地址（批量查中间表、发货通知单、客户主数据，避免逐条 Feign）。
     *
     * <p>合并报关关联多张发货通知单时，按中间表出现顺序遍历通知单，取第一条非空 receive_address；
     * 若均为空，再按同一顺序用 customer_id 匹配客户 mail_address。</p>
     */
    private Map<String, String> buildB2bMultiSheetBuyerAddressMap(List<TmsDeclareBillDTO.ExportDTO> list) {
        List<String> b2bDeclareIdList = list.stream()
                .filter(e -> CharSequenceUtil.equals(e.getType(), SourceTypeEnum.B2B_DECLARE_BILL.getCode()))
                .map(TmsDeclareBillDTO.ExportDTO::getId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(b2bDeclareIdList)) {
            return Collections.emptyMap();
        }
        List<DeliveryDeclareDetailMidEntity> midList = Optional.ofNullable(deliveryDeclareDetailMidService.listByDeclareBillIdList(b2bDeclareIdList))
                .orElse(Collections.emptyList());
        String noticeSourceType = SourceTypeEnum.SO_DELIVERY_NOTICE.getCode();
        Map<String, LinkedHashSet<String>> declareIdToNoticeIdSet = new LinkedHashMap<>();
        for (DeliveryDeclareDetailMidEntity mid : midList) {
            if (!CharSequenceUtil.equals(noticeSourceType, mid.getSourceType()) || StringUtils.isBlank(mid.getDeclareId())
                    || StringUtils.isBlank(mid.getSourceId())) {
                continue;
            }
            declareIdToNoticeIdSet
                    .computeIfAbsent(mid.getDeclareId(), k -> new LinkedHashSet<>())
                    .add(mid.getSourceId());
        }
        if (declareIdToNoticeIdSet.isEmpty()) {
            return Collections.emptyMap();
        }
        List<String> allNoticeIdList = declareIdToNoticeIdSet.values().stream()
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
        Map<String, SoDeliveryNoticeEntity> noticeById = CollectionUtils.isEmpty(allNoticeIdList)
                ? Collections.emptyMap()
                : Optional.ofNullable(soDeliveryNoticeFeign.listByIds(allNoticeIdList))
                .orElse(Collections.emptyList())
                .stream()
                .filter(e -> StringUtils.isNotBlank(e.getId()))
                .collect(Collectors.toMap(SoDeliveryNoticeEntity::getId, Function.identity(), (v1, v2) -> v1));

        Set<String> customerIdForMail = new LinkedHashSet<>();
        for (SoDeliveryNoticeEntity notice : noticeById.values()) {
            if (Objects.isNull(notice)) {
                continue;
            }
            if (StringUtils.isBlank(notice.getReceiveAddress()) && StringUtils.isNotBlank(notice.getCustomerId())) {
                customerIdForMail.add(notice.getCustomerId());
            }
        }
        Map<String, CustomerInfoEntity> customerById = CollectionUtils.isEmpty(customerIdForMail)
                ? Collections.emptyMap()
                : Optional.ofNullable(customerFeign.listCustomerByIds(new ArrayList<>(customerIdForMail)))
                .orElse(Collections.emptyList())
                .stream()
                .filter(e -> StringUtils.isNotBlank(e.getId()))
                .collect(Collectors.toMap(CustomerInfoEntity::getId, Function.identity(), (v1, v2) -> v1));

        Map<String, String> result = new HashMap<>(declareIdToNoticeIdSet.size());
        for (Map.Entry<String, LinkedHashSet<String>> entry : declareIdToNoticeIdSet.entrySet()) {
            String declareId = entry.getKey();
            String address = "";
            for (String noticeId : entry.getValue()) {
                SoDeliveryNoticeEntity notice = noticeById.get(noticeId);
                if (Objects.isNull(notice)) {
                    continue;
                }
                if (StringUtils.isNotBlank(notice.getReceiveAddress())) {
                    address = notice.getReceiveAddress().trim();
                    break;
                }
            }
            if (StringUtils.isBlank(address)) {
                for (String noticeId : entry.getValue()) {
                    SoDeliveryNoticeEntity notice = noticeById.get(noticeId);
                    if (Objects.isNull(notice) || StringUtils.isBlank(notice.getCustomerId())) {
                        continue;
                    }
                    CustomerInfoEntity customer = customerById.get(notice.getCustomerId());
                    if (Objects.nonNull(customer) && StringUtils.isNotBlank(customer.getMailAddress())) {
                        address = customer.getMailAddress().trim();
                        break;
                    }
                }
            }
            result.put(declareId, address);
        }
        return result;
    }

    /**
     * 组装合同 sheet 信息
     *
     * <p>金额计算规则：</p>
     * <ul>
     *   <li>合同总值 = 复用 fillExport 已计算的 ExportDTO.totalPrice，统一保留 4 位小数</li>
     *   <li>大写：含币别中文名前缀，调用 hutool Convert.digitToChinese</li>
     *   <li>明细行单价 / 总价均按 4 位小数保留</li>
     * </ul>
     */
    private TmsDeclareBillDTO.ContractInfo buildContractInfo(TmsDeclareBillDTO.ExportDTO exportDTO,
                                                             String sellerAddress,
                                                             String sellerMobile,
                                                             String buyerAddress) {
        TmsDeclareBillDTO.ContractInfo contractInfo = new TmsDeclareBillDTO.ContractInfo();
        contractInfo.setSellerName(Objects.toString(exportDTO.getSenderName(), ""));
        contractInfo.setSellerAddress(sellerAddress);
        contractInfo.setSellerMobile(sellerMobile);
        contractInfo.setBuyerName(Objects.toString(exportDTO.getReceiverName(), ""));
        contractInfo.setBuyerAddress(buyerAddress);
        contractInfo.setContractNo(Objects.toString(exportDTO.getCode(), ""));
        contractInfo.setContractDate(exportDTO.getDeclareDate());
        contractInfo.setDestination(Objects.toString(exportDTO.getCountryName(), ""));
        contractInfo.setPaymentTerms(Objects.toString(exportDTO.getDictTransactionMethodName(), ""));
        contractInfo.setPackingMarks("");

        List<TmsDeclareBillDTO.ExportProductDetail> productDetailList = exportDTO.getProductDetailList();
        contractInfo.setContractDetailList(convertContractDetailList(productDetailList));

        String currency = resolveContractCurrency(exportDTO, productDetailList);
        contractInfo.setCurrency(currency);

        BigDecimal totalAmount = Objects.isNull(exportDTO.getTotalPrice()) ? BigDecimal.ZERO : exportDTO.getTotalPrice();
        totalAmount = totalAmount.setScale(MathUtil.scale, RoundingMode.HALF_UP);
        contractInfo.setTotalAmount(totalAmount);
        contractInfo.setTotalAmountUpper(toAmountUpper(currency, totalAmount));
        return contractInfo;
    }

    /**
     * 组装发票 sheet 主表信息
     *
     * <p>金额计算规则：</p>
     * <ul>
     *   <li>合计数量 = 明细 qty 求和，明细 qty 为空按 0 处理</li>
     *   <li>合计金额 = 明细 totalPrice 求和，统一保留 4 位小数</li>
     *   <li>币别符号取明细首行 declareCurrency 对应符号；多币别日志由合同 sheet 统一输出，避免重复 warn</li>
     * </ul>
     */
    private TmsDeclareBillDTO.InvoiceInfo buildInvoiceInfo(TmsDeclareBillDTO.ExportDTO exportDTO) {
        TmsDeclareBillDTO.InvoiceInfo invoiceInfo = new TmsDeclareBillDTO.InvoiceInfo();
        invoiceInfo.setSellerName(Objects.toString(exportDTO.getSenderName(), ""));
        invoiceInfo.setBuyerName(Objects.toString(exportDTO.getReceiverName(), ""));
        invoiceInfo.setNo(Objects.toString(exportDTO.getCode(), ""));
        invoiceInfo.setDate(exportDTO.getDeclareDate());
        invoiceInfo.setMarks("");

        List<TmsDeclareBillDTO.ExportProductDetail> productDetailList = exportDTO.getProductDetailList();
        int totalQty = CollectionUtils.isEmpty(productDetailList) ? 0 : productDetailList.stream()
                .map(TmsDeclareBillDTO.ExportProductDetail::getQty)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
        BigDecimal totalAmount = CollectionUtils.isEmpty(productDetailList) ? BigDecimal.ZERO : productDetailList.stream()
                .map(TmsDeclareBillDTO.ExportProductDetail::getTotalPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        invoiceInfo.setTotalQty(totalQty);
        invoiceInfo.setTotalAmount(totalAmount.setScale(MathUtil.scale, RoundingMode.HALF_UP));
        invoiceInfo.setCurrencySymbol(resolveInvoiceCurrencySymbol(productDetailList));
        return invoiceInfo;
    }

    /**
     * 解析合同币别
     *
     * <p>多明细行币别不一致时，取首行币别并打印 warn 日志，便于线上排查异常数据。</p>
     */
    private String resolveContractCurrency(TmsDeclareBillDTO.ExportDTO exportDTO,
                                           List<TmsDeclareBillDTO.ExportProductDetail> productDetailList) {
        if (CollectionUtils.isEmpty(productDetailList)) {
            return "";
        }
        String firstCurrency = productDetailList.get(0).getDeclareCurrency();
        boolean multiCurrency = productDetailList.stream()
                .map(TmsDeclareBillDTO.ExportProductDetail::getDeclareCurrency)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .count() > 1;
        if (multiCurrency) {
            log.warn("报关单【{}】合同 sheet 存在多币别，按首行币别【{}】展示总额", exportDTO.getCode(), firstCurrency);
        }
        return Objects.toString(firstCurrency, "");
    }

    /**
     * 解析发票 sheet 币别符号
     *
     * <p>多币别异常日志由合同 sheet 统一输出；发票仅取首行币别符号，避免重复日志。</p>
     */
    private String resolveInvoiceCurrencySymbol(List<TmsDeclareBillDTO.ExportProductDetail> productDetailList) {
        if (CollectionUtils.isEmpty(productDetailList)) {
            return "";
        }
        String currencyCode = productDetailList.get(0).getDeclareCurrency();
        if (StringUtils.isBlank(currencyCode)) {
            return "";
        }
        return Objects.toString(CurrencyEnum.getSymbolByCode(currencyCode), "");
    }

    /**
     * 产品明细 -> 合同明细行映射
     */
    private List<TmsDeclareBillDTO.ContractDetailItem> convertContractDetailList(List<TmsDeclareBillDTO.ExportProductDetail> productDetailList) {
        if (CollectionUtils.isEmpty(productDetailList)) {
            return Collections.emptyList();
        }
        return productDetailList.stream().map(detail -> {
            TmsDeclareBillDTO.ContractDetailItem item = new TmsDeclareBillDTO.ContractDetailItem();
            item.setDeclareChineseName(Objects.toString(detail.getDeclareChineseName(), ""));
            item.setQty(detail.getQty());
            item.setDeclareUnit(Objects.toString(detail.getDeclareUnit(), ""));
            item.setDeclareUnitName(Objects.toString(detail.getDeclareUnitName(), ""));
            item.setPrice(Objects.isNull(detail.getPrice()) ? BigDecimal.ZERO : detail.getPrice().setScale(MathUtil.scale, RoundingMode.HALF_UP));
            item.setDeclareCurrency(Objects.toString(detail.getDeclareCurrency(), ""));
            item.setTotalPrice(Objects.isNull(detail.getTotalPrice()) ? BigDecimal.ZERO : detail.getTotalPrice().setScale(MathUtil.scale, RoundingMode.HALF_UP));
            return item;
        }).collect(Collectors.toList());
    }

    /**
     * 产品明细 -> 发票明细行映射
     */
    private List<TmsDeclareBillDTO.InvoiceDetailItem> convertInvoiceDetailList(List<TmsDeclareBillDTO.ExportProductDetail> productDetailList) {
        if (CollectionUtils.isEmpty(productDetailList)) {
            return Collections.emptyList();
        }
        return productDetailList.stream().map(detail -> {
            TmsDeclareBillDTO.InvoiceDetailItem item = new TmsDeclareBillDTO.InvoiceDetailItem();
            item.setMarkNo("N/M");
            item.setDeclareChineseName(Objects.toString(detail.getDeclareChineseName(), ""));
            item.setQty(detail.getQty());
            item.setDeclareUnit(Objects.toString(detail.getDeclareUnit(), ""));
            item.setDeclareUnitName(Objects.toString(detail.getDeclareUnitName(), ""));
            item.setPrice(Objects.isNull(detail.getPrice()) ? BigDecimal.ZERO : detail.getPrice().setScale(MathUtil.scale, RoundingMode.HALF_UP));
            item.setTotalPrice(Objects.isNull(detail.getTotalPrice()) ? BigDecimal.ZERO : detail.getTotalPrice().setScale(MathUtil.scale, RoundingMode.HALF_UP));
            return item;
        }).collect(Collectors.toList());
    }

    /**
     * 报关商品明细 -&gt; 装箱单 sheet 明细行
     *
     * <p>计算规则：净重 = product_pack.net_weight × qty，4 位精度，HALF_UP；
     * sku 未在 product_pack 维护时按 0 处理，不阻断导出。</p>
     */
    private List<TmsDeclareBillDTO.PackingListItem> convertPackingListItemList(List<TmsDeclareBillDTO.ExportProductDetail> productDetailList,
                                                                               Map<String, BigDecimal> skuNetWeightMap) {
        if (CollectionUtils.isEmpty(productDetailList)) {
            return Collections.emptyList();
        }
        return productDetailList.stream().map(detail -> {
            TmsDeclareBillDTO.PackingListItem item = new TmsDeclareBillDTO.PackingListItem();
            item.setDescription(Objects.toString(detail.getDeclareChineseName(), ""));
            int qty = Objects.isNull(detail.getQty()) ? 0 : detail.getQty();
            item.setQty(qty);
            BigDecimal singleNetWeight = StringUtils.isBlank(detail.getSkuId())
                    ? BigDecimal.ZERO
                    : skuNetWeightMap.getOrDefault(detail.getSkuId(), BigDecimal.ZERO);
            BigDecimal lineNetWeight = singleNetWeight.multiply(BigDecimal.valueOf(qty)).setScale(MathUtil.scale, RoundingMode.HALF_UP);
            item.setNetWeight(lineNetWeight);
            return item;
        }).collect(Collectors.toList());
    }

    /**
     * 组装装箱单 sheet 主表 + TOTAL 合计 + 明细行集合
     *
     * <p>TOTAL 合计：</p>
     * <ul>
     *   <li>总数(件) = 主表 boxQty</li>
     *   <li>总毛重 = 主表 grossWeight</li>
     *   <li>总数量 = 明细 qty 之和（== ExportDTO.totalQty）</li>
     *   <li>总净重 = 明细 N.W. 之和（4 位精度）；不再回退主表 netWeight，避免与单行规则不一致</li>
     * </ul>
     */
    private TmsDeclareBillDTO.PackingListInfo buildPackingListInfo(TmsDeclareBillDTO.ExportDTO exportDTO,
                                                                   List<TmsDeclareBillDTO.PackingListItem> itemList) {
        TmsDeclareBillDTO.PackingListInfo info = new TmsDeclareBillDTO.PackingListInfo();
        info.setBuyers(Objects.toString(exportDTO.getReceiverName(), ""));
        info.setDate(exportDTO.getDeclareDate());
        info.setInvoiceNo(Objects.toString(exportDTO.getCode(), ""));
        info.setContractNo(Objects.toString(exportDTO.getCode(), ""));
        info.setShippedBy(Objects.toString(exportDTO.getCountryName(), ""));
        info.setBoxNoLabel(buildBoxNoLabel(exportDTO.getBoxQty()));
        // From / TO / Marks / 付款条件 暂无明确数据源，预留占位
        info.setFromArea("");
        info.setToArea("");
        info.setPaymentTerms("");
        info.setMarks("");

        info.setTotalBoxQty(Objects.isNull(exportDTO.getBoxQty()) ? 0 : exportDTO.getBoxQty());
        info.setTotalGrossWeight(Objects.isNull(exportDTO.getGrossWeight()) ? BigDecimal.ZERO : exportDTO.getGrossWeight());

        int totalQty = CollectionUtils.isEmpty(itemList) ? 0 : itemList.stream()
                .map(TmsDeclareBillDTO.PackingListItem::getQty)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
        BigDecimal totalNetWeight = CollectionUtils.isEmpty(itemList) ? BigDecimal.ZERO : itemList.stream()
                .map(TmsDeclareBillDTO.PackingListItem::getNetWeight)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(MathUtil.scale, RoundingMode.HALF_UP);
        info.setTotalQty(totalQty);
        info.setTotalNetWeight(totalNetWeight);

        info.setItemList(CollectionUtils.isEmpty(itemList) ? Collections.emptyList() : itemList);
        return info;
    }

    /**
     * 箱号文案：1-N，N = boxQty；boxQty &lt;= 1 时退化为 "1"；缺失时返回空串
     */
    private String buildBoxNoLabel(Integer boxQty) {
        if (Objects.isNull(boxQty) || boxQty <= 0) {
            return "";
        }
        if (boxQty <= 1) {
            return "1";
        }
        return "1-" + boxQty;
    }

    /**
     * 装箱明细 sheet 行展开：view -&gt; carton -&gt; detail 三层
     *
     * <p>装箱重量取 wms_carton_detail.gross_weight（即装箱 SKU 在该箱内的预计毛重，单位 kg）。
     * 单号优先取 view.sourceCode；为空时按 taskId 反查 packing_task.source_code，
     * 仍取不到时回退当前 ExportDTO.sourceCode，确保单元格不空。</p>
     */
    private List<TmsDeclareBillDTO.PackingDetailItem> buildPackingDetailItemList(String fallbackSourceCode,
                                                                                 List<WmsCartonSpecDTO.WmsCartonSpecView> viewList,
                                                                                 Map<String, String> taskIdToSourceCodeMap) {
        if (CollectionUtils.isEmpty(viewList)) {
            return Collections.emptyList();
        }
        List<TmsDeclareBillDTO.PackingDetailItem> result = new ArrayList<>();
        for (WmsCartonSpecDTO.WmsCartonSpecView view : viewList) {
            if (Objects.isNull(view) || CollectionUtils.isEmpty(view.getWmsCartonList())) {
                continue;
            }
            String sourceCode = StringUtils.isNotBlank(view.getSourceCode())
                    ? view.getSourceCode()
                    : taskIdToSourceCodeMap.getOrDefault(view.getTaskId(), Objects.toString(fallbackSourceCode, ""));
            for (WmsCartonSpecDTO.ViewDTO carton : view.getWmsCartonList()) {
                if (Objects.isNull(carton) || CollectionUtils.isEmpty(carton.getDetailList())) {
                    continue;
                }
                for (WmsCartonDetailDTO.ViewDTO detail : carton.getDetailList()) {
                    TmsDeclareBillDTO.PackingDetailItem item = new TmsDeclareBillDTO.PackingDetailItem();
                    item.setSourceCode(sourceCode);
                    item.setBoxNo(carton.getBoxNo());
                    item.setSkuNo(Objects.toString(detail.getSkuNo(), ""));
                    item.setPackQty(detail.getPackQty());
                    item.setGrossWeight(Objects.isNull(carton.getPackageWeight()) ? BigDecimal.ZERO : carton.getPackageWeight());
                    result.add(item);
                }
            }
        }
        return result;
    }

    /**
     * 金额转中文大写，含币别中文名前缀
     */
    private String toAmountUpper(String currencyName, BigDecimal amount) {
        String chinese = Convert.digitToChinese(amount.doubleValue());
        return StringUtils.isBlank(currencyName) ? chinese : currencyName + " " + chinese;
    }

    /**
     * 单条记录写入：1 个 xlsx，多 sheet 编排
     *
     * <p>先在内存完成模板渲染再写 response，避免 fill/finish 阶段直接写响应流：
     * 调试断点暂停或客户端未及时读流时，Servlet 输出缓冲区满会导致线程阻塞。</p>
     */
    private void writeMultiSheet(TmsDeclareBillDTO.ExportDTO exportDTO,
                                 HttpServletResponse response,
                                 String fileName) throws Exception {
        ClassPathResource classPathResource = new ClassPathResource(DECLARE_MULTI_EXCEL_PATH);
        byte[] xlsxBytes;
        try (InputStream inputStream = classPathResource.getInputStream();
             ByteArrayOutputStream entryOut = new ByteArrayOutputStream()) {
            ExcelWriter excelWriter = EasyExcel.write(entryOut).withTemplate(inputStream).build();
            registerCommonConverters(excelWriter);
            fillMultiSheetForOne(excelWriter, exportDTO);
            excelWriter.finish();
            xlsxBytes = entryOut.toByteArray();
        } catch (Exception e) {
            log.error("多 sheet 报关单导出失败, 单号【{}】", exportDTO.getCode(), e);
            throw new ServiceException(ApiError.FILE_EXPORT_FAILED);
        }

        ExcelPrintUtils.getOutputStream(fileName, response);
        try (OutputStream out = response.getOutputStream();
             BufferedOutputStream bos = new BufferedOutputStream(out)) {
            bos.write(xlsxBytes);
            bos.flush();
        }
        response.flushBuffer();
    }

    /**
     * 多条记录写入：ZIP 包，包内每个 xlsx 都按 多 sheet 模板渲染
     *
     * <p>鲁棒性策略：先在内存里逐条渲染并收集成功条目，再统一写响应，避免单条出错让浏览器收到空 ZIP。</p>
     * <ul>
     *   <li>单条渲染失败：丢弃当前单号并写入响应头 {@value #HEADER_EXPORT_FAILED_CODES}，不影响其他条目</li>
     *   <li>全部失败：抛 FILE_EXPORT_FAILED，避免写出 0 entry ZIP</li>
     * </ul>
     */
    private void writeMultiSheetZip(List<TmsDeclareBillDTO.ExportDTO> list,
                                    HttpServletResponse response,
                                    String zipName) throws Exception {
        // 内存预渲染：单号 -> xlsx 字节内容
        Map<String, byte[]> entryMap = new LinkedHashMap<>();
        List<String> failedCodes = new ArrayList<>();
        for (TmsDeclareBillDTO.ExportDTO exportDTO : list) {
            String declareCode = Objects.toString(exportDTO.getCode(), "unknown");
            ClassPathResource classPathResource = new ClassPathResource(DECLARE_MULTI_EXCEL_PATH);
            try (InputStream inputStream = classPathResource.getInputStream();
                 ByteArrayOutputStream entryOut = new ByteArrayOutputStream()) {
                ExcelWriter excelWriter = EasyExcel.write(entryOut).withTemplate(inputStream).build();
                registerCommonConverters(excelWriter);
                fillMultiSheetForOne(excelWriter, exportDTO);
                excelWriter.finish();
                entryMap.put(declareCode, entryOut.toByteArray());
            } catch (Exception e) {
                failedCodes.add(declareCode);
                log.error("多 sheet 报关单 ZIP 导出 - 单条渲染失败, 单号【{}】", declareCode, e);
            }
        }
        if (entryMap.isEmpty()) {
            // 全部失败时直接抛业务异常；此时尚未调用 getZipOutputStream，response 未被设为 octet-stream，
            // GlobalExceptionHandler 可正常回写 ApiResult JSON
            throw new ServiceException(ApiError.FILE_EXPORT_FAILED);
        }

        if (CollUtil.isNotEmpty(failedCodes)) {
            response.setHeader(HEADER_EXPORT_PARTIAL_FAILURE, "true");
            response.setHeader(HEADER_EXPORT_FAILED_CODES, String.join(",", failedCodes));
            log.warn("多 sheet 报关单 ZIP 导出部分失败, 成功【{}/{}】, 失败单号【{}】",
                    entryMap.size(), list.size(), String.join(",", failedCodes));
        }

        OutputStream outputStream = ExcelPrintUtils.getZipOutputStream(zipName, response);
        try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {
            for (Map.Entry<String, byte[]> entry : entryMap.entrySet()) {
                zipOut.putNextEntry(new ZipEntry("报关单" + entry.getKey() + ".xlsx"));
                zipOut.write(entry.getValue());
                zipOut.closeEntry();
            }
        }
        // 主动 commit response，避免 controller 返回 ApiResult 时 Spring 用 octet-stream 二次序列化失败：
        // ZipOutputStream.close() 不一定立即触发 ServletOutputStream commit，必须显式 flushBuffer。
        response.flushBuffer();
    }

    /**
     * 把一条报关单数据填充到所有 sheet
     *
     * <p>sheet0 报关单 / sheet1 合同 / sheet2 发票 / sheet3 装箱单 / sheet4 装箱明细。</p>
     */
    private void fillMultiSheetForOne(ExcelWriter excelWriter, TmsDeclareBillDTO.ExportDTO exportDTO) {
        // 明细列表统一 forceNewRow，避免多行明细向下覆盖 footer 主表占位符行
        FillConfig detailFillConfig = FillConfig.builder().forceNewRow(Boolean.TRUE).build();

        // sheet 0：报关单（与单 sheet 模板字段口径一致）
        WriteSheet sheetDeclare = EasyExcel.writerSheet(0).build();
        excelWriter.fill(exportDTO.getProductDetailList(), detailFillConfig, sheetDeclare);
        excelWriter.fill(exportDTO, sheetDeclare);

        // sheet 1：合同（先明细后主表，明细须插入新行把 footer 整体下移）
        TmsDeclareBillDTO.ContractInfo contractInfo = exportDTO.getContractInfo();
        if (Objects.nonNull(contractInfo)) {
            WriteSheet sheetContract = EasyExcel.writerSheet(1).build();
            excelWriter.fill(new FillWrapper("contractDetail", contractInfo.getContractDetailList()), detailFillConfig, sheetContract);
            excelWriter.fill(contractInfo, sheetContract);
        }

        // sheet 2：发票
        // EasyExcel 2.2.7 已知 bug：fill(Map, sheet) 不支持 "prefix.field" 形式的复合 key，
        // 模板里的 {invoice.xxx} 占位符必须走 FillWrapper(prefix, ...) 路径，否则 doFill 触发 NPE。
        // 主表为单行数据，用单元素集合 + 默认 forceNewRow=false 即可就地填入占位符行。
        TmsDeclareBillDTO.InvoiceInfo invoiceInfo = exportDTO.getInvoiceInfo();
        if (Objects.nonNull(invoiceInfo)) {
            WriteSheet sheetInvoice = EasyExcel.writerSheet(2).build();
            excelWriter.fill(new FillWrapper("invoiceDetail", convertInvoiceDetailList(exportDTO.getProductDetailList())), detailFillConfig, sheetInvoice);
            excelWriter.fill(new FillWrapper("invoice", Collections.singletonList(invoiceInfo)), sheetInvoice);
        }

        // sheet 3：装箱单（先填明细命名集合，再填主表 + TOTAL 合计，避免主表覆盖明细行模板）
        // 与发票 sheet 同因，{packingList.xxx} 必须走 FillWrapper 路径。
        TmsDeclareBillDTO.PackingListInfo packingListInfo = exportDTO.getPackingListInfo();
        if (Objects.nonNull(packingListInfo)) {
            WriteSheet sheetPackingList = EasyExcel.writerSheet(3).build();
            excelWriter.fill(new FillWrapper("packingListItem", packingListInfo.getItemList()), detailFillConfig, sheetPackingList);
            excelWriter.fill(new FillWrapper("packingList", Collections.singletonList(packingListInfo)), sheetPackingList);
        }

        // sheet 4：装箱明细（仅明细行循环，无主表汇总）
        WriteSheet sheetPackingDetail = EasyExcel.writerSheet(4).build();
        excelWriter.fill(new FillWrapper("packingDetailItem", exportDTO.getPackingDetailItemList()), sheetPackingDetail);
    }

    /**
     * 注册 EasyExcel 常用 Converter（与 ExcelPrintUtils 保持一致）
     */
    private void registerCommonConverters(ExcelWriter excelWriter) {
        LocalDateTimeConverter dateTimeConverter = new LocalDateTimeConverter();
        excelWriter.writeContext().currentWriteHolder().converterMap()
                .put(ConverterKeyBuild.buildKey(dateTimeConverter.supportJavaTypeKey()), dateTimeConverter);
        excelWriter.writeContext().currentWriteHolder().converterMap()
                .put(ConverterKeyBuild.buildKey(dateTimeConverter.supportJavaTypeKey(), dateTimeConverter.supportExcelTypeKey()), dateTimeConverter);

        EasyExcelLocalTimeConverter localTimeConverter = new EasyExcelLocalTimeConverter();
        excelWriter.writeContext().currentWriteHolder().converterMap()
                .put(ConverterKeyBuild.buildKey(localTimeConverter.supportJavaTypeKey()), localTimeConverter);
        excelWriter.writeContext().currentWriteHolder().converterMap()
                .put(ConverterKeyBuild.buildKey(localTimeConverter.supportJavaTypeKey(), localTimeConverter.supportExcelTypeKey()), localTimeConverter);

        EasyExcelLocalDateConverter localDateConverter = new EasyExcelLocalDateConverter();
        excelWriter.writeContext().currentWriteHolder().converterMap()
                .put(ConverterKeyBuild.buildKey(localDateConverter.supportJavaTypeKey()), localDateConverter);
        excelWriter.writeContext().currentWriteHolder().converterMap()
                .put(ConverterKeyBuild.buildKey(localDateConverter.supportJavaTypeKey(), localDateConverter.supportExcelTypeKey()), localDateConverter);

        EasyExcelListConverter listConverter = new EasyExcelListConverter();
        excelWriter.writeContext().currentWriteHolder().converterMap()
                .put(ConverterKeyBuild.buildKey(listConverter.supportJavaTypeKey()), listConverter);
        excelWriter.writeContext().currentWriteHolder().converterMap()
                .put(ConverterKeyBuild.buildKey(listConverter.supportJavaTypeKey(), listConverter.supportExcelTypeKey()), listConverter);
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
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateBatchFiled(TmsDeclareBillDTO.BatchUpdateFieldDTO dto, SourceTypeEnum sourceTypeEnum) {
        TmsDeclareBillEntity old = getById(dto.getId());
        UpdateWrapper<TmsDeclareBillEntity> updateWrapper = new UpdateWrapper<>();
        List<TmsDeclareBillDTO.BatchUpdateFieldListDTO> fieldList = dto.getFieldList();
        for (TmsDeclareBillDTO.BatchUpdateFieldListDTO batchUpdateFieldListDTO : fieldList) {
            TmsDeclareBillBatchFieldEnum fieldEnum = TmsDeclareBillBatchFieldEnum.getEnumByCode(batchUpdateFieldListDTO.getUpdateFiledCode());
            if (Objects.isNull(fieldEnum)) {
                throw new ServiceException(ApiError.COMMON_FIELD_CODE_INVALID, batchUpdateFieldListDTO.getUpdateFiledCode());
            }
            parseBatchFieldValue(fieldEnum, batchUpdateFieldListDTO.getSelectValue());
            Object fieldValue = batchUpdateFieldListDTO.getSelectValue();
            String name = batchUpdateFieldListDTO.getSelectLabel();
            //设置参数
            setUpdateWrapperField(updateWrapper, fieldEnum, fieldValue, name);
        }
        // 批量更新数据库
        updateWrapper.eq("id", dto.getId());
        boolean updateFlag = this.update(updateWrapper);
                
        if (!updateFlag) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_BATCH_UPDATE_FAILED);
        }

        TmsDeclareBillEntity newEntity = getById(dto.getId());
        log.info("批量更新字段 开始记录报关单日志数据，单号：【{}】", old.getCode());
        String msg = CharSequenceUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ",
                UserContext.getDefaultLoginUser().getUserName(), old.getCode(), "报关单");
        operateLogService.addModuleOperateLogByObj(old, newEntity, sourceTypeEnum.getCode(), old.getId(), msg);
        return Boolean.TRUE;
    }

    private void parseBatchFieldValue(TmsDeclareBillBatchFieldEnum fieldEnum, Object values) {
        if (TmsDeclareBillBatchFieldEnum.DECLARE_DATE.equals(fieldEnum)
                || TmsDeclareBillBatchFieldEnum.EXPORT_DATE.equals(fieldEnum)) {
            if (Objects.isNull(values)) {
            }
            if (values instanceof LocalDate) {
            }
            String dateStr = Objects.toString(values, "");
            if (StringUtils.isBlank(dateStr)) {
            }
            try {
                LocalDate.parse(dateStr);
            } catch (Exception e) {
                throw new ServiceException(ApiError.LOGISTICS_DECLARE_FIELD_DATE_FORMAT_INVALID, fieldEnum.getName());
            }
        }
        if (TmsDeclareBillBatchFieldEnum.SHIPPING_FEE.equals(fieldEnum)
                || TmsDeclareBillBatchFieldEnum.INSURANCE_FEE.equals(fieldEnum)
                || TmsDeclareBillBatchFieldEnum.OTHER_FEE.equals(fieldEnum)) {
            if (Objects.isNull(values)) {
            }
            if (values instanceof BigDecimal) {
            }
            String valueStr = Objects.toString(values, "").trim();
            if (StringUtils.isBlank(valueStr)) {
            }
            try {
                new BigDecimal(valueStr);
            } catch (Exception e) {
                throw new ServiceException(ApiError.LOGISTICS_DECLARE_FIELD_NUMBER_FORMAT_INVALID, fieldEnum.getName());
            }
        }
    }

    /**
     * 设置更新 wrapper 的字段
     */
    private void setUpdateWrapperField(UpdateWrapper<TmsDeclareBillEntity> updateWrapper, 
                                       TmsDeclareBillBatchFieldEnum fieldEnum, 
                                       Object fieldValue, 
                                       String name) {
        String fieldName = fieldEnum.getCode();
        switch (fieldEnum) {
            case PRE_INPUT_NO:
            case DEST_CUSTOMS:
            case DECLARE_TYPE:
            case EXPORT_CUSTOMS_NAME:
            case DICT_SUPERVISION_METHOD:
            case DICT_NATURE_LEVY:
            case LICENSE_NO:
            case TRADING_AREA:
            case TO_AREA:
            case TO_PORT:
            case EXPORT_PORT:
            case DICT_PACK_TYPE:
            case DICT_TRANSACTION_METHOD:
            case REMARK:
                updateWrapper.set(fieldName, Objects.toString(fieldValue, ""));
                break;
            case SENDER_ID:
                updateWrapper.set(fieldName, "");
                updateWrapper.set("sender_name", "");
                break;
            case RECEIVER_ID:
                String str = Objects.toString(fieldValue, "");
                if(StringUtils.isNotBlank(str)){
                    List<CustomerInfoEntity> list = FeignQuery.create(CustomerInfoEntity.class).eq(CustomerInfoEntity::getId, str).list();
                    if(CollUtil.isNotEmpty(list)){
                        updateWrapper.set("receiver_type", CfgDeclareRuleReceiverTypeEnum.BY_CUSTOMER.getCode());
                    }else{
                        updateWrapper.set("receiver_type", CfgDeclareRuleReceiverTypeEnum.BY_COMPANY.getCode());
                    }
                    updateWrapper.set(fieldName, str);
                    updateWrapper.set("receiver_name", name);
                }else {
                    updateWrapper.set(fieldName, "");
                    updateWrapper.set("receiver_name", "");
                    updateWrapper.set("receiver_type", "");
                }
                break;
            case EXPORT_DATE:
            case DECLARE_DATE:
                updateWrapper.set(fieldName, fieldValue);
                break;
            case SHIPPING_FEE:
            case INSURANCE_FEE:
            case OTHER_FEE:
                updateWrapper.set(fieldName, Objects.isNull(fieldValue) ? BigDecimal.ZERO : fieldValue);
                break;
            default:
                break;
        }
    }

    @Override
    public List<TmsDeclareBillDTO.SplitDeclareDTO> listSplitFmDetail(String id) {
        TmsDeclareBillEntity declareBillEntity = super.getById(id);
        if(Objects.isNull(declareBillEntity)){
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_INFO_NOT_FOUND);
        }
        if (!CharSequenceUtil.equals(declareBillEntity.getType(), SourceTypeEnum.FM_DECLARE_BILL.getCode())) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_FM_SPLIT_VIEW_FORBIDDEN);
        }
        if (!CharSequenceUtil.equals(declareBillEntity.getDeclareStatus(), DeclareStatusEnum.WAIT.getCode())) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_SPLIT_WAIT_STATUS_REQUIRED);
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
            splitDeclareDTO.setBusinessCode(value.stream().map(DeliveryDeclareDetailMidEntity::getBusinessCode).filter(StringUtils::isNotBlank).findFirst().orElse(""));
            //sku信息描述格式：skuNo*qty,skuNo*qty
            String skuDesc = value.stream().map(obj -> CharSequenceUtil.format("{}*{}", obj.getSkuNo(), obj.getQty())).collect(Collectors.joining(","));
            splitDeclareDTO.setSkuDesc(skuDesc);
            List<TmsDeclareBillDTO.SplitDetailDTO> splitDetailDTOList = value.stream().map(obj -> new TmsDeclareBillDTO.SplitDetailDTO(obj.getSkuId(), obj.getSkuNo(), obj.getQty())).collect(Collectors.toList());
            splitDeclareDTO.setSkuDetailList(splitDetailDTOList);
            splitDeclareDTOList.add(splitDeclareDTO);
        }
        return splitDeclareDTOList;
    }

    @Override
    @DistributeLocker(
            businessType = DistributeKeyConstant.TMS_DECLARE_BILL_ID_KEY,
            keyName = "declareDTO.splitDeclareDTOList.id",
            maxRetries = 1,
            unlockAfterTx = true
    )
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean batchAddSplitFmDetail(TmsDeclareBillDTO.AddSplitDeclareDTO declareDTO) {
        if (CollUtil.isEmpty(declareDTO.getSplitDeclareDTOList())) {
            return Boolean.TRUE;
        }
        List<String> declareIdList = declareDTO.getSplitDeclareDTOList().stream().map(TmsDeclareBillDTO.SplitDeclareDTO::getId).distinct().collect(Collectors.toList());
        if (declareIdList.size() != MathUtil.ONE) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_SPLIT_SINGLE_BILL_REQUIRED);
        }
        TmsDeclareBillEntity declareBillEntity = super.getById(declareIdList.get(0));
        if (ObjectUtil.isEmpty(declareBillEntity)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_INFO_NOT_FOUND);
        }
        if (!CharSequenceUtil.equals(declareBillEntity.getType(), SourceTypeEnum.FM_DECLARE_BILL.getCode())) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_FM_SPLIT_ADD_FORBIDDEN);
        }
        if (!CharSequenceUtil.equals(declareBillEntity.getDeclareStatus(), DeclareStatusEnum.WAIT.getCode())) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_SPLIT_WAIT_STATUS_REQUIRED);
        }

        String splitBaseCode = declareBillEntity.getCode();
        TmsDeclareBillDTO.SplitDeclareCodeSequence splitCodeSequence = StringUtils.isNotBlank(splitBaseCode)
                ? new TmsDeclareBillDTO.SplitDeclareCodeSequence(splitBaseCode)
                : null;
        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDeliveryDetailList = deliveryDeclareDetailMidService.listSourceByDeclareIdList(Collections.singletonList(declareBillEntity.getId()));

        //删除原本的报关单
        deleteDeclareBillById(declareBillEntity.getId());

        for (TmsDeclareBillDTO.SplitDeclareDTO splitDeclareDTO : declareDTO.getSplitDeclareDTOList()) {

            //按规则合并数据
            List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> batchSourceList = filterSplitSourceDetail(sourceDeliveryDetailList, splitDeclareDTO);
            List<TmsDeclareBillDTO.MergeDeclareBillDTO> mergeDeclareBillDTOS = autoMergeDeclareBillView(
                    new TmsDeclareBillDTO.AutoMergeDeclareBillViewDTO(Boolean.TRUE, batchSourceList), Boolean.FALSE);

            //保存合并数据（合同号：原单号_1、_2…）
            batchAddMergeDetail(SourceTypeEnum.FM_DECLARE_BILL.getCode(), mergeDeclareBillDTOS, splitCodeSequence, Boolean.FALSE);
        }
        return Boolean.TRUE;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDeclareBillById (String id) {
        // 恢复报关单关联的中间表为待生成，保留来源箱明细历史数据。
        deliveryDeclareDetailMidService.removeByDeclareBillIds(Collections.singletonList(id));
        //删除明细数据
        detailService.deleteDetailByMainIdList(Collections.singletonList(id));
        //删除主表数据
        super.removeById(id);
    }


    @Override
    @DistributeLocker(
            businessType = DistributeKeyConstant.TMS_DECLARE_BILL_ID_KEY,
            keyName = "declareDTO.splitDeclareDTOList.id",
            maxRetries = 1,
            unlockAfterTx = true
    )
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean batchAddSplitB2bDetail(TmsDeclareBillDTO.AddSplitDeclareDTO declareDTO) {
        if (CollUtil.isEmpty(declareDTO.getSplitDeclareDTOList())) {
            return Boolean.TRUE;
        }
        List<String> declareIdList = declareDTO.getSplitDeclareDTOList().stream().map(TmsDeclareBillDTO.SplitDeclareDTO::getId).distinct().collect(Collectors.toList());
        if (declareIdList.size() != MathUtil.ONE) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_SPLIT_SINGLE_BILL_REQUIRED);
        }
        TmsDeclareBillEntity declareBillEntity = super.getById(declareIdList.get(0));
        if (ObjectUtil.isEmpty(declareBillEntity)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_INFO_NOT_FOUND);
        }
        if (!CharSequenceUtil.equals(declareBillEntity.getType(), SourceTypeEnum.B2B_DECLARE_BILL.getCode())) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_B2B_SPLIT_ADD_FORBIDDEN);
        }
        if (!CharSequenceUtil.equals(declareBillEntity.getDeclareStatus(), DeclareStatusEnum.WAIT.getCode())) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_SPLIT_WAIT_STATUS_REQUIRED);
        }

        String splitBaseCode = declareBillEntity.getCode();
        TmsDeclareBillDTO.SplitDeclareCodeSequence splitCodeSequence = StringUtils.isNotBlank(splitBaseCode)
                ? new TmsDeclareBillDTO.SplitDeclareCodeSequence(splitBaseCode)
                : null;
        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDeliveryDetailList = deliveryDeclareDetailMidService.listSourceByDeclareIdList(Collections.singletonList(declareBillEntity.getId()));
        for (TmsDeclareBillDTO.SplitDeclareDTO splitDeclareDTO : declareDTO.getSplitDeclareDTOList()) {

            //按规则合并数据
            List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> batchSourceList = filterSplitSourceDetail(sourceDeliveryDetailList, splitDeclareDTO);
            List<TmsDeclareBillDTO.MergeDeclareBillDTO> mergeDeclareBillDTOS = autoMergeDeclareBillView(
                    new TmsDeclareBillDTO.AutoMergeDeclareBillViewDTO(Boolean.TRUE, batchSourceList),
                    isB2bCustomerReceiver(declareBillEntity.getType(), declareBillEntity.getReceiverType()));

            //保存合并数据（合同号：原单号_1、_2…）
            batchAddMergeDetail(SourceTypeEnum.B2B_DECLARE_BILL.getCode(), mergeDeclareBillDTOS, splitCodeSequence, Boolean.FALSE);
        }
        String splitMsg = CharSequenceUtil.format("拆分报关单：拆分为{}{}", declareDTO.getSplitDeclareDTOList().size(), "票");
        operateLogService.addModuleOperateLog(splitMsg, SourceTypeEnum.B2B_DECLARE_BILL.getCode(), declareBillEntity.getId(), "拆分操作");
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
                    throw new ServiceException(ApiError.LOGISTICS_DECLARE_MERGE_WAIT_STATUS_REQUIRED, declareCode);
                });
        return deliveryDeclareDetailMidService.listSourceByDeclareIdList(ids);
    }

    @Override
    public List<TmsDeclareBillDTO.MergeDeclareBillDTO> listAfterMergeDetail( List<String> ids) {
        List<TmsDeclareBillEntity> tmsDeclareBillList = super.listByIds(ids);
        if (CollUtil.isEmpty(tmsDeclareBillList)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_INFO_NOT_FOUND);
        }
        //境外收货人类型需要一致
        long count = tmsDeclareBillList.stream().map(TmsDeclareBillEntity::getReceiverType).distinct().count();
        if (count > 1) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_MERGE_RECEIVER_TYPE_DIFF);
        }

        //仅待确认报关单支持操作合并
        tmsDeclareBillList.stream().filter(obj -> !CharSequenceUtil.equals(obj.getDeclareStatus(), DeclareStatusEnum.WAIT.getCode()))
                .findFirst()
                .ifPresent(obj -> {
                    String declareCode = CharSequenceUtil.blankToDefault(obj.getCode(), obj.getId());
                    throw new ServiceException(ApiError.LOGISTICS_DECLARE_MERGE_WAIT_STATUS_REQUIRED, declareCode);
                });
        Set<String> declareTypeSet = tmsDeclareBillList.stream()
                .map(TmsDeclareBillEntity::getType)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        if (declareTypeSet.size() > 1) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_BILL_TYPE_MISMATCH);
        }
        TmsDeclareBillEntity firstDeclareBill = tmsDeclareBillList.get(0);
        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDeliveryDetailList = deliveryDeclareDetailMidService.listSourceByDeclareIdList(ids);
        return autoMergeDeclareBillView(new TmsDeclareBillDTO.AutoMergeDeclareBillViewDTO(Boolean.TRUE,sourceDeliveryDetailList),
                isB2bCustomerReceiver(firstDeclareBill.getType(), firstDeclareBill.getReceiverType()));
    }

    /**
     * 按拆分选择的来源单据、箱号和 SKU 过滤来源明细。
     *
     * @param sourceDeliveryDetailList 原报关单来源明细
     * @param splitDeclareDTO 拆分选择
     * @return 本次拆分对应的来源明细
     */
    private List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> filterSplitSourceDetail(List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDeliveryDetailList,
                                                                                   TmsDeclareBillDTO.SplitDeclareDTO splitDeclareDTO) {
        Set<String> skuIdSet = Optional.ofNullable(splitDeclareDTO.getSkuDetailList()).orElse(Collections.emptyList()).stream()
                .map(TmsDeclareBillDTO.SplitDetailDTO::getSkuId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        return Optional.ofNullable(sourceDeliveryDetailList).orElse(Collections.emptyList()).stream()
                .filter(obj -> CharSequenceUtil.equals(obj.getSourceId(), splitDeclareDTO.getSourceId()))
                .filter(obj -> CharSequenceUtil.equals(obj.getBoxNo(), splitDeclareDTO.getBoxNo()))
                .filter(obj -> CollUtil.isEmpty(skuIdSet) || skuIdSet.contains(obj.getSkuId()))
                .collect(Collectors.toList());
    }


    /**
     * 处理合并前报关信息
     * @author will
     * @date 2026/4/27 17:32
     * @param viewDTO
     */
    @Override
    public  List<TmsDeclareBillDTO.MergeDeclareBillDTO> autoMergeDeclareBillView(TmsDeclareBillDTO.AutoMergeDeclareBillViewDTO viewDTO) {
        return autoMergeDeclareBillView(viewDTO, null);
    }

    private List<TmsDeclareBillDTO.MergeDeclareBillDTO> autoMergeDeclareBillView(TmsDeclareBillDTO.AutoMergeDeclareBillViewDTO viewDTO,
                                                                                Boolean includeSkuInMergeKey) {
        if (CollectionUtils.isEmpty(viewDTO.getSourceDeliveryDetailList())) {
            return Collections.emptyList();
        }
        boolean sixDimensionMerge = Objects.isNull(includeSkuInMergeKey) ? isSixDimensionMerge(viewDTO.getSourceDeliveryDetailList()) : includeSkuInMergeKey;

        // sixDimensionMerge=true (B2B 按客户分发) 时，单价/币别/币别符号取值来源是 so_detail 的 tax_price/currency/currency_symbol，
        // 由调用方在入参中预先填好，这里不能再用物流产品 (foreign_product_logistics) 覆盖。
        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> result = prepareSourceDetailsForDeclarationGeneration(viewDTO.getSourceDeliveryDetailList(), sixDimensionMerge);
        if (CollUtil.isEmpty(result)) {
            return Collections.emptyList();
        }
        DeclarationGenerationService declarationGenerationService = new DeclarationGenerationService();
        return declarationGenerationService.generateMergeBillDetails(result, viewDTO.getIsMultipleMerge(), sixDimensionMerge);
    }

    /**
     * B2B报关单且境外收货人为客户时，按 SKU + 五维度合并；其它场景按不含 SKU 的五维度合并。
     */
    private boolean isSixDimensionMerge(List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailList) {
        String declareBillType = resolveDeclareBillType(sourceDetailList);
        if (!CharSequenceUtil.equals(SourceTypeEnum.B2B_DECLARE_BILL.getCode(), declareBillType)) {
            return false;
        }
        CfgDeclareRuleEntity cfgDeclareRule = cfgDeclareRuleService.listMatchedRule(buildDeclareRuleMatchParamMap(declareBillType, sourceDetailList));
        if (cfgDeclareRule == null) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_RULE_NOT_FOUND_FOR_SOURCE);
        }
        return CharSequenceUtil.equals(CfgDeclareRuleReceiverTypeEnum.BY_CUSTOMER.getCode(), cfgDeclareRule.getReceiverType());
    }

    private boolean isB2bCustomerReceiver(String declareBillType, String receiverType) {
        return CharSequenceUtil.equals(SourceTypeEnum.B2B_DECLARE_BILL.getCode(), declareBillType)
                && CharSequenceUtil.equals(CfgDeclareRuleReceiverTypeEnum.BY_CUSTOMER.getCode(), receiverType);
    }

    private String resolveDeclareBillType(List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailList) {
        Set<String> sourceTypeSet = sourceDetailList.stream()
                .filter(Objects::nonNull)
                .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getSourceType)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        boolean hasFmSource = sourceTypeSet.contains(SourceTypeEnum.FIRST_MILE_DELIVERY.getCode())
                || sourceTypeSet.contains(SourceTypeEnum.FM_DECLARE_BILL.getCode());
        boolean hasB2bSource = sourceTypeSet.contains(SourceTypeEnum.SO_DELIVERY_NOTICE.getCode())
                || sourceTypeSet.contains(SourceTypeEnum.B2B_DECLARE_BILL.getCode());
        if (hasFmSource && hasB2bSource) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_BILL_TYPE_MISMATCH);
        }
        if (hasFmSource) {
            return SourceTypeEnum.FM_DECLARE_BILL.getCode();
        }
        if (hasB2bSource) {
            return SourceTypeEnum.B2B_DECLARE_BILL.getCode();
        }
        boolean hasSalesOrg = sourceDetailList.stream()
                .filter(Objects::nonNull)
                .anyMatch(item -> StringUtils.isNotBlank(item.getSalesOrgId()));
        return hasSalesOrg ? SourceTypeEnum.B2B_DECLARE_BILL.getCode() : SourceTypeEnum.FM_DECLARE_BILL.getCode();
    }

    private Map<String, String> buildDeclareRuleMatchParamMap(String declareBillType,
                                                              List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailList) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("ruleType", declareBillType);
        paramMap.put("countryCode", joinDistinct(sourceDetailList.stream()
                .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getCountryId)
                .collect(Collectors.toList())));
        paramMap.put("fromWarehouseId", joinDistinct(sourceDetailList.stream()
                .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getFromWarehouseId)
                .collect(Collectors.toList())));
        paramMap.put("transferWarehouseId", joinDistinct(sourceDetailList.stream()
                .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getTransferWarehouseIds)
                .collect(Collectors.toList())));
        if (CharSequenceUtil.equals(declareBillType, SourceTypeEnum.FM_DECLARE_BILL.getCode())) {
            paramMap.put("destWarehouseId", joinDistinct(sourceDetailList.stream()
                    .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getDestWarehouseId)
                    .collect(Collectors.toList())));
        } else {
            paramMap.put("salesOrgId", joinDistinct(sourceDetailList.stream()
                    .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getSalesOrgId)
                    .collect(Collectors.toList())));
        }
        return paramMap;
    }

    /**
     * 按产品物流补全报关要素、组合品拆行，并写入境内货源地/征免默认值。
     *
     * <p>sixDimensionMerge=true (B2B 按客户分发) 时，单价、报关币别、报关币别符号取值来源是
     * 调用方写入的 so_detail.tax_price / currency / currency_symbol，本方法不再用物流产品覆盖。</p>
     */
    private List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> prepareSourceDetailsForDeclarationGeneration(List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDeliveryDetailList, boolean sixDimensionMerge) {
        if (CollectionUtils.isEmpty(sourceDeliveryDetailList)) {
            return Collections.emptyList();
        }
        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> result = new ArrayList<>();

        List<String> skuIdList = sourceDeliveryDetailList.stream().map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailDTO.ProductLogisticDTO> productLogisticsList = plmTaskFeign.listProductLogisticsByIds(skuIdList);
        Map<String, ProductDetailDTO.ProductLogisticDTO> logisticsMap = CollUtil.isEmpty(productLogisticsList) ? new HashMap<>() : productLogisticsList.stream().collect(Collectors.toMap(ProductDetailDTO.ProductLogisticDTO::getSkuId, item -> item));

        List<BasicDictEntity> declareUnitList = FeignQuery.create(BasicDictEntity.class).eq(BasicDictEntity::getType, "declareUnit").list();
        Map<String, String> declareUnitNameMap = CollUtil.isEmpty(declareUnitList) ? new HashMap<>() : declareUnitList.stream().collect(Collectors.toMap(BasicDictEntity::getValue, BasicDictEntity::getName, (a, b) -> a));

        List<DictCurrencyEntity> dictCurrencyList = sysUserFeign.currencyList();
        Map<String, String> currencyMap = CollUtil.isEmpty(dictCurrencyList) ? new HashMap<>() : dictCurrencyList.stream().collect(Collectors.toMap(DictCurrencyEntity::getId, DictCurrencyEntity::getName));

        List<String> countryIdList = sourceDeliveryDetailList.stream()
                .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getCountryId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<DictCountryEntity> dictCountryList = CollUtil.isEmpty(countryIdList) ? Collections.emptyList() : sysDictFeign.listCountryByIds(countryIdList);
        Map<String, String> countryNameMap = CollUtil.isEmpty(dictCountryList) ? new HashMap<>() : dictCountryList.stream()
                .collect(Collectors.toMap(DictCountryEntity::getId, DictCountryEntity::getNameCn, (a, b) -> a));

        // B2B 按客户分发场景：按 so_info.id (=SourceDeliveryDetailDTO.businessId) 一次性拉取 so_detail，
        // 按 main_id + sku_id 维度回填单价/币别/币别符号，避免再被物流产品 (foreign_product_logistics) 覆盖。
        Map<String, SoDetailEntity> soDetailMap = sixDimensionMerge
                ? loadSoDetailMapForSixDimensionMerge(sourceDeliveryDetailList)
                : Collections.emptyMap();

        for (TmsDeclareBillDTO.SourceDeliveryDetailDTO detailDTO : sourceDeliveryDetailList) {
            ProductDetailDTO.ProductLogisticDTO productLogisticsDTO = logisticsMap.get(detailDTO.getSkuId());
            if (Objects.nonNull(productLogisticsDTO)) {
                fillDeclareInfo(detailDTO, productLogisticsDTO, declareUnitNameMap, currencyMap, sixDimensionMerge,soDetailMap);
            }
            String countryName = countryNameMap.get(detailDTO.getCountryId());
            if (StringUtils.isNotBlank(countryName)) {
                detailDTO.setCountryName(countryName);
            }
            applyDeclareLineDefaults(detailDTO);
            if (Objects.nonNull(productLogisticsDTO)
                    && CombinationDeclareTypeEnums.SPLIT.getCode().equals(productLogisticsDTO.getCombinationDeclareType())
                    && Boolean.TRUE.equals(productLogisticsDTO.getIsCombination())
                    && CollUtil.isNotEmpty(productLogisticsDTO.getChildList())) {
                for (ProductDetailDTO.ProductLogisticDTO logisticDTO : productLogisticsDTO.getChildList()) {
                    TmsDeclareBillDTO.SourceDeliveryDetailDTO sourceDeliveryDetailDTO = new TmsDeclareBillDTO.SourceDeliveryDetailDTO();
                    BeanUtil.copyProperties(detailDTO, sourceDeliveryDetailDTO);
                    sourceDeliveryDetailDTO.setSkuId(logisticDTO.getSkuId());
                    sourceDeliveryDetailDTO.setSkuNo(logisticDTO.getSkuNo());
                    sourceDeliveryDetailDTO.setBomVersion(logisticDTO.getBomVersion());
                    sourceDeliveryDetailDTO.setBomHistoryId(logisticDTO.getBomHistoryId());
                    sourceDeliveryDetailDTO.setQty((detailDTO.getQty() == null ? 0 : detailDTO.getQty()) * (logisticDTO.getChildQty() == null ? 1 : logisticDTO.getChildQty()));
                    // BOM 拆分场景：子 SKU 的境内货源地/征免必须按子 SKU 自己的物流产品信息取值，
                    // 否则会通过上面的 BeanUtil.copyProperties 继承父 SKU 已被 fillDeclareInfo / applyDeclareLineDefaults
                    // 处理过的值（fillDeclareInfo 对这两个字段是"非空才覆盖"，子 PLM 为空时会让父值泄漏到子级）。
                    // 这里显式置空，让下面的 fillDeclareInfo + applyDeclareLineDefaults 按
                    //   子 PLM > DeclareMergeDefaults 默认值（深圳特区 / 照章征税）
                    // 的优先级独立取值，不依赖父 SKU。
                    sourceDeliveryDetailDTO.setSourceCargo(null);
                    sourceDeliveryDetailDTO.setExemption(null);
                    fillDeclareInfo(sourceDeliveryDetailDTO, logisticDTO, declareUnitNameMap, currencyMap, sixDimensionMerge,Collections.emptyMap());
                    applyDeclareLineDefaults(sourceDeliveryDetailDTO);
                    result.add(sourceDeliveryDetailDTO);
                }
            } else {
                result.add(detailDTO);
            }
        }
        return result;
    }

    private void applyDeclareLineDefaults(TmsDeclareBillDTO.SourceDeliveryDetailDTO detailDTO) {
        if (Objects.isNull(detailDTO)) {
            return;
        }
        if (StringUtils.isBlank(detailDTO.getSourceCargo())) {
            detailDTO.setSourceCargo(DeclareMergeDefaults.DEFAULT_SOURCE_CARGO);
        }
        if (StringUtils.isBlank(detailDTO.getExemption())) {
            detailDTO.setExemption(DeclareMergeDefaults.DEFAULT_EXEMPTION);
        }
    }

    /**
     * 按 so_info.id 批量拉取 so_detail，并按 main_id + sku_id 维度建索引，
     * 用于 B2B 按客户分发场景下单价/币别/币别符号的取值来源。
     */
    private Map<String, SoDetailEntity> loadSoDetailMapForSixDimensionMerge(List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDeliveryDetailList) {
        List<String> mainIdList = sourceDeliveryDetailList.stream()
                .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getBusinessId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(mainIdList)) {
            return Collections.emptyMap();
        }
        List<SoDetailEntity> soDetailList = soInfoFeign.listSoDetailByMainIds(mainIdList);
        if (CollUtil.isEmpty(soDetailList)) {
            return Collections.emptyMap();
        }
        return soDetailList.stream()
                .filter(d -> StringUtils.isNotBlank(d.getMainId()) && StringUtils.isNotBlank(d.getSkuId()))
                .collect(Collectors.toMap(d -> buildSoDetailKey(d.getMainId(), d.getSkuId()), d -> d, (a, b) -> a));
    }


    private String buildSoDetailKey(String mainId, String skuId) {
        return mainId + "#" + skuId;
    }

    /**
     * 准备前端提交的报关明细
     * <p>
     * 该方法负责处理前端传入的合并报关明细列表，根据是否启用合并模式进行不同的处理：
     * - 合并模式（isMerge=true）：调用mergeEditedDetails对编辑后的明细进行重新合并计算
     * - 非合并模式（isMerge=false/null）：为明细列表中的每一项应用默认值填充
     * <p>
     * 处理流程：
     * 1. 过滤掉列表中的null元素，保证数据有效性
     * 2. 校验过滤后的列表不为空，为空则抛出异常
     * 3. 根据isMerge标志选择对应的处理策略
     *
     * @param mergeDetailList 前端提交的合并报关明细列表，不能为null
     * @param isMerge         是否为合并模式标识
     *                        true-启用合并模式，会对编辑后的明细进行重新合并
     *                        false/null-非合并模式，仅应用默认值
     * @return 处理后的报关明细列表，已根据模式完成合并或默认值填充
     * @throws ServiceException 当过滤后的明细列表为空时，抛出LOGISTICS_DECLARE_DETAIL_SAVE_REQUIRED异常
     * @author will
     * @date 2026/5/7 14:08
     */
    private List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> prepareSubmittedMergeDetailList(List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> mergeDetailList,
                                                                                             Boolean isMerge) {
        // 过滤掉列表中的null元素
        List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> filteredList = mergeDetailList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        // 校验过滤后的列表不能为空
        if (CollUtil.isEmpty(filteredList)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_SAVE_REQUIRED);
        }
        
        // 根据合并模式选择不同的处理策略
        if (Boolean.TRUE.equals(isMerge)) {
            // 合并模式：对编辑后的明细进行重新合并计算
            return mergeEditedDetails(filteredList);
        }
        
        // 非合并模式：为所有明细应用默认值
        filteredList.forEach(this::applyMergeDeclareDetailDefaults);
        return filteredList;
    }


    /**
     * 合并编辑后的报关明细
     * @author will
     * @date 2026/5/7 14:08
     * @param mergeDetailList
     * @return java.util.List<com.erp.model.tms.dto.TmsDeclareBillDTO.MergeDeclareBillDetailDTO>
     */
    private List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> mergeEditedDetails(List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> mergeDetailList) {
        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailList = new ArrayList<>();
        for (TmsDeclareBillDTO.MergeDeclareBillDetailDTO detailDTO : mergeDetailList) {
            applyMergeDeclareDetailDefaults(detailDTO);
            if (CollUtil.isEmpty(detailDTO.getSourceDeliveryDetailList())) {
                continue;
            }
            for (TmsDeclareBillDTO.SourceDeliveryDetailDTO sourceDetail : detailDTO.getSourceDeliveryDetailList()) {
                if (Objects.isNull(sourceDetail)) {
                    continue;
                }
                TmsDeclareBillDTO.SourceDeliveryDetailDTO copy = new TmsDeclareBillDTO.SourceDeliveryDetailDTO();
                BeanUtil.copyProperties(sourceDetail, copy);
                copyDeclareFieldsToSourceDetail(detailDTO, copy);
                sourceDetailList.add(copy);
            }
        }
        DeclarationGenerationService declarationGenerationService = new DeclarationGenerationService();
        return flattenMergeDeclareBillList(declarationGenerationService.generateMergeBillDetails(sourceDetailList, Boolean.TRUE, isSixDimensionMerge(sourceDetailList)));
    }

    /**
     * 拉平合并后的报关明细
     * @author will
     * @date 2026/5/7 14:08
     * @param mergeDeclareBillList
     * @return java.util.List<com.erp.model.tms.dto.TmsDeclareBillDTO.MergeDeclareBillDetailDTO>
     */
    private List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> flattenMergeDeclareBillList(List<TmsDeclareBillDTO.MergeDeclareBillDTO> mergeDeclareBillList) {
        if (CollUtil.isEmpty(mergeDeclareBillList)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_SAVE_REQUIRED);
        }
        List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> mergeDetailList = mergeDeclareBillList.stream()
                .filter(Objects::nonNull)
                .map(TmsDeclareBillDTO.MergeDeclareBillDTO::getDeclareBillList)
                .filter(CollUtil::isNotEmpty)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(mergeDetailList)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_SAVE_REQUIRED);
        }
        mergeDetailList.forEach(this::applyMergeDeclareDetailDefaults);
        return mergeDetailList;
    }

    /**
     * 把报关明细字段回填到来源明细
     * @author will
     * @date 2026/5/7 14:08
     * @param detailDTO
     * @param sourceDetail
     */
    private void copyDeclareFieldsToSourceDetail(TmsDeclareBillDTO.MergeDeclareBillDetailDTO detailDTO,
                                                 TmsDeclareBillDTO.SourceDeliveryDetailDTO sourceDetail) {
        sourceDetail.setHsCode(detailDTO.getHsCode());
        sourceDetail.setProductNameCn(detailDTO.getProductNameCn());
        sourceDetail.setDeclareElement(detailDTO.getDeclareElement());
        sourceDetail.setUnit(detailDTO.getUnit());
        sourceDetail.setUnitPrice(detailDTO.getUnitPrice());
        sourceDetail.setDeclareCurrency(detailDTO.getDeclareCurrency());
        sourceDetail.setDeclareCurrencySymbol(detailDTO.getDeclareCurrencySymbol());
        sourceDetail.setSourceCountry(detailDTO.getSourceCountry());
        sourceDetail.setSourceCountryName(detailDTO.getSourceCountryName());
        sourceDetail.setCountryId(detailDTO.getToCountry());
        sourceDetail.setCountryName(detailDTO.getToCountryName());
        sourceDetail.setSourceCargo(detailDTO.getSourceCargo());
        sourceDetail.setExemption(detailDTO.getExemption());
    }

    /**
     * 校验报关明细
     * @author will
     * @date 2026/5/7 14:08
     * @param mergeDetailList
     */
    private void validateDeclareMergeDetails(List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> mergeDetailList) {
        if (CollUtil.isEmpty(mergeDetailList)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_SAVE_REQUIRED);
        }
        if (mergeDetailList.size() > limitSkuNo) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_MERGE_SKU_LIMIT_EXCEEDED, limitSkuNo);
        }
        validateDuplicateSkuRows(mergeDetailList);
        Set<String> countrySet = new HashSet<>();
        Set<String> sourceDetailKeySet = new HashSet<>();
        for (int i = 0; i < mergeDetailList.size(); i++) {
            TmsDeclareBillDTO.MergeDeclareBillDetailDTO detailDTO = mergeDetailList.get(i);
            int rowNo = i + 1;
            applyMergeDeclareDetailDefaults(detailDTO);
            validateDeclareDetailRequired(detailDTO, rowNo);
            validateDeclareDetailSources(detailDTO, rowNo, countrySet, sourceDetailKeySet);
        }
        if (countrySet.size() > 1) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_MERGE_COUNTRY_MISMATCH);
        }
    }

    /**
     * 校验报关明细必填项
     * @author will
     * @date 2026/5/7 14:08
     * @param detailDTO
     * @param rowNo
     */
    private void validateDeclareDetailRequired(TmsDeclareBillDTO.MergeDeclareBillDetailDTO detailDTO, int rowNo) {
        requireNotBlank(detailDTO.getSkuNo(), rowNo, "SKU");
        requireNotBlank(detailDTO.getHsCode(), rowNo, "中国海关编码");
        requireNotBlank(detailDTO.getProductNameCn(), rowNo, "商品名称");
        requireNotBlank(detailDTO.getDeclareElement(), rowNo, "申报要素");
        requireNotBlank(detailDTO.getUnit(), rowNo, "单位");
        requireNotNull(detailDTO.getUnitPrice(), rowNo, "单价");
        requirePositive(detailDTO.getQty(), rowNo, "数量");
        requireNotBlank(detailDTO.getDeclareCurrency(), rowNo, "币制");
        requireNotBlank(detailDTO.getSourceCountry(), rowNo, "原产国(地区)");
        requireNotBlank(detailDTO.getToCountry(), rowNo, "最终目的国(地区)");
        requireNotBlank(detailDTO.getSourceCargo(), rowNo, "境内货源地");
        requireNotBlank(detailDTO.getExemption(), rowNo, "征免");
        if (CollUtil.isEmpty(detailDTO.getSourceDeliveryDetailList())) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_SOURCE_REQUIRED, rowNo);
        }
    }

    /**
     * 校验报关明细来源数据
     * @author will
     * @date 2026/5/7 14:08
     * @param detailDTO
     * @param rowNo
     * @param countrySet
     * @param sourceDetailKeySet
     */
    private void validateDeclareDetailSources(TmsDeclareBillDTO.MergeDeclareBillDetailDTO detailDTO,
                                              int rowNo,
                                              Set<String> countrySet,
                                              Set<String> sourceDetailKeySet) {
        int sourceQty = 0;
        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailList = mergeSourceDetailsInSameDeclareDetail(detailDTO.getSourceDeliveryDetailList());
        detailDTO.setSourceDeliveryDetailList(sourceDetailList);
        for (TmsDeclareBillDTO.SourceDeliveryDetailDTO sourceDetail : sourceDetailList) {
            if (Objects.isNull(sourceDetail)) {
                continue;
            }
            requireNotBlank(sourceDetail.getSourceId(), rowNo, "来源单据");
            requireNotBlank(sourceDetail.getSkuNo(), rowNo, "来源SKU");
            requirePositive(sourceDetail.getQty(), rowNo, "来源数量");
            if (!sourceDetailKeySet.add(buildSourceDetailKey(sourceDetail))) {
                throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_SOURCE_DUPLICATE, rowNo);
            }
            validateSourceValue(detailDTO.getDeclareCurrency(), sourceDetail.getDeclareCurrency(), rowNo, "币制");
            validateSourceValue(detailDTO.getSourceCountry(), sourceDetail.getSourceCountry(), rowNo, "原产国(地区)");
            validateSourceValue(detailDTO.getToCountry(), sourceDetail.getCountryId(), rowNo, "最终目的国(地区)");
            validateSourceValue(detailDTO.getSourceCargo(), defaultSourceCargo(sourceDetail.getSourceCargo()), rowNo, "境内货源地");
            validateSourceValue(detailDTO.getExemption(), defaultExemption(sourceDetail.getExemption()), rowNo, "征免");
            countrySet.add(StringUtils.defaultIfBlank(sourceDetail.getCountryId(), detailDTO.getToCountry()));
            sourceQty += Objects.isNull(sourceDetail.getQty()) ? 0 : sourceDetail.getQty();
        }
        if (!Objects.equals(detailDTO.getQty(), sourceQty)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_QTY_MISMATCH, rowNo);
        }
    }

    /**
     * 同一报关明细行内部，按来源单据+箱号+SKU聚合来源明细数量。
     *
     * @param sourceDetailList 来源明细集合
     * @return 聚合后的来源明细集合
     */
    private List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> mergeSourceDetailsInSameDeclareDetail(List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailList) {
        if (CollUtil.isEmpty(sourceDetailList)) {
            return Collections.emptyList();
        }
        Map<String, TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailMap = new LinkedHashMap<>();
        for (TmsDeclareBillDTO.SourceDeliveryDetailDTO sourceDetail : sourceDetailList) {
            if (Objects.isNull(sourceDetail)) {
                continue;
            }
            String key = buildSourceDetailKey(sourceDetail);
            TmsDeclareBillDTO.SourceDeliveryDetailDTO exist = sourceDetailMap.get(key);
            if (Objects.isNull(exist)) {
                TmsDeclareBillDTO.SourceDeliveryDetailDTO copy = new TmsDeclareBillDTO.SourceDeliveryDetailDTO();
                BeanUtil.copyProperties(sourceDetail, copy);
                sourceDetailMap.put(key, copy);
                continue;
            }
            exist.setQty((Objects.isNull(exist.getQty()) ? 0 : exist.getQty())
                    + (Objects.isNull(sourceDetail.getQty()) ? 0 : sourceDetail.getQty()));
        }
        return new ArrayList<>(sourceDetailMap.values());
    }

    /**
     * 校验来源箱明细是否已生成报关单
     * @author will
     * @date 2026/5/7 14:08
     * @param sourceKeySet 来源单据+箱号+SKU维度唯一键集合
     * @param sourceIdSet 来源单据id集合
     * @param sourceType 来源类型
     * @param currentDeclareId 当前报关单id
     */
    private void validateSourceNotGenerated(Set<String> sourceKeySet, Set<String> sourceIdSet, String sourceType, String currentDeclareId) {
        if (CollUtil.isEmpty(sourceKeySet)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_SOURCE_DETAIL_NOT_FOUND_FOR_SAVE);
        }
        List<DeliveryDeclareDetailMidEntity> existsMidList;
        if (StringUtils.isNotBlank(currentDeclareId)) {
            existsMidList = deliveryDeclareDetailMidService.lambdaQuery()
                    .eq(StringUtils.isNotBlank(sourceType), DeliveryDeclareDetailMidEntity::getSourceType, sourceType)
                    .in(CollUtil.isNotEmpty(sourceIdSet), DeliveryDeclareDetailMidEntity::getSourceId, sourceIdSet)
                    .ne(DeliveryDeclareDetailMidEntity::getDeclareId, currentDeclareId)
                    .list();
        } else {
            existsMidList = deliveryDeclareDetailMidService.lambdaQuery()
                    .eq(StringUtils.isNotBlank(sourceType), DeliveryDeclareDetailMidEntity::getSourceType, sourceType)
                    .in(CollUtil.isNotEmpty(sourceIdSet), DeliveryDeclareDetailMidEntity::getSourceId, sourceIdSet)
                    .list();
        }
        List<DeliveryDeclareDetailMidEntity> generatedMidList = existsMidList.stream()
                .filter(item -> sourceKeySet.contains(buildSourceDetailKey(item)))
                .filter(item -> StringUtils.isNotBlank(item.getDeclareId())
                        || DeliveryDeclareDetailMidGenerateStatusEnum.FINISH.getCode().equals(item.getGenerateStatus()))
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(generatedMidList)) {
            String repeatSourceCode = generatedMidList.stream()
                    .map(DeliveryDeclareDetailMidEntity::getSourceCode)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.joining("、"));
            if (StringUtils.isBlank(repeatSourceCode)) {
                throw new ServiceException(ApiError.LOGISTICS_DECLARE_SELECTED_DETAIL_GENERATED);
            }
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_SOURCE_GENERATED, repeatSourceCode);
        }
    }

    /**
     * 校验编辑时来源字段不可修改
     * @author will
     * @date 2026/5/7 14:08
     * @param declareId
     * @param mergeDetailList
     */
    private void validateUpdateImmutableSourceFields(String declareId,
                                                     List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> mergeDetailList) {
        List<DeliveryDeclareDetailMidEntity> oldMidList = deliveryDeclareDetailMidService.listByDeclareBillIdList(Collections.singletonList(declareId));
        if (CollUtil.isEmpty(oldMidList)) {
            return;
        }
        Map<String, DeliveryDeclareDetailMidEntity> oldKeyMap = oldMidList.stream()
                .collect(Collectors.toMap(this::buildSourceDetailKey, item -> item, (a, b) -> a));
        for (TmsDeclareBillDTO.MergeDeclareBillDetailDTO detailDTO : mergeDetailList) {
            if (CollUtil.isEmpty(detailDTO.getSourceDeliveryDetailList())) {
                continue;
            }
            for (TmsDeclareBillDTO.SourceDeliveryDetailDTO sourceDetail : detailDTO.getSourceDeliveryDetailList()) {
                if (Objects.isNull(sourceDetail)) {
                    continue;
                }
                DeliveryDeclareDetailMidEntity oldMid = oldKeyMap.get(buildSourceDetailKey(sourceDetail));
                if (Objects.isNull(oldMid)) {
                    throw new ServiceException(ApiError.LOGISTICS_DECLARE_SOURCE_BOX_IMMUTABLE);
                }
                if (!StringUtils.equals(StringUtils.defaultString(oldMid.getSourceId()), StringUtils.defaultString(sourceDetail.getSourceId()))
                        || !StringUtils.equals(StringUtils.defaultString(oldMid.getBusinessCode()), StringUtils.defaultString(sourceDetail.getBusinessCode()))
                        || !StringUtils.equals(StringUtils.defaultString(oldMid.getSkuNo()), StringUtils.defaultString(sourceDetail.getSkuNo()))
                        || !Objects.equals(Objects.isNull(oldMid.getQty()) ? 0 : oldMid.getQty(), Objects.isNull(sourceDetail.getQty()) ? 0 : sourceDetail.getQty())) {
                    throw new ServiceException(ApiError.LOGISTICS_DECLARE_SOURCE_QTY_IMMUTABLE);
                }
            }
        }
    }

    /**
     * 校验相同报关明细维度是否存在不同行明细
     * @author will
     * @date 2026/5/7 14:08
     * @param mergeDetailList 报关明细集合
     */
    private void validateDuplicateSkuRows(List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> mergeDetailList) {
        Map<String, Integer> declareDetailRowMap = new HashMap<>();
        for (int i = 0; i < mergeDetailList.size(); i++) {
            TmsDeclareBillDTO.MergeDeclareBillDetailDTO detailDTO = mergeDetailList.get(i);
            String detailKey = buildDeclareDetailDuplicateKey(detailDTO);
            if (StringUtils.isBlank(detailKey)) {
                continue;
            }
            Integer firstRow = declareDetailRowMap.putIfAbsent(detailKey, i + 1);
            if (Objects.nonNull(firstRow)) {
                throw new ServiceException(ApiError.LOGISTICS_DECLARE_DUPLICATE_SKU_ROW, detailDTO.getSkuNo(), firstRow, i + 1);
            }
        }
    }

    /**
     * 构建报关明细重复校验键。
     *
     * @param detailDTO 报关明细
     * @return 报关明细维度键
     */
    private String buildDeclareDetailDuplicateKey(TmsDeclareBillDTO.MergeDeclareBillDetailDTO detailDTO) {
        if (Objects.isNull(detailDTO)) {
            return "";
        }
        return CharSequenceUtil.join("|",
                normalizeSkuNo(detailDTO.getSkuNo()),
                StringUtils.defaultString(detailDTO.getHsCode()),
                StringUtils.defaultString(detailDTO.getProductNameCn()),
                StringUtils.defaultString(detailDTO.getDeclareElement()),
                StringUtils.defaultString(detailDTO.getUnit()),
                StringUtils.defaultString(detailDTO.getDeclareCurrency()),
                Objects.isNull(detailDTO.getUnitPrice()) ? "" : detailDTO.getUnitPrice().stripTrailingZeros().toPlainString(),
                StringUtils.defaultString(detailDTO.getSourceCountry()),
                StringUtils.defaultString(detailDTO.getToCountry()),
                defaultSourceCargo(detailDTO.getSourceCargo()),
                defaultExemption(detailDTO.getExemption()));
    }

    private String normalizeSkuNo(String skuNo) {
        if (StringUtils.isBlank(skuNo)) {
            return "";
        }
        return Arrays.stream(skuNo.split(","))
                .map(StringUtils::trimToEmpty)
                .filter(StringUtils::isNotBlank)
                .sorted()
                .collect(Collectors.joining(","));
    }

    /**
     * 收集来源箱明细唯一键
     * @author will
     * @date 2026/5/7 14:08
     * @param mergeDetailList 报关明细集合
     * @return java.util.Set<java.lang.String>
     */
    private Set<String> collectSourceKeySet(List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> mergeDetailList) {
        return mergeDetailList.stream()
                .map(TmsDeclareBillDTO.MergeDeclareBillDetailDTO::getSourceDeliveryDetailList)
                .filter(CollUtil::isNotEmpty)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .map(this::buildSourceDetailKey)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
    }

    /**
     * 收集来源单据id
     * @author will
     * @date 2026/5/13 11:34
     * @param mergeDetailList 报关明细集合
     * @return java.util.Set<java.lang.String>
     */
    private Set<String> collectSourceIdSet(List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> mergeDetailList) {
        return mergeDetailList.stream()
                .map(TmsDeclareBillDTO.MergeDeclareBillDetailDTO::getSourceDeliveryDetailList)
                .filter(CollUtil::isNotEmpty)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getSourceId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
    }

    /**
     * 给默认值
     * @author will
     * @date 2026/5/7 12:21
     * @param detailDTO
     */
    private void applyMergeDeclareDetailDefaults(TmsDeclareBillDTO.MergeDeclareBillDetailDTO detailDTO) {
        if (Objects.isNull(detailDTO)) {
            return;
        }
        if (StringUtils.isBlank(detailDTO.getSourceCargo())) {
            detailDTO.setSourceCargo(DeclareMergeDefaults.DEFAULT_SOURCE_CARGO);
        }
        if (StringUtils.isBlank(detailDTO.getExemption())) {
            detailDTO.setExemption(DeclareMergeDefaults.DEFAULT_EXEMPTION);
        }
        if (CollUtil.isNotEmpty(detailDTO.getSourceDeliveryDetailList())) {
            for (TmsDeclareBillDTO.SourceDeliveryDetailDTO sourceDetail : detailDTO.getSourceDeliveryDetailList()) {
                applyDeclareLineDefaults(sourceDetail);
            }
        }
    }

    /**
     * 校验明细字段与来源字段是否一致
     * @author will
     * @date 2026/5/7 14:08
     * @param detailValue
     * @param sourceValue
     * @param rowNo
     * @param fieldName
     */
    private void validateSourceValue(String detailValue, String sourceValue, int rowNo, String fieldName) {
        if (StringUtils.isBlank(sourceValue)) {
            return;
        }
        if (!StringUtils.equals(StringUtils.defaultString(detailValue), sourceValue)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_SOURCE_VALUE_MISMATCH, rowNo, fieldName);
        }
    }

    /**
     * 获取境内货源地默认值
     * @author will
     * @date 2026/5/7 14:08
     * @param sourceCargo
     * @return java.lang.String
     */
    private String defaultSourceCargo(String sourceCargo) {
        return StringUtils.isBlank(sourceCargo) ? DeclareMergeDefaults.DEFAULT_SOURCE_CARGO : sourceCargo;
    }

    /**
     * 获取征免默认值
     * @author will
     * @date 2026/5/7 14:08
     * @param exemption
     * @return java.lang.String
     */
    private String defaultExemption(String exemption) {
        return StringUtils.isBlank(exemption) ? DeclareMergeDefaults.DEFAULT_EXEMPTION : exemption;
    }

    /**
     * 校验字符串不能为空
     * @author will
     * @date 2026/5/7 14:08
     * @param value
     * @param rowNo
     * @param fieldName
     */
    private void requireNotBlank(String value, int rowNo, String fieldName) {
        if (StringUtils.isBlank(value)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_FIELD_REQUIRED, rowNo, fieldName);
        }
    }

    /**
     * 校验对象不能为空
     * @author will
     * @date 2026/5/7 14:08
     * @param value
     * @param rowNo
     * @param fieldName
     */
    private void requireNotNull(Object value, int rowNo, String fieldName) {
        if (Objects.isNull(value)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_FIELD_REQUIRED, rowNo, fieldName);
        }
    }

    /**
     * 校验数量必须大于0
     * @author will
     * @date 2026/5/7 14:08
     * @param value
     * @param rowNo
     * @param fieldName
     */
    private void requirePositive(Integer value, int rowNo, String fieldName) {
        if (Objects.isNull(value) || value <= 0) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_FIELD_POSITIVE_REQUIRED, rowNo, fieldName);
        }
    }

    /**
     * 自动生成头程报关单（addFmDeclare 非下推路径）前，按发货单维度校验报关必填信息。
     * 任一行 SKU 缺失"海关编码 / 报关品名 / 申报要素 / 单位 / 币种 / 单价"中的任意字段，直接抛错并提示
     * 「单据【发货单号】SKU【skuNo】缺少报关信息：xxx、xxx」，不再继续生成报关单。
     */
    private void validateAutoFmDeclareProductDetails(String sourceCode, List<TmsDeclareBillDTO.ProductDetail> productDetailList) {
        if (CollUtil.isEmpty(productDetailList)) {
            return;
        }
        for (TmsDeclareBillDTO.ProductDetail detail : productDetailList) {
            if (Objects.isNull(detail)) {
                continue;
            }
            List<String> missingFields = collectMissingDeclareFields(
                    detail.getCustomsCode(),
                    detail.getDeclareChineseName(),
                    detail.getDeclareElement(),
                    detail.getDeclareUnit(),
                    detail.getDeclareCurrency(),
                    detail.getPrice());
            if (CollUtil.isNotEmpty(missingFields)) {
                throw new ServiceException(ApiError.LOGISTICS_DECLARE_AUTO_DETAIL_FIELD_REQUIRED,
                        CharSequenceUtil.blankToDefault(sourceCode, "-"),
                        CharSequenceUtil.blankToDefault(detail.getSkuNo(),
                                CharSequenceUtil.blankToDefault(detail.getSkuId(), "-")),
                        String.join("、", missingFields));
            }
        }
    }

    /**
     * 自动生成报关单（batchAddMergeDetail / 拆分保存等链路）前，按合并明细维度校验报关必填信息。
     * 缺失字段时抛错并定位到来源单据 + SKU。
     * <p>
     * sourceCode 优先取该合并行 sourceDeliveryDetailList 中第一条非空 sourceCode；
     * skuNo 优先取合并行自身 skuNo，否则退化为来源明细的 skuNo / skuId。
     */
    @Override
    public void validateAutoMergeDeclareDetailRequired(List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> mergeDetailList) {
        if (CollUtil.isEmpty(mergeDetailList)) {
            return;
        }
        for (TmsDeclareBillDTO.MergeDeclareBillDetailDTO detailDTO : mergeDetailList) {
            if (Objects.isNull(detailDTO)) {
                continue;
            }
            List<String> missingFields = collectMissingDeclareFields(
                    detailDTO.getHsCode(),
                    detailDTO.getProductNameCn(),
                    detailDTO.getDeclareElement(),
                    detailDTO.getUnit(),
                    detailDTO.getDeclareCurrency(),
                    detailDTO.getUnitPrice());
            if (CollUtil.isEmpty(missingFields)) {
                continue;
            }
            String sourceCode = resolveMergeDetailSourceCode(detailDTO);
            String skuNo = resolveMergeDetailSkuNo(detailDTO);
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_AUTO_DETAIL_FIELD_REQUIRED,
                    sourceCode, skuNo, String.join("、", missingFields));
        }
    }

    /**
     * 合并明细维度过滤"目的国 = 中国大陆 (CN)"。
     * <p>
     * 判定口径：合并行 {@code toCountry == "CN"} 或其任一来源行 {@code countryId == "CN"} 即命中。
     * 命中后一次性收集所有相关来源单号（去重 + 排序）抛出，便于使用方一次看到要剔除哪些单据；
     * 抛错前不修改任何来源单 declare_status，被过滤的单据保持 WAIT，
     * 等使用方调整目的国后再次下推/自动生成即可。
     */
    @Override
    public void validateDestCountryNotMainlandChina(List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> mergeDetailList) {
        if (CollUtil.isEmpty(mergeDetailList)) {
            return;
        }
        Set<String> mainlandSourceCodes = new TreeSet<>();
        for (TmsDeclareBillDTO.MergeDeclareBillDetailDTO detailDTO : mergeDetailList) {
            if (Objects.isNull(detailDTO)) {
                continue;
            }
            collectMainlandChinaSourceCodes(detailDTO, mainlandSourceCodes);
        }
        if (CollUtil.isEmpty(mainlandSourceCodes)) {
            return;
        }
        throw new ServiceException(ApiError.LOGISTICS_DECLARE_DEST_COUNTRY_CN_NOT_GENERATE,
                String.join("、", mainlandSourceCodes));
    }

    /**
     * 收集合并行中目的国为中国大陆的来源单号，仅写入 collector，不抛错。
     * 优先以来源行的 {@code countryId} 判定；当来源行缺失国家字段时退化为合并行的 {@code toCountry}。
     */
    private void collectMainlandChinaSourceCodes(TmsDeclareBillDTO.MergeDeclareBillDetailDTO detailDTO,
                                                 Set<String> collector) {
        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceList = detailDTO.getSourceDeliveryDetailList();
        if (CollUtil.isEmpty(sourceList)) {
            if (isMainlandChinaCountry(detailDTO.getToCountry())) {
                collector.add(CharSequenceUtil.blankToDefault(resolveMergeDetailSourceCode(detailDTO), "-"));
            }
            return;
        }
        boolean mergeLevelMainland = isMainlandChinaCountry(detailDTO.getToCountry());
        for (TmsDeclareBillDTO.SourceDeliveryDetailDTO sourceDetail : sourceList) {
            if (Objects.isNull(sourceDetail)) {
                continue;
            }
            String countryId = sourceDetail.getCountryId();
            boolean sourceLevelMainland = StringUtils.isBlank(countryId)
                    ? mergeLevelMainland
                    : isMainlandChinaCountry(countryId);
            if (!sourceLevelMainland) {
                continue;
            }
            collector.add(CharSequenceUtil.blankToDefault(sourceDetail.getSourceCode(), "-"));
        }
    }

    /**
     * 判断字典国家码是否为中国大陆。
     * 兼容大小写，避免 PLM / 上游传入 "cn" 时漏判。
     */
    private boolean isMainlandChinaCountry(String countryCode) {
        return StringUtils.isNotBlank(countryCode)
                && MAINLAND_CHINA_COUNTRY_CODE.equalsIgnoreCase(countryCode);
    }

    /**
     * 收集缺失的报关必填字段（海关编码 / 报关品名 / 申报要素 / 单位 / 币种 / 单价）。
     * <p>
     * 单价规则：null 视为缺失（与现有 {@code requireNotNull(unitPrice, ...)} 行为一致，
     * 不在此处增加"必须大于0"约束，避免与现有页面下推路径行为冲突）。
     */
    private List<String> collectMissingDeclareFields(String customsCode, String declareName, String declareElement,
                                                     String unit, String currency, BigDecimal price) {
        List<String> missing = new ArrayList<>(6);
        if (StringUtils.isBlank(customsCode)) {
            missing.add("海关编码");
        }
        if (StringUtils.isBlank(declareName)) {
            missing.add("报关品名");
        }
        if (StringUtils.isBlank(declareElement)) {
            missing.add("申报要素");
        }
        if (StringUtils.isBlank(unit)) {
            missing.add("单位");
        }
        if (StringUtils.isBlank(currency)) {
            missing.add("币种");
        }
        if (Objects.isNull(price)) {
            missing.add("单价");
        }
        return missing;
    }

    private String resolveMergeDetailSourceCode(TmsDeclareBillDTO.MergeDeclareBillDetailDTO detailDTO) {
        if (CollUtil.isEmpty(detailDTO.getSourceDeliveryDetailList())) {
            return "-";
        }
        return detailDTO.getSourceDeliveryDetailList().stream()
                .filter(Objects::nonNull)
                .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getSourceCode)
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .orElse("-");
    }

    private String resolveMergeDetailSkuNo(TmsDeclareBillDTO.MergeDeclareBillDetailDTO detailDTO) {
        if (StringUtils.isNotBlank(detailDTO.getSkuNo())) {
            return detailDTO.getSkuNo();
        }
        if (CollUtil.isNotEmpty(detailDTO.getSourceDeliveryDetailList())) {
            return detailDTO.getSourceDeliveryDetailList().stream()
                    .filter(Objects::nonNull)
                    .map(s -> CharSequenceUtil.blankToDefault(s.getSkuNo(), s.getSkuId()))
                    .filter(StringUtils::isNotBlank)
                    .findFirst()
                    .orElse("-");
        }
        return "-";
    }

    private void mapMergeDeclareDetailToEntity(TmsDeclareBillDTO.MergeDeclareBillDetailDTO detailDTO, TmsDeclareBillDetailEntity detailEntity) {
        detailEntity.setSkuId(StringUtils.isNotBlank(detailDTO.getLeadSkuId()) ? detailDTO.getLeadSkuId() : null);
        detailEntity.setSkuNo(detailDTO.getSkuNo());
        detailEntity.setCustomsCode(detailDTO.getHsCode());
        detailEntity.setDeclareChineseName(detailDTO.getProductNameCn());
        detailEntity.setDeclareElement(detailDTO.getDeclareElement());
        detailEntity.setDeclareUnit(detailDTO.getUnit());
        detailEntity.setPrice(Objects.isNull(detailDTO.getUnitPrice()) ? BigDecimal.ZERO : detailDTO.getUnitPrice());
        detailEntity.setQty(Objects.isNull(detailDTO.getQty()) ? 0 : detailDTO.getQty());
        detailEntity.setDeclareCurrency(detailDTO.getDeclareCurrency());
        detailEntity.setDeclareCurrencySymbol(detailDTO.getDeclareCurrencySymbol());
        detailEntity.setSourceCountry(detailDTO.getSourceCountry());
        detailEntity.setSourceCountryName(detailDTO.getSourceCountryName());
        detailEntity.setToCountry(detailDTO.getToCountry());
        detailEntity.setToCountryName(detailDTO.getToCountryName());
        detailEntity.setSourceCargo(detailDTO.getSourceCargo());
        detailEntity.setExemption(detailDTO.getExemption());
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
                                 Map<String, String> currencyMap,
                                 boolean sixDimensionMerge,
                                 Map<String, SoDetailEntity> soDetailMap) {
        detailDTO.setHsCode(productLogisticsDTO.getCustomsCode());
        detailDTO.setProductNameCn(productLogisticsDTO.getDeclareChineseName());
        detailDTO.setDeclareElement(productLogisticsDTO.getDeclareElement());
        detailDTO.setUnit(productLogisticsDTO.getDeclareUnit());
        detailDTO.setUnitName(declareUnitNameMap.get(productLogisticsDTO.getDeclareUnit()));

        SoDetailEntity soDetailEntity = soDetailMap.get(buildSoDetailKey(detailDTO.getBusinessId(),detailDTO.getSkuId()));

        if (sixDimensionMerge && Objects.nonNull(soDetailEntity)) {
            // B2B 按客户分发场景：单价/币别/币别符号已由调用方按 so_detail.tax_price / currency / currency_symbol 填好，
            // 不允许再用物流产品覆盖；币别名称按保留的 declareCurrency 在字典里反查，保证与币别一致。
            detailDTO.setUnitPrice(soDetailEntity.getPrice());
            detailDTO.setDeclareCurrency(soDetailEntity.getCurrency());
            detailDTO.setDeclareCurrencySymbol(soDetailEntity.getCurrencySymbol());
            detailDTO.setDeclareCurrencyName(currencyMap.get(detailDTO.getDeclareCurrency()));
        } else {
            detailDTO.setUnitPrice(productLogisticsDTO.getPrice());
            detailDTO.setDeclareCurrency(productLogisticsDTO.getDeclareCurrency());
            detailDTO.setDeclareCurrencySymbol(productLogisticsDTO.getDeclareCurrencySymbol());
            detailDTO.setDeclareCurrencyName(currencyMap.get(productLogisticsDTO.getDeclareCurrency()));
        }
        detailDTO.setSourceCountry(productLogisticsDTO.getSourceCountry());
        detailDTO.setSourceCountryName(productLogisticsDTO.getSourceCountryName());
        if (StringUtils.isNotBlank(productLogisticsDTO.getSourceCargo())) {
            detailDTO.setSourceCargo(productLogisticsDTO.getSourceCargo());
        }
        if (StringUtils.isNotBlank(productLogisticsDTO.getExemption())) {
            detailDTO.setExemption(productLogisticsDTO.getExemption());
        }
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
     * 报关单类型转换为来源单类型，中间表 source_type 存来源单类型。
     */
    private String resolveDeclareSourceType(String declareBillType) {
        if (CharSequenceUtil.equals(declareBillType, SourceTypeEnum.FM_DECLARE_BILL.getCode())) {
            return SourceTypeEnum.FIRST_MILE_DELIVERY.getCode();
        }
        if (CharSequenceUtil.equals(declareBillType, SourceTypeEnum.B2B_DECLARE_BILL.getCode())) {
            return SourceTypeEnum.SO_DELIVERY_NOTICE.getCode();
        }
        throw new ServiceException(ApiError.LOGISTICS_DECLARE_BILL_TYPE_MISMATCH);
    }

    /**
     * 构建报关明细中间表。sourceType 为来源单类型，不是报关单类型。
     */
    private List<DeliveryDeclareDetailMidEntity> buildDeclareDetailMidList(List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> mergeDetailList,
                                                                           List<TmsDeclareBillDetailEntity> detailEntityList,
                                                                           String sourceType,
                                                                           String fallbackSourceId,
                                                                           String declareId,
                                                                           String declareCode) {
        //  源表 first_mile_delivery / so_delivery_notice 只存 transfer_warehouse_ids，
        // 上游 SQL (listBeforePushFmDeclare / listBeforePushB2bDeclare 等) 也仅 select ids，
        // 因此 SourceDeliveryDetailDTO.transferWarehouseNames 必然为空。
        // 这里按"整张报关单"为单位一次性收集 ids → 一次 RPC 拉名称 map，避免每行/每条来源单独 RPC。
        List<String> allTransferWarehouseIds = mergeDetailList.stream()
                .filter(Objects::nonNull)
                .map(TmsDeclareBillDTO.MergeDeclareBillDetailDTO::getSourceDeliveryDetailList)
                .filter(CollUtil::isNotEmpty)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getTransferWarehouseIds)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        Map<String, String> transferWarehouseNameMap = deliveryDeclareDetailMidService.getTransferWarehouseNameMap(allTransferWarehouseIds);

        List<DeliveryDeclareDetailMidEntity> midList = new ArrayList<>();
        for (int i = 0; i < mergeDetailList.size(); i++) {
            TmsDeclareBillDTO.MergeDeclareBillDetailDTO declareDetail = mergeDetailList.get(i);
            TmsDeclareBillDetailEntity billDetailEntity = detailEntityList.get(i);
            if (CollUtil.isEmpty(declareDetail.getSourceDeliveryDetailList())) {
                continue;
            }
            for (TmsDeclareBillDTO.SourceDeliveryDetailDTO sourceDetail : declareDetail.getSourceDeliveryDetailList()) {
                midList.add(buildDeclareDetailMidEntity(sourceType, sourceDetail, declareDetail,
                        fallbackSourceId, declareId, declareCode, billDetailEntity.getId(),
                        transferWarehouseNameMap));
            }
        }
        return midList;
    }

    /**
     * 格式化实体
     * @author will
     * @date 2026/5/7 19:18
     * @param sourceType
     * @param sourceDetail
     * @param declareDetail
     * @param fallbackSourceId
     * @param declareId
     * @param declareCode
     * @param declareDetailId
     * @return com.erp.model.tms.entity.DeliveryDeclareDetailMidEntity
     */
    private DeliveryDeclareDetailMidEntity buildDeclareDetailMidEntity(String sourceType,
                                                                       TmsDeclareBillDTO.SourceDeliveryDetailDTO sourceDetail,
                                                                       TmsDeclareBillDTO.MergeDeclareBillDetailDTO declareDetail,
                                                                       String fallbackSourceId,
                                                                       String declareId,
                                                                       String declareCode,
                                                                       String declareDetailId,
                                                                       Map<String, String> transferWarehouseNameMap) {
        DeliveryDeclareDetailMidEntity midEntity = new DeliveryDeclareDetailMidEntity();
        midEntity.setGenerateStatus(DeliveryDeclareDetailMidGenerateStatusEnum.FINISH.getCode());
        midEntity.setSourceType(sourceType);
        midEntity.setSourceId(StringUtils.defaultIfBlank(sourceDetail.getSourceId(), StringUtils.defaultString(fallbackSourceId)));
        midEntity.setSourceCode(StringUtils.defaultString(sourceDetail.getSourceCode()));
        midEntity.setBusinessId(StringUtils.defaultString(sourceDetail.getBusinessId()));
        midEntity.setBusinessCode(StringUtils.defaultString(sourceDetail.getBusinessCode()));
        midEntity.setBusinessType(sourceType);
        midEntity.setContractNo(declareCode);
        midEntity.setSkuId(StringUtils.defaultString(sourceDetail.getSkuId()));
        midEntity.setSkuNo(StringUtils.defaultString(sourceDetail.getSkuNo()));
        midEntity.setBomHistoryId(StringUtils.defaultString(sourceDetail.getBomHistoryId()));
        midEntity.setBomVersion(StringUtils.defaultString(sourceDetail.getBomVersion()));
        midEntity.setCurrency(StringUtils.defaultString(declareDetail.getDeclareCurrency()));
        midEntity.setCurrencySymbol(StringUtils.defaultString(declareDetail.getDeclareCurrencySymbol()));
        midEntity.setDeclareId(declareId);
        midEntity.setDeclareCode(declareCode);
        midEntity.setDeclareDetailId(declareDetailId);
        midEntity.setBoxNo(StringUtils.defaultString(sourceDetail.getBoxNo()));
        midEntity.setHsCode(StringUtils.defaultString(declareDetail.getHsCode()));
        midEntity.setProductNameCn(StringUtils.defaultString(declareDetail.getProductNameCn()));
        midEntity.setDeclareElement(StringUtils.defaultString(declareDetail.getDeclareElement()));
        midEntity.setUnit(StringUtils.defaultString(declareDetail.getUnit()));
        midEntity.setUnitPrice(Objects.isNull(declareDetail.getUnitPrice()) ? BigDecimal.ZERO : declareDetail.getUnitPrice());
        midEntity.setQty(Objects.isNull(sourceDetail.getQty()) ? 0 : sourceDetail.getQty());
        midEntity.setFromWarehouseId(sourceDetail.getFromWarehouseId());
        midEntity.setFromWarehouseName(sourceDetail.getFromWarehouseName());
        midEntity.setDestWarehouseId(sourceDetail.getDestWarehouseId());
        midEntity.setDestWarehouseName(sourceDetail.getDestWarehouseName());
        String transferWarehouseIds = StringUtils.defaultString(sourceDetail.getTransferWarehouseIds());
        midEntity.setTransferWarehouseIds(transferWarehouseIds);
        // 列表查询 dn.transferWarehouseName 才有值，避免显示成 "-"。
        midEntity.setTransferWarehouseNames(deliveryDeclareDetailMidService.buildTransferWarehouseNames(
                transferWarehouseIds, transferWarehouseNameMap));
        midEntity.setSalesOrgId(sourceDetail.getSalesOrgId());
        midEntity.setSalesOrgName(sourceDetail.getSalesOrgName());
        return midEntity;
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
        detailDTO.setBusinessId(entity.getBusinessId());
        detailDTO.setBusinessCode(entity.getBusinessCode());
        detailDTO.setBoxNo(entity.getBoxNo());
        detailDTO.setSkuId(entity.getSkuId());
        detailDTO.setSkuNo(entity.getSkuNo());
        detailDTO.setBomHistoryId(entity.getBomHistoryId());
        detailDTO.setBomVersion(entity.getBomVersion());
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
     * 构建来源明细匹配键
     * @author jack
     * @date 2026/4/30 16:35
     * @param entity
     * @return java.lang.String
     */
    private String buildSourceDetailKey(DeliveryDeclareDetailMidEntity entity) {
        return CharSequenceUtil.join("|",
                StringUtils.defaultString(entity.getSourceId()),
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
                StringUtils.defaultString(detailDTO.getBoxNo()),
                StringUtils.defaultString(detailDTO.getSkuId()));
    }

    /**
     * 判断自动生成请求是否已完整生成。
     *
     * @param mergeDetailList 本次请求明细
     * @param generatedMidList 已生成中间表明细
     * @return 是否幂等成功
     */
    private boolean isGeneratedIdempotentSuccess(List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> mergeDetailList,
                                                 List<DeliveryDeclareDetailMidEntity> generatedMidList) {
        if (CollUtil.isEmpty(mergeDetailList) || CollUtil.isEmpty(generatedMidList)) {
            return false;
        }
        Set<String> expectedKeySet = mergeDetailList.stream()
                .map(TmsDeclareBillDTO.MergeDeclareBillDetailDTO::getSourceDeliveryDetailList)
                .filter(CollUtil::isNotEmpty)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .map(this::buildSourceDetailKey)
                .collect(Collectors.toSet());
        if (CollUtil.isEmpty(expectedKeySet)) {
            return false;
        }
        Set<String> generatedKeySet = generatedMidList.stream()
                .filter(Objects::nonNull)
                .map(this::buildSourceDetailKey)
                .collect(Collectors.toSet());
        return expectedKeySet.equals(generatedKeySet);
    }


    @Override
    @DistributeLocker(
            businessType = DistributeKeyConstant.TMS_DECLARE_BILL_SOURCE_KEY,
            keyName = "list.declareBillList.sourceDeliveryDetailList.sourceId",
            maxRetries = 1,
            unlockAfterTx = true
    )
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean batchAddMergeDetail(String type, List<TmsDeclareBillDTO.MergeDeclareBillDTO> list) {
        return batchAddMergeDetail(type, list, null, Boolean.FALSE);
    }

    /**
     * 批量保存合并报关明细。
     *
     * @param splitCodeSequence 非空时表示拆分保存：合同号为「原报关单合同号_1、_2…」递增，贯穿多次调用（多箱/多票拆分）
     */
    private Boolean batchAddMergeDetail(String type, List<TmsDeclareBillDTO.MergeDeclareBillDTO> list,
                                        TmsDeclareBillDTO.SplitDeclareCodeSequence splitCodeSequence,
                                        boolean idempotent) {
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException(ApiError.BILL_SELECTION_REQUIRED);
        }
        String declareBillType = type;
        String sourceType = resolveDeclareSourceType(declareBillType);
        List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> mergeDetailList = list.stream()
                .filter(Objects::nonNull)
                .map(TmsDeclareBillDTO.MergeDeclareBillDTO::getDeclareBillList)
                .filter(CollUtil::isNotEmpty)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(mergeDetailList)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_SAVE_REQUIRED);
        }
        // 先按"单据 + SKU + 缺哪些报关字段"提示缺失，便于使用方一次性看到要补哪些 PLM 资料；
        // 之后再走 validateBatchMergeDeclareBills 的明细维度/票/箱号等校验。
        validateAutoMergeDeclareDetailRequired(mergeDetailList);
        // 目的国为中国大陆的来源单整批失败，错误信息列出所有命中的来源单号，
        // 不进入入库流程；来源单 declare_status 不动，仍保持 WAIT。
        validateDestCountryNotMainlandChina(mergeDetailList);
        validateBatchMergeDeclareBills(list);

        Set<String> sourceKeySet = mergeDetailList.stream()
                .map(TmsDeclareBillDTO.MergeDeclareBillDetailDTO::getSourceDeliveryDetailList)
                .filter(CollUtil::isNotEmpty)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .map(this::buildSourceDetailKey)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        if (CollUtil.isEmpty(sourceKeySet)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_SOURCE_DETAIL_NOT_FOUND_FOR_SAVE);
        }
        Set<String> sourceIdSet = collectSourceIdSet(mergeDetailList);

        List<DeliveryDeclareDetailMidEntity> existsMidList = deliveryDeclareDetailMidService.lambdaQuery()
                .eq(DeliveryDeclareDetailMidEntity::getSourceType, sourceType)
                .in(CollUtil.isNotEmpty(sourceIdSet), DeliveryDeclareDetailMidEntity::getSourceId, sourceIdSet)
                .list();
        existsMidList = existsMidList.stream()
                .filter(item -> sourceKeySet.contains(buildSourceDetailKey(item)))
                .collect(Collectors.toList());

        if (idempotent && CollUtil.isNotEmpty(existsMidList)) {
            List<DeliveryDeclareDetailMidEntity> generatedMidList = existsMidList.stream()
                    .filter(item -> StringUtils.isNotBlank(item.getDeclareId())
                            || DeliveryDeclareDetailMidGenerateStatusEnum.FINISH.getCode().equals(item.getGenerateStatus()))
                    .collect(Collectors.toList());
            if (isGeneratedIdempotentSuccess(mergeDetailList, generatedMidList)) {
                log.info("自动生成报关明细幂等命中，type={}，sourceKeyCount={}", type, sourceKeySet.size());
                return Boolean.TRUE;
            }
            if (CollUtil.isNotEmpty(generatedMidList)) {
                String repeatSourceCode = generatedMidList.stream()
                        .map(DeliveryDeclareDetailMidEntity::getSourceCode)
                        .filter(StringUtils::isNotBlank)
                        .distinct()
                        .collect(Collectors.joining("、"));
                if (StringUtils.isBlank(repeatSourceCode)) {
                    throw new ServiceException(ApiError.LOGISTICS_DECLARE_SELECTED_DETAIL_GENERATED);
                }
                throw new ServiceException(ApiError.LOGISTICS_DECLARE_SOURCE_GENERATED, repeatSourceCode);
            }
        }

        // 拆分保存多轮调用：上一轮已生成报关单及中间表，不能按「已生成」拦截，也不能删除刚生成的报关单
        if (splitCodeSequence == null) {
            Set<String> obsoleteDeclareBillIds = existsMidList.stream()
                    .map(DeliveryDeclareDetailMidEntity::getDeclareId)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toSet());

            if (CollUtil.isNotEmpty(obsoleteDeclareBillIds)) {
                // 合并确认：原报关单上的中间表已挂 declare_id，先按原单删除再落新单
                for (String declareBillId : obsoleteDeclareBillIds) {
                    TmsDeclareBillEntity oldBill = super.getById(declareBillId);
                    if (Objects.isNull(oldBill)) {
                        continue;
                    }
                    if (!CharSequenceUtil.equals(oldBill.getType(), declareBillType)) {
                        throw new ServiceException(ApiError.LOGISTICS_DECLARE_REPLACE_TYPE_MISMATCH, CharSequenceUtil.blankToDefault(oldBill.getCode(), declareBillId));
                    }
                    if (!CharSequenceUtil.equals(oldBill.getDeclareStatus(), DeclareStatusEnum.WAIT.getCode())) {
                        throw new ServiceException(ApiError.LOGISTICS_DECLARE_REPLACE_WAIT_STATUS_REQUIRED, CharSequenceUtil.blankToDefault(oldBill.getCode(), declareBillId));
                    }
                    deleteDeclareBillById(declareBillId);
                }
            } else {
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
                    if (StringUtils.isBlank(repeatSourceCode)) {
                        throw new ServiceException(ApiError.LOGISTICS_DECLARE_SELECTED_DETAIL_GENERATED);
                    }
                    throw new ServiceException(ApiError.LOGISTICS_DECLARE_SOURCE_GENERATED, repeatSourceCode);
                }
            }
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
                mapMergeDeclareDetailToEntity(detailDTO, detailEntity);
                detailEntityList.add(detailEntity);
            }
            if (CollUtil.isEmpty(detailEntityList)) {
                continue;
            }

            TmsDeclareBillEntity declareBillEntity = new TmsDeclareBillEntity();
            //数据处理
            fillBatchDeclareBillEntity(declareBillType, declareBillList, declareBillEntity);

            if (splitCodeSequence != null) {
                declareBillEntity.setCode(splitCodeSequence.nextCode());
            }
            BaseResultDTO.AddDTO addResult = add(declareBillEntity, detailEntityList, SourceTypeEnum.getEnum(declareBillType), false);

            addMidList.addAll(buildDeclareDetailMidList(declareBillList, detailEntityList,
                    sourceType, null, addResult.getId(), addResult.getCode()));
        }
        if (CollUtil.isNotEmpty(addMidList)) {
            deliveryDeclareDetailMidService.saveBatch(addMidList);
        }
        //更新报关状态
        updateSourceDeclareStatus(declareBillType, addMidList);
        return Boolean.TRUE;
    }

    /**
     * 校验批量保存合并报关明细
     * @author will
     * @date 2026/5/7 16:29
     * @param list
     */
    private void validateBatchMergeDeclareBills(List<TmsDeclareBillDTO.MergeDeclareBillDTO> list) {
        Map<String, Integer> boxBillIndexMap = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            TmsDeclareBillDTO.MergeDeclareBillDTO mergeDeclareBillDTO = list.get(i);
            if (Objects.isNull(mergeDeclareBillDTO) || CollUtil.isEmpty(mergeDeclareBillDTO.getDeclareBillList())) {
                continue;
            }
            validateDeclareMergeDetails(mergeDeclareBillDTO.getDeclareBillList());
            for (TmsDeclareBillDTO.MergeDeclareBillDetailDTO detailDTO : mergeDeclareBillDTO.getDeclareBillList()) {
                if (CollUtil.isEmpty(detailDTO.getSourceDeliveryDetailList())) {
                    continue;
                }
                for (TmsDeclareBillDTO.SourceDeliveryDetailDTO sourceDetail : detailDTO.getSourceDeliveryDetailList()) {
                    if (Objects.isNull(sourceDetail) || StringUtils.isBlank(sourceDetail.getBoxNo())) {
                        continue;
                    }
                    String boxKey = buildDeclareBoxKey(sourceDetail);
                    Integer existBillIndex = boxBillIndexMap.putIfAbsent(boxKey, i + 1);
                    if (Objects.nonNull(existBillIndex) && existBillIndex != i + 1) {
                        throw new ServiceException(ApiError.LOGISTICS_DECLARE_BOX_SINGLE_BILL_REQUIRED, existBillIndex, i + 1);
                    }
                }
            }
        }
    }

    /**
     * 填充批量保存报关单主表
     * @author will
     * @date 2026/5/7 16:29
     * @param type
     * @param declareBillList
     * @param declareBillEntity
     */
    private void fillBatchDeclareBillEntity(String type,
                                            List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> declareBillList,
                                            TmsDeclareBillEntity declareBillEntity) {
        declareBillEntity.setType(type);
        applyBatchDeclareBillDefaults(declareBillEntity);
        declareBillEntity.setDeclareStatus(com.erp.model.tms.enums.DeclareStatusEnum.WAIT.getCode());
        declareBillEntity.setDeclareDate(LocalDate.now());
        declareBillEntity.setShippingFee(BigDecimal.ZERO);
        declareBillEntity.setInsuranceFee(BigDecimal.ZERO);
        declareBillEntity.setOtherFee(BigDecimal.ZERO);

        TmsDeclareBillDTO.MergeDeclareBillDetailDTO firstDetail = declareBillList.stream()
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(new TmsDeclareBillDTO.MergeDeclareBillDetailDTO());
        declareBillEntity.setCountry(firstDetail.getToCountry());
        declareBillEntity.setCountryName(firstDetail.getToCountryName());
        declareBillEntity.setToArea(firstDetail.getToCountry());
        declareBillEntity.setToPort(firstDetail.getToCountry());

        TmsDeclareBillDTO.SelectedSkuHeaderParamDTO headerParamDTO = new TmsDeclareBillDTO.SelectedSkuHeaderParamDTO();
        headerParamDTO.setSourceDeliveryDetailList(declareBillList.stream()
                .map(TmsDeclareBillDTO.MergeDeclareBillDetailDTO::getSourceDeliveryDetailList)
                .filter(CollUtil::isNotEmpty)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        TmsDeclareBillDTO.SelectedSkuHeaderDTO headerDTO = querySelectedSkuHeader(headerParamDTO, SourceTypeEnum.getEnum(type));
        declareBillEntity.setTransportNo(headerDTO.getTransportNo());
        declareBillEntity.setBoxQty(Objects.isNull(headerDTO.getBoxQty()) ? 0 : headerDTO.getBoxQty());
        declareBillEntity.setGrossWeight(Objects.isNull(headerDTO.getGrossWeight()) ? BigDecimal.ZERO : headerDTO.getGrossWeight());
        declareBillEntity.setNetWeight(Objects.isNull(headerDTO.getNetWeight()) ? BigDecimal.ZERO : headerDTO.getNetWeight());
        fillBatchDeclareBillBusinessType(type, headerParamDTO.getSourceDeliveryDetailList(), declareBillEntity);

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("ruleType", type);
        paramMap.put("countryCode", declareBillEntity.getCountry());
        //发货仓
        String fromWarehouseId = headerParamDTO.getSourceDeliveryDetailList().stream().map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getFromWarehouseId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.joining(","));
        paramMap.put("fromWarehouseId", fromWarehouseId);
        //中转仓
        String transferWarehouseId = headerParamDTO.getSourceDeliveryDetailList().stream().map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getTransferWarehouseIds).filter(StringUtils::isNotBlank).distinct().collect(Collectors.joining(","));
        paramMap.put("transferWarehouseId", transferWarehouseId);
        if (CharSequenceUtil.equals(type,SourceTypeEnum.FM_DECLARE_BILL.getCode())) {
            //目的仓
            String destWarehouseId = headerParamDTO.getSourceDeliveryDetailList().stream().map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getDestWarehouseId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.joining(","));
            paramMap.put("destWarehouseId", destWarehouseId);
        } else {
            //组织
            String salesOrgId = headerParamDTO.getSourceDeliveryDetailList().stream().map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getSalesOrgId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.joining(","));
            paramMap.put("salesOrgId", salesOrgId);
        }
        CfgDeclareRuleEntity cfgDeclareRule = cfgDeclareRuleService.listMatchedRule(paramMap);
        if (cfgDeclareRule == null) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_RULE_NOT_FOUND_FOR_SOURCE);
        }
        declareBillEntity.setSenderId(cfgDeclareRule.getSenderId());
        declareBillEntity.setSenderName(cfgDeclareRule.getSenderName());
        declareBillEntity.setSenderType(cfgDeclareRule.getSenderType());

        fillBatchDeclareReceiver(type, headerParamDTO.getSourceDeliveryDetailList(), cfgDeclareRule, declareBillEntity);
    }

    private void fillBatchDeclareReceiver(String type,
                                          List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailList,
                                          CfgDeclareRuleEntity cfgDeclareRule,
                                          TmsDeclareBillEntity declareBillEntity) {
        declareBillEntity.setReceiverId(cfgDeclareRule.getReceiverId());
        declareBillEntity.setReceiverName(cfgDeclareRule.getReceiverName());
        declareBillEntity.setReceiverType(cfgDeclareRule.getReceiverType());

        if (!CharSequenceUtil.equals(type, SourceTypeEnum.B2B_DECLARE_BILL.getCode())
                || !CharSequenceUtil.equals(cfgDeclareRule.getReceiverType(), CfgDeclareRuleReceiverTypeEnum.BY_CUSTOMER.getCode())
                || CollUtil.isEmpty(sourceDetailList)) {
            return;
        }

        List<String> sourceIdList = sourceDetailList.stream()
                .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getSourceId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(sourceIdList)) {
            return;
        }

        List<SoDeliveryNoticeEntity> noticeList = soDeliveryNoticeFeign.listByIds(sourceIdList);
        if (CollUtil.isEmpty(noticeList)) {
            return;
        }
        Map<String, SoDeliveryNoticeEntity> noticeMap = noticeList.stream()
                .filter(Objects::nonNull)
                .filter(item -> StringUtils.isNotBlank(item.getId()))
                .collect(Collectors.toMap(SoDeliveryNoticeEntity::getId, Function.identity(), (a, b) -> a));

        for (String sourceId : sourceIdList) {
            SoDeliveryNoticeEntity notice = noticeMap.get(sourceId);
            if (Objects.isNull(notice) || StringUtils.isBlank(notice.getCustomerId())) {
                continue;
            }
            declareBillEntity.setReceiverId(notice.getCustomerId());
            declareBillEntity.setReceiverName(notice.getCustomerName());
            return;
        }
    }

    /**
     * 填充批量保存报关单默认字段，保持与自动下推保存入口一致。
     * @author will
     * @date 2026/5/13 12:32
     * @param declareBillEntity 报关单主表
     */
    private void applyBatchDeclareBillDefaults(TmsDeclareBillEntity declareBillEntity) {
        declareBillEntity.setDeclareType(DeclareDeclareTypeEnum.INDEPENDENT.getCode());
        declareBillEntity.setDictSupervisionMethod(DeclareSupervisionMethodEnum.COMMONLY.getCode());
        declareBillEntity.setDictNatureLevy(DeclareNatureLevyEnum.COMMONLY.getCode());
        declareBillEntity.setDictPackType(DeclarePackTypeEnum.CARTON.getCode());
        declareBillEntity.setDictTransactionMethod(DeclareTransactionMethodEnum.EXW.getCode());
        // 贸易国默认中国香港，与 addFmDeclare / addB2BDeclare 对齐。
        applyTradingAreaDefault(declareBillEntity);
    }

    /**
     * 填充批量保存报关单业务类型
     * @author will
     * @date 2026/5/7 16:29
     * @param type
     * @param sourceDetailList
     * @param declareBillEntity
     */
    private void fillBatchDeclareBillBusinessType(String type,
                                                  List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailList,
                                                  TmsDeclareBillEntity declareBillEntity) {
        List<String> sourceIdList = sourceDetailList.stream()
                .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getSourceId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(sourceIdList)) {
            return;
        }
        if (CharSequenceUtil.equals(type, SourceTypeEnum.FM_DECLARE_BILL.getCode())) {
            List<TmsDeclareBillDTO.DeliveryDTO> deliveryDTOList = getCanGenerateDeliveryOrder(TmsDeclareBillDTO.QuerySourceDTO.builder().ids(sourceIdList).build());
            declareBillEntity.setBusinessType(joinDistinct(deliveryDTOList.stream()
                    .map(TmsDeclareBillDTO.DeliveryDTO::getBusinessType)
                    .collect(Collectors.toList())));
        } else if (CharSequenceUtil.equals(type, SourceTypeEnum.B2B_DECLARE_BILL.getCode())) {
            List<TmsDeclareBillDTO.SoOutDTO> deliveryDTOList = getCanGenerateSoOut(TmsDeclareBillDTO.QuerySourceDTO.builder().ids(sourceIdList).build());
            declareBillEntity.setBusinessType(joinDistinct(deliveryDTOList.stream()
                    .map(TmsDeclareBillDTO.SoOutDTO::getBusinessType)
                    .collect(Collectors.toList())));
        }
    }

    /**
     * 构建报关箱号校验键
     * @author will
     * @date 2026/5/7 16:29
     * @param sourceDetail
     * @return java.lang.String
     */
    private String buildDeclareBoxKey(TmsDeclareBillDTO.SourceDeliveryDetailDTO sourceDetail) {
        String sourceKey = StringUtils.defaultIfBlank(sourceDetail.getSourceId(), sourceDetail.getBusinessId());
        return CharSequenceUtil.join("|", StringUtils.defaultString(sourceKey), StringUtils.defaultString(sourceDetail.getBoxNo()));
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
            Boolean updateResult = wmsFirstMileDeliveryFeign.updateStatus(dto);
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
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_B2B_SPLIT_VIEW_FORBIDDEN);
        }
        if (!CharSequenceUtil.equals(declareBillEntity.getDeclareStatus(), DeclareStatusEnum.WAIT.getCode())) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_SPLIT_WAIT_STATUS_REQUIRED);
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
            splitDeclareDTO.setBusinessCode(value.stream().map(DeliveryDeclareDetailMidEntity::getBusinessCode).filter(StringUtils::isNotBlank).findFirst().orElse(""));
            //sku信息描述格式：skuNo*qty,skuNo*qty
            String skuDesc = value.stream().map(obj -> CharSequenceUtil.format("{}*{}", obj.getSkuNo(), obj.getQty())).collect(Collectors.joining(","));
            splitDeclareDTO.setSkuDesc(skuDesc);
            List<TmsDeclareBillDTO.SplitDetailDTO> splitDetailDTOList = value.stream().map(obj -> new TmsDeclareBillDTO.SplitDetailDTO(obj.getSkuId(), obj.getSkuNo(), obj.getQty())).collect(Collectors.toList());
            splitDeclareDTO.setSkuDetailList(splitDetailDTOList);
            splitDeclareDTOList.add(splitDeclareDTO);
        }
        return splitDeclareDTOList;
    }


}
