package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.PlatformDeliveryDTO;
import com.common.business.dto.PlatformDeliveryDetailDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.ShopSysUserAuthDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.wms.dto.AliexpressDeliveryDTO;
import com.erp.model.wms.dto.AliexpressDeliveryProratedInfoDTO;
import com.erp.model.wms.entity.AliexpressDeliveryEntity;
import com.erp.model.wms.enums.AliexpressOrderDetailStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.ShopSysUserAuthFeign;
import com.erp.server.wms.mapper.AliexpressDeliveryMapper;
import com.erp.server.wms.service.AliexpressDeliveryDetailService;
import com.erp.server.wms.service.AliexpressDeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_ALIEXPRESS_DELIVERY_EXPORT;

/**
 * <p>
 * 速卖通发货单 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-26
 */
@Slf4j
@Service
public class AliexpressDeliveryServiceImpl extends SuperServiceImpl<AliexpressDeliveryMapper, AliexpressDeliveryEntity> implements AliexpressDeliveryService {
    @Resource
    private ShopSysUserAuthFeign shopSysUserAuthFeign;

    @Resource
    private AliexpressDeliveryDetailService detailService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addOrUpdate(AliexpressDeliveryDTO.AddDTO addDTO) {
        AliexpressDeliveryEntity aliexpressDeliveryEntity = new AliexpressDeliveryEntity();
        BeanMapperUtils.copy(addDTO, aliexpressDeliveryEntity);
        //检查记录是否已存在
        List<AliexpressDeliveryEntity> list = this.getBySoId(addDTO.getSoId());
        AliexpressDeliveryEntity entity = CollUtil.isNotEmpty(list) ? list.stream().filter(e -> Objects.equals(e.getPlatformDeliveryCode(),addDTO.getPlatformDeliveryCode()))
                .findFirst().orElse(null) : null;
        if (ObjectUtil.isNotEmpty(entity)) {
            aliexpressDeliveryEntity.setId(entity.getId());
            aliexpressDeliveryEntity.setIsOutstock(entity.getIsOutstock());
        }else if (CollUtil.isNotEmpty(list)){
            //处理历史数据 第三方单号不存在时， 平台单号+物流跟踪号一致的时候
            AliexpressDeliveryEntity entity1 = list.stream().filter(e -> Objects.equals(e.getPlatformCode(), addDTO.getPlatformCode()) && Objects.equals(e.getTrackNo(), addDTO.getTrackNo())).findFirst().orElse(null);
            if (Objects.nonNull(entity1)){
                //历史数据存在的情况下 不新增 不更新速卖通发货单
                return new BaseResultDTO.AddDTO(entity1.getId(),entity1.getPlatformCode());
            }
        }

        // 补充发货价格信息和来源明细ID
        // 查询主单
        List<SoB2cEntity> soB2cEntityList = FeignQuery.create(SoB2cEntity.class)
                .eq(SoB2cEntity::getPlatformCode, addDTO.getPlatformCode())
                .list();
        if (CollectionUtils.isEmpty(soB2cEntityList)) {
            ServiceException.runError("【速卖通发货生成】:未找到B2C销售订单：平台单号【{}】", addDTO.getPlatformCode());
        }
        List<String> soIds = soB2cEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<SoB2cDetailEntity> soB2cDetailEntityList = FeignQuery.create(SoB2cDetailEntity.class)
                .in(SoB2cDetailEntity::getMainId, soIds)
                .list();
        if (CollectionUtils.isEmpty(soB2cDetailEntityList)) {
            ServiceException.runError("【速卖通发货生成】:未找到B2C销售订单明细：平台单号【{}】", addDTO.getPlatformCode());
        }
        // 分摊所有信息
        List<AliexpressDeliveryProratedInfoDTO> proratedInfoList = proratedInfo(addDTO, soB2cDetailEntityList);

        fillData(aliexpressDeliveryEntity, addDTO, proratedInfoList);
        log.info("开始新增速卖通发货单");
        boolean save = super.saveOrUpdate(aliexpressDeliveryEntity);
        if(!save) {
            throw new ServiceException("速卖通发货单保存失败");
        }
        addDTO.getDetailList().forEach(v -> v.setMainId(aliexpressDeliveryEntity.getId()));
        detailService.addOrUpdate(addDTO.getDetailList(), aliexpressDeliveryEntity, proratedInfoList);
        return new BaseResultDTO.AddDTO(aliexpressDeliveryEntity.getId(), aliexpressDeliveryEntity.getPlatformCode());
    }

