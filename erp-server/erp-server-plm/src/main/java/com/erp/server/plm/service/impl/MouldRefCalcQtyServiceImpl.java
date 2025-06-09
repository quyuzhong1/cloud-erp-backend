package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.vo.LoginUser;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.dto.MouldInfoDTO;
import com.erp.model.plm.dto.MouldRefCalcQtyDTO;
import com.erp.model.plm.entity.MouldRefCalcQtyDetailEntity;
import com.erp.model.plm.entity.MouldRefCalcQtyEntity;
import com.erp.model.plm.entity.MouldRefProductEntity;
import com.erp.model.plm.entity.MouldRefundAgreementEntity;
import com.erp.model.plm.enums.MouldRefundStatusEnum;
import com.erp.model.plm.enums.NoticeEnum;
import com.erp.model.plm.enums.RefundStandardEnum;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.rpc.scm.feign.PurchaseOrderFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.server.plm.mapper.MouldRefCalcQtyMapper;
import com.erp.server.plm.service.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    @Resource
    private MouldRefundAgreementService mouldRefundAgreementService;

    @Resource
    @Lazy
    private MouldInfoService mouldInfoService;

    @Resource
    private ScmTaskFeign scmTaskFeign;

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
        Map<String, String> supplierMap = getSupplierMap(mouldRefCalcQtyDTOList);
        List<String> detailIds = mouldRefCalcQtyDTOList.stream().map(MouldRefCalcQtyDTO::getDetailId).distinct().collect(Collectors.toList());
        List<MouldRefundAgreementEntity> agreementEntityList = mouldRefundAgreementService.listByMouldDetailIdList(detailIds);
        removeOldData(detailIds);
        List<MouldRefProductEntity> mouldRefProductList = mouldRefProductService.listByMouldDetailIdList(detailIds);
        Map<String, List<String>> detailIdMap = mouldRefProductList.stream()
                .collect(Collectors.groupingBy(MouldRefProductEntity::getMouldDetailId, Collectors.mapping(v -> v.getSkuId() + "-" + v.getSupplierId(), Collectors.toList())));
        List<String> skuIdList = mouldRefProductList.stream().map(MouldRefProductEntity::getSkuId).distinct().collect(Collectors.toList());
        List<String> supplierIdList = mouldRefProductList.stream().map(MouldRefProductEntity::getSupplierId).distinct().collect(Collectors.toList());
        List<PurchaseOrderDTO.PurchaseCalcQtyDTO> purchaseOrderDetailList = purchaseOrderFeign.listAllPurchaseBySkuIdAndSupplier(new PurchaseOrderDTO.PurchaseCalcQtyParamsDTO(skuIdList, supplierIdList));
        List<MouldRefCalcQtyEntity> mouldRefCalcQtyList = new ArrayList<>();
        List<MouldRefundAgreementEntity> mouldRefundAgreementList = new ArrayList<>();
        List<MouldRefCalcQtyDetailEntity> mouldRefCalcQtyDetailList = new ArrayList<>();
        for (MouldRefCalcQtyDTO dto : mouldRefCalcQtyDTOList) {
            MouldRefCalcQtyEntity mouldRefCalcQty = new MouldRefCalcQtyEntity();
            MouldRefundAgreementEntity agreement = agreementEntityList.stream()
                    .filter(v -> v.getMouldDetailId().equals(dto.getDetailId()))
                    .findFirst()
                    .orElse(new MouldRefundAgreementEntity());
            agreement.setMouldDetailId(dto.getDetailId());
            mouldRefCalcQty.setMouldDetailId(dto.getDetailId());
            mouldRefCalcQty.setId(ObjectUtils.isEmpty(mouldRefCalcQty.getId()) ? IdWorker.getIdStr() : mouldRefCalcQty.getId());
            List<String> skuSupplierList = detailIdMap.get(dto.getDetailId());
            if (CollectionUtils.isEmpty(skuSupplierList)) {
                mouldRefCalcQty.setPurchaseQty(0);
                mouldRefCalcQty.setReceiveQty(0);
                mouldRefCalcQty.setStockInQty(0);
                mouldRefCalcQty.setCalcQty(0);
                agreement.setRefundStatus(MouldRefundStatusEnum.NOT_REACHED.getCode());
            } else {
                List<PurchaseOrderDTO.PurchaseCalcQtyDTO> dtos = purchaseOrderDetailList.stream()
                        .filter(v -> skuSupplierList.contains(v.getSkuId() + "-" + v.getSupplierId()))
                        .filter(v -> !v.getCreateTime().toLocalDate().isBefore(dto.getEnableDate()))
                        .collect(Collectors.toList());
                int purchaseQty = 0;
                int receiveQty = 0;
                int stockInQty = 0;
                for (PurchaseOrderDTO.PurchaseCalcQtyDTO calcQtyDTO : dtos) {
                    MouldRefCalcQtyDetailEntity detailEntity = getMouldRefCalcQtyDetailEntity(calcQtyDTO, mouldRefCalcQty);
                    mouldRefCalcQtyDetailList.add(detailEntity);
                    purchaseQty += calcQtyDTO.getPurchaseQty();
                    receiveQty += calcQtyDTO.getReceiveQty();
                    stockInQty += calcQtyDTO.getStockInQty();
                }
                mouldRefCalcQty.setPurchaseQty(purchaseQty);
                mouldRefCalcQty.setReceiveQty(receiveQty);
                mouldRefCalcQty.setStockInQty(stockInQty);
                int calcQty;
                if (RefundStandardEnum.PURCHASE_ORDERS.getCode().equals(dto.getRefundStandard())) {
                    calcQty = purchaseQty;
                } else if (RefundStandardEnum.RECEIVING.getCode().equals(dto.getRefundStandard())) {
                    calcQty = receiveQty;
                } else {
                    calcQty = stockInQty;
                }
                mouldRefCalcQty.setCalcQty(calcQty);
                if (MathUtil.compareTo(calcQty, agreement.getRefundOrderQty()) >= MathUtil.ZERO) {
                    agreement.setRefundStatus(MouldRefundStatusEnum.TO_BE_RETURNED.getCode());
                    sendNotice(dto, supplierMap, agreement);
                } else {
                    agreement.setRefundStatus(MouldRefundStatusEnum.NOT_REACHED.getCode());
                }
            }
            mouldRefundAgreementList.add(agreement);
            mouldRefCalcQtyList.add(mouldRefCalcQty);
        }
        ApplicationContextUtils.getBean(MouldRefCalcQtyServiceImpl.class).saveOrUpdateBatch(mouldRefCalcQtyList);
        mouldRefCalcQtyDetailService.saveOrUpdateBatch(mouldRefCalcQtyDetailList);
        mouldRefundAgreementService.updateBatchById(mouldRefundAgreementList);
    }

    private void sendNotice(MouldRefCalcQtyDTO dto, Map<String, String> supplierMap, MouldRefundAgreementEntity agreement) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", dto.getName());
        data.put("mouldNo", dto.getMouldNo());
        data.put("supplierName", Optional.ofNullable(supplierMap.get(dto.getSupplierId())).orElse(""));
        data.put("refundAmount", agreement.getRefundAmount());
        data.put("refundOrderQty", agreement.getRefundOrderQty());
        data.put("updateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        LoginUser user = UserContext.getDefaultLoginUser();
        mouldInfoService.mouldInfoNotice(NoticeEnum.MOULD_REFUND_REACH, data, new MouldInfoDTO.NoticeDTO(dto.getId(),
                dto.getName(), dto.getProductManagerId(), user.getUid(), user.getUserName()),"mouldRefundReach.ftl");
    }

    private Map<String, String> getSupplierMap(List<MouldRefCalcQtyDTO> mouldRefCalcQtyDTOList) {
        List<String> mouldSupplierIdList = mouldRefCalcQtyDTOList.stream()
                .map(MouldRefCalcQtyDTO::getSupplierId)
                .distinct().collect(Collectors.toList());
        List<SupplierEntity> supplierList = scmTaskFeign.getSupplierByIdList(mouldSupplierIdList);
        return supplierList.stream().collect(Collectors.toMap(SupplierEntity::getId, SupplierEntity::getName, (o1, o2) -> o1));
    }

    /**
     * 删除旧数据
     * @param detailIds id
     */
    private void removeOldData(List<String> detailIds) {
        List<MouldRefCalcQtyEntity> list = listByMouldDetailIdList(detailIds);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<String> calcQtyIds = list.stream().map(MouldRefCalcQtyEntity::getId).collect(Collectors.toList());
        removeByIds(calcQtyIds);
        mouldRefCalcQtyDetailService.remove(Wrappers.<MouldRefCalcQtyDetailEntity>lambdaQuery().in(MouldRefCalcQtyDetailEntity::getMainId, calcQtyIds));
    }

    /**
     * 构建明细实体
     *
     * @param calcQtyDTO         采购参数
     * @param mouldRefCalcQty    计算参数
     */
    private static MouldRefCalcQtyDetailEntity getMouldRefCalcQtyDetailEntity(PurchaseOrderDTO.PurchaseCalcQtyDTO calcQtyDTO, MouldRefCalcQtyEntity mouldRefCalcQty) {

        MouldRefCalcQtyDetailEntity detailEntity = new MouldRefCalcQtyDetailEntity();
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
