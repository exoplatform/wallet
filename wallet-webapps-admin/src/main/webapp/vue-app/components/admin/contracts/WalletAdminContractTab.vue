<template>
  <v-flex
    v-if="contractDetails && contractDetails.title"
    id="accountDetail"
    class="text-center white layout column">
    <v-card-title v-if="adminLevel >= 4" class="align-start accountDetailSummary">
      <v-layout column>
        <v-flex
          id="accountDetailTitle"
          class="mt-3">
          <h3 v-if="adminLevel >= 5 && contractDetails.fiatBalance" class="font-weight-light">
            {{ contractDetails.name }} - {{ $t('exoplatform.wallet.label.version') }}: {{ contractDetails.contractType }} - {{ $t('exoplatform.wallet.label.balance') }}: {{ walletUtils.toFixed(contractDetails.fiatBalance) }} {{ fiatSymbol }} / {{ walletUtils.toFixed(contractDetails.etherBalance) }} ether
          </h3>
          <h4 v-if="adminLevel >= 5" class="grey--text font-weight-light">
            <b>{{ contractDetails && contractDetails.name }}</b> {{ $t('exoplatform.wallet.label.sellPrice') }}: {{ contractDetails && contractDetails.sellPrice }} ether
          </h4>
          <h4 class="grey--text font-weight-light no-wrap">
            {{ $t('exoplatform.wallet.label.owner') }}: <wallet-address :value="contractDetails.owner" display-label />
          </h4>
        </v-flex>

        <v-flex v-if="adminLevel >= 4" id="accountDetailActions">
          <!-- Send ether -->
          <send-ether-modal
            v-if="adminLevel >= 5"
            :account="walletAddress"
            :balance="contractDetails.balance"
            :recipient="contractDetails.address"
            use-navigation
            disabled-recipient
            @success="successSendingEther"
            @sent="newTransactionPending"
            @error="transactionError" />

          <!-- add/remove admin -->
          <contract-admin-modal
            v-if="adminLevel >= 5"
            ref="addAdminModal"
            :contract-details="contractDetails"
            :wallet="userWallet"
            :title="$t('exoplatform.wallet.button.addAdmin')"
            :autocomplete-label="$t('exoplatform.wallet.label.account')"
            :autocomplete-placeholder="$t('exoplatform.wallet.label.addAdminPlaceholder')"
            :input-label="$t('exoplatform.wallet.label.habilitationLevel')"
            :input-placeholder="$t('exoplatform.wallet.label.habilitationLevelPlaceholder')"
            method-name="addAdmin"
            @sent="newTransactionPending"
            @success="successTransaction"
            @error="transactionError">
            <div class="alert alert-info">
              <i class="uiIconInfo"></i>{{ $t('exoplatform.wallet.label.habilitationLevels') }}:
              <ul>
                <li>
                  <strong>
                    {{ $t('exoplatform.wallet.label.level') }} 1
                  </strong>: {{ $t('exoplatform.wallet.message.level1Habilitation') }}
                </li>
                <li>
                  <strong>
                    {{ $t('exoplatform.wallet.label.level') }} 2
                  </strong>: {{ $t('exoplatform.wallet.message.level2Habilitation') }}
                </li>
                <li>
                  <strong>
                    {{ $t('exoplatform.wallet.label.level') }} 3
                  </strong>: {{ $t('exoplatform.wallet.message.level3Habilitation') }}
                </li>
                <li>
                  <strong>
                    {{ $t('exoplatform.wallet.label.level') }} 4
                  </strong>: {{ $t('exoplatform.wallet.message.level4Habilitation') }}
                </li>
                <li>
                  <strong>
                    {{ $t('exoplatform.wallet.label.level') }} 5
                  </strong>: {{ $t('exoplatform.wallet.message.level5Habilitation') }}
                </li>
                <li>
                  <strong>
                    {{ $t('exoplatform.wallet.label.owner') }}
                  </strong>: {{ $t('exoplatform.wallet.message.ownerHabilitation') }}
                </li>
              </ul>
            </div>
          </contract-admin-modal>
          <contract-admin-modal
            v-if="adminLevel >= 5"
            ref="removeAdminModal"
            :contract-details="contractDetails"
            :wallet="userWallet"
            :title="$t('exoplatform.wallet.button.removeAdmin')"
            :autocomplete-label="$t('exoplatform.wallet.label.account')"
            :autocomplete-placeholder="$t('exoplatform.wallet.label.removeAdminPlaceholder')"
            method-name="removeAdmin"
            @sent="newTransactionPending"
            @success="successTransaction"
            @error="transactionError" />

          <!-- approve/disapprove account -->
          <contract-admin-modal
            v-if="adminLevel >= 4"
            ref="approveAccountModal"
            :contract-details="contractDetails"
            :wallet="userWallet"
            :title="$t('exoplatform.wallet.button.approveAccount')"
            :autocomplete-label="$t('exoplatform.wallet.label.account')"
            :autocomplete-placeholder="$t('exoplatform.wallet.label.approveAccountPlaceholder')"
            method-name="approveAccount"
            @sent="newTransactionPending"
            @success="successTransaction"
            @error="transactionError" />
          <contract-admin-modal
            v-if="adminLevel >= 4"
            ref="disapproveAccountModal"
            :contract-details="contractDetails"
            :wallet="userWallet"
            :title="$t('exoplatform.wallet.button.disapproveAccount')"
            :autocomplete-label="$t('exoplatform.wallet.label.account')"
            :autocomplete-placeholder="$t('exoplatform.wallet.label.disapproveAccountPlaceholder')"
            method-name="disapproveAccount"
            @sent="newTransactionPending"
            @success="successTransaction"
            @error="transactionError" />

          <!-- pause/unpause contract -->
          <contract-admin-modal
            v-if="contractDetails && !contractDetails.isPaused && adminLevel >= 5"
            ref="pauseModal"
            :contract-details="contractDetails"
            :wallet="userWallet"
            :title="$t('exoplatform.wallet.button.pauseContract')"
            method-name="pause"
            @sent="newTransactionPending"
            @success="successTransaction"
            @error="transactionError" />
          <contract-admin-modal
            v-if="contractDetails && contractDetails.isPaused && adminLevel >= 5"
            ref="unPauseModal"
            :contract-details="contractDetails"
            :wallet="userWallet"
            :title="$t('exoplatform.wallet.button.unPauseContract')"
            method-name="unPause"
            @sent="newTransactionPending"
            @success="successTransaction"
            @error="transactionError" />

          <!-- set sell price -->
          <contract-admin-modal
            v-if="adminLevel >= 5"
            ref="setSellPriceModal"
            :contract-details="contractDetails"
            :wallet="userWallet"
            :title="$t('exoplatform.wallet.button.setSellPrice')"
            :input-label="$t('exoplatform.wallet.label.sellPriceOfToken', {0: `${contractDetails && contractDetails.name}`})"
            :input-placeholder="$t('exoplatform.wallet.label.sellPriceOfTokenInEther', {0: `${contractDetails && contractDetails.name}`})"
            method-name="setSellPrice"
            convert-wei
            @sent="newTransactionPending"
            @success="successTransaction"
            @error="transactionError" />

          <contract-admin-modal
            v-if="contractDetails && contractDetails.isOwner"
            ref="transferOwnership"
            :contract-details="contractDetails"
            :wallet="userWallet"
            :title="$t('exoplatform.wallet.button.transferOwnership')"
            :autocomplete-label="$t('exoplatform.wallet.label.newTokenOwner')"
            :autocomplete-placeholder="$t('exoplatform.wallet.label.newTokenOwnerPlaceholder')"
            method-name="transferOwnership"
            @sent="newTransactionPending"
            @success="successTransaction"
            @error="transactionError" />

          <initialize-account-modal
            v-if="adminLevel >= 5"
            ref="initializeAccount"
            :contract-details="contractDetails"
            :wallet="userWallet"
            method-name="transferOwnership"
            @sent="newTransactionPending"
            @success="successTransaction"
            @error="transactionError" />

          <upgrade-token-modal
            v-if="contractDetails && contractDetails.isOwner && contractDetails.contractType > 0 && contractDetails.contractType < 3"
            ref="upgrade"
            :contract-details="contractDetails"
            :wallet="userWallet"
            :implementation-version="3"
            @sent="newTransactionPending"
            @success="successTransaction"
            @error="transactionError" />
        </v-flex>
      </v-layout>
    </v-card-title>

    <v-tabs v-model="selectedTab" grow>
      <v-tabs-slider v-if="adminLevel >= 4" color="primary" />
      <v-tab
        v-if="adminLevel >= 4"
        key="transactions"
        href="#transactions">
        {{ $t('exoplatform.wallet.title.transactions') }}{{ totalTransactionsCount ? ` (${totalTransactionsCount})` : '' }}
      </v-tab>
      <v-tab
        v-if="contractDetails.contractType > 0 && adminLevel >= 4"
        key="approvedAccounts"
        href="#approvedAccounts">
        {{ $t('exoplatform.wallet.title.approvedAccounts') }}
      </v-tab>
      <v-tab
        v-if="contractDetails.contractType > 0 && adminLevel >= 5"
        key="adminAccounts"
        href="#adminAccounts">
        {{ $t('exoplatform.wallet.title.adminAccounts') }}
      </v-tab>
    </v-tabs>
    <v-tabs-items v-model="selectedTab">
      <v-tab-item
        id="transactions"
        value="transactions"
        eager>
        <v-layout column>
          <v-flex xs12>
            <transactions-list
              id="transactionsList"
              ref="transactionsList"
              :contract-details="contractDetails"
              :fiat-symbol="fiatSymbol"
              :error="error"
              display-full-transaction
              administration
              @loaded="computeTransactionsCount"
              @error="error = $event" />
          </v-flex>
        </v-layout>
      </v-tab-item>
      <v-tab-item
        v-if="contractDetails && contractDetails.contractType > 0 && adminLevel >= 4"
        id="approvedAccounts"
        value="approvedAccounts"
        eager>
        <v-flex v-if="!loading" justify-center>
          <v-btn
            :loading="loadingApprovedWalletsFromContract"
            color="primary"
            text
            @click="loadApprovedWalletsFromContract">
            {{ $t('exoplatform.wallet.button.loadFromBlockchain') }}
          </v-btn>
        </v-flex>
        <v-data-table
          v-if="contractDetails"
          :items="approvedWallets"
          :items-per-page="1000"
          :loading="loadingApprovedWalletsFromContract"
          no-data-text=""
          hide-default-footer
          hide-default-header>
          <template slot="item" slot-scope="props">
            <tr v-if="props.item.approved || props.item.adminLevel >= 1">
              <td>
                <v-avatar size="36px">
                  <img :src="props.item.avatar ? props.item.avatar : '/eXoSkin/skin/images/system/UserAvtDefault.png'" onerror="this.src = '/eXoSkin/skin/images/system/UserAvtDefault.png'">
                </v-avatar>
              </td>
              <td>
                <profile-chip
                  :address="props.item.address"
                  :profile-id="props.item.id"
                  :profile-technical-id="props.item.technicalId"
                  :space-id="props.item.spaceId"
                  :profile-type="props.item.type"
                  :display-name="props.item.name"
                  :enabled="props.item.enabled"
                  :deleted-user="props.item.deletedUser"
                  :disabled-user="props.item.disabledUser"
                  :avatar="props.item.avatar" />
              </td>
              <td v-if="$refs.disapproveAccountModal">
                <span v-if="props.item.adminLevel >= 1">
                  {{ $t('exoplatform.wallet.label.adminLevel') }}: {{ props.item.adminLevel }}
                </span>
                <v-btn
                  v-else
                  icon
                  right
                  @click="$refs.disapproveAccountModal.preselectAutocomplete(props.item.id, props.item.type, props.item.address)">
                  <v-icon>
                    close
                  </v-icon>
                </v-btn>
              </td>
            </tr>
          </template>
        </v-data-table>
      </v-tab-item>
      <v-tab-item
        v-if="contractDetails.contractType > 0 && adminLevel >= 5"
        id="adminAccounts"
        value="adminAccounts"
        eager>
        <v-flex v-if="!loading" justify-center>
          <v-btn
            :loading="loadingAdminWalletsFromContract"
            color="primary"
            text
            @click="loadAdminWalletsFromContract">
            {{ $t('exoplatform.wallet.button.loadFromBlockchain') }}
          </v-btn>
        </v-flex>
        <v-data-table
          v-if="contractDetails"
          :items="adminWallets"
          :items-per-page="1000"
          :loading="loadingAdminWalletsFromContract"
          no-data-text=""
          hide-default-footer
          hide-default-header>
          <template slot="item" slot-scope="props">
            <tr v-if="props.item.adminLevel >= 1">
              <td>
                <v-avatar size="36px">
                  <img :src="props.item.avatar ? props.item.avatar : '/eXoSkin/skin/images/system/UserAvtDefault.png'" onerror="this.src = '/eXoSkin/skin/images/system/UserAvtDefault.png'">
                </v-avatar>
              </td>
              <td>
                <profile-chip
                  :address="props.item.address"
                  :profile-id="props.item.id"
                  :profile-technical-id="props.item.technicalId"
                  :space-id="props.item.spaceId"
                  :profile-type="props.item.type"
                  :display-name="props.item.name"
                  :enabled="props.item.enabled"
                  :deleted-user="props.item.deletedUser"
                  :disabled-user="props.item.disabledUser"
                  :avatar="props.item.avatar" />
              </td>
              <td>
                {{ $t('exoplatform.wallet.label.level') }} {{ props.item.adminLevel }}
              </td>
              <td v-if="$refs.removeAdminModal">
                <v-btn
                  icon
                  right
                  @click="$refs.removeAdminModal.preselectAutocomplete(props.item.id, props.item.type, props.item.address)">
                  <v-icon>
                    close
                  </v-icon>
                </v-btn>
              </td>
            </tr>
          </template>
        </v-data-table>
      </v-tab-item>
    </v-tabs-items>
  </v-flex>
