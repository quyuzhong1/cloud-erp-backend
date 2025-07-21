package com.erp.server.mrp.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.mrp.dto.DeliverySuggestDTO;
import com.erp.model.mrp.dto.PurchaseSuggestIndependentDTO;
import com.erp.model.mrp.dto.PurchaseSuggestMergeDTO;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.model.mrp.entity.PurchaseSuggestEntity;
import com.erp.model.mrp.entity.PurchaseSuggestMergeEntity;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.model.mrp.enums.CreateTypeEnum;
import com.erp.model.mrp.enums.SuggestStatusEnum;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.scm.dto.PurchaseApplicationDetailDTO;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.scm.feign.PurchaseApplicationDetailFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.mrp.mapper.PurchaseSuggestIndependentMapper;
import com.erp.server.mrp.service.PurchaseSuggestIndependentService;
import com.erp.server.mrp.service.PurchaseSuggestService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private PurchaseApplicationDetailFeign purchaseApplicationDetailFeign;

    @Resource
    private PurchaseSuggestService purchaseSuggestService;


    @Override
    public List<PurchaseSuggestIndependentDTO.ListDTO> list(PurchaseSuggestIndependentDTO.ListParamDTO params) {
        List<PurchaseSuggestIndependentDTO.ListDTO> list = baseMapper.list(params);
        handleList(list);
        return list;
    }

    @Override
    public PagingVO<ReplenishmentSuggestionDTO.PurchaseSuggestionDTO> listPurchaseSuggestion(PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> params) {
        params.getParams().setPermissionSql(params.getPermissionSql());
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
        downloadTaskFeign.saveExportTask("采购建议", FileTaskEventEnum.EXPORT_MRP_PURCHASE_SUGGESTION_ENTITY.getCode(), pagingParamDTO);
        return Boolean.TRUE;
    }

    @Override
    public List<PurchaseSuggestIndependentDTO.IndependentFrameDTO> viewIndependentFrame(String id) {
        PurchaseSuggestMergeEntity suggestMergeEntity = this.getById(id);
        if (ObjectUtil.isEmpty(suggestMergeEntity) || Boolean.TRUE.equals(suggestMergeEntity.getIsMerge())) {
            throw new ServiceException(ApiError.ERROR_98004);
        }  //采购建议
        List<String> sourceIdList = Stream.of(suggestMergeEntity).filter(obj -> CollectionUtils.isNotEmpty(obj.getSourceIdJson()))
                .flatMap(obj -> Stream.of(obj.getSourceIdJson().stream().map(Object::toString).toArray(String[]::new)))
                .distinct().collect(Collectors.toList());
        List<PurchaseSuggestEntity> purchaseSuggestList = purchaseSuggestService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(purchaseSuggestList)) {
            throw new ServiceException("采购建议不存在");
        }
        //bom信息
        List<String> skuIdList = purchaseSuggestList.stream().map(PurchaseSuggestEntity::getParentSkuId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuList = CollUtil.isEmpty(skuIdList) ? Collections.emptyList() : plmTaskFeign.listHistoryBomChildBySkuIds(skuIdList);
        bomChildrenSkuList = bomChildrenSkuList.stream().filter(obj->
                BomTypeEnum.COMBINATION.getType().equals(obj.getType())
                && CharSequenceUtil.equals(obj.getBomVersion(), purchaseSuggestList.get(0).getBomVersion())
        ).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(bomChildrenSkuList)) {
            throw new ServiceException("选择数据非组合品");
        }
        //补货建议明细id
        String replenishSuggestDetailId = purchaseSuggestList.get(0).getSourceIdJson().get(0).toString();

        return getIndependentFrameDTOS(suggestMergeEntity, bomChildrenSkuList, replenishSuggestDetailId);
    }

    /**
     * 弹框数据
     * @Auther will
     * @Date 2025/1/15 17:49
     */
    private  List<PurchaseSuggestIndependentDTO.IndependentFrameDTO> getIndependentFrameDTOS(PurchaseSuggestMergeEntity suggestMergeEntity, List<BomChildrenSkuDTO> bomChildrenSkuList, String replenishSuggestDetailId) {
        List<PurchaseSuggestIndependentDTO.IndependentFrameDTO> mergeFrameDTOList = new ArrayList<>();
        PurchaseSuggestIndependentDTO.IndependentFrameDTO parentDTO = getIndependentParentFrameDTO(suggestMergeEntity, bomChildrenSkuList, replenishSuggestDetailId);
        mergeFrameDTOList.add(parentDTO);
        for (BomChildrenSkuDTO bomChildrenSkuDTO : bomChildrenSkuList) {
            PurchaseSuggestIndependentDTO.IndependentFrameDTO resultDTO = getIndependentChildFrameDTO(suggestMergeEntity, replenishSuggestDetailId, bomChildrenSkuDTO);
            mergeFrameDTOList.add(resultDTO);
        }
        return mergeFrameDTOList;
    }
    /**
     * 生成子件数据
     * @Auther will
     * @Date 2025/1/15 17:49
     */
    private  PurchaseSuggestIndependentDTO.IndependentFrameDTO getIndependentChildFrameDTO(PurchaseSuggestMergeEntity suggestMergeEntity, String replenishSuggestDetailId, BomChildrenSkuDTO bomChildrenSkuDTO) {
        PurchaseSuggestIndependentDTO.IndependentFrameDTO resultDTO = new PurchaseSuggestIndependentDTO.IndependentFrameDTO();
        resultDTO.setSkuId(bomChildrenSkuDTO.getSkuId());
        resultDTO.setSkuNo(bomChildrenSkuDTO.getSkuNo());
        resultDTO.setSuggestPurchaseQty(suggestMergeEntity.getSuggestPurchaseQty() * bomChildrenSkuDTO.getQuantity());
        resultDTO.setPlanPurchaseQty(suggestMergeEntity.getPlanPurchaseQty() * bomChildrenSkuDTO.getQuantity());
        resultDTO.setPurchaseStockUpQty(suggestMergeEntity.getPurchaseStockUpQty() * bomChildrenSkuDTO.getQuantity());
        resultDTO.setSourceId(replenishSuggestDetailId);
        return resultDTO;
    }
    /**
     * 生成父级数据
     * @Auther will
     * @Date 2025/1/15 17:48
     */
    private  PurchaseSuggestIndependentDTO.IndependentFrameDTO getIndependentParentFrameDTO(PurchaseSuggestMergeEntity suggestMergeEntity, List<BomChildrenSkuDTO> bomChildrenSkuList, String repleinshSuggestDetailId) {
        PurchaseSuggestIndependentDTO.IndependentFrameDTO parentDTO = new PurchaseSuggestIndependentDTO.IndependentFrameDTO();
        parentDTO.setCode(suggestMergeEntity.getCode());
        parentDTO.setSkuId(suggestMergeEntity.getSkuId());
        parentDTO.setSkuNo(bomChildrenSkuList.get(0).getParentSkuNo());
        parentDTO.setSuggestPurchaseQty(suggestMergeEntity.getSuggestPurchaseQty());
        parentDTO.setPlanPurchaseQty(suggestMergeEntity.getPlanPurchaseQty());
        parentDTO.setPurchaseStockUpQty(suggestMergeEntity.getPurchaseStockUpQty());
        parentDTO.setSourceId(repleinshSuggestDetailId);
        //说明
        String msg = CharSequenceUtil.format("{} = ",parentDTO.getSkuNo());
        for (BomChildrenSkuDTO skuDTO : bomChildrenSkuList) {
            String childMsg = CharSequenceUtil.format("{}*{}",skuDTO.getQuantity(), skuDTO.getSkuNo());
            msg = CharSequenceUtil.format("{} + {}",msg , childMsg);
        }
        parentDTO.setMsg(msg);
        return parentDTO;
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
        //采购建议
        List<String> sourceIdList = list.stream().filter(obj -> CollectionUtils.isNotEmpty(obj.getSourceIdJson()))
                .flatMap(obj -> Stream.of(obj.getSourceIdJson().stream().map(Object::toString).toArray(String[]::new)))
                .distinct().collect(Collectors.toList());
        List<PurchaseSuggestEntity> purchaseSuggestList = purchaseSuggestService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(purchaseSuggestList)) {
            throw new ServiceException("采购建议不存在");
        }
        //bom信息
        List<String> skuIdList = purchaseSuggestList.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getParentSkuId())).map(PurchaseSuggestEntity::getParentSkuId).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuList = CollUtil.isEmpty(skuIdList) ? Collections.emptyList() : plmTaskFeign.listHistoryBomChildBySkuIds(skuIdList);

        //平台信息
        List<String> platformList = list.stream().map(PurchaseSuggestIndependentDTO.ListDTO::getPlatform).distinct().collect(Collectors.toList());
        List<DictBasicEntity> dictBasicList = CollectionUtils.isEmpty(platformList) ? Collections.EMPTY_LIST : FeignQuery.create(DictBasicEntity.class).eq(DictBasicEntity::getType, DictBasicTypeEnum.SALES_PLATFORM.getType()).list();

        //币种信息
        List<String> currencyIdList = list.stream().map(PurchaseSuggestIndependentDTO.ListDTO::getCurrency).distinct().collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyIdList);

        //店铺信息
        List<String> shopIdList = list.stream().map(PurchaseSuggestIndependentDTO.ListDTO::getShopId).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopList = FeignQuery.getByIds(ShopInfoEntity.class, shopIdList);


        //采购申请单信息
        List<String> ids = list.stream().filter(obj -> CharSequenceUtil.equals(obj.getStatus(), SuggestStatusEnum.FINISH.getCode())).map(PurchaseSuggestIndependentDTO.ListDTO::getId).distinct().collect(Collectors.toList());
        List<PurchaseApplicationDetailDTO.PurchaseApplicationDTO> purchaseApplicationList = CollUtil.isEmpty(ids) ? Collections.emptyList() : purchaseApplicationDetailFeign.listByMergeIdList(ids);

        for (PurchaseSuggestIndependentDTO.ListDTO listDTO : list) {

            //是否是组合品
            long count = bomChildrenSkuList.stream().filter(obj ->
                    CharSequenceUtil.equals(obj.getBomVersion(), listDTO.getBomVersion())
                    && StrUtil.equals(obj.getParentSkuId(), listDTO.getSkuId())
                    && BomTypeEnum.COMBINATION.getType().equals(obj.getType())
            ).count();
            listDTO.setIsCombination(count > MathUtil.ZERO ? Boolean.TRUE : Boolean.FALSE);

            //币别
            String currencySymbol = currencyList.stream().filter(c -> c.getId().equals(listDTO.getCurrency())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSymbol())).orElse("￥");
            listDTO.setCurrencySymbol(currencySymbol);

            //补货建议id
            String sourceId = purchaseSuggestList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), listDTO.getSourceIdJson().get(0).toString())).map(PurchaseSuggestEntity::getReplenishmentSuggestionId).findFirst().orElse(null);
            listDTO.setSourceId(sourceId);

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

            //采购申请
            PurchaseApplicationDetailDTO.PurchaseApplicationDTO purchaseApplicationDTO = purchaseApplicationList.stream().filter(obj ->
                ObjectUtil.isNotEmpty(obj.getSourceJson()) && JSONUtil.toList(obj.getSourceJson(), PurchaseSuggestMergeDTO.PushSourceDTO.class).stream().anyMatch(e -> CharSequenceUtil.equals(e.getId(), listDTO.getId()))).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(purchaseApplicationDTO)) {
                listDTO.setPurchaseApplicationCode(purchaseApplicationDTO.getCode());
                listDTO.setIsPush(Boolean.TRUE);
                listDTO.setIsPushName("已下推");
            } else {
                listDTO.setIsPush(Boolean.FALSE);
                listDTO.setIsPushName("未下推");
            }
        }
    }
}
