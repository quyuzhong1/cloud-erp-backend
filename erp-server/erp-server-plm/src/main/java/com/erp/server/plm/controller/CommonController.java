package com.erp.server.plm.controller;

import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.FileUtil;
import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.modules.sys.dto.FindUserDTO;
import com.erp.model.plm.dto.ProductOperateRecordDTO;
import com.erp.model.plm.dto.ProductPurchaseRemarkDTO;
import com.erp.model.plm.dto.UploadImgDTO;
import com.erp.model.plm.entity.ProductOperateRecordEntity;
import com.erp.model.plm.entity.ProductPurchaseRemarkEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.enums.BasicDictTypeEnum;
import com.erp.server.plm.service.ProductOperateRecordService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** 公共接口
 * @Classname plm
 * @Description TODO
 * @Date 2022-10-08 14:59
 * @Created by yl
 */
@RestController
@RequestMapping("plm/common")
public class CommonController  extends BaseController {

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private ProductOperateRecordService productOperateRecordService;

    /**
     * 获取用户
     * @param dto
     * @return
     */
    @PostMapping("/findUserList")
    public ApiResult<List<FindUserDTO>> findUserList(@RequestBody  BaseSearchDTO dto){
        return sysUserFeign.userList(dto);
    }

    /**
     * 上传图片
     * @Author Luo_WG
     * @Date 2022/10/9 17:35
     * @param multipartFile 图片流
     * @return com.erp.common.dto.base.ApiResult
     **/
    @PostMapping("/upload")
    public ApiResult upload(@RequestParam("multipartFile") MultipartFile[] multipartFile, HttpServletRequest request){
        MultipartHttpServletRequest httpservletrequest = (MultipartHttpServletRequest) request;
        List<MultipartFile> lists = httpservletrequest.getFiles("multipartFile");
        List<String> list = new ArrayList<>();
        for (MultipartFile file : multipartFile) {
            String filePath = FastDFSClientUtil.uploadFile(file);
            list.add(filePath);
        }
        return this.success(list);
    }

    /**
     * 产品开发管理-项目任务-产品操作日志-查询
     * @Author Luo_WG
     * @Date 2022/10/11 11:51
     * @param productId 产品表id
     * @return com.erp.common.dto.base.ApiResult<java.util.List<com.erp.model.plm.entity.ProductOperateRecordEntity>>
     **/
    @GetMapping("/listOperateRecord")
    public ApiResult<List<ProductOperateRecordEntity>> listOperateRecord(@RequestParam(value = "productId") String productId) {
        List<ProductOperateRecordEntity> list = productOperateRecordService.list(productId);
        return this.success(list);
    }

    /**
     * 产品开发管理-项目任务-产品操作日志-新增
     * @Author Luo_WG
     * @Date 2022/10/11 11:52
     * @param dto 产品操作记录表（VO）
     * @return com.erp.common.dto.base.ApiResult
     **/
    @PostMapping("/saveOrUpdateOperateRecord")
    public ApiResult saveOrUpdateOperateRecord(@RequestBody ProductOperateRecordDTO dto) {
        Boolean flag = productOperateRecordService.saveOrUpdate(dto);
        return flag == true ? this.success() : this.failure();
    }

    /**
     * 产品开发管理-项目任务-产品操作日志-批量新增
     * @Author Luo_WG
     * @Date 2022/10/11 11:52
     * @param dto 产品操作记录表
     * @return com.erp.common.dto.base.ApiResult
     **/
    @PostMapping("/saveOrUpdateOperateRecordBatch")
    public ApiResult saveOrUpdateOperateRecordBatch(@RequestBody List<ProductOperateRecordDTO> dto) {
        Boolean flag = productOperateRecordService.saveOrUpdateBatch(dto);
        return flag == true ? this.success() : this.failure();
    }

    /**
     * 获取字典表所有类型
     * @Author Luo_WG
     * @Date 2022/10/11 11:52
     * @return com.erp.common.dto.base.ApiResult
     **/
    @PostMapping("/listBasicDictType")
    public ApiResult listBasicDictType() {
        return this.success(productOperateRecordService.listBasicDictType());
    }

}
