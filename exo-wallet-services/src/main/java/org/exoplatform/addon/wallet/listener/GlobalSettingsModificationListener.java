/*
 * Copyright (C) 2003-2018 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.addon.wallet.listener;

import org.exoplatform.addon.wallet.model.GlobalSettings;
import org.exoplatform.addon.wallet.service.EthereumClientConnector;
import org.exoplatform.addon.wallet.service.WalletTokenAdminService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.listener.*;

/**
 * A listener to listen to apply new global settings on blockchain connection
 */
@Asynchronous
public class GlobalSettingsModificationListener extends Listener<Object, GlobalSettings> {
  private EthereumClientConnector       ethereumClientConnector;

  private WalletTokenAdminService tokenTransactionService;

  @Override
  public void onEvent(Event<Object, GlobalSettings> event) throws Exception {
    GlobalSettings globalSettings = event.getData();
    if (globalSettings != null) {
      getEthereumClientConnector().changeSettings(globalSettings);
    }
    getTokenTransactionService().reinit();
  }

  private WalletTokenAdminService getTokenTransactionService() {
    if (tokenTransactionService == null) {
      tokenTransactionService = CommonsUtils.getService(WalletTokenAdminService.class);
    }
    return tokenTransactionService;
  }

  public EthereumClientConnector getEthereumClientConnector() {
    if (ethereumClientConnector == null) {
      ethereumClientConnector = CommonsUtils.getService(EthereumClientConnector.class);
    }
    return ethereumClientConnector;
  }

}
