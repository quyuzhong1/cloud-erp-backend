package com.erp.server.sys.service;

import com.common.business.dto.FindUserDTO;
import com.common.business.service.SuperService;
import com.erp.model.sys.dto.KingdeeBusinessOperatorDTO;
import com.erp.model.sys.entity.KingdeeBusinessOperatorEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 金蝶业务员 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-07-07
 */
public interface KingdeeBusinessOperatorService extends SuperService<KingdeeBusinessOperatorEntity> {


    /**
     * 导入数据
     * @author yl
     * @date 2023-07-07 16:49
     * @param excelFile
     * @param response
     * @return java.lang.Boolean
 */
    Boolean importFile(MultipartFile excelFile, HttpServletResponse response);


    /**
     * 获取到对应的业务员
     * @author yl
     * @date 2023-07-08 10:56
     * @param dto
     * @return com.erp.model.sys.entity.KingdeeBusinessOperatorEntity
     */
    KingdeeBusinessOperatorEntity find(KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO dto);


    /**
     * 获取业务员列表
     * @author yl
     * @date 2023-07-08 11:33
     * @param dto
     * @return java.util.List<com.common.business.dto.base.BaseDropDownDTO.CommonDTO>
     */
    List<FindUserDTO> listInfo(KingdeeBusinessOperatorDTO.ListBusinessOperatorDTO dto);
}
