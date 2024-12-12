package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.ApplicationContextUtils;
import com.erp.model.plm.dto.MouldRefCalcQtyDTO;
import com.erp.model.plm.entity.MouldRefCalcQtyDetailEntity;
import com.erp.model.plm.entity.MouldRefCalcQtyEntity;
import com.erp.model.plm.entity.MouldRefProductEntity;
import com.erp.model.plm.enums.RefundStandardEnum;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.rpc.scm.feign.PurchaseOrderFeign;
import com.erp.server.plm.mapper.MouldRefCalcQtyMapper;
import com.erp.server.plm.service.MouldRefCalcQtyDetailService;
import com.erp.server.plm.service.MouldRefCalcQtyService;
import com.erp.server.plm.service.MouldRefProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 模具返还数量计算 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-12-10
 */
@Service
public class MouldRefCalcQtyServiceImpl extends SuperServiceImpl<MouldRefCalcQtyMapper, MouldRefCalcQtyEntity> implements MouldRefCalcQtyService {
    @Resource
    private MouldRefProductService mouldRefProductService;

    @Resource
    private PurchaseOrderFeign purchaseOrderFeign;

    @Resource
    private MouldRefCalcQtyDetailService mouldRefCalcQtyDetailService;

    @Override
    public List<MouldRefCalcQtyEntity> listByMouldDetailIdList(List<String> detailIds) {
        return list(Wrappers.<MouldRefCalcQtyEntity>lambdaQuery().in(MouldRefCalcQtyEntity::getMouldDetailId, detailIds));
    }

