package com.erp.server.oms.service;
import com.erp.model.oms.entity.InvoiceDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.InvoiceDetailDTO;

import java.util.List;

/**
 * <p>
 * 上传记录订单明细 服务类
 * </p>
 *
 * @author zdy
 * @since 2025-03-07
 */
public interface InvoiceDetailService extends SuperService<InvoiceDetailEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2025-03-07
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(InvoiceDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2025-03-07
    * @param dto
    * @return
    */
    Boolean update(InvoiceDetailDTO.UpdateDTO dto);

    /**
     * 批量新增
     * @param id
     * @param detailList
     */
    void batchAdd(String id, List<InvoiceDetailDTO.AddDTO> detailList);

    /**
     * 批量更新
     * @param id
     * @param detailList
     */
    void batchUpdate(String id, List<InvoiceDetailDTO.UpdateDTO> detailList);
    /**
     * 根据主表ids查询
     * @author will
     * @date 2025/4/9 11:23
     * @param mainIdList
     * @return List<InvoiceDetailEntity>
     */
    List<InvoiceDetailEntity> listByMainIdList(List<String> mainIdList);
    /**
     * 根据主表id进行删除
     * @author will
     * @date 2025/4/9 15:44
     * @param mainId
     * @return void
     */
    void removeByMainId(String mainId);
}
