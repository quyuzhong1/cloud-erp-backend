package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.bi.vo.SkuCategoryVO;
import com.erp.model.dmp.entity.DmpSkuInfoEntity;
import com.erp.server.bi.mapper.BiSkuInfoMapper;
import com.erp.server.bi.service.BiSkuInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Classname BiSkuInfoServiceImpl
 * @Description TODO
 * @Date 2022-12-26 16:34
 * @Created by yl
 */
@Service
public class BiSkuInfoServiceImpl extends ServiceImpl<BiSkuInfoMapper, DmpSkuInfoEntity> implements BiSkuInfoService {


    /**
     * 分类 获取sku 分类信息
     *
     * @param
     * @return java.util.List<com.erp.model.bi.vo.SkuCategoryVO>
     * @author yl
     * @date 2022-12-26 16:43
     */
    @Override
    public List<SkuCategoryVO> getSkuCategoryList() {
        List<DmpSkuInfoEntity> list = this.getCategoryList();

        Map<String, List<DmpSkuInfoEntity>> groupMap = list.parallelStream().
                collect(Collectors.groupingBy(DmpSkuInfoEntity::getParentCategoryName));
        List<SkuCategoryVO> resultList = new ArrayList<>(groupMap.size());
        String defaultCategory = "无分类";
        for (Map.Entry<String, List<DmpSkuInfoEntity>> item : groupMap.entrySet()) {
            SkuCategoryVO vo = new SkuCategoryVO();
            List<DmpSkuInfoEntity> skuInfoList = item.getValue();
            String category = item.getKey();
            if (StringUtils.isNotBlank(category)) {
                vo.setName(category);
            } else {
                vo.setName(defaultCategory);
            }
            vo.setSkuList(skuInfoList.stream().map(DmpSkuInfoEntity::getSkuNo).collect(Collectors.toList()));
            resultList.add(vo);
        }

        return resultList;
    }


    /**
     * 品牌分类 获取sku
     * @return
     */
    @Override
    public List<SkuCategoryVO> getSkuBrandList() {
        List<DmpSkuInfoEntity> list = this.getBrandList();
        Map<String, List<DmpSkuInfoEntity>> groupMap = list.parallelStream().
                collect(Collectors.groupingBy(DmpSkuInfoEntity::getParentCategoryName));
        List<SkuCategoryVO> resultList = new ArrayList<>(groupMap.size());
        String defaultCategory = "无品牌";
        for (Map.Entry<String, List<DmpSkuInfoEntity>> item : groupMap.entrySet()) {
            SkuCategoryVO vo = new SkuCategoryVO();
            List<DmpSkuInfoEntity> skuInfoList = item.getValue();
            String brand = item.getKey();
            if (StringUtils.isNotBlank(brand)) {
                vo.setName(brand);
            } else {
                vo.setName(defaultCategory);
            }
            vo.setSkuList(skuInfoList.stream().map(DmpSkuInfoEntity::getSkuNo).collect(Collectors.toList()));
            resultList.add(vo);
        }
        return resultList;
    }

    private List<DmpSkuInfoEntity> getBrandList() {
        LambdaQueryWrapper<DmpSkuInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ne(DmpSkuInfoEntity::getBrandName, "")
                .or().ne(DmpSkuInfoEntity::getBrandName, null);
        return this.list(queryWrapper);
    }

    private List<DmpSkuInfoEntity> getCategoryList() {
        LambdaQueryWrapper<DmpSkuInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ne(DmpSkuInfoEntity::getParentCategoryName, "")
                .or().ne(DmpSkuInfoEntity::getParentCategoryName, null);
        return this.list(queryWrapper);
    }
}
