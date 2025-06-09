package com.erp.server.plm.controller.feign;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.dto.BomDTO;
import com.erp.model.plm.dto.ProductBomInfoDTO;
import com.erp.model.plm.dto.BomSkuPageDTO;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.server.plm.service.BomSkuService;
import com.erp.server.plm.service.ProductBomSkuHistoryService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: bom远程调用
 * @date 2023/5/17 9:36
 */
@RestController
@RequestMapping("feign/bom")
public class BomSkuFeignController {

    @Resource
    private BomSkuService bomSkuService;

    @Resource
    private ProductBomSkuHistoryService productBomSkuHistoryService;


    /**
     * @description: 根据父级skuIds查询子集sku
     * @author Will
     * @date: 2023/5/17 10:11
     * @param skuIds
     * @return List<BomChildrenSkuDTO>
     */
    @PostMapping("/listBomChildBySkuIds")
    public List<BomChildrenSkuDTO> listBomChildBySkuIds(@RequestBody List<String> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return new ArrayList<>();
        }
        return bomSkuService.listBomChildBySkuIds(skuIds);
    }

    /**
     * 根据skuid获取bom类型信息
     * @param skuIds
     * @return
     */
    @PostMapping("/listBomBySkuIds")
    public List<BomChildrenSkuDTO> listBomBySkuIds(@RequestBody List<String> skuIds){
        if (CollectionUtils.isEmpty(skuIds)) {
            return new ArrayList<>();
        }
        return bomSkuService.listBomBySkuIds(skuIds);
    }

    /**
     * 根据父级skuIds查询子集sku
     * @Author Luo_WG
     * @Date 2023/9/14 12:13
     * @param skuNos
     * @return java.util.List<com.erp.model.plm.dto.BomChildrenSkuDTO>
     **/
    @PostMapping("/listBomChildBySkuNos")
    public List<BomChildrenSkuDTO> listBomChildBySkuNos(@RequestBody List<String> skuNos) {
        return bomSkuService.listBomChildBySkuNos(skuNos);
    }

    /**
     * @description: 根据父级skuIds查询历史bom子集sku
     * @author Will
     * @date: 2023/8/21 10:38
     * @param skuIds
     * @return List<BomChildrenSkuDTO>
     */
    @PostMapping("/listHistoryBomChildBySkuIds")
    public List<BomChildrenSkuDTO> listHistoryBomChildBySkuIds(@RequestBody List<String> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return new ArrayList<>();
        }
        return productBomSkuHistoryService.listHistoryBomChildBySkuIds(skuIds);
    }
    
    /**
     * @description: 根据父级skuIds查询BOM
     * @author Will
     * @date: 2023/5/31 10:59
     * @param skuIds 
     * @return List<BomInfoEntity>
     */
    @PostMapping("/listBomByParentSkuIds")
    public List<BomInfoEntity> listBomByParentSkuIds(@RequestBody List<String> skuIds) {
        return bomSkuService.listBomByParentSkuIds(skuIds);
    }

    /**
     * @description: 根据父级skuNos查询BOM
     * @author Will
     * @date: 2023/5/31 10:59
     * @param skuNos
     * @return List<BomInfoEntity>
     */
    @PostMapping("/listBomByParentSkuNos")
    public List<BomInfoEntity> listBomByParentSkuNos(@RequestBody List<String> skuNos) {
        return bomSkuService.listBomByParentSkuNos(skuNos);
    }

    /**
     * 查询sku版本信息
     * @Author Luo_WG
     * @Date 2023/11/2 8:57
     * @param skuNos
     * @return java.util.List<com.erp.model.plm.dto.ProductBomInfoDTO.skuBomVersion>
     **/
    @PostMapping("/listBomVersionBySkuNos")
    public List<ProductBomInfoDTO.SkuBomVersion> listBomVersionBySkuNos(@RequestBody List<String> skuNos) {
        return bomSkuService.listBomVersionBySkuNos(skuNos);
    }

    /**
     * @description:
     * @author Will
     * @date: 2023/11/23 18:10
     * @param params
     * @return ListAllSkuDTO
     */
    @PostMapping("/listAllLevelSku")
    public BomSkuPageDTO.ListAllSkuDTO listAllLevelSku(@RequestBody BomSkuPageDTO.AllSkuParamDTO params) {
        return bomSkuService.listAllLevelSku(params);
    }

    /**
     * 查询bom (可以查询全部)
     * @return
     */
    @PostMapping("/listAllBom")
    public List<BomDTO.BomSku> listAllBom(@RequestBody List<String> childSkuIdList){
        return bomSkuService.listAllBom(childSkuIdList);
    }

    /**
     * @description: 根据父级skuNos查询子集sku
     * @author jack
     * @date: 2024-11-08
     * @param skuNos
     * @return List<BomChildrenSkuDTO>
     */
    @PostMapping("/checkExistAndListCombinationSku")
    public List<BomChildrenSkuDTO> checkExistAndListCombinationSku(@RequestBody List<String> skuNos) {
        if (CollectionUtils.isEmpty(skuNos)) {
            return Collections.emptyList();
        }
        return bomSkuService.checkExistAndListCombinationSku(skuNos);
    }
}
