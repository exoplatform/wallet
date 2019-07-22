<template>
  <v-card>
    <v-card-title primary-title>
      <div>
        <h4 class="no-wrap">
          <v-icon small class="mb-1">fa-unlock</v-icon>
          <span>{{ $t('exoplatform.wallet.label.password') }}</span>
          <v-tooltip v-if="!browserWalletExists" bottom>
            <v-icon slot="activator" color="orange">warning</v-icon>
            <span>
              {{ $t('exoplatform.wallet.warning.noPrivateKey') }}
            </span>
          </v-tooltip>
        </h4>
        <h5>{{ $t('exoplatform.wallet.message.securityPasswordManagement') }}</h5>
        <reset-modal
          ref="walletResetModal"
          :button-label="$t('exoplatform.wallet.button.resetWalletPassword')"
          display-remember-me
          @reseted="$emit('settings-changed')" />
      </div>
    </v-card-title>
    <v-card-title class="pt-0 pb-0">
      <div>
        <h4>
          <v-icon small>fa-key</v-icon>
          {{ $t('exoplatform.wallet.label.encryptionKeys') }}
        </h4>
        <h5>{{ $t('exoplatform.wallet.message.encryptionKeysManagement') }}</h5>
        <button class="btn" @click="$emit('manage-keys')">
          <v-icon small>fa-qrcode</v-icon>
          {{ $t('exoplatform.wallet.button.manageKeys') }}
        </button>
        <v-switch
          v-model="hasKeyOnServerSide"
          :disabled="!browserWalletExists"
          :label="$t('exoplatform.wallet.button.keepMyKeysSafeOnServer')"
          @change="$refs.confirmDialog.open()" />
      </div>
    </v-card-title>

    <confirm-dialog
      ref="confirmDialog"
      :loading="loading"
      :message="confirmMessage"
      :title="confirmTitle"
      :ok-label="confirmOkLabel"
      :cancel-label="$t('exoplatform.wallet.button.cancel')"
      @ok="saveKeysOnServer"
      @closed="init" />
  </v-card>
</template>

<script>
export default {
  props: {
    walletAddress: {
      type: String,
      default: function() {
        return null;
      },
    },
    isSpace: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
  },
  data() {
    return {
      loading: false,
      settings: false,
      hasKeyOnServerSide: false,
      browserWalletExists: false,
    };
  },
  computed: {
    confirmMessage() {
      return this.hasKeyOnServerSide ? this.$t('exoplatform.wallet.message.storePrivateKeyOnServer') : this.$t('exoplatform.wallet.message.deletePrivateKeyFromServer');
    },
    confirmTitle() {
      return this.hasKeyOnServerSide ? this.$t('exoplatform.wallet.label.storePrivateKeyOnServer') : this.$t('exoplatform.wallet.label.deletePrivateKeyFromServer');
    },
    confirmOkLabel() {
      return this.hasKeyOnServerSide ? this.$t('exoplatform.wallet.button.save') : this.$t('exoplatform.wallet.button.delete');
    },
  },
  methods: {
    init() {
      this.settings = window.walletSettings || {userPreferences: {}};
      this.hasKeyOnServerSide = this.settings.userPreferences.hasKeyOnServerSide;
      this.browserWalletExists = this.settings.browserWalletExists;
      if (this.$refs.walletResetModal) {
        this.$refs.walletResetModal.init();
      }
    },
    saveKeysOnServer() {
      if (this.hasKeyOnServerSide) {
        return this.walletUtils.sendPrivateKeyToServer(this.walletAddress)
          .then((result, error) => {
            if (error) {
              throw error;
            }
          })
          .catch(error => {
            console.debug("Error occurred", error);
            this.hasKeyOnServerSide = false;
          });
      } else {
        return this.walletUtils.removeServerSideBackup(this.walletAddress);
      }
    },
  }
}
</script>