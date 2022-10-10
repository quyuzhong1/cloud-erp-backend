package com.erp.server.plm.controller;

import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.FileUtil;
import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.modules.sys.dto.FindUserDTO;
import com.erp.model.plm.dto.UploadImgDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
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
     * @param files 图片流
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
}
