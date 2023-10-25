package com.erp.server.sys.service.impl;

import com.alibaba.excel.EasyExcel;
import com.common.business.dto.FindUserDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.sys.dto.KingdeeBusinessOperatorDTO;
import com.erp.model.sys.dto.UserInfoDTO;
import com.erp.model.sys.dto.excel.KingdeeBusinessOperatorImportExcelDTO;
import com.erp.model.sys.entity.KingdeeBusinessOperatorEntity;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.server.sys.listener.KingdeeBusinessOperatorExcelListener;
import com.erp.server.sys.mapper.KingdeeBusinessOperatorMapper;
import com.erp.server.sys.service.CommonService;
import com.erp.server.sys.service.KingdeeBusinessOperatorService;
import com.erp.server.sys.service.SysAccountingCompanyService;
import com.erp.server.sys.service.SysUserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 金蝶业务员 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-07-07
 */
@Service
@Slf4j
public class KingdeeBusinessOperatorServiceImpl extends SuperServiceImpl<KingdeeBusinessOperatorMapper, KingdeeBusinessOperatorEntity> implements KingdeeBusinessOperatorService {

    @Resource
    private SysUserInfoService sysUserInfoService;


    @Resource
    private CommonService commonService;

    @Resource
    private SysAccountingCompanyService sysAccountingCompanyService;

    /**
     * 导入数据
     *
     * @param excelFile
     * @param response
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-07-07 16:49
     */
    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        List<KingdeeBusinessOperatorEntity> kingdeeBusinessOperatorList = this.list();
        //用户信息
        List<FindUserDTO> userList = sysUserInfoService.getAllUserList();
        KingdeeBusinessOperatorExcelListener excelListener = new KingdeeBusinessOperatorExcelListener(this, userList, kingdeeBusinessOperatorList);
        try {
            EasyExcel.read(excelFile.getInputStream(), KingdeeBusinessOperatorImportExcelDTO.class, excelListener).sheet(0).doRead();
        } catch (Exception e) {
            log.error("金蝶业务员信息导入错误！>>>>>{}", e);
            return Boolean.FALSE;
        }
        List<KingdeeBusinessOperatorImportExcelDTO> errorList = excelListener.getErrorList();
        if (errorList.size() > 0) {
            String fileName = "金蝶业务员错误信息";
            ExcelUtil.export(fileName, "kingdeeBusinessOperatorError", errorList, KingdeeBusinessOperatorImportExcelDTO.class, response);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * 获取到对应的业务员
     *
     * @param dto
     * @return com.erp.model.sys.entity.KingdeeBusinessOperatorEntity
     * @author yl
     * @date 2023-07-08 10:56
     */
    @Override
    public KingdeeBusinessOperatorEntity find(KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO dto) {

        return this.lambdaQuery().eq(KingdeeBusinessOperatorEntity::getErpUserId, dto.getUserId())
                .eq(KingdeeBusinessOperatorEntity::getKingdeeOrgCode, dto.getOrgCode())
                .eq(KingdeeBusinessOperatorEntity::getKingdeeType, dto.getBusinessOperatorType())
                .eq(KingdeeBusinessOperatorEntity::getDisabled,Boolean.FALSE)
                .last("LIMIT 1").one();
    }


    /**
     * 获取业务员列表
     *
     * @param dto
     * @return java.util.List<com.common.business.dto.base.BaseDropDownDTO.CommonDTO>
     * @author yl
     * @date 2023-07-08 11:33
     */
    @Override
    public List<UserInfoDTO.BusinessOperationUserDTO> listInfo(KingdeeBusinessOperatorDTO.ListBusinessOperatorDTO dto) {
        List<UserInfoDTO.BusinessOperationUserDTO> resultList = new ArrayList<>(10);
        String orgId = dto.getOrgId();
        SysAccountingCompanyEntity orgInfo = sysAccountingCompanyService.getById(orgId);
        String code = orgInfo != null ? orgInfo.getCode() : "";
        List<UserInfoDTO.BusinessOperationUserDTO> dbList = baseMapper.listInfo(dto, code);
        String userId = commonService.getUserInfo().getUid();
        UserInfoDTO.BusinessOperationUserDTO findUser = dbList.stream().filter(d -> d.getUserId().equals(userId)).findFirst().orElse(null);
        if (findUser != null) {
            findUser.setIsMyState(1);
            resultList.add(findUser);
        } else {
            dbList.forEach(d -> d.setIsMyState(0));
        }
        List<UserInfoDTO.BusinessOperationUserDTO> wantList = dbList.stream().filter(d -> !userId.equals(d.getUserId())).collect(Collectors.toList());
        resultList.addAll(wantList);
        Integer zero= MathUtil.ZERO;
        for (UserInfoDTO.BusinessOperationUserDTO item : wantList) {
            Integer deleteState = item.getDeleteState();
            Integer userState = item.getUserState();
            if(zero.equals(deleteState)||zero.equals(userState)){
                item.setDisabled(Boolean.TRUE);
            }
        }
        if (StringUtils.isEmpty(dto.getOrgId())) {
            List<UserInfoDTO.BusinessOperationUserDTO> list = new ArrayList<>();
            List<String> userIdList = new ArrayList<>();
            for (UserInfoDTO.BusinessOperationUserDTO userDTO : resultList) {
                if (userIdList.contains(userDTO.getUserId())) {
                    continue;
                }
                userIdList.add(userDTO.getUserId());
                list.add(userDTO);
            }
            resultList = list;
        }
        return resultList;

    }
}
