package com.erp.server.sys.service.impl;

import com.erp.model.sys.entity.UserKingdeePostEntity;
import com.erp.server.sys.mapper.UserKingdeePostMapper;
import com.erp.server.sys.service.UserKingdeePostService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-06-02
 */
@Service
public class UserKingdeePostServiceImpl extends SuperServiceImpl<UserKingdeePostMapper, UserKingdeePostEntity> implements UserKingdeePostService {



    /**
     * 导入金蝶对应表
     * @author yl
     * @date 2023-06-02 16:03
     * @param excelFile
     * @param response
     * @return java.lang.Boolean
     */
    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {

        return null;
    }
}