    @Override
    public MouldRefCalcQtyEntity getByDetailId(String mouldDetailId) {
        return getOne(Wrappers.<MouldRefCalcQtyEntity>lambdaQuery().eq(MouldRefCalcQtyEntity::getMouldDetailId, mouldDetailId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void calcRefundQty() {
        List<MouldRefCalcQtyDTO> mouldRefCalcQtyDTOList = baseMapper.getNeedCalcData();
        List<String> detailIds = mouldRefCalcQtyDTOList.stream().map(MouldRefCalcQtyDTO::getDetailId).distinct().collect(Collectors.toList());
        List<MouldRefCalcQtyEntity> mouldRefList = listByMouldDetailIdList(detailIds);
        List<MouldRefProductEntity> mouldRefProductList = mouldRefProductService.listByMouldDetailIdList(detailIds);
        List<String> mouldRefMouldRefIds = mouldRefProductList.stream().map(MouldRefProductEntity::getId).distinct().collect(Collectors.toList());
        List<MouldRefCalcQtyDetailEntity> mouldRefDetailList = mouldRefCalcQtyDetailService.listByMainIds(mouldRefMouldRefIds);
        Map<String, List<String>> detailIdMap = mouldRefProductList.stream()
                .collect(Collectors.groupingBy(MouldRefProductEntity::getMouldDetailId, Collectors.mapping(v -> v.getSkuId() + "-" + v.getSupplierId(), Collectors.toList())));
        List<String> skuIdList = mouldRefProductList.stream().map(MouldRefProductEntity::getSkuId).distinct().collect(Collectors.toList());
        List<String> supplierIdList = mouldRefProductList.stream().map(MouldRefProductEntity::getSupplierId).distinct().collect(Collectors.toList());
        List<PurchaseOrderDTO.PurchaseCalcQtyDTO> purchaseOrderDetailList = purchaseOrderFeign.listAllPurchaseBySkuIdAndSupplier(new PurchaseOrderDTO.PurchaseCalcQtyParamsDTO(skuIdList, supplierIdList));
        List<MouldRefCalcQtyEntity> mouldRefCalcQtyList = new ArrayList<>();
        List<MouldRefCalcQtyDetailEntity> mouldRefCalcQtyDetailList = new ArrayList<>();
        for (MouldRefCalcQtyDTO dto : mouldRefCalcQtyDTOList) {
            MouldRefCalcQtyEntity mouldRefCalcQty = mouldRefList.stream()
                    .filter(v -> v.getMouldDetailId().equals(dto.getDetailId()))
                    .findFirst()
                    .orElse(new MouldRefCalcQtyEntity());
            mouldRefCalcQty.setMouldDetailId(dto.getDetailId());
            mouldRefCalcQty.setId(ObjectUtils.isEmpty(mouldRefCalcQty.getId()) ? IdWorker.getIdStr() : mouldRefCalcQty.getId());
            List<String> skuSupplierList = detailIdMap.get(dto.getDetailId());
            if (CollectionUtils.isEmpty(skuSupplierList)) {
                mouldRefCalcQty.setPurchaseQty(0);
                mouldRefCalcQty.setReceiveQty(0);
                mouldRefCalcQty.setStockInQty(0);
                mouldRefCalcQty.setCalcQty(0);
                mouldRefCalcQtyList.add(mouldRefCalcQty);
            } else {
                List<PurchaseOrderDTO.PurchaseCalcQtyDTO> dtos = purchaseOrderDetailList.stream()
                        .filter(v -> skuSupplierList.contains(v.getSkuId() + "-" + v.getSupplierId()))
                        .filter(v -> !v.getCreateTime().toLocalDate().isBefore(dto.getEnableDate()))
                        .collect(Collectors.toList());
                int purchaseQty = 0;
                int receiveQty = 0;
                int stockInQty = 0;
                for (PurchaseOrderDTO.PurchaseCalcQtyDTO calcQtyDTO : dtos) {
                    MouldRefCalcQtyDetailEntity detailEntity = getMouldRefCalcQtyDetailEntity(calcQtyDTO, mouldRefCalcQty, mouldRefDetailList);
                    mouldRefCalcQtyDetailList.add(detailEntity);
                    purchaseQty += calcQtyDTO.getPurchaseQty();
                    receiveQty += calcQtyDTO.getReceiveQty();
                    stockInQty += calcQtyDTO.getStockInQty();
                }
                mouldRefCalcQty.setPurchaseQty(purchaseQty);
                mouldRefCalcQty.setReceiveQty(receiveQty);
                mouldRefCalcQty.setStockInQty(stockInQty);
                if (RefundStandardEnum.PURCHASE_ORDERS.getCode().equals(dto.getRefundStandard())) {
                    mouldRefCalcQty.setCalcQty(purchaseQty);
                } else if (RefundStandardEnum.RECEIVING.getCode().equals(dto.getRefundStandard())) {
                    mouldRefCalcQty.setCalcQty(receiveQty);
                } else {
                    mouldRefCalcQty.setCalcQty(stockInQty);
                }
                mouldRefCalcQtyList.add(mouldRefCalcQty);
            }
        }
        ApplicationContextUtils.getBean(MouldRefCalcQtyServiceImpl.class).saveOrUpdateBatch(mouldRefCalcQtyList);
        mouldRefCalcQtyDetailService.saveOrUpdateBatch(mouldRefCalcQtyDetailList);
    }

    /**
     * 构建明细实体
     *
     * @param calcQtyDTO         采购参数
     * @param mouldRefCalcQty    计算参数
     * @param mouldRefDetailList 明细数据
     */
    private static MouldRefCalcQtyDetailEntity getMouldRefCalcQtyDetailEntity(PurchaseOrderDTO.PurchaseCalcQtyDTO calcQtyDTO, MouldRefCalcQtyEntity mouldRefCalcQty, List<MouldRefCalcQtyDetailEntity> mouldRefDetailList) {

        MouldRefCalcQtyDetailEntity detailEntity = mouldRefDetailList.stream()
                .filter(v -> v.getMainId().equals(mouldRefCalcQty.getId()))
                .filter(v -> v.getPurchaseOrderCode().equals(calcQtyDTO.getCode()))
                .filter(v -> v.getSkuId().equals(calcQtyDTO.getSkuId()))
                .filter(v -> v.getSupplierId().equals(calcQtyDTO.getSupplierId()))
                .findFirst()
                .orElse(new MouldRefCalcQtyDetailEntity());
        detailEntity.setMainId(mouldRefCalcQty.getId());
        detailEntity.setPurchaseQty(calcQtyDTO.getPurchaseQty());
        detailEntity.setReceiveQty(calcQtyDTO.getReceiveQty());
        detailEntity.setStockInQty(calcQtyDTO.getStockInQty());
        detailEntity.setPurchaseOrderCode(calcQtyDTO.getCode());
        detailEntity.setPoCreateTime(calcQtyDTO.getCreateTime());
        detailEntity.setSkuId(calcQtyDTO.getSkuId());
        detailEntity.setSkuNo(calcQtyDTO.getSkuNo());
        detailEntity.setSupplierId(calcQtyDTO.getSupplierId());
        return detailEntity;
    }
}
