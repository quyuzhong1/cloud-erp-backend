package com.erp.server.plm.service;

import com.erp.model.plm.dto.ProductMilepostDTO;
import com.erp.model.plm.dto.ProductProgressPhaseDTO;
import com.erp.model.plm.dto.productProgressShowDTO;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/11/23 19:05
 */
public interface ProjectTaskProgressService {

    /**
     * @description: 根据产品id获取里程碑任务
     * @author Will
     * @date: 2022/11/18 15:08
     * @param productId
     * @return List<ProductMilepostDTO>
     */
    List<ProductMilepostDTO> getMilepostTaskListByProductId(String productId);
    /**
     * @description: 查询产品各个阶段任务完成进度
     * @author Will
     * @date: 2022/11/21 9:27
     * @param productId
     * @return productProgressShowDTO
     */
    productProgressShowDTO getFinishProgressList(String productId);
}
