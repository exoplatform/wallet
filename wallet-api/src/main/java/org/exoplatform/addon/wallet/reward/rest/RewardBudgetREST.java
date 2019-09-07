package org.exoplatform.addon.wallet.reward.rest;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;

import org.exoplatform.addon.wallet.model.reward.RewardReport;
import org.exoplatform.addon.wallet.model.reward.WalletReward;
import org.exoplatform.addon.wallet.reward.service.RewardService;
import org.exoplatform.addon.wallet.service.WalletAccountService;
import org.exoplatform.addon.wallet.utils.WalletUtils;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;

import io.swagger.annotations.*;

@Path("/wallet/api/reward/")
@RolesAllowed("rewarding")
@Api(value = "/wallet/api/reward", description = "Manage wallet rewards") // NOSONAR
public class RewardBudgetREST implements ResourceContainer {
  private static final Log     LOG = ExoLogger.getLogger(RewardBudgetREST.class);

  private RewardService        rewardService;

  private WalletAccountService walletAccountService;

  public RewardBudgetREST(RewardService rewardService, WalletAccountService walletAccountService) {
    this.rewardService = rewardService;
    this.walletAccountService = walletAccountService;
  }

  @GET
  @Path("compute")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("rewarding")
  @ApiOperation(value = "Compute rewards of wallets per a chosen period of time", httpMethod = "GET", response = Response.class, produces = "application/json", notes = "returns a set of wallet reward object")
  @ApiResponses(value = {
      @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
      @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response computeRewards(@ApiParam(value = "Start date of period in milliseconds", required = true) @QueryParam("periodDateInSeconds") long periodDateInSeconds) {
    try {
      RewardReport rewardReport = rewardService.getRewardReport(periodDateInSeconds);

      for (WalletReward walletReward : rewardReport.getRewards()) {
        walletAccountService.retrieveWalletBlockchainState(walletReward.getWallet());
      }
      return Response.ok(rewardReport).build();
    } catch (Exception e) {
      LOG.error("Error getting computed reward", e);
      JSONObject object = new JSONObject();
      try {
        object.append("error", e.getMessage());
      } catch (JSONException e1) {
        // Nothing to do
      }
      return Response.status(HTTPStatus.INTERNAL_ERROR).type(MediaType.APPLICATION_JSON).entity(object.toString()).build();
    }
  }

  @GET
  @Path("send")
  @RolesAllowed("rewarding")
  @ApiOperation(value = "Send rewards of wallets per a chosen period of time", httpMethod = "GET", response = Response.class, notes = "return empty response")
  @ApiResponses(value = {
      @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
      @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response sendRewards(@ApiParam(value = "Start date of period in milliseconds", required = true) @QueryParam("periodDateInSeconds") long periodDateInSeconds) {
    try {
      rewardService.sendRewards(periodDateInSeconds, WalletUtils.getCurrentUserId());
      return Response.ok().build();
    } catch (Exception e) {
      LOG.error("Error getting computed reward", e);
      JSONObject object = new JSONObject();
      try {
        object.append("error", e.getMessage());
      } catch (JSONException e1) {
        // Nothing to do
      }
      return Response.status(HTTPStatus.INTERNAL_ERROR).type(MediaType.APPLICATION_JSON).entity(object.toString()).build();
    }
  }

}
