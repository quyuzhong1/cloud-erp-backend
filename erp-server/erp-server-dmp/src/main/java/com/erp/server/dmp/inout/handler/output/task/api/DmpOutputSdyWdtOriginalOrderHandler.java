package com.erp.server.dmp.inout.handler.output.task.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.ShudiyunB2cOrderDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.wrapper.FeignQuery;
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
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.output.task.DmpOutputTaskHandler;
import com.erp.server.dmp.push.consumer.sdy.SdyDeliveryOrderConsumer;
import com.erp.server.dmp.service.DmpSoDetailService;
import com.erp.server.dmp.service.ThirdMappingService;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 旺店通原始订单推送数帝云
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpOutputSdyWdtOriginalOrderHandler extends DmpOutputTaskHandler {
    @Resource
    private ThirdMappingService thirdMappingService;
    @Resource
    private SdyDeliveryOrderConsumer sdyDeliveryOrderConsumer;
    @Resource
    private DmpSoDetailService dmpSoDetailService;
    @Resource
    private SysUserFeign sysUserFeign;


    @Override
    protected List<DmpOutputTaskRecordEntity> outputData(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpSoOriginalInfoEntity> dmpSoOriginalInfoEntityMap = new HashMap<>();
        Map<String, List<DmpSoOriginalDetailEntity>> dmpSoOriginalDetailEntityMap = new HashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_so_original_info".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoOriginalInfoEntity dmpSoOriginalInfoEntity = (DmpSoOriginalInfoEntity) v;
                        dmpSoOriginalInfoEntityMap.put(dmpSoOriginalInfoEntity.getId(), dmpSoOriginalInfoEntity);
                    }
                } else if ("dmp_so_original_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoOriginalDetailEntity dmpSoOriginalDetailEntity = (DmpSoOriginalDetailEntity) v;
                        String mainId = dmpSoOriginalDetailEntity.getMainId();
                        List<DmpSoOriginalDetailEntity> list = dmpSoOriginalDetailEntityMap.get(mainId);
                        if (CollUtil.isEmpty(list)) {
                            list = new ArrayList<>();
                        }
                        list.add(dmpSoOriginalDetailEntity);
                        dmpSoOriginalDetailEntityMap.put(mainId, list);
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
                if ("dmp_so_original_info".equals(storageName)) {
                    for (BaseEntity v : value) {
                        changeIds.add(v.getId());
                    }
                } else if ("dmp_so_original_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoDetailEntity DmpSoDetailEntity = (DmpSoDetailEntity) v;
                        changeIds.add(DmpSoDetailEntity.getMainId());
                    }
                }
            }
        }

        Map<String, String> map = new HashMap<>();
        for(String changId : changeIds) {
            List<ShudiyunB2cOrderDTO> sdyDtoList = this.convert(dmpSoOriginalInfoEntityMap.get(changId), dmpSoOriginalDetailEntityMap.get(changId));
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
        if ("200".equals(handle.getCode())) {
            status = DmpOutputTaskRecordStatusEnum.FINISH.getCode();
        } else {
            status = DmpOutputTaskRecordStatusEnum.ERROR.getCode();
        }

        dmpOutputUtils.updateStatus(id, status, String.valueOf(handle.getData()) , handle.getMsg());
    }

    /**
     * 解析订单数据
     **/
    public List<ShudiyunB2cOrderDTO> convert(DmpSoOriginalInfoEntity dmpSoInfoEntity, List<DmpSoOriginalDetailEntity> dmpSoDetailEntityList) {
        DateTimeFormatter localDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter localDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        //数帝云数据结构
        List<ShudiyunB2cOrderDTO> shudiyunB2cOrderDTOList = new ArrayList<>();

        List<String> detailIds = dmpSoDetailEntityList.stream().map(req -> req.getThirdDetailId()).distinct().collect(Collectors.toList());
        List<DmpSoDetailEntity> wdtSoDetailEntities = dmpSoDetailService.lambdaQuery().in(DmpSoDetailEntity::getThirdDetailId, detailIds).list();



        //总售价 = 明细的单价 * 数量 汇总
        BigDecimal allAmount = dmpSoDetailEntityList.stream().map(req -> req.getPrice().multiply(MathUtil.valueOf(req.getNum()))).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
/*
        //订单详情
        for (int i = 0; i < dmpSoDetailEntityList.size(); i++) {
            DmpSoOriginalDetailEntity dmpSoDetailEntity = dmpSoDetailEntityList.get(i);
            DmpSoDetailEntity wdtDetailEntity = wdtSoDetailEntities.stream().filter(req -> req.getThirdDetailId().equalsIgnoreCase(dmpSoDetailEntity.getThirdDetailId())).findFirst().orElse(null);

            ShudiyunB2cOrderDTO shudiyunB2cOrderDTO = new ShudiyunB2cOrderDTO();
            shudiyunB2cOrderDTO.setBiz_uni_key(dmpSoInfoEntity.getId() + dmpSoDetailEntity.getId());
            shudiyunB2cOrderDTO.setBiz_no(dmpSoInfoEntity.getPlatformCode());
            if (dmpSoInfoEntity.getPayTime() != null) {
                shudiyunB2cOrderDTO.setRoot_node_create_time(localDateTime.format(dmpSoInfoEntity.getPayTime()));
            } else {
                return Collections.emptyList();
            }

            //线上原始订单
            shudiyunB2cOrderDTO.setTransaction_type("线上订单");

            shudiyunB2cOrderDTO.setTransaction_sub_type(OrderSubTypeEnum.ONLINE_ORDER.getCode());
            shudiyunB2cOrderDTO.setBiz_status("已审核");


            shudiyunB2cOrderDTO.setTotal_goods_transaction_amount(allAmount);
            //总优惠金额
            shudiyunB2cOrderDTO.setDiscount_deduction_amount(allAmount);

            BigDecimal totalQty = dmpSoDetailEntityList.stream().map(DmpSoOriginalDetailEntity::getNum).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            shudiyunB2cOrderDTO.setTotal_goods_quantity(totalQty.intValue());
            shudiyunB2cOrderDTO.setOrder_quantity_to_be_shipped(totalQty.intValue());

            //取消金额、数量
            shudiyunB2cOrderDTO.setTotal_canceled_goods_amount(dmpSoInfoEntity.getRefundAmount());

            // 取消商品数量（合计）
            if (ObjectUtil.isNotEmpty(wdtDetailEntity)) {
                shudiyunB2cOrderDTO.setTotal_canceled_goods_quantity(wdtDetailEntity.getRefundNum().intValue());
            } else {
                shudiyunB2cOrderDTO.setTotal_canceled_goods_quantity(0);
            }
            shudiyunB2cOrderDTO.setBuyer_actual_payment(dmpSoInfoEntity.getPaid());

            // todo 无运费
            shudiyunB2cOrderDTO.setTotal_freight(BigDecimal.ZERO);


                //查询旺店通对应系统店铺
                List<ThirdMappingEntity> shop = thirdMappingService.lambdaQuery()
                        .eq(ThirdMappingEntity::getType, ThirdSysTypeEnum.SHOP.getCode())
                        .eq(ThirdMappingEntity::getThirdSysType, PlatformDictEnum.WDT.getCode())
                        .eq(ThirdMappingEntity::getThirdCode, dmpSoInfoEntity.getShopNo())
                        .list();
                if (CollectionUtils.isEmpty(shop)) {
                    ShopInfoEntity shopInfo = FeignQuery.getById(ShopInfoEntity.class, shop.get(0).getSysId());
                    CustomerInfoEntity customerInfo = FeignQuery.getById(CustomerInfoEntity.class, shopInfo.getCustomerId());

                    if (ObjectUtil.isEmpty(customerInfo)) {
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

                    shudiyunB2cOrderDTO.setShop_no(shopInfo.getId());
                    shudiyunB2cOrderDTO.setShop_name(shopInfo.getName());
                    DictCurrencyEntity dictCurrencyEntity = FeignQuery.getById(DictCurrencyEntity.class, shopInfo.getTradeCurrency());
                    if (ObjectUtil.isNotEmpty(dictCurrencyEntity)) {
                        shudiyunB2cOrderDTO.setTransaction_currency(dictCurrencyEntity.getName());
                    }
                    shudiyunB2cOrderDTO.setTransaction_currency_code(shopInfo.getTradeCurrency());
                    shudiyunB2cOrderDTO.setSettlement_currency_code(shopInfo.getSettlementCurrency());
                }



            shudiyunB2cOrderDTO.setPlatform_id(dmpSoInfoEntity.getSourceSystem());
            shudiyunB2cOrderDTO.setPlatform_name(PlatformDictEnum.getNameByCode(dmpSoInfoEntity.getSourcePlatform()));
            shudiyunB2cOrderDTO.setShop_no(dmpSoInfoEntity.getShopId());
            shudiyunB2cOrderDTO.setShop_name(dmpSoInfoEntity.getShopName());
            shudiyunB2cOrderDTO.setRoot_node_no(dmpSoInfoEntity.getThirdCode());
            shudiyunB2cOrderDTO.setRoot_node_create_time(dmpSoInfoEntity.getPayTime());
            shudiyunB2cOrderDTO.setRoot_node_modify_time(dmpSoInfoEntity.getPlatformUpdateTime());
            shudiyunB2cOrderDTO.setGoods_no(dmpSoDetailEntity.getPlatformSku());
            shudiyunB2cOrderDTO.setGoods_name(dmpSoDetailEntity.getSkuName());
            shudiyunB2cOrderDTO.setSpec_no(dmpSoDetailEntity.getPlatformSpuNo());
            shudiyunB2cOrderDTO.setSpec_name("");
            if (dmpSoDetailEntity.getIsGift()) {
                shudiyunB2cOrderDTO.setIs_gift(1);
            } else {
                shudiyunB2cOrderDTO.setIs_gift(0);
            }

            shudiyunB2cOrderDTO.setRemark(dmpSoDetailEntity.getItemRemark());

            // 商品状态
            if (SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(dmpSoInfoEntity.getDeliveryStatus())) {
                shudiyunB2cOrderDTO.setGoods_status("10.10");
            }

            if (dmpSoInfoEntity.getIsCancel()) {
                shudiyunB2cOrderDTO.setGoods_status("10.20");
            }

            shudiyunB2cOrderDTO.setGoods_transaction_quantity(dmpSoDetailEntity.getQty());
            shudiyunB2cOrderDTO.setUnit(dmpSoDetailEntity.getProductUnit());

            shudiyunB2cOrderDTO.setGoods_transaction_amount(dmpSoDetailEntity.getAfterAmount());
            if (dmpSoDetailEntity.getAfterAmount().compareTo(BigDecimal.ZERO) > 0) {
                //获得分摊的商品优惠额
                BigDecimal shareDiscount = dmpSoDetailEntity.getAfterAmount().divide(dmpSoInfoEntity.getAllAmount(), 4, RoundingMode.HALF_UP).multiply(totalDiscount);
                //计算为真实售价(原始币别)-商品分摊优惠/订单数量
                if (dmpSoDetailEntityList.size() == i-1) {
                    shudiyunB2cOrderDTO.setPrice(dmpSoDetailEntity.getAfterAmount().subtract(dmpSoInfoEntity.getAllAmount()).divide(MathUtil.valueOf(dmpSoDetailEntity.getQty()), 4, RoundingMode.HALF_UP));
                    shudiyunB2cOrderDTO.setGoods_transaction_amount(dmpSoDetailEntity.getAfterAmount().subtract(dmpSoInfoEntity.getAllAmount()));
                } else {
                    shudiyunB2cOrderDTO.setPrice(dmpSoDetailEntity.getAfterAmount().subtract(shareDiscount).divide(MathUtil.valueOf(dmpSoDetailEntity.getQty()), 4, RoundingMode.HALF_UP));
                    shudiyunB2cOrderDTO.setGoods_transaction_amount(dmpSoDetailEntity.getAfterAmount().subtract(shareDiscount));
                }
                totalDiscount = totalDiscount.subtract(shareDiscount);
            }

            shudiyunB2cOrderDTO.setTransaction_currency_code(dmpSoDetailEntity.getCurrencyCode());

            shudiyunB2cOrderDTO.setPost_amount(dmpSoInfoEntity.getShippingAmount());
            shudiyunB2cOrderDTO.setMsku_code(dmpSoDetailEntity.getPlatformSku());
            shudiyunB2cOrderDTO.setMsku_name(dmpSoDetailEntity.getSkuName());
            shudiyunB2cOrderDTO.setSku_code("");
            shudiyunB2cOrderDTO.setSku_name("");

            shudiyunB2cOrderDTO.setSource_system("SDC");
            shudiyunB2cOrderDTO.setRoot_node_no_initial(dmpSoInfoEntity.getPlatformCode());

            shudiyunB2cOrderDTOList.add(shudiyunB2cOrderDTO);

        }*/
        return shudiyunB2cOrderDTOList;

    }
}
