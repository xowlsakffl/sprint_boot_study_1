package com.example.rest.util;

import org.imgscalr.Scalr;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    public static Path getFileAsResource(String uploadPath, Long book_id, String file_name)
            throws IOException {
        String location = uploadPath + book_id + "\\" + file_name;
        File file = new File(location);
        if (file.exists()) {
            Path path = Paths.get(file.getAbsolutePath());
            return path;
        } else {
            return null;
        }
    }

    public static boolean deleteImage(String uploadPath, Long book_id, String fileName) {
        try {
            File f = new File(uploadPath + book_id + "\\" +fileName); // file to be delete
            if (f.delete())
            {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
