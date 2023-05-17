package com.erp.server.oms.service.impl;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.dto.SoInfoDTO;
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
import com.erp.server.oms.constant.OmsConstant;
import com.erp.server.oms.mapper.SoDetailMapper;
import com.erp.server.oms.service.SoDetailService;
import com.erp.server.oms.service.SoInfoService;
import com.erp.server.oms.service.SoReturnDetailService;
import com.erp.server.oms.service.SoReturnService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
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

    /**
     * 获取tab列表数据
     *
     * @param
     * @return java.util.List<com.erp.model.oms.dto.SoInfoDTO.TabListDTO>
     * @author yl
     * @date 2023-05-17 14:03
     */
    @Override
    public List<SoInfoDTO.TabListDTO> tabList() {
        List<SoInfoDTO.TabListDTO> result = new ArrayList<>(5);
        //所有的
        List<SoDetailDTO.InfoDTO> list = baseMapper.listAllSoDetail();
        SoInfoDTO.TabListDTO all = new SoInfoDTO.TabListDTO();
        all.setCount(list.size());
        all.setSearchType(OmsConstant.ALL);
        result.add(all);

        //待审核
        String approveIngStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        SoInfoDTO.TabListDTO waitApprove = new SoInfoDTO.TabListDTO();
        waitApprove.setSearchType(OmsConstant.WAIT_APPROVE);
        int waitApproveCount = (int) list.stream().filter(s -> approveIngStatus.equals(s.getApproveStatus())).count();
        waitApprove.setCount(waitApproveCount);
        result.add(waitApprove);


        //待发货
        SoInfoDTO.TabListDTO waitDelivery = new SoInfoDTO.TabListDTO();
        waitDelivery.setSearchType(OmsConstant.WAIT_DELIVERY);
        int waitDeliveryCount = (int) list.stream().filter(s -> !s.getDeliveryStatus()).count();
        waitDelivery.setCount(waitDeliveryCount);
        result.add(waitDelivery);

        //不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        SoInfoDTO.TabListDTO reject = new SoInfoDTO.TabListDTO();
        reject.setSearchType(OmsConstant.REJECT);
        int rejectCount = (int) list.stream().filter(s -> rejectStatus.equals(s.getApproveStatus())).count();
        reject.setCount(rejectCount);
        result.add(reject);

        //已发货
        SoInfoDTO.TabListDTO delivery = new SoInfoDTO.TabListDTO();
        delivery.setSearchType(OmsConstant.DELIVERY);
        int deliveryCount = (int) list.stream().filter(s -> s.getDeliveryStatus()).count();
        delivery.setCount(deliveryCount);
        result.add(delivery);

        return result;
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
            //获取退货数量
            Integer returnQty = soReturnDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(addDetailView.getId()) && ReturnTypeEnum.REPLENISHMENT.getCode().equals(req.getReturnTypeDict())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            //获取已出库数量
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
            if (qty > curInventoryQty) {
                scarceQty = qty - curInventoryQty;
            }

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
            SoDetailDTO.SkuHistoryPriceDTO skuHistoryPrice = skuPriceHistoryList.stream().
                    filter(p -> p.getSkuId().equals(skuId)).findFirst().orElse(null);
            if (skuHistoryPrice != null) {
                item.setMaxPrice(skuHistoryPrice.getMaxPrice());
                item.setMinPrice(skuHistoryPrice.getMinPrice());
                item.setAvgPrice(skuHistoryPrice.getAvgPrice());
            }

        }

        return resultList;
    }


    /**
     * 根据搜索类型 获取到对应的明细id
     *
     * @param searchType
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-05-17 14:37
     */
    @Override
    public List<String> listParamDetailIdsBySearchType(String searchType) {
        switch (searchType) {
            case OmsConstant
                    .WAIT_APPROVE:
                //待审核
                String approveIngStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
                List<SoDetailDTO.InfoDTO> waitApproveList = baseMapper.listSoDetailByApprove(Arrays.asList(approveIngStatus));
                return waitApproveList.stream().map(SoDetailDTO.InfoDTO::getId).collect(Collectors.toList());

            case OmsConstant
                    .REJECT:
                //审核不通过
                String reject = ApproveStatusEnum.REJECT.getStatus();
                List<SoDetailDTO.InfoDTO> rejectList = baseMapper.listSoDetailByApprove(Arrays.asList(reject));
                return rejectList.stream().map(SoDetailDTO.InfoDTO::getId).collect(Collectors.toList());

            //未发货
            case OmsConstant
                    .WAIT_DELIVERY:
                List<SoDetailDTO.InfoDTO> waitDeliveryList = baseMapper.listSoDetailByDeliveryStatus(Boolean.FALSE);
                return waitDeliveryList.stream().map(SoDetailDTO.InfoDTO::getId).collect(Collectors.toList());

            //已发货
            case OmsConstant
                    .DELIVERY:
                List<SoDetailDTO.InfoDTO> deliveryList = baseMapper.listSoDetailByDeliveryStatus(Boolean.TRUE);
                return deliveryList.stream().map(SoDetailDTO.InfoDTO::getId).collect(Collectors.toList());

        }

        //特殊 标识 不要删除
        return null;

    }



    /**
     * 修改订单详情
     * @author yl
     * @date 2023-05-17 16:00
     * @param mainId
     * @param detailList
     * @return void
     */
    @Override
    public void updateSoDetail(String mainId, List<SoDetailDTO.UpdateDTO> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<SoDetailEntity> saveOrUpdateList = new ArrayList<>(detailList.size());
        //这是修改的
        List<SoDetailDTO.UpdateDTO> updateList = detailList.stream().filter(c -> StringUtils.isNotBlank(c.getId())).collect(Collectors.toList());
        //这是要添加的
        List<SoDetailDTO.UpdateDTO> addList = detailList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());
        //这个是要修改的实体
        List<SoDetailEntity> updateEntityList = BeanMapper.copyList(updateList, SoDetailEntity.class);
        //这个是要添加的
        List<SoDetailEntity> addEntityList = BeanMapper.copyList(addList, SoDetailEntity.class);
        saveOrUpdateList.addAll(updateEntityList);
        saveOrUpdateList.addAll(addEntityList);
        List<SoDetailEntity> dbList = this.listBaseByMainId(mainId);

        List<Pair<String, String>> pairList = updateList.stream().map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
        List<String> deleteIdList = getDeleteIds(pairList, dbList);
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        List<String> skuIdList = detailList.stream().map(SoDetailDTO.UpdateDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        //币种列表
        List<String> currencyList = detailList.stream().map(SoDetailDTO.UpdateDTO::getCurrency).collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currencyViewList = sysUserFeign.listByCurrency(currencyList);
        for (SoDetailEntity item : saveOrUpdateList) {
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
        this.saveOrUpdateBatch(saveOrUpdateList);
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
        List<SoDetailEntity> saveOrUpdateList = new ArrayList<>(detailList.size());
        //这是修改的
        List<SoDetailDTO.AddDTO> updateList = detailList.stream().filter(c -> StringUtils.isNotBlank(c.getId())).collect(Collectors.toList());
        //这是要添加的
        List<SoDetailDTO.AddDTO> addList = detailList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());

        //这个是要修改的实体
        List<SoDetailEntity> updateEntityList = BeanMapper.copyList(updateList, SoDetailEntity.class);

        //这个是要添加的
        List<SoDetailEntity> addEntityList = BeanMapper.copyList(addList, SoDetailEntity.class);

        saveOrUpdateList.addAll(updateEntityList);
        saveOrUpdateList.addAll(addEntityList);

        List<SoDetailEntity> dbList = this.listBaseByMainId(mainId);

        List<Pair<String, String>> pairList = updateList.stream().map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
        List<String> deleteIdList = getDeleteIds(pairList, dbList);
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        List<String> skuIdList = detailList.stream().map(SoDetailDTO.AddDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        //币种列表
        List<String> currencyList = detailList.stream().map(SoDetailDTO.AddDTO::getCurrency).collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currencyViewList = sysUserFeign.listByCurrency(currencyList);
        for (SoDetailEntity item : saveOrUpdateList) {
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
        this.saveOrUpdateBatch(saveOrUpdateList);
    }


    /**
     * 获取到删除的数据
     *
     * @param pairList
     * @param dbList
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-05-17 15:28
     */
    private List<String> getDeleteIds(List<Pair<String, String>> pairList, List<SoDetailEntity> dbList) {
        List<String> ids = pairList.stream().filter(g -> StringUtils.isNotBlank(g.getKey())).
                map(obj -> obj.getKey()).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(SoDetailEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());

    }

    /**
     * 获取可用数量
     *
     * @param curInventoryQty 即时库存数量
     * @param salesQty        销售数量
     * @return java.lang.Integer
     * @Author Luo_WG
     * @Date 2023/5/17 11:06
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
