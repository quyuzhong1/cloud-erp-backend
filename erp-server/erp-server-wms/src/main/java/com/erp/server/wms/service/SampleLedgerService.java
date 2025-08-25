package com.erp.server.wms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.entity.SampleLedgerEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SampleLedgerDTO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 样品库存统计 服务类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
public interface SampleLedgerService extends SuperService<SampleLedgerEntity> {

    /**
    * 新增
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SampleLedgerDTO.AddDTO dto);

    /**
    * 修改
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return
    */
    Boolean update(SampleLedgerDTO.UpdateDTO dto);

    /**
     * 根据用户ID查询台账列表
     * @param dto 查询条件对象，包含用户ID、SKU编号等查询参数
     * @return 符合条件的台账实体列表，如果查询条件为空则返回空列表
     */
    List<SampleLedgerDTO.SkuAvailableQtyDTO> listLedgerByUserId(SampleLedgerDTO.SearchDTO dto);

    PagingVO<SampleLedgerDTO.SkuAvailableQtyDTO> listSku(PagingDTO<SampleLedgerDTO.SearchDTO> pagingDTO);
}
