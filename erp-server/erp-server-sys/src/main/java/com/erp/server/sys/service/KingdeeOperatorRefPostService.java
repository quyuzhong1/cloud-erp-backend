package com.erp.server.sys.service;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.entity.KingdeeOperatorRefPostEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.KingdeeOperatorRefPostDTO;

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
    * @param dto
    * @return
    */
    Boolean add(KingdeeOperatorRefPostDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2024-03-11
    * @param dto
    * @return
    */
    Boolean update(KingdeeOperatorRefPostDTO.UpdateDTO dto);


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
}
