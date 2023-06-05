package com.erp.server.sys.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.FindUserDTO;
import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.ExcelUtil;
import com.erp.model.sys.dto.KingdeePostDTO;
import com.erp.model.sys.dto.excel.UserKingdeePostImportExcelDTO;
import com.erp.model.sys.entity.UserKingdeePostEntity;
import com.erp.server.sys.listener.UserKingdeePostExcelListener;
import com.erp.server.sys.mapper.UserKingdeePostMapper;
import com.erp.server.sys.service.SysUserInfoService;
import com.erp.server.sys.service.UserKingdeePostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-06-02
 */
@Service
@Slf4j
public class UserKingdeePostServiceImpl extends SuperServiceImpl<UserKingdeePostMapper, UserKingdeePostEntity> implements UserKingdeePostService {

    @Resource
    private SysUserInfoService sysUserInfoService;

    @Resource
    private UserKingdeePostService userKingdeePostService;

    /**
     * 导入金蝶对应表
     *
     * @param excelFile
     * @param response
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-06-02 16:03
     */
    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        //用户信息
        List<FindUserDTO> userList = sysUserInfoService.getAllUserList();
        List<UserKingdeePostEntity> userKingdeePostList = userKingdeePostService.list();
        UserKingdeePostExcelListener excelListener = new UserKingdeePostExcelListener(this, userList, userKingdeePostList);
        try {
            EasyExcel.read(excelFile.getInputStream(), UserKingdeePostImportExcelDTO.class, excelListener).sheet(0).doRead();
        } catch (Exception e) {
            log.error("金蝶岗位导入错误！", e);
            return Boolean.FALSE;
        }
        List<UserKingdeePostImportExcelDTO> errorList = excelListener.getErrorList();
        if (errorList.size() > 0) {
            String fileName = "金蝶岗位错误信息";
            ExcelUtil.export(fileName, "userKingdeePostError", errorList, UserKingdeePostImportExcelDTO.class, response);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;

    }


    /**
     * 根据用户id 获取岗位信息
     *
     * @param userId
     * @return
     */
    @Override
    public KingdeePostDTO.UserKingdeePostInfoDTO getUserKingdeePostByUserId(String userId) {
        UserKingdeePostEntity userKingdeePostEntity = this.getByUserId(userId);
        KingdeePostDTO.UserKingdeePostInfoDTO result = new KingdeePostDTO.UserKingdeePostInfoDTO();
        if (userKingdeePostEntity != null) {
            BeanMapper.copy(userKingdeePostEntity, result);
        }
        return result;
    }

    private UserKingdeePostEntity getByUserId(String userId) {
        LambdaQueryWrapper<UserKingdeePostEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserKingdeePostEntity::getUserId, userId);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);
    }
}
