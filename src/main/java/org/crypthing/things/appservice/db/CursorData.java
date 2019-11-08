package org.crypthing.things.appservice.db;

/**
 * CursorData
 */
public interface CursorData extends CursorMBean {

    Object next();
    
}