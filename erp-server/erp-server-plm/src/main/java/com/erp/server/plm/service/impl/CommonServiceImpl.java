package com.erp.server.plm.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.dto.FindUserDTO;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.ImageUtil;
import com.common.business.validator.ValidList;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FileUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.plm.dto.ZipTaskResultDTO;
import com.erp.model.plm.entity.PlmAttachmentEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.sys.dto.FindUserByThirdDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.plm.service.CommonService;
import com.erp.server.plm.service.PlmAttachmentService;
import com.erp.server.plm.service.ProductDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Administrator
 * @Classname CommonServiceImpl

 * @Date 2022-10-12 15:20
 * @Created by yl
 */
@Slf4j
@Service
public class CommonServiceImpl implements CommonService {

    @Autowired
    private SysUserFeign sysUserFeign;


    @Autowired
    private WorkflowFeign workflowFeign;
    @Resource
    private FileFeign fileFeign;

    @Resource
    private PlmAttachmentService plmAttachmentService;

    @Resource
    private ProductDetailService productDetailService;

    /**
     * 获取用户名
     *
     * @param
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-10-11 17:48
     */
    @Override
    public String getNameByIds(List<String> userIds) {
        if (CollectionUtils.isNotEmpty(userIds)) {
            List<FindUserDTO> userList = sysUserFeign.getUserList();
            List<String> names = new ArrayList<>();
            for (String userId : userIds) {
                FindUserDTO findUser = userList.stream().filter(u -> userId.equals(u.getUserId())).findFirst().orElse(null);
                if (findUser != null) {
                    names.add(findUser.getUserName());
                } else {
                    names.add("");
                }
            }
            return StringUtils.join(names, ",");
        }
        return "";
    }

    @Override
    public String getNameById(String userId) {
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        FindUserDTO user = userList.stream().filter(u -> userId.equals(u.getUserId())).findFirst().orElse(null);
        if (!Objects.isNull(user)) {
            return user.getUserName();
        }
        return "";
    }

    @Override
    public List<FindUserDTO> getAllUser() {
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        return userList;
    }


    /**
     * 根据第三方平台 以及 unionid 获取用户信息
     *
     * @param fsPlatform
     * @param fsUnionId
     * @return java.lang.String
     * @author yl
     * @date 2022-11-14 10:16
     */
    @Override
    public String getUidByUnionId(String fsPlatform, String fsUnionId) {
        FindUserByThirdDTO third = new FindUserByThirdDTO();
        third.setThirdPartyType(fsPlatform);
        third.setThirdPartyUnionId(fsUnionId);
        return sysUserFeign.getUidByUnionId(third);
    }


    @Override
    public List<String> listProcessCurBusinessIds (String businessKey) {
        //获取当前人需要审核的业务ids
        ValidList<ProcessManagementDTO.ApproveActivityDTO> dtoList = new ValidList<>();
        ProcessManagementDTO.ApproveActivityDTO approveActivityDTO = new ProcessManagementDTO.ApproveActivityDTO();
        approveActivityDTO.setCurApproveId(UserContext.getDefaultLoginUser().getUid());
        approveActivityDTO.setBusinessKey(businessKey);
        dtoList.add(approveActivityDTO);
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = workflowFeign.batchCurApproverByApprove(dtoList);
        if (200 != listApiResult.getCode()) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        List<String> businessIds = listApiResult.getData().stream().filter(obj -> StringUtils.isNotBlank(obj.getBusinessId())).map(ProcessManagementDTO.CurApproveInfoDTO::getBusinessId).collect(Collectors.toList());
        return  businessIds;
    }

    @Override
    public List<String> uploadImg(MultipartFile[] multipartFileList) {
        if (ObjectUtil.isNull(multipartFileList)) {
            throw new ServiceException(ApiError.ERROR_95185);
        }
        Long size = 0L;
        //获取压缩图片大小的配置
        CfgSettingEntity cfgSettingEntity = getCfgSettingEntity(SettingEnum.IMG_UPLOAD_SIZE_KEY);
        if (ObjectUtil.isNotEmpty(cfgSettingEntity) && ObjectUtil.isNotNull(cfgSettingEntity.getValue())) {
            size = Long.valueOf(cfgSettingEntity.getValue());
        }
        List<String> list = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFileList) {
            //图片压缩
            MultipartFile newMultipartFile = compressImage(multipartFile, size);
            //上传fastdfs
            String filePath = fileFeign.uploadFile(newMultipartFile);
            list.add(filePath);
        }
        return list;
    }

    /**
     * 图片压缩
     * @author will
     * @date 2024/12/26 19:39
     * @param multipartFile
     * @param size
     * @return MultipartFile
     */
    @Override
    public MultipartFile compressImage(MultipartFile multipartFile, Long size) {
        //压缩大小=0，则不压缩
        if (MathUtil.compareTo(size,MathUtil.ZERO) == MathUtil.ZERO) {
            return multipartFile;
        }
        try {
            multipartFile = ImageUtil.compressImageMultipartFile(multipartFile, size );
        } catch (IOException e) {
            throw new ServiceException("图片压缩失败");
        }
        return multipartFile;
    }

    /**
     * 查询配置
     * @author will
     * @date 2024/12/26 19:36
     * @param settingEnum
     * @return CfgSettingEntity
     */
    private  CfgSettingEntity getCfgSettingEntity(SettingEnum settingEnum) {
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
                .eq(CfgSettingEntity::getKey, settingEnum.getKey())
                .eq(CfgSettingEntity::getType, settingEnum.getType())
                .list();
        return CollUtil.isEmpty(list) ? new CfgSettingEntity() : list.get(0);
    }
}
