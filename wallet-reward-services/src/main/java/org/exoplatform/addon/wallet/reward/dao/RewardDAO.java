package org.exoplatform.addon.wallet.reward.dao;

import java.util.List;

import javax.persistence.TypedQuery;

import org.exoplatform.addon.wallet.reward.entity.WalletRewardEntity;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class RewardDAO extends GenericDAOJPAImpl<WalletRewardEntity, Long> {
  private static final Log LOG = ExoLogger.getLogger(RewardDAO.class);

  public List<WalletRewardEntity> findRewardsByPeriodId(long periodId) {
    TypedQuery<WalletRewardEntity> query = getEntityManager().createNamedQuery("Reward.findRewardsByPeriodId",
                                                                               WalletRewardEntity.class);
    query.setParameter("periodId", periodId);
    return query.getResultList();
  }

  public List<WalletRewardEntity> findRewardsByIdentityId(long identityId) {
    TypedQuery<WalletRewardEntity> query = getEntityManager().createNamedQuery("Reward.findRewardsByIdentityId",
                                                                               WalletRewardEntity.class);
    query.setParameter("identityId", identityId);
    return query.getResultList();
  }

  public WalletRewardEntity findRewardByIdentityIdAndPeriodId(long identityId, long periodId) {
    TypedQuery<WalletRewardEntity> query = getEntityManager().createNamedQuery("Reward.findRewardByIdentityIdAndPeriodId",
                                                                               WalletRewardEntity.class);
    query.setParameter("periodId", periodId);
    query.setParameter("identityId", identityId);
    List<WalletRewardEntity> result = query.getResultList();
    if (result == null || result.isEmpty()) {
      return null;
    } else if (result.size() > 1) {
      LOG.warn("More than one reward was found for identityId {} and periodId {}", identityId, periodId);
    }
    return result.get(0);
  }

}
