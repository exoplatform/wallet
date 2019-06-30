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
package org.exoplatform.addon.wallet.rest;

import static org.exoplatform.addon.wallet.utils.WalletUtils.getCurrentUserId;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.addon.wallet.model.transaction.TransactionDetail;
import org.exoplatform.addon.wallet.service.WalletTokenAdminService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;

import io.swagger.annotations.*;

@Path("/wallet/api/admin/transaction")
@RolesAllowed("rewarding")
@Api(value = "/wallet/api/admin/transaction", description = "Manages admin wallet transactions to send on blockchain") // NOSONAR
public class WalletAdminTransactionREST implements ResourceContainer {

  private static final String     BAD_REQUEST_SENT_TO_SERVER_BY = "Bad request sent to server by '";

  private static final Log        LOG                           = ExoLogger.getLogger(WalletAdminTransactionREST.class);

  private WalletTokenAdminService walletTokenAdminService;

  @POST
  @Path("intiialize")
  @RolesAllowed("rewarding")
  @ApiOperation(value = "Send blockchain transaction using Admin wallet to initialize wallet identified by its address", httpMethod = "POST", response = Response.class, notes = "returns transaction hash")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 400, message = "Invalid query input"),
      @ApiResponse(code = 403, message = "Unauthorized operation"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response intializeWallet(@ApiParam(value = "receiver wallet address", required = true) @FormParam("receiver") String receiver,
                                  @ApiParam(value = "ether amount to send to wallet", required = false) @FormParam("etherAmount") double etherAmount,
                                  @ApiParam(value = "token amount to send to wallet", required = false) @FormParam("tokenAmount") double tokenAmount,
                                  @ApiParam(value = "transaction label", required = false) @FormParam("transactionLabel") String transactionLabel,
                                  @ApiParam(value = "transaction message to send to receiver with transaction", required = false) @FormParam("transactionMessage") String transactionMessage) {
    String currentUserId = getCurrentUserId();
    if (StringUtils.isBlank(receiver)) {
      LOG.warn(BAD_REQUEST_SENT_TO_SERVER_BY + currentUserId + "' with empty address");
      return Response.status(400).build();
    }

    try {
      TransactionDetail transactionDetail = new TransactionDetail();
      transactionDetail.setTo(receiver);
      transactionDetail.setContractAmount(tokenAmount);
      transactionDetail.setValue(etherAmount);
      transactionDetail.setLabel(transactionLabel);
      transactionDetail.setMessage(transactionMessage);
      transactionDetail = getWalletTokenAdminService().initialize(transactionDetail, currentUserId);
      return Response.ok(transactionDetail == null ? "" : transactionDetail.getHash()).build();
    } catch (Exception e) {
      LOG.error("Error initializing wallet {}", receiver, e);
      return Response.serverError().build();
    }
  }

  @POST
  @Path("sendEther")
  @RolesAllowed("rewarding")
  @ApiOperation(value = "Send ether using blockchain transaction from Admin wallet", httpMethod = "POST", response = Response.class, notes = "returns transaction hash")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 400, message = "Invalid query input"),
      @ApiResponse(code = 403, message = "Unauthorized operation"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response sendEther(@ApiParam(value = "receiver wallet address", required = true) @FormParam("receiver") String receiver,
                            @ApiParam(value = "ether amount to send to wallet", required = false) @FormParam("etherAmount") double etherAmount,
                            @ApiParam(value = "transaction label", required = false) @FormParam("transactionLabel") String transactionLabel,
                            @ApiParam(value = "transaction message to send to receiver with transaction", required = false) @FormParam("transactionMessage") String transactionMessage) {
    String currentUserId = getCurrentUserId();
    if (StringUtils.isBlank(receiver)) {
      LOG.warn(BAD_REQUEST_SENT_TO_SERVER_BY + currentUserId + "' with empty address");
      return Response.status(400).build();
    }

    try {
      TransactionDetail transactionDetail = new TransactionDetail();
      transactionDetail.setTo(receiver);
      transactionDetail.setValue(etherAmount);
      transactionDetail.setLabel(transactionLabel);
      transactionDetail.setMessage(transactionMessage);
      transactionDetail = getWalletTokenAdminService().sendEther(transactionDetail, currentUserId);
      return Response.ok(transactionDetail == null ? "" : transactionDetail.getHash()).build();
    } catch (Exception e) {
      LOG.error("Error initializing wallet {}", receiver, e);
      return Response.serverError().build();
    }
  }

  /**
   * Workaround: WalletTokenAdminService retrieved here instead of dependency
   * injection using constructor because the service is added after
   * PortalContainer startup. (See PLF-8123)
   * 
   * @return wallet token service
   */
  private WalletTokenAdminService getWalletTokenAdminService() {
    if (walletTokenAdminService == null) {
      walletTokenAdminService = CommonsUtils.getService(WalletTokenAdminService.class);
    }
    return walletTokenAdminService;
  }

}
