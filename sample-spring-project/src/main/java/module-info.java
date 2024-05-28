
open module junitutils.sample.spring {
    requires spring.web;
    requires spring.context;
    requires spring.core;
    requires spring.beans;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires org.apache.commons.lang3;
    requires jakarta.ws.rs;

    exports lu.mms.common.controller;
    exports lu.mms.common.service;
}