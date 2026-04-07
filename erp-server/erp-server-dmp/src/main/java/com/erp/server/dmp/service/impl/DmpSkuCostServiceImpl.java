package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.service.impl.RedisService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.RedisUtil;
import com.common.core.enums.CurrencyEnum;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.business.constant.RedisCacheConstants;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.entity.DmpSkuCostCustomEntity;
import com.erp.model.dmp.entity.DmpSkuCostEntity;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.dto.BomSkuPageDTO;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.scm.dto.SkuCostDTO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.server.dmp.mapper.DmpSkuCostMapper;
import com.erp.server.dmp.service.DictBasicService;
import com.erp.server.dmp.service.DmpSkuCostCustomService;
import com.erp.server.dmp.service.DmpSkuCostService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * sku bom关系表 服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-06-09
 */
@Slf4j
@Service
public class DmpSkuCostServiceImpl extends SuperServiceImpl<DmpSkuCostMapper, DmpSkuCostEntity> implements DmpSkuCostService {

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Autowired
    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private MQProducerService<DmpSkuCostEntity> mqProducerService;

    @Resource
    private DmpSkuCostCustomService dmpSkuCostCustomService;

    @Resource
    private DictBasicService dictBasicService;

    /**
     * 同步采购单sku成本信息
     * @Author Luo_WG
     * @Date 2023/9/13 18:30
     * @return void
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncPurchaseOrderSkuCost(SkuCostDTO.QueryPurchaseDTO queryPurchaseDTO) {
        List<SkuCostDTO> skuCostList = scmTaskFeign.listPurchaseOrderByPurchaseDate(queryPurchaseDTO);
        if (CollectionUtils.isEmpty(skuCostList)) {
            log.warn("未发现进三个月成本信息，cleanSkuCostBySKuNos >>>>>> localDateList：{}",queryPurchaseDTO.getLocalDateList());
            return;
        }
        List<String> skuNoList = skuCostList.stream().map(SkuCostDTO::getSkuNo).distinct().collect(Collectors.toList());

        //更新本身及上级SKU
        cleanSkuCostBySKuNos (skuNoList, queryPurchaseDTO.getPurchaseOrderIds(), queryPurchaseDTO.getSupplierIds());
    }

    @Override
    public void cleanSkuCostBySKuNos(List<String> skuNoList, List<String> purchaseOrderIds, List<String> supplierIds) {
        if (CollectionUtils.isEmpty(skuNoList)) {
            log.warn("录入编码不能为空，cleanSkuCostBySKuNos >>>>>> skuNoList：{}",skuNoList);
            return;
        }

        //查询sku上级所有父级SKU及子级SKU
        BomSkuPageDTO.AllSkuParamDTO allSkuParamDTO = new BomSkuPageDTO.AllSkuParamDTO();
        allSkuParamDTO.setSkuNoList(skuNoList);
        BomSkuPageDTO.ListAllSkuDTO listAllSkuDTO = plmTaskFeign.listAllLevelSku(allSkuParamDTO);

        List<String> resultSkuNoList = new ArrayList<>();
        //所有父级sku
        List<String> parentSkuNoList = listAllSkuDTO.getChildList().stream().map(BomSkuPageDTO.ListSkuLevelDTO::getParentSkuNo).distinct().collect(Collectors.toList());
        //所有子级sku(不包括为bom的父级sku)
        List<String> childSkuNoList = listAllSkuDTO.getChildList().stream().filter(obj -> !parentSkuNoList.contains(obj.getSkuNo())).map(BomSkuPageDTO.ListSkuLevelDTO::getSkuNo).distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(childSkuNoList)) {
            resultSkuNoList.addAll(childSkuNoList);
        }
        //无bom的sku
        List<String> notBomSkuNoList = skuNoList.stream().filter(obj -> !parentSkuNoList.contains(obj) && !childSkuNoList.contains(obj)).distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(notBomSkuNoList)) {
            resultSkuNoList.addAll(notBomSkuNoList);
        }
        SkuCostDTO.ParamDTO paramDTO = new SkuCostDTO.ParamDTO();
        paramDTO.setSkuNoList(resultSkuNoList);
        paramDTO.setPurchaseOrderIds(purchaseOrderIds);
        paramDTO.setSupplierIds(supplierIds);
        //查询所有子级SKU近三个月成本信息
        List<SkuCostDTO> skuCostList = scmTaskFeign.listPurchaseOrderCost(paramDTO);
        //清除需要计算单过滤后没有成本信息的数据
        clearNoSkuCostData(skuCostList,resultSkuNoList);
        if (CollectionUtils.isEmpty(skuCostList)) {
            log.warn("未发现进三个月成本信息，cleanSkuCostBySKuNos >>>>>> skuNoList：{}",skuNoList);
            return;
        }
        //所有子级SKU成本
        List<DmpSkuCostEntity> childSkuCostList = groupSkuCost(skuCostList);
        //添加父级成本数据
        handleParentCost(childSkuCostList,listAllSkuDTO);

        //新增或修改
        addOrUpdateSkuCost(childSkuCostList);
    }

    private void clearNoSkuCostData(List<SkuCostDTO> skuCostList, List<String> skuNoList) {
        LocalDate localDate = LocalDate.now();
        List<String> costSkuNoList = skuCostList.stream().map(SkuCostDTO::getSkuNo).distinct().collect(Collectors.toList());
        List<String> skuNOList2 = skuNoList.stream().filter(e -> CollUtil.isNotEmpty(costSkuNoList) && !costSkuNoList.contains(e)).distinct().collect(Collectors.toList());
        if (CollUtil.isNotEmpty(skuNOList2)){
            this.lambdaUpdate().set(DmpSkuCostEntity::getIsDeleted, true).eq(DmpSkuCostEntity::getCostDate, localDate).in(DmpSkuCostEntity::getSkuNo, skuNOList2).update();
            skuNOList2.forEach(skuNo ->{
                String existKey = StrUtil.format(RedisCacheConstants.DMP_SKU_COST_CODE, skuNo);
                boolean isHas = redisUtil.hasKey(existKey);
                if (isHas) {
                    //删除缓存
                    redisUtil.keys(existKey).forEach(key -> redisUtil.del(key));
                }
            });
        }
    }


    @Override
    public List<DmpSkuCostEntity> listDmpSkuCostBySkuNo(List<String> skuNoList) {
        if (CollectionUtils.isEmpty(skuNoList)) {
            return Collections.EMPTY_LIST;
        }
        List<DmpSkuCostEntity> list = this.lambdaQuery()
                .in(DmpSkuCostEntity::getSkuNo, skuNoList)
                .list();
        return list;
    }

    @Override
    public List<DmpSkuCostEntity> listBySkuIdList(List<String> skuIdList) {
        if (CollectionUtils.isEmpty(skuIdList)) {
            return Collections.EMPTY_LIST;
        }
        List<DmpSkuCostEntity> list = this.lambdaQuery().in(DmpSkuCostEntity::getSkuId, skuIdList).list();
        return list;
    }

    @Override
    public List<DmpSkuCostEntity> listRedisBySkuNoList (List<String> skuNoList) {
        if (CollectionUtils.isEmpty(skuNoList)) {
            return Collections.emptyList();
        }
        //根据sku编码查询bom数据
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listBomChildBySkuNos(skuNoList);
        //仅销售套装bom查子件
        bomChildrenSkuList = CollUtil.isEmpty(bomChildrenSkuList) ? Collections.emptyList() : bomChildrenSkuList.stream().filter(obj -> CharSequenceUtil.equals(BomTypeEnum.COMBINATION.getType(),obj.getType())).collect(Collectors.toList());

        List<String> allSkuIdList = new ArrayList<>(skuNoList);
        if (CollUtil.isNotEmpty(bomChildrenSkuList)) {
            List<String> childSkuNoList = bomChildrenSkuList.stream().distinct().map(BomChildrenSkuDTO::getSkuNo).collect(Collectors.toList());
            allSkuIdList.addAll(childSkuNoList);
        }
        Map<String, List<BomChildrenSkuDTO>> bomMap = bomChildrenSkuList.stream()
                .collect(Collectors.groupingBy(BomChildrenSkuDTO::getParentSkuNo));

        List<DmpSkuCostEntity> childDmpSkuCostList = getChildDmpSkuCostEntity(allSkuIdList);
        if (CollUtil.isEmpty(childDmpSkuCostList)) {
            return Collections.emptyList();
        }
        Map<String, DmpSkuCostEntity> costMap = childDmpSkuCostList.stream()
                .collect(Collectors.toMap(DmpSkuCostEntity::getSkuNo,Function.identity()));

        List<DmpSkuCostEntity> returnList = new ArrayList<>();
        for (String skuNo : skuNoList) {
            List<BomChildrenSkuDTO> bomChildrenSkuDTOS = bomMap.get(skuNo);
            //非组合品
            if (CollUtil.isEmpty(bomChildrenSkuDTOS)) {
                DmpSkuCostEntity thisCostEntity = costMap.get(skuNo);
                if (ObjectUtil.isNotEmpty(thisCostEntity)) {
                    returnList.add(thisCostEntity);
                }
                continue;
            }
            //组合品
            DmpSkuCostEntity parentCostEntity = new DmpSkuCostEntity();
            BigDecimal totalCost = BigDecimal.ZERO;
            BigDecimal totalNotTaxCost = BigDecimal.ZERO;
            for (BomChildrenSkuDTO childrenSkuDTO : bomChildrenSkuDTOS) {
                DmpSkuCostEntity childCostEntity = costMap.get(childrenSkuDTO.getSkuNo());
                //含税成本
                BigDecimal childCost = ObjectUtil.isEmpty(childCostEntity) ? BigDecimal.ZERO : MathUtil.multiplyWithTwo(childCostEntity.getCostPrice(), childrenSkuDTO.getQuantity());
                totalCost = totalCost.add(childCost);
                //不含税成本
                BigDecimal childNotTaxCost = ObjectUtil.isEmpty(childCostEntity) ? BigDecimal.ZERO : MathUtil.multiplyWithTwo(childCostEntity.getNotTaxCostPrice(), childrenSkuDTO.getQuantity());
                totalNotTaxCost = totalNotTaxCost.add(childNotTaxCost);
            }
            parentCostEntity.setCostPrice(totalCost);
            parentCostEntity.setNotTaxCostPrice(totalNotTaxCost);
            parentCostEntity.setSkuId(bomChildrenSkuDTOS.get(0).getParentSkuId());
            parentCostEntity.setSkuNo(skuNo);
            returnList.add(parentCostEntity);
        }
        return returnList;
    }

    /**
     * 查询子级sku的成本
     * @param skuNoList
     * @return
     */
    private List<DmpSkuCostEntity> getChildDmpSkuCostEntity (List<String> skuNoList) {
        if (CollectionUtils.isEmpty(skuNoList)) {
            return Collections.emptyList();
        }
        //sku编码去重
        List<String> distSkuNoList = skuNoList.stream().distinct().collect(Collectors.toList());

        //返回结果集
        List<DmpSkuCostEntity> resultList = new ArrayList<>();

        //未查到缓存的skuId集合
        List<String> redisSkuNoList = new ArrayList<>();

        for (String skuNo : distSkuNoList) {
            //查询redis中存储的成本信息
            String existKey = StrUtil.format(RedisCacheConstants.DMP_SKU_COST_CODE, skuNo);
            DmpSkuCostEntity dmpSkuCostEntity = (DmpSkuCostEntity) redisUtil.get(existKey);
            if (ObjectUtil.isEmpty(dmpSkuCostEntity)) {
                redisSkuNoList.add(skuNo);
                continue;
            }
            resultList.add(dmpSkuCostEntity);
        }
        //查询数据添加缓存
        if (CollectionUtils.isEmpty(redisSkuNoList)) {
            return resultList;
        }
        List<DmpSkuCostEntity> dmpSkuCostList = this.listDmpSkuCostBySkuNo(redisSkuNoList);
        if (CollectionUtils.isEmpty(dmpSkuCostList)) {
            //ERP未找到成本则查询自定义成本数据
            List<DmpSkuCostCustomEntity> dmpSkuCostCustomList = dmpSkuCostCustomService.listDmpSkuCostCustomBySkuNoList(redisSkuNoList);
            if (CollectionUtils.isEmpty(dmpSkuCostCustomList)) {
                return resultList;
            }
            dmpSkuCostList = BeanMapperUtils.copyList(DmpSkuCostEntity.class,dmpSkuCostCustomList);
        }
        for (DmpSkuCostEntity dmpSkuCostEntity : dmpSkuCostList) {
            //添加缓存
            String existKey = StrUtil.format(RedisCacheConstants.DMP_SKU_COST_CODE, dmpSkuCostEntity.getSkuNo());
            redisUtil.set(existKey, dmpSkuCostEntity, RedisService.ONE_DAY_CACHE_TIME);
            resultList.add(dmpSkuCostEntity);
        }
        return resultList;
    }


