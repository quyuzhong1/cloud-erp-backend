package com.erp.server.tms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
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
import com.erp.model.tms.dto.*;
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
import com.erp.server.tms.utils.DeclareMergeDefaults;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_GENERATABLE_DELIVERY_NOT_FOUND);
        }
        TmsDeclareBillDTO.DeliveryDTO deliveryDTO = deliveryDTOList.get(0);
        if (CollUtil.isEmpty(addDTO.getMergeDetailList())) {
            addDTO.setMergeDetailList(prepareAddMergeDetailList(addDTO, SourceTypeEnum.FIRST_MILE_DELIVERY));
        }

        // 新增页面下推保存逻辑：按前端提交的合并明细直接生成报关单
        if (CollUtil.isNotEmpty(addDTO.getMergeDetailList())) {
            List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> mergeDetailList = prepareSubmittedMergeDetailList(addDTO.getMergeDetailList(), addDTO.getIsMerge());
            validateDeclareMergeDetails(mergeDetailList);
            Set<String> sourceDetailIdSet = collectSourceDetailIdSet(mergeDetailList);
            validateSourceDetailNotGenerated(sourceDetailIdSet, null);

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
                    SourceTypeEnum.FIRST_MILE_DELIVERY.getCode(), addDTO.getSourceId(), addResult.getId(), addResult.getCode());
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
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_GENERATABLE_DETAIL_NOT_FOUND);
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
        Set<String> sourceDetailIdSet = collectSourceDetailIdSet(mergeDetailList);
        validateSourceDetailNotGenerated(sourceDetailIdSet, old.getId());
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
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_BILL_SAVE_FAILED);
        }
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
        deliveryDeclareDetailMidService.lambdaUpdate()
                .eq(DeliveryDeclareDetailMidEntity::getDeclareId, old.getId())
                .remove();
        List<DeliveryDeclareDetailMidEntity> addMidList = buildDeclareDetailMidList(mergeDetailList, detailEntityList,
                sourceType, null, old.getId(), old.getCode());
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
            String logisticsSupplierIds = deliveryDTOList.stream().map(TmsDeclareBillDTO.DeliveryDTO::getLogisticsSupplierId).distinct().collect(Collectors.joining(";"));
            viewDTO.setLogisticsSupplierId(logisticsSupplierIds);
            //物流供应商名称
            String logisticsSupplierNames = deliveryDTOList.stream().map(TmsDeclareBillDTO.DeliveryDTO::getLogisticsSupplierName).distinct().collect(Collectors.joining(";"));
            viewDTO.setLogisticsSupplierName(logisticsSupplierNames);
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
            String logisticsSupplierIds = deliveryDTOList.stream().map(TmsDeclareBillDTO.SoOutDTO::getLogisticsSupplierId).distinct().collect(Collectors.joining(";"));
            viewDTO.setLogisticsSupplierId(logisticsSupplierIds);
            //物流供应商名称
            String logisticsSupplierNames = deliveryDTOList.stream().map(TmsDeclareBillDTO.SoOutDTO::getLogisticsSupplierName).distinct().collect(Collectors.joining(";"));
            viewDTO.setLogisticsSupplierName(logisticsSupplierNames);
        }

        fillViewDTO(viewDTO);
        return viewDTO;
    }

    @Override
    public List<TmsDeclareBillDTO.BatchUpdateFieldDropDownDTO> batchUpdateFieldDropDown() {
        CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingEnum.DECLARE_BATCH_UPDATE_FIELD.getCode());
        if (ObjectUtil.isEmpty(cfgSettingEntity) || ObjectUtil.isEmpty(cfgSettingEntity.getDataJson())) {
            return Collections.emptyList();
        }
        List<TmsDeclareBillDTO.BatchUpdateFieldDropDownDTO> dropDownList = parseBatchUpdateFieldDropDown(cfgSettingEntity.getDataJson());
        dropDownList.sort(Comparator.comparing(TmsDeclareBillDTO.BatchUpdateFieldDropDownDTO::getIndex, Comparator.nullsLast(Integer::compareTo)));
        return dropDownList;
    }

    private List<TmsDeclareBillDTO.BatchUpdateFieldDropDownDTO> parseBatchUpdateFieldDropDown(JSONObject dataJson) {
        if (ObjectUtil.isEmpty(dataJson)) {
            return new ArrayList<>();
        }
        if (ObjectUtil.isNotEmpty(dataJson.getJSONArray("data"))) {
            return JSONUtil.toList(dataJson.getJSONArray("data"), TmsDeclareBillDTO.BatchUpdateFieldDropDownDTO.class);
        }
        if (ObjectUtil.isNotEmpty(dataJson.get("field"))) {
            return Collections.singletonList(JSONUtil.toBean(dataJson, TmsDeclareBillDTO.BatchUpdateFieldDropDownDTO.class));
        }
        List<TmsDeclareBillDTO.BatchUpdateFieldDropDownDTO> result = new ArrayList<>();
        dataJson.values().forEach(value -> {
            if (ObjectUtil.isEmpty(value)) {
                return;
            }
            try {
                TmsDeclareBillDTO.BatchUpdateFieldDropDownDTO dto = JSONUtil.toBean(JSONUtil.parseObj(value), TmsDeclareBillDTO.BatchUpdateFieldDropDownDTO.class);
                if (ObjectUtil.isNotEmpty(dto) && ObjectUtil.isNotEmpty(dto.getField())) {
                    result.add(dto);
                }
            } catch (Exception e) {
                log.warn("解析报关单批量更新字段配置失败, value={}", value, e);
            }
        });
        return result;
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
            TmsDeclareBillEntity declareBillEntity = getDeclareBillByIdAndType(id, sourceTypeEnum);
            DeclareStatusEnum targetStatus = DeclareStatusEnum.getEnum(dto.getDeclareStatus());
            if (Objects.isNull(targetStatus)) {
                throw new ServiceException(ApiError.LOGISTICS_DECLARE_STATUS_INVALID);
            }
            if (DeclareStatusEnum.WAIT.getCode().equals(declareBillEntity.getDeclareStatus()) && DeclareStatusEnum.CONFIRMED.equals(targetStatus)) {
                fillDeclareConfirmUser(dto);
            }
            confirmDeclareStatusSingle(declareBillEntity, dto, targetStatus, sourceTypeEnum);
            return BatchResultDTO.success(declareBillEntity.getId(), declareBillEntity.getCode(), "报关状态更新成功");
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
            updateDeclareStatus(entity, targetStatus.getCode(), dto.getDeclarConfirmDate(), dto.getDeclarUserId(), dto.getDeclarUserName());
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
            updateDeclareStatus(entity, targetStatus.getCode(), entity.getDeclarConfirmDate(), entity.getDeclarUserId(), entity.getDeclarUserName());
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
        List<TmsDeclareBillDTO.SoOutDTO> deliveryDTOList = this.getCanGenerateSoOut(querySourceDTO);
        if (CollectionUtils.isEmpty(deliveryDTOList)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_GENERATABLE_SO_DELIVERY_NOT_FOUND);
        }
        TmsDeclareBillDTO.SoOutDTO deliveryDTO = deliveryDTOList.get(0);
        if (CollUtil.isEmpty(addDTO.getMergeDetailList())) {
            addDTO.setMergeDetailList(prepareAddMergeDetailList(addDTO, SourceTypeEnum.SO_DELIVERY_NOTICE));
        }

        // 新增页面下推保存逻辑：按前端提交的合并明细直接生成报关单
        if (CollUtil.isNotEmpty(addDTO.getMergeDetailList())) {
            List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> mergeDetailList = prepareSubmittedMergeDetailList(addDTO.getMergeDetailList(), addDTO.getIsMerge());
            validateDeclareMergeDetails(mergeDetailList);
            Set<String> sourceDetailIdSet = collectSourceDetailIdSet(mergeDetailList);
            validateSourceDetailNotGenerated(sourceDetailIdSet, null);

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
            declareBillEntity.setNetWeight(Objects.isNull(declareBillEntity.getNetWeight()) ? BigDecimal.ZERO : declareBillEntity.getNetWeight());

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
                    SourceTypeEnum.SO_DELIVERY_NOTICE.getCode(), addDTO.getSourceId(), addResult.getId(), addResult.getCode());
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
            case SENDER_ID:
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
            case RECEIVER_ID:
                updateWrapper.set(fieldName, Objects.toString(fieldValue, ""));
                updateWrapper.set("receiver_name", name);
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
        // 删除报关单关联的中间表（按报关单主键 declare_id，与来源单 source_id 无关）
        deliveryDeclareDetailMidService.lambdaUpdate()
                .eq(DeliveryDeclareDetailMidEntity::getDeclareId, id)
                .remove();
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
        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> result = prepareSourceDetailsForDeclarationGeneration(viewDTO.getSourceDeliveryDetailList());
        if (CollUtil.isEmpty(result)) {
            return Collections.emptyList();
        }
        DeclarationGenerationService declarationGenerationService = new DeclarationGenerationService();
        boolean sixDimensionMerge = Objects.isNull(includeSkuInMergeKey) ? isSixDimensionMerge(result) : includeSkuInMergeKey;
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
     */
    private List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> prepareSourceDetailsForDeclarationGeneration(List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDeliveryDetailList) {
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

        for (TmsDeclareBillDTO.SourceDeliveryDetailDTO detailDTO : sourceDeliveryDetailList) {
            ProductDetailDTO.ProductLogisticDTO productLogisticsDTO = logisticsMap.get(detailDTO.getSkuId());
            if (Objects.nonNull(productLogisticsDTO)) {
                fillDeclareInfo(detailDTO, productLogisticsDTO, declareUnitNameMap, currencyMap);
            }
            detailDTO.setCountryName(currencyMap.get(detailDTO.getCountryId()));
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
                    sourceDeliveryDetailDTO.setQty((detailDTO.getQty() == null ? 0 : detailDTO.getQty()) * (logisticDTO.getChildQty() == null ? 1 : logisticDTO.getChildQty()));
                    fillDeclareInfo(sourceDeliveryDetailDTO, logisticDTO, declareUnitNameMap, currencyMap);
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
     * 准备新增报关明细
     * @author will
     * @date 2026/5/7 14:08
     * @param addDTO
     * @param sourceTypeEnum
     * @return java.util.List<com.erp.model.tms.dto.TmsDeclareBillDTO.MergeDeclareBillDetailDTO>
     */
    private List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> prepareAddMergeDetailList(TmsDeclareBillDTO.AddDTO addDTO,
                                                                                       SourceTypeEnum sourceTypeEnum) {
        if (CollUtil.isNotEmpty(addDTO.getMergeDetailList())) {
            return prepareSubmittedMergeDetailList(addDTO.getMergeDetailList(), addDTO.getIsMerge());
        }
        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailList = listSourceDetailsForAdd(addDTO.getSourceId(), sourceTypeEnum);
        return mergeSourceDetails(sourceDetailList, addDTO.getIsMerge());
    }

    /**
     * 准备前端提交的报关明细
     * @author will
     * @date 2026/5/7 14:08
     * @param mergeDetailList
     * @param isMerge
     * @return java.util.List<com.erp.model.tms.dto.TmsDeclareBillDTO.MergeDeclareBillDetailDTO>
     */
    private List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> prepareSubmittedMergeDetailList(List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> mergeDetailList,
                                                                                             Boolean isMerge) {
        List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> filteredList = mergeDetailList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(filteredList)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_SAVE_REQUIRED);
        }
        if (Boolean.TRUE.equals(isMerge)) {
            return mergeEditedDetails(filteredList);
        }
        filteredList.forEach(this::applyMergeDeclareDetailDefaults);
        return filteredList;
    }

    /**
     * 查询新增来源明细
     * @author will
     * @date 2026/5/7 14:08
     * @param sourceId
     * @param sourceTypeEnum
     * @return java.util.List<com.erp.model.tms.dto.TmsDeclareBillDTO.SourceDeliveryDetailDTO>
     */
    private List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> listSourceDetailsForAdd(String sourceId, SourceTypeEnum sourceTypeEnum) {
        TmsDeclareBillDTO.PushDeclareBeforeParamDTO paramDTO = new TmsDeclareBillDTO.PushDeclareBeforeParamDTO(Boolean.FALSE, Collections.singletonList(sourceId), null);
        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailList;
        if (SourceTypeEnum.FIRST_MILE_DELIVERY == sourceTypeEnum) {
            sourceDetailList = wmsFirstMileDeliveryFeign.listBeforePushFmDeclare(paramDTO);
        } else if (SourceTypeEnum.SO_DELIVERY_NOTICE == sourceTypeEnum) {
            sourceDetailList = soDeliveryNoticeFeign.listBeforePushB2bDeclare(paramDTO);
        } else {
            sourceDetailList = Collections.emptyList();
        }
        if (CollUtil.isEmpty(sourceDetailList)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_GENERATABLE_DETAIL_NOT_FOUND);
        }
        sourceDetailList.forEach(item -> item.setSourceType(sourceTypeEnum.getCode()));
        return sourceDetailList;
    }

    /**
     * 按规则合并来源明细
     * @author will
     * @date 2026/5/7 14:08
     * @param sourceDetailList
     * @param isMerge
     * @return java.util.List<com.erp.model.tms.dto.TmsDeclareBillDTO.MergeDeclareBillDetailDTO>
     */
    private List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> mergeSourceDetails(List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailList,
                                                                                Boolean isMerge) {
        List<TmsDeclareBillDTO.MergeDeclareBillDTO> mergeDeclareBillList = autoMergeDeclareBillView(
                new TmsDeclareBillDTO.AutoMergeDeclareBillViewDTO(Boolean.TRUE.equals(isMerge), sourceDetailList));
        return flattenMergeDeclareBillList(mergeDeclareBillList);
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
        for (TmsDeclareBillDTO.SourceDeliveryDetailDTO sourceDetail : detailDTO.getSourceDeliveryDetailList()) {
            if (Objects.isNull(sourceDetail)) {
                continue;
            }
            requireNotBlank(sourceDetail.getSourceDetailId(), rowNo, "来源明细");
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
     * 校验来源明细是否已生成报关单
     * @author will
     * @date 2026/5/7 14:08
     * @param sourceDetailIdSet
     * @param currentDeclareId
     */
    private void validateSourceDetailNotGenerated(Set<String> sourceDetailIdSet, String currentDeclareId) {
        if (CollUtil.isEmpty(sourceDetailIdSet)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_SOURCE_DETAIL_NOT_FOUND_FOR_SAVE);
        }
        List<DeliveryDeclareDetailMidEntity> existsMidList;
        if (StringUtils.isNotBlank(currentDeclareId)) {
            existsMidList = deliveryDeclareDetailMidService.lambdaQuery()
                    .in(DeliveryDeclareDetailMidEntity::getSourceDetailId, sourceDetailIdSet)
                    .ne(DeliveryDeclareDetailMidEntity::getDeclareId, currentDeclareId)
                    .list();
        } else {
            existsMidList = deliveryDeclareDetailMidService.lambdaQuery()
                    .in(DeliveryDeclareDetailMidEntity::getSourceDetailId, sourceDetailIdSet)
                    .list();
        }
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
        Set<String> oldSourceDetailIdSet = oldMidList.stream()
                .map(DeliveryDeclareDetailMidEntity::getSourceDetailId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        for (TmsDeclareBillDTO.MergeDeclareBillDetailDTO detailDTO : mergeDetailList) {
            if (CollUtil.isEmpty(detailDTO.getSourceDeliveryDetailList())) {
                continue;
            }
            for (TmsDeclareBillDTO.SourceDeliveryDetailDTO sourceDetail : detailDTO.getSourceDeliveryDetailList()) {
                if (Objects.isNull(sourceDetail) || !oldSourceDetailIdSet.contains(sourceDetail.getSourceDetailId())) {
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
     * 校验相同SKU是否存在不同行明细
     * @author will
     * @date 2026/5/7 14:08
     * @param mergeDetailList
     */
    private void validateDuplicateSkuRows(List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> mergeDetailList) {
        Map<String, Integer> skuRowMap = new HashMap<>();
        for (int i = 0; i < mergeDetailList.size(); i++) {
            String skuNo = mergeDetailList.get(i).getSkuNo();
            if (StringUtils.isBlank(skuNo)) {
                continue;
            }
            Set<String> rowSkuSet = Arrays.stream(skuNo.split(","))
                    .map(StringUtils::trimToEmpty)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toSet());
            for (String rowSku : rowSkuSet) {
                Integer firstRow = skuRowMap.putIfAbsent(rowSku, i + 1);
                if (Objects.nonNull(firstRow)) {
                    throw new ServiceException(ApiError.LOGISTICS_DECLARE_DUPLICATE_SKU_ROW, rowSku, firstRow, i + 1);
                }
            }
        }
    }

    /**
     * 收集来源明细id
     * @author will
     * @date 2026/5/7 14:08
     * @param mergeDetailList
     * @return java.util.Set<java.lang.String>
     */
    private Set<String> collectSourceDetailIdSet(List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> mergeDetailList) {
        return mergeDetailList.stream()
                .map(TmsDeclareBillDTO.MergeDeclareBillDetailDTO::getSourceDeliveryDetailList)
                .filter(CollUtil::isNotEmpty)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getSourceDetailId)
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
        List<DeliveryDeclareDetailMidEntity> midList = new ArrayList<>();
        for (int i = 0; i < mergeDetailList.size(); i++) {
            TmsDeclareBillDTO.MergeDeclareBillDetailDTO declareDetail = mergeDetailList.get(i);
            TmsDeclareBillDetailEntity billDetailEntity = detailEntityList.get(i);
            if (CollUtil.isEmpty(declareDetail.getSourceDeliveryDetailList())) {
                continue;
            }
            for (TmsDeclareBillDTO.SourceDeliveryDetailDTO sourceDetail : declareDetail.getSourceDeliveryDetailList()) {
                midList.add(buildDeclareDetailMidEntity(sourceType, sourceDetail, declareDetail,
                        fallbackSourceId, declareId, declareCode, billDetailEntity.getId()));
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
                                                                       String declareDetailId) {
        DeliveryDeclareDetailMidEntity midEntity = new DeliveryDeclareDetailMidEntity();
        midEntity.setDeclareStatus(DeclareStatusEnum.WAIT.getCode());
        midEntity.setGenerateStatus(DeliveryDeclareDetailMidGenerateStatusEnum.FINISH.getCode());
        midEntity.setSourceType(sourceType);
        midEntity.setSourceId(StringUtils.defaultIfBlank(sourceDetail.getSourceId(), StringUtils.defaultString(fallbackSourceId)));
        midEntity.setSourceCode(StringUtils.defaultString(sourceDetail.getSourceCode()));
        midEntity.setSourceDetailId(StringUtils.defaultString(sourceDetail.getSourceDetailId()));
        midEntity.setBusinessId(StringUtils.defaultString(sourceDetail.getBusinessId()));
        midEntity.setBusinessCode(StringUtils.defaultString(sourceDetail.getBusinessCode()));
        midEntity.setBusinessType(sourceType);
        midEntity.setContractNo(declareCode);
        midEntity.setSkuId(StringUtils.defaultString(sourceDetail.getSkuId()));
        midEntity.setSkuNo(StringUtils.defaultString(sourceDetail.getSkuNo()));
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
        midEntity.setTransferWarehouseIds(StringUtils.defaultString(sourceDetail.getTransferWarehouseIds()));
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
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean batchAddMergeDetail(String type, List<TmsDeclareBillDTO.MergeDeclareBillDTO> list) {
        return batchAddMergeDetail(type, list, null, Boolean.FALSE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean batchAddMergeDetail(String type, List<TmsDeclareBillDTO.MergeDeclareBillDTO> list, Boolean idempotent) {
        return batchAddMergeDetail(type, list, null, Boolean.TRUE.equals(idempotent));
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
        validateBatchMergeDeclareBills(list);

        Set<String> sourceDetailIdSet = mergeDetailList.stream()
                .map(TmsDeclareBillDTO.MergeDeclareBillDetailDTO::getSourceDeliveryDetailList)
                .filter(CollUtil::isNotEmpty)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getSourceDetailId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        if (CollUtil.isEmpty(sourceDetailIdSet)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_SOURCE_DETAIL_NOT_FOUND_FOR_SAVE);
        }

        List<DeliveryDeclareDetailMidEntity> existsMidList = deliveryDeclareDetailMidService.lambdaQuery()
                .eq(DeliveryDeclareDetailMidEntity::getSourceType, sourceType)
                .in(DeliveryDeclareDetailMidEntity::getSourceDetailId, sourceDetailIdSet)
                .list();

        if (idempotent && CollUtil.isNotEmpty(existsMidList)) {
            List<DeliveryDeclareDetailMidEntity> generatedMidList = existsMidList.stream()
                    .filter(item -> StringUtils.isNotBlank(item.getDeclareId())
                            || DeliveryDeclareDetailMidGenerateStatusEnum.FINISH.getCode().equals(item.getGenerateStatus()))
                    .collect(Collectors.toList());
            if (isGeneratedIdempotentSuccess(mergeDetailList, generatedMidList)) {
                log.info("自动生成报关明细幂等命中，type={}，sourceDetailCount={}", type, sourceDetailIdSet.size());
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
        declareBillEntity.setReceiverId(cfgDeclareRule.getReceiverId());
        declareBillEntity.setSenderName(cfgDeclareRule.getSenderName());
        declareBillEntity.setReceiverName(cfgDeclareRule.getReceiverName());
        declareBillEntity.setSenderType(cfgDeclareRule.getSenderType());
        declareBillEntity.setReceiverType(cfgDeclareRule.getReceiverType());
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
            //sku信息描述格式：skuNo*qty,skuNo*qty
            String skuDesc = value.stream().map(obj -> CharSequenceUtil.format("{}*{}", obj.getSkuNo(), obj.getQty())).collect(Collectors.joining(","));
            splitDeclareDTO.setSkuDesc(skuDesc);
            List<TmsDeclareBillDTO.SplitDetailDTO> splitDetailDTOList = value.stream().map(obj -> new TmsDeclareBillDTO.SplitDetailDTO(obj.getSourceDetailId(), obj.getSkuId(), obj.getSkuNo(), obj.getQty())).collect(Collectors.toList());
            splitDeclareDTO.setSkuDetailList(splitDetailDTOList);
            splitDeclareDTOList.add(splitDeclareDTO);
        }
        return splitDeclareDTOList;
    }


}
