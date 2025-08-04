package com.erp.server.oms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BillApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.RedisUtil;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.*;
import com.erp.model.dmp.dto.KingdeeDTO;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.dto.excel.SoDetailImportExcelDTO;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.oms.entity.SoReturnDetailEntity;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.tms.dto.InventorySkuCostDTO;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;
import com.erp.model.wms.entity.SoDeliveryNoticeDetailEntity;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.VirtualWarehouseRelationEntity;
import com.erp.model.wms.enums.DeliveryStatusEnum;
import com.erp.model.wms.enums.ReturnReasonEnum;
import com.erp.model.wms.enums.ReturnTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.wms.enums.inventory.VirtualInventoryBusinessTypeEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.wms.feign.*;
import com.erp.server.oms.constant.OmsConstant;
import com.erp.server.oms.listener.SoDetailExcelListener;
import com.erp.server.oms.mapper.SoDetailMapper;
import com.erp.server.oms.service.*;
import com.erp.server.oms.utils.SoUtils;
import com.google.common.collect.Lists;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
@Slf4j
public class SoDetailServiceImpl extends SuperServiceImpl<SoDetailMapper, SoDetailEntity> implements SoDetailService {

    @Resource
    private CommonService commonService;
    @Resource
    private SoInfoService soInfoService;

    @Resource
    private SoReturnDetailService soReturnDetailService;

    @Resource
    private SoOutstockFeign soOutstockFeign;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private InventoryFeign inventoryFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private SoDeliveryNoticeFeign soDeliveryNoticeFeign;
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private LogisticsFeign logisticsFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private CustomerInfoService customerInfoService;

    @Resource
    private WmsVirtualWarehouseFeign wmsVirtualWarehouseFeign;

    @Resource
    private VirtualInventoryFeign virtualInventoryFeign;

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private SkuMappingService skuMappingService;
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
        if(CollectionUtils.isEmpty(detailIds)){
            return new ArrayList<>();
        }
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
    public List<SoInfoDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<SoInfoDTO.TabListDTO> result = new ArrayList<>(5);
        //所有的
        List<SoDetailDTO.TypeCountDTO> countList = baseMapper.listApproveCount(dto.getPermissionSql());
        int allCount = countList.stream().mapToInt(SoDetailDTO.TypeCountDTO::getCount).sum();
        SoInfoDTO.TabListDTO all = new SoInfoDTO.TabListDTO();
        all.setCount(allCount);
        all.setSearchType(OmsConstant.ALL);
        result.add(all);

