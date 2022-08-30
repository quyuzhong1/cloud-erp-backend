package com.cloud.erp.chrome.service;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.text.csv.CsvReader;
import cn.hutool.core.text.csv.CsvUtil;
import cn.hutool.core.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Classname CsvServer
 * @Description TODO
 * @Date 2022-08-24 11:59
 * @Created by yl
 */
@Slf4j
@Component
public class CsvServer<T> {


    public List<T> getObjectListByMultipartFile(MultipartFile multipartFile, Class<T> clazz) {
        List<T> resultList = new ArrayList<>();
        try {
            File f = this.multiToFile(multipartFile); //MultipartFile转file
            CsvReader csvReader = CsvUtil.getReader();
            resultList = csvReader.read(ResourceUtil.getReader(f.getPath(), CharsetUtil.CHARSET_GBK), clazz);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getObjectListByMultipartFile", e);
        }
        return resultList;

    }


    /**
     * 将MultipartFile 转化成 file
     *
     * @param
     * @return java.io.File
     * @author yl
     * @date 2022-08-24 10:28
     */
    private File multiToFile(MultipartFile multipartFile) {

        //选择用缓冲区来实现这个转换即使用java 创建的临时文件 使用 MultipartFile.transferto()方法 。
        File file = null;
        try {
            String originalFilename = multipartFile.getOriginalFilename();
            String[] filename = originalFilename.split("\\.");
            file = File.createTempFile(filename[0], filename[1]);
            multipartFile.transferTo(file);
            file.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return file;
    }



    public List<T> getObjectListByFile(File file, Class<T> clazz) {
        List<T> resultList = new ArrayList<>();
        try {
            CsvReader csvReader = CsvUtil.getReader();
            resultList = csvReader.read(ResourceUtil.getReader(file.getPath(), CharsetUtil.CHARSET_GBK), clazz);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getObjectListByFile", e);
        }
        return resultList;

    }
}
