package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.dto.BomSkuDTO;
import com.erp.model.plm.entity.BomSkuEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.server.plm.mapper.BomRefSkuMapper;
import com.erp.server.plm.service.BomSkuService;
import com.erp.server.plm.service.ProductDetailService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
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
        if (CollectionUtils.isNotEmpty(bomSkuList)) {
            for (BomSkuDTO item : bomSkuList) {
                getSaveTree("0", saveBatchList, item, bomId);
            }
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
        List<BomSkuDTO> bomSkuList = BeanMapperUtils.copyList(BomSkuDTO.class, bomSkuEntityList);
        List<BomSkuDTO> treeList = bomSkuList.stream().
                filter(b -> "0".equals(b.getParentSkuNo())).
                map(item -> {
                    item.setLevel(1);
                    item.setChildren(getChildren(item, bomSkuList, 1));
                    return item;
                }).collect(Collectors.toList());
        List<String> skuIdList = bomSkuList.stream().map(BomSkuDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuVOList = productDetailService.getSkuBySkuIds(skuIdList);
        for (BomSkuDTO item : bomSkuList) {
            String skuId = item.getSkuId();
            SkuVO sku = skuVOList.stream().filter(s -> s.getSkuId().equals(skuId)).
                    findFirst().orElse(null);
            if(sku!=null){
                item.setProductId(sku.getProductId());
            }
        }
        return treeList;
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
     * 获取子sku
     *
     * @param item
     * @param bomSkuList
     * @return java.util.List<com.erp.model.plm.dto.BomSkuDTO>
     * @author yl
     * @date 2023-01-11 17:11
     */
    private List<BomSkuDTO> getChildren(BomSkuDTO item, List<BomSkuDTO> bomSkuList, Integer level) {
        List<BomSkuDTO> collect = bomSkuList.stream().filter(bom -> item.getSkuNo().equals(bom.getParentSkuNo())).
                map(b -> {
                    b.setLevel(level + 1);
                    b.setChildren(getChildren(b, bomSkuList, level + 1));

                    return b;
                }).collect(Collectors.toList());

        return CollectionUtils.isEmpty(collect) ? null : collect;
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


    /**
     * 获取到保存的数据 树结构
     *
     * @param
     * @param saveBatchList 对应保存的实体
     * @param item          具体的参数
     * @param bomId         bom 表id
     * @return void
     * @author yl
     * @date 2023-01-09 14:30
     */
    private void getSaveTree(String parentSkuNo, List<BomSkuEntity> saveBatchList, BomSkuDTO item, String bomId) {
        BomSkuEntity bomRefSku = new BomSkuEntity();
        bomRefSku.setBomId(bomId);
        bomRefSku.setParentSkuNo(parentSkuNo);
        bomRefSku.setQuantity(item.getQuantity());
        bomRefSku.setSkuNo(item.getSkuNo());
        bomRefSku.setSkuId(item.getSkuId());
        saveBatchList.add(bomRefSku);
        List<BomSkuDTO> childrenList = item.getChildren();
        if (CollectionUtils.isNotEmpty(childrenList)) {
            for (BomSkuDTO childBomSku : childrenList) {
                this.getSaveTree(item.getSkuNo(), saveBatchList, childBomSku, bomId);
            }
        }
    }
}
