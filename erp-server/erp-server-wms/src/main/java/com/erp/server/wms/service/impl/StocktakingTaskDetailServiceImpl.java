package com.erp.server.wms.service.impl;

import com.common.business.dto.base.BaseIdDTO;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.StocktakingTaskDetailDTO;
import com.erp.model.wms.entity.StocktakingTaskDetailEntity;
import com.erp.server.wms.mapper.StocktakingTaskDetailMapper;
import com.erp.server.wms.pull.service.ProductDetailService;
import com.erp.server.wms.service.StocktakingTaskDetailService;
import com.common.business.service.SuperServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 盘点任务明细表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-07-31
 */
@Service
public class StocktakingTaskDetailServiceImpl extends SuperServiceImpl<StocktakingTaskDetailMapper, StocktakingTaskDetailEntity> implements StocktakingTaskDetailService {

    @Resource
    private ProductDetailService productDetailService;

    @Override
    public Boolean exportExcel(BaseIdDTO dto, HttpServletResponse response) {
        return null;
    }

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        return null;
    }

    /**
     * 更新明细
     *
     * @param dto
     * @return
     * @author yl
     * @date 2023-08-03 17:59
     */
    @Override
    public Boolean updateDetail(StocktakingTaskDetailDTO.UpdateDTO dto) {
        return null;
    }

    /**
     * 根据仓库id 集合 获取到任务明细
     *
     * @param warehouseIdList
     * @return
     */
    @Override
    public List<StocktakingTaskDetailEntity> listByWarehouseIds(List<String> warehouseIdList) {
        if (CollectionUtils.isEmpty(warehouseIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(StocktakingTaskDetailEntity::getWarehouseId, warehouseIdList).list();
    }


    /**
     * 根据主表id 获取到详情
     *
     * @param mainIdList
     * @return java.util.List<com.erp.model.wms.entity.StocktakingTaskDetailEntity>
     * @author yl
     * @date 2023-08-08 12:08
     */
    @Override
    public List<StocktakingTaskDetailEntity> listBaseByMainIds(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(StocktakingTaskDetailEntity::getMainId, mainIdList).list();
    }

    /**
     * 获取到对应的详情
     *
     * @param mainId
     * @return java.util.List<com.erp.model.wms.dto.StocktakingTaskDetailDTO.ViewDTO>
     * @author yl
     * @date 2023-08-08 16:48
     */
    @Override
    public List<StocktakingTaskDetailDTO.ViewDTO> listByMainId(String mainId) {
        List<StocktakingTaskDetailEntity> dbList = this.listBaseByMainIds(Arrays.asList(mainId));
        List<StocktakingTaskDetailDTO.ViewDTO> resultList = BeanMapper.copyList(dbList, StocktakingTaskDetailDTO.ViewDTO.class);
        List<String> skuIdList = resultList.stream().map(StocktakingTaskDetailDTO.ViewDTO::getSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> skuList = productDetailService.ListProductDetailByIds(skuIdList);
        for (StocktakingTaskDetailDTO.ViewDTO item : resultList) {
            String skuId = item.getSkuId();
            String skuName = skuList.stream().filter(s -> s.getId().equals(skuId)).findFirst().
                    map(ProductDetailEntity::getName).orElse("");
            item.setSkuName(skuName);
        }
        return resultList;
    }
}
