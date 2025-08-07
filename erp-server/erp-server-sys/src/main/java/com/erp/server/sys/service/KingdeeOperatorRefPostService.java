package com.erp.server.sys.service;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.KingdeeBusinessOperatorDTO;
import com.erp.model.sys.dto.UserInfoDTO;
import com.erp.model.sys.entity.KingdeeOperatorRefPostEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.KingdeeOperatorRefPostDTO;

import java.util.List;

/**
 * <p>
 * 金蝶业务员表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2024-03-11
 */
public interface KingdeeOperatorRefPostService extends SuperService<KingdeeOperatorRefPostEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2024-03-11
    * @param userPostId
    * @return
    */
    BatchResultDTO add(String typeCode,String userPostId);




    /**
     * 初始化金蝶数据
     * @description
     * @return
     * @date 2024-03-15 9:32
     * @author Lambda
     */
    Boolean init();

    /**
     *  分页查询
     * @param dto
     * @return
     */
    PagingVO<KingdeeOperatorRefPostDTO.PagingViewDTO> paging(PagingDTO<KingdeeOperatorRefPostDTO.PagingParamDTO> dto);

    BatchResultDTO delete(String id);


    Boolean updateSyncKingdeeId(String businessId, String syncKingdeeId);


    /**
     * 推送金蝶获取数据
     * @param dto
     * @return
     */
    KingdeeOperatorRefPostDTO.OperatorDTO find(KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO dto);

    /**
     * 业务员信息
     * @description
     * @param
     * @return
     * @date 2024-03-18 14:13
     * @author Lambda
     */
    List<UserInfoDTO.BusinessOperationUserDTO> listInfo(KingdeeBusinessOperatorDTO.ListBusinessOperatorDTO dto);

    List<KingdeeOperatorRefPostDTO.OperatorDTO> listOperatorByUserIdList(List<String> userIdList);

    void updateState(KingdeeBusinessOperatorDTO.BatchUpdateDTO dto);
    /**
     * 根据业务员类型和组织id集合查询用户信息
     * @author will
     * @date 2025/8/6 14:26
     * @param dto
     * @return List<BusinessOperationUserDTO>
     */
    List<UserInfoDTO.BusinessOperationUserDTO> listUser(KingdeeBusinessOperatorDTO.ListBusinessOperatorUserDTO dto);
}
