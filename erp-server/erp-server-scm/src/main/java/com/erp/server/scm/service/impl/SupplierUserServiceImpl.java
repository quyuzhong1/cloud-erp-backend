package com.erp.server.scm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.UserTypeEnum;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.scm.dto.SupplierRefUserDTO;
import com.erp.model.scm.dto.excel.SupplierUserImportExcelDTO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.entity.SupplierRefUserEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.vo.SupplierRefUserVO;
import com.erp.model.sys.dto.SysUserInfoDTO;
import com.erp.model.sys.dto.UpdateUserStateDTO;
import com.erp.model.sys.dto.UserPagingSearchDTO;
import com.erp.model.sys.entity.SysUserInfoEntity;
import com.erp.model.sys.vo.SupplierUserInfoVO;
import com.erp.model.sys.vo.SupplierUserVO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.sys.feign.UserInfoFeign;
import com.erp.server.scm.listener.SupplierUserExcelListener;
import com.erp.server.scm.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_SUPPLIER_USER;

/**
 * @author zdy
 * @ClassName SupplierUserServiceImpl
 * @description: 供应商协同用户
 * @date 2024年01月05日
 * @version: 1.0
 */
@Slf4j
@Service
public class SupplierUserServiceImpl implements SupplierUserService {
    @Resource
    private SupplierService supplierService;
    @Resource
    private UserInfoFeign userInfoFeign;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private SupplierRefUserService supplierRefUserService;
    @Resource
    private ModuleOperateLogService moduleOperateLogService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    /**
     * 分页查询 协同用户只展示 供应商超级管理员
     *
     * @param dto
     * @return
     */
    @Override
    public PagingVO<SupplierUserVO> paging(PagingDTO<UserPagingSearchDTO> dto) {
        Map<String, SupplierRefUserVO> supplierMap = new HashMap<>();
        PagingVO<SupplierUserVO> page = userInfoFeign.srmPaging(dto);
        List<SupplierUserVO> list = (List<SupplierUserVO>) page.getList();
        if (CollectionUtils.isNotEmpty(list)){
            List<String> uids = list.stream().map(SupplierUserVO::getUid).collect(Collectors.toList());
            List<SupplierRefUserVO> supplierRefUserVOS = supplierRefUserService.getSupplierRefByUids(uids);
            supplierMap = supplierRefUserVOS.stream().collect(Collectors.toMap(SupplierRefUserVO::getUid, Function.identity()));
        }
        dataProcessSupplierInfo(list, supplierMap);
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void add(SysUserInfoDTO sysUserInfoDTO) {
        //判断是否存在供应商
        if(StringUtils.isEmpty(sysUserInfoDTO.getSupplierId())) throw new ServiceException("供应商ID不能为空");
        SupplierEntity supplier = supplierService.getById(sysUserInfoDTO.getSupplierId());
        if (Objects.isNull(supplier)) throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        if (StringUtils.isEmpty(sysUserInfoDTO.getUserType())) {
            sysUserInfoDTO.setUserType(UserTypeEnum.SRM.code);
        }
        String uid = userInfoFeign.addSrmUser(sysUserInfoDTO);
        // 操作日志
        String msg = StrUtil.format("供应商协同用户【{}】新增【{}】id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "", uid);
        moduleOperateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SUPPLIER_REF_USER.getCode(), uid, "新增操作");
        //增加用户和供应商关系
        SupplierRefUserDTO.AddDTO addDTO = new SupplierRefUserDTO.AddDTO();
        addDTO.setUid(uid);
        addDTO.setSupplierId(sysUserInfoDTO.getSupplierId());
        addDTO.setDisabled(false);
        addDTO.setIsSuper(sysUserInfoDTO.getIsSuper());
        supplierRefUserService.add(addDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void update(SysUserInfoDTO sysUserInfoDTO) {
        //判断是否存在供应商
        if(StringUtils.isEmpty(sysUserInfoDTO.getRefId())) throw new ServiceException("供应商用户关系ID不能为空");
        if(StringUtils.isEmpty(sysUserInfoDTO.getSupplierId())) throw new ServiceException("供应商ID不能为空");
        SupplierEntity supplier = supplierService.getById(sysUserInfoDTO.getSupplierId());
        if (Objects.isNull(supplier)) throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        //1.更新用户基础信息
        userInfoFeign.updateSrmUser(sysUserInfoDTO);
        //2.更新供应商关系
        SupplierRefUserEntity old = supplierRefUserService.getById(sysUserInfoDTO.getRefId());
        SupplierRefUserEntity refUserEntity = new SupplierRefUserEntity();
        if (Objects.nonNull(old)) {
            refUserEntity.setId(old.getId());
        }
        refUserEntity.setUid(sysUserInfoDTO.getUid());
        refUserEntity.setSupplierId(sysUserInfoDTO.getSupplierId());
        refUserEntity.setIsSuper(Objects.nonNull(sysUserInfoDTO.getIsSuper())? sysUserInfoDTO.getIsSuper():false);
        refUserEntity.setDisabled(false);
        supplierRefUserService.saveOrUpdate(refUserEntity);
        // 记录主单操作日志
        log.info("编辑 开始记录日志数据，id：【{}】", sysUserInfoDTO.getRefId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), refUserEntity.getId(), "");
        moduleOperateLogService.addModuleOperateLogByObj(old, refUserEntity, ModuleTypeEnum.SUPPLIER_REF_USER.getCode(), refUserEntity.getId(), "", msg);
    }

    @Override
    public SupplierUserInfoVO getById(String uid) {
        SupplierUserInfoVO vo = new SupplierUserInfoVO();
        SysUserInfoEntity userInfoEntity = userInfoFeign.info(uid);
        if (Objects.nonNull(userInfoEntity)){
            BeanUtil.copyProperties(userInfoEntity, vo);
        }
        SupplierRefUserEntity supplierRelUserByUid = supplierRefUserService.getSupplierRelUserByUid(uid);
        if (Objects.nonNull(supplierRelUserByUid)){
            vo.setRefId(supplierRelUserByUid.getId());
            vo.setSupplierId(supplierRelUserByUid.getSupplierId());
            vo.setUid(supplierRelUserByUid.getUid());
            SupplierEntity supplierEntity = supplierService.getById(supplierRelUserByUid.getSupplierId());
            if (Objects.nonNull(supplierEntity)){
                vo.setSupplierName(supplierEntity.getName());
                vo.setPurchaseUserId(supplierEntity.getPurchaseUserId());
                vo.setPurchaseUserName(supplierEntity.getPurchaseUserName());
            }
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public ApiResult deleteById(String uid) {
        userInfoFeign.deleteSrmUser(Collections.singletonList(uid));
        //删除用户和供应商绑定记录
        supplierRefUserService.deleteRefByUids(Collections.singletonList(uid));
        return ApiResult.success();
    }

    @Override
    public ApiResult updateState(UpdateUserStateDTO stateDTO) {
        return userInfoFeign.updateStateSrm(stateDTO);
    }

    @Override
    public ApiResult changePassword(String uid, String pwd) {
        return userInfoFeign.changePassword(uid,pwd);
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
        }else if (excelDateList.size() > 5000){
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

    @Override
    public Boolean exportSupplierUser(UserPagingSearchDTO dto) {
        downloadTaskFeign.saveDownloadTask("供应商协同用户列表", EXPORT_SCM_SUPPLIER_USER.getCode(), dto);

        return Boolean.TRUE;
    }
    @Override
    public List<SupplierUserVO> getSupplierUserList(UserPagingSearchDTO dto) {
        Map<String, SupplierRefUserVO> supplierMap = new HashMap<>();
        List<SupplierUserVO> list = userInfoFeign.srmList(dto);
        if (CollectionUtils.isNotEmpty(list)){
            List<String> uids = list.stream().map(SupplierUserVO::getUid).collect(Collectors.toList());
            List<SupplierRefUserVO> supplierRefUserVOS = supplierRefUserService.getSupplierRefByUids(uids);
            supplierMap = supplierRefUserVOS.stream().collect(Collectors.toMap(SupplierRefUserVO::getUid, Function.identity()));
        }
        dataProcessSupplierInfo(list, supplierMap);
        return list;
    }

    @Override
    public PagingVO<SupplierUserVO> exportSupplierUser(PagingDTO<UserPagingSearchDTO> dto) {
        List<SupplierUserVO> list = getSupplierUserList(dto.getParams());
        return new PagingVO<>(list, 0, dto.getPageSize(), dto.getCurrPage());
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
            sysUserInfoDTO.setIsSuper(true);
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
                supplierRefUserService.save(refUserEntity);
            }catch (Exception e){
                excelDTO.setErrorMsg("保存供应商和用户关系失败：" + e.getMessage());
                errorList.add(excelDTO);
            }
        }
    }

    private List<String> checkImportData(SupplierUserImportExcelDTO excelDTO,SupplierRefUserEntity refUserEntity) {
        List<String> errorMsgList = new ArrayList<>();
        if (StringUtils.isEmpty(excelDTO.getSupplierName()) || StringUtils.isEmpty(excelDTO.getSupplierName().trim())){
            errorMsgList.add(ApiError.ERROR_EMPTY_SUPPLIER.msg);
            return errorMsgList;
        }
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)){
            return msgList;
        }
        //供应商是否存在
        List<SupplierEntity> supplierEntityList = supplierService.listBySupplierByNames(Collections.singletonList(excelDTO.getSupplierName().trim()));
        if (CollectionUtils.isNotEmpty(supplierEntityList)){
            SupplierEntity supplierEntity = supplierEntityList.get(0);
            //供应商状态判断
            if(Objects.isNull(supplierEntity.getDisabled()) ||  supplierEntity.getDisabled()){
                errorMsgList.add(ApiError.ERROR_SUPPLIER_DISABLE.msg);
                return errorMsgList;
            }
            if(Objects.isNull(supplierEntity.getApproveStatus()) ||  !supplierEntity.getApproveStatus().getStatus().equals(ApproveStatusEnum.APPROVE.getStatus())){
                errorMsgList.add(ApiError.ERROR_SUPPLIER_UN_APPROVE.msg);
                return errorMsgList;
            }
            if(Objects.isNull(supplierEntity.getSrmDisabled()) ||  supplierEntity.getSrmDisabled()){
                errorMsgList.add(ApiError.ERROR_SUPPLIER_SRM_DISABLE.msg);
                return errorMsgList;
            }
            supplierEntity.getApproveStatus();
            supplierEntity.getDisabled();
            refUserEntity.setSupplierId(supplierEntity.getId());
            refUserEntity.setDisabled(false);
            refUserEntity.setIsSuper(true);
        }else {
            errorMsgList.add(ApiError.ERROR_SUPPLIER_ABSENCE.msg);
            return errorMsgList;
        }
        //用户是否存在
        FindUserDTO user = sysUserFeign.getUserByMobile(excelDTO.getMobile(), UserTypeEnum.SRM.code);
        if (Objects.isNull(user)){
            errorMsgList.add(ApiError.MOBILE_IS_EXIST.msg);
            return errorMsgList;
        }
        return errorMsgList;
    }

    private void dataProcessSupplierInfo(List<SupplierUserVO> list, Map<String, SupplierRefUserVO> supplierMap) {
        if (CollectionUtils.isNotEmpty(list)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            list.forEach(supplierUserVO -> {
                SupplierRefUserVO supplierRefUserVO = supplierMap.get(supplierUserVO.getUid());
                if (Objects.nonNull(supplierRefUserVO)) {
                    supplierUserVO.setRefId(supplierRefUserVO.getRefId());
                    supplierUserVO.setSupplierId(supplierRefUserVO.getSupplierId());
                    supplierUserVO.setSupplierName(supplierRefUserVO.getSupplierName());
                    supplierUserVO.setPurchaseUserName(supplierRefUserVO.getPurchaseUserName());
                }
                if (Objects.nonNull(supplierUserVO.getIsBindWechat()) && supplierUserVO.getIsBindWechat()){
                    supplierUserVO.setIsBindWechatStr("已绑定");
                }else {
                    supplierUserVO.setIsBindWechatStr("未绑定");
                }
                if (Objects.nonNull(supplierUserVO.getUserState()) && 1 == supplierUserVO.getUserState()){
                    supplierUserVO.setUserStateStr("已启用");
                }else {
                    supplierUserVO.setUserStateStr("已禁用");
                }
                if (Objects.nonNull(supplierUserVO.getIsSuper()) && supplierUserVO.getIsSuper()){
                    supplierUserVO.setIsSuperStr("超级管理员");
                }else {
                    supplierUserVO.setIsSuperStr("业务员");
                }
                if (Objects.nonNull(supplierUserVO.getLastLoginTime())){

                    supplierUserVO.setLastLoginTimeStr(sdf.format(supplierUserVO.getLastLoginTime()));
                }else {
                    supplierUserVO.setLastLoginTimeStr("");
                }
                if (Objects.nonNull(supplierUserVO.getCreateTime())){

                    supplierUserVO.setCreateTimeStr(sdf.format(supplierUserVO.getCreateTime()));
                }else {
                    supplierUserVO.setCreateTimeStr("");
                }
            });
        }
    }
}
