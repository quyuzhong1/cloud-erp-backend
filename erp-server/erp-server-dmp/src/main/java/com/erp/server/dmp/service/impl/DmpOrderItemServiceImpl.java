package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.utils.RedisUtil;
import com.common.core.enums.ApiError;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RedisKeyConstant;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.SplitSkuDTO;
import com.erp.model.dmp.entity.DmpBomEntity;
import com.erp.model.dmp.entity.DmpOrderItemEntity;
import com.erp.model.dmp.entity.DmpSkuCostEntity;
import com.erp.model.dmp.entity.DmpSplitErrorLogEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.dto.NewProductDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.dmp.pull.mapper.DmpOrderItemMapper;
import com.erp.server.dmp.service.DmpBomService;
import com.erp.server.dmp.service.DmpOrderItemService;
import com.erp.server.dmp.service.DmpSkuCostService;
import com.erp.server.dmp.service.DmpSplitErrorLogService;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单商品详细信息
 */
@Service
public class DmpOrderItemServiceImpl extends ServiceImpl<DmpOrderItemMapper, DmpOrderItemEntity>
        implements DmpOrderItemService {

    @Resource
    private MQProducerService mQProducerService;


    @Resource
    private RedisUtil redisUtil;

    @Resource
    private DmpBomService dmpBomService;

    @Resource
    private DmpSplitErrorLogService dmpSplitErrorLogService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private DmpSkuCostService dmpSkuCostService;

    /**
     * 添加订单商品详细信息
     *
     * @param dmpOrderInfoEntity 订单商品信息集合
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     **/
    @Override
    public Boolean add(DmpOrderItemEntity dmpOrderInfoEntity, String platformSign) {
        //拆单
        List<DmpOrderItemEntity> itemEntityList = splitOrderItem(Arrays.asList(dmpOrderInfoEntity), platformSign);
        return this.saveBatch(itemEntityList);
    }

    /**
     * 批量添加订单商品详细信息
     *
     * @param dmpOrderInfoEntityList 订单商品信息集合
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     **/
    @Override
    public Boolean batchAdd(List<DmpOrderItemEntity> dmpOrderInfoEntityList, String platformSign) {
        //拆单
        List<DmpOrderItemEntity> itemEntityList = splitOrderItem(dmpOrderInfoEntityList, platformSign);
        return this.saveBatch(itemEntityList, 500);
    }

    /**
     * 批量修改订单商品详细信息
     *
     * @param dmpOrderInfoEntityList 订单商品信息集合
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     **/
    @Override
    public Boolean batchUpdate(List<DmpOrderItemEntity> dmpOrderInfoEntityList, String platformSign) {
        //拆单
        List<DmpOrderItemEntity> itemEntityList = splitOrderItem(dmpOrderInfoEntityList, platformSign);
        return this.saveOrUpdateBatch(itemEntityList, 500);
    }

    /**
     * 根据erp平台商品id查询订单商品信息
     *
     * @param erpOrderItemId erp平台商品id
     * @return com.erp.model.dmp.entity.DmpOrderItemEntity
     * @Author Luo_WG
     * @Date 2022/11/14 22:11
     **/
    @Override
    public DmpOrderItemEntity getByErpOrderItemId(String erpOrderItemId) {
        LambdaQueryWrapper<DmpOrderItemEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpOrderItemEntity::getErpOrderItemId, erpOrderItemId);
        lambdaQueryWrapper.last("limit 1");
        return this.getOne(lambdaQueryWrapper);
    }

    /**
     * 根据订单表id查询订单商品信息
     *
     * @param orderId 订单表id
     * @return java.util.List<com.erp.model.dmp.entity.DmpOrderItemEntity>
     * @Author Luo_WG
     * @Date 2022/12/14 16:10
     **/
    @Override
    public List<DmpOrderItemEntity> getByOrderId(String orderId) {
        LambdaQueryWrapper<DmpOrderItemEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpOrderItemEntity::getOrderId, orderId);
        return this.list(lambdaQueryWrapper);
    }

    /**
     * 根据erp平台商品id修改订单商品信息
     *
     * @param dmpOrderItemEntity 订单商品信息
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2022/11/14 21:39
     **/
    @Override
    public Boolean updateOrderItemByErpOrderItemId(DmpOrderItemEntity dmpOrderItemEntity) {
        LambdaQueryWrapper<DmpOrderItemEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpOrderItemEntity::getErpOrderItemId, dmpOrderItemEntity.getErpOrderItemId());
        return this.update(dmpOrderItemEntity, lambdaQueryWrapper);
    }

    /**
     * 校验订单商品信息在中台是否存在，存在就修改不存在则新增
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkOrderItem(List<DmpOrderItemEntity> orderItem, LocalDate platformCreateTime, String platformSign) {
        List<DmpOrderItemEntity> insertList = new ArrayList<>();
        List<DmpOrderItemEntity> updateList = new ArrayList<>();
        for (DmpOrderItemEntity orderItemBean : orderItem) {
            if(StrUtil.isBlank(orderItemBean.getSkuNo())){
                continue;
            }
            Object skuListing = redisUtil.hget(RedisKeyConstant.SKU_LISTING_TIME, orderItemBean.getSkuNo());
            if (ObjectUtil.isEmpty(skuListing)) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("skuNo", orderItemBean.getSkuNo());
                resultMap.put("listingTime", platformCreateTime);
                mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC, RocketMqTagEnum.PRODUCT_LISTING_UPDATE_TAG.getName(), resultMap, orderItemBean.getId());
            } else {
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate parse = LocalDate.parse(skuListing.toString(), dateTimeFormatter);
                // 新品标识 1为新品 0 为非新品
                if (platformCreateTime.getYear() == parse.getYear()) {
                    orderItemBean.setNewSign(1);
                } else {
                    orderItemBean.setNewSign(0);
                }
            }
            DmpOrderItemEntity dmpOrderItemEntity = this.getByErpOrderItemId(orderItemBean.getErpOrderItemId());
            if (null != dmpOrderItemEntity) {
                //如果数据有变动需要更新数据库订单商品信息
                if (!dmpOrderItemEntity.toString().equals(orderItemBean.toString())) {
                    orderItemBean.setId(dmpOrderItemEntity.getId());
//                    updateById(orderItemBean);
                    baseMapper.deleteById(dmpOrderItemEntity.getId());
                    updateList.add(orderItemBean);
                }
            } else {
                insertList.add(orderItemBean);
            }
        }
        if (CollectionUtil.isNotEmpty(insertList)) {
            this.batchAdd(insertList, platformSign);
        }
        if(CollectionUtil.isNotEmpty(updateList)){
            this.batchUpdate(updateList, platformSign);
        }
    }

    /**
     * 同步PLM的到货时间更新新老品
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNewSign(Map<String, List<NewProductDTO>> dto) {
        List<NewProductDTO> listingNotNullList = dto.get("listingNotNullList");
        listingNotNullList.forEach(req -> {
            LocalDate date = req.getNewListingTime();
            String year = String.valueOf(date.getYear());
            List<String> ids = baseMapper.getItemIdBySkuAndYear(year, req.getSkuNo());

            List<List<String>> partition = Lists.partition(ids, 200);
            partition.forEach(obj -> {
                LambdaUpdateWrapper<DmpOrderItemEntity> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.set(DmpOrderItemEntity::getNewSign, 1);
                updateWrapper.in(DmpOrderItemEntity::getId, obj);
                this.update(updateWrapper);
            });
        });
/*
        List<NewProductDTO> listingNullList = dto.get("listingNullList");

        List<String> skuNoList = listingNullList.stream().map(NewProductDTO::getSkuNo).collect(Collectors.toList());
        List<Map<String, String>> orderListingTime1 = baseMapper.getOrderListingTime(skuNoList);

        for (Map<String, String> stringStringMap : orderListingTime1) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("skuNo", stringStringMap.get("skuno"));
            resultMap.put("listingTime", stringStringMap.get("listingtime"));
            mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC, RocketMqTagEnum.PRODUCT_LISTING_UPDATE_TAG.getName(), resultMap, UUID.randomUUID().toString());
        }
*/

      /*  String year = "";
        String skuNo = String.valueOf(dto.getSkuNo());
        if (dto.getNewListingTime() != null) {
            LocalDate date = dto.getNewListingTime();
            year = String.valueOf(date.getYear());

        }*//* else if (map.get("pastListingTime") != null) {
            LocalDate date = LocalDate.parse(String.valueOf(map.get("pastListingTime")), fmt);
            year = String.valueOf(date.getYear());
            updateWrapper.set(DmpOrderItemEntity::getNewSign, 2);
        }*//* else {
            Object sku = redisUtil.hget(RedisKeyConstant.SKU_NOT_LISTING_TIME, skuNo);
            if (ObjectUtil.isNotEmpty(sku)) {
                return;
            }
            LocalDateTime orderListingTime = baseMapper.getOrderListingTime(dto.getSkuNo());
            if (orderListingTime == null) {
                redisUtil.hset(RedisKeyConstant.SKU_NOT_LISTING_TIME, skuNo, null, 24 * 3600);
                return;
            }
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("skuNo", dto.getSkuNo());
            resultMap.put("listingTime", orderListingTime.toLocalDate());
            mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC, RocketMqTagEnum.PRODUCT_LISTING_UPDATE_TAG.getName(), resultMap, dto.getId());
        }
        if (StringUtils.isBlank(year)) {
            return;
        }
        List<String> ids = baseMapper.getItemIdBySkuAndYear(year, skuNo);
        List<List<String>> partition = Lists.partition(ids, 200);
        partition.forEach(req -> {
            LambdaUpdateWrapper<DmpOrderItemEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(DmpOrderItemEntity::getNewSign, 1);
            updateWrapper.in(DmpOrderItemEntity::getId, req);
            this.update(updateWrapper);
        });*/
    }

    /**
     * 同步PLM的到货时间更新新老品
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void getProductListing(Map<String, List<NewProductDTO>> dto) {
        List<NewProductDTO> listingNullList = dto.get("listingNullList");
        List<String> skuNoList = listingNullList.stream().map(NewProductDTO::getSkuNo).collect(Collectors.toList());
        List<List<String>> partition = Lists.partition(skuNoList, 50);
        for (List<String> list : partition) {
            List<Map<String, String>> orderListingTime1 = baseMapper.getOrderListingTime(list);
            for (Map<String, String> stringStringMap : orderListingTime1) {
                if (StringUtils.isNotBlank(stringStringMap.get("listingtime"))) {
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("skuNo", stringStringMap.get("skuno"));
                    resultMap.put("listingTime", stringStringMap.get("listingtime"));
                    mQProducerService.asyncClassMsg(RocketMqTopic.SYNC_PLM_PRODUCT_TOPIC, RocketMqTagEnum.PRODUCT_LISTING_UPDATE_TAG.getName(), resultMap, UUID.randomUUID().toString());
                }
            }
        }

    }

    /**
     * 采购订单sku拆分
     * @Author Luo_WG
     * @Date 2023/9/13 14:01
     * @param itemEntityList
     * @return java.util.List<com.erp.model.dmp.entity.DmpOrderItemEntity>
     **/
    @Override
    public List<DmpOrderItemEntity> splitOrderItem(List<DmpOrderItemEntity> itemEntityList, String platformSign) {
        List<DmpOrderItemEntity> itemListAll = new ArrayList<>();
        //1、根据sku查询马帮bom检查是否有bom，有就需要拆单
        List<String> skuList = itemEntityList.stream().map(req -> req.getSkuNo()).distinct().collect(Collectors.toList());
        //根据财务编码查询ERP的bom
        List<BomChildrenSkuDTO> allBomList = plmTaskFeign.listBomChildBySkuNos(new ArrayList<>());
        //根据sku查询加工件
        List<DmpBomEntity> machining = dmpBomService.listFindBomBySkuList(skuList, PlatformEnum.MABANG.getDesc(), "machining");
        //获取财务编码
        List<String> financialCodeList = machining.stream().map(req -> req.getFinancialCode()).distinct().collect(Collectors.toList());
        if (PlatformEnum.MABANG.getDesc().equals(platformSign)) {
            allBomList = plmTaskFeign.listBomChildBySkuNos(financialCodeList);
        }
        //获取所有成本
        List<DmpSkuCostEntity> allSkuCostList = dmpSkuCostService.list();
        //遍历订单详情
        for (DmpOrderItemEntity dmpOrderItemEntity : itemEntityList) {
            //设置通用参数
            SplitSkuDTO splitSkuDTO = new SplitSkuDTO();
            splitSkuDTO.setPlatformSign(platformSign);
            splitSkuDTO.setId(dmpOrderItemEntity.getItemId());
            splitSkuDTO.setCleanCostPrice(dmpOrderItemEntity.getCleanCostPrice());
            splitSkuDTO.setIsSplitSku(dmpOrderItemEntity.getIsSplitSku());
            splitSkuDTO.setOriginalSkuNo(dmpOrderItemEntity.getOriginalSkuNo());
            splitSkuDTO.setSkuNo(dmpOrderItemEntity.getSkuNo());
            splitSkuDTO.setQuantity(dmpOrderItemEntity.getQuantity());
            if (dmpOrderItemEntity.getIsGift() != null) {
                splitSkuDTO.setIsGift(dmpOrderItemEntity.getIsGift());
            } else {
                splitSkuDTO.setIsGift(2);
            }
            splitSkuDTO.setAmountAfter(dmpOrderItemEntity.getAmountAfter());
            //拆单
            List<SplitSkuDTO> splitSkuDTOS = this.splitSku(splitSkuDTO, machining, allBomList, allSkuCostList);
            for (SplitSkuDTO skuDTO : splitSkuDTOS) {
                DmpOrderItemEntity entity = new DmpOrderItemEntity();
                BeanMapper.copy(dmpOrderItemEntity, entity);
                entity.setIsSplitSku(skuDTO.getIsSplitSku());
                entity.setAmountAfter(skuDTO.getAmountAfter());
                entity.setCleanCostPrice(skuDTO.getCleanCostPrice());
                entity.setSkuNo(skuDTO.getSkuNo());
                entity.setOriginalSkuNo(skuDTO.getOriginalSkuNo());
                entity.setIsGift(skuDTO.getIsGift());
                entity.setQuantity(skuDTO.getQuantity());
                itemListAll.add(entity);
            }
        }
        return itemListAll;
    }

    /**
     * 拆分sku
     * @param splitSkuDTO 基础信息
     * @param machining 加工单bom
     * @param allBomList ERP的bom
     * @param allSkuCostList 成本信息
     * @return
     */
    @Override
    public List<SplitSkuDTO> splitSku(SplitSkuDTO splitSkuDTO, List<DmpBomEntity> machining, List<BomChildrenSkuDTO> allBomList, List<DmpSkuCostEntity> allSkuCostList) {
        List<SplitSkuDTO> itemListAll = new ArrayList<>();
        List<BomChildrenSkuDTO> bomList = null;
        DmpBomEntity dmpBomEntity = null;
        String skuNo = splitSkuDTO.getSkuNo();
        skuNo = StrUtil.isNotBlank(skuNo) ? skuNo : "";
        if (PlatformEnum.MABANG.getDesc().equals(splitSkuDTO.getPlatformSign())) {
            String finalMabangSkuNo = skuNo;
            dmpBomEntity = machining.stream().filter(req -> req.getParentSku().equals(finalMabangSkuNo)).limit(1).findFirst().orElse(null);
            //如果财务编码不存在记录错误日志
            if (ObjectUtil.isEmpty(dmpBomEntity)) {
                itemListAll.add(splitSkuDTO);
                DmpSplitErrorLogEntity errorLogEntity = new DmpSplitErrorLogEntity();
                errorLogEntity.setBomId("");
                errorLogEntity.setItemId(splitSkuDTO.getId());
                errorLogEntity.setFinancialCode("");
                errorLogEntity.setSkuNo(skuNo);
                errorLogEntity.setMsg(String.format(ApiError.MABANG_BOM_EXIST.msg, skuNo));
                dmpSplitErrorLogService.save(errorLogEntity);
                return itemListAll;
            }

            //如果财务编码不存在记录错误日志
            if (StringUtils.isBlank(dmpBomEntity.getFinancialCode())) {
                itemListAll.add(splitSkuDTO);
                DmpSplitErrorLogEntity errorLogEntity = new DmpSplitErrorLogEntity();
                errorLogEntity.setBomId(dmpBomEntity.getId());
                errorLogEntity.setItemId(splitSkuDTO.getId());
                errorLogEntity.setFinancialCode(dmpBomEntity.getFinancialCode());
                errorLogEntity.setSkuNo(skuNo);
                errorLogEntity.setMsg(String.format(ApiError.CLEAN_SPLIT_FINANCIAL_EXIST.msg, skuNo));
                dmpSplitErrorLogService.save(errorLogEntity);
                return itemListAll;
            }
            //3、获取到马帮的财务编码，匹配ERP的bom
            DmpBomEntity finalDmpBomEntity = dmpBomEntity;
            bomList = allBomList.stream().filter(req -> req.getParentSkuNo().equals(finalDmpBomEntity.getFinancialCode())).collect(Collectors.toList());
        } else {
            //不是马帮的直接SKU匹配ERP的bom
            String finalOtherSkuNo = skuNo;
            bomList = allBomList.stream().filter(req -> req.getParentSkuNo().equals(finalOtherSkuNo)).collect(Collectors.toList());
        }

        //如果sku能直接匹配成本，那么就不拆单直接返回
        String finalSkuNo = skuNo;
        List<DmpSkuCostEntity> costEntities = allSkuCostList.stream().filter(req -> req.getSkuNo().equals(finalSkuNo)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(costEntities)) {
            itemListAll.add(splitSkuDTO);
            return itemListAll;
        }

        //如果匹配ERP的bom不存在记录错误日志
        if (CollectionUtils.isEmpty(bomList)) {
            itemListAll.add(splitSkuDTO);
            DmpSplitErrorLogEntity errorLogEntity = new DmpSplitErrorLogEntity();
            errorLogEntity.setBomId(ObjectUtil.isNotEmpty(dmpBomEntity) ? dmpBomEntity.getId() : "");
            errorLogEntity.setItemId(splitSkuDTO.getId());
            errorLogEntity.setFinancialCode(ObjectUtil.isNotEmpty(dmpBomEntity) ? dmpBomEntity.getFinancialCode() : "");
            errorLogEntity.setSkuNo(skuNo);
            errorLogEntity.setMsg(String.format(ApiError.ERP_BOM_EXIST.msg, ObjectUtil.isNotEmpty(dmpBomEntity) ? dmpBomEntity.getFinancialCode() : ""));
            dmpSplitErrorLogService.save(errorLogEntity);
            return itemListAll;
        }

        //如果没匹配到需要下查bom
        getSkuCost(bomList, allSkuCostList, splitSkuDTO, itemListAll, allBomList);
        //分摊销售额
        shareCost(itemListAll, splitSkuDTO.getAmountAfter());

        return itemListAll;
    }

    /**
     * 递归获取sku成本
     */
    private void getSkuCost(List<BomChildrenSkuDTO> bomList, List<DmpSkuCostEntity> allSkuCostList, SplitSkuDTO splitSkuDTO, List<SplitSkuDTO> itemList, List<BomChildrenSkuDTO> allBomList) {
        List<String> list = bomList.stream().map(req -> req.getParentSkuNo()).collect(Collectors.toList());
        allBomList = plmTaskFeign.listBomChildBySkuNos(list);
        for (BomChildrenSkuDTO bomChildrenSkuDTO : bomList) {
            //用bom里的sku匹配成本，没匹配到继续下查
            DmpSkuCostEntity entity = allSkuCostList.stream().filter(req -> req.getSkuNo().equals(bomChildrenSkuDTO.getSkuNo())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(entity)) {
                List<BomChildrenSkuDTO> skuList = allBomList.stream().filter(req -> req.getParentSkuNo().equals(bomChildrenSkuDTO.getSkuNo())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(skuList)) {
                    getSkuCost(skuList, allSkuCostList, splitSkuDTO, itemList, allBomList);
                } else {
                    //如果没有成本，也没有bom按赠品处理
                    SplitSkuDTO itemEntity = new SplitSkuDTO();
                    BeanMapper.copy(splitSkuDTO, itemEntity);
                    itemEntity.setSkuNo(bomChildrenSkuDTO.getSkuNo());
                    String skuNo = splitSkuDTO.getSkuNo();
                    skuNo = StrUtil.isNotBlank(skuNo) ? skuNo : "";
                    itemEntity.setOriginalSkuNo(skuNo);
                    itemEntity.setCleanCostPrice(BigDecimal.ZERO);
                    itemEntity.setIsGift(1);
                    itemEntity.setIsSplitSku(1);
                    itemList.add(itemEntity);
                }
            } else {
                SplitSkuDTO itemEntity = new SplitSkuDTO();
                BeanMapper.copy(splitSkuDTO, itemEntity);
                itemEntity.setSkuNo(entity.getSkuNo());
                itemEntity.setOriginalSkuNo(splitSkuDTO.getSkuNo() == null ? "" : splitSkuDTO.getSkuNo());
                itemEntity.setCleanCostPrice(entity.getCostPrice().multiply(MathUtil.valueOf(bomChildrenSkuDTO.getQuantity() + "")).multiply(MathUtil.valueOf(splitSkuDTO.getQuantity() + "")));
                itemEntity.setIsSplitSku(1);
                itemList.add(itemEntity);
            }
        }

    }

    /**
     * 分摊销售额
     * @param itemList
     * @param amountAfter
     */
    private void shareCost(List<SplitSkuDTO> itemList, BigDecimal amountAfter) {
        List<SplitSkuDTO> collect = itemList.stream().filter(req -> !req.getIsGift().equals(1)).collect(Collectors.toList());
        BigDecimal sumCostPrice = BigDecimal.ZERO;
        BigDecimal finalSumCostPrice = BigDecimal.ZERO;
        for (SplitSkuDTO itemEntity : collect) {
            sumCostPrice = sumCostPrice.add(itemEntity.getCleanCostPrice());
        }

        for (int i = 0; i < collect.size(); i++) {
            //最后一个sku计算方式为：总价-前面的所有sku价格汇总得出最后一个sku价格
            if (i == collect.size()-1) {
                for (int i1 = 0; i1 < collect.size() -1; i1++) {
                    finalSumCostPrice = finalSumCostPrice.add(collect.get(i1).getAmountAfter());
                }
                collect.get(i).setAmountAfter(amountAfter.subtract(finalSumCostPrice));
            } else {
                collect.get(i).setAmountAfter(amountAfter.divide(sumCostPrice, 4, BigDecimal.ROUND_DOWN).multiply(collect.get(i).getCleanCostPrice()).setScale(4, BigDecimal.ROUND_DOWN));
            }
        }
    }
}



