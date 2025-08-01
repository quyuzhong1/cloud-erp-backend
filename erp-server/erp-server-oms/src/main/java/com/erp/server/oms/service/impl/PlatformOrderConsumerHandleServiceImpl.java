package com.erp.server.oms.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.PlatformOrderDetailDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.dto.SplitSkuDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.*;
import com.erp.model.plm.vo.SkuInfoSimpleVO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.rpc.dmp.feign.DmpMongoDbFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.server.oms.kingdee.SyncSoB2cService;
import com.erp.server.oms.rocketmq.consumer.NewPlatformRefundOrderConsumerService;
import com.erp.server.oms.rocketmq.consumer.NewPlatformReturnOrderConsumerService;
import com.erp.server.oms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 平台消费处理接口
 *
 * @param
 * @author Jim
 * @date 2023/12/18 16:42
 * @Return
 */
@Slf4j
@Service
public class PlatformOrderConsumerHandleServiceImpl implements PlatformOrderConsumerHandleService {

    @Resource
    private SoB2cService soB2cService;
    @Resource
    private SoB2cDetailService soB2cDetailService;
    @Resource
    private SoB2cLogisticsService soB2cLogisticsService;
    @Resource
    private SoB2cReceiverService soB2cReceiverService;
    @Resource
    private SoB2cFinanceService soB2cFinanceService;

    @Resource
    private SoB2cExtendService soB2cExtendService;

    @Resource
    private CustomerB2cService customerB2cService;
    @Resource
    private CustomerB2cAddressService customerB2cAddressService;
    @Resource
    private CustomerB2cContactService customerB2cContactService;

    @Resource
    private ShopInfoService shopInfoService;
    @Resource
    private SysDictFeign sysDictFeign;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private DmpMongoDbFeign dmpMongoDbFeign;
    @Resource
    private DictBasicService dictBasicService;
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private SkuMappingService skuMappingService;
    @Resource
    private NewPlatformReturnOrderConsumerService newPlatformReturnOrderConsumerService;
    @Resource
    private NewPlatformRefundOrderConsumerService newPlatformRefundOrderConsumerService;
    @Lazy
    @Resource
    private PlatformOrderConsumerHandleService platformOrderConsumerHandleService;

    @Resource
    private SoB2cErrorService soB2cErrorService;

    @Resource
    private SyncSoB2cService syncSoB2cService;

    @Resource
    private SoB2cSplitService soB2cSplitService;

    @Resource
    private InvoiceInfoService invoiceInfoService;

    @Resource
    private CfgInvoiceSettingDetailService cfgInvoiceSettingDetailService;
    @Resource
    private CfgRuleInvoiceService cfgRuleInvoiceService;

    @Resource
    private SoB2cCoreService soB2cCoreService;

