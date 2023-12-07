package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.DictCityEntity;
import com.erp.model.sys.entity.ImlDictCityEntity;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.excel.ExportOverseasWarehouseInboundExcelDTO;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCancelInboundReq;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCreateInboundReq;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.OmsListingInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.convert.OverseasWarehouseInboundConverter;
import com.erp.server.wms.handler.ThirdWarehouseRegistry;
import com.erp.server.wms.mapper.OverseasWarehouseInboundMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 海外仓入库单 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2023-11-16
 */
@Slf4j
@Service
public class OverseasWarehouseInboundServiceImpl extends SuperServiceImpl<OverseasWarehouseInboundMapper, OverseasWarehouseInboundEntity> implements OverseasWarehouseInboundService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private CommonService commonService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private OverseasWarehouseInboundDetailService overseasWarehouseInboundDetailService;
    @Resource
    private FirstMileDeliveryService firstMileDeliveryService;
    @Resource
    private FirstMileDeliveryDetailService firstMileDeliveryDetailService;
    @Resource
    private WmsAttachmentService wmsAttachmentService;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private SysDictService sysDictService;
    @Resource
    private OverseasProviderWarehouseService overseasProviderWarehouseService;
    @Resource
    private OverseasTransferWarehouseService overseasTransferWarehouseService;
    @Resource
    private OverseasProviderService overseasProviderService;
    @Resource
    private ThirdWarehouseRegistry thirdWarehouseRegistry;
    @Resource
    private OmsListingInfoFeign omsListingInfoFeign;
    @Resource
    private TransferInfoService transferInfoService;
    @Resource
    private WarehouseService warehouseService;
    @Resource
    private DictBasicService dictBasicService;
    @Resource
    private FirstMileCartonDetailService firstMileCartonDetailService;
    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(OverseasWarehouseInboundDTO.AddDTO addDTO) {
        OverseasWarehouseInboundEntity oldEntity = this.getBySourceId(addDTO.getSourceId(),  OverseasInstockStatusEnum.CANCELED.getCode());
        if (null != oldEntity) {
            throw new ServiceException("该发货单的入库单已存在");
        }

        FirstMileDeliveryEntity deliveryEntity = firstMileDeliveryService.getById(addDTO.getSourceId());
        Optional.ofNullable(deliveryEntity).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "发货单"));
        if (!PackingStatusEnum.PACKING.getCode().equalsIgnoreCase(deliveryEntity.getPackingStatus())) {
            String msg = StrUtil.format("【{}】发货单未装箱完", deliveryEntity.getCode());
            throw new ServiceException(msg);
        }

        // 发货单明细
        List<FirstMileDeliveryDetailEntity> deliveryDetailEntityList = firstMileDeliveryDetailService.listByMainIds(Collections.singletonList(deliveryEntity.getId()));
        if (CollectionUtils.isEmpty(deliveryDetailEntityList)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "发货单明细");
        }
        // 查询发货目的仓平台授权
        OverseasProviderEntity providerEntity = overseasProviderWarehouseService.findPlatformByWarehouseId(deliveryEntity.getDestWarehouseId());
        String dictPlatform = null == providerEntity ? "" : providerEntity.getCode();

        OverseasWarehouseInboundEntity mainEntity = new OverseasWarehouseInboundEntity();
        // 数据处理
        handleData(mainEntity, addDTO, deliveryEntity, dictPlatform);

        log.info("开始新增海外仓入库单");
        mainEntity.setIsDeleted(false);
        mainEntity.setVersion(0);
        boolean save = this.save(mainEntity);
        if (!save) {
            throw new ServiceException("海外仓入库单保存失败");
        }
        List<String> skuIds = deliveryDetailEntityList.stream()
                .map(FirstMileDeliveryDetailEntity::getSkuId)
                .distinct()
                .collect(Collectors.toList());
        // 查询关联信息
        Map<String, SkuMappingDTO.ListStockSkuNoByProductSkuIdView> currentSkuMap = checkAdnQuerySkuMap(skuIds, dictPlatform, deliveryDetailEntityList);

        // 明细处理
        List<OverseasWarehouseInboundDetailEntity> detailEntityList = deliveryDetailEntityList.stream()
                .map(e -> OverseasWarehouseInboundConverter.INSTANCE.deliveryDetailToDetail(e, mainEntity, currentSkuMap.get(e.getSkuId())))
                .collect(Collectors.toList());

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】来源单号为【{}】", commonService.getUserInfo().getUserName(), "海外仓入库单", mainEntity.getSourceCode());
        // 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.OVERSEAS_WAREHOUSE_INBOUND.getCode(), mainEntity.getId(), "新增操作");
        // 新增明细（如果有明细的话）
        if (!overseasWarehouseInboundDetailService.saveBatch(detailEntityList)) {
            throw new ServiceException("海外仓入库单明细保存失败");
        }
        if (!CollectionUtils.isEmpty(addDTO.getAttachUrlList())) {
            //保存附件
            Class<OverseasWarehouseInboundEntity> aClass = OverseasWarehouseInboundEntity.class;
            TableName tableName = aClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String type = tableName.value();
            wmsAttachmentService.batchSave(addDTO.getAttachUrlList(), addDTO.getAttachNameList(), type, mainEntity.getId());
        }

        // 推送到第三方草稿
        if (null != providerEntity) {
            // 推送到第三方草稿
            ApiResult<String> resultInfo = this.pullThirdOverseasPlatformWithSkuMapping(currentSkuMap, providerEntity, mainEntity, deliveryDetailEntityList, OverseasVerifyEnum.INIT.getCode());
            if (200 != resultInfo.getCode()) {
                log.error("推送第三方仓库新增失败:msg={}", JSONUtil.toJsonStr(resultInfo));
                throw new ServiceException("推送第三方仓库失败:" + resultInfo.getMsg());
            }
            log.info("推送第三方仓库新增结果: ={}", JSONUtil.toJsonStr(resultInfo));
            // 记录单号
            mainEntity.setCode(resultInfo.getData());
            if (!this.updateById(mainEntity)) {
                throw new ServiceException("更新单号失败");
            }
        }
        return new BaseResultDTO.AddDTO(mainEntity.getId(), deliveryEntity.getCode());
    }

    /**
     * 查询和校验映射关系
     */
    private Map<String, SkuMappingDTO.ListStockSkuNoByProductSkuIdView> checkAdnQuerySkuMap(List<String> skuIds, String dictPlatform, List<FirstMileDeliveryDetailEntity> deliveryDetailEntityList) {
        List<SkuMappingDTO.ListStockSkuNoByProductSkuIdView> skuList = omsListingInfoFeign.listStockSkuNoByProductSkuIds(skuIds);
        List<SkuMappingDTO.ListStockSkuNoByProductSkuIdView> currentSkuList = skuList.stream().filter(e -> e.getDictPlatform().equalsIgnoreCase(dictPlatform)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(currentSkuList) && StringUtils.isNotBlank(dictPlatform)) {
            throw new ServiceException("数据异常：未找到SKU的映射关系");
        }
        Map<String, SkuMappingDTO.ListStockSkuNoByProductSkuIdView> currentSkuMap = currentSkuList.stream()
                .collect(Collectors.toMap(SkuMappingDTO.ListStockSkuNoByProductSkuIdView::getProductSkuId, Function.identity()));
        // 校验映射关系
        if (StringUtils.isNotBlank(dictPlatform)) {
            deliveryDetailEntityList.forEach(e -> {
                SkuMappingDTO.ListStockSkuNoByProductSkuIdView view = currentSkuMap.get(e.getSkuId());
                if (null == view) {
                    throw new ServiceException("未找到映射关系：skuId=" + e.getSkuId());
                }
            });
        }
        return currentSkuMap;
    }

    /**
     * 构建请求参数
     */
    private ThirdWarehouseCreateInboundReq entityToCreateInboundBill(OverseasWarehouseInboundEntity mainEntity,
                                                                     List<FirstMileCartonDTO.PackingItemDTO> itemDTOList,
                                                                     Map<String, SkuMappingDTO.ListStockSkuNoByProductSkuIdView> currentSkuMap,
                                                                     Map<SettingEnum, String> shipperInfo,
                                                                     String verifyCode,
                                                                     String code
    ) {

        // 交货方式
        OverseasInstockTypeEnum inStockTypeEnum = OverseasInstockTypeEnum.getByCode(mainEntity.getInstockType());
        String inStockType = null == inStockTypeEnum ? "" : inStockTypeEnum.getCode();

        // 物流方式
        LogisticsMethodEnum logisticsMethodEnum = LogisticsMethodEnum.getByCode(mainEntity.getLogisticsMethod());
        String receivingShippingType = null == logisticsMethodEnum ? "" : logisticsMethodEnum.getCode();

        // 报关方式
        OverseasCustomsTypeNewEnum typeNewEnum = OverseasCustomsTypeNewEnum.getByCode(mainEntity.getCustomsType());
        String customsTypeValue = null == typeNewEnum ? "" : typeNewEnum.getCode();

        // OpenCollectingServiceEnum： 0=自送货物，1=上门提货
        OverseasDeliveryModeEnum deliveryModeEnum = OverseasDeliveryModeEnum.getByCode(mainEntity.getDeliveryMode());
        String collectingService = null == deliveryModeEnum ? "" : deliveryModeEnum.getCode();

        // 装箱信息item
        List<ThirdWarehouseCreateInboundReq.Item> itemList = new LinkedList<>();
        for (FirstMileCartonDTO.PackingItemDTO itemDTO : itemDTOList) {
            SkuMappingDTO.ListStockSkuNoByProductSkuIdView view = currentSkuMap.get(itemDTO.getSkuId());
            if (null == view) {
                String msg = StrUtil.format("未找到绑定的库存SKU, skuId={}，skuNo={}", itemDTO.getSkuId(), itemDTO.getSkuNo());
                throw new ServiceException(msg);
            }
            ThirdWarehouseCreateInboundReq.Item currentItem = ThirdWarehouseCreateInboundReq.Item.builder()
                    .productSku(view.getStockSku())
                    .boxNo(Integer.parseInt(itemDTO.getBoxNo()))
                    .quantity(itemDTO.getPackQty())
                    .build();
            itemList.add(currentItem);
        }
        String contactName = shipperInfo.get(SettingEnum.WMS_OVERSEAS_INBOUND_FIRST_NAME) + shipperInfo.get(SettingEnum.WMS_OVERSEAS_INBOUND_LAST_NAME);
        String phone = shipperInfo.get(SettingEnum.WMS_OVERSEAS_INBOUND_MOBILE);
        String countryCode = shipperInfo.get(SettingEnum.WMS_OVERSEAS_INBOUND_COUNTRY_CODE);
        String stateName = shipperInfo.get(SettingEnum.WMS_OVERSEAS_INBOUND_PROVINCE_NAME);
        String cityName = shipperInfo.get(SettingEnum.WMS_OVERSEAS_INBOUND_CITY_NAME);
        String region = shipperInfo.get(SettingEnum.WMS_OVERSEAS_INBOUND_DISTRICT_NAME);
        String address1 = shipperInfo.get(SettingEnum.WMS_OVERSEAS_INBOUND_COUNTRY_CODE);


        ThirdWarehouseCreateInboundReq inboundReq = ThirdWarehouseCreateInboundReq.builder()
                // 发货单号
                .referenceNo(mainEntity.getSourceCode())
                // 交货方式，0自送，1揽收
                .incomeType(inStockType)
                .receivingType(inStockType)
                // 入库单类型 （标准入库单，中转入库单(标准货运单)，FBA入库单）
                .transitType(inStockType)
                // 物流方式
                .receivingShippingType(receivingShippingType)
                .trackingNumber(mainEntity.getTrackingNo())
                .warehouseCode(mainEntity.getPlatformToWarehouseCode())
                .etaDate(mainEntity.getEstimatedArrivalDate())
                // 入库单创建时取0，发货单审核通过更新为1
                .verify(verifyCode)
                .transitWarehouseCode(mainEntity.getPlatformTransferWarehouseCode())
                .smCode(mainEntity.getLogisticsProductName())
                .customsType(customsTypeValue)
                //  OpenCollectingServiceEnum： 0=自送货物，1=上门提货
                .collectingService(collectingService)
                .deliveryCode(mainEntity.getExpressNo())
                //发货信息
                .shiperInfo(ThirdWarehouseCreateInboundReq.ShiperInfo.builder()
                        .contacterName(contactName)
                        .phone(phone)
                        .countryCode(countryCode)
                        .stateName(stateName)
                        .cityName(cityName)
                        .region(region)
                        .address1(address1)
                        .build())
                .collect(ThirdWarehouseCreateInboundReq.Collect.builder()
                        .contacterName(mainEntity.getFirstName().concat(mainEntity.getLastName()))
                        .contacterFirstName(mainEntity.getFirstName())
                        .contacterLastName(mainEntity.getLastName())
                        .contactPhone(mainEntity.getMobile())
                        .collectCountryCode(mainEntity.getCollectCountryCode())
                        .collectStateId(mainEntity.getPlatformProvinceId())
                        .collectCityId(mainEntity.getPlatformCityId())
                        .collectAreaId(mainEntity.getPlatformDistrictId())
                        .collectStateName(mainEntity.getDictProvinceName())
                        .collectCityName(mainEntity.getDictCityName())
                        .collectZipcode(mainEntity.getZipcode())
                        .collectStreet(mainEntity.getStreet())
                        .build())
                .items(itemList)
                .build();
        if (StringUtils.isNotBlank(code)) {
            // 更新添加订单号
            inboundReq.setReceivingCode(code);
        }
        return inboundReq;
    }

    /**
     * 修改
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(OverseasWarehouseInboundDTO.UpdateDTO updateDTO) {
        OverseasWarehouseInboundEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "海外仓入库单"));
        OverseasWarehouseInboundEntity mainEntity = new OverseasWarehouseInboundEntity();
        BeanUtils.copyProperties(old, mainEntity);
        BeanUtils.copyProperties(updateDTO, mainEntity);

        mainEntity.setInstockType(updateDTO.getInstockType().getCode());
        mainEntity.setLogisticsMethod(updateDTO.getLogisticsMethod().getCode());

        FirstMileDeliveryEntity deliveryEntity = firstMileDeliveryService.getById(old.getSourceId());
        Optional.ofNullable(deliveryEntity).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "发货单"));

        // 查询发货目的仓平台授权
        OverseasProviderEntity providerEntity = overseasProviderWarehouseService.findPlatformByWarehouseId(deliveryEntity.getDestWarehouseId());
        String dictPlatform = null == providerEntity ? "" : providerEntity.getCode();

        // 发货单明细
        List<FirstMileDeliveryDetailEntity> deliveryDetailEntityList = firstMileDeliveryDetailService.listByMainIds(Collections.singletonList(deliveryEntity.getId()));
        if (CollectionUtils.isEmpty(deliveryDetailEntityList)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "发货单明细");
        }

        // 数据处理
        handleData(old, updateDTO, deliveryEntity, dictPlatform);
        log.info("编辑 开始修改海外仓入库单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(mainEntity);
        if (!save) {
            throw new ServiceException("海外仓入库单保存失败");
        }

        // 记录主单操作日志
        log.info("编辑 开始记录海外仓入库单日志数据，单号：【{}】", mainEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), mainEntity.getCode(), "海外仓入库单");
        // 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, mainEntity, null, mainEntity.getId(), msg);

        //保存附件
        Class<OverseasWarehouseInboundEntity> aClass = OverseasWarehouseInboundEntity.class;
        TableName tableName = aClass.getDeclaredAnnotation(TableName.class);
        //获取到表名
        String type = tableName.value();
        wmsAttachmentService.batchSave(updateDTO.getAttachUrlList(), updateDTO.getAttachNameList(), type, old.getId());

        // 推送到第三方
        if (null != providerEntity) {
            if (StringUtils.isBlank(mainEntity.getCode())) {
                throw new ServiceException("数据异常：历史入库单未有单号");
            }
            // 推送到第三方草稿
            ApiResult<String> resultInfo = this.pullThirdOverseasPlatform(providerEntity, mainEntity, deliveryDetailEntityList, OverseasVerifyEnum.INIT.getCode());
            if (200 != resultInfo.getCode()) {
                log.error("推送第三方仓库编辑失败:msg={}", JSONUtil.toJsonStr(resultInfo));
                throw new ServiceException("推送第三方仓库编辑失败:" + resultInfo.getMsg());
            }
            log.info("推送第三方仓库编辑结果: ={}", JSONUtil.toJsonStr(resultInfo));
            if (200 != resultInfo.getCode()) {
                log.error("推送第三方仓库编辑失败:msg={}", JSONUtil.toJsonStr(resultInfo));
                throw new ServiceException("推送第三方仓库编辑失败:" + resultInfo.getMsg());
            }
            log.info("推送第三方仓库编辑结果: ={}", JSONUtil.toJsonStr(resultInfo));
        }
        return Boolean.TRUE;
    }

    @Override
    public OverseasWarehouseInboundEntity getByCode(String receivingCode, String notInStockStatus) {
        return lambdaQuery()
                .eq(OverseasWarehouseInboundEntity::getCode, receivingCode)
                .ne(StringUtils.isNotBlank(notInStockStatus), OverseasWarehouseInboundEntity::getInstockStatus, notInStockStatus)
                .orderByDesc(OverseasWarehouseInboundEntity::getCreateTime)
                .last("LIMIT 1")
                .one();
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(OverseasWarehouseInboundEntity mainEntity,
                            OverseasWarehouseInboundDTO.CommonDTO commonDTO,
                            FirstMileDeliveryEntity deliveryEntity,
                            String dictPlatform
    ) {
        // 查询目的仓
        OverseasProviderWarehouseEntity toEntity = overseasProviderWarehouseService.getByWarehouseId(deliveryEntity.getDestWarehouseId());

        // 校验参数
        // 入库类型=自发头程
        if (OverseasInstockTypeEnum.SELF_HEADWAY.equals(commonDTO.getInstockType())) {
            if (null == commonDTO.getLogisticsMethod()) {
                throw new ServiceException("设置入库类型=自发头程：【logisticsMethod】运输方式不能为空");
            }
            // 设置其他参数为空
            commonDTO.setBlankOtherBySelfHeadway();
        }

        // 入库类型=自发头程, 交货方式=自送货物
        if (OverseasInstockTypeEnum.TRANSFER_AGENT.equals(commonDTO.getInstockType())) {
            if (StringUtils.isBlank(commonDTO.getDeliveryMode())) {
                throw new ServiceException("【deliveryMode】交货方式不能为空");
            }
            // 自送货物
            if (OverseasDeliveryModeEnum.SELF_DELIVERY.getCode().equalsIgnoreCase(commonDTO.getDeliveryMode())) {
                if (StringUtils.isBlank(commonDTO.getTransferWarehouseId())) {
                    throw new ServiceException("【transferWarehouseId】中转仓ID不能为空");
                }
                if (StringUtils.isBlank(commonDTO.getExpressNo())) {
                    throw new ServiceException("【expressNo】快递单号不能为空");
                }
                // 设置其他参数为空
                commonDTO.setBlankOtherByTransferAgentAndSelfDelivery();
            }
        }

        OverseasTransferWarehouseEntity transferEntity = null;
        // 入库类型=自发头程, 交货方式=上面揽收
        if (OverseasInstockTypeEnum.TRANSFER_AGENT.equals(commonDTO.getInstockType())) {
            if (StringUtils.isBlank(commonDTO.getDeliveryMode())) {
                throw new ServiceException("【deliveryMode】交货方式不能为空");
            }
            // 上门揽收
            if (OverseasDeliveryModeEnum.COLLECT_AT_HOME.getCode().equalsIgnoreCase(commonDTO.getDeliveryMode())) {
                if (StringUtils.isBlank(commonDTO.getTransferWarehouseId())) {
                    throw new ServiceException("【transferWarehouseId】中转仓ID不能为空");
                }
                // 查询设置中转仓信息
                transferEntity = overseasTransferWarehouseService.getById(commonDTO.getTransferWarehouseId());
                if (null == transferEntity) {
                    throw new ServiceException("未找到中转仓");
                }
                commonDTO.setTransferWarehouseName(transferEntity.getName());

                if (null == commonDTO.getCustomsType()) {
                    throw new ServiceException("【customsType】报关方式不能为空");
                }
                OverseasCustomsTypeNewEnum customsTypeNewEnum = OverseasCustomsTypeNewEnum.getByCode(commonDTO.getCustomsType());
                if (null == customsTypeNewEnum) {
                    throw new ServiceException("【customsType】报关方式不存在");
                }
                commonDTO.setCustomsTypeName(customsTypeNewEnum.getName());
                // 谷仓校验
                if (OmsPlatformEnum.OMS_GOOD_CANG.getCode().equalsIgnoreCase(dictPlatform)) {
                    if (StringUtils.isBlank(commonDTO.getLogisticsProductCode())) {
                        throw new ServiceException("【logisticsProductCode】 物流产品代码不能为空");
                    }
                    // 物流产品代码
                    commonDTO.setLogisticsProductName(transferEntity.getLogisticsProductName());
                }

                if (null == commonDTO.getEstimatedCollectDate()) {
                    throw new ServiceException("预计揽收日期不能为空");
                }

                if (StringUtils.isBlank(commonDTO.getDictProvinceId())) {
                    throw new ServiceException("【dictProvinceId】省ID不能为空");
                }

                if (StringUtils.isBlank(commonDTO.getDictCityId())) {
                    throw new ServiceException("【dictCityId】城市ID不能为空");
                }

                if (StringUtils.isBlank(commonDTO.getDictDistrictId())) {
                    throw new ServiceException("【dictDistrictId】地区ID不能为空");
                }
                // 查询和校验地区
                Map<String, DictCityEntity> dictCityEntityMap = sysDictService.mapAndCheckDictCityIds(
                        commonDTO.getDictProvinceId(),
                        commonDTO.getDictCityId(),
                        commonDTO.getDictDistrictId());
                // 省
                commonDTO.setDictProvinceName(dictCityEntityMap.get(commonDTO.getDictProvinceId()).getName());
                // 市
                commonDTO.setDictCityName(dictCityEntityMap.get(commonDTO.getDictCityId()).getName());
                // 区
                commonDTO.setDictDistrictName(dictCityEntityMap.get(commonDTO.getDictDistrictId()).getName());
                // iml
                if (OmsPlatformEnum.OMS_IML.getCode().equalsIgnoreCase(dictPlatform)) {
                    // 查询关联
                    Map<String, ImlDictCityEntity> imlCityEntityMap = sysDictService.mapAndCheckImlCityIds(
                            commonDTO.getDictProvinceId(),
                            commonDTO.getDictCityId(),
                            commonDTO.getDictDistrictId());
                    // 省
                    commonDTO.setPlatformProvinceId(imlCityEntityMap.get(commonDTO.getDictProvinceId()).getRegionId());
                    // 市
                    commonDTO.setPlatformCityId(imlCityEntityMap.get(commonDTO.getDictCityId()).getRegionId());
                    // 区
                    commonDTO.setPlatformDistrictId(imlCityEntityMap.get(commonDTO.getDictDistrictId()).getRegionId());

                }

                if (StringUtils.isBlank(commonDTO.getFirstName())) {
                    throw new ServiceException("【firstName】姓不能为空");
                }

                if (StringUtils.isBlank(commonDTO.getLastName())) {
                    throw new ServiceException("【lastName】名不能为空");
                }

                if (StringUtils.isBlank(commonDTO.getMobile())) {
                    throw new ServiceException("【mobile】手机号不能为空");
                }

                if (StringUtils.isBlank(commonDTO.getStreet())) {
                    throw new ServiceException("【street】详情地址不能为空");
                }
                if (StringUtils.isBlank(commonDTO.getZipcode())) {
                    throw new ServiceException("【zipcode】邮编不能为空");
                }
                // 设置其他参数为空
                commonDTO.setBlankOtherByTransferAgentAndCollectAtHome();
            }
        }

        BeanUtils.copyProperties(commonDTO, mainEntity);
        // 验证数据 & 数据赋值
        mainEntity.setDictPlatform(dictPlatform);
        mainEntity.setInstockType(commonDTO.getInstockType().getCode());
        mainEntity.setLogisticsMethod(null == commonDTO.getLogisticsMethod() ? "" : commonDTO.getLogisticsMethod().getCode());
        mainEntity.setEstimatedArrivalDate(LocalDateTime.of(commonDTO.getEstimatedArrivalDate(), LocalTime.MIN));
        mainEntity.setInstockStatus(OverseasInstockStatusEnum.TO_BE_SHIPPED.getCode());
        if (null != commonDTO.getEstimatedCollectDate()) {
            mainEntity.setEstimatedCollectDate(LocalDateTime.of(commonDTO.getEstimatedCollectDate(), LocalTime.MIN));
        }
        mainEntity.setOverseasWarehouseInboundId("");
        mainEntity.setCollectCountryCode("CN");
        // 设置仓库
        mainEntity.setToWarehouseId(deliveryEntity.getDestWarehouseId());
        mainEntity.setToWarehouseName(deliveryEntity.getDestWarehouseName());
        mainEntity.setDeliveryWarehouseId(deliveryEntity.getDeliveryWarehouseId());
        mainEntity.setDeliveryWarehouseName(deliveryEntity.getDeliveryWarehouseName());
        mainEntity.setPlatformToWarehouseCode(null == toEntity ? "" : toEntity.getPlatformWarehouseCode());
        mainEntity.setPlatformTransferWarehouseCode(null == transferEntity ? "" : transferEntity.getPlatformWarehouseCode());


        if (null != commonDTO.getCustomsType()) {
            mainEntity.setCustomsType(commonDTO.getCustomsType());
        }

        if (StringUtils.isBlank(dictPlatform)) {
            if (StringUtils.isBlank(commonDTO.getCode())) {
                throw new ServiceException("发货单未对接海外仓, 单号不能为空");
            }
            mainEntity.setCode(commonDTO.getCode());
        }
        mainEntity.setSourceId(deliveryEntity.getId());
        mainEntity.setSourceCode(deliveryEntity.getCode());
        mainEntity.setSourceType(SourceTypeEnum.FIRST_MILE_DELIVERY.getCode());
    }


    @Override
    public PagingVO<OverseasWarehouseInboundDTO.ListDTO> paging(PagingDTO<OverseasWarehouseInboundDTO.PagingParamDTO> dto) {
        OverseasWarehouseInboundDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<?> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<OverseasWarehouseInboundDTO.ListDTO> pageData = baseMapper.paging(query, params);
        if (CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO manualFinish(OverseasWarehouseInboundDTO.FinishDTO dto) {

        OverseasWarehouseInboundEntity entity = this.getById(dto.getId());
        Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.OVERSEAS_WAREHOUSE_INBOUND_NOT_EXIST));
        // TODO 校验

        entity.setFinishStatus(OverseasFinishStatusEnum.MANUAL.getCode());
        entity.setFinishReason(dto.getFinishReason());
        // 详情更新签收数量
        if (!this.updateById(entity)) {
            throw new ServiceException("海外仓入库单更新失败");
        }
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.UPDATE_STATUS);
    }

    @Override
    public OverseasWarehouseInboundDTO.ViewDTO view(String id) {
        OverseasWarehouseInboundEntity entity = this.getById(id);
        Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.OVERSEAS_WAREHOUSE_INBOUND_NOT_EXIST));

        OverseasWarehouseInboundDTO.ViewDTO resultDTO = OverseasWarehouseInboundConverter.INSTANCE.entityToViewDTO(entity);
        // 入库类型名称
        resultDTO.setInstockTypeName(OverseasInstockTypeEnum.getNameByCode(resultDTO.getInstockType()));
        // 入库状态名称
        resultDTO.setInstockStatusName(OverseasInstockStatusEnum.getName(resultDTO.getInstockStatus()));
        // 报关方式
        resultDTO.setCustomsTypeName(OverseasCustomsTypeNewEnum.getNameByCode(resultDTO.getCustomsType()));
        // 交货方式名称
        resultDTO.setDeliveryModeName(OverseasDeliveryModeEnum.getNameByCode(resultDTO.getDeliveryMode()));

        // 查询详情信息
        List<OverseasWarehouseInboundDetailEntity> detailEntityList = overseasWarehouseInboundDetailService.getByMainId(entity.getId());
        if (CollectionUtils.isEmpty(detailEntityList)) {
            return resultDTO;
        }
        //查询skuId产品信息
        List<String> skuIds = detailEntityList.stream().map(OverseasWarehouseInboundDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        Map<String, String> imageUrlMap = plmTaskFeign.getSkuInfoByIds(skuIds)
                .stream()
                .collect(Collectors.toMap(SkuVO::getSkuId, SkuVO::checkAndGetSkuImagesUrl));

        List<OverseasWarehouseInboundDetailDTO.ViewDTO> detailDTOList = detailEntityList.stream()
                .map(e -> OverseasWarehouseInboundConverter.INSTANCE.detailEntityToViewDTO(e, imageUrlMap.getOrDefault(e.getSkuId(), "")))
                .collect(Collectors.toList());
        resultDTO.setDetailList(detailDTOList);

        //获取到附件信息
        List<WmsAttachmentDTO.UpdateDTO> attachmentList = wmsAttachmentService.getByBusinessIds(Arrays.asList(entity.getId()));
        //附件地址
        List<String> attachmentUrlList = attachmentList.stream()
                .map(WmsAttachmentDTO.UpdateDTO::getAttachUrl)
                .collect(Collectors.toList());
        //附件名称
        List<String> attachmentNameList = attachmentList.stream()
                .map(WmsAttachmentDTO.UpdateDTO::getAttachName).
                collect(Collectors.toList());
        resultDTO.setAttachUrlList(attachmentUrlList);
        resultDTO.setAttachNameList(attachmentNameList);
        return resultDTO;
    }

    @Override
    public List<OverseasWarehouseInboundDetailDTO.ViewListDTO> viewList(OverseasWarehouseInboundDTO.ViewListReqDTO dto) {
        // 详情列表
        List<OverseasWarehouseInboundDetailEntity> detailEntityList = Collections.emptyList();
        // 主表ID
        List<String> mainIds = Collections.emptyList();
        if (RequestIdTypeEnum.MAIN_ID.equals(dto.getRequestIdType())) {
            detailEntityList = overseasWarehouseInboundDetailService.getByMainIds(dto.getRequestIdList());
            mainIds = dto.getRequestIdList();
        } else if (RequestIdTypeEnum.DETAIL_ID.equals(dto.getRequestIdType())) {
            detailEntityList = overseasWarehouseInboundDetailService.getByIds(dto.getRequestIdList());
            // 主键IDS
            mainIds = detailEntityList.stream()
                    .map(OverseasWarehouseInboundDetailEntity::getMainId)
                    .distinct()
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(detailEntityList)) {
            return Collections.emptyList();
        }
        Map<String, OverseasWarehouseInboundEntity> mainEntityMap = this.mapByIds(mainIds);
        // 组合
        return detailEntityList.stream()
                .map(e -> OverseasWarehouseInboundConverter.INSTANCE.detailEntityToViewListDTO(e, mainEntityMap.get(e.getMainId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<OverseasWarehouseInboundDTO.CountDTO> listCount(PermissionsDTO dto) {
        QueryChainWrapper<OverseasWarehouseInboundEntity> queryWrapper = query();

        queryWrapper.select("count(id) as count", "instock_status")
                .groupBy(OverseasWarehouseInboundEntity.INSTOCK_STATUS);
        if (StringUtils.isNotBlank(dto.getPermissionSql())) {
            queryWrapper.last(dto.getPermissionSql());
        }
        List<OverseasWarehouseInboundEntity> inStockStatusList = queryWrapper.list();

        Map<String, Integer> countMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(inStockStatusList)) {
            countMap = inStockStatusList
                    .stream()
                    .collect(Collectors.toMap(OverseasWarehouseInboundEntity::getInstockStatus, OverseasWarehouseInboundEntity::getCount));
        }
        Map<String, Integer> finalCountMap = countMap;
        return Arrays.stream(OverseasInstockStatusEnum.values())
                .map(e -> new OverseasWarehouseInboundDTO.CountDTO(e.getCode(), finalCountMap.getOrDefault(e.getCode(), 0)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO cancel(String id) {
        OverseasWarehouseInboundEntity mainEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException(ApiError.OVERSEAS_WAREHOUSE_INBOUND_NOT_EXIST));
        // 只有待提交的单据允许撤销
        if (!OverseasInstockStatusEnum.TO_BE_SHIPPED.getCode().equalsIgnoreCase(mainEntity.getInstockStatus())) {
            throw new ServiceException(ApiError.OVERSEAS_WAREHOUSE_INBOUND_NOT_CANCEL);
        }
        // 更新状态
        mainEntity.setInstockStatus(OverseasInstockStatusEnum.CANCELED.getCode());
        if (!this.updateById(mainEntity)) {
            throw new ServiceException("【海外入库单】更新状态失败");
        }

        FirstMileDeliveryEntity deliveryEntity = firstMileDeliveryService.getById(mainEntity.getSourceId());
        Optional.ofNullable(deliveryEntity).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "发货单"));

        // 查询发货目的仓平台授权
        OverseasProviderEntity providerEntity = overseasProviderWarehouseService.findPlatformByWarehouseId(deliveryEntity.getDestWarehouseId());

        // 发货单明细
        List<FirstMileDeliveryDetailEntity> deliveryDetailEntityList = firstMileDeliveryDetailService.listByMainIds(Collections.singletonList(deliveryEntity.getId()));
        if (CollectionUtils.isEmpty(deliveryDetailEntityList)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "发货单明细");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】取消了单据编号为【{}】的海外入库单", commonService.getUserInfo().getUserName(), mainEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.OVERSEAS_WAREHOUSE_INBOUND.getCode(), mainEntity.getId(), "取消操作");

        if (null != providerEntity){
            if (StringUtils.isBlank(mainEntity.getCode())) {
                throw new ServiceException("数据异常：历史入库单未有单号");
            }
            // 请求第三方
            ThirdWarehouseCancelInboundReq cancelInboundReq = new ThirdWarehouseCancelInboundReq();
            cancelInboundReq.setReceivingCode(mainEntity.getCode());
            ThirdWarehouseService handlerService = thirdWarehouseRegistry.getHandlerByAuthId(providerEntity.getId());
            log.info("取消海外入库单推送第三方仓库: dto={}", JSONUtil.toJsonStr(cancelInboundReq));
            ApiResult<String> resultInfo = handlerService.cancelInboundBill(cancelInboundReq, providerEntity.getId());
            if (200 != resultInfo.getCode()){
                log.error("取消海外入库单推送第三方仓库失败:msg={}", JSONUtil.toJsonStr(resultInfo));
                throw new ServiceException("取消第三方仓库失败:" + resultInfo.getMsg());
            }
            log.info("取消海外入库单推送结果: ={}", JSONUtil.toJsonStr(resultInfo));
        }

        return BatchResultDTO.success(mainEntity.getId(), mainEntity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        OverseasWarehouseInboundEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException(ApiError.OVERSEAS_WAREHOUSE_INBOUND_NOT_EXIST));
        // 只有取消的单据允许删除
        if (!OverseasInstockStatusEnum.CANCELED.getCode().equalsIgnoreCase(entity.getInstockStatus())) {
            throw new ServiceException(ApiError.OVERSEAS_WAREHOUSE_INBOUND_NOT_DELETE);
        }
        // 更新状态
        if (!this.removeById(id)) {
            throw new ServiceException("【海外入库单】更新状态失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】删除了单据编号为【{}】的海外入库单", commonService.getUserInfo().getUserName(), entity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.OVERSEAS_WAREHOUSE_INBOUND.getCode(), entity.getId(), "删除操作");

        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    @Override
    public Boolean exportExcel(OverseasWarehouseInboundDTO.ExportDTO dto, HttpServletResponse response) {
        List<OverseasWarehouseInboundDTO.ListDTO> list = baseMapper.listExportExcel(dto);
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.TRUE;
        }
        //数据处理
        fillList(list);
        List<ExportOverseasWarehouseInboundExcelDTO> resultList = BeanMapperUtils.copyList(ExportOverseasWarehouseInboundExcelDTO.class, list);
        String fileName = "海外入库单数据";
        try {
            ExcelUtil.export(fileName, "海外入库单数据", resultList, ExportOverseasWarehouseInboundExcelDTO.class, response);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
        return Boolean.TRUE;
    }

    @Override
    public List<String> getReceiptNumbersForStatus(List<String> statusList) {
        return this.list(Wrappers.<OverseasWarehouseInboundEntity>lambdaQuery()
                        .in(OverseasWarehouseInboundEntity::getInstockStatus, statusList))
                .stream()
                .map(OverseasWarehouseInboundEntity::getCode)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<OverseasWarehouseInboundEntity> listBySourceIds(List<String> sourceIds) {
        if (CollectionUtils.isEmpty(sourceIds)) {
            return Collections.emptyList();
        }

        return lambdaQuery()
                .in(OverseasWarehouseInboundEntity::getSourceId, sourceIds)
                .orderByDesc(OverseasWarehouseInboundEntity::getCreateTime)
                .list();
    }

    @Override
    public OverseasWarehouseInboundEntity getBySourceId(String sourceId, String notInStockStatus) {
        return lambdaQuery()
                .eq(OverseasWarehouseInboundEntity::getSourceId, sourceId)
                .ne(StringUtils.isNotBlank(notInStockStatus), OverseasWarehouseInboundEntity::getInstockStatus, notInStockStatus)
                .orderByDesc(OverseasWarehouseInboundEntity::getCreateTime)
                .last("LIMIT 1")
                .one();
    }

    @Override
    public Boolean updateInstockStatus(List<String> ids, String status) {
        return lambdaUpdate()
                .set(OverseasWarehouseInboundEntity::getInstockStatus, status)
                .in(OverseasWarehouseInboundEntity::getId, ids)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public String generateTransferOut(OverseasWarehouseInboundEntity mainEntity, OverseasWarehouseInboundDetailEntity detailEntity, OverseasWarehouseInboundReceivedEntity receivedEntity) {
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(Arrays.asList(mainEntity.getToWarehouseId(), mainEntity.getDeliveryWarehouseId()));

        //仓库列表配置的在途归属仓库，目的仓为FBA第三方仓时，在途仓优先取仓库列表配置，配置为空时默认为“FBA在途仓-xgwj-fba”
        WarehouseDTO.UpdateDTO destWarehouse = warehouseList.stream()
                .filter(req -> req.getId().equals(mainEntity.getToWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());

        //校验目的仓是否为FBA第三方仓
        List<DictBasicDTO.ListDTO> warehouseTypes = dictBasicService.getByKey("warehouseType");
        DictBasicDTO.ListDTO listDTO = warehouseTypes.stream().filter(req -> "FBA".equals(req.getValue())).findFirst().orElse(null);
        //如果是FBA第三方仓
        if (listDTO.getId().equals(destWarehouse.getTypeId())) {
            //如果配置为空时默认为“FBA在途仓-xgwj-fba”
            if (StringUtils.isBlank(destWarehouse.getOnwayWarehouseId())) {
                List<WarehouseEntity> warehouseEntities = warehouseService.listByKingdeeCodeList(Arrays.asList("xgwj-fba"));
                if (CollectionUtils.isEmpty(warehouseEntities)) {
                    throw new ServiceException(ApiError.WAREHOUSE_CODE_XGWJ_FBA_NOT_EXIST);
                }
                destWarehouse.setOnwayWarehouseId(warehouseEntities.get(0).getId());
                destWarehouse.setOnwayWarehouseName(warehouseEntities.get(0).getName());
            }
        }

        //如果目的仓没有配置在途归属仓，需要提示：目的仓没有配置在途归属仓库，请在【仓库列表】配置后再审核
        if (StringUtils.isBlank(destWarehouse.getOnwayWarehouseId())) {
            throw new ServiceException(ApiError.ONWAY_WAREHOUSE_NOT_EXIST);
        }

        //查询在途仓
        WarehouseEntity warehouseEntity = warehouseService.getById(destWarehouse.getOnwayWarehouseId());

        TransferInfoDTO.AddDTO addDTO = new TransferInfoDTO.AddDTO();
        //默认来源类型：FBA货件
        addDTO.setSourceType(SourceTypeEnum.FBA_SHIPMENT.getCode());
        //默认调出日期：当前日期
        addDTO.setBillDate(LocalDate.now());
        //默认调拨方向：普通
        addDTO.setTransferDirection(TransferDirectionEnum.ORDINARY.getCode());
        //调入组织
        addDTO.setInOrgId(warehouseEntity.getOrgId());
        //调出组织
        WarehouseDTO.UpdateDTO deliveryWarehouse = warehouseList.stream().filter(req -> req.getId().equals(mainEntity.getDeliveryWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
        addDTO.setOutOrgId(deliveryWarehouse.getOrgId());
        //调拨类型
        if (warehouseEntity.getOrgId().equals(deliveryWarehouse.getOrgId())) {
            addDTO.setType(TransferTypeEnum.IN_ORG.getCode());
        } else {
            addDTO.setType(TransferTypeEnum.CROSS_ORG.getCode());
        }
        addDTO.setSourceId(mainEntity.getId());
        addDTO.setSourceCode(mainEntity.getCode());
        addDTO.setRemark(String.format("发货单【%s】审核通过自动创建", mainEntity.getCode()));

        //详情信息
        TransferInfoDetailDTO.AddDTO detailAddDto = new TransferInfoDetailDTO.AddDTO();
        //映射产品信息
        detailAddDto.setSkuId(detailEntity.getSkuId());
        detailAddDto.setSkuNo(detailEntity.getSkuNo());
        detailAddDto.setQty(receivedEntity.getReceiveQty());
        detailAddDto.setOutWarehouseId(mainEntity.getDeliveryWarehouseId());
        detailAddDto.setOutWarehouseLocation("");
        detailAddDto.setInWarehouseId(warehouseEntity.getId());
        detailAddDto.setInWarehouseLocation("");
        detailAddDto.setSourceDetailId(detailEntity.getId());

        addDTO.setDetailList(Collections.singletonList(detailAddDto));
        return transferInfoService.add(addDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public ApiResult<String> pullThirdOverseasPlatform(
            OverseasProviderEntity providerEntity,
            OverseasWarehouseInboundEntity mainEntity,
            List<FirstMileDeliveryDetailEntity> deliveryDetailEntityList,
            String verityCode) {
        // 发货的skuIds
        List<String> skuIds = deliveryDetailEntityList.stream()
                .map(FirstMileDeliveryDetailEntity::getSkuId)
                .distinct()
                .collect(Collectors.toList());
        // 查询关联信息
        Map<String, SkuMappingDTO.ListStockSkuNoByProductSkuIdView> currentSkuMap = checkAdnQuerySkuMap(skuIds, mainEntity.getDictPlatform(), deliveryDetailEntityList);
        // 调用
        return this.pullThirdOverseasPlatformWithSkuMapping(currentSkuMap, providerEntity, mainEntity, deliveryDetailEntityList, verityCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public ApiResult<String> pullThirdOverseasPlatformWithSkuMapping(Map<String, SkuMappingDTO.ListStockSkuNoByProductSkuIdView> skuViewMap,
                                                                     OverseasProviderEntity providerEntity,
                                                                     OverseasWarehouseInboundEntity mainEntity,
                                                                     List<FirstMileDeliveryDetailEntity> deliveryDetailEntityList,
                                                                     String verityCode
    ) {
        // 查询包装信息
        List<FirstMileCartonDTO.PackingItemDTO> packingQtyDTOS = firstMileCartonDetailService.boxInfoByMainId(mainEntity.getSourceId());
        if (CollectionUtils.isEmpty(packingQtyDTOS)) {
            String format = StrUtil.format("【{}】发货单：未找到包装信息", mainEntity.getSourceCode());
            throw new ServiceException(format);
        }

        // 查询默认发货人
        Map<SettingEnum, String> shipperInfo = dmpTaskFeign.getCfgSettingList(SettingEnum.WMS_OVERSEAS_INBOUND);

        // 第三方单号：新增为空, 编辑不为空
        String code = mainEntity.getCode();

        // 请求第三方
        ThirdWarehouseCreateInboundReq createInboundReq = entityToCreateInboundBill(mainEntity, packingQtyDTOS, skuViewMap, shipperInfo, verityCode, code);
        ThirdWarehouseService handlerService = thirdWarehouseRegistry.getHandlerByAuthId(providerEntity.getId());
        log.info("推送第三方仓库: dto={}", JSONUtil.toJsonStr(createInboundReq));
        if (StringUtils.isBlank(code)){
            // 新增
            return handlerService.createInboundBill(createInboundReq, providerEntity.getId());
        } else {
            // 编辑
            return handlerService.editInboundBill(createInboundReq, providerEntity.getId());
        }
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<OverseasWarehouseInboundDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for (OverseasWarehouseInboundDTO.ListDTO data : list) {
            // 入库类型名称
            data.setInstockTypeName(OverseasInstockTypeEnum.getNameByCode(data.getInstockType()));
            // 入库状态名称
            data.setInstockStatusName(OverseasInstockStatusEnum.getName(data.getInstockStatus()));
            // 完结状态名称
            data.setFinishStatusName(OverseasFinishStatusEnum.getNameByCode(data.getFinishStatus()));
            // 物流方式
            data.setLogisticsMethodName(LogisticsMethodEnum.getName(data.getLogisticsMethod()));
            // 交货方式
            data.setDeliveryModeName(OverseasDeliveryModeEnum.getNameByCode(data.getDeliveryMode()));
        }
    }

}
