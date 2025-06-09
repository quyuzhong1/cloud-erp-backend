package com.erp.server.plm.service;

import com.erp.model.plm.dto.ProductMilepostShowDTO;
import com.erp.model.plm.dto.ProductProgressShowDTO;

/**
 * @author Will
 * @version 1.0

 * @date 2022/11/23 19:05
 */
public interface ProjectTaskProgressService {

    /**
     * @description: 根据产品id获取里程碑任务
     * @author Will
     * @date: 2022/11/18 15:08
     * @param productId
     * @return ProductMilepostShowDTO
     */
    ProductMilepostShowDTO getMilepostTaskListByProductId(String productId);
    /**
     * @description: 查询产品各个阶段任务完成进度
     * @author Will
     * @date: 2022/11/21 9:27
     * @param productId
     * @return productProgressShowDTO
     */
    ProductProgressShowDTO getFinishProgressList(String productId);
}
