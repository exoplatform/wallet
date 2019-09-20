/*
   * Copyright (C) 2003-2019 eXo Platform SAS.
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
package org.exoplatform.addon.wallet.reward.job;

import static org.exoplatform.addon.wallet.utils.RewardUtils.getRewardSettings;

import java.util.List;

import org.quartz.*;

import org.exoplatform.addon.wallet.model.reward.*;
import org.exoplatform.addon.wallet.reward.service.RewardReportService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.*;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * This job is used to check pending rewards sent for periods. When all
 * transaction rewards are sent correctly without error, an event is triggered
 * through {@link ListenerService}
 */
@DisallowConcurrentExecution
public class RewardCurrentPeriodStatusUpdaterJob implements Job {

  private static final Log    LOG = ExoLogger.getLogger(RewardCurrentPeriodStatusUpdaterJob.class);

  private ExoContainer        container;

  private RewardReportService rewardReportService;

  public RewardCurrentPeriodStatusUpdaterJob() {
    this.container = PortalContainer.getInstance();
  }

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    ExoContainer currentContainer = ExoContainerContext.getCurrentContainer();
    ExoContainerContext.setCurrentContainer(container);
    RequestLifeCycle.begin(this.container);
    try {
      RewardSettings rewardSettings = getRewardSettings();
      if (rewardSettings != null && rewardSettings.getPeriodType() != null) {
        List<RewardPeriod> rewardPeriods = getRewardReportService().getRewardPeriodsNotSent();
        if (rewardPeriods != null && !rewardPeriods.isEmpty()) {
          for (RewardPeriod rewardPeriod : rewardPeriods) {
            long startDateInSeconds = rewardPeriod.getStartDateInSeconds();
            RewardReport rewardReport = getRewardReportService().getRewardReport(startDateInSeconds);
            if (rewardReport != null) {
              rewardReport = getRewardReportService().computeRewards(startDateInSeconds);
              getRewardReportService().saveRewardReport(rewardReport);
            }
          }
        }

        // Compute and save current period if not estimated yet
        long currentTimeInSeconds = System.currentTimeMillis() / 1000;
        RewardReport rewardReport = getRewardReportService().getRewardReport(currentTimeInSeconds);
        if (rewardReport == null) {
          rewardReport = getRewardReportService().computeRewards(currentTimeInSeconds);
          getRewardReportService().saveRewardReport(rewardReport);
        }
      }
    } catch (Exception e) {
      LOG.error("Error while checking pending rewards", e);
    } finally {
      RequestLifeCycle.end();
      ExoContainerContext.setCurrentContainer(currentContainer);
    }
  }

  private RewardReportService getRewardReportService() {
    if (rewardReportService == null) {
      rewardReportService = CommonsUtils.getService(RewardReportService.class);
    }
    return rewardReportService;
  }
}
