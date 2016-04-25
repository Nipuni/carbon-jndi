/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.wso2.carbon.jndi.internal.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.naming.Binding;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

/**
 * OSGiServiceBinding enumeration implementation..
 */
public class OSGiServiceBindingsEnumeration implements NamingEnumeration<Binding> {

    /**
     * Caller bundle context.
     */
    private BundleContext bundleContext;
    /**
     * Maintain current position of the references.
     */
    private int currentIndex = 0;
    /**
     * Maintains list of bindings.
     */
    private List<Binding> bindings;
    /**
     * ServiceReference list for the given context.
     */
    private List<ServiceReference> references;

    /**
     * create OSGiServiceBindingsEnumeration instance building the Binding objects.
     *
     * @param bundleContext owning bundle context
     * @param refs          servicereferences of each service of the registry
     */
    public OSGiServiceBindingsEnumeration(BundleContext bundleContext, List<ServiceReference> refs) {
        this.bundleContext = bundleContext;
        bindings = buildBindings(refs);
        references = refs;
    }

    private List<Binding> buildBindings(List<ServiceReference> serviceReferencesList) {
        //name are a string with the service.id number
        //A Binding object contains the name, class of the service, and the service object.
        Predicate<ServiceReference> filterNotNullReferences =
                (ServiceReference reference) -> (bundleContext.getService(reference) != null);
        return serviceReferencesList.stream()
                .filter(filterNotNullReferences)
                .map(this::buildBindings)
                .collect(Collectors.toList());
    }

    private Binding buildBindings(ServiceReference serviceReference) {
        return new Binding(
                String.valueOf(serviceReference.getProperty(Constants.SERVICE_ID)),
                bundleContext.getService(serviceReference).getClass().getName(),
                bundleContext.getService(serviceReference));
    }

    /**
     * Retrieves the next element in the enumeration.
     */
    @Override
    public Binding next() throws NamingException {
        return nextElement();
    }

    /**
     * Determines whether there are any more elements in the enumeration.
     */
    @Override
    public boolean hasMore() throws NamingException {
        return hasMoreElements();
    }

    /**
     * unget any gotten services and cleanup.
     */
    @Override
    public void close() throws NamingException {
        references.forEach(bundleContext::ungetService);
    }

    /**
     * Tests if this enumeration contains more elements.
     */
    @Override
    public boolean hasMoreElements() {
        return currentIndex < bindings.size();
    }

    /**
     * Returns the next element of this enumeration if this enumeration
     * object has at least one more element to provide.
     */
    @Override
    public Binding nextElement() {
        return bindings.get(currentIndex++);
    }
}
