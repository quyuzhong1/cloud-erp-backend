package com.erp.server.oms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.oms.entity.SoReturnDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.enums.ReturnTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.InventoryFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.oms.mapper.SoDetailMapper;
import com.erp.server.oms.service.SoDetailService;
import com.erp.server.oms.service.SoInfoService;
import com.erp.server.oms.service.SoReturnDetailService;
import com.erp.server.oms.service.SoReturnService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 销售订单详情 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class SoDetailServiceImpl extends SuperServiceImpl<SoDetailMapper, SoDetailEntity> implements SoDetailService {
    @Resource
    private SoReturnService soReturnService;

    @Resource
    private SoInfoService soInfoService;

    @Resource
    private SoReturnDetailService soReturnDetailService;

    @Resource
    private SoOutstockFeign soOutstockFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private InventoryFeign inventoryFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;


    /**
     * 根据退货单详情表id查询退货单
     *
     * @param detailIds
     * @return java.util.List<com.erp.model.oms.entity.SoInfoEntity>
     * @Author Luo_WG
     * @Date 2023/5/11 18:16
     **/
    @Override
    public List<SoDetailEntity> listSoDetailByIds(List<String> detailIds) {
        return lambdaQuery().in(SoDetailEntity::getId, detailIds).list();
    }

    @Override
    public List<SoDetailDTO.AddDetailView> listAddDetailView(String id) {
        List<SoDetailDTO.AddDetailView> list = baseMapper.listAddDetailView(id);
        List<String> detailIds = list.stream().map(SoDetailDTO.AddDetailView::getId).collect(Collectors.toList());
        List<SoReturnDetailEntity> soReturnDetailEntities = soReturnDetailService.listDetailByMainId(id);
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockFeign.listDetailBySourceDetailId(detailIds);
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listApproveWarehouse();
        List<String> skuIdList = list.stream().map(SoDetailDTO.AddDetailView::getSkuId).distinct().collect(Collectors.toList());
        SoInfoEntity soInfoEntity = soInfoService.getById(id);
        InventoryQtyDTO.FindSkuInventoryParamDTO paramDTO = new InventoryQtyDTO.FindSkuInventoryParamDTO();
        paramDTO.setSkuIds(skuIdList);
        paramDTO.setWarehouseId(soInfoEntity.getWarehouseId());
        paramDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());
        //从wms 获取到sku 的即时库存信息
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = inventoryFeign.listSkuInventory(paramDTO);
        for (SoDetailDTO.AddDetailView addDetailView : list) {
            WarehouseDTO.UpdateDTO warehouse = warehouseList.stream().filter(req -> req.getOrgId().equals(addDetailView.getInventoryOrgId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
            addDetailView.setInventoryOrgName(warehouse.getName());
            Integer returnQty = soReturnDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(addDetailView.getId()) && ReturnTypeEnum.REPLENISHMENT.getCode().equals(req.getReturnTypeDict())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            Integer actualQty = soOutstockDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(addDetailView.getId())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            //即时库存
            Integer curInventoryQty = skuInventoryTotalList.stream().filter(s -> s.getSkuId().equals(addDetailView.getSkuId())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal())).orElse(0);
            addDetailView.setAvailableQty(getAvailableQty(curInventoryQty, addDetailView.getSalesQty()));
            addDetailView.setDeliveryQty(actualQty);
            addDetailView.setUnDeliveryQty(addDetailView.getSalesQty() + returnQty - actualQty);

        }
        return list;
    }


    /**
     * 获取订单详情数据
     *
     * @param mainId
     * @return java.util.List<com.erp.model.oms.dto.SoDetailDTO.ViewDTO>
     * @author yl
     * @date 2023-05-16 16:30
     */
    @Override
    public List<SoDetailDTO.ViewDTO> listByMainId(String mainId, String warehouseId) {
        List<SoDetailEntity> dbList = this.listBaseByMainId(mainId);
        List<SoDetailDTO.ViewDTO> resultList = BeanMapper.copyList(dbList, SoDetailDTO.ViewDTO.class);
        List<String> skuIdList = resultList.stream().map(SoDetailDTO.ViewDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        InventoryQtyDTO.FindSkuInventoryParamDTO paramDTO = new InventoryQtyDTO.FindSkuInventoryParamDTO();
        paramDTO.setSkuIds(skuIdList);
        paramDTO.setWarehouseId(warehouseId);
        paramDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());
        //从wms 获取到sku 的即时库存信息
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = inventoryFeign.listSkuInventory(paramDTO);
        List<String> detailIds = dbList.stream().map(SoDetailEntity::getId).collect(Collectors.toList());
        List<SoOutstockDetailEntity> soOutstockDetailList = soOutstockFeign.listDetailBySourceDetailId(detailIds);
        //sku的历史价格
        List<SoDetailDTO.SkuHistoryPriceDTO> skuPriceHistoryList = this.listSkuPriceHistory(skuIdList);
        for (SoDetailDTO.ViewDTO item : resultList) {
            String skuId = item.getSkuId();
            String skuName = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSkuName())).orElse("");
            item.setProductName(skuName);

            String unit = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getUnitName())).orElse("");
            item.setProductName(skuName);
            item.setUnit(unit);
            //即时库存
            Integer curInventoryQty = skuInventoryTotalList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal())).orElse(0);
            item.setCurInventoryQty(curInventoryQty);
            //销售数量
            Integer qty = item.getQty();
            /**
             * 缺货数量
             * 当可用即时库存数量小于销售数量时，
             * 缺货数量=销售数量-可用即时库存数量；
             * 当可用即时库存数量大于销售数量时，缺货数量为0
             */
            Integer scarceQty = 0;
            //即时库存是否大于 销售数量
            Boolean isGre = curInventoryQty > qty;

            /**
             * 已出库数量
             * 新增时默认为0
             * 编辑时根据关联出库单
             * 总共已发货数量同步
             *
             */
            Integer deliveryQty = soOutstockDetailList.stream().filter(req -> req.getSourceDetailId().equals(item.getId())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);

            /**
             * 剩余数量
             * 销售数量-已出库数量
             */
            Integer waitQty = qty > deliveryQty ? qty - deliveryQty : 0;
           
            item.setScarceQty(scarceQty);
            item.setAvailableQty(getAvailableQty(curInventoryQty, qty));
            item.setDeliveryQty(deliveryQty);
            item.setWaitQty(waitQty);
            //税率
            BigDecimal taxRate = item.getTaxRate();
            //单价
            BigDecimal price = item.getPrice();
            //含税单价=销售单价*（税率+1）
            BigDecimal multiplyTax = MathUtil.add(taxRate, MathUtil.BigDecimal_1);
            BigDecimal taxPrice = MathUtil.multiply(price, multiplyTax);
            item.setTaxPrice(taxPrice);
            //历史价格
            SoDetailDTO.SkuHistoryPriceDTO  skuHistoryPrice= skuPriceHistoryList.stream().
                    filter(p -> p.getSkuId().equals(skuId)).findFirst().orElse(null);
            if(skuHistoryPrice!=null){
                item.setMaxPrice(skuHistoryPrice.getMaxPrice());
                item.setMinPrice(skuHistoryPrice.getMinPrice());
                item.setAvgPrice(skuHistoryPrice.getAvgPrice());
            }

        }

        return resultList;
    }


    /**
     * 根据sku id list 获取sku 的历史价格
     *
     * @param skuIdList
     * @return java.util.List<com.erp.model.oms.dto.SoDetailDTO.SkuHistoryPriceDTO>
     * @author yl
     * @date 2023-05-17 9:21
     */
    private List<SoDetailDTO.SkuHistoryPriceDTO> listSkuPriceHistory(List<String> skuIdList) {
        if (CollectionUtils.isEmpty(skuIdList)) {
            return Collections.emptyList();
        }
        return baseMapper.listSkuPriceHistory(skuIdList);
    }


    private List<SoDetailEntity> listBaseByMainId(String mainId) {
        return this.lambdaQuery().eq(SoDetailEntity::getMainId, mainId).list();

    }

    /**
     * 添加销售订单明细
     *
     * @param mainId detailList
     * @return
     * @author yl
     * @date 2023-05-16 9:32
     */
    @Override
    public void addSoDetail(String mainId, List<SoDetailDTO.AddDTO> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<SoDetailEntity> addList = BeanMapper.copyList(detailList, SoDetailEntity.class);
        List<String> skuIdList = detailList.stream().map(SoDetailDTO.AddDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        //币种列表
        List<String> currencyList = detailList.stream().map(SoDetailDTO.AddDTO::getCurrency).collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currencyViewList = sysUserFeign.listByCurrency(currencyList);
        for (SoDetailEntity item : addList) {
            item.setMainId(mainId);
            String skuId = item.getSkuId();
            String currency = item.getCurrency();
            //是否赠品
            Boolean isGift = item.getIsGift();
            BigDecimal price = item.getPrice();
            Integer qty = item.getQty();
            //当是赠品的时候  单价为0
            if (isGift) {
                price = BigDecimal.ZERO;
            }
            //金额
            BigDecimal amount = MathUtil.multiply(price, qty);
            item.setAmount(amount);
            String skuNo = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSkuNo())).orElse("");
            item.setSkuNo(skuNo);

            String symbol = currencyViewList.stream().filter(c -> c.getId().equals(currency)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSymbol())).orElse("");
            item.setCurrencySymbol(symbol);

        }

        this.saveBatch(addList);

    }

    /**
     * 获取可用数量
     * @Author Luo_WG
     * @Date 2023/5/17 11:06
     * @param curInventoryQty 即时库存数量
     * @param salesQty 销售数量
     * @return java.lang.Integer
     **/
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

}
