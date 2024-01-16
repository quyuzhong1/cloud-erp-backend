package com.erp.server.srm.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.common.business.dto.FindUserDTO;
import com.common.business.enums.UserTypeEnum;
import com.common.business.interceptor.CommonInterceptor;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.scm.dto.excel.SupplierUserImportExcelDTO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.entity.SupplierRefUserEntity;
import com.erp.model.sys.dto.SysUserInfoDTO;
import com.erp.model.sys.dto.UserPagingSearchDTO;
import com.erp.model.sys.vo.SupplierUserInfoVO;
import com.erp.model.sys.vo.SupplierUserVO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.sys.feign.UserInfoFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.rpc.wms.feign.SupplierFeign;
import com.erp.rpc.wms.feign.SupplierUserFeign;
import com.erp.server.srm.listener.SupplierUserExcelListener;
import com.erp.server.srm.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * @author zdy
 * @ClassName UserServiceImpl
 * @description: 用户通用方法
 * @date 2024年01月09日
 * @version: 1.0
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private SupplierUserFeign supplierUserFeign;
    @Resource
    private UserInfoFeign userInfoFeign;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private ScmTaskFeign scmTaskFeign;
    /**
     * 获取当前用户供应商id
     * @return
     */
    @Override
    public String getSupplierId(){
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
        if (Objects.nonNull(loginUser)){
            String uid = loginUser.getUid();
            //获取用户关联供应商
            SupplierUserInfoVO info = supplierUserFeign.info(uid);
            if (Objects.nonNull(info) && StringUtils.isNotEmpty(info.getSupplierId())){
                //用户是否禁用
                if (Objects.isNull(info.getUserState()) || 0 == info.getUserState()) {
                    throw new ServiceException(ApiError.ERROR_9016);
                }else {
                    return info.getSupplierId();
                }
            }else {
                //用户未关联供应商
                throw new ServiceException(ApiError.ERROR_USER_NOT_REL_SUPPLIER);
            }
        }else {
            throw new ServiceException(ApiError.ERROR_403);
        }
    }

    @Override
    public void exportSupplier(UserPagingSearchDTO dto, HttpServletResponse response) {
        List<SupplierUserVO> list = supplierUserFeign.list(dto);
        String name = "供应商协同用户列表";
        StringBuffer sb = new StringBuffer();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        String excelPath = "excel/sysUserExport.xlsx";
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("供应商协同用户列表导出出错 >>>>>{}", e);
        }
    }

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        SupplierUserExcelListener excelListenerUtil = new SupplierUserExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), SupplierUserImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        List<SupplierUserImportExcelDTO> excelDateList = excelListenerUtil.getExcelDateList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        List<SupplierUserImportExcelDTO > errorList = excelListenerUtil.getErrorList();

        List<SupplierUserImportExcelDTO> successList = excelListenerUtil.getSuccessList();
        //处理验证成功数据
        handleImportSuccessList(successList, errorList);

        if (errorList.size() > 0) {
            StringBuffer sb = new StringBuffer();
            String excelPath = "excel/sysUserImportError.xlsx";
            String name = "sysUserImportError";
            String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
            sb.append(date);
            sb.append(name);
            try {
                new ExcelPrintUtils().patchExport(errorList, response, sb.toString(), excelPath);
            } catch (IOException e) {
                throw new ServiceException(ApiError.ERROR_95125);
            }
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }


    private void handleImportSuccessList(List<SupplierUserImportExcelDTO> successList, List<SupplierUserImportExcelDTO> errorList) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        for (SupplierUserImportExcelDTO excelDTO :successList) {
            SupplierRefUserEntity refUserEntity = new SupplierRefUserEntity();
            List<String> errorMsgList = checkImportData(excelDTO,refUserEntity);
            if (CollectionUtils.isNotEmpty(errorMsgList)){
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
                continue;
            }
            //新增用户
            SysUserInfoDTO sysUserInfoDTO = new SysUserInfoDTO();
            sysUserInfoDTO.setUserType(UserTypeEnum.SRM.code);
            sysUserInfoDTO.setUserName(excelDTO.getUserName());
            sysUserInfoDTO.setRealName(excelDTO.getUserName());
            sysUserInfoDTO.setEmail(excelDTO.getEmail());
            sysUserInfoDTO.setMobile(excelDTO.getMobile());
            sysUserInfoDTO.setUserState(1);
            sysUserInfoDTO.setNeedChangePwd(true);
            sysUserInfoDTO.setCreatePasswordType(0);
            //scm新增用户都为管理员
            sysUserInfoDTO.setIsSuper(false);
            try {
                String uid = userInfoFeign.addSrmUser(sysUserInfoDTO);
                refUserEntity.setUid(uid);
            }catch (Exception e){
                excelDTO.setErrorMsg("创建用户异常：" + e.getMessage());
                errorList.add(excelDTO);
                continue;
            }
            //创建用户和供应商关系
            try {
                supplierUserFeign.saveRef(refUserEntity);
            }catch (Exception e){
                excelDTO.setErrorMsg("保存供应商和用户关系失败：" + e.getMessage());
                errorList.add(excelDTO);
            }
        }
    }
    private List<String> checkImportData(SupplierUserImportExcelDTO excelDTO,SupplierRefUserEntity refUserEntity) {
        List<String> errorMsgList = new ArrayList<>();
        //供应商是否存在
        List<SupplierEntity> supplierEntityList = scmTaskFeign.listBySupplierByNames(Collections.singletonList(excelDTO.getSupplierName().trim()));
        if (CollectionUtils.isNotEmpty(supplierEntityList)){
            SupplierEntity supplierEntity = supplierEntityList.get(0);
            String supplierId = this.getSupplierId();
            if (!supplierId.equalsIgnoreCase(supplierEntity.getId())){
                //只能导入当前供应商用户
                errorMsgList.add(ApiError.ERROR_USER_NOT_REL_OTHER_SUPPLIER.msg);
            }else {
                refUserEntity.setSupplierId(supplierId);
                refUserEntity.setDisabled(false);
                refUserEntity.setIsSuper(false);
            }
        }else {
            errorMsgList.add(ApiError.ERROR_SUPPLIER_ABSENCE.msg);
        }
        //用户是否存在
        FindUserDTO user = sysUserFeign.getUserByMobile(excelDTO.getMobile(), UserTypeEnum.SRM.code);
        if (Objects.isNull(user)){
            errorMsgList.add(ApiError.MOBILE_IS_EXIST.msg);
        }
        return errorMsgList;
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/sysUserImportTemplate.xlsx";
        String excelName = "template.xlsx";
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), "ISO8859-1"));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            throw new ServiceException(ApiError.Default);
        }
    }
}
