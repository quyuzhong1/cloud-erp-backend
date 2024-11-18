package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.inventory.InstockForcastDTO;
import com.erp.model.wms.dto.inventory.InstockForcastDetailDTO;
import com.erp.model.wms.entity.InstockForcastDetailEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.InstockForcastDetailMapper;
import com.erp.server.wms.service.InstockForcastDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 入库预报明细表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-09
 */
@Service
public class InstockForcastDetailServiceImpl extends SuperServiceImpl<InstockForcastDetailMapper, InstockForcastDetailEntity> implements InstockForcastDetailService {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<InstockForcastDetailEntity> add(InstockForcastDTO.AddDTO dto, String id) {
        List<InstockForcastDetailDTO.AddDTO> details = dto.getDetails();
        /*
        Map<String, List<InstockForcastDetailDTO.AddDTO>> map = details.stream().collect(Collectors.groupingBy(InstockForcastDetailDTO.AddDTO::getSkuId));
        for (Map.Entry<String, List<InstockForcastDetailDTO.AddDTO>> entry : map.entrySet()) {
            List<InstockForcastDetailDTO.AddDTO> value = entry.getValue();
            if (value.size() > 1) {
                throw new ServiceException(ApiError.ERROR_1024.code, "sku编码【".concat(value.get(0).getSkuNo()).concat("】不能重复"));
            }
        }
         */
        // 获取产品信息
        List<String> skuIdList = details.stream().map(InstockForcastDetailDTO.AddDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);
        Map<String, List<SkuVO>> skuMap = skuList.stream().collect(Collectors.groupingBy(SkuVO::getSkuId));
        // 需要保存的详情集合
        List<InstockForcastDetailEntity> listDetail = new ArrayList<>();
        for (InstockForcastDetailDTO.AddDTO addDTO : details) {
            InstockForcastDetailEntity instockForcastDetailEntity =  new InstockForcastDetailEntity();
            instockForcastDetailEntity.setInfoId(id);
            instockForcastDetailEntity.setSkuId(addDTO.getSkuId());
            instockForcastDetailEntity.setSkuNo(addDTO.getSkuNo());
            instockForcastDetailEntity.setQty(addDTO.getQty());
            instockForcastDetailEntity.setPurchaseOrderDetailId(addDTO.getPurchaseOrderDetailId());
            if(skuMap.containsKey(instockForcastDetailEntity.getSkuId()) && CollUtil.isNotEmpty(skuMap.get(instockForcastDetailEntity.getSkuId()))) {
                SkuVO skuVO = skuMap.get(instockForcastDetailEntity.getSkuId()).get(0);
                instockForcastDetailEntity.setProductName(skuVO.getSkuName());
            }
            listDetail.add(instockForcastDetailEntity);
        }
        this.saveBatch(listDetail);
        return listDetail;
    }

    @Override
    public InstockForcastDetailEntity find(String mainId, String purchaseOrderDetailId) {
        LambdaQueryWrapper<InstockForcastDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InstockForcastDetailEntity::getInfoId, mainId);
        queryWrapper.eq(InstockForcastDetailEntity::getPurchaseOrderDetailId, purchaseOrderDetailId);
        queryWrapper.last("LIMIT 1");
        return baseMapper.selectOne(queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updateQtyByPoChange(String id, Integer qty) {
        return lambdaUpdate().set(InstockForcastDetailEntity::getQty, qty)
                .in(InstockForcastDetailEntity::getId, id)
                .update();
    }

}
