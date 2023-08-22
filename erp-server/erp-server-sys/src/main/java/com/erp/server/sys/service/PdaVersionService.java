package com.erp.server.sys.service;
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
}
