<template>
  <v-flex id="walletSetup" class="text-center">
    <wallet-backup-modal
      ref="walletBackupModal"
      class="mr-3"
      display-complete-message
      no-button
      @backed-up="$emit('backed-up')" />

    <div v-if="displayWalletCreationToolbar" class="alert alert-info">
      <i class="uiIconInfo"></i> <span v-if="isSpace">
        {{ $t('exoplatform.wallet.warning.notPrivateKey') }}.
        <strong>
          {{ $t('exoplatform.wallet.title.spaceWallet') }}
        </strong> {{ $t('exoplatform.wallet.warning.isReadOnlyMode') }}.
      </span> <span v-else>
        {{ $t('exoplatform.wallet.warning.notPrivateKey') }}.
        <strong>
          {{ $t('exoplatform.wallet.title.yourWallet') }}
        </strong> {{ $t('exoplatform.wallet.warning.isReadOnlyMode') }}.
      </span>
      <a
        v-if="!displayWalletSetup"
        href="javascript:void(0);"
        @click="displayWalletSetupActions()">
        {{ $t('exoplatform.wallet.label.moreOptions') }}
      </a>
    </div>

    <div v-if="displayWalletNotExistingYet" class="alert alert-info">
      <i class="uiIconInfo"></i> {{ $t('exoplatform.wallet.info.spaceWalletNotCreatedYet') }}
    </div>
    <wallet-browser-setup
      v-if="displayWalletSetup"
      ref="walletBrowserSetup"
      :is-space="isSpace"
      :is-space-administrator="isSpaceAdministrator"
      :wallet="wallet"
      :refresh-index="refreshIndex"
      :is-administration="isAdministration"
      :loading="loading"
      :initialization-state="initializationState"
      @configured="refresh()"
      @loading="$emit('loading')"
      @end-loading="$emit('end-loading')"
      @error="$emit('error', $event)" />
  </v-flex>
</template>

<script>
import WalletBrowserSetup from './WalletBrowserSetup.vue';
import WalletBackupModal from './WalletBackupModal.vue';

export default {
  components: {
    WalletBrowserSetup,
    WalletBackupModal,
  },
  props: {
    isSpace: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    loading: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    isAdministration: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    wallet: {
      type: Object,
      default: function() {
        return null;
      },
    },
    initializationState: {
      type: String,
      default: function() {
        return null;
      },
    },
    isMinimized: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
  },
  data() {
    return {
      isReadOnly: false,
      browserWalletExists: false,
      displayWalletSetup: false,
    };
  },
  computed: {
    backedUp () {
      return this.wallet && this.wallet.backedUp;
    },
    displayWalletNotExistingYet() {
      return !this.loading && !this.isAdministration && this.isSpace && !this.isSpaceAdministrator && !this.walletAddress;
    },
    isSpaceAdministrator() {
      return this.wallet && this.wallet.spaceAdministrator
    },
    walletAddress() {
      return this.wallet && this.wallet.address;
    },
    displayWalletCreationToolbar() {
      return !this.loading && this.walletAddress && !this.browserWalletExists && this.isReadOnly && (!this.isSpace || this.isSpaceAdministrator);
    },
    displayWalletBackup() {
      return !this.loading && !this.isAdministration && this.walletAddress && this.browserWalletExists && !this.backedUp;
    },
  },
  watch: {
    refreshIndex() {
      this.init();
    },
    displayWalletBackup() {
      if (this.displayWalletBackup && this.$refs && this.$refs.walletBackupModal) {
        this.$refs.walletBackupModal.dialog = true;
      }
    },
  },
  methods: {
    refresh() {
      this.$emit('refresh');
    },
    init() {
      if (!window.walletSettings) {
        return;
      }
      this.isReadOnly = window.walletSettings.isReadOnly;
      this.browserWalletExists = window.walletSettings.browserWalletExists;
      this.displayWalletSetup = !this.walletAddress && (!this.isSpace || this.isSpaceAdministrator);

      this.$nextTick(() => {
        if(this.$refs.walletBrowserSetup) {
          this.$refs.walletBrowserSetup.init();
        }
      });
    },
    displayWalletSetupActions() {
      this.displayWalletSetup = true;
    },
  },
};
</script>
