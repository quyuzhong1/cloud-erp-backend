package com.erp.server.mrp.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.mrp.dto.DeliverySuggestDTO;
import com.erp.model.mrp.dto.PurchaseSuggestIndependentDTO;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.model.mrp.entity.PurchaseSuggestMergeEntity;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.model.mrp.enums.CreateTypeEnum;
import com.erp.model.mrp.enums.SuggestStatusEnum;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.mrp.mapper.PurchaseSuggestIndependentMapper;
import com.erp.server.mrp.service.PurchaseSuggestIndependentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 建议采购 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-29
 */
@Slf4j
@Service
public class PurchaseSuggestIndependentServiceImpl extends SuperServiceImpl<PurchaseSuggestIndependentMapper, PurchaseSuggestMergeEntity> implements PurchaseSuggestIndependentService {
    @Resource
    private DownloadTaskFeign f;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;


    @Override
    public List<PurchaseSuggestIndependentDTO.ListDTO> list(PurchaseSuggestIndependentDTO.ListParamDTO params) {
        List<PurchaseSuggestIndependentDTO.ListDTO> list = baseMapper.list(params);
        handleList(list);
        return list;
    }

    @Override
    public PagingVO<ReplenishmentSuggestionDTO.PurchaseSuggestionDTO> listPurchaseSuggestion(PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> params) {
        Page<ReplenishmentSuggestionDTO.PurchaseSuggestionDTO> pagingVO = baseMapper.pagingExportPurchaseSuggestion(new Page<>(params.getCurrPage(), params.getPageSize()), params.getParams());
        if (CollectionUtils.isEmpty(pagingVO.getRecords())) {
            throw new ServiceException("未找到采购计划数据");
        }
        handleExport(pagingVO.getRecords());
        return new PagingVO<>(pagingVO);
    }

    @Override
    public PagingVO<PurchaseSuggestIndependentDTO.ListDTO> paging(PagingDTO<PurchaseSuggestIndependentDTO.PagingParamDTO> pagingDTO) {
        pagingDTO.getParams().setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<PurchaseSuggestIndependentDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        //数据处理
        handleList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public Boolean export(DeliverySuggestDTO.PagingParamDTO pagingParamDTO) {
        downloadTaskFeign.saveDownloadTask("采购建议", FileTaskEventEnum.EXPORT_MRP_PURCHASE_SUGGESTION_ENTITY.getCode(), pagingParamDTO);
        return Boolean.TRUE;
    }

    /**
     * 导出处理
     * @author will
     * @date 2024/9/8 12:21
     * @param list
     */
    private void handleExport (List<ReplenishmentSuggestionDTO.PurchaseSuggestionDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<String> skuIdList = list.stream().map(ReplenishmentSuggestionDTO.PurchaseSuggestionDTO::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailList = FeignQuery.getByIds(ProductDetailEntity.class, skuIdList);


        //所有店铺
        List<ShopInfoEntity> shopInfoList = FeignQuery.list(ShopInfoEntity.class);

        //平台信息
        List<String> platformList = list.stream().map(ReplenishmentSuggestionDTO.PurchaseSuggestionDTO::getPlatform).distinct().collect(Collectors.toList());
        List<DictBasicEntity> dictBasicList = CollectionUtils.isEmpty(platformList) ? Collections.emptyList() : FeignQuery.create(DictBasicEntity.class).eq(DictBasicEntity::getType, DictBasicTypeEnum.SALES_PLATFORM.getType()).list();

        for (ReplenishmentSuggestionDTO.PurchaseSuggestionDTO purchaseSuggestionDTO : list) {

            //平台类型
            purchaseSuggestionDTO.setPlatformTypeName(CfgRulePlatformTypeEnum.getName(purchaseSuggestionDTO.getPlatformType()));

            //物流方式
            purchaseSuggestionDTO.setLogisticsMethodName(LogisticsMethodEnum.getName(purchaseSuggestionDTO.getLogisticsMethod()));

            //平台名称
            String platformName = dictBasicList.stream().filter(obj -> CharSequenceUtil.equals(obj.getValue(),purchaseSuggestionDTO.getPlatform())).map(DictBasicEntity::getName).findFirst().orElse("");
            purchaseSuggestionDTO.setPlatformName(platformName);

            //店铺名称
            String shopName = shopInfoList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), purchaseSuggestionDTO.getShopId())).map(ShopInfoEntity::getName).findFirst().orElse("");
            purchaseSuggestionDTO.setShopName(shopName);

            //产品名称
            String productName = productDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), purchaseSuggestionDTO.getSkuId())).map(ProductDetailEntity::getName).findFirst().orElse("");
            purchaseSuggestionDTO.setProductName(productName);
            //创建名称
            purchaseSuggestionDTO.setDataTypeName(CreateTypeEnum.getNameByCode(purchaseSuggestionDTO.getDataType()));
        }
    }

    /**
     * 处理数据
     * @author will
     * @date 2024/9/9 11:55
     * @param list
     */
    private void handleList(List<PurchaseSuggestIndependentDTO.ListDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        //平台信息
        List<String> platformList = list.stream().map(PurchaseSuggestIndependentDTO.ListDTO::getPlatform).distinct().collect(Collectors.toList());
        List<DictBasicEntity> dictBasicList = CollectionUtils.isEmpty(platformList) ? Collections.EMPTY_LIST : FeignQuery.create(DictBasicEntity.class).eq(DictBasicEntity::getType, DictBasicTypeEnum.SALES_PLATFORM.getType()).list();

        //币种信息
        List<String> currencyIdList = list.stream().map(PurchaseSuggestIndependentDTO.ListDTO::getCurrency).distinct().collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyIdList);

        //店铺信息
        List<String> shopIdList = list.stream().map(PurchaseSuggestIndependentDTO.ListDTO::getShopId).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopList = FeignQuery.getByIds(ShopInfoEntity.class, shopIdList);

        for (PurchaseSuggestIndependentDTO.ListDTO listDTO : list) {

            //币别
            String currencySymbol = currencyList.stream().filter(c -> c.getId().equals(listDTO.getCurrency())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSymbol())).orElse("￥");
            listDTO.setCurrencySymbol(currencySymbol);

            //数据类型
            listDTO.setDataTypeName(CreateTypeEnum.getNameByCode(listDTO.getDataType()));
            //平台类型
            listDTO.setPlatformTypeName(CfgRulePlatformTypeEnum.getName(listDTO.getPlatformType()));
            //平台名称
            String platformName = dictBasicList.stream().filter(obj -> StrUtil.equals(obj.getValue(),listDTO.getPlatform())).map(DictBasicEntity::getName).findFirst().orElse("");
            listDTO.setPlatformName(platformName);
            //状态
            listDTO.setStatusName(SuggestStatusEnum.getName(listDTO.getStatus()));
            //物流方式
            listDTO.setLogisticsMethodName(LogisticsMethodEnum.getName(listDTO.getLogisticsMethod()));
            //物流方式（系统）
            listDTO.setSysLogisticsMethodName(LogisticsMethodEnum.getName(listDTO.getSysLogisticsMethod()));
            //店铺名称
            String shopName = shopList.stream().filter(obj -> StrUtil.equals(obj.getId(), listDTO.getShopId())).map(ShopInfoEntity::getName).findFirst().orElse("");
            listDTO.setShopName(shopName);
        }
    }
}
