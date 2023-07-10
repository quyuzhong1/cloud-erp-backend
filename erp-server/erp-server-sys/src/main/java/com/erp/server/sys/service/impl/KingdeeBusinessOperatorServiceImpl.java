package com.erp.server.sys.service.impl;

import com.alibaba.excel.EasyExcel;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.ExcelUtil;
import com.erp.model.sys.dto.KingdeeBusinessOperatorDTO;
import com.erp.model.sys.dto.excel.KingdeeBusinessOperatorImportExcelDTO;
import com.erp.model.sys.entity.KingdeeBusinessOperatorEntity;
import com.erp.server.sys.listener.KingdeeBusinessOperatorExcelListener;
import com.erp.server.sys.mapper.KingdeeBusinessOperatorMapper;
import com.erp.server.sys.service.KingdeeBusinessOperatorService;
import com.erp.server.sys.service.SysUserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

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
        KingdeeBusinessOperatorExcelListener excelListener=new KingdeeBusinessOperatorExcelListener(this,userList,kingdeeBusinessOperatorList);
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
     * @author yl
     * @date 2023-07-08 10:56
     * @param dto
     * @return com.erp.model.sys.entity.KingdeeBusinessOperatorEntity
     */
    @Override
    public KingdeeBusinessOperatorEntity find(KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO dto) {

        return this.lambdaQuery().eq(KingdeeBusinessOperatorEntity::getErpUserId,dto.getUserId())
                .eq(KingdeeBusinessOperatorEntity::getKingdeeOrgCode,dto.getOrgCode())
                .eq(KingdeeBusinessOperatorEntity::getKingdeeType,dto.getBusinessOperatorType())
                .last("LIMIT 1").one();
    }


    /**
     * 获取业务员列表
     * @author yl
     * @date 2023-07-08 11:33
     * @param dto
     * @return java.util.List<com.common.business.dto.base.BaseDropDownDTO.CommonDTO>
     */
    @Override
    public List<BaseDropDownDTO.CommonDTO> listInfo(KingdeeBusinessOperatorDTO.ListBusinessOperatorDTO dto) {
        return baseMapper.listInfo(dto);
    }
}
