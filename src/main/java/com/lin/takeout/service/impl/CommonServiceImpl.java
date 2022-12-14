package com.lin.takeout.service.impl;

import com.lin.takeout.common.Result;
import com.lin.takeout.service.CommonService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.UUID;

@Service
public class CommonServiceImpl implements CommonService {

    @Value("${local.img}")
    String localImg;

    //获取/下载图片
    @Override
    public void getImg(String img, ServletOutputStream outputStream) throws Exception {

        //如果在配置文件中配置了本地路径就将接下来的注释掉
        String pathName = getClass().getResource("/")+"";
        pathName = pathName.substring(6);
        pathName = pathName+"../../src/main/resources/static/img/";
        pathName = pathName+img;

        if (StringUtils.isNotEmpty(localImg)){
            pathName = localImg+img;
        }

        pathName = pathName.replace("/","\\");

        FileInputStream inputStream = new FileInputStream(new File(pathName));
        int len;
        byte[] bytes = new byte[1024];

        while ((len = inputStream.read(bytes)) != -1){
            outputStream.write(bytes,0,len);
            outputStream.flush();
        }

        outputStream.close();
        inputStream.close();
    }

    //上传图片
    @Override
    public Result<String> uploadImg(MultipartFile file) throws Exception {

        String pathName = getClass().getResource("/")+"";
        pathName = pathName.substring(6);
        pathName = pathName+"../../src/main/resources/static/img/";

        if (StringUtils.isNotEmpty(localImg)){
            pathName = localImg;
        }

        String imgName = file.getOriginalFilename();
        String suffix = imgName.substring(imgName.lastIndexOf("."));
        imgName = UUID.randomUUID().toString()+suffix;

        pathName = pathName+imgName;
        pathName = pathName.replace("/","\\");
        file.transferTo(new File(pathName));

        return Result.success(imgName);
    }

    //删除图片
    @Override
    public int deleteImg(String img) throws Exception {

        String pathName = getClass().getResource("/")+"";
        pathName = pathName.substring(6);
        pathName = pathName+"../../src/main/resources/static/img/";
        pathName = pathName+img;

        if (StringUtils.isNotEmpty(localImg)){
            pathName = localImg+img;
        }
        pathName = pathName.replace("/","\\");

        if (new File(pathName).delete())
            return 1;

        return -1;
    }
}