</template>

<script>
import ContractAdminModal from '../common/WalletAdminOperationModal.vue';
import InitializeAccountModal from './modals/WalletAdminInitializeModal.vue';
import UpgradeTokenModal from './modals/WalletAdminUpgradeTokenModal.vue';

export default {
  components: {
    ContractAdminModal,
    InitializeAccountModal,
    UpgradeTokenModal,
  },
  props: {
    walletAddress: {
      type: String,
      default: function() {
        return null;
      },
    },
    userWallet: {
      type: Object,
      default: function() {
        return null;
      },
    },
    contractDetails: {
      type: Object,
      default: function() {
        return null;
      },
    },
    adminLevel: {
      type: Number,
      default: function() {
        return null;
      },
    },
    fiatSymbol: {
      type: String,
      default: function() {
        return null;
      },
    },
    wallets: {
      type: Array,
      default: function() {
        return null;
      },
    },
    addressEtherscanLink: {
      type: String,
      default: function() {
        return null;
      },
    },
  },
  data() {
    return {
      selectedTab: -1,
      totalTransactionsCount: 0,
      approvedWallets: [],
      adminWallets: [],
      checkingPendingTransactions: false,
      approvedWalletsLoadedFromContract: false,
      loadingApprovedWalletsFromContract: false,
      loadingAdminWalletsFromContract: false,
      error: null,
    };
  },
  computed: {
    totalSupply() {
      return this.contractDetails && this.contractDetails.totalSupply ? this.walletUtils.convertTokenAmountReceived(this.contractDetails.totalSupply, this.contractDetails.decimals) : 0;
    },
  },
  created() {
    this.$nextTick(() => this.selectedTab = 'transactions');
  },
  methods: {
    refreshTransactionList() {
      if (this.$refs.transactionsList) {
        this.$refs.transactionsList.init();
      }
    },
    successSendingEther() {
      this.refreshBalance()
        .then(() => {
          this.$emit('success');
          this.$forceUpdate();
        });
    },
    retrieveAccountDetails(wallet, ignoreApproved, ignoreAdmin) {
      const promises = [];
      if (!ignoreApproved && !wallet.hasOwnProperty('approved')) {
        promises.push(
          this.contractDetails.contract.methods
            .isApprovedAccount(wallet.address)
            .call()
            .then((approved) => {
              this.$set(wallet, 'approved', approved);
            })
            .catch((e) => {
              console.debug('Error getting approval of account', wallet.address, e);
              delete wallet['approved'];
            })
        );
      }
      if (!ignoreAdmin && !wallet.hasOwnProperty('adminLevel')) {
        promises.push(
          this.contractDetails.contract.methods
            .getAdminLevel(wallet.address)
            .call()
            .then((level) => {
              level = Number(level);
              this.$set(wallet, 'adminLevel', level);
            })
            .catch((e) => {
              console.debug('Error getting admin level of account', wallet.address, e);
              delete wallet['adminLevel'];
            })
        );
      }
      return Promise.all(promises);
    },
    newTransactionPending(transaction) {
      this.$emit('pending-transaction', transaction);
      if (this.$refs.transactionsList) {
        this.$refs.transactionsList.init(true);
      }
      if (this.contractDetails && transaction.value > 0) {
        this.$set(this.contractDetails, 'loadingBalance', true);
      }
      this.$forceUpdate();
    },
    transactionError(error) {
      this.error = String(error);
      this.$forceUpdate();
    },
    computeTransactionsCount(transactions, count) {
      this.totalTransactionsCount = count;
    },
    refreshBalance() {
      return this.tokenUtils.getSavedContractDetails(this.contractDetails.address).then((contractDetails) => {
        Object.assign(this.contractDetails, contractDetails);
      })
      .finally(() => {
        this.$set(this.contractDetails, 'loadingBalance', false);
      });
    },
    loadApprovedWalletsFromContract() {
      this.approvedWalletsLoadedFromContract = true;
      this.loadWalletsFromContract('ApprovedAccount', 'target', this.approvedWallets)
      .then((result, error) => {
        this.approvedWalletsLoadedFromContract = false;
      });
    },
    loadAdminWalletsFromContract() {
      this.loadWalletsFromContract('AddedAdmin', 'target', this.adminWallets);
    },
    loadWalletsFromContract(eventName, paramName, walletsArray) {
      this.loadingApprovedWalletsFromContract = true;
      this.loadingAdminWalletsFromContract = true;
      try {
        return this.contractDetails.contract.getPastEvents(eventName, {
          fromBlock: 0,
          toBlock: 'latest',
          filter: {
            isError: 0,
            txreceipt_status: 1,
          },
        })
          .then((events) => {
            if(events && events.length) {
              const promises = [];
              events.forEach((event) => {
                if (event.returnValues && event.returnValues[paramName]) {
                  const address = event.returnValues[paramName].toLowerCase();
                  let wallet = this.wallets.find(walletTmp => walletTmp.address && walletTmp.address.toLowerCase() === address);
                  if (!wallet) {
                    wallet = {
                        address: address,
                    };
                  }
                  if(this.contractDetails.owner && address.toLowerCase() === this.contractDetails.owner.toLowerCase()) {
                    wallet.owner = true;
                    wallet.name = "Admin";
                  }
                  walletsArray.unshift(wallet);
                  promises.push(
                      Promise.resolve(this.retrieveAccountDetails(wallet))
                  );
                }
              });
              return Promise.all(promises);
            }
          })
          .catch((e) => {
            console.debug('Error loading wallets from Contract', e);
          })
          .finally(() => {
            this.loadingApprovedWalletsFromContract = false;
            this.loadingAdminWalletsFromContract = false;
          });
      } catch(e) {
        this.loadingApprovedWalletsFromContract = false;
        this.loadingAdminWalletsFromContract = false;
        console.debug('Error loading wallets from Contract', e);
        return Promise.reject(e);
      }
    },
    successTransaction(hash, methodName, autoCompleteValue, inputValue) {
      if(methodName === 'disapproveAccount') {
        const index = this.approvedWallets.findIndex(wallet => wallet.address === autoCompleteValue);
        if(index >= 0) {
          this.approvedWallets.splice(index, 1);
        }
      }
    },
  },
};
</script>
