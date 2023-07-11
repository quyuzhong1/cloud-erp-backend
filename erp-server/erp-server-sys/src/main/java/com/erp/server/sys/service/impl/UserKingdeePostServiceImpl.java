package com.erp.server.sys.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.dto.FindUserDTO;
import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
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
import java.util.Collections;
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

    @Override
    public List<KingdeePostDTO.UserKingdeePostInfoDTO> listUserKingdeePostByUserIds(List<String> userIds) {
        List<UserKingdeePostEntity> list = lambdaQuery()
                .in(UserKingdeePostEntity::getUserId, userIds)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        List<KingdeePostDTO.UserKingdeePostInfoDTO> resultList = BeanMapperUtils.copyList(KingdeePostDTO.UserKingdeePostInfoDTO.class, list);
        return resultList;
    }

    @Override
    public List<KingdeePostDTO.UserKingdeePostInfoDTO> listUserKingdeePostByKingdeePostCodes(List<String> codes) {
        List<UserKingdeePostEntity> list = lambdaQuery()
                .in(UserKingdeePostEntity::getKingdeePostCode, codes)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        List<KingdeePostDTO.UserKingdeePostInfoDTO> resultList = BeanMapperUtils.copyList(KingdeePostDTO.UserKingdeePostInfoDTO.class, list);
        return resultList;
    }


    /**
     * 根据组织 和 user id 获取信息
     *
     * @param dto
     * @return com.erp.model.sys.dto.KingdeePostDTO.UserKingdeePostInfoDTO
     * @author yl
     * @date 2023-07-07 3:16
     */
    @Override
    public KingdeePostDTO.UserKingdeePostInfoDTO getUserKingdeePost(KingdeePostDTO.FindUserKingdeePostInfoDTO dto) {
        LambdaQueryWrapper<UserKingdeePostEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserKingdeePostEntity::getUserId, dto.getUserId());
        queryWrapper.eq(UserKingdeePostEntity::getUseOrgCode, dto.getOrgCode());
        queryWrapper.last("LIMIT 1");
        UserKingdeePostEntity entity = this.getOne(queryWrapper);
        KingdeePostDTO.UserKingdeePostInfoDTO result = new KingdeePostDTO.UserKingdeePostInfoDTO();
        BeanMapper.copy(entity, result);
        return result;
    }


    /**
     * 获取到岗位信息
     *
     * @param dto
     * @return com.erp.model.sys.dto.KingdeePostDTO.UserKingdeePostInfoDTO
     * @author yl
     * @date 2023-07-11 12:19
     */
    @Override
    public KingdeePostDTO.UserKingdeePostInfoDTO getUserKingdeePostInfoByPostCode(KingdeePostDTO.FindUserKingdeePostDTO dto) {
        UserKingdeePostEntity postEntity = this.lambdaQuery().eq(UserKingdeePostEntity::getUseOrgCode, dto.getOrgCode()).
                eq(UserKingdeePostEntity::getKingdeePostCode, dto.getKingdeePostCode()).
                last("LIMIT 1").one();
        if (postEntity != null) {
            KingdeePostDTO.UserKingdeePostInfoDTO result = new KingdeePostDTO.UserKingdeePostInfoDTO();
            BeanMapper.copy(postEntity, result);
            return result;
        }
        return null;
    }

    private UserKingdeePostEntity getByUserId(String userId) {
        LambdaQueryWrapper<UserKingdeePostEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserKingdeePostEntity::getUserId, userId);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);
    }
}
