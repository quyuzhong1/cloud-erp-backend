package com.erp.server.plm.controller.feign;

import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.dto.ProductBomHistoryDTO;
import com.erp.server.plm.service.ProductBomHistoryService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * BOM 历史远程调用
 *
 * @author jack
 * @since 2026-06-23
 */
@RestController
@RequestMapping("feign/productBomHistory")
public class ProductBomHistoryFeignController {

    @Resource
    private ProductBomHistoryService productBomHistoryService;

    /**
     * 根据 BOM 历史 id 查询历史父子件。
     */
    @PostMapping("/listBomHistoryByIds")
    public List<BomChildrenSkuDTO> listBomHistoryByIds(@RequestBody ProductBomHistoryDTO.BomHistoryQueryDTO dto) {
        if (Objects.isNull(dto) || CollectionUtils.isEmpty(dto.getBomHistoryIds())) {
            return Collections.emptyList();
        }
        return productBomHistoryService.listBomHistoryByIds(dto.getBomHistoryIds());
    }

    /**
     * 根据组合品父 SKU 查询全部历史父子件。
     */
    @PostMapping("/listBomHistoryByParentSkuIds")
    public List<BomChildrenSkuDTO> listBomHistoryByParentSkuIds(@RequestBody ProductBomHistoryDTO.BomHistoryQueryDTO dto) {
        if (Objects.isNull(dto) || CollectionUtils.isEmpty(dto.getParentSkuIds())) {
            return Collections.emptyList();
        }
        return productBomHistoryService.listBomHistoryByParentSkuIds(dto.getParentSkuIds());
    }
}
