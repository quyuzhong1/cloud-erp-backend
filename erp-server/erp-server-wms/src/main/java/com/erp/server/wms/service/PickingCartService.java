package com.erp.server.wms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.entity.PickingCartEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.PickingCartDTO;

import java.util.List;

/**
 * <p>
 * 拣货车管理 服务类
 * </p>
 *
 * @author will
 * @since 2024-06-20
 */
public interface PickingCartService extends SuperService<PickingCartEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-06-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(PickingCartDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-06-20
    * @param dto
    * @return
    */
    Boolean update(PickingCartDTO.UpdateDTO dto);

    /**
     * 分页查询
     * @author will
     * @date 2024/6/24 9:19
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<PickingCartDTO.ListDTO> paging(PagingDTO<PickingCartDTO.PagingParamDTO> dto);
    /**
     * 拣货车删除
     * @author will
     * @date 2024/6/24 9:21
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO delete(String id);
    /**
     * 查看详情
     * @author will
     * @date 2024/6/24 9:22
     * @param id
     * @return ViewDTO
     */
    PickingCartDTO.ViewDTO view(String id);
    /**
     * 根据类型id查询
     * @author will
     * @date 2024/6/24 10:45
     * @param typeId
     * @return List<PickingCartEntity>
     */
    List<PickingCartEntity> listByTypeId(String typeId);
    /**
     * 更新拣货车状态
     * @author will
     * @date 2024/6/24 10:53
     * @param dto
     * @return Boolean
     */
    Boolean updateStatus(PickingCartDTO.UpdateStatusDTO dto);

    /**
     * 模糊查询拣货车编号
     *
     * @param code
     * @return
     * @date: 2024-07-07
     * @author: tanmujin
     */
    List<PickingCartDTO.ViewDTO> searchByKeyword(String code);
}
