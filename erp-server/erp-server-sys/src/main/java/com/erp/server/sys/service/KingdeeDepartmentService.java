package com.erp.server.sys.service;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.entity.KingdeeDepartmentEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.KingdeeDepartmentDTO;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Lambda
 * @since 2024-03-11
 */
public interface KingdeeDepartmentService extends SuperService<KingdeeDepartmentEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2024-03-11
    * @param dto
    * @return
    */
    Boolean add(KingdeeDepartmentDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2024-03-11
    * @param dto
    * @return
    */
    Boolean update(KingdeeDepartmentDTO.UpdateDTO dto);


    /**
     * 初始化金蝶部门
     * @description
     * @param
     * @return
     * @date 2024-03-11 11:48
     * @author Lambda
     */
    Boolean init();

    /**
     * 详情
     * @param id
     * @return
     */
    KingdeeDepartmentDTO.ViewDTO view(String id);

    /**
     * 分页查询
     * @param dto
     * @return
     */
    PagingVO<KingdeeDepartmentDTO.PagingViewDTO> paging(PagingDTO<KingdeeDepartmentDTO.PagingParamDTO> dto);

    /**
     * 更改金蝶信息
     * @param businessId
     * @param syncKingdeeId
     * @param syncKingdeeCode
     */
    Boolean updateSyncKingdeeId(String businessId, String syncKingdeeId, String syncKingdeeCode);

    /**
     * 删除金蝶部门
     * @description
     * @param id
     * @return
     * @date 2024-03-12 15:57
     * @author Lambda
     */
    BatchResultDTO delete(String id);
}