    @Override
    public void handleAll(PlatformOrderDTO dto) {
        String oldBillStatus = dto.getBillStatus();
        SoB2cDTO.PullOrderResultDTO resultDTO = platformOrderConsumerHandleService.checkAndSaveAll(dto);
        SoB2cEntity mainEntity = resultDTO.getSoB2cEntity();
        //平台仓订单
        Boolean hasPlatformWarehouse = mainEntity.hasPlatformWarehouseOrder();
        Boolean isWarehouseEmpty = resultDTO.getIsWarehouseEmpty();
        //已发货
        String shipped = SoB2cBillStatusEnum.ENUM_SHIPPED.getCode();
        String billStatus = mainEntity.getBillStatus();
        Boolean isShipped = shipped.equals(billStatus);
        //如果已发货且仓库为空且是平台仓订单
        if (isShipped && isWarehouseEmpty && hasPlatformWarehouse && !PlatformDictEnum.ALI_EXPRESS.getCode().equals(mainEntity.getDictPlatform())) {
            String warehouseId = resultDTO.getShopWarehouseId();
            if(StringUtils.isNotBlank(warehouseId)){
              soB2cDetailService.updateWarehouseIdByMainId(mainEntity.getId(),warehouseId,true);
            }
        }

        Boolean retryFlag = false;
        // 跳过未作废的自发货无地址的订单
        // 待发货/已发货订单不生成异常
        if ( notPlatformOrderNotExistAddress(dto,oldBillStatus)
                && null != dto.getInvalidStatus()
                && !dto.getInvalidStatus()
                && !SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equalsIgnoreCase(mainEntity.getBillStatus())
                && !SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode().equalsIgnoreCase(mainEntity.getBillStatus())
        ) {
            log.warn("卖家自发货订单无地址暂不新增：单号={}", dto.getPlatformCode());

            SoB2cErrorEntity soB2cError = resultDTO.getSoB2cError();
            if(null == soB2cError){
                //记录异常
                SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO();
                addError.setType(SoB2cErrorTypeEnum.ORDER_FETCH.getCode());
                addError.setMainId(mainEntity.getId());
                addError.setMessage(ApiError.ERROR_SO_B2C_ORDER_FETCH.msg);
                soB2cErrorService.add(addError);

                //更新主表error标识
                soB2cService.lambdaUpdate()
                        .set(SoB2cEntity::getSignOrderError, SoB2cErrorTypeEnum.ORDER_FETCH.getCode())
                        .eq(SoB2cEntity::getId, mainEntity.getId())
                        .update();
            }
        }else{
            //拉取成功后清除订单异常信息，并自动触发订单审核和配货规则
            SoB2cErrorEntity soB2cError = resultDTO.getSoB2cError();
            if(null != soB2cError){
                soB2cErrorService.removeErrorOrder(mainEntity.getId(), SoB2cErrorTypeEnum.ORDER_FETCH.getCode());
                retryFlag = true;
            }
        }
        // 平台仓订单不走任何规则
        // 取消订单不走规则
        if ( (!mainEntity.hasPlatformWarehouseOrder()
                && !mainEntity.getIsCancel()
                && !ApproveStatusEnum.REJECT.equals(mainEntity.getApproveStatus())) || Boolean.TRUE.equals(retryFlag)
        ) {
            // 已审核过的订单不走规则
            Integer count = operateLogService.lambdaQuery()
                    .eq(OperateLogEntity::getBusinessId, mainEntity.getId())
                    .eq(OperateLogEntity::getOperation, "审核操作")
                    .count();
            if (0 == count){
                // 规则处理(分平台)
                SoB2cHandler.handleRule(mainEntity);
            }
        }
        // 销售出库单处理(分平台)
        SoB2cHandler.handleSoOutStock(dto, resultDTO, mainEntity);
        //平台取消订单后自动取消预报
        if(Objects.nonNull(mainEntity.getIsCancel()) && mainEntity.getIsCancel()){
            soB2cService.autoCancelOrderForecast(mainEntity);
        }

        // 非平台
        if (!mainEntity.hasPlatformWarehouseOrder()
                && resultDTO.isUpdateCancel()
                && SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode().equalsIgnoreCase(mainEntity.getBillStatus())
                && !mainEntity.getIsIntercept()
        ){
            soB2cService.deliveryIntercept(mainEntity.getId(), "平台取消");
        }


//        //走过订单规则审核的不需要重复推送DMP，规则审核时已经推送过
//        Integer count = operateLogService.lambdaQuery()
//                .eq(OperateLogEntity::getBusinessId, mainEntity.getId())
//                .eq(OperateLogEntity::getOperation, "审核操作")
//                .count();
//        if (0 == count) {
//            //推送到DMP
//            soB2cService.syncOrderToDmp(mainEntity.getId(), SyncOperateEnum.OPERATE_UPDATE.getCode());
//        }

        // 退货单处理
        if (CollectionUtils.isNotEmpty(dto.getReturnDTOList())){
            dto.getReturnDTOList().forEach(e->{
                newPlatformReturnOrderConsumerService.handle(JSON.toJSONString(e));
            });
        }

        // 退款单处理
        if (CollectionUtils.isNotEmpty(dto.getRefundDTOList())){
            dto.getRefundDTOList().forEach(e->{
                newPlatformRefundOrderConsumerService.handle(JSON.toJSONString(e));
            });
        }
        //亚马逊平台仓订单已发货生成发票
        if (isShipped  && hasPlatformWarehouse && PlatformDictEnum.AMAZON.getCode().equals(mainEntity.getDictPlatform())) {
            List<InvoiceInfoEntity> invoiceInfoEntities = invoiceInfoService.listBySoIds(Collections.singletonList(mainEntity.getId()));
            if (CollectionUtils.isEmpty(invoiceInfoEntities)){
                try {
                    invoiceInfoService.batchGenerateVatInvoice(Collections.singletonList(mainEntity.getId()));
                }catch (Exception e){
                    log.error("亚马逊订单已发货生成发票异常：{}",e.getMessage());
                }
            }
        }
//        //生成nf-e发票
//        SoB2cEntity entity = CharSequenceUtil.isNotBlank(mainEntity.getId()) ? soB2cService.getById(mainEntity.getId()) : null;
//        if (Objects.nonNull(entity) && ApproveStatusEnum.APPROVE.getCode().equals(entity.getApproveStatus().getCode())){
//            cfgInvoiceSettingDetailService.generateNfeInvoice (mainEntity,InvoiceNodeEnum.AFTER_AUDIT.getCode());
//        }
    }

