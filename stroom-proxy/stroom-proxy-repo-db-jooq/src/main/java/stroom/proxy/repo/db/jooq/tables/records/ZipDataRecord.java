/*
 * This file is generated by jOOQ.
 */
package stroom.proxy.repo.db.jooq.tables.records;


import javax.annotation.processing.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record5;
import org.jooq.Row5;
import org.jooq.impl.UpdatableRecordImpl;

import stroom.proxy.repo.db.jooq.tables.ZipData;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.12.3"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ZipDataRecord extends UpdatableRecordImpl<ZipDataRecord> implements Record5<Integer, String, String, Integer, Boolean> {

    private static final long serialVersionUID = -1483116814;

    /**
     * Setter for <code>zip_data.id</code>.
     */
    public void setId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>zip_data.id</code>.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>zip_data.name</code>.
     */
    public void setName(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>zip_data.name</code>.
     */
    public String getName() {
        return (String) get(1);
    }

    /**
     * Setter for <code>zip_data.feedName</code>.
     */
    public void setFeedname(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>zip_data.feedName</code>.
     */
    public String getFeedname() {
        return (String) get(2);
    }

    /**
     * Setter for <code>zip_data.fk_zip_source_id</code>.
     */
    public void setFkZipSourceId(Integer value) {
        set(3, value);
    }

    /**
     * Getter for <code>zip_data.fk_zip_source_id</code>.
     */
    public Integer getFkZipSourceId() {
        return (Integer) get(3);
    }

    /**
     * Setter for <code>zip_data.has_dest</code>.
     */
    public void setHasDest(Boolean value) {
        set(4, value);
    }

    /**
     * Getter for <code>zip_data.has_dest</code>.
     */
    public Boolean getHasDest() {
        return (Boolean) get(4);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record5 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row5<Integer, String, String, Integer, Boolean> fieldsRow() {
        return (Row5) super.fieldsRow();
    }

    @Override
    public Row5<Integer, String, String, Integer, Boolean> valuesRow() {
        return (Row5) super.valuesRow();
    }

    @Override
    public Field<Integer> field1() {
        return ZipData.ZIP_DATA.ID;
    }

    @Override
    public Field<String> field2() {
        return ZipData.ZIP_DATA.NAME;
    }

    @Override
    public Field<String> field3() {
        return ZipData.ZIP_DATA.FEEDNAME;
    }

    @Override
    public Field<Integer> field4() {
        return ZipData.ZIP_DATA.FK_ZIP_SOURCE_ID;
    }

    @Override
    public Field<Boolean> field5() {
        return ZipData.ZIP_DATA.HAS_DEST;
    }

    @Override
    public Integer component1() {
        return getId();
    }

    @Override
    public String component2() {
        return getName();
    }

    @Override
    public String component3() {
        return getFeedname();
    }

    @Override
    public Integer component4() {
        return getFkZipSourceId();
    }

    @Override
    public Boolean component5() {
        return getHasDest();
    }

    @Override
    public Integer value1() {
        return getId();
    }

    @Override
    public String value2() {
        return getName();
    }

    @Override
    public String value3() {
        return getFeedname();
    }

    @Override
    public Integer value4() {
        return getFkZipSourceId();
    }

    @Override
    public Boolean value5() {
        return getHasDest();
    }

    @Override
    public ZipDataRecord value1(Integer value) {
        setId(value);
        return this;
    }

    @Override
    public ZipDataRecord value2(String value) {
        setName(value);
        return this;
    }

    @Override
    public ZipDataRecord value3(String value) {
        setFeedname(value);
        return this;
    }

    @Override
    public ZipDataRecord value4(Integer value) {
        setFkZipSourceId(value);
        return this;
    }

    @Override
    public ZipDataRecord value5(Boolean value) {
        setHasDest(value);
        return this;
    }

    @Override
    public ZipDataRecord values(Integer value1, String value2, String value3, Integer value4, Boolean value5) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached ZipDataRecord
     */
    public ZipDataRecord() {
        super(ZipData.ZIP_DATA);
    }

    /**
     * Create a detached, initialised ZipDataRecord
     */
    public ZipDataRecord(Integer id, String name, String feedname, Integer fkZipSourceId, Boolean hasDest) {
        super(ZipData.ZIP_DATA);

        set(0, id);
        set(1, name);
        set(2, feedname);
        set(3, fkZipSourceId);
        set(4, hasDest);
    }
}
