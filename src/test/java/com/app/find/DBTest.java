package com.app.find;

import com.app.find.mapper.mysql.Mysql;
import com.app.find.mapper.oracle.Oracle;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class DBTest {

    @Autowired
    Oracle oracle;

    @Autowired
    Mysql mysql;

    @Test
    public void dbTestOracle() {
//        log.info(oracle.selectRecordInfo("20200208").toString());
    }

    @Test
    public void dbTestMySQL() {
//        log.info(mysql.selectFindList() + "=============");
    }
}
