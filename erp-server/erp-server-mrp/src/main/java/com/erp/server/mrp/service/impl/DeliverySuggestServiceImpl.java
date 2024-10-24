package com.erp.server.mrp.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.mrp.dto.DeliverySuggestDTO;
import com.erp.model.mrp.dto.DeliverySuggestSysDTO;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.model.mrp.entity.DeliverySuggestEntity;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.model.mrp.enums.CreateTypeEnum;
import com.erp.model.mrp.enums.SuggestStatusEnum;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SkuMappingEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.WmsDeliveryPlanDTO;
import com.erp.model.wms.dto.WmsDeliveryPlanDetailDTO;
import com.erp.model.wms.entity.WmsDeliveryPlanDetailEntity;
import com.erp.model.wms.enums.DeliveryPlanTypeEnum;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.wms.feign.DeliveryPlanFeign;
import com.erp.server.mrp.mapper.DeliverySuggestMapper;
import com.erp.server.mrp.service.DeliverySuggestService;
import com.erp.server.mrp.service.DeliverySuggestSysService;
import com.erp.server.mrp.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
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
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @Autowired
    private DownloadTaskFeign downloadTaskFeign;

    @Autowired
    private DeliveryPlanFeign deliveryPlanFeign;

    @Autowired
    private DeliverySuggestSysService deliverySuggestSysService;


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DeliverySuggestDTO.AddDTO addDTO) {
        DeliverySuggestEntity deliverySuggestEntity = new DeliverySuggestEntity();
        BeanMapperUtils.copy(addDTO, deliverySuggestEntity);

        // 数据处理
        handleData(deliverySuggestEntity);

        log.info("开始新增发货计划");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_S);
        deliverySuggestEntity.setCode(code);
        boolean save = super.save(deliverySuggestEntity);
        if(!save) {
            throw new ServiceException("发货计划保存失败");
        }
        return new BaseResultDTO.AddDTO(deliverySuggestEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DeliverySuggestDTO.UpdateDTO updateDTO) {
        DeliverySuggestEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "发货计划"));
        DeliverySuggestEntity deliverySuggestEntity =  BeanMapperUtils.map(DeliverySuggestEntity.class, updateDTO);

        // 数据处理
        handleData(deliverySuggestEntity);
        log.info("编辑 开始修改发货计划数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(deliverySuggestEntity);
        if(!save) {
            throw new ServiceException("发货计划保存失败");
        }

        //保存系统值
        DeliverySuggestSysDTO.AddDTO dto = new DeliverySuggestSysDTO.AddDTO();
        BeanMapperUtils.copy(old,dto);
        deliverySuggestSysService.add(dto);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<DeliverySuggestDTO.ListDTO> paging(PagingDTO<DeliverySuggestDTO.PagingParamDTO> pagingDTO) {
        pagingDTO.getParams().setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<DeliverySuggestDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        //数据处理
        handleList(pageData.getRecords());
        return new PagingVO(pageData);
    }

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

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/deliverySuggestTemplate.xlsx";
        String excelName = "template.xlsx";
        ExcelUtil.downloadTemplate(path,excelName,response);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO locking(String id) {
        DeliverySuggestEntity old = super.getById(id);
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "发货建议"));
        if (!StrUtil.equals(old.getStatus(), SuggestStatusEnum.DRAFT.getCode())) {
            throw new ServiceException(ApiError.ERROR_SUGGEST_LOCKING);
        }
        //更新成待确认状态
        old.setStatus(SuggestStatusEnum.WAIT_CONFIRM.getCode());
        this.updateById(old);

        // 操作日志
        String msg = StrUtil.format("锁定了发货建议");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DELIVERY_SUGGEST.getCode(), old.getId(), "锁定");
        return BatchResultDTO.success(old.getId(), old.getCode(), OperationTypeEnum.LOCKING);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO confirm(String id) {
        DeliverySuggestEntity old = super.getById(id);
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "发货建议"));
        if (!StrUtil.equals(old.getStatus(), SuggestStatusEnum.WAIT_CONFIRM.getCode())) {
            throw new ServiceException(ApiError.ERROR_SUGGEST_CONFIRM);
        }
        //更新成完成状态
        old.setStatus(SuggestStatusEnum.FINISH.getCode());
        this.updateById(old);

        // 操作日志
        String msg = StrUtil.format("确认了发货建议");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DELIVERY_SUGGEST.getCode(), old.getId(), "确认");
        return BatchResultDTO.success(old.getId(), old.getCode(), OperationTypeEnum.CONFIRM);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO invalid(String id,String remark) {
        DeliverySuggestEntity old = super.getById(id);
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "发货建议"));
        //草稿和待确认支持作废
        if (!Arrays.asList(SuggestStatusEnum.DRAFT.getCode(),SuggestStatusEnum.WAIT_CONFIRM.getCode()).contains(old.getStatus())) {
            throw new ServiceException(ApiError.ERROR_SUGGEST_INVALID);
        }
        if (old.getInvalidStatus()) {
            throw new ServiceException(ApiError.ERROR_98012);
        }
        //更新成作废状态
        old.setInvalidStatus(Boolean.TRUE);
        old.setInvalidRemark(remark);
        this.updateById(old);

        // 操作日志
        String msg = StrUtil.format("作废了发货建议");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DELIVERY_SUGGEST.getCode(), old.getId(), "作废");
        return BatchResultDTO.success(old.getId(), old.getCode(), OperationTypeEnum.CONFIRM);
    }

    @Override
    public Boolean export(DeliverySuggestDTO.PagingParamDTO pagingParamDTO) {
        downloadTaskFeign.saveDownloadTask("发货建议", FileTaskEventEnum.EXPORT_MRP_DELIVERY_SUGGESTION_ENTITY.getCode(), pagingParamDTO);
        return Boolean.TRUE;
    }

    @Override
    public DeliverySuggestDTO.ViewPushDeliveryPlanDTO viewPushDeliveryPlan(List<String> ids) {
        List<DeliverySuggestEntity> deliverySuggestList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(deliverySuggestList)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        DeliverySuggestDTO.ViewPushDeliveryPlanDTO viewPushDeliveryPlanDTO = new DeliverySuggestDTO.ViewPushDeliveryPlanDTO();
        //店铺id集合
        List<String> shopIdList = deliverySuggestList.stream().map(DeliverySuggestEntity::getShopId).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoList = FeignQuery.getByIds(ShopInfoEntity.class, shopIdList);

        //sku映射
        List<String> skuIdList = deliverySuggestList.stream().map(obj -> obj.getSkuId()).distinct().collect(Collectors.toList());
        List<SkuMappingEntity> list = FeignQuery.create(SkuMappingEntity.class)
                .in(SkuMappingEntity::getProductSkuId, skuIdList).
                eq(SkuMappingEntity::getIsExpire,Boolean.FALSE).list();
        
        //sku映射的listing
        List<String> listingIdList = list.stream().filter(obj -> StrUtil.isNotBlank(obj.getListingId()))
                .map(SkuMappingEntity::getListingId).distinct().collect(Collectors.toList());
        List<ListingInfoEntity> listingList = FeignQuery.getByIds(ListingInfoEntity.class, listingIdList);

        //产品信息
        List<ProductDetailEntity> skuList = FeignQuery.getByIds(ProductDetailEntity.class, skuIdList);

        long count = deliverySuggestList.stream().map(DeliverySuggestEntity::getShopId).distinct().count();
        //校验
        if (count > MathUtil.ONE) {
            throw new ServiceException("下推发货计划店铺必须一致");
        }
        DeliverySuggestEntity entity = deliverySuggestList.get(0);
        viewPushDeliveryPlanDTO.setShopId(entity.getShopId());
        viewPushDeliveryPlanDTO.setType(DeliveryPlanTypeEnum.FBA.getCode());
        viewPushDeliveryPlanDTO.setDeliveryDate(entity.getSuggestDeliveryDate());
        viewPushDeliveryPlanDTO.setLogisticsMethod(entity.getLogisticsMethod());
        viewPushDeliveryPlanDTO.setLogisticsMethodName(LogisticsMethodEnum.getName(entity.getLogisticsMethod()));
        //店铺信息
        ShopInfoEntity shopInfoEntity = shopInfoList.stream().filter(obj -> StrUtil.equals(obj.getId(), entity.getShopId())).findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(shopInfoEntity)) {
            viewPushDeliveryPlanDTO.setShopName(shopInfoEntity.getName());
            viewPushDeliveryPlanDTO.setCountry(shopInfoEntity.getDictCountryCode());
            viewPushDeliveryPlanDTO.setCountryName(shopInfoEntity.getCountryName());
            viewPushDeliveryPlanDTO.setWarehouseId(shopInfoEntity.getWarehouseId());
            viewPushDeliveryPlanDTO.setWarehouseName(shopInfoEntity.getWarehouseName());
        }
        List<DeliverySuggestDTO.ViewPushDeliveryPlanDetailDTO> detailList = new ArrayList<>();

        Map<String, List<DeliverySuggestEntity>> map = deliverySuggestList.stream().collect(Collectors.groupingBy(DeliverySuggestEntity::getSkuId));
        for (Map.Entry<String, List<DeliverySuggestEntity>> entry : map.entrySet()) {
            List<DeliverySuggestEntity> value = entry.getValue();
            DeliverySuggestEntity suggestEntity = value.get(0);

            DeliverySuggestDTO.ViewPushDeliveryPlanDetailDTO detailDTO = new DeliverySuggestDTO.ViewPushDeliveryPlanDetailDTO();
            BeanMapperUtils.copy(suggestEntity,detailDTO);
            //sku信息
            ProductDetailEntity productDetailEntity = skuList.stream().filter(obj -> StrUtil.equals(obj.getId(), suggestEntity.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            detailDTO.setSkuNo(productDetailEntity.getSkuNo());
            detailDTO.setProductName(productDetailEntity.getName());
            //发货建议
            List<DeliverySuggestDTO.DeliverySuggestInfoDTO> suggestInfoList = new ArrayList<>();
            for (DeliverySuggestEntity deliverySuggestEntity : value) {
                DeliverySuggestDTO.DeliverySuggestInfoDTO deliverySuggestInfoDTO = new DeliverySuggestDTO.DeliverySuggestInfoDTO();
                deliverySuggestInfoDTO.setSourceId(deliverySuggestEntity.getId());
                deliverySuggestInfoDTO.setSourceCode(deliverySuggestEntity.getCode());
                deliverySuggestInfoDTO.setPlanDeliveryQty(deliverySuggestEntity.getSuggestDeliveryQty());
                suggestInfoList.add(deliverySuggestInfoDTO);
            }
            detailDTO.setDeliverySuggestList(suggestInfoList);

            //sku映射表
            SkuMappingEntity skuMappingEntity = list.stream().filter(obj -> StrUtil.equals(obj.getProductSkuId(), suggestEntity.getShopId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(skuMappingEntity)) {
                //listing信息
                List<ListingInfoEntity> detailListingList = listingList.stream().filter(obj -> StrUtil.equals(skuMappingEntity.getListingId(), obj.getId())).collect(Collectors.toList());
                for (ListingInfoEntity listingInfoEntity : detailListingList) {
                    DeliverySuggestDTO.ViewPushDeliveryPlanDetailDTO newDTO = new DeliverySuggestDTO.ViewPushDeliveryPlanDetailDTO();
                    BeanMapperUtils.copy(detailDTO,newDTO);
                    newDTO.setMSKu(listingInfoEntity.getPlatformSkuNo());
                    newDTO.setFnSku(listingInfoEntity.getPlatformFnSku());
                    newDTO.setAsin(listingInfoEntity.getPlatformSpuNo());
                    newDTO.setPlatformSkuName(listingInfoEntity.getPlatformSkuName());
                    detailList.add(newDTO);
                }
            } else {
                detailList.add(detailDTO);
            }
        }
        viewPushDeliveryPlanDTO.setDetailList(detailList);
        return viewPushDeliveryPlanDTO;
    }

    @Override
    public Boolean pushDeliveryPlan(DeliverySuggestDTO.AddPushDeliveryPlanDTO deliveryPlanDTO) {
        WmsDeliveryPlanDTO.AddDTO addDTO = new WmsDeliveryPlanDTO.AddDTO();
        addDTO.setShopId(deliveryPlanDTO.getShopId());
        addDTO.setType(DeliveryPlanTypeEnum.FBA.getCode());
        addDTO.setPlanDeliveryDate(deliveryPlanDTO.getDeliveryDate());
        addDTO.setToWarehouseId(deliveryPlanDTO.getWarehouseId());
        List<WmsDeliveryPlanDetailDTO.AddDTO> detailList =  new ArrayList<>();
        for (DeliverySuggestDTO.PushDeliveryPlanDetailDTO detailDTO: deliveryPlanDTO.getDetailList()) {
            WmsDeliveryPlanDetailDTO.AddDTO addDetailDTO = new WmsDeliveryPlanDetailDTO.AddDTO();
            addDetailDTO.setMSKU(detailDTO.getMSKu());
            addDetailDTO.setFnSku(detailDTO.getFnSku());
            addDetailDTO.setAsin(detailDTO.getAsin());
            addDetailDTO.setPlatformSkuName(detailDTO.getPlatformSkuName());
            addDetailDTO.setPlatformSku(detailDTO.getMSKu());
            addDetailDTO.setSkuId(detailDTO.getSkuId());
            addDetailDTO.setQty(detailDTO.getPlanDeliveryQty());
            //来源信息
            List<WmsDeliveryPlanDetailDTO.SourceJsonDTO> sourceJsonDTOList = BeanMapperUtils.copyList(WmsDeliveryPlanDetailDTO.SourceJsonDTO.class, detailDTO.getDeliverySuggestList());
            addDetailDTO.setSourceJsonList(sourceJsonDTOList);
            detailList.add(addDetailDTO);
        }
        addDTO.setDetailList(detailList);
        deliveryPlanFeign.addDeliveryPlan(addDTO);
        return Boolean.TRUE;
    }

    @Override
    public BatchResultDTO updateRemark(String id, String remark) {
        DeliverySuggestEntity old = super.getById(id);
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "补货计划"));
        //草稿和待确认支持更新备注
        if (!Arrays.asList(SuggestStatusEnum.DRAFT.getCode(),SuggestStatusEnum.WAIT_CONFIRM.getCode()).contains(old.getStatus())) {
            throw new ServiceException(ApiError.ERROR_SUGGEST_UPDATE_REMARK);
        }
        // 操作日志备注
        String msg = StrUtil.format("更新了补货计划备注，由【{}】更新为【{}】",old.getRemark(),remark);

        //更新备注
        old.setRemark(remark);
        this.updateById(old);

        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DELIVERY_SUGGEST.getCode(), old.getId(), "更新备注");
        return BatchResultDTO.success(old.getId(), old.getCode(), OperationTypeEnum.UPDATE);
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DeliverySuggestEntity deliverySuggestEntity) {
    // TODO 验证数据 &
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
        List<DictBasicEntity> dictBasicList = CollectionUtils.isEmpty(platformList) ? Collections.EMPTY_LIST : FeignQuery.create(DictBasicEntity.class).eq(DictBasicEntity::getType, DictBasicTypeEnum.SALES_PLATFORM.getType()).list();

        for (ReplenishmentSuggestionDTO.DeliverySuggestionDTO deliverySuggestionDTO : list) {

            //平台类型
            deliverySuggestionDTO.setPlatformTypeName(CfgRulePlatformTypeEnum.getName(deliverySuggestionDTO.getPlatformType()));

            //物流方式
            deliverySuggestionDTO.setLogisticsMethodName(LogisticsMethodEnum.getName(deliverySuggestionDTO.getLogisticsMethod()));

            //平台名称
            String platformName = dictBasicList.stream().filter(obj -> StrUtil.equals(obj.getValue(),deliverySuggestionDTO.getPlatform())).map(DictBasicEntity::getName).findFirst().orElse("");
            deliverySuggestionDTO.setPlatformName(platformName);

            //店铺名称
            String shopName = shopInfoList.stream().filter(obj -> StrUtil.equals(obj.getId(), deliverySuggestionDTO.getShopId())).map(ShopInfoEntity::getName).findFirst().orElse("");
            deliverySuggestionDTO.setShopName(shopName);

            //产品名称
            String productName = productDetailList.stream().filter(obj -> StrUtil.equals(obj.getId(), deliverySuggestionDTO.getSkuId())).map(ProductDetailEntity::getName).findFirst().orElse("");
            deliverySuggestionDTO.setProductName(productName);
            //创建名称
            deliverySuggestionDTO.setCreateTypeName(CreateTypeEnum.getNameByCode(deliverySuggestionDTO.getCreateType()));
        }
    }

    /**
     * 数据处理
     * @author will
     * @date 2024/10/23 11:23
     * @param list
     */
    private void handleList(List<DeliverySuggestDTO.ListDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //店铺
        List<String> shopIdList = list.stream().map(DeliverySuggestDTO.ListDTO::getShopId).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoList = FeignQuery.getByIds(ShopInfoEntity.class, shopIdList);

        //产品信息
        List<String> skuIdList = list.stream().map(DeliverySuggestDTO.ListDTO::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> skuList = FeignQuery.getByIds(ProductDetailEntity.class, skuIdList);

        //发货计划
        List<String> idList = list.stream().map(DeliverySuggestDTO.ListDTO::getId).distinct().collect(Collectors.toList());
        List<WmsDeliveryPlanDetailEntity> deliveryPlanDetailList = deliveryPlanFeign.listBySourceIdList(idList);


        for (DeliverySuggestDTO.ListDTO listDTO : list) {
            //数据类型
            listDTO.setDataTypeName(CreateTypeEnum.getNameByCode(listDTO.getDataType()));
            //物流方式
            listDTO.setLogisticsMethodName(LogisticsMethodEnum.getName(listDTO.getLogisticsMethod()));
            //物流方式（系统）
            listDTO.setSysLogisticsMethodName(LogisticsMethodEnum.getName(listDTO.getSysLogisticsMethod()));
            //状态名称
            listDTO.setStatusName(SuggestStatusEnum.getName(listDTO.getStatus()));
            //店铺名称
            ShopInfoEntity shopInfoEntity = shopInfoList.stream().filter(obj -> StrUtil.equals(obj.getId(), listDTO.getShopId())).findFirst().orElse(new ShopInfoEntity());
            listDTO.setShopName(shopInfoEntity.getName());
            listDTO.setCountryName(shopInfoEntity.getCountryName());
            //sku
            ProductDetailEntity productDetailEntity = skuList.stream().filter(obj -> StrUtil.equals(obj.getId(), listDTO.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            listDTO.setSkuNo(productDetailEntity.getSkuNo());
            listDTO.setProductName(productDetailEntity.getName());
            //发货计划
            WmsDeliveryPlanDetailEntity wmsDeliveryPlanDetailEntity = deliveryPlanDetailList.stream().filter(obj -> {
                long count = BeanUtil.copyToList(JSONUtil.parseArray(obj.getSourceJson()), WmsDeliveryPlanDetailDTO.SourceJsonDTO.class).stream().filter(e -> StrUtil.equals(e.getSourceId(), listDTO.getId())).count();
                if (count > 0) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(wmsDeliveryPlanDetailEntity)) {
                listDTO.setDeliveryPlanId(wmsDeliveryPlanDetailEntity.getId());
                listDTO.setDeliveryPlanCode(wmsDeliveryPlanDetailEntity.getCode());
                //已发数量
                Integer qty = BeanUtil.copyToList(JSONUtil.parseArray(wmsDeliveryPlanDetailEntity.getSourceJson()), WmsDeliveryPlanDetailDTO.SourceJsonDTO.class).stream().filter(e -> StrUtil.equals(e.getSourceId(), listDTO.getId())).map(WmsDeliveryPlanDetailDTO.SourceJsonDTO::getPlanDeliveryQty).findFirst().orElse(MathUtil.ZERO);
                listDTO.setHasDeliveryPlanQty(qty);
            }
        }
    }
}