    /**
     * 添加redis缓存
     */
    private void setRedisSkuCost (DmpSkuCostEntity dmpSkuCostEntity) {
        String existKey = StrUtil.format(RedisCacheConstants.DMP_SKU_COST_CODE, dmpSkuCostEntity.getSkuNo());
        boolean isHas = redisUtil.hasKey(existKey);
        if (isHas) {
            //删除缓存
            redisUtil.keys(existKey).forEach(key -> redisUtil.del(key));
        }
        //添加缓存
        redisUtil.set(existKey,dmpSkuCostEntity);
    }

    /**
     * @description: 新增或修改SKU成本
     * @author Will
     * @date: 2023/11/23 18:49
     * @param dmpSkuCostEntityList
     */
    private void addOrUpdateSkuCost ( List<DmpSkuCostEntity> dmpSkuCostEntityList) {
        if (CollectionUtils.isEmpty(dmpSkuCostEntityList)) {
            return;
        }
        List<String> skuNoList = dmpSkuCostEntityList.stream().map(DmpSkuCostEntity::getSkuNo).distinct().collect(Collectors.toList());
        List<DmpSkuCostEntity> dmpSkuCostList = this.listDmpSkuCostBySkuNo(skuNoList);
        for (DmpSkuCostEntity dmpSkuCostEntity : dmpSkuCostEntityList) {
            DmpSkuCostEntity entity = dmpSkuCostList.stream().filter(req -> req.getSkuNo().equals(dmpSkuCostEntity.getSkuNo())).limit(1).findFirst().orElse(null);
            //成品信息不存在就新增，存在就修改
            if (ObjectUtil.isEmpty(entity)) {
                this.saveOrUpdate(dmpSkuCostEntity);
            } else {
                dmpSkuCostEntity.setId(entity.getId());
                this.saveOrUpdate(dmpSkuCostEntity);
            }
            //判断是否存在redis缓存，存在则删除后更新，不存在则添加
            setRedisSkuCost(dmpSkuCostEntity);
        }
    }

