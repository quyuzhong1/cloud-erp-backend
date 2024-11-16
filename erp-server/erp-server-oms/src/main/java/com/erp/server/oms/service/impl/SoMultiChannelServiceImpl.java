package com.erp.server.oms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.PlatformOrderDetailDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.PlatformDictEnum;

import com.common.business.enums.SourceTypeEnum;
import com.common.core.enums.ApiError;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cPayStatusEnum;
import com.erp.model.plm.vo.SkuInfoSimpleVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.server.oms.convert.B2cOrderConsumerConverter;
import com.erp.server.oms.convert.SoMultiChannelConsumerConverter;
import com.erp.server.oms.mapper.SoMultiChannelMapper;
import com.erp.server.oms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.util.*;

/**
 * <p>
 * 多渠道订单 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2024-05-30
 */
@Slf4j
@Service
public class SoMultiChannelServiceImpl extends SuperServiceImpl<SoMultiChannelMapper, SoMultiChannelEntity> implements SoMultiChannelService {

    @Resource
    private SoMultiChannelDetailService soMultiChannelDetailService;
    @Resource
    private SkuMappingService skuMappingService;
    @Resource
    private ShopInfoService shopInfoService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private OperateLogService operateLogService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public SoMultiChannelEntity handleSave(PlatformOrderDTO dto) {
        // 查询关联关系
        List<String> platformSkuList = dto.convertPlatformSkuList();

        // 速卖通同店铺存在相同SkuNo需要配合平台产ID/SPU查询
        List<String> platformSpuList = new LinkedList<>();
        if (PlatformDictEnum.ALI_EXPRESS.getCode().equalsIgnoreCase(dto.getPlatform())
                || PlatformDictEnum.MERCADOLIBRE.getCode().equalsIgnoreCase(dto.getPlatform())
                || PlatformDictEnum.SHOPIFY.getCode().equalsIgnoreCase(dto.getPlatform())
                || PlatformDictEnum.TIK_TOK.getCode().equalsIgnoreCase(dto.getPlatform())){
            platformSpuList = dto.convertPlatformSpuList();
        }

        Map<String, List<ListingInfoWithSkuMappingDTO>> listingInfoWithSkuMappingDTOMap = skuMappingService.mapListingByPlatformSkuNo(platformSkuList, platformSpuList, dto.getDictPlatform(), dto.getShopId(), dto.getPlatformOrderCreateTime(), null);

        // 查询当前店铺信息
        ShopInfoEntity shopInfo = shopInfoService.getById(dto.getShopId());
        if (null == shopInfo) {
            throw new ServiceException("未找到订单的店铺" + dto.getShopId());
        }

        List<String> skuIds = listingInfoWithSkuMappingDTOMap.values().stream()
                .flatMap(List::stream)
                .map(ListingInfoWithSkuMappingDTO::getProductSkuId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<SkuInfoSimpleVO> skuList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(skuIds)) {
            skuList = plmTaskFeign.getSimpleSkuInfoByIds(skuIds);
        }

        // 主表更新或保存
        SoMultiChannelEntity mainEntity =  this.saveOrUpdateEntity(dto, shopInfo);

        // 详情更新或保存
        soMultiChannelDetailService.saveOrUpdateEntity(dto, mainEntity, listingInfoWithSkuMappingDTOMap, shopInfo, skuList);
        return mainEntity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SoMultiChannelEntity saveOrUpdateEntity(PlatformOrderDTO dto, ShopInfoEntity shopInfo) {
        if (dto.getInvalidStatus()) {
            dto.setRemark("平台取消");
        }
        // 平台来源币种为空取默认币种
        if (StringUtils.isBlank(dto.getCurrency())){
            dto.setCurrency(shopInfo.getDefaultCurrency());
        }
        log.debug("===== start saveOrUpdateEntity:{}", dto);
        SoMultiChannelEntity oldEntity = null;
        try {
            oldEntity = this.getByPlatformInfo(dto.getPlatformCode(), dto.getDictPlatform(), dto.getShopId(), SourceTypeEnum.SO_MULTI_CHANNEL.getCode());
        } catch (Exception e) {
            log.error("查询订单异常：{}", e.getMessage());
        }
        if (null == oldEntity) {
            // 组合信息
            SoMultiChannelEntity entity = new SoMultiChannelEntity();
            BeanUtils.copyProperties(dto, entity);
            handleData(entity, false, false);
            if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(dto.getApproveStatusStr())) {
                ApproveStatusEnum approveStatusEnum = ApproveStatusEnum.getByStatus(dto.getApproveStatusStr());
                if (null == approveStatusEnum) {
                    String msg = StrUtil.format("[{}]审核状态类型存在:{}", dto.getUniqueId(), dto.getApproveStatusStr());
                    throw new ServiceException(msg);
                }
                entity.setApproveStatus(approveStatusEnum);
            }
            // 检查新增自动作废
            // Shopify全退款的订单新增自动作废
            entity.setInvalidStatus(dto.checkInsertInvalidStatus());
            // 生成单号
            String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_XSDD);
            entity.setCode(code);
            boolean save = false;
            try {
                save = this.save(entity);
            } catch (Exception e) {
                log.error("报错实体：{}", entity);
                log.error("保存多渠道订单信息异常：PlatformCode：{},{}", dto.getPlatformCode(), e.getMessage());
            }
            if (!save) {
                throw new ServiceException("soB2c订单保存失败");
            }
            // 新增日志
            String msg = StrUtil.format("从【{}】平台下载订单成功", dto.getDictPlatform());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_MULTI_CHANNEL.getCode(), entity.getId(), "新增操作");
            return entity;
        } else {
            // 历史异常记录修复
            if (StringUtils.isBlank(oldEntity.getCode()) && !BusinessCommonConstants.hasProfile("prod")) {
                String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_DQDD);
                oldEntity.setCode(code);
            }
            if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(oldEntity.getShopId()) && !BusinessCommonConstants.hasProfile("prod")) {
                oldEntity.setShopId(dto.getShopId());
            }
            //

            if (0 == oldEntity.getExchangeRate().compareTo(BigDecimal.ZERO)) {
                handleData(oldEntity, false, false);
            }
            ApproveStatusEnum oldApproveStatus = oldEntity.getApproveStatus();
            // 自发货订单如果来源状态是带配货不更新状态, 审核状态也不更新
            if (SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode().equalsIgnoreCase(dto.getBillStatus())){
                dto.setApproveStatusStr("");
            }
            if (StringUtils.isNotBlank(dto.getApproveStatusStr())) {
                ApproveStatusEnum approveStatusEnum = ApproveStatusEnum.getByStatus(dto.getApproveStatusStr());
                if (null == approveStatusEnum) {
                    String msg = StrUtil.format("[{}]审核状态类型存在:{}", dto.getUniqueId(), dto.getApproveStatusStr());
                    throw new ServiceException(msg);
                }
                oldEntity.setApproveStatus(approveStatusEnum);
            }
            //已发货
            //平台订单状态
            String platformOrderStatus = dto.getPlatformOrderStatus();
            // 亚马逊作废保留以前状态
            if (PlatformDictEnum.AMAZON.getCode().equalsIgnoreCase(dto.getDictPlatform()) && dto.getInvalidStatus()) {
                oldEntity.setApproveStatus(oldApproveStatus);
                dto.setPayStatus(oldEntity.getPayStatus());
                dto.setPayTime(oldEntity.getPayTime());
                dto.setBillStatus(oldEntity.getBillStatus());
            }
            // Shopify作废保留以前状态
            if (PlatformDictEnum.SHOPIFY.getCode().equalsIgnoreCase(dto.getDictPlatform()) &&
                    ("voided".equalsIgnoreCase(dto.getPlatformOrderStatus())) || ("partially_refunded".equalsIgnoreCase(dto.getPlatformOrderStatus()))) {
                oldEntity.setApproveStatus(oldApproveStatus);
                dto.setPayStatus(oldEntity.getPayStatus());
                dto.setPayTime(oldEntity.getPayTime());
                dto.setBillStatus(oldEntity.getBillStatus());
            }
            //速卖通
            if (PlatformDictEnum.ALI_EXPRESS.getCode().equalsIgnoreCase(dto.getDictPlatform())) {
                if ("IN_CANCEL".equals(platformOrderStatus)
                        || "IN_FROZEN".equals(platformOrderStatus)
                        || "RISK_CONTROL".equals(platformOrderStatus)) {
                    oldEntity.setApproveStatus(oldApproveStatus);
                    dto.setPayStatus(oldEntity.getPayStatus());
                    dto.setBillStatus(SoB2cBillStatusEnum.ENUM_FROZEN.getCode());
                }
                if ("FINISH".equals(platformOrderStatus)) {
                    oldEntity.setApproveStatus(oldApproveStatus);
                    dto.setPayStatus(oldEntity.getPayStatus());
                    dto.setBillStatus(oldEntity.getBillStatus());
                }
            }
            //沃尔玛
            if (PlatformDictEnum.WALMART.getCode().equalsIgnoreCase(dto.getDictPlatform())) {
                if ("Cancelled".equals(platformOrderStatus)
                        || "Refund".equals(platformOrderStatus)
                ) {
                    oldEntity.setApproveStatus(oldApproveStatus);
                    dto.setPayStatus(oldEntity.getPayStatus());
                    dto.setBillStatus(oldEntity.getBillStatus());
                }
            }
            //TikTok
            if (PlatformDictEnum.TIK_TOK.getCode().equalsIgnoreCase(dto.getDictPlatform())) {
                if ("AWAITING_COLLECTION".equalsIgnoreCase(platformOrderStatus)
                        || "PARTIALLY_SHIPPING".equalsIgnoreCase(platformOrderStatus)
                        || "IN_TRANSIT".equalsIgnoreCase(platformOrderStatus)
                        || "DELIVERED".equalsIgnoreCase(platformOrderStatus)
                        || "COMPLETED".equalsIgnoreCase(platformOrderStatus)
                ) {
                    oldEntity.setApproveStatus(oldApproveStatus);
                    dto.setPayStatus(oldEntity.getPayStatus());
                    dto.setBillStatus(oldEntity.getBillStatus());
                    dto.setInvalidStatus(oldEntity.getInvalidStatus());
                    if ("平台作废".equals(oldEntity.getRemark())) {
                        oldEntity.setRemark("");
                    }
                }
            }
            //TikTok
            if (PlatformDictEnum.TIK_TOK.getCode().equalsIgnoreCase(dto.getDictPlatform())) {
                if ("CANCELLED".equalsIgnoreCase(platformOrderStatus)) {
                    oldEntity.setApproveStatus(oldApproveStatus);
                    dto.setPayStatus(oldEntity.getPayStatus());
                    dto.setBillStatus(oldEntity.getBillStatus());
                    dto.setInvalidStatus(oldEntity.getInvalidStatus());
                    dto.setIsCancel(Boolean.TRUE);
                }
            }

            // 保留历史作废状态
            if (oldEntity.getInvalidStatus()){
                dto.setInvalidStatus(true);
                dto.setInvalidRemark("平台取消或退款");
            }

            // 只替换更新信息
            SoMultiChannelEntity entity = SoMultiChannelConsumerConverter.INSTANCE.convertUpdateMainOrder(oldEntity, dto);
            if(com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(dto.getSellerOrderCode())){
                entity.setSellerOrderCode(dto.getSellerOrderCode());
            }
            if (!oldEntity.toString().equals(entity.toString())) {
                if (!this.updateById(entity)) {
                    throw new ServiceException("多渠道订单更新失败");
                }
            }

            return entity;
        }
    }

    @Override
    public SoMultiChannelEntity getByPlatformInfo(String platformCode, String dictPlatform, String shopId, String sourceType) {
        return lambdaQuery()
                .eq(SoMultiChannelEntity::getPlatformCode, platformCode)
                .eq(SoMultiChannelEntity::getDictPlatform, dictPlatform)
                .eq(SoMultiChannelEntity::getShopId, shopId)
                .eq(SoMultiChannelEntity::getSourceType, sourceType)
                .last("LIMIT 1")
                .one();
    }

    /**
     * 新增修改处理数据
     */
    public void handleData(SoMultiChannelEntity entity, Boolean exchangeRateThrow, Boolean checkPayTime) {
        if (ObjectUtils.isEmpty(entity)) {
            return;
        }
        entity.setBillDate(ObjectUtils.isEmpty(entity.getBillDate()) ? LocalDate.now() : entity.getBillDate());
        entity.setCreateTime(ObjectUtils.isEmpty(entity.getCreateTime()) ? LocalDateTime.now() : entity.getCreateTime());
        if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(entity.getCurrency())) {
            BigDecimal exchangeRate = dmpTaskFeign.getRate(entity.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), entity.getCurrency());
            if (MathUtil.compareTo(exchangeRate, MathUtil.ZERO) == MathUtil.ZERO && exchangeRateThrow) {
                throw new ServiceException(ApiError.ERROR_EXCHANGE_RATE_NOT_EXIST, entity.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), entity.getCurrency());
            }
            entity.setExchangeRate(null == exchangeRate ? BigDecimal.ZERO : exchangeRate);
        }

        //店铺
        ShopInfoEntity shopInfoEntity = shopInfoService.getById(entity.getShopId());
        if (ObjectUtils.isEmpty(shopInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_92058);
        }
        entity.setOrgId(shopInfoEntity.getSalesOrgId());
        entity.setOrgName(shopInfoEntity.getSalesOrgName());

        //付款时间不为空则已付款
        if (ObjectUtils.isNotEmpty(entity.getPayTime()) && checkPayTime) {
            entity.setPayStatus(SoB2cPayStatusEnum.ENUM_PAID.getCode());
        }

    }
}
