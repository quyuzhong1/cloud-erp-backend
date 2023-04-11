package com.erp.server.sys.controller.api;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.FastDFSClientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lambda
 * @Classname CommonController
 * @Description TODO
 * @Date 2023-03-23 18:54
 * @Created by yl
 */
@RestController
@RequestMapping("common")
public class CommonController extends BaseController {


    /**
     * 上传图片
     *
     * @param multipartFile 图片流
     * @return com.common.core.vo.ApiResult
     * @Author Luo_WG
     * @Date 2022/10/9 17:35
     **/
    @PostMapping("/upload")
    public ApiResult upload(@RequestParam("multipartFile") MultipartFile[] multipartFile, HttpServletRequest request) {
        List<String> list = new ArrayList<>();
        for (MultipartFile file : multipartFile) {
            String filePath = FastDFSClientUtil.uploadFile(file);
            list.add(filePath);
        }
        return this.success(list);
    }

    /**
     * 删除 上传utl
     *
     * @return com.common.core.vo.ApiResult
     * @Author Luo_WG
     * @Date 2022/10/9 17:35
     **/
    @PostMapping("/deleteUrl")
    public ApiResult deleteUrl(@RequestParam("url") String url) {
        if (StringUtils.isNotBlank(url)) {
            int num = FastDFSClientUtil.deleteFile(url);
            if (num == 0) {
                return success();
            }
        }
        return failure();

    }


}
