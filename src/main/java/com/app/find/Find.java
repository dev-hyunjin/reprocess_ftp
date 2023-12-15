package com.app.find;

import com.app.find.mapper.mysql.Mysql;
import com.app.find.mapper.oracle.Oracle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class Find extends Thread {

    @Autowired
    private Oracle oracle;

    @Autowired
    private Mysql mysql;

    @Value("${rec.mode}")
    private String recMode;

    @Value("${bpart.code}")
    private String bpartCode;

    @Value("${data.file.path}")
    private String dataFilePath;

    @Value("${pcm.file.path}")
    private String pcmFilePath;

    @Value("${ftp.send.file.path}")
    private String sendFilePath;

    private final FtpUtils ftpUtils;

//    @Scheduled(fixedDelay = 50000)
    @Scheduled(cron = "${set.cron.time}")
    @Override
    public void run() {
        try {
            List<Map<String, Object>> reprocessMaps = mysql.selectUCIDList();

            if(reprocessMaps.size() != 0) {
                FileText fileText = new FileText();

                reprocessMaps.forEach(reprocess -> {
                    try {
                        if(reprocess == null) {
                            log.error("ucid = null");
                            return;
                        }

                        String recKey = (String)reprocess.get("rec_key");
                        String ext = (String)reprocess.get("ext");
                        String recDate = (String)reprocess.get("rec_date");


//                    bpartCode 하나일때 코드
                        Map<String, Object> recordInfo = oracle.selectRecordInfo(recKey, ext, recDate, recMode, bpartCode);

//                        bpartCode 여러개일때 코드
//                        String[] bpartCodes = bpartCode.split(",");
//                        Map<String, Object> recordInfo = null;
//
//                        for (int i = 0; i < bpartCodes.length; i++) {
//                            bpartCodes[i] = bpartCodes[i].trim();
//                            recordInfo = oracle.selectRecordInfo(ucid, recMode, bpartCodes[i]);
//
//                            if(recordInfo != null) {
//                                break;
//                            }
//                        }

                        if(recordInfo == null) {
                            log.error("UCID : " + recKey + ", 내선번호 : " + ext + ", 녹취시간 : " + recDate + " - 녹취 데이터 없음");
                            return;
                        }

                        String recFileName = ((String)recordInfo.get("REC_FILENAME")).replace(".wav", "");

//                    file 이름 설정, data 파일 생성 및 ftp 전송
                        setFileNameAndFtpSend(fileText, recordInfo, recFileName);

//                    내선 번호, 키 코드로 찾은 상태 업데이트
                        mysql.updateFindStatus(recKey, ext, recDate);
                    } catch (ConnectException e) {
                        throw new RuntimeException(e);
                    } catch (Exception e) {
                        log.error(e.toString());
//                        e.printStackTrace();
                    }
                });
            } else {
                log.info("데이터 없음");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//        file 이름 설정, data 파일 생성 및 ftp 전송
    private void setFileNameAndFtpSend(FileText fileText, Map<String, Object> recordInfo, String recFileName) throws ConnectException {
        String dataFile = recFileName + ".data";
        String rxFile = recFileName + "_rx.pcm";
        String txFile = recFileName + "_tx.pcm";

//        String filePath = pcmFilePath + recordInfo.get("REC_DATE") + "\\" + recordInfo.get("REC_START_TIME_FOLDER") + "\\";
        String filePath = pcmFilePath + recordInfo.get("REC_DATE") + "/" + recordInfo.get("REC_START_TIME_FOLDER") + "/";

        File fileDataPath = new File(dataFilePath);
        File fileRx = new File(filePath + rxFile);
        File fileTx = new File(filePath + txFile);
        File fileData = new File(dataFilePath + dataFile);

        String dataText = "1" + dataText(recordInfo, rxFile) + "\n2" + dataText(recordInfo, txFile);

        log.info(dataFile + " - " + dataText);

        fileText.write(fileDataPath, fileData, dataText);

        String[] fileNameArr = {rxFile, txFile, dataFile};
//        어디에 있는 file 인지 각각 지정
        File[] fileArr = {fileRx, fileTx, fileData};

        fileSend(fileNameArr, fileArr, fileDataPath);
//        ftp 전송
//        ftpSend(fileNameArr, fileArr, fileDataPath);
    }

//    data 파일 내용 생성
    private String dataText(Map<String, Object> recordInfo, String typeFile) {
        /**
         * 각 필드 파라메터 관련 내용
         */
        /*
            Direction | REC_SEQ | LOCAL_NO | CUSTOM_FILED8가 null이면 rec_keycode | USER_ID | USER_NAME | REC_START_TIME | REC_END_TIME | REC_ELAPSE_TIME(초) | CUST_TEL | REC_FILENAME | CURRENT_TIME(현재시간) | REC_INOUT
            1|11|50001|00001004491700693610|agent01|테스트|2023-11-23 16:53:32|2023-11-23 16:54:21|44|01040494502|20231123165332_50001_rx.pcm|2023-11-23 17:55:55|O
            2|11|50001|00001004491700693610|agent01|테스트|2023-11-23 16:53:32|2023-11-23 16:54:21|44|01040494502|20231123165332_50001_tx.pcm|2023-11-23 17:55:55|O
         */

        String recSeq = recordInfo.get("REC_SEQ") == null ? "" : recordInfo.get("REC_SEQ").toString();
        String localNo = recordInfo.get("LOCAL_NO") == null ? "" : (String)recordInfo.get("LOCAL_NO");
        String customFld = recordInfo.get("CUSTOM_FLD_08") == null ? (String)recordInfo.get("REC_KEYCODE") : (recordInfo.get("CUSTOM_FLD_08") == null ? "" : (String)recordInfo.get("CUSTOM_FLD_08"));
        String userId = recordInfo.get("USER_ID") == null ? "" : (String)recordInfo.get("USER_ID");
        String userName = recordInfo.get("USER_NAME") == null ? "" : (String)recordInfo.get("USER_NAME");
        String recStartTime = recordInfo.get("REC_START_TIME") == null ? "" : (String)recordInfo.get("REC_START_TIME");
        String recEndTime = recordInfo.get("REC_END_TIME") == null ? "" : (String)recordInfo.get("REC_END_TIME");
        String recElapseTime = recordInfo.get("REC_ELAPSE_TIME") == null ? "" : recordInfo.get("REC_ELAPSE_TIME").toString();
        String custTel = recordInfo.get("CUST_TEL") == null ? "" : (String)recordInfo.get("CUST_TEL");
        String currentTime = recordInfo.get("CURRENT_TIME") == null ? "" : (String)recordInfo.get("CURRENT_TIME");
        String recInOut = recordInfo.get("REC_INOUT") == null ? "" : (String)recordInfo.get("REC_INOUT");

//        return "|" + recordInfo.get("REC_SEQ") + "|" + recordInfo.get("LOCAL_NO")
//                + "|" + (recordInfo.get("CUSTOM_FLD_08") == null ? recordInfo.get("REC_KEYCODE") : recordInfo.get("CUSTOM_FLD_08"))
//                + "|" + recordInfo.get("USER_ID") + "|" + recordInfo.get("USER_NAME") + "|" + recordInfo.get("REC_START_TIME") + "|" + recordInfo.get("REC_END_TIME")
//                + "|" + recordInfo.get("REC_ELAPSE_TIME") + "|" + recordInfo.get("CUST_TEL") + "|" + typeFile + "|" + recordInfo.get("CURRENT_TIME") + "|" + recordInfo.get("REC_INOUT");
        return "|" + recSeq + "|" + localNo + "|" + customFld + "|" + userId + "|" + userName + "|" + recStartTime + "|" + recEndTime
                + "|" + recElapseTime + "|" + custTel + "|" + typeFile + "|" + currentTime + "|" + recInOut;
    }

    private void fileSend(String[] fileNameArr, File[] fileArr, File fileDataPath) {
        try {
            File file = new File(sendFilePath);

            if(!file.exists()) {
                file.mkdir();
            }

            for (int i = 0; i < fileNameArr.length - 1; i++) {
                File newFile = new File(sendFilePath, fileNameArr[i]);

                Files.copy(fileArr[i].toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            Path oldFile = Paths.get(dataFilePath + fileNameArr[2]);
            Path newFile = Paths.get(sendFilePath + fileNameArr[2]);

            Files.move(oldFile, newFile, StandardCopyOption.ATOMIC_MOVE);

            if(fileArr[2].exists()) {
                fileArr[2].delete();
            }

            if(fileDataPath.exists()){
                fileDataPath.delete();
            }
        } catch (IOException e) {
            if(fileArr[2].exists()) {
                fileArr[2].delete();
            }

            if(fileDataPath.exists()){
                fileDataPath.delete();
            }

            throw new RuntimeException(e);
        }
    }

    //    ftp 전송
    private void ftpSend(String[] fileNameArr, File[] fileArr, File fileDataPath) throws ConnectException {
        try {
            FileInputStream[] fileInputStream = new FileInputStream[3];

//            ftp 연결 및 넣고싶은 폴더 지정(없다면 생성)
            ftpUtils.connect();

            for (int i = 0; i < fileArr.length; i++) {
                fileInputStream[i] = new FileInputStream(fileArr[i]);
            }

            for (int i = 0; i < fileInputStream.length; i++) {
                ftpUtils.upload(fileNameArr[i], fileInputStream[i]); 	//파일명

                if(fileInputStream[i] != null) {
                    fileInputStream[i].close();
                }

                if(i == 2) {
                    if(fileArr[2].exists()){
                        fileArr[2].delete();
                    }

                    if(fileDataPath.exists()){
                        fileDataPath.delete();
                    }
                }
            }

            ftpUtils.disconnect();
        } catch (ConnectException e) {
            if(fileArr[2].exists()) {
                fileArr[2].delete();
            }

            if(fileDataPath.exists()){
                fileDataPath.delete();
            }

            throw e;
        } catch (Exception e) {
            if(fileArr[2].exists()) {
                fileArr[2].delete();
            }

            if(fileDataPath.exists()){
                fileDataPath.delete();
            }

            throw new RuntimeException(e);
        }
    }
}
