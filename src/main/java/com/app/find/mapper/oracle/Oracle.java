package com.app.find.mapper.oracle;

import java.time.LocalDateTime;
import java.util.Map;

public interface Oracle {
    public Map<String, Object> selectRecordInfo(String ucid, String localNo, String recDate, String recMode, String bpartCode);
}
