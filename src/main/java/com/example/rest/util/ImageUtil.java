package com.example.rest.util;

import org.imgscalr.Scalr;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ImageUtil {
    public static String makePath(String uploadPath, String fileName, Long book_id) throws IOException {
        String path=uploadPath+book_id;
        Files.createDirectories(Paths.get(path));
        return new File(path).getAbsolutePath()+"\\"+fileName;
    }

    public static BufferedImage getThumbnail(MultipartFile originFile, Integer width) throws IOException{
        BufferedImage thumbImg=null;
        BufferedImage img= ImageIO.read(originFile.getInputStream());
        thumbImg= Scalr.resize(img, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC, width, Scalr.OP_ANTIALIAS);
        return thumbImg;
    }
}
