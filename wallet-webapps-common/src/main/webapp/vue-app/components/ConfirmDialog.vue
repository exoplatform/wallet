<template>
  <v-dialog
    v-model="dialog"
    :width="width"
    attach="#walletDialogsParent"
    content-class="uiPopup"
    max-width="100vw"
    hide-overlay
    @keydown.esc="close">
    <v-card class="elevation-12">
      <div class="ignore-vuetify-classes popupHeader ClearFix">
        <a
          class="uiIconClose pull-right"
          aria-hidden="true"
          @click="close"></a>
        <span class="ignore-vuetify-classes PopupTitle popupTitle">
          {{ title }}
        </span>
      </div>
      <!-- eslint-disable-next-line vue/no-v-html -->
      <v-card-text v-html="message" />
      <v-card-actions v-if="!hideActions">
        <v-spacer />
        <button
          :disabled="loading"
          :loading="loading"
          class="ignore-vuetify-classes btn btn-primary mr-2"
          @click="ok">
          {{ okLabel }}
        </button>
        <button
          :disabled="loading"
          :loading="loading"
          class="ignore-vuetify-classes btn ml-2"
          @click="close">
          {{ cancelLabel }}
        </button>
        <v-spacer />
      </v-card-actions>
    </v-card>
  </v-dialog>
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
    title: {
      type: String,
      default: function() {
        return null;
      },
    },
    message: {
      type: String,
      default: function() {
        return null;
      },
    },
    okLabel: {
      type: String,
      default: function() {
        return 'ok';
      },
    },
    cancelLabel: {
      type: String,
      default: function() {
        return 'Cancel';
      },
    },
    hideActions: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    width: {
      type: String,
      default: function() {
        return '290px';
      },
    },
  },
  data: () => ({
    dialog: false,
  }),
  watch: {
    dialog() {
      if (this.dialog) {
        this.$emit('dialog-opened');
      } else {
        this.$emit('dialog-closed');
      }
    },
  },
  methods: {
    ok() {
      this.$emit('ok');
      this.dialog = false;
    },
    open() {
      this.dialog = true;
    },
    close() {
      this.$emit('closed');
      this.dialog = false;
    },
  },
}
</script>