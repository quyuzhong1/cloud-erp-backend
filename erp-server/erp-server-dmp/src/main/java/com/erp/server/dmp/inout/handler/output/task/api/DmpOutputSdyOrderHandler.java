package com.erp.server.dmp.inout.handler.output.task.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.ShudiyunB2cOrderDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.business.wrapper.QueryParam;
import com.common.business.wrapper.QueryTypeEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.OrderSubTypeEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.dmp.inout.dto.request.DmpOutputHotfixCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.factory.DmpOutputCreateFactory;
import com.erp.server.dmp.inout.handler.output.task.DmpOutputTaskHandler;
import com.erp.server.dmp.push.consumer.sdy.SdyDeliveryOrderConsumer;
import com.erp.server.dmp.service.ThirdMappingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;


/**
 * 数帝云线上订单映射推送
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpOutputSdyOrderHandler extends DmpOutputTaskHandler {
    @Resource
    private ThirdMappingService thirdMappingService;
    @Resource
    private SdyDeliveryOrderConsumer sdyDeliveryOrderConsumer;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private DmpOutputCreateFactory dmpOutputCreateFactory;

    @Override
    protected List<DmpOutputTaskRecordEntity> outputData(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpSoInfoEntity> DmpSoInfoEntityMap = new HashMap<>();
        Map<String, List<DmpSoDetailEntity>> DmpSoDetailEntityMap = new HashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_so_info".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoInfoEntity dmpSoInfoEntity = (DmpSoInfoEntity) v;
                        DmpSoInfoEntityMap.put(dmpSoInfoEntity.getId(), dmpSoInfoEntity);
                    }
                } else if ("dmp_so_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoDetailEntity DmpSoDetailEntity = (DmpSoDetailEntity) v;
                        String mainId = DmpSoDetailEntity.getMainId();
                        List<DmpSoDetailEntity> list = DmpSoDetailEntityMap.get(mainId);
                        if (CollUtil.isEmpty(list)) {
                            list = new ArrayList<>();
                        }
                        list.add(DmpSoDetailEntity);
                        DmpSoDetailEntityMap.put(mainId, list);
                    }
                }
            }
        }

        Map<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMaps = dmpRequest.getChangeConvertInputDmpBaseEntityListMaps();
        Set<String> changeIds = new HashSet<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMap : changeConvertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = changeConvertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = changeConvertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_so_info".equals(storageName)) {
                    for (BaseEntity v : value) {
                        changeIds.add(v.getId());
                    }
                } else if ("dmp_so_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoDetailEntity DmpSoDetailEntity = (DmpSoDetailEntity) v;
                        changeIds.add(DmpSoDetailEntity.getMainId());
                    }
                }
            }
        }

        Map<String, String> map = new HashMap<>();
        for(String changId : changeIds) {
            List<ShudiyunB2cOrderDTO> sdyDtoList = this.convert(DmpSoInfoEntityMap.get(changId), DmpSoDetailEntityMap.get(changId));
            if(CollUtil.isNotEmpty(sdyDtoList)) {
                map.put(changId, JSON.toJSONString(sdyDtoList));
            }
        }
        List<DmpOutputTaskRecordEntity> dmpOutputTaskRecordEntityList = new ArrayList<>();
        if(!map.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            int i = 0;
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String dataId = entry.getKey();
                List<ShudiyunB2cOrderDTO> shudiyunB2cOrderDTOList = JSON.parseArray(entry.getValue(), ShudiyunB2cOrderDTO.class);
                for (ShudiyunB2cOrderDTO shudiyunB2cOrderDTO : shudiyunB2cOrderDTOList) {
                    DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity = new DmpOutputTaskRecordEntity();
                    String id = identifierGenerator.nextId(dmpOutputTaskRecordEntity).toString();
                    dmpOutputTaskRecordEntity.setId(id);
                    dmpOutputTaskRecordEntity.setMainId(dmpRequest.getOutputTaskId());
                    dmpOutputTaskRecordEntity.setDataId(shudiyunB2cOrderDTO.getBiz_uni_key().substring(dataId.length()));
                    dmpOutputTaskRecordEntity.setSourceCode(shudiyunB2cOrderDTOList.get(0).getBiz_no());
                    dmpOutputTaskRecordEntity.setRequestData(JSON.toJSONString(shudiyunB2cOrderDTO));
                    dmpOutputTaskRecordEntity.setStatus(DmpOutputTaskRecordStatusEnum.INIT.getCode());
                    LocalDateTime insertTime = now.plus(i, ChronoUnit.MILLIS);
                    dmpOutputTaskRecordEntity.setCreateTime(insertTime);
                    dmpOutputTaskRecordEntity.setUpdateTime(insertTime);
                    dmpOutputTaskRecordEntityList.add(dmpOutputTaskRecordEntity);
                    i = i + 1;
                }
            }
        }

        return dmpOutputTaskRecordEntityList;
    }

    @Override
    protected void pushData(DmpCfgOutputEntity dmpCfgOutputEntity, DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity) {
        String id = dmpOutputTaskRecordEntity.getId();
        String status = "";
        String requestData = dmpOutputTaskRecordEntity.getRequestData();
        ApiResult handle = sdyDeliveryOrderConsumer.handle(requestData);
        if (200 == handle.getCode()) {
            status = DmpOutputTaskRecordStatusEnum.FINISH.getCode();
        } else {
            status = DmpOutputTaskRecordStatusEnum.ERROR.getCode();
        }

        dmpOutputUtils.updateStatus(id, status, String.valueOf(handle.getData()) , handle.getMsg());

        //创建旺店通原始订单任务
        ShudiyunB2cOrderDTO shudiyunB2cOrderDTO = JSON.parseObject(requestData, ShudiyunB2cOrderDTO.class);
        DmpOutputHotfixCreateRequest request = new DmpOutputHotfixCreateRequest();
        request.setCfgOutputId("1861317267527064372");
        List<QueryParam> queryParams = new ArrayList<>();
        QueryParam queryParam = new QueryParam();
        queryParam.setType(QueryTypeEnum.EQ);
        queryParam.setName("third_code");
        queryParam.setValue(shudiyunB2cOrderDTO.getBiz_no());
        queryParams.add(queryParam);
        request.setQueryParams(queryParams);
        dmpOutputCreateFactory.doHotfixOutputTask(request);
    }

    /**
     * 解析订单数据
     **/
    public List<ShudiyunB2cOrderDTO> convert(DmpSoInfoEntity dmpSoInfoEntity, List<DmpSoDetailEntity> dmpSoDetailEntityList) {
        DateTimeFormatter localDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter localDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        //优惠额
        BigDecimal totalDiscount = dmpSoInfoEntity.getTotalDiscount();


        BigDecimal shareTotalDiscount = BigDecimal.ZERO;

        //如果是亚马逊的优惠额在明细里
        if (PlatformDictEnum.AMAZON.getCode().equals(dmpSoInfoEntity.getSourcePlatform())) {
            totalDiscount = dmpSoDetailEntityList.stream().filter(req -> req.getDiscount() != null).map(req -> req.getDiscount()).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        }

        //数帝云数据结构
        List<ShudiyunB2cOrderDTO> shudiyunB2cOrderDTOList = new ArrayList<>();

        //订单详情
        for (int i = 0; i < dmpSoDetailEntityList.size(); i++) {
            DmpSoDetailEntity dmpSoDetailEntity = dmpSoDetailEntityList.get(i);

            ShudiyunB2cOrderDTO shudiyunB2cOrderDTO = new ShudiyunB2cOrderDTO();
            shudiyunB2cOrderDTO.setBiz_uni_key(dmpSoInfoEntity.getId() + dmpSoDetailEntity.getId());

            shudiyunB2cOrderDTO.setBiz_no(dmpSoInfoEntity.getThirdCode());
            if (dmpSoInfoEntity.getPayTime() != null) {
                shudiyunB2cOrderDTO.setBiz_time(localDateTime.format(dmpSoInfoEntity.getPayTime()));
            } else {
                shudiyunB2cOrderDTO.setBiz_time(localDateTime.format(dmpSoInfoEntity.getPlatformCreateTime()));
            }

            //如果是旺店通中台表的订单属于配货单，其他的都是线上原始订单
            if (PlatformDictEnum.WDT.getCode().equalsIgnoreCase(dmpSoInfoEntity.getSourceSystem())) {
                //配货单
                shudiyunB2cOrderDTO.setTransaction_type("配货单");
                shudiyunB2cOrderDTO.setBiz_status(wdtStatusHandler(dmpSoInfoEntity.getOrderStatus()));
            } else {
                //线上订单
                shudiyunB2cOrderDTO.setTransaction_type("线上订单");
                if (CharSequenceUtil.isBlank(ApproveStatusEnum.getName(dmpSoInfoEntity.getOrderStatus()))) {
                    shudiyunB2cOrderDTO.setBiz_status("已审核");
                } else {
                    shudiyunB2cOrderDTO.setBiz_status(ApproveStatusEnum.getName(dmpSoInfoEntity.getOrderStatus()));
                }
            }
            shudiyunB2cOrderDTO.setStatus("已创建");

            shudiyunB2cOrderDTO.setTransaction_sub_type(OrderSubTypeEnum.ONLINE_ORDER.getName());

            shudiyunB2cOrderDTO.setTotal_goods_transaction_amount(dmpSoInfoEntity.getAllAmount());
            //总优惠金额
            shudiyunB2cOrderDTO.setDiscount_deduction_amount(dmpSoInfoEntity.getTotalDiscount());

            Integer totalQty = dmpSoDetailEntityList.stream().mapToInt(DmpSoDetailEntity::getQty).sum();
            shudiyunB2cOrderDTO.setTotal_goods_quantity(totalQty);
            shudiyunB2cOrderDTO.setOrder_quantity_to_be_shipped(totalQty);

            shudiyunB2cOrderDTO.setBuyer_actual_payment(dmpSoInfoEntity.getPayAmount());
            shudiyunB2cOrderDTO.setTotal_freight(dmpSoInfoEntity.getShippingAmount());
            if (PlatformDictEnum.WDT.getCode().equals(dmpSoInfoEntity.getSourceSystem())) {
                //查询旺店通对应系统店铺
                List<ThirdMappingEntity> shop = thirdMappingService.lambdaQuery()
                        .eq(ThirdMappingEntity::getType, ThirdSysTypeEnum.SHOP.getCode())
                        .eq(ThirdMappingEntity::getThirdSysType, PlatformDictEnum.WDT.getCode())
                        .eq(ThirdMappingEntity::getThirdCode, dmpSoInfoEntity.getShopId())
                        .list();
                if (CollectionUtils.isNotEmpty(shop)) {
                    ShopInfoEntity shopInfo = FeignQuery.getById(ShopInfoEntity.class, shop.get(0).getSysId());
                    shudiyunB2cOrderDTO.setShop_no(shopInfo.getId());
                    shudiyunB2cOrderDTO.setShop_name(shopInfo.getName());

                    CustomerInfoEntity customerInfo = FeignQuery.getById(CustomerInfoEntity.class, shopInfo.getCustomerId());

                    if (ObjectUtil.isNotEmpty(customerInfo)) {
                        //组织编码
                        List<BaseIdDTO.CodeDTO> companyEntities = sysUserFeign.getAccountingCompanyList(Arrays.asList(customerInfo.getFinancialOrganization(), shopInfo.getSalesOrgId()));
                        //销售组织
                        BaseIdDTO.CodeDTO salesOrg = companyEntities.stream().filter(req -> req.getId().equals(shopInfo.getSalesOrgId())).findFirst().orElse(null);
                        shudiyunB2cOrderDTO.setSales_company_code(salesOrg.getCode());

                        BaseIdDTO.CodeDTO sysAccountingCompanyEntity = companyEntities.stream().filter(req -> req.getId().equals(customerInfo.getFinancialOrganization())).findFirst().orElse(null);
                        if (ObjectUtil.isNotEmpty(sysAccountingCompanyEntity)) {
                            shudiyunB2cOrderDTO.setReceiving_company_code(sysAccountingCompanyEntity.getCode());
                            shudiyunB2cOrderDTO.setOrganization_code(sysAccountingCompanyEntity.getCode());
                            shudiyunB2cOrderDTO.setOrganization_name(sysAccountingCompanyEntity.getName());
                        }
                    }

                    DictCurrencyEntity dictCurrencyEntity = FeignQuery.getById(DictCurrencyEntity.class, shopInfo.getTradeCurrency());
                    if (ObjectUtil.isNotEmpty(dictCurrencyEntity)) {
                        shudiyunB2cOrderDTO.setTransaction_currency(dictCurrencyEntity.getName());
                    }
                    shudiyunB2cOrderDTO.setTransaction_currency_code(shopInfo.getTradeCurrency());
                    shudiyunB2cOrderDTO.setSettlement_currency_code(shopInfo.getSettlementCurrency());
                    shudiyunB2cOrderDTO.setPlatform_id(shopInfo.getDictPlatform());
                    shudiyunB2cOrderDTO.setPlatform_name(PlatformDictEnum.getNameByCode(shopInfo.getDictPlatform()));
                    shudiyunB2cOrderDTO.setSku_code(dmpSoDetailEntity.getSkuNo());
                    shudiyunB2cOrderDTO.setSku_name(dmpSoDetailEntity.getSkuName());
                    shudiyunB2cOrderDTO.setSpec_no(dmpSoDetailEntity.getPlatformSpuNo());
                    shudiyunB2cOrderDTO.setSpec_name(dmpSoDetailEntity.getSpecifics());
                }

                shudiyunB2cOrderDTO.setGoods_status(wdtItemStatus(dmpSoInfoEntity.getOrderStatus()));

                //取消金额、数量
                if (dmpSoDetailEntity.getRefundNum().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal canceledAmount = dmpSoDetailEntityList.stream().map(req -> req.getAfterAmount().divide(MathUtil.valueOf(req.getQty()), 4, RoundingMode.DOWN).multiply(dmpSoDetailEntity.getRefundNum())).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                    shudiyunB2cOrderDTO.setTotal_canceled_goods_amount(canceledAmount);
                    // 取消商品数量（合计）
                    shudiyunB2cOrderDTO.setTotal_canceled_goods_quantity(dmpSoDetailEntity.getRefundNum().intValue());
                } else {
                    shudiyunB2cOrderDTO.setTotal_canceled_goods_amount(BigDecimal.ZERO);
                    shudiyunB2cOrderDTO.setTotal_canceled_goods_quantity(0);
                }


            } else {
                String shopId = "";
                if (CharSequenceUtil.isNotBlank(dmpSoInfoEntity.getNextLevelId())) {
                    shopId = dmpSoInfoEntity.getNextLevelId();
                } else {
                    shopId = dmpSoInfoEntity.getShopId();
                }
                ShopInfoEntity shopInfo = FeignQuery.getById(ShopInfoEntity.class, shopId);
                if (ObjectUtil.isEmpty(shopInfo)) {
                    throw new ServiceException(ApiError.ERROR_SDY_NOT_FOUND_SHOP, shopId);
                }
                //组织信息
                List<BaseIdDTO.CodeDTO> companyEntities = new ArrayList<>();
                CustomerInfoEntity customerInfo = FeignQuery.getById(CustomerInfoEntity.class, shopInfo.getCustomerId());
                if (ObjectUtil.isNotEmpty(customerInfo)) {
                    //收款组织编码
                    companyEntities = sysUserFeign.getAccountingCompanyList(Arrays.asList(customerInfo.getFinancialOrganization()));
                    BaseIdDTO.CodeDTO sysAccountingCompanyEntity = companyEntities.stream().filter(req -> req.getId().equals(customerInfo.getFinancialOrganization())).findFirst().orElse(null);
                    if (ObjectUtil.isNotEmpty(sysAccountingCompanyEntity)) {
                        shudiyunB2cOrderDTO.setReceiving_company_code(sysAccountingCompanyEntity.getCode());
                        shudiyunB2cOrderDTO.setOrganization_code(sysAccountingCompanyEntity.getCode());
                        shudiyunB2cOrderDTO.setOrganization_name(sysAccountingCompanyEntity.getName());
                    }
                }

                //销售组织
                companyEntities = sysUserFeign.getAccountingCompanyList(Arrays.asList(shopInfo.getSalesOrgId()));
                String salesOrgCode = companyEntities.stream().filter(req -> req.getId().equals(shopInfo.getSalesOrgId())).map(req -> req.getCode()).findFirst().orElse("");
                shudiyunB2cOrderDTO.setSales_company_code(salesOrgCode);

                shudiyunB2cOrderDTO.setShop_no(shopInfo.getId());
                shudiyunB2cOrderDTO.setShop_name(shopInfo.getName());
                DictCurrencyEntity dictCurrencyEntity = FeignQuery.getById(DictCurrencyEntity.class, shopInfo.getTradeCurrency());
                if (ObjectUtil.isNotEmpty(dictCurrencyEntity)) {
                    shudiyunB2cOrderDTO.setTransaction_currency(dictCurrencyEntity.getName());
                }
                shudiyunB2cOrderDTO.setTransaction_currency_code(shopInfo.getTradeCurrency());
                shudiyunB2cOrderDTO.setSettlement_currency_code(shopInfo.getSettlementCurrency());
                shudiyunB2cOrderDTO.setPlatform_id(dmpSoInfoEntity.getSourceSystem());
                shudiyunB2cOrderDTO.setPlatform_name(PlatformDictEnum.getNameByCode(dmpSoInfoEntity.getSourcePlatform()));
                shudiyunB2cOrderDTO.setSubplatform_no(shopInfo.getDictPlatform());
                shudiyunB2cOrderDTO.setSubplatform_name(PlatformDictEnum.getNameByCode(shopInfo.getDictPlatform()));

                shudiyunB2cOrderDTO.setSku_code("");
                shudiyunB2cOrderDTO.setSku_name("");
                shudiyunB2cOrderDTO.setSpec_no(dmpSoDetailEntity.getPlatformSpuNo());
                shudiyunB2cOrderDTO.setSpec_name(dmpSoDetailEntity.getSpecifics());

                shudiyunB2cOrderDTO.setGoods_status("未发货");
                // 商品状态
                if (SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(dmpSoInfoEntity.getDeliveryStatus()) || SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(dmpSoInfoEntity.getOrderStatus())) {
                    shudiyunB2cOrderDTO.setGoods_status("已发货");
                }

                if (dmpSoInfoEntity.getIsCancel() && dmpSoInfoEntity.getIsCancel() != null) {
                    shudiyunB2cOrderDTO.setGoods_status("已取消");
                }

                //取消金额、数量
                if (dmpSoInfoEntity.getSourceSystem().equals(PlatformDictEnum.ALI_EXPRESS.getCode())
                        || dmpSoInfoEntity.getSourceSystem().equals(PlatformDictEnum.SHOPEE.getCode())
                ) {
                    shudiyunB2cOrderDTO.setTotal_canceled_goods_amount(dmpSoInfoEntity.getTotalCancelGoodsAmount());
                } else {
                    if (dmpSoInfoEntity.getIsCancel()) {
                        shudiyunB2cOrderDTO.setTotal_canceled_goods_amount(dmpSoInfoEntity.getAllAmount());

                        // 取消商品数量（合计）
                        shudiyunB2cOrderDTO.setTotal_canceled_goods_quantity(totalQty);
                    }
                }
            }


            shudiyunB2cOrderDTO.setRoot_node_no(dmpSoInfoEntity.getThirdCode());

            if (dmpSoInfoEntity.getPayTime() != null) {
                shudiyunB2cOrderDTO.setRoot_node_create_time(localDateTime.format(dmpSoInfoEntity.getPayTime()));
            } else {
                shudiyunB2cOrderDTO.setRoot_node_create_time(localDateTime.format(dmpSoInfoEntity.getPlatformCreateTime()));
            }
            shudiyunB2cOrderDTO.setRoot_node_modify_time(localDateTime.format(dmpSoInfoEntity.getPlatformUpdateTime()));
            shudiyunB2cOrderDTO.setGoods_no(dmpSoDetailEntity.getPlatformSku());
            shudiyunB2cOrderDTO.setGoods_name(dmpSoDetailEntity.getSkuName());

            if (dmpSoDetailEntity.getIsGift() && dmpSoDetailEntity.getIsGift() != null) {
                shudiyunB2cOrderDTO.setIs_gift(1);
            } else {
                shudiyunB2cOrderDTO.setIs_gift(0);
            }

            shudiyunB2cOrderDTO.setRemark(dmpSoDetailEntity.getItemRemark());

            shudiyunB2cOrderDTO.setGoods_status("未发货");
            // 商品状态
            if (SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(dmpSoInfoEntity.getDeliveryStatus()) || SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(dmpSoInfoEntity.getOrderStatus())) {
                shudiyunB2cOrderDTO.setGoods_status("已发货");
            }

            if (dmpSoInfoEntity.getIsCancel() && dmpSoInfoEntity.getIsCancel() != null) {
                shudiyunB2cOrderDTO.setGoods_status("已取消");
            }

            shudiyunB2cOrderDTO.setGoods_transaction_quantity(dmpSoDetailEntity.getQty());
            shudiyunB2cOrderDTO.setUnit("PCS");

            shudiyunB2cOrderDTO.setGoods_transaction_amount(dmpSoDetailEntity.getAfterAmount());
            if (dmpSoDetailEntity.getAfterAmount().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal shareDiscount = BigDecimal.ZERO;
                //获得售价占比分摊的商品优惠额
                if (dmpSoInfoEntity.getAllAmount().compareTo(BigDecimal.ZERO) > 0) {
                    shareDiscount = dmpSoDetailEntity.getAfterAmount().divide(dmpSoInfoEntity.getAllAmount(), 4, RoundingMode.DOWN).multiply(totalDiscount);
                }
                //计算为真实售价(原始币别)-商品分摊优惠/订单数量
                if (dmpSoDetailEntityList.size() == i-1) {
                    shudiyunB2cOrderDTO.setPrice(dmpSoDetailEntity.getAfterAmount().subtract((totalDiscount.subtract(shareTotalDiscount))).divide(MathUtil.valueOf(dmpSoDetailEntity.getQty()), 4, RoundingMode.DOWN));
                    shudiyunB2cOrderDTO.setGoods_transaction_amount(dmpSoDetailEntity.getAfterAmount().subtract((totalDiscount.subtract(shareTotalDiscount))));
                } else {
                    shudiyunB2cOrderDTO.setPrice(dmpSoDetailEntity.getAfterAmount().subtract(shareDiscount).divide(MathUtil.valueOf(dmpSoDetailEntity.getQty()), 4, RoundingMode.DOWN));
                    shudiyunB2cOrderDTO.setGoods_transaction_amount(dmpSoDetailEntity.getAfterAmount().subtract(shareDiscount));
                }
                shareTotalDiscount = shareTotalDiscount.add(shareDiscount);
            }
            shudiyunB2cOrderDTO.setPost_amount(dmpSoInfoEntity.getShippingAmount());
            shudiyunB2cOrderDTO.setMsku_code(dmpSoDetailEntity.getPlatformSku());
            if (CharSequenceUtil.isBlank(dmpSoDetailEntity.getSkuName())) {
                shudiyunB2cOrderDTO.setMsku_name(dmpSoDetailEntity.getSkuNo());
            } else {
                shudiyunB2cOrderDTO.setMsku_name(dmpSoDetailEntity.getSkuName());
            }

            shudiyunB2cOrderDTO.setSource_system("SDC");
            shudiyunB2cOrderDTO.setRoot_node_no_initial(dmpSoInfoEntity.getThirdCode());

            shudiyunB2cOrderDTOList.add(shudiyunB2cOrderDTO);

        }
        return shudiyunB2cOrderDTOList;
    }

    private String wdtItemStatus(String status) {
        if ("95".equals(status)) {
            return "已发货";
        } else if ("96".equals(status)) {
            return "成本确认（待录入计划成本，订单结算时有货品无计划成本）";
        } else if ("101".equals(status)) {
            return "已发货";
        } else if ("5".equals(status)) {
            return "已取消";
        } else {
            return "未发货";
        }
    }

    private String wdtStatusHandler(String status) {
        if ("4".equals(status)) {
            return "线下退款";
        } else if ("5".equals(status)) {
            return "已取消";
        } else if ("6".equals(status)) {
            return "待转预订单(待审核)";
        } else if ("7".equals(status)) {
            return "待转已完成";
        } else if ("10".equals(status)) {
            return "未付款";
        } else if ("12".equals(status)) {
            return "待尾款";
        } else if ("15".equals(status)) {
            return "等未付";
        } else if ("16".equals(status)) {
            return "延时审核";
        } else if ("19".equals(status)) {
            return "预订单前处理";
        } else if ("20".equals(status)) {
            return "审核前处理";
        } else if ("21".equals(status)) {
            return "自流转待发货";
        } else if ("23".equals(status)) {
            return "异常订单";
        } else if ("24".equals(status)) {
            return "换货预订单";
        } else if ("25".equals(status)) {
            return "待处理预订单";
        } else if ("27".equals(status)) {
            return "待分配预订单";
        } else if ("30".equals(status)) {
            return "待客审";
        } else if ("35".equals(status)) {
            return "待财审";
        } else if ("40".equals(status)) {
            return "审核中";
        } else if ("55".equals(status)) {
            return "已审核";
        } else if ("95".equals(status)) {
            return "已发货";
        } else if ("96".equals(status)) {
            return "成本确认（待录入计划成本，订单结算时有货品无计划成本）";
        } else if ("101".equals(status)) {
            return "已过账";
        } else {
            return "已完成";
        }
    }


}
