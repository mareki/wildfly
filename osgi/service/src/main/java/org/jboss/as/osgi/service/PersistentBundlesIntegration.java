/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
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
package org.jboss.as.osgi.service;

import static org.jboss.as.osgi.OSGiConstants.SERVICE_BASE_NAME;
import static org.jboss.as.osgi.OSGiLogger.LOGGER;
import static org.jboss.as.server.Services.JBOSS_SERVER_CONTROLLER;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.registry.Resource;
import org.jboss.as.server.deployment.Phase;
import org.jboss.as.server.deployment.Services;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.msc.service.AbstractService;
import org.jboss.msc.service.AbstractServiceListener;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceController.Transition;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.osgi.deployment.deployer.Deployment;
import org.jboss.osgi.framework.IntegrationServices;
import org.jboss.osgi.framework.PersistentBundlesHandler;
import org.jboss.osgi.framework.PersistentBundlesHandlerComplete;

/**
 * A service that provides persistent bundles on framework startup.
 *
 * @author thomas.diesler@jboss.com
 * @since 12-Apr-2012
 */
public class PersistentBundlesIntegration implements PersistentBundlesHandler {

    private final InitialDeploymentTracker deploymentTracker;

    public static ServiceController<?> addService(ServiceTarget serviceTarget, InitialDeploymentTracker deploymentTracker) {
        PersistentBundlesIntegration service = new PersistentBundlesIntegration(deploymentTracker);
        ServiceBuilder<PersistentBundlesHandler> builder = serviceTarget.addService(IntegrationServices.PERSISTENT_BUNDLES_HANDLER, service);
        builder.addDependencies(IntegrationServices.AUTOINSTALL_HANDLER_COMPLETE, InitialDeploymentTracker.INITIAL_DEPLOYMENTS_COMPLETE);
        builder.setInitialMode(Mode.ON_DEMAND);
        return builder.install();
    }

    private PersistentBundlesIntegration(InitialDeploymentTracker deploymentTracker) {
        this.deploymentTracker = deploymentTracker;
    }

    @Override
    public void start(StartContext context) throws StartException {
        final ServiceController<?> controller = context.getController();
        LOGGER.infof("Starting: %s in mode %s", controller.getName(), controller.getMode());
        Map<ServiceName, Deployment> installedBundles = deploymentTracker.getInstalledBundles();
        PersistentBundlesHandlerComplete installComplete = new PersistentBundlesHandlerComplete(installedBundles);
        installComplete.install(context.getChildTarget());
    }

    @Override
    public void stop(StopContext context) {
        ServiceController<?> controller = context.getController();
        LOGGER.infof("Stopping: %s in mode %s", controller.getName(), controller.getMode());
    }

    @Override
    public PersistentBundlesIntegration getValue() {
        return this;
    }

    public static class InitialDeploymentTracker extends AbstractServiceListener<Object> {

        static final ServiceName INITIAL_DEPLOYMENTS_COMPLETE = SERVICE_BASE_NAME.append("initial", "deployments", "COMPLETE");

        private final Set<String> deploymentNames;
        private final AtomicInteger deploymentCount;
        private final ServiceTarget serviceTarget;

        private Map<ServiceName, Deployment> installedBundles;
        private Set<ServiceName> bundleInstallServices;
        private Set<ServiceName> deploymentServiceNames;
        private ServiceTarget listenerTarget;

        public InitialDeploymentTracker(OperationContext context) {
            serviceTarget = context.getServiceTarget();
            deploymentNames = getDeploymentNames(context);
            deploymentCount = new AtomicInteger(deploymentNames.size());
            if (deploymentCount.get() == 0) {
                initialDeploymentsComplete();
                return;
            }

            bundleInstallServices = new HashSet<ServiceName>();
            deploymentServiceNames = new HashSet<ServiceName>();
            installedBundles = new LinkedHashMap<ServiceName, Deployment>();

            for (String deploymentName : deploymentNames) {
                ServiceName serviceName = Services.deploymentUnitName(deploymentName);
                deploymentServiceNames.add(serviceName.append(Phase.INSTALL.toString()));
            }
            ServiceRegistry serviceRegistry = context.getServiceRegistry(false);
            listenerTarget = serviceRegistry.getService(JBOSS_SERVER_CONTROLLER).getServiceContainer();
            listenerTarget.addListener(Inheritance.ALL, this);
        }

        @Override
        public void transition(ServiceController<? extends Object> controller, Transition transition) {
            if (isClosed() == false) {
                ServiceName serviceName = controller.getName();
                synchronized (deploymentServiceNames) {
                    if (deploymentServiceNames.contains(serviceName)) {
                        switch (transition) {
                            case STARTING_to_UP:
                            case STARTING_to_START_FAILED:
                                deploymentServiceNames.remove(serviceName);
                                int remaining = deploymentCount.decrementAndGet();
                                LOGGER.infof("Deployment tracked: %s (remaining=%d)", serviceName.getCanonicalName(), remaining);
                                if (deploymentCount.get() == 0) {
                                    listenerTarget.removeListener(this);
                                    initialDeploymentsComplete();
                                }
                        }
                    }
                }
            }
        }

        private void initialDeploymentsComplete() {
            LOGGER.infof("Initial deployments complete");
            final ServiceBuilder<Void> builder = serviceTarget.addService(INITIAL_DEPLOYMENTS_COMPLETE, new AbstractService<Void>() {
                public void start(StartContext context) throws StartException {
                    final ServiceController<?> controller = context.getController();
                    LOGGER.infof("Starting: %s in mode %s", controller.getName(), controller.getMode());
                }
            });
            if (bundleInstallServices != null) {
                builder.addDependencies(bundleInstallServices);
            }
            builder.install();
        }

        public boolean isClosed() {
            return deploymentCount.get() == 0;
        }

        public boolean removeDeploymentName(String depname) {
            return deploymentNames.remove(depname);
        }

        public void registerPersistentBundleInstallService(ServiceName serviceName) {
            if (isClosed() == false) {
                LOGGER.infof("Add bundle install dependency: %s", serviceName);
                bundleInstallServices.add(serviceName);
            }
        }

        public void addInstalledBundle(ServiceName serviceName, Deployment deployment) {
            if (isClosed() == false) {
                LOGGER.infof("Add installed bundle dependency: %s", serviceName);
                installedBundles.put(serviceName, deployment);
            }
        }

        @SuppressWarnings("unchecked")
        private Map<ServiceName, Deployment> getInstalledBundles() {
            return installedBundles != null ? installedBundles : Collections.EMPTY_MAP;
        }

        private Set<String> getDeploymentNames(OperationContext context) {
            final Set<String> result = new HashSet<String>();
            final ModelNode model = Resource.Tools.readModel(context.readResourceFromRoot(PathAddress.EMPTY_ADDRESS, true));
            final ModelNode depmodel = model.get(ModelDescriptionConstants.DEPLOYMENT);
            if (depmodel.isDefined()) {
                final List<ModelNode> deploymentNodes = depmodel.asList();
                for (ModelNode node : deploymentNodes) {
                    Property property = node.asProperty();
                    result.add(property.getName());
                }
                LOGGER.infof("Expecting initial deployments: %s", result);
            }
            return result;
        }
    }
}
