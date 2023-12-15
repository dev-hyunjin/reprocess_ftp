package com.app.find;

import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Component
public class FileText {

    public void write(File filePath, File fileData, String content) {
        try {
//            File folder = new File(filePath);

//            폴더가 없으면 폴더 생성
            if(!filePath.exists()) {
                filePath.mkdir();
            }
            
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileData));
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
