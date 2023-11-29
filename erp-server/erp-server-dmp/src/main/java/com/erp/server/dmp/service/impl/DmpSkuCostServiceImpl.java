package com.erp.server.dmp.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.service.impl.RedisService;
import com.common.business.utils.RedisUtil;
import com.common.core.enums.CurrencyEnum;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RedisKeyConstant;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.entity.DmpSkuCostEntity;
import com.erp.model.dmp.mabang.RedisMabngSkuEntity;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.dto.BomSkuPageDTO;
import com.erp.model.scm.dto.SkuCostDTO;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.rpc.dmp.feign.DmpSyncFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.server.dmp.mapper.DmpSkuCostMapper;
import com.erp.server.dmp.service.DmpSkuCostService;
import com.common.business.service.impl.SuperServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private MQProducerService<DmpSkuCostEntity> mqProducerService;


    /**
     * 同步采购单sku成本信息
     * @Author Luo_WG
     * @Date 2023/9/13 18:30
     * @return void
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncPurchaseOrderSkuCost(List<LocalDate> localDateList) {
        List<SkuCostDTO> skuCostList = scmTaskFeign.listPurchaseOrderByPurchaseDate(localDateList);
        if (CollectionUtils.isEmpty(skuCostList)) {
            log.warn("未发现进三个月成本信息，cleanSkuCostBySKuNos >>>>>> localDateList：{}",localDateList);
            return;
        }
        List<DmpSkuCostEntity> dmpSkuCostEntityList = groupSkuCost(skuCostList);
        List<String> skuNoList = dmpSkuCostEntityList.stream().map(req -> req.getSkuNo()).distinct().collect(Collectors.toList());
        //新增或修改
        addOrUpdateSkuCost(dmpSkuCostEntityList,skuNoList);
    }

    @Override
    public void cleanSkuCostBySKuNos(List<String> skuNoList) {
        if (CollectionUtils.isEmpty(skuNoList)) {
            log.warn("录入编码不能为空，cleanSkuCostBySKuNos >>>>>> skuNoList：{}",skuNoList);
            return;
        }
        //查询sku上级所有父级SKU
        BomSkuPageDTO.AllSkuParamDTO allSkuParamDTO = new BomSkuPageDTO.AllSkuParamDTO();
        allSkuParamDTO.setSkuNoList(skuNoList);
        List<BomSkuPageDTO.ListAllSkuDTO> listAllSkuList = plmTaskFeign.listAllParentSku(allSkuParamDTO);
        if (CollectionUtils.isNotEmpty(listAllSkuList)) {
            List<String> parentSkuList = listAllSkuList.stream().map(BomSkuPageDTO.ListAllSkuDTO::getParentSkuNo).collect(Collectors.toList());
            skuNoList.addAll(parentSkuList);
        }
        //近三个月成本信息
        SkuCostDTO.ParamDTO paramDTO = new SkuCostDTO.ParamDTO();
        paramDTO.setSkuNoList(skuNoList);
        List<SkuCostDTO> skuCostList = scmTaskFeign.listPurchaseOrderCost(paramDTO);
        if (CollectionUtils.isEmpty(skuCostList)) {
            log.warn("未发现进三个月成本信息，cleanSkuCostBySKuNos >>>>>> skuNoList：{}",skuNoList);
            return;
        }
        List<DmpSkuCostEntity> dmpSkuCostEntityList = groupSkuCost(skuCostList);
        //新增或修改
        addOrUpdateSkuCost(dmpSkuCostEntityList,skuNoList);
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
            BigDecimal totalCostPrice = BigDecimal.ZERO;
            BigDecimal totalNoTaxCostPrice = BigDecimal.ZERO;
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
                totalCostPrice = MathUtil.add(totalCostPrice,MathUtil.multiply(skuCostDTO.getCostPrice(), exchangeRate));
                //未含税成本
                totalNoTaxCostPrice = MathUtil.add(totalNoTaxCostPrice,MathUtil.multiply(skuCostDTO.getNotTaxCostPrice(), exchangeRate));

                size++;
            }
            dmpSkuCostEntity.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
            dmpSkuCostEntity.setCostPrice(MathUtil.divide(totalCostPrice, BigDecimal.valueOf(size),4) );
            dmpSkuCostEntity.setNotTaxCostPrice(MathUtil.divide(totalNoTaxCostPrice, BigDecimal.valueOf(size),4));
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
            return Collections.EMPTY_LIST;
        }
        //sku编码去重
       List<String> distSkuNoList = skuNoList.stream().distinct().collect(Collectors.toList());

        //返回结果集
        List<DmpSkuCostEntity> resultList = new ArrayList<>();

        //未查到缓存的skuId集合
        List<String> redisSkuNoList = new ArrayList<>();

        for (String skuNo : distSkuNoList) {
            //查询redis中存储的成本信息
            String existKey = StrUtil.format(RedisKeyConstant.DMP_SKU_COST_CODE, skuNo);
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
            return resultList;
        }
        for (DmpSkuCostEntity dmpSkuCostEntity : dmpSkuCostList) {
            //添加缓存
            String existKey = StrUtil.format(RedisKeyConstant.DMP_SKU_COST_CODE, dmpSkuCostEntity.getSkuNo());
            redisUtil.set(existKey, dmpSkuCostEntity, RedisService.ONE_DAY_CACHE_TIME);
            resultList.add(dmpSkuCostEntity);
        }
        return resultList;
    }


    /**
     * 添加redis缓存
     */
    private void setRedisSkuCost (DmpSkuCostEntity dmpSkuCostEntity) {
        String existKey = StrUtil.format(RedisKeyConstant.DMP_SKU_COST_CODE, dmpSkuCostEntity.getSkuNo());
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
     * @param skuNoList
     */
    private void addOrUpdateSkuCost ( List<DmpSkuCostEntity> dmpSkuCostEntityList,List<String> skuNoList) {
        if (CollectionUtils.isEmpty(dmpSkuCostEntityList)) {
            return;
        }
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
