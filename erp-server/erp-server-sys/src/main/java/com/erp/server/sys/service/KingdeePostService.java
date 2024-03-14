package com.erp.server.sys.service;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.entity.KingdeePostEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.KingdeePostDTO;

import java.util.List;

/**
 * <p>
 * 金蝶岗位表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2024-03-11
 */
public interface KingdeePostService extends SuperService<KingdeePostEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2024-03-11
    * @param dto
    * @return
    */
    Boolean add(KingdeePostDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2024-03-11
    * @param dto
    * @return
    */
    Boolean update(KingdeePostDTO.UpdateDTO dto);


    /**
     * 初始话金蝶岗位信息
     * @description
     * @param
     * @return
     * @date 2024-03-13 9:34
     * @author Lambda
     */
    Boolean init();

    /**
     * 岗位详情
     * @description
     * @param id
     * @return
     * @date 2024-03-13 14:52
     * @author Lambda
     */
    KingdeePostDTO.ViewDTO view(String id);
    
    /**
     * 更改金蝶返回来的信息
     * @description
     * @return
     * @date 2024-03-13 17:22
     * @author Lambda
     */
    Boolean updateSyncKingdeeId(String businessId, String syncKingdeeId, String syncKingdeeCode);

    /**
     * @description
     * @param id
     * @return
     * @date 2024-03-13 18:07
     * @author Lambda
     */
    BatchResultDTO delete(String id);

    /**
     * 分页查询
     * @param dto
     * @return
     */
    PagingVO<KingdeePostDTO.PagingViewDTO> paging(PagingDTO<KingdeePostDTO.PagingParamDTO> dto);

    List<KingdeePostEntity> listByOrgId(String orgId);

    /**
     * 根据部门id 查询数据
     * @description
     * @param deptId
     * @return
     * @date 2024-03-14 15:30
     * @author Lambda
     */
    List<KingdeePostEntity> listByKingdeptId(String deptId);
}
