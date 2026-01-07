package com.securities.parser.model;

import java.util.List;

/**
 * 파싱된 증권 메시지 전체 구조 (Java 17 record)
 */
public record SecuritiesMessage(
    SecuritiesHeader header,
    List<SecuritiesDataRecord> dataRecords,
    long timestamp
) {
    public int recordCount() {
        return dataRecords.size();
    }
}
