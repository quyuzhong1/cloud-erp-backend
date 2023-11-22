package com.erp.server.dmp.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.common.core.utils.BeanMapper;
import com.erp.model.dmp.entity.DmpSkuCostEntity;
import com.erp.model.scm.dto.SkuCostDTO;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.server.dmp.mapper.DmpSkuCostMapper;
import com.erp.server.dmp.service.DmpSkuCostService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    /**
     * 同步采购单sku成本信息
     * @Author Luo_WG
     * @Date 2023/9/13 18:30
     * @return void
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncPurchaseOrderSkuCost(String flag, List<LocalDate> localDateList) {
        if (flag.equals("now")) {
            localDateList = new ArrayList<>();
            localDateList.add(LocalDate.now());
        }
        List<SkuCostDTO> skuCostDTOS = scmTaskFeign.listPurchaseOrderByPurchaseDate(localDateList);
        List<DmpSkuCostEntity> dmpSkuCostEntities = BeanMapper.copyList(skuCostDTOS, DmpSkuCostEntity.class);
        List<String> skuNoList = dmpSkuCostEntities.stream().map(req -> req.getSkuNo()).distinct().collect(Collectors.toList());
        List<DmpSkuCostEntity> dmpSkuCostList = this.listDmpSkuCostBySkuNo(skuNoList);
        for (DmpSkuCostEntity dmpSkuCostEntity : dmpSkuCostEntities) {
            DmpSkuCostEntity entity = dmpSkuCostList.stream().filter(req -> req.getSkuNo().equals(dmpSkuCostEntity.getSkuNo())).limit(1).findFirst().orElse(null);
            //成品信息不存在就新增，存在就修改
            if (ObjectUtil.isEmpty(entity)) {
                this.saveOrUpdate(dmpSkuCostEntity);
            } else {
                BeanMapper.copy(entity, dmpSkuCostEntity);
                this.saveOrUpdate(dmpSkuCostEntity);
            }
        }
    }

    @Override
    public List<DmpSkuCostEntity> listDmpSkuCostBySkuNo(List<String> skuNoList) {
        List<DmpSkuCostEntity> list = lambdaQuery().in(DmpSkuCostEntity::getSkuNo, skuNoList).list();
        return list;
    }
}
