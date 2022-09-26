package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.BaseIdDTO;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.DeliveryDocsDTO;
import com.erp.model.plm.dto.DocsDTO;
import com.erp.model.plm.dto.TaskDocsCountDTO;
import com.erp.model.plm.dto.setDocsPowerDTO;
import com.erp.model.plm.entity.TaskDeliveryDocsEntity;

import java.util.List;

/**
 * @Classname TaskDocsService
 * @Description TODO
 * @Date 2022-09-22 9:42
 * @Created by yl
 */
public interface TaskDeliveryService extends IService<TaskDeliveryDocsEntity> {
    List<TaskDocsCountDTO> getTaskDocsCount(List<String> taskId);

    void saveDeliveryDocs(String userId,String taskId,String productId, List<DocsDTO> deliveryDocsList);

    PagingVO paging(PagingDTO<BaseSearchDTO> dto);

    void setPower(setDocsPowerDTO dto);

    List<DeliveryDocsDTO> getByTaskId(BaseIdDTO dto);
}