        // 待提交
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        SoInfoDTO.TabListDTO waitSubmit = new SoInfoDTO.TabListDTO();
        waitSubmit.setSearchType(OmsConstant.WAIT_SUBMIT);
        int waitSubmitCount = countList.stream().filter(a -> a.getType().equals(waitSubmitStatus)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        waitSubmit.setCount(waitSubmitCount);
        result.add(waitSubmit);

        //待审核
        SoInfoDTO.TabListDTO waitApprove = new SoInfoDTO.TabListDTO();
        waitApprove.setSearchType(OmsConstant.WAIT_APPROVE);
        //需要审核的业务ids
        List<String> businessIds = commonService.listProcessCurBusinessIds(SourceTypeEnum.SO_INFO.getCode());
        int waitApproveCount = 0;
        if(CollectionUtils.isNotEmpty(businessIds)){
            List<SoInfoEntity> soInfoEntityList = soInfoService.listByIds(businessIds);
            soInfoEntityList = soInfoEntityList.stream().filter(v->v.getApproveStatus().equals(BillApproveStatusEnum.APPROVE_ING)).collect(Collectors.toList());
            waitApproveCount = soInfoEntityList.size();
        }
        waitApprove.setCount(waitApproveCount);
        result.add(waitApprove);


        List<SoDetailDTO.TypeCountDTO> deliveryCountList = baseMapper.listDeliveryCount(dto.getPermissionSql());
        //待发货
        SoInfoDTO.TabListDTO waitDelivery = new SoInfoDTO.TabListDTO();
        waitDelivery.setSearchType(OmsConstant.WAIT_DELIVERY);

        //已发货
        String completeShipment = DeliveryStatusEnum.COMPLETE_SHIPMENT.getCode();

        //已审核+未发货+部分发货的
        int waitDeliveryCount = deliveryCountList.stream().filter(s -> !completeShipment.equals(s.getType())).
                mapToInt(SoDetailDTO.TypeCountDTO::getCount).sum();
        waitDelivery.setCount(waitDeliveryCount);


        result.add(waitDelivery);

        //不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        SoInfoDTO.TabListDTO reject = new SoInfoDTO.TabListDTO();
        reject.setSearchType(OmsConstant.REJECT);
        int rejectCount = countList.stream().filter(a -> a.getType().equals(rejectStatus)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        reject.setCount(rejectCount);
        result.add(reject);

        //已发货
        SoInfoDTO.TabListDTO delivery = new SoInfoDTO.TabListDTO();
        delivery.setSearchType(OmsConstant.DELIVERY);
        int deliveryCount = (int) deliveryCountList.stream().filter(s -> completeShipment.equals(s.getType())).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        delivery.setCount(deliveryCount);
        result.add(delivery);
        return result;
    }

    @Override
    public List<SoDetailDTO.AddDetailView> listAddDetailView(listAddDetailViewDTO dto) {
        List<SoDetailDTO.AddDetailView> list = baseMapper.listAddDetailView(dto);
        List<String> soIds = list.stream().map(SoDetailDTO.AddDetailView::getMainId).distinct().collect(Collectors.toList());
        List<SoReturnDetailEntity> soReturnDetailEntities = soReturnDetailService.listDetailByMainId(dto.getId());
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockFeign.listDetailBySoIds(soIds);
        List<String> skuIdList = list.stream().map(SoDetailDTO.AddDetailView::getSkuId).distinct().collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> productDetailEntitys = plmTaskFeign.getByIdList(skuIdList);
        SoInfoEntity soInfoEntity = soInfoService.getById(dto.getId());
        //从wms 获取到sku 的即时库存信息
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = listSkuInventoryTotalList(skuIdList, soInfoEntity.getWarehouseId());
        for (SoDetailDTO.AddDetailView addDetailView : list) {
            if (StringUtils.isNotBlank(addDetailView.getReturnTypeDict())) {
                addDetailView.setReturnTypeDictName(ReturnTypeEnum.getName(addDetailView.getReturnTypeDict()));
            }
            if (StringUtils.isNotBlank(addDetailView.getReturnReasonDict())) {
                addDetailView.setReturnReasonDictName(ReturnReasonEnum.getName(addDetailView.getReturnReasonDict()));
            }
            //产品sku信息
            ProductDetailEntity productDetailEntity = productDetailEntitys.stream().filter(entityClass -> entityClass.getId().equals(addDetailView.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            addDetailView.setProductName(productDetailEntity.getName());
            addDetailView.setVariantProperty(productDetailEntity.getVariantProperty());
            //获取退货数量
            Integer returnQty = soReturnDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(addDetailView.getId())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            //获取已出库数量
            Integer actualQty = soOutstockDetailEntities.stream().filter(req -> req.getSoId().equals(addDetailView.getMainId()) && req.getSkuId().equals(addDetailView.getSkuId()) && ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            //即时库存
            Integer curInventoryQty = skuInventoryTotalList.stream().filter(s -> s.getSkuId().equals(addDetailView.getSkuId())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal())).orElse(0);
            addDetailView.setAvailableQty(getAvailableQty(curInventoryQty, addDetailView.getSalesQty()));
            addDetailView.setDeliveryQty(actualQty);
            addDetailView.setCurInventoryQty(curInventoryQty);
            addDetailView.setUnDeliveryQty(addDetailView.getSalesQty() + returnQty - actualQty);
        }
        return list;
    }


    /**
     * 获取到sku 的即时库存
     *
     * @param skuIdList
     * @param warehouseId
     * @return java.util.List<com.erp.model.wms.dto.inventory.InventoryQtyDTO.SkuInventoryTotalDTO>
     * @author yl
     * @date 2023-05-24 18:59
     */
    private List<InventoryQtyDTO.SkuInventoryTotalDTO> listSkuInventoryTotalList(List<String> skuIdList, String warehouseId) {
        if (CollectionUtils.isEmpty(skuIdList)) {
            return new ArrayList<>();
        }
        if(StringUtils.isBlank(warehouseId)){
            throw new ServiceException("仓库不能为空");
        }
        InventoryQtyDTO.FindSkuInventoryParamDTO paramDTO = new InventoryQtyDTO.FindSkuInventoryParamDTO();
        paramDTO.setSkuIds(skuIdList);
        paramDTO.setWarehouseId(warehouseId);
        paramDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());
        //从wms 获取到sku 的即时库存信息
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = inventoryFeign.listSkuInventory(paramDTO);
        return skuInventoryTotalList;
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
        SoInfoEntity soInfoEntity = soInfoService.getById(mainId);
        if(null == soInfoEntity){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "销售订单");
        }
        List<SoDetailEntity> dbList = this.listBaseByMainId(mainId);
        List<SoDetailDTO.ViewDTO> resultList = BeanMapper.copyList(dbList, SoDetailDTO.ViewDTO.class);
        List<String> skuIdList = resultList.stream().map(SoDetailDTO.ViewDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);
        //从wms 获取到sku 的即时库存信息
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = new ArrayList<>();
        if (StringUtils.isNotBlank(warehouseId) && CollectionUtils.isNotEmpty(skuIdList)) {
            skuInventoryTotalList = listSkuInventoryTotalList(skuIdList, warehouseId);
        }

        List<String> detailIds = dbList.stream().map(SoDetailEntity::getId).collect(Collectors.toList());
        List<SoOutstockDetailDTO.DeliveryQtyDTO> soOutstockDetailList = soOutstockFeign.listDetailBySoDetailIds(detailIds);
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        soOutstockDetailList = soOutstockDetailList.stream().filter(s -> s.getApproveStatus().equals(approveStatus)).collect(Collectors.toList());
        //sku的历史价格
        List<SoDetailDTO.SkuHistoryPriceDTO> skuPriceHistoryList = this.listSkuPriceHistory(skuIdList);

        //查询虚拟库存
        List<VirtualInventoryDTO.VirtualInventoryQtyDTO> virtualInventoryQtyList = handleVirtualInventory(soInfoEntity, skuIdList);

        //发货通知单的
        List<SoDeliveryNoticeDetailDTO.ListDTO> soDeliveryNoticeList = soDeliveryNoticeFeign.listBySourceIdList(Arrays.asList(mainId));
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
             * 当可用即时库存数量小于销售数量时， 缺货数量=可用即时库存数量-(销售数量-发货通知单数量)；
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
            Integer deliveryQty = soOutstockDetailList.stream().filter(req -> req.getSoDetailId().equals(item.getId())).
                    map(SoOutstockDetailDTO.DeliveryQtyDTO::getActualQty).reduce(MathUtil.ZERO, Integer::sum);

            /**
             * 剩余数量
             * 销售数量-已出库数量
             */
            Integer waitQty = qty > deliveryQty ? qty - deliveryQty : 0;
            if (qty > curInventoryQty) {
                // 当可用即时库存数量小于销售数量时， 缺货数量=可用即时库存数量-(销售数量-发货通知单数量)；
                Integer deliveryNoticeQty = soDeliveryNoticeList.stream().filter(f -> f.getDetailId().equals(item.getId())).
                        mapToInt(SoDeliveryNoticeDetailDTO.ListDTO::getDeliveryQty).sum();
                scarceQty = curInventoryQty - (qty - deliveryNoticeQty);
                scarceQty = scarceQty > 0 ? 0 : Math.abs(scarceQty);
            }

            item.setScarceQty(scarceQty);
            item.setAvailableQty(getAvailableQty(curInventoryQty, qty));
            item.setDeliveryQty(deliveryQty);
            item.setWaitQty(waitQty);

            //虚拟可用库存
            Integer virtualUsableQty = virtualInventoryQtyList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), skuId) && CharSequenceUtil.equals(obj.getDictInventoryStatus(),InventoryStatusEnum.USABLE.getCode())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getInventoryQty())).orElse(MathUtil.ZERO);
            item.setVirtualAvailableQty(MathUtil.compareTo(virtualUsableQty,qty) > MathUtil.ZERO ? qty : virtualUsableQty);
            item.setVirtualUsableQty(virtualUsableQty);
            //虚拟冻结库存
            Integer virtualFrozenQty = virtualInventoryQtyList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), skuId) && CharSequenceUtil.equals(obj.getDictInventoryStatus(),InventoryStatusEnum.FROZEN.getCode())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getInventoryQty())).orElse(MathUtil.ZERO);
            item.setVirtualFrozenQty(virtualFrozenQty);

            //虚拟缺货数量(显示正数)
            Integer virtualScarceQty =  MathUtil.compareTo(virtualUsableQty,qty) > MathUtil.ZERO ? MathUtil.ZERO : qty - virtualUsableQty;
            item.setVirtualScarceQty(virtualScarceQty);
            //单价
            BigDecimal price = item.getPrice();
            //汇率
            BigDecimal exchangeRate = item.getExchangeRate();
            if (Objects.isNull(exchangeRate)) {
                exchangeRate = MathUtil.BigDecimal_1;
            }
            //销售单价(本位币)
            item.setPriceLc(MathUtil.multiplyWithTwo(price, exchangeRate,4));
            //含税单价
            BigDecimal taxPrice = item.getTaxPrice();
            item.setTaxPrice(taxPrice);
            //含税单价(本位币)
            item.setTaxPriceLc(MathUtil.multiplyWithTwo(taxPrice, exchangeRate,4));

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

    @Override
    public List<SoDetailEntity> listSoDetailByMainIds(List<String> ids) {
        if (CollUtil.isEmpty(ids)){
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(SoDetailEntity::getMainId, ids).list();
    }

    @Override
    public List<SoDetailEntity> listSoDetailByMainId(String id) {
        LambdaQueryWrapper<SoDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SoDetailEntity::getMainId, id);
        queryWrapper.eq(SoDetailEntity::getIsDeleted, false);
        return baseMapper.selectList(queryWrapper);
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
            case OmsConstant.WAIT_SUBMIT:
                // 待提交
                String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
                List<SoDetailDTO.InfoDTO> waitSubmitList = baseMapper.listSoDetailByApprove(Arrays.asList(waitSubmitStatus));
                return waitSubmitList.stream().map(SoDetailDTO.InfoDTO::getMainId).distinct().collect(Collectors.toList());
            case OmsConstant
                    .WAIT_APPROVE:
                //待审核
                String approveIngStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
                List<SoDetailDTO.InfoDTO> waitApproveList = baseMapper.listSoDetailByApprove(Arrays.asList(approveIngStatus));
                return waitApproveList.stream().map(SoDetailDTO.InfoDTO::getMainId).distinct().collect(Collectors.toList());

            case OmsConstant
                    .REJECT:
                //审核不通过
                String reject = ApproveStatusEnum.REJECT.getStatus();
                List<SoDetailDTO.InfoDTO> rejectList = baseMapper.listSoDetailByApprove(Arrays.asList(reject));
                return rejectList.stream().map(SoDetailDTO.InfoDTO::getMainId).distinct().collect(Collectors.toList());

            //未发货
            case OmsConstant
                    .WAIT_DELIVERY:

                String unShipped = DeliveryStatusEnum.UN_SHIPPED.getCode();
                String partialShipment = DeliveryStatusEnum.PARTIAL_SHIPMENT.getCode();
                List<String> deliveryStatusList = Arrays.asList(unShipped, partialShipment);
                List<SoDetailDTO.InfoDTO> waitDeliveryList = baseMapper.listSoDetailByDeliveryStatus(deliveryStatusList);
                return waitDeliveryList.stream().map(SoDetailDTO.InfoDTO::getMainId).distinct().collect(Collectors.toList());

            //已发货
            case OmsConstant
                    .DELIVERY:
                String completeShipment = DeliveryStatusEnum.COMPLETE_SHIPMENT.getCode();
                List<String> deliveryStatus = Arrays.asList(completeShipment);
                List<SoDetailDTO.InfoDTO> deliveryList = baseMapper.listSoDetailByDeliveryStatus(deliveryStatus);
                return deliveryList.stream().map(SoDetailDTO.InfoDTO::getMainId).distinct().collect(Collectors.toList());

        }

        //特殊 标识 不要删除
        return null;

    }


    /**
     * 修改订单详情
     *
     * @param mainId
     * @param detailList
     * @return void
     * @author yl
     * @date 2023-05-17 16:00
     */
    @Override
    public void updateSoDetail(String mainId, Boolean isTax, List<SoDetailDTO.UpdateDTO> detailList, SoInfoEntity oldEntity) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        //这是修改的
        List<SoDetailDTO.UpdateDTO> updateList = detailList.stream().filter(c -> StringUtils.isNotBlank(c.getId())).collect(Collectors.toList());
        List<SoDetailEntity> saveOrUpdateList = BeanMapper.copyList(detailList, SoDetailEntity.class);
        List<SoDetailEntity> dbList = this.listBaseByMainId(mainId);

        List<Pair<String, String>> pairList = updateList.stream().map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
        List<String> deleteIdList = getDeleteIds(pairList, dbList);
        List<SoDetailEntity> removeList = dbList.stream().filter(r -> deleteIdList.contains(r.getId())).collect(Collectors.toList());

        SoInfoEntity soInfoEntity = soInfoService.getById(mainId);
        //校验更新的明细和删除的明细是否冻结库存下推了发货通知
        checkSoDetailQty(dbList,updateList,removeList,soInfoEntity,oldEntity);

        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        List<String> skuIdList = detailList.stream().map(SoDetailDTO.UpdateDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuCostByIds(skuIdList);
        //重置sku含税成本
        resetSkuVo(skuIdList,skuList,soInfoEntity);
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIdList);
        //币种列表
        List<String> currencyList = detailList.stream().map(SoDetailDTO.UpdateDTO::getCurrency).collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currencyViewList = sysUserFeign.listByCurrency(currencyList);
        for (SoDetailEntity item : saveOrUpdateList) {
            item.setMainId(mainId);
            String skuId = item.getSkuId();
            String currency = item.getCurrency();
            String skuNo = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSkuNo())).orElse("");
            item.setSkuNo(skuNo);
            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(item.getSkuId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                item.setBomVersion(sonSkuList.get(MathUtil.ZERO).getBomVersion());
            }
            String symbol = currencyViewList.stream().filter(c -> c.getId().equals(currency)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSymbol())).orElse("");
            item.setCurrencySymbol(symbol);
        }
        // 金额信息加上折扣额计算
        SoUtils.handleDetailAmount(isTax, soInfoEntity.getDiscountAmount(), saveOrUpdateList);
        for (SoDetailEntity item : saveOrUpdateList) {
            // 计算毛利成本
            calCost(skuList, soInfoEntity.getBillDate(), item, Boolean.FALSE);
        }
        //这是删除
        List<Pair<String, String>> removePairList = removeList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("删除了一个销售产品【%s】", ModuleTypeEnum.SO.getCode(), removePairList, "编辑操作");

        //这是添加
        List<Pair<String, String>> addPairList = saveOrUpdateList.stream().filter(s -> StringUtils.isBlank(s.getId())).map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("添加了一个销售产品【%s】", ModuleTypeEnum.SO.getCode(), addPairList, "编辑操作");
        //修改的
        //这个是要修改的实体
        List<SoDetailEntity> updateEntityList = saveOrUpdateList.stream().filter(s -> StringUtils.isNotBlank(s.getId())).collect(Collectors.toList());
        for (SoDetailEntity update : updateEntityList) {
            String id = update.getId();
            SoDetailEntity old = dbList.stream().filter(d -> d.getId().equals(id)).findFirst().orElse(null);
            if (old != null) {
                operateLogService.addModuleOperateLogByObj(old, update, ModuleTypeEnum.SO.getCode(), mainId, "", "");
            }
        }
        BigDecimal allAmountLc = saveOrUpdateList.stream().map(SoDetailEntity::getAllAmountLocalCurrency).reduce(BigDecimal.ZERO, BigDecimal::add);
        soInfoEntity.setAllAmountLc(allAmountLc);
        soInfoService.updateById(soInfoEntity);
        this.saveOrUpdateBatch(saveOrUpdateList);
    }

    private void resetSkuVo(List<String> skuIdList, List<SkuVO> skuList, SoInfoEntity soInfoEntity) {
        LocalDate billDate = soInfoEntity.getBillDate();
        if (Objects.isNull(billDate)){
            return;
        }
        InventorySkuCostDTO.QueryB2BDTO queryB2BDTO = InventorySkuCostDTO.QueryB2BDTO.builder().skuIds(skuIdList)
                .salesOrgId(soInfoEntity.getSalesOrgId()).warehouseId(soInfoEntity.getWarehouseId()).billDate(billDate).build();
        //根据sku获取 人民币材料成本
        List<InventorySkuCostDTO.SkuCostDTO> skuCostDTOS = logisticsFeign.listSkuCostBySkuIds(queryB2BDTO);

        //重置sku采购单价
        for (SkuVO skuVO : skuList){
            if (CollUtil.isEmpty(skuCostDTOS)){
                skuVO.setCostSource("采购平均成本");
                continue;
            }
            InventorySkuCostDTO.SkuCostDTO skuCostDTO = skuCostDTOS.stream().filter(e -> Objects.equals(skuVO.getSkuId(), e.getSkuId())).findFirst().orElse(null);
            if (Objects.isNull(skuCostDTO)){
                skuVO.setCostSource("采购平均成本");
                continue;
            }
            BigDecimal rate = dmpTaskFeign.getRate(billDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), skuCostDTO.getCurrency());
            skuVO.setNotTaxCostPrice(MathUtil.multiplyWithTwo(rate,skuCostDTO.getProductCost(),4));
            skuVO.setCostSource(skuCostDTO.getAllocatedMonth().format(DateTimeFormatter.ofPattern("yyyy-MM")) + "财务导入成本");
        }
    }


    /**
     * 根据主表ids 删除数据
     *
     * @param mainIdList
     * @return void
     * @author yl
     * @date 2023-05-17 17:09
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByMainIdList(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return;
        }
        LambdaQueryWrapper<SoDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SoDetailEntity::getMainId, mainIdList);
        this.remove(queryWrapper);

    }


    /**
     * 下载模板
     *
     * @param response
     * @return void
     * @author yl
     * @date 2023-05-17 19:25
     */
    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "excel/soSku.xlsx";
        String excelName = "template.xlsx";
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            log.error("下载模板出错了==={}", e);
            throw new ServiceException(ApiError.DEFAULT);
        }

    }


    /**
     * 导入sku
     *
     * @param excelFile
     * @param response
     * @return com.erp.model.oms.dto.SoDetailDTO.ImportDTO
     * @author yl
     * @date 2023-05-17 19:43
     */
    @Override
    public SoDetailDTO.ImportDTO importSku(MultipartFile excelFile, HttpServletResponse response, String warehouseId,Boolean isTax,String customerId) {
        List<SkuVO> skuList = plmTaskFeign.listApproveSku();
        ListingInfoDTO.QueryDTO queryDTO = new ListingInfoDTO.QueryDTO();
        queryDTO.setAuthId(customerId);
        List<SkuMappingDTO.SkuMappingViewDTO> skuMappingViewDTOS = skuMappingService.listSkuMappingByParams(queryDTO);
        SoDetailExcelListener excelListenerUtil = new SoDetailExcelListener(skuList,skuMappingViewDTOS,isTax);

        try {
            EasyExcel.read(excelFile.getInputStream(), SoDetailImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (Exception e) {
            log.error("导入错误=={}", e);
            throw new ServiceException(ApiError.ERROR_95124);
        }
        SoDetailDTO.ImportDTO result = new SoDetailDTO.ImportDTO();
        List<SoDetailDTO.SkuDTO> successList = excelListenerUtil.getSuccessList();
        List<String> skuIdList = successList.stream().map(SoDetailDTO.SkuDTO::getSkuId).collect(Collectors.toList());
        //sku的历史价格
        List<SoDetailDTO.SkuHistoryPriceDTO> skuPriceHistoryList = this.listSkuPriceHistory(skuIdList);
        //从wms 获取到sku 的即时库存信息
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = CollectionUtils.isNotEmpty(skuIdList) ? listSkuInventoryTotalList(skuIdList, warehouseId) : Collections.emptyList();
        for (SoDetailDTO.SkuDTO item : successList) {
            String skuId = item.getSkuId();
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
            Integer deliveryQty = 0;
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
            if(Objects.isNull(item.getTaxRate())){
                item.setTaxRate(BigDecimal.ZERO);
            }
            BigDecimal taxRate = item.getTaxRate();
            BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);
            //单价
            BigDecimal price = item.getPrice();
            item.setAmount(MathUtil.multiplyWithTwo(price, qty));
            //含税单价=销售单价*（税率+1）
            BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
            BigDecimal taxPrice = MathUtil.multiplyWithTwo(price, multiplyTax);

            item.setTaxPrice(taxPrice);
            //价税金额
            BigDecimal taxAmount = MathUtil.multiplyWithTwo(taxPrice, qty);
            item.setTaxAmount(taxAmount);

            //历史价格
            SoDetailDTO.SkuHistoryPriceDTO skuHistoryPrice = skuPriceHistoryList.stream().
                    filter(p -> p.getSkuId().equals(skuId)).findFirst().orElse(null);
            if (skuHistoryPrice != null) {
                item.setMaxPrice(skuHistoryPrice.getMaxPrice());
                item.setMinPrice(skuHistoryPrice.getMinPrice());
                item.setAvgPrice(skuHistoryPrice.getAvgPrice());
            } else {
                item.setMaxPrice(price);
                item.setMinPrice(price);
                item.setAvgPrice(price);
            }
            if(isTax){
                if(taxRate.compareTo(BigDecimal.ZERO) <= 0){
                    throw new ServiceException("是否含税选择为是，税率必须大于0");
                }
            }else{
                if(taxRate.compareTo(BigDecimal.ZERO) > 0){
                    throw new ServiceException("是否含税选择为否，税率不能大于0");
                }
            }
        }
        result.setSuccessList(successList);
        //导出错误数据
        List<SoDetailImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "销售订单错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, SoDetailImportExcelDTO.class);
            if (!file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        result.setErrorUrl(url);

        return result;
    }


    /**
     * 获取sku 详情
     *
     * @param skuNo
     * @return com.erp.model.oms.dto.SoDetailDTO.SkuDTO
     * @author yl
     * @date 2023-05-18 14:43
     */
    @Override
    public SoDetailDTO.SkuDTO getSkuInfoBySkuNo(String skuNo, String warehouseId) {
        if (StringUtils.isEmpty(warehouseId)) {
            throw new ServiceException(ApiError.ERROR_99001);
        }
        SoDetailDTO.SkuDTO result = new SoDetailDTO.SkuDTO();
        List<String> skuIdList = new ArrayList<>(1);
        Map<String, String> param = new HashMap<>();
        param.put("id", "");
        param.put("skuNo", skuNo);
        ProductDetailDTO sku = plmTaskFeign.getSkuByParam(param);
        if (Objects.isNull(sku)) {
            throw new ServiceException(ApiError.ERROR_95107);
        }
        String skuId = sku.getId();
        skuIdList.add(skuId);
        //sku的历史价格
        List<SoDetailDTO.SkuHistoryPriceDTO> skuPriceHistoryList = this.listSkuPriceHistory(skuIdList);
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = listSkuInventoryTotalList(skuIdList, warehouseId);

        String skuName = sku.getName();
        result.setProductName(skuName);
        result.setSkuId(skuId);
        result.setQty(0);
        result.setProductName(skuName);
        result.setUnit(sku.getUnitName());
        result.setSkuNo(skuNo);

        //即时库存
        Integer curInventoryQty = skuInventoryTotalList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal())).orElse(0);
        result.setCurInventoryQty(curInventoryQty);
        //销售数量
        Integer qty = 0;
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
        Integer deliveryQty = 0;

        /**
         * 剩余数量
         * 销售数量-已出库数量
         */
        Integer waitQty = qty > deliveryQty ? qty - deliveryQty : 0;
        if (qty > curInventoryQty) {
            scarceQty = qty - curInventoryQty;
        }

        result.setScarceQty(scarceQty);
        result.setAvailableQty(getAvailableQty(curInventoryQty, qty));
        result.setDeliveryQty(deliveryQty);
        result.setWaitQty(waitQty);
        //税率
        BigDecimal taxRate = BigDecimal.ZERO;
        BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);

        //单价
        BigDecimal price = BigDecimal.ZERO;

        //含税单价=销售单价*（税率+1）
        BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
        BigDecimal taxPrice = MathUtil.multiplyWithTwo(price, multiplyTax);
        result.setTaxPrice(taxPrice);
        result.setPrice(price);
        result.setAmount(MathUtil.multiplyWithTwo(price, qty));
        result.setTaxRate(taxRate);
        result.setIsGift(Boolean.FALSE);
        result.setIsReissue(Boolean.FALSE);
        result.setIsClose(Boolean.FALSE);
        result.setTaxAmount(MathUtil.multiplyWithTwo(taxPrice, qty));
        result.setRemark("");
        //历史价格
        SoDetailDTO.SkuHistoryPriceDTO skuHistoryPrice = skuPriceHistoryList.stream().
                filter(p -> p.getSkuId().equals(skuId)).findFirst().orElse(null);
        if (skuHistoryPrice != null) {
            result.setMaxPrice(skuHistoryPrice.getMaxPrice());
            result.setMinPrice(skuHistoryPrice.getMinPrice());
            result.setAvgPrice(skuHistoryPrice.getAvgPrice());
        } else {
            result.setMaxPrice(price);
            result.setMinPrice(price);
            result.setAvgPrice(price);
        }
        return result;
    }


    /**
     * 根据主表id 获取合同信息
     *
     * @param mainId
     * @return java.util.List<com.erp.model.oms.dto.SoDetailDTO.ExportPdfDTO>
     * @author yl
     * @date 2023-05-18 15:53
     */
    @Override
    public List<SoDetailDTO.ExportPdfDTO> listExportPdf(String mainId) {
        List<SoDetailEntity> dbList = this.listBaseByMainId(mainId);
        if (CollectionUtils.isEmpty(dbList)) {
            return Collections.emptyList();
        }
        List<String> skuIdList = dbList.stream().map(SoDetailEntity::getSkuId).collect(Collectors.toList());

        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);

        List<SoDetailDTO.ExportPdfDTO> resultList = new ArrayList<>(dbList.size());
        for (SoDetailEntity item : dbList) {
            SoDetailDTO.ExportPdfDTO result = new SoDetailDTO.ExportPdfDTO();
            result.setCurrency(item.getCurrency());
            result.setCurrencySymbol(item.getCurrencySymbol());
            result.setSkuNo(item.getSkuNo());
            String skuId = item.getSkuId();
            result.setSkuId(skuId);
            Integer qty = item.getQty();
            result.setQty(qty);

            BigDecimal taxRate = item.getTaxRate();
            BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);

            result.setTaxRate(taxRate);
            //单价
            BigDecimal price = item.getPrice();
            result.setPrice(price);
            //含税单价=销售单价*（税率+1）
            BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
            BigDecimal taxPrice = MathUtil.multiplyWithTwo(price, multiplyTax);
            result.setTaxPrice(taxPrice);
            SkuVO skuVO = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).
                    findFirst().orElse(null);
            result.setDeclareModel("");
            result.setAmount(MathUtil.multiplyWithTwo(item.getPrice(),item.getQty()));
            result.setTaxAmount(item.getTaxAmount());
            if (skuVO != null) {
                result.setProductName(skuVO.getSkuName());
                result.setUnit(skuVO.getUnitName());
                result.setDeclareModel(skuVO.getSpuNo());
            } else {
                result.setProductName("");
                result.setUnit("");
                result.setDeclareModel("");
            }
            resultList.add(result);
        }
        return resultList;
    }

    @Override
    public List<SoDetailDTO.ViewDTO> listBySoId(String soId) {
        SoInfoEntity soInfo = soInfoService.getById(soId);
        if (Objects.isNull(soInfo)) {
            throw new ServiceException(ApiError.ERROR_92016);
        }
        String warehouseId = soInfo.getWarehouseId();

        //发货通知单的
        List<SoDeliveryNoticeDetailDTO.ListDTO> soDeliveryNoticeList = soDeliveryNoticeFeign.listBySourceIdList(Arrays.asList(soId));

        List<SoDetailEntity> dbList = this.listBaseByMainId(soId);
        //获取未关闭的数据
        dbList = dbList.stream().filter(s -> s.getIsClose()).collect(Collectors.toList());
        List<SoDetailDTO.ViewDTO> resultList = BeanMapper.copyList(dbList, SoDetailDTO.ViewDTO.class);
        List<String> skuIdList = resultList.stream().map(SoDetailDTO.ViewDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);
        List<String> detailIds = dbList.stream().map(SoDetailEntity::getId).collect(Collectors.toList());
        List<SoOutstockDetailEntity> soOutstockDetailList = soOutstockFeign.listDetailBySourceDetailId(detailIds);
        //从wms 获取到sku 的即时库存信息
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = listSkuInventoryTotalList(skuIdList, warehouseId);

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
             * 当可用即时库存数量小于销售数量时， 缺货数量=可用即时库存数量-(销售数量-发货通知单数量)；
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
                Integer deliveryNoticeQty = soDeliveryNoticeList.stream().filter(f -> f.getSourceDetailId().equals(item.getId())).
                        mapToInt(SoDeliveryNoticeDetailDTO.ListDTO::getDeliveryQty).sum();
                scarceQty = curInventoryQty - (qty - deliveryNoticeQty);
                scarceQty = scarceQty > 0 ? 0 : Math.abs(scarceQty);

            }

            item.setScarceQty(scarceQty);
            item.setAvailableQty(getAvailableQty(curInventoryQty, qty));
            item.setDeliveryQty(deliveryQty);
            item.setWaitQty(waitQty);
            //税率
            BigDecimal taxRate = item.getTaxRate();
            BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);
            //单价
            BigDecimal price = item.getPrice();
            //含税单价=销售单价*（税率+1）
            BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
            BigDecimal taxPrice = MathUtil.multiplyWithTwo(price, multiplyTax);
            item.setTaxPrice(taxPrice);

        }
        return resultList;
    }


    /**
     * 更改发货状态
     *
     * @param paramList
     * @return void
     * @author yl
     * @date 2023-05-23 10:26
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDeliveryStatus(List<SoDetailDTO.UpdateDeliveryStatusDTO> paramList) {
        if (CollectionUtils.isEmpty(paramList)) {
            return;
        }
        List<String> idList = paramList.stream().map(SoDetailDTO.UpdateDeliveryStatusDTO::getId).collect(Collectors.toList());
        List<SoDetailEntity> soDetailList = this.listByIds(idList);
        for (SoDetailEntity item : soDetailList) {
            String id = item.getId();
            Integer qty = item.getQty();
            //已发货数据
            Integer alreadyDeliverQty = item.getDeliveryQty();
            //本次发货数量
            Integer deliveryQty = paramList.stream().filter(p -> p.getId().equals(id)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getDeliveryQty())).orElse(0);
            //最终的
            Integer finalDeliverQty = alreadyDeliverQty + deliveryQty;
            if (finalDeliverQty >= qty) {
                item.setDeliveryQty(qty);
            } else {
                item.setDeliveryQty(finalDeliverQty);
            }
            if (finalDeliverQty < 0) {
                item.setDeliveryQty(0);
            }
            if (finalDeliverQty >= qty) {
                item.setDeliveryStatus(DeliveryStatusEnum.COMPLETE_SHIPMENT.getCode());
            }
            if (finalDeliverQty < qty && finalDeliverQty >= 0) {
                item.setDeliveryStatus(DeliveryStatusEnum.PARTIAL_SHIPMENT.getCode());
            }
            if (finalDeliverQty <= 0) {
                item.setDeliveryStatus(DeliveryStatusEnum.UN_SHIPPED.getCode());
            }
        }
        this.updateBatchById(soDetailList);
    }




    /**
     * 检查sku 数量是否够用
     *
     * @param warehouseId
     * @param detailList
     * @return void
     * @author yl
     * @date 2023-05-24 18:42
     */
    @Override
    public String checkSkuQty(String warehouseId, List<SoDetailDTO.AddDTO> detailList) {
        StringBuffer errMsg = new StringBuffer("");
        // 忽略库存计算SKU
        List<SkuVO> ignoreInventorySkuList = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = Lists.newArrayList();
        if (CollUtil.isNotEmpty(ignoreInventorySkuList)) {
            ignoreInventorySkuIds = ignoreInventorySkuList.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList());
        }
        if (CollectionUtils.isNotEmpty(detailList)) {
            //即时库存
            List<String> skuIdList = detailList.stream().map(SoDetailDTO.AddDTO::getSkuId).collect(Collectors.toList());
            List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);
            List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = listSkuInventoryTotalList(skuIdList, warehouseId);

            //仓库
            List<WarehouseDTO.UpdateDTO> updateDTOS = wmsTaskFeign.listWarehouseByIds(Arrays.asList(warehouseId));

            for (SoDetailDTO.AddDTO item : detailList) {
                int qty = item.getQty();
                String skuId = item.getSkuId();
                //即时库存
                Integer curInventoryQty = skuInventoryTotalList.stream().filter(s -> s.getSkuId().equals(skuId)).
                        mapToInt(InventoryQtyDTO.SkuInventoryTotalDTO::getInventoryTotal).sum();
                if (qty > curInventoryQty && !ignoreInventorySkuIds.contains(skuId)) {
                    String skuNo = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().
                            flatMap(obj -> Optional.ofNullable(obj.getSkuNo())).orElse("");
                    String msg =  CharSequenceUtil.format("仓库【{}】SKU【{}】【缺货：{}个】", updateDTOS.get(0).getName(), skuNo, (qty - curInventoryQty));
                    errMsg.append(msg).append("</br>");
                }
            }
        }
        return errMsg.toString();
    }


    /**
     * 获取到对应销售订单的详情
     *
     * @param soId
     * @param soDetailIds
     * @param hasContain  是否包含 true 包含
     * @return java.util.List<com.erp.model.oms.entity.SoDetailEntity>
     * @author yl
     * @date 2023-05-26 10:16
     */
    @Override
    public List<SoDetailEntity> listDetailBySoId(String soId, List<String> soDetailIds, Boolean hasContain) {
        LambdaQueryWrapper<SoDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SoDetailEntity::getMainId, soId);
        if (CollectionUtils.isNotEmpty(soDetailIds) && hasContain != null && hasContain) {
            queryWrapper.in(SoDetailEntity::getId, soDetailIds);
        }
        return this.list(queryWrapper);
    }

    /**
     * 按照顺序排序
     *
     * @param soDetailIdList
     * @return java.util.List<com.erp.model.oms.entity.SoDetailEntity>
     * @author yl
     * @date 2023-06-07 10:32
     */
    @Override
    public List<SoDetailEntity> listByIdsSeq(List<String> soDetailIdList) {
        if (CollectionUtils.isEmpty(soDetailIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(SoDetailEntity::getId, soDetailIdList).orderByDesc(SoDetailEntity::getId).list();
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
        String status = ApproveStatusEnum.APPROVE.getStatus();
        return baseMapper.listSkuPriceHistory(skuIdList, status);
    }


    @Override
    public List<SoDetailEntity> listBaseByMainId(String mainId) {
        return this.lambdaQuery().eq(SoDetailEntity::getMainId, mainId).orderByAsc(SoDetailEntity::getId).list();

    }

    @Override
    public List<SoDetailEntity> listBaseByMainIdList(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(SoDetailEntity::getMainId, mainIdList).list();
    }

    /**
     * 添加销售订单明细
     *
     * @param mainId detailList
     * @param isTax  是否含税  true 是
     * @return
     * @author yl
     * @date 2023-05-16 9:32
     */
    @Override
    public void addSoDetail(String mainId, Boolean isTax, List<SoDetailDTO.AddDTO> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        //这是修改的
        List<SoDetailDTO.AddDTO> updateList = detailList.stream().filter(c -> StringUtils.isNotBlank(c.getId())).collect(Collectors.toList());

        List<SoDetailEntity> saveOrUpdateList = BeanMapper.copyList(detailList, SoDetailEntity.class);

        List<SoDetailEntity> dbList = this.listBaseByMainId(mainId);
        SoInfoEntity soInfoEntity = soInfoService.getById(mainId);
        List<Pair<String, String>> pairList = updateList.stream().map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
        List<String> deleteIdList = getDeleteIds(pairList, dbList);
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        List<String> skuIdList = detailList.stream().map(SoDetailDTO.AddDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuCostByIds(skuIdList);
        resetSkuVo(skuIdList,skuList,soInfoEntity);
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIdList);
        //币种列表
        List<String> currencyList = detailList.stream().map(SoDetailDTO.AddDTO::getCurrency).collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currencyViewList = sysUserFeign.listByCurrency(currencyList);
        for (int i = 0; i < saveOrUpdateList.size(); i++) {
            SoDetailEntity item = saveOrUpdateList.get(i);
            item.setMainId(mainId);
            String skuId = item.getSkuId();
            String currency = item.getCurrency();
            String skuNo = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSkuNo())).orElse("");
            item.setSkuNo(skuNo);
            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(item.getSkuId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                item.setBomVersion(sonSkuList.get(MathUtil.ZERO).getBomVersion());
            }
            String symbol = currencyViewList.stream().filter(c -> c.getId().equals(currency)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSymbol())).orElse("");
            item.setCurrencySymbol(symbol);
        }
        // 金额折扣处理
        SoUtils.handleDetailAmount(isTax, soInfoEntity.getDiscountAmount(), saveOrUpdateList);
        for (int i = 0; i < saveOrUpdateList.size(); i++) {
            SoDetailEntity item = saveOrUpdateList.get(i);
            // 计算毛利成本
            calCost(skuList, soInfoEntity.getBillDate(), item, Boolean.FALSE);
        }
        BigDecimal allAmountLc = saveOrUpdateList.stream().map(SoDetailEntity::getAllAmountLocalCurrency).reduce(BigDecimal.ZERO, BigDecimal::add);
        soInfoEntity.setAllAmountLc(allAmountLc);
        soInfoService.updateById(soInfoEntity);
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

    /**
     * 计算毛利成本
     *
     * @param skuList
     * @param saleOrderBillDate
     * @param isBrush
     * @param item
     */
    @Override
    public void calCost(List<SkuVO> skuList, LocalDate saleOrderBillDate, SoDetailEntity item, Boolean isBrush) {
        String skuId = item.getSkuId();
        // sku对应的一级供应商
        SkuVO skuVO = skuList.stream().filter(r -> Objects.equals(r.getSkuId(), skuId)).findFirst().orElse(null);
        log.warn("SKU编号【{}】对应的一级供应商id：【{}】", item.getSkuNo(), Objects.isNull(skuVO)? "":skuVO.getSupplierId());
        BigDecimal purchasePrice = BigDecimal.ZERO;
        String currency = CurrencyEnum.CNY.getCurrencyCode();
        if (Objects.nonNull(skuVO) && Objects.nonNull(skuVO.getNotTaxCostPrice())) {
            purchasePrice = skuVO.getNotTaxCostPrice();
        }
        item.setCostSource(Objects.isNull(skuVO) ? "" : skuVO.getCostSource());
        // 销售金额转换
        BigDecimal saleAmount = item.getAmount();
        // 价税合计（折后）转换
        BigDecimal taxAmount = item.getTaxAmount();
        if (Objects.equals(item.getCurrency(), "CNY")) {
            item.setExchangeRate(BigDecimal.ONE);
        }
        item.setAmountLocalCurrency(saleAmount);
        item.setAllAmountLocalCurrency(taxAmount);


        if (Objects.nonNull(saleAmount) &&
                saleAmount.compareTo(BigDecimal.ZERO) >= 0 &&
                !Objects.equals(item.getCurrency(), "CNY")) {
            LocalDate calDate = LocalDate.now();
            if (Objects.equals(isBrush, Boolean.TRUE)) {
                calDate = item.getCreateTime().toLocalDate();
            }
            String currentDate = calDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            BigDecimal rate = dmpTaskFeign.getRate(currentDate, item.getCurrency());
            log.info("提交的币制：{}，转换后汇率：{}", item.getCurrency(), rate);
            if (Objects.isNull(rate) || rate.compareTo(BigDecimal.ZERO) <= 0) {
                item.setExchangeRate(BigDecimal.ZERO);
                saleAmount = BigDecimal.ZERO;
                log.warn("汇率日期【{}】，币制【{}】", currentDate, currency);
                throw new ServiceException( CharSequenceUtil.format("未找到币制对应的汇率，请联系系统管理员配置"));
            } else {
                // 转换成人民币销售金额
                item.setExchangeRate(rate);
                saleAmount = MathUtil.multiplyWithTwo(rate, saleAmount, 2);
            }
            // 销售金额（本位币）
            item.setAmountLocalCurrency(saleAmount);
        }
        if (Objects.nonNull(taxAmount) &&
                taxAmount.compareTo(BigDecimal.ZERO) > 0 &&
                !Objects.equals(item.getCurrency(), "CNY")) {
            if (Objects.isNull(item.getExchangeRate()) || item.getExchangeRate().compareTo(BigDecimal.ZERO) <= 0) {
                item.setAllAmountLocalCurrency(BigDecimal.ZERO);
            } else {
                item.setAllAmountLocalCurrency(MathUtil.multiplyWithTwo(item.getExchangeRate(), taxAmount, 2));
            }
        }
        SoUtils.updateSoDetailCost(item, purchasePrice);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateCost(String id, SoDetailEntity item) {
        lambdaUpdate().set(SoDetailEntity::getPurchasePrice, item.getPurchasePrice())
                .set(SoDetailEntity::getSaleCost, item.getSaleCost())
                .set(SoDetailEntity::getSaleProfit, item.getSaleProfit())
                .set(SoDetailEntity::getSaleProfitRate, item.getSaleProfitRate())
                .eq(SoDetailEntity::getId, id).update();
    }

    @Override
    public void updateRemarkByIds(List<String> ids, String remark) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        lambdaUpdate().in(SoDetailEntity::getId, ids)
                .set(SoDetailEntity::getRemark, remark)
                .update(new SoDetailEntity());
    }

    @Override
    public List<VirtualInventoryDTO.VirtualInventoryQtyDTO> handleVirtualInventory(SoInfoEntity soInfoEntity,List<String> skuIdList) {
        //客户信息
        CustomerInfoEntity customerInfoEntity = customerInfoService.getCustomerById(soInfoEntity.getCustomerId());
        if (ObjectUtil.isEmpty(customerInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_92011);
        }
        //虚拟仓库信息
        VirtualWarehouseChannelDTO.PlatformDTO platformDTO = new VirtualWarehouseChannelDTO.PlatformDTO();
        platformDTO.setDictPlatform(customerInfoEntity.getPlatformType());
        platformDTO.setWarehouseIdList(Arrays.asList(soInfoEntity.getWarehouseId()));
        platformDTO.setRelationId("");
        platformDTO.setPartitionId(soInfoEntity.getPartitionId());
        List<VirtualWarehouseRelationEntity> virtualWarehouseList = wmsVirtualWarehouseFeign.getVirtualWarehouse(platformDTO);
        if (CollectionUtils.isEmpty(virtualWarehouseList)) {
            return Collections.EMPTY_LIST;
        }
        //查询虚拟库存
        VirtualInventoryDTO.VirtualInventoryParamDTO paramDTO = new VirtualInventoryDTO.VirtualInventoryParamDTO();
        paramDTO.setSkuIdList(skuIdList);
        paramDTO.setWarehouseIdList(Arrays.asList(soInfoEntity.getWarehouseId()));
        paramDTO.setVirtualWarehouseIdList(Arrays.asList(virtualWarehouseList.get(0).getVirtualWarehouseId()));
        List<VirtualInventoryDTO.VirtualInventoryQtyDTO> virtualInventorList = virtualInventoryFeign.listInventoryQty(paramDTO);
        return virtualInventorList;
    }

    /**
     * sku集合信息
     *
     * @param dto
     * @return java.util.List<com.erp.model.oms.dto.SoDetailDTO.SkuDTO>
     * @author yl
     * @date 2023-08-02 10:53
     */
    @Override
    public List<SoDetailDTO.SkuDTO> listSkuInfoBySkuNo(SoDetailDTO.ListSkuParamDTO dto) {
        String warehouseId = dto.getWarehouseId();
        List<String> skuNoList = dto.getSkuNoList();
        if (CollectionUtils.isEmpty(skuNoList)) {
            return Collections.emptyList();
        }
        List<SoDetailDTO.SkuDTO> resultList = new ArrayList<>(skuNoList.size());
        List<SkuVO> skuList = plmTaskFeign.listBySkuNoList(skuNoList);
        List<String> skuIdList = skuList.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList());


        List<SoDetailDTO.SkuHistoryPriceDTO> skuPriceHistoryList = this.listSkuPriceHistory(skuIdList);

        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = listSkuInventoryTotalList(skuIdList, warehouseId);
        for (String skuNo : skuNoList) {
            SoDetailDTO.SkuDTO result = new SoDetailDTO.SkuDTO();
            SkuVO item = skuList.stream().filter(s -> s.getSkuNo().equals(skuNo)).
                    findFirst().orElse(null);
            String skuId = "";
            if (item != null) {
                skuId = item.getSkuId();
                result.setProductName(item.getSkuName());
                result.setUnit(item.getUnitName());
                result.setSkuNo(item.getSkuNo());
            } else {
                result.setProductName("");
                result.setUnit("");
                result.setSkuNo("");
            }

            result.setSkuId(skuId);
            result.setQty(0);
            //即时库存
            String finalSkuId = skuId;
            Integer curInventoryQty = skuInventoryTotalList.stream().filter(s -> s.getSkuId().equals(finalSkuId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal())).orElse(0);
            result.setCurInventoryQty(curInventoryQty);

            //销售数量
            Integer qty = 0;
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
            Integer deliveryQty = 0;

            /**
             * 剩余数量
             * 销售数量-已出库数量
             */
            Integer waitQty = qty > deliveryQty ? qty - deliveryQty : 0;
            if (qty > curInventoryQty) {
                scarceQty = qty - curInventoryQty;
            }

            result.setScarceQty(scarceQty);
            result.setAvailableQty(getAvailableQty(curInventoryQty, qty));
            result.setDeliveryQty(deliveryQty);
            result.setWaitQty(waitQty);

            //虚拟库存
            result.setVirtualAvailableQty(MathUtil.ZERO);
            result.setVirtualScarceQty(MathUtil.ZERO);


            //税率
            BigDecimal taxRate = BigDecimal.ZERO;
            BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);

            //单价
            BigDecimal price = BigDecimal.ZERO;

            //含税单价=销售单价*（税率+1）
            BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
            BigDecimal taxPrice = MathUtil.multiplyWithTwo(price, multiplyTax);
            result.setTaxPrice(taxPrice);
            result.setPrice(price);
            result.setAmount(MathUtil.multiplyWithTwo(price, qty));
            result.setTaxRate(taxRate);
            result.setIsGift(Boolean.FALSE);
            result.setIsReissue(Boolean.FALSE);
            result.setIsClose(Boolean.FALSE);
            result.setTaxAmount(MathUtil.multiplyWithTwo(taxPrice, qty));
            result.setRemark("");
            //历史价格
            SoDetailDTO.SkuHistoryPriceDTO skuHistoryPrice = skuPriceHistoryList.stream().
                    filter(p -> p.getSkuId().equals(finalSkuId)).findFirst().orElse(null);
            if (skuHistoryPrice != null) {
                result.setMaxPrice(skuHistoryPrice.getMaxPrice());
                result.setMinPrice(skuHistoryPrice.getMinPrice());
                result.setAvgPrice(skuHistoryPrice.getAvgPrice());
            } else {
                result.setMaxPrice(price);
                result.setMinPrice(price);
                result.setAvgPrice(price);
            }
            resultList.add(result);
        }
        return resultList;
    }


    /**
     * 销售变更单成功后
     * 更改销售订单详情的金蝶id
     *
     * @param soId
     * @return void
     * @author yl
     * @date 2023-08-23 14:00
     */
    @Override
    public void updateDetailKingdeeId(String soId) {
        SoInfoEntity soInfo = soInfoService.getById(soId);
        if (Objects.nonNull(soInfo)) {
            KingdeeDTO kingdeeDTO = new KingdeeDTO();
            kingdeeDTO.setNumber(soInfo.getCode());
            kingdeeDTO.setId(soInfo.getSyncKingdeeId());
            String moduleCode = KingdeePushModuleEnum.SAL_SALEORDER.getCode();
            kingdeeDTO.setKingdeePushModuleCode(moduleCode);
            JSONObject jsonObject = dmpTaskFeign.getByKingdeeId(kingdeeDTO);
            List<SoDetailEntity> updateList = new ArrayList<>(10);
            if (jsonObject.containsKey("SaleOrderEntry")) {
                List<JSONObject> list = (List<JSONObject>) jsonObject.get("SaleOrderEntry");
                List<SoDetailEntity> detailList = this.lambdaQuery().eq(SoDetailEntity::getMainId, soId).orderByAsc(SoDetailEntity::getId).list();
                for (int i = 0; i < list.size(); i++) {
                    if (detailList.size() >= list.size()) {
                        JSONObject object = list.get(i);
                        String kingdeeDetailId = String.valueOf(object.getOrDefault("Id", ""));
                        SoDetailEntity soDetail = detailList.get(i);
                        //如果不相等
                        if (!kingdeeDetailId.equals(soDetail.getKingdeeDetailId())) {
                            soDetail.setKingdeeDetailId(kingdeeDetailId);
                            updateList.add(soDetail);
                        }
                    }

                }
            }
            if (CollectionUtils.isNotEmpty(updateList)) {
                this.updateBatchById(updateList);
            }

        }
    }


    @Override
    public void closeSoDetailByIds(List<String> closeSoDetailIdList) {
        if (CollectionUtils.isEmpty(closeSoDetailIdList)) {
            return;
        }
        LambdaUpdateWrapper<SoDetailEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(SoDetailEntity::getIsClose, Boolean.TRUE);
        updateWrapper.set(SoDetailEntity::getDiscountAmount,BigDecimal.ZERO);
        updateWrapper.in(SoDetailEntity::getId, closeSoDetailIdList);
        this.update(updateWrapper);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    @DistributeLocker(keyName = "saveDTO.detailId")
    public BatchResultDTO saveLockVirtualInventory(SoInfoDTO.LockVirtualInventorySaveDTO saveDTO) {
        SoDetailEntity soDetailEntity = this.getById(saveDTO.getDetailId());
        if (ObjectUtil.isEmpty(soDetailEntity)) {
            throw new ServiceException(ApiError.ERROR_92015);
        }
        //销售订单
        SoInfoEntity soInfoEntity = soInfoService.getById(soDetailEntity.getMainId());
        if (ObjectUtil.isEmpty(soInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_92016);
        }
        if (CharSequenceUtil.equals(soInfoEntity.getApproveStatus().getStatus(), BillApproveStatusEnum.DRAFT.getStatus())) {
            throw new ServiceException("暂存状态不允许锁定");
        }
        if (StrUtil.isBlank(soInfoEntity.getVirtualWarehouseId())) {
            throw new ServiceException( CharSequenceUtil.format("单据【{}】无虚拟仓不支持锁定",soInfoEntity.getCode()));
        }
        //校验冻结数量
        if (MathUtil.compareTo(saveDTO.getFrozenQty(), soDetailEntity.getFrozenQty()) == MathUtil.ZERO) {
            return new BatchResultDTO(soDetailEntity.getId(), CharSequenceUtil.format("【{}】{}",soInfoEntity.getCode(),soDetailEntity.getSkuNo()) ,"冻结数量未变无需更新",Boolean.TRUE);
        }

        //发货通知单
        List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailList = soDeliveryNoticeFeign.listDetailBySourceDetailIds(Arrays.asList(soDetailEntity.getId()));

        //旧冻结库存
        Integer oldFrozenQty = soDetailEntity.getFrozenQty();
        //冻结数量
        Integer frozenQty = saveDTO.getFrozenQty();

        //发货通知单下推数量
        Integer totalNoticeQty = soDeliveryNoticeDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSourceDetailId(), soDetailEntity.getId())
                        && CharSequenceUtil.equals(obj.getSkuId(),soDetailEntity.getSkuId())
                )
                .map(SoDeliveryNoticeDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
        if (frozenQty + totalNoticeQty > soDetailEntity.getQty()) {
            throw new ServiceException( CharSequenceUtil.format("销售订单【{}】SKU【{}】“销售数量【{}】不得小于锁定数量与发货通知单数量之和【{}】",soInfoEntity.getCode(),soDetailEntity.getSkuNo(),soDetailEntity.getQty(),totalNoticeQty + frozenQty));
        }
        soDetailEntity.setFrozenQty(frozenQty);
        soDetailEntity.setFrozenTime(LocalDateTime.now());
        boolean updated = this.updateById(soDetailEntity);
        if (!updated) {
            throw new ServiceException("更新销售订单明细{}失败，可能数据已变更,请稍后重试",soDetailEntity.getSkuNo());
        }
        //库存扣减
        VirtualInventoryStockDTO.StockParamDTO stockParamDTO = new VirtualInventoryStockDTO.StockParamDTO();
        stockParamDTO.setParamList(lockVirtualInventory(soInfoEntity,soDetailEntity,frozenQty - oldFrozenQty));
        stockParamDTO.setBusinessType(frozenQty > oldFrozenQty ? VirtualInventoryBusinessTypeEnum.SO_INFO_LOCK_ADD.getCode() : VirtualInventoryBusinessTypeEnum.SO_INFO_LOCK_LESS.getCode());
        virtualInventoryFeign.approveByType(stockParamDTO);

        //添加日志
        String content = StrUtil.format("操作了锁定库存，SKU【{}】 从【{}】到【{}】",soDetailEntity.getSkuNo(),oldFrozenQty,frozenQty);
        operateLogService.addModuleOperateLog(content, ModuleTypeEnum.SO.getCode(), soInfoEntity.getId(), "锁定库存操作");
        return new BatchResultDTO(soDetailEntity.getId(), CharSequenceUtil.format("【{}】{}",soInfoEntity.getCode(),soDetailEntity.getSkuNo()),"库存锁定成功",Boolean.TRUE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO batchUnLockVirtualInventory(String detailId,SoInfoEntity oldEntity) {
        SoDetailEntity old =  this.getById(detailId);
        if (ObjectUtil.isEmpty(old)) {
            throw new ServiceException(ApiError.ERROR_92015);
        }
        SoInfoEntity soInfoEntity = ObjectUtil.isEmpty(oldEntity) ? soInfoService.getById(old.getMainId()) : oldEntity;
        if (ObjectUtil.isEmpty(soInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_92016);
        }
        //无虚拟仓库
        if (StrUtil.isBlank(soInfoEntity.getVirtualWarehouseId())) {
            return new BatchResultDTO(old.getId(),soInfoEntity.getCode(),"无虚拟仓，不支持锁定库存",Boolean.TRUE);
        }
        //无锁定库存
        if (MathUtil.compareTo(old.getFrozenQty(),MathUtil.ZERO) == MathUtil.ZERO) {
            return new BatchResultDTO(old.getId(),soInfoEntity.getCode(),"无需要释放的锁定库存",Boolean.TRUE);
        }
        SoDetailEntity soDetailEntity = new SoDetailEntity();
        BeanMapperUtils.copy(old,soDetailEntity);

        VirtualInventoryStockDTO.StockParamDTO stockParamDTO = new VirtualInventoryStockDTO.StockParamDTO();
        stockParamDTO.setParamList(unLockVirtualInventory(soInfoEntity,old));
        stockParamDTO.setBusinessType(VirtualInventoryBusinessTypeEnum.SO_INFO_UNLOCK.getCode());
        virtualInventoryFeign.approveByType(stockParamDTO);

        //更新库存锁定数量
        soDetailEntity.setFrozenQty(MathUtil.ZERO);
        soDetailEntity.setFrozenTime(LocalDateTime.now());
        this.updateById(soDetailEntity);

        //添加日志
        String content = StrUtil.format("SKU【{}】操作了释放锁定库存",soDetailEntity.getSkuNo());
        operateLogService.addModuleOperateLog(content, ModuleTypeEnum.SO.getCode(), soInfoEntity.getId(), "释放库存操作");
        return new BatchResultDTO(soDetailEntity.getId(), CharSequenceUtil.format("【{}】{}",soInfoEntity.getCode(),soDetailEntity.getSkuNo()),"释放库存成功",Boolean.TRUE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO batchUnLockVirtualInventory(List<String> detailIdList,SoInfoEntity oldEntity) {
        List<SoDetailEntity> oldList =  this.listByIds(detailIdList);
        if (CollectionUtils.isEmpty(oldList)) {
            throw new ServiceException(ApiError.ERROR_92015);
        }
        SoInfoEntity soInfoEntity = ObjectUtil.isEmpty(oldEntity) ? soInfoService.getById(oldList.get(0).getMainId()) : oldEntity;
        if (ObjectUtil.isEmpty(soInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_92016);
        }
        //无虚拟仓库
        if (StrUtil.isBlank(soInfoEntity.getVirtualWarehouseId())) {
            return new BatchResultDTO(soInfoEntity.getId(),soInfoEntity.getCode(),"无虚拟仓，不支持释放锁定库存",Boolean.TRUE);
        }
        VirtualInventoryStockDTO.StockParamDTO stockParamDTO = new VirtualInventoryStockDTO.StockParamDTO();
        List<VirtualInventoryStockDTO.OutInStockDTO> outInStockList = unLockVirtualInventory(soInfoEntity, oldList);
        if (CollectionUtils.isEmpty(outInStockList)) {
            return new BatchResultDTO(soInfoEntity.getId(),soInfoEntity.getCode(),"无需要释放的锁定库存",Boolean.TRUE);
        }
        stockParamDTO.setParamList(outInStockList);
        stockParamDTO.setBusinessType(VirtualInventoryBusinessTypeEnum.SO_INFO_UNLOCK.getCode());
        virtualInventoryFeign.approveByType(stockParamDTO);

        //更新库存锁定数量
        oldList.stream().forEach(obj -> obj.setFrozenQty(MathUtil.ZERO));
        this.updateBatchById(oldList);
        return new BatchResultDTO(soInfoEntity.getId(), CharSequenceUtil.format("【{}】",soInfoEntity.getCode()),"释放库存成功",Boolean.TRUE);
    }

    @Override
    public void updateFrozenQty(List<SoDetailDTO.UpdateFrozenQtyDTO> soParamList) {
        if (CollectionUtils.isEmpty(soParamList)) {
            return;
        }
        List<String> detailIdList = soParamList.stream().map(SoDetailDTO.UpdateFrozenQtyDTO::getDetailId).distinct().collect(Collectors.toList());
        List<SoDetailEntity> soDetailEntityList = this.listByIds(detailIdList);
        if (CollectionUtils.isEmpty(soDetailEntityList)) {
            return;
        }
        for (SoDetailEntity soDetailEntity : soDetailEntityList) {
            Integer frozenQty = soParamList.stream().filter(obj -> CharSequenceUtil.equals(obj.getDetailId(), soDetailEntity.getId())).map(SoDetailDTO.UpdateFrozenQtyDTO::getFrozenQty).findFirst().orElse(MathUtil.ZERO);
            soDetailEntity.setFrozenQty(frozenQty);
            soDetailEntity.setFrozenTime(LocalDateTime.now());
        }
        this.updateBatchById(soDetailEntityList);
    }

    @Override
    public List<ReportOrderDataDTO.ViewDTO> listAllVirtualSoDetail() {
        return baseMapper.listAllVirtualSoDetail();
    }

    /**
     * 锁定库存
     * @author will
     * @date 2024/7/16 14:04
     * @param soInfoEntity
     * @param soDetailEntity
     * @param qty
     * @return List<OutInStockDTO>
     */
    private List<VirtualInventoryStockDTO.OutInStockDTO> lockVirtualInventory(SoInfoEntity soInfoEntity,SoDetailEntity soDetailEntity,Integer qty) {
        VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
        outInStockDTO.setSkuId(soDetailEntity.getSkuId());
        outInStockDTO.setSkuNo(soDetailEntity.getSkuNo());
        outInStockDTO.setWarehouseId(soInfoEntity.getWarehouseId());
        outInStockDTO.setVirtualWarehouseId(soInfoEntity.getVirtualWarehouseId());
        outInStockDTO.setBillDate(LocalDate.now());
        outInStockDTO.setQty(Math.abs(qty));
        outInStockDTO.setSourceId(soInfoEntity.getId());
        outInStockDTO.setSourceCode(soInfoEntity.getCode());
        outInStockDTO.setSourceType(InventorySourceTypeEnum.SO_INFO);
        outInStockDTO.setSourceDetailId(soDetailEntity.getId());
        return Arrays.asList(outInStockDTO);
    }

    /**
     * 处理库存数据
     * @author will
     * @date 2024/7/16 9:41
     * @param soInfoEntity
     * @param soDetailEntity
     * @return List<OutInStockDTO>
     */
    private List<VirtualInventoryStockDTO.OutInStockDTO> unLockVirtualInventory (SoInfoEntity soInfoEntity, SoDetailEntity soDetailEntity) {
        VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
        outInStockDTO.setSkuId(soDetailEntity.getSkuId());
        outInStockDTO.setSkuNo(soDetailEntity.getSkuNo());
        outInStockDTO.setWarehouseId(soInfoEntity.getWarehouseId());
        outInStockDTO.setVirtualWarehouseId(soInfoEntity.getVirtualWarehouseId());
        outInStockDTO.setBillDate(LocalDate.now());
        outInStockDTO.setQty(soDetailEntity.getFrozenQty());
        outInStockDTO.setSourceId(soInfoEntity.getId());
        outInStockDTO.setSourceCode(soInfoEntity.getCode());
        outInStockDTO.setSourceType(InventorySourceTypeEnum.SO_INFO);
        outInStockDTO.setSourceDetailId(soDetailEntity.getId());
       return Arrays.asList(outInStockDTO);
    }

    /**
     * 处理库存数据
     * @author will
     * @date 2024/7/16 9:41
     * @param soInfoEntity
     * @param soDetailEntityList
     * @return List<OutInStockDTO>
     */
    private List<VirtualInventoryStockDTO.OutInStockDTO> unLockVirtualInventory (SoInfoEntity soInfoEntity, List<SoDetailEntity> soDetailEntityList) {
        List<VirtualInventoryStockDTO.OutInStockDTO> resultList = new ArrayList<>();
        for (SoDetailEntity soDetailEntity : soDetailEntityList) {
            VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
            outInStockDTO.setSkuId(soDetailEntity.getSkuId());
            outInStockDTO.setSkuNo(soDetailEntity.getSkuNo());
            outInStockDTO.setWarehouseId(soInfoEntity.getWarehouseId());
            outInStockDTO.setVirtualWarehouseId(soInfoEntity.getVirtualWarehouseId());
            outInStockDTO.setBillDate(LocalDate.now());
            //数量未大于0无需扣建库存
            if (MathUtil.compareTo(soDetailEntity.getFrozenQty(),MathUtil.ZERO) <= MathUtil.ZERO) {
                continue;
            }
            outInStockDTO.setQty(soDetailEntity.getFrozenQty());
            outInStockDTO.setSourceId(soInfoEntity.getId());
            outInStockDTO.setSourceCode(soInfoEntity.getCode());
            outInStockDTO.setSourceType(InventorySourceTypeEnum.SO_INFO);
            outInStockDTO.setSourceDetailId(soDetailEntity.getId());
            resultList.add(outInStockDTO);
        }
        return resultList;
    }

    /**
     * 更新校验
     * @author will
     * @date 2024/7/16 17:48
     * @param dbList
     * @param updateList
     * @param removeList
     */
    private void checkSoDetailQty ( List<SoDetailEntity> dbList,List<SoDetailDTO.UpdateDTO> updateList,List<SoDetailEntity> removeList,SoInfoEntity soInfoEntity,SoInfoEntity oldEntity) {
        if (CollectionUtils.isEmpty(updateList) && CollectionUtils.isEmpty(removeList)) {
            return;
        }
        //修改明细id
        List<String> updateDetailIdList = updateList.stream().map(SoDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        //删除明细id
        List<String> removeDetailIdList = removeList.stream().map(SoDetailEntity::getId).collect(Collectors.toList());
        updateDetailIdList.addAll(removeDetailIdList);
        //发货通知单
        List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailList = soDeliveryNoticeFeign.listDetailBySourceDetailIds(updateDetailIdList);

        //释放有变更虚拟仓
        boolean isChangeVirtual = !CharSequenceUtil.equals(soInfoEntity.getVirtualWarehouseId(),oldEntity.getVirtualWarehouseId());

        //需要释放库存的明细
        List<String> unLockIdList = new ArrayList<>();

        for (SoDetailDTO.UpdateDTO updateDTO : updateList) {
            //明细
            SoDetailEntity soDetailEntity = dbList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), updateDTO.getId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_92015);
            }
            //有效数量
            Integer noticeQty = soDeliveryNoticeDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSourceDetailId(), updateDTO.getId()))
                    .map(SoDeliveryNoticeDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
            //SKU变更校验
            if (noticeQty > MathUtil.ZERO && !CharSequenceUtil.equals(updateDTO.getSkuId(),soDetailEntity.getSkuId())) {
                throw new ServiceException( CharSequenceUtil.format("SKU【{}】已下推发货通知单不支持变更SKU",soDetailEntity.getSkuNo()));
            }
            //销售数量校验
            if (noticeQty > updateDTO.getQty()) {
                throw new ServiceException( CharSequenceUtil.format("SKU【{}】销售数量不能小于发货通知单下推数量",soDetailEntity.getSkuNo()));
            }
            //已审核数量
            Integer noticeApproveQty = soDeliveryNoticeDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSourceDetailId(), updateDTO.getId())
                            && CharSequenceUtil.equals(obj.getApproveStatus(),ApproveStatusEnum.APPROVE.getStatus()))
                    .map(SoDeliveryNoticeDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);

            //销售数量不能小于（冻结数量+发货通知单审核数量）
            if (CharSequenceUtil.equals(updateDTO.getSkuId(),soDetailEntity.getSkuId()) && noticeApproveQty + soDetailEntity.getFrozenQty() > updateDTO.getQty()) {
                throw new ServiceException( CharSequenceUtil.format("SKU【{}】销售数量不能小于（冻结数量+发货通知单审核数量）",soDetailEntity.getSkuNo()));
            }
            
            //B2B销售订单明细行冻结库存检查 - 修改数量时不允许小于冻结数量
            if (CharSequenceUtil.equals(updateDTO.getSkuId(),soDetailEntity.getSkuId()) && MathUtil.compareTo(soDetailEntity.getFrozenQty(),MathUtil.ZERO) > MathUtil.ZERO) {
                if (soDetailEntity.getFrozenQty() > updateDTO.getQty()) {
                    throw new ServiceException( CharSequenceUtil.format("SKU【{}】发货数量不允许小于冻结数量【{}】，如需修改请联系PMC释放库存后操作",soDetailEntity.getSkuNo(), soDetailEntity.getFrozenQty()));
                }
            }
            
            //有更新sku或者变更虚拟仓则需要释放库存
            if ((!CharSequenceUtil.equals(updateDTO.getSkuId(),soDetailEntity.getSkuId()) || isChangeVirtual ) && MathUtil.compareTo(soDetailEntity.getFrozenQty(),MathUtil.ZERO) > MathUtil.ZERO) {
                unLockIdList.add(soDetailEntity.getId());
            }
        }
        for (SoDetailEntity soDetailEntity : removeList) {
            //下推数量
            long count = soDeliveryNoticeDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSourceDetailId(), soDetailEntity.getId()))
                    .map(SoDeliveryNoticeDetailEntity::getDeliveryQty).count();
            if (count > 0) {
                throw new ServiceException( CharSequenceUtil.format("SKU【{}】已下推发货通知单不支持删除",soDetailEntity.getSkuNo()));
            }
            
            //B2B销售订单明细行冻结库存检查 - 删除时不允许删除已冻结库存的明细行
            if (MathUtil.compareTo(soDetailEntity.getFrozenQty(),MathUtil.ZERO) > MathUtil.ZERO) {
                throw new ServiceException( CharSequenceUtil.format("SKU【{}】已冻结数量【{}】，不允许删除，如需删除请联系PMC释放库存后操作",soDetailEntity.getSkuNo(), soDetailEntity.getFrozenQty()));
            }
            
            //删除明细释放库存
            if (MathUtil.compareTo(soDetailEntity.getFrozenQty(),MathUtil.ZERO) > MathUtil.ZERO) {
                unLockIdList.add(soDetailEntity.getId());
            }
        }
        //释放冻结库存
        if (CollectionUtils.isNotEmpty(unLockIdList)) {
            batchUnLockVirtualInventory(unLockIdList,oldEntity);
        }
    }
}
