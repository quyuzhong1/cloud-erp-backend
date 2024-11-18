package com.erp.server.mrp.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.mrp.dto.DeliverySuggestDTO;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.model.mrp.entity.DeliverySuggestEntity;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.model.mrp.enums.CreateTypeEnum;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.server.mrp.mapper.DeliverySuggestMapper;
import com.erp.server.mrp.service.DeliverySuggestService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 发货计划 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-27
 */
@Slf4j
@Service
public class DeliverySuggestServiceImpl extends SuperServiceImpl<DeliverySuggestMapper, DeliverySuggestEntity> implements DeliverySuggestService {




    @Override
    public List<DeliverySuggestDTO.ListDTO> list(DeliverySuggestDTO.ListParamDTO params) {
        List<DeliverySuggestDTO.ListDTO> list = baseMapper.list(params);
        handleList(list);
        return list;
    }

    @Override
    public List<DeliverySuggestEntity> listByReplenishmentId(String detailId) {
        return list(Wrappers.<DeliverySuggestEntity>lambdaQuery().eq(DeliverySuggestEntity::getSourceId, detailId));
    }

    @Override
    public PagingVO<ReplenishmentSuggestionDTO.DeliverySuggestionDTO> listDeliverySuggestion(PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> params) {
        Page<ReplenishmentSuggestionDTO.DeliverySuggestionDTO> pagingVO = baseMapper.pagingExportDeliverySuggestion(new Page<>(params.getCurrPage(), params.getPageSize()), params.getParams());
        if (CollectionUtils.isEmpty(pagingVO.getRecords())) {
            throw new ServiceException("未找到采购计划数据");
        }
        handleExport(pagingVO.getRecords());
        return new PagingVO<>(pagingVO);
    }



    /**
     * 导出处理
     * @author will
     * @date 2024/9/8 12:21
     * @param list
     */
    private void handleExport (List<ReplenishmentSuggestionDTO.DeliverySuggestionDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<String> skuIdList = list.stream().map(ReplenishmentSuggestionDTO.DeliverySuggestionDTO::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailList = FeignQuery.getByIds(ProductDetailEntity.class, skuIdList);


        //所有店铺
        List<ShopInfoEntity> shopInfoList = FeignQuery.list(ShopInfoEntity.class);

        //平台信息
        List<String> platformList = list.stream().map(ReplenishmentSuggestionDTO.DeliverySuggestionDTO::getPlatform).distinct().collect(Collectors.toList());
        List<DictBasicEntity> dictBasicList = CollectionUtils.isEmpty(platformList) ? Collections.emptyList() : FeignQuery.create(DictBasicEntity.class).eq(DictBasicEntity::getType, DictBasicTypeEnum.SALES_PLATFORM.getType()).list();

        for (ReplenishmentSuggestionDTO.DeliverySuggestionDTO deliverySuggestionDTO : list) {

            //平台类型
            deliverySuggestionDTO.setPlatformTypeName(CfgRulePlatformTypeEnum.getName(deliverySuggestionDTO.getPlatformType()));

            //物流方式
            deliverySuggestionDTO.setLogisticsMethodName(LogisticsMethodEnum.getName(deliverySuggestionDTO.getLogisticsMethod()));

            //平台名称
            String platformName = dictBasicList.stream().filter(obj -> CharSequenceUtil.equals(obj.getValue(),deliverySuggestionDTO.getPlatform())).map(DictBasicEntity::getName).findFirst().orElse("");
            deliverySuggestionDTO.setPlatformName(platformName);

            //店铺名称
            String shopName = shopInfoList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), deliverySuggestionDTO.getShopId())).map(ShopInfoEntity::getName).findFirst().orElse("");
            deliverySuggestionDTO.setShopName(shopName);

            //产品名称
            String productName = productDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), deliverySuggestionDTO.getSkuId())).map(ProductDetailEntity::getName).findFirst().orElse("");
            deliverySuggestionDTO.setProductName(productName);
            //创建名称
            deliverySuggestionDTO.setCreateTypeName(CreateTypeEnum.getNameByCode(deliverySuggestionDTO.getCreateType()));
        }
    }

    private void handleList(List<DeliverySuggestDTO.ListDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (DeliverySuggestDTO.ListDTO listDTO : list) {
            listDTO.setCreateTypeName(CreateTypeEnum.getNameByCode(listDTO.getCreateType()));
            listDTO.setLogisticsMethodName(LogisticsMethodEnum.getName(listDTO.getLogisticsMethod()));
        }
    }
}
