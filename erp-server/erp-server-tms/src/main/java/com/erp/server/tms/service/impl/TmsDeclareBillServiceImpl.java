package com.erp.server.tms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.dto.base.*;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.OrderTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.RedisUtil;
import com.common.business.vo.PagingVO;
import com.common.core.constant.EnumMessage;
import com.common.core.dto.ExcelData;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.entity.BasicDictEntity;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.tms.dto.AutoGenerateBillDTO;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.DictBasicDTO;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.*;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.model.wms.enums.PackingTaskStatusEnum;
import com.erp.model.wms.enums.WmsDeclareStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.server.tms.mapper.TmsDeclareBillMapper;
import com.erp.server.tms.service.*;
import com.google.common.collect.Lists;
import freemarker.template.utility.StringUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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

    @Override
    public Boolean addFmDeclare(TmsDeclareBillDTO.AddDTO addDTO) {
        TmsDeclareBillDTO.QuerySourceDTO querySourceDTO = TmsDeclareBillDTO.QuerySourceDTO.builder()
//                .packingStatus(PackingTaskStatusEnum.PACKED.getCode())
                .declareStatus(WmsDeclareStatusEnum.WAIT.getCode())
                .ids(Arrays.asList(addDTO.getSourceId()))
                .build();
        if(!addDTO.getIsAuto()){
            querySourceDTO.setPackingStatus(PackingTaskStatusEnum.PACKED.getCode());
        }
        List<TmsDeclareBillDTO.DeliveryDTO> deliveryDTOList = wmsFirstMileDeliveryFeign.getCanGenerateDeclare(querySourceDTO);
        if(CollectionUtils.isEmpty(deliveryDTOList)){
            throw new ServiceException("没有可生成报关单的发货单");
        }
        TmsDeclareBillDTO.DeliveryDTO deliveryDTO = deliveryDTOList.get(0);
        TmsDeclareBillEntity baseTmsDeclareBillEntity = new TmsDeclareBillEntity();
        BeanMapperUtils.copy(addDTO, baseTmsDeclareBillEntity);
        BeanMapperUtils.copy(deliveryDTO, baseTmsDeclareBillEntity);
        baseTmsDeclareBillEntity.setSourceType(SourceTypeEnum.FIRST_MILE_DELIVERY.getCode());
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
        if(!addDTO.getIsAuto()){
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
    @Override
    public Boolean update(TmsDeclareBillDTO.UpdateDTO updateDTO,SourceTypeEnum sourceTypeEnum) {
        TmsDeclareBillEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "报关单"));
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
        if(!old.getDeclareStatus().equals(com.erp.model.tms.enums.DeclareStatusEnum.WAIT.getCode())){
            throw new ServiceException("报关单状态不是待报关，不能编辑");
        }
        boolean save = super.updateById(tmsDeclareBillEntity);
        if(!save) {
            throw new ServiceException("报关单保存失败");
        }
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
        IPage<TmsDeclareBillDTO.PagingVO> pageData = baseMapper.paging(query, params);
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
        List<String> sourceCodes = list.stream().map(TmsDeclareBillDTO.PagingVO::getSourceCode).collect(Collectors.toList());
        List<String> allMergeSourceIds = list.stream()
                .map(obj -> obj.getMergeSourceId().split(","))
                .flatMap(Arrays::stream)
                .distinct().collect(Collectors.toList());
        List<TmsDeclareBillEntity> allMergeSourceList = this.listByIds(allMergeSourceIds);
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
            List<String> sourceCodeList = new ArrayList<>();
            sourceCodeList.add(v.getSourceCode());
            List<String> mergeIds = Arrays.asList(v.getMergeSourceId().split(","));
            List<String> mergedSourceCodeList = allMergeSourceList.stream().filter(t->mergeIds.contains(t.getId())).map(TmsDeclareBillEntity::getSourceCode).distinct().collect(Collectors.toList());
            sourceCodeList.addAll(mergedSourceCodeList);
            v.setSourceCodeList(sourceCodeList.stream().distinct().collect(Collectors.joining(",")));
            v.setIsInvalid(v.getDeclareStatus().equals(com.erp.model.tms.enums.DeclareStatusEnum.INVALID.getCode()));
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
        Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST, "报关单"));
        TmsDeclareBillDTO.ViewDTO viewDTO = BeanUtil.copyProperties(entity,TmsDeclareBillDTO.ViewDTO.class);
        List<String> list = Arrays.asList(entity.getMergeSourceId().split(","));
        List<String> sourceCodeList = new ArrayList<>();
        List<String> sourceIdList = new ArrayList<>();
        sourceCodeList.add(entity.getSourceCode());
        sourceIdList.add(entity.getSourceId());
        if(CollectionUtils.isNotEmpty(list)){
            List<TmsDeclareBillEntity> entityList = this.listByIds(list);
            sourceCodeList.addAll(entityList.stream().map(TmsDeclareBillEntity::getSourceCode).collect(Collectors.toList()));
            sourceIdList.addAll(entityList.stream().map(TmsDeclareBillEntity::getSourceId).collect(Collectors.toList()));
        }
        sourceIdList = sourceIdList.stream().distinct().collect(Collectors.toList());
        sourceCodeList = sourceCodeList.stream().distinct().collect(Collectors.toList());
        viewDTO.setSourceCodeList(sourceCodeList);
        List<TmsDeclareBillDetailEntity> detailEntityList = detailService.listByMainIds(Arrays.asList(entity.getId()));
        List<TmsDeclareBillDTO.ProductDetail> productDetailList = BeanUtil.copyToList(detailEntityList,TmsDeclareBillDTO.ProductDetail.class);
        productDetailList.forEach(v->v.setTotalPrice(v.getPrice().multiply(new BigDecimal(v.getQty()))));
        viewDTO.setProductDetailList(productDetailList);
        if(entity.getType().equals(SourceTypeEnum.FM_DECLARE_BILL.getCode())){
            List<TmsDeclareBillDTO.DeliveryDTO> deliveryDTOList = this.getCanGenerateDeliveryOrder(TmsDeclareBillDTO.QuerySourceDTO.builder().ids(sourceIdList).build());
            if(CollectionUtils.isEmpty(deliveryDTOList)){
                throw new ServiceException("未找到发货单信息");
            }
            List<TmsDeclareBillDTO.PackingDTO> allPackDTOList = deliveryDTOList.stream()
                    .filter(v -> CollectionUtils.isNotEmpty(v.getPackingDTOList()))
                    .flatMap(v -> v.getPackingDTOList().stream())
                    .collect(Collectors.toList());
            if(deliveryDTOList.size()>1){
                deliveryDTOList = deliveryDTOList.stream().filter(v->v.getSourceCode().equals(entity.getSourceCode())).collect(Collectors.toList());
            }
            TmsDeclareBillDTO.DeliveryDTO deliveryDTO = deliveryDTOList.get(0);
            deliveryDTO.setPackingDTOList(allPackDTOList);
            BeanUtil.copyProperties(deliveryDTO,viewDTO, CopyOptions.create().setOverride(false));
            viewDTO.setLogisticsSupplierId(deliveryDTO.getLogisticsSupplierId());
            viewDTO.setLogisticsSupplierName(deliveryDTO.getLogisticsSupplierName());
        }else if(entity.getType().equals(SourceTypeEnum.B2B_DECLARE_BILL.getCode())){
            List<TmsDeclareBillDTO.SoOutDTO> deliveryDTOList = this.getCanGenerateSoOut(TmsDeclareBillDTO.QuerySourceDTO.builder().ids(sourceIdList).build());
            if(CollectionUtils.isEmpty(deliveryDTOList)){
                throw new ServiceException("未找到销售出库单信息");
            }
            List<TmsDeclareBillDTO.PackingDTO> allPackDTOList = deliveryDTOList.stream()
                    .filter(v -> CollectionUtils.isNotEmpty(v.getPackingDTOList()))
                    .flatMap(v -> v.getPackingDTOList().stream())
                    .collect(Collectors.toList());
            if(deliveryDTOList.size()>1){
                deliveryDTOList = deliveryDTOList.stream().filter(v->v.getSourceCode().equals(entity.getSourceCode())).collect(Collectors.toList());
            }
            TmsDeclareBillDTO.SoOutDTO deliveryDTO = deliveryDTOList.get(0);
            deliveryDTO.setPackingDTOList(allPackDTOList);
            BeanUtil.copyProperties(deliveryDTO,viewDTO, CopyOptions.create().setOverride(false));
            viewDTO.setLogisticsSupplierId(deliveryDTO.getLogisticsSupplierId());
            viewDTO.setLogisticsSupplierName(deliveryDTO.getLogisticsSupplierName());
        }

        fillViewDTO(viewDTO);
        return viewDTO;
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
        //处理单位
        List<BasicDictEntity> sysDictBasicEntityList = plmTaskFeign.listDictByType("declareUnit");
        for (TmsDeclareBillDTO.ProductDetail productDetail : viewDTO.getProductDetailList()) {
            BasicDictEntity unitDTO = sysDictBasicEntityList.stream().filter(v->v.getValue().equals(productDetail.getDeclareUnit())).findFirst().orElse(new BasicDictEntity());
            productDetail.setDeclareUnitName(unitDTO.getName());
            productDetail.setDeclareCurrencyName(CurrencyEnum.getNameByCode(productDetail.getDeclareCurrency()));
        }
        //处理发货人
        if(StringUtils.isNotBlank(viewDTO.getSenderId())){
            SysAccountingCompanyEntity sysAccountingCompanyEntity = sysUserFeign.getCompanyById(viewDTO.getSenderId());
            if(Objects.nonNull(sysAccountingCompanyEntity)){
                viewDTO.setSenderName(sysAccountingCompanyEntity.getCompanyName());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> updateToDeclare(TmsDeclareBillDTO.UpdateDeclareStatusDTO dto,SourceTypeEnum sourceTypeEnum) {
        if(Objects.isNull(dto.getDate())){
            throw new ServiceException("报关日期不能为空");
        }
        List<TmsDeclareBillEntity> entityList = this.listByIds(dto.getIds());
        if(CollectionUtils.isEmpty(entityList)){
            return new ArrayList<>();
        }
        List<String> sourceIds = entityList.stream().map(TmsDeclareBillEntity::getSourceId).collect(Collectors.toList());
        List<SoOutstockEntity> soOutstockEntityList = new ArrayList<>();
        List<FirstMileDeliveryEntity> firstMileDeliveryEntityList = new ArrayList<>();
        if(sourceTypeEnum == SourceTypeEnum.B2B_DECLARE_BILL){
            soOutstockEntityList = soOutstockFeign.listByIds(sourceIds);
        }else if (sourceTypeEnum == SourceTypeEnum.FM_DECLARE_BILL){
            firstMileDeliveryEntityList = wmsFirstMileDeliveryFeign.listByIds(sourceIds);
        }
        List<TmsDeclareBillEntity> updateList = new ArrayList<>();
        List<BatchResultDTO> resultList = new ArrayList<>();
        for (TmsDeclareBillEntity entity : entityList) {
            if(!entity.getDeclareStatus().equals(com.erp.model.tms.enums.DeclareStatusEnum.WAIT.getCode())){
                resultList.add(BatchResultDTO.fail(entity.getId(),entity.getCode(),"只有待报关的单据才能更新成已报关"));
                continue;
            }
            if(sourceTypeEnum == SourceTypeEnum.B2B_DECLARE_BILL){
                SoOutstockEntity soOutstockEntity = soOutstockEntityList.stream().filter(v->v.getId().equals(entity.getSourceId())).findFirst().orElse(null);
                if(Objects.isNull(soOutstockEntity)){
                    resultList.add(BatchResultDTO.fail(entity.getId(),entity.getCode(),"查询不到来源的出库单"));
                    continue;
                }
                if(!soOutstockEntity.getApproveStatus().equals(ApproveStatusEnum.APPROVE)){
                    resultList.add(BatchResultDTO.fail(entity.getId(),entity.getCode(),CharSequenceUtil.format("关联单据{}尚未审核通过无法报关",soOutstockEntity.getCode())));
                    continue;
                }
            }else if (sourceTypeEnum == SourceTypeEnum.FM_DECLARE_BILL){
                FirstMileDeliveryEntity firstMileDeliveryEntity = firstMileDeliveryEntityList.stream().filter(v->v.getId().equals(entity.getSourceId())).findFirst().orElse(null);
                if(Objects.isNull(firstMileDeliveryEntity)){
                    resultList.add(BatchResultDTO.fail(entity.getId(),entity.getCode(),"查询不到来源的发货单"));
                    continue;
                }
                if(!firstMileDeliveryEntity.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())){
                    resultList.add(BatchResultDTO.fail(entity.getId(),entity.getCode(),CharSequenceUtil.format("关联单据{}尚未审核通过无法报关",firstMileDeliveryEntity.getCode())));
                    continue;
                }
            }
            entity.setDeclareStatus(com.erp.model.tms.enums.DeclareStatusEnum.DECLARED.getCode());
            entity.setDeclareDate(dto.getDate());
            updateList.add(entity);
        }
        if(CollectionUtils.isNotEmpty(updateList)){
            this.updateBatchById(updateList);
        }
        return resultList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> cancelDeclare(TmsDeclareBillDTO.UpdateDeclareStatusDTO dto) {
        List<TmsDeclareBillEntity> entityList = this.listByIds(dto.getIds());
        List<TmsDeclareBillEntity> updateList = new ArrayList<>();
        List<BatchResultDTO> resultList = new ArrayList<>();
        for (TmsDeclareBillEntity entity : entityList) {
            if(!entity.getDeclareStatus().equals(com.erp.model.tms.enums.DeclareStatusEnum.DECLARED.getCode())){
                resultList.add(BatchResultDTO.fail(entity.getId(),entity.getCode(),"只有已报关的单据才能取消报关"));
                continue;
            }
            entity.setDeclareStatus(com.erp.model.tms.enums.DeclareStatusEnum.WAIT.getCode());
            entity.setDeclareDate(null);
            updateList.add(entity);
        }
        if(CollectionUtils.isNotEmpty(updateList)){
            this.updateBatchById(updateList);
        }
        return resultList;
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
            throw new ServiceException("仅支持待报关的报关单合并");
        }
        TmsDeclareBillEntity mergedEntity = entityList.stream().filter(v->v.getCode().equals(dto.getCode())).findFirst().orElse(null);
        if(Objects.isNull(mergedEntity)){
            throw new ServiceException("选择的报关单中没有该表头");
        }
        if(entityList.stream().anyMatch(v->StringUtils.isBlank(v.getCountryName())) ||
        entityList.stream().map(TmsDeclareBillEntity::getCountryName).collect(Collectors.toSet()).size() > 1){
            throw new ServiceException("不同国家报关单不能合并");
        }
        List<String> outOutCodeList = entityList.stream().map(TmsDeclareBillEntity::getSourceCode).distinct().collect(Collectors.toList());
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
        mergedEntity.setMergeSourceId(String.join(",",ids));
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
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> cancelMerge(TmsDeclareBillDTO.MergeDeclareDTO dto) {
        List<TmsDeclareBillEntity> entityList = this.listByIds(dto.getIds());
        List<String> allMergeSourceIds = entityList.stream()
                .map(obj -> obj.getMergeSourceId().split(","))
                .flatMap(Arrays::stream)
                .distinct().collect(Collectors.toList());
        List<TmsDeclareBillEntity> allBeforeEntityList = this.listByIds(allMergeSourceIds);
        List<TmsDeclareBillEntity> updateList = new ArrayList<>();
        List<BatchResultDTO> resultList = new ArrayList<>();
        for (TmsDeclareBillEntity entity : entityList) {
            if(!entity.getDeclareStatus().equals(com.erp.model.tms.enums.DeclareStatusEnum.WAIT.getCode())){
                resultList.add(BatchResultDTO.fail(entity.getId(),entity.getCode(),"只有待报关的单据才能取消合并"));
                continue;
            }
            List<String> list = Arrays.asList(entity.getMergeSourceId().split(","));
            List<TmsDeclareBillEntity> beforeEntityList = allBeforeEntityList.stream().filter(v->list.contains(v.getId())).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(beforeEntityList)){
                resultList.add(BatchResultDTO.fail(entity.getId(),entity.getCode(),"仅可操作有存在合并订单类型"));
                continue;
            }
            entity.setDeclareStatus(com.erp.model.tms.enums.DeclareStatusEnum.INVALID.getCode());
            beforeEntityList.forEach(v->v.setDeclareStatus(com.erp.model.tms.enums.DeclareStatusEnum.WAIT.getCode()));
            updateList.add(entity);
            updateList.addAll(beforeEntityList);
        }
        if(CollectionUtils.isNotEmpty(updateList)){
            this.updateBatchById(updateList);
        }
        return resultList;
    }


    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> delete(TmsDeclareBillDTO.DeleteDTO dto) {
        List<TmsDeclareBillEntity> entityList = this.listByIds(dto.getIds());
        List<BatchResultDTO> resultList = new ArrayList<>();
        List<String> removeIds = new ArrayList<>();
        List<String> updateFhdSourceIds = new ArrayList<>();
        List<String> updateOutSourceIds = new ArrayList<>();
        for (TmsDeclareBillEntity entity : entityList) {
            if(!entity.getDeclareStatus().equals(com.erp.model.tms.enums.DeclareStatusEnum.WAIT.getCode())){
                resultList.add(BatchResultDTO.fail(entity.getId(),entity.getCode(),"只有待报关的单据才能删除"));
                continue;
            }
            removeIds.add(entity.getId());
            List<String> mergeIds = Arrays.asList(entity.getMergeSourceId().split(","));
            if(CollectionUtils.isNotEmpty(mergeIds)){
                List<TmsDeclareBillEntity> mergeEntityList = this.listByIds(mergeIds);
                if(CollectionUtils.isNotEmpty(mergeEntityList)){
                    if(entity.getType().equals(SourceTypeEnum.FM_DECLARE_BILL.getCode())){
                        updateFhdSourceIds.addAll(mergeEntityList.stream().map(TmsDeclareBillEntity::getSourceId).collect(Collectors.toList()));
                    }
                    if(entity.getType().equals(SourceTypeEnum.B2B_DECLARE_BILL.getCode())){
                        updateOutSourceIds.addAll(mergeEntityList.stream().map(TmsDeclareBillEntity::getSourceId).collect(Collectors.toList()));
                    }
                }
            }
            if(entity.getType().equals(SourceTypeEnum.FM_DECLARE_BILL.getCode())){
                updateFhdSourceIds.add(entity.getSourceId());
            }
            if(entity.getType().equals(SourceTypeEnum.B2B_DECLARE_BILL.getCode())){
                updateOutSourceIds.add(entity.getSourceId());
            }
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
            TmsDeclareBillDTO.UpdateStatusDTO updateStatusDTO = new TmsDeclareBillDTO.UpdateStatusDTO();
            updateStatusDTO.setIds(updateOutSourceIds);
            updateStatusDTO.setDeclareStatus(WmsDeclareStatusEnum.WAIT.code);
            soOutstockFeign.updateStatus(updateStatusDTO);
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

        return lambdaQuery().in(TmsDeclareBillEntity::getSourceId, sourceIds).list();
    }

    @Override
    public List<TmsDeclareBillDTO.SoOutDTO> getCanGenerateSoOut(TmsDeclareBillDTO.QuerySourceDTO querySourceDTO) {
        List<TmsDeclareBillDTO.SoOutDTO> deliveryDTOList = soOutstockFeign.getCanGenerateDeclare(querySourceDTO);
        List<String> sourceCodes = deliveryDTOList.stream().map(TmsDeclareBillDTO.SoOutDTO::getSourceCode).collect(Collectors.toList());
        List<LogisticsBillEntity> logisticsBillEntityList = logisticService.listByOutstockCodeList(sourceCodes);
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
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean addB2BDeclare(TmsDeclareBillDTO.AddDTO addDTO) {
        TmsDeclareBillDTO.QuerySourceDTO querySourceDTO = TmsDeclareBillDTO.QuerySourceDTO.builder()
//                .packingStatus(PackingTaskStatusEnum.PACKED.getCode())
                .declareStatus(WmsDeclareStatusEnum.WAIT.getCode())
                .ids(Arrays.asList(addDTO.getSourceId()))
                .build();
        if(!addDTO.getIsAuto()){
            querySourceDTO.setPackingStatus(PackingTaskStatusEnum.PACKED.getCode());
        }
        List<TmsDeclareBillDTO.SoOutDTO> soOutDTOList = soOutstockFeign.getCanGenerateDeclare(querySourceDTO);
        if(CollectionUtils.isEmpty(soOutDTOList)){
            throw new ServiceException("没有可生成报关单的单据");
        }
        TmsDeclareBillDTO.SoOutDTO soOutDTO = soOutDTOList.get(0);
        TmsDeclareBillEntity baseTmsDeclareBillEntity = new TmsDeclareBillEntity();
        BeanMapperUtils.copy(addDTO, baseTmsDeclareBillEntity);
        BeanMapperUtils.copy(soOutDTO, baseTmsDeclareBillEntity);
        baseTmsDeclareBillEntity.setSourceType(SourceTypeEnum.SO_OUTSTOCK.getCode());
        baseTmsDeclareBillEntity.setDeclareStatus(com.erp.model.tms.enums.DeclareStatusEnum.WAIT.getCode());
        baseTmsDeclareBillEntity.setType(SourceTypeEnum.B2B_DECLARE_BILL.getCode());
        //50个明细为一个报关单
        List<TmsDeclareBillDTO.ProductDetail> allProductDetailList = soOutDTO.getProductDetailList();
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
        if(!addDTO.getIsAuto()){
            TmsDeclareBillDTO.UpdateStatusDTO dto = new TmsDeclareBillDTO.UpdateStatusDTO();
            dto.setIds(Arrays.asList(addDTO.getSourceId()));
            dto.setDeclareStatus(WmsDeclareStatusEnum.FINISH.getCode());
            soOutstockFeign.updateStatus(dto);
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
    @GlobalTransactional(rollbackFor = Exception.class)
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
                throw new ServiceException(ApiError.ERROR_1015);
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
}
