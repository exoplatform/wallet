package org.exoplatform.addon.wallet.blockchain.service;

import static org.exoplatform.addon.wallet.utils.WalletUtils.NEW_TRANSACTION_EVENT;

import java.io.IOException;
import java.security.Provider;
import java.security.Security;
import java.util.concurrent.*;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import org.exoplatform.addon.wallet.blockchain.ExoBlockchainTransaction;
import org.exoplatform.addon.wallet.blockchain.ExoBlockchainTransactionService;
import org.exoplatform.addon.wallet.blockchain.listener.BlockchainTransactionProcessorListener;
import org.exoplatform.addon.wallet.job.ContractTransactionVerifierJob;
import org.exoplatform.addon.wallet.job.PendingTransactionVerifierJob;
import org.exoplatform.addon.wallet.service.BlockchainTransactionService;
import org.exoplatform.addon.wallet.service.WalletTokenAdminService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.scheduler.CronJob;
import org.exoplatform.services.scheduler.JobSchedulerService;

/**
 * A Servlet added to replace old bouncy castle provider loaded in parent class
 * loader by a new one that defines more algorithms and a newer implementation.
 * Workaround for PLF-8123
 */
public class ServiceLoaderServlet extends HttpServlet implements ExoBlockchainTransactionService {

  private static final long                     serialVersionUID = 4629318431709644350L;

  private static final Log                      LOG              = ExoLogger.getLogger(ServiceLoaderServlet.class);

  private static final ScheduledExecutorService executor         = Executors.newScheduledThreadPool(1);

  @Override
  public void init() throws ServletException {
    executor.scheduleAtFixedRate(this::instantiateBlockchainServices, 10, 10, TimeUnit.SECONDS);
  }

  @ExoBlockchainTransaction
  private void instantiateBlockchainServices() {
    PortalContainer container = PortalContainer.getInstance();
    if (container == null || !container.isStarted()) {
      LOG.debug("Portal Container is not yet started");
      return;
    }
    ExoContainerContext.setCurrentContainer(container);
    RequestLifeCycle.begin(container);
    try {
      // Replace old bouncy castle provider by the newer version
      ClassLoader webappClassLoader = getWebappClassLoader();
      Class<?> class1 = webappClassLoader.loadClass(BouncyCastleProvider.class.getName());
      Provider provider = (Provider) class1.newInstance();
      Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
      Security.addProvider(provider);
      provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
      LOG.info("BouncyCastleProvider class registered with version {}",
               provider.getVersion());

      // start connection to blockchain
      EthereumClientConnector web3jConnector = new EthereumClientConnector(webappClassLoader);
      container.registerComponentInstance(EthereumClientConnector.class, web3jConnector);
      web3jConnector.start();

      // Blockchain transaction decoder
      EthereumBlockchainTransactionService transactionDecoderService = new EthereumBlockchainTransactionService(web3jConnector,
                                                                                                                webappClassLoader);
      container.registerComponentInstance(BlockchainTransactionService.class,
                                          transactionDecoderService);

      // Instantiate service with current webapp classloader
      EthereumWalletTokenAdminService tokenAdminService = new EthereumWalletTokenAdminService(web3jConnector, webappClassLoader);
      container.registerComponentInstance(WalletTokenAdminService.class, tokenAdminService);
      tokenAdminService.start();

      ListenerService listernerService = CommonsUtils.getService(ListenerService.class);
      listernerService.addListener(NEW_TRANSACTION_EVENT, new BlockchainTransactionProcessorListener(container));

      addBlockchainScheduledJob(PendingTransactionVerifierJob.class,
                                "Configuration for wallet transaction stored status verifier",
                                "0/10 * * * * ?");
      addBlockchainScheduledJob(ContractTransactionVerifierJob.class,
                                "Add a job to verify if mined contract transactions are added in database",
                                "0 0 * ? * * *");

      LOG.debug("Blockchain Service instances created");
    } catch (Throwable e) {
      LOG.error("Error registering service into portal container", e);
    } finally {
      RequestLifeCycle.end();
    }
    executor.shutdown();
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    this.init();
    super.service(req, resp);
  }

  private void addBlockchainScheduledJob(Class<?> jobClass, String description, String defaultCronExpression) throws Exception {
    JobSchedulerService schedulerService = CommonsUtils.getService(JobSchedulerService.class);
    String jobSimpleName = jobClass.getSimpleName();
    InitParams params = new InitParams();
    PropertiesParam propertiesParam = new PropertiesParam();
    propertiesParam.setName("cronjob.info");
    propertiesParam.setDescription(description);
    propertiesParam.setProperty("jobName", jobSimpleName);
    propertiesParam.setProperty("groupName", "Wallet");
    propertiesParam.setProperty("job", jobClass.getName());
    propertiesParam.setProperty("expression",
                                System.getProperty("exo.wallet." + jobSimpleName + ".expression", defaultCronExpression));
    params.addParam(propertiesParam);
    CronJob cronJob = new CronJob(params);
    schedulerService.addCronJob(cronJob);
  }

  @Override
  public ClassLoader getWebappClassLoader() {
    return getServletContext().getClassLoader();
  }
}
