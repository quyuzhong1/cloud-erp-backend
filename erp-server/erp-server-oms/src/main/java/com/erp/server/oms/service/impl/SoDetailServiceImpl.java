package com.erp.server.oms.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.dto.excel.SoDetailImportExcelDTO;
import com.erp.model.oms.dto.listAddDetailViewDTO;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.oms.entity.SoReturnDetailEntity;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.enums.DeliveryStatusEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.InventoryFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.oms.constant.OmsConstant;
import com.erp.server.oms.listener.SoDetailExcelListener;
import com.erp.server.oms.mapper.SoDetailMapper;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SoDetailService;
import com.erp.server.oms.service.SoInfoService;
import com.erp.server.oms.service.SoReturnDetailService;
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

    @Resource
    private OperateLogService operateLogService;


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
    public List<SoInfoDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<SoInfoDTO.TabListDTO> result = new ArrayList<>(5);
        //所有的
        List<SoDetailDTO.TypeCountDTO> countList = baseMapper.listApproveCount(dto.getPermissionSql());
        int allCount = countList.stream().mapToInt(SoDetailDTO.TypeCountDTO::getCount).sum();
        SoInfoDTO.TabListDTO all = new SoInfoDTO.TabListDTO();
        all.setCount(allCount);
        all.setSearchType(OmsConstant.ALL);
        result.add(all);

        //待审核
        String approveIngStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        SoInfoDTO.TabListDTO waitApprove = new SoInfoDTO.TabListDTO();
        waitApprove.setSearchType(OmsConstant.WAIT_APPROVE);
        int waitApproveCount = countList.stream().filter(a -> a.getType().equals(approveIngStatus)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getCount())).orElse(0);
        waitApprove.setCount(waitApproveCount);
        result.add(waitApprove);


        List<SoDetailDTO.TypeCountDTO> deliveryCountList = baseMapper.listDeliveryCount(dto.getPermissionSql());
        //待发货
        SoInfoDTO.TabListDTO waitDelivery = new SoInfoDTO.TabListDTO();
        waitDelivery.setSearchType(OmsConstant.WAIT_DELIVERY);

        //已发货
        String completeShipment = DeliveryStatusEnum.COMPLETE_SHIPMENT.getCode();

        //已审核+未发货+部分发货的
        int waitDeliveryCount = deliveryCountList.stream().filter(s->!completeShipment.equals(s.getType())).
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
        int deliveryCount = (int) deliveryCountList.stream().filter(s ->  completeShipment.equals(s.getType())).findFirst().
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
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listApproveWarehouse();
        List<String> skuIdList = list.stream().map(SoDetailDTO.AddDetailView::getSkuId).distinct().collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> productDetailEntitys = plmTaskFeign.getByIdList(skuIdList);
        SoInfoEntity soInfoEntity = soInfoService.getById(dto.getId());
        //从wms 获取到sku 的即时库存信息
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = listSkuInventoryTotalList(skuIdList, soInfoEntity.getWarehouseId());
        for (SoDetailDTO.AddDetailView addDetailView : list) {
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
        List<SoDetailEntity> dbList = this.listBaseByMainId(mainId);
        List<SoDetailDTO.ViewDTO> resultList = BeanMapper.copyList(dbList, SoDetailDTO.ViewDTO.class);
        List<String> skuIdList = resultList.stream().map(SoDetailDTO.ViewDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        //从wms 获取到sku 的即时库存信息
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = new ArrayList<>();
        if (StringUtils.isNotBlank(warehouseId) && CollectionUtils.isNotEmpty(skuIdList)) {
            skuInventoryTotalList = listSkuInventoryTotalList(skuIdList, warehouseId);
        }

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
            BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);
            //单价
            BigDecimal price = item.getPrice();
            //含税单价=销售单价*（税率+1）
            BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
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

    @Override
    public List<SoDetailEntity> listSoDetailByMainIds(List<String> ids) {
        return baseMapper.listSoDetailByMainIds(ids);
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

                String unShipped = DeliveryStatusEnum.UN_SHIPPED.getCode();
                String partialShipment = DeliveryStatusEnum.PARTIAL_SHIPMENT.getCode();
                List<String> deliveryStatusList = Arrays.asList(unShipped, partialShipment);
                List<SoDetailDTO.InfoDTO> waitDeliveryList = baseMapper.listSoDetailByDeliveryStatus(deliveryStatusList);
                return waitDeliveryList.stream().map(SoDetailDTO.InfoDTO::getId).collect(Collectors.toList());

            //已发货
            case OmsConstant
                    .DELIVERY:
                String completeShipment = DeliveryStatusEnum.COMPLETE_SHIPMENT.getCode();
                List<String> deliveryStatus = Arrays.asList(completeShipment);
                List<SoDetailDTO.InfoDTO> deliveryList = baseMapper.listSoDetailByDeliveryStatus(deliveryStatus);
                return deliveryList.stream().map(SoDetailDTO.InfoDTO::getId).collect(Collectors.toList());

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
        List<SoDetailEntity> removeList = dbList.stream().filter(r -> deleteIdList.contains(r.getId())).collect(Collectors.toList());
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
            item.setPrice(price);

            //税率
            BigDecimal taxRate = item.getTaxRate();
            BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);
            //含税单价=销售单价*（税率+1）
            BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
            BigDecimal taxPrice = MathUtil.multiply(price, multiplyTax);
            //金额
            BigDecimal amount = MathUtil.multiply(taxPrice, qty);

            item.setAmount(amount);
            String skuNo = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSkuNo())).orElse("");
            item.setSkuNo(skuNo);

            String symbol = currencyViewList.stream().filter(c -> c.getId().equals(currency)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSymbol())).orElse("");
            item.setCurrencySymbol(symbol);
        }
        //这是删除
        List<Pair<String, String>> removePairList = removeList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("删除了一个销售产品【%s】", ModuleTypeEnum.SO.getCode(), removePairList, "编辑操作");

        //这是添加
        List<Pair<String, String>> addPairList = saveOrUpdateList.stream().filter(s -> StringUtils.isBlank(s.getId())).map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("添加了一个销售产品【%s】", ModuleTypeEnum.SO.getCode(), addPairList, "编辑操作");
        //修改的
        updateEntityList = saveOrUpdateList.stream().filter(s -> StringUtils.isNotBlank(s.getId())).collect(Collectors.toList());
        for (SoDetailEntity update : updateEntityList) {
            String id = update.getId();
            SoDetailEntity old = dbList.stream().filter(d -> d.getId().equals(id)).findFirst().orElse(null);
            if (old != null) {
                operateLogService.addModuleOperateLogByObj(old, update, ModuleTypeEnum.SO.getCode(), mainId, "", "");
            }
        }

        this.saveOrUpdateBatch(saveOrUpdateList);
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
        String path = "classpath:excel/soSku.xlsx";
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
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), "ISO8859-1"));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            log.error("下载模板出错了==={}", e);
            throw new ServiceException(ApiError.Default);
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
    public SoDetailDTO.ImportDTO importSku(MultipartFile excelFile, HttpServletResponse response, String warehouseId) {
        List<SkuVO> skuList = plmTaskFeign.listApproveSku();
        SoDetailExcelListener excelListenerUtil = new SoDetailExcelListener(skuList);
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
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = listSkuInventoryTotalList(skuIdList, warehouseId);
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
            BigDecimal taxRate = item.getTaxRate();
            BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);
            //单价
            BigDecimal price = item.getPrice();
            item.setAmount(MathUtil.multiply(price, qty));
            //含税单价=销售单价*（税率+1）
            BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
            BigDecimal taxPrice = MathUtil.multiply(price, multiplyTax);
            item.setTaxPrice(taxPrice);
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
        }
        result.setSuccessList(successList);
        //导出错误数据
        List<SoDetailImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "销售订单错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, SoDetailImportExcelDTO.class);
            if (file != null && !file.isDirectory()) {
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
        result.setAmount(MathUtil.multiply(price, qty));
        //含税单价=销售单价*（税率+1）
        BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
        BigDecimal taxPrice = MathUtil.multiply(price, multiplyTax);
        result.setTaxPrice(taxPrice);
        result.setPrice(price);
        result.setTaxRate(taxRate);
        result.setIsGift(Boolean.FALSE);
        result.setIsReissue(Boolean.FALSE);
        result.setIsClose(Boolean.FALSE);
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

        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);

        List<SoDetailDTO.ExportPdfDTO> resultList = new ArrayList<>(dbList.size());
        for (SoDetailEntity item : dbList) {
            SoDetailDTO.ExportPdfDTO result = new SoDetailDTO.ExportPdfDTO();
            result.setCurrency(item.getCurrency());
            result.setCurrencySymbol(item.getCurrencySymbol());
            result.setSkuNo(item.getSkuNo());
            String skuId = item.getSkuId();
            result.setSkuId(skuId);
            result.setQty(item.getQty());
            result.setAmount(item.getAmount());
            BigDecimal taxRate = item.getTaxRate();
            BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);

            result.setTaxRate(taxRate);
            //单价
            BigDecimal price = item.getPrice();
            //含税单价=销售单价*（税率+1）
            BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
            result.setTaxPrice(MathUtil.multiply(price, multiplyTax));
            SkuVO skuVO = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).
                    findFirst().orElse(null);
            result.setDeclareModel("");
            if (skuVO != null) {
                result.setProductName(skuVO.getSkuName());
                result.setUnit(skuVO.getUnitName());
            } else {
                result.setProductName("");
                result.setUnit("");

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

        List<SoDetailEntity> dbList = this.listBaseByMainId(soId);
        //获取未关闭的数据
        dbList = dbList.stream().filter(s -> s.getIsClose()).collect(Collectors.toList());
        List<SoDetailDTO.ViewDTO> resultList = BeanMapper.copyList(dbList, SoDetailDTO.ViewDTO.class);
        List<String> skuIdList = resultList.stream().map(SoDetailDTO.ViewDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);
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
            BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);
            //单价
            BigDecimal price = item.getPrice();
            //含税单价=销售单价*（税率+1）
            BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
            BigDecimal taxPrice = MathUtil.multiply(price, multiplyTax);
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
    public void updateDeliveryStatus(List<SoDetailDTO.UpdateDeliveryStatusDTO> paramList) {
        if (CollectionUtils.isNotEmpty(paramList)) {
            List<String> idList = paramList.stream().map(SoDetailDTO.UpdateDeliveryStatusDTO::getId).collect(Collectors.toList());
            List<SoDetailEntity> soDetailList = this.listByIds(idList);
            for (SoDetailEntity item : soDetailList) {
                String id = item.getId();
                Integer qty = item.getQty();
                Integer deliveryQty = paramList.stream().filter(p -> p.getId().equals(id)).findFirst().
                        flatMap(obj -> Optional.ofNullable(obj.getAlreadyDeliveryQty())).orElse(0);
                if (deliveryQty >= qty) {
                    item.setDeliveryStatus(DeliveryStatusEnum.COMPLETE_SHIPMENT.getCode());
                }
                if (deliveryQty < qty && deliveryQty != 0) {
                    item.setDeliveryStatus(DeliveryStatusEnum.PARTIAL_SHIPMENT.getCode());
                }
                if ( deliveryQty == 0) {
                    item.setDeliveryStatus(DeliveryStatusEnum.UN_SHIPPED.getCode());
                }
            }
            this.updateBatchById(soDetailList);
        }

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
    public List<String> checkSkuQty(String warehouseId, List<SoDetailDTO.AddDTO> detailList) {
        List<String> scarceSkuList = new ArrayList<>(10);
        if (CollectionUtils.isNotEmpty(detailList)) {
            List<String> skuIdList = detailList.stream().map(SoDetailDTO.AddDTO::getSkuId).collect(Collectors.toList());
            List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);
            List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = listSkuInventoryTotalList(skuIdList, warehouseId);
            for (SoDetailDTO.AddDTO item : detailList) {
                int qty = item.getQty();
                String skuId = item.getSkuId();
                //即时库存
                Integer curInventoryQty = skuInventoryTotalList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().
                        flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal())).orElse(0);
                if (qty >= curInventoryQty) {
                    String skuNo = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().
                            flatMap(obj -> Optional.ofNullable(obj.getSkuNo())).orElse("");
                    scarceSkuList.add(skuNo);
                }
            }
        }
        return scarceSkuList;
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


    @Override
    public List<SoDetailEntity> listBaseByMainId(String mainId) {
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
            item.setPrice(price);

            //税率
            BigDecimal taxRate = item.getTaxRate();
            BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);

            //含税单价=销售单价*（税率+1）
            BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
            BigDecimal taxPrice = MathUtil.multiply(price, multiplyTax);
            //金额
            BigDecimal amount = MathUtil.multiply(taxPrice, qty);
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
