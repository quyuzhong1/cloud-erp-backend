package com.erp.server.sys.service;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.entity.KingdeeUserRefPostEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.KingdeeUserRefPostDTO;

/**
 * <p>
 * 金蝶员工任岗表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2024-03-11
 */
public interface KingdeeUserRefPostService extends SuperService<KingdeeUserRefPostEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2024-03-11
    * @param dto
    * @return
    */
    Boolean add(KingdeeUserRefPostDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2024-03-11
    * @param dto
    * @return
    */
    Boolean update(KingdeeUserRefPostDTO.UpdateDTO dto);


    /**
     * 金蝶初始化数据
     * @description
     * @param
     * @return
     * @date 2024-03-13 19:53
     * @author Lambda
     */
    Boolean init();

    /**
     * 员工任岗 员工分页
     * @description
     * @param
     * @return
     * @date 2024-03-14 10:55
     * @author Lambda
     */
    PagingVO<KingdeeUserRefPostDTO.PagingUserViewDTO> paging(PagingDTO<KingdeeUserRefPostDTO.PagingParamDTO> dto);

    /**
     * 详情
     * @param id
     * @return
     */
    KingdeeUserRefPostDTO.UserPostViewDTO view(String id);

    /**
     * 删除
     * @description
     * @param
     * @return
     * @date 2024-03-14 14:12
     * @author Lambda
     */
    BatchResultDTO delete(String id);

    Boolean updateSyncKingdeeId(String businessId, String syncKingdeeId, String syncKingdeeCode);
}
