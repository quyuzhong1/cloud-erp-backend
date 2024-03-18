package com.erp.server.sys.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.sys.dto.KingdeePostDTO;
import com.erp.model.sys.dto.KingdeeUserRefPostDTO;
import com.erp.model.sys.entity.KingdeeUserRefPostEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 金蝶员工任岗表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2024-03-11
 */
@Mapper
public interface KingdeeUserRefPostMapper extends BaseMapper<KingdeeUserRefPostEntity> {

    
    /**
     * 分页获取员工
     * @description
     * @param
     * @return
     * @date 2024-03-14 11:02
     * @author Lambda
     */
    IPage<KingdeeUserRefPostDTO.PagingUserViewDTO> paging(Page query, @Param("params") KingdeeUserRefPostDTO.PagingParamDTO paramDTO);

    /**
     * 明细分页
     * @description
     * @param
     * @return
     * @date 2024-03-18 16:35
     * @author Lambda
     */
    IPage<KingdeeUserRefPostDTO.DetailPagingViewDTO> detailPaging(Page query, @Param("params")KingdeeUserRefPostDTO.DetailPagingParamDTO paramDTO);


    /**
     * 获取员工任岗信息
     * @description
     * @param
     * @return
     * @date 2024-03-18 9:35
     * @author Lambda
     */
    KingdeePostDTO.UserKingdeePostInfoDTO getKingdeeUserPost(@Param("userId") String userId, @Param("orgId") String orgId);

}
