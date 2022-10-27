package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.BaseIdDTO;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.TaskDeliveryDocsEntity;

import java.util.List;

/**
 * @Classname TaskDocsService
 * @Description TODO
 * @Date 2022-09-22 9:42
 * @Created by yl
 */
public interface TaskDeliveryService extends IService<TaskDeliveryDocsEntity> {
    List<CountDTO> getTaskDocsCount(List<String> taskId);

    void saveDeliveryDocs(String userId,String taskId,String productId, List<DocsDTO> deliveryDocsList);

    PagingVO<List<DeliveryDocsDTO>> paging(PagingDTO<BaseSearchDTO> dto);

    void setPower(SetDocsPowerDTO dto);

    List<DeliveryDocsDTO> getByTaskId(String taskId);

    void saveSysDeliveryDocs(String id, List<DocsDTO> docsList);

    void saveTaskDeliveryDocs(String productId, String taskId, String sysTaskId);

    void removeByTaskId(String taskId);


    List<CountDTO> getTaskDocsCountByProductId();

    List<DocsDTO> getDocsByTaskId(String taskId);


    List<DocsDTO> getSysTaskFinishDocs(String taskId);
}
