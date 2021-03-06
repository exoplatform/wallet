<template>
  <v-flex flat>
    <confirm-dialog
      ref="informationModal"
      :loading="loading"
      :title="informationTitle"
      :message="informationMessage"
      :hide-default-footer="hideConfirmActions"
      width="400px"
      @ok="proceessAction" />
    <div v-if="error" class="alert alert-error v-content">
      <i class="uiIconError"></i>{{ error }}
    </div>
    <div v-if="!loading && !useWalletAdmin" class="red--text wrap title">
      <div>{{ $t('exoplatform.wallet.warning.adminWalletNotInitializedPart1') }}</div>
      <div>{{ $t('exoplatform.wallet.warning.adminWalletNotInitializedPart2') }}</div>
    </div>
    <admin-wallet
      v-if="!loading"
      :admin-wallet="walletAdmin"
      :initial-token-amount="tokenAmount"
      :contract-details="contractDetails"
      @refresh-balance="refreshWallet(walletAdmin, true)" />
    <v-layout
      row
      wrap
      class="border-box-sizing mx-0">
      <v-flex
        md4
        xs12
        class="mt-2 border-box-sizing">
        <v-btn-toggle
          v-model="walletTypes"
          class="walletFilterButtons"
          mandatory
          multiple
          text>
          <v-btn value="user">
            {{ $t('exoplatform.wallet.label.users') }}
          </v-btn>
          <v-btn value="space">
            {{ $t('exoplatform.wallet.label.spaces') }}
          </v-btn>
        </v-btn-toggle>
      </v-flex>
      <v-flex
        md3
        offset-md1
        offset-xs0
        xs12
        class="mt-2 border-box-sizing">
        <v-btn-toggle
          v-model="walletStatuses"
          class="walletFilterButtons"
          multiple
          text>
          <v-btn value="disabled">
            {{ $t('exoplatform.wallet.label.disabled') }}
          </v-btn>
          <v-btn value="disapproved">
            {{ $t('exoplatform.wallet.label.disapproved') }}
          </v-btn>
          <v-btn value="deletedIdentity">
            {{ $t('exoplatform.wallet.label.deleted') }}
          </v-btn>
        </v-btn-toggle>
      </v-flex>
      <v-flex
        md3
        offset-md1
        offset-xs0
        xs12
        class="mt-2 border-box-sizing">
        <v-text-field
          v-model="search"
          :label="$t('exoplatform.wallet.label.searchInWalletPlaceholder')"
          append-icon="search"
          class="pt-0 mt-0" />
      </v-flex>
    </v-layout>
    <v-data-table
      :headers="walletTableHeaders"
      :items="filteredWallets"
      :items-per-page="1000"
      :loading="loadingWallets"
      hide-default-footer>
      <template slot="item" slot-scope="props">
        <transition name="fade">
          <tr v-show="props.item.displayedWallet">
            <td class="clickable" @click="openAccountDetail(props.item)">
              <v-avatar size="36px">
                <img
                  v-if="props.item.avatar"
                  :src="props.item.avatar"
                  onerror="this.src = '/eXoSkin/skin/images/system/SpaceAvtDefault.png'">
                <v-icon v-else size="36">fa-cog</v-icon>
              </v-avatar>
            </td>
            <td class="clickable text-left" @click="openAccountDetail(props.item)">
              <profile-chip
                :address="props.item.address"
                :profile-id="props.item.id"
                :profile-technical-id="props.item.technicalId"
                :space-id="props.item.spaceId"
                :profile-type="props.item.type"
                :display-name="props.item.name"
                :enabled="props.item.enabled"
                :disapproved="!props.item.isApproved"
                :deleted-user="props.item.deletedUser"
                :disabled-user="props.item.disabledUser"
                :avatar="props.item.avatar"
                display-no-address
                no-status />
            </td>
            <td class="clickable text-center" @click="openAccountDetail(props.item)">
              <template>
                <template v-if="props.item.deletedUser">{{ $t('exoplatform.wallet.label.deletedIdentity') }}</template>
                <template v-else-if="props.item.disabledUser">{{ $t('exoplatform.wallet.label.disabledUser') }}</template>
                <template v-else-if="!props.item.enabled">{{ $t('exoplatform.wallet.label.disabledWallet') }}</template>
                <template v-else-if="props.item.initializationState === 'NEW'">{{ $t('exoplatform.wallet.label.newWallet') }}</template>
                <template v-else-if="props.item.initializationState === 'DENIED'">{{ $t('exoplatform.wallet.label.rejectedWallet') }}</template>
                <template v-else-if="!props.item.isApproved">{{ $t('exoplatform.wallet.label.disapprovedWallet') }}</template>
                <template v-else>
                  <template v-if="Number(props.item.etherBalance) === 0 || (etherAmount && walletUtils.toFixed(props.item.etherBalance) < Number(etherAmount))">
                    <v-icon color="orange">
                      warning
                    </v-icon>
                    {{ $t('exoplatform.wallet.warning.lowEtherAmount') }}
                  </template>
                  <template v-else-if="Number(props.item.tokenBalance) === 0">
                    <v-icon color="orange">
                      warning
                    </v-icon>
                    {{ $t('exoplatform.wallet.warning.lowTokenAmount', {0: contractDetails && contractDetails.name}) }}
                  </template>
                  <template v-else>
                    {{ $t('exoplatform.wallet.label.initializedWallet') }}
                  </template>
                </template>
              </template>
            </td>
            <td
              v-if="contractDetails"
              class="clickable text-center"
              @click="openAccountDetail(props.item)">
              <v-progress-circular
                v-if="props.item.loading"
                color="primary"
                class="mr-4"
                indeterminate
                size="20" />
              <template v-else>
                {{ walletUtils.toFixed(props.item.tokenBalance) || 0 }} {{ contractDetails && contractDetails.symbol ? contractDetails.symbol : '' }}
              </template>
            </td>
            <td class="clickable text-center" @click="openAccountDetail(props.item)">
              <v-progress-circular
                v-if="props.item.loading"
                color="primary"
                class="mr-4"
                indeterminate
                size="20" />
              <template v-else>
                {{ walletUtils.toFixed(props.item.etherBalance) || 0 }} eth
              </template>
            </td>
            <td
              v-if="contractDetails && contractDetails.contractType && contractDetails.contractType > 1"
              class="clickable"
              @click="openAccountDetail(props.item)">
              <template v-if="props.item.type === 'user' || props.item.type === 'space'">
                <v-icon
                  v-if="props.item.initializationState === 'NEW'"
                  :title="$t('exoplatform.wallet.label.newWallet')"
                  color="primary">
                  fa-hands
                </v-icon>
                <v-icon
                  v-else-if="props.item.initializationState === 'MODIFIED'"
                  :title="$t('exoplatform.wallet.label.modifiedWallet')"
                  color="orange">
                  fa-hands
                </v-icon>
                <v-icon
                  v-else-if="props.item.initializationState === 'DENIED'"
                  :title="$t('exoplatform.wallet.label.deniedWallet')"
                  color="orange">
                  fa-times-circle
                </v-icon>
                <v-icon
                  v-else-if="props.item.initializationState === 'PENDING'"
                  :title="$t('exoplatform.wallet.label.initializationInProgressForWallet')"
                  color="primary">
                  fa-dot-circle
                </v-icon>
                <v-icon
                  v-else-if="props.item.initializationState === 'INITIALIZED'"
                  :title="$t('exoplatform.wallet.label.initializedWallet')"
                  color="green">
                  fa-check-circle
                </v-icon>
              </template>
            </td>
            <td class="text-center">
              <v-progress-circular
                v-if="props.item.pendingTransaction || props.item.loading"
                :title="$t('exoplatform.wallet.message.transactionInProgress')"
                color="primary"
                class="mr-4"
                indeterminate
                size="20" />
              <v-menu v-else offset-y>
                <template v-slot:activator="{ on }">
                  <v-btn
                    icon
                    small
                    v-on="on">
                    <v-icon size="20px">fa-ellipsis-v</v-icon>
                  </v-btn>
                </template>
                <v-list flat class="pt-0 pb-0">
                  <template>
                    <v-list-item @click="refreshWallet(props.item, true)">
                      <v-list-item-title>{{ $t('exoplatform.wallet.button.refreshWallet') }}</v-list-item-title>
                    </v-list-item>
                    <v-divider />
                    <template v-if="(props.item.type === 'user' || props.item.type === 'space')">
                      <template v-if="useWalletAdmin">
                        <template v-if="contractDetails && contractDetails.contractType && contractDetails.contractType > 1 && (props.item.initializationState === 'NEW' || props.item.initializationState === 'MODIFIED' || props.item.initializationState === 'DENIED') && !props.item.disabledUser && !props.item.deletedUser && props.item.enabled">
                          <v-list-item :disabled="adminNotHavingEnoughToken" @click="openAcceptInitializationModal(props.item)">
                            <v-list-item-title>{{ $t('exoplatform.wallet.button.initializeWallet') }}</v-list-item-title>
                          </v-list-item>
                          <v-list-item v-if="props.item.initializationState !== 'DENIED'" @click="openDenyInitializationModal(props.item)">
                            <v-list-item-title>{{ $t('exoplatform.wallet.button.rejectWallet') }}</v-list-item-title>
                          </v-list-item>
                          <v-divider />
                        </template>
                        <template v-else-if="props.item.isApproved && !props.item.disabledUser && !props.item.deletedUser && props.item.enabled && (Number(props.item.etherBalance) === 0 || (etherAmount && walletUtils.toFixed(props.item.etherBalance) < Number(etherAmount)))">
                          <v-list-item :disabled="adminNotHavingEnoughEther" @click="openSendEtherModal(props.item)">
                            <v-list-item-title>{{ $t('exoplatform.wallet.button.sendEther') }}</v-list-item-title>
                          </v-list-item>
                          <v-divider />
                        </template>
                        <v-list-item
                          v-if="contractDetails && !contractDetails.isPaused && !props.item.disabledUser && !props.item.deletedUser && props.item.enabled && props.item.isApproved && tokenAmount > 0"
                          :disabled="adminNotHavingEnoughToken"
                          @click="openSendTokenModal(props.item)">
                          <v-list-item-title>{{ $t('exoplatform.wallet.button.sendToken', {0: contractDetails && contractDetails.name}) }}</v-list-item-title>
                        </v-list-item>
                        <v-divider />
                      </template>

                      <v-list-item v-if="props.item.enabled" @click="openDisableWalletModal(props.item)">
                        <v-list-item-title>{{ $t('exoplatform.wallet.button.disableWallet') }}</v-list-item-title>
                      </v-list-item>
                      <v-list-item v-else-if="!props.item.disabledUser && !props.item.deletedUser" @click="enableWallet(props.item, true)">
                        <v-list-item-title>{{ $t('exoplatform.wallet.button.enableWallet') }}</v-list-item-title>
                      </v-list-item>
                    </template>
                  </template>
                </v-list>
              </v-menu>
            </td>
          </tr>
        </transition>
      </template>
    </v-data-table>
    <v-flex v-if="showLoadMore" justify-center>
      <v-btn
        :loading="loading"
        color="primary"
        text
        @click="limit += pageSize">
        {{ $t('exoplatform.wallet.button.loadMore') }}
      </v-btn>
    </v-flex>

    <initialize-account-modal
      ref="initAccountModal"
      @sent="walletPendingTransaction" />

    <send-token-modal
      ref="sendTokenModal"
      :contract-details="contractDetails"
      @sent="walletPendingTransaction" />
    <send-ether-modal
      ref="sendEtherModal"
      @sent="walletPendingTransaction" />

    <!-- The selected account detail -->
    <v-navigation-drawer
      id="accountDetailsDrawer"
      v-model="seeAccountDetails"
      fixed
      temporary
      right
      stateless
      width="700"
      max-width="100vw">
      <account-detail
        ref="accountDetail"
        :fiat-symbol="fiatSymbol"
        :wallet="selectedWallet"
        :contract-details="selectedWalletDetails"
        :selected-transaction-hash="selectedTransactionHash"
        is-read-only
        is-display-only
        is-administration
        @back="back()" />
    </v-navigation-drawer>
  </v-flex>
