package com.erp.server.oms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.oms.dto.SoChangeDetailDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.SoChangeTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.wms.dto.SoOutstockDetailDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.entity.SoDeliveryNoticeDetailEntity;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.scm.feign.*;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.InventoryFeign;
import com.erp.rpc.wms.feign.SoDeliveryNoticeFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.oms.mapper.SoChangeDetailMapper;
import com.erp.server.oms.service.*;
import com.erp.server.oms.utils.SoUtils;
import com.google.common.collect.Maps;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 销售订单变更明细 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Slf4j
@Service
public class SoChangeDetailServiceImpl extends SuperServiceImpl<SoChangeDetailMapper, SoChangeDetailEntity> implements SoChangeDetailService {

    @Resource
    private SoDetailService soDetailService;

    @Resource
    private SoInfoService soInfoService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private InventoryFeign inventoryFeign;

    @Resource
    private SoOutstockFeign soOutstockFeign;

    @Resource
    private SoDeliveryNoticeFeign soDeliveryNoticeFeign;

    @Resource
    private CustomerAddressService customerAddressService;

    @Resource
    private SoReturnService soReturnService;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    /**
     * 添加变更详情信息
     *
     * @param mainId
     * @param detailList
     * @return void
     * @author yl
     * @date 2023-05-24 14:12
     */
    @Override
    public void addDetailList(String mainId, List<SoChangeDetailDTO.AddDTO> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<SoChangeDetailEntity> addList = new ArrayList<>(detailList.size());
        //销售订单的详情id 集合
        List<String> soDetailIdList = detailList.stream().map(SoChangeDetailDTO.AddDTO::getSoDetailId).collect(Collectors.toList());
        List<SoDetailEntity> soDetailList = CollectionUtils.isNotEmpty(soDetailIdList) ? soDetailService.listByIds(soDetailIdList) : Collections.emptyList();
        //币种列表
        List<String> currencyList = detailList.stream().map(SoChangeDetailDTO.AddDTO::getCurrency).collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currencyViewList = sysUserFeign.listByCurrency(currencyList);
        List<String> skuIdList = detailList.stream().map(SoChangeDetailDTO.AddDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);

        for (SoChangeDetailDTO.AddDTO item : detailList) {
            SoChangeDetailEntity soChangeDetail = new SoChangeDetailEntity();
            String skuId = item.getSkuId();
            //销售订单详情
            String soDetailId = item.getSoDetailId();
            //原来的销售订单
            SoDetailEntity soDetail = soDetailList.stream().filter(s -> s.getId().equals(soDetailId)).findFirst().orElse(null);
            Boolean isGift = item.getIsGift();
            BigDecimal price = item.getPrice();
            Integer qty = item.getQty();
            //当是赠品的时候  单价为0
            if (isGift) {
                price = BigDecimal.ZERO;
            }
            String currency = item.getCurrency();
            String symbol = currencyViewList.stream().filter(c -> c.getId().equals(currency)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSymbol())).orElse("");

            //税率
            BigDecimal taxRate = item.getTaxRate();
            BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);
            //含税单价=销售单价*（税率+1）
            BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
            BigDecimal taxPrice = MathUtil.multiply(price, multiplyTax);
            //金额
            BigDecimal amount = MathUtil.multiply(price, qty);
            soChangeDetail.setIsGift(isGift);
            soChangeDetail.setPrice(price);
            soChangeDetail.setCurrency(currency);
            soChangeDetail.setCurrencySymbol(symbol);
            soChangeDetail.setIsReissue(item.getIsReissue());
            soChangeDetail.setAmount(amount);
            soChangeDetail.setMainId(mainId);
            soChangeDetail.setSkuId(skuId);
            soChangeDetail.setRemark(item.getRemark());
            soChangeDetail.setChangeType(item.getChangeType());
            soChangeDetail.setQty(item.getQty());
            soChangeDetail.setTaxRate(item.getTaxRate());
            soChangeDetail.setOldPrice(soDetail != null ? soDetail.getPrice() : BigDecimal.ZERO);
            soChangeDetail.setOldAmount(soDetail != null ? soDetail.getAmount() : BigDecimal.ZERO);
            soChangeDetail.setOldCurrency(soDetail != null ? soDetail.getCurrency() : "");
            soChangeDetail.setOldCurrencySymbol(soDetail != null ? soDetail.getCurrencySymbol() : "");
            soChangeDetail.setOldQty(soDetail != null ? soDetail.getQty() : 0);
            soChangeDetail.setOldTaxRate(soDetail != null ? soDetail.getTaxRate() : BigDecimal.ZERO);
            soChangeDetail.setSoDetailId(soDetailId);
            String skuNo = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSkuNo())).orElse("");
            soChangeDetail.setSkuNo(skuNo);
            addList.add(soChangeDetail);
        }
        this.saveBatch(addList);


    }


    /**
     * 根据主表id 获取详情信息
     *
     * @param mainId
     * @return java.util.List<com.erp.model.oms.dto.SoChangeDetailDTO.ViewDTO>
     * @author yl
     * @date 2023-05-25 9:00
     */
    @Override
    public List<SoChangeDetailDTO.ViewDTO> listDetailByMainId(String mainId) {
        List<SoChangeDetailEntity> dbList = this.listDetailDbByMainId(mainId);
        if (CollectionUtils.isEmpty(dbList)) {
            throw new ServiceException(ApiError.ERROR_92036);
        }
        List<SoChangeDetailDTO.ViewDTO> viewList = BeanMapper.copyList(dbList, SoChangeDetailDTO.ViewDTO.class);
        List<String> skuIdList = viewList.stream().map(SoChangeDetailDTO.ViewDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);
        for (SoChangeDetailDTO.ViewDTO item : viewList) {
            String skuId = item.getSkuId();
            SkuVO sku = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(null);
            String unit = "";
            String productName = "";
            String variantProperty = "";
            if (sku != null) {
                unit = sku.getUnitName();
                productName = sku.getSkuName();
                variantProperty = sku.getVariantProperty();
            }
            item.setUnit(unit);
            item.setProductName(productName);
            item.setVariantProperty(variantProperty);
            //税率
            BigDecimal taxRate = item.getTaxRate();
            //税率
            BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);
            //单价
            BigDecimal price = item.getPrice();
            //含税单价=销售单价*（税率+1）
            BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
            BigDecimal taxPrice = MathUtil.multiply(price, multiplyTax);
            item.setTaxPrice(taxPrice);

            BigDecimal oldPrice = item.getOldPrice();
            BigDecimal oldTaxRate = item.getOldTaxRate();
            BigDecimal oldFlagTaxRate = MathUtil.divide(oldTaxRate, MathUtil.BigDecimal_100);

            //含税单价=销售单价*（税率+1）
            BigDecimal oldMultiplyTax = MathUtil.add(oldFlagTaxRate, MathUtil.BigDecimal_1);
            BigDecimal oldTaxPrice = MathUtil.multiply(oldPrice, oldMultiplyTax);
            item.setOldPrice(oldPrice);
            item.setOldTaxPrice(oldTaxPrice);
        }
        return viewList;
    }

    @Override
    public List<SoChangeDetailEntity> listByMainIdList(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(SoChangeDetailEntity::getMainId,mainIdList).list();
    }


    /**
     * 根据销售单id 获取到对应详情数据
     *
     * @param soId
     * @return java.util.List<com.erp.model.oms.dto.SoChangeDetailDTO.ViewDTO>
     * @author yl
     * @date 2023-05-25 14:11
     */
    @Override
    public List<SoChangeDetailDTO.ViewDTO> listDetailBySoId(String soId, List<String> soDetailIds, Boolean hasContain) {
        List<SoDetailEntity> soDetailList = soDetailService.listDetailBySoId(soId, soDetailIds, hasContain);
        List<SoChangeDetailDTO.ViewDTO> viewList = new ArrayList<>(soDetailList.size());
        if (CollectionUtils.isNotEmpty(soDetailList)) {
            BigDecimal zero = BigDecimal.ZERO;
            SoChangeTypeEnum update = SoChangeTypeEnum.UPDATE;
            List<String> skuIdList = soDetailList.stream().map(SoDetailEntity::getSkuId).collect(Collectors.toList());
            List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);
            for (SoDetailEntity item : soDetailList) {
                SoChangeDetailDTO.ViewDTO view = new SoChangeDetailDTO.ViewDTO();
                view.setOldAmount(item.getPrice());
                view.setOldCurrency(item.getCurrency());
                view.setOldCurrencySymbol(item.getCurrencySymbol());
                view.setOldQty(item.getQty());
                BigDecimal oldPrice = item.getPrice();
                view.setOldPrice(oldPrice);
                BigDecimal oldTaxRate = item.getTaxRate();
                BigDecimal oldFlagTaxRate = MathUtil.divide(oldTaxRate, MathUtil.BigDecimal_100);
                view.setOldTaxRate(oldTaxRate);
                //含税单价=销售单价*（税率+1）
                BigDecimal oldMultiplyTax = MathUtil.add(oldFlagTaxRate, MathUtil.BigDecimal_1);
                BigDecimal oldTaxPrice = MathUtil.multiply(oldPrice, oldMultiplyTax);
                view.setOldTaxPrice(oldTaxPrice);
                view.setQty(0);
                view.setTaxPrice(zero);
                view.setAmount(zero);
                view.setChangeType(update);
                view.setIsGift(Boolean.FALSE);
                view.setIsReissue(Boolean.FALSE);
                view.setPrice(zero);
                view.setTaxRate(zero);
                view.setCurrency(item.getCurrency());
                view.setCurrencySymbol(item.getCurrencySymbol());
                String skuId = item.getSkuId();
                view.setSkuId(skuId);
                view.setSkuNo(item.getSkuNo());
                view.setSoDetailId(item.getId());
                SkuVO sku = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(null);
                String unit = "";
                String productName = "";
                String variantProperty = "";
                if (sku != null) {
                    unit = sku.getUnitName();
                    productName = sku.getSkuName();
                    variantProperty = sku.getVariantProperty();
                }
                view.setUnit(unit);
                view.setProductName(productName);
                view.setVariantProperty(variantProperty);
                viewList.add(view);
            }
        }
        return viewList;
    }


    /**
     * 根据销售单id 获取到选择产品的信息
     *
     * @param soId
     * @param soDetailIds 销售订单详情id
     * @param hasContain  是否包含
     * @return java.util.List<com.erp.model.oms.dto.SoChangeDetailDTO.ViewDTO>
     * @author yl
     * @date 2023-05-25 14:11
     */
    @Override
    public List<SoChangeDetailDTO.SoDetailViewDTO> listSelectDetailBySoId(String soId, List<String> soDetailIds, Boolean hasContain) {
        SoInfoEntity soInfo = soInfoService.getById(soId);
        if (Objects.isNull(soInfo)) {
            throw new ServiceException(ApiError.ERROR_92016);
        }
        String warehouseOrgName = soInfo.getWarehouseOrgName();
        String warehouseId = soInfo.getWarehouseId();
        List<SoDetailEntity> soDetailList = soDetailService.listDetailBySoId(soId, soDetailIds, hasContain);
        if (CollectionUtils.isEmpty(soDetailList)) {
            return Collections.emptyList();
        }
        List<String> detailIds = soDetailList.stream().map(SoDetailEntity::getId).collect(Collectors.toList());
        List<SoOutstockDetailDTO.DeliveryQtyDTO> soOutstockDetailList = soOutstockFeign.listDetailBySoDetailIds(detailIds);
        List<String> skuIdList = soDetailList.stream().map(SoDetailEntity::getSkuId).collect(Collectors.toList());
        //从wms 获取到sku 的即时库存信息
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = new ArrayList<>();
        if (StringUtils.isNotBlank(warehouseId) && CollectionUtils.isNotEmpty(skuIdList)) {
            skuInventoryTotalList = listSkuInventoryTotalList(skuIdList, warehouseId);
        }
        List<SoChangeDetailDTO.SoDetailViewDTO> viewList = new ArrayList<>(soDetailList.size());
        BigDecimal zero = BigDecimal.ZERO;
        SoChangeTypeEnum update = SoChangeTypeEnum.UPDATE;
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);
        for (SoDetailEntity item : soDetailList) {
            SoChangeDetailDTO.SoDetailViewDTO view = new SoChangeDetailDTO.SoDetailViewDTO();
            view.setOldAmount(item.getPrice());
            view.setOldCurrency(item.getCurrency());
            view.setOldCurrencySymbol(item.getCurrencySymbol());
            view.setOldQty(item.getQty());
            BigDecimal oldPrice = item.getPrice();
            view.setOldPrice(oldPrice);
            BigDecimal oldTaxRate = item.getTaxRate();
            BigDecimal oldFlagTaxRate = MathUtil.divide(oldTaxRate, MathUtil.BigDecimal_100);
            view.setOldTaxRate(oldTaxRate);
            view.setWarehouseOrgName(warehouseOrgName);
            //含税单价=销售单价*（税率+1）
            BigDecimal oldMultiplyTax = MathUtil.add(oldFlagTaxRate, MathUtil.BigDecimal_1);
            BigDecimal oldTaxPrice = MathUtil.multiply(oldPrice, oldMultiplyTax);
            view.setOldTaxPrice(oldTaxPrice);
            view.setQty(0);
            view.setTaxPrice(zero);
            view.setAmount(zero);
            view.setChangeType(update);
            view.setIsGift(Boolean.FALSE);
            view.setIsReissue(Boolean.FALSE);
            view.setPrice(zero);
            view.setTaxRate(zero);
            view.setCurrency(item.getCurrency());
            view.setCurrencySymbol(item.getCurrencySymbol());
            String skuId = item.getSkuId();
            view.setSkuId(skuId);
            view.setSkuNo(item.getSkuNo());
            view.setSoDetailId(item.getId());

            //即时库存
            Integer curInventoryQty = skuInventoryTotalList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal())).orElse(0);
            view.setCurInventoryQty(curInventoryQty);
            //销售数量
            Integer qty = item.getQty();
            /**
             * 缺货数量
             * 当可用即时库存数量小于销售数量时，
             * 缺货数量=销售数量-可用即时库存数量；
             * 当可用即时库存数量大于销售数量时，缺货数量为0
             */
            Integer scarceQty = 0;

            /**
             * 已出库数量
             * 新增时默认为0
             * 编辑时根据关联出库单
             * 总共已发货数量同步
             *
             */
            Integer deliveryQty = soOutstockDetailList.stream().filter(req -> req.getSoDetailId().equals(item.getId())).map(SoOutstockDetailDTO.DeliveryQtyDTO::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            /**
             * 剩余数量
             * 销售数量-已出库数量
             */
            Integer waitQty = qty > deliveryQty ? qty - deliveryQty : 0;
            if (qty > curInventoryQty) {
                scarceQty = qty - curInventoryQty;
            }
            view.setScarceQty(scarceQty);
            view.setAvailableQty(getAvailableQty(curInventoryQty, qty));
            view.setDeliveryQty(deliveryQty);
            view.setWaitQty(waitQty);
            SkuVO sku = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(null);
            String unit = "";
            String productName = "";
            String variantProperty = "";
            if (sku != null) {
                unit = sku.getUnitName();
                productName = sku.getSkuName();
                variantProperty = sku.getVariantProperty();
            }
            view.setUnit(unit);
            view.setProductName(productName);
            view.setVariantProperty(variantProperty);
            viewList.add(view);
        }
        return viewList;
    }

    private Integer getAvailableQty(Integer curInventoryQty, Integer salesQty) {
        /**
         * 可出数量
         * 根据可用即时库存计算可出数量，
         * 当可用即时库存数量大于销售数量时 可出数量=销售数量；
         * 若可用即时库存数量小于销售数量，可出数量=即时可用库存数量
         */
        Integer availableQty = 0;
        Boolean isGre = curInventoryQty > salesQty;
        if (isGre) {
            availableQty = salesQty;
        } else {
            availableQty = curInventoryQty;
        }
        return availableQty;
    }

    private List<InventoryQtyDTO.SkuInventoryTotalDTO> listSkuInventoryTotalList(List<String> skuIdList, String warehouseId) {
        InventoryQtyDTO.FindSkuInventoryParamDTO paramDTO = new InventoryQtyDTO.FindSkuInventoryParamDTO();
        paramDTO.setSkuIds(skuIdList);
        paramDTO.setWarehouseId(warehouseId);
        paramDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());
        //从wms 获取到sku 的即时库存信息
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = inventoryFeign.listSkuInventory(paramDTO);
        return skuInventoryTotalList;
    }


    /**
     * 审核通过处理数据
     *
     * @param list
     * @return void
     * @author yl
     * @date 2023-05-25 17:44
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public void handleDb(List<SoChangeEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        // 更新销售订单和销售退货订单的地址信息（地址信息发生了变更的情况下）
        List<String> soIds = list.stream().map(SoChangeEntity::getSoId).distinct().collect(Collectors.toList());
        List<SoInfoEntity> soInfoList = soInfoService.listByIds(soIds);
        Map<String, SoInfoEntity> soInfoMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(soInfoList)) {
            soInfoMap = soInfoList.stream().collect(Collectors.toMap(SoInfoEntity::getId, Function.identity()));
            List<String> receiveAddressIds = list.stream().filter(r -> StrUtils.isNotEmpty(r.getReceiveAddressId())).map(SoChangeEntity::getReceiveAddressId).distinct().collect(Collectors.toList());
            Map<String, CustomerAddressEntity> customerAddressMap = Maps.newHashMap();
            if (CollUtil.isNotEmpty(receiveAddressIds)) {
                List<CustomerAddressEntity> customerAddressList = customerAddressService.listByIds(receiveAddressIds);
                customerAddressMap = customerAddressList.stream().collect(Collectors.toMap(CustomerAddressEntity::getId, Function.identity()));
            }

            for (SoChangeEntity soChangeEntity : list) {
                SoInfoEntity soInfoEntity = soInfoMap.get(soChangeEntity.getSoId());
                if (!Objects.equals(soInfoEntity.getReceiveAddressId(), soChangeEntity.getReceiveAddressId())
                        || !Objects.equals(soInfoEntity.getAddressType(), soChangeEntity.getAddressType())
                        || !Objects.equals(soInfoEntity.getReceiverName(), soChangeEntity.getReceiverName())
                        || !Objects.equals(soInfoEntity.getTelNumber(), soChangeEntity.getTelNumber())) {
                    log.warn("销售订单【{}】销售变更单【{}】地址信息发生变化，同步更新销售订单和销售退货订单的地址信息", soInfoEntity.getCode(), soChangeEntity.getCode());
                    soInfoService.updateAddress(soInfoEntity.getId(), soChangeEntity.getReceiveAddressId(), soChangeEntity.getAddressType(),
                            soChangeEntity.getReceiverName(), soChangeEntity.getTelNumber());

                    String receiveAddress = customerAddressMap.getOrDefault(soChangeEntity.getReceiveAddressId(), new CustomerAddressEntity()).getAddress();
                    soReturnService.updateAddress(soInfoEntity.getId(), receiveAddress, soChangeEntity.getReceiverName(), soChangeEntity.getTelNumber());
                }
            }
        }

        List<String> mainIds = list.stream().map(SoChangeEntity::getId).collect(Collectors.toList());
        //这个就是变更的详情
        List<SoChangeDetailEntity> soChangeDetailList = this.listDetailByMainIds(mainIds);
        List<String> soDetailIds = soChangeDetailList.stream().map(SoChangeDetailEntity::getSoDetailId).distinct().collect(Collectors.toList());
        List<SoDetailEntity> soDetailEntityList = soDetailService.listSoDetailByIds(soDetailIds);
        if (CollectionUtils.isNotEmpty(soChangeDetailList)) {
            SoChangeTypeEnum deleteType = SoChangeTypeEnum.DELETE;
            //终止
            SoChangeTypeEnum terminate = SoChangeTypeEnum.TERMINATE;

            //添加
            SoChangeTypeEnum addType = SoChangeTypeEnum.ADD;

            List<String> deleteSoDetailIdList = soChangeDetailList.stream().filter(s -> s.getChangeType().equals(deleteType)).
                    map(SoChangeDetailEntity::getSoDetailId).collect(Collectors.toList());
            soDetailService.removeByIds(deleteSoDetailIdList);
            //关闭的销售订单id
            List<String> closeSoDetailIdList = soChangeDetailList.stream().filter(s -> s.getChangeType().equals(terminate)).
                    map(SoChangeDetailEntity::getSoDetailId).collect(Collectors.toList());
            //关闭销售订单
            soDetailService.closeSoDetailByIds(closeSoDetailIdList);
            List<SoChangeTypeEnum> excludeList = Arrays.asList(deleteType, terminate);

            List<SoChangeDetailEntity> otherList = soChangeDetailList.stream().filter(s -> !excludeList.contains(s.getChangeType())).
                    collect(Collectors.toList());
            List<SoDetailEntity> saveOrUpdateList = new ArrayList<>(otherList.size());
            for (SoChangeDetailEntity item : otherList) {
                //变更类型
                SoChangeTypeEnum changeType = item.getChangeType();
                String soChangeId = item.getMainId();
                String soId = list.stream().filter(l -> l.getId().equals(soChangeId)).findFirst().
                        flatMap(obj -> Optional.ofNullable(obj.getSoId())).orElse("");
                if (StringUtils.isEmpty(soId)) {
                    continue;
                }
                //可能为空 表示添加
                String soDetailId = item.getSoDetailId();
                SoDetailEntity soDetail = new SoDetailEntity();
                soDetail.setPrice(item.getPrice());
                soDetail.setCurrency(item.getCurrency());
                soDetail.setCurrencySymbol(item.getCurrencySymbol());
                soDetail.setSkuNo(item.getSkuNo());
                soDetail.setSkuId(item.getSkuId());
                soDetail.setAmount(item.getAmount());
                soDetail.setTaxRate(item.getTaxRate());
                soDetail.setQty(item.getQty());
                soDetail.setIsGift(item.getIsGift());
                soDetail.setIsReissue(item.getIsReissue());
                soDetail.setRemark(item.getRemark());
                soDetail.setMainId(soId);
                //添加的话id 为null
                if (addType.equals(changeType)) {
                    soDetail.setId(null);
                } else {
                    SoDetailEntity dbEntity = soDetailEntityList.stream().filter(v->v.getId().equals(soDetailId)).findFirst().orElse(null);
                    if(Objects.nonNull(dbEntity)){
                        soDetail.setExchangeRate(dbEntity.getExchangeRate());
                    }
                    soDetail.setId(soDetailId);
                }

                saveOrUpdateList.add(soDetail);
            }


            if (CollUtil.isNotEmpty(saveOrUpdateList)) {
                this.handleDetailAmountByChange(saveOrUpdateList, soInfoMap, closeSoDetailIdList);
                List<String> skuIdList = saveOrUpdateList.stream().map(SoDetailEntity::getSkuId).collect(Collectors.toList());
                List<SkuVO> skuList = plmTaskFeign.listSkuCostByIds(skuIdList);
                // 供应商id集合
//                List<String> supplierIds = skuList.stream().filter(r -> StrUtil.isNotEmpty(r.getSupplierId())).map(SkuVO::getSupplierId).distinct().collect(Collectors.toList());
//                List<PurchasePriceDTO.SupplierSkuPrice> purchasePriceList = Lists.newArrayList();
//                if (CollUtil.isNotEmpty(supplierIds)) {
//                    purchasePriceList = scmTaskFeign.listSupplierSkuPrice(supplierIds);
//                }
                Map<String, List<SoDetailEntity>> soDetailSaveMap = saveOrUpdateList.stream().collect(Collectors.groupingBy(SoDetailEntity::getMainId));
                for (Map.Entry<String, List<SoDetailEntity>> soEntry : soDetailSaveMap.entrySet()) {
                    // 金额信息加上折扣额计算
                    String soId = soEntry.getKey();
                    SoInfoEntity soInfoEntity = soInfoMap.get(soId);
                    for (SoDetailEntity item : saveOrUpdateList) {
                        // 计算毛利成本
                        soDetailService.calCost(skuList, soInfoEntity.getBillDate(), item, Boolean.FALSE);
                    }
                }
            }
            soDetailService.saveOrUpdateBatch(saveOrUpdateList);
            //更新销售出库单价
            soOutstockFeign.updateSoOutPrice(saveOrUpdateList);
            //关闭关联单据的关闭状态
            wmsTaskFeign.closeBySoDetailIds(closeSoDetailIdList);
            //释放明细库存
            List<String> idList = saveOrUpdateList.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getId())).map(SoDetailEntity::getId).distinct().collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(idList)) {
                soDetailService.batchUnLockVirtualInventory(idList,null);
            }
        }

    }


    /**
     * 填充变更单的的金额信息
     *
     * @param saveOrUpdateList
     * @param soInfoMap
     * @return void
     * @author yl
     * @date 2023-10-16 16:05
     */
    public void handleDetailAmountByChange(List<SoDetailEntity> saveOrUpdateList, Map<String, SoInfoEntity> soInfoMap, List<String> closeSoDetailIdList) {
        if (CollectionUtils.isEmpty(saveOrUpdateList)) {
            return;
        }
        List<SoDetailEntity> newList = new ArrayList<>();
        //根据销售订单分组
        Map<String, List<SoDetailEntity>> map = saveOrUpdateList.stream().collect(Collectors.groupingBy(SoDetailEntity::getMainId));
        for (Map.Entry<String, List<SoDetailEntity>> item : map.entrySet()) {
            String soId = item.getKey();
            SoInfoEntity soInfo = soInfoMap.getOrDefault(soId, null);
            if (Objects.isNull(soInfo)) {
                continue;
            }
            List<SoDetailEntity> soDetailList = item.getValue();
            List<String> updateIdList = soDetailList.stream().map(SoDetailEntity::getId).collect(Collectors.toList());
            updateIdList.addAll(closeSoDetailIdList);
            List<SoDetailEntity> soDbDetailList = soDetailService.listSoDetailByMainId(soId);
            //表示只有只有未修改的
            soDbDetailList = soDbDetailList.stream().filter(s -> !updateIdList.contains(s.getId())).collect(Collectors.toList());
            //是否含税
            Boolean isTax = soInfo.getIsTax();
            //折扣总额
            BigDecimal discountAmount = soInfo.getDiscountAmount();
            soDetailList.addAll(soDbDetailList);

            SoUtils.handleDetailAmount(isTax, discountAmount, soDetailList);
            newList.addAll(soDetailList);

        }
        saveOrUpdateList = newList;


    }

    private List<SoChangeDetailEntity> listDetailByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(SoChangeDetailEntity::getMainId, mainIds).list();
    }


    /**
     * 检查对应的变更类型
     *
     * @param detailList
     * @return void
     * @author yl
     * @date 2023-05-25 10:14
     */
    @Override
    public void checkChange(List<SoChangeDetailDTO.UpdateDTO> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        //刪除
        SoChangeTypeEnum delete = SoChangeTypeEnum.DELETE;
        List<SoChangeDetailDTO.AddDTO> notDeleteList = detailList.stream().filter(d -> !d.getChangeType().equals(delete)).collect(Collectors.toList());
        long qtyCount = notDeleteList.stream().filter(n -> n.getQty() <= 0).count();
        if (qtyCount > 0) {
            throw new ServiceException("销售数量不能小于0");
        }
        long priceCount = notDeleteList.stream().filter(n -> !n.getIsGift() && !n.getIsReissue() && n.getPrice().compareTo(BigDecimal.ZERO) <= 0).count();
        if (priceCount > 0) {
            throw new ServiceException("单价不能小于0");
        }
        List<SoChangeDetailDTO.UpdateDTO> deleteDetailList = listDetailParamByType(detailList, delete);
        if (CollectionUtils.isNotEmpty(deleteDetailList)) {
            List<String> soDetailIdList = deleteDetailList.stream().
                    map(SoChangeDetailDTO.UpdateDTO::getSoDetailId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(soDetailIdList)) {
                throw new ServiceException(ApiError.ERROR_92015);
            }
            //下推单据的数量
            Integer pushDownCount = wmsTaskFeign.getPushDownBySoDetailIds(soDetailIdList);
            if (pushDownCount > 0) {
                throw new ServiceException(ApiError.ERROR_92037);
            }
        }
        //这个是修改
        SoChangeTypeEnum update = SoChangeTypeEnum.UPDATE;
        List<SoChangeDetailDTO.UpdateDTO> updateDetailList = listDetailParamByType(detailList, update);
        if (CollectionUtils.isNotEmpty(updateDetailList)) {
            List<String> soDetailIdList = updateDetailList.stream().
                    map(SoChangeDetailDTO.UpdateDTO::getSoDetailId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(soDetailIdList)) {
                throw new ServiceException(ApiError.ERROR_92015);
            }
            //发货通知单
            List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailList = soDeliveryNoticeFeign.listDetailBySourceDetailIds(soDetailIdList);
            for (SoChangeDetailDTO.UpdateDTO item : updateDetailList) {
                Integer qty = item.getQty();
                Integer deliveryQty = soDeliveryNoticeDetailList.stream().filter(s -> s.getSourceDetailId().
                                equals(item.getSoDetailId()) && !s.getInvalidStatus()).
                        mapToInt(SoDeliveryNoticeDetailEntity::getDeliveryQty).sum();
                if (qty < deliveryQty) {
                    throw new ServiceException(ApiError.ERROR_92049);
                }
            }
        }
        //这个是终止
        SoChangeTypeEnum terminate = SoChangeTypeEnum.TERMINATE;
        //终止的
        List<SoChangeDetailDTO.UpdateDTO> terminateDetailList = listDetailParamByType(detailList, terminate);
        if (CollectionUtils.isNotEmpty(terminateDetailList)) {
            List<String> idList=terminateDetailList.stream().filter(t->StringUtils.isNotBlank(t.getId())).
                    map(SoChangeDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
            //销售订单详情id
            List<String> soDetailIdList = terminateDetailList.stream().map(SoChangeDetailDTO.UpdateDTO::getSoDetailId).collect(Collectors.toList());
            //已经存在的
            List<SoChangeDetailEntity> dbChangeDetailList = this.listBySoDetailIdList(soDetailIdList,terminate);
            List<SoChangeDetailEntity>  needCheckList= dbChangeDetailList.stream().filter(d->!idList.contains(d.getId())).collect(Collectors.toList());
            long existCount= needCheckList.stream().filter(c->soDetailIdList.contains(c.getSoDetailId())).count();
            if(existCount>0){
                throw new ServiceException(ApiError.SO_CHANGE_TERMINATE_EXIST);
            }

        }

    }


    private List<SoChangeDetailEntity> listBySoDetailIdList(List<String> soDetailIdList,SoChangeTypeEnum typeEnum) {
        if (CollectionUtils.isEmpty(soDetailIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(SoChangeDetailEntity::getSoDetailId,soDetailIdList)
                .eq(SoChangeDetailEntity::getChangeType,typeEnum).list();
    }


    public List<SoChangeDetailDTO.UpdateDTO> listDetailParamByType(List<SoChangeDetailDTO.UpdateDTO> detailList, SoChangeTypeEnum typeEnum) {
        return detailList.stream().filter(d -> typeEnum.equals(d.getChangeType())).collect(Collectors.toList());
    }


    /**
     * 根据主表删除明细
     *
     * @param mainIds
     * @return void
     * @author yl
     * @date 2023-05-25 11:11
     */
    @Override
    public void removeByMainIdList(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return;
        }
        LambdaQueryWrapper<SoChangeDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SoChangeDetailEntity::getMainId, mainIds);
        this.remove(queryWrapper);

    }


    /**
     * 更改销售变更详情
     *
     * @param mainId
     * @param detailList
     * @return void
     * @author yl
     * @date 2023-05-25 12:03
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDetailList(String mainId, List<SoChangeDetailDTO.UpdateDTO> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<SoChangeDetailEntity> saveOrUpdateList = new ArrayList<>(detailList.size());
        //这是修改的
        List<SoChangeDetailDTO.UpdateDTO> updateList = detailList.stream().filter(c -> StringUtils.isNotBlank(c.getId())).collect(Collectors.toList());
        //这是要添加的
        List<SoChangeDetailDTO.UpdateDTO> addList = detailList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());
        //这个是要修改的实体
        List<SoChangeDetailEntity> updateEntityList = BeanMapper.copyList(updateList, SoChangeDetailEntity.class);
        //这个是要添加的
        List<SoChangeDetailEntity> addEntityList = BeanMapper.copyList(addList, SoChangeDetailEntity.class);
        saveOrUpdateList.addAll(updateEntityList);
        saveOrUpdateList.addAll(addEntityList);
        List<SoChangeDetailEntity> dbList = this.listDetailDbByMainId(mainId);
        List<Pair<String, String>> pairList = updateList.stream().map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
        List<String> deleteIdList = getDeleteIds(pairList, dbList);
        List<SoChangeDetailEntity> removeList = dbList.stream().filter(r -> deleteIdList.contains(r.getId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        List<String> skuIdList = detailList.stream().map(SoChangeDetailDTO.UpdateDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);
        //币种列表
        List<String> currencyList = detailList.stream().map(SoChangeDetailDTO.UpdateDTO::getCurrency).collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currencyViewList = sysUserFeign.listByCurrency(currencyList);

        //销售订单的详情id 集合
        List<String> soDetailIdList = detailList.stream().map(SoChangeDetailDTO.AddDTO::getSoDetailId).collect(Collectors.toList());
        List<SoDetailEntity> soDetailList = CollectionUtils.isNotEmpty(soDetailIdList) ? soDetailService.listByIds(soDetailIdList) : Collections.emptyList();

        for (SoChangeDetailEntity item : saveOrUpdateList) {
            String skuId = item.getSkuId();
            //销售订单详情
            String soDetailId = item.getSoDetailId();
            //原来的销售订单
            SoDetailEntity soDetail = soDetailList.stream().filter(s -> s.getId().equals(soDetailId)).findFirst().orElse(null);
            Boolean isGift = item.getIsGift();
            BigDecimal price = item.getPrice();
            Integer qty = item.getQty();
            //当是赠品的时候  单价为0
            if (isGift) {
                price = BigDecimal.ZERO;
            }
            String currency = item.getCurrency();
            String symbol = currencyViewList.stream().filter(c -> c.getId().equals(currency)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSymbol())).orElse("");

            //税率
            BigDecimal taxRate = item.getTaxRate();
            BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);
            //含税单价=销售单价*（税率+1）
            BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
            BigDecimal taxPrice = MathUtil.multiply(price, multiplyTax);
            //金额
            BigDecimal amount = MathUtil.multiply(price, qty);
            item.setPrice(price);
            item.setCurrencySymbol(symbol);
            item.setAmount(amount);
            item.setMainId(mainId);
            item.setOldPrice(soDetail != null ? soDetail.getPrice() : BigDecimal.ZERO);
            item.setOldAmount(soDetail != null ? soDetail.getAmount() : BigDecimal.ZERO);
            item.setOldCurrency(soDetail != null ? soDetail.getCurrency() : "");
            item.setOldCurrencySymbol(soDetail != null ? soDetail.getCurrencySymbol() : "");
            item.setOldQty(soDetail != null ? soDetail.getQty() : 0);
            item.setOldTaxRate(soDetail != null ? soDetail.getTaxRate() : BigDecimal.ZERO);
            item.setSoDetailId(soDetailId);
            String skuNo = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSkuNo())).orElse("");
            item.setSkuNo(skuNo);
        }
        //这是删除
        List<Pair<String, String>> removePairList = removeList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("删除了一个销售变更单产品【%s】", ModuleTypeEnum.SO_CHANGE.getCode(), removePairList, "编辑操作");

        //这是添加
        List<Pair<String, String>> addPairList = saveOrUpdateList.stream().filter(s -> StringUtils.isBlank(s.getId())).map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("添加了一个销售变更单产品【%s】", ModuleTypeEnum.SO_CHANGE.getCode(), addPairList, "编辑操作");
        //修改的
        updateEntityList = saveOrUpdateList.stream().filter(s -> StringUtils.isNotBlank(s.getId())).collect(Collectors.toList());
        for (SoChangeDetailEntity update : updateEntityList) {
            String id = update.getId();
            SoChangeDetailEntity old = dbList.stream().filter(d -> d.getId().equals(id)).findFirst().orElse(null);
            if (old != null) {
                operateLogService.addModuleOperateLogByObj(old, update, ModuleTypeEnum.SO_CHANGE.getCode(), mainId, "", "");
            }
        }

        this.saveOrUpdateBatch(saveOrUpdateList);
    }


    /**
     * 获取到删除的数据
     *
     * @param pairList
     * @param dbList
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-05-25 12:07
     */
    private List<String> getDeleteIds(List<Pair<String, String>> pairList, List<SoChangeDetailEntity> dbList) {
        List<String> ids = pairList.stream().filter(g -> StringUtils.isNotBlank(g.getKey())).
                map(obj -> obj.getKey()).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(SoChangeDetailEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());
    }

    @Override
    public List<SoChangeDetailEntity> listDetailDbByMainId(String mainId) {
        return this.lambdaQuery().eq(SoChangeDetailEntity::getMainId, mainId).orderByAsc(SoChangeDetailEntity::getId).list();
    }

    /**
     * 查询变更是否存在 添加的
     *
     * @param mainId
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-08-23 10:01
     */
    @Override
    public Boolean existAdd(String mainId) {
        long count = this.lambdaQuery().eq(SoChangeDetailEntity::getMainId, mainId).
                eq(SoChangeDetailEntity::getChangeType, SoChangeTypeEnum.ADD).count();
        return count > 0;
    }

    @Override
    public List<SoChangeDetailEntity> listBySoDetailIdList(List<String> soDetailIds) {
        if (CollectionUtils.isEmpty(soDetailIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(SoChangeDetailEntity::getSoDetailId, soDetailIds).list();
    }
}
