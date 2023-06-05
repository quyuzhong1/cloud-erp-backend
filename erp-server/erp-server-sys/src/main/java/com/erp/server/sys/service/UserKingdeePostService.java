package com.erp.server.sys.service;

import com.erp.model.sys.dto.KingdeePostDTO;
import com.erp.model.sys.entity.UserKingdeePostEntity;
import com.common.business.service.SuperService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-06-02
 */
public interface UserKingdeePostService extends SuperService<UserKingdeePostEntity> {

    
    /**
     * 导入金蝶对应表
     * @author yl
     * @date 2023-06-02 16:03
     * @param excelFile
     * @param response
     * @return java.lang.Boolean
     */
    Boolean importFile(MultipartFile excelFile, HttpServletResponse response);


    /**
     * 根据用户id 获取岗位信息
     * @param userId
     * @return
     */
    KingdeePostDTO.UserKingdeePostInfoDTO getUserKingdeePostByUserId(String userId);
}
