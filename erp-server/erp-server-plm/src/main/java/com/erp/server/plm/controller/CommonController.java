package com.erp.server.plm.controller;

import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.FileUtil;
import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.modules.sys.dto.FindUserDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
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

    @PostMapping("/upload")
    public ApiResult upload(@RequestParam(value = "multipartFile") MultipartFile multipartFile, @RequestParam(value = "fileName") String fileName){
        File file = FileUtil.multiToFile(multipartFile);
        String filePath = FastDFSClientUtil.uploadFile(file, fileName);
        return this.success(filePath);
    }


}
