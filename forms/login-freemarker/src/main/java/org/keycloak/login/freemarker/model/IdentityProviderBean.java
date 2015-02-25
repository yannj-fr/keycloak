/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.keycloak.login.freemarker.model;

import org.keycloak.OAuth2Constants;
import org.keycloak.models.ApplicationModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.RealmModel;
import org.keycloak.services.resources.flows.Urls;

import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class IdentityProviderBean {

    private boolean displaySocial;

    private List<IdentityProvider> providers;
    private RealmModel realm;

    public IdentityProviderBean(RealmModel realm, URI baseURI, UriInfo uriInfo) {
        this.realm = realm;
        List<IdentityProviderModel> identityProviders = realm.getIdentityProviders();

        if (!identityProviders.isEmpty()) {
            providers = new LinkedList<IdentityProvider>();

            for (IdentityProviderModel identityProvider : identityProviders) {
                if (identityProvider.isEnabled()) {
                    String clientId = uriInfo.getQueryParameters().getFirst(OAuth2Constants.CLIENT_ID);

                    if (clientId != null) {
                        ClientModel clientModel = realm.findClient(clientId);

                        if (clientModel != null && !clientModel.hasIdentityProvider(identityProvider.getId())) {
                            if (ApplicationModel.class.isInstance(clientModel)) {
                                ApplicationModel applicationModel = (ApplicationModel) clientModel;

                                if (applicationModel.getName().equals(Constants.ACCOUNT_MANAGEMENT_APP)) {
                                    addIdentityProvider(realm, baseURI, identityProvider);
                                }
                            }

                            continue;
                        }
                    }

                    addIdentityProvider(realm, baseURI, identityProvider);
                }
            }

            if (!providers.isEmpty()) {
                displaySocial = true;
            }
        }
    }

    private void addIdentityProvider(RealmModel realm, URI baseURI, IdentityProviderModel identityProvider) {
        String loginUrl = Urls.identityProviderAuthnRequest(baseURI, identityProvider.getId(), realm.getName()).toString();
        providers.add(new IdentityProvider(identityProvider.getId(), identityProvider.getName(), loginUrl));
    }

    public List<IdentityProvider> getProviders() {
        return providers;
    }

    public boolean isDisplayInfo() {
        return  realm.isRegistrationAllowed() || displaySocial;
    }

    public static class IdentityProvider {

        private final String id;
        private final String name;
        private final String loginUrl;

        public IdentityProvider(String id, String name, String loginUrl) {
            this.id = id;

            if (name == null) {
                name = id;
            }

            this.name = name;
            this.loginUrl = loginUrl;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getLoginUrl() {
            return loginUrl;
        }

    }
}
