package com.erp.server.wms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.model.wms.entity.PackingTaskEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.PackingTaskDTO;
import com.erp.model.wms.entity.SoDeliveryNoticeEntity;

import java.util.List;

/**
 * <p>
 * 装箱任务表 服务类
 * </p>
 *
 * @author zdy
 * @since 2024-07-02
 */
public interface PackingTaskService extends SuperService<PackingTaskEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2024-07-02
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(PackingTaskDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2024-07-02
    * @param dto
    * @return
    */
    Boolean update(PackingTaskDTO.UpdateDTO dto);

    /**
     * 发货单转换成装箱任务实体
     * @param soDeliveryNoticeEntity
     * @return
     */
    void addPackingByB2BDelivery(SoDeliveryNoticeEntity soDeliveryNoticeEntity);

    /**
     * 头程发货单转换装箱任务实体
     * @param firstMileDeliveryEntity
     * @return
     */
    void addPackingByFirstMileDelivery(FirstMileDeliveryEntity firstMileDeliveryEntity);

    /**
     * 装箱任务-分页查询
     * @param dto
     * @return
     */
    PagingVO<PackingTaskDTO.PagingViewDTO> paging(PagingDTO<PackingTaskDTO.PagingParamDTO> dto);

    /**
     * 按照分类进行统计
     * @param dto
     * @return
     */
    List<PackingTaskDTO.TabListDTO> tabList(PermissionsDTO dto);
}
