package com.erp.server.wms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.PlatformInboundDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.DictCityEntity;
import com.erp.model.sys.entity.DictThirdCity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.third.ThirdWarehouseCancelInboundReq;
import com.erp.model.wms.dto.third.ThirdWarehouseCreateInboundReq;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.oms.feign.OmsListingInfoFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.constant.WmsConstant;
import com.erp.server.wms.convert.OverseasWarehouseInboundConverter;
import com.erp.server.wms.handler.ThirdWarehouseRegistry;
import com.erp.server.wms.mapper.OverseasWarehouseInboundMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_OVERSEAS_WAREHOUSE_INBOUND;

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
    private WmsCartonDetailService wmsCartonDetailService;
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private OtherOutstockService otherOutstockService;
    @Resource
    private OtherInstockService otherInstockService;

    @Resource
    private OverseasWarehouseInboundReceivedService overseasWarehouseInboundReceivedService;

    @Resource
    private SkuMappingFeign skuMappingFeign;

    @Resource
    private PackingTaskService packingTaskService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private FileFeign fileFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(OverseasWarehouseInboundDTO.AddDTO addDTO) {
        OverseasWarehouseInboundEntity oldEntity = this.getBySourceId(addDTO.getSourceId(),  OverseasInstockStatusEnum.CANCELED.getCode());
        if (null != oldEntity) {
            throw new ServiceException("该发货单的入库单已存在");
        }

        FirstMileDeliveryEntity deliveryEntity = firstMileDeliveryService.getById(addDTO.getSourceId());
        if (Objects.isNull(deliveryEntity)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "发货单");
        }
        List<PackingTaskEntity> packingTaskEntity = packingTaskService.listBySourceCodes(Arrays.asList(deliveryEntity.getCode(),deliveryEntity.getSourceCode()));

        if (CollectionUtils.isEmpty(packingTaskEntity) || !PackingTaskStatusEnum.PACKED.getCode().equals(packingTaskEntity.get(0).getPackingStatus())) {
            throw new ServiceException(ApiError.NOT_PACKING_NOT_GENERATE_INBOUND);
        }

        // 发货单明细
        List<FirstMileDeliveryDetailEntity> deliveryDetailEntityList = firstMileDeliveryDetailService.listByMainIds(Collections.singletonList(deliveryEntity.getId()));
        if (CollectionUtils.isEmpty(deliveryDetailEntityList)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "发货单明细");
        }
        // 查询发货目的仓平台授权
        OverseasProviderEntity providerEntity = overseasProviderWarehouseService.findPlatformByWarehouseId(deliveryEntity.getDestWarehouseId());
        if(Objects.nonNull(providerEntity) && providerEntity.getCode().equals(OmsPlatformEnum.CAI_NIAO.getCode())){
            providerEntity = null;
        }
        if (FbaDemandTypeEnum.DEMAND_ALIEXPRESS.getCode().equals(deliveryEntity.getDemandType())) {
            providerEntity = null;
        }
        String dictPlatform = null == providerEntity ? "" : providerEntity.getCode();


        OverseasWarehouseInboundEntity mainEntity = new OverseasWarehouseInboundEntity();
        // 数据处理
        handleData(mainEntity, addDTO, deliveryEntity, dictPlatform);

        log.info("开始新增海外仓入库单");
        mainEntity.setIsDeleted(false);
        mainEntity.setVersion(0);
        LoginUser loginUser = UserContext.getLoginUser();
        mainEntity.setCreateUserId(loginUser.getUid());
        mainEntity.setCreateUserName(loginUser.getUserName());
        mainEntity.setCreateTime(LocalDateTime.now());
        boolean save = this.save(mainEntity);
        if (!save) {
            throw new ServiceException("海外仓入库单保存失败");
        }
        List<String> platformSkuNoList = deliveryDetailEntityList.stream()
                .map(FirstMileDeliveryDetailEntity::getPlatformSkuNo)
                .distinct()
                .collect(Collectors.toList());
        ListingInfoParamDTO listingInfoParamDTO = new ListingInfoParamDTO();
        listingInfoParamDTO.setPlatformSkuNoList(platformSkuNoList);
        listingInfoParamDTO.setPlatform(dictPlatform);
        List<SkuMappingDTO.MappingSkuViewDTO> mappingSkuViewDTOList = skuMappingFeign.listByPlatformSkuNoAndPlatform(listingInfoParamDTO);
        // 明细处理
        List<OverseasWarehouseInboundDetailEntity> detailEntityList = deliveryDetailEntityList.stream()
                .map(e -> {
                            SkuMappingDTO.MappingSkuViewDTO view = mappingSkuViewDTOList.stream().filter(v->v.getPlatformSkuNo().equals(e.getPlatformSkuNo())).findFirst().orElse(new SkuMappingDTO.MappingSkuViewDTO());
                            return OverseasWarehouseInboundConverter.INSTANCE.deliveryDetailToDetail(e, mainEntity, view.getPlatformProductName(), mainEntity.getCreateUserId(), mainEntity.getCreateUserName(), mainEntity.getCreateTime());
                        })
                .collect(Collectors.toList());

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】来源单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "海外仓入库单", mainEntity.getSourceCode());
        // 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.OVERSEAS_WAREHOUSE_INBOUND.getCode(), mainEntity.getId(), "新增操作");
        // 新增明细（如果有明细的话）
        if (!overseasWarehouseInboundDetailService.saveBatch(detailEntityList)) {
            throw new ServiceException("海外仓入库单明细保存失败");
        }
        String base64 = null;
        if (!CollectionUtils.isEmpty(addDTO.getAttachUrlList())) {
            //保存附件
            Class<OverseasWarehouseInboundEntity> aClass = OverseasWarehouseInboundEntity.class;
            TableName tableName = aClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String type = tableName.value();
            wmsAttachmentService.batchSave(addDTO.getAttachUrlList(), addDTO.getAttachNameList(), type, mainEntity.getId());
            byte[] content = fileFeign.downloadFile(addDTO.getAttachUrlList().get(0));
            if (content != null) {
                base64 = Base64.getEncoder().encodeToString(content);
                mainEntity.setBase64Str(base64);
                mainEntity.setFileName(addDTO.getAttachNameList().get(0));
            }
        }

        // 推送到第三方草稿
        if (null != providerEntity && !OmsPlatformEnum.CAI_NIAO.getCode().equals(providerEntity.getCode())) {
            // 推送到第三方草稿
            ApiResult<String> resultInfo = this.pullThirdOverseasPlatformWithSkuMapping( providerEntity, mainEntity, deliveryDetailEntityList, OverseasVerifyEnum.INIT.getCode());
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
     * 构建请求参数
     */
    private ThirdWarehouseCreateInboundReq entityToCreateInboundBill(OverseasWarehouseInboundEntity mainEntity,
                                                                     List<WmsCartonSpecDTO.PackingItemDTO> itemDTOList,
                                                                     Map<SettingEnum, String> shipperInfo,
                                                                     String verifyCode,
                                                                     String code,
                                                                     List<FirstMileDeliveryDetailEntity> deliveryDetailEntityList,
                                                                     OverseasProviderEntity providerEntity
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
        for (WmsCartonSpecDTO.PackingItemDTO itemDTO : itemDTOList) {
            FirstMileDeliveryDetailEntity  firstMileDeliveryDetailEntity = deliveryDetailEntityList.stream().filter(v->v.getSkuId().equals(itemDTO.getSkuId()) && v.getPlatformSkuNo().equals(itemDTO.getPlatformSkuNo())).findFirst().orElse(null);
            if (null == firstMileDeliveryDetailEntity) {
                String msg = CharSequenceUtil.format("海外仓入库单明细中找不到skuId为【{}】的明细", itemDTO.getSkuId());
                throw new ServiceException(msg);
            }
            ThirdWarehouseCreateInboundReq.Item currentItem = BeanUtil.copyProperties(itemDTO, ThirdWarehouseCreateInboundReq.Item.class);
            currentItem.setProductSku(firstMileDeliveryDetailEntity.getPlatformSkuNo());
            currentItem.setQuantity(itemDTO.getPackQty());
            currentItem.setBoxNo(Integer.parseInt(itemDTO.getBoxNo()));
            itemList.add(currentItem);
        }
        String contactName = shipperInfo.get(SettingEnum.WMS_OVERSEAS_INBOUND_FIRST_NAME) + shipperInfo.get(SettingEnum.WMS_OVERSEAS_INBOUND_LAST_NAME);
        String phone = shipperInfo.get(SettingEnum.WMS_OVERSEAS_INBOUND_MOBILE);
        String countryCode = shipperInfo.get(SettingEnum.WMS_OVERSEAS_INBOUND_COUNTRY_CODE);
        String stateName = shipperInfo.get(SettingEnum.WMS_OVERSEAS_INBOUND_PROVINCE_NAME);
        String cityName = shipperInfo.get(SettingEnum.WMS_OVERSEAS_INBOUND_CITY_NAME);
        String region = shipperInfo.get(SettingEnum.WMS_OVERSEAS_INBOUND_DISTRICT_NAME);
        String address1 = shipperInfo.get(SettingEnum.WMS_OVERSEAS_INBOUND_COUNTRY_CODE);
        String shopId = providerEntity.getAuthJson().getOrDefault("shopId","").toString();


        ThirdWarehouseCreateInboundReq inboundReq = ThirdWarehouseCreateInboundReq.builder()
                // 发货单号
                .referenceNo(mainEntity.getSourceCode())
                .shopId(shopId)
                .ownerCode(providerEntity.getOwnerCode())
                .fileBase64(mainEntity.getBase64Str())
                .fileName(mainEntity.getFileName())
                // 交货方式，0自送，1揽收
                .incomeType(collectingService)
                .receivingType(inStockType)
                // 入库单类型 （标准入库单，中转入库单(标准货运单)，FBA入库单）
                .transitType(inStockType)
                // 物流方式
                .receivingShippingType(receivingShippingType)
                // 跟踪号：当物流跟踪号为空的时候传快递单号
                .trackingNumber(CharSequenceUtil.isBlank(mainEntity.getTrackingNo()) ?  mainEntity.getExpressNo() : mainEntity.getTrackingNo())
                .warehouseCode(mainEntity.getPlatformToWarehouseCode())
                .etaDate(mainEntity.getEstimatedArrivalDate())
                .collectStartTime(mainEntity.getCollectStartTime())
                .collectEndTime(mainEntity.getCollectEndTime())
                // 入库单创建时取0，发货单审核通过更新为1
                .verify(verifyCode)
                .transitWarehouseCode(mainEntity.getPlatformTransferWarehouseCode())
                .smCode(mainEntity.getLogisticsProductCode())
                .containerType(mainEntity.getContainerType())
                .customsType(customsTypeValue)
                //  OpenCollectingServiceEnum： 0=自送货物，1=上门提货
                .collectingService(collectingService)
                .deliveryCode(mainEntity.getExpressNo())
                .declareType(mainEntity.getDeclareType())
                .remark(mainEntity.getRemark())
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
        if (CharSequenceUtil.isNotBlank(code)) {
            // 更新添加订单号
            inboundReq.setReceivingCode(code);
        }
        return inboundReq;
    }

    /**
     * 修改
     */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(OverseasWarehouseInboundDTO.UpdateDTO updateDTO) {
        OverseasWarehouseInboundEntity old = super.getById(updateDTO.getId());
        if (Objects.isNull(old)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "海外仓入库单");
        }
        OverseasWarehouseInboundEntity mainEntity = new OverseasWarehouseInboundEntity();
        BeanUtils.copyProperties(old, mainEntity);
        BeanUtils.copyProperties(updateDTO, mainEntity);

        mainEntity.setInstockType(updateDTO.getInstockType().getCode());
        if (null != updateDTO.getLogisticsMethod()){
            mainEntity.setLogisticsMethod(updateDTO.getLogisticsMethod().getCode());
        }
        if (null != updateDTO.getEstimatedArrivalDate()){
            mainEntity.setEstimatedArrivalDate(LocalDateTime.of(updateDTO.getEstimatedArrivalDate(), LocalTime.MIN));
        }
        if (null != updateDTO.getEstimatedCollectDate()){
            mainEntity.setEstimatedCollectDate(LocalDateTime.of(updateDTO.getEstimatedCollectDate(), LocalTime.MIN));
        }

        FirstMileDeliveryEntity deliveryEntity = firstMileDeliveryService.getById(old.getSourceId());
        if (Objects.isNull(deliveryEntity)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "发货单");
        }

        // 查询发货目的仓平台授权
        OverseasProviderEntity providerEntity = overseasProviderWarehouseService.findPlatformByWarehouseId(deliveryEntity.getDestWarehouseId());
        if(Objects.nonNull(providerEntity) && providerEntity.getCode().equals(OmsPlatformEnum.CAI_NIAO.getCode())){
            providerEntity = null;
        }
        String dictPlatform = null == providerEntity ? "" : providerEntity.getCode();

        // 发货单明细
        List<FirstMileDeliveryDetailEntity> deliveryDetailEntityList = firstMileDeliveryDetailService.listByMainIds(Collections.singletonList(deliveryEntity.getId()));
        if (CollectionUtils.isEmpty(deliveryDetailEntityList)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "发货单明细");
        }

        // 数据处理
        handleData(mainEntity, updateDTO, deliveryEntity, dictPlatform);
        log.info("编辑 开始修改海外仓入库单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(mainEntity);
        if (!save) {
            throw new ServiceException("海外仓入库单保存失败");
        }

        // 记录主单操作日志
        log.info("编辑 开始记录海外仓入库单日志数据，单号：【{}】", mainEntity.getCode());
        String msg = CharSequenceUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), mainEntity.getCode(), "海外仓入库单");
        // 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, mainEntity, null, mainEntity.getId(), msg);

        //保存附件
        Class<OverseasWarehouseInboundEntity> aClass = OverseasWarehouseInboundEntity.class;
        TableName tableName = aClass.getDeclaredAnnotation(TableName.class);
        //获取到表名
        String type = tableName.value();
        wmsAttachmentService.batchSave(updateDTO.getAttachUrlList(), updateDTO.getAttachNameList(), type, old.getId());

        String base64 = null;
        if (!CollectionUtils.isEmpty(updateDTO.getAttachUrlList())) {
            byte[] content = fileFeign.downloadFile(updateDTO.getAttachUrlList().get(0));
            if (content != null) {
                base64 = Base64.getEncoder().encodeToString(content);
                mainEntity.setBase64Str(base64);
                mainEntity.setFileName(updateDTO.getAttachNameList().get(0));
            }
        }else{
            List<WmsAttachmentDTO.UpdateDTO> attachmentList = wmsAttachmentService.getByBusinessIds(Arrays.asList(updateDTO.getId()));
            if(!CollectionUtils.isEmpty(attachmentList)){
                byte[] content = fileFeign.downloadFile(attachmentList.get(0).getAttachUrl());
                if (content != null) {
                    base64 = Base64.getEncoder().encodeToString(content);
                    mainEntity.setBase64Str(base64);
                    mainEntity.setFileName(attachmentList.get(0).getAttachName());
                }
            }

        }
        // 推送到第三方
        if (null != providerEntity) {
            if (CharSequenceUtil.isBlank(mainEntity.getCode())) {
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
                .ne(CharSequenceUtil.isNotBlank(notInStockStatus), OverseasWarehouseInboundEntity::getInstockStatus, notInStockStatus)
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

        OverseasTransferWarehouseEntity transferEntity = null;
        // 入库类型=自发头程, 交货方式=自送货物
        if (OverseasInstockTypeEnum.TRANSFER_AGENT.equals(commonDTO.getInstockType())) {
            if (CharSequenceUtil.isBlank(commonDTO.getDeliveryMode())) {
                throw new ServiceException("【deliveryMode】交货方式不能为空");
            }
            // 查询设置中转仓信息
            transferEntity = overseasTransferWarehouseService.getById(commonDTO.getTransferWarehouseId());
            if (null == transferEntity) {
                if (OmsPlatformEnum.WEI_SHI.getCode().equalsIgnoreCase(dictPlatform)) {
                    transferEntity = new OverseasTransferWarehouseEntity();
                }else{
                    throw new ServiceException("未找到中转仓");
                }
            }
            commonDTO.setTransferWarehouseName(transferEntity.getName());
            // 自送货物
            if (OverseasDeliveryModeEnum.SELF_DELIVERY.getCode().equalsIgnoreCase(commonDTO.getDeliveryMode())) {
                if (CharSequenceUtil.isBlank(commonDTO.getTransferWarehouseId()) && !OmsPlatformEnum.WEI_SHI.getCode().equalsIgnoreCase(dictPlatform)) {
                    throw new ServiceException("【transferWarehouseId】中转仓ID不能为空");
                }
                if (CharSequenceUtil.isBlank(commonDTO.getExpressNo())) {
                    throw new ServiceException("【expressNo】快递单号不能为空");
                }
                // 设置其他参数为空
                commonDTO.setBlankOtherByTransferAgentAndSelfDelivery();
            }

            if (OmsPlatformEnum.OMS_IML.getCode().equalsIgnoreCase(dictPlatform)) {
                if (CharSequenceUtil.isBlank(commonDTO.getLogisticsProductCode())) {
                    throw new ServiceException("【logisticsProductCode】 物流产品代码不能为空");
                }
                List<DictBasicDTO.ListDTO> dictList = dictBasicService.getByKey("imlLogisticProduct");
                commonDTO.setLogisticsProductName(dictList.stream().filter(v->v.getValue().equals(commonDTO.getLogisticsProductCode())).findFirst().orElse(new DictBasicDTO.ListDTO()).getName());
                if (CharSequenceUtil.isBlank(commonDTO.getDeclareType())) {
                    throw new ServiceException("报关类型不能为空");
                }
            }
        }


        // 入库类型=自发头程, 交货方式=上面揽收
        if (OverseasInstockTypeEnum.TRANSFER_AGENT.equals(commonDTO.getInstockType())) {
            if (CharSequenceUtil.isBlank(commonDTO.getDeliveryMode())) {
                throw new ServiceException("【deliveryMode】交货方式不能为空");
            }
            // 上门揽收
            if (OverseasDeliveryModeEnum.COLLECT_AT_HOME.getCode().equalsIgnoreCase(commonDTO.getDeliveryMode())) {
                if (CharSequenceUtil.isBlank(commonDTO.getTransferWarehouseId())  && !OmsPlatformEnum.WEI_SHI.getCode().equalsIgnoreCase(dictPlatform)) {
                    throw new ServiceException("【transferWarehouseId】中转仓ID不能为空");
                }
                // 查询设置中转仓信息
                transferEntity = overseasTransferWarehouseService.getById(commonDTO.getTransferWarehouseId());
                if (null == transferEntity) {
                    if (OmsPlatformEnum.WEI_SHI.getCode().equalsIgnoreCase(dictPlatform)) {
                        transferEntity = new OverseasTransferWarehouseEntity();
                    }else{
                        throw new ServiceException("未找到中转仓");
                    }
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
                    if (CharSequenceUtil.isBlank(commonDTO.getLogisticsProductCode())) {
                        throw new ServiceException("【logisticsProductCode】 物流产品代码不能为空");
                    }
                    // 物流产品代码
                    commonDTO.setLogisticsProductName(transferEntity.getLogisticsProductName());
                }

                if (null == commonDTO.getEstimatedCollectDate()) {
                    throw new ServiceException("预计揽收日期不能为空");
                }

                if (CharSequenceUtil.isBlank(commonDTO.getDictProvinceId())) {
                    throw new ServiceException("【dictProvinceId】省ID不能为空");
                }

                if (CharSequenceUtil.isBlank(commonDTO.getDictCityId())) {
                    throw new ServiceException("【dictCityId】城市ID不能为空");
                }

                if (CharSequenceUtil.isBlank(commonDTO.getDictDistrictId()) ) {
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
                if (OmsPlatformEnum.OMS_IML.getCode().equalsIgnoreCase(dictPlatform)
                || OmsPlatformEnum.OMS_ANTU.getCode().equalsIgnoreCase(dictPlatform)) {
                    // 查询关联
                    Map<String, DictThirdCity> thirdCityEntityMap = sysDictService.mapAndCheckThirdCityIds(
                            commonDTO.getDictProvinceId(),
                            commonDTO.getDictCityId(),
                            commonDTO.getDictDistrictId(),dictPlatform );
                    // 省
                    commonDTO.setPlatformProvinceId(thirdCityEntityMap.get(commonDTO.getDictProvinceId()).getRegionId());
                    // 市
                    commonDTO.setPlatformCityId(thirdCityEntityMap.get(commonDTO.getDictCityId()).getRegionId());
                    // 区
                    commonDTO.setPlatformDistrictId(thirdCityEntityMap.get(commonDTO.getDictDistrictId()).getRegionId());

                }

                if (CharSequenceUtil.isBlank(commonDTO.getFirstName())) {
                    throw new ServiceException("【firstName】姓不能为空");
                }

                if (CharSequenceUtil.isBlank(commonDTO.getLastName())) {
                    throw new ServiceException("【lastName】名不能为空");
                }

                if (CharSequenceUtil.isBlank(commonDTO.getMobile())) {
                    throw new ServiceException("【mobile】手机号不能为空");
                }

                if (CharSequenceUtil.isBlank(commonDTO.getStreet())) {
                    throw new ServiceException("【street】详情地址不能为空");
                }
                if (CharSequenceUtil.isBlank(commonDTO.getZipcode())) {
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

        if (CharSequenceUtil.isBlank(dictPlatform)) {
            if (CharSequenceUtil.isBlank(commonDTO.getCode())) {
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
        // 根据入库单ID获取入库单实体
        OverseasWarehouseInboundEntity entity = Optional.ofNullable(this.getById(dto.getId()))
                .orElseThrow(() -> new ServiceException(ApiError.OVERSEAS_WAREHOUSE_INBOUND_NOT_EXIST));
        List<OverseasWarehouseInboundDetailEntity> detailEntityList = overseasWarehouseInboundDetailService.getByMainId(entity.getId());
        List<OverseasWarehouseInboundDetailEntity> updateDetailEntityList = new ArrayList<>();
        if (overseasProviderWarehouseService.isApiWarehouse(entity.getToWarehouseId())){
            //有平台对接的入库单，判断入库状态
            if(!OverseasInstockStatusEnum.SIGNED.getCode().equals(entity.getInstockStatus())){
                throw new ServiceException("平台状态未签收完成，不能手动完结");
            }
            if(OverseasInstockStatusEnum.AUTOMATIC_COMPLETION.getCode().equals(entity.getInstockStatus())
                    || OverseasInstockStatusEnum.MANUAL_COMPLETION.getCode().equals(entity.getInstockStatus())){
                throw new ServiceException("已经自动完结或手动完结，不能手动完结");
            }
            //签收数小于发货数情况下手动完结生成其他出库单（报损）在途仓
            List<OverseasWarehouseInboundDetailEntity> lossDetailList = detailEntityList.stream().filter(v->v.getDiffQty()<0).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(lossDetailList)){
                updateDetailEntityList.addAll(lossDetailList);
                otherOutstockService.generateByOverseasInbound(entity,lossDetailList,String.format("海外仓入库单【%s】差异数据完结自动生成", entity.getCode()),true);
            }
            //签收数大于发货数情况下自动完结再签收再手动完结生成其他入库单（报溢）在途仓
            List<OverseasWarehouseInboundDetailEntity> profitDetailList = detailEntityList.stream().filter(v->v.getDiffQty()>0).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(profitDetailList)){
                updateDetailEntityList.addAll(profitDetailList);
                otherInstockService.generateByOverseasInbound(entity,profitDetailList,String.format("海外仓入库单【%s】差异数据完结自动生成", entity.getCode()),true);
            }
        }
        entity.setInstockStatus(OverseasInstockStatusEnum.MANUAL_COMPLETION.getCode());
        entity.setFinishReason(dto.getFinishReason());
        // 详情更新签收数量
        if (!this.updateById(entity)) {
            throw new ServiceException("海外仓入库单更新失败");
        }
        if(CollectionUtils.isNotEmpty(updateDetailEntityList) && !overseasWarehouseInboundDetailService.updateBatchById(updateDetailEntityList)){
            throw new ServiceException("海外仓入库明细更新失败");
        }
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.UPDATE_STATUS);
    }

    @Override
    public OverseasWarehouseInboundDTO.ViewDTO view(String id) {
        OverseasWarehouseInboundEntity entity = this.getById(id);
        if (Objects.isNull(entity)){
            throw new ServiceException(ApiError.OVERSEAS_WAREHOUSE_INBOUND_NOT_EXIST);
        }

        OverseasWarehouseInboundDTO.ViewDTO resultDTO = OverseasWarehouseInboundConverter.INSTANCE.entityToViewDTO(entity);
        // 入库类型名称
        resultDTO.setInstockTypeName(OverseasInstockTypeEnum.getNameByCode(resultDTO.getInstockType()));
        // 入库状态名称
        resultDTO.setInstockStatusName(OverseasInstockStatusEnum.getName(resultDTO.getInstockStatus()));
        // 报关方式
        resultDTO.setCustomsTypeName(OverseasCustomsTypeNewEnum.getNameByCode(resultDTO.getCustomsType()));
        // 交货方式名称
        resultDTO.setDeliveryModeName(OverseasDeliveryModeEnum.getNameByCode(resultDTO.getDeliveryMode()));
        List<DictBasicDTO.ListDTO> dictList = dictBasicService.getByKey("imlDeclareType");
        resultDTO.setDeclareTypeName(dictList.stream().filter(v->v.getValue().equals(resultDTO.getDeclareType())).findFirst().orElse(new DictBasicDTO.ListDTO()).getName());
        // 查询详情信息
        List<OverseasWarehouseInboundDetailEntity> detailEntityList = overseasWarehouseInboundDetailService.getByMainId(entity.getId());
        if (CollectionUtils.isEmpty(detailEntityList)) {
            return resultDTO;
        }
        //查询skuId产品信息
        List<String> skuIds = detailEntityList.stream().map(OverseasWarehouseInboundDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        Map<String, String> imageUrlMap = plmTaskFeign.listSkuProductByIds(skuIds)
                .stream()
                .collect(Collectors.toMap(SkuVO::getSkuId, SkuVO::checkAndGetSkuImagesUrl));

        List<OverseasWarehouseInboundDetailDTO.ViewDTO> detailDTOList = detailEntityList.stream()
                .map(e -> OverseasWarehouseInboundConverter.INSTANCE.detailEntityToViewDTO(e, imageUrlMap.getOrDefault(e.getSkuId(), "")))
                .collect(Collectors.toList());
        resultDTO.setDetailList(detailDTOList);

        //获取到附件信息
        List<WmsAttachmentDTO.UpdateDTO> attachmentList = wmsAttachmentService.getByBusinessIds(Collections.singletonList(entity.getId()));
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
        List<OverseasWarehouseInboundDTO.CountDTO> list = baseMapper.tabList(dto);
        // 获取入库状态列表
        List<String> instockStatus = OverseasInstockStatusEnum.getStatusList();

        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(OverseasWarehouseInboundDTO.CountDTO::getTabFlag).collect(Collectors.toList());
        instockStatus.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
                list.add(new OverseasWarehouseInboundDTO.CountDTO(status, 0));
            }
        });

        list.add(new OverseasWarehouseInboundDTO.CountDTO("all", list.stream().mapToInt(OverseasWarehouseInboundDTO.CountDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO cancel(String id) {
        OverseasWarehouseInboundEntity mainEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException(ApiError.OVERSEAS_WAREHOUSE_INBOUND_NOT_EXIST));
        boolean isApi = overseasProviderWarehouseService.isApiWarehouse(mainEntity.getToWarehouseId());
        if(isApi){
            // 只有待提交的单据允许撤销
            if (!OverseasInstockStatusEnum.TO_BE_SHIPPED.getCode().equalsIgnoreCase(mainEntity.getInstockStatus())) {
                throw new ServiceException(ApiError.OVERSEAS_WAREHOUSE_INBOUND_NOT_CANCEL);
            }
        }else{
            // 无API对接的三方仓入库单，可以在“待签收”状态下，操作取消入库
            if (!OverseasInstockStatusEnum.TO_BE_SHIPPED.getCode().equalsIgnoreCase(mainEntity.getInstockStatus())
             && !OverseasInstockStatusEnum.TO_BE_SIGNED.getCode().equalsIgnoreCase(mainEntity.getInstockStatus())) {
                throw new ServiceException(ApiError.OVERSEAS_WAREHOUSE_INBOUND_NOT_CANCEL);
            }
        }
        // 更新状态
        mainEntity.setInstockStatus(OverseasInstockStatusEnum.CANCELED.getCode());
        if (!this.updateById(mainEntity)) {
            throw new ServiceException("【海外入库单】更新状态失败");
        }

        FirstMileDeliveryEntity deliveryEntity = firstMileDeliveryService.getById(mainEntity.getSourceId());
        if (Objects.isNull(deliveryEntity)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "发货单");
        }

        // 查询发货目的仓平台授权
        OverseasProviderEntity providerEntity = overseasProviderWarehouseService.findPlatformByWarehouseId(deliveryEntity.getDestWarehouseId());
        if(Objects.nonNull(providerEntity) && providerEntity.getCode().equals(OmsPlatformEnum.CAI_NIAO.getCode())){
            providerEntity = null;
            isApi = false;
        }
        // 发货单明细
        List<FirstMileDeliveryDetailEntity> deliveryDetailEntityList = firstMileDeliveryDetailService.listByMainIds(Collections.singletonList(deliveryEntity.getId()));
        if (CollectionUtils.isEmpty(deliveryDetailEntityList)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "发货单明细");
        }
        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】取消了单据编号为【{}】的海外入库单", UserContext.getDefaultLoginUser().getUserName(), mainEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.OVERSEAS_WAREHOUSE_INBOUND.getCode(), mainEntity.getId(), "取消操作");

        if (isApi){
            if (CharSequenceUtil.isBlank(mainEntity.getCode())) {
                throw new ServiceException("数据异常：历史入库单未有单号");
            }
            OverseasProviderWarehouseEntity overseasProviderWarehouseEntity = overseasProviderWarehouseService.getByWarehouseId(mainEntity.getToWarehouseId());
            // 请求第三方
            ThirdWarehouseCancelInboundReq cancelInboundReq = new ThirdWarehouseCancelInboundReq();
            cancelInboundReq.setReceivingCode(mainEntity.getCode());
            cancelInboundReq.setSourceCode(mainEntity.getSourceCode());
            cancelInboundReq.setOwnerCode(providerEntity.getOwnerCode());
            String shopId = providerEntity.getAuthJson().getOrDefault("shopId","").toString();

            cancelInboundReq.setShopId(shopId);
            cancelInboundReq.setWarehouseCode(overseasProviderWarehouseEntity.getPlatformWarehouseCode());
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
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
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
        String msg = CharSequenceUtil.format("用户【{}】删除了单据编号为【{}】的海外入库单", UserContext.getDefaultLoginUser().getUserName(), entity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.OVERSEAS_WAREHOUSE_INBOUND.getCode(), entity.getId(), "删除操作");

        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    @Override
    public Boolean exportExcel(OverseasWarehouseInboundDTO.ExportDTO dto) {
        downloadTaskFeign.saveDownloadTask("海外入库单数据", EXPORT_WMS_OVERSEAS_WAREHOUSE_INBOUND.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public List<String> getReceiptNumbersForStatus(List<String> statusList, String authId) {
        //查询授权信息下有关联erp仓库的数据
        List<OverseasProviderWarehouseEntity> overseasProviderWarehouseEntities = overseasProviderWarehouseService.listByMainIds(Arrays.asList(authId));
        overseasProviderWarehouseEntities = overseasProviderWarehouseEntities.stream().filter(v->StringUtils.isNotBlank(v.getWarehouseId()) && !v.getDisabled()).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(overseasProviderWarehouseEntities)){
            return Collections.emptyList();
        }
        List<String> warehouseIds = overseasProviderWarehouseEntities.stream().map(OverseasProviderWarehouseEntity::getWarehouseId).distinct().collect(Collectors.toList());
        return this.list(Wrappers.<OverseasWarehouseInboundEntity>lambdaQuery()
                        .in(OverseasWarehouseInboundEntity::getInstockStatus, statusList)
                        .in(OverseasWarehouseInboundEntity::getToWarehouseId,warehouseIds))
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
                .ne(CharSequenceUtil.isNotBlank(notInStockStatus), OverseasWarehouseInboundEntity::getInstockStatus, notInStockStatus)
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
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public String generateTransferOut(OverseasWarehouseInboundEntity mainEntity, List<OverseasWarehouseInboundDetailEntity> detailEntityList, Map<String, Integer> receiverdMap) {
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(Arrays.asList(mainEntity.getToWarehouseId(), mainEntity.getDeliveryWarehouseId()));

        //如果目的仓没有配置在途归属仓，需要提示：目的仓没有配置在途归属仓库，请在【仓库列表】配置后再审核
//        if (CharSequenceUtil.isBlank(destWarehouse.getOnwayWarehouseId())) {
//            throw new ServiceException(ApiError.ONWAY_WAREHOUSE_NOT_EXIST);
//        }

        // 仓库列表配置的在途归属仓库
        //仓库列表配置的在途归属仓库，目的仓为FBA第三方仓时，在途仓优先取仓库列表配置，配置为空时默认为“FBA在途仓-xgwj-fba”
        WarehouseDTO.UpdateDTO destWarehouse = warehouseList.stream().filter(req -> req.getId().equals(mainEntity.getToWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());

        //校验目的仓是否为FBA第三方仓
        List<DictBasicDTO.ListDTO> warehouseTypes = dictBasicService.getByKey("warehouseType");
        DictBasicDTO.ListDTO listDTO = warehouseTypes.stream().filter(req -> "FBA".equals(req.getValue())).findFirst().orElse(null);
        //如果是FBA第三方仓
        if (listDTO.getId().equals(destWarehouse.getTypeId())) {

            //如果配置为空时默认为“FBA在途仓-xgwj-fba”
            if (CharSequenceUtil.isBlank(destWarehouse.getOnwayWarehouseId())) {
                List<WarehouseEntity> warehouseEntities = warehouseService.listByKingdeeCodeList(Collections.singletonList("xgwj-fba"));
                if (CollectionUtils.isEmpty(warehouseEntities)) {
                    throw new ServiceException(ApiError.WAREHOUSE_CODE_XGWJ_FBA_NOT_EXIST);
                }
                destWarehouse.setOnwayWarehouseId(warehouseEntities.get(0).getId());
                destWarehouse.setOnwayWarehouseName(warehouseEntities.get(0).getName());
            }
        }

        //查询在途仓
        WarehouseEntity warehouseEntity = warehouseService.getById(destWarehouse.getOnwayWarehouseId());

        TransferInfoDTO.AddDTO addDTO = new TransferInfoDTO.AddDTO();
        //默认来源类型：FBA货件
        addDTO.setSourceType(SourceTypeEnum.OVERSEAS_INBOUND.getCode());
        //默认调出日期：当前日期
        LocalDateTime receiveTime = detailEntityList.stream().map(OverseasWarehouseInboundDetailEntity::getReceiveTime).filter(Objects::nonNull).findFirst().orElse(LocalDateTime.now());
        addDTO.setBillDate(LocalDate.from(receiveTime));
        //默认调拨方向：普通
        addDTO.setTransferDirection(TransferDirectionEnum.ORDINARY.getCode());
        //调入组织
        addDTO.setInOrgId(destWarehouse.getOrgId());
        //调出组织
        WarehouseDTO.UpdateDTO deliveryWarehouse = warehouseList.stream().filter(req -> req.getId().equals(mainEntity.getDeliveryWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
        addDTO.setOutOrgId(warehouseEntity.getOrgId());
        //调拨类型
        if (destWarehouse.getOrgId().equals(warehouseEntity.getOrgId())) {
            addDTO.setType(TransferTypeEnum.IN_ORG.getCode());
        } else {
            addDTO.setType(TransferTypeEnum.CROSS_ORG.getCode());
        }
        addDTO.setSourceId(mainEntity.getId());
        addDTO.setSourceCode(mainEntity.getCode());
        addDTO.setRemark(String.format("海外仓入库单【%s】签收自动创建", mainEntity.getCode()));

        //详情信息
        List<TransferInfoDetailDTO.AddDTO> detailAddDtoList = new LinkedList<>();
        for (OverseasWarehouseInboundDetailEntity detailEntity : detailEntityList) {
            Integer receiverQty = receiverdMap.get(detailEntity.getId());
            if (null == receiverQty){
                throw new ServiceException("签收数据异常");
            }
            TransferInfoDetailDTO.AddDTO detailAddDto = new TransferInfoDetailDTO.AddDTO();
            //映射产品信息
            detailAddDto.setSkuId(detailEntity.getSkuId());
            detailAddDto.setSkuNo(detailEntity.getSkuNo());
            detailAddDto.setQty(receiverQty);
            detailAddDto.setOutWarehouseId(destWarehouse.getOnwayWarehouseId());
            detailAddDto.setOutWarehouseLocation("");
            detailAddDto.setInWarehouseId(mainEntity.getToWarehouseId());
            detailAddDto.setInWarehouseLocation("");
            detailAddDto.setSourceDetailId(detailEntity.getId());
            detailAddDtoList.add(detailAddDto);
        }

        addDTO.setDetailList(detailAddDtoList);
        return transferInfoService.add(addDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
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
        // 调用
        return this.pullThirdOverseasPlatformWithSkuMapping( providerEntity, mainEntity, deliveryDetailEntityList, verityCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public ApiResult<String> pullThirdOverseasPlatformWithSkuMapping(OverseasProviderEntity providerEntity,
                                                                     OverseasWarehouseInboundEntity mainEntity,
                                                                     List<FirstMileDeliveryDetailEntity> deliveryDetailEntityList,
                                                                     String verityCode
    ) {
        // 查询包装信息
        FirstMileDeliveryEntity firstMileDeliveryEntity = firstMileDeliveryService.getById(mainEntity.getSourceId());
        String requisitionId = Objects.nonNull(firstMileDeliveryEntity)?firstMileDeliveryEntity.getSourceId():"";

        List<WmsCartonSpecDTO.PackingItemDTO> packingQtyDTOS = wmsCartonDetailService.boxInfoBySourceIds(Arrays.asList(requisitionId,mainEntity.getSourceId()));
        if (CollectionUtils.isEmpty(packingQtyDTOS)) {
            String format = CharSequenceUtil.format("【{}】发货单：未找到装箱信息", mainEntity.getSourceCode());
            throw new ServiceException(format);
        }

        // 查询默认发货人
        Map<SettingEnum, String> shipperInfo = dmpTaskFeign.getCfgSettingList(SettingEnum.WMS_OVERSEAS_INBOUND);

        // 第三方单号：新增为空, 编辑不为空
        String code = mainEntity.getCode();

        // 请求第三方
        ThirdWarehouseCreateInboundReq createInboundReq = entityToCreateInboundBill(mainEntity, packingQtyDTOS, shipperInfo, verityCode, code,deliveryDetailEntityList,providerEntity);
        ThirdWarehouseService handlerService = thirdWarehouseRegistry.getHandlerByAuthId(providerEntity.getId());
        log.info("推送第三方仓库: dto={}", JSONUtil.toJsonStr(createInboundReq));
        if (CharSequenceUtil.isBlank(code)){
            // 新增
            return handlerService.createInboundBill(createInboundReq, providerEntity.getId());
        } else {
            // 编辑
            return handlerService.editInboundBill(createInboundReq, providerEntity.getId());
        }
    }

    private String getFinishStatusByReceiveStatus(String receiveStatus,boolean isAllDiffZero){
        if(isAllDiffZero){
            return OverseasInstockStatusEnum.AUTOMATIC_COMPLETION.getCode();
        }
        return receiveStatus;
    }

    private void groupBySku(PlatformInboundDTO dto) {
        List<PlatformInboundDTO.Item> items = dto.getItems();
        Map<String, Integer> mergedMap = items.stream()
                .collect(Collectors.toMap(PlatformInboundDTO.Item::getProductSku, PlatformInboundDTO.Item::getReceivedQuantity, Integer::sum));
        dto.setItems(mergedMap.entrySet().stream()
                .map(entry -> new PlatformInboundDTO.Item(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public ApiResult<?> handlePlatformMessage(PlatformInboundDTO dto) {
        boolean changeFlag = Boolean.FALSE;
        if(StringUtils.isBlank(dto.getReceivingCode()) && StringUtils.isBlank(dto.getSourceCode())){
            return ApiResult.success();
        }
        //根据sku汇总数量
        this.groupBySku(dto);
        //通过单号查询主表记录
        OverseasWarehouseInboundEntity mainEntity;
        if(StringUtils.isBlank(dto.getReceivingCode())){
            mainEntity = this.getBySourceCode(dto.getSourceCode());
        }else{
            mainEntity = this.getByCode(dto.getReceivingCode(), null);
        }
        if (Objects.isNull(mainEntity)) {
            return ApiResult.success();
        }
        // 谷仓有入库流水忽略时间校验
        if (Objects.nonNull(mainEntity.getReceiveTime())
                && dto.getDownloadTime().isBefore(mainEntity.getReceiveTime())
                && !PlatformDictEnum.GOOD_CANG.getCode().equalsIgnoreCase(dto.getPlatform())
                && !PlatformDictEnum.DA_MAI.getCode().equalsIgnoreCase(dto.getPlatform())) {
            return ApiResult.success();
        }
        //更新入库状态
        String receivingStatus = dto.getReceivingStatus();
        if(!receivingStatus.equals(mainEntity.getInstockStatus())){
            this.lambdaUpdate().set(OverseasWarehouseInboundEntity::getInstockStatus, receivingStatus).eq(OverseasWarehouseInboundEntity::getId, mainEntity.getId()).update();
        }
        //查询明细数据
        List<OverseasWarehouseInboundDetailEntity> detailList = overseasWarehouseInboundDetailService.getByMainId(mainEntity.getId());
        Map<String, OverseasWarehouseInboundDetailEntity> detailEntityMap = detailList.stream().collect(Collectors.toMap(OverseasWarehouseInboundDetailEntity::getPlatformSkuNo, Function.identity()));
        Map<String, PlatformInboundDTO.Item> itemMap = dto.getItems().stream().collect(Collectors.toMap(PlatformInboundDTO.Item::getProductSku, Function.identity()));
        List<OverseasWarehouseInboundReceivedEntity> insertReceiveEntityList = new ArrayList<>();
        Map<String, OverseasWarehouseInboundDetailEntity> updateDetailEntityMap = new HashMap<>();
        Map<String, Integer> thisSignQtyMap = new HashMap<>();
        //更新明细表
        for (OverseasWarehouseInboundDetailEntity detailEntity : detailList) {
            PlatformInboundDTO.Item item = itemMap.get(detailEntity.getPlatformSkuNo());
            if (Objects.isNull(item)) {
                continue;
            }
            //签收数量不一致才更新
            if (item.getReceivedQuantity().equals(detailEntity.getReceiveQty())) {
                continue;
            }
            changeFlag = true;
            Integer thisSignNumber = item.getReceivedQuantity() - detailEntity.getReceiveQty();
            thisSignQtyMap.put(detailEntity.getId(), thisSignNumber);
            detailEntity.setReceiveQty(item.getReceivedQuantity());
            detailEntity.setDiffQty(detailEntity.getReceiveQty() - detailEntity.getPackQty());
            detailEntity.setTransportQty(detailEntity.getPackQty() - detailEntity.getReceiveQty());
            detailEntity.setReceiveTime(dto.getDownloadTime());
            detailEntity.setReceiveStatus("already");
            detailEntity.setReceiveType("system");
            updateDetailEntityMap.put(detailEntity.getId(), detailEntity);
            //如果没有签收数据，在这里封装签收记录
            if (!dto.getHasReceivedData()) {
                OverseasWarehouseInboundReceivedEntity receivedEntity = new OverseasWarehouseInboundReceivedEntity();
                receivedEntity.setDetailId(detailEntity.getId());
                receivedEntity.setReceiveQty(thisSignNumber);
                receivedEntity.setReceiveTime(dto.getDownloadTime());
                receivedEntity.setSourceType(SignSourceTypeEnum.API.getCode());
                insertReceiveEntityList.add(receivedEntity);
            }
        }
        if(CollectionUtils.isNotEmpty(updateDetailEntityMap.values())){
            overseasWarehouseInboundDetailService.updateBatchById(updateDetailEntityMap.values());
        }
        List<String> detailIds = detailList.stream().map(OverseasWarehouseInboundDetailEntity::getId).collect(Collectors.toList());
        List<OverseasWarehouseInboundReceivedEntity> receivedEntityList = overseasWarehouseInboundReceivedService.listByDetailIds(detailIds);
        Map<String, OverseasWarehouseInboundReceivedEntity> receivedEntityMap = receivedEntityList.stream().collect(Collectors.toMap(v -> v.getDetailId() + v.getReceiveQty() + LocalDateTimeUtil.formatNormal(v.getReceiveTime()), Function.identity(), (v1, v2) -> v1));
        //有签收记录直接保存，没有签收记录判断签收数量与数据库是否一致，不一致的话用签收数量-数据库签收数量
        if (dto.getHasReceivedData() && CollectionUtils.isNotEmpty(dto.getReceivingDataList())) {
            //判断是否存在，通过明细id+数量+时间
            for (PlatformInboundDTO.Receiving receiving : dto.getReceivingDataList()) {
                OverseasWarehouseInboundDetailEntity detailEntity = detailEntityMap.get(receiving.getProductSku());
                String detailId = detailEntity.getId();
                if (StringUtil.isBlank(detailId)) {
                    continue;
                }
                if (PlatformDictEnum.GOOD_CANG.getCode().equalsIgnoreCase(dto.getPlatform())){
                    // 按流水ID判断已存在
                    if (receivedEntityList.stream().anyMatch(e -> e.getFlowId().equals(receiving.getThirdId()) && e.getCreateUserId().equals(dto.getAuthId()))){
                        continue;
                    }
                }else if(PlatformDictEnum.DA_MAI.getCode().equalsIgnoreCase(dto.getPlatform())){
                    // 先执行流水ID判断
                    if (receivedEntityList.stream().anyMatch(e ->
                            e.getFlowId().equals(receiving.getThirdId()) && e.getCreateUserId().equals(dto.getAuthId())
                    )) {
                        continue;
                    }
                    // 再执行key判断
                    String key = detailId + receiving.getReceiveQty() + LocalDateTimeUtil.formatNormal(receiving.getReceiveTime());
                    if (receivedEntityMap.containsKey(key)) {
                        continue;
                    }
                } else {
                    String key = detailId + receiving.getReceiveQty() + LocalDateTimeUtil.formatNormal(receiving.getReceiveTime());
                    if (receivedEntityMap.containsKey(key)) {
                        continue;
                    }
                }


                updateDetailEntityMap.put(detailId, detailEntity);
                changeFlag = true;
                OverseasWarehouseInboundReceivedEntity receivedEntity = new OverseasWarehouseInboundReceivedEntity();
                receivedEntity.setDetailId(detailId);
                receivedEntity.setReceiveQty(receiving.getReceiveQty());
                receivedEntity.setReceiveTime(receiving.getReceiveTime());
                receivedEntity.setSourceType(SignSourceTypeEnum.API.getCode());
                receivedEntity.setFlowId(StringUtil.isBlank(receiving.getThirdId()) ? "" : receiving.getThirdId());
                receivedEntity.setCreateUserId(dto.getAuthId());
                insertReceiveEntityList.add(receivedEntity);
            }
        }
        if (CollectionUtils.isNotEmpty(insertReceiveEntityList)) {
            overseasWarehouseInboundReceivedService.saveBatch(insertReceiveEntityList);
        }

        List<OverseasWarehouseInboundDetailEntity> updateList = new ArrayList<>(updateDetailEntityMap.values());
        if (changeFlag) {
            mainEntity.setReceiveTime(dto.getDownloadTime());
            if(OverseasInstockStatusEnum.MANUAL_COMPLETION.getCode().equals(mainEntity.getInstockStatus())){
                //设置差异数为本次签收数
                updateList.forEach(v -> v.setDiffQty(thisSignQtyMap.get(v.getId())));
                List<OverseasWarehouseInboundDetailEntity> greaterThanZeroReceiveList = updateList.stream().filter(v->v.getDiffQty()>0).collect(Collectors.toList());
                List<OverseasWarehouseInboundDetailEntity> lessThanZeroReceiveList = updateList.stream().filter(v->v.getDiffQty()<0).collect(Collectors.toList());
                //手动完结再签收，不变更入库状态，生成其他入库单（报溢）目的仓，不生成调拨单
                if(CollectionUtils.isNotEmpty(greaterThanZeroReceiveList)){
                    otherInstockService.generateByOverseasInbound(mainEntity, greaterThanZeroReceiveList, String.format("海外仓入库单【%s】签收自动创建", mainEntity.getCode()), false);
                }
                //手动完结时，海外仓入库单同步负数签收数据时，生成其他出库单，目的仓报损
                if(CollectionUtils.isNotEmpty(lessThanZeroReceiveList)){
                    otherOutstockService.generateByOverseasInbound(mainEntity, lessThanZeroReceiveList, String.format("【%s】签收数量减少后出库反冲", mainEntity.getCode()), false);
                }
            }else{
                //生成直接调拨单（根据数量是否为负数判断）
                List<OverseasWarehouseInboundReceivedEntity> greaterThanZeroReceiveList = insertReceiveEntityList.stream().filter(v->v.getReceiveQty() > 0).collect(Collectors.toList());
                List<OverseasWarehouseInboundReceivedEntity> lessThanZeroReceiveList = insertReceiveEntityList.stream().filter(v->v.getReceiveQty() < 0).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(greaterThanZeroReceiveList)){
                    transferInfoService.generateFromOverseasInbound(mainEntity, updateList, greaterThanZeroReceiveList, String.format("海外仓入库单【%s】签收自动创建", mainEntity.getCode()),false);
                }
                if(CollectionUtils.isNotEmpty(lessThanZeroReceiveList)){
                    transferInfoService.generateFromOverseasInbound(mainEntity, updateList, lessThanZeroReceiveList, String.format("【%s】签收数量减少后反向调拨", mainEntity.getCode()),true);
                }
                if(OverseasInstockStatusEnum.AUTOMATIC_COMPLETION.getCode().equals(mainEntity.getInstockStatus())){
                    mainEntity.setInstockStatus(OverseasInstockStatusEnum.SIGNED.getCode());
                }else{
                    //差异数量为0时 自动完结
                    boolean isAllDiffZero = detailList.stream().allMatch(v -> v.getDiffQty().equals(0));
                    mainEntity.setInstockStatus(this.getFinishStatusByReceiveStatus(dto.getReceivingStatus(), isAllDiffZero));
                }
            }
            //更新主表
            this.updateById(mainEntity);
        }
        return ApiResult.success();
    }

    private OverseasWarehouseInboundEntity getBySourceCode(String sourceCode) {
        return lambdaQuery()
                .eq(OverseasWarehouseInboundEntity::getSourceCode, sourceCode)
                .orderByDesc(OverseasWarehouseInboundEntity::getCreateTime)
                .last("LIMIT 1")
                .one();
    }

    @Override
    public List<FirstMileDeliveryDTO.ReceiveDTO> countReceiveQtyByParams(FirstMileDeliveryDTO.RequestReceiveDTO dto) {
        if (Objects.isNull(dto) || (CollectionUtils.isEmpty(dto.getSourceCodes()) && Objects.isNull(dto.getMonth()) && CollectionUtils.isEmpty(dto.getBusinessCodes()))){
            return Collections.emptyList();
        }
        return baseMapper.countReceiveQtyByParams(dto);
    }

    @Override
    public List<BaseDropDownDTO.CommonDTO> getLogisticByTransferWarehouseId(String transferWarehouseId) {
        OverseasTransferWarehouseEntity transferEntity = overseasTransferWarehouseService.getById(transferWarehouseId);
        if (null == transferEntity){
            return Collections.emptyList();
        }
        String warehouseCode = transferEntity.getPlatformWarehouseCode();
        List<LogisticsSaleChannelEntity> list = FeignQuery.create(LogisticsSaleChannelEntity.class).eq(LogisticsSaleChannelEntity::getPlatformWarehouseCode,warehouseCode).list();
        return list.stream()
                .map(e-> new BaseDropDownDTO.CommonDTO(e.getCode(), e.getCnName()+"["+e.getCode()+"]"))
                .collect(Collectors.toList());
    }

    @Override
    public PagingVO<OverseasWarehouseInboundDTO.ListDTO> exportOverseasWarehouseInbound(PagingDTO<OverseasWarehouseInboundDTO.ExportDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<OverseasWarehouseInboundDTO.ListDTO> page = baseMapper.listExportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            //数据处理
            fillList(page.getRecords());
        }
        return new PagingVO<>(page);
    }

    @Override
    public List<OverseasWarehouseInboundDetailDTO.ViewChangeDTO> viewChangeList(OverseasWarehouseInboundDTO.ViewListReqDTO dto) {
        if (Objects.isNull(dto) || CollUtil.isEmpty(dto.getRequestIdList()) || Objects.isNull(dto.getRequestIdType()) || CharSequenceUtil.isBlank(dto.getRequestIdType().getCode())){
            return Collections.emptyList();
        }
        return baseMapper.viewChangeList(dto);
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<OverseasWarehouseInboundDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        //获取sku信息
        List<String> skuIdList = list.stream().map(OverseasWarehouseInboundDTO.ListDTO::getSkuId).collect(Collectors.toList());

        //获取库存sku信息
        List<SkuMappingDTO.ListStockSkuNoByProductSkuIdView> listStockSkuNoByProductSkuIdViews = omsListingInfoFeign.listStockSkuNoByProductSkuIds(skuIdList);

        // 属性赋值
        for (OverseasWarehouseInboundDTO.ListDTO data : list) {
            // 入库类型名称
            data.setInstockTypeName(OverseasInstockTypeEnum.getNameByCode(data.getInstockType()));
            // 入库状态名称
            data.setInstockStatusName(OverseasInstockStatusEnum.getName(data.getInstockStatus()));
            // 物流方式
            data.setLogisticsMethodName(LogisticsMethodEnum.getName(data.getLogisticsMethod()));
            // 交货方式
            data.setDeliveryModeName(OverseasDeliveryModeEnum.getNameByCode(data.getDeliveryMode()));

            // 无api对接平台查询映射关系
            if (CharSequenceUtil.isBlank(data.getDictPlatform())){
                //获取库存sku
                SkuMappingDTO.ListStockSkuNoByProductSkuIdView listStockSkuNoByProductSkuIdView = listStockSkuNoByProductSkuIdViews.stream()
                        .filter(req -> req.getProductSkuId().equals(data.getSkuId())
                                && (req.getHasMappingAll() || req.getWarehouseId().equals(data.getToWarehouseId()))
                        ).distinct()
                        .findFirst().orElse(null);

                if (null != listStockSkuNoByProductSkuIdView) {
                    data.setPlatformSkuNo(listStockSkuNoByProductSkuIdView.getStockSku());
                    data.setPlatformProductName(listStockSkuNoByProductSkuIdView.getStockSkuName());
                }
            }
        }
    }

}
