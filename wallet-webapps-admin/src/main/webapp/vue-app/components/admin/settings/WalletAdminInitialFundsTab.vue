<template>
  <v-card
    class="text-center pr-3 pl-3 pt-2"
    flat>
    <v-card-title class="subtitle-1">
      {{ $t('exoplatform.wallet.info.initialFundsIntroduction', {0: contractDetails && contractDetails.name}) }}
    </v-card-title>
    <v-card-text>
      <v-text-field
        v-model="etherAmountLabel"
        :title="$t('exoplatform.wallet.message.etherInitialFunds')"
        :label="$t('exoplatform.wallet.label.etherInitialFunds')"
        :placeholder="$t('exoplatform.wallet.label.etherInitialFundsPlaceholder')"
        name="etherAmount"
        required
        readonly
        @input="$emit('amount-selected', amount)" />

      <v-text-field
        v-model.number="tokenAmount"
        :label="$t('exoplatform.wallet.label.tokenInitialFunds', {0: contractDetails && contractDetails.name})"
        :placeholder="$t('exoplatform.wallet.label.tokenInitialFundsPlaceholder')"
        :disabled="loading"
        name="tokenAmount"
        required
        @input="$emit('amount-selected', amount)" />

      <v-textarea
        id="requestMessage"
        v-model="requestMessage"
        :label="$t('exoplatform.wallet.label.initialFundsMessageInputPlaceholder')"
        :placeholder="$t('exoplatform.wallet.label.initialFundsMessageInputPlaceholder')"
        name="requestMessage"
        class="mb-0"
        rows="7"
        flat
        no-resize />
    </v-card-text>
    <v-card-actions>
      <v-spacer />
      <button class="ignore-vuetify-classes btn btn-primary mb-3" @click="save">
        {{ $t('exoplatform.wallet.button.save') }}
      </button>
      <v-spacer />
    </v-card-actions>
  </v-card>
</template>
<script>
export default {
  props: {
    loading: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    contractDetails: {
      type: Object,
      default: function() {
        return null;
      },
    },
    settings: {
      type: Object,
      default: function() {
        return null;
      },
    },
  },
  data() {
    return {
      etherAmount: 0,
      tokenAmount: 0,
      requestMessage: null,
      isLoadingSuggestions: false,
    };
  },
  computed: {
    etherAmountLabel() {
      return this.etherAmountInFiat ? `${this.etherAmount} (${this.etherAmountInFiat} ${window.walletSettings.fiatSymbol})` : this.etherAmount;
    },
    etherAmountInFiat() {
      return (this.etherAmount && this.walletUtils.etherToFiat(this.etherAmount)) || 0;
    },
    initialFunds() {
      return this.settings && this.settings.initialFunds;
    },
  },
  watch: {
    initialFunds() {
      this.reloadInitialFunds();
    },
    requestMessage() {
      this.reloadInitialFunds();
    },
  },
  methods: {
    reloadInitialFunds() {
      this.etherAmount = this.initialFunds && this.initialFunds.etherAmount || 0;
      this.tokenAmount = this.initialFunds && this.initialFunds.tokenAmount || 0;
      this.requestMessage = this.initialFunds && this.initialFunds.requestMessage || '';
    },
    save() {
      const initialFunds = {
        requestMessage: this.requestMessage,
        etherAmount: this.etherAmount,
        tokenAmount: this.tokenAmount,
      };

      this.loading = true;
      return fetch('/portal/rest/wallet/api/settings/saveInitialFunds', {
        method: 'POST',
        credentials: 'include',
        headers: {
          Accept: 'application/json',
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(initialFunds),
      })
        .then((resp) => {
          if (resp && resp.ok) {
            return resp.text();
          } else {
            throw new Error('Error saving settings');
          }
        })
        .then(() => this.$emit('saved', initialFunds))
        .catch((e) => {
          this.loading = false;
          console.debug('fetch settings - error', e);
          this.error = this.$t('exoplatform.wallet.warning.errorSavingSettings');
        });
    },
  },
};
</script>
