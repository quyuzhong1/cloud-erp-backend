package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableField;
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
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.plm.enums.CustomsTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.DictCityEntity;
import com.erp.model.sys.entity.ImlDictCityEntity;
import com.erp.model.wms.dto.OverseasWarehouseInboundDTO;
import com.erp.model.wms.dto.OverseasWarehouseInboundDetailDTO;
import com.erp.model.wms.dto.WmsAttachmentDTO;
import com.erp.model.wms.dto.excel.ExportOverseasWarehouseInboundExcelDTO;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCreateInboundReq;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.model.wms.enums.OverseasDeliveryModeEnum;
import com.erp.model.wms.enums.OverseasFinishStatusEnum;
import com.erp.model.wms.enums.OverseasInstockTypeEnum;
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

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(OverseasWarehouseInboundDTO.AddDTO addDTO) {
        OverseasWarehouseInboundEntity oldEntity = this.getBySourceId(addDTO.getSourceId());
        if (null != oldEntity){
            throw new ServiceException("该发货单的入库单已存在");
        }

        FirstMileDeliveryEntity deliveryEntity = firstMileDeliveryService.getById(addDTO.getSourceId());
        Optional.ofNullable(deliveryEntity).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "发货单"));
        // 发货单明细
        List<FirstMileDeliveryDetailEntity> deliveryDetailEntityList = firstMileDeliveryDetailService.listByMainIds(Collections.singletonList(deliveryEntity.getId()));
        if (CollectionUtils.isEmpty(deliveryDetailEntityList)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "发货单明细");
        }
        // 查询发货目的仓平台
        OverseasProviderEntity providerEntity = overseasProviderWarehouseService.findPlatformByWarehouseId(deliveryEntity.getDestWarehouseId());
        String dictPlatform = null == providerEntity ? "" : providerEntity.getCode();

        OverseasWarehouseInboundEntity mainEntity = new OverseasWarehouseInboundEntity();
        // 数据处理
        handleData(mainEntity, addDTO, deliveryEntity, dictPlatform);

        log.info("开始新增海外仓入库单");
        boolean save = super.save(mainEntity);
        if (!save) {
            throw new ServiceException("海外仓入库单保存失败");
        }
        List<String> skuIds = deliveryDetailEntityList.stream()
                .map(FirstMileDeliveryDetailEntity::getSkuId)
                .distinct()
                .collect(Collectors.toList());
        // 查询关联信息
        List<SkuMappingDTO.ListStockSkuNoByProductSkuIdView> skuList = omsListingInfoFeign.listStockSkuNoByProductSkuIds(skuIds);
        List<SkuMappingDTO.ListStockSkuNoByProductSkuIdView> currentSkuList = skuList.stream().filter(e -> e.getDictPlatform().equalsIgnoreCase(dictPlatform)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(currentSkuList) && StringUtils.isNotBlank(dictPlatform)){
            throw new ServiceException("数据异常：未找到SKU的映射关系");
        }
        Map<String, SkuMappingDTO.ListStockSkuNoByProductSkuIdView> currentSkuMap = currentSkuList.stream()
                .collect(Collectors.toMap(SkuMappingDTO.ListStockSkuNoByProductSkuIdView::getProductSkuId, Function.identity()));
        // 校验映射关系
        if (StringUtils.isNotBlank(dictPlatform)){
            deliveryDetailEntityList.forEach(e-> {
                SkuMappingDTO.ListStockSkuNoByProductSkuIdView view = currentSkuMap.get(e.getSkuId());
                if (null == view){
                    throw new ServiceException("未找到映射关系：skuId=" + e.getSkuId());
                }
            });
        }

        // 明细处理
        List<OverseasWarehouseInboundDetailEntity> detailEntityList = deliveryDetailEntityList.stream()
                .map(e -> OverseasWarehouseInboundConverter.INSTANCE.deliveryDetailToDetail(e, mainEntity, currentSkuMap.get(e.getSkuId())))
                .collect(Collectors.toList());

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】来源单号为【{}】", commonService.getUserInfo().getUserName(), "海外仓入库单", mainEntity.getSourceCode());
        // 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.OVERSEAS_WAREHOUSE_INBOUND.getCode(), mainEntity.getId(), "新增操作");
        // 新增明细（如果有明细的话）
        if (!overseasWarehouseInboundDetailService.saveBatch(detailEntityList)){
            throw new ServiceException("海外仓入库单明细保存失败");
        }
        if (CollectionUtils.isEmpty(addDTO.getAttachUrlList())){
            return new BaseResultDTO.AddDTO(mainEntity.getId(), deliveryEntity.getCode());
        }
        //保存附件
        Class<OverseasWarehouseInboundEntity> aClass = OverseasWarehouseInboundEntity.class;
        TableName tableName = aClass.getDeclaredAnnotation(TableName.class);
        //获取到表名
        String type = tableName.value();
        wmsAttachmentService.batchSave(addDTO.getAttachUrlList(), addDTO.getAttachNameList(), type, mainEntity.getId());

        // 推送到第三方草稿
