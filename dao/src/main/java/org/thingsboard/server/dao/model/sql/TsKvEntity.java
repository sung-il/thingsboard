/**
 * Copyright © 2016-2018 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.dao.model.sql;

import lombok.Data;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.kv.BasicTsKvEntry;
import org.thingsboard.server.common.data.kv.BooleanDataEntry;
import org.thingsboard.server.common.data.kv.DoubleDataEntry;
import org.thingsboard.server.common.data.kv.KvEntry;
import org.thingsboard.server.common.data.kv.LongDataEntry;
import org.thingsboard.server.common.data.kv.StringDataEntry;
import org.thingsboard.server.common.data.kv.TsKvEntry;
import org.thingsboard.server.dao.model.ToData;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import static org.thingsboard.server.dao.model.ModelConstants.BOOLEAN_VALUE_COLUMN;
import static org.thingsboard.server.dao.model.ModelConstants.DOUBLE_VALUE_COLUMN;
import static org.thingsboard.server.dao.model.ModelConstants.ENTITY_ID_COLUMN;
import static org.thingsboard.server.dao.model.ModelConstants.ENTITY_TYPE_COLUMN;
import static org.thingsboard.server.dao.model.ModelConstants.KEY_COLUMN;
import static org.thingsboard.server.dao.model.ModelConstants.LONG_VALUE_COLUMN;
import static org.thingsboard.server.dao.model.ModelConstants.STRING_VALUE_COLUMN;
import static org.thingsboard.server.dao.model.ModelConstants.TS_COLUMN;

@Data
@Entity
@Table(name = "ts_kv")
@IdClass(TsKvCompositeKey.class)
public final class TsKvEntity implements ToData<TsKvEntry> {

    public TsKvEntity() {
    }

    public TsKvEntity(Long longSumValue, Double doubleSumValue, Long longCountValue, Long doubleCountValue) {
        double sum = 0.0;
        if (longSumValue != null) {
            sum += longSumValue;
        }
        if (doubleSumValue != null) {
            sum += doubleSumValue;
        }
        this.doubleValue = sum / (longCountValue + doubleCountValue);
    }

    public TsKvEntity(Long sumLongValue, Double sumDoubleValue) {
        if (sumDoubleValue != null) {
            this.doubleValue = sumDoubleValue + (sumLongValue != null ? sumLongValue.doubleValue() : 0.0);
        } else {
            this.longValue = sumLongValue;
        }
    }

    public TsKvEntity(String strValue, Long longValue, Double doubleValue, boolean max) {
        this.strValue = strValue;
        if (longValue != null && doubleValue != null) {
            this.doubleValue = max ? Math.max(doubleValue, longValue.doubleValue()) : Math.min(doubleValue, longValue.doubleValue());
        } else {
            this.longValue = longValue;
            this.doubleValue = doubleValue;
        }
    }

    public TsKvEntity(Long booleanValueCount, Long strValueCount, Long longValueCount, Long doubleValueCount) {
        if (booleanValueCount != 0) {
            this.longValue = booleanValueCount;
        } else if (strValueCount != 0) {
            this.longValue = strValueCount;
        } else {
            this.longValue = longValueCount + doubleValueCount;
        }
    }

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = ENTITY_TYPE_COLUMN)
    private EntityType entityType;

    @Id
    @Column(name = ENTITY_ID_COLUMN)
    private String entityId;

    @Id
    @Column(name = KEY_COLUMN)
    private String key;

    @Id
    @Column(name = TS_COLUMN)
    private long ts;

    @Column(name = BOOLEAN_VALUE_COLUMN)
    private Boolean booleanValue;

    @Column(name = STRING_VALUE_COLUMN)
    private String strValue;

    @Column(name = LONG_VALUE_COLUMN)
    private Long longValue;

    @Column(name = DOUBLE_VALUE_COLUMN)
    private Double doubleValue;

    @Override
    public TsKvEntry toData() {
        KvEntry kvEntry = null;
        if (strValue != null) {
            kvEntry = new StringDataEntry(key, strValue);
        } else if (longValue != null) {
            kvEntry = new LongDataEntry(key, longValue);
        } else if (doubleValue != null) {
            kvEntry = new DoubleDataEntry(key, doubleValue);
        } else if (booleanValue != null) {
            kvEntry = new BooleanDataEntry(key, booleanValue);
        }
        return new BasicTsKvEntry(ts, kvEntry);
    }

    public boolean isNotEmpty() {
        return strValue != null || longValue != null || doubleValue != null || booleanValue != null;
    }
}
