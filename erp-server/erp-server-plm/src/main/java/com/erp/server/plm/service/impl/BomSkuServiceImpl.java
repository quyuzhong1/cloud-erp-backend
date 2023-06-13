package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.dto.BomSkuDTO;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.model.plm.entity.BomSkuEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.server.plm.mapper.BomRefSkuMapper;
import com.erp.server.plm.service.BomSkuService;
import com.erp.server.plm.service.ProductDetailService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * bom 与sku关系表(BomRefSku)表服务实现类
 *
 * @author yl
 * @since 2023-01-09 11:45:28
 */
@Service
public class BomSkuServiceImpl extends ServiceImpl<BomRefSkuMapper, BomSkuEntity> implements BomSkuService {


    @Resource
    private ProductDetailService productDetailService;

    /**
     * 保存bom 与sku 关系
     *
     * @param bomId
     * @param bomSkuList
     * @return void
     * @author yl
     * @date 2023-01-09 14:19
     */
    @Override
    public void saveBomSku(String bomId, List<BomSkuDTO> bomSkuList) {
        List<BomSkuEntity> saveBatchList = new LinkedList<>();
        for (BomSkuDTO item : bomSkuList) {
            List<BomChildrenSkuDTO> childrenList = item.getChildren();
            for (BomChildrenSkuDTO children : childrenList) {
                BomSkuEntity entity = new BomSkuEntity();
                entity.setParentSkuId(item.getSkuId());
                entity.setParentSkuNo(item.getSkuNo());
                entity.setSkuId(children.getSkuId());
                entity.setSkuNo(children.getSkuNo());
                entity.setQuantity(children.getQuantity());
                entity.setBomId(bomId);
                entity.setProductId(children.getProductId());
                saveBatchList.add(entity);
            }
        }
        if (CollectionUtils.isNotEmpty(saveBatchList)) {
            this.saveBatch(saveBatchList);
        }

    }


    /**
     * 根据bom id 查询出bom yu sku信息
     *
     * @param bomId
     * @return java.util.List<com.erp.model.plm.dto.BomSkuDTO>
     * @author yl
     * @date 2023-01-11 16:51
     */
    @Override
    public List<BomSkuDTO> getByBomId(String bomId) {
        List<BomSkuEntity> bomSkuEntityList = this.getBomSkuListByBomId(bomId);
        Map<String, List<BomSkuEntity>> map = bomSkuEntityList.stream().
                collect(Collectors.groupingBy(BomSkuEntity::getParentSkuNo));

        List<BomSkuDTO> resultList = new ArrayList<>(map.size());
        List<String> skuIdList = bomSkuEntityList.stream().map(BomSkuEntity::getParentSkuId).collect(Collectors.toList());
        List<SkuVO> skuVOList = productDetailService.getSkuBySkuIds(skuIdList);

        for (Map.Entry<String, List<BomSkuEntity>> item : map.entrySet()) {
            List<BomSkuEntity> bomSkuList = item.getValue();
            BomSkuEntity skuEntity = bomSkuList.get(0);
            BomSkuDTO bomSku = new BomSkuDTO();
            bomSku.setLevel(1);
            String skuId = skuEntity.getParentSkuId();
            bomSku.setSkuId(skuId);
            bomSku.setSkuNo(skuEntity.getParentSkuNo());
            SkuVO sku = skuVOList.stream().filter(s -> s.getSkuId().equals(skuId)).
                    findFirst().orElse(null);
            if (sku != null) {
                bomSku.setProductId(sku.getProductId());
            }
            List<BomChildrenSkuDTO> children = BeanMapper.copyList(bomSkuList, BomChildrenSkuDTO.class);
            for (BomChildrenSkuDTO skuDTO : children) {
                skuDTO.setLevel(2);
            }
            bomSku.setChildren(children);
            resultList.add(bomSku);
        }
        return resultList;
    }


    /**
     * 更改bom sku信息
     *
     * @param bomId
     * @param bomSkuList
     * @return void
     * @author yl
     * @date 2023-01-11 18:27
     */
    @Override
    @Transactional
    public void updateBomSku(String bomId, List<BomSkuDTO> bomSkuList) {
        //先删除
        deleteBomSku(bomId);
        saveBomSku(bomId, bomSkuList);

    }

    @Override
    public void deleteByBomId(String bomId) {
        LambdaQueryWrapper<BomSkuEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BomSkuEntity::getBomId, bomId);
        this.remove(queryWrapper);
    }

    @Override
    public List<BomSkuEntity> getByParentSkuId(String parentSkuId) {
        List<BomSkuEntity> list = lambdaQuery().eq(BomSkuEntity::getParentSkuId, parentSkuId).list();
        return list;
    }

    @Override
    public List<BomChildrenSkuDTO> listBomChildBySkuIds(List<String> parentSkuIds) {
        return baseMapper.listBomChildBySkuIds(parentSkuIds);
    }

    @Override
    public List<BomInfoEntity> listBomByParentSkuIds(List<String> parentSkuIds) {
        return baseMapper.listBomByParentSkuIds(parentSkuIds);
    }

    /**
     * 根据Bomid 删除 bom sku 信息
     *
     * @param bomId
     * @return void
     * @author yl
     * @date 2023-01-12 17:29
     */
    private void deleteBomSku(String bomId) {
        LambdaQueryWrapper<BomSkuEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BomSkuEntity::getBomId, bomId);
        this.remove(queryWrapper);

    }

    /**
     * 根据bom id  获取列表
     *
     * @param bomId
     * @return java.util.List<com.erp.model.plm.entity.BomSkuEntity>
     * @author yl
     * @date 2023-01-11 17:03
     */
    private List<BomSkuEntity> getBomSkuListByBomId(String bomId) {
        LambdaQueryWrapper<BomSkuEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BomSkuEntity::getBomId, bomId);
        queryWrapper.orderByDesc(BomSkuEntity::getCreateTime);
        return this.list(queryWrapper);
    }


}
