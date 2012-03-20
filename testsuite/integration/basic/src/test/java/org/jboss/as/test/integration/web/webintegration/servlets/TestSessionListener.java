/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.test.integration.web.webintegration.servlets;

import java.util.HashMap;
import java.util.Enumeration;
import javax.servlet.http.HttpSessionListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSession;

import org.jboss.logging.Logger;

/**
 * A test HttpSessionListener that tracks which sessions have seen
 * create/destory events.
 * 
 * @author Scott.Stark@jboss.org
 */
public class TestSessionListener implements HttpSessionListener {
    
    static Logger log = Logger.getLogger(TestSessionListener.class);
    
    static HashMap<String, boolean[]> eventMap = new HashMap<String, boolean[]>();

    static boolean wasCreated(String sessionID) {
        boolean[] events = eventMap.get(sessionID);
        if (events == null)
            return false;
        return events[0];
    }

    static boolean wasDestroyed(String sessionID) {
        boolean[] events = eventMap.get(sessionID);
        if (events == null)
            return false;
        return events[1];
    }

    public void sessionCreated(HttpSessionEvent event) {
        String sessionID = event.getSession().getId();
        log.info("sessionCreated, sessionID=" + sessionID);
        boolean[] events = { true, false };
        eventMap.put(sessionID, events);
    }

    public void sessionDestroyed(HttpSessionEvent event) {
        String sessionID = event.getSession().getId();
        log.info("sessionDestroyed, sessionID=" + sessionID);
        HttpSession session = event.getSession();
        Enumeration<String> names = session.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            log.info(name + "=" + session.getAttribute(name));
        }
        boolean[] events = eventMap.get(sessionID);
        events[1] = true;
    }
}