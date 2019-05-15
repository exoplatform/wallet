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
package org.exoplatform.addon.wallet.reward.plugin;

import java.lang.reflect.Method;
import java.util.*;

import org.exoplatform.addon.wallet.reward.api.RewardPlugin;
import org.exoplatform.addon.wallet.utils.RewardUtils;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.Component;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class KudosRewardPlugin extends RewardPlugin {

  private static final Log     LOG                           = ExoLogger.getLogger(KudosRewardPlugin.class);

  private static final String  KUDOS_SERVICE_FQN             = "org.exoplatform.addon.kudos.service.KudosService";

  private static final String  COUNT_USERS_KUDOS_METHOD_NAME = "countKudosByPeriodAndReceiver";

  private ConfigurationManager configurationManager;

  private ExoContainer         container;

  private Object               serviceInstance;

  private Method               retrievePointsMethod;

  private boolean              enabled;

  public KudosRewardPlugin(ExoContainer container, ConfigurationManager configurationManager) {
    this.container = container;
    this.configurationManager = configurationManager;

    Component component = this.configurationManager.getComponent(KUDOS_SERVICE_FQN);
    enabled = component != null;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public Map<Long, Double> gtEarnedPoints(Set<Long> identityIds, long startDateInSeconds, long endDateInSeconds) {
    HashMap<Long, Double> earnedPoints = new HashMap<>();
    if (identityIds == null || identityIds.isEmpty()) {
      return earnedPoints;
    }
    Method method = getMethod();
    if (method == null) {
      throw new IllegalStateException("Can't find kudos service method to retrieve user points");
    }
    for (Long identityId : identityIds) {
      long points = 0;
      try {
        points = (Long) method.invoke(getService(), identityId, startDateInSeconds, endDateInSeconds);
      } catch (Exception e) {
        LOG.warn("Error getting kudos count for user with id {}", identityId, e);
      }
      earnedPoints.put(identityId, (double) points);
    }
    return earnedPoints;
  }

  private Method getMethod() {
    if (this.retrievePointsMethod != null) {
      return retrievePointsMethod;
    }
    retrievePointsMethod = RewardUtils.getMethod(container, KUDOS_SERVICE_FQN, COUNT_USERS_KUDOS_METHOD_NAME);
    return retrievePointsMethod;
  }

  private Object getService() {
    if (this.serviceInstance != null) {
      return serviceInstance;
    }
    serviceInstance = RewardUtils.getService(container, KUDOS_SERVICE_FQN);
    return serviceInstance;
  }

}
