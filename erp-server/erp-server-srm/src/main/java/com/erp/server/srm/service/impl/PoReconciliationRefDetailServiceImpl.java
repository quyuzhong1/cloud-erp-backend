package com.erp.server.srm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.DictBasicDTO;
import com.erp.model.scm.entity.PurchaseApplicationEntity;
import com.erp.model.scm.entity.PurchaseApplicationRefPoEntity;
import com.erp.model.scm.enums.DictBasicEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.srm.dto.PoReconciliationRefDetailDTO;
import com.erp.model.srm.entity.PoReconciliationDetailEntity;
import com.erp.model.srm.entity.PoReconciliationEntity;
import com.erp.model.srm.entity.PoReconciliationRefDetailEntity;
import com.erp.model.srm.enums.ConfirmStatusEnum;
import com.erp.model.srm.enums.PoReconciliationDetailEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.wms.enums.ReturnOrderSourceEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.scm.feign.ScmDictFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.srm.mapper.PoReconciliationRefDetailMapper;
import com.erp.server.srm.service.OperateLogService;
import com.erp.server.srm.service.PoReconciliationDetailScmService;
import com.erp.server.srm.service.PoReconciliationRefDetailService;
import com.erp.server.srm.service.PoReconciliationScmService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 采购对账单明细已对账信息 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-12-22
 */
@Slf4j
@Service
public class PoReconciliationRefDetailServiceImpl extends SuperServiceImpl<PoReconciliationRefDetailMapper, PoReconciliationRefDetailEntity> implements PoReconciliationRefDetailService {
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private PoReconciliationScmService poReconciliationScmService;

