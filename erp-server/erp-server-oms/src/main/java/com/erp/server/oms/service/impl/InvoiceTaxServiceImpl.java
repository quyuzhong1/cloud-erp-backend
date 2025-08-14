package com.erp.server.oms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.InvoiceTaxDTO;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.entity.InvoiceTaxEntity;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.server.oms.mapper.InvoiceTaxMapper;
import com.erp.server.oms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 发票税务信息 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-04-07
 */
@Slf4j
@Service
public class InvoiceTaxServiceImpl extends SuperServiceImpl<InvoiceTaxMapper, InvoiceTaxEntity> implements InvoiceTaxService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private SkuMappingService skuMappingService;
    @Lazy
    @Resource
    private SoB2cReceiverService soB2cReceiverService;
    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO addOrUpdate(InvoiceTaxDTO.UpdateDTO addOrUpdateDTO) {

        InvoiceTaxEntity invoiceTaxEntity =  BeanMapperUtils.map(InvoiceTaxEntity.class, addOrUpdateDTO);
        // 数据处理
        handleData(invoiceTaxEntity);
        boolean save = super.saveOrUpdate(invoiceTaxEntity);
        if(!save) {
            throw new ServiceException("发票税务信息保存失败");
        }
        //修改买家发票地址
        if (CharSequenceUtil.isNotBlank(addOrUpdateDTO.getSoId())){
            soB2cReceiverService.updateInvoiceAddress(addOrUpdateDTO.getSoId(),addOrUpdateDTO.getInvoiceAddress());
        }
        // 记录主单操作日志
        log.info("编辑 开始记录发票税务信息日志数据，id：【{}】", invoiceTaxEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), invoiceTaxEntity.getId(), "发票税务信息");
        operateLogService.addModuleOperateLogByObj(invoiceTaxEntity, invoiceTaxEntity, null, invoiceTaxEntity.getId(), msg);
        return BatchResultDTO.success(invoiceTaxEntity.getId(), invoiceTaxEntity.getId(), "生成发票税务信息成功");
    }

    @Override
    public InvoiceTaxDTO.ViewDTO view(String listingId) {
        InvoiceTaxEntity taxEntity = getByListingId(listingId);
        if (ObjUtil.isEmpty(taxEntity)) {
            return  new InvoiceTaxDTO.ViewDTO();
        }
        return BeanUtil.toBean(taxEntity, InvoiceTaxDTO.ViewDTO.class);
    }

    @Override
    public List<InvoiceTaxEntity> listByListingIdList(List<String> listingIdList) {
        if (CollUtil.isEmpty(listingIdList)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(InvoiceTaxEntity::getListingId, listingIdList).list();
    }

    @Override
    public Boolean checkInvoiceTax(InvoiceTaxEntity invoiceTaxEntity) {
        if (ObjUtil.isEmpty(invoiceTaxEntity)) {
            return Boolean.FALSE;
        }
        if (CharSequenceUtil.isBlank(invoiceTaxEntity.getInvoiceProductName()) ||
                CharSequenceUtil.isBlank(invoiceTaxEntity.getInvoiceHsCode()) ||
                CharSequenceUtil.isBlank(invoiceTaxEntity.getSameStateTaxCode()) ||
                CharSequenceUtil.isBlank(invoiceTaxEntity.getDiffStateTaxCode()) ||
                CharSequenceUtil.isBlank(invoiceTaxEntity.getUnit())) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importUpdate(List<InvoiceTaxDTO.UpdateDTO> invoiceTaxList) {
        if (CollUtil.isEmpty(invoiceTaxList)) {
            return;
        }
        List<String> listingIdList = invoiceTaxList.stream().map(InvoiceTaxDTO.UpdateDTO::getListingId).distinct().collect(Collectors.toList());
        List<InvoiceTaxEntity> oldTaxList = listByListingIdList(listingIdList);
        Map<String, InvoiceTaxEntity> map = oldTaxList.stream().collect(Collectors.toMap(InvoiceTaxEntity::getListingId, Function.identity()));

        List<InvoiceTaxEntity> addOrUpdateList = BeanUtil.copyToList(invoiceTaxList, InvoiceTaxEntity.class);
        for (InvoiceTaxEntity entity : addOrUpdateList) {
            InvoiceTaxEntity oldEntity = map.get(entity.getListingId());
            if (ObjUtil.isNotEmpty(oldEntity)) {
                entity.setId(oldEntity.getId());
            }
        }
        super.saveOrUpdateBatch(addOrUpdateList);
    }

    @Override
    public List<InvoiceTaxDTO.UpdateDTO> invoiceAddressView(BaseIdsDTO.IdsDTO dto) {
        if (Objects.isNull(dto) || CollUtil.isEmpty(dto.getIds())){
            return Collections.emptyList();
        }
        List<InvoiceTaxDTO.UpdateDTO> updateDTOS = baseMapper.invoiceAddressView(dto.getIds());
        return null;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(InvoiceTaxEntity invoiceTaxEntity) {
        if(CharSequenceUtil.isBlank(invoiceTaxEntity.getListingId()) && (CharSequenceUtil.isBlank(invoiceTaxEntity.getPlatformSkuNo()) ||  CharSequenceUtil.isBlank(invoiceTaxEntity.getPlatform()) ||  CharSequenceUtil.isBlank(invoiceTaxEntity.getShopId()))) {
            throw new ServiceException("listingId和平台SKU、平台、店铺不能全部为空");
        }
        String listingId = invoiceTaxEntity.getListingId();
        if (CharSequenceUtil.isBlank(listingId)) {
            // 查询该店铺所有平台sku
            ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
            paramDTO.setPlatform(invoiceTaxEntity.getPlatform());
            paramDTO.setShopIdList(Collections.singletonList(invoiceTaxEntity.getShopId()));
            paramDTO.setType(RuleTypeEnum.PLATFORM.getCode());
            paramDTO.setPlatformSkuNoList(Collections.singletonList(invoiceTaxEntity.getPlatformSkuNo()));
            // 所有包含历史映射关系
            List<ListingInfoWithSkuMappingDTO> listDto = skuMappingService.findListDto(paramDTO);
            if (CollUtil.isNotEmpty(listDto)) {
                listingId = listDto.get(0).getListingId();
            }
        }
        if (CharSequenceUtil.isBlank(listingId)) {
            throw new ServiceException(ApiError.ERROR_LISTING_NOT_EXIST);
        }
        invoiceTaxEntity.setListingId(listingId);
        InvoiceTaxEntity old = getByListingId(listingId);
        if (ObjUtil.isNotEmpty(old)) {
            invoiceTaxEntity.setId(old.getId());
        }
    }

    /**
     * 根据发票清单id查询
     * @param listingId
     * @return
     */
    private InvoiceTaxEntity getByListingId(String listingId) {
        return lambdaQuery().eq(InvoiceTaxEntity::getListingId, listingId).last("limit 1").one();
    }
}