</template>

<script>
import InitializeAccountModal from './modals/WalletAdminInitializeAccountModal.vue';
import SendEtherModal from './modals/WalletAdminSendEtherModal.vue';
import SendTokenModal from './modals/WalletAdminSendTokenModal.vue';
import AdminWallet from './AdminWallet.vue';

export default {
  components: {
    InitializeAccountModal,
    SendEtherModal,
    SendTokenModal,
    AdminWallet,
  },
  props: {
    loading: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    fiatSymbol: {
      type: String,
      default: function() {
        return null;
      },
    },
    walletAddress: {
      type: String,
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
    contractDetails: {
      type: Object,
      default: function() {
        return null;
      },
    },
  },
  data() {
    return {
      search: null,
      loadingWallets: false,
      selectedTransactionHash: null,
      seeAccountDetails: false,
      seeAccountDetailsPermanent: false,
      selectedWalletAddress: null,
      selectedWallet: null,
      selectedWalletDetails: null,
      informationTitle: null,
      informationMessage: null,
      hideConfirmActions: null,
      confirmAction: null,
      error: null,
      wallets: [],
      walletToProcess: null,
      etherAmount: null,
      tokenAmount: null,
      limit: 10,
      pageSize: 10,
      walletTypes: ['user'],
      walletStatuses: ['disapproved'],
    };
  },
  computed: {
    walletHeaders() {
      return [
        {
          text: '',
          align: 'center',
          sortable: false,
          value: 'avatar',
        },
        {
          text: this.$t('exoplatform.wallet.label.name'),
          align: 'left',
          sortable: true,
          value: 'name',
        },
        {
          text: this.$t('exoplatform.wallet.label.walletStatus'),
          align: 'center',
          sortable: false,
          value: 'isApproved',
        },
        {
          text: this.$t('exoplatform.wallet.label.tokenBalance', {0: this.contractDetails && this.contractDetails.name}),
          align: 'center',
          value: 'tokenBalance',
        },
        {
          text: this.$t('exoplatform.wallet.label.etherBalance'),
          align: 'center',
          value: 'etherBalance',
        },
        {
          text: this.$t('exoplatform.wallet.label.initializationstatus'),
          align: 'center',
          sortable: true,
          value: 'initializationState',
        },
        {
          text: '',
          align: 'center',
          sortable: false,
          value: '',
        },
      ];
    },
    walletAdmin() {
      return this.wallets && this.wallets.find(wallet => wallet && wallet.type === 'admin');
    },
    adminNotHavingEnoughToken() {
      return this.walletAdmin && this.walletAdmin.tokenBalance < this.tokenAmount;
    },
    adminNotHavingEnoughEther() {
      return this.walletAdmin && this.walletAdmin.etherBalance < this.etherAmount;
    },
    useWalletAdmin() {
      return this.walletAdmin && this.walletAdmin.adminLevel >= 2 && this.walletAdmin.etherBalance && Number(this.walletAdmin.etherBalance) >= 0.002 && this.walletAdmin.tokenBalance && Number(this.walletAdmin.tokenBalance) >= 0.02;
    },
    displayUsers() {
      return this.walletTypes && this.walletTypes.includes('user');
    },
    displaySpaces() {
      return this.walletTypes && this.walletTypes.includes('space');
    },
    displayAdmin() {
      return this.walletTypes && this.walletTypes.includes('admin');
    },
    displayDisapprovedWallets() {
      return this.walletStatuses && this.walletStatuses.includes('disapproved');
    },
    displayDisabledWallets() {
      return this.walletStatuses && this.walletStatuses.includes('disabled');
    },
    displayDeletedIdentities() {
      return this.walletStatuses && this.walletStatuses.includes('deletedIdentity');
    },
    etherAccountDetails() {
      return {
        title: 'ether',
        icon: 'ether',
        symbol: 'ether',
        isContract: false,
        balance: 10, //  Fake balance to not display unsufficient funds
        address: this.walletAddress,
      };
    },
    walletTableHeaders() {
      const walletTableHeaders = this.walletHeaders.slice();
      if (!this.contractDetails) {
        walletTableHeaders.splice(4, 1);
      }
      if (!this.contractDetails || this.contractDetails.contractType < 2) {
        walletTableHeaders.splice(2, 1);
      }
      return walletTableHeaders;
    },
    showLoadMore() {
      return this.displayedWallets.length === this.limit;
    },
    displayedWallets() {
      return this.wallets.filter(this.isDisplayWallet).slice(0, this.limit);
    },
    filteredWallets() {
      if(this.displayedWallets && this.displayedWallets.length) {
        const lastElement = this.displayedWallets[this.displayedWallets.length - 1];
        const limit = this.wallets.findIndex(wallet => wallet.technicalId === lastElement.technicalId) + 1;
        // Set 'displayedWallet' attribute on wallets, used to know whether to retrieve
        // wallet data from blockchain or not
        this.wallets.forEach((wallet, index) => {
          wallet.displayedWallet = index < limit && this.displayedWallets.filter(displayedWallet => displayedWallet.technicalId === wallet.technicalId) && true;
        });
        return this.displayedWallets.slice(0, limit);
      } else {
        return [];
      }
    }
  },
  watch: {
    seeAccountDetails() {
      if (this.seeAccountDetails) {
        $('body').addClass('hide-scroll');
        const thiss = this;
        setTimeout(() => {
          thiss.seeAccountDetailsPermanent = true;
        }, 200);
      } else {
        $('body').removeClass('hide-scroll');
        this.seeAccountDetailsPermanent = false;
      }
    },
  },
  methods: {
    init() {
      if(this.loadingWallets) {
        return;
      }

      const initialFunds = window.walletSettings.initialFunds || {};
      this.etherAmount = initialFunds.etherAmount || 0;
      this.tokenAmount = initialFunds.tokenAmount || 0;

      return this.walletUtils.getWallets()
        .then((wallets) => {
          wallets.forEach((wallet) => {
            wallet.loading = true;
          });

          this.wallets = wallets.sort(this.sortByName);
          // *async* approval retrieval
          this.wallets.forEach((wallet) => {
            wallet.fiatBalance = wallet.fiatBalance || (wallet.etherBalance && this.walletUtils.etherToFiat(wallet.etherBalance));
            wallet.loading = false;
          });
          return this.$nextTick();
        })
        .then(() => {
          this.$emit('wallets-loaded', this.wallets);
        })
        .finally(() => {
          this.loadingWallets = false;
        });
    },
    sortByName(a, b) {
      // To use same Vuetify datable sort algorithm
      const sortA = a.name && a.name.toLocaleLowerCase();
      const sortB = b.name && b.name.toLocaleLowerCase();
      return (sortA > sortB && 1) || (sortA < sortB && (-1)) || 0; // NOSONAR
    },
    isDisplayWallet(wallet) {
      return wallet && wallet.address
        && (this.displayUsers || wallet.type !== 'user')
        && (this.displaySpaces || wallet.type !== 'space')
        && (this.displayAdmin || wallet.type !== 'admin')
        && (this.displayDisabledWallets || (wallet.enabled && !wallet.disabledUser))
        && (this.displayDeletedIdentities || !wallet.deletedUser)
        && (this.displayDisapprovedWallets || wallet.isApproved)
        && (!this.search
            || wallet.initializationState.toLowerCase().indexOf(this.search.toLowerCase()) >= 0
            || wallet.name.toLowerCase().indexOf(this.search.toLowerCase()) >= 0
            || wallet.address.toLowerCase().indexOf(this.search.toLowerCase()) >= 0);
    },
    refreshWallet(wallet, refreshOnBlockchain) {
      wallet.loading = true;
      return this.addressRegistry.refreshWallet(wallet, refreshOnBlockchain)
        .then(() => {
          wallet.fiatBalance = wallet.fiatBalance || (wallet.etherBalance && this.walletUtils.etherToFiat(wallet.etherBalance));
        })
        .finally(() => {
          wallet.loading = false;
        });
    },
    openAccountDetail(wallet, hash) {
      this.selectedTransactionHash = hash;
      this.selectedWalletAddress = wallet.address;
      this.selectedWallet = wallet;
      this.selectedWalletDetails = {
        title: 'ether',
        icon: 'fab fa-ethereum',
        symbol: 'ether',
        address: this.selectedWalletAddress,
        balance: wallet.etherBalance,
        balanceFiat: wallet.fiatBalance,
        details: wallet,
      };
      this.seeAccountDetails = true;

      this.$nextTick(() => {
        const thiss = this;
        $('.v-overlay')
          .off('click')
          .on('click', (event) => {
            thiss.back();
          });
      });
    },
    back() {
      this.seeAccountDetails = false;
      this.seeAccountDetailsPermanent = false;
      this.selectedWalletAddress = null;
      this.selectedWalletDetails = null;
    },
    enableWallet(wallet, enable) {
      this.error = null;
      return this.walletUtils.enableWallet(wallet.address, enable)
        .then((result) => {
          if(result) {
            return this.addressRegistry.refreshWallet(wallet);
          } else {
            this.error = (enable && this.$t('exoplatform.wallet.error.errorEnablingWallet', {0: wallet.name})) || this.$t('exoplatform.wallet.error.errorDisablingWallet', {0: wallet.name});
          }
        })
        .catch(e => this.error = String(e));
    },
    walletPendingTransaction(hash) {
      this.$emit('pending', hash);
      this.error = null;
      if (hash) {
        this.$set(this.walletToProcess, 'pendingTransaction', (this.walletToProcess.pendingTransaction || 0) + 1);
        this.watchWalletTransaction(this.walletToProcess, hash);
      }
    },
    openAcceptInitializationModal(wallet) {
      this.walletToProcess = wallet;
      this.$refs.initAccountModal.open(wallet, window.walletSettings.initialFunds.requestMessage, this.etherAmount, this.tokenAmount);
    },
    openSendTokenModal(wallet) {
      this.walletToProcess = wallet;
      this.$refs.sendTokenModal.open(wallet, null, this.tokenAmount);
    },
    openSendEtherModal(wallet) {
      this.walletToProcess = wallet;
      const etherAmount = this.walletUtils.toFixed(this.etherAmount) - Number(wallet && wallet.etherBalance);
      if (etherAmount && etherAmount > 0 && Number.isFinite(etherAmount)) {
        this.$refs.sendEtherModal.open(wallet, null, etherAmount);
      }
    },
    openDenyInitializationModal(wallet) {
      this.walletToProcess = wallet;
      this.informationTitle = this.$t('exoplatform.wallet.title.rejectWalletInitializationConfirmationModal');
      this.informationMessage = this.$t('exoplatform.wallet.message.rejectWalletInitializationConfirmation', {0: wallet.type, 1: `<strong>${wallet.name}</strong>`});
      this.hideConfirmActions = false;
      this.confirmAction = 'deny';
      this.$refs.informationModal.open();
    },
    openDisableWalletModal(wallet) {
      this.walletToProcess = wallet;
      this.informationTitle = this.$t('exoplatform.wallet.title.disableWalletConfirmationModal');
      this.informationMessage = this.$t('exoplatform.wallet.message.disableWalletConfirmation', {0: wallet.type, 1: `<strong>${wallet.name}</strong>`});
      this.hideConfirmActions = false;
      this.confirmAction = 'disable';
      this.$refs.informationModal.open();
    },
    changeWalletInitializationStatus(wallet, status) {
      if(wallet) {
        return this.walletUtils.saveWalletInitializationStatus(wallet.address, status)
          .then(() => {
            return this.refreshWallet(wallet);
          }).catch(e => {
            this.error = String(e);
          });
      }
    },
    watchWalletTransaction(wallet, hash) {
      this.walletUtils.watchTransactionStatus(hash, () => {
        this.$set(wallet, 'pendingTransaction', (wallet.pendingTransaction || 1) - 1);
        this.refreshWallet(wallet);
      });
    },
    proceessAction() {
      if (this.confirmAction === 'disable') {
        this.enableWallet(this.walletToProcess, false);
      } else if (this.confirmAction === 'deny') {
        this.changeWalletInitializationStatus(this.walletToProcess, 'DENIED');
      }
    }
  },
};
</script>