    @Resource
    private PoReconciliationDetailScmService poReconciliationDetailScmService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private ScmDictFeign scmDictFeign;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;
    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean srmUpdate(List<PoReconciliationRefDetailDTO.UpdateDTO> detailList,String poReconciliationId) {
        if (CollectionUtils.isEmpty(detailList)) {
            return Boolean.TRUE;
        }
        List<PoReconciliationRefDetailEntity> list =  BeanMapperUtils.copyList(PoReconciliationRefDetailEntity.class, detailList);

        //原明细数据被删除的需要清除mainId
        List<PoReconciliationRefDetailEntity> oldList = this.listPoReconciliationIdList(Collections.singletonList(poReconciliationId));
        List<String> deleteIds = getDeleteIds(list, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            //删除的数据
            super.removeByIds(deleteIds);

            List<PoReconciliationRefDetailEntity> deleteList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = deleteList.stream().map(obj -> new Pair<>(poReconciliationId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.PO_RECONCILIATION.getCode(),pairList,"编辑操作");
        }
        //更新数据处理
        handleUpdateData (list,poReconciliationId,Boolean.FALSE);

        log.info("编辑 开始修改采购对账单数据，id：【{}】", poReconciliationId);
        boolean save = super.saveOrUpdateBatch(list);
        if(!save) {
            throw new ServiceException("采购对账单明细保存失败");
        }
        //更新对账状态
        List<String> detailIdList = detailList.stream().map(PoReconciliationRefDetailDTO.UpdateDTO::getPoReconciliationDetailId).distinct().collect(Collectors.toList());
        poReconciliationDetailScmService.autoUpdateStatus(detailIdList);

        //更新主表对账金额
        poReconciliationScmService.updateAmount(poReconciliationId);
        return Boolean.TRUE;
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<PoReconciliationRefDetailDTO.ScmUpdateDTO> detailList, String poReconciliationId) {
        if (CollectionUtils.isEmpty(detailList)) {
            return Boolean.TRUE;
        }
        List<PoReconciliationRefDetailEntity> list =  BeanMapperUtils.copyList(PoReconciliationRefDetailEntity.class, detailList);

        //更新数据处理
        handleUpdateData (list,poReconciliationId,Boolean.TRUE);

        //原明细数据被删除的需要清除mainId
        List<PoReconciliationRefDetailEntity> oldList = this.listPoReconciliationIdList(Collections.singletonList(poReconciliationId));
        List<String> deleteIds = getDeleteIds(list, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            //删除的数据
            super.removeByIds(deleteIds);
            List<PoReconciliationRefDetailEntity> deleteList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = deleteList.stream().map(obj -> new Pair<>(poReconciliationId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.PO_RECONCILIATION.getCode(),pairList,"编辑操作");
        }
        log.info("编辑 开始修改采购对账单数据，id：【{}】", poReconciliationId);
        boolean save = super.saveOrUpdateBatch(list);
        if(!save) {
            throw new ServiceException("采购对账单明细保存失败");
        }
        //更新对账状态
        List<String> detailIdList = detailList.stream().map(PoReconciliationRefDetailDTO.ScmUpdateDTO::getPoReconciliationDetailId).distinct().collect(Collectors.toList());
        poReconciliationDetailScmService.autoUpdateStatus(detailIdList);

        //更新主表对账金额
        poReconciliationScmService.updateAmount(poReconciliationId);
        return Boolean.TRUE;
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<PoReconciliationRefDetailEntity> newList, List<PoReconciliationRefDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(PoReconciliationRefDetailEntity::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(PoReconciliationRefDetailEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
     * 根据采购对账单ID集合查询已对账明细
     * @author will
     * @date 2025/12/22 18:42
     * @param poReconciliationIdList
     * @return List<PoReconciliationRefDetailEntity>
     */
    @Override
    public List<PoReconciliationRefDetailEntity> listPoReconciliationIdList(List<String> poReconciliationIdList) {
        if (CollUtil.isEmpty(poReconciliationIdList)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(PoReconciliationRefDetailEntity::getPoReconciliationId,poReconciliationIdList).list();
    }

    @Override
    public List<PoReconciliationRefDetailEntity> listPoReconciliationDetailIdList(List<String> poReconciliationDetailIdList) {
        if (CollUtil.isEmpty(poReconciliationDetailIdList)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(PoReconciliationRefDetailEntity::getPoReconciliationDetailId,poReconciliationDetailIdList).list();
    }

    @Override
    public List<PoReconciliationRefDetailEntity> listBySourceCodeAndSku(String poReconciliationId,List<String> sourceCodeList, List<String> skuNOList) {
        return lambdaQuery()
                .eq(PoReconciliationRefDetailEntity::getPoReconciliationId,poReconciliationId)
                .in(PoReconciliationRefDetailEntity::getSourceCode,sourceCodeList)
                .in(PoReconciliationRefDetailEntity::getSkuNo,skuNOList)
                .list();
    }

    @Override
    public void batchAdd(List<PoReconciliationDetailEntity> poReconciliationDetailList, String id) {
        if (CollUtil.isEmpty(poReconciliationDetailList)) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_DETAIL_NOT_EXIST);
        }
        List<String> detailIdList = poReconciliationDetailList.stream().map(PoReconciliationDetailEntity::getId).distinct().collect(Collectors.toList());
        List<PoReconciliationRefDetailEntity> oldRefDetailList = this.listPoReconciliationDetailIdList(detailIdList);

        List<PoReconciliationRefDetailEntity> refDetailList = poReconciliationDetailList.stream().map(detail -> {

            //新增默认取可对账数量生成对账单明细
            Integer hasQty = oldRefDetailList.stream().filter(obj -> CharSequenceUtil.equals(detail.getId(), obj.getPoReconciliationDetailId())).map(PoReconciliationRefDetailEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
            if (hasQty >= detail.getQty()) {
                throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_DETAIL_HAS_IN_RECONCILIATION,detail.getSourceCode(),detail.getSkuNo());
            }
            PoReconciliationRefDetailEntity refDetailEntity = new PoReconciliationRefDetailEntity();
            BeanUtil.copyProperties(detail,refDetailEntity);
            refDetailEntity.setId(IdWorker.getIdStr());
            refDetailEntity.setPoReconciliationId(id);
            refDetailEntity.setPoReconciliationDetailId(detail.getId());
            refDetailEntity.setQty(detail.getQty() - hasQty);
            refDetailEntity.setTaxAmount(MathUtil.multiplyWithFour(new BigDecimal(refDetailEntity.getQty()),refDetailEntity.getTaxPrice()));
            refDetailEntity.setDiscountTaxAmount(refDetailEntity.getTaxAmount());
            return refDetailEntity;
        }).collect(Collectors.toList());
        boolean save = this.saveBatch(refDetailList);
        if (!save) {
            throw new ServiceException(ApiError.ERROR_1002);
        }
        //更新对账状态
        poReconciliationDetailScmService.autoUpdateStatus(detailIdList);
        //更新对账单主表对账金额
        poReconciliationScmService.updateAmount(id);
    }


    @Override
    public List<PoReconciliationRefDetailDTO.ViewDTO> viewDetail(PoReconciliationRefDetailDTO.PagingParamDTO dto) {
        List<PoReconciliationRefDetailDTO.ListDTO> list = this.baseMapper.listDetail(dto);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        //数据处理
        fillList(list, dto.getIsSrm());
        return BeanMapperUtils.copyList(PoReconciliationRefDetailDTO.ViewDTO.class, list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByPoReconciliationIdList(List<String> poReconciliationIdList) {
        if (CollUtil.isEmpty(poReconciliationIdList)) {
            return;
        }
        List<PoReconciliationRefDetailEntity> poReconciliationRefDetailList = this.listPoReconciliationIdList(poReconciliationIdList);
        if (CollUtil.isEmpty(poReconciliationRefDetailList)) {
            return;
        }
        List<String> idList = poReconciliationRefDetailList.stream().map(PoReconciliationRefDetailEntity::getId).distinct().collect(Collectors.toList());
        super.removeByIds(idList);

        //删除明细后，更新对账状态
        List<String> detailIdList = poReconciliationRefDetailList.stream().map(PoReconciliationRefDetailEntity::getPoReconciliationDetailId).distinct().collect(Collectors.toList());
        poReconciliationDetailScmService.autoUpdateStatus(detailIdList);
    }

    /**
     * 分页查询、 数据处理
     */
    @Override
    public void fillList(List<PoReconciliationRefDetailDTO.ListDTO> list, Boolean isSrm) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        //产品信息
        List<String> skuIdList = list.stream().map(PoReconciliationRefDetailDTO.ListDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);

        //结算方式
        List<DictBasicDTO> settleDictList = scmDictFeign.listDictByKey(DictBasicEnum.SUPPLIER_PAY_MODE.getType());

        //付款条件
        List<BaseDropDownDTO.DisabledDTO>  paymentConditionList =  scmTaskFeign.listPaymentCondition();

        //关联信息
        // 采购申请单id集合
        List<String> purchaseApplicationIds = Lists.newArrayList();
        List<String> purchaseOrderIds = list.stream().map(PoReconciliationRefDetailDTO.ListDTO::getPoId).distinct().collect(Collectors.toList());
        List<PurchaseApplicationRefPoEntity> refList = FeignQuery.create(PurchaseApplicationRefPoEntity.class).in(PurchaseApplicationRefPoEntity::getPurchaseOrderId,purchaseOrderIds).list();

        //币种信息
        List<String> currencyIdList = list.stream().map(PoReconciliationRefDetailDTO.ListDTO::getCurrency).collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyIdList);
        Integer index = MathUtil.ONE;
        for (PoReconciliationRefDetailDTO.ListDTO listDTO : list) {
            listDTO.setSourceTypeName(SourceTypeEnum.PO_RETURN.getCode().equals(listDTO.getSourceType()) ? ReturnOrderSourceEnum.getName(listDTO.getReturnSourceType()) : "采购入库");
            //对账单下的明细都是确认状态
            listDTO.setBusinessStatusName(ConfirmStatusEnum.CONFIRM.getName());
            listDTO.setTaxRate(MathUtil.multiplyWithTwo(listDTO.getTaxRate(),MathUtil.BigDecimal_100));
            listDTO.setTaxRateStr( CharSequenceUtil.format("{}%",listDTO.getTaxRate().stripTrailingZeros().toPlainString()));
            BigDecimal discountAmount = MathUtil.multiplyWithFour(listDTO.getTaxAmount(), listDTO.getDiscountRate());
            listDTO.setDiscountAmount(discountAmount);
            listDTO.setDiscountRate(MathUtil.multiplyWithTwo(listDTO.getDiscountRate(),MathUtil.BigDecimal_100));
            listDTO.setDiscountRateStr(CharSequenceUtil.format("{}%",listDTO.getDiscountRate().stripTrailingZeros().toPlainString()));
            listDTO.setDiscountTaxAmount(MathUtil.subtract(listDTO.getTaxAmount(),discountAmount).subtract(listDTO.getPrepayAmount()));

            //产品名称
            String productName = skuList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), listDTO.getSkuId())).findFirst()
                    .flatMap(obj -> Optional.ofNullable(obj.getSkuName())).orElse("");
            listDTO.setProductName(productName);

            //结算方式
            String settleDictName = settleDictList.stream().filter(obj -> CharSequenceUtil.equals(obj.getValue(), listDTO.getSettleDict())).findFirst()
                    .flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            listDTO.setSettleDictName(settleDictName);

            //付款条件名称
            String paymentConditionName = paymentConditionList.stream().filter(obj -> CharSequenceUtil.equals(obj.getCode(), listDTO.getPaymentCondition())).findFirst()
                    .flatMap(obj -> Optional.ofNullable(obj.getValue())).orElse("");
            listDTO.setPaymentConditionName(paymentConditionName);

            //srm仅展示退货单号来源的单号
            if (!SourceTypeEnum.PO_RETURN.getCode().equals(listDTO.getPoSourceType()) && isSrm) {
                listDTO.setPoSourceCode("");
            } else {
                // 采购申请单号
                if (CollUtil.isNotEmpty(refList) && CharSequenceUtil.isBlank(listDTO.getPoSourceType())) {
                    // 采购申请单明细id和采购订单明细id是多对多，可能存在多条
                    List<PurchaseApplicationRefPoEntity> filterRefList = refList.stream().filter(r -> {
                        if (Objects.equals(listDTO.getPoId(), r.getPurchaseOrderId())) {
                            return true;
                        }
                        return false;
                    }).collect(Collectors.toList());
                    if (CollUtil.isNotEmpty(filterRefList)) {
                        List<String> applicationIds = filterRefList.stream().map(PurchaseApplicationRefPoEntity::getPurchaseApplicationId).distinct().collect(Collectors.toList());
                        purchaseApplicationIds.addAll(applicationIds);
                        listDTO.setPurchaseApplicationIds(applicationIds);
                    }

                }
            }

            //币种符号
            String currencySymbol = currencyList.stream().filter(c -> c.getId().equals(listDTO.getCurrency())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSymbol())).orElse("");
            listDTO.setCurrencySymbol(currencySymbol);
            listDTO.setUnitName("Pcs");
            listDTO.setStatusName(PoReconciliationDetailEnum.StatusEnum.getNameByCode(listDTO.getStatus()));
            //备注
            listDTO.setIndex(index);
            index++;
        }
        if (CollUtil.isNotEmpty(purchaseApplicationIds)) {
            List<PurchaseApplicationEntity> purchaseApplicationList = FeignQuery.getByIds(PurchaseApplicationEntity.class,purchaseApplicationIds);
            // 采购申请单id和采购申请单对应map
            Map<String, PurchaseApplicationEntity> refMap = purchaseApplicationList.stream().collect(Collectors.toMap(PurchaseApplicationEntity::getId, Function.identity()));

            list.forEach(obj -> {
                if (CollUtil.isNotEmpty(obj.getPurchaseApplicationIds())) {
                    StringBuffer applicationCodes = new StringBuffer();
                    obj.getPurchaseApplicationIds().forEach(applicationId -> {
                        PurchaseApplicationEntity refEntity = refMap.get(applicationId);
                        if (Objects.nonNull(refEntity)) {
                            applicationCodes.append(refEntity.getCode()).append(",");
                        }
                    });
                    if (applicationCodes.toString().endsWith(",")) {
                        applicationCodes.deleteCharAt(applicationCodes.length() - 1);
                    }
                    obj.setPoSourceCode(applicationCodes.toString());
                }
            });
        }
    }

    /**
     * @description: 修改处理
     * @author Will
     * @date: 2024/1/20 16:55
     * @param list
     * @param poReconciliationId
     */
    private void handleUpdateData (List<PoReconciliationRefDetailEntity> list,String poReconciliationId,Boolean isScm) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //已存在对应明细
        List<String> detailIdList = list.stream().map(PoReconciliationRefDetailEntity::getId).collect(Collectors.toList());
        List<PoReconciliationRefDetailEntity> oldRefDetailList = this.listByIds(detailIdList);

        //查询相同来源明细id已对账数据
        List<String> poReconciliationDetailIdList = list.stream().map(PoReconciliationRefDetailEntity::getPoReconciliationDetailId).distinct().collect(Collectors.toList());
        List<PoReconciliationRefDetailEntity> poReconciliationRefDetailList = this.listPoReconciliationDetailIdList(poReconciliationDetailIdList);

        //对账单
        PoReconciliationEntity poReconciliationEntity = poReconciliationScmService.getById(poReconciliationId);
        if (ObjectUtils.isEmpty(poReconciliationEntity)) {
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_NOT_EXIST);
        }
        //对账单明细基础信息
        List<PoReconciliationDetailEntity> poReconciliationDetailList = poReconciliationDetailScmService.listByIds(poReconciliationDetailIdList);
        if (CollUtil.isEmpty(poReconciliationDetailList)) {
            log.error("采购对账单待对账明细不存在，mainId：{}", poReconciliationId);
            throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_DETAIL_NOT_EXIST);
        }
        Map<String, PoReconciliationDetailEntity> detailMap = poReconciliationDetailList.stream().collect(Collectors.toMap(PoReconciliationDetailEntity::getId, obj -> obj));

        //新增不需要添加新增SKU的日志
        List<String> addIdList = list.stream().filter(obj -> StrUtil.isBlank(obj.getId())).map(PoReconciliationRefDetailEntity::getId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(addIdList)) {
            List<Pair<String, String>> addPairList = oldRefDetailList.stream().filter(obj -> addIdList.contains(obj.getId())).map(obj -> new Pair<>(poReconciliationId, CharSequenceUtil.format("SKU【{}】",obj.getSkuNo()))).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("新增%s", ModuleTypeEnum.PO_RECONCILIATION.getCode(), addPairList, "编辑操作");
        }
        for (PoReconciliationRefDetailEntity entity : list) {
            //待对账明细信息
            PoReconciliationDetailEntity detailEntity = detailMap.get(entity.getPoReconciliationDetailId());
            if (ObjUtil.isEmpty(detailEntity)) {
                log.error("采购对账单明细不存在，id：{}", entity.getPoReconciliationDetailId());
                throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_DETAIL_NOT_EXIST);
            }
            //供应商、结算组织验证
            if (!CharSequenceUtil.equals(poReconciliationEntity.getSupplierId(),detailEntity.getSupplierId())
                    || !CharSequenceUtil.equals(poReconciliationEntity.getSettleOrgId(),detailEntity.getSettleOrgId())) {
                throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_ADD_DETAIL,poReconciliationEntity.getCode(),poReconciliationEntity.getSupplierName(),poReconciliationEntity.getSettleOrgName());
            }
            //已对账数量
            Integer hasReconciledQty = poReconciliationRefDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getPoReconciliationDetailId(), entity.getPoReconciliationDetailId()) && !CharSequenceUtil.equals(obj.getId(), entity.getId()))
                    .map(PoReconciliationRefDetailEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
            if (hasReconciledQty + entity.getQty() > detailEntity.getQty()) {
                throw new ServiceException(ApiError.ERROR_PO_RECONCILIATION_DETAIL_QTY_OVERFLOW,detailEntity.getSourceCode(),detailEntity.getSkuNo(),entity.getQty(), detailEntity.getQty() - hasReconciledQty );
            }
            //设置主表ID
            entity.setPoReconciliationId(poReconciliationId);
            //更新数据
            PoReconciliationRefDetailEntity old = poReconciliationRefDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), entity.getId())).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(old)) {
                entity.setId(old.getId());
            } else {
                String[] ignoreProperties = {"qty", "taxRate", "discountRate"," taxPrice", "taxAmount", "discountTaxAmount","prepayAmount"};
                BeanUtil.copyProperties(detailEntity,entity,ignoreProperties);
                entity.setId(IdWorker.getIdStr());
            }
            //srm需要更新税率、折扣、价税合计、折后价税合计
            if (isScm) {
                //税率
                BigDecimal taxRate = MathUtil.compareTo(entity.getTaxRate(), MathUtil.ZERO) == MathUtil.ZERO ? BigDecimal.ZERO : MathUtil.divide(entity.getTaxRate(), MathUtil.BigDecimal_100);
                entity.setTaxRate(taxRate);

                //折扣
                BigDecimal discountRate = MathUtil.compareTo(entity.getDiscountRate(), MathUtil.ZERO) == MathUtil.ZERO ? BigDecimal.ZERO : MathUtil.divide(entity.getDiscountRate(), MathUtil.BigDecimal_100);
                entity.setDiscountRate(discountRate);

                //价税合计
                entity.setTaxAmount(MathUtil.multiplyWithTwo(entity.getTaxPrice(),entity.getQty()));
                //折后价税合计
                BigDecimal discountAmount = MathUtil.multiplyWithFour(entity.getTaxAmount(), entity.getDiscountRate());
                entity.setDiscountTaxAmount(MathUtil.subtract(entity.getTaxAmount(),entity.getPrepayAmount()).subtract(discountAmount));
            }
            //更新操作日志
            if (ObjUtil.isNotEmpty(old)) {
                operateLogService.addModuleOperateLogByObj(old,entity, ModuleTypeEnum.PO_RECONCILIATION.getCode(),poReconciliationId,"",String.format("【%s】",old.getSkuNo()));
            }
        }
    }


}
