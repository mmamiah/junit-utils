open module junitutils.quality {
    // requires
    requires org.apache.commons.lang3;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.engine;
    requires org.junit.jupiter.params;
    requires org.junit.platform.launcher;
    requires org.junit.platform.commons;
    requires org.junit.platform.engine;
    requires spring.core;
    requires spring.test;

    requires org.slf4j;

    requires java.sql;
    requires spring.jdbc;
    requires org.mockito;
    requires org.reflections;
    requires org.apache.commons.collections4;
    requires spring.boot;
    requires org.mybatis;
    requires modelmapper;
    requires spring.beans;
    requires org.apache.commons.io;
    requires org.apache.commons.text;
    requires org.mockito.junit.jupiter;
    requires org.hamcrest;
    requires spring.context;
    requires spring.boot.test;
    requires spring.boot.autoconfigure;
    requires jakarta.annotation;
    requires org.apiguardian.api;

    // export
    exports lu.mms.common.quality;
    exports lu.mms.common.quality.assets;
    exports lu.mms.common.quality.assets.bdd;
    exports lu.mms.common.quality.assets.bdd.source;
    exports lu.mms.common.quality.assets.conditions;
    exports lu.mms.common.quality.assets.fixture;
    exports lu.mms.common.quality.assets.lifecycle;
    exports lu.mms.common.quality.assets.mock;
    exports lu.mms.common.quality.assets.mock.context;
    exports lu.mms.common.quality.assets.mock.injection;
    exports lu.mms.common.quality.assets.mock.reinforcement;
    exports lu.mms.common.quality.assets.mockvalue;
    exports lu.mms.common.quality.assets.mockvalue.commons;
    exports lu.mms.common.quality.assets.mybatis;
    exports lu.mms.common.quality.assets.spring.context;
    exports lu.mms.common.quality.assets.testutils;

    exports lu.mms.common.quality.assets.db;
    exports lu.mms.common.quality.assets.db.db2;
    exports lu.mms.common.quality.assets.db.h2;
    exports lu.mms.common.quality.assets.db.hsql;
    exports lu.mms.common.quality.assets.db.oracle;
    exports lu.mms.common.quality.assets.db.re;
    exports lu.mms.common.quality.assets.db.re.schema;
    exports lu.mms.common.quality.assets.db.re.script;

    exports lu.mms.common.quality.assets.no;
}