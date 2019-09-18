package org.exoplatform.addon.wallet.reward.dao;

import java.util.Collections;
import java.util.List;

import javax.persistence.TypedQuery;

import org.exoplatform.addon.wallet.model.reward.RewardPeriodType;
import org.exoplatform.addon.wallet.model.reward.RewardStatus;
import org.exoplatform.addon.wallet.reward.entity.WalletRewardPeriodEntity;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

public class RewardPeriodDAO extends GenericDAOJPAImpl<WalletRewardPeriodEntity, Long> {

  public WalletRewardPeriodEntity findRewardPeriodByTypeAndTime(RewardPeriodType periodType, long periodTime) {
    TypedQuery<WalletRewardPeriodEntity> query = getEntityManager().createNamedQuery("RewardPeriod.findRewardPeriodByTypeAndTime",
                                                                                     WalletRewardPeriodEntity.class);
    query.setParameter("periodType", periodType);
    query.setParameter("periodTime", periodTime);
    List<WalletRewardPeriodEntity> resultList = query.getResultList();
    return resultList == null || resultList.isEmpty() ? null : resultList.get(0);
  }

  public List<WalletRewardPeriodEntity> findRewardPeriodsByStatus(RewardStatus status) {
    TypedQuery<WalletRewardPeriodEntity> query = getEntityManager().createNamedQuery("RewardPlugin.findRewardPeriodsByStatus",
                                                                                     WalletRewardPeriodEntity.class);
    query.setParameter("status", status);
    List<WalletRewardPeriodEntity> resultList = query.getResultList();
    return resultList == null ? Collections.emptyList() : resultList;
  }

}
