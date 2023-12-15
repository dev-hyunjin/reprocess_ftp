package com.app.find;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@SpringBootTest
public class MakeFileTest {

    @Test
    public void makeFile() throws IOException {
        for(int i = 1; i <= 50; i++) {
            File fileData = new File("D:\\RECORD\\20221225\\00", "20221225235727_43608" + i + "_rx.pcm");

            BufferedWriter writer = new BufferedWriter(new FileWriter(fileData));
            writer.write("");
            writer.close();
        }
    }
}