    /**
     * 填充数据
     */
    private void fillData(AliexpressDeliveryEntity aliexpressDeliveryEntity, AliexpressDeliveryDTO.AddDTO addDTO, List<AliexpressDeliveryProratedInfoDTO> proratedInfoList) {
        if (null == addDTO.getOrderAfterTaxAmount()) {
            ServiceException.runError("速卖通发货单税后金额不能为空");
        }
        AliexpressDeliveryProratedInfoDTO proratedInfoDTO = proratedInfoList.stream().filter(e -> e.getTrackNo().equals(addDTO.getTrackNo())).findFirst().orElse(null);
        if (null == proratedInfoDTO) {
            ServiceException.runError("速卖通发货单分摊信息未找到:平台单号【{}】", addDTO.getPlatformCode());
        }
        aliexpressDeliveryEntity.setAfterTaxAmount(proratedInfoDTO.getDeliveryAfterTaxAmount());
    }



    @Override
    public PagingVO<AliexpressDeliveryDTO.ListDTO> paging(PagingDTO<AliexpressDeliveryDTO.SearchParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<AliexpressDeliveryDTO.ListDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<AliexpressDeliveryDTO.ListDTO> pageData = this.baseMapper.paging(query, dto.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }
        return new PagingVO<>(pageData);
    }