    /**
     * 检查亚马逊卖家自发货订单无地址
     */
    private Boolean checkHasMfnOrderAndNoAddress(PlatformOrderDTO dto) {
        if (StrUtil.isNotBlank(dto.getLabelJson())) {
            SoB2cDTO.LabelDTO labelJsonDTO = JSONUtil.toBean(dto.getLabelJson(), SoB2cDTO.LabelDTO.class);
            //FBA
            if ("AFN".equalsIgnoreCase(labelJsonDTO.getFulfillmentChannel())){
                return false;
            }
        }
        if (null == dto.getReceiver()){
            return true;
        }
        return StringUtils.isBlank(dto.getReceiver().getName());
    }

    @Override
    public void handleRule(SoB2cEntity mainEntity) {
        //订单状态
        String billStatus = mainEntity.getBillStatus();

        String id = mainEntity.getId();
        //是否是平台仓订单 true 是
        Boolean isPlatformWarehouseOrder = mainEntity.hasPlatformWarehouseOrder();

        //付款状态
        String payStatus = mainEntity.getPayStatus();
        //已付款
        String paid = SoB2cPayStatusEnum.ENUM_PAID.getCode();

        //查询支付方式是否支持继续发货
        Boolean isFlag = soB2cCoreService.listPayMethodSetting(mainEntity);

        //自动匹配订单规则 待配貨和已付款 就要订单规则
        if (SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode().equalsIgnoreCase(billStatus)
                && (paid.equalsIgnoreCase(payStatus) || isFlag)
                && !mainEntity.getInvalidStatus()) {
            List<SoB2cDetailEntity> detailList = soB2cDetailService.listByMainId(id);
            Map<String, Object> map = soB2cService.handleMatchJson(id, detailList, new HashMap<>());
            if (isPlatformWarehouseOrder) {
                soB2cService.platformWarehouseOrderHandle(id, map);
            } else {
                //拉取订单正常处理
                soB2cService.pullOrderHandle(id, detailList, map);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SoB2cDTO.PullOrderResultDTO checkAndSaveAll(PlatformOrderDTO dto) {
        // 查询关联关系
        List<String> platformSkuList = dto.convertPlatformSkuList();

        // 速卖通同店铺存在相同SkuNo需要配合平台产ID/SPU查询
        List<String> platformSpuList = new LinkedList<>();
        if (PlatformDictEnum.ALI_EXPRESS.getCode().equalsIgnoreCase(dto.getPlatform())
                || PlatformDictEnum.MERCADOLIBRE.getCode().equalsIgnoreCase(dto.getPlatform())
                || PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode().equalsIgnoreCase(dto.getPlatform())
                || PlatformDictEnum.SHOPEE.getCode().equalsIgnoreCase(dto.getPlatform())
                || PlatformDictEnum.SHOPIFY.getCode().equalsIgnoreCase(dto.getPlatform())
                || PlatformDictEnum.TIK_TOK_FULLY.getCode().equalsIgnoreCase(dto.getPlatform())
                || PlatformDictEnum.TE_MU.getCode().equalsIgnoreCase(dto.getPlatform())
                || PlatformDictEnum.TIK_TOK.getCode().equalsIgnoreCase(dto.getPlatform())){
            platformSpuList = dto.convertPlatformSpuList();
        }
        Map<String, List<ListingInfoWithSkuMappingDTO>> listingInfoWithSkuMappingDTOMap = skuMappingService.mapListingByPlatformSkuNo(platformSkuList, platformSpuList, dto.getDictPlatform(), dto.getShopId(), dto.getPlatformOrderCreateTime(), null);

        // 查询当前店铺信息
        ShopInfoEntity shopInfo = shopInfoService.getById(dto.getShopId());
        if (null == shopInfo) {
            throw new ServiceException("未找到订单的店铺" + dto.getShopId());
        }

        // 查询国家信息
        List<String> countryIds;
        if (Objects.nonNull(dto.getReceiver())){
            countryIds = Stream.of(shopInfo.getDictCountryCode(), dto.getReceiver().getCountry()).distinct().collect(Collectors.toList());
        }else {
            countryIds = Stream.of(shopInfo.getDictCountryCode()).collect(Collectors.toList());
        }
        List<DictCountryEntity> countryList = sysDictFeign.listCountryByIdsOrAlpha3(countryIds);

        List<String> skuIds = listingInfoWithSkuMappingDTOMap.values().stream()
                .flatMap(List::stream)
                .map(ListingInfoWithSkuMappingDTO::getProductSkuId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<SkuInfoSimpleVO> skuList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(skuIds)) {
            skuList = plmTaskFeign.getSimpleSkuInfoByIds(skuIds);
        }
        //全托管转换平台状态
        if (PlatformDictEnum.TIK_TOK_FULLY.getCode().equalsIgnoreCase(dto.getDictPlatform())) {
            dto.setPlatformOrderStatus(FullyManagedPlatformStatusEnum.getErpCodeByCode(dto.getDictPlatform(),dto.getPlatformOrderStatus()));
        }
        // 主表更新或保存
        SoB2cDTO.PullOrderResultDTO resultDTO = soB2cService.saveOrUpdateEntity(dto, shopInfo);
        SoB2cEntity mainEntity = resultDTO.getSoB2cEntity();
        resultDTO.setShopWarehouseId(shopInfo.getWarehouseId());
        // 详情更新或保存
        List<SoB2cDetailEntity> detailList = soB2cDetailService.saveOrUpdateEntity(dto, mainEntity, listingInfoWithSkuMappingDTOMap, shopInfo, skuList);
        Boolean isWarehouseEmpty = detailList.stream().anyMatch(d -> StringUtils.isBlank(d.getWarehouseId()));
        resultDTO.setIsWarehouseEmpty(isWarehouseEmpty);
        if (CollectionUtils.isEmpty(detailList)){
            // 拆分后无平台来源明细不更新
            log.warn("[B2C订单消费] 平台订单【{}】：拆分后无平台来源明细不更新", dto.getPlatformCode());
            return resultDTO;
        }
        resultDTO.setWarehouseName(detailList.get(MathUtil.ZERO).getWarehouseName());
        //查询b2c error信息
        SoB2cErrorEntity soB2cError = soB2cErrorService.getByMainIdAndType(mainEntity.getId(), SoB2cErrorTypeEnum.ORDER_FETCH.getCode());
        resultDTO.setSoB2cError(soB2cError);
        // 毛重(捆绑商品按拆分后计算)
        BigDecimal allNetWeight = BigDecimal.ZERO;
        //长宽高计算
        BigDecimal maxLength = BigDecimal.ZERO;
        BigDecimal maxWidth = BigDecimal.ZERO;
        BigDecimal totalHeight = BigDecimal.ZERO;
        if (CollectionUtils.isNotEmpty(skuList)){
            //拆分明细
            List<SplitSkuDTO> splitSkuDTOS = soB2cService.splitBySoDetail(detailList, skuIds, mainEntity.getCode(), true);
            //根据sku进行计算
            List<String> keyList = new ArrayList<>();
            keyList.add(CalculateSizeEnum.LENGTH.getCode());
            keyList.add(CalculateSizeEnum.WIDTH.getCode());
            keyList.add(CalculateSizeEnum.HEIGHT.getCode());
            keyList.add(CalculateSizeEnum.GROSS_WEIGHT.getCode());
            List<DictBasicEntity> byKeyList = dictBasicService.getByKeyList(keyList);
            Map<String, String> collect = byKeyList.stream().collect(Collectors.toMap(DictBasicEntity::getType, DictBasicEntity::getValue));
            maxLength = SplitSkuDTO.calculateSplitSkuDTOLength(splitSkuDTOS,collect.get(CalculateSizeEnum.LENGTH.getCode()));
            maxWidth = SplitSkuDTO.calculateSplitSkuDTOWidth(splitSkuDTOS,collect.get(CalculateSizeEnum.WIDTH.getCode()));
            totalHeight = SplitSkuDTO.calculateSplitSkuDTOHeight(splitSkuDTOS,collect.get(CalculateSizeEnum.HEIGHT.getCode()));
            allNetWeight = SplitSkuDTO.calculateSplitSkuDTOGrossWeight(splitSkuDTOS, collect.get(CalculateSizeEnum.GROSS_WEIGHT.getCode()));
        }
        //物流信息更新保存
        SoB2cLogisticsEntity logisticsEntity = soB2cLogisticsService.saveOrUpdateEntity(dto, mainEntity, allNetWeight,maxLength,maxWidth,totalHeight);

        //财务信息更新保存
        soB2cFinanceService.saveOrUpdateEntity(dto, mainEntity, logisticsEntity, detailList);

        //扩展信息保存
        soB2cExtendService.saveOrUpdateEntity(dto, mainEntity);
        if(!PlatformDictEnum.TIK_TOK_FULLY.getCode().equals(dto.getDictPlatform())){
            //买家信息更新保存
            SoB2cReceiverEntity receiverEntity = soB2cReceiverService.saveOrUpdateEntity(dto, mainEntity, countryList,null == soB2cError);

            //客户信息
            CustomerB2cEntity customerB2cEntity = customerB2cService.saveOrUpdateEntity(dto, mainEntity, receiverEntity, shopInfo.getDictCountryCode(), countryList,null == soB2cError);

            customerB2cAddressService.saveOrUpdateEntity(dto, customerB2cEntity, receiverEntity,null == soB2cError);

            customerB2cContactService.saveOrUpdateEntity(dto, customerB2cEntity, receiverEntity,null == soB2cError);

            receiverEntity.setCustomerId(customerB2cEntity.getId());
            soB2cReceiverService.buildPartitionId(receiverEntity,shopInfo);
            if(PlatformDictEnum.TE_MU.getCode().equals(dto.getDictPlatform()) && StringUtils.isBlank(receiverEntity.getCountry()) && StringUtils.isNotBlank(shopInfo.getDictCountryCode())){
                receiverEntity.setCountry(shopInfo.getDictCountryCode());
            }
            soB2cReceiverService.saveOrUpdate(receiverEntity);
        }

//        if (!soB2cReceiverService.saveOrUpdate(receiverEntity)) {
//            throw new ServiceException("[SoB2cReceiverEntity] 保存失败");
//        }


        //如果是已支付的订单
//        if (SoB2cPayStatusEnum.ENUM_PAID.getCode().equals(mainEntity.getPayStatus()) ) {
//            //同步数帝云
//            List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listByMainId(mainEntity.getId());
//            syncSoB2cService.syncDataToSdy(mainEntity, soB2cDetailEntityList, SyncOperateEnum.OPERATE_UPDATE.getCode());
//        }
        // 已支付订单在出库时推送
        // 已取消订单推送?
        if (mainEntity.getIsCancel() && ApproveStatusEnum.APPROVE.equals(mainEntity.getApproveStatus())) {
            //同步数帝云
            syncSoB2cService.syncSdyCancelOrder(mainEntity, detailList, SyncOperateEnum.OPERATE_UPDATE.getCode());
        }
        //校验发票开票规则
        cfgRuleInvoiceService.invoiceCfgRule(mainEntity,detailList,new HashMap<>());
        //生成Nf-e发票
        cfgInvoiceSettingDetailService.generateNfeInvoice(mainEntity,InvoiceNodeEnum.AFTER_PULL.getCode());
        return resultDTO;
    }

    @Override
    public Boolean tiktokSplit(PlatformOrderDTO dto) {
        List<SoB2cEntity> soB2cEntityList = soB2cService.getByPlatformCodeList(Arrays.asList(dto.getPlatformCode()),dto.getDictPlatform(),dto.getShopId(),"");
        if(CollectionUtils.isEmpty(soB2cEntityList)){
            return true;
        }
        soB2cEntityList = soB2cEntityList.stream().filter(v->!v.getInvalidStatus()).collect(Collectors.toList());
        List<String> ids = soB2cEntityList.stream().map(SoB2cEntity::getId).collect(Collectors.toList());
        List<SoB2cDetailEntity> detailList = soB2cDetailService.listByMainIds(ids);
        //过滤掉包裹号为空的订单
        detailList = detailList.stream().filter(v->StringUtils.isNotBlank(v.getPlatformPackageId())).collect(Collectors.toList());
        List<String> filterSoIds = detailList.stream().map(SoB2cDetailEntity::getMainId).distinct().collect(Collectors.toList());
        soB2cEntityList = soB2cEntityList.stream().filter(v->filterSoIds.contains(v.getId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(soB2cEntityList)){
            return true;
        }
        List<String> dtoPackageIds = dto.getDetails().stream().map(PlatformOrderDetailDTO::getPlatformPackageId).distinct().collect(Collectors.toList());
        List<String> soPackageIds = detailList.stream().map(SoB2cDetailEntity::getPlatformPackageId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(dtoPackageIds) || CollectionUtils.isEmpty(soPackageIds)){
            return true;
        }
        if (dtoPackageIds.size() != soPackageIds.size() || !new HashSet<>(dtoPackageIds).containsAll(soPackageIds)){
            if(!soB2cEntityList.isEmpty() && dtoPackageIds.size() > 1){
                SoB2cEntity soB2cEntity = soB2cEntityList.get(0);
                if(SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode().equals(soB2cEntity.getBillStatus())){
                    soB2cErrorService.generateErrorOrder(soB2cEntity.getId(),SoB2cErrorTypeEnum.OTHER.getCode(),"tiktok平台拆单，ERP更新拆单信息失败:{订单已提交发货，无法拆单}","","","");
                    return false;
                }
                SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsService.getByMainId(soB2cEntity.getId());
                if(StringUtils.isNotBlank(soB2cLogisticsEntity.getCode())){
                    try {
                        BatchResultDTO batchResultDTO = soB2cLogisticsService.cancelLogistic(soB2cEntity.getId(),Arrays.asList(soB2cEntity),Arrays.asList(soB2cLogisticsEntity),true);
                        if(!batchResultDTO.getSuccess()){
                            soB2cErrorService.generateErrorOrder(soB2cEntity.getId(),SoB2cErrorTypeEnum.OTHER.getCode(),"平台拆单，ERP取消物流单失败，无法更新拆单信息:"+batchResultDTO.getMsg(),"","","");
                            return false;
                        }
                    }catch (Exception e){
                        soB2cErrorService.generateErrorOrder(soB2cEntity.getId(),SoB2cErrorTypeEnum.OTHER.getCode(),"平台拆单，ERP取消物流单失败，无法更新拆单信息:"+e.getMessage(),"","","");
                        throw new ServiceException(e.getMessage());
                    }
                }
                //erp已做了拆分，先取消拆单
                if(soB2cEntityList.size() > 1){
                    try {
                        //做取消拆分
                        BatchResultDTO batchResultDTO = soB2cSplitService.cancelSplit(soB2cEntityList.get(0).getId(), false);
                        if(!batchResultDTO.getSuccess()){
                            soB2cErrorService.generateErrorOrder(soB2cEntity.getId(),SoB2cErrorTypeEnum.OTHER.getCode(),"平台取消拆单-ERP取消拆单失败，"+batchResultDTO.getMsg(),"","","");
                            return false;
                        }
                    }catch (Exception e){
                        soB2cErrorService.generateErrorOrder(soB2cEntity.getId(),SoB2cErrorTypeEnum.OTHER.getCode(),"平台取消拆单-ERP取消拆单失败，"+e.getMessage(),"","","");
                        throw new ServiceException(e.getMessage());
                    }
                }
                //做拆分，根据package拆分
                SoB2cDTO.SplitSaveDTO splitSaveDTO = new SoB2cDTO.SplitSaveDTO();
                splitSaveDTO.setId(soB2cEntity.getId());
                splitSaveDTO.setIsSyncPlatform(false);
                Map<String,List<PlatformOrderDetailDTO>> packageIdMap = dto.getDetails().stream().collect(Collectors.groupingBy(PlatformOrderDetailDTO::getPlatformPackageId));
                List<SoB2cDTO.GroupSplitSaveDTO> groupList = new ArrayList<>();
                List<SoB2cDetailEntity> finalDetailList = detailList;
                packageIdMap.forEach((k, v) -> {
                    SoB2cDTO.GroupSplitSaveDTO groupSplitSaveDTO = new SoB2cDTO.GroupSplitSaveDTO();
                    List<SoB2cDTO.SplitDetailSaveDTO> splitDetailList = new ArrayList<>();
                    v.forEach(e -> {
                        SoB2cDTO.SplitDetailSaveDTO splitDetailSaveDTO = new SoB2cDTO.SplitDetailSaveDTO();
                        SoB2cDetailEntity soB2cDetailEntity = finalDetailList.stream().filter(d -> d.getPlatformSkuNo().equals(e.getPlatformSkuNo())).findFirst().orElse(null);
                        if(Objects.isNull(soB2cDetailEntity)){
                            return;
                        }
                        splitDetailSaveDTO.setQty(e.getQty());
                        splitDetailSaveDTO.setId(soB2cDetailEntity.getId());
                        splitDetailList.add(splitDetailSaveDTO);
                    });
                    groupSplitSaveDTO.setDetailList(splitDetailList);
                    groupList.add(groupSplitSaveDTO);
                });
                splitSaveDTO.setGroupList(groupList);
                SoB2cDTO.SplitSaveResultDTO splitSaveResultDTO;
                try {
                    //做取消拆分
                    splitSaveResultDTO = soB2cSplitService.splitSave(splitSaveDTO);
                }catch (Exception e){
                    soB2cErrorService.generateErrorOrder(soB2cEntity.getId(),SoB2cErrorTypeEnum.OTHER.getCode(),"平台拆单-ERP拆单失败，"+e.getMessage(),"","","");
                    throw new ServiceException(e.getMessage());
                }
                //更新拆分后的packageId
                List<String> splitSoIds = splitSaveResultDTO.getSoB2cIds();
                if(CollectionUtils.isNotEmpty(splitSoIds)){
                    List<SoB2cDetailEntity> splitDetailList = soB2cDetailService.listByMainIds(splitSoIds);
                    //key platformLineNumber val: platformPackageId
                    Map<String,String> linePackageMap = dto.getDetails().stream().collect(Collectors.toMap(PlatformOrderDetailDTO::getPlatformLineNumber,PlatformOrderDetailDTO::getPlatformPackageId,(v1,v2)->v1));
                    splitDetailList.forEach(v->{
                        v.setPlatformPackageId(linePackageMap.get(v.getPlatformLineNumber()));
                        v.setSourceDetailId(v.getPlatformSkuNo()+linePackageMap.get(v.getPlatformLineNumber()));
                    });
                    soB2cDetailService.updateBatchById(splitDetailList);
                }
                return false;
            }else if(soB2cEntityList.size() > 1 && dtoPackageIds.size()  == 1){
                //做取消拆分
                BatchResultDTO batchResultDTO;
                try {
                    //做取消拆分
                    batchResultDTO = soB2cSplitService.cancelSplit(soB2cEntityList.get(0).getId(), false);
                    if(!batchResultDTO.getSuccess()){
                        soB2cErrorService.generateErrorOrder(soB2cEntityList.get(0).getId(),SoB2cErrorTypeEnum.OTHER.getCode(),"平台取消拆单-ERP取消拆单失败，"+batchResultDTO.getMsg(),"","","");
                        return false;
                    }
                }catch (Exception e){
                    soB2cErrorService.generateErrorOrder(soB2cEntityList.get(0).getId(),SoB2cErrorTypeEnum.OTHER.getCode(),"平台取消拆单-ERP取消拆单失败，"+e.getMessage(),"","","");
                    throw new ServiceException(e.getMessage());
                }
                //更新拆分后的packageId
                String cancelSplitSoIds = batchResultDTO.getId();
                if(StringUtils.isNotBlank(cancelSplitSoIds)){
                    List<SoB2cDetailEntity> cancelSplitDetailList = soB2cDetailService.listByMainId(cancelSplitSoIds);
                    //key platformLineNumber val: platformPackageId
                    Map<String,String> linePackageMap = dto.getDetails().stream().collect(Collectors.toMap(PlatformOrderDetailDTO::getPlatformLineNumber,PlatformOrderDetailDTO::getPlatformPackageId,(v1,v2)->v1));
                    cancelSplitDetailList.forEach(v->{
                        v.setPlatformPackageId(linePackageMap.get(v.getPlatformLineNumber()));
                        v.setSourceDetailId(v.getPlatformSkuNo()+linePackageMap.get(v.getPlatformLineNumber()));
                    });

                    soB2cDetailService.updateBatchById(cancelSplitDetailList);
                }
                return false;
            }else{
                //更新包裹号
                for (SoB2cDetailEntity soB2cDetailEntity : detailList) {
                    soB2cDetailEntity.setPlatformPackageId(dtoPackageIds.get(0));
                    soB2cDetailEntity.setSourceDetailId(soB2cDetailEntity.getPlatformSkuNo()+dtoPackageIds.get(0));
                }
                soB2cDetailService.updateBatchById(detailList);
                return false;
            }
        }
        //如果是已经做了拆单，不允许更新订单
        return soB2cEntityList.size() <= 1;
    }

    @Override
    public void updateTikTokDetail(PlatformOrderDTO dto) {
        List<SoB2cEntity> soB2cEntityList = soB2cService.getByPlatformCodeList(Arrays.asList(dto.getPlatformCode()),dto.getDictPlatform(),dto.getShopId(),"");
        if(CollectionUtils.isEmpty(soB2cEntityList)){
            return ;
        }
        List<String> ids = soB2cEntityList.stream().map(SoB2cEntity::getId).collect(Collectors.toList());
        List<SoB2cDetailEntity> detailList = soB2cDetailService.listByMainIds(ids);
        detailList = detailList.stream().filter(v->StringUtils.isBlank(v.getSplitDetailId()) && StringUtils.isBlank(v.getPlatformPackageId()) && StringUtils.isNotBlank(v.getPlatformLineNumber())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(detailList)){
            return;
        }
        List<SoB2cDetailEntity> updateList = new ArrayList<>();
        List<PlatformOrderDetailDTO> platformOrderDetailDTOS = dto.getDetails();
        detailList.forEach(v->{
            PlatformOrderDetailDTO platformOrderDetailDTO = platformOrderDetailDTOS.stream().filter(e->e.getPlatformLineNumber().equals(v.getPlatformLineNumber())).findFirst().orElse(null);
            if(Objects.nonNull(platformOrderDetailDTO)){
                v.setPlatformPackageId(platformOrderDetailDTO.getPlatformPackageId());
                v.setSourceDetailId(v.getPlatformSkuNo()+platformOrderDetailDTO.getPlatformPackageId());
                updateList.add(v);
            }
        });
        if(CollectionUtils.isNotEmpty(updateList)){
            log.warn("[B2C订单消费] TIKTOK平台订单【{}】：更新包裹号", dto.getPlatformCode());
            soB2cDetailService.updateBatchById(updateList);
        }
    }

    /**
     * 自发货订单不存在地址
     */
    private boolean notPlatformOrderNotExistAddress(PlatformOrderDTO dto, String oldBillStatus) {
        if (PlatformDictEnum.AMAZON.getCode().equalsIgnoreCase(dto.getDictPlatform())) {
            return this.checkHasMfnOrderAndNoAddress(dto);
        }
        if (PlatformDictEnum.ALI_EXPRESS.getCode().equalsIgnoreCase(dto.getDictPlatform())) {
            return this.aliExpressNotPlatformOrderNotExistAddress(dto,oldBillStatus);
        }
        if (PlatformDictEnum.TE_MU.getCode().equalsIgnoreCase(dto.getDictPlatform())) {
            return this.temuPlatformOrderNotExistAddress(dto);
        }
        return false;
    }

    /**
     * 速卖通自发货订单未解密地址
     */
    private boolean aliExpressNotPlatformOrderNotExistAddress(PlatformOrderDTO dto, String oldBillStatus) {
        if (StrUtil.isNotBlank(dto.getLabelJson())) {
            SoB2cDTO.LabelDTO labelJsonDTO = JSONUtil.toBean(dto.getLabelJson(), SoB2cDTO.LabelDTO.class);
            Boolean isAliexpressPlatformWarehouseOrder = labelJsonDTO.getIsPlatformWarehouseOrder();
            if (isAliexpressPlatformWarehouseOrder){
                return false;
            }
            if (null == dto.getReceiver()){
                return false;
            }
            //已发货 或者 已取消
            if(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(oldBillStatus) || (Objects.nonNull(dto.getIsCancel()) && dto.getIsCancel())){
                return false;
            }
            return StringUtils.isNotBlank(dto.getReceiver().getFullAddress()) && dto.getReceiver().getFullAddress().contains("***");
        }
        return false;
    }


    /**
     * 速卖通自发货订单未解密地址
     */
    private boolean temuPlatformOrderNotExistAddress(PlatformOrderDTO dto) {
        if (StrUtil.isNotBlank(dto.getLabelJson())) {
            SoB2cDTO.LabelDTO labelJsonDTO = JSONUtil.toBean(dto.getLabelJson(), SoB2cDTO.LabelDTO.class);
            Boolean isTemuPlatformWarehouseOrder = labelJsonDTO.getIsPlatformWarehouseOrder();
            if (isTemuPlatformWarehouseOrder){
                return false;
            }
            return null != dto.getReceiver().getIsUpdateError() && dto.getReceiver().getIsUpdateError();
        }
        return false;
    }

}
