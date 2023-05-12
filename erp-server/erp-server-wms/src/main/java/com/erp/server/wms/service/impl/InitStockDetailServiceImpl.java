package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.StrUtils;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.inventory.InitStockDetailDTO;
import com.erp.model.wms.entity.InitStockDetailEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.InitStockDetailMapper;
import com.erp.server.wms.service.InitStockDetailService;
import com.erp.server.wms.service.OperateLogService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Classname: InitStockDetailServiceImpl
 * @Description: 期初库存明细服务实现类
 * @CreateTime: 2023-05-11  10:31
 * @Author: zhangchunlin
 */
@Service
public class InitStockDetailServiceImpl extends SuperServiceImpl<InitStockDetailMapper, InitStockDetailEntity> implements InitStockDetailService {

    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Override
    public List<InitStockDetailEntity> findList(String mainId) {
        List<InitStockDetailEntity> members = lambdaQuery().eq(InitStockDetailEntity::getMainId,mainId).list();
        if(CollUtil.isNotEmpty(members)) {
            members.stream().sorted(Comparator.comparing(InitStockDetailEntity::getId)).collect(Collectors.toList());
        }
        return members;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(List<InitStockDetailDTO.AddDTO> details, String mainId) {
        List<InitStockDetailEntity> list = BeanMapperUtils.copyList(InitStockDetailEntity.class, details);
        handleDetails(list, mainId);
        // 批量保存
        this.saveBatch(list);
    }

    @Override
    public InitStockDetailEntity findDetail(String mainId, String skuId) {
        return lambdaQuery()
                .eq(InitStockDetailEntity::getMainId,mainId)
                .eq(InitStockDetailEntity::getSkuId,skuId)
                .one();
    }


    public void handleDetails(List<InitStockDetailEntity> list, String mainId) {
        //添加操作日志
        List<InitStockDetailEntity> addList = list.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(addList)) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("新增了一条SKU【%s】", ModuleTypeEnum.INIT_STOCK.getCode(), addPairList, "编辑操作");
        }
        List<String> skuIds = list.stream().map(InitStockDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuInfos = plmTaskFeign.getSkuInfoByIds(skuIds);
        Map<String,SkuVO> skuMap =  skuInfos.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity()));
        for(int i = 0, length = list.size();i < length;i++) {
            InitStockDetailEntity data = list.get(i);
            if(!skuMap.containsKey(data.getSkuId())) {
                throw new ServiceException(StrUtil.format("第{}行SKU错误", (i + 1)));
            }
            data.setMainId(mainId);
            data.setSkuNo(skuMap.get(data.getSkuId()).getSkuNo());// 填充真实的sku no
            // 修改时添加日志
            if(StrUtils.isNotEmpty(data.getId())) {
                InitStockDetailEntity initStockDetailOld = super.getById(data.getId());
                if (ObjectUtils.isEmpty(initStockDetailOld)) {
                    throw new ServiceException("未找到期初库存明细数据");
                }
                operateLogService.addModuleOperateLogByObj(initStockDetailOld,data, ModuleTypeEnum.INIT_STOCK.getCode(),mainId,"",String.format("【%s】",initStockDetailOld.getSkuNo()));
            }
        }
    }

}