    @Override
    public Boolean exportExcel(AliexpressDeliveryDTO.SearchParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("速卖通发货单", EXPORT_WMS_ALIEXPRESS_DELIVERY_EXPORT.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public List<ShopSysUserAuthDTO.ViewShopDTO> listUserAuthShop() {
        ShopSysUserAuthDTO.UserAuthShopParamDTO dto = new ShopSysUserAuthDTO.UserAuthShopParamDTO();
        dto.setUserId(UserContext.getDefaultLoginUser().getUid());
        dto.setDictPlatform(PlatformDictEnum.ALI_EXPRESS.getCode());
        return shopSysUserAuthFeign.listUserAuthShop(dto);
    }

    @Override
    public PagingVO<AliexpressDeliveryDTO.ListDTO> exportAliexpressDelivery(PagingDTO<AliexpressDeliveryDTO.SearchParamDTO> dto) {
        Page<AliexpressDeliveryDTO.ListDTO> page = baseMapper.listExportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        return new PagingVO<>(page);
    }

    @Override
    public void updateAliexpressOustock(AliexpressDeliveryDTO.StatusDTO statusDTO) {
        if (Objects.nonNull(statusDTO) && CharSequenceUtil.isNotBlank(statusDTO.getPlatformDeliveryCode()) && CharSequenceUtil.isNotBlank(statusDTO.getSoId()) && Objects.nonNull(statusDTO.getIsOutstock())) {
            this.lambdaUpdate().eq(AliexpressDeliveryEntity::getSoId, statusDTO.getSoId()).eq(AliexpressDeliveryEntity::getPlatformDeliveryCode, statusDTO.getPlatformDeliveryCode())
                    .set(AliexpressDeliveryEntity::getIsOutstock, statusDTO.getIsOutstock()).update();
        }
    }

    public List<AliexpressDeliveryEntity> getBySoId(String soId) {
        return lambdaQuery().eq(AliexpressDeliveryEntity::getSoId, soId).list();
    }

    /**
     * 检查匹配订单明细
     */
    private static SoB2cDetailEntity checkAndGetSoB2cDetailEntity(List<SoB2cDetailEntity> soB2cDetailEntityList, String platformSkuId, String platformSpuNo) {
        // 平台skuId 优先
        SoB2cDetailEntity detailEntity = soB2cDetailEntityList.stream()
                .filter(e -> e.getPlatformSkuId().equalsIgnoreCase(platformSkuId))
                .findFirst()
                .orElse(null);
        if (null == detailEntity){
            // 平台产品ID匹配
            detailEntity= soB2cDetailEntityList.stream()
                    .filter(e -> e.getPlatformSpuNo().equalsIgnoreCase(platformSpuNo))
                    .findFirst()
                    .orElse(null);
        }
        if (null == detailEntity){
            throw  new ServiceException("未找对应明细:产品sku 平台skuId={}", platformSkuId);
        }
        return detailEntity;
    }


    /**
     * 根据发货数量和订单明细数量判断单价
     * @return 计算后发货sku单价
     */
    private Pair<BigDecimal, BigDecimal> checkQtyGetPrice(Integer deliveryQty, BigDecimal deliveryPrice, Integer orderDetailQty, BigDecimal orderDetailAmount) {
        if (deliveryQty > orderDetailQty){
            // 速卖通发货单数量大于订单明细数量
            // 速卖通发货单价格 * 订单明细数量 / 速卖通发货单发货数量
            BigDecimal price = deliveryPrice
                    .multiply(BigDecimal.valueOf(orderDetailQty))
                    .divide(BigDecimal.valueOf(deliveryQty), 4, RoundingMode.DOWN);
            return new Pair<>(price, orderDetailAmount);
        } else {
            //  速卖通发货单数量小于等于订单明细数量
            return new Pair<>(deliveryPrice, orderDetailAmount);
        }
    }

    /**
     * 根据来源分摊信息
     * @param addDTO 添加DTO
     * @return LinkedList<AliexpressDeliveryProratedInfoDTO> 链表类型的分摊信息
     */
    public List<AliexpressDeliveryProratedInfoDTO> proratedInfo(AliexpressDeliveryDTO.AddDTO addDTO, List<SoB2cDetailEntity> soB2cDetailEntityList) {
        // 使用stream流铺平所有发货单明细，生成LinkedList<AliexpressDeliveryProratedInfoDTO>铺平已有信息
        LinkedList<AliexpressDeliveryProratedInfoDTO> proratedInfoList = addDTO.getAllSourceDeliveryList().stream()
                .flatMap(delivery -> delivery.getDetailDTOList().stream().map(detail -> {
                    AliexpressDeliveryProratedInfoDTO info = new AliexpressDeliveryProratedInfoDTO();
                    info.setTrackNo(delivery.getTrackNo());
                    info.setDeliveryWarehouseTime(delivery.getDeliveryWarehouseTime());
                    info.setOrderStatus(delivery.getOrderStatus());
                    info.setPlatformCode(delivery.getSourceCode());
                    info.setUniqueId(detail.getUniqueId());
                    info.setPrice(detail.getPrice());
                    info.setPlatformSkuId(detail.getPlatformSkuId());
                    info.setPlatformSpuNo(detail.getPlatformSpuNo());
                    info.setQty(detail.getQty());
                    info.setPayAmount(detail.getPayAmount());
                    info.setPayCurrency(detail.getPayCurrency());
                    info.setOrderDetailPlatformStatus(detail.getOrderDetailPlatformStatus());
                    info.setAfterTaxAmount(addDTO.getOrderAfterTaxAmount());
                    info.setOrderAmount(addDTO.getOrderAmount());

                    // 可补充其他字段
                    return info;
                }))
                .sorted(Comparator.comparing(AliexpressDeliveryProratedInfoDTO::getDeliveryWarehouseTime).thenComparing(AliexpressDeliveryProratedInfoDTO::getPlatformSkuId))
                .collect(Collectors.toCollection(LinkedList::new));

        // 按订单明细分组
        Map<String, List<AliexpressDeliveryProratedInfoDTO>> groupMap = proratedInfoList.stream().collect(Collectors.groupingBy(AliexpressDeliveryProratedInfoDTO::getPlatformSkuId));

        // 分摊商品金额
        for (Map.Entry<String, List<AliexpressDeliveryProratedInfoDTO>> entry : groupMap.entrySet()) {
            List<AliexpressDeliveryProratedInfoDTO> curList = entry.getValue().stream().sorted(Comparator.comparing(AliexpressDeliveryProratedInfoDTO::getDeliveryWarehouseTime)).collect(Collectors.toList());
            // 明细是否全部已发货
            boolean detailFinish = entry.getValue().stream().anyMatch(e -> !AliexpressOrderDetailStatusEnum.SELLER_PART_SEND_GOODS.getCode().equalsIgnoreCase(e.getOrderStatus()));

            // 明细商品 剩余金额
            BigDecimal lastOrderDetailAmount = null;
            for (int i = 0; i < curList.size(); i++) {
                // 匹配
                AliexpressDeliveryProratedInfoDTO proratedInfoDTO = curList.get(i);
                // 补充订单明细记录信息
                SoB2cDetailEntity soB2cDetailEntity = checkAndGetSoB2cDetailEntity(soB2cDetailEntityList, proratedInfoDTO.getPlatformSkuId(), proratedInfoDTO.getPlatformSpuNo());
                proratedInfoDTO.setOrderDetailAmount(soB2cDetailEntity.getAmount());
                proratedInfoDTO.setOrderDetailQty(soB2cDetailEntity.getQty());
                proratedInfoDTO.setPlatformOrderDetailId(soB2cDetailEntity.getSourceDetailId());
                // 首次设置金额
                if (0 == i){
                    lastOrderDetailAmount = soB2cDetailEntity.getAmount();
                }
                if (1 == curList.size() && detailFinish){
                    // 一个订单明细对应一个发货明细
                    Pair<BigDecimal, BigDecimal> decimalPair = checkQtyGetPrice(proratedInfoDTO.getQty(), proratedInfoDTO.getPrice(), proratedInfoDTO.getOrderDetailQty(), proratedInfoDTO.getOrderDetailAmount());
                    proratedInfoDTO.setProratedUnitPrice(decimalPair.getFirst());
                    proratedInfoDTO.setProratedAmount(decimalPair.getSecond());
                    break;
                }
                // 明细完结判断最后一个发货单
                if (i == curList.size() - 1 && detailFinish) {
                    // 最后一个发货明细
                    proratedInfoDTO.setProratedUnitPrice(lastOrderDetailAmount.divide(BigDecimal.valueOf(proratedInfoDTO.getQty()), 4, RoundingMode.DOWN));
                    proratedInfoDTO.setProratedAmount(lastOrderDetailAmount);
                    break;
                }

                BigDecimal proratedAmount = AliexpressDeliveryProratedInfoDTO.calculateDeliveryProratedAmount(proratedInfoDTO.getOrderAmount(), addDTO.getActualAmount(), proratedInfoDTO.getPayAmount());
                proratedInfoDTO.setProratedUnitPrice(proratedAmount.divide(BigDecimal.valueOf(proratedInfoDTO.getQty()), 4, RoundingMode.DOWN));
                proratedInfoDTO.setProratedAmount(proratedAmount);
                // 最后剩余税后金额 = 当前发货单税后金额 - 当前发货单税后金额
                lastOrderDetailAmount = lastOrderDetailAmount.subtract(proratedAmount);
            }

        }
        List<AliexpressDeliveryProratedInfoDTO> resultList = groupMap.values()
                .stream()
                .flatMap(List::stream)
                .sorted(Comparator.comparing(AliexpressDeliveryProratedInfoDTO::getDeliveryWarehouseTime).thenComparing(AliexpressDeliveryProratedInfoDTO::getPlatformSkuId))
                .collect(Collectors.toList());
        // resultList 按照发货时间排序再按平台SKU ID排序
        BigDecimal orderAmount = resultList.get(0).getOrderAmount();
        // 税后支付金额
        BigDecimal afterTaxAmount = resultList.get(0).getAfterTaxAmount();
        // 最后税后支付金额
        BigDecimal lastAfterTaxAmount = resultList.get(0).getAfterTaxAmount();

        // 所有已发货明细实付金额
        BigDecimal allDeliveryPayAmount = addDTO.getAllSourceDeliveryList().stream().map(PlatformDeliveryDTO::getDetailDTOList)
                .flatMap(List::stream)
                .map(PlatformDeliveryDetailDTO::getPayAmount)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
        // 订单整单已发货完毕
        boolean orderFinish = addDTO.getActualAmount().compareTo(allDeliveryPayAmount) <= 0;

        // resultList 按 proratedAmount / orderAmount * afterTaxAmount 设置到prorateAfterTaxAmount
        for (int i = 0; i < resultList.size(); i++) {
            AliexpressDeliveryProratedInfoDTO proratedInfoDTO = resultList.get(i);
            // 明细完结判断最后一个发货单
            if (i == resultList.size() - 1 && orderFinish) {
                // 最后一个发货明细
                proratedInfoDTO.setProratedAfterTaxAmount(lastAfterTaxAmount);
                break;
            }
            // 计算分摊税后金额
            BigDecimal prorateAfterTaxAmount = proratedInfoDTO.getProratedAmount().divide(orderAmount, 4, RoundingMode.DOWN).multiply(afterTaxAmount);
            proratedInfoDTO.setProratedAfterTaxAmount(prorateAfterTaxAmount);
            // 最后剩余税后金额 = 当前发货单税后金额 - 当前发货单税后金额
            lastAfterTaxAmount = lastAfterTaxAmount.subtract(prorateAfterTaxAmount);
        }

        // resultList累加相同tradeNo的ProrateAfterTaxAmount到deliveryAfterTaxAmount
        for (AliexpressDeliveryProratedInfoDTO proratedInfoDTO : resultList) {
            // 累加相同trackNo的ProrateAfterTaxAmount到deliveryAfterTaxAmount
            List<AliexpressDeliveryProratedInfoDTO> sameTrackNoList = resultList.stream()
                    .filter(e -> e.getTrackNo().equals(proratedInfoDTO.getTrackNo()))
                    .collect(Collectors.toList());
            BigDecimal deliveryAfterTaxAmount = sameTrackNoList.stream()
                    .map(AliexpressDeliveryProratedInfoDTO::getProratedAfterTaxAmount)
                    .reduce(BigDecimal::add)
                    .orElse(BigDecimal.ZERO);
            proratedInfoDTO.setDeliveryAfterTaxAmount(deliveryAfterTaxAmount);
        }

        return proratedInfoList;
    }
}
