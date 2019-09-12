package org.exoplatform.addon.wallet.service;

import java.util.*;

import org.exoplatform.addon.wallet.model.transaction.TransactionDetail;
import org.exoplatform.addon.wallet.model.transaction.TransactionStatistics;

/**
 * Manage stored transaction details in eXo internal datasource
 */
public interface WalletTransactionService {

  /**
   * @return {@link Set} of pending transaction hashes
   */
  Set<String> getPendingTransactionHashes();

  /**
   * @param address wallet address
   * @param contractAddress contract address to use to filter transactions
   * @param contractMethodName the contract method name to use to filter on
   *          transactions
   * @param hash the transaction hash to include in resulted transactions
   * @param limit limit size of returned transactions unless the hash parameter
   *          is not null, in that case, continue searching in transactions list
   *          until the hash is included in results
   * @param onlyPending whether filtering on pending transactions only or not
   * @param administration include administration transactions or not
   * @param currentUser the user accessing the list of transactions
   * @return the list of transactions corresponding to filter parameters
   * @throws IllegalAccessException if the current user isn't allowed to access
   *           wallet transactions
   */
  List<TransactionDetail> getTransactions(String address,
                                          String contractAddress,
                                          String contractMethodName,
                                          String hash,
                                          int limit,
                                          boolean onlyPending,
                                          boolean administration,
                                          String currentUser) throws IllegalAccessException;

  /**
   * Retrives the Transaction statistics of a user on a designated contract by
   * period of time
   * 
   * @param address
   * @param periodicity
   * @param selectedDate
   * @param locale
   * @return {@link TransactionStatistics} with sent and received amounts and
   *         labels
   */
  TransactionStatistics getTransactionStatistics(String address,
                                                 String periodicity,
                                                 String selectedDate,
                                                 Locale locale);

  /**
   * @param hash transaction hash
   * @return the transaction detail corresponding to the hash parameter,
   *         retrieved from internal database
   */
  TransactionDetail getTransactionByHash(String hash);

  /**
   * @param hash transaction hash
   * @param currentUser current username that is getting transaction details
   * @return the transaction detail corresponding to the hash parameter,
   *         retrieved from internal database
   */
  TransactionDetail getTransactionByHash(String hash, String currentUser);

  /**
   * @param fromAddress
   * @param nonce
   * @param currentUserId
   * @return the transaction identified by sender address and its nonce
   */
  TransactionDetail getTransactionByNonce(String fromAddress, long nonce, String currentUserId);

  /**
   * Save transaction details in database
   * 
   * @param transactionDetail transaction detail to save
   * @param broadcastMinedTransaction whether the transaction has been mined on
   *          blockchain or not
   */
  void saveTransactionDetail(TransactionDetail transactionDetail, boolean broadcastMinedTransaction);

  /**
   * Save transaction details in database
   *
   * @param transactionDetail transaction detail to save
   * @param currentUser current username that is saving transaction
   * @throws IllegalAccessException if current user is not allowed to save
   *           transaction to sender and receiver wallet
   */
  void saveTransactionDetail(TransactionDetail transactionDetail, String currentUser) throws IllegalAccessException;

  /**
   * @return max days to wait until marking a non existing transaction on
   *         blockchain as failed
   */
  long getPendingTransactionMaxDays();

}