    /**
     * @description: 添加父级成本数据
     * @author Will
     * @date: 2023/11/30 17:17
     * @param childSkuCostList
     * @param listAllSkuDTO
     */
    private void handleParentCost (List<DmpSkuCostEntity> childSkuCostList,BomSkuPageDTO.ListAllSkuDTO listAllSkuDTO) {
        /**
         * 最底层sku成本信息（bom子件+无bom的sku） childSkuCostList
         * bom父级sku成本信息
         */

        //父级SKU下面所有的子级SKU
        List<BomSkuPageDTO.ListSkuLevelDTO> allList = listAllSkuDTO.getChildList();
        if (CollectionUtils.isEmpty(allList)) {
            log.info("未发现上级bom信息，handleParentCost >>>>>> childSkuCostList：{}",childSkuCostList);
            return;
        }
        Map<String, List<BomSkuPageDTO.ListSkuLevelDTO>> map = allList.stream().collect(Collectors.groupingBy(BomSkuPageDTO.ListSkuLevelDTO::getParentSkuId));
        for (Map.Entry<String, List<BomSkuPageDTO.ListSkuLevelDTO>> entry : map.entrySet()) {
            BomSkuPageDTO.ListSkuLevelDTO parentSkuLevelDTO = entry.getValue().get(0);
            DmpSkuCostEntity dmpSkuCostEntity = new DmpSkuCostEntity();
            dmpSkuCostEntity.setSkuId(parentSkuLevelDTO.getParentSkuId());
            dmpSkuCostEntity.setSkuNo(parentSkuLevelDTO.getParentSkuNo());
            dmpSkuCostEntity.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
            //计算成本信息和未税成本信息
            getParentCost(childSkuCostList,allList,parentSkuLevelDTO.getParentSkuId(),dmpSkuCostEntity);
            JSONObject jsonObject = new JSONObject();
            List<BomSkuPageDTO.ListSkuLevelDTO> childSkuList = allList.stream().filter(obj -> obj.getParentSkuId().equals(parentSkuLevelDTO.getParentSkuId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(childSkuList)) {
                log.info("未发现下级bom信息，handleParentCost >>>>>> parentSkuNo：{}",parentSkuLevelDTO.getParentSkuNo());
                continue;
            }
            //无成本不添加
            if (MathUtil.compareTo(dmpSkuCostEntity.getCostPrice(),MathUtil.ZERO) == MathUtil.ZERO) {
                continue;
            }
            childSkuList.forEach(obj -> jsonObject.set(obj.getSkuNo(),obj.getQuantity()));
            dmpSkuCostEntity.setBomSkuJson(jsonObject);
            childSkuCostList.add(dmpSkuCostEntity);
        }
    }

    /**
     * @description: 迭代查询父级SKU成本
     * @author Will
     * @date: 2023/11/30 17:16
     * @param childSkuCostList
     * @param childList
     * @param parentSkuId
     * @param parentSkuCostEntity
     */
    private void getParentCost (List<DmpSkuCostEntity> childSkuCostList,List<BomSkuPageDTO.ListSkuLevelDTO> childList,String parentSkuId,DmpSkuCostEntity parentSkuCostEntity) {
        List<BomSkuPageDTO.ListSkuLevelDTO> childSkuList = childList.stream().filter(obj -> obj.getParentSkuId().equals(parentSkuId)).collect(Collectors.toList());
        //存在SKU则累加sku信息
        if (CollectionUtils.isNotEmpty(childSkuList)) {
            for (BomSkuPageDTO.ListSkuLevelDTO childSkuLevelDTO :  childSkuList) {
                DmpSkuCostEntity dmpSkuCostEntity = childSkuCostList.stream().filter(obj -> obj.getSkuId().equals(childSkuLevelDTO.getSkuId())).findFirst().orElse(null);
                /**
                 * 当子级SKU的成本不存在时
                 * 1、如果子级SKU仍然存在下级SKU则继续循环
                 * 2、如果子级SKU不存在下级SKU则查询SKU自定义成本表获取成本数据
                 */
                if (ObjectUtil.isEmpty(dmpSkuCostEntity)) {
                    List<BomSkuPageDTO.ListSkuLevelDTO> bomChildSkuList = childList.stream().filter(obj -> obj.getParentSkuId().equals(childSkuLevelDTO.getSkuId())).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(bomChildSkuList)) {
                        getParentCost(childSkuCostList,childList,childSkuLevelDTO.getSkuId(),parentSkuCostEntity);
                    } else {
                        //取自定义成本数据
                        DmpSkuCostCustomEntity costCustomEntity = dmpSkuCostCustomService.getBySkuNo(childSkuLevelDTO.getSkuNo());
                        if (ObjectUtil.isNotEmpty(costCustomEntity)) {
                            parentSkuCostEntity.setCostPrice(MathUtil.add(MathUtil.multiplyWithTwo(costCustomEntity.getCostPrice(),new BigDecimal(childSkuLevelDTO.getQuantity()),4) ,parentSkuCostEntity.getCostPrice()));
                            parentSkuCostEntity.setNotTaxCostPrice(MathUtil.add(MathUtil.multiplyWithTwo(costCustomEntity.getNotTaxCostPrice(),new BigDecimal(childSkuLevelDTO.getQuantity()),4) ,parentSkuCostEntity.getNotTaxCostPrice()));
                        }
                    }
                    continue;
                }
                parentSkuCostEntity.setCostPrice(MathUtil.add(MathUtil.multiplyWithTwo(dmpSkuCostEntity.getCostPrice(),new BigDecimal(childSkuLevelDTO.getQuantity()),4) ,parentSkuCostEntity.getCostPrice()));
                parentSkuCostEntity.setNotTaxCostPrice(MathUtil.add(MathUtil.multiplyWithTwo(dmpSkuCostEntity.getNotTaxCostPrice(),new BigDecimal(childSkuLevelDTO.getQuantity()),4) ,parentSkuCostEntity.getNotTaxCostPrice()));
            }
        }

    }