//        if (null != providerEntity){
//            ThirdWarehouseCreateInboundReq createInboundReq = entityToCreateInboundBill(mainEntity, GoodCangEnums.VerifyEnum.INIT.getCode());
//            ThirdWarehouseService handlerService = thirdWarehouseRegistry.getHandlerByAuthId(providerEntity.getId());
//            handlerService.createInboundBill(createInboundReq, providerEntity.getId());
//        }
        return new BaseResultDTO.AddDTO(mainEntity.getId(), deliveryEntity.getCode());
    }

    /**
     * 构建请求参数
     */
    private ThirdWarehouseCreateInboundReq entityToCreateInboundBill(OverseasWarehouseInboundEntity mainEntity, String verifyCode) {
        // 交货方式
        String inStockType = "";
        if(StringUtils.isBlank(mainEntity.getInstockType())){
            OverseasInstockTypeEnum inStockTypeEnum = OverseasInstockTypeEnum.getByCode(mainEntity.getInstockType());
        }

        // 物流方式
        String receivingShippingType = "";
        if (StringUtils.isNotBlank(mainEntity.getLogisticsMethod())){
            LogisticsMethodEnum logisticsMethodEnum = LogisticsMethodEnum.getByCode(mainEntity.getLogisticsMethod());
            receivingShippingType = null == logisticsMethodEnum ? "" : logisticsMethodEnum.getProductCodeEnum().getCode().toString();
        }

        // 报关方式
        String customsTypeValue = "";
        if (StringUtils.isNotBlank(mainEntity.getCustomsType())){
            GoodCangEnums.CustomsTypeNewEnum typeNewEnum = GoodCangEnums.CustomsTypeNewEnum.getByCode(Integer.parseInt(mainEntity.getCustomsType()));
            customsTypeValue = null == typeNewEnum ? "" : typeNewEnum.getCode().toString();
        }

        // OpenCollectingServiceEnum： 0=自送货物，1=上门提货
        OverseasDeliveryModeEnum deliveryModeEnum = OverseasDeliveryModeEnum.getByCode(mainEntity.getDeliveryMode());
        String collectingService = null == deliveryModeEnum ? "" : deliveryModeEnum.getServiceEnum().getCode().toString();


        return ThirdWarehouseCreateInboundReq.builder()
                // 发货单号
                .referenceNo(mainEntity.getSourceCode())
                // 交货方式，0自送，1揽收
                .incomeType("1")
                // 入库单类型 （标准入库单，中转入库单(标准货运单)，FBA入库单）
                .transitType("0")
                // 物流方式
                .receivingShippingType(receivingShippingType)
                .trackingNumber(mainEntity.getTrackingNo())
                .warehouseCode("UAW1")
                .etaDate(mainEntity.getEstimatedArrivalDate())
                // 入库单创建时取0，发货单审核通过更新为1
                .verify(verifyCode)
                .transitWarehouseCode("DG")
                .smCode(mainEntity.getLogisticsProductName())
                .customsType(customsTypeValue)
                //  OpenCollectingServiceEnum： 0=自送货物，1=上门提货
                .collectingService(collectingService)
                .deliveryCode(mainEntity.getExpressNo())
                //发货信息
                .shiperInfo(ThirdWarehouseCreateInboundReq.ShiperInfo.builder()
                        .contacterName(mainEntity.getFirstName().concat(mainEntity.getLastName()))
                        .phone(mainEntity.getMobile())
                        .countryCode(mainEntity.getCollectCountryCode())
                        .stateName(mainEntity.getDictProvinceName())
                        .cityName(mainEntity.getDictCityName())
                        .region(mainEntity.getDictDistrictName())
                        .address1(mainEntity.getStreet())
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
                .items(Arrays.asList(ThirdWarehouseCreateInboundReq.Item.builder()
                        .productSku("2823A")
                        .boxNo(1)
                        .quantity(1)
                        .build(),ThirdWarehouseCreateInboundReq.Item.builder()
                        .productSku("2823A")
                        .boxNo(1)
                        .quantity(3)
                        .build(),ThirdWarehouseCreateInboundReq.Item.builder()
                        .productSku("C003GBB1")
                        .boxNo(3)
                        .quantity(10)
                        .build(),ThirdWarehouseCreateInboundReq.Item.builder()
                        .productSku("C003GBB1")
                        .boxNo(2)
                        .quantity(11)
                        .build()))
                .build();
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(OverseasWarehouseInboundDTO.UpdateDTO updateDTO) {
        OverseasWarehouseInboundEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "海外仓入库单"));
        OverseasWarehouseInboundEntity overseasWarehouseInboundEntity = BeanMapperUtils.map(OverseasWarehouseInboundEntity.class, updateDTO);
        old.setInstockType(updateDTO.getInstockType().getCode());
        old.setLogisticsMethod(updateDTO.getLogisticsMethod().getCode());

        FirstMileDeliveryEntity deliveryEntity = firstMileDeliveryService.getById(old.getSourceId());
        Optional.ofNullable(deliveryEntity).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "发货单"));

        // 查询发货目的仓平台
        OverseasProviderEntity providerEntity = overseasProviderWarehouseService.findPlatformByWarehouseId(deliveryEntity.getDestWarehouseId());
        String dictPlatform = null == providerEntity ? "" : providerEntity.getCode();

        // 数据处理
        handleData(old, updateDTO, deliveryEntity, dictPlatform);
        log.info("编辑 开始修改海外仓入库单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(overseasWarehouseInboundEntity);
        if (!save) {
            throw new ServiceException("海外仓入库单保存失败");
        }

        // 记录主单操作日志
        log.info("编辑 开始记录海外仓入库单日志数据，单号：【{}】", overseasWarehouseInboundEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), overseasWarehouseInboundEntity.getCode(), "海外仓入库单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, overseasWarehouseInboundEntity, null, overseasWarehouseInboundEntity.getId(), msg);

        //保存附件
        Class<OverseasWarehouseInboundEntity> aClass = OverseasWarehouseInboundEntity.class;
        TableName tableName = aClass.getDeclaredAnnotation(TableName.class);
        //获取到表名
        String type = tableName.value();
        wmsAttachmentService.batchSave(updateDTO.getAttachUrlList(), updateDTO.getAttachNameList(), type, old.getId());

        return Boolean.TRUE;
    }

    @Override
    public OverseasWarehouseInboundEntity getByCode(String receivingCode) {
        return lambdaQuery().eq(OverseasWarehouseInboundEntity::getCode, receivingCode).one();
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(OverseasWarehouseInboundEntity mainEntity,
                            OverseasWarehouseInboundDTO.CommonDTO commonDTO,
                            FirstMileDeliveryEntity deliveryEntity,
                            String dictPlatform
    ) {
        // 校验参数
        // 入库类型=自发头程
        if (OverseasInstockTypeEnum.SELF_HEADWAY.equals(commonDTO.getInstockType())){
            if (null == commonDTO.getLogisticsMethod()){
                throw new ServiceException("设置入库类型=自发头程：【logisticsMethod】运输方式不能为空");
            }
            // 设置其他参数为空
            commonDTO.setBlankOtherBySelfHeadway();
        }

        // 入库类型=自发头程, 交货方式=自送货物
        if (OverseasInstockTypeEnum.TRANSFER_AGENT.equals(commonDTO.getInstockType())){
            if (StringUtils.isBlank(commonDTO.getDeliveryMode())){
                throw new ServiceException("【deliveryMode】交货方式不能为空");
            }
            // 自送货物
            if (OverseasDeliveryModeEnum.SELF_DELIVERY.getCode().equalsIgnoreCase(commonDTO.getDeliveryMode())){
                if (StringUtils.isBlank(commonDTO.getTransferWarehouseId())){
                    throw new ServiceException("【transferWarehouseId】中转仓ID不能为空");
                }
                if (StringUtils.isBlank(commonDTO.getExpressNo())){
                    throw new ServiceException("【expressNo】快递单号不能为空");
                }
                // 设置其他参数为空
                commonDTO.setBlankOtherByTransferAgentAndSelfDelivery();
            }
        }

        // 入库类型=自发头程, 交货方式=上面揽收
        if (OverseasInstockTypeEnum.TRANSFER_AGENT.equals(commonDTO.getInstockType())){
            if (StringUtils.isBlank(commonDTO.getDeliveryMode())){
                throw new ServiceException("【deliveryMode】交货方式不能为空");
            }
            // 上门揽收
            if (OverseasDeliveryModeEnum.COLLECT_AT_HOME.getCode().equalsIgnoreCase(commonDTO.getDeliveryMode())){
                if (StringUtils.isBlank(commonDTO.getTransferWarehouseId())){
                    throw new ServiceException("【transferWarehouseId】中转仓ID不能为空");
                }
                // 查询设置中转仓信息
                OverseasTransferWarehouseEntity transferEntity = overseasTransferWarehouseService.getById(commonDTO.getTransferWarehouseId());
                if (null == transferEntity){
                    throw new ServiceException("未找到中转仓");
                }
                commonDTO.setTransferWarehouseName(transferEntity.getName());

                if (null == commonDTO.getCustomsType()){
                    throw new ServiceException("【customsType】报关方式不能为空");
                }
                GoodCangEnums.CustomsTypeNewEnum customsTypeNewEnum = GoodCangEnums.CustomsTypeNewEnum.getByCode(commonDTO.getCustomsType());
                if (null == customsTypeNewEnum){
                    throw new ServiceException("【customsType】报关方式不存在");
                }
                commonDTO.setCustomsTypeName(customsTypeNewEnum.getName());
                // 谷仓校验
                if (OmsPlatformEnum.OMS_GOOD_CANG.getCode().equalsIgnoreCase(dictPlatform)){
                    if (StringUtils.isBlank(commonDTO.getLogisticsProductCode())){
                        throw new ServiceException("【logisticsProductCode】 物流产品代码不能为空");
                    }
                    // 物流产品代码
                    commonDTO.setLogisticsProductName(transferEntity.getLogisticsProductName());
                }

                if (null == commonDTO.getEstimatedCollectDate()){
                    throw new ServiceException("预计揽收日期不能为空");
                }

                if (StringUtils.isBlank(commonDTO.getDictProvinceId())){
                    throw new ServiceException("【dictProvinceId】省ID不能为空");
                }

                if (StringUtils.isBlank(commonDTO.getDictCityId())){
                    throw new ServiceException("【dictCityId】城市ID不能为空");
                }

                if (StringUtils.isBlank(commonDTO.getDictDistrictId())){
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
                if(OmsPlatformEnum.OMS_IML.getCode().equalsIgnoreCase(dictPlatform)){
                    // 查询关联
                    Map<String, ImlDictCityEntity> imlCityEntityMap =sysDictService.mapAndCheckImlCityIds(
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

                if (StringUtils.isBlank(commonDTO.getFirstName())){
                    throw new ServiceException("【firstName】姓不能为空");
                }

                if (StringUtils.isBlank(commonDTO.getLastName())){
                    throw new ServiceException("【lastName】名不能为空");
                }

                if (StringUtils.isBlank(commonDTO.getMobile())){
                    throw new ServiceException("【mobile】手机号不能为空");
                }

                if (StringUtils.isBlank(commonDTO.getStreet())){
                    throw new ServiceException("【street】详情地址不能为空");
                }
                if (StringUtils.isBlank(commonDTO.getZipcode())){
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
        if (null != commonDTO.getEstimatedCollectDate()){
            mainEntity.setEstimatedCollectDate(LocalDateTime.of(commonDTO.getEstimatedCollectDate(), LocalTime.MIN));
        }
        mainEntity.setOverseasWarehouseInboundId("");
        mainEntity.setCollectCountryCode("CN");
        // 设置仓库
        mainEntity.setToWarehouseId(deliveryEntity.getDestWarehouseId());
        mainEntity.setToWarehouseName(deliveryEntity.getDestWarehouseName());
        mainEntity.setDeliveryWarehouseId(deliveryEntity.getDeliveryWarehouseId());
        mainEntity.setDeliveryWarehouseName(deliveryEntity.getDeliveryWarehouseName());


        if (null != commonDTO.getCustomsType()){
            mainEntity.setCustomsType(commonDTO.getCustomsType().toString());
        }

        if (StringUtils.isBlank(dictPlatform)){
            if (StringUtils.isBlank(commonDTO.getCode())){
                throw new ServiceException("发货单未对接海外仓, 单号不能为空");
            }
            OverseasWarehouseInboundEntity oldEntity = this.getByCode(commonDTO.getCode());
            if (null != oldEntity){
                throw new ServiceException("code单号已存在");
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
        resultDTO.setCustomsTypeName(GoodCangEnums.CustomsTypeNewEnum.getNameByCode(Integer.parseInt(resultDTO.getCustomsType())));
        // 交货方式名称
        resultDTO.setDeliveryModeName(OverseasDeliveryModeEnum.getNameByCode(resultDTO.getDeliveryMode()));

        // 查询详情信息
        List<OverseasWarehouseInboundDetailEntity> detailEntityList = overseasWarehouseInboundDetailService.getByMainId(entity.getId());
        if (CollectionUtils.isEmpty(detailEntityList)){
            return resultDTO;
        }
        //查询skuId产品信息
        List<String> skuIds = detailEntityList.stream().map(OverseasWarehouseInboundDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        Map<String, String> imageUrlMap = plmTaskFeign.getSkuInfoByIds(skuIds)
                .stream()
                .collect(Collectors.toMap(SkuVO::getSkuId, SkuVO::checkAndGetSkuImagesUrl));

        List<OverseasWarehouseInboundDetailDTO.ViewDTO> detailDTOList = detailEntityList.stream()
                .map(e-> OverseasWarehouseInboundConverter.INSTANCE.detailEntityToViewDTO(e, imageUrlMap.getOrDefault(e.getSkuId(), "")))
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
        if (StringUtils.isNotBlank(dto.getPermissionSql())){
            queryWrapper.last(dto.getPermissionSql());
        }
        List<OverseasWarehouseInboundEntity> inStockStatusList = queryWrapper.list();

        Map<String, Integer> countMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(inStockStatusList)){
            countMap = inStockStatusList
                    .stream()
                    .collect(Collectors.toMap(OverseasWarehouseInboundEntity::getInstockStatus, OverseasWarehouseInboundEntity::getCount));
        }
        Map<String, Integer> finalCountMap = countMap;
        return Arrays.stream(OverseasInstockStatusEnum.values())
                .map(e-> new OverseasWarehouseInboundDTO.CountDTO(e.getCode(), finalCountMap.getOrDefault(e.getCode(), 0)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO cancel(String id) {
        OverseasWarehouseInboundEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException(ApiError.OVERSEAS_WAREHOUSE_INBOUND_NOT_EXIST));
        // 只有待提交的单据允许撤销
        if (!OverseasInstockStatusEnum.TO_BE_SHIPPED.getCode().equalsIgnoreCase(entity.getInstockStatus())) {
            throw new ServiceException(ApiError.OVERSEAS_WAREHOUSE_INBOUND_NOT_CANCEL);
        }
        // 更新状态
        entity.setInstockStatus(OverseasInstockStatusEnum.CANCELED.getCode());
        if (! this.updateById(entity)){
            throw new ServiceException("【海外入库单】更新状态失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】取消了单据编号为【{}】的海外入库单", commonService.getUserInfo().getUserName(), entity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.OVERSEAS_WAREHOUSE_INBOUND.getCode(), entity.getId(),"取消操作");
        // TODO 调用第三方取消接口

        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
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
        if (!this.removeById(id)){
            throw new ServiceException("【海外入库单】更新状态失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】删除了单据编号为【{}】的海外入库单", commonService.getUserInfo().getUserName(), entity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.OVERSEAS_WAREHOUSE_INBOUND.getCode(), entity.getId(),"删除操作");

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
    public OverseasWarehouseInboundEntity getBySourceId(String sourceId) {
        return lambdaQuery()
                .eq(OverseasWarehouseInboundEntity::getSourceId, sourceId)
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
