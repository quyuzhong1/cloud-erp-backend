package com.erp.server.mrp.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
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
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.dto.FileExcelDTO;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.mrp.dto.*;
import com.erp.model.mrp.dto.excel.DeliverySuggestImportExcelDTO;
import com.erp.model.mrp.entity.CfgRuleWarehouseEntity;
import com.erp.model.mrp.entity.DeliverySuggestEntity;
import com.erp.model.mrp.entity.ReplenishmentSuggestionEntity;
import com.erp.model.mrp.enums.*;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SkuMappingEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.wms.dto.WmsDeliveryPlanDTO;
import com.erp.model.wms.dto.WmsDeliveryPlanDetailDTO;
import com.erp.model.wms.entity.WmsDeliveryPlanDetailEntity;
import com.erp.model.wms.enums.DeliveryPlanTypeEnum;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.model.wms.enums.VitualWarehouseChannelTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.DeliveryPlanFeign;
import com.erp.server.mrp.listener.DeliverySuggestImportExcelListener;
import com.erp.server.mrp.mapper.DeliverySuggestMapper;
import com.erp.server.mrp.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 补货计划 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-27
 */
@Slf4j
@Service
public class DeliverySuggestServiceImpl extends SuperServiceImpl<DeliverySuggestMapper, DeliverySuggestEntity> implements DeliverySuggestService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private DeliveryPlanFeign deliveryPlanFeign;

    @Resource
    private DeliverySuggestSysService deliverySuggestSysService;

    @Resource
    private HistoryImportRecordService historyImportRecordService;

    @Resource
    private ReplenishmentSuggestionService replenishmentSuggestionService;

    @Resource
    private SysDictFeign sysDictFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private CfgRuleWarehouseService cfgRuleWarehouseService;

    @Resource
    private CfgRuleWarehouseDetailService cfgRuleWarehouseDetailService;


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DeliverySuggestDTO.AddDTO addDTO) {
        DeliverySuggestEntity deliverySuggestEntity = new DeliverySuggestEntity();
        BeanMapperUtils.copy(addDTO, deliverySuggestEntity);

        // 数据处理
        handleData(deliverySuggestEntity);
        log.info("开始新增补货计划");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_S);
        deliverySuggestEntity.setCode(code);
        boolean save = super.save(deliverySuggestEntity);
        if(!save) {
            throw new ServiceException("补货计划保存失败");
        }
        //保存系统值
        addDeliverySuggestSys (deliverySuggestEntity);

        // 操作日志
        String msg = StrUtil.format("新建了补货计划【编号：{}】",code);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DELIVERY_SUGGEST.getCode(), deliverySuggestEntity.getId(), "");
        return new BaseResultDTO.AddDTO(deliverySuggestEntity.getId(), code);
    }

    @Override
    public void addDeliverySuggestSys (DeliverySuggestEntity deliverySuggestEntity) {
        //保存系统值
        DeliverySuggestSysDTO.AddDTO dto = new DeliverySuggestSysDTO.AddDTO();
        BeanMapperUtils.copy(deliverySuggestEntity,dto);
        dto.setSourceId(deliverySuggestEntity.getId());
        dto.setSourceType(SourceTypeEnum.DELIVERY_SUGGESTION.getCode());
        deliverySuggestSysService.add(dto);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DeliverySuggestDTO.UpdateDTO updateDTO) {
        DeliverySuggestEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "补货计划"));
        if (!Arrays.asList(SuggestStatusEnum.DRAFT.getCode(),SuggestStatusEnum.WAIT_CONFIRM.getCode()).contains(old.getStatus()) || old.getInvalidStatus()) {
            throw new ServiceException(ApiError.ERROR_SUGGEST_UPDATE);
        }
        DeliverySuggestEntity deliverySuggestEntity =  BeanMapperUtils.map(DeliverySuggestEntity.class, updateDTO);
        deliverySuggestEntity.setSourceId(old.getSourceId());
        // 数据处理
        handleData(deliverySuggestEntity);
        log.info("编辑 开始修改补货计划数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(deliverySuggestEntity);
        if(!save) {
            throw new ServiceException("补货计划保存失败");
        }
        //操作日志
        operateLogService.addModuleOperateLogByObj(old, deliverySuggestEntity, ModuleTypeEnum.DELIVERY_SUGGEST.getCode(), deliverySuggestEntity.getId(), "", "");
        return Boolean.TRUE;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean importUpdate(DeliverySuggestDTO.ImportUpdateDTO updateDTO) {
        DeliverySuggestEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "补货计划"));
        if (!Arrays.asList(SuggestStatusEnum.DRAFT.getCode(),SuggestStatusEnum.WAIT_CONFIRM.getCode()).contains(old.getStatus()) || old.getInvalidStatus()) {
            throw new ServiceException(ApiError.ERROR_SUGGEST_UPDATE);
        }
        DeliverySuggestEntity deliverySuggestEntity =  BeanMapperUtils.map(DeliverySuggestEntity.class, updateDTO);
        log.info("编辑 开始修改补货计划数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(deliverySuggestEntity);
        if(!save) {
            throw new ServiceException("补货计划保存失败");
        }
        //操作日志
        operateLogService.addModuleOperateLogByObj(old, deliverySuggestEntity, ModuleTypeEnum.DELIVERY_SUGGEST.getCode(), deliverySuggestEntity.getId(), "", "");
        return Boolean.TRUE;
    }

    @Override
    public void deliverySuggestInvalid() {
       LocalDate now = LocalDate.now();
       List<DeliverySuggestEntity> list =  baseMapper.listFinishDeliverySuggest(now.minusDays(30L));
       if (CollectionUtils.isEmpty(list)) {
           return;
       }
        //补货计划
        List<String> ids = list.stream().map(DeliverySuggestEntity::getId).collect(Collectors.toList());
        List<WmsDeliveryPlanDetailEntity> deliveryPlanDetailList = deliveryPlanFeign.listBySourceIdList(ids);
        List<String> idList = new ArrayList<>();
        for (DeliverySuggestEntity entity :list) {
            //补货计划
            WmsDeliveryPlanDetailEntity wmsDeliveryPlanDetailEntity = deliveryPlanDetailList.stream().filter(obj -> {
                    long count = BeanUtil.copyToList(JSONUtil.parseArray(obj.getSourceJson()), WmsDeliveryPlanDetailDTO.SourceJsonDTO.class).stream().filter(e -> StrUtil.equals(e.getSourceId(), entity.getId())).count();
                    if (count > 0) {
                        return Boolean.TRUE;
                    }
                    return Boolean.FALSE;
            }).findFirst().orElse(null);
            //以下推的发货建议数据直接跳过
            if (ObjectUtil.isNotEmpty(wmsDeliveryPlanDetailEntity)) {
                continue;
            }
            idList.add(entity.getId());
        }
        if (CollectionUtils.isEmpty(idList)) {
            return;
        }
        idList.stream().forEach(obj -> this.invalid(obj,"超期自动作废"));
    }

    @Override
    public List<DeliverySuggestDTO.DeliverySuggestWarehouseDTO> listOverseasWarehouse(List<String> ids) {
        List<DeliverySuggestEntity> deliverySuggestList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(deliverySuggestList)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "补货计划");
        }
        //非海外平台直接返回空
        long count = deliverySuggestList.stream().filter(obj -> !StrUtil.equals(obj.getPlatformType(), CfgRulePlatformTypeEnum.OVERSEAS.getCode())).count();
        if (count > MathUtil.ZERO) {
            return Collections.EMPTY_LIST;
        }
        CfgRuleWarehouseEntity  ruleWarehouseEntity = cfgRuleWarehouseService.getByPlatformType(CfgRulePlatformTypeEnum.OVERSEAS.getCode());
        if (ObjectUtil.isEmpty(ruleWarehouseEntity)) {
            return Collections.EMPTY_LIST;
        }
        List<String> shopIdList = deliverySuggestList.stream().map(DeliverySuggestEntity::getShopId).distinct().collect(Collectors.toList());

        List<ShopInfoEntity> shopList = FeignQuery.getByIds(ShopInfoEntity.class, shopIdList);
        List<String> dictPlatformList = shopList.stream().map(ShopInfoEntity::getDictPlatform).distinct().collect(Collectors.toList());
        List<CfgRuleWarehouseDetailDTO.ViewDTO> viewList = cfgRuleWarehouseDetailService.listViewByMainIdList(Arrays.asList(ruleWarehouseEntity.getId()));
        if (CollectionUtils.isEmpty(viewList)) {
            return Collections.EMPTY_LIST;
        }
        List<DeliverySuggestDTO.DeliverySuggestWarehouseDTO> resultList = new ArrayList<>();
        for (CfgRuleWarehouseDetailDTO.ViewDTO viewDTO : viewList) {
            if (!StrUtil.equals(CfgRuleWarehouseTypeEnum.OVERSEAS.getCode(),viewDTO.getWarehouseType())) {
                continue;
            }

            //按店铺
            if (StrUtil.equals(viewDTO.getChannelType(), VitualWarehouseChannelTypeEnum.SHOP.getCode())) {
                long shopCont = viewDTO.getChannelIdList().stream().filter(obj -> shopIdList.contains(obj)).count();
                if (shopCont > 0) {
                    resultList.add(new DeliverySuggestDTO.DeliverySuggestWarehouseDTO(viewDTO.getWarehouseId(),viewDTO.getWarehouseName()));
                }
                continue;
            }
            //按平台
            long platformCont = viewDTO.getChannelIdList().stream().filter(obj -> dictPlatformList.contains(obj)).count();
            if (platformCont > 0) {
                resultList.add(new DeliverySuggestDTO.DeliverySuggestWarehouseDTO(viewDTO.getWarehouseId(),viewDTO.getWarehouseName()));
            }
        }
        if (CollUtil.isNotEmpty(resultList)) {
            resultList = resultList.stream().distinct().collect(Collectors.toList());
        }
        return resultList;
    }

    @Override
    public PagingVO<DeliverySuggestDTO.ListDTO> paging(PagingDTO<DeliverySuggestDTO.PagingParamDTO> pagingDTO) {
        pagingDTO.getParams().setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<DeliverySuggestDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        //数据处理
        List<DeliverySuggestDTO.ListDTO> list = handleList(pageData.getRecords());
        pageData.setRecords(list);
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
            throw new ServiceException("未找到发货计划数据");
        }
        handleExport(pagingVO.getRecords());
        return new PagingVO<>(pagingVO);
    }

    @Override
    public void downloadTemplate(String platformType,HttpServletResponse response) {
        String path = "classpath:excel/deliverySuggestTemplate.xlsx";
        String excelName = CharSequenceUtil.equals(CfgRulePlatformTypeEnum.AMAZON.getCode(),platformType) ?  "本地发FBA_备货确认表.xlsx" : "本地发海外仓_备货确认表.xlsx";
        ExcelUtil.downloadTemplate(path,excelName,response);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO locking(String id) {
        DeliverySuggestEntity old = super.getById(id);
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "补货计划"));
        if (!StrUtil.equals(old.getStatus(), SuggestStatusEnum.DRAFT.getCode()) || old.getInvalidStatus()) {
            throw new ServiceException(ApiError.ERROR_SUGGEST_LOCKING);
        }
        //更新成待确认状态
        old.setStatus(SuggestStatusEnum.WAIT_CONFIRM.getCode());
        this.updateById(old);

        // 操作日志
        String msg = StrUtil.format("锁定了补货计划");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DELIVERY_SUGGEST.getCode(), old.getId(), "锁定");
        return BatchResultDTO.success(old.getId(), old.getCode(), OperationTypeEnum.LOCKING);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO confirm(String id) {
        DeliverySuggestEntity old = super.getById(id);
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "补货计划"));
        if (!StrUtil.equals(old.getStatus(), SuggestStatusEnum.WAIT_CONFIRM.getCode()) || old.getInvalidStatus()) {
            throw new ServiceException(ApiError.ERROR_SUGGEST_CONFIRM);
        }
        //更新成完成状态
        old.setStatus(SuggestStatusEnum.FINISH.getCode());
        old.setFinishDate(LocalDate.now());
        this.updateById(old);

        // 操作日志
        String msg = StrUtil.format("确认了补货计划");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DELIVERY_SUGGEST.getCode(), old.getId(), "确认");
        return BatchResultDTO.success(old.getId(), old.getCode(), OperationTypeEnum.CONFIRM);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO invalid(String id,String remark) {
        DeliverySuggestEntity old = super.getById(id);
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "补货计划"));
        Boolean isPush = isPushDeliveryPlan(id);
        if (isPush) {
            return BatchResultDTO.fail(old.getId(),old.getCode(),ApiError.ERROR_SUGGEST_INVALID.msg);
        }

        if (old.getInvalidStatus()) {
            return BatchResultDTO.fail(old.getId(),old.getCode(),ApiError.ERROR_98012.msg);
        }
        //创建人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        //更新成作废状态
        old.setInvalidStatus(Boolean.TRUE);
        old.setInvalidRemark(remark);
        old.setInvalidTime(LocalDateTime.now());
        old.setInvalidUserId(userInfo.getUid());
        old.setInvalidUserName(userInfo.getUserName());
        this.updateById(old);

        // 操作日志
        String msg = StrUtil.format("作废了补货计划");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DELIVERY_SUGGEST.getCode(), old.getId(), "作废");
        return BatchResultDTO.success(old.getId(), old.getCode(), OperationTypeEnum.CONFIRM);
    }


    @Override
    public Boolean export(DeliverySuggestDTO.PagingParamDTO pagingParamDTO) {
        String platformType = pagingParamDTO.getPlatformType();
        String fileName = "补货计划";
        switch (CfgRulePlatformTypeEnum.getEnum(platformType)){
            case AMAZON:
                fileName = "补货计划_本地发FBA";
                break;
            case OVERSEAS:
                fileName = "补货计划_本地发海外仓";
                break;
            case INTERNAL:
                fileName = "补货计划_本地备货";
                break;
            case B2B:
                fileName = "补货计划_B2B本地备货";
                break;
            default:
                ;
        }
        downloadTaskFeign.saveDownloadTask(fileName, FileTaskEventEnum.EXPORT_MRP_DELIVERY_SUGGESTION_ENTITY.getCode(), pagingParamDTO);
        return Boolean.TRUE;
    }

    @Override
    public DeliverySuggestDTO.ViewPushDeliveryPlanDTO viewPushDeliveryPlan(List<String> ids,String warehouseId) {
        List<DeliverySuggestEntity> deliverySuggestList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(deliverySuggestList)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        String codes = deliverySuggestList.stream().filter(obj -> !StrUtil.equals(obj.getStatus(), SuggestStatusEnum.FINISH.getCode()))
                .map(DeliverySuggestEntity::getCode).collect(Collectors.joining(","));
        if (StrUtil.isNotBlank(codes)) {
            throw new ServiceException(ApiError.ERROR_DELIVERY_SUGGEST_PUSH,codes);
        }
        String invalidCodes = deliverySuggestList.stream().filter(obj -> obj.getInvalidStatus().equals(Boolean.TRUE))
                .map(DeliverySuggestEntity::getCode).collect(Collectors.joining(","));
        if (StrUtil.isNotBlank(invalidCodes)) {
            throw new ServiceException(ApiError.ERROR_DELIVERY_SUGGEST_PUSH_INVALID,codes);
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

        //补货计划
        List<WmsDeliveryPlanDetailEntity> deliveryPlanDetailList = deliveryPlanFeign.listBySourceIdList(ids);

        DeliverySuggestEntity entity = deliverySuggestList.get(0);
        viewPushDeliveryPlanDTO.setShopId(entity.getShopId());
        boolean isOverseas = StrUtil.equals(deliverySuggestList.get(0).getPlatformType(), CfgRulePlatformTypeEnum.OVERSEAS.getCode());
        if (isOverseas) {
            //海外平台
            handleOverseas (viewPushDeliveryPlanDTO,entity,warehouseId);
        } else {
            long count = deliverySuggestList.stream().map(DeliverySuggestEntity::getShopId).distinct().count();
            //校验
            if (count > MathUtil.ONE) {
                throw new ServiceException("下推发货计划店铺必须一致");
            }
            //亚马逊平台数据处理
            handleAmazon (viewPushDeliveryPlanDTO,entity,shopInfoList);
        }
        List<DeliverySuggestDTO.ViewPushDeliveryPlanDetailDTO> detailList = new ArrayList<>();

        Map<String, List<DeliverySuggestEntity>> map = deliverySuggestList.stream().collect(Collectors.groupingBy(DeliverySuggestEntity::getSkuId));
        for (Map.Entry<String, List<DeliverySuggestEntity>> entry : map.entrySet()) {
            List<DeliverySuggestEntity> value = entry.getValue();
            DeliverySuggestEntity suggestEntity = value.get(0);

            DeliverySuggestDTO.ViewPushDeliveryPlanDetailDTO detailDTO = new DeliverySuggestDTO.ViewPushDeliveryPlanDetailDTO();
            BeanMapperUtils.copy(suggestEntity,detailDTO);

            Integer deliveryStockUpQty = value.stream().map(DeliverySuggestEntity::getDeliveryStockUpQty).reduce(MathUtil.ZERO, Integer::sum);
            detailDTO.setDeliveryStockUpQty(deliveryStockUpQty);
            detailDTO.setPlanDeliveryQty(deliveryStockUpQty);
            //sku信息
            ProductDetailEntity productDetailEntity = skuList.stream().filter(obj -> StrUtil.equals(obj.getId(), suggestEntity.getSkuId()))
                    .findFirst().orElse(new ProductDetailEntity());
            detailDTO.setSkuNo(productDetailEntity.getSkuNo());
            detailDTO.setProductName(productDetailEntity.getName());
            //补货计划
            List<DeliverySuggestDTO.DeliverySuggestInfoDTO> suggestInfoList = new ArrayList<>();
            for (DeliverySuggestEntity deliverySuggestEntity : value) {
                //补货计划
                WmsDeliveryPlanDetailEntity deliveryPlanDetail = deliveryPlanDetailList.stream().filter(obj -> {
                    long planCount = BeanUtil.copyToList(JSONUtil.parseArray(obj.getSourceJson()), WmsDeliveryPlanDetailDTO.SourceJsonDTO.class)
                            .stream().filter(e -> StrUtil.equals(e.getSourceId(),deliverySuggestEntity.getId())).count();
                    if (planCount > 0) {
                        return Boolean.TRUE;
                    }
                    return Boolean.FALSE;
                }).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(deliveryPlanDetail)) {
                    throw new ServiceException(StrUtil.format("发货建议【{}】已下推发货计划【{}】，不支持再次下推",deliverySuggestEntity.getCode(),deliveryPlanDetail.getCode()));
                }
                DeliverySuggestDTO.DeliverySuggestInfoDTO deliverySuggestInfoDTO = new DeliverySuggestDTO.DeliverySuggestInfoDTO();
                deliverySuggestInfoDTO.setSourceId(deliverySuggestEntity.getId());
                deliverySuggestInfoDTO.setSourceCode(deliverySuggestEntity.getCode());
                deliverySuggestInfoDTO.setPlanDeliveryQty(deliverySuggestEntity.getDeliveryStockUpQty());
                suggestInfoList.add(deliverySuggestInfoDTO);
            }
            detailDTO.setDeliverySuggestList(suggestInfoList);

            //sku映射表
            List<SkuMappingEntity> skuMappingList;

            if (isOverseas) {
                skuMappingList = list.stream().filter(obj ->
                        CharSequenceUtil.equals(obj.getWarehouseId(),warehouseId) && StrUtil.equals(obj.getProductSkuId(), suggestEntity.getSkuId()))
                        .collect(Collectors.toList());
            } else {
                skuMappingList = list.stream().filter(obj -> CharSequenceUtil.equals(obj.getShopId(),suggestEntity.getShopId())
                        && StrUtil.equals(obj.getProductSkuId(), suggestEntity.getSkuId())).collect(Collectors.toList());
            }
            handleOverseasSkuMapping(detailList,skuMappingList, listingList, detailDTO, suggestEntity);
        }
        viewPushDeliveryPlanDTO.setDetailList(detailList);
        return viewPushDeliveryPlanDTO;
    }

    /**
     * 处理sku映射
     * @author will
     * @date 2024/11/18 17:02
     * @param skuMappingList
     * @param listingList
     * @param detailDTO
     * @param suggestEntity
     * @return List<ViewPushDeliveryPlanDetailDTO>
     */
    private void  handleOverseasSkuMapping ( List<DeliverySuggestDTO.ViewPushDeliveryPlanDetailDTO> detailList,List<SkuMappingEntity> skuMappingList,List<ListingInfoEntity> listingList,
                                            DeliverySuggestDTO.ViewPushDeliveryPlanDetailDTO detailDTO, DeliverySuggestEntity suggestEntity) {
        if (CollectionUtils.isEmpty(skuMappingList)) {
            detailList.add(detailDTO);
            return;
        }
        for (SkuMappingEntity skuMappingEntity : skuMappingList) {
            //listing信息
            List<ListingInfoEntity> detailListingList = listingList.stream().filter(obj -> StrUtil.equals(skuMappingEntity.getListingId(), obj.getId()))
                    .collect(Collectors.toList());
            for (ListingInfoEntity listingInfoEntity : detailListingList) {
                DeliverySuggestDTO.ViewPushDeliveryPlanDetailDTO newDTO = new DeliverySuggestDTO.ViewPushDeliveryPlanDetailDTO();
                BeanMapperUtils.copy(detailDTO,newDTO);
                newDTO.setId(suggestEntity.getSkuId());
                newDTO.setMSKu(listingInfoEntity.getPlatformSkuNo());
                newDTO.setFnSku(listingInfoEntity.getPlatformFnSku());
                newDTO.setAsin(listingInfoEntity.getPlatformSpuNo());
                newDTO.setPlatformSkuName(listingInfoEntity.getPlatformSkuName());
                //清空计划发货数量，前端填写
                newDTO.setPlanDeliveryQty(null);
                detailList.add(newDTO);
            }
        }
        if (detailList.size() == MathUtil.ZERO) {
            detailList.stream().forEach(obj -> obj.setPlanDeliveryQty(detailDTO.getPlanDeliveryQty()));
        }
    }

    /**
     * 海外平台
     * @author will
     * @date 2024/11/14 9:50
     * @param viewPushDeliveryPlanDTO
     * @param entity
     */
    private void handleOverseas (DeliverySuggestDTO.ViewPushDeliveryPlanDTO viewPushDeliveryPlanDTO, DeliverySuggestEntity entity,String warehouseId) {
            viewPushDeliveryPlanDTO.setType(DeliveryPlanTypeEnum.THIRD_WAREHOUSE.getCode() );
            viewPushDeliveryPlanDTO.setTypeName( DeliveryPlanTypeEnum.THIRD_WAREHOUSE.getName());
            viewPushDeliveryPlanDTO.setDeliveryDate(entity.getSuggestDeliveryDate());
            viewPushDeliveryPlanDTO.setLogisticsMethod(entity.getLogisticsMethod());
            viewPushDeliveryPlanDTO.setLogisticsMethodName(LogisticsMethodEnum.getName(entity.getLogisticsMethod()));
            viewPushDeliveryPlanDTO.setWarehouseId(warehouseId);
    }

    /**
     * 处理亚马逊数据
     * @author will
     * @date 2024/11/14 9:45
     * @param viewPushDeliveryPlanDTO
     * @param entity
     * @param shopInfoList
     */
    private void handleAmazon (DeliverySuggestDTO.ViewPushDeliveryPlanDTO viewPushDeliveryPlanDTO, DeliverySuggestEntity entity,
                               List<ShopInfoEntity> shopInfoList) {
        viewPushDeliveryPlanDTO.setType(DeliveryPlanTypeEnum.FBA.getCode() );
        viewPushDeliveryPlanDTO.setTypeName( DeliveryPlanTypeEnum.FBA.getName());
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
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean pushDeliveryPlan(DeliverySuggestDTO.AddPushDeliveryPlanDTO deliveryPlanDTO) {
        WmsDeliveryPlanDTO.AddDTO addDTO = new WmsDeliveryPlanDTO.AddDTO();
        addDTO.setShopId(deliveryPlanDTO.getShopId());
        addDTO.setType(deliveryPlanDTO.getType());
        addDTO.setPlanDeliveryDate(deliveryPlanDTO.getDeliveryDate());
        addDTO.setToWarehouseId(deliveryPlanDTO.getWarehouseId());
        addDTO.setExpectLogisticsMethod(deliveryPlanDTO.getLogisticsMethod());
        addDTO.setExpectDeliveryDate(deliveryPlanDTO.getDeliveryDate());
        addDTO.setSourceType(SourceTypeEnum.DELIVERY_SUGGESTION.getCode());
        addDTO.setRemark(deliveryPlanDTO.getRemark());
        List<WmsDeliveryPlanDetailDTO.AddDTO> detailList =  new ArrayList<>();
        //建议
        List<DeliverySuggestDTO.DeliverySuggestInfoDTO> deliverySuggestList = new ArrayList<>();
        for (DeliverySuggestDTO.PushDeliveryPlanDetailDTO detailDTO: deliveryPlanDTO.getDetailList()) {
            WmsDeliveryPlanDetailDTO.AddDTO addDetailDTO = new WmsDeliveryPlanDetailDTO.AddDTO();
            addDetailDTO.setMSKU(detailDTO.getMSKu());
            addDetailDTO.setFnSku(detailDTO.getFnSku());
            addDetailDTO.setAsin(detailDTO.getAsin());
            addDetailDTO.setPlatformSkuName(detailDTO.getPlatformSkuName());
            addDetailDTO.setPlatformSku(detailDTO.getMSKu());
            addDetailDTO.setSkuId(detailDTO.getSkuId());
            addDetailDTO.setQty(detailDTO.getPlanDeliveryQty());
            if (StrUtil.isBlank(detailDTO.getMSKu()) || StrUtil.isBlank(detailDTO.getFnSku())) {
                continue;
            }
            //来源信息
            List<WmsDeliveryPlanDetailDTO.SourceJsonDTO> sourceJsonDTOList = BeanMapperUtils.copyList(WmsDeliveryPlanDetailDTO.SourceJsonDTO.class, detailDTO.getDeliverySuggestList());
            addDetailDTO.setSourceJsonList(sourceJsonDTOList);
            detailList.add(addDetailDTO);
            deliverySuggestList.addAll(detailDTO.getDeliverySuggestList());
        }
        //无明细数据则直接跳过
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException("未找到可新增的数据");
        }
        addDTO.setDetailList(detailList);
        BaseResultDTO.AddDTO addReturnDTO = deliveryPlanFeign.addDeliveryPlan(addDTO);

        //超期提醒
        deliverySuggestList.stream().distinct().forEach(obj -> {
            // 操作日志
            String msg = StrUtil.format("补货计划【编号:{}】,下推生成发货计划【发货计划编号:{}】",obj.getSourceCode(),addReturnDTO.getCode());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DELIVERY_SUGGEST.getCode(), obj.getSourceId(), "下推");
        });
        return Boolean.TRUE;
    }

    @Override
    public BatchResultDTO updateRemark(String id, String remark) {
        DeliverySuggestEntity old = super.getById(id);
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "补货计划"));
        //草稿和待确认支持更新备注
        if (!Arrays.asList(SuggestStatusEnum.DRAFT.getCode(),SuggestStatusEnum.WAIT_CONFIRM.getCode()).contains(old.getStatus()) || old.getInvalidStatus()) {
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

    @Override
    public void importDeliverySuggest(MultipartFile excelFile, String platformType, HttpServletResponse response) {
        DeliverySuggestImportExcelListener excelListenerUtil = new DeliverySuggestImportExcelListener();

        try {
            EasyExcel.read(excelFile.getInputStream(), DeliverySuggestImportExcelDTO.class, excelListenerUtil).headRowNumber(1).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<DeliverySuggestImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            return;
        }
        //导入数据处理
        List<DeliverySuggestImportExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<DeliverySuggestImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        //处理校验导入成功数据
        handleImport(successList, errorList,platformType);
        //导入文件名称
        String originalFilename = excelFile.getOriginalFilename();
        //上传正确数据
        upLoadSuccessExcel (originalFilename,successList,platformType);
        //导出错误数据
        exportErrorExcel (response,errorList);
    }


    /**
     * 上传正确数据
     * @author will
     * @date 2024/10/24 12:07
     * @param originalFilename
     * @param successList
     */
    private void upLoadSuccessExcel (String originalFilename, List<DeliverySuggestImportExcelDTO> successList,String platformType) {
        //全部为空则无需处理
        if (CollectionUtils.isEmpty(successList) ) {
            return;
        }
        String fileName = StrUtil.isBlank(originalFilename) ? "补货计划.xlsx" : originalFilename;
        String pathUrl = "excel/deliverySuggest.xlsx";
        FileExcelDTO.ExportFileDTO exportFileDTO = new FileExcelDTO.ExportFileDTO();
        exportFileDTO.setFileName(fileName);
        exportFileDTO.setPathUrl(pathUrl);
        List<Pair<Integer, List<?>>> sheetList = new ArrayList<>();
        sheetList.add(new Pair<>(MathUtil.ZERO,successList));
        exportFileDTO.setSheetList(sheetList);

        //添加导入记录
        HistoryImportRecordDTO.AddDTO dto = new HistoryImportRecordDTO.AddDTO();
        dto.setName(fileName);
        dto.setModule(SourceTypeEnum.DELIVERY_SUGGESTION.getCode());
        dto.setPlatformType(platformType);
        dto.setType(HistoryImportRecordTypeEnum.DELIVERY_SUGGESTION_CONFIRM.getCode());
        dto.setExportFileDTO(exportFileDTO);
        historyImportRecordService.add(dto);
    }

    /**
     * 导出错误数据
     * @author will
     * @date 2024/10/24 12:10
     * @param response
     * @param errorList
     */
    private void exportErrorExcel (HttpServletResponse response, List<DeliverySuggestImportExcelDTO> errorList) {
        if (CollectionUtils.isEmpty(errorList) ) {
            return;
        }
        List<Pair<Integer, List<?>>> pairList = new ArrayList<>();
        pairList.add(new Pair<>(MathUtil.ZERO,errorList));
        String name = "补货计划错误数据";
        StringBuffer sb = new StringBuffer();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        String excelPath = "excel/deliverySuggestError.xlsx";
        try {
            new ExcelPrintUtils().sheetPatchExport(pairList, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("信息导出出错 >>>>>{}", e);
            throw new ServiceException("补货计划错误数据导出失败");
        }
    }

    /**
     * 导入数据处理
     * @author will
     * @date 2024/10/24 10:53
     * @param successList
     * @param errorList
     */
    private void handleImport (List<DeliverySuggestImportExcelDTO> successList,List<DeliverySuggestImportExcelDTO> errorList,String platformType) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        //发货建议
        List<String> codeList = successList.stream().map(DeliverySuggestImportExcelDTO::getCode).distinct().collect(Collectors.toList());
        List<DeliverySuggestEntity> deliverySuggestList = this.listByCodeList(codeList);

        //记录错误数据
        List<DeliverySuggestImportExcelDTO>  wrongList = new ArrayList<>();
        //已存在的数据
        List<String> hasList = new ArrayList<>();
        for (DeliverySuggestImportExcelDTO excelDTO : successList) {
            List<String> errorMsgList = new ArrayList<>();
            //发货建议
            DeliverySuggestEntity deliverySuggestEntity = deliverySuggestList.stream().filter(obj -> StrUtil.equals(obj.getCode(), excelDTO.getCode())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(deliverySuggestEntity)) {
                errorMsgList.add("未找到发货建议");
            } else {
                if (!StrUtil.equals(platformType,deliverySuggestEntity.getPlatformType())) {
                    errorMsgList.add("发货建议数据不支持跨平台类型导入");
                }
                if (!StrUtil.equals(deliverySuggestEntity.getStatus(),SuggestStatusEnum.WAIT_CONFIRM.getCode())) {
                    errorMsgList.add("仅待确认数据支持导入");
                }
                if (deliverySuggestEntity.getInvalidStatus()) {
                    errorMsgList.add("已作废数据不支持导入");
                }
                if (hasList.contains(deliverySuggestEntity.getId())) {
                    errorMsgList.add("建议编码已导入，请勿重复导入");
                }
            }
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                //错误数据
                wrongList.add(excelDTO);
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
                continue;
            }
            DeliverySuggestDTO.ImportUpdateDTO updateDTO = new DeliverySuggestDTO.ImportUpdateDTO();
            updateDTO.setId(deliverySuggestEntity.getId());
            updateDTO.setPlanDeliveryQty(Integer.valueOf(excelDTO.getPlanDeliveryQty()));
            updateDTO.setActualDeliveryQty(Integer.valueOf(excelDTO.getActualDeliveryQty()));
            updateDTO.setDeliveryStockUpQty(Integer.valueOf(excelDTO.getDeliveryStockUpQty()));
            updateDTO.setRemark(excelDTO.getRemark());
            try {
                this.importUpdate(updateDTO);
            } catch (Exception e) {
                errorMsgList.add(e.getMessage());
            }
            //保存里面的验证
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                //错误数据
                wrongList.add(excelDTO);
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
                continue;
            }
            hasList.add(updateDTO.getId());
        }
        successList.removeAll(wrongList);
    }

    /**
     * 根据编码集合查询
     * @author will
     * @date 2024/10/24 11:09
     * @param codeList
     * @return List<DeliverySuggestEntity>
     */
    private List<DeliverySuggestEntity> listByCodeList (List<String> codeList) {
        if (CollectionUtils.isEmpty(codeList)) {
            return Collections.EMPTY_LIST;
        }
       return lambdaQuery().in(DeliverySuggestEntity::getCode,codeList).list();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(DeliverySuggestEntity deliverySuggestEntity) {
        //补货建议
        ReplenishmentSuggestionEntity entity = replenishmentSuggestionService.getById(deliverySuggestEntity.getSourceId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("补货建议不能为空");
        }
        deliverySuggestEntity.setSkuId(entity.getSkuId());
        deliverySuggestEntity.setCountry(entity.getCountry());
        deliverySuggestEntity.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
        deliverySuggestEntity.setShopId(entity.getShopId());
        deliverySuggestEntity.setPlatformType(entity.getPlatformType());
        deliverySuggestEntity.setPlatform(entity.getPlatform());
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

            //创建名称
            deliverySuggestionDTO.setDataTypeName(CreateTypeEnum.getNameByCode(deliverySuggestionDTO.getDataType()));
        }
    }

    /**
     * 数据处理
     * @author will
     * @date 2024/10/23 11:23
     * @param list
     */
    private List<DeliverySuggestDTO.ListDTO> handleList(List<DeliverySuggestDTO.ListDTO> list) {
        List<DeliverySuggestDTO.ListDTO> resultList = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return resultList;
        }
        //店铺
        List<String> shopIdList = list.stream().map(DeliverySuggestDTO.ListDTO::getShopId).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoList = FeignQuery.getByIds(ShopInfoEntity.class, shopIdList);

        //国家
        List<String> countryCodeList = shopInfoList.stream().map(ShopInfoEntity::getDictCountryCode).distinct().collect(Collectors.toList());
        List<DictCountryEntity> countryList = CollectionUtils.isEmpty(countryCodeList) ? new ArrayList<>() : sysDictFeign.listCountryByIds(countryCodeList);

        //补货计划
        List<String> idList = list.stream().map(DeliverySuggestDTO.ListDTO::getId).distinct().collect(Collectors.toList());
        List<WmsDeliveryPlanDetailEntity> deliveryPlanDetailList = deliveryPlanFeign.listBySourceIdList(idList);
        
        //平台数据
        List<String> platformList = list.stream().map(DeliverySuggestDTO.ListDTO::getPlatform).distinct().collect(Collectors.toList());
        List<DictBasicEntity> dictBasicList = CollectionUtils.isEmpty(platformList) ? Collections.EMPTY_LIST : FeignQuery.create(DictBasicEntity.class).eq(DictBasicEntity::getType, DictBasicTypeEnum.SALES_PLATFORM.getType()).list();

        //币种信息
        List<String> currencyIdList = list.stream().map(DeliverySuggestDTO.ListDTO::getCurrency).distinct().collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyIdList);

        Map<String, List<DeliverySuggestDTO.ListDTO>> map = list.stream().collect(Collectors.groupingBy(obj -> obj.getPlatform().concat(obj.getShopId())));
        for (Map.Entry<String, List<DeliverySuggestDTO.ListDTO>> entry : map.entrySet()) {
            List<DeliverySuggestDTO.ListDTO> value = entry.getValue();
            DeliverySuggestDTO.ListDTO parentListDTO = new DeliverySuggestDTO.ListDTO();

            //店铺名称
            ShopInfoEntity shopInfoEntity = shopInfoList.stream().filter(obj -> StrUtil.equals(obj.getId(), value.get(0).getShopId())).findFirst().orElse(new ShopInfoEntity());
            parentListDTO.setId(value.get(0).getShopId());
            parentListDTO.setShopId(value.get(0).getShopId());
            parentListDTO.setShopName(shopInfoEntity.getName());
            //平台信息
            parentListDTO.setPlatform(value.get(0).getPlatform());
            String platformName = dictBasicList.stream().filter(obj -> StrUtil.equals(obj.getValue(), value.get(0).getPlatform())).map(DictBasicEntity::getName).findFirst().orElse("");
            parentListDTO.setPlatformName(platformName);

            //国家
            DictCountryEntity dictCountry = countryList.stream().filter(obj -> obj.getId().equals(shopInfoEntity.getDictCountryCode())).findFirst().orElse(new DictCountryEntity());
            parentListDTO.setCountryName(dictCountry.getNameCn());
            parentListDTO.setCountryImgUrl(dictCountry.getFlagUrl());

            resultList.add(parentListDTO);
            for (DeliverySuggestDTO.ListDTO listDTO : value) {
                //数据类型
                listDTO.setDataTypeName(CreateTypeEnum.getNameByCode(listDTO.getDataType()));
                //物流方式
                listDTO.setLogisticsMethodName(LogisticsMethodEnum.getName(listDTO.getLogisticsMethod()));
                //物流方式（系统）
                listDTO.setSysLogisticsMethodName(LogisticsMethodEnum.getName(listDTO.getSysLogisticsMethod()));
                //状态名称
                listDTO.setStatusName(SuggestStatusEnum.getName(listDTO.getStatus()));
                //币别
                String currencySymbol = currencyList.stream().filter(c -> c.getId().equals(listDTO.getCurrency())).findFirst().
                        flatMap(obj -> Optional.ofNullable(obj.getSymbol())).orElse("￥");
                listDTO.setCurrencySymbol(currencySymbol);

                //以店铺id为父级id用于前端显示
                listDTO.setParentId(listDTO.getShopId());
                //补货计划
                WmsDeliveryPlanDetailEntity wmsDeliveryPlanDetailEntity = deliveryPlanDetailList.stream().filter(obj -> {
                    long count = BeanUtil.copyToList(JSONUtil.parseArray(obj.getSourceJson()), WmsDeliveryPlanDetailDTO.SourceJsonDTO.class).stream().filter(e -> StrUtil.equals(e.getSourceId(), listDTO.getId())).count();
                    if (count > 0) {
                        return Boolean.TRUE;
                    }
                    return Boolean.FALSE;
                }).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(wmsDeliveryPlanDetailEntity)) {
                    listDTO.setDeliveryPlanId(wmsDeliveryPlanDetailEntity.getMainId());
                    listDTO.setDeliveryPlanCode(wmsDeliveryPlanDetailEntity.getCode());
                    //已发数量
                    Integer qty = BeanUtil.copyToList(JSONUtil.parseArray(wmsDeliveryPlanDetailEntity.getSourceJson()), WmsDeliveryPlanDetailDTO.SourceJsonDTO.class).stream().filter(e -> StrUtil.equals(e.getSourceId(), listDTO.getId())).map(WmsDeliveryPlanDetailDTO.SourceJsonDTO::getPlanDeliveryQty).findFirst().orElse(MathUtil.ZERO);
                    listDTO.setHasDeliveryPlanQty(qty);
                    listDTO.setIsPush(Boolean.TRUE);
                    listDTO.setIsPushName("已下推");
                } else {
                    listDTO.setIsPush(Boolean.FALSE);
                    listDTO.setIsPushName("未下推");
                }
            }
            resultList.addAll(value);
        }
        return resultList;
    }

    /**
     * 是否下推发货计划
     * @author will
     * @date 2024/11/6 18:15
     * @return Boolean
     */
    private Boolean isPushDeliveryPlan(String id) {
        //补货计划
        List<WmsDeliveryPlanDetailEntity> deliveryPlanDetailList = deliveryPlanFeign.listBySourceIdList(Collections.singletonList(id));
        //补货计划
        WmsDeliveryPlanDetailEntity entity = deliveryPlanDetailList.stream().filter(obj -> {
            long count = BeanUtil.copyToList(JSONUtil.parseArray(obj.getSourceJson()), WmsDeliveryPlanDetailDTO.SourceJsonDTO.class).stream().filter(e -> StrUtil.equals(e.getSourceId(),id)).count();
            if (count > 0) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        }).findFirst().orElse(null);
        return ObjectUtil.isNotEmpty(entity) ? Boolean.TRUE : Boolean.FALSE;
    }
}
