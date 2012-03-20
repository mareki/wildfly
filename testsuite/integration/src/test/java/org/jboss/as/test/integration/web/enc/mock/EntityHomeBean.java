/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.as.test.integration.web.enc.mock;

import java.io.Serializable;
import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.DuplicateKeyException;
import javax.ejb.EJBMetaData;
import javax.ejb.FinderException;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.RemoveException;

/**
 * A bean that can be bound into jndi, and implements EntityHome
 * 
 * @author Scott.Stark@jboss.org
 */
public class EntityHomeBean implements Serializable, EntityHome {

    private static final long serialVersionUID = 1;

    public Entity create(String key) throws CreateException, DuplicateKeyException, RemoteException {
        return null;
    }

    public Entity findByPrimaryKey(String key) throws FinderException, RemoteException {
        return null;
    }

    public EJBMetaData getEJBMetaData() throws RemoteException {
        return null;
    }

    public HomeHandle getHomeHandle() throws RemoteException {
        return null;
    }

    public void remove(Handle handle) throws RemoteException, RemoveException {
    }

    public void remove(Object primaryKey) throws RemoteException, RemoveException {
    }

}