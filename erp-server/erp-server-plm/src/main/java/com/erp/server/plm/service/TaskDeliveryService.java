package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.TaskDeliveryDocsEntity;
import com.erp.model.plm.entity.TaskDocsNameEntity;

import java.util.List;

/**
 * @Classname TaskDocsService
 * @Description TODO
 * @Date 2022-09-22 9:42
 * @Created by yl
 */
public interface TaskDeliveryService extends IService<TaskDeliveryDocsEntity> {
    List<CountDTO> getTaskDocsCount(List<String> taskId);

    void saveDeliveryDocs(String taskId,String productId, List<DocsDTO> deliveryDocsList);

    PagingVO<List<DeliveryDocsDTO>> paging(PagingDTO<BaseSearchDTO> dto);

    void setPower(SetDocsPowerDTO dto);

    List<DeliveryDocsDTO> getByTaskId(String taskId);

    void saveSysDeliveryDocs(String id, List<DocsDTO> docsList);

    void saveTaskDeliveryDocs(String productId, String taskId, String sysTaskId, List<TaskDocsNameEntity> docsNameList);

    void removeByTaskId(String taskId);


    List<CountDTO> getTaskDocsCountByProductId();

    List<DocsDTO> getDocsByTaskId(String taskId);


    List<DocsDTO> getSysTaskFinishDocs(String taskId);

    List<TaskDeliveryDocsEntity> getByProductId(String productId);

    List<String> getDocsNameByTaskIds(List<String> sysTaskIds);

    /**
     * @description: 根据交付文档分组显示
     * @author Will
     * @date: 2023/2/7 10:19
     * @param id
     * @return List<DeliveryDocsGroupDTO>
     */
    List<DeliveryDocsGroupDTO> listGroupByTaskId(String id);
    /**
     * @description: 查询项目文档
     * @author Will
     * @date: 2023/3/8 10:34
     * @param productId
     * @return List<DeliveryDocsDTO>
     */
    List<DeliveryDocsDTO> listProductDocs(String productId);

    /**
     * 根据任务id 获取对应数据
     * @author yl
     * @date 2023-03-08 10:48
     * @param taskIds
     * @return java.util.List<com.erp.model.plm.entity.TaskDeliveryDocsEntity>
     */
    List<TaskDeliveryDocsEntity> geByTaskIds(List<String> taskIds);
}
