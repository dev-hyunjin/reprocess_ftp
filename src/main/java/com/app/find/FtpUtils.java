package com.app.find;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
@Slf4j
public class FtpUtils {
    @Value("${ftp.server.ip}")
    private String server;

    @Value("${ftp.server.port}")
    private int port;

    @Value("${ftp.server.username}")
    private String username;

    @Value("${ftp.server.password}")
    private String password;

    @Value("${ftp.send.file.path}")
    private String ftpFilePath;

    private FTPClient ftpClient = new FTPClient();

    // FTP 연결 및 설정
    public void connect() throws Exception {
        try {
            boolean result = false;
            ftpClient.connect(server, port);			//FTP 연결
            ftpClient.setControlEncoding("UTF-8");	//FTP 인코딩 설정
            int reply = ftpClient.getReplyCode();	//응답코드 받기

            if (!FTPReply.isPositiveCompletion(reply)) {	//응답 False인 경우 연결 해제
                ftpClient.disconnect();
                throw new Exception("FTP서버 연결실패");
            }
            if(!ftpClient.login(username, password)) {
                ftpClient.logout();
                throw new Exception("FTP서버 로그인실패");
            }

            ftpClient.setSoTimeout(1000 * 10);		//Timeout 설정
            ftpClient.login(username, password);				//FTP 로그인
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);	//파일타입설정
            ftpClient.enterLocalPassiveMode();			//Active 모드 설정
            result = ftpClient.changeWorkingDirectory(ftpFilePath);	//저장파일경로

            if(!result){	// result = False 는 저장파일경로가 존재하지 않음
                ftpClient.makeDirectory(ftpFilePath);	//저장파일경로 생성
                ftpClient.changeWorkingDirectory(ftpFilePath);
            }
        } catch (Exception e) {
            if(e.getMessage().indexOf("refused") != -1) {
                throw new Exception("FTP서버 연결실패");
            }
            throw e;
        }
    }

    // FTP 연결해제
    public void disconnect(){
        try {
            if(ftpClient.isConnected()){
                ftpClient.disconnect();
            }
        } catch (IOException e) {
            int a = 0;
            int b = 0;
            a = b;
            b = a;
        }
    }

    // FTP 파일 업로드
    public void upload(String saveFileNm, InputStream inputStream) throws Exception{
        try {
            if(!ftpClient.storeFile(saveFileNm, inputStream)) {
                throw new Exception("FTP서버 업로드실패");
            }
        } catch (Exception e) {
            if(e.getMessage().indexOf("not open") != -1) {
                throw new Exception("FTP서버 연결실패");
            }
            throw e;
        }
    }
}