    /**
     * @description: 分组处理成本信息
     * @author Will
     * @date: 2023/11/28 18:41
     * @param skuCostList
     * @return List<DmpSkuCostEntity>
     */
    private List<DmpSkuCostEntity> groupSkuCost (List<SkuCostDTO> skuCostList) {
        List<DmpSkuCostEntity> resultList = new ArrayList<>();
        Map<String, List<SkuCostDTO>> map = skuCostList.stream().collect(Collectors.groupingBy(obj -> obj.getSkuNo()));

        List<SkuCostDTO.SendWarnMsgDTO> sendList = new ArrayList<>();
        for (Map.Entry<String, List<SkuCostDTO>> entry : map.entrySet()) {
            List<SkuCostDTO> value = entry.getValue();
            SkuCostDTO skuCost = entry.getValue().get(0);
            //成本信息
            DmpSkuCostEntity dmpSkuCostEntity = new DmpSkuCostEntity();
            BeanMapperUtils.copy(skuCost,dmpSkuCostEntity);
            BigDecimal totalCostAmount = BigDecimal.ZERO;
            BigDecimal totalNoTaxCostAmount = BigDecimal.ZERO;
            //条数
            Integer size = MathUtil.ZERO;
            for (SkuCostDTO skuCostDTO : value) {
                //汇率
                BigDecimal exchangeRate = BigDecimal.ONE;
                if (StrUtil.isNotBlank(skuCostDTO.getCurrency()) && !CurrencyEnum.CNY.getCurrencyCode().equals(skuCostDTO.getCurrency())) {
                    exchangeRate = dmpTaskFeign.getRate(skuCostDTO.getCostDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), skuCostDTO.getCurrency());
                }
                if (ObjectUtil.isEmpty(exchangeRate)) {
                    log.info("未找到汇率，>>>>>>>> costDate = {},currency = {}",skuCostDTO.getCostDate(),skuCostDTO.getCurrency());
                    SkuCostDTO.SendWarnMsgDTO sendWarnMsgDTO = new SkuCostDTO.SendWarnMsgDTO();
                    sendWarnMsgDTO.setDate(skuCostDTO.getCostDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                    sendWarnMsgDTO.setCurrency(skuCostDTO.getCurrency());
                    sendList.add(sendWarnMsgDTO);
                    continue;
                }
                //含税成本
                totalCostAmount = MathUtil.add(totalCostAmount,MathUtil.multiplyWithTwo(MathUtil.multiplyWithTwo(skuCostDTO.getCostPrice(), exchangeRate),skuCostDTO.getQty()) );
                //未含税成本
                totalNoTaxCostAmount = MathUtil.add(totalNoTaxCostAmount,MathUtil.multiplyWithTwo(MathUtil.multiplyWithTwo(skuCostDTO.getNotTaxCostPrice(), exchangeRate),skuCostDTO.getQty()));
                size++;
            }
            //总数量
            Integer totalQty = value.stream().map(SkuCostDTO::getQty).reduce(MathUtil.ZERO, Integer::sum);

            dmpSkuCostEntity.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
            dmpSkuCostEntity.setCostPrice(MathUtil.divide(totalCostAmount, new BigDecimal(totalQty),4) );
            dmpSkuCostEntity.setNotTaxCostPrice(MathUtil.divide(totalNoTaxCostAmount, new BigDecimal(totalQty),4));
            //无成本则不保存
            if (MathUtil.compareTo(dmpSkuCostEntity.getCostPrice(),MathUtil.ZERO) == MathUtil.ZERO) {
                continue;
            }
            resultList.add(dmpSkuCostEntity);
        }
        //预警
        if (CollectionUtils.isNotEmpty(sendList)) {
            List<SkuCostDTO.SendWarnMsgDTO> sendWarnMsgList = sendList.stream().distinct().collect(Collectors.toList());
            sendWarnMsgList.forEach(obj -> sendWarnMsg(obj.getDate(),obj.getCurrency()));
        }
        return  resultList;
    }

    /**
     * @description: 预警信息
     * @author Will
     * @date: 2023/11/28 18:48
     * @param date
     * @param currency
     */
    public void sendWarnMsg(String date,String currency) {
        WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
        warnMsgInfo.setBizName("SKU成本信息同步");
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_DMP);
        warnMsgInfo.setTitle("汇率查询失败");
        warnMsgInfo.setTableName("dmp_sku_cost");
        warnMsgInfo.setTableId(currency);
        warnMsgInfo.setKeyInfo(StrUtil.format("时间【{}】币别【{}】未找到汇率信息",date,currency));
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        mqProducerService.sendWarnMsg(warnMsgInfo);
    }

}
