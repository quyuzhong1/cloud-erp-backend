package com.erp.server.sys.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.PdaVersionDTO;
import com.erp.model.sys.entity.PdaVersionEntity;
import com.common.business.service.SuperService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-08-14
 */
public interface PdaVersionService extends SuperService<PdaVersionEntity> {

    /**
     * 发版信息列表分页查询
     * @Author Luo_WG
     * @Date 2023/9/11 16:01
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.sys.dto.PdaVersionDTO.PagingDTO>
     **/
    PagingVO<PdaVersionDTO.PagingDTO> paging(PagingDTO<PdaVersionDTO.PagingParamDTO> dto);

    /**
     * 获取pda最新版本
     * @Author Luo_WG
     * @Date 2023/8/14 16:27
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.sys.entity.PdaVersionEntity>
     **/
    PdaVersionEntity getPdaVersion();

    /**
     * 发版
     * @Author Luo_WG
     * @Date 2023/8/22 16:46
     * @param dto
     * @return java.lang.Boolean
     **/
    Boolean release(PdaVersionDTO.AddDTO dto);

    /**
     * 跳过版本
     * @Author Luo_WG
     * @Date 2023/9/12 12:15
     * @param versionId
     * @return java.lang.Boolean
     **/
    Boolean skipVersion(String versionId);

    /**
     * 更新版本信息
     * @Author 
     * @Date 
     * @param dto
     * @return java.lang.Boolean
     **/
    Boolean update(PdaVersionDTO.UpdateDTO dto);

    /**
     * 删除版本
     * @Author 
     * @Date 
     * @param id
     * @return java.lang.Boolean
     **/
    BatchResultDTO delete(String id);

    /**
     * 详情
     * @Author
     * @Date
     * @param id
     * @return java.lang.Boolean
     **/
    PdaVersionDTO.ViewDTO view(String id);
}
